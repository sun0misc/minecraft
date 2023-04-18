package net.minecraft.world.block;

import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public interface NeighborUpdater {
   Direction[] UPDATE_ORDER = new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};

   void replaceWithStateForNeighborUpdate(Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, int flags, int maxUpdateDepth);

   void updateNeighbor(BlockPos pos, Block sourceBlock, BlockPos sourcePos);

   void updateNeighbor(BlockState state, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify);

   default void updateNeighbors(BlockPos pos, Block sourceBlock, @Nullable Direction except) {
      Direction[] var4 = UPDATE_ORDER;
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         Direction lv = var4[var6];
         if (lv != except) {
            this.updateNeighbor(pos.offset(lv), sourceBlock, pos);
         }
      }

   }

   static void replaceWithStateForNeighborUpdate(WorldAccess world, Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, int flags, int maxUpdateDepth) {
      BlockState lv = world.getBlockState(pos);
      BlockState lv2 = lv.getStateForNeighborUpdate(direction, neighborState, world, pos, neighborPos);
      Block.replace(lv, lv2, world, pos, flags, maxUpdateDepth);
   }

   static void tryNeighborUpdate(World world, BlockState state, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
      try {
         state.neighborUpdate(world, pos, sourceBlock, sourcePos, notify);
      } catch (Throwable var9) {
         CrashReport lv = CrashReport.create(var9, "Exception while updating neighbours");
         CrashReportSection lv2 = lv.addElement("Block being updated");
         lv2.add("Source block type", () -> {
            try {
               return String.format(Locale.ROOT, "ID #%s (%s // %s)", Registries.BLOCK.getId(sourceBlock), sourceBlock.getTranslationKey(), sourceBlock.getClass().getCanonicalName());
            } catch (Throwable var2) {
               return "ID #" + Registries.BLOCK.getId(sourceBlock);
            }
         });
         CrashReportSection.addBlockInfo(lv2, world, pos, state);
         throw new CrashException(lv);
      }
   }
}
