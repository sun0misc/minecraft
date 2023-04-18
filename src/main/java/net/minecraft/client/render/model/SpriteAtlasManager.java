package net.minecraft.client.render.model;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SpriteAtlasManager implements AutoCloseable {
   private final Map atlases;

   public SpriteAtlasManager(Map loaders, TextureManager textureManager) {
      this.atlases = (Map)loaders.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry) -> {
         SpriteAtlasTexture lv = new SpriteAtlasTexture((Identifier)entry.getKey());
         textureManager.registerTexture((Identifier)entry.getKey(), lv);
         return new Atlas(lv, (Identifier)entry.getValue());
      }));
   }

   public SpriteAtlasTexture getAtlas(Identifier id) {
      return ((Atlas)this.atlases.get(id)).atlas();
   }

   public void close() {
      this.atlases.values().forEach(Atlas::close);
      this.atlases.clear();
   }

   public Map reload(ResourceManager resourceManager, int mipmapLevels, Executor executor) {
      return (Map)this.atlases.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry) -> {
         Atlas lv = (Atlas)entry.getValue();
         return SpriteLoader.fromAtlas(lv.atlas).load(resourceManager, lv.atlasInfoLocation, mipmapLevels, executor).thenApply((stitchResult) -> {
            return new AtlasPreparation(lv.atlas, stitchResult);
         });
      }));
   }

   @Environment(EnvType.CLIENT)
   private static record Atlas(SpriteAtlasTexture atlas, Identifier atlasInfoLocation) implements AutoCloseable {
      final SpriteAtlasTexture atlas;
      final Identifier atlasInfoLocation;

      Atlas(SpriteAtlasTexture arg, Identifier arg2) {
         this.atlas = arg;
         this.atlasInfoLocation = arg2;
      }

      public void close() {
         this.atlas.clear();
      }

      public SpriteAtlasTexture atlas() {
         return this.atlas;
      }

      public Identifier atlasInfoLocation() {
         return this.atlasInfoLocation;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class AtlasPreparation {
      private final SpriteAtlasTexture atlasTexture;
      private final SpriteLoader.StitchResult stitchResult;

      public AtlasPreparation(SpriteAtlasTexture atlasTexture, SpriteLoader.StitchResult stitchResult) {
         this.atlasTexture = atlasTexture;
         this.stitchResult = stitchResult;
      }

      @Nullable
      public Sprite getSprite(Identifier id) {
         return (Sprite)this.stitchResult.regions().get(id);
      }

      public Sprite getMissingSprite() {
         return this.stitchResult.missing();
      }

      public CompletableFuture whenComplete() {
         return this.stitchResult.readyForUpload();
      }

      public void upload() {
         this.atlasTexture.upload(this.stitchResult);
      }
   }
}
