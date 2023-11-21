package io.icaco.core.vcs;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

import static java.nio.charset.Charset.defaultCharset;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.write;
import static org.eclipse.jgit.api.Git.cloneRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GitChangesTest {

    Path repoPath = Path.of("target/icaco-git-test");
    Git git;

    @BeforeEach
    void cloneRepo() throws Exception {
        removeRepo();
        git = cloneRepository()
                .setURI("https://github.com/icaco/icaco-git-test.git")
                .setDirectory(repoPath.toFile())
                .call();
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
        GitChanges gitChanges = new GitChanges(git);
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
        git.add().addFilepattern(".").call();
        GitChanges gitChanges = new GitChanges(git);
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
        git.add().addFilepattern(".").call();
        GitChanges gitChanges = new GitChanges(git);
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
        GitChanges gitChanges = new GitChanges(git);
        // When
        Set<String> files =  gitChanges.list();
        // Then
        assertEquals(Set.of("README.md"), files);
    }

    @Test
    void defaultBranch() throws Exception {
        // Given
        removeRepo();
        git = cloneRepository()
                .setURI("https://github.com/icaco/icaco-git-test.git")
                .setDirectory(repoPath.toFile())
                .setBranch("feature/issue1")
                .call();
        GitChanges gitChanges = new GitChanges(git);
        // When
        Ref defaultBranch = gitChanges.defaultBranch();
        // Then
        assertEquals("refs/heads/release/1.0", defaultBranch.getName());
    }

    @Test
    void currentBranch() throws Exception {
        // Given
        removeRepo();
        git = cloneRepository()
                .setURI("https://github.com/icaco/icaco-git-test.git")
                .setDirectory(repoPath.toFile())
                .setBranch("feature/issue1")
                .call();
        GitChanges gitChanges = new GitChanges(git);
        // When
        Ref defaultBranch = gitChanges.currentBranch();
        // Then
        assertEquals("refs/heads/feature/issue1", defaultBranch.getName());
    }

    @Test
    void listBranchDiff() throws Exception {
        // Given
        removeRepo();
        git = cloneRepository()
                .setURI("https://github.com/icaco/icaco-git-test.git")
                .setDirectory(repoPath.toFile())
                .setBranch("feature/issue1")
                .call();
        GitChanges gitChanges = new GitChanges(git);
        // When
        Set<String> paths = gitChanges.list();
        // Then
        assertEquals("[LICENSES, src/Test.java, README.md]", paths.toString());
    }

}