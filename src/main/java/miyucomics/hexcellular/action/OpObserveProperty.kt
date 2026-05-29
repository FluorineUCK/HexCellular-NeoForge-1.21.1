package miyucomics.hexcellular.action

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import miyucomics.hexcellular.StateStorage
import miyucomics.hexcellular.getProperty

object OpObserveProperty : ConstMediaAction {
	override val argc = 1
	override fun execute(args: List<Iota>, env: CastingEnvironment) =
		listOf(StateStorage.getProperty(env.world, args.getProperty(0, argc)))
}