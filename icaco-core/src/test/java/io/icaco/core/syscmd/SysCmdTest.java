package io.icaco.core.syscmd;

import org.junit.jupiter.api.Test;

import static java.lang.String.join;
import static org.junit.jupiter.api.Assertions.*;

class SysCmdTest {

    @Test
    void ls() {
        // Given
        String cmd = "ls -la";
        // When
        SysCmdResult sysCmdResult = SysCmd.exec(cmd);
        // Then
        assertEquals(0, sysCmdResult.getExitCode());
        String out = join(" ", sysCmdResult.getOutput());
        assertTrue(out.contains("pom.xml"));
    }

    @Test
    void lsMamma() {
        // Given
        String cmd = "ls --MAMMA";
        // When
        SysCmdResult sysCmdResult = SysCmd.exec(cmd);
        // Then
        assertNotEquals(0, sysCmdResult.getExitCode());
    }

    @Test
    void beppo() {
        // Given
        String cmd = "beppo";
        // When
        try {
            SysCmd.exec(cmd);
            fail();
        }
        catch (SysCmdException e) {
            // Then
            assertEquals("Cannot run program \"beppo\": error=2, No such file or directory", e.getMessage());
        }
    }

}