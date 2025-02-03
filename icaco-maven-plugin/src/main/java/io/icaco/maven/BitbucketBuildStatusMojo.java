package io.icaco.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "bitbucket-set-build-status")
public class BitbucketBuildStatusMojo extends AbstractBitbucketBuildStatusMojo {


    @Parameter(property = "buildState", required = true)
    String buildState;

    @Override
    String buildState() {
        return buildState;
    }
}