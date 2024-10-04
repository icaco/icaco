package io.icaco.maven;

import io.icaco.core.vcs.VcsLatestTagCommand;
import io.icaco.core.vcs.VcsType;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;

import static io.icaco.core.vcs.VcsType.Git;
import static io.icaco.core.vcs.VcsType.findVcsType;
import static org.apache.maven.plugins.annotations.LifecyclePhase.VALIDATE;

@Mojo(name = "vcs-version", defaultPhase = VALIDATE)
public class VcsBranchVersionMojo extends AbstractMojo {

    public static final String BRANCH_VERSION_PROPERTY_NAME = "icaco.branch.version";
    @Parameter(defaultValue = "${project}", readonly = true)
    MavenProject project;

    @Parameter(property = "vcsType", defaultValue = "git")
    String vcsType;

    @Parameter(property = "defaultVersion", defaultValue = "0.0.0-SNAPSHOT")
    String defaultVersion;

    @Override
    public void execute() {
        String branchVersion = computeVersion();
        getLog().info(BRANCH_VERSION_PROPERTY_NAME + ": " + branchVersion);
        project.getProperties().setProperty(BRANCH_VERSION_PROPERTY_NAME, branchVersion);
    }

    String computeVersion() {
        VcsType vcs = findVcsType(vcsType).orElse(Git);
        Path basePath = project.getBasedir().toPath();
        return VcsLatestTagCommand.create(vcs, basePath)
                .execute()
                .orElse(defaultVersion);
    }

}
