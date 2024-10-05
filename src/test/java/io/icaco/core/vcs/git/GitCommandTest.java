package io.icaco.core.vcs.git;

import io.icaco.core.syscmd.SysCmdResult;
import io.icaco.core.vcs.VcsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Path;

import static io.icaco.core.syscmd.SysCmd.exec;
import static java.nio.charset.Charset.defaultCharset;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.write;

abstract class GitCommandTest {
    final Path repoPath = Path.of("target/icaco-git-test");

    @BeforeEach
    void cloneRepo() throws Exception {
        removeRepo();
        exec("git clone https://github.com/icaco/icaco-git-test.git " + repoPath);
    }

    @AfterEach
    void removeRepo() throws Exception {
        deleteDirectory(repoPath.toFile());
    }

    void execGit(String... arguments) {
        SysCmdResult result = exec("git -C " + repoPath.toAbsolutePath(), arguments);
        if (result.getExitCode() != 0)
            throw new VcsException(result);
    }

    void writeString(Path path, String data) throws IOException {
        write(path.toFile(), data, defaultCharset());
    }

}
