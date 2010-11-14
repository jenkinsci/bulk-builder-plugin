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

package org.jvnet.hudson.plugins.bulkbuilder.model;

import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author simon
 */
public class Builder {

    private static final Logger LOGGER = Logger.getLogger(Builder.class.getName());

    /**
     * Build all Hudson projects
     */
    public void buildAll() {

        LOGGER.log(Level.FINE, "Starting to build all jobs.");

        for (AbstractProject project : getProjects()) {
            project.scheduleBuild(new Cause.UserCause());
        }

        LOGGER.log(Level.FINE, "Finished building all jobs.");
    }

    /**
     * Build failed Hudson projects.
     *
     * This includes projects that have not been built before and failed and
     * aborted projects.
     */
    public void buildFailed() {

        LOGGER.log(Level.FINE, "Starting to build failed jobs.");

        for (AbstractProject project : getProjects()) {
            Run build = project.getLastCompletedBuild();

            if (build == null || build.getResult().isWorseOrEqualTo(Result.FAILURE)) {
                LOGGER.log(Level.FINE, "Scheduling build for job: {0}", project.getDisplayName());
                project.scheduleBuild(new Cause.UserCause());
            }
        }

        LOGGER.log(Level.FINE, "Finished building failed jobs.");
    }

    /**
     * Build projects that matched the supplied pattern
     *
     * @param pattern
     */
    public void buildPattern(String pattern) {

        LOGGER.log(Level.FINE, "Starting to jobs matching pattern, '{0}'.", pattern);

        for (AbstractProject project : getProjects()) {
            if (project.getDisplayName().contains(pattern)) {
                project.scheduleBuild(new Cause.UserCause());
            }
        }

        LOGGER.log(Level.FINE, "Finished building jobs matching pattern.");
    }

    /**
     * Return a list of projects which can be built
     * 
     * @return
     */
    protected List<AbstractProject> getProjects() {
        
        List<AbstractProject> projects = new ArrayList<AbstractProject>();

        for (TopLevelItem topLevelItem : Hudson.getInstance().getItems()) {
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
}
