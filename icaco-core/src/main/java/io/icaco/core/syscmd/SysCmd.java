package io.icaco.core.syscmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class SysCmd {

    public static SysCmdResult exec(String cmd, String... flags) {
        Process process = null;
        try {
            List<String> command = asList(cmd.split(" "));
            command.addAll(asList(flags));
            process = new ProcessBuilder(command)
                    .start();
            process.waitFor();
            return SysCmdResult.builder()
                    .output(getProcessOutput(process))
                    .exitCode(process.exitValue())
                    .build();
        } catch (IOException | InterruptedException e) {
            throw new SysCmdException(e);
        } finally {
            if (process != null)
                process.destroy();
        }
    }

    static List<String> getProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            List<String> result = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null)
                result.add(line);
            return result;
        }
    }

}
