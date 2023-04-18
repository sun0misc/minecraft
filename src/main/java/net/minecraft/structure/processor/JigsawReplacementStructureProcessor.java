package net.minecraft.structure.processor;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class JigsawReplacementStructureProcessor extends StructureProcessor {
   private static final Logger field_43332 = LogUtils.getLogger();
   public static final Codec CODEC = Codec.unit(() -> {
      return INSTANCE;
   });
   public static final JigsawReplacementStructureProcessor INSTANCE = new JigsawReplacementStructureProcessor();

   private JigsawReplacementStructureProcessor() {
   }

   @Nullable
   public StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.StructureBlockInfo currentBlockInfo, StructurePlacementData data) {
      BlockState lv = currentBlockInfo.state();
      if (lv.isOf(Blocks.JIGSAW)) {
         if (currentBlockInfo.nbt() == null) {
            field_43332.warn("Jigsaw block at {} is missing nbt, will not replace", pos);
            return currentBlockInfo;
         } else {
            String string = currentBlockInfo.nbt().getString("final_state");

            BlockState lv3;
            try {
               BlockArgumentParser.BlockResult lv2 = BlockArgumentParser.block(world.createCommandRegistryWrapper(RegistryKeys.BLOCK), string, true);
               lv3 = lv2.blockState();
            } catch (CommandSyntaxException var11) {
               throw new RuntimeException(var11);
            }

            return lv3.isOf(Blocks.STRUCTURE_VOID) ? null : new StructureTemplate.StructureBlockInfo(currentBlockInfo.pos(), lv3, (NbtCompound)null);
         }
      } else {
         return currentBlockInfo;
      }
   }

   protected StructureProcessorType getType() {
      return StructureProcessorType.JIGSAW_REPLACEMENT;
   }
}
