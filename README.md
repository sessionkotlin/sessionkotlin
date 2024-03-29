# SessionKotlin

Multiparty Session Types in Kotlin: http://hdl.handle.net/10362/151147


![master](https://github.com/sessionkotlin/sessionkotlin/actions/workflows/test.yml/badge.svg) ![master coverage](../badges/jacoco.svg)

```kotlin
val a = SKRole("ClientA")
val b = SKRole("ClientB")
val seller = SKRole("Seller")

globalProtocol("Protocol") {
    send<String>(a, seller, "id")

    send<Int>(seller, a, "valA")
    send<Int>(seller, b, "valB", "valA == valB")

    send<Int>(a, b, "proposal", "proposal <= valA")

    choice(b) {
        branch {
            send<Address>(b, seller, "buy")
            send<Date>(seller, b)
            send<Date>(b, a, "ok")
        }
        branch {
            send<Unit>(b, seller, "quit")
            send<Unit>(b, a, "quit")
        }
    }
}
```
> [!TIP]
> If you'd like to see a more complex example, check our simplified [SMTP implementation](./evaluation/smtp) and the [MPST definition](evaluation/smtp/protocol/src/main/kotlin/App.kt).

## Table of Contents

<!-- TOC -->
* [Getting started](#getting-started)
* [Features](#features)
* [Plugin configuration](#plugin-configuration)
* [Reference](#reference)
<!-- TOC -->

## Getting started

### Prerequisites

- JDK 11

### Project Templates

#### Gradle (recommended)

https://github.com/sessionkotlin/sessionkotlin-template-gradle

#### Maven

https://github.com/sessionkotlin/sessionkotlin-template-maven

## Features

#### Reusable (and parametric) protocol definitions

```kotlin
val a = SKRole("A")
val b = SKRole("B")

fun subProtocol(x: SKRole, y: SKRole): GlobalProtocol = {
    send<Int>(x, y)
    send<Int>(y, x)
}

globalProtocol("ComplexProtocol") {
    choice(a) {
        branch {
            send<Int>(a, b, "branch1")
            subProtocol(a, b)()  // Proceed with subProtocol
        }
        branch {
            send<Int>(a, b, "branch2")
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

globalProtocol("RefinedProtocol") {
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

## Plugin configuration

To configure the plugin use the extension (default values shown):

```kotlin
sessionkotlin {
    cleanBeforeCopying = false //  always clean the solver dependencies before copying
}
```
## Reference

```bib
@masterthesis { costa2022,
 author	= "Costa, David Maria Almeida Amorim da",
 title	= "Session Kotlin: A hybrid session type embedding in Kotlin",
 year	= "2022"
 url   = {http://hdl.handle.net/10362/151147}
}
```
