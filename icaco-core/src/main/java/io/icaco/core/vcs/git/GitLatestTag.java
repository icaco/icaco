package io.icaco.core.vcs.git;

import io.icaco.core.syscmd.SysCmdException;
import io.icaco.core.syscmd.SysCmdResult;
import io.icaco.core.vcs.VcsException;
import io.icaco.core.vcs.VcsLatestTag;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.slf4j.LoggerFactory.getLogger;

public class GitLatestTag extends GitCommand implements VcsLatestTag {

    private static final Logger LOG = getLogger(GitLatestTag.class);

    public GitLatestTag(Path workingDir) {
        super(workingDir);
    }

    @Override
    public Optional<String> execute() {
        try {
            if (!validRepo) {
                return empty();
            }
            SysCmdResult result = execGit("describe", "--tags");
            if (result.getExitCode() == 128) {
                LOG.debug(result.getSingleValueOutput());
                return empty();
            }
            if (result.getExitCode() != 0) {
                LOG.debug(result.getSingleValueOutput());
                throw new VcsException("Git command '" + result.getCommand() + "' has exit code " + result.getExitCode());
            }
            return Optional.of(result.getSingleValueOutput());
        } catch (SysCmdException e) {
            throw new VcsException(e);
        }

    }
}
