package io.icaco.maven;

import org.apache.maven.plugin.testing.MojoRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.nio.file.Path;

import static io.icaco.core.syscmd.SysCmd.exec;
import static java.nio.charset.Charset.defaultCharset;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.write;
import static org.junit.Assert.assertEquals;

public class VcsVersionMojoTest {

    @Rule
    public MojoRule rule = new MojoRule();

    Path repoPath = Path.of("target/icaco-git-test");

    @Before
    public void cloneRepo() throws Exception {
        removeRepo();
        exec("git clone https://github.com/icaco/icaco-git-test.git " + repoPath);
    }

    @After
    public void removeRepo() throws Exception {
        deleteDirectory(repoPath.toFile());
    }


    @Test
    public void tag() throws Exception {
        // Given
        exec("git -C " + repoPath.toAbsolutePath() + " checkout feature/issue1");
        exec("git -C " + repoPath.toAbsolutePath() + " tag 1.1.1");
        VcsVersionMojo myMojo = (VcsVersionMojo) rule.lookupConfiguredMojo(repoPath.toFile(), "vcs-version");
        // When
        myMojo.execute();
        // Then
        String property = myMojo.project.getProperties().getProperty(myMojo.vcsVersionPropertyName);
        assertEquals("1.1.1", property);
    }

    @Test
    public void latest() throws Exception {
        // Given
        exec("git -C " + repoPath.toAbsolutePath() + " checkout feature/issue1");
        exec("git -C " + repoPath.toAbsolutePath() + " tag 1.1.2");
        write(repoPath.resolve("src").resolve("test.txt").toFile(), "\"text\"", defaultCharset());
        exec("git -C " + repoPath.toAbsolutePath() + " add .");
        exec("git -C " + repoPath.toAbsolutePath() + " commit -m \"text\"");
        VcsVersionMojo myMojo = (VcsVersionMojo) rule.lookupConfiguredMojo(repoPath.toFile(), "vcs-version");
        // When
        myMojo.execute();
        // Then
        String property = myMojo.project.getProperties().getProperty(myMojo.vcsVersionPropertyName);
        assertEquals("1.1.2-latest", property);
    }

    @Test
    public void snapshot() throws Exception {
        // Given
        exec("git -C " + repoPath.toAbsolutePath() + " checkout feature/issue1");
        exec("git -C " + repoPath.toAbsolutePath() + " tag 1.1.3");
        write(repoPath.resolve("src").resolve("test.txt").toFile(), "\"text\"", defaultCharset());
        exec("git -C " + repoPath.toAbsolutePath() + " add .");
        exec("git -C " + repoPath.toAbsolutePath() + " commit -m \"text\"");
        VcsVersionMojo myMojo = (VcsVersionMojo) rule.lookupConfiguredMojo(repoPath.toFile(), "vcs-version");
        myMojo.commitsAheadTagPostfix = "SNAPSHOT";
        // When
        myMojo.execute();
        // Then
        String property = myMojo.project.getProperties().getProperty(myMojo.vcsVersionPropertyName);
        assertEquals("1.1.3-SNAPSHOT", property);
    }

    @Test
    public void jiraId() throws Exception {
        exec("git -C " + repoPath.toAbsolutePath() + " checkout feature/issue1");
        exec("git -C " + repoPath.toAbsolutePath() + " tag 1.1.4");
        exec("git -C " + repoPath.toAbsolutePath() + " checkout -b feature/KAA-2333-mamma-pappa-barn");
        VcsVersionMojo myMojo = (VcsVersionMojo) rule.lookupConfiguredMojo(repoPath.toFile(), "vcs-version");
        myMojo.useJiraIdOnFeatureBranch = true;
        // When
        myMojo.execute();
        // Then
        String property = myMojo.project.getProperties().getProperty(myMojo.vcsVersionPropertyName);
        assertEquals("KAA-2333-latest", property);
    }

    @Test
    public void releaseBranch() throws Exception {
        exec("git -C " + repoPath.toAbsolutePath() + " checkout feature/issue1");
        exec("git -C " + repoPath.toAbsolutePath() + " tag 1.1.4");
        exec("git -C " + repoPath.toAbsolutePath() + " checkout -b release/1.2.3");
        VcsVersionMojo myMojo = (VcsVersionMojo) rule.lookupConfiguredMojo(repoPath.toFile(), "vcs-version");
        myMojo.useReleaseBranchVersionNumber = true;
        // When
        myMojo.execute();
        // Then
        String property = myMojo.project.getProperties().getProperty(myMojo.vcsVersionPropertyName);
        assertEquals("1.2.3", property);
    }

    @Test
    public void doNotUseReleaseBranchVersionNumber() throws Exception {
        exec("git -C " + repoPath.toAbsolutePath() + " checkout feature/issue1");
        exec("git -C " + repoPath.toAbsolutePath() + " tag 1.5.4");
        exec("git -C " + repoPath.toAbsolutePath() + " checkout -b release/1.2.3");
        VcsVersionMojo myMojo = (VcsVersionMojo) rule.lookupConfiguredMojo(repoPath.toFile(), "vcs-version");
        myMojo.useReleaseBranchVersionNumber = false;
        // When
        myMojo.execute();
        // Then
        String property = myMojo.project.getProperties().getProperty(myMojo.vcsVersionPropertyName);
        assertEquals("1.5.4", property);
    }

    @Test
    public void releaseBranchWithCommits() throws Exception {
        exec("git -C " + repoPath.toAbsolutePath() + " checkout feature/issue1");
        exec("git -C " + repoPath.toAbsolutePath() + " tag 1.1.4");
        exec("git -C " + repoPath.toAbsolutePath() + " checkout -b release/1.2.4");
        write(repoPath.resolve("src").resolve("test.txt").toFile(), "\"text\"", defaultCharset());
        exec("git -C " + repoPath.toAbsolutePath() + " add .");
        exec("git -C " + repoPath.toAbsolutePath() + " commit -m \"text\"");
        VcsVersionMojo myMojo = (VcsVersionMojo) rule.lookupConfiguredMojo(repoPath.toFile(), "vcs-version");
        myMojo.useReleaseBranchVersionNumber = true;
        // When
        myMojo.execute();
        // Then
        String property = myMojo.project.getProperties().getProperty(myMojo.vcsVersionPropertyName);
        assertEquals("1.2.4", property);
    }

    @Test
    public void noJiraId() throws Exception {
        exec("git -C " + repoPath.toAbsolutePath() + " checkout feature/issue1");
        exec("git -C " + repoPath.toAbsolutePath() + " tag 1.1.5");
        exec("git -C " + repoPath.toAbsolutePath() + " checkout -b feature/KAA-2333-mamma-pappa-barn");
        VcsVersionMojo myMojo = (VcsVersionMojo) rule.lookupConfiguredMojo(repoPath.toFile(), "vcs-version");
        // When
        myMojo.execute();
        // Then
        String property = myMojo.project.getProperties().getProperty(myMojo.vcsVersionPropertyName);
        assertEquals("1.1.5", property);
    }

}

