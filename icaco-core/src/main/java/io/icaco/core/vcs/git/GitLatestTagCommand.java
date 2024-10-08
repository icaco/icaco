package io.icaco.core.vcs.git;

import io.icaco.core.syscmd.SysCmdResult;
import io.icaco.core.vcs.VcsException;
import io.icaco.core.vcs.VcsLatestTagCommand;
import io.icaco.core.vcs.model.VcsTag;
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
    public Optional<VcsTag> execute() {
        if (!validRepo)
            return empty();
        Optional<String> tagWithOptionalPrefix = getTag(new String[]{"describe", "--tags"});
        if (tagWithOptionalPrefix.isEmpty())
            return empty();
        Optional<String> tagWithoutOptionalPrefix = getTag(new String[]{"describe", "--tags", "--abbrev=0"});
        return tagWithoutOptionalPrefix.map(s -> new VcsTag(tagWithOptionalPrefix.get(), s));
    }

    private Optional<String> getTag(String[] gitArgs) {
        SysCmdResult result = execGit(gitArgs);
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
