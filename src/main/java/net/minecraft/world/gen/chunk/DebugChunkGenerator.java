package net.minecraft.world.gen.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.noise.NoiseConfig;

public class DebugChunkGenerator extends ChunkGenerator {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(RegistryOps.getEntryCodec(BiomeKeys.PLAINS)).apply(instance, instance.stable(DebugChunkGenerator::new));
   });
   private static final int field_31467 = 2;
   private static final List BLOCK_STATES;
   private static final int X_SIDE_LENGTH;
   private static final int Z_SIDE_LENGTH;
   protected static final BlockState AIR;
   protected static final BlockState BARRIER;
   public static final int field_31465 = 70;
   public static final int field_31466 = 60;

   public DebugChunkGenerator(RegistryEntry.Reference biomeEntry) {
      super(new FixedBiomeSource(biomeEntry));
   }

   protected Codec getCodec() {
      return CODEC;
   }

   public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
   }

   public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
      BlockPos.Mutable lv = new BlockPos.Mutable();
      ChunkPos lv2 = chunk.getPos();
      int i = lv2.x;
      int j = lv2.z;

      for(int k = 0; k < 16; ++k) {
         for(int l = 0; l < 16; ++l) {
            int m = ChunkSectionPos.getOffsetPos(i, k);
            int n = ChunkSectionPos.getOffsetPos(j, l);
            world.setBlockState(lv.set(m, 60, n), BARRIER, Block.NOTIFY_LISTENERS);
            BlockState lv3 = getBlockState(m, n);
            world.setBlockState(lv.set(m, 70, n), lv3, Block.NOTIFY_LISTENERS);
         }
      }

   }

   public CompletableFuture populateNoise(Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
      return CompletableFuture.completedFuture(chunk);
   }

   public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
      return 0;
   }

   public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
      return new VerticalBlockSample(0, new BlockState[0]);
   }

   public void getDebugHudText(List text, NoiseConfig noiseConfig, BlockPos pos) {
   }

   public static BlockState getBlockState(int x, int z) {
      BlockState lv = AIR;
      if (x > 0 && z > 0 && x % 2 != 0 && z % 2 != 0) {
         x /= 2;
         z /= 2;
         if (x <= X_SIDE_LENGTH && z <= Z_SIDE_LENGTH) {
            int k = MathHelper.abs(x * X_SIDE_LENGTH + z);
            if (k < BLOCK_STATES.size()) {
               lv = (BlockState)BLOCK_STATES.get(k);
            }
         }
      }

      return lv;
   }

   public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {
   }

   public void populateEntities(ChunkRegion region) {
   }

   public int getMinimumY() {
      return 0;
   }

   public int getWorldHeight() {
      return 384;
   }

   public int getSeaLevel() {
      return 63;
   }

   static {
      BLOCK_STATES = (List)StreamSupport.stream(Registries.BLOCK.spliterator(), false).flatMap((block) -> {
         return block.getStateManager().getStates().stream();
      }).collect(Collectors.toList());
      X_SIDE_LENGTH = MathHelper.ceil(MathHelper.sqrt((float)BLOCK_STATES.size()));
      Z_SIDE_LENGTH = MathHelper.ceil((float)BLOCK_STATES.size() / (float)X_SIDE_LENGTH);
      AIR = Blocks.AIR.getDefaultState();
      BARRIER = Blocks.BARRIER.getDefaultState();
   }
}
