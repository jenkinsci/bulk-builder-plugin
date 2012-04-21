/*
 * The MIT License
 *
 * Copyright (c) 2010-2011 Simon Westcott
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

package org.jenkinsci.plugins.bulkbuilder.model;

import hudson.model.FreeStyleProject;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.recipes.PresetData;
import org.jvnet.hudson.test.recipes.PresetData.DataSet;

/**
 * @author simon
 */
public class BuilderTest2 extends HudsonTestCase {

    /**
     * Test user has necessary permission to build job.
     */
    @Test
    @PresetData(DataSet.ANONYMOUS_READONLY)
    public void testInsufficientBuildPermission() throws Exception {
        FreeStyleProject project = createFreeStyleProject("restricted");
        project.scheduleBuild2(0).get();
        waitUntilNoActivity();

        Builder builder = new Builder(BuildAction.valueOf("IMMEDIATE_BUILD"));
        builder.setPattern("restricted");
        assertEquals(0, builder.buildAll());
        assertEquals(1, project.getNextBuildNumber());
    }
}
