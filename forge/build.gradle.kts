plugins {
	id("net.minecraftforge.gradle") version "6.0.+"
}

evaluationDependsOn(":common")

sourceSets {
	main {
		val commonSourceSets = project(":common").extensions.getByType<SourceSetContainer>()
		java.srcDirs(commonSourceSets["main"].java.srcDirs)
		resources.srcDirs(commonSourceSets["main"].resources.srcDirs)
	}
}

minecraft {
	mappings("official", libs.versions.minecraft.get())

	runs {
		configureEach {
			workingDirectory(project.file("run"))
			mods {
				create("hexcellular") {
					source(sourceSets.main.get())
					source(project(":Common").sourceSets.main.get())
				}
			}
		}
	}
}

dependencies {
	minecraft("net.minecraftforge:forge:${libs.versions.minecraft.get()}-${libs.versions.forge.loader.get()}")
	implementation(libs.forge.kotlin)

	implementation(libs.hexcasting.forge) {
		exclude(module = "phosphor")
		exclude(module = "lithium")
		exclude(module = "emi")
	}

	runtimeOnly(libs.cloth.config.forge)
	runtimeOnly(libs.patchouli.forge)
	runtimeOnly(libs.paucal.forge)
}

tasks.processResources {
	inputs.property("version", project.version)
	filesMatching("META-INF/mods.toml") {
		expand("version" to project.version)
	}
}