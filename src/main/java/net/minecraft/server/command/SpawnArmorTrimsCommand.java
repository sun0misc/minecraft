package net.minecraft.server.command;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimMaterials;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.item.trim.ArmorTrimPatterns;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class SpawnArmorTrimsCommand {
   private static final Map ARMOR_PIECES = (Map)Util.make(Maps.newHashMap(), (map) -> {
      map.put(Pair.of(ArmorMaterials.CHAIN, EquipmentSlot.HEAD), Items.CHAINMAIL_HELMET);
      map.put(Pair.of(ArmorMaterials.CHAIN, EquipmentSlot.CHEST), Items.CHAINMAIL_CHESTPLATE);
      map.put(Pair.of(ArmorMaterials.CHAIN, EquipmentSlot.LEGS), Items.CHAINMAIL_LEGGINGS);
      map.put(Pair.of(ArmorMaterials.CHAIN, EquipmentSlot.FEET), Items.CHAINMAIL_BOOTS);
      map.put(Pair.of(ArmorMaterials.IRON, EquipmentSlot.HEAD), Items.IRON_HELMET);
      map.put(Pair.of(ArmorMaterials.IRON, EquipmentSlot.CHEST), Items.IRON_CHESTPLATE);
      map.put(Pair.of(ArmorMaterials.IRON, EquipmentSlot.LEGS), Items.IRON_LEGGINGS);
      map.put(Pair.of(ArmorMaterials.IRON, EquipmentSlot.FEET), Items.IRON_BOOTS);
      map.put(Pair.of(ArmorMaterials.GOLD, EquipmentSlot.HEAD), Items.GOLDEN_HELMET);
      map.put(Pair.of(ArmorMaterials.GOLD, EquipmentSlot.CHEST), Items.GOLDEN_CHESTPLATE);
      map.put(Pair.of(ArmorMaterials.GOLD, EquipmentSlot.LEGS), Items.GOLDEN_LEGGINGS);
      map.put(Pair.of(ArmorMaterials.GOLD, EquipmentSlot.FEET), Items.GOLDEN_BOOTS);
      map.put(Pair.of(ArmorMaterials.NETHERITE, EquipmentSlot.HEAD), Items.NETHERITE_HELMET);
      map.put(Pair.of(ArmorMaterials.NETHERITE, EquipmentSlot.CHEST), Items.NETHERITE_CHESTPLATE);
      map.put(Pair.of(ArmorMaterials.NETHERITE, EquipmentSlot.LEGS), Items.NETHERITE_LEGGINGS);
      map.put(Pair.of(ArmorMaterials.NETHERITE, EquipmentSlot.FEET), Items.NETHERITE_BOOTS);
      map.put(Pair.of(ArmorMaterials.DIAMOND, EquipmentSlot.HEAD), Items.DIAMOND_HELMET);
      map.put(Pair.of(ArmorMaterials.DIAMOND, EquipmentSlot.CHEST), Items.DIAMOND_CHESTPLATE);
      map.put(Pair.of(ArmorMaterials.DIAMOND, EquipmentSlot.LEGS), Items.DIAMOND_LEGGINGS);
      map.put(Pair.of(ArmorMaterials.DIAMOND, EquipmentSlot.FEET), Items.DIAMOND_BOOTS);
      map.put(Pair.of(ArmorMaterials.TURTLE, EquipmentSlot.HEAD), Items.TURTLE_HELMET);
   });
   private static final List PATTERNS;
   private static final List MATERIALS;
   private static final ToIntFunction PATTERN_INDEX_GETTER;
   private static final ToIntFunction MATERIAL_INDEX_GETTER;

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("spawn_armor_trims").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), ((ServerCommandSource)context.getSource()).getPlayerOrThrow());
      }));
   }

   private static int execute(ServerCommandSource source, PlayerEntity player) {
      World lv = player.getWorld();
      DefaultedList lv2 = DefaultedList.of();
      Registry lv3 = lv.getRegistryManager().get(RegistryKeys.TRIM_PATTERN);
      Registry lv4 = lv.getRegistryManager().get(RegistryKeys.TRIM_MATERIAL);
      lv3.stream().sorted(Comparator.comparing((pattern) -> {
         return PATTERN_INDEX_GETTER.applyAsInt((RegistryKey)lv3.getKey(pattern).orElse((Object)null));
      })).forEachOrdered((pattern) -> {
         lv4.stream().sorted(Comparator.comparing((material) -> {
            return MATERIAL_INDEX_GETTER.applyAsInt((RegistryKey)lv4.getKey(material).orElse((Object)null));
         })).forEachOrdered((material) -> {
            lv2.add(new ArmorTrim(lv4.getEntry((Object)material), lv3.getEntry((Object)pattern)));
         });
      });
      BlockPos lv5 = player.getBlockPos().offset((Direction)player.getHorizontalFacing(), 5);
      int i = ArmorMaterials.values().length - 1;
      double d = 3.0;
      int j = 0;
      int k = 0;

      for(Iterator var12 = lv2.iterator(); var12.hasNext(); ++j) {
         ArmorTrim lv6 = (ArmorTrim)var12.next();
         ArmorMaterials[] var14 = ArmorMaterials.values();
         int var15 = var14.length;

         for(int var16 = 0; var16 < var15; ++var16) {
            ArmorMaterial lv7 = var14[var16];
            if (lv7 != ArmorMaterials.LEATHER) {
               double e = (double)lv5.getX() + 0.5 - (double)(j % lv4.size()) * 3.0;
               double f = (double)lv5.getY() + 0.5 + (double)(k % i) * 3.0;
               double g = (double)lv5.getZ() + 0.5 + (double)(j / lv4.size() * 10);
               ArmorStandEntity lv8 = new ArmorStandEntity(lv, e, f, g);
               lv8.setYaw(180.0F);
               lv8.setNoGravity(true);
               EquipmentSlot[] var25 = EquipmentSlot.values();
               int var26 = var25.length;

               for(int var27 = 0; var27 < var26; ++var27) {
                  EquipmentSlot lv9 = var25[var27];
                  Item lv10 = (Item)ARMOR_PIECES.get(Pair.of(lv7, lv9));
                  if (lv10 != null) {
                     ItemStack lv11 = new ItemStack(lv10);
                     ArmorTrim.apply(lv.getRegistryManager(), lv11, lv6);
                     lv8.equipStack(lv9, lv11);
                     if (lv10 instanceof ArmorItem) {
                        ArmorItem lv12 = (ArmorItem)lv10;
                        if (lv12.getMaterial() == ArmorMaterials.TURTLE) {
                           lv8.setCustomName(((ArmorTrimPattern)lv6.getPattern().value()).getDescription(lv6.getMaterial()).copy().append(" ").append(((ArmorTrimMaterial)lv6.getMaterial().value()).description()));
                           lv8.setCustomNameVisible(true);
                           continue;
                        }
                     }

                     lv8.setInvisible(true);
                  }
               }

               lv.spawnEntity(lv8);
               ++k;
            }
         }
      }

      source.sendFeedback(Text.literal("Armorstands with trimmed armor spawned around you"), true);
      return 1;
   }

   static {
      PATTERNS = List.of(ArmorTrimPatterns.SENTRY, ArmorTrimPatterns.DUNE, ArmorTrimPatterns.COAST, ArmorTrimPatterns.WILD, ArmorTrimPatterns.WARD, ArmorTrimPatterns.EYE, ArmorTrimPatterns.VEX, ArmorTrimPatterns.TIDE, ArmorTrimPatterns.SNOUT, ArmorTrimPatterns.RIB, ArmorTrimPatterns.SPIRE, ArmorTrimPatterns.WAYFINDER, ArmorTrimPatterns.SHAPER, ArmorTrimPatterns.SILENCE, ArmorTrimPatterns.RAISER, ArmorTrimPatterns.HOST);
      MATERIALS = List.of(ArmorTrimMaterials.QUARTZ, ArmorTrimMaterials.IRON, ArmorTrimMaterials.NETHERITE, ArmorTrimMaterials.REDSTONE, ArmorTrimMaterials.COPPER, ArmorTrimMaterials.GOLD, ArmorTrimMaterials.EMERALD, ArmorTrimMaterials.DIAMOND, ArmorTrimMaterials.LAPIS, ArmorTrimMaterials.AMETHYST);
      PATTERN_INDEX_GETTER = Util.lastIndexGetter(PATTERNS);
      MATERIAL_INDEX_GETTER = Util.lastIndexGetter(MATERIALS);
   }
}
