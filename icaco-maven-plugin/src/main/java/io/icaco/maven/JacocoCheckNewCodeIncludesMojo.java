package io.icaco.maven;

import io.icaco.core.coverage.ChangedClassFiles;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;
import java.util.List;

import static io.icaco.core.vcs.VcsType.Git;
import static io.icaco.core.vcs.VcsType.findVcsType;
import static java.lang.String.join;
import static org.apache.maven.plugins.annotations.LifecyclePhase.INITIALIZE;

@Mojo(name = "jacocoCheckNewCodeIncludes", defaultPhase = INITIALIZE)
public class JacocoCheckNewCodeIncludesMojo extends AbstractMojo {

    @Parameter(property = "project", readonly = true)
    MavenProject project;

    @Parameter(property = "jacoco.check.new.code.includes.propertyName", defaultValue = "jacoco.check.new.code.includes")
    String jacocoCheckNewCodeIncludesPropertyName;

    @Parameter(property = "vcsType", defaultValue = "git")
    String vcsType;

    @Override
    public void execute() {
        List<String> changedClassFiles = getChangedClassFiles();
        changedClassFiles.forEach(f -> getLog().info("Changed class file: " + f));
        project.getProperties().setProperty(jacocoCheckNewCodeIncludesPropertyName, join(",", changedClassFiles));
    }

    List<String> getChangedClassFiles() {
        return ChangedClassFiles.find(
                findVcsType(vcsType).orElse(Git),
                project.getBasedir().toPath(),
                Path.of(project.getBuild().getSourceDirectory())
        );
    }

}
