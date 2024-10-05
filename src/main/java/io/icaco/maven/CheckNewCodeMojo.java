package io.icaco.maven;

import io.icaco.core.vcs.VcsChangesCommand;
import io.icaco.core.vcs.VcsType;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jacoco.maven.IcacoAbstractCheckMojo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static io.icaco.core.vcs.VcsType.Git;
import static io.icaco.core.vcs.VcsType.findVcsType;
import static java.util.stream.Collectors.toList;
import static org.apache.maven.plugins.annotations.LifecyclePhase.VERIFY;

@Mojo(name = "check-new-code", defaultPhase = VERIFY, threadSafe = true)
public class CheckNewCodeMojo extends IcacoAbstractCheckMojo {

    @Parameter(property = "vcsType", defaultValue = "git")
    String vcsType;

    @Override
    public void executeMojo() throws MojoExecutionException {
        try {
            includes = getChangedClassFiles();
            includes.forEach(f -> getLog().info("Changed class file: " + f));
            super.executeMojo();
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException(e);
        }
    }

    List<String> getChangedClassFiles() throws IOException, InterruptedException {
        Path sourcePath = Path.of(getProject().getBuild().getSourceDirectory()).toAbsolutePath();
        VcsType vcs = findVcsType(vcsType).orElse(Git);
        return VcsChangesCommand.create(vcs, getProject().getBasedir().toPath())
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
