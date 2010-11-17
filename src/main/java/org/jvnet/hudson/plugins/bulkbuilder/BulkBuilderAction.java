/*
 * The MIT License
 *
 * Copyright (c) 2010 Simon Westcott
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
import hudson.model.Hudson;
import hudson.model.RootAction;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import org.jvnet.hudson.plugins.bulkbuilder.model.Builder;
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

    public final String getIconFileName() {
        return "/plugin/bulk-builder/icons/builder-32x32.png";
    }

    public final String getDisplayName() {
        return "Bulk Builder";
    }

    public final String getUrlName() {
        return "/bulkbuilder";
    }

    //Computer[] computers = Hudson.getInstance().getComputers();
    //for (Computer c : computers) {
    //    c.countBusy();
    //}

    public final void doBuild(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException {
        LOGGER.log(Level.FINE, "doBuild action called");
        String build = req.getParameter("build");

        Builder builder = new Builder();

        if (build.equalsIgnoreCase("all")) {
            builder.buildAll();
        } else if (build.equalsIgnoreCase("failed")) {
            builder.buildFailed();
        } else if (build.equalsIgnoreCase("pattern")) {
            builder.buildPattern(req.getParameter("pattern"));
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
}
