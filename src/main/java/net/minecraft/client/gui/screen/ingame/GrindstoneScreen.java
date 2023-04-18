package net.minecraft.client.gui.screen.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class GrindstoneScreen extends HandledScreen {
   private static final Identifier TEXTURE = new Identifier("textures/gui/container/grindstone.png");

   public GrindstoneScreen(GrindstoneScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      this.drawBackground(matrices, delta, mouseX, mouseY);
      super.render(matrices, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(matrices, mouseX, mouseY);
   }

   protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
      RenderSystem.setShaderTexture(0, TEXTURE);
      int k = (this.width - this.backgroundWidth) / 2;
      int l = (this.height - this.backgroundHeight) / 2;
      drawTexture(matrices, k, l, 0, 0, this.backgroundWidth, this.backgroundHeight);
      if ((((GrindstoneScreenHandler)this.handler).getSlot(0).hasStack() || ((GrindstoneScreenHandler)this.handler).getSlot(1).hasStack()) && !((GrindstoneScreenHandler)this.handler).getSlot(2).hasStack()) {
         drawTexture(matrices, k + 92, l + 31, this.backgroundWidth, 0, 28, 21);
      }

   }
}
