package net.minecraft.block.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractFurnaceBlockEntity extends LockableContainerBlockEntity implements SidedInventory, RecipeUnlocker, RecipeInputProvider {
   protected static final int INPUT_SLOT_INDEX = 0;
   protected static final int FUEL_SLOT_INDEX = 1;
   protected static final int OUTPUT_SLOT_INDEX = 2;
   public static final int BURN_TIME_PROPERTY_INDEX = 0;
   private static final int[] TOP_SLOTS = new int[]{0};
   private static final int[] BOTTOM_SLOTS = new int[]{2, 1};
   private static final int[] SIDE_SLOTS = new int[]{1};
   public static final int FUEL_TIME_PROPERTY_INDEX = 1;
   public static final int COOK_TIME_PROPERTY_INDEX = 2;
   public static final int COOK_TIME_TOTAL_PROPERTY_INDEX = 3;
   public static final int PROPERTY_COUNT = 4;
   public static final int DEFAULT_COOK_TIME = 200;
   public static final int field_31295 = 2;
   protected DefaultedList inventory;
   int burnTime;
   int fuelTime;
   int cookTime;
   int cookTimeTotal;
   protected final PropertyDelegate propertyDelegate;
   private final Object2IntOpenHashMap recipesUsed;
   private final RecipeManager.MatchGetter matchGetter;

   protected AbstractFurnaceBlockEntity(BlockEntityType blockEntityType, BlockPos pos, BlockState state, RecipeType recipeType) {
      super(blockEntityType, pos, state);
      this.inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
      this.propertyDelegate = new PropertyDelegate() {
         public int get(int index) {
            switch (index) {
               case 0:
                  return AbstractFurnaceBlockEntity.this.burnTime;
               case 1:
                  return AbstractFurnaceBlockEntity.this.fuelTime;
               case 2:
                  return AbstractFurnaceBlockEntity.this.cookTime;
               case 3:
                  return AbstractFurnaceBlockEntity.this.cookTimeTotal;
               default:
                  return 0;
            }
         }

         public void set(int index, int value) {
            switch (index) {
               case 0:
                  AbstractFurnaceBlockEntity.this.burnTime = value;
                  break;
               case 1:
                  AbstractFurnaceBlockEntity.this.fuelTime = value;
                  break;
               case 2:
                  AbstractFurnaceBlockEntity.this.cookTime = value;
                  break;
               case 3:
                  AbstractFurnaceBlockEntity.this.cookTimeTotal = value;
            }

         }

         public int size() {
            return 4;
         }
      };
      this.recipesUsed = new Object2IntOpenHashMap();
      this.matchGetter = RecipeManager.createCachedMatchGetter(recipeType);
   }

   public static Map createFuelTimeMap() {
      Map map = Maps.newLinkedHashMap();
      addFuel(map, (ItemConvertible)Items.LAVA_BUCKET, 20000);
      addFuel(map, (ItemConvertible)Blocks.COAL_BLOCK, 16000);
      addFuel(map, (ItemConvertible)Items.BLAZE_ROD, 2400);
      addFuel(map, (ItemConvertible)Items.COAL, 1600);
      addFuel(map, (ItemConvertible)Items.CHARCOAL, 1600);
      addFuel(map, (TagKey)ItemTags.LOGS, 300);
      addFuel(map, (TagKey)ItemTags.BAMBOO_BLOCKS, 300);
      addFuel(map, (TagKey)ItemTags.PLANKS, 300);
      addFuel(map, (ItemConvertible)Blocks.BAMBOO_MOSAIC, 300);
      addFuel(map, (TagKey)ItemTags.WOODEN_STAIRS, 300);
      addFuel(map, (ItemConvertible)Blocks.BAMBOO_MOSAIC_STAIRS, 300);
      addFuel(map, (TagKey)ItemTags.WOODEN_SLABS, 150);
      addFuel(map, (ItemConvertible)Blocks.BAMBOO_MOSAIC_SLAB, 150);
      addFuel(map, (TagKey)ItemTags.WOODEN_TRAPDOORS, 300);
      addFuel(map, (TagKey)ItemTags.WOODEN_PRESSURE_PLATES, 300);
      addFuel(map, (TagKey)ItemTags.WOODEN_FENCES, 300);
      addFuel(map, (TagKey)ItemTags.FENCE_GATES, 300);
      addFuel(map, (ItemConvertible)Blocks.NOTE_BLOCK, 300);
      addFuel(map, (ItemConvertible)Blocks.BOOKSHELF, 300);
      addFuel(map, (ItemConvertible)Blocks.CHISELED_BOOKSHELF, 300);
      addFuel(map, (ItemConvertible)Blocks.LECTERN, 300);
      addFuel(map, (ItemConvertible)Blocks.JUKEBOX, 300);
      addFuel(map, (ItemConvertible)Blocks.CHEST, 300);
      addFuel(map, (ItemConvertible)Blocks.TRAPPED_CHEST, 300);
      addFuel(map, (ItemConvertible)Blocks.CRAFTING_TABLE, 300);
      addFuel(map, (ItemConvertible)Blocks.DAYLIGHT_DETECTOR, 300);
      addFuel(map, (TagKey)ItemTags.BANNERS, 300);
      addFuel(map, (ItemConvertible)Items.BOW, 300);
      addFuel(map, (ItemConvertible)Items.FISHING_ROD, 300);
      addFuel(map, (ItemConvertible)Blocks.LADDER, 300);
      addFuel(map, (TagKey)ItemTags.SIGNS, 200);
      addFuel(map, (TagKey)ItemTags.HANGING_SIGNS, 800);
      addFuel(map, (ItemConvertible)Items.WOODEN_SHOVEL, 200);
      addFuel(map, (ItemConvertible)Items.WOODEN_SWORD, 200);
      addFuel(map, (ItemConvertible)Items.WOODEN_HOE, 200);
      addFuel(map, (ItemConvertible)Items.WOODEN_AXE, 200);
      addFuel(map, (ItemConvertible)Items.WOODEN_PICKAXE, 200);
      addFuel(map, (TagKey)ItemTags.WOODEN_DOORS, 200);
      addFuel(map, (TagKey)ItemTags.BOATS, 1200);
      addFuel(map, (TagKey)ItemTags.WOOL, 100);
      addFuel(map, (TagKey)ItemTags.WOODEN_BUTTONS, 100);
      addFuel(map, (ItemConvertible)Items.STICK, 100);
      addFuel(map, (TagKey)ItemTags.SAPLINGS, 100);
      addFuel(map, (ItemConvertible)Items.BOWL, 100);
      addFuel(map, (TagKey)ItemTags.WOOL_CARPETS, 67);
      addFuel(map, (ItemConvertible)Blocks.DRIED_KELP_BLOCK, 4001);
      addFuel(map, (ItemConvertible)Items.CROSSBOW, 300);
      addFuel(map, (ItemConvertible)Blocks.BAMBOO, 50);
      addFuel(map, (ItemConvertible)Blocks.DEAD_BUSH, 100);
      addFuel(map, (ItemConvertible)Blocks.SCAFFOLDING, 50);
      addFuel(map, (ItemConvertible)Blocks.LOOM, 300);
      addFuel(map, (ItemConvertible)Blocks.BARREL, 300);
      addFuel(map, (ItemConvertible)Blocks.CARTOGRAPHY_TABLE, 300);
      addFuel(map, (ItemConvertible)Blocks.FLETCHING_TABLE, 300);
      addFuel(map, (ItemConvertible)Blocks.SMITHING_TABLE, 300);
      addFuel(map, (ItemConvertible)Blocks.COMPOSTER, 300);
      addFuel(map, (ItemConvertible)Blocks.AZALEA, 100);
      addFuel(map, (ItemConvertible)Blocks.FLOWERING_AZALEA, 100);
      addFuel(map, (ItemConvertible)Blocks.MANGROVE_ROOTS, 300);
      return map;
   }

   private static boolean isNonFlammableWood(Item item) {
      return item.getRegistryEntry().isIn(ItemTags.NON_FLAMMABLE_WOOD);
   }

   private static void addFuel(Map fuelTimes, TagKey tag, int fuelTime) {
      Iterator var3 = Registries.ITEM.iterateEntries(tag).iterator();

      while(var3.hasNext()) {
         RegistryEntry lv = (RegistryEntry)var3.next();
         if (!isNonFlammableWood((Item)lv.value())) {
            fuelTimes.put((Item)lv.value(), fuelTime);
         }
      }

   }

   private static void addFuel(Map fuelTimes, ItemConvertible item, int fuelTime) {
      Item lv = item.asItem();
      if (isNonFlammableWood(lv)) {
         if (SharedConstants.isDevelopment) {
            throw (IllegalStateException)Util.throwOrPause(new IllegalStateException("A developer tried to explicitly make fire resistant item " + lv.getName((ItemStack)null).getString() + " a furnace fuel. That will not work!"));
         }
      } else {
         fuelTimes.put(lv, fuelTime);
      }
   }

   private boolean isBurning() {
      return this.burnTime > 0;
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
      Inventories.readNbt(nbt, this.inventory);
      this.burnTime = nbt.getShort("BurnTime");
      this.cookTime = nbt.getShort("CookTime");
      this.cookTimeTotal = nbt.getShort("CookTimeTotal");
      this.fuelTime = this.getFuelTime((ItemStack)this.inventory.get(1));
      NbtCompound lv = nbt.getCompound("RecipesUsed");
      Iterator var3 = lv.getKeys().iterator();

      while(var3.hasNext()) {
         String string = (String)var3.next();
         this.recipesUsed.put(new Identifier(string), lv.getInt(string));
      }

   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      nbt.putShort("BurnTime", (short)this.burnTime);
      nbt.putShort("CookTime", (short)this.cookTime);
      nbt.putShort("CookTimeTotal", (short)this.cookTimeTotal);
      Inventories.writeNbt(nbt, this.inventory);
      NbtCompound lv = new NbtCompound();
      this.recipesUsed.forEach((identifier, count) -> {
         lv.putInt(identifier.toString(), count);
      });
      nbt.put("RecipesUsed", lv);
   }

   public static void tick(World world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity) {
      boolean bl = blockEntity.isBurning();
      boolean bl2 = false;
      if (blockEntity.isBurning()) {
         --blockEntity.burnTime;
      }

      ItemStack lv = (ItemStack)blockEntity.inventory.get(1);
      boolean bl3 = !((ItemStack)blockEntity.inventory.get(0)).isEmpty();
      boolean bl4 = !lv.isEmpty();
      if (blockEntity.isBurning() || bl4 && bl3) {
         Recipe lv2;
         if (bl3) {
            lv2 = (Recipe)blockEntity.matchGetter.getFirstMatch(blockEntity, world).orElse((Object)null);
         } else {
            lv2 = null;
         }

         int i = blockEntity.getMaxCountPerStack();
         if (!blockEntity.isBurning() && canAcceptRecipeOutput(world.getRegistryManager(), lv2, blockEntity.inventory, i)) {
            blockEntity.burnTime = blockEntity.getFuelTime(lv);
            blockEntity.fuelTime = blockEntity.burnTime;
            if (blockEntity.isBurning()) {
               bl2 = true;
               if (bl4) {
                  Item lv3 = lv.getItem();
                  lv.decrement(1);
                  if (lv.isEmpty()) {
                     Item lv4 = lv3.getRecipeRemainder();
                     blockEntity.inventory.set(1, lv4 == null ? ItemStack.EMPTY : new ItemStack(lv4));
                  }
               }
            }
         }

         if (blockEntity.isBurning() && canAcceptRecipeOutput(world.getRegistryManager(), lv2, blockEntity.inventory, i)) {
            ++blockEntity.cookTime;
            if (blockEntity.cookTime == blockEntity.cookTimeTotal) {
               blockEntity.cookTime = 0;
               blockEntity.cookTimeTotal = getCookTime(world, blockEntity);
               if (craftRecipe(world.getRegistryManager(), lv2, blockEntity.inventory, i)) {
                  blockEntity.setLastRecipe(lv2);
               }

               bl2 = true;
            }
         } else {
            blockEntity.cookTime = 0;
         }
      } else if (!blockEntity.isBurning() && blockEntity.cookTime > 0) {
         blockEntity.cookTime = MathHelper.clamp(blockEntity.cookTime - 2, 0, blockEntity.cookTimeTotal);
      }

      if (bl != blockEntity.isBurning()) {
         bl2 = true;
         state = (BlockState)state.with(AbstractFurnaceBlock.LIT, blockEntity.isBurning());
         world.setBlockState(pos, state, Block.NOTIFY_ALL);
      }

      if (bl2) {
         markDirty(world, pos, state);
      }

   }

   private static boolean canAcceptRecipeOutput(DynamicRegistryManager registryManager, @Nullable Recipe recipe, DefaultedList slots, int count) {
      if (!((ItemStack)slots.get(0)).isEmpty() && recipe != null) {
         ItemStack lv = recipe.getOutput(registryManager);
         if (lv.isEmpty()) {
            return false;
         } else {
            ItemStack lv2 = (ItemStack)slots.get(2);
            if (lv2.isEmpty()) {
               return true;
            } else if (!lv2.isItemEqual(lv)) {
               return false;
            } else if (lv2.getCount() < count && lv2.getCount() < lv2.getMaxCount()) {
               return true;
            } else {
               return lv2.getCount() < lv.getMaxCount();
            }
         }
      } else {
         return false;
      }
   }

   private static boolean craftRecipe(DynamicRegistryManager registryManager, @Nullable Recipe recipe, DefaultedList slots, int count) {
      if (recipe != null && canAcceptRecipeOutput(registryManager, recipe, slots, count)) {
         ItemStack lv = (ItemStack)slots.get(0);
         ItemStack lv2 = recipe.getOutput(registryManager);
         ItemStack lv3 = (ItemStack)slots.get(2);
         if (lv3.isEmpty()) {
            slots.set(2, lv2.copy());
         } else if (lv3.isOf(lv2.getItem())) {
            lv3.increment(1);
         }

         if (lv.isOf(Blocks.WET_SPONGE.asItem()) && !((ItemStack)slots.get(1)).isEmpty() && ((ItemStack)slots.get(1)).isOf(Items.BUCKET)) {
            slots.set(1, new ItemStack(Items.WATER_BUCKET));
         }

         lv.decrement(1);
         return true;
      } else {
         return false;
      }
   }

   protected int getFuelTime(ItemStack fuel) {
      if (fuel.isEmpty()) {
         return 0;
      } else {
         Item lv = fuel.getItem();
         return (Integer)createFuelTimeMap().getOrDefault(lv, 0);
      }
   }

   private static int getCookTime(World world, AbstractFurnaceBlockEntity furnace) {
      return (Integer)furnace.matchGetter.getFirstMatch(furnace, world).map(AbstractCookingRecipe::getCookTime).orElse(200);
   }

   public static boolean canUseAsFuel(ItemStack stack) {
      return createFuelTimeMap().containsKey(stack.getItem());
   }

   public int[] getAvailableSlots(Direction side) {
      if (side == Direction.DOWN) {
         return BOTTOM_SLOTS;
      } else {
         return side == Direction.UP ? TOP_SLOTS : SIDE_SLOTS;
      }
   }

   public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
      return this.isValid(slot, stack);
   }

   public boolean canExtract(int slot, ItemStack stack, Direction dir) {
      if (dir == Direction.DOWN && slot == 1) {
         return stack.isOf(Items.WATER_BUCKET) || stack.isOf(Items.BUCKET);
      } else {
         return true;
      }
   }

   public int size() {
      return this.inventory.size();
   }

   public boolean isEmpty() {
      Iterator var1 = this.inventory.iterator();

      ItemStack lv;
      do {
         if (!var1.hasNext()) {
            return true;
         }

         lv = (ItemStack)var1.next();
      } while(lv.isEmpty());

      return false;
   }

   public ItemStack getStack(int slot) {
      return (ItemStack)this.inventory.get(slot);
   }

   public ItemStack removeStack(int slot, int amount) {
      return Inventories.splitStack(this.inventory, slot, amount);
   }

   public ItemStack removeStack(int slot) {
      return Inventories.removeStack(this.inventory, slot);
   }

   public void setStack(int slot, ItemStack stack) {
      ItemStack lv = (ItemStack)this.inventory.get(slot);
      boolean bl = !stack.isEmpty() && stack.isItemEqual(lv) && ItemStack.areNbtEqual(stack, lv);
      this.inventory.set(slot, stack);
      if (stack.getCount() > this.getMaxCountPerStack()) {
         stack.setCount(this.getMaxCountPerStack());
      }

      if (slot == 0 && !bl) {
         this.cookTimeTotal = getCookTime(this.world, this);
         this.cookTime = 0;
         this.markDirty();
      }

   }

   public boolean canPlayerUse(PlayerEntity player) {
      return Inventory.canPlayerUse(this, player);
   }

   public boolean isValid(int slot, ItemStack stack) {
      if (slot == 2) {
         return false;
      } else if (slot != 1) {
         return true;
      } else {
         ItemStack lv = (ItemStack)this.inventory.get(1);
         return canUseAsFuel(stack) || stack.isOf(Items.BUCKET) && !lv.isOf(Items.BUCKET);
      }
   }

   public void clear() {
      this.inventory.clear();
   }

   public void setLastRecipe(@Nullable Recipe recipe) {
      if (recipe != null) {
         Identifier lv = recipe.getId();
         this.recipesUsed.addTo(lv, 1);
      }

   }

   @Nullable
   public Recipe getLastRecipe() {
      return null;
   }

   public void unlockLastRecipe(PlayerEntity player) {
   }

   public void dropExperienceForRecipesUsed(ServerPlayerEntity player) {
      List list = this.getRecipesUsedAndDropExperience(player.getWorld(), player.getPos());
      player.unlockRecipes((Collection)list);
      this.recipesUsed.clear();
   }

   public List getRecipesUsedAndDropExperience(ServerWorld world, Vec3d pos) {
      List list = Lists.newArrayList();
      ObjectIterator var4 = this.recipesUsed.object2IntEntrySet().iterator();

      while(var4.hasNext()) {
         Object2IntMap.Entry entry = (Object2IntMap.Entry)var4.next();
         world.getRecipeManager().get((Identifier)entry.getKey()).ifPresent((recipe) -> {
            list.add(recipe);
            dropExperience(world, pos, entry.getIntValue(), ((AbstractCookingRecipe)recipe).getExperience());
         });
      }

      return list;
   }

   private static void dropExperience(ServerWorld world, Vec3d pos, int multiplier, float experience) {
      int j = MathHelper.floor((float)multiplier * experience);
      float g = MathHelper.fractionalPart((float)multiplier * experience);
      if (g != 0.0F && Math.random() < (double)g) {
         ++j;
      }

      ExperienceOrbEntity.spawn(world, pos, j);
   }

   public void provideRecipeInputs(RecipeMatcher finder) {
      Iterator var2 = this.inventory.iterator();

      while(var2.hasNext()) {
         ItemStack lv = (ItemStack)var2.next();
         finder.addInput(lv);
      }

   }
}
