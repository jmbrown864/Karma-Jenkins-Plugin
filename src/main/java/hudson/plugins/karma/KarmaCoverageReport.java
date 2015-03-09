package hudson.plugins.karma;

import hudson.model.AbstractBuild;
import hudson.util.IOException2;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Root object of the coverage report.
 * 
 * @author Kohsuke Kawaguchi
 */
public final class KarmaCoverageReport extends KarmaCoverageObject<KarmaCoverageReport> {
    private final KarmaBuildAction action;
	
	private String name;
	
	public KarmaCoverageReport(KarmaBuildAction action) {
        this.action = action;
		this.line = action.line;
		this.statement = action.statement;
		this.function = action.function;
		this.branch = action.branch;
		
        setName("Karmakarma");
    }
	
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return name;
    }

    @Override
    public KarmaCoverageReport getPreviousResult() {
        KarmaBuildAction prev = action.getPreviousResult();
        if(prev!=null)
            return prev.getResult();
        else
            return null;
    }

    @Override
    public AbstractBuild<?,?> getBuild() {
        return action.owner;
    }

}
