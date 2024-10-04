package io.icaco.core.vcs.git;

import io.icaco.core.syscmd.SysCmdResult;
import io.icaco.core.vcs.VcsChangesCommand;
import io.icaco.core.vcs.VcsException;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static io.icaco.core.vcs.git.GitChangeType.Deleted;
import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;

public class GitChangesCommand extends GitCommand implements VcsChangesCommand {

    private static final Logger LOG = getLogger(GitChangesCommand.class);

    public GitChangesCommand(Path workingDir) {
        super(workingDir);
    }

    @Override
    public Set<Path> execute() {
        if (!validRepo)
            return Set.of();
        Set<Path> result = new LinkedHashSet<>(listLocalChanges());
        getDefaultBranch().ifPresentOrElse(
                defaultBranch -> result.addAll(listRemoteChanges(defaultBranch)),
                () -> LOG.info("git repository has no remote")
        );
        return result;
    }

    Set<Path> listRemoteChanges(String defaultBranch) {
        return listChanges("diff", "--name-status", "--no-renames", defaultBranch);
    }

    Set<Path> listLocalChanges() {
        return listChanges("status", "--porcelain", "--no-renames");
    }

    Set<Path> listChanges(String... gitArguments) {
        SysCmdResult result = execGit(gitArguments);
        if (result.getExitCode() != 0)
            throw new VcsException(result);
        return result
                .getOutput()
                .stream()
                .map(row -> new GitChange(row, repoPath))
                .filter(c -> c.getStagingAreaChangeType() != Deleted)
                .filter(c -> c.getWorkingTreeChangeType() != Deleted)
                .map(GitChange::getPaths)
                .flatMap(Collection::stream)
                .map(Path::toAbsolutePath)
                .collect(toSet());
    }

    Optional<String> getDefaultBranch() {
        SysCmdResult result = execGit("symbolic-ref", "refs/remotes/origin/HEAD");
        if (result.getExitCode() == 128)
            return Optional.empty();
        if (result.getExitCode() != 0)
            throw new VcsException(result);
        if (result.getOutput().isEmpty())
            throw new VcsException("Couldn't get default branch by executing: " + result.getCommand());
        LOG.info("default branch: {}", result.getSingleValueOutput());
        return Optional.of(result.getSingleValueOutput());
    }

}
