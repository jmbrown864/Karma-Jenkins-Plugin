package hudson.plugins.karma;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.Result;
import hudson.util.IOException2;
import hudson.util.NullStream;
import hudson.util.StreamTaskListener;

import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.StaplerProxy;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Build view extension by Karma plugin.
 *
 * As {@link KarmaCoverageObject}, it retains the overall coverage report.
 *
 * @author Kohsuke Kawaguchi
 */
public final class KarmaBuildAction extends KarmaCoverageObject<KarmaBuildAction> implements HealthReportingAction, StaplerProxy {
	
    public final AbstractBuild<?,?> owner;

    private transient WeakReference<KarmaCoverageReport> report;
    
    /**
     * The thresholds that applied when this build was built.
     */
    private final KarmaHealthReportThresholds thresholds;

    public KarmaBuildAction(AbstractBuild<?,?> owner, Rule rule, Ratio lineCoverage, Ratio statementCoverage, Ratio functionCoverage, Ratio branchCoverage, KarmaHealthReportThresholds thresholds) {
        this.owner = owner;
        this.line = lineCoverage;
        this.statement = statementCoverage;
        this.function = functionCoverage;
        this.branch = branchCoverage;
        this.thresholds = thresholds;
    }

    public String getDisplayName() {
        return Messages.KarmaBuildAction_DisplayName();
    }

    public String getIconFileName() {
        return "graph.gif";
    }

    public String getUrlName() {
        return "karma";
    }

    /**
     * Get the coverage {@link hudson.model.HealthReport}.
     *
     * @return The health report or <code>null</code> if health reporting is disabled.
     * @since 1.7
     */
    public HealthReport getBuildHealth() {
        if (thresholds == null) {
            // no thresholds => no report
            return null;
        }
        thresholds.ensureValid();
        int score = 100, percent;
        ArrayList<Localizable> reports = new ArrayList<Localizable>(5);
        if (line != null && thresholds.getMaxLine() > 0) {
            percent = line.getPercentage();
            if (percent < thresholds.getMaxLine()) {
                reports.add(Messages._KarmaBuildAction_Lines(line, percent));
            }
            score = updateHealthScore(score, thresholds.getMinLine(),
                                      percent, thresholds.getMaxLine());
        }
        if (statement != null && thresholds.getMaxStatement() > 0) {
            percent = statement.getPercentage();
            if (percent < thresholds.getMaxStatement()) {
                reports.add(Messages._KarmaBuildAction_Statementes(statement, percent));
            }
            score = updateHealthScore(score, thresholds.getMinStatement(),
                                      percent, thresholds.getMaxStatement());
        }
        if (function != null && thresholds.getMaxFunction() > 0) {
            percent = function.getPercentage();
            if (percent < thresholds.getMaxFunction()) {
                reports.add(Messages._KarmaBuildAction_Functions(function, percent));
            }
            score = updateHealthScore(score, thresholds.getMinFunction(),
                                      percent, thresholds.getMaxFunction());
        }
        if (branch != null && thresholds.getMaxBranch() > 0) {
            percent = branch.getPercentage();
            if (percent < thresholds.getMaxBranch()) {
                reports.add(Messages._KarmaBuildAction_Branchs(branch, percent));
            }
            score = updateHealthScore(score, thresholds.getMinBranch(),
                                      percent, thresholds.getMaxBranch());
        }
        if (score == 100) {
            reports.add(Messages._KarmaBuildAction_Perfect());
        }
        // Collect params and replace nulls with empty string
        Object[] args = reports.toArray(new Object[5]);
        for (int i = 4; i >= 0; i--) if (args[i]==null) args[i] = ""; else break;
        return new HealthReport(score, Messages._KarmaBuildAction_Description(
                args[0], args[1], args[2], args[3], args[4]));
    }

    private static int updateHealthScore(int score, int min, int value, int max) {
        if (value >= max) return score;
        if (value <= min) return 0;
        assert max != min;
        final int scaled = (int) (100.0 * ((float) value - min) / (max - min));
        if (scaled < score) return scaled;
        return score;
    }

    public Object getTarget() {
        return getResult();
    }

    @Override
    public AbstractBuild<?,?> getBuild() {
        return owner;
    }
    
