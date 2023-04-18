package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.jetbrains.annotations.Nullable;

public class DyingPhase extends AbstractPhase {
   @Nullable
   private Vec3d target;
   private int ticks;

   public DyingPhase(EnderDragonEntity arg) {
      super(arg);
   }

   public void clientTick() {
      if (this.ticks++ % 10 == 0) {
         float f = (this.dragon.getRandom().nextFloat() - 0.5F) * 8.0F;
         float g = (this.dragon.getRandom().nextFloat() - 0.5F) * 4.0F;
         float h = (this.dragon.getRandom().nextFloat() - 0.5F) * 8.0F;
         this.dragon.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.dragon.getX() + (double)f, this.dragon.getY() + 2.0 + (double)g, this.dragon.getZ() + (double)h, 0.0, 0.0, 0.0);
      }

   }

   public void serverTick() {
      ++this.ticks;
      if (this.target == null) {
         BlockPos lv = this.dragon.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, EndPortalFeature.ORIGIN);
         this.target = Vec3d.ofBottomCenter(lv);
      }

      double d = this.target.squaredDistanceTo(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
      if (!(d < 100.0) && !(d > 22500.0) && !this.dragon.horizontalCollision && !this.dragon.verticalCollision) {
         this.dragon.setHealth(1.0F);
      } else {
         this.dragon.setHealth(0.0F);
      }

   }

   public void beginPhase() {
      this.target = null;
      this.ticks = 0;
   }

   public float getMaxYAcceleration() {
      return 3.0F;
   }

   @Nullable
   public Vec3d getPathTarget() {
      return this.target;
   }

   public PhaseType getType() {
      return PhaseType.DYING;
   }
}
