package org.jvnet.hudson.plugins.bulkbuilder;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import java.io.IOException;
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

    @Ignore("intermittently failing with 404 error")
    @Test
    public void testBulkBuilderForm() throws Exception {
        HtmlPage page = new WebClient().goTo("/bulkbuilder");

        assertStringContains(page.asXml(), "Build All Jobs");

        HtmlForm form = page.getFormByName("buildAll");
        System.out.println(form.asXml());
        assertEquals("buildAll", form.getActionAttribute());
        assertEquals("post", form.getMethodAttribute());
    }

    @Test
    public void atestDoBuildAll() {
        // TODO complete test
    }

    @Test
    public void testDoBuildFailed() throws Exception {
//        FreeStyleProject project = createFreeStyleProject("project1");
//        project.getBuildersList().add(new TestBuilder() {

//            @Override
//            public boolean perform(AbstractBuild<?, ?> ab, Launcher lnchr, BuildListener bl) throws InterruptedException, IOException {
//                return false;
//            }
//        });
        //project.scheduleBuild2(0);

//        HtmlPage page = new WebClient().goTo("/bulkbuilder/buildFailed");

 //       assertEquals(1, action.getQueueSize());
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
