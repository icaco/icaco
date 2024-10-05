package io.icaco.core.vcs.git;

import io.icaco.core.vcs.VcsException;
import lombok.Value;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.walk;
import static java.util.stream.Collectors.toSet;

@Value
class GitChange {

    GitChangeType stagingAreaChangeType;
    GitChangeType workingTreeChangeType;
    Set<Path> paths;

    GitChange(String row, Path repoPath) {
        try {
            StringTokenizer st = new StringTokenizer(row);
            String changeTypes = st.nextToken();
            stagingAreaChangeType = GitChangeType.fromStr(changeTypes.substring(0, 1));
            if (changeTypes.length() == 2)
                workingTreeChangeType = GitChangeType.fromStr(changeTypes.substring(1, 2));
            else
                workingTreeChangeType = null;
            Path path = repoPath.resolve(st.nextToken());
            if (isDirectory(path))
                try (Stream<Path> stream = walk(path)) {
                    paths = stream.filter(p -> !isDirectory(p)).collect(toSet());
                }
            else
                paths = Set.of(path);
        } catch (IOException e) {
            throw new VcsException(e);
        }
    }

}
