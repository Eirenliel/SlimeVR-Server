/*
 * This file was generated by the Gradle "init" task.
 *
 * This generated file contains a sample Java Library project to get you started.
 * For more details take a look at the Java Libraries chapter in the Gradle
 * User Manual available at https://docs.gradle.org/6.3/userguide/java_library_plugin.html
 */
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
	kotlin("jvm")
	application
	id("com.github.johnrengelman.shadow")
	id("com.github.gmazzo.buildconfig")
	id("org.ajoberstar.grgit")
}

kotlin {
	jvmToolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}
java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}
tasks.withType<KotlinCompile> {
	kotlinOptions.jvmTarget = "17"
}

// Set compiler to use UTF-8
tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}
tasks.withType<Test> {
	systemProperty("file.encoding", "UTF-8")
}
tasks.withType<Javadoc> {
	options.encoding = "UTF-8"
}

allprojects {
	repositories {
		// Use jcenter for resolving dependencies.
		// You can declare any Maven/Ivy/file repository here.
		mavenCentral()
	}
}

dependencies {
	implementation(project(":server:core"))

	implementation("commons-cli:commons-cli:1.5.0")
	implementation("org.apache.commons:commons-lang3:3.12.0")
	implementation("net.java.dev.jna:jna:5.+")
	implementation("net.java.dev.jna:jna-platform:5.+")
}

tasks.shadowJar {
	minimize {
		exclude(dependency("com.fazecast:jSerialComm:.*"))
		exclude(dependency("net.java.dev.jna:.*:.*"))
		exclude(dependency("com.google.flatbuffers:flatbuffers-java:.*"))

		exclude(project(":solarxr-protocol"))
	}
	archiveBaseName.set("slimevr")
	archiveClassifier.set("")
	archiveVersion.set("")
}
application {
	mainClass.set("dev.slimevr.desktop.Main")
}

fun String.runCommand(currentWorkingDir: File = file("./")): String {
	val byteOut = ByteArrayOutputStream()
	project.exec {
		workingDir = currentWorkingDir
		commandLine = this@runCommand.split("\\s".toRegex())
		standardOutput = byteOut
	}
	return String(byteOut.toByteArray()).trim()
}

buildConfig {
	useKotlinOutput { topLevelConstants = true }
	packageName("dev.slimevr.desktop")

	val gitVersionTag = providers.exec {
		commandLine("git", "--no-pager", "tag", "--points-at", "HEAD")
	}.standardOutput.asText.get()
	buildConfigField("String", "GIT_COMMIT_HASH", "\"${grgit.head().abbreviatedId}\"")
	buildConfigField("String", "GIT_VERSION_TAG", "\"${gitVersionTag}\"")
	buildConfigField("boolean", "GIT_CLEAN", grgit.status().isClean.toString())
}

tasks.run<JavaExec> {
	standardInput = System.`in` // this is not working
	args = listOf("run")
}
