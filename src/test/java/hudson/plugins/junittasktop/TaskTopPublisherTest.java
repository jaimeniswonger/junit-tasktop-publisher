package hudson.plugins.junittasktop;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.Serializable;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.ExtractResourceSCM;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.Builder;
import hudson.tasks.junit.ClassResult;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.tasks.junit.TestDataPublisher;
import hudson.tasks.junit.TestResultAction;
import hudson.util.DescribableList;

public class TaskTopPublisherTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    // Package name used in tests in workspace2.zip
    private static final String TEST_PACKAGE = "com.example.test";

    @Test
    public void testBasic() throws Exception {
        TestResultAction action = getTestResultActionForBuild("workspace.zip", Result.SUCCESS);

        ClassResult cr = getClassResult(action, "test.foo.bar", "DefaultIntegrationTest");
        assertNotNull(cr);
    }

    static ClassResult getClassResult(TestResultAction action, String className) {
        return getClassResult(action, TEST_PACKAGE, className);
    }

    static ClassResult getClassResult(TestResultAction action, String packageName, String className) {
        return action.getResult().byPackage(packageName).getClassResult(className);
    }

    private FreeStyleBuild getBuild(String workspaceZip) throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();

        DescribableList<TestDataPublisher, Descriptor<TestDataPublisher>> publishers =
            new DescribableList<TestDataPublisher, Descriptor<TestDataPublisher>>(project);
        publishers.add(new TaskTopPublisher());

        project.setScm(new ExtractResourceSCM(getClass().getResource(workspaceZip)));
        project.getBuildersList().add(new TouchBuilder());
        JUnitResultArchiver archiver = new JUnitResultArchiver("*.xml");
        archiver.setTestDataPublishers(publishers);
        project.getPublishersList().add(archiver);

        return project.scheduleBuild2(0).get();
    }

    // Creates a job from the given workspace zip file, builds it and retrieves the TestResultAction
    private TestResultAction getTestResultActionForBuild(String workspaceZip, Result expectedStatus) throws Exception {
        FreeStyleBuild b = getBuild(workspaceZip);
        j.assertBuildStatus(expectedStatus, b);

        TestResultAction action = b.getAction(TestResultAction.class);
        assertNotNull(action);

        return action;
    }

    public static final class TouchBuilder extends Builder implements Serializable {
        @Override
        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException,
                IOException {
            for (FilePath f : build.getWorkspace().list()) {
                f.touch(System.currentTimeMillis());
            }
            return true;
        }
    }

}
