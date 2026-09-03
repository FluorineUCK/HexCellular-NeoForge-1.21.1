package miyucomics.hexcellular

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.HexRegistries
import miyucomics.hexcellular.action.OpCreateProperty
import miyucomics.hexcellular.action.OpObserveProperty
import miyucomics.hexcellular.action.OpReadonlyProperty
import miyucomics.hexcellular.action.OpSetProperty
import net.neoforged.neoforge.registries.RegisterEvent

object HexcellularActions {
    fun register(event: RegisterEvent) {
        register(event, "create_property", "aawe", OpCreateProperty)
        register(event, "observe_property", "aawd", OpObserveProperty)
        register(event, "set_property", "aawq", OpSetProperty)
        register(event, "readonly_property", "aawa", OpReadonlyProperty)
    }

    private fun register(event: RegisterEvent, name: String, signature: String, action: Action) {
        event.register(HexRegistries.ACTION, HexcellularNeo.id(name)) {
            ActionRegistryEntry(HexPattern.fromAngles(signature, HexDir.SOUTH_WEST), action)
        }
    }
}
