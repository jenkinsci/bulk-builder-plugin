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

package org.jvnet.hudson.plugins.bulkbuilder.model;

import org.junit.Before;
import hudson.model.FreeStyleProject;
import org.jvnet.hudson.test.HudsonTestCase;
import org.junit.Test;
import org.jvnet.hudson.test.FailureBuilder;
import org.jvnet.hudson.test.UnstableBuilder;

/**
 * @author simon
 */
public class BuilderTest extends HudsonTestCase {

    private Builder builder;

    // Successful
    private FreeStyleProject project1;
    // Failure
    private FreeStyleProject project2;
    // Unstable
    private FreeStyleProject project3;
    // Not Built
    private FreeStyleProject project4;
    // Disabled
    private FreeStyleProject project5;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        project1 = createFreeStyleProject("success");
        project1.scheduleBuild2(0).get();

        project2 = createFreeStyleProject("fail");
        project2.getBuildersList().add(new FailureBuilder());
        project2.scheduleBuild2(0);

        project3 = createFreeStyleProject("unstable");
        project3.getBuildersList().add(new UnstableBuilder());
        project3.scheduleBuild2(0);

        project4 = createFreeStyleProject("not built");

        project5 = createFreeStyleProject("disabled");
        project5.disable();

        builder = new Builder();
    }

    /**
     * Test of buildAll method, of class Builder.
     */
    @Test
    public void testBuildAll() throws Exception{
        assertEquals(4, builder.buildAll());
    }

    /**
     * Test of buildFailed method, of class Builder.
     */
    @Test
    public void testBuildFailed() {
        assertEquals(3, builder.buildFailed());
    }

    /**
     * Test of buildPattern method, of class Builder.
     */
    @Test
    public void testBuildPattern() {
        assertEquals(1, builder.buildPattern("stable"));
        assertEquals(3, builder.buildPattern("l"));
    }
}
