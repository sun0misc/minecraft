package net.minecraft.client.render.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ChunkRendererRegionBuilder {
   private final Long2ObjectMap chunks = new Long2ObjectOpenHashMap();

   @Nullable
   public ChunkRendererRegion build(World world, BlockPos startPos, BlockPos endPos, int offset) {
      int j = ChunkSectionPos.getSectionCoord(startPos.getX() - offset);
      int k = ChunkSectionPos.getSectionCoord(startPos.getZ() - offset);
      int l = ChunkSectionPos.getSectionCoord(endPos.getX() + offset);
      int m = ChunkSectionPos.getSectionCoord(endPos.getZ() + offset);
      ClientChunk[][] lvs = new ClientChunk[l - j + 1][m - k + 1];

      int o;
      for(int n = j; n <= l; ++n) {
         for(o = k; o <= m; ++o) {
            lvs[n - j][o - k] = (ClientChunk)this.chunks.computeIfAbsent(ChunkPos.toLong(n, o), (pos) -> {
               return new ClientChunk(world.getChunk(ChunkPos.getPackedX(pos), ChunkPos.getPackedZ(pos)));
            });
         }
      }

      if (isEmptyBetween(startPos, endPos, j, k, lvs)) {
         return null;
      } else {
         RenderedChunk[][] lvs2 = new RenderedChunk[l - j + 1][m - k + 1];

         for(o = j; o <= l; ++o) {
            for(int p = k; p <= m; ++p) {
               lvs2[o - j][p - k] = lvs[o - j][p - k].getRenderedChunk();
            }
         }

         return new ChunkRendererRegion(world, j, k, lvs2);
      }
   }

   private static boolean isEmptyBetween(BlockPos startPos, BlockPos endPos, int offsetX, int offsetZ, ClientChunk[][] chunks) {
      int k = ChunkSectionPos.getSectionCoord(startPos.getX());
      int l = ChunkSectionPos.getSectionCoord(startPos.getZ());
      int m = ChunkSectionPos.getSectionCoord(endPos.getX());
      int n = ChunkSectionPos.getSectionCoord(endPos.getZ());

      for(int o = k; o <= m; ++o) {
         for(int p = l; p <= n; ++p) {
            WorldChunk lv = chunks[o - offsetX][p - offsetZ].getChunk();
            if (!lv.areSectionsEmptyBetween(startPos.getY(), endPos.getY())) {
               return false;
            }
         }
      }

      return true;
   }

   @Environment(EnvType.CLIENT)
   private static final class ClientChunk {
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
