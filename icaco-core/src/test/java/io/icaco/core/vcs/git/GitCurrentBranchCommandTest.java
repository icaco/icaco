package io.icaco.core.vcs.git;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GitCurrentBranchCommandTest extends GitCommandTest {

    @Test
    void release10() {
        // Given
        GitCurrentBranchCommand latestTag = new GitCurrentBranchCommand(repoPath);
        // When
        Optional<String> branch = latestTag.execute();
        // Then
        assertEquals("release/1.0", branch.orElseThrow());
    }

    @Test
    void featureBranch() {
        // Given
        GitCurrentBranchCommand latestTag = new GitCurrentBranchCommand(repoPath);
        execGit("checkout", "-b", "feature/KAA-2333-mamma-pappa-barn");
        // When
        Optional<String> branch = latestTag.execute();
        // Then
        assertEquals("feature/KAA-2333-mamma-pappa-barn", branch.orElseThrow());
    }

}