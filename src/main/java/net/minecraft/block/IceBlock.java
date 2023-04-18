package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class IceBlock extends TransparentBlock {
   public IceBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public static BlockState getMeltedState() {
      return Blocks.WATER.getDefaultState();
   }

   public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
      super.afterBreak(world, player, pos, state, blockEntity, tool);
      if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, tool) == 0) {
         if (world.getDimension().ultrawarm()) {
            world.removeBlock(pos, false);
            return;
         }

         BlockState lv = world.getBlockState(pos.down());
         Material lv2 = lv.getMaterial();
         if (lv2.blocksMovement() || lv.isLiquid()) {
            world.setBlockState(pos, getMeltedState());
         }
      }

   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (world.getLightLevel(LightType.BLOCK, pos) > 11 - state.getOpacity(world, pos)) {
         this.melt(state, world, pos);
      }

   }

   protected void melt(BlockState state, World world, BlockPos pos) {
      if (world.getDimension().ultrawarm()) {
         world.removeBlock(pos, false);
      } else {
         world.setBlockState(pos, getMeltedState());
         world.updateNeighbor(pos, getMeltedState().getBlock(), pos);
      }
   }
}
