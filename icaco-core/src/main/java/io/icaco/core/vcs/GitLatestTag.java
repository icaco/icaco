package io.icaco.core.vcs;

import io.icaco.core.syscmd.SysCmdException;
import io.icaco.core.syscmd.SysCmdResult;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class GitLatestTag extends GitCommand implements VcsLatestTag {

    private static final Logger LOG = getLogger(GitLatestTag.class);

    GitLatestTag(Path workingDir) {
        super(workingDir);
    }

    @Override
    public Optional<String> execute() {
        try {
            SysCmdResult result = execGit("describe", "--tags");
            if (result.getExitCode() == 128) {
                LOG.debug(result.getSingleValueOutput());
                return Optional.empty();
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
