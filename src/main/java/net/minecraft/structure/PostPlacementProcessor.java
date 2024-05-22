/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure;

import net.minecraft.structure.StructurePiecesList;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

@FunctionalInterface
public interface PostPlacementProcessor {
    public static final PostPlacementProcessor EMPTY = (world, structureAccessor, chunkGenerator, random, chunkBox, pos, children) -> {};

    public void afterPlace(StructureWorldAccess var1, StructureAccessor var2, ChunkGenerator var3, Random var4, BlockBox var5, ChunkPos var6, StructurePiecesList var7);
}

