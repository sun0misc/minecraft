package net.minecraft.client.gui.widget;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class TextWidget extends AbstractTextWidget {
   private float horizontalAlignment;

   public TextWidget(Text message, TextRenderer textRenderer) {
      int var10003 = textRenderer.getWidth(message.asOrderedText());
      Objects.requireNonNull(textRenderer);
      this(0, 0, var10003, 9, message, textRenderer);
   }

   public TextWidget(int width, int height, Text message, TextRenderer textRenderer) {
      this(0, 0, width, height, message, textRenderer);
   }

   public TextWidget(int x, int y, int width, int height, Text message, TextRenderer textRenderer) {
      super(x, y, width, height, message, textRenderer);
      this.horizontalAlignment = 0.5F;
      this.active = false;
   }

   public TextWidget setTextColor(int textColor) {
      super.setTextColor(textColor);
      return this;
   }

   private TextWidget align(float horizontalAlignment) {
      this.horizontalAlignment = horizontalAlignment;
      return this;
   }

   public TextWidget alignLeft() {
      return this.align(0.0F);
   }

   public TextWidget alignCenter() {
      return this.align(0.5F);
   }

   public TextWidget alignRight() {
      return this.align(1.0F);
   }

   public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      Text lv = this.getMessage();
      TextRenderer lv2 = this.getTextRenderer();
      int k = this.getX() + Math.round(this.horizontalAlignment * (float)(this.getWidth() - lv2.getWidth((StringVisitable)lv)));
      int var10000 = this.getY();
      int var10001 = this.getHeight();
      Objects.requireNonNull(lv2);
      int l = var10000 + (var10001 - 9) / 2;
      drawTextWithShadow(matrices, lv2, lv, k, l, this.getTextColor());
   }

   // $FF: synthetic method
   public AbstractTextWidget setTextColor(int textColor) {
      return this.setTextColor(textColor);
   }
}
