package miyucomics.hexcellular

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.hex.HexActions
import miyucomics.hexcellular.patterns.OpCreateProperty
import miyucomics.hexcellular.patterns.OpObserveProperty
import miyucomics.hexcellular.patterns.OpReadonlyProperty
import miyucomics.hexcellular.patterns.OpSetProperty
import net.minecraft.registry.Registry

object HexcellularPatterns {
	@JvmStatic
	fun init() {
		register("create_property", "aawe", HexDir.SOUTH_WEST, OpCreateProperty())
		register("observe_property", "aawd", HexDir.SOUTH_WEST, OpObserveProperty())
		register("set_property", "aawq", HexDir.SOUTH_WEST, OpSetProperty())
		register("readonly_property", "aawa", HexDir.SOUTH_WEST, OpReadonlyProperty())
	}

	private fun register(name: String, signature: String, startDir: HexDir, action: Action) {
		Registry.register(HexActions.REGISTRY, HexcellularMain.id(name), ActionRegistryEntry(HexPattern.fromAngles(signature, startDir), action))
	}
}