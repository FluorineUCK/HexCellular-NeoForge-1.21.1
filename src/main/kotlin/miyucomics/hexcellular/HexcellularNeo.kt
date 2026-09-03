package miyucomics.hexcellular

import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.common.lib.HexItems
import at.petrak.hexcasting.common.lib.HexRegistries
import at.petrak.hexcasting.xplat.IXplatAbstractions
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.server.ServerStartedEvent
import net.neoforged.neoforge.registries.RegisterEvent
import org.slf4j.LoggerFactory

@Mod(HexcellularNeo.MOD_ID)
class HexcellularNeo(modBus: IEventBus) {
    init {
        modBus.addListener(this::registerHexContent)
        NeoForge.EVENT_BUS.addListener(this::registerCommands)
        NeoForge.EVENT_BUS.addListener(HexcellularProbeValidation::onServerStarted)
    }

    private fun registerHexContent(event: RegisterEvent) {
        event.register(HexRegistries.IOTA_TYPE, id("property")) { PropertyIota.TYPE }
        HexcellularActions.register(event)
    }

    private fun registerCommands(event: RegisterCommandsEvent) {
        val dispatcher = event.dispatcher
        dispatcher.register(
            Commands.literal("hexcellular")
                .requires { source -> source.hasPermission(2) }
                .then(
                    Commands.literal("giveProperty")
                        .then(
                            Commands.argument("property", StringArgumentType.word())
                                .suggests { context, builder ->
                                    StateStorage.listProperties(context.source.level)
                                        .filter { it.startsWith(builder.remaining, ignoreCase = true) }
                                        .forEach(builder::suggest)
                                    builder.buildFuture()
                                }
                                .executes { context ->
                                    val property = StringArgumentType.getString(context, "property")
                                    val player = context.source.playerOrException
                                    val stack = ItemStack(HexItems.THOUGHT_KNOT.get())
                                    IXplatAbstractions.INSTANCE.findDataHolder(stack)
                                        ?.writeIota(PropertyIota(property), false)
                                    player.addItem(stack)
                                    if (property !in StateStorage.listProperties(context.source.level)) {
                                        StateStorage.setProperty(context.source.level, property, NullIota())
                                    }
                                    context.source.sendSuccess(
                                        { Component.translatable("hexcellular.property.given", property, player.name) },
                                        false,
                                    )
                                    1
                                },
                        ),
                )
                .then(
                    Commands.literal("removeProperty")
                        .then(
                            Commands.argument("property", StringArgumentType.word())
                                .suggests { context, builder ->
                                    StateStorage.listProperties(context.source.level)
                                        .filter { it.startsWith(builder.remaining, ignoreCase = true) }
                                        .forEach(builder::suggest)
                                    builder.buildFuture()
                                }
                                .executes { context ->
                                    val property = StringArgumentType.getString(context, "property")
                                    StateStorage.removeProperty(context.source.level, property)
                                    context.source.sendSuccess(
                                        { Component.translatable("hexcellular.property.removed", property) },
                                        false,
                                    )
                                    1
                                },
                        ),
                ),
        )
    }

    companion object {
        const val MOD_ID = "hexcellular"
        val LOGGER = LoggerFactory.getLogger(MOD_ID)

        @JvmStatic
        fun id(path: String): ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
    }
}
