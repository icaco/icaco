package io.icaco.core.vcs;

import io.icaco.core.vcs.git.GitLatestTag;

import java.nio.file.Path;
import java.util.Optional;

import static io.icaco.core.vcs.VcsType.Git;

public interface VcsLatestTag extends VcsCommand<Optional<String>> {

    static VcsLatestTag create(VcsType vcsType, Path path) {
        if (vcsType == Git)
            return new GitLatestTag(path);
        throw new VcsException("VcsType not implemented: " + vcsType);
    }
}
