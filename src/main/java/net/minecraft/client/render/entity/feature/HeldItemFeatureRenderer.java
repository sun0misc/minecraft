package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class HeldItemFeatureRenderer extends FeatureRenderer {
   private final HeldItemRenderer heldItemRenderer;

   public HeldItemFeatureRenderer(FeatureRendererContext context, HeldItemRenderer heldItemRenderer) {
      super(context);
      this.heldItemRenderer = heldItemRenderer;
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, LivingEntity arg3, float f, float g, float h, float j, float k, float l) {
      boolean bl = arg3.getMainArm() == Arm.RIGHT;
      ItemStack lv = bl ? arg3.getOffHandStack() : arg3.getMainHandStack();
      ItemStack lv2 = bl ? arg3.getMainHandStack() : arg3.getOffHandStack();
      if (!lv.isEmpty() || !lv2.isEmpty()) {
         arg.push();
         if (this.getContextModel().child) {
            float m = 0.5F;
            arg.translate(0.0F, 0.75F, 0.0F);
            arg.scale(0.5F, 0.5F, 0.5F);
         }

         this.renderItem(arg3, lv2, ModelTransformationMode.THIRD_PERSON_RIGHT_HAND, Arm.RIGHT, arg, arg2, i);
         this.renderItem(arg3, lv, ModelTransformationMode.THIRD_PERSON_LEFT_HAND, Arm.LEFT, arg, arg2, i);
         arg.pop();
      }
   }

   protected void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode transformationMode, Arm arm, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      if (!stack.isEmpty()) {
         matrices.push();
         ((ModelWithArms)this.getContextModel()).setArmAngle(arm, matrices);
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
         boolean bl = arm == Arm.LEFT;
         matrices.translate((float)(bl ? -1 : 1) / 16.0F, 0.125F, -0.625F);
         this.heldItemRenderer.renderItem(entity, stack, transformationMode, bl, matrices, vertexConsumers, light);
         matrices.pop();
      }
   }
}
