package io.icaco.core.vcs.git;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitChangesTest extends GitCommandTest {

    @Test
    void listUntracked() throws Exception {
        // Given
        writeString(repoPath.resolve("src").resolve("test.txt"), "hej");
        GitChanges gitChanges = new GitChanges(repoPath);
        // When
        Set<Path> files =  gitChanges.execute();
        // Then
        assertEquals(toAbsolutePaths("src/test.txt"), files);
    }

    @Test
    void listAdded() throws Exception {
        // Given
        writeString(repoPath.resolve("src").resolve("test.txt"), "hej");
        GitChanges gitChanges = new GitChanges(repoPath);
        execGit("add", ".");
        // When
        Set<Path> files =  gitChanges.execute();
        // Then
        assertEquals(toAbsolutePaths("src/test.txt"), files);
    }

    @Test
    void listChanged() throws Exception {
        // Given
        writeString(repoPath.resolve("README.md"), "hej");
        GitChanges gitChanges = new GitChanges(repoPath);
        execGit("add", ".");
        // When
        Set<Path> files =  gitChanges.execute();
        // Then
        assertEquals(toAbsolutePaths("README.md"), files);
    }

    @Test
    void listModified() throws Exception {
        // Given
        writeString(repoPath.resolve("README.md"), "hej");
        GitChanges gitChanges = new GitChanges(repoPath);
        // When
        Set<Path> files =  gitChanges.execute();
        // Then
        assertEquals(toAbsolutePaths("README.md"), files);
    }

    @Test
    void defaultBranch() {
        // Given
        GitChanges gitChanges = new GitChanges(repoPath);
        execGit("checkout", "feature/issue1");
        // When
        Optional<String> defaultBranch = gitChanges.getDefaultBranch();
        // Then
        assertTrue(defaultBranch.isPresent());
        assertEquals("refs/remotes/origin/release/1.0", defaultBranch.get());
    }

    @Test
    void hasNoRemote() throws IOException {
        // Given
        Path repoPath =  Files.createTempDirectory(randomUUID().toString());
        execGit("init");
        GitChanges gitChanges = new GitChanges(repoPath);
        // When
        Optional<String> defaultBranch = gitChanges.getDefaultBranch();
        Set<Path> paths = gitChanges.execute();
        // Then
        assertFalse(defaultBranch.isPresent());
        assertEquals("[]", paths.toString());
    }

    @Test
    void listBranchDiff() {
        // Given
        GitChanges gitChanges = new GitChanges(repoPath);
        execGit("checkout", "feature/issue1");
        // When
        Set<Path> paths = gitChanges.execute();
        // Then
        assertEquals(toAbsolutePaths("LICENSES", "src/main/java/icaco/Test.java", "README.md", "pom.xml"), paths);
    }

    @Test
    void notAGitRepo() throws IOException {
        // Given
        Path repoPath =  Files.createTempDirectory(randomUUID().toString());
        GitChanges gitChanges = new GitChanges(repoPath);
        // When
        Set<Path> paths = gitChanges.execute();
        // Then
        assertEquals("[]", paths.toString());
    }

    Set<Path> toAbsolutePaths(String... paths) {
        return Arrays.stream(paths)
                .map(repoPath::resolve)
                .map(Path::toAbsolutePath)
                .collect(toSet());
    }

}