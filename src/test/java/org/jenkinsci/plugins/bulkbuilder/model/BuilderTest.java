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

import hudson.model.BooleanParameterDefinition;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.FailureBuilder;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.UnstableBuilder;
import org.jvnet.hudson.test.recipes.PresetData;
import org.jvnet.hudson.test.recipes.PresetData.DataSet;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author simon
 */
public class BuilderTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();
    private Builder builder;

    // Successful
    private FreeStyleProject project1;
    private int project1NextBuildNumber;

    // Failure
    private FreeStyleProject project2;
    private int project2NextBuildNumber;

    // Unstable
    private FreeStyleProject project3;
    private int project3NextBuildNumber;

    // Not Built
    private FreeStyleProject project4;
    private int project4NextBuildNumber;

    // Disabled
    private FreeStyleProject project5;
    private int project5NextBuildNumber;

    @After
    public void tearDown() throws Exception {
        builder = null;
        project1 = project2 = project3 = project4 = project5 = null;
        project1NextBuildNumber = project2NextBuildNumber = project3NextBuildNumber = project4NextBuildNumber = project5NextBuildNumber = -1;
    }

    private void setUpBasicJobs() throws Exception {
        project1 = jenkinsRule.createFreeStyleProject("success");
        project1.scheduleBuild2(0).get();

        project2 = jenkinsRule.createFreeStyleProject("fail");
        project2.getBuildersList().add(new FailureBuilder());
        project2.scheduleBuild2(0).get();

        project3 = jenkinsRule.createFreeStyleProject("unstable");
        project3.getBuildersList().add(new UnstableBuilder());
        project3.scheduleBuild2(0).get();

        project4 = jenkinsRule.createFreeStyleProject("not built");

        project5 = jenkinsRule.createFreeStyleProject("disabled");
        project5.disable();

        jenkinsRule.waitUntilNoActivity();
        builder = new Builder(BuildAction.valueOf("IMMEDIATE_BUILD"));

        project1NextBuildNumber = project1.getNextBuildNumber();
        project2NextBuildNumber = project2.getNextBuildNumber();
        project3NextBuildNumber = project3.getNextBuildNumber();
        project4NextBuildNumber = project4.getNextBuildNumber();
        project5NextBuildNumber = project5.getNextBuildNumber();
    }

    /**
     * Test of buildAll method.
     */
    @Test
    public void testBuildAll() throws Exception {
        setUpBasicJobs();
        assertEquals(4, builder.buildAll());
        jenkinsRule.waitUntilNoActivity();

        assertEquals(project1NextBuildNumber, project1.getLastBuild().getNumber());
        assertEquals(project2NextBuildNumber, project2.getLastBuild().getNumber());
        assertEquals(project3NextBuildNumber, project3.getLastBuild().getNumber());
        assertEquals(project4NextBuildNumber, project4.getLastBuild().getNumber());
        assertNull(project5.getLastBuild());
    }

    /**
     * Test of buildFailed method.
     */
    @Test
    public void testBuildFailed() throws Exception {
        setUpBasicJobs();
        assertEquals(2, builder.buildFailed());
        jenkinsRule.waitUntilNoActivity();

        assertEquals(project1NextBuildNumber, project1.getNextBuildNumber());
        assertEquals(project2NextBuildNumber, project2.getLastBuild().getNumber());
        assertEquals(project3NextBuildNumber, project3.getNextBuildNumber());
        assertEquals(project4NextBuildNumber, project4.getLastBuild().getNumber());
        assertNull(project5.getLastBuild());
    }

    /**
     * Test of buildUnstableOnly method.
     */
    @Test
    public void testBuildUnstableOnly() throws Exception {
        setUpBasicJobs();
        assertEquals(1, builder.buildUnstableOnly());
        jenkinsRule.waitUntilNoActivity();

        assertEquals(project1NextBuildNumber, project1.getNextBuildNumber());
        assertEquals(project2NextBuildNumber, project2.getNextBuildNumber());
        assertEquals(project3NextBuildNumber, project3.getLastBuild().getNumber());
        assertEquals(project4NextBuildNumber, project4.getNextBuildNumber());
        assertNull(project5.getLastBuild());
    }

    /**
     * Test of buildPattern method.
     */
    @Test
    public void testBuildPattern() throws Exception {
        setUpBasicJobs();
        builder.setPattern("*a*");
        assertEquals(2, builder.buildAll());
        jenkinsRule.waitUntilNoActivity();

        assertEquals(project1NextBuildNumber, project1.getNextBuildNumber());
        assertEquals(project2NextBuildNumber, project2.getLastBuild().getNumber());
        assertEquals(project3NextBuildNumber, project3.getLastBuild().getNumber());
        assertEquals(project4NextBuildNumber, project4.getNextBuildNumber());
        assertNull(project5.getLastBuild());
    }

    /**
     * Test user has necessary permission to build job.
     */
//    @Test
    @Ignore("PresetData isn't working correctly")
    @PresetData(DataSet.ANONYMOUS_READONLY)
    public void testInsufficientBuildPermission() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("restricted");
        project.scheduleBuild2(0).get();
        jenkinsRule.waitUntilNoActivity();

        builder = new Builder(BuildAction.valueOf("IMMEDIATE_BUILD"));
        builder.setPattern("restricted");
        assertEquals(0, builder.buildAll());
        assertEquals(1, project.getNextBuildNumber());
    }

    /**
     * Test parameterized job is passed user supplied string parameters
     * and uses default value for non-string parameters.
     */
    @Test
    public void testParameterisedBuildWithUserSuppliedParameter() throws Exception {
        FreeStyleProject paramJob = jenkinsRule.createFreeStyleProject("paramJob");
        StringParameterDefinition spd = new StringParameterDefinition("foo", "bar");
        ParametersDefinitionProperty spdp = new ParametersDefinitionProperty(spd);
        paramJob.addProperty(spdp);

        BooleanParameterDefinition bpd = new BooleanParameterDefinition("foo2", true, "Test");
        ParametersDefinitionProperty bpdp = new ParametersDefinitionProperty(bpd);
        paramJob.addProperty(bpdp);
        jenkinsRule.waitUntilNoActivity();

        BulkParamProcessor processor = new BulkParamProcessor("foo=baz");
        builder = new Builder(BuildAction.valueOf("IMMEDIATE_BUILD"));
        builder.setPattern("paramJob");
        builder.setUserParams(processor.getProjectParams());
        assertEquals(1, builder.buildAll());
        jenkinsRule.waitUntilNoActivity();

        Map<String, String> buildVariables = paramJob.getLastBuild().getBuildVariables();
        assertEquals("baz", buildVariables.get("foo"));
    }

    /**
     * Test parameterized job uses default value when no parameters specified by user
     */
    @Test
    public void testParameterisedBuildWithNoUserSuppliedParameter() throws Exception {
        FreeStyleProject paramJob = jenkinsRule.createFreeStyleProject("paramJob");
        StringParameterDefinition spd = new StringParameterDefinition("foo", "bar");
        ParametersDefinitionProperty pdp = new ParametersDefinitionProperty(spd);
        paramJob.addProperty(pdp);
        jenkinsRule.waitUntilNoActivity();

        builder = new Builder(BuildAction.valueOf("IMMEDIATE_BUILD"));
        builder.setPattern("paramJob");
        assertEquals(1, builder.buildAll());
        jenkinsRule.waitUntilNoActivity();

        Map<String, String> buildVariables = paramJob.getLastBuild().getBuildVariables();
        assertEquals("bar", buildVariables.get("foo"));
    }
}
