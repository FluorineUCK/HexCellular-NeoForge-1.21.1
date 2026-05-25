pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
		maven("https://maven.fabricmc.net/")
		maven("https://maven.minecraftforge.net/")
	}
}

rootProject.name = "hexcellular"

include("common")
include("fabric")
include("forge")