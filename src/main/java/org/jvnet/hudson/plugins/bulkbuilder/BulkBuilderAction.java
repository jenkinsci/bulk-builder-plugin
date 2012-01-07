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

package org.jvnet.hudson.plugins.bulkbuilder;

import hudson.Extension;
import hudson.model.RootAction;
import hudson.model.Hudson;
import hudson.model.View;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.jvnet.hudson.plugins.bulkbuilder.model.BuildAction;
import org.jvnet.hudson.plugins.bulkbuilder.model.BuildHistory;
import org.jvnet.hudson.plugins.bulkbuilder.model.BuildHistoryItem;
import org.jvnet.hudson.plugins.bulkbuilder.model.BuildType;
import org.jvnet.hudson.plugins.bulkbuilder.model.Builder;
import org.jvnet.hudson.plugins.bulkbuilder.model.BulkParamProcessor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
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

        BuildAction action = BuildAction.valueOf(buildAction.toUpperCase());
        BuildType type = BuildType.valueOf(buildType.toUpperCase());

        // TODO
        String params = req.getParameter("params");
        BulkParamProcessor processor = new BulkParamProcessor(params);

        Builder builder = new Builder(action);

        String pattern = req.getParameter("pattern");
        if (pattern != null && !pattern.isEmpty()) {
            builder.setPattern(pattern);
            BuildHistory history = Hudson.getInstance().getPlugin(
                BuildHistory.class);
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
        return Hudson.getInstance().getQueue().getItems().length;
    }

    /**
     * Gets the build pattern history
     *
     * @return
     */
    @Exported
    public final List<BuildHistoryItem> getHistory() {
        return Hudson.getInstance().getPlugin(BuildHistory.class).getAll();
    }

    /**
     * Get all available {@link hudson.model.View} for drop down box.
     *
     * @return the views
     */
    public final Collection<View> getViews() {
        return Hudson.getInstance().getViews();
    }
}
