package net.minecraft.item;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.client.item.BundleTooltipData;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BundleItem extends Item {
   private static final String ITEMS_KEY = "Items";
   public static final int MAX_STORAGE = 64;
   private static final int BUNDLE_ITEM_OCCUPANCY = 4;
   private static final int ITEM_BAR_COLOR = MathHelper.packRgb(0.4F, 0.4F, 1.0F);

   public BundleItem(Item.Settings arg) {
      super(arg);
   }

   public static float getAmountFilled(ItemStack stack) {
      return (float)getBundleOccupancy(stack) / 64.0F;
   }

   public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
      if (clickType != ClickType.RIGHT) {
         return false;
      } else {
         ItemStack lv = slot.getStack();
         if (lv.isEmpty()) {
            this.playRemoveOneSound(player);
            removeFirstStack(stack).ifPresent((removedStack) -> {
               addToBundle(stack, slot.insertStack(removedStack));
            });
         } else if (lv.getItem().canBeNested()) {
            int i = (64 - getBundleOccupancy(stack)) / getItemOccupancy(lv);
            int j = addToBundle(stack, slot.takeStackRange(lv.getCount(), i, player));
            if (j > 0) {
               this.playInsertSound(player);
            }
         }

         return true;
      }
   }

   public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
      if (clickType == ClickType.RIGHT && slot.canTakePartial(player)) {
         if (otherStack.isEmpty()) {
            removeFirstStack(stack).ifPresent((arg3) -> {
               this.playRemoveOneSound(player);
               cursorStackReference.set(arg3);
            });
         } else {
            int i = addToBundle(stack, otherStack);
            if (i > 0) {
               this.playInsertSound(player);
               otherStack.decrement(i);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      ItemStack lv = user.getStackInHand(hand);
      if (dropAllBundledItems(lv, user)) {
         this.playDropContentsSound(user);
         user.incrementStat(Stats.USED.getOrCreateStat(this));
         return TypedActionResult.success(lv, world.isClient());
      } else {
         return TypedActionResult.fail(lv);
      }
   }

   public boolean isItemBarVisible(ItemStack stack) {
      return getBundleOccupancy(stack) > 0;
   }

   public int getItemBarStep(ItemStack stack) {
      return Math.min(1 + 12 * getBundleOccupancy(stack) / 64, 13);
   }

   public int getItemBarColor(ItemStack stack) {
      return ITEM_BAR_COLOR;
   }

   private static int addToBundle(ItemStack bundle, ItemStack stack) {
      if (!stack.isEmpty() && stack.getItem().canBeNested()) {
         NbtCompound lv = bundle.getOrCreateNbt();
         if (!lv.contains("Items")) {
            lv.put("Items", new NbtList());
         }

         int i = getBundleOccupancy(bundle);
         int j = getItemOccupancy(stack);
         int k = Math.min(stack.getCount(), (64 - i) / j);
         if (k == 0) {
            return 0;
         } else {
            NbtList lv2 = lv.getList("Items", NbtElement.COMPOUND_TYPE);
            Optional optional = canMergeStack(stack, lv2);
            if (optional.isPresent()) {
               NbtCompound lv3 = (NbtCompound)optional.get();
               ItemStack lv4 = ItemStack.fromNbt(lv3);
               lv4.increment(k);
               lv4.writeNbt(lv3);
               lv2.remove(lv3);
               lv2.add(0, (NbtElement)lv3);
            } else {
               ItemStack lv5 = stack.copyWithCount(k);
               NbtCompound lv6 = new NbtCompound();
               lv5.writeNbt(lv6);
               lv2.add(0, (NbtElement)lv6);
            }

            return k;
         }
      } else {
         return 0;
      }
   }

   private static Optional canMergeStack(ItemStack stack, NbtList items) {
      if (stack.isOf(Items.BUNDLE)) {
         return Optional.empty();
      } else {
         Stream var10000 = items.stream();
         Objects.requireNonNull(NbtCompound.class);
         var10000 = var10000.filter(NbtCompound.class::isInstance);
         Objects.requireNonNull(NbtCompound.class);
         return var10000.map(NbtCompound.class::cast).filter((item) -> {
            return ItemStack.canCombine(ItemStack.fromNbt(item), stack);
         }).findFirst();
      }
   }

   private static int getItemOccupancy(ItemStack stack) {
      if (stack.isOf(Items.BUNDLE)) {
         return 4 + getBundleOccupancy(stack);
      } else {
         if ((stack.isOf(Items.BEEHIVE) || stack.isOf(Items.BEE_NEST)) && stack.hasNbt()) {
            NbtCompound lv = BlockItem.getBlockEntityNbt(stack);
            if (lv != null && !lv.getList("Bees", NbtElement.COMPOUND_TYPE).isEmpty()) {
               return 64;
            }
         }

         return 64 / stack.getMaxCount();
      }
   }

   private static int getBundleOccupancy(ItemStack stack) {
      return getBundledStacks(stack).mapToInt((arg) -> {
         return getItemOccupancy(arg) * arg.getCount();
      }).sum();
   }

   private static Optional removeFirstStack(ItemStack stack) {
      NbtCompound lv = stack.getOrCreateNbt();
      if (!lv.contains("Items")) {
         return Optional.empty();
      } else {
         NbtList lv2 = lv.getList("Items", NbtElement.COMPOUND_TYPE);
         if (lv2.isEmpty()) {
            return Optional.empty();
         } else {
            int i = false;
            NbtCompound lv3 = lv2.getCompound(0);
            ItemStack lv4 = ItemStack.fromNbt(lv3);
            lv2.remove(0);
            if (lv2.isEmpty()) {
               stack.removeSubNbt("Items");
            }

            return Optional.of(lv4);
         }
      }
   }

   private static boolean dropAllBundledItems(ItemStack stack, PlayerEntity player) {
      NbtCompound lv = stack.getOrCreateNbt();
      if (!lv.contains("Items")) {
         return false;
      } else {
         if (player instanceof ServerPlayerEntity) {
            NbtList lv2 = lv.getList("Items", NbtElement.COMPOUND_TYPE);

            for(int i = 0; i < lv2.size(); ++i) {
               NbtCompound lv3 = lv2.getCompound(i);
               ItemStack lv4 = ItemStack.fromNbt(lv3);
               player.dropItem(lv4, true);
            }
         }

         stack.removeSubNbt("Items");
         return true;
      }
   }

   private static Stream getBundledStacks(ItemStack stack) {
      NbtCompound lv = stack.getNbt();
      if (lv == null) {
         return Stream.empty();
      } else {
         NbtList lv2 = lv.getList("Items", NbtElement.COMPOUND_TYPE);
         Stream var10000 = lv2.stream();
         Objects.requireNonNull(NbtCompound.class);
         return var10000.map(NbtCompound.class::cast).map(ItemStack::fromNbt);
      }
   }

   public Optional getTooltipData(ItemStack stack) {
      DefaultedList lv = DefaultedList.of();
      Stream var10000 = getBundledStacks(stack);
      Objects.requireNonNull(lv);
      var10000.forEach(lv::add);
      return Optional.of(new BundleTooltipData(lv, getBundleOccupancy(stack)));
   }

   public void appendTooltip(ItemStack stack, World world, List tooltip, TooltipContext context) {
      tooltip.add(Text.translatable("item.minecraft.bundle.fullness", getBundleOccupancy(stack), 64).formatted(Formatting.GRAY));
   }

   public void onItemEntityDestroyed(ItemEntity entity) {
      ItemUsage.spawnItemContents(entity, getBundledStacks(entity.getStack()));
   }

   private void playRemoveOneSound(Entity entity) {
      entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
   }

   private void playInsertSound(Entity entity) {
      entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
   }

   private void playDropContentsSound(Entity entity) {
      entity.playSound(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
   }
}
