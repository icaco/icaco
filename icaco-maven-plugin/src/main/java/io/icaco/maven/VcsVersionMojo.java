package io.icaco.maven;

import io.icaco.core.vcs.VcsCurrentBranchCommand;
import io.icaco.core.vcs.VcsLatestTagCommand;
import io.icaco.core.vcs.VcsType;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;
import java.util.Optional;

import static io.icaco.core.vcs.VcsType.Git;
import static io.icaco.core.vcs.VcsType.findVcsType;
import static java.util.Optional.empty;
import static org.apache.maven.plugins.annotations.LifecyclePhase.VALIDATE;

@Mojo(name = "vcs-version", defaultPhase = VALIDATE)
public class VcsVersionMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    MavenProject project;

    @Parameter(property = "vcsType", defaultValue = "git")
    String vcsType;

    @Parameter(property = "defaultVersion", defaultValue = "0.0.0")
    String defaultVersion;

    @Parameter(property = "commitsAheadTagPostfix", defaultValue = "latest")
    String commitsAheadTagPostfix;

    @Parameter(property = "useJiraIdOnFeatureBranch")
    boolean useJiraIdOnFeatureBranch;

    @Parameter(property = "featureBranchPrefix", defaultValue = "feature/")
    String featureBranchPrefix;

    @Parameter(property = "useReleaseBranchVersionNumber")
    boolean useReleaseBranchVersionNumber;

    @Parameter(property = "releaseBranchPrefix", defaultValue = "release/")
    String releaseBranchPrefix;

    @Parameter(property = "vcsVersionPropertyName", defaultValue = "icaco.vcs.version")
    String vcsVersionPropertyName;

    @Parameter(property = "mainBranchName", defaultValue = "main")
    String mainBranchName;

    @Parameter(property = "useLatestVersionOnMainBranch")
    boolean useLatestVersionOnMainBranch;

    @Override
    public void execute() {
        String vcsVersion = computeVersion();
        getLog().info(vcsVersionPropertyName + ": " + vcsVersion);
        project.getProperties().setProperty(vcsVersionPropertyName, vcsVersion);
    }

    String computeVersion() {
        VcsType vcs = findVcsType(vcsType).orElse(Git);
        Path basePath = project.getBasedir().toPath();
        Optional<String> currentBranch = VcsCurrentBranchCommand.create(vcs, basePath).execute();
        if (currentBranch.isPresent()) {
            String branch = currentBranch.get();
            if (useJiraIdOnFeatureBranch && branch.startsWith(featureBranchPrefix)) {
                Optional<String> jiraId = getJiraId(currentBranch.get());
                if (jiraId.isPresent())
                    return jiraId.get();
            }
            if (useReleaseBranchVersionNumber && branch.startsWith(releaseBranchPrefix))
                return branch.replace(releaseBranchPrefix, "").replace("/", "");
            if (useLatestVersionOnMainBranch && branch.equals(mainBranchName))
                return "latest";
        }
        return VcsLatestTagCommand.create(vcs, basePath)
                .execute()
                .map(e -> e.getName() + (e.hasCommitsOnTag() ? "-" + commitsAheadTagPostfix : ""))
                .orElse(defaultVersion);
    }

    Optional<String> getJiraId(String branch) {
        String[] tokens = branch.replace(featureBranchPrefix, "")
                .replace("/", "")
                .split("-");
        if (tokens.length >= 2) {
            return Optional.of(tokens[0] + "-" + tokens[1] + "-" + commitsAheadTagPostfix);
        } else
            getLog().warn("Not found jira id on feature branch '" + branch + "'. Using version from tag instead.");
        return empty();
    }

}
