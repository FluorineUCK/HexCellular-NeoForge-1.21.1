package miyucomics.hexcellular

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.NullIota
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.saveddata.SavedData

/**
 * Server-global property storage, matching upstream's use of the overworld
 * PersistentState manager. Values stay serialized so world-sensitive iotas are
 * decoded against the world from which they are observed.
 */
class StateStorage : SavedData() {
    val properties: HashMap<String, CompoundTag> = HashMap()

    override fun save(tag: CompoundTag, registries: HolderLookup.Provider): CompoundTag {
        properties.forEach { (name, value) -> tag.put(name, value.copy()) }
        return tag
    }

    companion object {
        const val ID = "hexcellular"
        const val LEGACY_EMBEDDED_ID = "iotaworks_hexcellular_state"
        private const val LEGACY_PROPERTIES_KEY = "properties"

        @JvmField
        val FACTORY = SavedData.Factory(::StateStorage, ::createFromNbt)

        private fun createFromNbt(tag: CompoundTag, registries: HolderLookup.Provider): StateStorage {
            val state = StateStorage()
            val source =
                if (tag.contains(LEGACY_PROPERTIES_KEY, Tag.TAG_COMPOUND.toInt())) {
                    tag.getCompound(LEGACY_PROPERTIES_KEY)
                } else {
                    tag
                }
            source.allKeys.forEach { key ->
                if (source.contains(key, Tag.TAG_COMPOUND.toInt())) {
                    state.properties[key] = source.getCompound(key).copy()
                }
            }
            return state
        }

        @JvmStatic
        fun getServerState(server: MinecraftServer): StateStorage {
            val storage = server.overworld().dataStorage
            val state = storage.computeIfAbsent(FACTORY, ID)
            if (state.properties.isEmpty()) {
                val legacy = storage.get(FACTORY, LEGACY_EMBEDDED_ID)
                if (legacy != null && legacy.properties.isNotEmpty()) {
                    legacy.properties.forEach { (name, value) ->
                        state.properties[name] = value.copy()
                    }
                    state.setDirty()
                }
            }
            return state
        }

        @JvmStatic
        fun listProperties(world: ServerLevel): List<String> =
            getServerState(world.server).properties.keys.sorted()

        @JvmStatic
        fun setProperty(world: ServerLevel, name: String, iota: Iota) {
            val state = getServerState(world.server)
            state.properties[name] = encode(iota)
            state.setDirty()
        }

        @JvmStatic
        fun removeProperty(world: ServerLevel, name: String) {
            val state = getServerState(world.server)
            if (state.properties.remove(name) != null) {
                state.setDirty()
            }
        }

        @JvmStatic
        fun getProperty(world: ServerLevel, name: String): Iota {
            val value = getServerState(world.server).properties[name] ?: return NullIota()
            return decode(value) ?: NullIota()
        }

        @JvmStatic
        fun saveForProbe(world: ServerLevel): CompoundTag =
            getServerState(world.server).save(CompoundTag(), world.registryAccess())

        @JvmStatic
        fun loadForProbe(tag: CompoundTag, registries: HolderLookup.Provider): StateStorage =
            createFromNbt(tag, registries)

        private fun encode(iota: Iota): CompoundTag =
            IotaType.TYPED_CODEC
                .encodeStart(NbtOps.INSTANCE, iota)
                .result()
                .filter { it is CompoundTag }
                .map { it as CompoundTag }
                .orElseThrow { IllegalArgumentException("Unable to encode Hexcellular property iota ${iota.javaClass.name}") }

        private fun decode(tag: CompoundTag): Iota? =
            IotaType.TYPED_CODEC.parse(NbtOps.INSTANCE, tag).result().orElse(null)
    }
}
