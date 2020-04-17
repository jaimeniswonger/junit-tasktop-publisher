package hudson.plugins.junittasktop;

import java.io.IOException;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.junit.TestDataPublisher;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;

public class TaskTopPublisher extends TestDataPublisher {

    private static final Logger LOG = Logger.getLogger(TaskTopPublisher.class.getName());

    @DataBoundConstructor
    public TaskTopPublisher() {
    }

    @Override
    public TestResultAction.Data contributeTestData(Run<?, ?> build, FilePath workspace, Launcher launcher,
                                   TaskListener listener, TestResult testResult) throws IOException,
            InterruptedException {

        LOG.info(testResult.getDisplayName());
        testResult.getChildren().forEach(packageResult -> {
            LOG.info("PACKAGE: " + packageResult.getName());
            packageResult.getChildren().forEach(classResult -> {
                LOG.info("CLASS: " + classResult.getName());
                classResult.getChildren().forEach(caseResult -> {
                    LOG.info("CASE: " + caseResult.getFullDisplayName() + " PASSED: " + caseResult.isPassed() + " DURATION: " + caseResult.getDurationString());
                });
            });
        });
        return null;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<TestDataPublisher> {

        @Override
        public String getDisplayName() {
            return "Publish JUnit test metrics to TaskTop";
        }

    }

}
