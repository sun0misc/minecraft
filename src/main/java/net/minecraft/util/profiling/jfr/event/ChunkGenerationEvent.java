/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.profiling.jfr.event;

import jdk.jfr.Category;
import jdk.jfr.Enabled;
import jdk.jfr.Event;
import jdk.jfr.EventType;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

@Name(value="minecraft.ChunkGeneration")
@Label(value="Chunk Generation")
@Category(value={"Minecraft", "World Generation"})
@StackTrace(value=false)
@Enabled(value=false)
@DontObfuscate
public class ChunkGenerationEvent
extends Event {
    public static final String EVENT_NAME = "minecraft.ChunkGeneration";
    public static final EventType TYPE = EventType.getEventType(ChunkGenerationEvent.class);
    @Name(value="worldPosX")
    @Label(value="First Block X World Position")
    public final int worldPosX;
    @Name(value="worldPosZ")
    @Label(value="First Block Z World Position")
    public final int worldPosZ;
    @Name(value="chunkPosX")
    @Label(value="Chunk X Position")
    public final int chunkPosX;
    @Name(value="chunkPosZ")
    @Label(value="Chunk Z Position")
    public final int chunkPosZ;
    @Name(value="status")
    @Label(value="Status")
    public final String targetStatus;
    @Name(value="level")
    @Label(value="Level")
    public final String level;

    public ChunkGenerationEvent(ChunkPos chunkPos, RegistryKey<World> world, String targetStatus) {
        this.targetStatus = targetStatus;
        this.level = world.toString();
        this.chunkPosX = chunkPos.x;
        this.chunkPosZ = chunkPos.z;
        this.worldPosX = chunkPos.getStartX();
        this.worldPosZ = chunkPos.getStartZ();
    }

    public static class Names {
        public static final String WORLD_POS_X = "worldPosX";
        public static final String WORLD_POS_Z = "worldPosZ";
        public static final String CHUNK_POS_X = "chunkPosX";
        public static final String CHUNK_POS_Z = "chunkPosZ";
        public static final String STATUS = "status";
        public static final String LEVEL = "level";

        private Names() {
        }
    }
}

