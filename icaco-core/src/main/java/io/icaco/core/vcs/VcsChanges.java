package io.icaco.core.vcs;

import java.nio.file.Path;
import java.util.Set;

import static io.icaco.core.vcs.VcsType.Git;

public interface VcsChanges extends VcsCommand<Set<Path>> {

    static VcsChanges create(VcsType vcsType, Path path) {
        if (vcsType == Git)
            return new GitChanges(path);
        throw new VcsException("VcsType not implemented: " + vcsType);
    }
}
