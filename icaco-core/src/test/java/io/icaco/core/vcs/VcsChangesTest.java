package io.icaco.core.vcs;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class VcsChangesTest {

    @Test
    void createGit() {
        VcsChanges.create("git", Path.of("."));
    }

    @Test
    void createClearcase() {
        try {
            VcsChanges.create("clearcase", Path.of("."));
            fail();
        } catch (VcsException e) {
            assertEquals("VcsType not implemented: clearcase", e.getMessage());
        }
    }

}