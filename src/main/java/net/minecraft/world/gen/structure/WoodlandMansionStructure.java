/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import java.util.LinkedList;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.structure.StructurePiecesList;
import net.minecraft.structure.WoodlandMansionGenerator;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class WoodlandMansionStructure
extends Structure {
    public static final MapCodec<WoodlandMansionStructure> CODEC = WoodlandMansionStructure.createCodec(WoodlandMansionStructure::new);

    public WoodlandMansionStructure(Structure.Config arg) {
        super(arg);
    }

    @Override
    public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context) {
        BlockRotation lv = BlockRotation.random(context.random());
        BlockPos lv2 = this.getShiftedPos(context, lv);
        if (lv2.getY() < 60) {
            return Optional.empty();
        }
        return Optional.of(new Structure.StructurePosition(lv2, collector -> this.addPieces((StructurePiecesCollector)collector, context, lv2, lv)));
    }

    private void addPieces(StructurePiecesCollector collector, Structure.Context context, BlockPos pos, BlockRotation rotation) {
        LinkedList<WoodlandMansionGenerator.Piece> list = Lists.newLinkedList();
        WoodlandMansionGenerator.addPieces(context.structureTemplateManager(), pos, rotation, list, context.random());
        list.forEach(collector::addPiece);
    }

    @Override
    public void postPlace(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox box, ChunkPos chunkPos, StructurePiecesList pieces) {
        BlockPos.Mutable lv = new BlockPos.Mutable();
        int i = world.getBottomY();
        BlockBox lv2 = pieces.getBoundingBox();
        int j = lv2.getMinY();
        for (int k = box.getMinX(); k <= box.getMaxX(); ++k) {
            block1: for (int l = box.getMinZ(); l <= box.getMaxZ(); ++l) {
                lv.set(k, j, l);
                if (world.isAir(lv) || !lv2.contains(lv) || !pieces.contains(lv)) continue;
                for (int m = j - 1; m > i; --m) {
                    lv.setY(m);
                    if (!world.isAir(lv) && !world.getBlockState(lv).isLiquid()) continue block1;
                    world.setBlockState(lv, Blocks.COBBLESTONE.getDefaultState(), Block.NOTIFY_LISTENERS);
                }
            }
        }
    }

    @Override
    public StructureType<?> getType() {
        return StructureType.WOODLAND_MANSION;
    }
}

