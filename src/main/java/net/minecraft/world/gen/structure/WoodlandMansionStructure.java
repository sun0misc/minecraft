package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Objects;
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

public class WoodlandMansionStructure extends Structure {
   public static final Codec CODEC = createCodec(WoodlandMansionStructure::new);

   public WoodlandMansionStructure(Structure.Config arg) {
      super(arg);
   }

   public Optional getStructurePosition(Structure.Context context) {
      BlockRotation lv = BlockRotation.random(context.random());
      BlockPos lv2 = this.getShiftedPos(context, lv);
      return lv2.getY() < 60 ? Optional.empty() : Optional.of(new Structure.StructurePosition(lv2, (collector) -> {
         this.addPieces(collector, context, lv2, lv);
      }));
   }

   private void addPieces(StructurePiecesCollector collector, Structure.Context context, BlockPos pos, BlockRotation rotation) {
      List list = Lists.newLinkedList();
      WoodlandMansionGenerator.addPieces(context.structureTemplateManager(), pos, rotation, list, context.random());
      Objects.requireNonNull(collector);
      list.forEach(collector::addPiece);
   }

   public void postPlace(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox box, ChunkPos chunkPos, StructurePiecesList pieces) {
      BlockPos.Mutable lv = new BlockPos.Mutable();
      int i = world.getBottomY();
      BlockBox lv2 = pieces.getBoundingBox();
      int j = lv2.getMinY();

      for(int k = box.getMinX(); k <= box.getMaxX(); ++k) {
         for(int l = box.getMinZ(); l <= box.getMaxZ(); ++l) {
            lv.set(k, j, l);
            if (!world.isAir(lv) && lv2.contains(lv) && pieces.contains(lv)) {
               for(int m = j - 1; m > i; --m) {
                  lv.setY(m);
                  if (!world.isAir(lv) && !world.getBlockState(lv).isLiquid()) {
                     break;
                  }

                  world.setBlockState(lv, Blocks.COBBLESTONE.getDefaultState(), Block.NOTIFY_LISTENERS);
               }
            }
         }
      }

   }

   public StructureType getType() {
      return StructureType.WOODLAND_MANSION;
   }
}
