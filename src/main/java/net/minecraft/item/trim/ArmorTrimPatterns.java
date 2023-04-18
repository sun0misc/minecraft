package net.minecraft.item.trim;

import java.util.Optional;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class ArmorTrimPatterns {
   public static final RegistryKey SENTRY = of("sentry");
   public static final RegistryKey DUNE = of("dune");
   public static final RegistryKey COAST = of("coast");
   public static final RegistryKey WILD = of("wild");
   public static final RegistryKey WARD = of("ward");
   public static final RegistryKey EYE = of("eye");
   public static final RegistryKey VEX = of("vex");
   public static final RegistryKey TIDE = of("tide");
   public static final RegistryKey SNOUT = of("snout");
   public static final RegistryKey RIB = of("rib");
   public static final RegistryKey SPIRE = of("spire");
   public static final RegistryKey WAYFINDER = of("wayfinder");
   public static final RegistryKey SHAPER = of("shaper");
   public static final RegistryKey SILENCE = of("silence");
   public static final RegistryKey RAISER = of("raiser");
   public static final RegistryKey HOST = of("host");

   public static void bootstrap(Registerable registry) {
      register(registry, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, SENTRY);
      register(registry, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, DUNE);
      register(registry, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, COAST);
      register(registry, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, WILD);
      register(registry, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, WARD);
      register(registry, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, EYE);
      register(registry, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, VEX);
      register(registry, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, TIDE);
      register(registry, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, SNOUT);
      register(registry, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, RIB);
      register(registry, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, SPIRE);
      register(registry, Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, WAYFINDER);
      register(registry, Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, SHAPER);
      register(registry, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, SILENCE);
      register(registry, Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, RAISER);
      register(registry, Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, HOST);
   }

   public static Optional get(DynamicRegistryManager registryManager, ItemStack stack) {
      return registryManager.get(RegistryKeys.TRIM_PATTERN).streamEntries().filter((pattern) -> {
         return stack.itemMatches(((ArmorTrimPattern)pattern.value()).templateItem());
      }).findFirst();
   }

   private static void register(Registerable registry, Item template, RegistryKey key) {
      ArmorTrimPattern lv = new ArmorTrimPattern(key.getValue(), Registries.ITEM.getEntry(template), Text.translatable(Util.createTranslationKey("trim_pattern", key.getValue())));
      registry.register(key, lv);
   }

   private static RegistryKey of(String id) {
      return RegistryKey.of(RegistryKeys.TRIM_PATTERN, new Identifier(id));
   }
}
