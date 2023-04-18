package net.minecraft.block;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WallWitherSkullBlock extends WallSkullBlock {
   protected WallWitherSkullBlock(AbstractBlock.Settings arg) {
      super(SkullBlock.Type.WITHER_SKELETON, arg);
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
      Blocks.WITHER_SKELETON_SKULL.onPlaced(world, pos, state, placer, itemStack);
   }
}
