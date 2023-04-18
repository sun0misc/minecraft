package net.minecraft.client.gui.screen.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.screen.CartographyTableScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class CartographyTableScreen extends HandledScreen {
   private static final Identifier TEXTURE = new Identifier("textures/gui/container/cartography_table.png");

   public CartographyTableScreen(CartographyTableScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
      this.titleY -= 2;
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      super.render(matrices, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(matrices, mouseX, mouseY);
   }

   protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
      this.renderBackground(matrices);
      RenderSystem.setShaderTexture(0, TEXTURE);
      int k = this.x;
      int l = this.y;
      drawTexture(matrices, k, l, 0, 0, this.backgroundWidth, this.backgroundHeight);
      ItemStack lv = ((CartographyTableScreenHandler)this.handler).getSlot(1).getStack();
      boolean bl = lv.isOf(Items.MAP);
      boolean bl2 = lv.isOf(Items.PAPER);
      boolean bl3 = lv.isOf(Items.GLASS_PANE);
      ItemStack lv2 = ((CartographyTableScreenHandler)this.handler).getSlot(0).getStack();
      boolean bl4 = false;
      Integer integer;
      MapState lv3;
      if (lv2.isOf(Items.FILLED_MAP)) {
         integer = FilledMapItem.getMapId(lv2);
         lv3 = FilledMapItem.getMapState((Integer)integer, this.client.world);
         if (lv3 != null) {
            if (lv3.locked) {
               bl4 = true;
               if (bl2 || bl3) {
                  drawTexture(matrices, k + 35, l + 31, this.backgroundWidth + 50, 132, 28, 21);
               }
            }

            if (bl2 && lv3.scale >= 4) {
               bl4 = true;
               drawTexture(matrices, k + 35, l + 31, this.backgroundWidth + 50, 132, 28, 21);
            }
         }
      } else {
         integer = null;
         lv3 = null;
      }

      this.drawMap(matrices, integer, lv3, bl, bl2, bl3, bl4);
   }

   private void drawMap(MatrixStack matrices, @Nullable Integer mapId, @Nullable MapState mapState, boolean cloneMode, boolean expandMode, boolean lockMode, boolean cannotExpand) {
      int i = this.x;
      int j = this.y;
      if (expandMode && !cannotExpand) {
         drawTexture(matrices, i + 67, j + 13, this.backgroundWidth, 66, 66, 66);
         this.drawMap(matrices, mapId, mapState, i + 85, j + 31, 0.226F);
      } else if (cloneMode) {
         drawTexture(matrices, i + 67 + 16, j + 13, this.backgroundWidth, 132, 50, 66);
         this.drawMap(matrices, mapId, mapState, i + 86, j + 16, 0.34F);
         RenderSystem.setShaderTexture(0, TEXTURE);
         matrices.push();
         matrices.translate(0.0F, 0.0F, 1.0F);
         drawTexture(matrices, i + 67, j + 13 + 16, this.backgroundWidth, 132, 50, 66);
         this.drawMap(matrices, mapId, mapState, i + 70, j + 32, 0.34F);
         matrices.pop();
      } else if (lockMode) {
         drawTexture(matrices, i + 67, j + 13, this.backgroundWidth, 0, 66, 66);
         this.drawMap(matrices, mapId, mapState, i + 71, j + 17, 0.45F);
         RenderSystem.setShaderTexture(0, TEXTURE);
         matrices.push();
         matrices.translate(0.0F, 0.0F, 1.0F);
         drawTexture(matrices, i + 66, j + 12, 0, this.backgroundHeight, 66, 66);
         matrices.pop();
      } else {
         drawTexture(matrices, i + 67, j + 13, this.backgroundWidth, 0, 66, 66);
         this.drawMap(matrices, mapId, mapState, i + 71, j + 17, 0.45F);
      }

   }

   private void drawMap(MatrixStack matrices, @Nullable Integer mapId, @Nullable MapState mapState, int x, int y, float scale) {
      if (mapId != null && mapState != null) {
         matrices.push();
         matrices.translate((float)x, (float)y, 1.0F);
         matrices.scale(scale, scale, 1.0F);
         VertexConsumerProvider.Immediate lv = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
         this.client.gameRenderer.getMapRenderer().draw(matrices, lv, mapId, mapState, true, LightmapTextureManager.MAX_LIGHT_COORDINATE);
         lv.draw();
         matrices.pop();
      }

   }
}
