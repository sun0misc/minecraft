package net.minecraft.client.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.spectator.SpectatorMenu;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuCloseCallback;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuCommand;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SpectatorHud extends DrawableHelper implements SpectatorMenuCloseCallback {
   private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/widgets.png");
   public static final Identifier SPECTATOR_TEXTURE = new Identifier("textures/gui/spectator_widgets.png");
   private static final long FADE_OUT_DELAY = 5000L;
   private static final long FADE_OUT_DURATION = 2000L;
   private final MinecraftClient client;
   private long lastInteractionTime;
   @Nullable
   private SpectatorMenu spectatorMenu;

   public SpectatorHud(MinecraftClient client) {
      this.client = client;
   }

   public void selectSlot(int slot) {
      this.lastInteractionTime = Util.getMeasuringTimeMs();
      if (this.spectatorMenu != null) {
         this.spectatorMenu.useCommand(slot);
      } else {
         this.spectatorMenu = new SpectatorMenu(this);
      }

   }

   private float getSpectatorMenuHeight() {
      long l = this.lastInteractionTime - Util.getMeasuringTimeMs() + 5000L;
      return MathHelper.clamp((float)l / 2000.0F, 0.0F, 1.0F);
   }

   public void renderSpectatorMenu(MatrixStack matrices) {
      if (this.spectatorMenu != null) {
         float f = this.getSpectatorMenuHeight();
         if (f <= 0.0F) {
            this.spectatorMenu.close();
         } else {
            int i = this.client.getWindow().getScaledWidth() / 2;
            matrices.push();
            matrices.translate(0.0F, 0.0F, -90.0F);
            int j = MathHelper.floor((float)this.client.getWindow().getScaledHeight() - 22.0F * f);
            SpectatorMenuState lv = this.spectatorMenu.getCurrentState();
            this.renderSpectatorMenu(matrices, f, i, j, lv);
            matrices.pop();
         }
      }
   }

   protected void renderSpectatorMenu(MatrixStack matrices, float height, int x, int y, SpectatorMenuState state) {
      RenderSystem.enableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, height);
      RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
      drawTexture(matrices, x - 91, y, 0, 0, 182, 22);
      if (state.getSelectedSlot() >= 0) {
         drawTexture(matrices, x - 91 - 1 + state.getSelectedSlot() * 20, y - 1, 0, 22, 24, 22);
      }

      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

      for(int k = 0; k < 9; ++k) {
         this.renderSpectatorCommand(matrices, k, this.client.getWindow().getScaledWidth() / 2 - 90 + k * 20 + 2, (float)(y + 3), height, state.getCommand(k));
      }

      RenderSystem.disableBlend();
   }

   private void renderSpectatorCommand(MatrixStack matrices, int slot, int x, float y, float height, SpectatorMenuCommand command) {
      RenderSystem.setShaderTexture(0, SPECTATOR_TEXTURE);
      if (command != SpectatorMenu.BLANK_COMMAND) {
         int k = (int)(height * 255.0F);
         matrices.push();
         matrices.translate((float)x, y, 0.0F);
         float h = command.isEnabled() ? 1.0F : 0.25F;
         RenderSystem.setShaderColor(h, h, h, height);
         command.renderIcon(matrices, h, k);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         matrices.pop();
         if (k > 3 && command.isEnabled()) {
            Text lv = this.client.options.hotbarKeys[slot].getBoundKeyLocalizedText();
            this.client.textRenderer.drawWithShadow(matrices, lv, (float)(x + 19 - 2 - this.client.textRenderer.getWidth((StringVisitable)lv)), y + 6.0F + 3.0F, 16777215 + (k << 24));
         }
      }

   }

   public void render(MatrixStack matrices) {
      int i = (int)(this.getSpectatorMenuHeight() * 255.0F);
      if (i > 3 && this.spectatorMenu != null) {
         SpectatorMenuCommand lv = this.spectatorMenu.getSelectedCommand();
         Text lv2 = lv == SpectatorMenu.BLANK_COMMAND ? this.spectatorMenu.getCurrentGroup().getPrompt() : lv.getName();
         if (lv2 != null) {
            int j = (this.client.getWindow().getScaledWidth() - this.client.textRenderer.getWidth((StringVisitable)lv2)) / 2;
            int k = this.client.getWindow().getScaledHeight() - 35;
            this.client.textRenderer.drawWithShadow(matrices, lv2, (float)j, (float)k, 16777215 + (i << 24));
         }
      }

   }

   public void close(SpectatorMenu menu) {
      this.spectatorMenu = null;
      this.lastInteractionTime = 0L;
   }

   public boolean isOpen() {
      return this.spectatorMenu != null;
   }

   public void cycleSlot(int i) {
      int j;
      for(j = this.spectatorMenu.getSelectedSlot() + i; j >= 0 && j <= 8 && (this.spectatorMenu.getCommand(j) == SpectatorMenu.BLANK_COMMAND || !this.spectatorMenu.getCommand(j).isEnabled()); j += i) {
      }

      if (j >= 0 && j <= 8) {
         this.spectatorMenu.useCommand(j);
         this.lastInteractionTime = Util.getMeasuringTimeMs();
      }

   }

   public void useSelectedCommand() {
      this.lastInteractionTime = Util.getMeasuringTimeMs();
      if (this.isOpen()) {
         int i = this.spectatorMenu.getSelectedSlot();
         if (i != -1) {
            this.spectatorMenu.useCommand(i);
         }
      } else {
         this.spectatorMenu = new SpectatorMenu(this);
      }

   }
}
