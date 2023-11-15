package io.icaco.maven;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Runtime.getRuntime;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;
import static org.apache.maven.plugins.annotations.LifecyclePhase.INITIALIZE;

@Mojo( name = "jacocoBranchCheckIncludes", defaultPhase = INITIALIZE)
public class JacocoBranchCheckIncludesMojo extends AbstractMojo {

    @Parameter(property = "project", readonly = true)
    MavenProject project;

    @Parameter(property = "jacoco.branch.check.includes.propertyName", defaultValue = "jacoco.branch.check.includes")
    String jacocoCheckIncludesPropertyName;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            String defaultBranch = getDefaultBranch();
            getLog().info("Default branch is '" + defaultBranch + "'");
            List<String> changedClassFiles = getChangedClassFiles(defaultBranch);
            changedClassFiles.forEach(f -> getLog().info("Changed class file: " + f));
            project.getProperties().setProperty(jacocoCheckIncludesPropertyName, join(",", changedClassFiles));
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException(e);
        }
    }

    List<String> getChangedClassFiles(String defaultBranch) throws IOException, InterruptedException {
        // TODO filter in module
        String cmd = "git diff --name-status " + defaultBranch;
        List<String> result = execCmd(cmd);
        return result
                .stream()
                .filter(s -> s.endsWith(".java"))
                .filter(s -> !s.startsWith("D "))
                .filter(s -> !s.contains("src/test/java"))
                .map(s -> s.substring(s.indexOf("src/main/java/") + "src/main/java/".length()))
                .map(s -> s.replace(".java", ".class"))
                .collect(toList());
    }

    String getDefaultBranch() throws IOException, InterruptedException, MojoExecutionException {
        String cmd = "git symbolic-ref refs/remotes/origin/HEAD";
        List<String> output = execCmd(cmd);
        if (output.isEmpty())
            throw new MojoExecutionException("Couldn't get default branch by executing: " + cmd);
        if (output.size() > 1)
            getLog().warn("Strange! Executing cmd '" + cmd + "' gives more than 1 row");
        return output.get(0);
    }

    List<String> execCmd(String cmd) throws IOException, InterruptedException {
        Process process = null;
        try {
            process = getRuntime().exec(cmd.split(" "));
            process.waitFor();
            return getProcessOutput(process);
        } finally {
            if (process != null)
                process.destroy();
        }
    }

    List<String> getProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            List<String> result = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null)
                result.add(line);
            return result;
        }
    }
}
