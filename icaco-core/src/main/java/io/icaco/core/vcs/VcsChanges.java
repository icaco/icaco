package io.icaco.core.vcs;

import java.nio.file.Path;
import java.util.Set;

import static io.icaco.core.vcs.VcsType.Git;
import static java.util.Objects.requireNonNull;

public interface VcsChanges {
    Set<String> list();

    default VcsChanges create(VcsType type, Path path) {
        if (requireNonNull(type) == Git)
            return new GitChanges(path);
        throw new VcsException("VcsType not implemented: " + type);
    }
}
