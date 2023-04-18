package net.minecraft.client.texture;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.atlas.AtlasLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SpriteLoader {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Identifier id;
   private final int maxTextureSize;
   private final int width;
   private final int height;

   public SpriteLoader(Identifier id, int maxTextureSize, int width, int height) {
      this.id = id;
      this.maxTextureSize = maxTextureSize;
      this.width = width;
      this.height = height;
   }

   public static SpriteLoader fromAtlas(SpriteAtlasTexture atlasTexture) {
      return new SpriteLoader(atlasTexture.getId(), atlasTexture.getMaxTextureSize(), atlasTexture.getWidth(), atlasTexture.getHeight());
   }

   public StitchResult stitch(List sprites, int mipLevel, Executor executor) {
      int j = this.maxTextureSize;
      TextureStitcher lv = new TextureStitcher(j, j, mipLevel);
      int k = Integer.MAX_VALUE;
      int l = 1 << mipLevel;

      SpriteContents lv2;
      int m;
      for(Iterator var8 = sprites.iterator(); var8.hasNext(); lv.add(lv2)) {
         lv2 = (SpriteContents)var8.next();
         k = Math.min(k, Math.min(lv2.getWidth(), lv2.getHeight()));
         m = Math.min(Integer.lowestOneBit(lv2.getWidth()), Integer.lowestOneBit(lv2.getHeight()));
         if (m < l) {
            LOGGER.warn("Texture {} with size {}x{} limits mip level from {} to {}", new Object[]{lv2.getId(), lv2.getWidth(), lv2.getHeight(), MathHelper.floorLog2(l), MathHelper.floorLog2(m)});
            l = m;
         }
      }

      int n = Math.min(k, l);
      int o = MathHelper.floorLog2(n);
      if (o < mipLevel) {
         LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", new Object[]{this.id, mipLevel, o, n});
         m = o;
      } else {
         m = mipLevel;
      }

      try {
         lv.stitch();
      } catch (TextureStitcherCannotFitException var16) {
         CrashReport lv4 = CrashReport.create(var16, "Stitching");
         CrashReportSection lv5 = lv4.addElement("Stitcher");
         lv5.add("Sprites", var16.getSprites().stream().map((sprite) -> {
            return String.format(Locale.ROOT, "%s[%dx%d]", sprite.getId(), sprite.getWidth(), sprite.getHeight());
         }).collect(Collectors.joining(",")));
         lv5.add("Max Texture Size", (Object)j);
         throw new CrashException(lv4);
      }

      int p = Math.max(lv.getWidth(), this.width);
      int q = Math.max(lv.getHeight(), this.height);
      Map map = this.collectStitchedSprites(lv, p, q);
      Sprite lv6 = (Sprite)map.get(MissingSprite.getMissingSpriteId());
      CompletableFuture completableFuture;
      if (m > 0) {
         completableFuture = CompletableFuture.runAsync(() -> {
            map.values().forEach((sprite) -> {
               sprite.getContents().generateMipmaps(m);
            });
         }, executor);
      } else {
         completableFuture = CompletableFuture.completedFuture((Object)null);
      }

      return new StitchResult(p, q, m, lv6, map, completableFuture);
   }

   public static CompletableFuture loadAll(List sources, Executor executor) {
      List list2 = sources.stream().map((source) -> {
         return CompletableFuture.supplyAsync(source, executor);
      }).toList();
      return Util.combineSafe(list2).thenApply((sprites) -> {
         return sprites.stream().filter(Objects::nonNull).toList();
      });
   }

   public CompletableFuture load(ResourceManager resourceManager, Identifier path, int mipLevel, Executor executor) {
      return CompletableFuture.supplyAsync(() -> {
         return AtlasLoader.of(resourceManager, path).loadSources(resourceManager);
      }, executor).thenCompose((sources) -> {
         return loadAll(sources, executor);
      }).thenApply((sprites) -> {
         return this.stitch(sprites, mipLevel, executor);
      });
   }

   @Nullable
   public static SpriteContents load(Identifier id, Resource resource) {
      AnimationResourceMetadata lv;
      try {
         lv = (AnimationResourceMetadata)resource.getMetadata().decode(AnimationResourceMetadata.READER).orElse(AnimationResourceMetadata.EMPTY);
      } catch (Exception var8) {
         LOGGER.error("Unable to parse metadata from {}", id, var8);
         return null;
      }

      NativeImage lv2;
      try {
         InputStream inputStream = resource.getInputStream();

         try {
            lv2 = NativeImage.read(inputStream);
         } catch (Throwable var9) {
            if (inputStream != null) {
               try {
                  inputStream.close();
               } catch (Throwable var7) {
                  var9.addSuppressed(var7);
               }
            }

            throw var9;
         }

         if (inputStream != null) {
            inputStream.close();
         }
      } catch (IOException var10) {
         LOGGER.error("Using missing texture, unable to load {}", id, var10);
         return null;
      }

      SpriteDimensions lv3 = lv.getSize(lv2.getWidth(), lv2.getHeight());
      if (MathHelper.isMultipleOf(lv2.getWidth(), lv3.width()) && MathHelper.isMultipleOf(lv2.getHeight(), lv3.height())) {
         return new SpriteContents(id, lv3, lv2, lv);
      } else {
         LOGGER.error("Image {} size {},{} is not multiple of frame size {},{}", new Object[]{id, lv2.getWidth(), lv2.getHeight(), lv3.width(), lv3.height()});
         lv2.close();
         return null;
      }
   }

   private Map collectStitchedSprites(TextureStitcher stitcher, int atlasWidth, int atlasHeight) {
      Map map = new HashMap();
      stitcher.getStitchedSprites((info, x, y) -> {
         map.put(info.getId(), new Sprite(this.id, info, atlasWidth, atlasHeight, x, y));
      });
      return map;
   }

   @Environment(EnvType.CLIENT)
   public static record StitchResult(int width, int height, int mipLevel, Sprite missing, Map regions, CompletableFuture readyForUpload) {
      public StitchResult(int i, int j, int k, Sprite arg, Map map, CompletableFuture completableFuture) {
         this.width = i;
         this.height = j;
         this.mipLevel = k;
         this.missing = arg;
         this.regions = map;
         this.readyForUpload = completableFuture;
      }

      public CompletableFuture whenComplete() {
         return this.readyForUpload.thenApply((void_) -> {
            return this;
         });
      }

      public int width() {
         return this.width;
      }

      public int height() {
         return this.height;
      }

      public int mipLevel() {
         return this.mipLevel;
      }

      public Sprite missing() {
         return this.missing;
      }

      public Map regions() {
         return this.regions;
      }

      public CompletableFuture readyForUpload() {
         return this.readyForUpload;
      }
   }
}
