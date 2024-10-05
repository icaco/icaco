# icaco
## Icaco Plugin
### VCS Version

     mvn io.github.icaco:icaco-maven-plugin:1.2.1:vcs-version versions:set -DnewVersion=\${icaco.branch.version} && mvn clean install -DskipTests

## Maven

    mvn versions:set -DgenerateBackupPoms=false -DnewVersion=1.0.0

## Git
### Remove local tag

    git tag -d <tag>

### Remove remote tag

    git push --delete origin <tag>