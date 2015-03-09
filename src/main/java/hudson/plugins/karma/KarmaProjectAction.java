package hudson.plugins.karma;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Result;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;

/**
 * Project view extension by Karma plugin.
 * 
 * @author Kohsuke Kawaguchi
 */
public final class KarmaProjectAction implements Action {
    public final AbstractProject<?,?> project;

    public KarmaProjectAction(AbstractProject project) {
        this.project = project;
    }

    public String getIconFileName() {
        return "graph.gif";
    }

    public String getDisplayName() {
        return Messages.KarmaProjectAction_DisplayName();
    }

    public String getUrlName() {
        return "karma";
    }

    /**
     * Gets the most recent {@link KarmaBuildAction} object.
     */
    public KarmaBuildAction getLastResult() {
        for( AbstractBuild<?,?> b = project.getLastBuild(); b!=null; b=b.getPreviousBuild()) {
            if(b.getResult()== Result.FAILURE)
                continue;
            KarmaBuildAction r = b.getAction(KarmaBuildAction.class);
            if(r!=null)
                return r;
        }
        return null;
    }

    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
       if (getLastResult() != null)
          getLastResult().doGraph(req,rsp);
    }
}
