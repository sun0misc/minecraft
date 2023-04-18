package net.minecraft.client.font;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class UnicodeTextureFont implements Font {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int field_32232 = 256;
   private static final int field_32233 = 256;
   private static final int field_32234 = 256;
   private static final byte field_37905 = 0;
   private static final int field_40410 = 65536;
   private final byte[] sizes;
   private final FontImage[] fontImages = new FontImage[256];

   public UnicodeTextureFont(ResourceManager resourceManager, byte[] sizes, String template) {
      this.sizes = sizes;
      Set set = new HashSet();

      for(int i = 0; i < 256; ++i) {
         int j = i * 256;
         set.add(getImageId(template, j));
      }

      String string2 = getCommonPath(set);
      Map map = new HashMap();
      Objects.requireNonNull(set);
      resourceManager.findResources(string2, set::contains).forEach((id, resource) -> {
         map.put(id, CompletableFuture.supplyAsync(() -> {
            try {
               InputStream inputStream = resource.getInputStream();

               NativeImage var3;
               try {
                  var3 = NativeImage.read(NativeImage.Format.RGBA, inputStream);
               } catch (Throwable var6) {
                  if (inputStream != null) {
                     try {
                        inputStream.close();
                     } catch (Throwable var5) {
                        var6.addSuppressed(var5);
                     }
                  }

                  throw var6;
               }

               if (inputStream != null) {
                  inputStream.close();
               }

               return var3;
            } catch (IOException var7) {
               LOGGER.error("Failed to read resource {} from pack {}", id, resource.getResourcePackName());
               return null;
            }
         }, Util.getMainWorkerExecutor()));
      });
      List list = new ArrayList(256);

      for(int k = 0; k < 256; ++k) {
         int l = k * 256;
         Identifier lv = getImageId(template, l);
         CompletableFuture completableFuture = (CompletableFuture)map.get(lv);
         if (completableFuture != null) {
            list.add(completableFuture.thenAcceptAsync((image) -> {
               if (image != null) {
                  if (image.getWidth() == 256 && image.getHeight() == 256) {
                     for(int kx = 0; kx < 256; ++kx) {
                        byte b = sizes[l + kx];
                        if (b != 0 && getStart(b) > getEnd(b)) {
                           sizes[l + kx] = 0;
                        }
                     }

                     this.fontImages[k] = new FontImage(sizes, image);
                  } else {
                     image.close();
                     Arrays.fill(sizes, l, l + 256, (byte)0);
                  }

               }
            }, Util.getMainWorkerExecutor()));
         }
      }

      CompletableFuture.allOf((CompletableFuture[])list.toArray((ix) -> {
         return new CompletableFuture[ix];
      })).join();
   }

   private static String getCommonPath(Set ids) {
      String string = StringUtils.getCommonPrefix((String[])ids.stream().map(Identifier::getPath).toArray((ix) -> {
         return new String[ix];
      }));
      int i = string.lastIndexOf("/");
      return i == -1 ? "" : string.substring(0, i);
   }

   public void close() {
      FontImage[] var1 = this.fontImages;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         FontImage lv = var1[var3];
         if (lv != null) {
            lv.close();
         }
      }

   }

   private static Identifier getImageId(String template, int codePoint) {
      String string2 = String.format(Locale.ROOT, "%02x", codePoint / 256);
      Identifier lv = new Identifier(String.format(Locale.ROOT, template, string2));
      return lv.withPrefixedPath("textures/");
   }

   @Nullable
   public Glyph getGlyph(int codePoint) {
      if (codePoint >= 0 && codePoint < this.sizes.length) {
         int j = codePoint / 256;
         FontImage lv = this.fontImages[j];
         return lv != null ? lv.getGlyph(codePoint) : null;
      } else {
         return null;
      }
   }

   public IntSet getProvidedGlyphs() {
      IntSet intSet = new IntOpenHashSet();

      for(int i = 0; i < this.sizes.length; ++i) {
         if (this.sizes[i] != 0) {
            intSet.add(i);
         }
      }

      return intSet;
   }

   static int getStart(byte size) {
      return size >> 4 & 15;
   }

   static int getEnd(byte size) {
      return (size & 15) + 1;
   }

   @Environment(EnvType.CLIENT)
   static class FontImage implements AutoCloseable {
      private final byte[] sizes;
      private final NativeImage image;

      FontImage(byte[] sizes, NativeImage image) {
         this.sizes = sizes;
         this.image = image;
      }

      public void close() {
         this.image.close();
      }

      @Nullable
      public Glyph getGlyph(int codePoint) {
         byte b = this.sizes[codePoint];
         if (b != 0) {
            int j = UnicodeTextureFont.getStart(b);
            return new UnicodeTextureGlyph(codePoint % 16 * 16 + j, (codePoint & 255) / 16 * 16, UnicodeTextureFont.getEnd(b) - j, 16, this.image);
         } else {
            return null;
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private static record UnicodeTextureGlyph(int unpackSkipPixels, int unpackSkipRows, int width, int height, NativeImage image) implements Glyph {
      final int unpackSkipPixels;
      final int unpackSkipRows;
      final int width;
      final int height;
      final NativeImage image;

      UnicodeTextureGlyph(int i, int j, int k, int l, NativeImage image) {
         this.unpackSkipPixels = i;
         this.unpackSkipRows = j;
         this.width = k;
         this.height = l;
         this.image = image;
      }

      public float getAdvance() {
         return (float)(this.width / 2 + 1);
      }

      public float getShadowOffset() {
         return 0.5F;
      }

      public float getBoldOffset() {
         return 0.5F;
      }

      public GlyphRenderer bake(Function function) {
         return (GlyphRenderer)function.apply(new RenderableGlyph() {
            public float getOversample() {
               return 2.0F;
            }

            public int getWidth() {
               return UnicodeTextureGlyph.this.width;
            }

            public int getHeight() {
               return UnicodeTextureGlyph.this.height;
            }

            public void upload(int x, int y) {
               UnicodeTextureGlyph.this.image.upload(0, x, y, UnicodeTextureGlyph.this.unpackSkipPixels, UnicodeTextureGlyph.this.unpackSkipRows, UnicodeTextureGlyph.this.width, UnicodeTextureGlyph.this.height, false, false);
            }

            public boolean hasColor() {
               return UnicodeTextureGlyph.this.image.getFormat().getChannelCount() > 1;
            }
         });
      }

      public int unpackSkipPixels() {
         return this.unpackSkipPixels;
      }

      public int unpackSkipRows() {
         return this.unpackSkipRows;
      }

      public int width() {
         return this.width;
      }

      public int height() {
         return this.height;
      }

      public NativeImage image() {
         return this.image;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Loader implements FontLoader {
      private final Identifier sizes;
      private final String template;

      public Loader(Identifier sizes, String template) {
         this.sizes = sizes;
         this.template = template;
      }

      public static FontLoader fromJson(JsonObject json) {
         return new Loader(new Identifier(JsonHelper.getString(json, "sizes")), getLegacyUnicodeTemplate(json));
      }

      private static String getLegacyUnicodeTemplate(JsonObject json) {
         String string = JsonHelper.getString(json, "template");

         try {
            String.format(Locale.ROOT, string, "");
            return string;
         } catch (IllegalFormatException var3) {
            throw new JsonParseException("Invalid legacy unicode template supplied, expected single '%s': " + string);
         }
      }

      @Nullable
      public Font load(ResourceManager manager) {
         try {
            InputStream inputStream = MinecraftClient.getInstance().getResourceManager().open(this.sizes);

            UnicodeTextureFont var4;
            try {
               byte[] bs = inputStream.readNBytes(65536);
               var4 = new UnicodeTextureFont(manager, bs, this.template);
            } catch (Throwable var6) {
               if (inputStream != null) {
                  try {
                     inputStream.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (inputStream != null) {
               inputStream.close();
            }

            return var4;
         } catch (IOException var7) {
            UnicodeTextureFont.LOGGER.error("Cannot load {}, unicode glyphs will not render correctly", this.sizes);
            return null;
         }
      }
   }
}
