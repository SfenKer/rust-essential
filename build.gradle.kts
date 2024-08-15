import com.palantir.gradle.gitversion.VersionDetails
import groovy.lang.Closure
import java.lang.String.format

plugins {
    id("java")
    id("net.kyori.blossom") version "1.3.1"
    id("com.palantir.git-version") version "3.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

project.group = "pl.mrstudios.essential"
project.version = "1.2.0"

val versionDetails: Closure<VersionDetails> by extra
fun projectVersion(): String = format("%s (git/%s)", project.version, versionDetails().gitHash)

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

blossom {
    replaceToken("{project}", project.name)
    replaceToken("{version}", projectVersion())
}

repositories {
    mavenCentral()
    maven("https://repo.mrstudios.pl/public/")
    maven("https://repo.panda-lang.org/releases/")
    maven("https://storehouse.okaeri.eu/repository/maven-public/")
}

dependencies {

    /* JDA */
    implementation("net.dv8tion:JDA:${project.property("jda.version")}") {
        exclude("opus-java")
    }

    /* Commons */
    implementation("pl.mrstudios.commons:commons-sql:${project.property("commons.version")}")

    /* HikariCP */
    implementation("com.zaxxer:HikariCP:${project.property("hikaricp.version")}")

    /* SQLite */
    implementation("org.xerial:sqlite-jdbc:${project.property("xerial.sqlite.version")}")

    /* Logback Classic */
    implementation("ch.qos.logback:logback-classic:${project.property("logback.classic.version")}")

    /* Unirest */
    implementation("com.konghq:unirest-java-core:${project.property("unirest.version")}")
    implementation("com.konghq:unirest-modules-gson:${project.property("unirest.version")}")

    /* Caffeine */
    implementation("com.github.ben-manes.caffeine:caffeine:${project.property("caffeine.version")}")

    /* Lite Commands */
    implementation("dev.rollczi:litecommands-jda:${project.property("litecommands.version")}")

    /* Okaeri Configs */
    implementation("eu.okaeri:okaeri-configs-yaml-snakeyaml:${project.property("okaeri.configs.version")}")

    /* Rome */
    implementation("com.rometools:rome:${project.property("rome.version")}")

    /* Source Query */
    implementation("com.ibasco.agql:agql-source-query:${project.property("source.query.version")}")

    /* Lombok */
    compileOnly("org.projectlombok:lombok:${project.property("lombok.version")}")
    annotationProcessor("org.projectlombok:lombok:${project.property("lombok.version")}")

    /* JetBrains Annotations */
    compileOnly("org.jetbrains:annotations:${project.property("jetbrains.annotations.version")}")
    annotationProcessor("org.jetbrains:annotations:${project.property("jetbrains.annotations.version")}")

}

tasks {

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
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