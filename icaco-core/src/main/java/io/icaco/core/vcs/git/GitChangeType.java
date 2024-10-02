package io.icaco.core.vcs.git;

import static java.util.Arrays.stream;

enum GitChangeType {
    Untracked("?"),
    Added("A"),
    Modified("M"),
    FileTypeChanged("T"),
    Renamed("R"),
    Copied("C"),
    Deleted("D");

    final String symbol;

    GitChangeType(String symbol) {
        this.symbol = symbol;
    }

    static GitChangeType fromStr(String symbol) {
        return stream(values())
                .filter(r -> r.symbol.equalsIgnoreCase(symbol))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unknown symbol: " + symbol));
    }
}
