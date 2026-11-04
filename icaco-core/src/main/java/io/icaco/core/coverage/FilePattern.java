package io.icaco.core.coverage;

import java.util.Arrays;
import java.util.List;

/**
 * Ant-style path pattern, as used by JaCoCo includes and excludes: {@code **} matches
 * any number of directories, {@code *} any characters within a directory and
 * {@code ?} a single character. A pattern ending with {@code /} matches everything below it.
 */
class FilePattern {

    private final List<String> segments;

    FilePattern(String pattern) {
        String normalized = pattern.trim().replace('\\', '/');
        if (normalized.endsWith("/"))
            normalized += "**";
        segments = split(normalized);
    }

    boolean matches(String path) {
        return matches(0, split(path.replace('\\', '/')), 0);
    }

    private boolean matches(int patternIndex, List<String> pathSegments, int pathIndex) {
        if (patternIndex == segments.size())
            return pathIndex == pathSegments.size();
        String segment = segments.get(patternIndex);
        if (segment.equals("**")) {
            for (int i = pathIndex; i <= pathSegments.size(); i++)
                if (matches(patternIndex + 1, pathSegments, i))
                    return true;
            return false;
        }
        return pathIndex < pathSegments.size()
                && segmentMatches(segment, pathSegments.get(pathIndex))
                && matches(patternIndex + 1, pathSegments, pathIndex + 1);
    }

    private static boolean segmentMatches(String pattern, String name) {
        String regex = ("\\Q" + pattern + "\\E")
                .replace("*", "\\E.*\\Q")
                .replace("?", "\\E.\\Q");
        return name.matches(regex);
    }

    private static List<String> split(String path) {
        return Arrays.asList(path.replaceAll("^/+", "").split("/+"));
    }

}
