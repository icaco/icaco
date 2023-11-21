package io.icaco.core.vcs;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.eclipse.jgit.api.Git.open;
import static org.eclipse.jgit.diff.DiffEntry.ChangeType.DELETE;

class GitChanges implements VcsChanges {

    final Git git;
    final Repository repository;

    GitChanges(Git git) {
        this.git = git;
        this.repository = git.getRepository();
    }

    GitChanges(Path path){
        this(path.toFile());
    }

    GitChanges(File file){
        this(openGitWithUncheckedException(file));
    }

    static Git openGitWithUncheckedException(File file) {
        try {
            return open(file);
        } catch (IOException e) {
            throw new VcsException(e);
        }
    }

    @Override
    public Set<String> list() {
        try {
            Set<String> result = new HashSet<>();
            Status status = git.status().call();
            result.addAll(status.getUntracked());
            result.addAll(status.getAdded());
            result.addAll(status.getChanged());
            result.addAll(status.getModified());
            result.addAll(branchDiff());
            return result;
        } catch (GitAPIException | IOException e) {
            throw new VcsException(e);
        }
    }

    Set<String> branchDiff() throws IOException, GitAPIException {
        AbstractTreeIterator currentBranchTree = treeIterator(currentBranch());
        AbstractTreeIterator defaultBranchTree = treeIterator(defaultBranch());
        return git.diff()
                .setOldTree(defaultBranchTree)
                .setNewTree(currentBranchTree)
                .call()
                .stream()
                .filter(e -> e.getChangeType() != DELETE)
                .map(DiffEntry::getNewPath)
                .collect(Collectors.toSet());
    }

    Ref defaultBranch() throws GitAPIException {
        return git.lsRemote()
                .call()
                .stream()
                .filter(Ref::isSymbolic)
                .findFirst()
                .flatMap(r -> ofNullable(r.getTarget()))
                .orElseThrow();
    }

    Ref currentBranch() throws IOException {
        return repository.findRef(repository.getBranch());
    }

    AbstractTreeIterator treeIterator(Ref ref) throws IOException {
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(ref.getObjectId());
            RevTree tree = walk.parseTree(commit.getTree().getId());
            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }
            walk.dispose();
            return treeParser;
        }
    }
}
