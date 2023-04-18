package net.minecraft.item.trim;

import java.util.Map;
import java.util.Optional;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class ArmorTrimMaterials {
   public static final RegistryKey QUARTZ = of("quartz");
   public static final RegistryKey IRON = of("iron");
   public static final RegistryKey NETHERITE = of("netherite");
   public static final RegistryKey REDSTONE = of("redstone");
   public static final RegistryKey COPPER = of("copper");
   public static final RegistryKey GOLD = of("gold");
   public static final RegistryKey EMERALD = of("emerald");
   public static final RegistryKey DIAMOND = of("diamond");
   public static final RegistryKey LAPIS = of("lapis");
   public static final RegistryKey AMETHYST = of("amethyst");

   public static void bootstrap(Registerable registry) {
      register(registry, QUARTZ, Items.QUARTZ, Style.EMPTY.withColor(14931140), 0.1F);
      register(registry, IRON, Items.IRON_INGOT, Style.EMPTY.withColor(15527148), 0.2F, Map.of(ArmorMaterials.IRON, "iron_darker"));
      register(registry, NETHERITE, Items.NETHERITE_INGOT, Style.EMPTY.withColor(6445145), 0.3F, Map.of(ArmorMaterials.NETHERITE, "netherite_darker"));
      register(registry, REDSTONE, Items.REDSTONE, Style.EMPTY.withColor(9901575), 0.4F);
      register(registry, COPPER, Items.COPPER_INGOT, Style.EMPTY.withColor(11823181), 0.5F);
      register(registry, GOLD, Items.GOLD_INGOT, Style.EMPTY.withColor(14594349), 0.6F, Map.of(ArmorMaterials.GOLD, "gold_darker"));
      register(registry, EMERALD, Items.EMERALD, Style.EMPTY.withColor(1155126), 0.7F);
      register(registry, DIAMOND, Items.DIAMOND, Style.EMPTY.withColor(7269586), 0.8F, Map.of(ArmorMaterials.DIAMOND, "diamond_darker"));
      register(registry, LAPIS, Items.LAPIS_LAZULI, Style.EMPTY.withColor(4288151), 0.9F);
      register(registry, AMETHYST, Items.AMETHYST_SHARD, Style.EMPTY.withColor(10116294), 1.0F);
   }

   public static Optional get(DynamicRegistryManager registryManager, ItemStack stack) {
      return registryManager.get(RegistryKeys.TRIM_MATERIAL).streamEntries().filter((recipe) -> {
         return stack.itemMatches(((ArmorTrimMaterial)recipe.value()).ingredient());
      }).findFirst();
   }

   private static void register(Registerable registry, RegistryKey key, Item ingredient, Style style, float itemModelIndex) {
      register(registry, key, ingredient, style, itemModelIndex, Map.of());
   }

   private static void register(Registerable registry, RegistryKey key, Item ingredient, Style style, float itemModelIndex, Map overrideArmorMaterials) {
      ArmorTrimMaterial lv = ArmorTrimMaterial.of(key.getValue().getPath(), ingredient, itemModelIndex, Text.translatable(Util.createTranslationKey("trim_material", key.getValue())).fillStyle(style), overrideArmorMaterials);
      registry.register(key, lv);
   }

   private static RegistryKey of(String id) {
      return RegistryKey.of(RegistryKeys.TRIM_MATERIAL, new Identifier(id));
   }
}
