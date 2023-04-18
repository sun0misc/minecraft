package net.minecraft.client.particle;

import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.VibrationParticleEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.PositionSource;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class VibrationParticle extends SpriteBillboardParticle {
   private final PositionSource vibration;
   private float field_28250;
   private float field_28248;
   private float field_40507;
   private float field_40508;

   VibrationParticle(ClientWorld world, double x, double y, double z, PositionSource vibration, int maxAge) {
      super(world, x, y, z, 0.0, 0.0, 0.0);
      this.scale = 0.3F;
      this.vibration = vibration;
      this.maxAge = maxAge;
      Optional optional = vibration.getPos(world);
      if (optional.isPresent()) {
         Vec3d lv = (Vec3d)optional.get();
         double g = x - lv.getX();
         double h = y - lv.getY();
         double j = z - lv.getZ();
         this.field_28248 = this.field_28250 = (float)MathHelper.atan2(g, j);
         this.field_40508 = this.field_40507 = (float)MathHelper.atan2(h, Math.sqrt(g * g + j * j));
      }

   }

   public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
      float g = MathHelper.sin(((float)this.age + tickDelta - 6.2831855F) * 0.05F) * 2.0F;
      float h = MathHelper.lerp(tickDelta, this.field_28248, this.field_28250);
      float i = MathHelper.lerp(tickDelta, this.field_40508, this.field_40507) + 1.5707964F;
      this.render(vertexConsumer, camera, tickDelta, (rotationQuaternion) -> {
         rotationQuaternion.rotateY(h).rotateX(-i).rotateY(g);
      });
      this.render(vertexConsumer, camera, tickDelta, (rotationQuaternion) -> {
         rotationQuaternion.rotateY(-3.1415927F + h).rotateX(i).rotateY(g);
      });
   }

   private void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta, Consumer transforms) {
      Vec3d lv = camera.getPos();
      float g = (float)(MathHelper.lerp((double)tickDelta, this.prevPosX, this.x) - lv.getX());
      float h = (float)(MathHelper.lerp((double)tickDelta, this.prevPosY, this.y) - lv.getY());
      float i = (float)(MathHelper.lerp((double)tickDelta, this.prevPosZ, this.z) - lv.getZ());
      Vector3f vector3f = (new Vector3f(0.5F, 0.5F, 0.5F)).normalize();
      Quaternionf quaternionf = (new Quaternionf()).setAngleAxis(0.0F, vector3f.x(), vector3f.y(), vector3f.z());
      transforms.accept(quaternionf);
      Vector3f[] vector3fs = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
      float j = this.getSize(tickDelta);

      for(int k = 0; k < 4; ++k) {
         Vector3f vector3f2 = vector3fs[k];
         vector3f2.rotate(quaternionf);
         vector3f2.mul(j);
         vector3f2.add(g, h, i);
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

   public int getBrightness(float tint) {
      return 240;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.age++ >= this.maxAge) {
         this.markDead();
      } else {
         Optional optional = this.vibration.getPos(this.world);
         if (optional.isEmpty()) {
            this.markDead();
         } else {
            int i = this.maxAge - this.age;
            double d = 1.0 / (double)i;
            Vec3d lv = (Vec3d)optional.get();
            this.x = MathHelper.lerp(d, this.x, lv.getX());
            this.y = MathHelper.lerp(d, this.y, lv.getY());
            this.z = MathHelper.lerp(d, this.z, lv.getZ());
            double e = this.x - lv.getX();
            double f = this.y - lv.getY();
            double g = this.z - lv.getZ();
            this.field_28248 = this.field_28250;
            this.field_28250 = (float)MathHelper.atan2(e, g);
            this.field_40508 = this.field_40507;
            this.field_40507 = (float)MathHelper.atan2(f, Math.sqrt(e * e + g * g));
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(VibrationParticleEffect arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         VibrationParticle lv = new VibrationParticle(arg2, d, e, f, arg.getVibration(), arg.getArrivalInTicks());
         lv.setSprite(this.spriteProvider);
         lv.setAlpha(1.0F);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((VibrationParticleEffect)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
