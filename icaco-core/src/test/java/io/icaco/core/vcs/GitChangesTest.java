package io.icaco.core.vcs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

import static io.icaco.core.syscmd.SysCmd.exec;
import static java.nio.charset.Charset.defaultCharset;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.write;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GitChangesTest {

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

    @Test
    void listUntracked() throws Exception {
        // Given
        File untrackedFile = repoPath.resolve("src").resolve("test.txt").toFile();
        write(untrackedFile, "hej", defaultCharset());
        GitChanges gitChanges = new GitChanges(repoPath);
        // When
        Set<String> files =  gitChanges.list();
        // Then
        assertEquals(Set.of("src/test.txt"), files);
    }

    @Test
    void listAdded() throws Exception {
        // Given
        File addedFile = repoPath.resolve("src").resolve("test.txt").toFile();
        write(addedFile, "hej", defaultCharset());
        GitChanges gitChanges = new GitChanges(repoPath);
        gitAdd(".");
        // When
        Set<String> files =  gitChanges.list();
        // Then
        assertEquals(Set.of("src/test.txt"), files);
    }

    @Test
    void listChanged() throws Exception {
        // Given
        File changedFile = repoPath.resolve("README.md").toFile();
        write(changedFile, "hej", defaultCharset());
        GitChanges gitChanges = new GitChanges(repoPath);
        gitAdd(".");
        // When
        Set<String> files =  gitChanges.list();
        // Then
        assertEquals(Set.of("README.md"), files);
    }

    @Test
    void listModified() throws Exception {
        // Given
        File changedFile = repoPath.resolve("README.md").toFile();
        write(changedFile, "hej", defaultCharset());
        GitChanges gitChanges = new GitChanges(repoPath);
        // When
        Set<String> files =  gitChanges.list();
        // Then
        assertEquals(Set.of("README.md"), files);
    }

    @Test
    void defaultBranch() {
        // Given
        GitChanges gitChanges = new GitChanges(repoPath);
        gitCheckout("feature/issue1");
        // When
        String defaultBranch = gitChanges.getDefaultBranch();
        // Then
        assertEquals("refs/remotes/origin/release/1.0", defaultBranch);
    }

    @Test
    void listBranchDiff() {
        // Given
        GitChanges gitChanges = new GitChanges(repoPath);
        gitCheckout("feature/issue1");
        // When
        Set<String> paths = gitChanges.list();
        // Then
        assertEquals("[LICENSES, src/Test.java, README.md]", paths.toString());
    }

    void gitAdd(String s) {
        exec("git -C " + repoPath.toAbsolutePath() + " add " + s);
    }

    void gitCheckout(String s) {
        exec("git -C " + repoPath.toAbsolutePath() + " checkout " + s);
    }

}