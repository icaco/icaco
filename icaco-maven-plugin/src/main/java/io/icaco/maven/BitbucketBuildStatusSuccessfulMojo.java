package io.icaco.maven;

import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "bitbucket-set-build-status-successful")
public class BitbucketBuildStatusSuccessfulMojo extends AbstractBitbucketBuildStatusMojo {


    @Override
    String buildState() {
        return "SUCCESSFUL";
    }
}