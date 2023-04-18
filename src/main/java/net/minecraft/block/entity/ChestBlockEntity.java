package net.minecraft.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ChestBlockEntity extends LootableContainerBlockEntity implements LidOpenable {
   private static final int VIEWER_COUNT_UPDATE_EVENT_TYPE = 1;
   private DefaultedList inventory;
   private final ViewerCountManager stateManager;
   private final ChestLidAnimator lidAnimator;

   protected ChestBlockEntity(BlockEntityType arg, BlockPos arg2, BlockState arg3) {
      super(arg, arg2, arg3);
      this.inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
      this.stateManager = new ViewerCountManager() {
         protected void onContainerOpen(World world, BlockPos pos, BlockState state) {
            ChestBlockEntity.playSound(world, pos, state, SoundEvents.BLOCK_CHEST_OPEN);
         }

         protected void onContainerClose(World world, BlockPos pos, BlockState state) {
            ChestBlockEntity.playSound(world, pos, state, SoundEvents.BLOCK_CHEST_CLOSE);
         }

         protected void onViewerCountUpdate(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
            ChestBlockEntity.this.onViewerCountUpdate(world, pos, state, oldViewerCount, newViewerCount);
         }

         protected boolean isPlayerViewing(PlayerEntity player) {
            if (!(player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
               return false;
            } else {
               Inventory lv = ((GenericContainerScreenHandler)player.currentScreenHandler).getInventory();
               return lv == ChestBlockEntity.this || lv instanceof DoubleInventory && ((DoubleInventory)lv).isPart(ChestBlockEntity.this);
            }
         }
      };
      this.lidAnimator = new ChestLidAnimator();
   }

   public ChestBlockEntity(BlockPos pos, BlockState state) {
      this(BlockEntityType.CHEST, pos, state);
   }

   public int size() {
      return 27;
   }

   protected Text getContainerName() {
      return Text.translatable("container.chest");
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
      if (!this.deserializeLootTable(nbt)) {
         Inventories.readNbt(nbt, this.inventory);
      }

   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      if (!this.serializeLootTable(nbt)) {
         Inventories.writeNbt(nbt, this.inventory);
      }

   }

   public static void clientTick(World world, BlockPos pos, BlockState state, ChestBlockEntity blockEntity) {
      blockEntity.lidAnimator.step();
   }

   static void playSound(World world, BlockPos pos, BlockState state, SoundEvent soundEvent) {
      ChestType lv = (ChestType)state.get(ChestBlock.CHEST_TYPE);
      if (lv != ChestType.LEFT) {
         double d = (double)pos.getX() + 0.5;
         double e = (double)pos.getY() + 0.5;
         double f = (double)pos.getZ() + 0.5;
         if (lv == ChestType.RIGHT) {
            Direction lv2 = ChestBlock.getFacing(state);
            d += (double)lv2.getOffsetX() * 0.5;
            f += (double)lv2.getOffsetZ() * 0.5;
         }

         world.playSound((PlayerEntity)null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
      }
   }

   public boolean onSyncedBlockEvent(int type, int data) {
      if (type == 1) {
         this.lidAnimator.setOpen(data > 0);
         return true;
      } else {
         return super.onSyncedBlockEvent(type, data);
      }
   }

   public void onOpen(PlayerEntity player) {
      if (!this.removed && !player.isSpectator()) {
         this.stateManager.openContainer(player, this.getWorld(), this.getPos(), this.getCachedState());
      }

   }

   public void onClose(PlayerEntity player) {
      if (!this.removed && !player.isSpectator()) {
         this.stateManager.closeContainer(player, this.getWorld(), this.getPos(), this.getCachedState());
      }

   }

   protected DefaultedList getInvStackList() {
      return this.inventory;
   }

   protected void setInvStackList(DefaultedList list) {
      this.inventory = list;
   }

   public float getAnimationProgress(float tickDelta) {
      return this.lidAnimator.getProgress(tickDelta);
   }

   public static int getPlayersLookingInChestCount(BlockView world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos);
      if (lv.hasBlockEntity()) {
         BlockEntity lv2 = world.getBlockEntity(pos);
         if (lv2 instanceof ChestBlockEntity) {
            return ((ChestBlockEntity)lv2).stateManager.getViewerCount();
         }
      }

      return 0;
   }

   public static void copyInventory(ChestBlockEntity from, ChestBlockEntity to) {
      DefaultedList lv = from.getInvStackList();
      from.setInvStackList(to.getInvStackList());
      to.setInvStackList(lv);
   }

   protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
      return GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, this);
   }

   public void onScheduledTick() {
      if (!this.removed) {
         this.stateManager.updateViewerCount(this.getWorld(), this.getPos(), this.getCachedState());
      }

   }

   protected void onViewerCountUpdate(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
      Block lv = state.getBlock();
      world.addSyncedBlockEvent(pos, lv, 1, newViewerCount);
   }
}
