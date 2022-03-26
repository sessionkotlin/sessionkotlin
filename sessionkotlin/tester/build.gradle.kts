plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "1.6.10-1.0.2"
}

dependencies {
    implementation(project(":lib"))
    implementation(project(":processor"))
//    ksp("org.david:sessionkotlin_processor:0.0.1")
    ksp(project(":processor"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
