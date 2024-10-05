package io.icaco.core.vcs;

import org.slf4j.Logger;

import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public enum VcsType {
    Git;

    private static final Logger LOG = getLogger(VcsType.class);

    public static Optional<VcsType> findVcsType(String typeName) {
        for (VcsType type: VcsType.values())
            if (type.name().equalsIgnoreCase(typeName))
                return Optional.of(type);
        LOG.warn("No type match for {} in {}", typeName, values());
        return Optional.empty();
    }
}
