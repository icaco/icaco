package io.icaco.core.vcs;

import io.icaco.core.vcs.git.GitLatestTagCommand;
import io.icaco.core.vcs.model.VcsTag;

import java.nio.file.Path;
import java.util.Optional;

import static io.icaco.core.vcs.VcsType.Git;

public interface VcsLatestTagCommand extends VcsCommand<Optional<VcsTag>> {

    static VcsLatestTagCommand create(VcsType vcsType, Path path) {
        if (vcsType == Git)
            return new GitLatestTagCommand(path);
        throw new VcsException("VcsType not implemented: " + vcsType);
    }
}
