package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.structure.EndCityGenerator;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;

public class EndCityStructure extends Structure {
   public static final Codec CODEC = createCodec(EndCityStructure::new);

   public EndCityStructure(Structure.Config arg) {
      super(arg);
   }

   public Optional getStructurePosition(Structure.Context context) {
      BlockRotation lv = BlockRotation.random(context.random());
      BlockPos lv2 = this.getShiftedPos(context, lv);
      return lv2.getY() < 60 ? Optional.empty() : Optional.of(new Structure.StructurePosition(lv2, (collector) -> {
         this.addPieces(collector, lv2, lv, context);
      }));
   }

   private void addPieces(StructurePiecesCollector collector, BlockPos pos, BlockRotation rotation, Structure.Context context) {
      List list = Lists.newArrayList();
      EndCityGenerator.addPieces(context.structureTemplateManager(), pos, rotation, list, context.random());
      Objects.requireNonNull(collector);
      list.forEach(collector::addPiece);
   }

   public StructureType getType() {
      return StructureType.END_CITY;
   }
}