    /**
     * 
     * @param file
     * @return path to file with report
     * @throws IOException
     * @throws InterruptedException
     */
	protected static File getKarmaReport(File file) throws IOException, InterruptedException {
		if (file.isDirectory()) {
			File report = null;
			File[] files = file.listFiles();
			for(int i = 0; i < files.length; i++){
				if (files[i].getName().equals("index.html")) {
					report = files[i];
					break;
				}
			}
			return report;
		} else {
			return file;
		}
	}

    /**
     * Obtains the detailed {@link KarmaCoverageReport} instance.
     */
    public synchronized KarmaCoverageReport getResult() {

        if(report!=null) {
            final KarmaCoverageReport r = report.get();
            if(r!=null)     return r;
        }

		// Generate the report
		KarmaCoverageReport r = new KarmaCoverageReport(this);

		report = new WeakReference<KarmaCoverageReport>(r);
		return r;
    }

    @Override
    public KarmaBuildAction getPreviousResult() {
        return getPreviousResult(owner);
    }

    /**
     * Gets the previous {@link KarmaBuildAction} of the given build.
     */
    /*package*/ static KarmaBuildAction getPreviousResult(AbstractBuild<?,?> start) {
        AbstractBuild<?,?> b = start;
        while(true) {
            b = b.getPreviousBuild();
            if(b==null)
                return null;
            if(b.getResult()== Result.FAILURE)
                continue;
            KarmaBuildAction r = b.getAction(KarmaBuildAction.class);
            if(r!=null)
                return r;
        }
    }

    /**
     * Constructs the object from karma XML report files.
     *
     * @throws IOException
     *      if failed to parse the file.
     * @throws InterruptedException 
     */
    public static KarmaBuildAction load(AbstractBuild<?,?> owner, Rule rule, KarmaHealthReportThresholds thresholds, FilePath... files) throws IOException, InterruptedException {
        Ratio ratios[] = null;
        for (FilePath f: files ) {
            InputStream in = f.read();
            try {
                ratios = loadRatios(in, ratios);
            } finally {
                in.close();
            }
        }
        return new KarmaBuildAction(owner,rule,ratios[0],ratios[1],ratios[2],ratios[3],thresholds);
    }

    public static KarmaBuildAction load(AbstractBuild<?,?> owner, Rule rule, KarmaHealthReportThresholds thresholds, InputStream... streams) throws IOException {
        Ratio ratios[] = null;
        for (InputStream in: streams) {
          ratios = loadRatios(in, ratios);
        }
        return new KarmaBuildAction(owner,rule,ratios[0],ratios[1],ratios[2],ratios[3],thresholds);
    }
    
    private static Ratio[] loadRatios(InputStream in, Ratio[] r) throws IOException {
    	
    	if (r == null || r.length < 4) {
    		r = new Ratio[4];
    	}
    	
    	BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    	
    	try {
    		String line = null;
    		String ratio = null;
    		while ((line = reader.readLine()) != null) {
    			
    			if (line.contains("<small>")) {
    				line = line.trim();

    				int firstParen = line.indexOf("(");
    				int secondParen = line.indexOf(")");

    				ratio = ratioFinder(line, firstParen + 1, secondParen);

    				String[] parts = ratio.split(" / ");
    				int numerator = 0;
    				int denominator = 0;
    				try {
    					numerator = Integer.parseInt(parts[0]);
    					denominator = Integer.parseInt(parts[1]);
    				} catch (NumberFormatException e) {
    					numerator = -1;
    					denominator = -1;
    				}
    				if (r[0] == null) {
    					r[0] = new Ratio((float) numerator, (float) denominator);
    				} else if (r[1] == null) {
    					r[1] = new Ratio((float) numerator, (float) denominator);
    				} else if (r[2] == null) {
    					r[2] = new Ratio((float) numerator, (float) denominator);
    				} else if (r[3] == null) {
    					r[3] = new Ratio((float) numerator, (float) denominator);
    					break;
    				}
    			}
    		}
    		reader.close();
    		
    	} finally {
    		reader.close();
    	}
    	return r;
    }
    
    private static String ratioFinder(String line, int start, int end) {
    	
    	String ratio = line.substring(start, end);
    	
    	if (!(ratio.matches("\\d+ / \\d+"))) {
    		return "Ratio does not match correct format!";
    	}
    	
    	return ratio;
    }

}
