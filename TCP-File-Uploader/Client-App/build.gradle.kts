import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "ru.nsu.fit.sckwo"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("commons-cli:commons-cli:1.5.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(19)
}

application {
    mainClass.set("ClientFileUploader")
}

tasks {
    register<ShadowJar>("userShadowJar") {
        from(project.sourceSets.main.get().output)
        configurations = project.configurations.filter { it.name == "runtimeClasspath" }
        archiveFileName.set("ClientFileUploaderUser.jar")
        destinationDirectory.set(file("../out/artifacts"))
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        manifest {
            attributes(mapOf("Main-Class" to "ClientFileUploader"))
        }
    }
    register<ShadowJar>("dockerShadowJar") {
        from(project.sourceSets.main.get().output)
        configurations = project.configurations.filter { it.name == "runtimeClasspath" }
        archiveFileName.set("ClientFileUploaderDocker.jar")
        destinationDirectory.set(file("out/artifacts"))
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        manifest {
            attributes(mapOf("Main-Class" to "ClientFileUploader"))
        }
    }
    named<ShadowJar>("shadowJar") {
        archiveFileName.set("ClientFileUploaderUser.jar")
        destinationDirectory.set(file("../out/artifacts"))
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        manifest {
            attributes(mapOf("Main-Class" to "ClientFileUploader"))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
        dependsOn("dockerShadowJar")
    }
}