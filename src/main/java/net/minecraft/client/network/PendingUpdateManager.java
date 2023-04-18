package net.minecraft.client.network;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class PendingUpdateManager implements AutoCloseable {
   private final Long2ObjectOpenHashMap blockPosToPendingUpdate = new Long2ObjectOpenHashMap();
   private int sequence;
   private boolean pendingSequence;

   public void addPendingUpdate(BlockPos pos, BlockState state, ClientPlayerEntity player) {
      this.blockPosToPendingUpdate.compute(pos.asLong(), (posLong, pendingUpdate) -> {
         return pendingUpdate != null ? pendingUpdate.withSequence(this.sequence) : new PendingUpdate(this.sequence, state, player.getPos());
      });
   }

   public boolean hasPendingUpdate(BlockPos pos, BlockState state) {
      PendingUpdate lv = (PendingUpdate)this.blockPosToPendingUpdate.get(pos.asLong());
      if (lv == null) {
         return false;
      } else {
         lv.setBlockState(state);
         return true;
      }
   }

   public void processPendingUpdates(int maxProcessableSequence, ClientWorld world) {
      ObjectIterator objectIterator = this.blockPosToPendingUpdate.long2ObjectEntrySet().iterator();

      while(objectIterator.hasNext()) {
         Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)objectIterator.next();
         PendingUpdate lv = (PendingUpdate)entry.getValue();
         if (lv.sequence <= maxProcessableSequence) {
            BlockPos lv2 = BlockPos.fromLong(entry.getLongKey());
            objectIterator.remove();
            world.processPendingUpdate(lv2, lv.blockState, lv.playerPos);
         }
      }

   }

   public PendingUpdateManager incrementSequence() {
      ++this.sequence;
      this.pendingSequence = true;
      return this;
   }

   public void close() {
      this.pendingSequence = false;
   }

   public int getSequence() {
      return this.sequence;
   }

   public boolean hasPendingSequence() {
      return this.pendingSequence;
   }

   @Environment(EnvType.CLIENT)
   private static class PendingUpdate {
      final Vec3d playerPos;
      int sequence;
      BlockState blockState;

      PendingUpdate(int sequence, BlockState blockState, Vec3d playerPos) {
         this.sequence = sequence;
         this.blockState = blockState;
         this.playerPos = playerPos;
      }

      PendingUpdate withSequence(int sequence) {
         this.sequence = sequence;
         return this;
      }

      void setBlockState(BlockState state) {
         this.blockState = state;
      }
   }
}
