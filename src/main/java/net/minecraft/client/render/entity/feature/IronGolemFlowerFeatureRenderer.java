package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.model.IronGolemEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class IronGolemFlowerFeatureRenderer extends FeatureRenderer {
   private final BlockRenderManager blockRenderManager;

   public IronGolemFlowerFeatureRenderer(FeatureRendererContext context, BlockRenderManager blockRenderManager) {
      super(context);
      this.blockRenderManager = blockRenderManager;
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, IronGolemEntity arg3, float f, float g, float h, float j, float k, float l) {
      if (arg3.getLookingAtVillagerTicks() != 0) {
         arg.push();
         ModelPart lv = ((IronGolemEntityModel)this.getContextModel()).getRightArm();
         lv.rotate(arg);
         arg.translate(-1.1875F, 1.0625F, -0.9375F);
         arg.translate(0.5F, 0.5F, 0.5F);
         float m = 0.5F;
         arg.scale(0.5F, 0.5F, 0.5F);
         arg.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
         arg.translate(-0.5F, -0.5F, -0.5F);
         this.blockRenderManager.renderBlockAsEntity(Blocks.POPPY.getDefaultState(), arg, arg2, i, OverlayTexture.DEFAULT_UV);
         arg.pop();
      }
   }
}
