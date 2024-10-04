package io.icaco.core.vcs;

import io.icaco.core.vcs.git.GitChangesCommand;

import java.nio.file.Path;
import java.util.Set;

import static io.icaco.core.vcs.VcsType.Git;

public interface VcsChangesCommand extends VcsCommand<Set<Path>> {

    static VcsChangesCommand create(VcsType vcsType, Path path) {
        if (vcsType == Git)
            return new GitChangesCommand(path);
        throw new VcsException("VcsType not implemented: " + vcsType);
    }
}
