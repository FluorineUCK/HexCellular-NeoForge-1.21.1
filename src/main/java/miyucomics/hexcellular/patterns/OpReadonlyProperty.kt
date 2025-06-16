package miyucomics.hexcellular.patterns

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import at.petrak.hexcasting.api.misc.MediaConstants
import miyucomics.hexcellular.PropertyIota
import miyucomics.hexcellular.StateStorage
import miyucomics.hexcellular.getProperty
import net.minecraft.server.network.ServerPlayerEntity

class OpReadonlyProperty : ConstMediaAction {
	override val argc = 1
	override fun execute(args: List<Iota>, env: CastingEnvironment) =
		listOf(PropertyIota(args.getProperty(0, argc), true))
}