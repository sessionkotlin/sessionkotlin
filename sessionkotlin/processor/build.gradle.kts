plugins {
    kotlin("jvm")
    `java-library`
    id("com.google.devtools.ksp") version "1.6.10-1.0.2"
}
val kspVersion: String by project

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
}

ksp {
    // Passing an argument to the symbol processor.
    // Change value to "true" in order to apply the argument.
    arg("ignoreGenericArgs", "false")
}
