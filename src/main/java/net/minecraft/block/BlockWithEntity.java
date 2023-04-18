package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class BlockWithEntity extends Block implements BlockEntityProvider {
   protected BlockWithEntity(AbstractBlock.Settings arg) {
      super(arg);
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.INVISIBLE;
   }

   public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
      super.onSyncedBlockEvent(state, world, pos, type, data);
      BlockEntity lv = world.getBlockEntity(pos);
      return lv == null ? false : lv.onSyncedBlockEvent(type, data);
   }

   @Nullable
   public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
      BlockEntity lv = world.getBlockEntity(pos);
      return lv instanceof NamedScreenHandlerFactory ? (NamedScreenHandlerFactory)lv : null;
   }

   @Nullable
   protected static BlockEntityTicker checkType(BlockEntityType givenType, BlockEntityType expectedType, BlockEntityTicker ticker) {
      return expectedType == givenType ? ticker : null;
   }
}
