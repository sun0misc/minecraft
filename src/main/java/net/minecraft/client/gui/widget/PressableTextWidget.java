package net.minecraft.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class PressableTextWidget extends ButtonWidget {
   private final TextRenderer textRenderer;
   private final Text text;
   private final Text hoverText;

   public PressableTextWidget(int x, int y, int width, int height, Text text, ButtonWidget.PressAction onPress, TextRenderer textRenderer) {
      super(x, y, width, height, text, onPress, DEFAULT_NARRATION_SUPPLIER);
      this.textRenderer = textRenderer;
      this.text = text;
      this.hoverText = Texts.setStyleIfAbsent(text.copy(), Style.EMPTY.withUnderline(true));
   }

   public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      Text lv = this.isSelected() ? this.hoverText : this.text;
      drawTextWithShadow(matrices, this.textRenderer, lv, this.getX(), this.getY(), 16777215 | MathHelper.ceil(this.alpha * 255.0F) << 24);
   }
}
