package net.minecraft.enchantment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.collection.Weighting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

public class EnchantmentHelper {
   private static final String ID_KEY = "id";
   private static final String LEVEL_KEY = "lvl";
   private static final float field_38222 = 0.15F;

   public static NbtCompound createNbt(@Nullable Identifier id, int lvl) {
      NbtCompound lv = new NbtCompound();
      lv.putString("id", String.valueOf(id));
      lv.putShort("lvl", (short)lvl);
      return lv;
   }

   public static void writeLevelToNbt(NbtCompound nbt, int lvl) {
      nbt.putShort("lvl", (short)lvl);
   }

   public static int getLevelFromNbt(NbtCompound nbt) {
      return MathHelper.clamp(nbt.getInt("lvl"), 0, 255);
   }

   @Nullable
   public static Identifier getIdFromNbt(NbtCompound nbt) {
      return Identifier.tryParse(nbt.getString("id"));
   }

   @Nullable
   public static Identifier getEnchantmentId(Enchantment enchantment) {
      return Registries.ENCHANTMENT.getId(enchantment);
   }

   public static int getLevel(Enchantment enchantment, ItemStack stack) {
      if (stack.isEmpty()) {
         return 0;
      } else {
         Identifier lv = getEnchantmentId(enchantment);
         NbtList lv2 = stack.getEnchantments();

         for(int i = 0; i < lv2.size(); ++i) {
            NbtCompound lv3 = lv2.getCompound(i);
            Identifier lv4 = getIdFromNbt(lv3);
            if (lv4 != null && lv4.equals(lv)) {
               return getLevelFromNbt(lv3);
            }
         }

         return 0;
      }
   }

   public static Map get(ItemStack stack) {
      NbtList lv = stack.isOf(Items.ENCHANTED_BOOK) ? EnchantedBookItem.getEnchantmentNbt(stack) : stack.getEnchantments();
      return fromNbt(lv);
   }

   public static Map fromNbt(NbtList list) {
      Map map = Maps.newLinkedHashMap();

      for(int i = 0; i < list.size(); ++i) {
         NbtCompound lv = list.getCompound(i);
         Registries.ENCHANTMENT.getOrEmpty(getIdFromNbt(lv)).ifPresent((enchantment) -> {
            map.put(enchantment, getLevelFromNbt(lv));
         });
      }

      return map;
   }

   public static void set(Map enchantments, ItemStack stack) {
      NbtList lv = new NbtList();
      Iterator var3 = enchantments.entrySet().iterator();

      while(var3.hasNext()) {
         Map.Entry entry = (Map.Entry)var3.next();
         Enchantment lv2 = (Enchantment)entry.getKey();
         if (lv2 != null) {
            int i = (Integer)entry.getValue();
            lv.add(createNbt(getEnchantmentId(lv2), i));
            if (stack.isOf(Items.ENCHANTED_BOOK)) {
               EnchantedBookItem.addEnchantment(stack, new EnchantmentLevelEntry(lv2, i));
            }
         }
      }

      if (lv.isEmpty()) {
         stack.removeSubNbt("Enchantments");
      } else if (!stack.isOf(Items.ENCHANTED_BOOK)) {
         stack.setSubNbt("Enchantments", lv);
      }

   }

   private static void forEachEnchantment(Consumer consumer, ItemStack stack) {
      if (!stack.isEmpty()) {
         NbtList lv = stack.getEnchantments();

         for(int i = 0; i < lv.size(); ++i) {
            NbtCompound lv2 = lv.getCompound(i);
            Registries.ENCHANTMENT.getOrEmpty(getIdFromNbt(lv2)).ifPresent((enchantment) -> {
               consumer.accept(enchantment, getLevelFromNbt(lv2));
            });
         }

      }
   }

   private static void forEachEnchantment(Consumer consumer, Iterable stacks) {
      Iterator var2 = stacks.iterator();

      while(var2.hasNext()) {
         ItemStack lv = (ItemStack)var2.next();
         forEachEnchantment(consumer, lv);
      }

   }

   public static int getProtectionAmount(Iterable equipment, DamageSource source) {
      MutableInt mutableInt = new MutableInt();
      forEachEnchantment((enchantment, level) -> {
         mutableInt.add(enchantment.getProtectionAmount(level, source));
      }, equipment);
      return mutableInt.intValue();
   }

   public static float getAttackDamage(ItemStack stack, EntityGroup group) {
      MutableFloat mutableFloat = new MutableFloat();
      forEachEnchantment((enchantment, level) -> {
         mutableFloat.add(enchantment.getAttackDamage(level, group));
      }, stack);
      return mutableFloat.floatValue();
   }

