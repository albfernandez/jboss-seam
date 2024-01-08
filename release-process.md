# Release Process

This guide provides a chronological steps which goes through release tagging, staging, verification and publishing.

To see the original Jboss Seam guide, see [release-process.txt](release-process.txt)

## Check the SNAPSHOT builds and pass the tests

Check that the project builds in java 8 and java 11.

```bash
mvn clean package install verify -Pdistribution,examples
JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/ mvn clean package install verify -Pdistribution,examples
```

## Set version and build 

```bash
# change release in poms (don't forget <version.seam> in pom.xml and bom/pom.xml), README.md and distribution/src/assembly/changelog.txt
mvn clean package install -Pdistribution,examples
mvn -pl '!functional-tests,!seam-integration-tests' clean package install deploy
git add -A
git commit -S -m 'Release <2.3.28.ayg>'
git tag -a <2.3.28.ayg> -m "Tagging release <2.3.28.ayg>"
git push
git push --tags
```


## Create release and upload artifacts to Github

Manually creating the release in Github project page, and upload generated artifacts.



## Prepare next iteration

```bash
# change release in poms (don't forget <version.seam> in pom.xml and bom/pom.xml) and distribution/src/assembly/changelog.txt
git add -A
git commit -S -m 'Next release cycle'
git push
```
