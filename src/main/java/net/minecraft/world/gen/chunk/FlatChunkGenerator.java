/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.chunk;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.noise.NoiseConfig;

public class FlatChunkGenerator
extends ChunkGenerator {
    public static final MapCodec<FlatChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)FlatChunkGeneratorConfig.CODEC.fieldOf("settings")).forGetter(FlatChunkGenerator::getConfig)).apply((Applicative<FlatChunkGenerator, ?>)instance, instance.stable(FlatChunkGenerator::new)));
    private final FlatChunkGeneratorConfig config;

    public FlatChunkGenerator(FlatChunkGeneratorConfig config) {
        super(new FixedBiomeSource(config.getBiome()), Util.memoize(config::createGenerationSettings));
        this.config = config;
    }

    @Override
    public StructurePlacementCalculator createStructurePlacementCalculator(RegistryWrapper<StructureSet> structureSetRegistry, NoiseConfig noiseConfig, long seed) {
        Stream stream = this.config.getStructureOverrides().map(RegistryEntryList::stream).orElseGet(() -> structureSetRegistry.streamEntries().map(arg -> arg));
        return StructurePlacementCalculator.create(noiseConfig, seed, this.biomeSource, stream);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    public FlatChunkGeneratorConfig getConfig() {
        return this.config;
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
    }

    @Override
    public int getSpawnHeight(HeightLimitView world) {
        return world.getBottomY() + Math.min(world.getHeight(), this.config.getLayerBlocks().size());
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        List<BlockState> list = this.config.getLayerBlocks();
        BlockPos.Mutable lv = new BlockPos.Mutable();
        Heightmap lv2 = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap lv3 = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        for (int i = 0; i < Math.min(chunk.getHeight(), list.size()); ++i) {
            BlockState lv4 = list.get(i);
            if (lv4 == null) continue;
            int j = chunk.getBottomY() + i;
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    chunk.setBlockState(lv.set(k, j, l), lv4, false);
                    lv2.trackUpdate(k, j, l, lv4);
                    lv3.trackUpdate(k, j, l, lv4);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        List<BlockState> list = this.config.getLayerBlocks();
        for (int k = Math.min(list.size(), world.getTopY()) - 1; k >= 0; --k) {
            BlockState lv = list.get(k);
            if (lv == null || !heightmap.getBlockPredicate().test(lv)) continue;
            return world.getBottomY() + k + 1;
        }
        return world.getBottomY();
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        return new VerticalBlockSample(world.getBottomY(), (BlockState[])this.config.getLayerBlocks().stream().limit(world.getHeight()).map(state -> state == null ? Blocks.AIR.getDefaultState() : state).toArray(BlockState[]::new));
    }

    @Override
    public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {
    }

    @Override
    public void populateEntities(ChunkRegion region) {
    }

    @Override
    public int getMinimumY() {
        return 0;
    }

    @Override
    public int getWorldHeight() {
        return 384;
    }

    @Override
    public int getSeaLevel() {
        return -63;
    }
}

