package miyucomics.hexcellular

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.NullIota
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.PersistentState
import net.minecraft.world.World
import java.util.function.Consumer

class StateStorage : PersistentState() {
	val properties: HashMap<String, NbtCompound> = HashMap()

	override fun writeNbt(nbt: NbtCompound): NbtCompound {
		properties.forEach { (key: String?, element: NbtCompound?) -> nbt.put(key, element) }
		return nbt
	}

	companion object {
		private fun createFromNbt(nbt: NbtCompound): StateStorage {
			val state = StateStorage()
			nbt.keys.forEach(Consumer { key: String -> state.properties[key] = nbt.getCompound(key) })
			return state
		}

		fun getServerState(server: MinecraftServer): StateStorage {
			val persistentStateManager = server.getWorld(World.OVERWORLD)!!.persistentStateManager
			return persistentStateManager.getOrCreate(::createFromNbt, ::StateStorage, HexcellularMain.MOD_ID)
		}

		fun listProperties(world: ServerWorld) = getServerState(world.server).properties.keys.toList()

		fun setProperty(world: ServerWorld, name: String, iota: Iota) {
			val state = getServerState(world.server)
			state.properties[name] = IotaType.serialize(iota)
			state.markDirty()
		}

		fun removeProperty(world: ServerWorld, name: String) {
			val state = getServerState(world.server)
			state.properties.remove(name)
			state.markDirty()
		}

		fun getProperty(world: ServerWorld, name: String): Iota {
			val state = getServerState(world.server)
			if (!state.properties.containsKey(name))
				return NullIota()
			return IotaType.deserialize(state.properties[name]!!, world)
		}
	}
}