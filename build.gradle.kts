import com.palantir.gradle.gitversion.VersionDetails
import groovy.lang.Closure
import org.gradle.jvm.toolchain.JavaLanguageVersion.of
import java.lang.String.format

plugins {
    id("java")
    id("net.kyori.blossom") version "1.3.1"
    id("com.gradleup.shadow") version "8.3.3"
    id("com.palantir.git-version") version "3.1.0"
}

project.group = "pl.mrstudios.essential"
project.version = "1.2.2"

val versionDetails: Closure<VersionDetails> by extra
fun projectVersion(): String = format("%s (git/%s)", project.version, versionDetails().gitHash)

java {
    toolchain.languageVersion.set(of(21))
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
    implementation("net.dv8tion:JDA:${property("jda.version")}") {
        exclude("opus-java")
    }

    /* Commons */
    implementation("pl.mrstudios.commons:commons-sql:${property("commons.version")}")

    /* HikariCP */
    implementation("com.zaxxer:HikariCP:${property("hikaricp.version")}")

    /* SQLite */
    implementation("org.xerial:sqlite-jdbc:${property("xerial.sqlite.version")}")

    /* Logback Classic */
    implementation("ch.qos.logback:logback-classic:${property("logback.classic.version")}")

    /* Unirest */
    implementation("com.konghq:unirest-java-core:${property("unirest.version")}")
    implementation("com.konghq:unirest-modules-gson:${property("unirest.version")}")

    /* Caffeine */
    implementation("com.github.ben-manes.caffeine:caffeine:${property("caffeine.version")}")

    /* Lite Commands */
    implementation("dev.rollczi:litecommands-jda:${property("litecommands.version")}")

    /* Okaeri Configs */
    implementation("eu.okaeri:okaeri-configs-yaml-snakeyaml:${property("okaeri.configs.version")}")

    /* Rome */
    implementation("com.rometools:rome:${property("rome.version")}")

    /* Source Query */
    implementation("com.ibasco.agql:agql-source-query:${property("source.query.version")}")

    /* Lombok */
    compileOnly("org.projectlombok:lombok:${property("lombok.version")}")
    annotationProcessor("org.projectlombok:lombok:${property("lombok.version")}")

    /* JetBrains Annotations */
    compileOnly("org.jetbrains:annotations:${property("jetbrains.annotations.version")}")
    annotationProcessor("org.jetbrains:annotations:${property("jetbrains.annotations.version")}")

}

tasks {

    compileJava {
        options.encoding = "UTF-8"
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
