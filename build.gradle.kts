plugins {
    kotlin("multiplatform") version "1.7.10" apply false
    kotlin("plugin.serialization") version "1.7.10" apply false
}

allprojects {
    group = "com.github.habit-kt"
    version = "1.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    extra["fritz2Version"] = "1.0-RC1"
}