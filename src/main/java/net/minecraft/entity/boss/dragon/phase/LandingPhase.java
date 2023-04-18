package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.jetbrains.annotations.Nullable;

public class LandingPhase extends AbstractPhase {
   @Nullable
   private Vec3d target;

   public LandingPhase(EnderDragonEntity arg) {
      super(arg);
   }

   public void clientTick() {
      Vec3d lv = this.dragon.getRotationVectorFromPhase(1.0F).normalize();
      lv.rotateY(-0.7853982F);
      double d = this.dragon.head.getX();
      double e = this.dragon.head.getBodyY(0.5);
      double f = this.dragon.head.getZ();

      for(int i = 0; i < 8; ++i) {
         Random lv2 = this.dragon.getRandom();
         double g = d + lv2.nextGaussian() / 2.0;
         double h = e + lv2.nextGaussian() / 2.0;
         double j = f + lv2.nextGaussian() / 2.0;
         Vec3d lv3 = this.dragon.getVelocity();
         this.dragon.world.addParticle(ParticleTypes.DRAGON_BREATH, g, h, j, -lv.x * 0.07999999821186066 + lv3.x, -lv.y * 0.30000001192092896 + lv3.y, -lv.z * 0.07999999821186066 + lv3.z);
         lv.rotateY(0.19634955F);
      }

   }

   public void serverTick() {
      if (this.target == null) {
         this.target = Vec3d.ofBottomCenter(this.dragon.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.ORIGIN));
      }

      if (this.target.squaredDistanceTo(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ()) < 1.0) {
         ((SittingFlamingPhase)this.dragon.getPhaseManager().create(PhaseType.SITTING_FLAMING)).reset();
         this.dragon.getPhaseManager().setPhase(PhaseType.SITTING_SCANNING);
      }

   }

   public float getMaxYAcceleration() {
      return 1.5F;
   }

   public float getYawAcceleration() {
      float f = (float)this.dragon.getVelocity().horizontalLength() + 1.0F;
      float g = Math.min(f, 40.0F);
      return g / f;
   }

   public void beginPhase() {
      this.target = null;
   }

   @Nullable
   public Vec3d getPathTarget() {
      return this.target;
   }

   public PhaseType getType() {
      return PhaseType.LANDING;
   }
}
