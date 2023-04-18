package net.minecraft.item.trim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public record ArmorTrimPattern(Identifier assetId, RegistryEntry templateItem, Text description) {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Identifier.CODEC.fieldOf("asset_id").forGetter(ArmorTrimPattern::assetId), RegistryFixedCodec.of(RegistryKeys.ITEM).fieldOf("template_item").forGetter(ArmorTrimPattern::templateItem), Codecs.TEXT.fieldOf("description").forGetter(ArmorTrimPattern::description)).apply(instance, ArmorTrimPattern::new);
   });
   public static final Codec ENTRY_CODEC;

   public ArmorTrimPattern(Identifier arg, RegistryEntry arg2, Text arg3) {
      this.assetId = arg;
      this.templateItem = arg2;
      this.description = arg3;
   }

   public Text getDescription(RegistryEntry material) {
      return this.description.copy().fillStyle(((ArmorTrimMaterial)material.value()).description().getStyle());
   }

   public Identifier assetId() {
      return this.assetId;
   }

   public RegistryEntry templateItem() {
      return this.templateItem;
   }

   public Text description() {
      return this.description;
   }

   static {
      ENTRY_CODEC = RegistryElementCodec.of(RegistryKeys.TRIM_PATTERN, CODEC);
   }
}
