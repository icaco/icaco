package io.icaco.maven;


import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.WithoutMojo;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JacocoCheckNewCodeIncludesMojoTest {

    @Rule
    public MojoRule rule = new MojoRule() {
        @Override
        protected void before() {
        }

        @Override
        protected void after() {
        }
    };

    @Test
    public void testSomething() throws Exception {
        File pom = new File("target/test-classes/project-to-test/");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JacocoCheckNewCodeIncludesMojo myMojo = (JacocoCheckNewCodeIncludesMojo) rule.lookupConfiguredMojo(pom, "jacocoCheckNewCodeIncludes");
        assertNotNull(myMojo);
        myMojo.execute();

        String property = myMojo.project.getProperties().getProperty("jacoco.check.new.code.includes");
        System.out.println(property);
    }

    /**
     * Do not need the MojoRule.
     */
    @WithoutMojo
    @Test
    public void testSomethingWhichDoesNotNeedTheMojoAndProbablyShouldBeExtractedIntoANewClassOfItsOwn() {
        assertTrue(true);
    }

}

