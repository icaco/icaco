package io.icaco.maven;


import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.WithoutMojo;

import org.junit.Rule;
import static org.junit.Assert.*;
import org.junit.Test;

import java.io.File;

public class MyMojoTest {

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
        File pom = new File( "target/test-classes/project-to-test/" );
        assertNotNull( pom );
        assertTrue( pom.exists() );

        JacocoBranchCheckIncludesMojo myMojo = (JacocoBranchCheckIncludesMojo) rule.lookupConfiguredMojo( pom, "jacocoBranchCheckIncludes" );
        assertNotNull( myMojo );
        myMojo.execute();

        String property = myMojo.project.getProperties().getProperty("jacoco.branch.check.includes");
        System.out.println(property);
    }

    /** Do not need the MojoRule. */
    @WithoutMojo
    @Test
    public void testSomethingWhichDoesNotNeedTheMojoAndProbablyShouldBeExtractedIntoANewClassOfItsOwn() {
        assertTrue( true );
    }

}

