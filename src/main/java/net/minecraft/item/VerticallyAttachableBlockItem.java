package net.minecraft.item;

import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class VerticallyAttachableBlockItem extends BlockItem {
   protected final Block wallBlock;
   private final Direction verticalAttachmentDirection;

   public VerticallyAttachableBlockItem(Block standingBlock, Block wallBlock, Item.Settings settings, Direction verticalAttachmentDirection) {
      super(standingBlock, settings);
      this.wallBlock = wallBlock;
      this.verticalAttachmentDirection = verticalAttachmentDirection;
   }

   protected boolean canPlaceAt(WorldView world, BlockState state, BlockPos pos) {
      return state.canPlaceAt(world, pos);
   }

   @Nullable
   protected BlockState getPlacementState(ItemPlacementContext context) {
      BlockState lv = this.wallBlock.getPlacementState(context);
      BlockState lv2 = null;
      WorldView lv3 = context.getWorld();
      BlockPos lv4 = context.getBlockPos();
      Direction[] var6 = context.getPlacementDirections();
      int var7 = var6.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         Direction lv5 = var6[var8];
         if (lv5 != this.verticalAttachmentDirection.getOpposite()) {
            BlockState lv6 = lv5 == this.verticalAttachmentDirection ? this.getBlock().getPlacementState(context) : lv;
            if (lv6 != null && this.canPlaceAt(lv3, lv6, lv4)) {
               lv2 = lv6;
               break;
            }
         }
      }

      return lv2 != null && lv3.canPlace(lv2, lv4, ShapeContext.absent()) ? lv2 : null;
   }

   public void appendBlocks(Map map, Item item) {
      super.appendBlocks(map, item);
      map.put(this.wallBlock, item);
   }
}
