/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.structure;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.structure.SwampHutGenerator;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class SwampHutStructure
extends Structure {
    public static final MapCodec<SwampHutStructure> CODEC = SwampHutStructure.createCodec(SwampHutStructure::new);

    public SwampHutStructure(Structure.Config arg) {
        super(arg);
    }

    @Override
    public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context) {
        return SwampHutStructure.getStructurePosition(context, Heightmap.Type.WORLD_SURFACE_WG, collector -> SwampHutStructure.addPieces(collector, context));
    }

    private static void addPieces(StructurePiecesCollector collector, Structure.Context context) {
        collector.addPiece(new SwampHutGenerator(context.random(), context.chunkPos().getStartX(), context.chunkPos().getStartZ()));
    }

    @Override
    public StructureType<?> getType() {
        return StructureType.SWAMP_HUT;
    }
}

