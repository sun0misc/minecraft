package net.minecraft.block;

import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RedstoneLampBlock extends Block {
   public static final BooleanProperty LIT;

   public RedstoneLampBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)this.getDefaultState().with(LIT, false));
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(LIT, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
      if (!world.isClient) {
         boolean bl2 = (Boolean)state.get(LIT);
         if (bl2 != world.isReceivingRedstonePower(pos)) {
            if (bl2) {
               world.scheduleBlockTick(pos, this, 4);
            } else {
               world.setBlockState(pos, (BlockState)state.cycle(LIT), Block.NOTIFY_LISTENERS);
            }
         }

      }
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if ((Boolean)state.get(LIT) && !world.isReceivingRedstonePower(pos)) {
         world.setBlockState(pos, (BlockState)state.cycle(LIT), Block.NOTIFY_LISTENERS);
      }

   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(LIT);
   }

   static {
      LIT = RedstoneTorchBlock.LIT;
   }
}
