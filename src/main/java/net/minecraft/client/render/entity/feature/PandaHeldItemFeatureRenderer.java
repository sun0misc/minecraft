package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class PandaHeldItemFeatureRenderer extends FeatureRenderer {
   private final HeldItemRenderer heldItemRenderer;

   public PandaHeldItemFeatureRenderer(FeatureRendererContext context, HeldItemRenderer heldItemRenderer) {
      super(context);
      this.heldItemRenderer = heldItemRenderer;
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, PandaEntity arg3, float f, float g, float h, float j, float k, float l) {
      ItemStack lv = arg3.getEquippedStack(EquipmentSlot.MAINHAND);
      if (arg3.isSitting() && !arg3.isScaredByThunderstorm()) {
         float m = -0.6F;
         float n = 1.4F;
         if (arg3.isEating()) {
            m -= 0.2F * MathHelper.sin(j * 0.6F) + 0.2F;
            n -= 0.09F * MathHelper.sin(j * 0.6F);
         }

         arg.push();
         arg.translate(0.1F, n, m);
         this.heldItemRenderer.renderItem(arg3, lv, ModelTransformationMode.GROUND, false, arg, arg2, i);
         arg.pop();
      }
   }
}
