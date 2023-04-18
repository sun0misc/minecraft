package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.FoxEntityModel;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class FoxHeldItemFeatureRenderer extends FeatureRenderer {
   private final HeldItemRenderer heldItemRenderer;

   public FoxHeldItemFeatureRenderer(FeatureRendererContext context, HeldItemRenderer heldItemRenderer) {
      super(context);
      this.heldItemRenderer = heldItemRenderer;
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, FoxEntity arg3, float f, float g, float h, float j, float k, float l) {
      boolean bl = arg3.isSleeping();
      boolean bl2 = arg3.isBaby();
      arg.push();
      float m;
      if (bl2) {
         m = 0.75F;
         arg.scale(0.75F, 0.75F, 0.75F);
         arg.translate(0.0F, 0.5F, 0.209375F);
      }

      arg.translate(((FoxEntityModel)this.getContextModel()).head.pivotX / 16.0F, ((FoxEntityModel)this.getContextModel()).head.pivotY / 16.0F, ((FoxEntityModel)this.getContextModel()).head.pivotZ / 16.0F);
      m = arg3.getHeadRoll(h);
      arg.multiply(RotationAxis.POSITIVE_Z.rotation(m));
      arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(k));
      arg.multiply(RotationAxis.POSITIVE_X.rotationDegrees(l));
      if (arg3.isBaby()) {
         if (bl) {
            arg.translate(0.4F, 0.26F, 0.15F);
         } else {
            arg.translate(0.06F, 0.26F, -0.5F);
         }
      } else if (bl) {
         arg.translate(0.46F, 0.26F, 0.22F);
      } else {
         arg.translate(0.06F, 0.27F, -0.5F);
      }

      arg.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
      if (bl) {
         arg.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
      }

      ItemStack lv = arg3.getEquippedStack(EquipmentSlot.MAINHAND);
      this.heldItemRenderer.renderItem(arg3, lv, ModelTransformationMode.GROUND, false, arg, arg2, i);
      arg.pop();
   }
}
