package net.minecraft.block;

import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;

public class GlazedTerracottaBlock extends HorizontalFacingBlock {
   public GlazedTerracottaBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING);
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
   }
}
