package miyucomics.hexcellular.patterns

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import at.petrak.hexcasting.api.misc.MediaConstants
import miyucomics.hexcellular.StateStorage
import miyucomics.hexcellular.getProperty
import net.minecraft.server.network.ServerPlayerEntity

class OpSetProperty : ConstMediaAction {
	override val argc = 2
	override val mediaCost = MediaConstants.DUST_UNIT / 10
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val name = args.getProperty(0, 2)
		val iota = args[1]
		val trueName = MishapOthersName.getTrueNameFromDatum(iota, env.castingEntity as? ServerPlayerEntity)
		if (trueName != null)
			throw MishapOthersName(trueName)
		StateStorage.setProperty(env.world, name, iota)
		return listOf()
	}
}