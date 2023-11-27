package io.icaco.core.vcs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static io.icaco.core.syscmd.SysCmd.exec;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.UUID.randomUUID;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.write;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        exec("git -C " + repoPath.toAbsolutePath() + " add .");
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
        exec("git -C " + repoPath.toAbsolutePath() + " add .");
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
        exec("git -C " + repoPath.toAbsolutePath() + " checkout feature/issue1");
        // When
        Optional<String> defaultBranch = gitChanges.getDefaultBranch();
        // Then
        assertTrue(defaultBranch.isPresent());
        assertEquals("refs/remotes/origin/release/1.0", defaultBranch.get());
    }

    @Test
    void hasNoRemote() throws IOException {
        // Given
        repoPath =  Files.createTempDirectory(randomUUID().toString());
        exec("git -C " + repoPath.toAbsolutePath() + " init");
        GitChanges gitChanges = new GitChanges(repoPath);
        // When
        Optional<String> defaultBranch = gitChanges.getDefaultBranch();
        Set<String> paths = gitChanges.list();
        // Then
        assertFalse(defaultBranch.isPresent());
        assertEquals("[]", paths.toString());
    }

    @Test
    void listBranchDiff() {
        // Given
        GitChanges gitChanges = new GitChanges(repoPath);
        exec("git -C " + repoPath.toAbsolutePath() + " checkout feature/issue1");
        // When
        Set<String> paths = gitChanges.list();
        // Then
        assertEquals("[LICENSES, src/Test.java, README.md]", paths.toString());
    }

    @Test
    void notAGitRepo() throws IOException {
        // Given
        repoPath =  Files.createTempDirectory(randomUUID().toString());
        GitChanges gitChanges = new GitChanges(repoPath);
        // When
        Set<String> paths = gitChanges.list();
        // Then
        assertEquals("[]", paths.toString());
    }

}