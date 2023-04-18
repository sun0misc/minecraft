package net.minecraft.client.texture.atlas;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SingleAtlasSource implements AtlasSource {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Identifier.CODEC.fieldOf("resource").forGetter((arg) -> {
         return arg.resource;
      }), Identifier.CODEC.optionalFieldOf("sprite").forGetter((arg) -> {
         return arg.sprite;
      })).apply(instance, SingleAtlasSource::new);
   });
   private final Identifier resource;
   private final Optional sprite;

   public SingleAtlasSource(Identifier resource, Optional sprite) {
      this.resource = resource;
      this.sprite = sprite;
   }

   public void load(ResourceManager resourceManager, AtlasSource.SpriteRegions regions) {
      Identifier lv = RESOURCE_FINDER.toResourcePath(this.resource);
      Optional optional = resourceManager.getResource(lv);
      if (optional.isPresent()) {
         regions.add((Identifier)this.sprite.orElse(this.resource), (Resource)optional.get());
      } else {
         LOGGER.warn("Missing sprite: {}", lv);
      }

   }

   public AtlasSourceType getType() {
      return AtlasSourceManager.SINGLE;
   }
}
