package net.minecraft.client.texture.atlas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.metadata.BlockEntry;

@Environment(EnvType.CLIENT)
public class FilterAtlasSource implements AtlasSource {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(BlockEntry.CODEC.fieldOf("pattern").forGetter((arg) -> {
         return arg.pattern;
      })).apply(instance, FilterAtlasSource::new);
   });
   private final BlockEntry pattern;

   public FilterAtlasSource(BlockEntry pattern) {
      this.pattern = pattern;
   }

   public void load(ResourceManager resourceManager, AtlasSource.SpriteRegions regions) {
      regions.removeIf(this.pattern.getIdentifierPredicate());
   }

   public AtlasSourceType getType() {
      return AtlasSourceManager.FILTER;
   }
}
