package io.icaco.core.vcs.git;

import io.icaco.core.syscmd.SysCmdResult;
import io.icaco.core.vcs.VcsCurrentBranchCommand;
import io.icaco.core.vcs.VcsException;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.slf4j.LoggerFactory.getLogger;

public class GitCurrentBranchCommand extends GitCommand implements VcsCurrentBranchCommand {

    private static final Logger LOG = getLogger(GitCurrentBranchCommand.class);

    public GitCurrentBranchCommand(Path workingDir) {
        super(workingDir);
    }


    @Override
    public Optional<String> execute() {
        SysCmdResult result = execGit("branch", "--show-current");
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
