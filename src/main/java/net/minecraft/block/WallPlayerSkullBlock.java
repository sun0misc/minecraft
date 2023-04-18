package net.minecraft.block;

import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WallPlayerSkullBlock extends WallSkullBlock {
   protected WallPlayerSkullBlock(AbstractBlock.Settings arg) {
      super(SkullBlock.Type.PLAYER, arg);
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
      Blocks.PLAYER_HEAD.onPlaced(world, pos, state, placer, itemStack);
   }

   public List getDroppedStacks(BlockState state, LootContext.Builder builder) {
      return Blocks.PLAYER_HEAD.getDroppedStacks(state, builder);
   }
}
