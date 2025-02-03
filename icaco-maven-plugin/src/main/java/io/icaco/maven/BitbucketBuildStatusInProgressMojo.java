package io.icaco.maven;

import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "bitbucket-set-build-status-in-progress")
public class BitbucketBuildStatusInProgressMojo extends AbstractBitbucketBuildStatusMojo {


    @Override
    String buildState() {
        return "INPROGRESS";
    }
}