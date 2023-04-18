package net.minecraft.item;

import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SignItem extends VerticallyAttachableBlockItem {
   public SignItem(Item.Settings settings, Block standingBlock, Block wallBlock) {
      super(standingBlock, wallBlock, settings, Direction.DOWN);
   }

   public SignItem(Item.Settings settings, Block standingBlock, Block wallBlock, Direction verticalAttachmentDirection) {
      super(standingBlock, wallBlock, settings, verticalAttachmentDirection);
   }

   protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
      boolean bl = super.postPlacement(pos, world, player, stack, state);
      if (!world.isClient && !bl && player != null) {
         BlockEntity var9 = world.getBlockEntity(pos);
         if (var9 instanceof SignBlockEntity) {
            SignBlockEntity lv = (SignBlockEntity)var9;
            Block var10 = world.getBlockState(pos).getBlock();
            if (var10 instanceof AbstractSignBlock) {
               AbstractSignBlock lv2 = (AbstractSignBlock)var10;
               lv2.openEditScreen(player, lv, true);
            }
         }
      }

      return bl;
   }
}
