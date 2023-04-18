package net.minecraft.entity.passive;

import java.util.EnumSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TraderLlamaEntity extends LlamaEntity {
   private int despawnDelay = 47999;

   public TraderLlamaEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public boolean isTrader() {
      return true;
   }

   @Nullable
   protected LlamaEntity createChild() {
      return (LlamaEntity)EntityType.TRADER_LLAMA.create(this.world);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("DespawnDelay", this.despawnDelay);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("DespawnDelay", NbtElement.NUMBER_TYPE)) {
         this.despawnDelay = nbt.getInt("DespawnDelay");
      }

   }

   protected void initGoals() {
      super.initGoals();
      this.goalSelector.add(1, new EscapeDangerGoal(this, 2.0));
      this.targetSelector.add(1, new DefendTraderGoal(this));
   }

   public void setDespawnDelay(int despawnDelay) {
      this.despawnDelay = despawnDelay;
   }

   protected void putPlayerOnBack(PlayerEntity player) {
      Entity lv = this.getHoldingEntity();
      if (!(lv instanceof WanderingTraderEntity)) {
         super.putPlayerOnBack(player);
      }
   }

   public void tickMovement() {
      super.tickMovement();
      if (!this.world.isClient) {
         this.tryDespawn();
      }

   }

   private void tryDespawn() {
      if (this.canDespawn()) {
         this.despawnDelay = this.heldByTrader() ? ((WanderingTraderEntity)this.getHoldingEntity()).getDespawnDelay() - 1 : this.despawnDelay - 1;
         if (this.despawnDelay <= 0) {
            this.detachLeash(true, false);
            this.discard();
         }

      }
   }

   private boolean canDespawn() {
      return !this.isTame() && !this.leashedByPlayer() && !this.hasPlayerRider();
   }

   private boolean heldByTrader() {
      return this.getHoldingEntity() instanceof WanderingTraderEntity;
   }

   private boolean leashedByPlayer() {
      return this.isLeashed() && !this.heldByTrader();
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      if (spawnReason == SpawnReason.EVENT) {
         this.setBreedingAge(0);
      }

      if (entityData == null) {
         entityData = new PassiveEntity.PassiveData(false);
      }

      return super.initialize(world, difficulty, spawnReason, (EntityData)entityData, entityNbt);
   }

   protected static class DefendTraderGoal extends TrackTargetGoal {
      private final LlamaEntity llama;
      private LivingEntity offender;
      private int traderLastAttackedTime;

      public DefendTraderGoal(LlamaEntity llama) {
         super(llama, false);
         this.llama = llama;
         this.setControls(EnumSet.of(Goal.Control.TARGET));
      }

      public boolean canStart() {
         if (!this.llama.isLeashed()) {
            return false;
         } else {
            Entity lv = this.llama.getHoldingEntity();
            if (!(lv instanceof WanderingTraderEntity)) {
               return false;
            } else {
               WanderingTraderEntity lv2 = (WanderingTraderEntity)lv;
               this.offender = lv2.getAttacker();
               int i = lv2.getLastAttackedTime();
               return i != this.traderLastAttackedTime && this.canTrack(this.offender, TargetPredicate.DEFAULT);
            }
         }
      }

      public void start() {
         this.mob.setTarget(this.offender);
         Entity lv = this.llama.getHoldingEntity();
         if (lv instanceof WanderingTraderEntity) {
            this.traderLastAttackedTime = ((WanderingTraderEntity)lv).getLastAttackedTime();
         }

         super.start();
      }
   }
}
