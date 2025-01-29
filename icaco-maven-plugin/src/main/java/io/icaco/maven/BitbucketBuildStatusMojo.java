package io.icaco.maven;

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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.net.http.HttpClient.Version.HTTP_2;
import static java.time.Duration.ofSeconds;
import static org.apache.maven.plugins.annotations.LifecyclePhase.VALIDATE;

@Mojo(name = "bitbucket-set-build-status", defaultPhase = VALIDATE)
public class BitbucketBuildStatusMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Parameter(defaultValue = "${mojoExecution}", readonly = true, required = true)
    private MojoExecution mojoExecution;

    @Parameter(property = "bitbucket.url")
    private String bitbucketUrl;

    @Parameter(property = "bitbucket.token")
    private String bitbucketToken;

    @Parameter(property = "build.state", defaultValue = "INPROGRESS", required = true)
    private String buildState;

    @Parameter(property = "build.url")
    private String buildUrl;

    final HttpClient httpClient = HttpClient.newBuilder()
            .version(HTTP_2)
            .connectTimeout(ofSeconds(10))
            .build();

    String executeGitCommand(String... args) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String result = reader.readLine();
            if (result == null || result.isEmpty()) {
                throw new MojoExecutionException("Git command returned no output: " + String.join(" ", args));
            }
            return result;
        }
    }

    String getCommitHash() throws Exception {
        return executeGitCommand("git", "rev-parse", "HEAD");
    }

    String getBitbucketUrl() throws Exception {
        if (bitbucketUrl != null) {
            return bitbucketUrl;
        }

        String remoteUrl = executeGitCommand("git", "remote", "get-url", "origin");
        Pattern pattern = Pattern.compile("(?:https://|git@)([^:/]+)[:/](.+?)(?:\\.git)?$");
        Matcher matcher = pattern.matcher(remoteUrl);

        if (matcher.find()) {
            String host = matcher.group(1);
            return "https://" + host;
        }

        throw new MojoExecutionException("Could not determine Bitbucket URL from git remote: " + remoteUrl);
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
        return String.format("MAVEN-%s-%d",
                project.getArtifactId(),
                System.currentTimeMillis()
        );
    }

    String getBuildName() {
        return String.format("Maven Build - %s:%s",
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
            String commitHash = getCommitHash();
            String apiUrl = String.format("%s/rest/build-status/1.0/commits/%s",
                    getBitbucketUrl(),
                    commitHash
            );
            JsonObject buildStatus = Json.createObjectBuilder()
                    .add("state", buildState)
                    .add("key", generateBuildKey())
                    .add("name", getBuildName())
                    .add("url", buildUrl != null ? buildUrl : "")
                    .build();
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
            getLog().info(String.format(
                    "Build status updated to %s for commit %s",
                    buildStatus.getString("state"),
                    commitHash
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