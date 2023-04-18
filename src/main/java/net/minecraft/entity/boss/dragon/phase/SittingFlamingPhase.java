package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class SittingFlamingPhase extends AbstractSittingPhase {
   private static final int DURATION = 200;
   private static final int MAX_TIMES_RUN = 4;
   private static final int DRAGON_BREATH_MAX_TICK = 10;
   private int ticks;
   private int timesRun;
   @Nullable
   private AreaEffectCloudEntity dragonBreathEntity;

   public SittingFlamingPhase(EnderDragonEntity arg) {
      super(arg);
   }

   public void clientTick() {
      ++this.ticks;
      if (this.ticks % 2 == 0 && this.ticks < 10) {
         Vec3d lv = this.dragon.getRotationVectorFromPhase(1.0F).normalize();
         lv.rotateY(-0.7853982F);
         double d = this.dragon.head.getX();
         double e = this.dragon.head.getBodyY(0.5);
         double f = this.dragon.head.getZ();

         for(int i = 0; i < 8; ++i) {
            double g = d + this.dragon.getRandom().nextGaussian() / 2.0;
            double h = e + this.dragon.getRandom().nextGaussian() / 2.0;
            double j = f + this.dragon.getRandom().nextGaussian() / 2.0;

            for(int k = 0; k < 6; ++k) {
               this.dragon.world.addParticle(ParticleTypes.DRAGON_BREATH, g, h, j, -lv.x * 0.07999999821186066 * (double)k, -lv.y * 0.6000000238418579, -lv.z * 0.07999999821186066 * (double)k);
            }

            lv.rotateY(0.19634955F);
         }
      }

   }

   public void serverTick() {
      ++this.ticks;
      if (this.ticks >= 200) {
         if (this.timesRun >= 4) {
            this.dragon.getPhaseManager().setPhase(PhaseType.TAKEOFF);
         } else {
            this.dragon.getPhaseManager().setPhase(PhaseType.SITTING_SCANNING);
         }
      } else if (this.ticks == 10) {
         Vec3d lv = (new Vec3d(this.dragon.head.getX() - this.dragon.getX(), 0.0, this.dragon.head.getZ() - this.dragon.getZ())).normalize();
         float f = 5.0F;
         double d = this.dragon.head.getX() + lv.x * 5.0 / 2.0;
         double e = this.dragon.head.getZ() + lv.z * 5.0 / 2.0;
         double g = this.dragon.head.getBodyY(0.5);
         double h = g;
         BlockPos.Mutable lv2 = new BlockPos.Mutable(d, g, e);

         while(this.dragon.world.isAir(lv2)) {
            --h;
            if (h < 0.0) {
               h = g;
               break;
            }

            lv2.set(d, h, e);
         }

         h = (double)(MathHelper.floor(h) + 1);
         this.dragonBreathEntity = new AreaEffectCloudEntity(this.dragon.world, d, h, e);
         this.dragonBreathEntity.setOwner(this.dragon);
         this.dragonBreathEntity.setRadius(5.0F);
         this.dragonBreathEntity.setDuration(200);
         this.dragonBreathEntity.setParticleType(ParticleTypes.DRAGON_BREATH);
         this.dragonBreathEntity.addEffect(new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE));
         this.dragon.world.spawnEntity(this.dragonBreathEntity);
      }

   }

   public void beginPhase() {
      this.ticks = 0;
      ++this.timesRun;
   }

   public void endPhase() {
      if (this.dragonBreathEntity != null) {
         this.dragonBreathEntity.discard();
         this.dragonBreathEntity = null;
      }

   }

   public PhaseType getType() {
      return PhaseType.SITTING_FLAMING;
   }

   public void reset() {
      this.timesRun = 0;
   }
}
