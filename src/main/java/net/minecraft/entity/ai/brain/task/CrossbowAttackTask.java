package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;

public class CrossbowAttackTask extends MultiTickTask {
   private static final int RUN_TIME = 1200;
   private int chargingCooldown;
   private CrossbowState state;

   public CrossbowAttackTask() {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT), 1200);
      this.state = CrossbowAttackTask.CrossbowState.UNCHARGED;
   }

   protected boolean shouldRun(ServerWorld arg, MobEntity arg2) {
      LivingEntity lv = getAttackTarget(arg2);
      return arg2.isHolding(Items.CROSSBOW) && LookTargetUtil.isVisibleInMemory(arg2, lv) && LookTargetUtil.isTargetWithinAttackRange(arg2, lv, 0);
   }

   protected boolean shouldKeepRunning(ServerWorld arg, MobEntity arg2, long l) {
      return arg2.getBrain().hasMemoryModule(MemoryModuleType.ATTACK_TARGET) && this.shouldRun(arg, arg2);
   }

   protected void keepRunning(ServerWorld arg, MobEntity arg2, long l) {
      LivingEntity lv = getAttackTarget(arg2);
      this.setLookTarget(arg2, lv);
      this.tickState(arg2, lv);
   }

   protected void finishRunning(ServerWorld arg, MobEntity arg2, long l) {
      if (arg2.isUsingItem()) {
         arg2.clearActiveItem();
      }

      if (arg2.isHolding(Items.CROSSBOW)) {
         ((CrossbowUser)arg2).setCharging(false);
         CrossbowItem.setCharged(arg2.getActiveItem(), false);
      }

   }

   private void tickState(MobEntity entity, LivingEntity target) {
      if (this.state == CrossbowAttackTask.CrossbowState.UNCHARGED) {
         entity.setCurrentHand(ProjectileUtil.getHandPossiblyHolding(entity, Items.CROSSBOW));
         this.state = CrossbowAttackTask.CrossbowState.CHARGING;
         ((CrossbowUser)entity).setCharging(true);
      } else if (this.state == CrossbowAttackTask.CrossbowState.CHARGING) {
         if (!entity.isUsingItem()) {
            this.state = CrossbowAttackTask.CrossbowState.UNCHARGED;
         }

         int i = entity.getItemUseTime();
         ItemStack lv = entity.getActiveItem();
         if (i >= CrossbowItem.getPullTime(lv)) {
            entity.stopUsingItem();
            this.state = CrossbowAttackTask.CrossbowState.CHARGED;
            this.chargingCooldown = 20 + entity.getRandom().nextInt(20);
            ((CrossbowUser)entity).setCharging(false);
         }
      } else if (this.state == CrossbowAttackTask.CrossbowState.CHARGED) {
         --this.chargingCooldown;
         if (this.chargingCooldown == 0) {
            this.state = CrossbowAttackTask.CrossbowState.READY_TO_ATTACK;
         }
      } else if (this.state == CrossbowAttackTask.CrossbowState.READY_TO_ATTACK) {
         ((RangedAttackMob)entity).attack(target, 1.0F);
         ItemStack lv2 = entity.getStackInHand(ProjectileUtil.getHandPossiblyHolding(entity, Items.CROSSBOW));
         CrossbowItem.setCharged(lv2, false);
         this.state = CrossbowAttackTask.CrossbowState.UNCHARGED;
      }

   }

   private void setLookTarget(MobEntity entity, LivingEntity target) {
      entity.getBrain().remember(MemoryModuleType.LOOK_TARGET, (Object)(new EntityLookTarget(target, true)));
   }

   private static LivingEntity getAttackTarget(LivingEntity entity) {
      return (LivingEntity)entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).get();
   }

   // $FF: synthetic method
   protected void finishRunning(ServerWorld world, LivingEntity entity, long time) {
      this.finishRunning(world, (MobEntity)entity, time);
   }

   // $FF: synthetic method
   protected void keepRunning(ServerWorld world, LivingEntity entity, long time) {
      this.keepRunning(world, (MobEntity)entity, time);
   }

   private static enum CrossbowState {
      UNCHARGED,
      CHARGING,
      CHARGED,
      READY_TO_ATTACK;

      // $FF: synthetic method
      private static CrossbowState[] method_36616() {
         return new CrossbowState[]{UNCHARGED, CHARGING, CHARGED, READY_TO_ATTACK};
      }
   }
}
