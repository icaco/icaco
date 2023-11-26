package io.icaco.core.vcs;

import java.nio.file.Path;
import java.util.Set;

public interface VcsChanges {
    Set<String> list();

    static VcsChanges create(String vcsType, Path path) {
        if ("git".equalsIgnoreCase(vcsType))
            return new GitChanges(path);
        throw new VcsException("VcsType not implemented: " + vcsType);
    }
}
