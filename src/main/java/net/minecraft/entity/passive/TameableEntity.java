package net.minecraft.entity.passive;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.EntityView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class TameableEntity extends AnimalEntity implements Tameable {
   protected static final TrackedData TAMEABLE_FLAGS;
   protected static final TrackedData OWNER_UUID;
   private boolean sitting;

   protected TameableEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.onTamedChanged();
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(TAMEABLE_FLAGS, (byte)0);
      this.dataTracker.startTracking(OWNER_UUID, Optional.empty());
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      if (this.getOwnerUuid() != null) {
         nbt.putUuid("Owner", this.getOwnerUuid());
      }

      nbt.putBoolean("Sitting", this.sitting);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      UUID uUID;
      if (nbt.containsUuid("Owner")) {
         uUID = nbt.getUuid("Owner");
      } else {
         String string = nbt.getString("Owner");
         uUID = ServerConfigHandler.getPlayerUuidByName(this.getServer(), string);
      }

      if (uUID != null) {
         try {
            this.setOwnerUuid(uUID);
            this.setTamed(true);
         } catch (Throwable var4) {
            this.setTamed(false);
         }
      }

      this.sitting = nbt.getBoolean("Sitting");
      this.setInSittingPose(this.sitting);
   }

   public boolean canBeLeashedBy(PlayerEntity player) {
      return !this.isLeashed();
   }

   protected void showEmoteParticle(boolean positive) {
      ParticleEffect lv = ParticleTypes.HEART;
      if (!positive) {
         lv = ParticleTypes.SMOKE;
      }

      for(int i = 0; i < 7; ++i) {
         double d = this.random.nextGaussian() * 0.02;
         double e = this.random.nextGaussian() * 0.02;
         double f = this.random.nextGaussian() * 0.02;
         this.world.addParticle(lv, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), d, e, f);
      }

   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES) {
         this.showEmoteParticle(true);
      } else if (status == EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES) {
         this.showEmoteParticle(false);
      } else {
         super.handleStatus(status);
      }

   }

   public boolean isTamed() {
      return ((Byte)this.dataTracker.get(TAMEABLE_FLAGS) & 4) != 0;
   }

   public void setTamed(boolean tamed) {
      byte b = (Byte)this.dataTracker.get(TAMEABLE_FLAGS);
      if (tamed) {
         this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b | 4));
      } else {
         this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b & -5));
      }

      this.onTamedChanged();
   }

   protected void onTamedChanged() {
   }

   public boolean isInSittingPose() {
      return ((Byte)this.dataTracker.get(TAMEABLE_FLAGS) & 1) != 0;
   }

   public void setInSittingPose(boolean inSittingPose) {
      byte b = (Byte)this.dataTracker.get(TAMEABLE_FLAGS);
      if (inSittingPose) {
         this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b | 1));
      } else {
         this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b & -2));
      }

   }

   @Nullable
   public UUID getOwnerUuid() {
      return (UUID)((Optional)this.dataTracker.get(OWNER_UUID)).orElse((Object)null);
   }

   public void setOwnerUuid(@Nullable UUID uuid) {
      this.dataTracker.set(OWNER_UUID, Optional.ofNullable(uuid));
   }

   public void setOwner(PlayerEntity player) {
      this.setTamed(true);
      this.setOwnerUuid(player.getUuid());
      if (player instanceof ServerPlayerEntity) {
         Criteria.TAME_ANIMAL.trigger((ServerPlayerEntity)player, this);
      }

   }

   public boolean canTarget(LivingEntity target) {
      return this.isOwner(target) ? false : super.canTarget(target);
   }

   public boolean isOwner(LivingEntity entity) {
      return entity == this.getOwner();
   }

   public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
      return true;
   }

   public AbstractTeam getScoreboardTeam() {
      if (this.isTamed()) {
         LivingEntity lv = this.getOwner();
         if (lv != null) {
            return lv.getScoreboardTeam();
         }
      }

      return super.getScoreboardTeam();
   }

   public boolean isTeammate(Entity other) {
      if (this.isTamed()) {
         LivingEntity lv = this.getOwner();
         if (other == lv) {
            return true;
         }

         if (lv != null) {
            return lv.isTeammate(other);
         }
      }

      return super.isTeammate(other);
   }

   public void onDeath(DamageSource damageSource) {
      if (!this.world.isClient && this.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES) && this.getOwner() instanceof ServerPlayerEntity) {
         this.getOwner().sendMessage(this.getDamageTracker().getDeathMessage());
      }

      super.onDeath(damageSource);
   }

   public boolean isSitting() {
      return this.sitting;
   }

   public void setSitting(boolean sitting) {
      this.sitting = sitting;
   }

   // $FF: synthetic method
   public EntityView method_48926() {
      return super.getWorld();
   }

   static {
      TAMEABLE_FLAGS = DataTracker.registerData(TameableEntity.class, TrackedDataHandlerRegistry.BYTE);
      OWNER_UUID = DataTracker.registerData(TameableEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
   }
}
