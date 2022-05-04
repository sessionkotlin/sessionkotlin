# sessionkotlin

## Tasks

### Test and Publish locally

```
./gradlew clean ktlintCheck build publishToMavenLocal
```

### Test and Publish to GitHub Packages

```
./gradlew clean ktlintCheck build publish
```

### Generate HTML documentation

```
./gradlew dokkaHtmlMultiModule
```

### Generate code coverage report (JaCoCo)

```
./gradlew codeCoverageReport
```