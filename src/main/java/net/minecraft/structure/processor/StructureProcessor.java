package net.minecraft.structure.processor;

import java.util.List;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public abstract class StructureProcessor {
   @Nullable
   public StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.StructureBlockInfo currentBlockInfo, StructurePlacementData data) {
      return currentBlockInfo;
   }

   protected abstract StructureProcessorType getType();

   public List reprocess(ServerWorldAccess world, BlockPos pos, BlockPos pivot, List originalBlockInfos, List currentBlockInfos, StructurePlacementData data) {
      return currentBlockInfos;
   }
}
