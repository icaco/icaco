package io.icaco.core.syscmd;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SysCmdResult {
    List<String> output;
    int exitCode;
}
