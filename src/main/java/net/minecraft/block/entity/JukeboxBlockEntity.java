package net.minecraft.block.entity;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Clearable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class JukeboxBlockEntity extends BlockEntity implements Clearable, SingleStackInventory {
   private static final int SECOND_PER_TICK = 20;
   private final DefaultedList inventory;
   private int ticksThisSecond;
   private long tickCount;
   private long recordStartTick;
   private boolean isPlaying;

   public JukeboxBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.JUKEBOX, pos, state);
      this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      if (nbt.contains("RecordItem", NbtElement.COMPOUND_TYPE)) {
         this.inventory.set(0, ItemStack.fromNbt(nbt.getCompound("RecordItem")));
      }

      this.isPlaying = nbt.getBoolean("IsPlaying");
      this.recordStartTick = nbt.getLong("RecordStartTick");
      this.tickCount = nbt.getLong("TickCount");
   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      if (!this.getStack().isEmpty()) {
         nbt.put("RecordItem", this.getStack().writeNbt(new NbtCompound()));
      }

      nbt.putBoolean("IsPlaying", this.isPlaying);
      nbt.putLong("RecordStartTick", this.recordStartTick);
      nbt.putLong("TickCount", this.tickCount);
   }

   public boolean isPlayingRecord() {
      return !this.getStack().isEmpty() && this.isPlaying;
   }

   private void updateState(@Nullable Entity entity, boolean hasRecord) {
      if (this.world.getBlockState(this.getPos()) == this.getCachedState()) {
         this.world.setBlockState(this.getPos(), (BlockState)this.getCachedState().with(JukeboxBlock.HAS_RECORD, hasRecord), Block.NOTIFY_LISTENERS);
         this.world.emitGameEvent(GameEvent.BLOCK_CHANGE, this.getPos(), GameEvent.Emitter.of(entity, this.getCachedState()));
      }

   }

   @VisibleForTesting
   public void startPlaying() {
      this.recordStartTick = this.tickCount;
      this.isPlaying = true;
      this.world.updateNeighborsAlways(this.getPos(), this.getCachedState().getBlock());
      this.world.syncWorldEvent((PlayerEntity)null, WorldEvents.JUKEBOX_STARTS_PLAYING, this.getPos(), Item.getRawId(this.getStack().getItem()));
      this.markDirty();
   }

   private void stopPlaying() {
      this.isPlaying = false;
      this.world.emitGameEvent(GameEvent.JUKEBOX_STOP_PLAY, this.getPos(), GameEvent.Emitter.of(this.getCachedState()));
      this.world.updateNeighborsAlways(this.getPos(), this.getCachedState().getBlock());
      this.world.syncWorldEvent(WorldEvents.JUKEBOX_STOPS_PLAYING, this.getPos(), 0);
      this.markDirty();
   }

   private void tick(World world, BlockPos pos, BlockState state) {
      ++this.ticksThisSecond;
      if (this.isPlayingRecord()) {
         Item var5 = this.getStack().getItem();
         if (var5 instanceof MusicDiscItem) {
            MusicDiscItem lv = (MusicDiscItem)var5;
            if (this.isSongFinished(lv)) {
               this.stopPlaying();
            } else if (this.hasSecondPassed()) {
               this.ticksThisSecond = 0;
               world.emitGameEvent(GameEvent.JUKEBOX_PLAY, pos, GameEvent.Emitter.of(state));
               this.spawnNoteParticle(world, pos);
            }
         }
      }

      ++this.tickCount;
   }

   private boolean isSongFinished(MusicDiscItem musicDisc) {
      return this.tickCount >= this.recordStartTick + (long)musicDisc.getSongLengthInTicks() + 20L;
   }

   private boolean hasSecondPassed() {
      return this.ticksThisSecond >= 20;
   }

   public ItemStack getStack(int slot) {
      return (ItemStack)this.inventory.get(slot);
   }

   public ItemStack removeStack(int slot, int amount) {
      ItemStack lv = (ItemStack)Objects.requireNonNullElse((ItemStack)this.inventory.get(slot), ItemStack.EMPTY);
      this.inventory.set(slot, ItemStack.EMPTY);
      if (!lv.isEmpty()) {
         this.updateState((Entity)null, false);
         this.stopPlaying();
      }

      return lv;
   }

   public void setStack(int slot, ItemStack stack) {
      if (stack.isIn(ItemTags.MUSIC_DISCS) && this.world != null) {
         this.inventory.set(slot, stack);
         this.updateState((Entity)null, true);
         this.startPlaying();
      }

   }

   public int getMaxCountPerStack() {
      return 1;
   }

   public boolean canPlayerUse(PlayerEntity player) {
      return Inventory.canPlayerUse(this, player);
   }

   public boolean isValid(int slot, ItemStack stack) {
      return stack.isIn(ItemTags.MUSIC_DISCS) && this.getStack(slot).isEmpty();
   }

   public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
      return hopperInventory.containsAny(ItemStack::isEmpty);
   }

   private void spawnNoteParticle(World world, BlockPos pos) {
      if (world instanceof ServerWorld lv) {
         Vec3d lv2 = Vec3d.ofBottomCenter(pos).add(0.0, 1.2000000476837158, 0.0);
         float f = (float)world.getRandom().nextInt(4) / 24.0F;
         lv.spawnParticles(ParticleTypes.NOTE, lv2.getX(), lv2.getY(), lv2.getZ(), 0, (double)f, 0.0, 0.0, 1.0);
      }

   }

   public void dropRecord() {
      if (this.world != null && !this.world.isClient) {
         BlockPos lv = this.getPos();
         ItemStack lv2 = this.getStack();
         if (!lv2.isEmpty()) {
            this.removeStack();
            Vec3d lv3 = Vec3d.add(lv, 0.5, 1.01, 0.5).addRandom(this.world.random, 0.7F);
            ItemStack lv4 = lv2.copy();
            ItemEntity lv5 = new ItemEntity(this.world, lv3.getX(), lv3.getY(), lv3.getZ(), lv4);
            lv5.setToDefaultPickupDelay();
            this.world.spawnEntity(lv5);
         }
      }
   }

   public static void tick(World world, BlockPos pos, BlockState state, JukeboxBlockEntity blockEntity) {
      blockEntity.tick(world, pos, state);
   }

   @VisibleForTesting
   public void setDisc(ItemStack stack) {
      this.inventory.set(0, stack);
      this.world.updateNeighborsAlways(this.getPos(), this.getCachedState().getBlock());
      this.markDirty();
   }
}
