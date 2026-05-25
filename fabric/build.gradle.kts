plugins {
	id("fabric-loom") version "1.9-SNAPSHOT"
}

evaluationDependsOn(":common")

sourceSets {
	main {
		val commonSourceSets = project(":common").extensions.getByType<SourceSetContainer>()["main"]
		java.srcDirs(commonSourceSets.java.srcDirs)
		resources.srcDirs(commonSourceSets.resources.srcDirs)
	}
}

dependencies {
	minecraft(libs.minecraft)
	mappings(variantOf(libs.yarn.mappings) { classifier("v2") })

	modImplementation(libs.fabric.loader)
	modImplementation(libs.fabric.api)
	modImplementation(libs.fabric.language.kotlin)

	modImplementation(libs.hexcasting.fabric) {
		exclude(module = "phosphor")
		exclude(module = "lithium")
		exclude(module = "emi")
	}

	modLocalRuntime(libs.cardinal.components)
	modLocalRuntime(libs.cloth.config.fabric)
	modLocalRuntime(libs.inline.fabric)
	modLocalRuntime(libs.patchouli.fabric)
	modLocalRuntime(libs.paucal.fabric)
	modLocalRuntime(libs.serialization.hooks)
}

tasks.processResources {
	inputs.property("version", project.version)
	filesMatching("fabric.mod.json") {
		expand("version" to project.version)
	}
}