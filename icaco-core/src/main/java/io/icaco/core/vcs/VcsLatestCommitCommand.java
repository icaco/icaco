package io.icaco.core.vcs;

import io.icaco.core.vcs.git.GitLatestCommitCommand;

import java.nio.file.Path;
import java.util.Optional;

import static io.icaco.core.vcs.VcsType.Git;

public interface VcsLatestCommitCommand extends VcsCommand<Optional<String>> {

    static VcsLatestCommitCommand create(VcsType vcsType, Path path) {
        if (vcsType == Git)
            return new GitLatestCommitCommand(path);
        throw new VcsException("VcsType not implemented: " + vcsType);
    }
}
