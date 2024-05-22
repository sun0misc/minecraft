/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructurePiecesGenerator;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.noise.NoiseConfig;

@FunctionalInterface
public interface StructureGeneratorFactory<C extends FeatureConfig> {
    public Optional<StructurePiecesGenerator<C>> createGenerator(Context<C> var1);

    public static <C extends FeatureConfig> StructureGeneratorFactory<C> simple(Predicate<Context<C>> predicate, StructurePiecesGenerator<C> generator) {
        Optional optional = Optional.of(generator);
        return context -> predicate.test(context) ? optional : Optional.empty();
    }

    public static <C extends FeatureConfig> Predicate<Context<C>> checkForBiomeOnTop(Heightmap.Type heightmapType) {
        return context -> context.isBiomeValid(heightmapType);
    }

    public record Context<C extends FeatureConfig>(ChunkGenerator chunkGenerator, BiomeSource biomeSource, NoiseConfig noiseConfig, long seed, ChunkPos chunkPos, C config, HeightLimitView world, Predicate<RegistryEntry<Biome>> validBiome, StructureTemplateManager structureTemplateManager, DynamicRegistryManager registryManager) {
        public boolean isBiomeValid(Heightmap.Type heightmapType) {
            int i = this.chunkPos.getCenterX();
            int j = this.chunkPos.getCenterZ();
            int k = this.chunkGenerator.getHeightInGround(i, j, heightmapType, this.world, this.noiseConfig);
            RegistryEntry<Biome> lv = this.chunkGenerator.getBiomeSource().getBiome(BiomeCoords.fromBlock(i), BiomeCoords.fromBlock(k), BiomeCoords.fromBlock(j), this.noiseConfig.getMultiNoiseSampler());
            return this.validBiome.test(lv);
        }
    }
}

