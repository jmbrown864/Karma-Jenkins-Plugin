package hudson.plugins.karma;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * {@link Publisher} that captures Karma coverage reports.
 *
 * @author Kohsuke Kawaguchi
 */
public class KarmaPublisher extends Recorder {
    /**
     * Relative path to the Karma XML file inside the workspace.
     */
    public String includes;

    /**
     * Rule to be enforced. Can be null.
     *
     * TODO: define a configuration mechanism.
     */
    public Rule rule;

    /**
     * {@link hudson.model.HealthReport} thresholds to apply.
     */
    public KarmaHealthReportThresholds healthReports = new KarmaHealthReportThresholds();
    
    /**
     * look for coverage reports based in the configured parameter includes.
     * 'includes' is 
     *   - an Ant-style pattern
     *   - a list of files and folders separated by the characters ;:,  
     */
    protected static FilePath[] locateKarmaCoverageReports(FilePath workspace, String includes) throws IOException, InterruptedException {

    	// First use ant-style pattern
    	try {
        	FilePath[] ret = workspace.list(includes);
            if (ret.length > 0) { 
            	return ret;
            }
        } catch (Exception e) {
        	
        }

        // If it fails, do a legacy search
        ArrayList<FilePath> files = new ArrayList<FilePath>();
		String parts[] = includes.split("\\s*[;:,]+\\s*");
		for (String path : parts) {
			FilePath src = workspace.child(path);
			if (src.exists()) {
				if (src.isDirectory()) {
					files.addAll(Arrays.asList(src.list("index.html")));
				} else {
					files.add(src);
				}
			}
		}
		return files.toArray(new FilePath[files.size()]);

	}
	
    /**
     * save karma reports from the workspace to build folder  
     */
	protected static void saveKarmaCoverageReports(FilePath folder, FilePath[] files) throws IOException, InterruptedException {
		folder.mkdirs();
		for (int i = 0; i < files.length; i++) {
			String name = "coverage" + (i > 0 ? i : "") + ".xml";
			FilePath src = files[i];
			FilePath dst = folder.child(name);
			src.copyTo(dst);
		}
	}

    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        EnvVars env = build.getEnvironment(listener);
        env.overrideAll(build.getBuildVariables());
        
        includes = env.expand(includes);
        
        final PrintStream logger = listener.getLogger();
        logger.println("logger: " + logger);
        logger.println("listener: "+listener);
        FilePath[] reports;
        if (includes == null || includes.trim().length() == 0) {
            logger.println("Karma: looking for coverage reports in the entire workspace: " + build.getWorkspace().getRemote());
            reports = locateKarmaCoverageReports(build.getWorkspace(), "coverage");
        } else {
            logger.println("Karma: looking for coverage reports in the provided path: " + includes );
            reports = locateKarmaCoverageReports(build.getWorkspace(), includes);
        }
        
        if (reports.length == 0) {
            if(build.getResult().isWorseThan(Result.UNSTABLE))
                return true;
            
            logger.println("Karma: no coverage files found in workspace. Was any report generated?");
            build.setResult(Result.FAILURE);
            return true;
        } else {
        	String found = "";
        	for (FilePath f: reports) 
        		found += "\n          " + f.getRemote();
            logger.println("Karma: found " + reports.length  + " report files: " + found );
        }
        
        FilePath karmafolder = new FilePath(getKarmaReport(build));
        saveKarmaCoverageReports(karmafolder, reports);
        logger.println("Karma: stored " + reports.length + " report files in the build folder: "+ karmafolder);

        final KarmaBuildAction action = KarmaBuildAction.load(build, rule, healthReports, reports);
        
        logger.println("Karma: " + action.getBuildHealth().getDescription());

        build.getActions().add(action);

        final KarmaCoverageReport result = action.getResult();
        if (result == null) {
            logger.println("Karma: Could not parse coverage results. Setting Build to failure.");
            build.setResult(Result.FAILURE);
        } else if (result.isFailed()) {
            logger.println("Karma: code coverage enforcement failed. Setting Build to unstable.");
            build.setResult(Result.UNSTABLE);
        }

        return true;
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new KarmaProjectAction(project);
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    /**
     * Gets the directory to store report files
     */
    static File getKarmaReport(AbstractBuild<?,?> build) {
        return new File(build.getRootDir(), "karma");
    }

    @Override
    public BuildStepDescriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final BuildStepDescriptor<Publisher> DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public DescriptorImpl() {
            super(KarmaPublisher.class);
        }

        public String getDisplayName() {
            return Messages.KarmaPublisher_DisplayName();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject json) throws FormException {
            KarmaPublisher pub = new KarmaPublisher();
            req.bindParameters(pub, "karma.");
            req.bindParameters(pub.healthReports, "karmaHealthReports.");
			//set max defaults
            if ("".equals(req.getParameter("karmaHealthReports.maxLine"))) {
                pub.healthReports.setMaxLine(90);
            }
            if ("".equals(req.getParameter("karmaHealthReports.maxStatement"))) {
                pub.healthReports.setMaxStatement(80);
            }
            if ("".equals(req.getParameter("karmaHealthReports.maxFunction"))) {
                pub.healthReports.setMaxFunction(50);
            }
            if ("".equals(req.getParameter("karmaHealthReports.maxBranch"))) {
                pub.healthReports.setMaxBranch(50);
            }
            // end ugly hack
            return pub;
        }
    }
}
