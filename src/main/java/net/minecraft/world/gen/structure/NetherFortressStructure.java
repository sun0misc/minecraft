package net.minecraft.world.gen.structure;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.structure.NetherFortressGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.SpawnSettings;

public class NetherFortressStructure extends Structure {
   public static final Pool MONSTER_SPAWNS;
   public static final Codec CODEC;

   public NetherFortressStructure(Structure.Config arg) {
      super(arg);
   }

   public Optional getStructurePosition(Structure.Context context) {
      ChunkPos lv = context.chunkPos();
      BlockPos lv2 = new BlockPos(lv.getStartX(), 64, lv.getStartZ());
      return Optional.of(new Structure.StructurePosition(lv2, (collector) -> {
         addPieces(collector, context);
      }));
   }

   private static void addPieces(StructurePiecesCollector collector, Structure.Context context) {
      NetherFortressGenerator.Start lv = new NetherFortressGenerator.Start(context.random(), context.chunkPos().getOffsetX(2), context.chunkPos().getOffsetZ(2));
      collector.addPiece(lv);
      lv.fillOpenings(lv, collector, context.random());
      List list = lv.pieces;

      while(!list.isEmpty()) {
         int i = context.random().nextInt(list.size());
         StructurePiece lv2 = (StructurePiece)list.remove(i);
         lv2.fillOpenings(lv, collector, context.random());
      }

      collector.shiftInto(context.random(), 48, 70);
   }

   public StructureType getType() {
      return StructureType.FORTRESS;
   }

   static {
      MONSTER_SPAWNS = Pool.of((Weighted[])(new SpawnSettings.SpawnEntry(EntityType.BLAZE, 10, 2, 3), new SpawnSettings.SpawnEntry(EntityType.ZOMBIFIED_PIGLIN, 5, 4, 4), new SpawnSettings.SpawnEntry(EntityType.WITHER_SKELETON, 8, 5, 5), new SpawnSettings.SpawnEntry(EntityType.SKELETON, 2, 5, 5), new SpawnSettings.SpawnEntry(EntityType.MAGMA_CUBE, 3, 4, 4)));
      CODEC = createCodec(NetherFortressStructure::new);
   }
}
