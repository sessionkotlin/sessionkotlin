# sessionkotlin

Multiparty Session Types in Kotlin

![master](https://github.com/d-costa/session-kotlin/actions/workflows/test_master.yml/badge.svg) ![master coverage](../badges/jacoco.svg)

## Add as dependency

### Gradle

build.gradle.kts:

```kotlin
dependencies {
    api("org.david:sessionkotlin-lib:0.0.1") // or the latest version
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
}

repositories {
    mavenCentral()
    maven {
        name = "SessionKotlin-GithubPackages"
        url = uri("https://maven.pkg.github.com/d-costa/sessionkotlin")
    }
}

```

### Maven

```xml

<dependencies>
    <dependency>
        <groupId>org.david</groupId>
        <artifactId>sessionkotlin-lib</artifactId>
        <version>0.0.1</version>
    </dependency>
</dependencies>

<repositories>
<repository>
    <id>SessionKotlin-GithubPackages</id>
    <url>https://maven.pkg.github.com/d-costa/sessionkotlin</url>
</repository>
</repositories>
```

## Local development

1. Build and publish the library:

```
cd sessionkotlin
./gradlew clean build publishToMavenLocal
cd ..
```

2. Run the demo:
    1. Go to the demo folder:
    ```
    cd demo
    ```

    2. Clean:

    ```
    ./gradlew protocols:clean app:clean
    ```

    3. Generate the API

    ```
    ./gradlew protocols:run
    ```

    4. Run the main app:

    ```
    ./gradlew app:run
    ```
