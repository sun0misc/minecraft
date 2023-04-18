package net.minecraft.client.font;

import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;

@Environment(EnvType.CLIENT)
public enum BuiltinEmptyGlyph implements Glyph {
   WHITE(() -> {
      return createRectImage(5, 8, (x, y) -> {
         return -1;
      });
   }),
   MISSING(() -> {
      int i = true;
      int j = true;
      return createRectImage(5, 8, (x, y) -> {
         boolean bl = x == 0 || x + 1 == 5 || y == 0 || y + 1 == 8;
         return bl ? -1 : 0;
      });
   });

   final NativeImage image;

   private static NativeImage createRectImage(int width, int height, ColorSupplier colorSupplier) {
      NativeImage lv = new NativeImage(NativeImage.Format.RGBA, width, height, false);

      for(int k = 0; k < height; ++k) {
         for(int l = 0; l < width; ++l) {
            lv.setColor(l, k, colorSupplier.getColor(l, k));
         }
      }

      lv.untrack();
      return lv;
   }

   private BuiltinEmptyGlyph(Supplier imageSupplier) {
      this.image = (NativeImage)imageSupplier.get();
   }

   public float getAdvance() {
      return (float)(this.image.getWidth() + 1);
   }

   public GlyphRenderer bake(Function function) {
      return (GlyphRenderer)function.apply(new RenderableGlyph() {
         public int getWidth() {
            return BuiltinEmptyGlyph.this.image.getWidth();
         }

         public int getHeight() {
            return BuiltinEmptyGlyph.this.image.getHeight();
         }

         public float getOversample() {
            return 1.0F;
         }

         public void upload(int x, int y) {
            BuiltinEmptyGlyph.this.image.upload(0, x, y, false);
         }

         public boolean hasColor() {
            return true;
         }
      });
   }

   // $FF: synthetic method
   private static BuiltinEmptyGlyph[] method_41838() {
      return new BuiltinEmptyGlyph[]{WHITE, MISSING};
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   interface ColorSupplier {
      int getColor(int x, int y);
   }
}
