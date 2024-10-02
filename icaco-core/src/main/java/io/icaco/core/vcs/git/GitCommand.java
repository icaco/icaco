package io.icaco.core.vcs.git;

import io.icaco.core.syscmd.SysCmdResult;
import org.slf4j.Logger;

import java.nio.file.Path;

import static io.icaco.core.syscmd.SysCmd.exec;
import static org.slf4j.LoggerFactory.getLogger;

abstract class GitCommand {

    private static final Logger LOG = getLogger(GitCommand.class);

    final Path workingDir;
    final boolean validRepo;
    Path repoPath;


    public GitCommand(Path workingDir) {
        this.workingDir = workingDir;
        LOG.info(gitVersion());
        LOG.info("git working directory: {}", workingDir.toAbsolutePath());
        this.validRepo = isValidRepo();
        if (validRepo) {
            repoPath = gitRepoPath();
            LOG.info("git repository path: {}", repoPath.toAbsolutePath());
        }
        else {
            LOG.warn("repository path isn't a valid: {}", workingDir.toAbsolutePath());
        }
    }

    String gitVersion() {
        return execGit("version").getSingleValueOutput();
    }

    boolean isValidRepo() {
        return execGit("status").getExitCode() == 0;
    }


    Path gitRepoPath() {
        String relativeRoot = execGit("rev-parse", "--show-cdup").getSingleValueOutput();
        if (relativeRoot.isBlank())
            return workingDir;
        return workingDir.resolve(relativeRoot).normalize();
    }

    SysCmdResult execGit(String... arguments) {
        return exec("git -C " + workingDir.toAbsolutePath(), arguments);
    }
}
