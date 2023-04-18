package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public abstract class StuckObjectsFeatureRenderer extends FeatureRenderer {
   public StuckObjectsFeatureRenderer(LivingEntityRenderer entityRenderer) {
      super(entityRenderer);
   }

   protected abstract int getObjectCount(LivingEntity entity);

   protected abstract void renderObject(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Entity entity, float directionX, float directionY, float directionZ, float tickDelta);

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, LivingEntity arg3, float f, float g, float h, float j, float k, float l) {
      int m = this.getObjectCount(arg3);
      Random lv = Random.create((long)arg3.getId());
      if (m > 0) {
         for(int n = 0; n < m; ++n) {
            arg.push();
            ModelPart lv2 = ((PlayerEntityModel)this.getContextModel()).getRandomPart(lv);
            ModelPart.Cuboid lv3 = lv2.getRandomCuboid(lv);
            lv2.rotate(arg);
            float o = lv.nextFloat();
            float p = lv.nextFloat();
            float q = lv.nextFloat();
            float r = MathHelper.lerp(o, lv3.minX, lv3.maxX) / 16.0F;
            float s = MathHelper.lerp(p, lv3.minY, lv3.maxY) / 16.0F;
            float t = MathHelper.lerp(q, lv3.minZ, lv3.maxZ) / 16.0F;
            arg.translate(r, s, t);
            o = -1.0F * (o * 2.0F - 1.0F);
            p = -1.0F * (p * 2.0F - 1.0F);
            q = -1.0F * (q * 2.0F - 1.0F);
            this.renderObject(arg, arg2, i, arg3, o, p, q, h);
            arg.pop();
         }

      }
   }
}
