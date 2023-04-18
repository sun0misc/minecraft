package net.minecraft.world.gen.structure;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.structure.IglooGenerator;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.Heightmap;

public class IglooStructure extends Structure {
   public static final Codec CODEC = createCodec(IglooStructure::new);

   public IglooStructure(Structure.Config arg) {
      super(arg);
   }

   public Optional getStructurePosition(Structure.Context context) {
      return getStructurePosition(context, Heightmap.Type.WORLD_SURFACE_WG, (collector) -> {
         this.addPieces(collector, context);
      });
   }

   private void addPieces(StructurePiecesCollector collector, Structure.Context context) {
      ChunkPos lv = context.chunkPos();
      ChunkRandom lv2 = context.random();
      BlockPos lv3 = new BlockPos(lv.getStartX(), 90, lv.getStartZ());
      BlockRotation lv4 = BlockRotation.random(lv2);
      IglooGenerator.addPieces(context.structureTemplateManager(), lv3, lv4, collector, lv2);
   }

   public StructureType getType() {
      return StructureType.IGLOO;
   }
}
