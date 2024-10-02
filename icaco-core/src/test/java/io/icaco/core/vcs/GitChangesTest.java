package io.icaco.core.vcs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static io.icaco.core.syscmd.SysCmd.exec;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toSet;
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
        Set<Path> files =  gitChanges.execute();
        // Then
        assertEquals(toAbsolutePaths("src/test.txt"), files);
    }

    @Test
    void listAdded() throws Exception {
        // Given
        File addedFile = repoPath.resolve("src").resolve("test.txt").toFile();
        write(addedFile, "hej", defaultCharset());
        GitChanges gitChanges = new GitChanges(repoPath);
        exec("git -C " + repoPath.toAbsolutePath() + " add .");
        // When
        Set<Path> files =  gitChanges.execute();
        // Then
        assertEquals(toAbsolutePaths("src/test.txt"), files);
    }

    @Test
    void listChanged() throws Exception {
        // Given
        File changedFile = repoPath.resolve("README.md").toFile();
        write(changedFile, "hej", defaultCharset());
        GitChanges gitChanges = new GitChanges(repoPath);
        exec("git -C " + repoPath.toAbsolutePath() + " add .");
        // When
        Set<Path> files =  gitChanges.execute();
        // Then
        assertEquals(toAbsolutePaths("README.md"), files);
    }

    @Test
    void listModified() throws Exception {
        // Given
        File changedFile = repoPath.resolve("README.md").toFile();
        write(changedFile, "hej", defaultCharset());
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
        Set<Path> paths = gitChanges.execute();
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
        Set<Path> paths = gitChanges.execute();
        // Then
        assertEquals(toAbsolutePaths("LICENSES", "src/main/java/icaco/Test.java", "README.md", "pom.xml"), paths);
    }

    @Test
    void notAGitRepo() throws IOException {
        // Given
        repoPath =  Files.createTempDirectory(randomUUID().toString());
        GitChanges gitChanges = new GitChanges(repoPath);
        // When
        Set<Path> paths = gitChanges.execute();
        // Then
        assertEquals("[]", paths.toString());
    }

    Set<Path> toAbsolutePaths(String... paths) {
        return Arrays.stream(paths)
                .map(p -> repoPath.resolve(p))
                .map(Path::toAbsolutePath)
                .collect(toSet());
    }

}