package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class VillagerHeldItemFeatureRenderer extends FeatureRenderer {
   private final HeldItemRenderer heldItemRenderer;

   public VillagerHeldItemFeatureRenderer(FeatureRendererContext context, HeldItemRenderer heldItemRenderer) {
      super(context);
      this.heldItemRenderer = heldItemRenderer;
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, LivingEntity arg3, float f, float g, float h, float j, float k, float l) {
      arg.push();
      arg.translate(0.0F, 0.4F, -0.4F);
      arg.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0F));
      ItemStack lv = arg3.getEquippedStack(EquipmentSlot.MAINHAND);
      this.heldItemRenderer.renderItem(arg3, lv, ModelTransformationMode.GROUND, false, arg, arg2, i);
      arg.pop();
   }
}
