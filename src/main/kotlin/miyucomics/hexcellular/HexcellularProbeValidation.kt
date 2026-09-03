package miyucomics.hexcellular

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.common.lib.HexRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.world.InteractionHand
import net.neoforged.neoforge.common.util.FakePlayerFactory
import net.neoforged.neoforge.event.server.ServerStartedEvent

object HexcellularProbeValidation {
    private const val ENABLE_PROPERTY = "hexcellular.probe.validateRegistries"

    @JvmStatic
    fun onServerStarted(event: ServerStartedEvent) {
        if (!java.lang.Boolean.getBoolean(ENABLE_PROPERTY)) {
            return
        }

        var failures = 0
        try {
            val server = event.server
            val level = server.overworld()
            val actions = server.registryAccess().registryOrThrow(HexRegistries.ACTION)
            val iotaTypes = server.registryAccess().registryOrThrow(HexRegistries.IOTA_TYPE)
            val expected = linkedMapOf(
                "create_property" to "aawe",
                "observe_property" to "aawd",
                "set_property" to "aawq",
                "readonly_property" to "aawa",
            )

            val missing = expected.keys.filter { !actions.containsKey(HexcellularNeo.id(it)) }
            val wrongSignatures = expected.filter { (name, signature) ->
                actions.get(HexcellularNeo.id(name))?.prototype()?.anglesSignature() != signature
            }
            val iotaTypeOk = iotaTypes.get(HexcellularNeo.id("property")) === PropertyIota.TYPE
            val commandOk = server.commands.dispatcher.root.getChild("hexcellular") != null
            if (missing.isEmpty() && wrongSignatures.isEmpty() && iotaTypeOk && commandOk) {
                HexcellularNeo.LOGGER.info(
                    "[HEXCELLULAR-PROBE] registries=PASS actions={} iota_type=true commands=true",
                    expected.size,
                )
            } else {
                failures++
                HexcellularNeo.LOGGER.error(
                    "[HEXCELLULAR-PROBE] registries=FAIL missing={} wrong_signatures={} iota_type={} commands={}",
                    missing,
                    wrongSignatures,
                    iotaTypeOk,
                    commandOk,
                )
            }

            val original = PropertyIota("probe-property", false)
            val readonly = PropertyIota("probe-property", true)
            val encoded = PropertyIota.TYPE.codec().codec().encodeStart(NbtOps.INSTANCE, readonly).result().orElse(null)
            val decoded =
                if (encoded == null) null
                else PropertyIota.TYPE.codec().codec().parse(NbtOps.INSTANCE, encoded).result().orElse(null)
            val codecOk = decoded?.name == readonly.name && decoded.readonly
            val equalityOk = Iota.tolerates(original, PropertyIota(original.name, false)) &&
                !Iota.tolerates(original, readonly)
            val displayOk = readonly.display().style.isBold
            if (codecOk && equalityOk && displayOk) {
                HexcellularNeo.LOGGER.info(
                    "[HEXCELLULAR-PROBE] property_iota=PASS codec=true readonly_equality=true styled=true",
                )
            } else {
                failures++
                HexcellularNeo.LOGGER.error(
                    "[HEXCELLULAR-PROBE] property_iota=FAIL codec={} equality={} display={}",
                    codecOk,
                    equalityOk,
                    displayOk,
                )
            }

            val env = StaffCastEnv(FakePlayerFactory.getMinecraft(level), InteractionHand.MAIN_HAND)
            val create = actions.get(HexcellularNeo.id("create_property"))!!.action() as SpellAction
            val createResult = create.execute(emptyList(), env)
            val createdImage = createResult.effect.cast(env, CastingImage())
            val created = createdImage?.stack?.lastOrNull() as? PropertyIota
            val createOk = createResult.cost == MediaConstants.CRYSTAL_UNIT * 5 &&
                created != null &&
                StateStorage.listProperties(level).contains(created.name)

            val set = actions.get(HexcellularNeo.id("set_property"))!!.action() as ConstMediaAction
            val observe = actions.get(HexcellularNeo.id("observe_property"))!!.action() as ConstMediaAction
            val makeReadonly = actions.get(HexcellularNeo.id("readonly_property"))!!.action() as ConstMediaAction
            val value = DoubleIota(64.0)
            if (created != null) {
                set.execute(listOf(created, value), env)
            }
            val observed =
                if (created == null) null
                else observe.execute(listOf(created), env).singleOrNull() as? DoubleIota
            val readonlyCopy =
                if (created == null) null
                else makeReadonly.execute(listOf(created), env).singleOrNull() as? PropertyIota
            val readonlyRejected =
                try {
                    if (readonlyCopy != null) {
                        set.execute(listOf(readonlyCopy, value), env)
                    }
                    false
                } catch (_: MishapInvalidIota) {
                    true
                }
            val actionsOk = createOk &&
                set.mediaCost == MediaConstants.DUST_UNIT / 10 &&
                observed?.double == value.double &&
                readonlyCopy?.readonly == true &&
                readonlyRejected
            if (actionsOk) {
                HexcellularNeo.LOGGER.info(
                    "[HEXCELLULAR-PROBE] actions=PASS create_cost={} set_cost={} property={} readonly_rejected=true",
                    createResult.cost,
                    set.mediaCost,
                    created?.name,
                )
            } else {
                failures++
                HexcellularNeo.LOGGER.error(
                    "[HEXCELLULAR-PROBE] actions=FAIL create_ok={} set_cost={} observed={} readonly={} readonly_rejected={}",
                    createOk,
                    set.mediaCost,
                    observed?.double,
                    readonlyCopy?.readonly,
                    readonlyRejected,
                )
            }

            val persistName = "hexcellular-probe-persistence"
            StateStorage.setProperty(level, persistName, DoubleIota(91.0))
            val saved = StateStorage.saveForProbe(level)
            val reloaded = StateStorage.loadForProbe(saved, level.registryAccess())
            val serialized = reloaded.properties[persistName]
            val restored =
                if (serialized == null) null
                else at.petrak.hexcasting.api.casting.iota.IotaType.TYPED_CODEC
                    .parse(NbtOps.INSTANCE, serialized)
                    .result()
                    .orElse(null) as? DoubleIota
            val persistenceOk = restored?.double == 91.0 && !saved.contains("properties")
            if (persistenceOk) {
                HexcellularNeo.LOGGER.info(
                    "[HEXCELLULAR-PROBE] persistence=PASS global_overworld=true root_format=true",
                )
            } else {
                failures++
                HexcellularNeo.LOGGER.error(
                    "[HEXCELLULAR-PROBE] persistence=FAIL restored={} tag={}",
                    restored?.double,
                    saved,
                )
            }

            if (failures == 0) {
                HexcellularNeo.LOGGER.info(
                    "[HEXCELLULAR-PROBE] complete=PASS registries=true property_iota=true actions=true persistence=true",
                )
            } else {
                HexcellularNeo.LOGGER.error("[HEXCELLULAR-PROBE] complete=FAIL failures={}", failures)
            }
        } catch (throwable: Throwable) {
            HexcellularNeo.LOGGER.error("[HEXCELLULAR-PROBE] complete=FAIL exception", throwable)
        } finally {
            event.server.halt(false)
        }
    }
}
