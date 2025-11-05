package miyucomics.hexcellular

import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.common.lib.HexItems
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import at.petrak.hexcasting.xplat.IXplatAbstractions
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandSource
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registry
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class HexcellularMain : ModInitializer {
	override fun onInitialize() {
		HexcellularActions.init()
		Registry.register(HexIotaTypes.REGISTRY, id("property"), PropertyIota.TYPE)

		CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
			dispatcher.register(CommandManager.literal("hexcellular")
				.requires { source -> source.hasPermissionLevel(2) }
				.then(createPropertySubcommand("giveProperty") { context, world, property ->
					context.source.playerOrThrow.giveItemStack(ItemStack(HexItems.THOUGHT_KNOT).also { IXplatAbstractions.INSTANCE.findDataHolder(it)?.writeIota(PropertyIota(property, false), false) })
					context.source.sendFeedback({ Text.translatable("hexcellular.property.given", property, context.source.playerOrThrow.name) }, false)
					if (!StateStorage.listProperties(world).contains(property))
						StateStorage.setProperty(world, property, NullIota())
					return@createPropertySubcommand 1
				})
				.then(createPropertySubcommand("removeProperty") { context, world, property ->
					StateStorage.removeProperty(world, property)
					context.source.sendFeedback({ Text.translatable("hexcellular.property.removed", property) }, false)
					return@createPropertySubcommand 1
				})
			)
		}
	}

	fun createPropertySubcommand(name: String, method: (CommandContext<ServerCommandSource>, ServerWorld, String) -> Int): LiteralArgumentBuilder<ServerCommandSource> {
		return CommandManager.literal(name)
			.then(CommandManager.argument("property", StringArgumentType.word()).suggests { context, builder ->
				StateStorage.listProperties(context.source.world).filter { CommandSource.shouldSuggest(builder.remaining, it) }.forEach(builder::suggest)
				builder.buildFuture()
			}.executes { method(it, it.source.world, StringArgumentType.getString(it, "property")) }
		)
	}

	companion object {
		const val MOD_ID: String = "hexcellular"
		fun id(string: String) = Identifier(MOD_ID, string)
	}
}