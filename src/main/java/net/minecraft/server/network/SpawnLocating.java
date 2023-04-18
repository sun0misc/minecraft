package net.minecraft.server.network;

import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

public class SpawnLocating {
   @Nullable
   protected static BlockPos findOverworldSpawn(ServerWorld world, int x, int z) {
      boolean bl = world.getDimension().hasCeiling();
      WorldChunk lv = world.getChunk(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
      int k = bl ? world.getChunkManager().getChunkGenerator().getSpawnHeight(world) : lv.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, x & 15, z & 15);
      if (k < world.getBottomY()) {
         return null;
      } else {
         int l = lv.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x & 15, z & 15);
         if (l <= k && l > lv.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR, x & 15, z & 15)) {
            return null;
         } else {
            BlockPos.Mutable lv2 = new BlockPos.Mutable();

            for(int m = k + 1; m >= world.getBottomY(); --m) {
               lv2.set(x, m, z);
               BlockState lv3 = world.getBlockState(lv2);
               if (!lv3.getFluidState().isEmpty()) {
                  break;
               }

               if (Block.isFaceFullSquare(lv3.getCollisionShape(world, lv2), Direction.UP)) {
                  return lv2.up().toImmutable();
               }
            }

            return null;
         }
      }
   }

   @Nullable
   public static BlockPos findServerSpawnPoint(ServerWorld world, ChunkPos chunkPos) {
      if (SharedConstants.isOutsideGenerationArea(chunkPos)) {
         return null;
      } else {
         for(int i = chunkPos.getStartX(); i <= chunkPos.getEndX(); ++i) {
            for(int j = chunkPos.getStartZ(); j <= chunkPos.getEndZ(); ++j) {
               BlockPos lv = findOverworldSpawn(world, i, j);
               if (lv != null) {
                  return lv;
               }
            }
         }

         return null;
      }
   }
}
