package miyucomics.hexcellular.action

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import at.petrak.hexcasting.api.misc.MediaConstants
import miyucomics.hexcellular.PropertyIota
import miyucomics.hexcellular.StateStorage
import miyucomics.hexcellular.getProperty
import net.minecraft.server.network.ServerPlayerEntity

object OpSetProperty : ConstMediaAction {
	override val argc = 2
	override val mediaCost = MediaConstants.DUST_UNIT / 10
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val name = args.getProperty(0, argc)
		if ((args[0] as PropertyIota).readonly)
			throw MishapInvalidIota.of(args[0], 1, "writeable_prop")

		val iota = args[1]
		val trueName = MishapOthersName.getTrueNameFromDatum(iota, env.castingEntity as? ServerPlayerEntity)
		if (trueName != null)
			throw MishapOthersName(trueName)
		StateStorage.setProperty(env.world, name, iota)
		return listOf()
	}
}