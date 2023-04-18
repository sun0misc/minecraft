package net.minecraft.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TabButtonWidget extends ClickableWidget {
   private static final Identifier TEXTURE = new Identifier("textures/gui/tab_button.png");
   private static final int field_43059 = 130;
   private static final int field_43060 = 24;
   private static final int field_43061 = 2;
   private static final int field_43062 = 0;
   private static final int field_43063 = 3;
   private static final int field_43064 = 1;
   private static final int field_43065 = 1;
   private static final int field_43066 = 4;
   private static final int field_43067 = 2;
   private final TabManager tabManager;
   private final Tab tab;

   public TabButtonWidget(TabManager tabManager, Tab tab, int width, int height) {
      super(0, 0, width, height, tab.getTitle());
      this.tabManager = tabManager;
      this.tab = tab;
   }

   public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      RenderSystem.setShaderTexture(0, TEXTURE);
      drawNineSlicedTexture(matrices, this.getX(), this.getY(), this.width, this.height, 2, 2, 2, 0, 130, 24, 0, this.getTextureV());
      TextRenderer lv = MinecraftClient.getInstance().textRenderer;
      int k = this.active ? -1 : -6250336;
      this.drawMessage(matrices, lv, k);
      if (this.isCurrentTab()) {
         this.drawCurrentTabLine(matrices, lv, k);
      }

   }

   public void drawMessage(MatrixStack matrices, TextRenderer textRenderer, int color) {
      int j = this.getX() + 1;
      int k = this.getY() + (this.isCurrentTab() ? 0 : 3);
      int l = this.getX() + this.getWidth() - 1;
      int m = this.getY() + this.getHeight();
      drawScrollableText(matrices, textRenderer, this.getMessage(), j, k, l, m, color);
   }

   private void drawCurrentTabLine(MatrixStack matrices, TextRenderer textRenderer, int color) {
      int j = Math.min(textRenderer.getWidth((StringVisitable)this.getMessage()), this.getWidth() - 4);
      int k = this.getX() + (this.getWidth() - j) / 2;
      int l = this.getY() + this.getHeight() - 2;
      fill(matrices, k, l, k + j, l + 1, color);
   }

   protected int getTextureV() {
      int i = 2;
      if (this.isCurrentTab() && this.isSelected()) {
         i = 1;
      } else if (this.isCurrentTab()) {
         i = 0;
      } else if (this.isSelected()) {
         i = 3;
      }

      return i * 24;
   }

   protected void appendClickableNarrations(NarrationMessageBuilder builder) {
      builder.put(NarrationPart.TITLE, (Text)Text.translatable("gui.narrate.tab", this.tab.getTitle()));
   }

   public void playDownSound(SoundManager soundManager) {
   }

   public Tab getTab() {
      return this.tab;
   }

   public boolean isCurrentTab() {
      return this.tabManager.getCurrentTab() == this.tab;
   }
}
