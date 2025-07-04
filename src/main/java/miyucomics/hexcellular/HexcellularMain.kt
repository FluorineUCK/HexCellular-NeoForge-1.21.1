package miyucomics.hexcellular

import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import net.fabricmc.api.ModInitializer
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

class HexcellularMain : ModInitializer {
	override fun onInitialize() {
		HexcellularActions.init()
		Registry.register(HexIotaTypes.REGISTRY, id("property"), PropertyIota.TYPE)
	}

	companion object {
		const val MOD_ID: String = "hexcellular"
		fun id(string: String) = Identifier(MOD_ID, string)
	}
}