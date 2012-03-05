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

import org.jenkinsci.plugins.bulkbuilder.BulkBuilderAction;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import java.io.IOException;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hudson.test.For;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 * @author simon
 */
@For(BulkBuilderAction.class)
public class BulkBuilderActionTest extends HudsonTestCase
{
    private BulkBuilderAction action;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
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
        HtmlPage page = new WebClient().goTo("/bulkbuilder");

        // form
        HtmlForm form = page.getFormByName("builder");
        assertEquals("build", form.getActionAttribute());
        assertEquals("post", form.getMethodAttribute());


        List<HtmlRadioButtonInput> buildActionRadioButtons = form.getRadioButtonsByName("action");
        assertEquals(2, buildActionRadioButtons.size());

        List<HtmlRadioButtonInput> buildTypeRadioButtons = form.getRadioButtonsByName("build");
        assertEquals(8, buildTypeRadioButtons.size());

        // text box
        form.getInputByName("pattern");
    }

    @Ignore("HTMLUnit is struggling to find submit button") @Test
    public void atestFormSubmitBuildAll() throws Exception {
        HtmlPage page = new WebClient().goTo("/bulkbuilder");

        HtmlForm form = page.getFormByName("builder");

        List<HtmlRadioButtonInput> radioButtons = form.getRadioButtonsByName("build");
        for (HtmlRadioButtonInput radioButton : radioButtons) {
            if (radioButton.getValueAttribute().equalsIgnoreCase("all")) {
                radioButton.setChecked(true);
            }
        }

        HtmlButton submitButton = form.getButtonByCaption("Build!");

        FreeStyleProject project1 = createFreeStyleProject("project1");
        FreeStyleProject project2 = createFreeStyleProject("project2");

        // Click that button!
        Page click = submitButton.click();

        assertEquals(2, action.getQueueSize());
    }

    @Test
    public void testGetQueueSizeZeroWhenEmpty() {
        assertEquals(0, action.getQueueSize());
    }

    @Test
    public void testGetQueueSizeWithOneJob() throws IOException {
        FreeStyleProject project = createFreeStyleProject("project1");
        project.scheduleBuild(new Cause.UserCause());
        assertEquals(1, action.getQueueSize());
    }

    @Test
    public void testGetQueueSizeWithTwoJobs() throws IOException {
        FreeStyleProject project1 = createFreeStyleProject("project1");
        FreeStyleProject project2 = createFreeStyleProject("project2");
        project1.scheduleBuild(new Cause.UserCause());
        project2.scheduleBuild(new Cause.UserCause());
        assertEquals(2, action.getQueueSize());
    }
}
