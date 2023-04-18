package net.minecraft.client.texture.atlas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class DirectoryAtlasSource implements AtlasSource {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.STRING.fieldOf("source").forGetter((arg) -> {
         return arg.source;
      }), Codec.STRING.fieldOf("prefix").forGetter((arg) -> {
         return arg.prefix;
      })).apply(instance, DirectoryAtlasSource::new);
   });
   private final String source;
   private final String prefix;

   public DirectoryAtlasSource(String source, String prefix) {
      this.source = source;
      this.prefix = prefix;
   }

   public void load(ResourceManager resourceManager, AtlasSource.SpriteRegions regions) {
      ResourceFinder lv = new ResourceFinder("textures/" + this.source, ".png");
      lv.findResources(resourceManager).forEach((arg3, resource) -> {
         Identifier lvx = lv.toResourceId(arg3).withPrefixedPath(this.prefix);
         regions.add(lvx, resource);
      });
   }

   public AtlasSourceType getType() {
      return AtlasSourceManager.DIRECTORY;
   }
}
