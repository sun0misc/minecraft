package net.minecraft.client.font;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class FontStorage implements AutoCloseable {
   private static final Random RANDOM = Random.create();
   private static final float MAX_ADVANCE = 32.0F;
   private final TextureManager textureManager;
   private final Identifier id;
   private GlyphRenderer blankGlyphRenderer;
   private GlyphRenderer whiteRectangleGlyphRenderer;
   private final List fonts = Lists.newArrayList();
   private final Int2ObjectMap glyphRendererCache = new Int2ObjectOpenHashMap();
   private final Int2ObjectMap glyphCache = new Int2ObjectOpenHashMap();
   private final Int2ObjectMap charactersByWidth = new Int2ObjectOpenHashMap();
   private final List glyphAtlases = Lists.newArrayList();

   public FontStorage(TextureManager textureManager, Identifier id) {
      this.textureManager = textureManager;
      this.id = id;
   }

   public void setFonts(List fonts) {
      this.closeFonts();
      this.closeGlyphAtlases();
      this.glyphRendererCache.clear();
      this.glyphCache.clear();
      this.charactersByWidth.clear();
      this.blankGlyphRenderer = BuiltinEmptyGlyph.MISSING.bake(this::getGlyphRenderer);
      this.whiteRectangleGlyphRenderer = BuiltinEmptyGlyph.WHITE.bake(this::getGlyphRenderer);
      IntSet intSet = new IntOpenHashSet();
      Iterator var3 = fonts.iterator();

      while(var3.hasNext()) {
         Font lv = (Font)var3.next();
         intSet.addAll(lv.getProvidedGlyphs());
      }

      Set set = Sets.newHashSet();
      intSet.forEach((codePoint) -> {
         Iterator var4 = fonts.iterator();

         while(var4.hasNext()) {
            Font lv = (Font)var4.next();
            Glyph lv2 = lv.getGlyph(codePoint);
            if (lv2 != null) {
               set.add(lv);
               if (lv2 != BuiltinEmptyGlyph.MISSING) {
                  ((IntList)this.charactersByWidth.computeIfAbsent(MathHelper.ceil(lv2.getAdvance(false)), (advance) -> {
                     return new IntArrayList();
                  })).add(codePoint);
               }
               break;
            }
         }

      });
      Stream var10000 = fonts.stream();
      Objects.requireNonNull(set);
      var10000 = var10000.filter(set::contains);
      List var10001 = this.fonts;
      Objects.requireNonNull(var10001);
      var10000.forEach(var10001::add);
   }

   public void close() {
      this.closeFonts();
      this.closeGlyphAtlases();
   }

   private void closeFonts() {
      Iterator var1 = this.fonts.iterator();

      while(var1.hasNext()) {
         Font lv = (Font)var1.next();
         lv.close();
      }

      this.fonts.clear();
   }

   private void closeGlyphAtlases() {
      Iterator var1 = this.glyphAtlases.iterator();

      while(var1.hasNext()) {
         GlyphAtlasTexture lv = (GlyphAtlasTexture)var1.next();
         lv.close();
      }

      this.glyphAtlases.clear();
   }

   private static boolean isAdvanceInvalid(Glyph glyph) {
      float f = glyph.getAdvance(false);
      if (!(f < 0.0F) && !(f > 32.0F)) {
         float g = glyph.getAdvance(true);
         return g < 0.0F || g > 32.0F;
      } else {
         return true;
      }
   }

   private GlyphPair findGlyph(int codePoint) {
      Glyph lv = null;
      Iterator var3 = this.fonts.iterator();

      while(var3.hasNext()) {
         Font lv2 = (Font)var3.next();
         Glyph lv3 = lv2.getGlyph(codePoint);
         if (lv3 != null) {
            if (lv == null) {
               lv = lv3;
            }

            if (!isAdvanceInvalid(lv3)) {
               return new GlyphPair(lv, lv3);
            }
         }
      }

      if (lv != null) {
         return new GlyphPair(lv, BuiltinEmptyGlyph.MISSING);
      } else {
         return FontStorage.GlyphPair.MISSING;
      }
   }

   public Glyph getGlyph(int codePoint, boolean validateAdvance) {
      return ((GlyphPair)this.glyphCache.computeIfAbsent(codePoint, this::findGlyph)).getGlyph(validateAdvance);
   }

   private GlyphRenderer findGlyphRenderer(int codePoint) {
      Iterator var2 = this.fonts.iterator();

      Glyph lv2;
      do {
         if (!var2.hasNext()) {
            return this.blankGlyphRenderer;
         }

         Font lv = (Font)var2.next();
         lv2 = lv.getGlyph(codePoint);
      } while(lv2 == null);

      return lv2.bake(this::getGlyphRenderer);
   }

   public GlyphRenderer getGlyphRenderer(int codePoint) {
      return (GlyphRenderer)this.glyphRendererCache.computeIfAbsent(codePoint, this::findGlyphRenderer);
   }

   private GlyphRenderer getGlyphRenderer(RenderableGlyph c) {
      Iterator var2 = this.glyphAtlases.iterator();

      GlyphRenderer lv2;
      do {
         if (!var2.hasNext()) {
            GlyphAtlasTexture lv3 = new GlyphAtlasTexture(this.id.withPath((string) -> {
               return string + "/" + this.glyphAtlases.size();
            }), c.hasColor());
            this.glyphAtlases.add(lv3);
            this.textureManager.registerTexture(lv3.getId(), lv3);
            GlyphRenderer lv4 = lv3.getGlyphRenderer(c);
            return lv4 == null ? this.blankGlyphRenderer : lv4;
         }

         GlyphAtlasTexture lv = (GlyphAtlasTexture)var2.next();
         lv2 = lv.getGlyphRenderer(c);
      } while(lv2 == null);

      return lv2;
   }

   public GlyphRenderer getObfuscatedGlyphRenderer(Glyph glyph) {
      IntList intList = (IntList)this.charactersByWidth.get(MathHelper.ceil(glyph.getAdvance(false)));
      return intList != null && !intList.isEmpty() ? this.getGlyphRenderer(intList.getInt(RANDOM.nextInt(intList.size()))) : this.blankGlyphRenderer;
   }

   public GlyphRenderer getRectangleRenderer() {
      return this.whiteRectangleGlyphRenderer;
   }

   @Environment(EnvType.CLIENT)
   private static record GlyphPair(Glyph glyph, Glyph advanceValidatedGlyph) {
      static final GlyphPair MISSING;

      GlyphPair(Glyph arg, Glyph arg2) {
         this.glyph = arg;
         this.advanceValidatedGlyph = arg2;
      }

      Glyph getGlyph(boolean validateAdvance) {
         return validateAdvance ? this.advanceValidatedGlyph : this.glyph;
      }

      public Glyph glyph() {
         return this.glyph;
      }

      public Glyph advanceValidatedGlyph() {
         return this.advanceValidatedGlyph;
      }

      static {
         MISSING = new GlyphPair(BuiltinEmptyGlyph.MISSING, BuiltinEmptyGlyph.MISSING);
      }
   }
}
