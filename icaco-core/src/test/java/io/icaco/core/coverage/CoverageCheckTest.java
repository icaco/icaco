package io.icaco.core.coverage;

import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.check.Limit;
import org.jacoco.report.check.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.jacoco.core.analysis.ICoverageNode.ElementType.BUNDLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoverageCheckTest {

    final File execFile = new File("target/coverage-check-test/jacoco.exec");
    final File classesDirectory = new File("target/classes");
    final String resultClass = "io/icaco/core/coverage/CoverageCheckResult.class";

    @BeforeEach
    void createExecFile() throws Exception {
        // An execution data file without any data, which gives 0% coverage
        new ExecFileLoader().save(execFile, false);
    }

    static Rule lineCoverageRule(String minimum) {
        Limit limit = new Limit();
        limit.setCounter("LINE");
        limit.setValue("COVEREDRATIO");
        limit.setMinimum(minimum);
        Rule rule = new Rule();
        rule.setElement(BUNDLE);
        rule.setLimits(List.of(limit));
        return rule;
    }

    CoverageCheck.CoverageCheckBuilder check(String minimum) {
        return CoverageCheck.builder()
                .execFile(execFile)
                .classesDirectory(classesDirectory)
                .bundleName("test")
                .rules(List.of(lineCoverageRule(minimum)));
    }

    @Test
    void coverageMet() throws Exception {
        // When
        CoverageCheckResult result = check("0.0").includes(List.of(resultClass)).build().execute();
        // Then
        assertTrue(result.isMet());
        assertEquals("test", result.getBundleName());
        assertEquals(1, result.getClassCount());
    }

    @Test
    void coverageNotMet() throws Exception {
        // When
        CoverageCheckResult result = check("0.5").includes(List.of(resultClass)).build().execute();
        // Then
        assertFalse(result.isMet());
        assertEquals(1, result.getViolations().size());
        assertTrue(result.getViolations().get(0).contains("lines covered ratio is 0.0, but expected minimum is 0.5"),
                result.getViolations().get(0));
    }

    @Test
    void emptyIncludesIncludesAllClasses() throws Exception {
        // When
        CoverageCheckResult result = check("0.0").build().execute();
        // Then
        assertTrue(result.getClassCount() > 1);
    }

    @Test
    void innerClassPattern() throws Exception {
        // When
        CoverageCheckResult result = check("0.0")
                .classesDirectory(new File("target/test-classes"))
                .includes(List.of("io/icaco/core/coverage/fixture/Outer.class", "io/icaco/core/coverage/fixture/Outer$*.class"))
                .build()
                .execute();
        // Then Outer, Outer$Inner and Outer$1
        assertEquals(3, result.getClassCount());
    }

    @Test
    void excludes() throws Exception {
        // When
        CoverageCheckResult result = check("0.0")
                .includes(List.of("io/icaco/core/coverage/**"))
                .excludes(List.of("**/CoverageCheck*.class"))
                .build()
                .execute();
        // Then
        assertEquals(2, result.getClassCount());
    }

}
