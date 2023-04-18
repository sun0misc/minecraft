package net.minecraft.client.texture.atlas;

import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class PalettedPermutationsAtlasSource implements AtlasSource {
   static final Logger LOGGER = LogUtils.getLogger();
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.list(Identifier.CODEC).fieldOf("textures").forGetter((arg) -> {
         return arg.textures;
      }), Identifier.CODEC.fieldOf("palette_key").forGetter((arg) -> {
         return arg.paletteKey;
      }), Codec.unboundedMap(Codec.STRING, Identifier.CODEC).fieldOf("permutations").forGetter((arg) -> {
         return arg.permutations;
      })).apply(instance, PalettedPermutationsAtlasSource::new);
   });
   private final List textures;
   private final Map permutations;
   private final Identifier paletteKey;

   private PalettedPermutationsAtlasSource(List textures, Identifier paletteKey, Map permutations) {
      this.textures = textures;
      this.permutations = permutations;
      this.paletteKey = paletteKey;
   }

   public void load(ResourceManager resourceManager, AtlasSource.SpriteRegions regions) {
      Supplier supplier = Suppliers.memoize(() -> {
         return method_48486(resourceManager, this.paletteKey);
      });
      Map map = new HashMap();
      this.permutations.forEach((string, arg2) -> {
         map.put(string, Suppliers.memoize(() -> {
            return method_48492((int[])supplier.get(), method_48486(resourceManager, arg2));
         }));
      });
      Iterator var5 = this.textures.iterator();

      while(true) {
         while(var5.hasNext()) {
            Identifier lv = (Identifier)var5.next();
            Identifier lv2 = RESOURCE_FINDER.toResourcePath(lv);
            Optional optional = resourceManager.getResource(lv2);
            if (optional.isEmpty()) {
               LOGGER.warn("Unable to find texture {}", lv2);
            } else {
               Sprite lv3 = new Sprite(lv2, (Resource)optional.get(), map.size());
               Iterator var10 = map.entrySet().iterator();

               while(var10.hasNext()) {
                  Map.Entry entry = (Map.Entry)var10.next();
                  Identifier lv4 = lv.withSuffixedPath("_" + (String)entry.getKey());
                  regions.add(lv4, (AtlasSource.SpriteRegion)(new PalettedSpriteRegion(lv3, (Supplier)entry.getValue(), lv4)));
               }
            }
         }

         return;
      }
   }

   private static IntUnaryOperator method_48492(int[] is, int[] js) {
      if (js.length != is.length) {
         LOGGER.warn("Palette mapping has different sizes: {} and {}", is.length, js.length);
         throw new IllegalArgumentException();
      } else {
         Int2IntMap int2IntMap = new Int2IntOpenHashMap(js.length);

         for(int i = 0; i < is.length; ++i) {
            int j = is[i];
            if (ColorHelper.Abgr.getAlpha(j) != 0) {
               int2IntMap.put(ColorHelper.Abgr.getBgr(j), js[i]);
            }
         }

         return (ix) -> {
            int j = ColorHelper.Abgr.getAlpha(ix);
            if (j == 0) {
               return ix;
            } else {
               int k = ColorHelper.Abgr.getBgr(ix);
               int l = int2IntMap.getOrDefault(k, ColorHelper.Abgr.toOpaque(k));
               int m = ColorHelper.Abgr.getAlpha(l);
               return ColorHelper.Abgr.withAlpha(j * m / 255, l);
            }
         };
      }
   }

   public static int[] method_48486(ResourceManager arg, Identifier arg2) {
      Optional optional = arg.getResource(RESOURCE_FINDER.toResourcePath(arg2));
      if (optional.isEmpty()) {
         LOGGER.error("Failed to load palette image {}", arg2);
         throw new IllegalArgumentException();
      } else {
         try {
            InputStream inputStream = ((Resource)optional.get()).getInputStream();

            int[] var5;
            try {
               NativeImage lv = NativeImage.read(inputStream);

               try {
                  var5 = lv.copyPixelsRgba();
               } catch (Throwable var9) {
                  if (lv != null) {
                     try {
                        lv.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }
                  }

                  throw var9;
               }

               if (lv != null) {
                  lv.close();
               }
            } catch (Throwable var10) {
               if (inputStream != null) {
                  try {
                     inputStream.close();
                  } catch (Throwable var7) {
                     var10.addSuppressed(var7);
                  }
               }

               throw var10;
            }

            if (inputStream != null) {
               inputStream.close();
            }

            return var5;
         } catch (Exception var11) {
            LOGGER.error("Couldn't load texture {}", arg2, var11);
            throw new IllegalArgumentException();
         }
      }
   }

   public AtlasSourceType getType() {
      return AtlasSourceManager.PALETTED_PERMUTATIONS;
   }

   @Environment(EnvType.CLIENT)
   private static record PalettedSpriteRegion(Sprite baseImage, Supplier palette, Identifier permutationLocation) implements AtlasSource.SpriteRegion {
      PalettedSpriteRegion(Sprite arg, Supplier supplier, Identifier arg2) {
         this.baseImage = arg;
         this.palette = supplier;
         this.permutationLocation = arg2;
      }

      @Nullable
      public SpriteContents get() {
         SpriteContents var2;
         try {
            NativeImage lv = this.baseImage.read().apply((IntUnaryOperator)this.palette.get());
            var2 = new SpriteContents(this.permutationLocation, new SpriteDimensions(lv.getWidth(), lv.getHeight()), lv, AnimationResourceMetadata.EMPTY);
            return var2;
         } catch (IllegalArgumentException | IOException var6) {
            PalettedPermutationsAtlasSource.LOGGER.error("unable to apply palette to {}", this.permutationLocation, var6);
            var2 = null;
         } finally {
            this.baseImage.close();
         }

         return var2;
      }

      public void close() {
         this.baseImage.close();
      }

      public Sprite baseImage() {
         return this.baseImage;
      }

      public Supplier palette() {
         return this.palette;
      }

      public Identifier permutationLocation() {
         return this.permutationLocation;
      }

      // $FF: synthetic method
      @Nullable
      public Object get() {
         return this.get();
      }
   }
}
