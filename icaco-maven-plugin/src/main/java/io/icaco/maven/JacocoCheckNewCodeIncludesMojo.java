package io.icaco.maven;

import io.icaco.core.vcs.VcsChanges;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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
    public void execute() throws MojoExecutionException {
        try {
            List<String> changedClassFiles = getChangedClassFiles();
            changedClassFiles.forEach(f -> getLog().info("Changed class file: " + f));
            project.getProperties().setProperty(jacocoCheckNewCodeIncludesPropertyName, join(",", changedClassFiles));
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException(e);
        }
    }

    List<String> getChangedClassFiles() throws IOException, InterruptedException {
        Path sourcePath = Path.of(project.getBuild().getSourceDirectory()).toAbsolutePath();
        return VcsChanges.create(vcsType, project.getBasedir().toPath())
                .list()
                .stream()
                .filter(path -> path.startsWith(sourcePath))
                .map(Path::toString)
                .filter(s -> s.endsWith(".java"))
                .map(s -> s.replace(sourcePath + "/", ""))
                .map(s -> s.replace(".java", ".class"))
                .collect(toList());
    }

}
