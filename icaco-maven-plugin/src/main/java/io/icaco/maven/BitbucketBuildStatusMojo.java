package io.icaco.maven;

import io.icaco.core.vcs.VcsLatestCommitCommand;
import io.icaco.core.vcs.VcsRemoteUrlCommand;
import io.icaco.core.vcs.VcsType;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import javax.json.Json;
import javax.json.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.util.regex.Matcher;

import static io.icaco.core.vcs.VcsType.Git;
import static io.icaco.core.vcs.VcsType.findVcsType;
import static java.lang.String.format;
import static java.net.http.HttpClient.Version.HTTP_2;
import static java.time.Duration.ofSeconds;
import static java.util.regex.Pattern.compile;
import static org.apache.maven.plugins.annotations.LifecyclePhase.VALIDATE;

@Mojo(name = "bitbucket-set-build-status", defaultPhase = VALIDATE)
public class BitbucketBuildStatusMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    MavenSession session;

    @Parameter(defaultValue = "${mojoExecution}", readonly = true, required = true)
    MojoExecution mojoExecution;

    @Parameter(property = "vcsType", defaultValue = "git")
    String vcsType;

    @Parameter(property = "bitbucket.token")
    String bitbucketToken;

    @Parameter(property = "build.state", defaultValue = "INPROGRESS", required = true)
    String buildState;

    @Parameter(property = "build.url")
    String buildUrl;

    final HttpClient httpClient = HttpClient.newBuilder()
            .version(HTTP_2)
            .connectTimeout(ofSeconds(10))
            .build();

    String getLatestCommit(VcsType vcs, Path basePath) throws MojoExecutionException {
        return VcsLatestCommitCommand.create(vcs, basePath)
                .execute()
                .orElseThrow(() -> new MojoExecutionException("Could not determine latest commit"));
    }

    String getBitbucketUrl(VcsType vcs, Path basePath) throws MojoExecutionException {
        return VcsRemoteUrlCommand.create(vcs, basePath)
                .execute()
                .map(remoteUrl -> compile("(?:https://|git@)([^:/]+)[:/](.+?)(?:\\.git)?$").matcher(remoteUrl))
                .filter(Matcher::find)
                .map(m -> "https://" + m.group(1))
                .orElseThrow(() -> new MojoExecutionException("Could not determine Bitbucket URL from git remote"));

    }

    String getBitbucketToken() throws MojoExecutionException {
        if (bitbucketToken != null) {
            return bitbucketToken;
        }
        String token = System.getenv("BITBUCKET_TOKEN");
        if (token != null && !token.isEmpty()) {
            return token;
        }
        throw new MojoExecutionException(
                "No Bitbucket token found. Please set either bitbucket.token property " +
                        "or BITBUCKET_TOKEN environment variable"
        );
    }

    String generateBuildKey() {
        return format("MAVEN-%s-%d",
                project.getArtifactId(),
                System.currentTimeMillis()
        );
    }

    String getBuildName() {
        return format("Maven Build - %s:%s",
                project.getGroupId(),
                project.getArtifactId()
        );
    }

    boolean shouldFailOnError() {
        MavenExecutionResult result = session.getResult();
        return result == null || !result.hasExceptions();
    }

    @Override
    public void execute() throws MojoExecutionException {
        try {
            VcsType vcs = findVcsType(vcsType).orElse(Git);
            Path basePath = project.getBasedir().toPath();
            String latestCommit = getLatestCommit(vcs, basePath);
            String apiUrl = format("%s/rest/build-status/1.0/commits/%s",
                    getBitbucketUrl(vcs, basePath),
                    latestCommit
            );
            System.out.println(apiUrl);
            if (true)
                return;
            JsonObject buildStatus = Json.createObjectBuilder()
                    .add("state", buildState)
                    .add("key", generateBuildKey())
                    .add("name", getBuildName())
                    .add("url", buildUrl != null ? buildUrl : "")
                    .build();
            System.out.println(buildStatus.toString());
            if (true)
                return;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + getBitbucketToken())
                    .POST(HttpRequest.BodyPublishers.ofString(buildStatus.toString()))
                    .build();
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new MojoExecutionException(
                        "Failed to update build status. Server returned: " +
                                response.statusCode() + " " + response.body()
                );
            }
            getLog().info(format(
                    "Build status updated to %s for commit %s",
                    buildStatus.getString("state"),
                    latestCommit
            ));
        } catch (Exception e) {
            if (shouldFailOnError()) {
                throw new MojoExecutionException("Error updating build status", e);
            } else {
                getLog().error("Error updating build status", e);
            }
        }
    }
}