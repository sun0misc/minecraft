package net.minecraft.client.realms.gui.screen;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class DisconnectedRealmsScreen extends RealmsScreen {
   private final Text reason;
   private MultilineText lines;
   private final Screen parent;
   private int textHeight;

   public DisconnectedRealmsScreen(Screen parent, Text title, Text reason) {
      super(title);
      this.lines = MultilineText.EMPTY;
      this.parent = parent;
      this.reason = reason;
   }

   public void init() {
      MinecraftClient lv = MinecraftClient.getInstance();
      lv.setConnectedToRealms(false);
      lv.getServerResourcePackProvider().clear();
      this.lines = MultilineText.create(this.textRenderer, this.reason, this.width - 50);
      int var10001 = this.lines.count();
      Objects.requireNonNull(this.textRenderer);
      this.textHeight = var10001 * 9;
      ButtonWidget.Builder var2 = ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
         lv.setScreen(this.parent);
      });
      int var10002 = this.width / 2 - 100;
      int var10003 = this.height / 2 + this.textHeight / 2;
      Objects.requireNonNull(this.textRenderer);
      this.addDrawableChild(var2.dimensions(var10002, var10003 + 9, 200, 20).build());
   }

   public Text getNarratedTitle() {
      return Text.empty().append(this.title).append(": ").append(this.reason);
   }

   public void close() {
      MinecraftClient.getInstance().setScreen(this.parent);
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      TextRenderer var10001 = this.textRenderer;
      Text var10002 = this.title;
      int var10003 = this.width / 2;
      int var10004 = this.height / 2 - this.textHeight / 2;
      Objects.requireNonNull(this.textRenderer);
      drawCenteredTextWithShadow(matrices, var10001, var10002, var10003, var10004 - 9 * 2, 11184810);
      this.lines.drawCenterWithShadow(matrices, this.width / 2, this.height / 2 - this.textHeight / 2);
      super.render(matrices, mouseX, mouseY, delta);
   }
}
