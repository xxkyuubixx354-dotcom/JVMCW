plugins {
    scala
}

group = "com.eventplanning"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    // Scala
    implementation("org.scala-lang:scala3-library_3:3.3.1")

    // JSON for persistence
    implementation("com.google.code.gson:gson:2.10.1")

    // Database (SQLite)
    implementation("org.xerial:sqlite-jdbc:3.43.0.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}



