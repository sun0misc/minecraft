package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class MooshroomMushroomFeatureRenderer extends FeatureRenderer {
   private final BlockRenderManager blockRenderManager;

   public MooshroomMushroomFeatureRenderer(FeatureRendererContext context, BlockRenderManager blockRenderManager) {
      super(context);
      this.blockRenderManager = blockRenderManager;
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, MooshroomEntity arg3, float f, float g, float h, float j, float k, float l) {
      if (!arg3.isBaby()) {
         MinecraftClient lv = MinecraftClient.getInstance();
         boolean bl = lv.hasOutline(arg3) && arg3.isInvisible();
         if (!arg3.isInvisible() || bl) {
            BlockState lv2 = arg3.getVariant().getMushroomState();
            int m = LivingEntityRenderer.getOverlay(arg3, 0.0F);
            BakedModel lv3 = this.blockRenderManager.getModel(lv2);
            arg.push();
            arg.translate(0.2F, -0.35F, 0.5F);
            arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-48.0F));
            arg.scale(-1.0F, -1.0F, 1.0F);
            arg.translate(-0.5F, -0.5F, -0.5F);
            this.renderMushroom(arg, arg2, i, bl, lv2, m, lv3);
            arg.pop();
            arg.push();
            arg.translate(0.2F, -0.35F, 0.5F);
            arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(42.0F));
            arg.translate(0.1F, 0.0F, -0.6F);
            arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-48.0F));
            arg.scale(-1.0F, -1.0F, 1.0F);
            arg.translate(-0.5F, -0.5F, -0.5F);
            this.renderMushroom(arg, arg2, i, bl, lv2, m, lv3);
            arg.pop();
            arg.push();
            ((CowEntityModel)this.getContextModel()).getHead().rotate(arg);
            arg.translate(0.0F, -0.7F, -0.2F);
            arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-78.0F));
            arg.scale(-1.0F, -1.0F, 1.0F);
            arg.translate(-0.5F, -0.5F, -0.5F);
            this.renderMushroom(arg, arg2, i, bl, lv2, m, lv3);
            arg.pop();
         }
      }
   }

   private void renderMushroom(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, boolean renderAsModel, BlockState mushroomState, int overlay, BakedModel mushroomModel) {
      if (renderAsModel) {
         this.blockRenderManager.getModelRenderer().render(matrices.peek(), vertexConsumers.getBuffer(RenderLayer.getOutline(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)), mushroomState, mushroomModel, 0.0F, 0.0F, 0.0F, light, overlay);
      } else {
         this.blockRenderManager.renderBlockAsEntity(mushroomState, matrices, vertexConsumers, light, overlay);
      }

   }
}
