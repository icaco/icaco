package io.icaco.core.syscmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.MIN_VALUE;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class SysCmd {

    public static SysCmdResult exec(String cmd) {
        Process process = null;
        try {
            String[] command = cmd.split(" ");
            process = new ProcessBuilder(command).start();
            process.waitFor();
            return SysCmdResult.builder()
                    .output(getProcessOutput(process))
                    .exitValue(process.exitValue())
                    .build();
        } catch (IOException | InterruptedException e) {
            return SysCmdResult.builder()
                    .output(e.getMessage().lines().collect(toList()))
                    .exitValue(MIN_VALUE)
                    .exception(e)
                    .build();
        } finally {
            ofNullable(process).ifPresent(Process::destroy);
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
