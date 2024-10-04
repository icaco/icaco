package io.icaco.core.vcs.git;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitLatestTagCommandTest extends GitCommandTest {

    @Test
    void noLatestTag() {
        // Given
        GitLatestTagCommand latestTag = new GitLatestTagCommand(repoPath);
        // When
        Optional<String> tag = latestTag.execute();
        // Then
        assertTrue(tag.isEmpty());
    }

    @Test
    void latestTag() {
        // Given
        execGit("tag", "1.0.0");
        GitLatestTagCommand latestTag = new GitLatestTagCommand(repoPath);
        // When
        Optional<String> tag = latestTag.execute();
        // Then
        assertEquals("1.0.0", tag.orElseThrow());
    }

    @Test
    void latestTagWithHash() throws IOException {
        // Given
        execGit("tag", "1.0.0");
        writeString(repoPath.resolve("src").resolve("test.txt"), "hej");
        execGit("add", ".");
        execGit("commit", "-m", "\"text\"");
        GitLatestTagCommand latestTag = new GitLatestTagCommand(repoPath);
        // When
        Optional<String> tag = latestTag.execute();
        // Then
        assertTrue(tag.orElseThrow().startsWith("1.0.0"));
        assertNotEquals("1.0.0", tag.get());
    }
}