package io.icaco.maven;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static io.icaco.core.syscmd.SysCmd.exec;
import static java.nio.charset.Charset.defaultCharset;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.write;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VcsVersionMojoTest {

    Path repoPath = Path.of("target/icaco-git-test");

    @BeforeEach
    void cloneRepo() throws Exception {
        removeRepo();
        exec("git clone https://github.com/icaco/icaco-git-test.git " + repoPath);
    }

    @AfterEach
    void removeRepo() throws Exception {
        deleteDirectory(repoPath.toFile());
    }

    VcsVersionMojo mojo() throws Exception {
        VcsVersionMojo mojo = MojoDefaults.apply(new VcsVersionMojo(), "vcs-version");
        mojo.project = new MavenProject();
        mojo.project.setFile(repoPath.resolve("pom.xml").toAbsolutePath().toFile());
        return mojo;
    }


    @Test
    void tag() throws Exception {
        // Given
        exec("git -C " + repoPath.toAbsolutePath() + " checkout feature/issue1");
        exec("git -C " + repoPath.toAbsolutePath() + " tag 1.1.1");
        VcsVersionMojo myMojo = mojo();
        // When
        myMojo.execute();
        // Then
        String property = myMojo.project.getProperties().getProperty(myMojo.vcsVersionPropertyName);
        assertEquals("1.1.1", property);
    }

    @Test
    void latest() throws Exception {
        // Given
        exec("git -C " + repoPath.toAbsolutePath() + " checkout feature/issue1");
        exec("git -C " + repoPath.toAbsolutePath() + " tag 1.1.2");
        write(repoPath.resolve("src").resolve("test.txt").toFile(), "\"text\"", defaultCharset());
        exec("git -C " + repoPath.toAbsolutePath() + " add .");
        exec("git -C " + repoPath.toAbsolutePath() + " commit -m \"text\"");
        VcsVersionMojo myMojo = mojo();
        // When
        myMojo.execute();
        // Then
        String property = myMojo.project.getProperties().getProperty(myMojo.vcsVersionPropertyName);
        assertEquals("1.1.2-latest", property);
    }

    @Test
    void snapshot() throws Exception {
        // Given
        exec("git -C " + repoPath.toAbsolutePath() + " checkout feature/issue1");
        exec("git -C " + repoPath.toAbsolutePath() + " tag 1.1.3");
        write(repoPath.resolve("src").resolve("test.txt").toFile(), "\"text\"", defaultCharset());
        exec("git -C " + repoPath.toAbsolutePath() + " add .");
        exec("git -C " + repoPath.toAbsolutePath() + " commit -m \"text\"");
        VcsVersionMojo myMojo = mojo();
        myMojo.commitsAheadTagPostfix = "SNAPSHOT";
        // When
        myMojo.execute();
        // Then
        String property = myMojo.project.getProperties().getProperty(myMojo.vcsVersionPropertyName);
        assertEquals("1.1.3-SNAPSHOT", property);
    }

    @Test
    void jiraId() throws Exception {
        exec("git -C " + repoPath.toAbsolutePath() + " checkout feature/issue1");
        exec("git -C " + repoPath.toAbsolutePath() + " tag 1.1.4");
        exec("git -C " + repoPath.toAbsolutePath() + " checkout -b feature/KAA-2333-mamma-pappa-barn");
        VcsVersionMojo myMojo = mojo();
        myMojo.useJiraIdOnFeatureBranch = true;
        // When
        myMojo.execute();
        // Then
        String property = myMojo.project.getProperties().getProperty(myMojo.vcsVersionPropertyName);
        assertEquals("KAA-2333-latest", property);
    }

    @Test
    void releaseBranch() throws Exception {
        exec("git -C " + repoPath.toAbsolutePath() + " checkout feature/issue1");
        exec("git -C " + repoPath.toAbsolutePath() + " tag 1.1.4");
        exec("git -C " + repoPath.toAbsolutePath() + " checkout -b release/1.2.3");
        VcsVersionMojo myMojo = mojo();
        myMojo.useReleaseBranchVersionNumber = true;
        // When
        myMojo.execute();
        // Then
        String property = myMojo.project.getProperties().getProperty(myMojo.vcsVersionPropertyName);
        assertEquals("1.2.3", property);
    }

    @Test
    void doNotUseReleaseBranchVersionNumber() throws Exception {
        exec("git -C " + repoPath.toAbsolutePath() + " checkout feature/issue1");
        exec("git -C " + repoPath.toAbsolutePath() + " tag 1.5.4");
        exec("git -C " + repoPath.toAbsolutePath() + " checkout -b release/1.2.3");
        VcsVersionMojo myMojo = mojo();
        myMojo.useReleaseBranchVersionNumber = false;
        // When
        myMojo.execute();
        // Then
        String property = myMojo.project.getProperties().getProperty(myMojo.vcsVersionPropertyName);
        assertEquals("1.5.4", property);
    }

    @Test
    void releaseBranchWithCommits() throws Exception {
        exec("git -C " + repoPath.toAbsolutePath() + " checkout feature/issue1");
        exec("git -C " + repoPath.toAbsolutePath() + " tag 1.1.4");
        exec("git -C " + repoPath.toAbsolutePath() + " checkout -b release/1.2.4");
        write(repoPath.resolve("src").resolve("test.txt").toFile(), "\"text\"", defaultCharset());
        exec("git -C " + repoPath.toAbsolutePath() + " add .");
        exec("git -C " + repoPath.toAbsolutePath() + " commit -m \"text\"");
        VcsVersionMojo myMojo = mojo();
        myMojo.useReleaseBranchVersionNumber = true;
        // When
        myMojo.execute();
        // Then
        String property = myMojo.project.getProperties().getProperty(myMojo.vcsVersionPropertyName);
        assertEquals("1.2.4", property);
    }

    @Test
    void noJiraId() throws Exception {
        exec("git -C " + repoPath.toAbsolutePath() + " checkout feature/issue1");
        exec("git -C " + repoPath.toAbsolutePath() + " tag 1.1.5");
        exec("git -C " + repoPath.toAbsolutePath() + " checkout -b feature/KAA-2333-mamma-pappa-barn");
        VcsVersionMojo myMojo = mojo();
        // When
        myMojo.execute();
        // Then
        String property = myMojo.project.getProperties().getProperty(myMojo.vcsVersionPropertyName);
        assertEquals("1.1.5", property);
    }

    @Test
    void latestOnMain() throws Exception {
        exec("git -C " + repoPath.toAbsolutePath() + " checkout main");
        VcsVersionMojo myMojo = mojo();
        myMojo.useLatestVersionOnMainBranch = true;
        // When
        myMojo.execute();
        // Then
        String property = myMojo.project.getProperties().getProperty(myMojo.vcsVersionPropertyName);
        assertEquals("latest", property);
    }

    @Test
    void latestOnMaster() throws Exception {
        exec("git -C " + repoPath.toAbsolutePath() + " checkout master");
        VcsVersionMojo myMojo = mojo();
        myMojo.useLatestVersionOnMainBranch = true;
        myMojo.mainBranchName = "master";
        // When
        myMojo.execute();
        // Then
        String property = myMojo.project.getProperties().getProperty(myMojo.vcsVersionPropertyName);
        assertEquals("latest", property);
    }

}

