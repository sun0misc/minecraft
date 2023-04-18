package net.minecraft.block.entity;

import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Clearable;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class CampfireBlockEntity extends BlockEntity implements Clearable {
   private static final int field_31330 = 2;
   private static final int field_31331 = 4;
   private final DefaultedList itemsBeingCooked;
   private final int[] cookingTimes;
   private final int[] cookingTotalTimes;
   private final RecipeManager.MatchGetter matchGetter;

   public CampfireBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.CAMPFIRE, pos, state);
      this.itemsBeingCooked = DefaultedList.ofSize(4, ItemStack.EMPTY);
      this.cookingTimes = new int[4];
      this.cookingTotalTimes = new int[4];
      this.matchGetter = RecipeManager.createCachedMatchGetter(RecipeType.CAMPFIRE_COOKING);
   }

   public static void litServerTick(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
      boolean bl = false;

      for(int i = 0; i < campfire.itemsBeingCooked.size(); ++i) {
         ItemStack lv = (ItemStack)campfire.itemsBeingCooked.get(i);
         if (!lv.isEmpty()) {
            bl = true;
            int var10002 = campfire.cookingTimes[i]++;
            if (campfire.cookingTimes[i] >= campfire.cookingTotalTimes[i]) {
               Inventory lv2 = new SimpleInventory(new ItemStack[]{lv});
               ItemStack lv3 = (ItemStack)campfire.matchGetter.getFirstMatch(lv2, world).map((recipe) -> {
                  return recipe.craft(lv2, world.getRegistryManager());
               }).orElse(lv);
               if (lv3.isItemEnabled(world.getEnabledFeatures())) {
                  ItemScatterer.spawn(world, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), lv3);
                  campfire.itemsBeingCooked.set(i, ItemStack.EMPTY);
                  world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
                  world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
               }
            }
         }
      }

      if (bl) {
         markDirty(world, pos, state);
      }

   }

   public static void unlitServerTick(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
      boolean bl = false;

      for(int i = 0; i < campfire.itemsBeingCooked.size(); ++i) {
         if (campfire.cookingTimes[i] > 0) {
            bl = true;
            campfire.cookingTimes[i] = MathHelper.clamp(campfire.cookingTimes[i] - 2, 0, campfire.cookingTotalTimes[i]);
         }
      }

      if (bl) {
         markDirty(world, pos, state);
      }

   }

   public static void clientTick(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
      Random lv = world.random;
      int i;
      if (lv.nextFloat() < 0.11F) {
         for(i = 0; i < lv.nextInt(2) + 2; ++i) {
            CampfireBlock.spawnSmokeParticle(world, pos, (Boolean)state.get(CampfireBlock.SIGNAL_FIRE), false);
         }
      }

      i = ((Direction)state.get(CampfireBlock.FACING)).getHorizontal();

      for(int j = 0; j < campfire.itemsBeingCooked.size(); ++j) {
         if (!((ItemStack)campfire.itemsBeingCooked.get(j)).isEmpty() && lv.nextFloat() < 0.2F) {
            Direction lv2 = Direction.fromHorizontal(Math.floorMod(j + i, 4));
            float f = 0.3125F;
            double d = (double)pos.getX() + 0.5 - (double)((float)lv2.getOffsetX() * 0.3125F) + (double)((float)lv2.rotateYClockwise().getOffsetX() * 0.3125F);
            double e = (double)pos.getY() + 0.5;
            double g = (double)pos.getZ() + 0.5 - (double)((float)lv2.getOffsetZ() * 0.3125F) + (double)((float)lv2.rotateYClockwise().getOffsetZ() * 0.3125F);

            for(int k = 0; k < 4; ++k) {
               world.addParticle(ParticleTypes.SMOKE, d, e, g, 0.0, 5.0E-4, 0.0);
            }
         }
      }

   }

   public DefaultedList getItemsBeingCooked() {
      return this.itemsBeingCooked;
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      this.itemsBeingCooked.clear();
      Inventories.readNbt(nbt, this.itemsBeingCooked);
      int[] is;
      if (nbt.contains("CookingTimes", NbtElement.INT_ARRAY_TYPE)) {
         is = nbt.getIntArray("CookingTimes");
         System.arraycopy(is, 0, this.cookingTimes, 0, Math.min(this.cookingTotalTimes.length, is.length));
      }

      if (nbt.contains("CookingTotalTimes", NbtElement.INT_ARRAY_TYPE)) {
         is = nbt.getIntArray("CookingTotalTimes");
         System.arraycopy(is, 0, this.cookingTotalTimes, 0, Math.min(this.cookingTotalTimes.length, is.length));
      }

   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      Inventories.writeNbt(nbt, this.itemsBeingCooked, true);
      nbt.putIntArray("CookingTimes", this.cookingTimes);
      nbt.putIntArray("CookingTotalTimes", this.cookingTotalTimes);
   }

   public BlockEntityUpdateS2CPacket toUpdatePacket() {
      return BlockEntityUpdateS2CPacket.create(this);
   }

   public NbtCompound toInitialChunkDataNbt() {
      NbtCompound lv = new NbtCompound();
      Inventories.writeNbt(lv, this.itemsBeingCooked, true);
      return lv;
   }

   public Optional getRecipeFor(ItemStack stack) {
      return this.itemsBeingCooked.stream().noneMatch(ItemStack::isEmpty) ? Optional.empty() : this.matchGetter.getFirstMatch(new SimpleInventory(new ItemStack[]{stack}), this.world);
   }

   public boolean addItem(@Nullable Entity user, ItemStack stack, int cookTime) {
      for(int j = 0; j < this.itemsBeingCooked.size(); ++j) {
         ItemStack lv = (ItemStack)this.itemsBeingCooked.get(j);
         if (lv.isEmpty()) {
            this.cookingTotalTimes[j] = cookTime;
            this.cookingTimes[j] = 0;
            this.itemsBeingCooked.set(j, stack.split(1));
            this.world.emitGameEvent(GameEvent.BLOCK_CHANGE, this.getPos(), GameEvent.Emitter.of(user, this.getCachedState()));
            this.updateListeners();
            return true;
         }
      }

      return false;
   }

   private void updateListeners() {
      this.markDirty();
      this.getWorld().updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
   }

   public void clear() {
      this.itemsBeingCooked.clear();
   }

   public void spawnItemsBeingCooked() {
      if (this.world != null) {
         this.updateListeners();
      }

   }

   // $FF: synthetic method
   public Packet toUpdatePacket() {
      return this.toUpdatePacket();
   }
}
