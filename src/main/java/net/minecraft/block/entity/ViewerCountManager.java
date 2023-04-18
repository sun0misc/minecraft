package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public abstract class ViewerCountManager {
   private static final int SCHEDULE_TICK_DELAY = 5;
   private int viewerCount;

   protected abstract void onContainerOpen(World world, BlockPos pos, BlockState state);

   protected abstract void onContainerClose(World world, BlockPos pos, BlockState state);

   protected abstract void onViewerCountUpdate(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount);

   protected abstract boolean isPlayerViewing(PlayerEntity player);

   public void openContainer(PlayerEntity player, World world, BlockPos pos, BlockState state) {
      int i = this.viewerCount++;
      if (i == 0) {
         this.onContainerOpen(world, pos, state);
         world.emitGameEvent(player, GameEvent.CONTAINER_OPEN, pos);
         scheduleBlockTick(world, pos, state);
      }

      this.onViewerCountUpdate(world, pos, state, i, this.viewerCount);
   }

   public void closeContainer(PlayerEntity player, World world, BlockPos pos, BlockState state) {
      int i = this.viewerCount--;
      if (this.viewerCount == 0) {
         this.onContainerClose(world, pos, state);
         world.emitGameEvent(player, GameEvent.CONTAINER_CLOSE, pos);
      }

      this.onViewerCountUpdate(world, pos, state, i, this.viewerCount);
   }

   private int getInRangeViewerCount(World world, BlockPos pos) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      float f = 5.0F;
      Box lv = new Box((double)((float)i - 5.0F), (double)((float)j - 5.0F), (double)((float)k - 5.0F), (double)((float)(i + 1) + 5.0F), (double)((float)(j + 1) + 5.0F), (double)((float)(k + 1) + 5.0F));
      return world.getEntitiesByType(TypeFilter.instanceOf(PlayerEntity.class), lv, this::isPlayerViewing).size();
   }

   public void updateViewerCount(World world, BlockPos pos, BlockState state) {
      int i = this.getInRangeViewerCount(world, pos);
      int j = this.viewerCount;
      if (j != i) {
         boolean bl = i != 0;
         boolean bl2 = j != 0;
         if (bl && !bl2) {
            this.onContainerOpen(world, pos, state);
            world.emitGameEvent((Entity)null, GameEvent.CONTAINER_OPEN, pos);
         } else if (!bl) {
            this.onContainerClose(world, pos, state);
            world.emitGameEvent((Entity)null, GameEvent.CONTAINER_CLOSE, pos);
         }

         this.viewerCount = i;
      }

      this.onViewerCountUpdate(world, pos, state, j, i);
      if (i > 0) {
         scheduleBlockTick(world, pos, state);
      }

   }

   public int getViewerCount() {
      return this.viewerCount;
   }

   private static void scheduleBlockTick(World world, BlockPos pos, BlockState state) {
      world.scheduleBlockTick(pos, state.getBlock(), 5);
   }
}
