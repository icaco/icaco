package io.icaco.core.vcs;

import io.icaco.core.vcs.git.GitRemoteUrlCommand;

import java.nio.file.Path;
import java.util.Optional;

import static io.icaco.core.vcs.VcsType.Git;

public interface VcsRemoteUrlCommand extends VcsCommand<Optional<String>> {

    static VcsRemoteUrlCommand create(VcsType vcsType, Path path) {
        if (vcsType == Git)
            return new GitRemoteUrlCommand(path);
        throw new VcsException("VcsType not implemented: " + vcsType);
    }
}
