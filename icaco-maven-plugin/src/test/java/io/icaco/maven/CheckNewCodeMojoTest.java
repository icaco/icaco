package io.icaco.maven;

import org.apache.maven.project.MavenProject;
import org.jacoco.core.tools.ExecFileLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static io.icaco.core.syscmd.SysCmd.exec;
import static io.icaco.maven.CheckAllCodeMojoTest.lineCoverageRule;
import static java.nio.charset.Charset.defaultCharset;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.write;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

public class CheckNewCodeMojoTest {

    File repo = new File("target/check-new-code-test").getAbsoluteFile();

    CheckNewCodeMojo mojo = new CheckNewCodeMojo();

    @Before
    public void setUp() throws Exception {
        removeRepo();
        repo.mkdirs();
        exec("git -C " + repo, "init");
        File dataFile = new File(repo, "target/jacoco.exec");
        // An execution data file without any data, which gives 0% coverage
        new ExecFileLoader().save(dataFile, false);
        mojo.project = new MavenProject();
        mojo.project.setName("check-new-code-test");
        mojo.project.setFile(new File(repo, "pom.xml"));
        mojo.project.getBuild().setSourceDirectory(new File(repo, "src/main/java").getPath());
        mojo.project.getBuild().setOutputDirectory(new File("target/classes").getAbsolutePath());
        mojo.dataFile = dataFile;
        mojo.haltOnFailure = true;
        mojo.vcsType = "git";
        mojo.rules = List.of(lineCoverageRule("0.5"));
    }

    @After
    public void removeRepo() throws Exception {
        deleteDirectory(repo);
    }

    @Test
    public void noChangedClasses() throws Exception {
        // When
        mojo.execute();
        // Then
        assertFalse(mojo.violations);
    }

    @Test
    public void changedClass() throws Exception {
        // Given a changed source file for a class in target/classes
        write(new File(repo, "src/main/java/io/icaco/maven/RuleConfiguration.java"), "", defaultCharset());
        // When Then
        assertThrows(Exception.class, mojo::execute);
        assertEquals(List.of("io/icaco/maven/RuleConfiguration.class", "io/icaco/maven/RuleConfiguration$*.class"), mojo.includes);
    }

}
