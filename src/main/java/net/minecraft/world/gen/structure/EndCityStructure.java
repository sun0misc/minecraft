/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.structure.EndCityGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class EndCityStructure
extends Structure {
    public static final MapCodec<EndCityStructure> CODEC = EndCityStructure.createCodec(EndCityStructure::new);

    public EndCityStructure(Structure.Config arg) {
        super(arg);
    }

    @Override
    public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context) {
        BlockRotation lv = BlockRotation.random(context.random());
        BlockPos lv2 = this.getShiftedPos(context, lv);
        if (lv2.getY() < 60) {
            return Optional.empty();
        }
        return Optional.of(new Structure.StructurePosition(lv2, collector -> this.addPieces((StructurePiecesCollector)collector, lv2, lv, context)));
    }

    private void addPieces(StructurePiecesCollector collector, BlockPos pos, BlockRotation rotation, Structure.Context context) {
        ArrayList<StructurePiece> list = Lists.newArrayList();
        EndCityGenerator.addPieces(context.structureTemplateManager(), pos, rotation, list, context.random());
        list.forEach(collector::addPiece);
    }

    @Override
    public StructureType<?> getType() {
        return StructureType.END_CITY;
    }
}

