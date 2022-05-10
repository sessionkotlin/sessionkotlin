# sessionkotlin

Multiparty Session Types in Kotlin

![master](https://github.com/d-costa/session-kotlin/actions/workflows/test_master.yml/badge.svg) ![master coverage](../badges/jacoco.svg)

```kotlin
val a = SKRole("Client A")
val b = SKRole("Client B")
val seller = SKRole("Seller")

globalProtocolInternal { 
   send<String>(a, seller)

   send<Int>(seller, a, "valSentToA")
   send<Int>(seller, b, "valSentToA", "valSentToA == valSentToB")
   
   send<Int>(a, b, "proposal", "proposal <= valSentToA")
   
   choice(b) {
     branch("Ok") {
         send<Address>(b, seller)
         send<Date>(seller, b)
         send<Date>(b, a)
     }
     branch("Quit") {
         send<Unit>(b, seller)
         send<Unit>(b, a)
     }
}
}
```

## Getting started

### Using a Template

#### Gradle

https://github.com/d-costa/sessionkotlin-template-gradle

#### Maven

https://github.com/d-costa/sessionkotlin-template-maven

### Add as dependency

#### Gradle

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

#### Maven

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
