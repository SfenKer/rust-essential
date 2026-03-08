import com.palantir.gradle.gitversion.VersionDetails
import groovy.lang.Closure
import org.gradle.jvm.toolchain.JavaLanguageVersion.of
import java.lang.String.format
import java.lang.String.valueOf

plugins {
    id("java")
    id("net.kyori.blossom") version "2.2.0"
    id("com.palantir.git-version") version "5.0.0"
    id("org.springframework.boot") version "4.1.0-M2"
}

project.group = "com.github.sfenker.essential"
project.version = "2.0.0"

val versionDetails: Closure<VersionDetails> by extra
fun projectVersion(): String = format("%s (git/%s)", project.version, versionDetails().gitHash)

java {
    toolchain.languageVersion.set(of(25))
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("version", valueOf(project.version))
                property("gitTag", versionDetails().lastTag ?: "unknown")
                property("gitHash", versionDetails().gitHash ?: "unknown")
                property("gitBranch", versionDetails().branchName ?: "unknown")
            }
        }
    }
}

repositories {
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots/")
}

dependencies {

    implementation(libs.jda.core)
    implementation(libs.jda.commands)

    /* Serialization & Data */
    implementation(libs.gson)
    implementation(libs.guava)

    /* Database */
    implementation(libs.hikaricp)
    implementation(libs.sqlite.driver)
    implementation(libs.jakarta.persistence)
    implementation(libs.hibernate.core)
    implementation(libs.hibernate.hikaricp)
    implementation(libs.hibernate.community.dialects)

    /* System & Tools */
    implementation(libs.oshi.core)
    implementation(libs.unirest.java.core)
    implementation(libs.unirest.modules.gson)
    implementation(libs.logback.classic)

    /* Misc */
    implementation(libs.rome)
    implementation(libs.source.query)

    /* Lombok */
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    /* JetBrains Annotations */
    compileOnly(libs.jetbrains.annotations)
    annotationProcessor(libs.jetbrains.annotations)

}

tasks {

    compileJava {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    jar {
        enabled = false
    }

    bootJar {
        dependsOn(generateTemplates)
        archiveFileName.set("${project.name}.jar")
        mainClass.set("${project.group}.entrypoint.Entrypoint")
    }

    build {
        dependsOn(bootJar)
    }

}
