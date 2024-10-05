package io.icaco.maven;

import org.apache.maven.plugin.testing.MojoRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.nio.file.Path;

import static io.icaco.core.syscmd.SysCmd.exec;
import static io.icaco.maven.VcsBranchVersionMojo.BRANCH_VERSION_PROPERTY_NAME;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.junit.Assert.assertEquals;

public class VcsBranchVersionMojoTest {

    @Rule
    public MojoRule rule = new MojoRule();

    Path repoPath = Path.of("target/icaco-git-test");

    @Before
    public void cloneRepo() throws Exception {
        removeRepo();
        exec("git clone https://github.com/icaco/icaco-git-test.git " + repoPath);
    }

    @After
    public void removeRepo() throws Exception {
        deleteDirectory(repoPath.toFile());
    }


    @Test
    public void execute() throws Exception {
        // Given
        exec("git -C " + repoPath.toAbsolutePath() + " checkout feature/issue1");
        exec("git -C " + repoPath.toAbsolutePath() + " tag 1.1.1");
        VcsBranchVersionMojo myMojo = (VcsBranchVersionMojo) rule.lookupConfiguredMojo(repoPath.toFile(), "vcs-version");
        // When
        myMojo.execute();
        // Then
        String property = myMojo.project.getProperties().getProperty(BRANCH_VERSION_PROPERTY_NAME);
        assertEquals("1.1.1", property);
    }


}

