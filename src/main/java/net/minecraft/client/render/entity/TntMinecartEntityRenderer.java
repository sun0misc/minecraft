package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class TntMinecartEntityRenderer extends MinecartEntityRenderer {
   private final BlockRenderManager tntBlockRenderManager;

   public TntMinecartEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, EntityModelLayers.TNT_MINECART);
      this.tntBlockRenderManager = arg.getBlockRenderManager();
   }

   protected void renderBlock(TntMinecartEntity arg, float f, BlockState arg2, MatrixStack arg3, VertexConsumerProvider arg4, int i) {
      int j = arg.getFuseTicks();
      if (j > -1 && (float)j - f + 1.0F < 10.0F) {
         float g = 1.0F - ((float)j - f + 1.0F) / 10.0F;
         g = MathHelper.clamp(g, 0.0F, 1.0F);
         g *= g;
         g *= g;
         float h = 1.0F + g * 0.3F;
         arg3.scale(h, h, h);
      }

      renderFlashingBlock(this.tntBlockRenderManager, arg2, arg3, arg4, i, j > -1 && j / 5 % 2 == 0);
   }

   public static void renderFlashingBlock(BlockRenderManager blockRenderManager, BlockState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, boolean drawFlash) {
      int j;
      if (drawFlash) {
         j = OverlayTexture.packUv(OverlayTexture.getU(1.0F), 10);
      } else {
         j = OverlayTexture.DEFAULT_UV;
      }

      blockRenderManager.renderBlockAsEntity(state, matrices, vertexConsumers, light, j);
   }
}
