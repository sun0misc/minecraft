package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class DolphinHeldItemFeatureRenderer extends FeatureRenderer {
   private final HeldItemRenderer heldItemRenderer;

   public DolphinHeldItemFeatureRenderer(FeatureRendererContext context, HeldItemRenderer heldItemRenderer) {
      super(context);
      this.heldItemRenderer = heldItemRenderer;
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, DolphinEntity arg3, float f, float g, float h, float j, float k, float l) {
      boolean bl = arg3.getMainArm() == Arm.RIGHT;
      arg.push();
      float m = 1.0F;
      float n = -1.0F;
      float o = MathHelper.abs(arg3.getPitch()) / 60.0F;
      if (arg3.getPitch() < 0.0F) {
         arg.translate(0.0F, 1.0F - o * 0.5F, -1.0F + o * 0.5F);
      } else {
         arg.translate(0.0F, 1.0F + o * 0.8F, -1.0F + o * 0.2F);
      }

      ItemStack lv = bl ? arg3.getMainHandStack() : arg3.getOffHandStack();
      this.heldItemRenderer.renderItem(arg3, lv, ModelTransformationMode.GROUND, false, arg, arg2, i);
      arg.pop();
   }
}
