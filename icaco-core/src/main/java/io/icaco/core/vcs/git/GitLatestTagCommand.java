package io.icaco.core.vcs.git;

import io.icaco.core.syscmd.SysCmdResult;
import io.icaco.core.vcs.VcsException;
import io.icaco.core.vcs.VcsLatestTagCommand;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.slf4j.LoggerFactory.getLogger;

public class GitLatestTagCommand extends GitCommand implements VcsLatestTagCommand {

    private static final Logger LOG = getLogger(GitLatestTagCommand.class);

    public GitLatestTagCommand(Path workingDir) {
        super(workingDir);
    }

    @Override
    public Optional<String> execute() {
        if (!validRepo)
            return empty();
        SysCmdResult result = execGit("describe", "--tags");
        if (result.getExitCode() == 0)
            return Optional.of(result.getSingleValueOutput());
        if (result.getExitCode() == 128) {
            LOG.debug(result.getSingleValueOutput());
            return empty();
        }
        LOG.error(result.getSingleValueOutput());
        throw new VcsException(result);
    }
}
