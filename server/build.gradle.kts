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
	kotlin("jvm") version "1.7.21"
	application
	id("com.github.johnrengelman.shadow") version "7.1.2"
	id("com.diffplug.spotless") version "6.12.0"
	id("com.github.gmazzo.buildconfig") version "3.1.0"
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
	implementation(project(":solarxr-protocol"))

	// This dependency is used internally,
	// and not exposed to consumers on their own compile classpath.
	implementation("com.google.flatbuffers:flatbuffers-java:22.10.26")
	implementation("commons-cli:commons-cli:1.3.1")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.12.7.1")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.3")

	implementation("com.github.jonpeterson:jackson-module-model-versioning:1.2.2")
	implementation("org.apache.commons:commons-math3:3.6.1")
	implementation("org.apache.commons:commons-lang3:3.12.0")
	implementation("net.java.dev.jna:jna:5.+")
	implementation("net.java.dev.jna:jna-platform:5.+")
	implementation("com.illposed.osc:javaosc-core:0.8")
	implementation("com.fazecast:jSerialComm:2.+")
	implementation("com.google.protobuf:protobuf-java:3.+")
	implementation("org.java-websocket:Java-WebSocket:1.+")
	implementation("com.melloware:jintellitype:1.+")

	testImplementation(kotlin("test"))
	// Use JUnit test framework
	testImplementation(platform("org.junit:junit-bom:5.9.0"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testImplementation("org.junit.platform:junit-platform-launcher")
}
tasks.test {
	useJUnitPlatform()
}

tasks.shadowJar {
	archiveBaseName.set("slimevr")
	archiveClassifier.set("")
	archiveVersion.set("")
}
application {
	mainClass.set("dev.slimevr.Main")
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
	val gitCommitHash = "git rev-parse --verify --short HEAD".runCommand().trim()
	val gitVersionTag = "git --no-pager tag --points-at HEAD".runCommand().trim()
	val gitClean = "git status --porcelain".runCommand().trim().isEmpty()
	useKotlinOutput { topLevelConstants = true }
	packageName("dev.slimevr")

	buildConfigField("String", "GIT_COMMIT_HASH", "\"${gitCommitHash}\"")
	buildConfigField("String", "GIT_VERSION_TAG", "\"${gitVersionTag}\"")
	buildConfigField("boolean", "GIT_CLEAN", gitClean.toString())
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
	// optional: limit format enforcement to just the files changed by this feature branch
	// ratchetFrom "origin/main"

	format("misc") {
		// define the files to apply `misc` to
		target("*.gradle", "*.md", ".gitignore")

		// define the steps to apply to those files
		trimTrailingWhitespace()
		endWithNewline()
		indentWithTabs()
	}
	// format "yaml", {
	// 	target "*.yml", "*.yaml",

	// 	trimTrailingWhitespace()
	// 	endWithNewline()
	// 	indentWithSpaces(2)  // YAML cannot contain tabs: https://yaml.org/faq.html
	// }

	// .editorconfig doesn't work so, manual override
	// https://github.com/diffplug/spotless/issues/142
	val editorConfig =
		mapOf(
			"indent_size" to 4,
			"indent_style" to "tab",
//			"max_line_length" to 88,
			"ktlint_experimental" to "enabled"
		)
	val ktlintVersion = "0.47.1"
	kotlinGradle {
		target("*.gradle.kts") // default target for kotlinGradle
		ktlint(ktlintVersion)
			.setUseExperimental(true)
			.editorConfigOverride(editorConfig)
	}
	kotlin {
		targetExclude("build/**/**.kt")
		ktlint(ktlintVersion)
			.setUseExperimental(true)
			.editorConfigOverride(editorConfig)
	}
	java {
		targetExclude("**/BuildConfig.java")

		removeUnusedImports()
		// Use eclipse JDT formatter
		eclipse().configFile("spotless.xml")
	}
}
