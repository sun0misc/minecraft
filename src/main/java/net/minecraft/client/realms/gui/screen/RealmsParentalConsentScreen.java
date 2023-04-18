package net.minecraft.client.realms.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class RealmsParentalConsentScreen extends RealmsScreen {
   private static final Text PRIVACY_INFO_TEXT = Text.translatable("mco.account.privacyinfo");
   private final Screen parent;
   private MultilineText privacyInfoText;

   public RealmsParentalConsentScreen(Screen parent) {
      super(NarratorManager.EMPTY);
      this.privacyInfoText = MultilineText.EMPTY;
      this.parent = parent;
   }

   public void init() {
      Text lv = Text.translatable("mco.account.update");
      Text lv2 = ScreenTexts.BACK;
      int i = Math.max(this.textRenderer.getWidth((StringVisitable)lv), this.textRenderer.getWidth((StringVisitable)lv2)) + 30;
      Text lv3 = Text.translatable("mco.account.privacy.info");
      int j = (int)((double)this.textRenderer.getWidth((StringVisitable)lv3) * 1.2);
      this.addDrawableChild(ButtonWidget.builder(lv3, (button) -> {
         Util.getOperatingSystem().open("https://aka.ms/MinecraftGDPR");
      }).dimensions(this.width / 2 - j / 2, row(11), j, 20).build());
      this.addDrawableChild(ButtonWidget.builder(lv, (button) -> {
         Util.getOperatingSystem().open("https://aka.ms/UpdateMojangAccount");
      }).dimensions(this.width / 2 - (i + 5), row(13), i, 20).build());
      this.addDrawableChild(ButtonWidget.builder(lv2, (button) -> {
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 + 5, row(13), i, 20).build());
      this.privacyInfoText = MultilineText.create(this.textRenderer, PRIVACY_INFO_TEXT, (int)Math.round((double)this.width * 0.9));
   }

   public Text getNarratedTitle() {
      return PRIVACY_INFO_TEXT;
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      this.privacyInfoText.drawCenterWithShadow(matrices, this.width / 2, 15, 15, 16777215);
      super.render(matrices, mouseX, mouseY, delta);
   }
}
