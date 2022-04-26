# sessionkotlin

Multiparty Session Types in Kotlin

![master](https://github.com/d-costa/session-kotlin/actions/workflows/test_master.yml/badge.svg)
![master coverage](../badges/jacoco.svg)

## Try it out

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
