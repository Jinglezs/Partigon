import org.gradle.kotlin.dsl.invoke

plugins {
    kotlin("jvm") version libs.versions.kotlin
    `maven-publish`
    // Shades and relocates dependencies into our plugin jar. See https://imperceptiblethoughts.com/shadow/introduction/
    id("com.gradleup.shadow") version libs.versions.shadow

}

group = "xyz.gameoholic"
version = "1.4.0"
description = "A Minecraft particle animation library written in Kotlin."

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
}

val shade: Configuration = project.configurations.create("shade") {
    isTransitive = false
}

dependencies {
    compileOnly(libs.kotlinx.coroutines)
    compileOnly(libs.mccoroutine.api)
    compileOnly(libs.paper)
    shade(libs.exp4j)
    shade(libs.commons.math3)
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

configurations {
    // Include all shaded/platform dependencies in the "compile" scope
    compileOnly.configure {
        extendsFrom(shade)
    }
}

tasks.shadowJar {
    artifacts {
        configurations = listOf(shade)
    }

    // helper function to relocate a package into our package
    fun reloc(pkg: String) = relocate(pkg, "${project.group}.${project.name}.dependency.$pkg")

    //relocate("kotlin", "xyz.gameoholic.partigon.dependency.kotlin")
    reloc("net.objecthunter")
    reloc("org.apache.commons")

    archiveClassifier.set("")
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

publishing {
    publications {
        create<MavenPublication>("partigon") {
            artifact(sourcesJar)
            shadow.component(this)
        }
    }
}
