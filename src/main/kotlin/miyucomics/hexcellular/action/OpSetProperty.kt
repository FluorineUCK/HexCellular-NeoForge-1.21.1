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
import net.minecraft.world.entity.player.Player

object OpSetProperty : ConstMediaAction {
    override val argc = 2
    override val mediaCost = MediaConstants.DUST_UNIT / 10

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val name = args.getProperty(0, argc)
        if ((args[0] as PropertyIota).readonly) {
            throw MishapInvalidIota.of(args[0], 1, "writeable_prop")
        }

        val iota = args[1]
        val trueNameMishap = MishapOthersName.getTrueNameMishapFromDatum(
            env.world,
            iota,
            env.castingEntity as? Player,
        )
        if (trueNameMishap != null) {
            throw trueNameMishap
        }
        StateStorage.setProperty(env.world, name, iota)
        return emptyList()
    }
}
