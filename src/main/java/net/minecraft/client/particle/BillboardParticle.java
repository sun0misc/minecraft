package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public abstract class BillboardParticle extends Particle {
   protected float scale;

   protected BillboardParticle(ClientWorld arg, double d, double e, double f) {
      super(arg, d, e, f);
      this.scale = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
   }

   protected BillboardParticle(ClientWorld arg, double d, double e, double f, double g, double h, double i) {
      super(arg, d, e, f, g, h, i);
      this.scale = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
   }

   public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
      Vec3d lv = camera.getPos();
      float g = (float)(MathHelper.lerp((double)tickDelta, this.prevPosX, this.x) - lv.getX());
      float h = (float)(MathHelper.lerp((double)tickDelta, this.prevPosY, this.y) - lv.getY());
      float i = (float)(MathHelper.lerp((double)tickDelta, this.prevPosZ, this.z) - lv.getZ());
      Quaternionf quaternionf;
      if (this.angle == 0.0F) {
         quaternionf = camera.getRotation();
      } else {
         quaternionf = new Quaternionf(camera.getRotation());
         quaternionf.rotateZ(MathHelper.lerp(tickDelta, this.prevAngle, this.angle));
      }

      Vector3f[] vector3fs = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
      float j = this.getSize(tickDelta);

      for(int k = 0; k < 4; ++k) {
         Vector3f vector3f = vector3fs[k];
         vector3f.rotate(quaternionf);
         vector3f.mul(j);
         vector3f.add(g, h, i);
      }

      float l = this.getMinU();
      float m = this.getMaxU();
      float n = this.getMinV();
      float o = this.getMaxV();
      int p = this.getBrightness(tickDelta);
      vertexConsumer.vertex((double)vector3fs[0].x(), (double)vector3fs[0].y(), (double)vector3fs[0].z()).texture(m, o).color(this.red, this.green, this.blue, this.alpha).light(p).next();
      vertexConsumer.vertex((double)vector3fs[1].x(), (double)vector3fs[1].y(), (double)vector3fs[1].z()).texture(m, n).color(this.red, this.green, this.blue, this.alpha).light(p).next();
      vertexConsumer.vertex((double)vector3fs[2].x(), (double)vector3fs[2].y(), (double)vector3fs[2].z()).texture(l, n).color(this.red, this.green, this.blue, this.alpha).light(p).next();
      vertexConsumer.vertex((double)vector3fs[3].x(), (double)vector3fs[3].y(), (double)vector3fs[3].z()).texture(l, o).color(this.red, this.green, this.blue, this.alpha).light(p).next();
   }

   public float getSize(float tickDelta) {
      return this.scale;
   }

   public Particle scale(float scale) {
      this.scale *= scale;
      return super.scale(scale);
   }

   protected abstract float getMinU();

   protected abstract float getMaxU();

   protected abstract float getMinV();

   protected abstract float getMaxV();
}
