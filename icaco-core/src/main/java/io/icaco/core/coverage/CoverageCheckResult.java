package io.icaco.core.coverage;

import lombok.Value;

import java.util.List;

@Value
public class CoverageCheckResult {
    String bundleName;
    int classCount;
    List<String> violations;

    public boolean isMet() {
        return violations.isEmpty();
    }
}
