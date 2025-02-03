package io.icaco.maven;

import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "bitbucket-set-build-status-failed")
public class BitbucketBuildStatusFailedMojo extends AbstractBitbucketBuildStatusMojo {


    @Override
    String buildState() {
        return "FAILED";
    }
}