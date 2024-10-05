package io.icaco.maven;

import io.icaco.core.vcs.VcsChangesCommand;
import io.icaco.core.vcs.VcsType;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;
import java.util.List;

import static io.icaco.core.vcs.VcsType.Git;
import static io.icaco.core.vcs.VcsType.findVcsType;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;
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
        Path sourcePath = Path.of(project.getBuild().getSourceDirectory()).toAbsolutePath();
        Path basePath = project.getBasedir().toPath();
        VcsType vcs = findVcsType(vcsType).orElse(Git);
        return VcsChangesCommand.create(vcs, basePath)
                .execute()
                .stream()
                .filter(path -> path.startsWith(sourcePath))
                .map(Path::toString)
                .filter(s -> s.endsWith(".java"))
                .map(s -> s.replace(sourcePath + "/", ""))
                .map(s -> s.replace(".java", ".class"))
                .collect(toList());
    }

}
