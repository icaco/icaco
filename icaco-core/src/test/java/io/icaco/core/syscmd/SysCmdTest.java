package io.icaco.core.syscmd;

import org.junit.jupiter.api.Test;

import static java.lang.Integer.MIN_VALUE;
import static java.lang.String.join;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SysCmdTest {

    @Test
    void ls() {
        // Given
        String cmd = "ls -la";
        // When
        SysCmdResult sysCmdResult = SysCmd.exec(cmd);
        // Then
        assertEquals(0, sysCmdResult.getExitValue());
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
        assertEquals(1, sysCmdResult.getExitValue());
    }

    @Test
    void beppo() {
        // Given
        String cmd = "beppo";
        // When
        SysCmdResult sysCmdResult = SysCmd.exec(cmd);
        // Then
        assertEquals(MIN_VALUE, sysCmdResult.getExitValue());
        assertEquals("Cannot run program \"beppo\": error=2, No such file or directory", sysCmdResult.getException().getMessage());
    }

}