   public static float getSweepingMultiplier(LivingEntity entity) {
      int i = getEquipmentLevel(Enchantments.SWEEPING, entity);
      return i > 0 ? SweepingEnchantment.getMultiplier(i) : 0.0F;
   }

   public static void onUserDamaged(LivingEntity user, Entity attacker) {
      Consumer lv = (enchantment, level) -> {
         enchantment.onUserDamaged(user, attacker, level);
      };
      if (user != null) {
         forEachEnchantment(lv, user.getItemsEquipped());
      }

      if (attacker instanceof PlayerEntity) {
         forEachEnchantment(lv, user.getMainHandStack());
      }

   }

   public static void onTargetDamaged(LivingEntity user, Entity target) {
      Consumer lv = (enchantment, level) -> {
         enchantment.onTargetDamaged(user, target, level);
      };
      if (user != null) {
         forEachEnchantment(lv, user.getItemsEquipped());
      }

      if (user instanceof PlayerEntity) {
         forEachEnchantment(lv, user.getMainHandStack());
      }

   }

   public static int getEquipmentLevel(Enchantment enchantment, LivingEntity entity) {
      Iterable iterable = enchantment.getEquipment(entity).values();
      if (iterable == null) {
         return 0;
      } else {
         int i = 0;
         Iterator var4 = iterable.iterator();

         while(var4.hasNext()) {
            ItemStack lv = (ItemStack)var4.next();
            int j = getLevel(enchantment, lv);
            if (j > i) {
               i = j;
            }
         }

         return i;
      }
   }

   public static float getSwiftSneakSpeedBoost(LivingEntity entity) {
      return (float)getEquipmentLevel(Enchantments.SWIFT_SNEAK, entity) * 0.15F;
   }

   public static int getKnockback(LivingEntity entity) {
      return getEquipmentLevel(Enchantments.KNOCKBACK, entity);
   }

   public static int getFireAspect(LivingEntity entity) {
      return getEquipmentLevel(Enchantments.FIRE_ASPECT, entity);
   }

   public static int getRespiration(LivingEntity entity) {
      return getEquipmentLevel(Enchantments.RESPIRATION, entity);
   }

   public static int getDepthStrider(LivingEntity entity) {
      return getEquipmentLevel(Enchantments.DEPTH_STRIDER, entity);
   }

   public static int getEfficiency(LivingEntity entity) {
      return getEquipmentLevel(Enchantments.EFFICIENCY, entity);
   }

   public static int getLuckOfTheSea(ItemStack stack) {
      return getLevel(Enchantments.LUCK_OF_THE_SEA, stack);
   }

   public static int getLure(ItemStack stack) {
      return getLevel(Enchantments.LURE, stack);
   }

   public static int getLooting(LivingEntity entity) {
      return getEquipmentLevel(Enchantments.LOOTING, entity);
   }

   public static boolean hasAquaAffinity(LivingEntity entity) {
      return getEquipmentLevel(Enchantments.AQUA_AFFINITY, entity) > 0;
   }

   public static boolean hasFrostWalker(LivingEntity entity) {
      return getEquipmentLevel(Enchantments.FROST_WALKER, entity) > 0;
   }

   public static boolean hasSoulSpeed(LivingEntity entity) {
      return getEquipmentLevel(Enchantments.SOUL_SPEED, entity) > 0;
   }

   public static boolean hasBindingCurse(ItemStack stack) {
      return getLevel(Enchantments.BINDING_CURSE, stack) > 0;
   }

   public static boolean hasVanishingCurse(ItemStack stack) {
      return getLevel(Enchantments.VANISHING_CURSE, stack) > 0;
   }

   public static boolean hasSilkTouch(ItemStack stack) {
      return getLevel(Enchantments.SILK_TOUCH, stack) > 0;
   }

   public static int getLoyalty(ItemStack stack) {
      return getLevel(Enchantments.LOYALTY, stack);
   }

   public static int getRiptide(ItemStack stack) {
      return getLevel(Enchantments.RIPTIDE, stack);
   }

   public static boolean hasChanneling(ItemStack stack) {
      return getLevel(Enchantments.CHANNELING, stack) > 0;
   }

   @Nullable
   public static Map.Entry chooseEquipmentWith(Enchantment enchantment, LivingEntity entity) {
      return chooseEquipmentWith(enchantment, entity, (stack) -> {
         return true;
      });
   }

   @Nullable
   public static Map.Entry chooseEquipmentWith(Enchantment enchantment, LivingEntity entity, Predicate condition) {
      Map map = enchantment.getEquipment(entity);
      if (map.isEmpty()) {
         return null;
      } else {
         List list = Lists.newArrayList();
         Iterator var5 = map.entrySet().iterator();

         while(var5.hasNext()) {
            Map.Entry entry = (Map.Entry)var5.next();
            ItemStack lv = (ItemStack)entry.getValue();
            if (!lv.isEmpty() && getLevel(enchantment, lv) > 0 && condition.test(lv)) {
               list.add(entry);
            }
         }

         return list.isEmpty() ? null : (Map.Entry)list.get(entity.getRandom().nextInt(list.size()));
      }
   }

