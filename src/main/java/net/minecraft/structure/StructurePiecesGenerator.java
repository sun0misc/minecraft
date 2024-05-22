/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure;

import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.FeatureConfig;

@FunctionalInterface
public interface StructurePiecesGenerator<C extends FeatureConfig> {
    public void generatePieces(StructurePiecesCollector var1, Context<C> var2);

    public record Context<C extends FeatureConfig>(C config, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, ChunkPos chunkPos, HeightLimitView world, ChunkRandom random, long seed) {
    }
}

