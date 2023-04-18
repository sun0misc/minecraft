package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public abstract class LockableContainerBlockEntity extends BlockEntity implements Inventory, NamedScreenHandlerFactory, Nameable {
   private ContainerLock lock;
   @Nullable
   private Text customName;

   protected LockableContainerBlockEntity(BlockEntityType arg, BlockPos arg2, BlockState arg3) {
      super(arg, arg2, arg3);
      this.lock = ContainerLock.EMPTY;
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      this.lock = ContainerLock.fromNbt(nbt);
      if (nbt.contains("CustomName", NbtElement.STRING_TYPE)) {
         this.customName = Text.Serializer.fromJson(nbt.getString("CustomName"));
      }

   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      this.lock.writeNbt(nbt);
      if (this.customName != null) {
         nbt.putString("CustomName", Text.Serializer.toJson(this.customName));
      }

   }

   public void setCustomName(Text customName) {
      this.customName = customName;
   }

   public Text getName() {
      return this.customName != null ? this.customName : this.getContainerName();
   }

   public Text getDisplayName() {
      return this.getName();
   }

   @Nullable
   public Text getCustomName() {
      return this.customName;
   }

   protected abstract Text getContainerName();

   public boolean checkUnlocked(PlayerEntity player) {
      return checkUnlocked(player, this.lock, this.getDisplayName());
   }

   public static boolean checkUnlocked(PlayerEntity player, ContainerLock lock, Text containerName) {
      if (!player.isSpectator() && !lock.canOpen(player.getMainHandStack())) {
         player.sendMessage(Text.translatable("container.isLocked", containerName), true);
         player.playSound(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1.0F, 1.0F);
         return false;
      } else {
         return true;
      }
   }

   @Nullable
   public ScreenHandler createMenu(int i, PlayerInventory arg, PlayerEntity arg2) {
      return this.checkUnlocked(arg2) ? this.createScreenHandler(i, arg) : null;
   }

   protected abstract ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory);
}
