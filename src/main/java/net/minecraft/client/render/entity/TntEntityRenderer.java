package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class TntEntityRenderer extends EntityRenderer {
   private final BlockRenderManager blockRenderManager;

   public TntEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg);
      this.shadowRadius = 0.5F;
      this.blockRenderManager = arg.getBlockRenderManager();
   }

   public void render(TntEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      arg2.push();
      arg2.translate(0.0F, 0.5F, 0.0F);
      int j = arg.getFuse();
      if ((float)j - g + 1.0F < 10.0F) {
         float h = 1.0F - ((float)j - g + 1.0F) / 10.0F;
         h = MathHelper.clamp(h, 0.0F, 1.0F);
         h *= h;
         h *= h;
         float k = 1.0F + h * 0.3F;
         arg2.scale(k, k, k);
      }

      arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F));
      arg2.translate(-0.5F, -0.5F, 0.5F);
      arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
      TntMinecartEntityRenderer.renderFlashingBlock(this.blockRenderManager, Blocks.TNT.getDefaultState(), arg2, arg3, i, j / 5 % 2 == 0);
      arg2.pop();
      super.render(arg, f, g, arg2, arg3, i);
   }

   public Identifier getTexture(TntEntity arg) {
      return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
   }
}
