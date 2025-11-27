plugins {
    kotlin("jvm") version "1.9.20"
    scala
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.eventplanning"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))

    // JavaFX for GUI
    implementation("org.openjfx:javafx-controls:21")
    implementation("org.openjfx:javafx-fxml:21")

    // Scala
    implementation(project(":scheduling-scala"))
    implementation("org.scala-lang:scala3-library_3:3.3.1")

    // JSON for persistence
    implementation("com.google.code.gson:gson:2.10.1")

    // Database (SQLite)
    implementation("org.xerial:sqlite-jdbc:3.43.0.0")

    // Testing
    testImplementation(kotlin("test"))
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("MainKt")
}

