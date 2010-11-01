package org.jvnet.hudson.plugins;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.model.RootAction;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * @author simon
 */
@ExportedBean(defaultVisibility = 999)
@Extension
public class BulkBuilderAction implements RootAction {

    private static final Logger LOGGER = Logger.getLogger(BulkBuilderAction.class.getName());

    public String getIconFileName() {
        return "/plugin/bulk-builder/icons/builder-32x32.png";
    }

    public String getDisplayName() {
        return "Bulk Builder";
    }

    public String getUrlName() {
        return "/bulkbuilder";
    }

    /**
     * Schedule build of all projects
     *
     * @param req
     * @param rsp
     * @throws IOException
     * @throws ServletException
     */
    public void doBuildAll(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        LOGGER.log(Level.FINE, "doBuildAll action called");

        for (AbstractProject project : getProjects()) {
            LOGGER.log(Level.INFO, "Scheduling build for job: {0}", project.getDisplayName());
            project.scheduleBuild(new Cause.UserCause());
        }


        //Computer[] computers = Hudson.getInstance().getComputers();
        //for (Computer c : computers) {
        //    c.countBusy();
        //}

        rsp.forwardToPreviousPage(req);
    }

    /**
     * Schedule build of all projects where the last build was not successful
     *
     * @param req
     * @param rsp
     * @throws IOException
     * @throws ServletException
     */
    public void doBuildFailed(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        LOGGER.log(Level.FINE, "doBuildFailed action called");

        for (AbstractProject project : getProjects()) {
            Run build = project.getLastCompletedBuild();

            if (build == null || build.getResult().isWorseOrEqualTo(Result.FAILURE)) {
                LOGGER.log(Level.INFO, "Scheduling build for job: {0}", project.getDisplayName());
                project.scheduleBuild(new Cause.UserCause());
            }
        }

        rsp.forwardToPreviousPage(req);
    }

    /**
     * Get list a of projects that can be built
     *
     * @return
     */
    private List<AbstractProject> getProjects() {
        List<AbstractProject> projects = new ArrayList<AbstractProject>();

        List<TopLevelItem> topLevelItems = Hudson.getInstance().getItems();
        for (TopLevelItem topLevelItem : topLevelItems) {
            if (!(topLevelItem instanceof AbstractProject)) {
                continue;
            }

            AbstractProject project = (AbstractProject) topLevelItem;
            if (!project.isBuildable()) {
                continue;
            }

            projects.add(project);
        }

        return projects;
    }

    /**
     * Gets the number projects in the build queue
     *
     * @return
     */
    @Exported
    public int getQueueSize() {
        return Hudson.getInstance().getQueue().getItems().length;
    }
}
