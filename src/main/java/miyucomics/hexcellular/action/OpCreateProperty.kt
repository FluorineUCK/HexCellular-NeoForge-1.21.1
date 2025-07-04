package miyucomics.hexcellular.action

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.misc.MediaConstants
import miyucomics.hexcellular.PropertyIota
import miyucomics.hexcellular.StateStorage
import miyucomics.hexcellular.generatePropertyName

object OpCreateProperty : SpellAction {
	override val argc = 0
	override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
		val state = StateStorage.getServerState(env.world.server)
		var property = generatePropertyName()
		while (state.properties.containsKey(property))
			property = generatePropertyName()
		return SpellAction.Result(Spell(property), MediaConstants.CRYSTAL_UNIT * 5, listOf())
	}

	private data class Spell(private val property: String) : RenderedSpell {
		override fun cast(env: CastingEnvironment) {}
		override fun cast(env: CastingEnvironment, image: CastingImage): CastingImage? {
			StateStorage.setProperty(env.world, property, NullIota())
			return image.copy(stack = image.stack.plus(PropertyIota(property)))
		}
	}
}