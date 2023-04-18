package net.minecraft.client.gui.screen.option;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class SkinOptionsScreen extends GameOptionsScreen {
   public SkinOptionsScreen(Screen parent, GameOptions gameOptions) {
      super(parent, gameOptions, Text.translatable("options.skinCustomisation.title"));
   }

   protected void init() {
      int i = 0;
      PlayerModelPart[] var2 = PlayerModelPart.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         PlayerModelPart lv = var2[var4];
         this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.gameOptions.isPlayerModelPartEnabled(lv)).build(this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), 150, 20, lv.getOptionName(), (button, enabled) -> {
            this.gameOptions.togglePlayerModelPart(lv, enabled);
         }));
         ++i;
      }

      this.addDrawableChild(this.gameOptions.getMainArm().createWidget(this.gameOptions, this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), 150));
      ++i;
      if (i % 2 == 1) {
         ++i;
      }

      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 - 100, this.height / 6 + 24 * (i >> 1), 200, 20).build());
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 20, 16777215);
      super.render(matrices, mouseX, mouseY, delta);
   }
}
