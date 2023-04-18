package net.minecraft.world.gen.chunk;

import com.google.common.primitives.Doubles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.EightWayDirection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;

public class BlendingData {
   private static final double field_35514 = 0.1;
   protected static final int field_36280 = 4;
   protected static final int field_35511 = 8;
   protected static final int field_36281 = 2;
   private static final double field_37704 = 1.0;
   private static final double field_37705 = -1.0;
   private static final int field_35516 = 2;
   private static final int BIOMES_PER_CHUNK = BiomeCoords.fromBlock(16);
   private static final int LAST_CHUNK_BIOME_INDEX;
   private static final int CHUNK_BIOME_END_INDEX;
   private static final int NORTH_WEST_END_INDEX;
   private static final int SOUTH_EAST_END_INDEX_PART;
   private static final int HORIZONTAL_BIOME_COUNT;
   private final HeightLimitView oldHeightLimit;
   private static final List SURFACE_BLOCKS;
   protected static final double field_35513 = Double.MAX_VALUE;
   private boolean initializedBlendingData;
   private final double[] surfaceHeights;
   private final List biomes;
   private final transient double[][] collidableBlockDensities;
   private static final Codec DOUBLE_ARRAY_CODEC;
   public static final Codec CODEC;

   private static DataResult validate(BlendingData data) {
      return data.surfaceHeights.length != HORIZONTAL_BIOME_COUNT ? DataResult.error(() -> {
         return "heights has to be of length " + HORIZONTAL_BIOME_COUNT;
      }) : DataResult.success(data);
   }

   private BlendingData(int oldBottomSectionY, int oldTopSectionY, Optional heights) {
      this.surfaceHeights = (double[])heights.orElse((double[])Util.make(new double[HORIZONTAL_BIOME_COUNT], (heights2) -> {
         Arrays.fill(heights2, Double.MAX_VALUE);
      }));
      this.collidableBlockDensities = new double[HORIZONTAL_BIOME_COUNT][];
      ObjectArrayList objectArrayList = new ObjectArrayList(HORIZONTAL_BIOME_COUNT);
      objectArrayList.size(HORIZONTAL_BIOME_COUNT);
      this.biomes = objectArrayList;
      int k = ChunkSectionPos.getBlockCoord(oldBottomSectionY);
      int l = ChunkSectionPos.getBlockCoord(oldTopSectionY) - k;
      this.oldHeightLimit = HeightLimitView.create(k, l);
   }

   @Nullable
   public static BlendingData getBlendingData(ChunkRegion chunkRegion, int chunkX, int chunkZ) {
      Chunk lv = chunkRegion.getChunk(chunkX, chunkZ);
      BlendingData lv2 = lv.getBlendingData();
      if (lv2 == null) {
         return null;
      } else {
         lv2.initChunkBlendingData(lv, getAdjacentChunksWithNoise(chunkRegion, chunkX, chunkZ, false));
         return lv2;
      }
   }

   public static Set getAdjacentChunksWithNoise(StructureWorldAccess access, int chunkX, int chunkZ, boolean oldNoise) {
      Set set = EnumSet.noneOf(EightWayDirection.class);
      EightWayDirection[] var5 = EightWayDirection.values();
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         EightWayDirection lv = var5[var7];
         int k = chunkX + lv.getOffsetX();
         int l = chunkZ + lv.getOffsetZ();
         if (access.getChunk(k, l).usesOldNoise() == oldNoise) {
            set.add(lv);
         }
      }

