package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.WitchEntityModel;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class WitchHeldItemFeatureRenderer extends VillagerHeldItemFeatureRenderer {
   public WitchHeldItemFeatureRenderer(FeatureRendererContext arg, HeldItemRenderer arg2) {
      super(arg, arg2);
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, LivingEntity arg3, float f, float g, float h, float j, float k, float l) {
      ItemStack lv = arg3.getMainHandStack();
      arg.push();
      if (lv.isOf(Items.POTION)) {
         ((WitchEntityModel)this.getContextModel()).getHead().rotate(arg);
         ((WitchEntityModel)this.getContextModel()).getNose().rotate(arg);
         arg.translate(0.0625F, 0.25F, 0.0F);
         arg.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
         arg.multiply(RotationAxis.POSITIVE_X.rotationDegrees(140.0F));
         arg.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(10.0F));
         arg.translate(0.0F, -0.4F, 0.4F);
      }

      super.render(arg, arg2, i, arg3, f, g, h, j, k, l);
      arg.pop();
   }
}
