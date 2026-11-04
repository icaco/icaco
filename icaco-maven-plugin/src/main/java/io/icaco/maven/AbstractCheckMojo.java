package io.icaco.maven;

import io.icaco.core.coverage.CoverageCheck;
import io.icaco.core.coverage.CoverageCheckResult;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public abstract class AbstractCheckMojo extends AbstractMojo {

    static final String CHECK_SUCCESS = "All coverage checks have been met.";
    static final String CHECK_FAILED = "Coverage checks have not been met. See log for details.";

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "jacoco.skip", defaultValue = "false")
    boolean skip;

    @Parameter(required = true)
    List<RuleConfiguration> rules;

    @Parameter(property = "jacoco.haltOnFailure", defaultValue = "true", required = true)
    boolean haltOnFailure;

    @Parameter(defaultValue = "${project.build.directory}/jacoco.exec")
    File dataFile;

    @Parameter
    List<String> includes;

    @Parameter
    List<String> excludes;

    boolean violations;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip)
            getLog().info("Skipping JaCoCo execution because property jacoco.skip is set.");
        else
            executeMojo();
    }

    protected void executeMojo() throws MojoExecutionException {
        if (canCheckCoverage())
            executeCheck();
    }

    protected MavenProject getProject() {
        return project;
    }

    File classesDirectory() {
        return new File(project.getBuild().getOutputDirectory());
    }

    boolean canCheckCoverage() {
        if (!dataFile.exists()) {
            getLog().info("Skipping JaCoCo execution due to missing execution data file:" + dataFile);
            return false;
        }
        if (!classesDirectory().exists()) {
            getLog().info("Skipping JaCoCo execution due to missing classes directory:" + classesDirectory());
            return false;
        }
        return true;
    }

    void executeCheck() throws MojoExecutionException {
        CoverageCheckResult result;
        try {
            result = CoverageCheck.builder()
                    .execFile(dataFile)
                    .classesDirectory(classesDirectory())
                    .bundleName(ofNullable(project.getName()).orElse(project.getArtifactId()))
                    .rules(rules.stream().map(r -> r.rule).collect(toList()))
                    .includes(ofNullable(includes).orElse(List.of()))
                    .excludes(ofNullable(excludes).orElse(List.of()))
                    .build()
                    .execute();
        } catch (IOException e) {
            throw new MojoExecutionException("Error while checking code coverage: " + e.getMessage(), e);
        }
        getLog().info(format("Analyzed bundle '%s' with %d classes", result.getBundleName(), result.getClassCount()));
        result.getViolations().forEach(getLog()::warn);
        violations = !result.isMet();
        if (!violations)
            getLog().info(CHECK_SUCCESS);
        else if (haltOnFailure)
            throw new MojoExecutionException(CHECK_FAILED);
        else
            getLog().warn(CHECK_FAILED);
    }

}
