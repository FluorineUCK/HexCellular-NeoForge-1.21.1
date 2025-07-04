package miyucomics.hexcellular.action

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import miyucomics.hexcellular.PropertyIota
import miyucomics.hexcellular.getProperty

object OpReadonlyProperty : ConstMediaAction {
	override val argc = 1
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val name = args.getProperty(0, OpSetProperty.argc)
		if ((args[0] as PropertyIota).readonly)
			throw MishapInvalidIota.of(args[0], 1, "writeable_prop")
		return listOf(PropertyIota(name, true))
	}
}