   public static int calculateRequiredExperienceLevel(Random random, int slotIndex, int bookshelfCount, ItemStack stack) {
      Item lv = stack.getItem();
      int k = lv.getEnchantability();
      if (k <= 0) {
         return 0;
      } else {
         if (bookshelfCount > 15) {
            bookshelfCount = 15;
         }

         int l = random.nextInt(8) + 1 + (bookshelfCount >> 1) + random.nextInt(bookshelfCount + 1);
         if (slotIndex == 0) {
            return Math.max(l / 3, 1);
         } else {
            return slotIndex == 1 ? l * 2 / 3 + 1 : Math.max(l, bookshelfCount * 2);
         }
      }
   }

   public static ItemStack enchant(Random random, ItemStack target, int level, boolean treasureAllowed) {
      List list = generateEnchantments(random, target, level, treasureAllowed);
      boolean bl2 = target.isOf(Items.BOOK);
      if (bl2) {
         target = new ItemStack(Items.ENCHANTED_BOOK);
      }

      Iterator var6 = list.iterator();

      while(var6.hasNext()) {
         EnchantmentLevelEntry lv = (EnchantmentLevelEntry)var6.next();
         if (bl2) {
            EnchantedBookItem.addEnchantment(target, lv);
         } else {
            target.addEnchantment(lv.enchantment, lv.level);
         }
      }

      return target;
   }

   public static List generateEnchantments(Random random, ItemStack stack, int level, boolean treasureAllowed) {
      List list = Lists.newArrayList();
      Item lv = stack.getItem();
      int j = lv.getEnchantability();
      if (j <= 0) {
         return list;
      } else {
         level += 1 + random.nextInt(j / 4 + 1) + random.nextInt(j / 4 + 1);
         float f = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
         level = MathHelper.clamp(Math.round((float)level + (float)level * f), 1, Integer.MAX_VALUE);
         List list2 = getPossibleEntries(level, stack, treasureAllowed);
         if (!list2.isEmpty()) {
            Optional var10000 = Weighting.getRandom(random, list2);
            Objects.requireNonNull(list);
            var10000.ifPresent(list::add);

            while(random.nextInt(50) <= level) {
               if (!list.isEmpty()) {
                  removeConflicts(list2, (EnchantmentLevelEntry)Util.getLast(list));
               }

               if (list2.isEmpty()) {
                  break;
               }

               var10000 = Weighting.getRandom(random, list2);
               Objects.requireNonNull(list);
               var10000.ifPresent(list::add);
               level /= 2;
            }
         }

         return list;
      }
   }

   public static void removeConflicts(List possibleEntries, EnchantmentLevelEntry pickedEntry) {
      Iterator iterator = possibleEntries.iterator();

      while(iterator.hasNext()) {
         if (!pickedEntry.enchantment.canCombine(((EnchantmentLevelEntry)iterator.next()).enchantment)) {
            iterator.remove();
         }
      }

   }

   public static boolean isCompatible(Collection existing, Enchantment candidate) {
      Iterator var2 = existing.iterator();

      Enchantment lv;
      do {
         if (!var2.hasNext()) {
            return true;
         }

         lv = (Enchantment)var2.next();
      } while(lv.canCombine(candidate));

      return false;
   }

   public static List getPossibleEntries(int power, ItemStack stack, boolean treasureAllowed) {
      List list = Lists.newArrayList();
      Item lv = stack.getItem();
      boolean bl2 = stack.isOf(Items.BOOK);
      Iterator var6 = Registries.ENCHANTMENT.iterator();

      while(true) {
         while(true) {
            Enchantment lv2;
            do {
               do {
                  do {
                     if (!var6.hasNext()) {
                        return list;
                     }

                     lv2 = (Enchantment)var6.next();
                  } while(lv2.isTreasure() && !treasureAllowed);
               } while(!lv2.isAvailableForRandomSelection());
            } while(!lv2.target.isAcceptableItem(lv) && !bl2);

            for(int j = lv2.getMaxLevel(); j > lv2.getMinLevel() - 1; --j) {
               if (power >= lv2.getMinPower(j) && power <= lv2.getMaxPower(j)) {
                  list.add(new EnchantmentLevelEntry(lv2, j));
                  break;
               }
            }
         }
      }
   }

   @FunctionalInterface
   private interface Consumer {
      void accept(Enchantment enchantment, int level);
   }
}
