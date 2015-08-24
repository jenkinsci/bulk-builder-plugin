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

package org.jenkinsci.plugins.bulkbuilder;

import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.model.ListView;
import hudson.model.TreeView;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.For;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;


/**
 * @author simon
 */
@For(BulkBuilderAction.class)
public class BulkBuilderActionTest {
    public static final int NO_OF_ACTION_RADIO_BUTTONS = 2;
    public static final int NO_OF_TYPE_RADIO_BUTTONS = 8;
    private BulkBuilderAction action;

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Before
    public void setUp() throws Exception {
        action = new BulkBuilderAction();
    }

    @Test
    public void testGetIconFileName() {
        assertEquals("/plugin/bulk-builder/icons/builder-32x32.png", action.getIconFileName());
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Bulk Builder", action.getDisplayName());
    }

    @Test
    public void testGetUrlName() {
        assertEquals("/bulkbuilder", action.getUrlName());
    }

    @Test
    public void testFormElementsPresent() throws Exception {
        HtmlPage page = jenkinsRule.createWebClient().goTo("bulkbuilder");
        HtmlForm form = page.getFormByName("builder");

        assertEquals("build", form.getActionAttribute());
        assertEquals("post", form.getMethodAttribute());

        List<HtmlRadioButtonInput> buildActionRadioButtons = form.getRadioButtonsByName("action");
        assertEquals(NO_OF_ACTION_RADIO_BUTTONS, buildActionRadioButtons.size());
        List<HtmlRadioButtonInput> buildTypeRadioButtons = form.getRadioButtonsByName("build");
        assertEquals(NO_OF_TYPE_RADIO_BUTTONS, buildTypeRadioButtons.size());

        form.getInputByName("pattern");
    }

    @Ignore("HTMLUnit is struggling to find submit button")
    @Test
    public void testFormSubmitBuildAll() throws Exception {
        HtmlPage page = jenkinsRule.createWebClient().goTo("bulkbuilder");
        HtmlForm form = page.getFormByName("builder");

        List<HtmlRadioButtonInput> radioButtons = form.getRadioButtonsByName("build");
        for (HtmlRadioButtonInput radioButton : radioButtons) {
            if (radioButton.getValueAttribute().equalsIgnoreCase("all")) {
                radioButton.setChecked(true);
            }
        }
        HtmlButton submitButton = form.getButtonByCaption("Build!");
        jenkinsRule.createFreeStyleProject("project1");
        jenkinsRule.createFreeStyleProject("project2");

        submitButton.click();
        assertEquals(2, action.getQueueSize());
    }

    @Test
    public void testGetQueueSizeZeroWhenEmpty() {
        assertEquals(0, action.getQueueSize());
    }

    @Test
    public void testGetQueueSizeWithOneJob() throws IOException {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("project1");
        project.scheduleBuild(new Cause.UserIdCause());
        assertEquals(1, action.getQueueSize());
    }

    @Test
    public void testGetQueueSizeWithTwoJobs() throws IOException {
        FreeStyleProject project1 = jenkinsRule.createFreeStyleProject("project1");
        FreeStyleProject project2 = jenkinsRule.createFreeStyleProject("project2");
        project1.scheduleBuild(new Cause.UserIdCause());
        project2.scheduleBuild(new Cause.UserIdCause());
        assertEquals(2, action.getQueueSize());
    }

    @Test
    public void testGetViews() throws IOException {
        ListView view1 = new ListView("View 1");
        Jenkins.getInstance().addView(view1);
        TreeView treeView = new TreeView("tree View");
        Jenkins.getInstance().addView(treeView);
        final ListView view2 = new ListView("View 2", treeView);
        Jenkins.getInstance().addView(view2);
        // On the view list, "ALL" item is always visible
        assertEquals(4, action.getViews().size());
    }
}
