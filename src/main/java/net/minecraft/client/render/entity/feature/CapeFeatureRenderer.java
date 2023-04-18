package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class CapeFeatureRenderer extends FeatureRenderer {
   public CapeFeatureRenderer(FeatureRendererContext arg) {
      super(arg);
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, AbstractClientPlayerEntity arg3, float f, float g, float h, float j, float k, float l) {
      if (arg3.canRenderCapeTexture() && !arg3.isInvisible() && arg3.isPartVisible(PlayerModelPart.CAPE) && arg3.getCapeTexture() != null) {
         ItemStack lv = arg3.getEquippedStack(EquipmentSlot.CHEST);
         if (!lv.isOf(Items.ELYTRA)) {
            arg.push();
            arg.translate(0.0F, 0.0F, 0.125F);
            double d = MathHelper.lerp((double)h, arg3.prevCapeX, arg3.capeX) - MathHelper.lerp((double)h, arg3.prevX, arg3.getX());
            double e = MathHelper.lerp((double)h, arg3.prevCapeY, arg3.capeY) - MathHelper.lerp((double)h, arg3.prevY, arg3.getY());
            double m = MathHelper.lerp((double)h, arg3.prevCapeZ, arg3.capeZ) - MathHelper.lerp((double)h, arg3.prevZ, arg3.getZ());
            float n = MathHelper.lerpAngleDegrees(h, arg3.prevBodyYaw, arg3.bodyYaw);
            double o = (double)MathHelper.sin(n * 0.017453292F);
            double p = (double)(-MathHelper.cos(n * 0.017453292F));
            float q = (float)e * 10.0F;
            q = MathHelper.clamp(q, -6.0F, 32.0F);
            float r = (float)(d * o + m * p) * 100.0F;
            r = MathHelper.clamp(r, 0.0F, 150.0F);
            float s = (float)(d * p - m * o) * 100.0F;
            s = MathHelper.clamp(s, -20.0F, 20.0F);
            if (r < 0.0F) {
               r = 0.0F;
            }

            float t = MathHelper.lerp(h, arg3.prevStrideDistance, arg3.strideDistance);
            q += MathHelper.sin(MathHelper.lerp(h, arg3.prevHorizontalSpeed, arg3.horizontalSpeed) * 6.0F) * 32.0F * t;
            if (arg3.isInSneakingPose()) {
               q += 25.0F;
            }

            arg.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0F + r / 2.0F + q));
            arg.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(s / 2.0F));
            arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - s / 2.0F));
            VertexConsumer lv2 = arg2.getBuffer(RenderLayer.getEntitySolid(arg3.getCapeTexture()));
            ((PlayerEntityModel)this.getContextModel()).renderCape(arg, lv2, i, OverlayTexture.DEFAULT_UV);
            arg.pop();
         }
      }
   }
}
