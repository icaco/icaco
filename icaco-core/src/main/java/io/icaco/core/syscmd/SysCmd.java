package io.icaco.core.syscmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.String.join;
import static java.util.Arrays.asList;

public class SysCmd {

    public static SysCmdResult exec(String cmd, String... args) {
        Process process = null;
        try {
            List<String> commandTokens = new ArrayList<>(asList(cmd.split(" ")));
            commandTokens.addAll(asList(args));
            process = new ProcessBuilder(commandTokens)
                    .start();
            process.waitFor();
            return SysCmdResult.builder()
                    .command(join(" ", commandTokens))
                    .output(readProcessOutput(process))
                    .exitCode(process.exitValue())
                    .build();
        } catch (IOException | InterruptedException e) {
            throw new SysCmdException(e);
        } finally {
            if (process != null)
                process.destroy();
        }
    }

    static List<String> readProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getResultStream(process).get()))) {
            List<String> result = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null)
                result.add(line);
            return result;
        }
    }

    static Supplier<InputStream> getResultStream(Process process) {
        if ( process.exitValue() == 0)
            return process::getInputStream;
        return process::getErrorStream;
    }


}
