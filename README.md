# SessionKotlin

Multiparty Session Types in Kotlin

![master](https://github.com/d-costa/session-kotlin/actions/workflows/test_master.yml/badge.svg) ![master coverage](../badges/jacoco.svg)

```kotlin
val a = SKRole("ClientA")
val b = SKRole("ClientB")
val seller = SKRole("Seller")

globalProtocol("Protocol") {
    send<String>(a, seller)

    send<Int>(seller, a, "valA")
    send<Int>(seller, b, "valB", "valA == valB")

    send<Int>(a, b, "proposal", "proposal <= valA")

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

### Prerequisites

- Java 11

### Templates

#### Gradle (recommended)

https://github.com/d-costa/sessionkotlin-template-gradle

#### Maven

https://github.com/d-costa/sessionkotlin-template-maven

## Features

#### Reusable protocol definitions

```kotlin
val a = SKRole("A")
val b = SKRole("B")

fun subProtocol(x: SKRole, y: SKRole): GlobalProtocol = {
    send<Int>(x, y)
    send<Int>(y, x)
}

globalProtocol("Complex Protocol") {
    choice(a) {
        branch("Branch1") {
            send<Int>(a, b)
            subProtocol(a, b)()  // Proceed with subProtocol
        }
        branch("Branch2") {
            send<Int>(a, b)
            subProtocol(b, a)()  // Proceed with subProtocol
        }
    }
}
```

#### Refinements

Basic arithmetic and logical operations supported by the Z3 theorem prover.

```kotlin
val a = SKRole("A")
val b = SKRole("B")

globalProtocol("Refined Protocol") {
    send<Int>(a, b, "val1")
    send<Int>(b, a, "val2", "val2 >= val1")
}
```

#### Fluent and Callback-based local APIs

```kotlin
runBlocking {
    val chan = SKChannel()

    // Alice
    launch {
        SKMPEndpoint().use { e ->
            e.connect(Bob, chan)

            val buf = SKBuffer<Int>()

            ExampleProtocolAlice1(e)
                .sendToBob(1)
                .receiveFromBob(buf)
                .also { println("Alice received ${buf.value} from Bob") }
                .sendToBob("Hello")
        }
    }

    // Bob
    launch {
        var received = 0

        val callbacks = object : ExampleProtocolCallbacksBob {
            override fun onReceiveVal1FromAlice(value: Int) {
                received = value
            }
            override fun onSendVal2ToAlice(): Int = received * 2

            override fun onReceiveStringValueFromAlice(value: String) {
                println(value)
            }
        }

        ExampleProtocolCallbacksClassBob(callbacks).use { e ->
            e.connect(Alice, chan)
            e.start()
        }
    }
}
```

#### Communication through Sockets or Channels

```kotlin
val chanAB = SKChannel()

// Endpoint A
SKMPEndpoint().use { e ->
    e.connect(B, chanAB)
    // ...
}

// Endpoint B
SKMPEndpoint().use { e ->
    e.connect(A, chanAB)
    e.accept(C, 9999)
    // ...
}

// Endpoint C
SKMPEndpoint().use { e ->
    e.request(B, "localhost", 9999)
    // ...
}
```

## Plugin

To configure the plugin use the extension (the default values are shown):

```kotlin
sessionkotlin {
    cleanBeforeCopying = false //  always clean the solver dependencies before copying
}
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
