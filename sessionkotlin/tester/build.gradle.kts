plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "1.6.10-1.0.2"
}

dependencies {
    implementation("org.david:sessionkotlin_lib:0.0.1")
    implementation("org.david:sessionkotlin_processor:0.0.1")
//    ksp("org.david:sessionkotlin_processor:0.0.1")
    ksp(project(":processor"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