      return set;
   }

   private void initChunkBlendingData(Chunk chunk, Set newNoiseChunkDirections) {
      if (!this.initializedBlendingData) {
         if (newNoiseChunkDirections.contains(EightWayDirection.NORTH) || newNoiseChunkDirections.contains(EightWayDirection.WEST) || newNoiseChunkDirections.contains(EightWayDirection.NORTH_WEST)) {
            this.initBlockColumn(getNorthWestIndex(0, 0), chunk, 0, 0);
         }

         int i;
         if (newNoiseChunkDirections.contains(EightWayDirection.NORTH)) {
            for(i = 1; i < BIOMES_PER_CHUNK; ++i) {
               this.initBlockColumn(getNorthWestIndex(i, 0), chunk, 4 * i, 0);
            }
         }

         if (newNoiseChunkDirections.contains(EightWayDirection.WEST)) {
            for(i = 1; i < BIOMES_PER_CHUNK; ++i) {
               this.initBlockColumn(getNorthWestIndex(0, i), chunk, 0, 4 * i);
            }
         }

         if (newNoiseChunkDirections.contains(EightWayDirection.EAST)) {
            for(i = 1; i < BIOMES_PER_CHUNK; ++i) {
               this.initBlockColumn(getSouthEastIndex(CHUNK_BIOME_END_INDEX, i), chunk, 15, 4 * i);
            }
         }

         if (newNoiseChunkDirections.contains(EightWayDirection.SOUTH)) {
            for(i = 0; i < BIOMES_PER_CHUNK; ++i) {
               this.initBlockColumn(getSouthEastIndex(i, CHUNK_BIOME_END_INDEX), chunk, 4 * i, 15);
            }
         }

         if (newNoiseChunkDirections.contains(EightWayDirection.EAST) && newNoiseChunkDirections.contains(EightWayDirection.NORTH_EAST)) {
            this.initBlockColumn(getSouthEastIndex(CHUNK_BIOME_END_INDEX, 0), chunk, 15, 0);
         }

         if (newNoiseChunkDirections.contains(EightWayDirection.EAST) && newNoiseChunkDirections.contains(EightWayDirection.SOUTH) && newNoiseChunkDirections.contains(EightWayDirection.SOUTH_EAST)) {
            this.initBlockColumn(getSouthEastIndex(CHUNK_BIOME_END_INDEX, CHUNK_BIOME_END_INDEX), chunk, 15, 15);
         }

         this.initializedBlendingData = true;
      }
   }

   private void initBlockColumn(int index, Chunk chunk, int chunkBlockX, int chunkBlockZ) {
      if (this.surfaceHeights[index] == Double.MAX_VALUE) {
         this.surfaceHeights[index] = (double)this.getSurfaceBlockY(chunk, chunkBlockX, chunkBlockZ);
      }

      this.collidableBlockDensities[index] = this.calculateCollidableBlockDensityColumn(chunk, chunkBlockX, chunkBlockZ, MathHelper.floor(this.surfaceHeights[index]));
      this.biomes.set(index, this.getVerticalBiomeSections(chunk, chunkBlockX, chunkBlockZ));
   }

   private int getSurfaceBlockY(Chunk chunk, int blockX, int blockZ) {
      int k;
      if (chunk.hasHeightmap(Heightmap.Type.WORLD_SURFACE_WG)) {
         k = Math.min(chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, blockX, blockZ) + 1, this.oldHeightLimit.getTopY());
      } else {
         k = this.oldHeightLimit.getTopY();
      }

      int l = this.oldHeightLimit.getBottomY();
      BlockPos.Mutable lv = new BlockPos.Mutable(blockX, k, blockZ);

      do {
         if (lv.getY() <= l) {
            return l;
         }

         lv.move(Direction.DOWN);
      } while(!SURFACE_BLOCKS.contains(chunk.getBlockState(lv).getBlock()));

      return lv.getY();
   }

   private static double getAboveCollidableBlockValue(Chunk chunk, BlockPos.Mutable mutablePos) {
      return isCollidableAndNotTreeAt(chunk, mutablePos.move(Direction.DOWN)) ? 1.0 : -1.0;
   }

   private static double getCollidableBlockDensityBelow(Chunk chunk, BlockPos.Mutable mutablePos) {
      double d = 0.0;

      for(int i = 0; i < 7; ++i) {
         d += getAboveCollidableBlockValue(chunk, mutablePos);
      }

      return d;
   }

   private double[] calculateCollidableBlockDensityColumn(Chunk chunk, int chunkBlockX, int chunkBlockZ, int surfaceHeight) {
      double[] ds = new double[this.getVerticalHalfSectionCount()];
      Arrays.fill(ds, -1.0);
      BlockPos.Mutable lv = new BlockPos.Mutable(chunkBlockX, this.oldHeightLimit.getTopY(), chunkBlockZ);
      double d = getCollidableBlockDensityBelow(chunk, lv);

      int l;
      double e;
      double f;
      for(l = ds.length - 2; l >= 0; --l) {
         e = getAboveCollidableBlockValue(chunk, lv);
         f = getCollidableBlockDensityBelow(chunk, lv);
         ds[l] = (d + e + f) / 15.0;
         d = f;
      }

      l = this.getHalfSectionHeight(MathHelper.floorDiv(surfaceHeight, 8));
      if (l >= 0 && l < ds.length - 1) {
         e = ((double)surfaceHeight + 0.5) % 8.0 / 8.0;
         f = (1.0 - e) / e;
         double g = Math.max(f, 1.0) * 0.25;
         ds[l + 1] = -f / g;
         ds[l] = 1.0 / g;
      }

      return ds;
   }

   private List getVerticalBiomeSections(Chunk chunk, int chunkBlockX, int chunkBlockZ) {
      ObjectArrayList objectArrayList = new ObjectArrayList(this.getVerticalBiomeCount());
      objectArrayList.size(this.getVerticalBiomeCount());

      for(int k = 0; k < objectArrayList.size(); ++k) {
         int l = k + BiomeCoords.fromBlock(this.oldHeightLimit.getBottomY());
         objectArrayList.set(k, chunk.getBiomeForNoiseGen(BiomeCoords.fromBlock(chunkBlockX), l, BiomeCoords.fromBlock(chunkBlockZ)));
      }

      return objectArrayList;
   }

   private static boolean isCollidableAndNotTreeAt(Chunk chunk, BlockPos pos) {
      BlockState lv = chunk.getBlockState(pos);
      if (lv.isAir()) {
         return false;
      } else if (lv.isIn(BlockTags.LEAVES)) {
         return false;
      } else if (lv.isIn(BlockTags.LOGS)) {
         return false;
      } else if (!lv.isOf(Blocks.BROWN_MUSHROOM_BLOCK) && !lv.isOf(Blocks.RED_MUSHROOM_BLOCK)) {
         return !lv.getCollisionShape(chunk, pos).isEmpty();
      } else {
         return false;
      }
   }

   protected double getHeight(int biomeX, int biomeY, int biomeZ) {
      if (biomeX != CHUNK_BIOME_END_INDEX && biomeZ != CHUNK_BIOME_END_INDEX) {
         return biomeX != 0 && biomeZ != 0 ? Double.MAX_VALUE : this.surfaceHeights[getNorthWestIndex(biomeX, biomeZ)];
      } else {
         return this.surfaceHeights[getSouthEastIndex(biomeX, biomeZ)];
      }
   }

   private double getCollidableBlockDensity(@Nullable double[] collidableBlockDensityColumn, int halfSectionY) {
      if (collidableBlockDensityColumn == null) {
         return Double.MAX_VALUE;
      } else {
         int j = this.getHalfSectionHeight(halfSectionY);
         return j >= 0 && j < collidableBlockDensityColumn.length ? collidableBlockDensityColumn[j] * 0.1 : Double.MAX_VALUE;
      }
   }

   protected double getCollidableBlockDensity(int chunkBiomeX, int halfSectionY, int chunkBiomeZ) {
      if (halfSectionY == this.getBottomHalfSectionY()) {
         return 0.1;
      } else if (chunkBiomeX != CHUNK_BIOME_END_INDEX && chunkBiomeZ != CHUNK_BIOME_END_INDEX) {
         return chunkBiomeX != 0 && chunkBiomeZ != 0 ? Double.MAX_VALUE : this.getCollidableBlockDensity(this.collidableBlockDensities[getNorthWestIndex(chunkBiomeX, chunkBiomeZ)], halfSectionY);
      } else {
         return this.getCollidableBlockDensity(this.collidableBlockDensities[getSouthEastIndex(chunkBiomeX, chunkBiomeZ)], halfSectionY);
      }
   }

   protected void acceptBiomes(int biomeX, int biomeY, int biomeZ, BiomeConsumer consumer) {
      if (biomeY >= BiomeCoords.fromBlock(this.oldHeightLimit.getBottomY()) && biomeY < BiomeCoords.fromBlock(this.oldHeightLimit.getTopY())) {
         int l = biomeY - BiomeCoords.fromBlock(this.oldHeightLimit.getBottomY());

         for(int m = 0; m < this.biomes.size(); ++m) {
            if (this.biomes.get(m) != null) {
               RegistryEntry lv = (RegistryEntry)((List)this.biomes.get(m)).get(l);
               if (lv != null) {
                  consumer.consume(biomeX + getX(m), biomeZ + getZ(m), lv);
               }
            }
         }

      }
   }

   protected void acceptHeights(int biomeX, int biomeZ, HeightConsumer consumer) {
      for(int k = 0; k < this.surfaceHeights.length; ++k) {
         double d = this.surfaceHeights[k];
         if (d != Double.MAX_VALUE) {
            consumer.consume(biomeX + getX(k), biomeZ + getZ(k), d);
         }
      }

   }

   protected void acceptCollidableBlockDensities(int biomeX, int biomeZ, int minHalfSectionY, int maxHalfSectionY, CollidableBlockDensityConsumer consumer) {
      int m = this.getOneAboveBottomHalfSectionY();
      int n = Math.max(0, minHalfSectionY - m);
      int o = Math.min(this.getVerticalHalfSectionCount(), maxHalfSectionY - m);

      for(int p = 0; p < this.collidableBlockDensities.length; ++p) {
         double[] ds = this.collidableBlockDensities[p];
         if (ds != null) {
            int q = biomeX + getX(p);
            int r = biomeZ + getZ(p);

            for(int s = n; s < o; ++s) {
               consumer.consume(q, s + m, r, ds[s] * 0.1);
            }
         }
      }

   }

   private int getVerticalHalfSectionCount() {
      return this.oldHeightLimit.countVerticalSections() * 2;
   }

   private int getVerticalBiomeCount() {
      return BiomeCoords.fromChunk(this.oldHeightLimit.countVerticalSections());
   }

   private int getOneAboveBottomHalfSectionY() {
      return this.getBottomHalfSectionY() + 1;
   }

   private int getBottomHalfSectionY() {
      return this.oldHeightLimit.getBottomSectionCoord() * 2;
   }

   private int getHalfSectionHeight(int halfSectionY) {
      return halfSectionY - this.getOneAboveBottomHalfSectionY();
   }

   private static int getNorthWestIndex(int chunkBiomeX, int chunkBiomeZ) {
      return LAST_CHUNK_BIOME_INDEX - chunkBiomeX + chunkBiomeZ;
   }

   private static int getSouthEastIndex(int chunkBiomeX, int chunkBiomeZ) {
      return NORTH_WEST_END_INDEX + chunkBiomeX + CHUNK_BIOME_END_INDEX - chunkBiomeZ;
   }

   private static int getX(int index) {
      if (index < NORTH_WEST_END_INDEX) {
         return method_39355(LAST_CHUNK_BIOME_INDEX - index);
      } else {
         int j = index - NORTH_WEST_END_INDEX;
         return CHUNK_BIOME_END_INDEX - method_39355(CHUNK_BIOME_END_INDEX - j);
      }
   }

   private static int getZ(int index) {
      if (index < NORTH_WEST_END_INDEX) {
         return method_39355(index - LAST_CHUNK_BIOME_INDEX);
      } else {
         int j = index - NORTH_WEST_END_INDEX;
         return CHUNK_BIOME_END_INDEX - method_39355(j - CHUNK_BIOME_END_INDEX);
      }
   }

   private static int method_39355(int i) {
      return i & ~(i >> 31);
   }

   public HeightLimitView getOldHeightLimit() {
      return this.oldHeightLimit;
   }

   static {
      LAST_CHUNK_BIOME_INDEX = BIOMES_PER_CHUNK - 1;
      CHUNK_BIOME_END_INDEX = BIOMES_PER_CHUNK;
      NORTH_WEST_END_INDEX = 2 * LAST_CHUNK_BIOME_INDEX + 1;
      SOUTH_EAST_END_INDEX_PART = 2 * CHUNK_BIOME_END_INDEX + 1;
      HORIZONTAL_BIOME_COUNT = NORTH_WEST_END_INDEX + SOUTH_EAST_END_INDEX_PART;
      SURFACE_BLOCKS = List.of(Blocks.PODZOL, Blocks.GRAVEL, Blocks.GRASS_BLOCK, Blocks.STONE, Blocks.COARSE_DIRT, Blocks.SAND, Blocks.RED_SAND, Blocks.MYCELIUM, Blocks.SNOW_BLOCK, Blocks.TERRACOTTA, Blocks.DIRT);
      DOUBLE_ARRAY_CODEC = Codec.DOUBLE.listOf().xmap(Doubles::toArray, Doubles::asList);
      CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Codec.INT.fieldOf("min_section").forGetter((blendingData) -> {
            return blendingData.oldHeightLimit.getBottomSectionCoord();
         }), Codec.INT.fieldOf("max_section").forGetter((blendingData) -> {
            return blendingData.oldHeightLimit.getTopSectionCoord();
         }), DOUBLE_ARRAY_CODEC.optionalFieldOf("heights").forGetter((blendingData) -> {
            return DoubleStream.of(blendingData.surfaceHeights).anyMatch((height) -> {
               return height != Double.MAX_VALUE;
            }) ? Optional.of(blendingData.surfaceHeights) : Optional.empty();
         })).apply(instance, BlendingData::new);
      }).comapFlatMap(BlendingData::validate, Function.identity());
   }

   protected interface BiomeConsumer {
      void consume(int biomeX, int biomeZ, RegistryEntry biome);
   }

   protected interface HeightConsumer {
      void consume(int biomeX, int biomeZ, double height);
   }

   protected interface CollidableBlockDensityConsumer {
      void consume(int biomeX, int halfSectionY, int biomeZ, double collidableBlockDensity);
   }
}
