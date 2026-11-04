# icaco

Tools for CI/CD pipelines. Icaco is a Maven plugin, plus a small core library,
that reads information from version control and uses it in the build. It can:

- work out a project version from git tags and branch names
- check JaCoCo coverage only on code changed compared with the default branch
- expose the JaCoCo agent as a property you can pass to any JVM
- report the build status of a commit to Bitbucket

Released to Maven Central as `io.github.icaco:icaco-maven-plugin`.

## Requirements

- Java 11 or later
- Maven 3.9 or later
- `git` on the `PATH`. All VCS operations run the git CLI in the project's base directory.

## Modules

| Module               | Description                                                                                                                |
|----------------------|----------------------------------------------------------------------------------------------------------------------------|
| `icaco-core`         | VCS abstraction (`VcsType`, `Vcs*Command`) with a git implementation, and `SysCmd` for running system commands. |
| `icaco-maven-plugin` | The Maven plugin and its goals.                                                                                       |

## Maven plugin

```xml
<plugin>
    <groupId>io.github.icaco</groupId>
    <artifactId>icaco-maven-plugin</artifactId>
    <version>1.5.1</version>
</plugin>
```

| Goal                                                         | Default phase | Description                                                         |
|--------------------------------------------------------------|---------------|---------------------------------------------------------------------|
| [`vcs-version`](#vcs-version)                                | `validate`    | Works out a version from git and sets it as a property.             |
| [`check-new-code`](#check-new-code)                          | `verify`      | Runs a JaCoCo coverage check on changed classes only.               |
| [`check-all-code`](#check-all-code)                          | `verify`      | Runs a JaCoCo coverage check on all classes.                        |
| [`jacocoCheckNewCodeIncludes`](#jacocochecknewcodeincludes)  | `initialize`  | Puts the changed classes in a property, for use with `jacoco:check`. |
| [`init-jacoco-javaagent`](#init-jacoco-javaagent)            | `initialize`  | Prepares the JaCoCo agent and sets the `jacoco-javaagent` property. |
| [`bitbucket-set-build-status`](#bitbucket-set-build-status)  | (none)        | Reports a commit's build status to Bitbucket.                       |

### vcs-version

Works out a version from git and stores it in the project property
`icaco.vcs.version`. The goal checks these rules in order:

1. **Feature branch** (when `useJiraIdOnFeatureBranch=true`): on a branch that starts with
   `feature/`, the version is the Jira ID from the branch name plus the postfix.
   `feature/ABC-123-new-login` gives `ABC-123-latest`. If the branch name has no Jira ID,
   the goal falls back to the tag rule.
2. **Release branch** (when `useReleaseBranchVersionNumber=true`): on a branch that starts
   with `release/`, the version is the rest of the branch name. `release/2.1.0` gives `2.1.0`.
3. **Main branch** (when `useLatestVersionOnMainBranch=true`): on the branch `main`, the
   version is `latest`.
4. **Latest tag**: the latest tag reachable from `HEAD` (`git describe --tags`). If there are
   commits after the tag, the postfix is added: `1.3.0` gives `1.3.0-latest`.
5. **Default**: if none of the rules above apply, the version is `0.0.0`.

| Parameter                       | Property                        | Default             |
|---------------------------------|---------------------------------|---------------------|
| `vcsType`                       | `vcsType`                       | `git`               |
| `defaultVersion`                | `defaultVersion`                | `0.0.0`             |
| `commitsAheadTagPostfix`        | `commitsAheadTagPostfix`        | `latest`            |
| `useJiraIdOnFeatureBranch`      | `useJiraIdOnFeatureBranch`      | `false`             |
| `featureBranchPrefix`           | `featureBranchPrefix`           | `feature/`          |
| `useReleaseBranchVersionNumber` | `useReleaseBranchVersionNumber` | `false`             |
| `releaseBranchPrefix`           | `releaseBranchPrefix`           | `release/`          |
| `vcsVersionPropertyName`        | `vcsVersionPropertyName`        | `icaco.vcs.version` |
| `useLatestVersionOnMainBranch`  | `useLatestVersionOnMainBranch`  | `false`             |
| `mainBranchName`                | `mainBranchName`                | `main`              |
| `doNotExecuteInSubModules`      | `doNotExecuteInSubModules`      | `true`              |

In a multi-module build, the goal only runs in the top project by default. Set
`doNotExecuteInSubModules=false` to run it in every module.

The usual way to use it is from the command line, together with `versions:set`:

```sh
mvn io.github.icaco:icaco-maven-plugin:1.5.1:vcs-version versions:set \
    -DnewVersion=\${icaco.vcs.version} \
    -DuseJiraIdOnFeatureBranch=true \
  && mvn clean install -DskipTests
```

### check-new-code

Runs a JaCoCo coverage check, like `jacoco:check`, on changed classes only. A file
counts as changed if it is:

- changed locally and not committed (`git status`), or
- different from the remote default branch (`git diff <origin/HEAD>`).

Only `.java` files under the project's source directory are included. Deleted files
are skipped. If the repository has no `origin` remote, only local changes are used.

The goal reads the same configuration as `jacoco:check` (`rules`, `haltOnFailure`,
`dataFile`, `excludes`), plus `vcsType` (default `git`). It needs `jacoco.exec`, so
run `jacoco:prepare-agent` (or `init-jacoco-javaagent`) before the tests.

```xml
<plugin>
    <groupId>io.github.icaco</groupId>
    <artifactId>icaco-maven-plugin</artifactId>
    <version>1.5.1</version>
    <executions>
        <execution>
            <goals>
                <goal>init-jacoco-javaagent</goal>
                <goal>check-new-code</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### check-all-code

The same check as `check-new-code`, but on every class, with any `includes` you
configure. It is a thin wrapper around the JaCoCo check logic, so the configuration
is the same as for `jacoco:check`.

### jacocoCheckNewCodeIncludes

Finds the changed classes in the same way as `check-new-code`, and puts them as a
comma-separated list in a project property instead of running a check. Use this to
run the standard `jacoco:check` on new code only.

| Parameter                               | Property                                     | Default                          |
|-----------------------------------------|----------------------------------------------|----------------------------------|
| `jacocoCheckNewCodeIncludesPropertyName` | `jacoco.check.new.code.includes.propertyName` | `jacoco.check.new.code.includes` |
| `vcsType`                               | `vcsType`                                    | `git`                            |

### init-jacoco-javaagent

Does the same as `jacoco:prepare-agent` and accepts the same configuration. It also
copies the `-javaagent:...` argument to the property `jacoco-javaagent`. Use it when
coverage is needed from a JVM that does not read `argLine`, such as an application
started by another plugin during integration tests.

### bitbucket-set-build-status

Reports a build status for a commit through the Bitbucket build status REST API
(`POST https://<host>/rest/build-status/1.0/commits/<commit>`). The host is taken
from the `origin` remote URL, and the commit defaults to `HEAD`.

Each parameter can be set as a property or an environment variable. The property
takes precedence.

| Property             | Environment variable   | Required | Fallback             | Description                              |
|----------------------|------------------------|----------|----------------------|------------------------------------------|
| `bitbucketAuthToken` | `BITBUCKET_AUTH_TOKEN` | yes      |                      | Sent as `Authorization: Bearer <token>`. |
| `buildState`         | `BUILD_STATE`          | yes      |                      | `INPROGRESS`, `SUCCESSFUL` or `FAILED`.  |
| `buildUrl`           | `BUILD_URL`            | no       |                      | Link to the build, shown in Bitbucket.   |
| `gitCommit`          | `GIT_COMMIT`           | no       | `git rev-parse HEAD` | Commit to report the status for.         |

As with `vcs-version`, the goal skips submodules unless `doNotExecuteInSubModules=false`
is set, so a multi-module build reports the status only once.

`GIT_COMMIT` and `BUILD_URL` are set by Jenkins, so on Jenkins it is usually
enough to give the token and the state:

```sh
mvn io.github.icaco:icaco-maven-plugin:1.5.1:bitbucket-set-build-status -DbuildState=SUCCESSFUL
```

The goal fails unless Bitbucket answers `204 No Content`.

> **Note:** The endpoint is the Bitbucket Server / Data Center API. The `origin` remote
> must be a URL that `java.net.URI` can parse, for example `https://host/scm/proj/repo.git`
> or `ssh://git@host:7999/proj/repo.git`. The short SSH form `git@host:proj/repo.git` is
> not supported.

## Development

### Build

```sh
mvn clean install -Dgpg.skip
```

The build signs artifacts with GPG in the `verify` phase. Use `-Dgpg.skip` to build
locally without a signing key.

### Change version

```sh
mvn versions:set -DgenerateBackupPoms=false -DnewVersion=1.5.1
```

### Release

Releases are published to Maven Central by the GitHub Actions workflow
[`maven-publish.yml`](.github/workflows/maven-publish.yml). It runs when a GitHub
release is created, sets the project version to the release's tag name and runs
`mvn deploy`. The workflow needs these repository secrets:

- `CENTRAL_TOKEN_USERNAME`, `CENTRAL_TOKEN_PASSWORD`: Sonatype Central token
- `GPG_SIGNING_KEY`, `GPG_SIGNING_KEY_PASSWORD`: GPG key for signing

### Git tags

```sh
# Remove a local tag
git tag -d <tag>

# Remove a remote tag
git push --delete origin <tag>
```

## License

[Apache License 2.0](LICENSE)
