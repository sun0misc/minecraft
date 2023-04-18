package net.minecraft.world.gen.structure;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesCollector;

public class StrongholdStructure extends Structure {
   public static final Codec CODEC = createCodec(StrongholdStructure::new);

   public StrongholdStructure(Structure.Config arg) {
      super(arg);
   }

   public Optional getStructurePosition(Structure.Context context) {
      return Optional.of(new Structure.StructurePosition(context.chunkPos().getStartPos(), (collector) -> {
         addPieces(collector, context);
      }));
   }

   private static void addPieces(StructurePiecesCollector collector, Structure.Context context) {
      int i = 0;

      StrongholdGenerator.Start lv;
      do {
         collector.clear();
         context.random().setCarverSeed(context.seed() + (long)(i++), context.chunkPos().x, context.chunkPos().z);
         StrongholdGenerator.init();
         lv = new StrongholdGenerator.Start(context.random(), context.chunkPos().getOffsetX(2), context.chunkPos().getOffsetZ(2));
         collector.addPiece(lv);
         lv.fillOpenings(lv, collector, context.random());
         List list = lv.pieces;

         while(!list.isEmpty()) {
            int j = context.random().nextInt(list.size());
            StructurePiece lv2 = (StructurePiece)list.remove(j);
            lv2.fillOpenings(lv, collector, context.random());
         }

         collector.shiftInto(context.chunkGenerator().getSeaLevel(), context.chunkGenerator().getMinimumY(), context.random(), 10);
      } while(collector.isEmpty() || lv.portalRoom == null);

   }

   public StructureType getType() {
      return StructureType.STRONGHOLD;
   }
}
