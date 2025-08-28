import com.palantir.gradle.gitversion.VersionDetails
import groovy.lang.Closure
import org.gradle.jvm.toolchain.JavaLanguageVersion.of
import java.lang.String.format

plugins {
    id("java")
    id("net.kyori.blossom") version "1.3.1"
    id("com.gradleup.shadow") version "8.3.9"
    id("com.palantir.git-version") version "4.0.0"
}

project.group = "pl.mrstudios.essential"
project.version = "1.2.2"

val versionDetails: Closure<VersionDetails> by extra
fun projectVersion(): String = format("%s (git/%s)", project.version, versionDetails().gitHash)

java {
    toolchain.languageVersion.set(of(23))
}

blossom {
    replaceToken("{project}", project.name)
    replaceToken("{version}", projectVersion())
}

repositories {
    mavenCentral()
    maven("https://repo.mrstudios.pl/public/")
    maven("https://repo.eternalcode.pl/releases/")
    maven("https://storehouse.okaeri.eu/repository/maven-public/")
}

dependencies {

    /* JDA */
    implementation(libs.jda.core)
    implementation(libs.jda.commands)

    /* Commons */
    implementation(libs.commons.sql)

    /* HikariCP */
    implementation(libs.hikaricp)

    /* SQLite */
    implementation(libs.sqlite)

    /* Logback Classic */
    implementation(libs.logback.classic)

    /* Unirest */
    implementation(libs.unirest.core)
    implementation(libs.unirest.gson)

    /* Caffeine */
    implementation(libs.caffeine)

    /* Okaeri Configs */
    implementation(libs.okaeri.configs)

    /* Rome */
    implementation(libs.rome)

    /* Source Query */
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

    processResources {
        filteringCharset = "UTF-8"
    }

    jar {
        dependsOn(shadowJar)
        manifest {
            attributes["Main-Class"] = "pl.mrstudios.essential.bootstrap.Bootstrap"
        }
    }

    shadowJar {
        dependencies {
            isEnableRelocation = false
            relocationPrefix = format("%s.libraries", project.group)
        }
    }

}
