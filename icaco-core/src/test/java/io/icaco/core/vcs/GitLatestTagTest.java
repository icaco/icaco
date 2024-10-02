package io.icaco.core.vcs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static io.icaco.core.syscmd.SysCmd.exec;
import static java.nio.charset.Charset.defaultCharset;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.write;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitLatestTagTest {

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
    void noLatestTag() {
        // Given
        GitLatestTag latestTag = new GitLatestTag(repoPath);
        // When
        Optional<String> tag = latestTag.execute();
        // Then
        assertTrue(tag.isEmpty());
    }

    @Test
    void latestTag() {
        // Given
        exec("git -C " + repoPath.toAbsolutePath() + " tag 1.0.0");
        GitLatestTag latestTag = new GitLatestTag(repoPath);
        // When
        Optional<String> tag = latestTag.execute();
        // Then
        assertEquals("1.0.0", tag.get());
    }

    @Test
    void latestTagWithHash() throws IOException {
        // Given
        exec("git -C " + repoPath.toAbsolutePath() + " tag 1.0.0");
        File addedFile = repoPath.resolve("src").resolve("test.txt").toFile();
        write(addedFile, "hej", defaultCharset());
        exec("git -C " + repoPath.toAbsolutePath() + " add .");
        exec("git -C " + repoPath.toAbsolutePath() + " commit -m\"text\"");
        GitLatestTag latestTag = new GitLatestTag(repoPath);
        // When
        Optional<String> tag = latestTag.execute();
        // Then
        assertTrue(tag.get().startsWith("1.0.0"));
        assertNotEquals("1.0.0", tag.get());
    }
}