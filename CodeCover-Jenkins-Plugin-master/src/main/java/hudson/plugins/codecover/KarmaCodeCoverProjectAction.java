package hudson.plugins.codecover;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Result;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;

/**
 * Project view extension by KarmaCodeCover plugin.
 * 
 * @author Kohsuke Kawaguchi
 */
public final class KarmaCodeCoverProjectAction implements Action {
    public final AbstractProject<?,?> project;

    public KarmaCodeCoverProjectAction(AbstractProject project) {
        this.project = project;
    }

    public String getIconFileName() {
        return "graph.gif";
    }

    public String getDisplayName() {
        return Messages.ProjectAction_DisplayName();
    }

    public String getUrlName() {
        return "codecover";
    }

    /**
     * Gets the most recent {@link KarmaCodeCoverBuildAction} object.
     */
    public KarmaCodeCoverBuildAction getLastResult() {
        for( AbstractBuild<?,?> b = project.getLastBuild(); b!=null; b=b.getPreviousBuild()) {
            if(b.getResult()== Result.FAILURE)
                continue;
            KarmaCodeCoverBuildAction r = b.getAction(KarmaCodeCoverBuildAction.class);
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
