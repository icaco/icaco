package io.icaco.core.vcs;

import java.util.Optional;

public enum VcsType {
    Git;

    public static Optional<VcsType> findVcsType(String typeName) {
        for (VcsType type: VcsType.values()) {
            if (type.name().equalsIgnoreCase(typeName))
                return Optional.of(type);
        }
        return Optional.empty();
    }
}
