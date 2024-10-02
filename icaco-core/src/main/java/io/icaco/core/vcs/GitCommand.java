package io.icaco.core.vcs;

import org.slf4j.Logger;

import java.nio.file.Path;

import static io.icaco.core.syscmd.SysCmd.exec;
import static java.lang.String.join;
import static org.slf4j.LoggerFactory.getLogger;

public class GitCommons {

    private static final Logger LOG = getLogger(GitCommons.class);

    final Path workingDir;
    final boolean validRepo;
    Path repoPath;


    public GitCommons(Path workingDir) {
        LOG.info(gitVersion());
        this.workingDir = workingDir;
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
        return join(" ", exec("git version").getOutput());
    }

    boolean isValidRepo() {
        return exec("git -C " + workingDir.toAbsolutePath() + " status").getExitCode() == 0;
    }


    Path gitRepoPath() {
        String relativeRoot = join(" ", exec("git -C " + workingDir.toAbsolutePath() + " rev-parse --show-cdup").getOutput());
        if (relativeRoot.isBlank())
            return workingDir;
        return workingDir.resolve(relativeRoot).normalize();
    }
}
