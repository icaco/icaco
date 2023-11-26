package io.icaco.core.vcs;

import io.icaco.core.syscmd.SysCmdException;
import io.icaco.core.syscmd.SysCmdResult;
import lombok.Value;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import static io.icaco.core.syscmd.SysCmd.exec;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.walk;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;

public class GitChanges implements VcsChanges {

    private static final Logger LOG = getLogger(GitChanges.class);


    final Path repoPath;

    public GitChanges(Path repoPath) {
        this.repoPath = repoPath;
    }

    @Override
    public Set<String> list() {
        String localChangesCmd = "git -C " + repoPath.toAbsolutePath() + " status --porcelain --no-renames ";
        String remoteChangesCmd = "git -C " + repoPath.toAbsolutePath() + " diff --name-status --no-renames " + getDefaultBranch();
        Set<String> result = new LinkedHashSet<>();
        result.addAll(listChanges(localChangesCmd));
        result.addAll(listChanges(remoteChangesCmd));
        return result;
    }

    public Set<String> listChanges(String cmd) {
        try {
            SysCmdResult sysCmdResult = exec(cmd);
            if (sysCmdResult.getExitCode() != 0)
                throw new VcsException("Git command '" + cmd + "' has exit code " + sysCmdResult.getExitCode());
            return sysCmdResult
                    .getOutput()
                    .stream()
                    .map(row -> new Change(row, repoPath))
                    .filter(c -> c.stagingAreaChangeType != ChangeType.Deleted)
                    .filter(c -> c.workingTreeChangeType != ChangeType.Deleted)
                    .map(Change::getPaths)
                    .flatMap(Collection::stream)
                    .map(Path::toString)
                    .map(s -> s.replace(repoPath + "/", ""))
                    .collect(toSet());
        } catch (SysCmdException e) {
            throw new VcsException(e);
        }
    }

    String getDefaultBranch() {
        try {
            String cmd = "git -C " + repoPath.toAbsolutePath() + " symbolic-ref refs/remotes/origin/HEAD";
            SysCmdResult sysCmdResult = exec(cmd);
            if (sysCmdResult.getExitCode() != 0)
                throw new VcsException("Git command '" + cmd + "' has exit code " + sysCmdResult.getExitCode());
            List<String> output = sysCmdResult.getOutput();
            if (output.isEmpty())
                throw new VcsException("Couldn't get default branch by executing: " + cmd);
            if (output.size() > 1)
                LOG.warn("Strange! Executing cmd {} gives more than 1 row", cmd);
            return output.get(0);
        } catch (SysCmdException e) {
            throw new VcsException(e);
        }
    }

    enum ChangeType {
        Untracked("?"),
        Added("A"),
        Modified("M"),
        FileTypeChanged("T"),
        Renamed("R"),
        Copied("C"),
        Deleted("D");

        final String symbol;

        ChangeType(String symbol) {
            this.symbol = symbol;
        }

        static ChangeType fromStr(String symbol) {
            return stream(values())
                    .filter(r -> r.symbol.equalsIgnoreCase(symbol))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Unknown symbol: " + symbol));
        }
    }

    @Value
    static class Change {

        ChangeType stagingAreaChangeType;
        ChangeType workingTreeChangeType;
        Set<Path> paths;

        Change(String row, Path repoPath) {
            try {
                StringTokenizer st = new StringTokenizer(row);
                String changeTypes = st.nextToken();
                stagingAreaChangeType = ChangeType.fromStr(changeTypes.substring(0, 1));
                if (changeTypes.length() == 2)
                    workingTreeChangeType = ChangeType.fromStr(changeTypes.substring(1, 2));
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
}
