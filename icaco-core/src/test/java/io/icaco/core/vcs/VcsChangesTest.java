package io.icaco.core.vcs;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static io.icaco.core.vcs.VcsType.Git;

class VcsChangesTest {

    @Test
    void createGit() {
        VcsChangesCommand.create(Git, Path.of("."));
    }

}