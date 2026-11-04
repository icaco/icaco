package io.icaco.maven;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static io.icaco.core.syscmd.SysCmd.exec;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JacocoCheckNewCodeIncludesMojoTest {

    Path repoPath = Path.of("target/icaco-git-test");

    @BeforeEach
    void cloneRepo() throws Exception {
        removeRepo();
        exec("git clone https://github.com/icaco/icaco-git-test.git " + repoPath);
    }

    @AfterEach
    void removeRepo() throws Exception {
        deleteDirectory(repoPath.toFile());
    }


    @Test
    void execute() throws Exception {
        // Given
        exec("git -C " + repoPath.toAbsolutePath() + " checkout feature/issue1");
        JacocoCheckNewCodeIncludesMojo myMojo = MojoDefaults.apply(new JacocoCheckNewCodeIncludesMojo(), "jacocoCheckNewCodeIncludes");
        myMojo.project = new MavenProject();
        myMojo.project.setFile(repoPath.resolve("pom.xml").toAbsolutePath().toFile());
        myMojo.project.getBuild().setSourceDirectory(repoPath.resolve("src/main/java").toAbsolutePath().toString());
        // When
        myMojo.execute();
        // Then
        String property = myMojo.project.getProperties().getProperty("jacoco.check.new.code.includes");
        assertEquals("icaco/Test.class,icaco/Test$*.class", property);
    }


}

