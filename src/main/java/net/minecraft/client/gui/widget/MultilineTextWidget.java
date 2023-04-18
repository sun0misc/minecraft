package net.minecraft.client.gui.widget;

import java.util.Objects;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.CachedMapper;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class MultilineTextWidget extends AbstractTextWidget {
   private OptionalInt maxWidth;
   private OptionalInt maxRows;
   private final CachedMapper cacheKeyToText;
   private boolean centered;

   public MultilineTextWidget(Text message, TextRenderer textRenderer) {
      this(0, 0, message, textRenderer);
   }

   public MultilineTextWidget(int x, int y, Text message, TextRenderer textRenderer) {
      super(x, y, 0, 0, message, textRenderer);
      this.maxWidth = OptionalInt.empty();
      this.maxRows = OptionalInt.empty();
      this.centered = false;
      this.cacheKeyToText = Util.cachedMapper((cacheKey) -> {
         return cacheKey.maxRows.isPresent() ? MultilineText.create(textRenderer, cacheKey.message, cacheKey.maxWidth, cacheKey.maxRows.getAsInt()) : MultilineText.create(textRenderer, cacheKey.message, cacheKey.maxWidth);
      });
      this.active = false;
   }

   public MultilineTextWidget setTextColor(int i) {
      super.setTextColor(i);
      return this;
   }

   public MultilineTextWidget setMaxWidth(int maxWidth) {
      this.maxWidth = OptionalInt.of(maxWidth);
      return this;
   }

   public MultilineTextWidget setMaxRows(int maxRows) {
      this.maxRows = OptionalInt.of(maxRows);
      return this;
   }

   public MultilineTextWidget setCentered(boolean centered) {
      this.centered = centered;
      return this;
   }

   public int getWidth() {
      return ((MultilineText)this.cacheKeyToText.map(this.getCacheKey())).getMaxWidth();
   }

   public int getHeight() {
      int var10000 = ((MultilineText)this.cacheKeyToText.map(this.getCacheKey())).count();
      Objects.requireNonNull(this.getTextRenderer());
      return var10000 * 9;
   }

   public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      MultilineText lv = (MultilineText)this.cacheKeyToText.map(this.getCacheKey());
      int k = this.getX();
      int l = this.getY();
      Objects.requireNonNull(this.getTextRenderer());
      int m = 9;
      int n = this.getTextColor();
      if (this.centered) {
         lv.drawCenterWithShadow(matrices, k + this.getWidth() / 2, l, m, n);
      } else {
         lv.drawWithShadow(matrices, k, l, m, n);
      }

   }

   private CacheKey getCacheKey() {
      return new CacheKey(this.getMessage(), this.maxWidth.orElse(Integer.MAX_VALUE), this.maxRows);
   }

   // $FF: synthetic method
   public AbstractTextWidget setTextColor(int textColor) {
      return this.setTextColor(textColor);
   }

   @Environment(EnvType.CLIENT)
   private static record CacheKey(Text message, int maxWidth, OptionalInt maxRows) {
      final Text message;
      final int maxWidth;
      final OptionalInt maxRows;

      CacheKey(Text arg, int i, OptionalInt optionalInt) {
         this.message = arg;
         this.maxWidth = i;
         this.maxRows = optionalInt;
      }

      public Text message() {
         return this.message;
      }

      public int maxWidth() {
         return this.maxWidth;
      }

      public OptionalInt maxRows() {
         return this.maxRows;
      }
   }
}
