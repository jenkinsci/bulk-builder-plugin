/*
 * The MIT License
 *
 * Copyright (c) 2010-2011 Simon Westcott, Jesse Farinacci
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.bulkbuilder;

import hudson.Extension;
import hudson.model.RootAction;
import hudson.model.View;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;

import hudson.model.ViewGroup;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.bulkbuilder.model.*;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Manage bulk build/SCM poll for your jobs.
 *
 * @author simon
 * @author <a href="mailto:jieryn@gmail.com">Jesse Farinacci</a>
 */
@ExportedBean
@Extension
public class BulkBuilderAction implements RootAction {

    private static final Logger LOGGER = Logger
       .getLogger(BulkBuilderAction.class.getName());

    public final String getIconFileName() {
        return "/plugin/bulk-builder/icons/builder-32x32.png";
    }

    public final String getDisplayName() {
        return Messages.Plugin_Title();
    }

    public final String getUrlName() {
        return "/bulkbuilder";
    }

    public final void doBuild(StaplerRequest req, StaplerResponse rsp)
        throws ServletException, IOException {
        if (LOGGER.isLoggable(Level.FINE)) {
           LOGGER.log(Level.FINE, "doBuild action called");
        }

        String buildAction = req.getParameter("action");
        if (buildAction == null) {
            rsp.forwardToPreviousPage(req);
            return;
        }

        String buildType = req.getParameter("build");
        if (buildType == null) {
            rsp.forwardToPreviousPage(req);
            return;
        }

        BuildAction action;
        BuildType type;

        try {
            action = BuildAction.valueOf(buildAction.toUpperCase());
            type = BuildType.valueOf(buildType.toUpperCase());
        } catch (IllegalArgumentException e) {
            rsp.forwardToPreviousPage(req);
            return;
        }

        Builder builder = new Builder(action);

        String params = req.getParameter("params");
        BulkParamProcessor processor = new BulkParamProcessor(params);
        Map<String, String> projectParams = processor.getProjectParams();

        String paramBuild = req.getParameter("paramBuild");
        if (paramBuild != null && !paramBuild.isEmpty() && !projectParams.isEmpty()) {
            builder.setUserParams(projectParams);
        }

        String pattern = req.getParameter("pattern");
        if (pattern != null && !pattern.isEmpty()) {
            builder.setPattern(pattern);
            BuildHistory history = Jenkins.getInstance().getPlugin(BuildHistory.class);
            history.add(new BuildHistoryItem(pattern));
        }

        String view = req.getParameter("view");
        if (view != null && !view.isEmpty()) {
            builder.setView(view);
        }

        switch (type) {
            case ABORTED:
                builder.buildAborted();
                break;
            case ALL:
                builder.buildAll();
                break;
            case FAILED:
                builder.buildFailed();
                break;
            case FAILED_ONLY:
                builder.buildFailedOnly();
                break;
            case NOT_BUILD_ONLY:
                builder.buildNotBuildOnly();
                break;
            case NOT_BUILT:
                builder.buildNotBuilt();
                break;
            case UNSTABLE:
                builder.buildUnstable();
                break;
            case UNSTABLE_ONLY:
                builder.buildUnstableOnly();
                break;
        }

        rsp.forwardToPreviousPage(req);
    }

    /**
     * Gets the number projects in the build queue
     *
     * @return
     */
    @Exported
    public final int getQueueSize() {
        return Jenkins.getInstance().getQueue().getItems().length;
    }

    /**
     * Gets the build pattern history
     *
     * @return
     */
    @Exported
    public final List<BuildHistoryItem> getHistory() {
        return Jenkins.getInstance().getPlugin(BuildHistory.class).getAll();
    }

    /**
     * Get all available {@link View} for drop down box, including nested views.
     *
     * @return the views
     */
    public final Collection<View> getViews() {
        Collection<View> fullList = new ArrayList<View>();
        Collection<View> parentViews = Jenkins.getInstance().getViews();
        addViews(fullList, parentViews);
        return fullList;
    }

    /**
     * Recurrent search for all nested views
     *
     * @return
     */
    private void addViews(Collection<View> fullList, Collection<View> parentViews) {
        for (View parentView : parentViews) {
            fullList.add(parentView);
            if(parentView instanceof ViewGroup) {
                addViews(fullList, ((ViewGroup) parentView).getViews());
            }
        }
    }
}
