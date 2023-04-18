package net.minecraft.client.gui.screen.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public abstract class ForgingScreen extends HandledScreen implements ScreenHandlerListener {
   private final Identifier texture;

   public ForgingScreen(ForgingScreenHandler handler, PlayerInventory playerInventory, Text title, Identifier texture) {
      super(handler, playerInventory, title);
      this.texture = texture;
   }

   protected void setup() {
   }

   protected void init() {
      super.init();
      this.setup();
      ((ForgingScreenHandler)this.handler).addListener(this);
   }

   public void removed() {
      super.removed();
      ((ForgingScreenHandler)this.handler).removeListener(this);
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      super.render(matrices, mouseX, mouseY, delta);
      this.renderForeground(matrices, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(matrices, mouseX, mouseY);
   }

   protected void renderForeground(MatrixStack matrices, int mouseX, int mouseY, float delta) {
   }

   protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
      RenderSystem.setShaderTexture(0, this.texture);
      drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
      this.drawInvalidRecipeArrow(matrices, this.x, this.y);
   }

   protected abstract void drawInvalidRecipeArrow(MatrixStack matrices, int x, int y);

   public void onPropertyUpdate(ScreenHandler handler, int property, int value) {
   }

   public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
   }
}
