package io.icaco.core.vcs;

import java.nio.file.Path;
import java.util.Optional;

public interface VcsLatestTag extends VcsCommand<Optional<String>> {

    static VcsLatestTag create(String vcsType, Path path) {
        if ("git".equalsIgnoreCase(vcsType))
            return new GitLatestTag(path);
        throw new VcsException("VcsType not implemented: " + vcsType);
    }
}
