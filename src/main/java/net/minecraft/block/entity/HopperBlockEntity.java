package net.minecraft.block.entity;

import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HopperBlockEntity extends LootableContainerBlockEntity implements Hopper {
   public static final int TRANSFER_COOLDOWN = 8;
   public static final int INVENTORY_SIZE = 5;
   private DefaultedList inventory;
   private int transferCooldown;
   private long lastTickTime;

   public HopperBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.HOPPER, pos, state);
      this.inventory = DefaultedList.ofSize(5, ItemStack.EMPTY);
      this.transferCooldown = -1;
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
      if (!this.deserializeLootTable(nbt)) {
         Inventories.readNbt(nbt, this.inventory);
      }

      this.transferCooldown = nbt.getInt("TransferCooldown");
   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      if (!this.serializeLootTable(nbt)) {
         Inventories.writeNbt(nbt, this.inventory);
      }

      nbt.putInt("TransferCooldown", this.transferCooldown);
   }

   public int size() {
      return this.inventory.size();
   }

   public ItemStack removeStack(int slot, int amount) {
      this.checkLootInteraction((PlayerEntity)null);
      return Inventories.splitStack(this.getInvStackList(), slot, amount);
   }

   public void setStack(int slot, ItemStack stack) {
      this.checkLootInteraction((PlayerEntity)null);
      this.getInvStackList().set(slot, stack);
      if (stack.getCount() > this.getMaxCountPerStack()) {
         stack.setCount(this.getMaxCountPerStack());
      }

   }

   protected Text getContainerName() {
      return Text.translatable("container.hopper");
   }

   public static void serverTick(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity) {
      --blockEntity.transferCooldown;
      blockEntity.lastTickTime = world.getTime();
      if (!blockEntity.needsCooldown()) {
         blockEntity.setTransferCooldown(0);
         insertAndExtract(world, pos, state, blockEntity, () -> {
            return extract((World)world, (Hopper)blockEntity);
         });
      }

   }

   private static boolean insertAndExtract(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, BooleanSupplier booleanSupplier) {
      if (world.isClient) {
         return false;
      } else {
         if (!blockEntity.needsCooldown() && (Boolean)state.get(HopperBlock.ENABLED)) {
            boolean bl = false;
            if (!blockEntity.isEmpty()) {
               bl = insert(world, pos, state, blockEntity);
            }

            if (!blockEntity.isFull()) {
               bl |= booleanSupplier.getAsBoolean();
            }

            if (bl) {
               blockEntity.setTransferCooldown(8);
               markDirty(world, pos, state);
               return true;
            }
         }

         return false;
      }
   }

   private boolean isFull() {
      Iterator var1 = this.inventory.iterator();

      ItemStack lv;
      do {
         if (!var1.hasNext()) {
            return true;
         }

         lv = (ItemStack)var1.next();
      } while(!lv.isEmpty() && lv.getCount() == lv.getMaxCount());

      return false;
   }

   private static boolean insert(World world, BlockPos pos, BlockState state, Inventory inventory) {
      Inventory lv = getOutputInventory(world, pos, state);
      if (lv == null) {
         return false;
      } else {
         Direction lv2 = ((Direction)state.get(HopperBlock.FACING)).getOpposite();
         if (isInventoryFull(lv, lv2)) {
            return false;
         } else {
            for(int i = 0; i < inventory.size(); ++i) {
               if (!inventory.getStack(i).isEmpty()) {
                  ItemStack lv3 = inventory.getStack(i).copy();
                  ItemStack lv4 = transfer(inventory, lv, inventory.removeStack(i, 1), lv2);
                  if (lv4.isEmpty()) {
                     lv.markDirty();
                     return true;
                  }

                  inventory.setStack(i, lv3);
               }
            }

            return false;
         }
      }
   }

   private static IntStream getAvailableSlots(Inventory inventory, Direction side) {
      return inventory instanceof SidedInventory ? IntStream.of(((SidedInventory)inventory).getAvailableSlots(side)) : IntStream.range(0, inventory.size());
   }

   private static boolean isInventoryFull(Inventory inventory, Direction direction) {
      return getAvailableSlots(inventory, direction).allMatch((slot) -> {
         ItemStack lv = inventory.getStack(slot);
         return lv.getCount() >= lv.getMaxCount();
      });
   }

   private static boolean isInventoryEmpty(Inventory inv, Direction facing) {
      return getAvailableSlots(inv, facing).allMatch((slot) -> {
         return inv.getStack(slot).isEmpty();
      });
   }

   public static boolean extract(World world, Hopper hopper) {
      Inventory lv = getInputInventory(world, hopper);
      if (lv != null) {
         Direction lv2 = Direction.DOWN;
         return isInventoryEmpty(lv, lv2) ? false : getAvailableSlots(lv, lv2).anyMatch((slot) -> {
            return extract(hopper, lv, slot, lv2);
         });
      } else {
         Iterator var3 = getInputItemEntities(world, hopper).iterator();

         ItemEntity lv3;
         do {
            if (!var3.hasNext()) {
               return false;
            }

            lv3 = (ItemEntity)var3.next();
         } while(!extract((Inventory)hopper, (ItemEntity)lv3));

         return true;
      }
   }

   private static boolean extract(Hopper hopper, Inventory inventory, int slot, Direction side) {
      ItemStack lv = inventory.getStack(slot);
      if (!lv.isEmpty() && canExtract(hopper, inventory, lv, slot, side)) {
         ItemStack lv2 = lv.copy();
         ItemStack lv3 = transfer(inventory, hopper, inventory.removeStack(slot, 1), (Direction)null);
         if (lv3.isEmpty()) {
            inventory.markDirty();
            return true;
         }

         inventory.setStack(slot, lv2);
      }

      return false;
   }

   public static boolean extract(Inventory inventory, ItemEntity itemEntity) {
      boolean bl = false;
      ItemStack lv = itemEntity.getStack().copy();
      ItemStack lv2 = transfer((Inventory)null, inventory, lv, (Direction)null);
      if (lv2.isEmpty()) {
         bl = true;
         itemEntity.discard();
      } else {
         itemEntity.setStack(lv2);
      }

      return bl;
   }

   public static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, @Nullable Direction side) {
      int i;
      if (to instanceof SidedInventory lv) {
         if (side != null) {
            int[] is = lv.getAvailableSlots(side);

            for(i = 0; i < is.length && !stack.isEmpty(); ++i) {
               stack = transfer(from, to, stack, is[i], side);
            }

            return stack;
         }
      }

      int j = to.size();

      for(i = 0; i < j && !stack.isEmpty(); ++i) {
         stack = transfer(from, to, stack, i, side);
      }

      return stack;
   }

   private static boolean canInsert(Inventory inventory, ItemStack stack, int slot, @Nullable Direction side) {
      if (!inventory.isValid(slot, stack)) {
         return false;
      } else {
         boolean var10000;
         if (inventory instanceof SidedInventory) {
            SidedInventory lv = (SidedInventory)inventory;
            if (!lv.canInsert(slot, stack, side)) {
               var10000 = false;
               return var10000;
            }
         }

         var10000 = true;
         return var10000;
      }
   }

   private static boolean canExtract(Inventory hopperInventory, Inventory fromInventory, ItemStack stack, int slot, Direction facing) {
      if (!fromInventory.canTransferTo(hopperInventory, slot, stack)) {
         return false;
      } else {
         boolean var10000;
         if (fromInventory instanceof SidedInventory) {
            SidedInventory lv = (SidedInventory)fromInventory;
            if (!lv.canExtract(slot, stack, facing)) {
               var10000 = false;
               return var10000;
            }
         }

         var10000 = true;
         return var10000;
      }
   }

   private static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, int slot, @Nullable Direction side) {
      ItemStack lv = to.getStack(slot);
      if (canInsert(to, stack, slot, side)) {
         boolean bl = false;
         boolean bl2 = to.isEmpty();
         if (lv.isEmpty()) {
            to.setStack(slot, stack);
            stack = ItemStack.EMPTY;
            bl = true;
         } else if (canMergeItems(lv, stack)) {
            int j = stack.getMaxCount() - lv.getCount();
            int k = Math.min(stack.getCount(), j);
            stack.decrement(k);
            lv.increment(k);
            bl = k > 0;
         }

         if (bl) {
            if (bl2 && to instanceof HopperBlockEntity) {
               HopperBlockEntity lv2 = (HopperBlockEntity)to;
               if (!lv2.isDisabled()) {
                  int k = 0;
                  if (from instanceof HopperBlockEntity) {
                     HopperBlockEntity lv3 = (HopperBlockEntity)from;
                     if (lv2.lastTickTime >= lv3.lastTickTime) {
                        k = 1;
                     }
                  }

                  lv2.setTransferCooldown(8 - k);
               }
            }

            to.markDirty();
         }
      }

      return stack;
   }

   @Nullable
   private static Inventory getOutputInventory(World world, BlockPos pos, BlockState state) {
      Direction lv = (Direction)state.get(HopperBlock.FACING);
      return getInventoryAt(world, pos.offset(lv));
   }

   @Nullable
   private static Inventory getInputInventory(World world, Hopper hopper) {
      return getInventoryAt(world, hopper.getHopperX(), hopper.getHopperY() + 1.0, hopper.getHopperZ());
   }

   public static List getInputItemEntities(World world, Hopper hopper) {
      return (List)hopper.getInputAreaShape().getBoundingBoxes().stream().flatMap((box) -> {
         return world.getEntitiesByClass(ItemEntity.class, box.offset(hopper.getHopperX() - 0.5, hopper.getHopperY() - 0.5, hopper.getHopperZ() - 0.5), EntityPredicates.VALID_ENTITY).stream();
      }).collect(Collectors.toList());
   }

   @Nullable
   public static Inventory getInventoryAt(World world, BlockPos pos) {
      return getInventoryAt(world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
   }

   @Nullable
   private static Inventory getInventoryAt(World world, double x, double y, double z) {
      Inventory lv = null;
      BlockPos lv2 = BlockPos.ofFloored(x, y, z);
      BlockState lv3 = world.getBlockState(lv2);
      Block lv4 = lv3.getBlock();
      if (lv4 instanceof InventoryProvider) {
         lv = ((InventoryProvider)lv4).getInventory(lv3, world, lv2);
      } else if (lv3.hasBlockEntity()) {
         BlockEntity lv5 = world.getBlockEntity(lv2);
         if (lv5 instanceof Inventory) {
            lv = (Inventory)lv5;
            if (lv instanceof ChestBlockEntity && lv4 instanceof ChestBlock) {
               lv = ChestBlock.getInventory((ChestBlock)lv4, lv3, world, lv2, true);
            }
         }
      }

      if (lv == null) {
         List list = world.getOtherEntities((Entity)null, new Box(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5), EntityPredicates.VALID_INVENTORIES);
         if (!list.isEmpty()) {
            lv = (Inventory)list.get(world.random.nextInt(list.size()));
         }
      }

      return (Inventory)lv;
   }

   private static boolean canMergeItems(ItemStack first, ItemStack second) {
      if (!first.isOf(second.getItem())) {
         return false;
      } else if (first.getDamage() != second.getDamage()) {
         return false;
      } else {
         return first.getCount() > first.getMaxCount() ? false : ItemStack.areNbtEqual(first, second);
      }
   }

   public double getHopperX() {
      return (double)this.pos.getX() + 0.5;
   }

   public double getHopperY() {
      return (double)this.pos.getY() + 0.5;
   }

   public double getHopperZ() {
      return (double)this.pos.getZ() + 0.5;
   }

   private void setTransferCooldown(int transferCooldown) {
      this.transferCooldown = transferCooldown;
   }

   private boolean needsCooldown() {
      return this.transferCooldown > 0;
   }

   private boolean isDisabled() {
      return this.transferCooldown > 8;
   }

   protected DefaultedList getInvStackList() {
      return this.inventory;
   }

   protected void setInvStackList(DefaultedList list) {
      this.inventory = list;
   }

   public static void onEntityCollided(World world, BlockPos pos, BlockState state, Entity entity, HopperBlockEntity blockEntity) {
      if (entity instanceof ItemEntity && VoxelShapes.matchesAnywhere(VoxelShapes.cuboid(entity.getBoundingBox().offset((double)(-pos.getX()), (double)(-pos.getY()), (double)(-pos.getZ()))), blockEntity.getInputAreaShape(), BooleanBiFunction.AND)) {
         insertAndExtract(world, pos, state, blockEntity, () -> {
            return extract((Inventory)blockEntity, (ItemEntity)((ItemEntity)entity));
         });
      }

   }

   protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
      return new HopperScreenHandler(syncId, playerInventory, this);
   }
}
