plugins {
    java
    `java-library`
}

allprojects {
    group = "com.nexcom.channels"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
