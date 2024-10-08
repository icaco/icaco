package io.icaco.core.vcs;

import io.icaco.core.vcs.git.GitCurrentBranchCommand;

import java.nio.file.Path;
import java.util.Optional;

import static io.icaco.core.vcs.VcsType.Git;

public interface VcsCurrentBranchCommand extends VcsCommand<Optional<String>> {

    static VcsCurrentBranchCommand create(VcsType vcsType, Path path) {
        if (vcsType == Git)
            return new GitCurrentBranchCommand(path);
        throw new VcsException("VcsType not implemented: " + vcsType);
    }
}
