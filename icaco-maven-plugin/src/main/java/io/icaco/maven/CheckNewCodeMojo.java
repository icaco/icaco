package io.icaco.maven;

import io.icaco.core.coverage.ChangedClassFiles;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.nio.file.Path;
import java.util.List;

import static io.icaco.core.vcs.VcsType.Git;
import static io.icaco.core.vcs.VcsType.findVcsType;
import static org.apache.maven.plugins.annotations.LifecyclePhase.VERIFY;

@Mojo(name = "check-new-code", defaultPhase = VERIFY, threadSafe = true)
public class CheckNewCodeMojo extends AbstractCheckMojo {

    @Parameter(property = "vcsType", defaultValue = "git")
    String vcsType;

    @Override
    public void executeMojo() throws MojoExecutionException {
        includes = getChangedClassFiles();
        if (includes.isEmpty()) {
            getLog().info("Skipping coverage check because no classes have changed.");
            return;
        }
        includes.forEach(f -> getLog().info("Changed class file: " + f));
        super.executeMojo();
    }

    List<String> getChangedClassFiles() {
        return ChangedClassFiles.find(
                findVcsType(vcsType).orElse(Git),
                getProject().getBasedir().toPath(),
                Path.of(getProject().getBuild().getSourceDirectory())
        );
    }
}
