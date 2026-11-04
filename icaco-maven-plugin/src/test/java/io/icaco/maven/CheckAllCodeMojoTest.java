package io.icaco.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.check.Limit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckAllCodeMojoTest {

    File dataFile = new File("target/check-all-code-test/jacoco.exec");

    CheckAllCodeMojo mojo;

    @BeforeEach
    void setUp() throws Exception {
        mojo = MojoDefaults.apply(new CheckAllCodeMojo(), "check-all-code");
        // An execution data file without any data, which gives 0% coverage
        new ExecFileLoader().save(dataFile, false);
        mojo.project = new MavenProject();
        mojo.project.setName("check-all-code-test");
        mojo.project.getBuild().setOutputDirectory("target/classes");
        mojo.dataFile = dataFile;
        mojo.includes = List.of("io/icaco/maven/RuleConfiguration.class");
    }

    static RuleConfiguration lineCoverageRule(String minimum) {
        Limit limit = new Limit();
        limit.setCounter("LINE");
        limit.setValue("COVEREDRATIO");
        limit.setMinimum(minimum);
        RuleConfiguration rule = new RuleConfiguration();
        rule.setElement("BUNDLE");
        rule.setLimits(List.of(limit));
        return rule;
    }

    @Test
    void coverageMet() throws Exception {
        // Given
        mojo.rules = List.of(lineCoverageRule("0.0"));
        // When
        mojo.execute();
        // Then
        assertFalse(mojo.violations);
    }

    @Test
    void coverageNotMet() {
        // Given
        mojo.rules = List.of(lineCoverageRule("0.5"));
        // When Then
        assertThrows(MojoExecutionException.class, mojo::execute);
        assertTrue(mojo.violations);
    }

    @Test
    void coverageNotMetWithoutHaltOnFailure() throws Exception {
        // Given
        mojo.rules = List.of(lineCoverageRule("0.5"));
        mojo.haltOnFailure = false;
        // When
        mojo.execute();
        // Then
        assertTrue(mojo.violations);
    }

    @Test
    void missingDataFile() throws Exception {
        // Given
        mojo.rules = List.of(lineCoverageRule("0.5"));
        mojo.dataFile = new File("target/check-all-code-test/missing.exec");
        // When
        mojo.execute();
        // Then
        assertFalse(mojo.violations);
    }

    @Test
    void skip() throws Exception {
        // Given
        mojo.rules = List.of(lineCoverageRule("0.5"));
        mojo.skip = true;
        // When
        mojo.execute();
        // Then
        assertFalse(mojo.violations);
    }

}
