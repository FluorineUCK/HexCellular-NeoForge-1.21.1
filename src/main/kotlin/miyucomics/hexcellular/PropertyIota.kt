package miyucomics.hexcellular

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.ChatFormatting
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

/**
 * A stable reference to a server-wide Hexcellular property.
 *
 * The JVM field names intentionally remain `name` and `readonly`: HexOverpowered
 * and Hexic both have optional compatibility hooks targeting the upstream ABI.
 */
class PropertyIota(
    val name: String,
    val readonly: Boolean = false,
) : Iota({ TYPE }) {
    override fun isTruthy() = true

    override fun toleratesOther(that: Iota) =
        that is PropertyIota && name == that.name && readonly == that.readonly

    override fun display(): Component =
        Component.literal(name)
            .withStyle(ChatFormatting.GREEN)
            .let { if (readonly) it.withStyle(ChatFormatting.BOLD) else it }

    override fun hashCode() = 31 * name.hashCode() + readonly.hashCode()

    companion object {
        @JvmField
        val TYPE: IotaType<PropertyIota> = object : IotaType<PropertyIota>() {
            override fun codec() = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    Codec.STRING.fieldOf("name").forGetter(PropertyIota::name),
                    Codec.BOOL.optionalFieldOf("readonly", false).forGetter(PropertyIota::readonly),
                ).apply(instance, ::PropertyIota)
            }

            override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, PropertyIota> =
                ByteBufCodecs.fromCodecWithRegistries(codec().codec())

            override fun color() = -0x591c5f
        }
    }
}

/**
 * Kept as a top-level function so the generated ABI remains
 * `PropertyIotaKt.getProperty(List, int, int)`, matching upstream optional Mixins.
 */
fun List<Iota>.getProperty(idx: Int, argc: Int): String {
    val value = getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, size) }
    if (value is PropertyIota) {
        return value.name
    }
    throw MishapInvalidIota.ofType(value, if (argc == 0) idx else argc - (idx + 1), "property")
}
