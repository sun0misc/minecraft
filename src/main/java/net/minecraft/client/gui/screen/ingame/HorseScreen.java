package net.minecraft.client.gui.screen.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class HorseScreen extends HandledScreen {
   private static final Identifier TEXTURE = new Identifier("textures/gui/container/horse.png");
   private final AbstractHorseEntity entity;
   private float mouseX;
   private float mouseY;

   public HorseScreen(HorseScreenHandler handler, PlayerInventory inventory, AbstractHorseEntity entity) {
      super(handler, inventory, entity.getDisplayName());
      this.entity = entity;
      this.passEvents = false;
   }

   protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
      RenderSystem.setShaderTexture(0, TEXTURE);
      int k = (this.width - this.backgroundWidth) / 2;
      int l = (this.height - this.backgroundHeight) / 2;
      drawTexture(matrices, k, l, 0, 0, this.backgroundWidth, this.backgroundHeight);
      if (this.entity instanceof AbstractDonkeyEntity) {
         AbstractDonkeyEntity lv = (AbstractDonkeyEntity)this.entity;
         if (lv.hasChest()) {
            drawTexture(matrices, k + 79, l + 17, 0, this.backgroundHeight, lv.getInventoryColumns() * 18, 54);
         }
      }

      if (this.entity.canBeSaddled()) {
         drawTexture(matrices, k + 7, l + 35 - 18, 18, this.backgroundHeight + 54, 18, 18);
      }

      if (this.entity.hasArmorSlot()) {
         if (this.entity instanceof LlamaEntity) {
            drawTexture(matrices, k + 7, l + 35, 36, this.backgroundHeight + 54, 18, 18);
         } else {
            drawTexture(matrices, k + 7, l + 35, 0, this.backgroundHeight + 54, 18, 18);
         }
      }

      InventoryScreen.drawEntity(matrices, k + 51, l + 60, 17, (float)(k + 51) - this.mouseX, (float)(l + 75 - 50) - this.mouseY, this.entity);
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      this.mouseX = (float)mouseX;
      this.mouseY = (float)mouseY;
      super.render(matrices, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(matrices, mouseX, mouseY);
   }
}
