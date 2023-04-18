package net.minecraft.world;

import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

public interface WorldView extends BlockRenderView, CollisionView, RedstoneView, BiomeAccess.Storage {
   @Nullable
   Chunk getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create);

   /** @deprecated */
   @Deprecated
   boolean isChunkLoaded(int chunkX, int chunkZ);

   int getTopY(Heightmap.Type heightmap, int x, int z);

   int getAmbientDarkness();

   BiomeAccess getBiomeAccess();

   default RegistryEntry getBiome(BlockPos pos) {
      return this.getBiomeAccess().getBiome(pos);
   }

   default Stream getStatesInBoxIfLoaded(Box box) {
      int i = MathHelper.floor(box.minX);
      int j = MathHelper.floor(box.maxX);
      int k = MathHelper.floor(box.minY);
      int l = MathHelper.floor(box.maxY);
      int m = MathHelper.floor(box.minZ);
      int n = MathHelper.floor(box.maxZ);
      return this.isRegionLoaded(i, k, m, j, l, n) ? this.getStatesInBox(box) : Stream.empty();
   }

   default int getColor(BlockPos pos, ColorResolver colorResolver) {
      return colorResolver.getColor((Biome)this.getBiome(pos).value(), (double)pos.getX(), (double)pos.getZ());
   }

   default RegistryEntry getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
      Chunk lv = this.getChunk(BiomeCoords.toChunk(biomeX), BiomeCoords.toChunk(biomeZ), ChunkStatus.BIOMES, false);
      return lv != null ? lv.getBiomeForNoiseGen(biomeX, biomeY, biomeZ) : this.getGeneratorStoredBiome(biomeX, biomeY, biomeZ);
   }

   RegistryEntry getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ);

   boolean isClient();

   /** @deprecated */
   @Deprecated
   int getSeaLevel();

   DimensionType getDimension();

   default int getBottomY() {
      return this.getDimension().minY();
   }

   default int getHeight() {
      return this.getDimension().height();
   }

   default BlockPos getTopPosition(Heightmap.Type heightmap, BlockPos pos) {
      return new BlockPos(pos.getX(), this.getTopY(heightmap, pos.getX(), pos.getZ()), pos.getZ());
   }

   default boolean isAir(BlockPos pos) {
      return this.getBlockState(pos).isAir();
   }

   default boolean isSkyVisibleAllowingSea(BlockPos pos) {
      if (pos.getY() >= this.getSeaLevel()) {
         return this.isSkyVisible(pos);
      } else {
         BlockPos lv = new BlockPos(pos.getX(), this.getSeaLevel(), pos.getZ());
         if (!this.isSkyVisible(lv)) {
            return false;
         } else {
            for(lv = lv.down(); lv.getY() > pos.getY(); lv = lv.down()) {
               BlockState lv2 = this.getBlockState(lv);
               if (lv2.getOpacity(this, lv) > 0 && !lv2.isLiquid()) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   default float getPhototaxisFavor(BlockPos pos) {
      return this.getBrightness(pos) - 0.5F;
   }

   /** @deprecated */
   @Deprecated
   default float getBrightness(BlockPos pos) {
      float f = (float)this.getLightLevel(pos) / 15.0F;
      float g = f / (4.0F - 3.0F * f);
      return MathHelper.lerp(this.getDimension().ambientLight(), g, 1.0F);
   }

   default Chunk getChunk(BlockPos pos) {
      return this.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
   }

   default Chunk getChunk(int chunkX, int chunkZ) {
      return this.getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);
   }

   default Chunk getChunk(int chunkX, int chunkZ, ChunkStatus status) {
      return this.getChunk(chunkX, chunkZ, status, true);
   }

   @Nullable
   default BlockView getChunkAsView(int chunkX, int chunkZ) {
      return this.getChunk(chunkX, chunkZ, ChunkStatus.EMPTY, false);
   }

   default boolean isWater(BlockPos pos) {
      return this.getFluidState(pos).isIn(FluidTags.WATER);
   }

   default boolean containsFluid(Box box) {
      int i = MathHelper.floor(box.minX);
      int j = MathHelper.ceil(box.maxX);
      int k = MathHelper.floor(box.minY);
      int l = MathHelper.ceil(box.maxY);
      int m = MathHelper.floor(box.minZ);
      int n = MathHelper.ceil(box.maxZ);
      BlockPos.Mutable lv = new BlockPos.Mutable();

      for(int o = i; o < j; ++o) {
         for(int p = k; p < l; ++p) {
            for(int q = m; q < n; ++q) {
               BlockState lv2 = this.getBlockState(lv.set(o, p, q));
               if (!lv2.getFluidState().isEmpty()) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   default int getLightLevel(BlockPos pos) {
      return this.getLightLevel(pos, this.getAmbientDarkness());
   }

   default int getLightLevel(BlockPos pos, int ambientDarkness) {
      return pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000 ? this.getBaseLightLevel(pos, ambientDarkness) : 15;
   }

   /** @deprecated */
   @Deprecated
   default boolean isPosLoaded(int x, int z) {
      return this.isChunkLoaded(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
   }

   /** @deprecated */
   @Deprecated
   default boolean isChunkLoaded(BlockPos pos) {
      return this.isPosLoaded(pos.getX(), pos.getZ());
   }

   /** @deprecated */
   @Deprecated
   default boolean isRegionLoaded(BlockPos min, BlockPos max) {
      return this.isRegionLoaded(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
   }

   /** @deprecated */
   @Deprecated
   default boolean isRegionLoaded(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      return maxY >= this.getBottomY() && minY < this.getTopY() ? this.isRegionLoaded(minX, minZ, maxX, maxZ) : false;
   }

   /** @deprecated */
   @Deprecated
   default boolean isRegionLoaded(int minX, int minZ, int maxX, int maxZ) {
      int m = ChunkSectionPos.getSectionCoord(minX);
      int n = ChunkSectionPos.getSectionCoord(maxX);
      int o = ChunkSectionPos.getSectionCoord(minZ);
      int p = ChunkSectionPos.getSectionCoord(maxZ);

      for(int q = m; q <= n; ++q) {
         for(int r = o; r <= p; ++r) {
            if (!this.isChunkLoaded(q, r)) {
               return false;
            }
         }
      }

      return true;
   }

   DynamicRegistryManager getRegistryManager();

   FeatureSet getEnabledFeatures();

   default RegistryWrapper createCommandRegistryWrapper(RegistryKey registryRef) {
      Registry lv = this.getRegistryManager().get(registryRef);
      return lv.getReadOnlyWrapper().withFeatureFilter(this.getEnabledFeatures());
   }
}
