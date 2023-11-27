import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import kotlin.io.path.Path

plugins {
    kotlin("jvm") version "1.9.0"
    id("com.google.protobuf") version "0.9.4"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "ru.sckwo"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.protobuf:protobuf-kotlin:3.24.3")
    runtimeOnly("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
    testImplementation(kotlin("test"))
}

val protobufGenPath = "build/generated/source/proto"

sourceSets {
    main {
        proto {
            srcDir("src/main/proto")
        }
        kotlin.srcDirs += File(protobufGenPath)
    }
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

protobuf {
    buildDir = File(protobufGenPath)
}

tasks.compileKotlin {
    source("${buildDir.absolutePath}/generated/src/main/java")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(19)
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("snake")
        destinationDirectory.set(projectDir.resolve(Path("build", "out").toFile()))
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        manifest {
            attributes(mapOf("Main-Class" to "MainKt"))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

application {
    mainClass.set("MainKt")
}