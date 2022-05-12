# sessionkotlin

## Tasks

### Run linter

```shell
./gradlew ktlintCheck  # or 'ktlintFormat' to fix
```

### Publish locally

```shell
./gradlew clean build publishToMavenLocal
```

### Publish to GitHub Packages

```shell
./gradlew clean build publish
```

### Generate HTML documentation

```shell
./gradlew dokkaHtmlMultiModule
```

### Generate code coverage report (JaCoCo)

```shell
./gradlew codeCoverageReport
```