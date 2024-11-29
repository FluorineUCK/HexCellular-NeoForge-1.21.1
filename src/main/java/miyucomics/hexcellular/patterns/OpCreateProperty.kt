package miyucomics.hexcellular.patterns

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.misc.MediaConstants
import miyucomics.hexcellular.PropertyIota
import miyucomics.hexcellular.StateStorage
import miyucomics.hexcellular.generatePropertyName

class OpCreateProperty : ConstMediaAction {
	override val argc = 0
	override val mediaCost = MediaConstants.CRYSTAL_UNIT

	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val state = StateStorage.getServerState(env.world.server)
		var property = generatePropertyName()
		while (state.properties.containsKey(property))
			property = generatePropertyName()
		StateStorage.setProperty(env.world, property, NullIota())
		return listOf(PropertyIota(property))
	}
}