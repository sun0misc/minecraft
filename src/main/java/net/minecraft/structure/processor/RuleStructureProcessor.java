package net.minecraft.structure.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class RuleStructureProcessor extends StructureProcessor {
   public static final Codec CODEC;
   private final ImmutableList rules;

   public RuleStructureProcessor(List rules) {
      this.rules = ImmutableList.copyOf(rules);
   }

   @Nullable
   public StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.StructureBlockInfo currentBlockInfo, StructurePlacementData data) {
      Random lv = Random.create(MathHelper.hashCode(currentBlockInfo.pos()));
      BlockState lv2 = world.getBlockState(currentBlockInfo.pos());
      UnmodifiableIterator var9 = this.rules.iterator();

      StructureProcessorRule lv3;
      do {
         if (!var9.hasNext()) {
            return currentBlockInfo;
         }

         lv3 = (StructureProcessorRule)var9.next();
      } while(!lv3.test(currentBlockInfo.state(), lv2, originalBlockInfo.pos(), currentBlockInfo.pos(), pivot, lv));

      return new StructureTemplate.StructureBlockInfo(currentBlockInfo.pos(), lv3.getOutputState(), lv3.getOutputNbt(lv, currentBlockInfo.nbt()));
   }

   protected StructureProcessorType getType() {
      return StructureProcessorType.RULE;
   }

   static {
      CODEC = StructureProcessorRule.CODEC.listOf().fieldOf("rules").xmap(RuleStructureProcessor::new, (processor) -> {
         return processor.rules;
      }).codec();
   }
}
