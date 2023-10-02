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
    mainClass.set("ServerFileUploader")
}

tasks {
    register<ShadowJar>("userShadowJar") {
        from(project.sourceSets.main.get().output)
        configurations = project.configurations.filter { it.name == "runtimeClasspath" }
        archiveFileName.set("ServerFileUploaderUser.jar")
        destinationDirectory.set(file("../out/artifacts"))
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        manifest {
            attributes(mapOf("Main-Class" to "ServerFileUploader"))
        }
    }
    register<ShadowJar>("dockerShadowJar") {
        from(project.sourceSets.main.get().output)
        configurations = project.configurations.filter { it.name == "runtimeClasspath" }
        archiveFileName.set("ServerFileUploaderDocker.jar")
        destinationDirectory.set(file("out/artifacts"))
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        manifest {
            attributes(mapOf("Main-Class" to "ServerFileUploader"))
        }
    }
    named<ShadowJar>("shadowJar") {
        archiveFileName.set("ServerFileUploader.jar")
        destinationDirectory.set(file("../out/artifacts"))
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        manifest {
            attributes(mapOf("Main-Class" to "ServerFileUploader"))
        }
    }
}

tasks {
    build {
        dependsOn("dockerShadowJar")
        dependsOn(shadowJar)
    }
}