package io.icaco.core.syscmd;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SysCmdResult {
    String command;
    List<String> output;
    int exitCode;

    public String getSingleValueOutput() {
        if (output.isEmpty())
            return "";
        if (output.size() == 1)
            return output.get(0);
        throw new IllegalStateException("More than 1 output elements");
    }
}
