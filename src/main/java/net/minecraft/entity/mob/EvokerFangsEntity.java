package net.minecraft.entity.mob;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EvokerFangsEntity extends Entity implements Ownable {
   public static final int field_30662 = 20;
   public static final int field_30663 = 2;
   public static final int field_30664 = 14;
   private int warmup;
   private boolean startedAttack;
   private int ticksLeft;
   private boolean playingAnimation;
   @Nullable
   private LivingEntity owner;
   @Nullable
   private UUID ownerUuid;

   public EvokerFangsEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.ticksLeft = 22;
   }

   public EvokerFangsEntity(World world, double x, double y, double z, float yaw, int warmup, LivingEntity owner) {
      this(EntityType.EVOKER_FANGS, world);
      this.warmup = warmup;
      this.setOwner(owner);
      this.setYaw(yaw * 57.295776F);
      this.setPosition(x, y, z);
   }

   protected void initDataTracker() {
   }

   public void setOwner(@Nullable LivingEntity owner) {
      this.owner = owner;
      this.ownerUuid = owner == null ? null : owner.getUuid();
   }

   @Nullable
   public LivingEntity getOwner() {
      if (this.owner == null && this.ownerUuid != null && this.world instanceof ServerWorld) {
         Entity lv = ((ServerWorld)this.world).getEntity(this.ownerUuid);
         if (lv instanceof LivingEntity) {
            this.owner = (LivingEntity)lv;
         }
      }

      return this.owner;
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      this.warmup = nbt.getInt("Warmup");
      if (nbt.containsUuid("Owner")) {
         this.ownerUuid = nbt.getUuid("Owner");
      }

   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      nbt.putInt("Warmup", this.warmup);
      if (this.ownerUuid != null) {
         nbt.putUuid("Owner", this.ownerUuid);
      }

   }

   public void tick() {
      super.tick();
      if (this.world.isClient) {
         if (this.playingAnimation) {
            --this.ticksLeft;
            if (this.ticksLeft == 14) {
               for(int i = 0; i < 12; ++i) {
                  double d = this.getX() + (this.random.nextDouble() * 2.0 - 1.0) * (double)this.getWidth() * 0.5;
                  double e = this.getY() + 0.05 + this.random.nextDouble();
                  double f = this.getZ() + (this.random.nextDouble() * 2.0 - 1.0) * (double)this.getWidth() * 0.5;
                  double g = (this.random.nextDouble() * 2.0 - 1.0) * 0.3;
                  double h = 0.3 + this.random.nextDouble() * 0.3;
                  double j = (this.random.nextDouble() * 2.0 - 1.0) * 0.3;
                  this.world.addParticle(ParticleTypes.CRIT, d, e + 1.0, f, g, h, j);
               }
            }
         }
      } else if (--this.warmup < 0) {
         if (this.warmup == -8) {
            List list = this.world.getNonSpectatingEntities(LivingEntity.class, this.getBoundingBox().expand(0.2, 0.0, 0.2));
            Iterator var15 = list.iterator();

            while(var15.hasNext()) {
               LivingEntity lv = (LivingEntity)var15.next();
               this.damage(lv);
            }
         }

         if (!this.startedAttack) {
            this.world.sendEntityStatus(this, EntityStatuses.PLAY_ATTACK_SOUND);
            this.startedAttack = true;
         }

         if (--this.ticksLeft < 0) {
            this.discard();
         }
      }

   }

   private void damage(LivingEntity target) {
      LivingEntity lv = this.getOwner();
      if (target.isAlive() && !target.isInvulnerable() && target != lv) {
         if (lv == null) {
            target.damage(this.getDamageSources().magic(), 6.0F);
         } else {
            if (lv.isTeammate(target)) {
               return;
            }

            target.damage(this.getDamageSources().indirectMagic(this, lv), 6.0F);
         }

      }
   }

   public void handleStatus(byte status) {
      super.handleStatus(status);
      if (status == EntityStatuses.PLAY_ATTACK_SOUND) {
         this.playingAnimation = true;
         if (!this.isSilent()) {
            this.world.playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_EVOKER_FANGS_ATTACK, this.getSoundCategory(), 1.0F, this.random.nextFloat() * 0.2F + 0.85F, false);
         }
      }

   }

   public float getAnimationProgress(float tickDelta) {
      if (!this.playingAnimation) {
         return 0.0F;
      } else {
         int i = this.ticksLeft - 2;
         return i <= 0 ? 1.0F : 1.0F - ((float)i - tickDelta) / 20.0F;
      }
   }

   // $FF: synthetic method
   @Nullable
   public Entity getOwner() {
      return this.getOwner();
   }
}
