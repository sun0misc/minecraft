package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class StuckArrowsFeatureRenderer extends StuckObjectsFeatureRenderer {
   private final EntityRenderDispatcher dispatcher;

   public StuckArrowsFeatureRenderer(EntityRendererFactory.Context context, LivingEntityRenderer entityRenderer) {
      super(entityRenderer);
      this.dispatcher = context.getRenderDispatcher();
   }

   protected int getObjectCount(LivingEntity entity) {
      return entity.getStuckArrowCount();
   }

   protected void renderObject(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Entity entity, float directionX, float directionY, float directionZ, float tickDelta) {
      float k = MathHelper.sqrt(directionX * directionX + directionZ * directionZ);
      ArrowEntity lv = new ArrowEntity(entity.world, entity.getX(), entity.getY(), entity.getZ());
      lv.setYaw((float)(Math.atan2((double)directionX, (double)directionZ) * 57.2957763671875));
      lv.setPitch((float)(Math.atan2((double)directionY, (double)k) * 57.2957763671875));
      lv.prevYaw = lv.getYaw();
      lv.prevPitch = lv.getPitch();
      this.dispatcher.render(lv, 0.0, 0.0, 0.0, 0.0F, tickDelta, matrices, vertexConsumers, light);
   }
}
