/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.chunk.RenderedChunk;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChunkRendererRegionBuilder {
    private final Long2ObjectMap<ClientChunk> chunks = new Long2ObjectOpenHashMap<ClientChunk>();

    @Nullable
    public ChunkRendererRegion build(World world, ChunkSectionPos arg2) {
        ClientChunk lv = this.method_60900(world, arg2.getSectionX(), arg2.getSectionZ());
        if (lv.getChunk().method_60791(arg2.getSectionY())) {
            return null;
        }
        int i = arg2.getSectionX() - 1;
        int j = arg2.getSectionZ() - 1;
        int k = arg2.getSectionX() + 1;
        int l = arg2.getSectionZ() + 1;
        RenderedChunk[] lvs = new RenderedChunk[9];
        for (int m = j; m <= l; ++m) {
            for (int n = i; n <= k; ++n) {
                int o = ChunkRendererRegion.method_60899(i, j, n, m);
                ClientChunk lv2 = n == arg2.getSectionX() && m == arg2.getSectionZ() ? lv : this.method_60900(world, n, m);
                lvs[o] = lv2.getRenderedChunk();
            }
        }
        return new ChunkRendererRegion(world, i, j, lvs);
    }

    private ClientChunk method_60900(World arg, int i, int j) {
        return this.chunks.computeIfAbsent(ChunkPos.toLong(i, j), l -> new ClientChunk(arg.getChunk(ChunkPos.getPackedX(l), ChunkPos.getPackedZ(l))));
    }

    @Environment(value=EnvType.CLIENT)
    static final class ClientChunk {
        private final WorldChunk chunk;
        @Nullable
        private RenderedChunk renderedChunk;

        ClientChunk(WorldChunk chunk) {
            this.chunk = chunk;
        }

        public WorldChunk getChunk() {
            return this.chunk;
        }

        public RenderedChunk getRenderedChunk() {
            if (this.renderedChunk == null) {
                this.renderedChunk = new RenderedChunk(this.chunk);
            }
            return this.renderedChunk;
        }
    }
}

