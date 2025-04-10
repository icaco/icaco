package io.icaco.maven;

import io.icaco.core.vcs.VcsLatestCommitCommand;
import io.icaco.core.vcs.VcsRemoteUrlCommand;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static io.icaco.core.vcs.VcsType.Git;
import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.net.http.HttpClient.Version.HTTP_2;
import static java.time.Duration.ofSeconds;
import static java.util.Optional.ofNullable;

@Mojo(name = "bitbucket-set-build-status")
public class BitbucketBuildStatusMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "gitCommit")
    String gitCommit;

    @Parameter(property = "bitbucketAuthToken")
    String bitbucketAuthToken;

    @Parameter(property = "buildState")
    String buildState;

    @Parameter(property = "buildUrl")
    String buildUrl;

    @Parameter(property = "doNotExecuteInSubModules", defaultValue = "true")
    boolean doNotExecuteInSubModules;

    final HttpClient httpClient = HttpClient.newBuilder()
            .version(HTTP_2)
            .connectTimeout(ofSeconds(10))
            .build();

    String gitCommit() throws MojoExecutionException {
        if (gitCommit != null)
            return gitCommit;
        String envGitCommit = getenv("GIT_COMMIT");
        if (envGitCommit != null)
            return envGitCommit;
        return VcsLatestCommitCommand
                .create(Git, project.getBasedir().toPath())
                .execute()
                .orElseThrow(() -> new MojoExecutionException("Could not determine latest commit"));
    }

    String authToken() throws MojoExecutionException {
        if (bitbucketAuthToken != null)
            return bitbucketAuthToken;
        return ofNullable(getenv("BITBUCKET_AUTH_TOKEN"))
                .orElseThrow(() -> new MojoExecutionException("Could not determine bitbucketAuthToken"));
    }

    String buildState() throws MojoExecutionException {
        if (buildState != null)
            return buildState;
        return ofNullable(getenv("BUILD_STATE"))
                .orElseThrow(() -> new MojoExecutionException("Could not determine buildState"));
    }

    String buildUrl() {
        if (buildUrl != null)
            return buildUrl;
        return ofNullable(getenv("BUILD_URL"))
                .orElse("https://about.gitlab.com/");
    }

    String bitbucketHostName() throws MojoExecutionException {
        return VcsRemoteUrlCommand
                .create(Git, project.getBasedir().toPath())
                .execute()
                .map(URI::create)
                .map(URI::getHost)
                .orElseThrow(() -> new MojoExecutionException("Could not determine Bitbucket URL from git remote"));
    }

    URI apiUri(String gitCommit) throws MojoExecutionException {
        String uri = format("https://%s/rest/build-status/1.0/commits/%s", bitbucketHostName(), gitCommit);
        getLog().info("API URI: " + uri);
        return URI.create(uri);
    }

    String payload(String gitCommit) throws MojoExecutionException {
        JsonObject jsonObject = Json.createObjectBuilder()
                .add("state", buildState())
                .add("key", gitCommit)
                .add("name", "Commit " + gitCommit)
                .add("url", buildUrl())
                .build();
        String requestBody = jsonObject.toString();
        getLog().info("Request Body: " + requestBody);
        return requestBody;
    }

    HttpRequest httpRequest(String gitCommit) throws MojoExecutionException {
        return HttpRequest.newBuilder()
                .uri(apiUri(gitCommit))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken())
                .POST(HttpRequest.BodyPublishers.ofString(payload(gitCommit)))
                .build();
    }

    @Override
    public void execute() throws MojoExecutionException {
        try {
            if (doNotExecuteInSubModules && project.hasParent())
                getLog().info("Skipping execution in submodule.");
            else {
                String commit = gitCommit();
                HttpRequest request = httpRequest(commit);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 204)
                    throw new MojoExecutionException(
                            "Failed to update build status. Server returned: " + response.statusCode() + " " + response.body()
                    );
                getLog().info(format("Build status updated to %s for commit %s", buildState(), commit));
            }
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException("Error updating build status", e);
        }
    }

}