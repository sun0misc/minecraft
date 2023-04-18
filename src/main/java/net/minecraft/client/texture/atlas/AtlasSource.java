package net.minecraft.client.texture.atlas;

import java.util.function.Predicate;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public interface AtlasSource {
   ResourceFinder RESOURCE_FINDER = new ResourceFinder("textures", ".png");

   void load(ResourceManager resourceManager, SpriteRegions regions);

   AtlasSourceType getType();

   @Environment(EnvType.CLIENT)
   public interface SpriteRegion extends Supplier {
      default void close() {
      }
   }

   @Environment(EnvType.CLIENT)
   public interface SpriteRegions {
      default void add(Identifier id, Resource resource) {
         this.add(id, () -> {
            return SpriteLoader.load(id, resource);
         });
      }

      void add(Identifier arg, SpriteRegion region);

      void removeIf(Predicate predicate);
   }
}
