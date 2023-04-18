package net.minecraft.world.block;

import com.mojang.logging.LogUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ChainRestrictedNeighborUpdater implements NeighborUpdater {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final World world;
   private final int maxChainDepth;
   private final ArrayDeque queue = new ArrayDeque();
   private final List pending = new ArrayList();
   private int depth = 0;

   public ChainRestrictedNeighborUpdater(World world, int maxChainDepth) {
      this.world = world;
      this.maxChainDepth = maxChainDepth;
   }

   public void replaceWithStateForNeighborUpdate(Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, int flags, int maxUpdateDepth) {
      this.enqueue(pos, new StateReplacementEntry(direction, neighborState, pos.toImmutable(), neighborPos.toImmutable(), flags, maxUpdateDepth));
   }

   public void updateNeighbor(BlockPos pos, Block sourceBlock, BlockPos sourcePos) {
      this.enqueue(pos, new SimpleEntry(pos, sourceBlock, sourcePos.toImmutable()));
   }

   public void updateNeighbor(BlockState state, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
      this.enqueue(pos, new StatefulEntry(state, pos.toImmutable(), sourceBlock, sourcePos.toImmutable(), notify));
   }

   public void updateNeighbors(BlockPos pos, Block sourceBlock, @Nullable Direction except) {
      this.enqueue(pos, new SixWayEntry(pos.toImmutable(), sourceBlock, except));
   }

   private void enqueue(BlockPos pos, Entry entry) {
      boolean bl = this.depth > 0;
      boolean bl2 = this.maxChainDepth >= 0 && this.depth >= this.maxChainDepth;
      ++this.depth;
      if (!bl2) {
         if (bl) {
            this.pending.add(entry);
         } else {
            this.queue.push(entry);
         }
      } else if (this.depth - 1 == this.maxChainDepth) {
         LOGGER.error("Too many chained neighbor updates. Skipping the rest. First skipped position: " + pos.toShortString());
      }

      if (!bl) {
         this.runQueuedUpdates();
      }

   }

   private void runQueuedUpdates() {
      try {
         while(!this.queue.isEmpty() || !this.pending.isEmpty()) {
            for(int i = this.pending.size() - 1; i >= 0; --i) {
               this.queue.push((Entry)this.pending.get(i));
            }

            this.pending.clear();
            Entry lv = (Entry)this.queue.peek();

            while(this.pending.isEmpty()) {
               if (!lv.update(this.world)) {
                  this.queue.pop();
                  break;
               }
            }
         }
      } finally {
         this.queue.clear();
         this.pending.clear();
         this.depth = 0;
      }

   }

   static record StateReplacementEntry(Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, int updateFlags, int updateLimit) implements Entry {
      StateReplacementEntry(Direction arg, BlockState arg2, BlockPos arg3, BlockPos arg4, int i, int j) {
         this.direction = arg;
         this.neighborState = arg2;
         this.pos = arg3;
         this.neighborPos = arg4;
         this.updateFlags = i;
         this.updateLimit = j;
      }

      public boolean update(World world) {
         NeighborUpdater.replaceWithStateForNeighborUpdate(world, this.direction, this.neighborState, this.pos, this.neighborPos, this.updateFlags, this.updateLimit);
         return false;
      }

      public Direction direction() {
         return this.direction;
      }

      public BlockState neighborState() {
         return this.neighborState;
      }

      public BlockPos pos() {
         return this.pos;
      }

      public BlockPos neighborPos() {
         return this.neighborPos;
      }

      public int updateFlags() {
         return this.updateFlags;
      }

      public int updateLimit() {
         return this.updateLimit;
      }
   }

   private interface Entry {
      boolean update(World world);
   }

   static record SimpleEntry(BlockPos pos, Block sourceBlock, BlockPos sourcePos) implements Entry {
      SimpleEntry(BlockPos arg, Block arg2, BlockPos arg3) {
         this.pos = arg;
         this.sourceBlock = arg2;
         this.sourcePos = arg3;
      }

      public boolean update(World world) {
         BlockState lv = world.getBlockState(this.pos);
         NeighborUpdater.tryNeighborUpdate(world, lv, this.pos, this.sourceBlock, this.sourcePos, false);
         return false;
      }

      public BlockPos pos() {
         return this.pos;
      }

      public Block sourceBlock() {
         return this.sourceBlock;
      }

      public BlockPos sourcePos() {
         return this.sourcePos;
      }
   }

   static record StatefulEntry(BlockState state, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean movedByPiston) implements Entry {
      StatefulEntry(BlockState arg, BlockPos arg2, Block arg3, BlockPos arg4, boolean bl) {
         this.state = arg;
         this.pos = arg2;
         this.sourceBlock = arg3;
         this.sourcePos = arg4;
         this.movedByPiston = bl;
      }

      public boolean update(World world) {
         NeighborUpdater.tryNeighborUpdate(world, this.state, this.pos, this.sourceBlock, this.sourcePos, this.movedByPiston);
         return false;
      }

      public BlockState state() {
         return this.state;
      }

      public BlockPos pos() {
         return this.pos;
      }

      public Block sourceBlock() {
         return this.sourceBlock;
      }

      public BlockPos sourcePos() {
         return this.sourcePos;
      }

      public boolean movedByPiston() {
         return this.movedByPiston;
      }
   }

   static final class SixWayEntry implements Entry {
      private final BlockPos pos;
      private final Block sourceBlock;
      @Nullable
      private final Direction except;
      private int currentDirectionIndex = 0;

      SixWayEntry(BlockPos pos, Block sourceBlock, @Nullable Direction except) {
         this.pos = pos;
         this.sourceBlock = sourceBlock;
         this.except = except;
         if (NeighborUpdater.UPDATE_ORDER[this.currentDirectionIndex] == except) {
            ++this.currentDirectionIndex;
         }

      }

      public boolean update(World world) {
         BlockPos lv = this.pos.offset(NeighborUpdater.UPDATE_ORDER[this.currentDirectionIndex++]);
         BlockState lv2 = world.getBlockState(lv);
         lv2.neighborUpdate(world, lv, this.sourceBlock, this.pos, false);
         if (this.currentDirectionIndex < NeighborUpdater.UPDATE_ORDER.length && NeighborUpdater.UPDATE_ORDER[this.currentDirectionIndex] == this.except) {
            ++this.currentDirectionIndex;
         }

         return this.currentDirectionIndex < NeighborUpdater.UPDATE_ORDER.length;
      }
   }
}
