package net.minecraft.client.font;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class BitmapFont implements Font {
   static final Logger LOGGER = LogUtils.getLogger();
   private final NativeImage image;
   private final Int2ObjectMap glyphs;

   BitmapFont(NativeImage image, Int2ObjectMap glyphs) {
      this.image = image;
      this.glyphs = glyphs;
   }

   public void close() {
      this.image.close();
   }

   @Nullable
   public Glyph getGlyph(int codePoint) {
      return (Glyph)this.glyphs.get(codePoint);
   }

   public IntSet getProvidedGlyphs() {
      return IntSets.unmodifiable(this.glyphs.keySet());
   }

   @Environment(EnvType.CLIENT)
   private static record BitmapFontGlyph(float scaleFactor, NativeImage image, int x, int y, int width, int height, int advance, int ascent) implements Glyph {
      final float scaleFactor;
      final NativeImage image;
      final int x;
      final int y;
      final int width;
      final int height;
      final int ascent;

      BitmapFontGlyph(float scaleFactor, NativeImage image, int x, int y, int width, int height, int advance, int ascent) {
         this.scaleFactor = scaleFactor;
         this.image = image;
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.advance = advance;
         this.ascent = ascent;
      }

      public float getAdvance() {
         return (float)this.advance;
      }

      public GlyphRenderer bake(Function function) {
         return (GlyphRenderer)function.apply(new RenderableGlyph() {
            public float getOversample() {
               return 1.0F / BitmapFontGlyph.this.scaleFactor;
            }

            public int getWidth() {
               return BitmapFontGlyph.this.width;
            }

            public int getHeight() {
               return BitmapFontGlyph.this.height;
            }

            public float getAscent() {
               return RenderableGlyph.super.getAscent() + 7.0F - (float)BitmapFontGlyph.this.ascent;
            }

            public void upload(int x, int y) {
               BitmapFontGlyph.this.image.upload(0, x, y, BitmapFontGlyph.this.x, BitmapFontGlyph.this.y, BitmapFontGlyph.this.width, BitmapFontGlyph.this.height, false, false);
            }

            public boolean hasColor() {
               return BitmapFontGlyph.this.image.getFormat().getChannelCount() > 1;
            }
         });
      }

      public float scaleFactor() {
         return this.scaleFactor;
      }

      public NativeImage image() {
         return this.image;
      }

      public int x() {
         return this.x;
      }

      public int y() {
         return this.y;
      }

      public int width() {
         return this.width;
      }

      public int height() {
         return this.height;
      }

      public int advance() {
         return this.advance;
      }

      public int ascent() {
         return this.ascent;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Loader implements FontLoader {
      private final Identifier filename;
      private final List chars;
      private final int height;
      private final int ascent;

      public Loader(Identifier id, int height, int ascent, List chars) {
         this.filename = id.withPrefixedPath("textures/");
         this.chars = chars;
         this.height = height;
         this.ascent = ascent;
      }

      public static Loader fromJson(JsonObject json) {
         int i = JsonHelper.getInt(json, "height", 8);
         int j = JsonHelper.getInt(json, "ascent");
         if (j > i) {
            throw new JsonParseException("Ascent " + j + " higher than height " + i);
         } else {
            List list = Lists.newArrayList();
            JsonArray jsonArray = JsonHelper.getArray(json, "chars");

            for(int k = 0; k < jsonArray.size(); ++k) {
               String string = JsonHelper.asString(jsonArray.get(k), "chars[" + k + "]");
               int[] is = string.codePoints().toArray();
               if (k > 0) {
                  int l = ((int[])list.get(0)).length;
                  if (is.length != l) {
                     throw new JsonParseException("Elements of chars have to be the same length (found: " + is.length + ", expected: " + l + "), pad with space or \\u0000");
                  }
               }

               list.add(is);
            }

            if (!list.isEmpty() && ((int[])list.get(0)).length != 0) {
               return new Loader(new Identifier(JsonHelper.getString(json, "file")), i, j, list);
            } else {
               throw new JsonParseException("Expected to find data in chars, found none.");
            }
         }
      }

      @Nullable
      public Font load(ResourceManager manager) {
         try {
            InputStream inputStream = manager.open(this.filename);

            BitmapFont var22;
            try {
               NativeImage lv = NativeImage.read(NativeImage.Format.RGBA, inputStream);
               int i = lv.getWidth();
               int j = lv.getHeight();
               int k = i / ((int[])this.chars.get(0)).length;
               int l = j / this.chars.size();
               float f = (float)this.height / (float)l;
               Int2ObjectMap int2ObjectMap = new Int2ObjectOpenHashMap();
               int m = 0;

               while(true) {
                  if (m >= this.chars.size()) {
                     var22 = new BitmapFont(lv, int2ObjectMap);
                     break;
                  }

                  int n = 0;
                  int[] var12 = (int[])this.chars.get(m);
                  int var13 = var12.length;

                  for(int var14 = 0; var14 < var13; ++var14) {
                     int o = var12[var14];
                     int p = n++;
                     if (o != 0) {
                        int q = this.findCharacterStartX(lv, k, l, p, m);
                        BitmapFontGlyph lv2 = (BitmapFontGlyph)int2ObjectMap.put(o, new BitmapFontGlyph(f, lv, p * k, m * l, k, l, (int)(0.5 + (double)((float)q * f)) + 1, this.ascent));
                        if (lv2 != null) {
                           BitmapFont.LOGGER.warn("Codepoint '{}' declared multiple times in {}", Integer.toHexString(o), this.filename);
                        }
                     }
                  }

                  ++m;
               }
            } catch (Throwable var20) {
               if (inputStream != null) {
                  try {
                     inputStream.close();
                  } catch (Throwable var19) {
                     var20.addSuppressed(var19);
                  }
               }

               throw var20;
            }

            if (inputStream != null) {
               inputStream.close();
            }

            return var22;
         } catch (IOException var21) {
            throw new RuntimeException(var21.getMessage());
         }
      }

      private int findCharacterStartX(NativeImage image, int characterWidth, int characterHeight, int charPosX, int charPosY) {
         int m;
         for(m = characterWidth - 1; m >= 0; --m) {
            int n = charPosX * characterWidth + m;

            for(int o = 0; o < characterHeight; ++o) {
               int p = charPosY * characterHeight + o;
               if (image.getOpacity(n, p) != 0) {
                  return m + 1;
               }
            }
         }

         return m + 1;
      }
   }
}
