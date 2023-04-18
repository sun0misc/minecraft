package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class DropperBlockEntity extends DispenserBlockEntity {
   public DropperBlockEntity(BlockPos arg, BlockState arg2) {
      super(BlockEntityType.DROPPER, arg, arg2);
   }

   protected Text getContainerName() {
      return Text.translatable("container.dropper");
   }
}
