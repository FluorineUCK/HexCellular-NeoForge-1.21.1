import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	java
	id("org.jetbrains.kotlin.jvm") version "2.2.21"
}

subprojects {
	version = "1.2.0"
	group = "miyucomics.hexcellular"

	tasks.withType<JavaCompile>().configureEach {
		options.release.set(17)
	}

	tasks.withType<KotlinCompile>().configureEach {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_17)
		}
	}

	repositories {
		mavenCentral()
		maven("https://maven.blamejared.com/")
		maven("https://maven.ladysnake.org/releases")
		maven("https://maven.shedaniel.me/")
		maven("https://maven.terraformersmc.com/releases")
		maven("https://maven.hexxy.media")
		maven("https://thedarkcolour.github.io/KotlinForForge/") { content { includeGroup("thedarkcolour") } }
	}
}