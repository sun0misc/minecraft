package net.minecraft.item.trim;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.slf4j.Logger;

public class ArmorTrim {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(ArmorTrimMaterial.ENTRY_CODEC.fieldOf("material").forGetter(ArmorTrim::getMaterial), ArmorTrimPattern.ENTRY_CODEC.fieldOf("pattern").forGetter(ArmorTrim::getPattern)).apply(instance, ArmorTrim::new);
   });
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String NBT_KEY = "Trim";
   private static final Text UPGRADE_TEXT;
   private final RegistryEntry material;
   private final RegistryEntry pattern;
   private final Function leggingsModelIdGetter;
   private final Function genericModelIdGetter;

   public ArmorTrim(RegistryEntry material, RegistryEntry pattern) {
      this.material = material;
      this.pattern = pattern;
      this.leggingsModelIdGetter = Util.memoize((armorMaterial) -> {
         Identifier lv = ((ArmorTrimPattern)pattern.value()).assetId();
         String string = this.getMaterialAssetNameFor(armorMaterial);
         return lv.withPath((path) -> {
            return "trims/models/armor/" + path + "_leggings_" + string;
         });
      });
      this.genericModelIdGetter = Util.memoize((armorMaterial) -> {
         Identifier lv = ((ArmorTrimPattern)pattern.value()).assetId();
         String string = this.getMaterialAssetNameFor(armorMaterial);
         return lv.withPath((path) -> {
            return "trims/models/armor/" + path + "_" + string;
         });
      });
   }

   private String getMaterialAssetNameFor(ArmorMaterial armorMaterial) {
      Map map = ((ArmorTrimMaterial)this.material.value()).overrideArmorMaterials();
      return armorMaterial instanceof ArmorMaterials && map.containsKey(armorMaterial) ? (String)map.get(armorMaterial) : ((ArmorTrimMaterial)this.material.value()).assetName();
   }

   public boolean equals(RegistryEntry pattern, RegistryEntry material) {
      return pattern == this.pattern && material == this.material;
   }

   public RegistryEntry getPattern() {
      return this.pattern;
   }

   public RegistryEntry getMaterial() {
      return this.material;
   }

   public Identifier getLeggingsModelId(ArmorMaterial armorMaterial) {
      return (Identifier)this.leggingsModelIdGetter.apply(armorMaterial);
   }

   public Identifier getGenericModelId(ArmorMaterial armorMaterial) {
      return (Identifier)this.genericModelIdGetter.apply(armorMaterial);
   }

   public boolean equals(Object o) {
      if (!(o instanceof ArmorTrim lv)) {
         return false;
      } else {
         return lv.pattern == this.pattern && lv.material == this.material;
      }
   }

   public static boolean apply(DynamicRegistryManager registryManager, ItemStack stack, ArmorTrim trim) {
      if (stack.isIn(ItemTags.TRIMMABLE_ARMOR)) {
         stack.getOrCreateNbt().put("Trim", (NbtElement)CODEC.encodeStart(RegistryOps.of(NbtOps.INSTANCE, (RegistryWrapper.WrapperLookup)registryManager), trim).result().orElseThrow());
         return true;
      } else {
         return false;
      }
   }

   public static Optional getTrim(DynamicRegistryManager registryManager, ItemStack stack) {
      if (stack.isIn(ItemTags.TRIMMABLE_ARMOR) && stack.getNbt() != null && stack.getNbt().contains("Trim")) {
         NbtCompound lv = stack.getSubNbt("Trim");
         DataResult var10000 = CODEC.parse(RegistryOps.of(NbtOps.INSTANCE, (RegistryWrapper.WrapperLookup)registryManager), lv);
         Logger var10001 = LOGGER;
         Objects.requireNonNull(var10001);
         ArmorTrim lv2 = (ArmorTrim)var10000.resultOrPartial(var10001::error).orElse((Object)null);
         return Optional.ofNullable(lv2);
      } else {
         return Optional.empty();
      }
   }

   public static void appendTooltip(ItemStack stack, DynamicRegistryManager registryManager, List tooltip) {
      Optional optional = getTrim(registryManager, stack);
      if (optional.isPresent()) {
         ArmorTrim lv = (ArmorTrim)optional.get();
         tooltip.add(UPGRADE_TEXT);
         tooltip.add(ScreenTexts.space().append(((ArmorTrimPattern)lv.getPattern().value()).getDescription(lv.getMaterial())));
         tooltip.add(ScreenTexts.space().append(((ArmorTrimMaterial)lv.getMaterial().value()).description()));
      }

   }

   static {
      UPGRADE_TEXT = Text.translatable(Util.createTranslationKey("item", new Identifier("smithing_template.upgrade"))).formatted(Formatting.GRAY);
   }
}
