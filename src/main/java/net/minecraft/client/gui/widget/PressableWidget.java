package net.minecraft.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public abstract class PressableWidget extends ClickableWidget {
   protected static final int field_43046 = 46;
   protected static final int field_43047 = 200;
   protected static final int field_43048 = 20;
   protected static final int field_43100 = 20;
   protected static final int field_43101 = 4;
   protected static final int field_43050 = 2;

   public PressableWidget(int i, int j, int k, int l, Text arg) {
      super(i, j, k, l, arg);
   }

   public abstract void onPress();

   public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      MinecraftClient lv = MinecraftClient.getInstance();
      RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
      RenderSystem.enableBlend();
      RenderSystem.enableDepthTest();
      drawNineSlicedTexture(matrices, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      int k = this.active ? 16777215 : 10526880;
      this.drawMessage(matrices, lv.textRenderer, k | MathHelper.ceil(this.alpha * 255.0F) << 24);
   }

   public void drawMessage(MatrixStack matrices, TextRenderer textRenderer, int color) {
      this.drawScrollableText(matrices, textRenderer, 2, color);
   }

   private int getTextureY() {
      int i = 1;
      if (!this.active) {
         i = 0;
      } else if (this.isSelected()) {
         i = 2;
      }

      return 46 + i * 20;
   }

   public void onClick(double mouseX, double mouseY) {
      this.onPress();
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.active && this.visible) {
         if (KeyCodes.isToggle(keyCode)) {
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            this.onPress();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
