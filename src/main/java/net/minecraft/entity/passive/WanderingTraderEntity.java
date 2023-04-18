package net.minecraft.entity.passive;

import java.util.EnumSet;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.HoldInHandsGoal;
import net.minecraft.entity.ai.goal.LookAtCustomerGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.StopAndLookAtEntityGoal;
import net.minecraft.entity.ai.goal.StopFollowingCustomerGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.entity.mob.ZoglinEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WanderingTraderEntity extends MerchantEntity {
   private static final int field_30629 = 5;
   @Nullable
   private BlockPos wanderTarget;
   private int despawnDelay;

   public WanderingTraderEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   protected void initGoals() {
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(0, new HoldInHandsGoal(this, PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.INVISIBILITY), SoundEvents.ENTITY_WANDERING_TRADER_DISAPPEARED, (wanderingTrader) -> {
         return this.world.isNight() && !wanderingTrader.isInvisible();
      }));
      this.goalSelector.add(0, new HoldInHandsGoal(this, new ItemStack(Items.MILK_BUCKET), SoundEvents.ENTITY_WANDERING_TRADER_REAPPEARED, (wanderingTrader) -> {
         return this.world.isDay() && wanderingTrader.isInvisible();
      }));
      this.goalSelector.add(1, new StopFollowingCustomerGoal(this));
      this.goalSelector.add(1, new FleeEntityGoal(this, ZombieEntity.class, 8.0F, 0.5, 0.5));
      this.goalSelector.add(1, new FleeEntityGoal(this, EvokerEntity.class, 12.0F, 0.5, 0.5));
      this.goalSelector.add(1, new FleeEntityGoal(this, VindicatorEntity.class, 8.0F, 0.5, 0.5));
      this.goalSelector.add(1, new FleeEntityGoal(this, VexEntity.class, 8.0F, 0.5, 0.5));
      this.goalSelector.add(1, new FleeEntityGoal(this, PillagerEntity.class, 15.0F, 0.5, 0.5));
      this.goalSelector.add(1, new FleeEntityGoal(this, IllusionerEntity.class, 12.0F, 0.5, 0.5));
      this.goalSelector.add(1, new FleeEntityGoal(this, ZoglinEntity.class, 10.0F, 0.5, 0.5));
      this.goalSelector.add(1, new EscapeDangerGoal(this, 0.5));
      this.goalSelector.add(1, new LookAtCustomerGoal(this));
      this.goalSelector.add(2, new WanderToTargetGoal(this, 2.0, 0.35));
      this.goalSelector.add(4, new GoToWalkTargetGoal(this, 0.35));
      this.goalSelector.add(8, new WanderAroundFarGoal(this, 0.35));
      this.goalSelector.add(9, new StopAndLookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F));
      this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
   }

   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      return null;
   }

   public boolean isLeveledMerchant() {
      return false;
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack lv = player.getStackInHand(hand);
      if (!lv.isOf(Items.VILLAGER_SPAWN_EGG) && this.isAlive() && !this.hasCustomer() && !this.isBaby()) {
         if (hand == Hand.MAIN_HAND) {
            player.incrementStat(Stats.TALKED_TO_VILLAGER);
         }

         if (this.getOffers().isEmpty()) {
            return ActionResult.success(this.world.isClient);
         } else {
            if (!this.world.isClient) {
               this.setCustomer(player);
               this.sendOffers(player, this.getDisplayName(), 1);
            }

            return ActionResult.success(this.world.isClient);
         }
      } else {
         return super.interactMob(player, hand);
      }
   }

   protected void fillRecipes() {
      TradeOffers.Factory[] lvs = (TradeOffers.Factory[])TradeOffers.WANDERING_TRADER_TRADES.get(1);
      TradeOffers.Factory[] lvs2 = (TradeOffers.Factory[])TradeOffers.WANDERING_TRADER_TRADES.get(2);
      if (lvs != null && lvs2 != null) {
         TradeOfferList lv = this.getOffers();
         this.fillRecipesFromPool(lv, lvs, 5);
         int i = this.random.nextInt(lvs2.length);
         TradeOffers.Factory lv2 = lvs2[i];
         TradeOffer lv3 = lv2.create(this, this.random);
         if (lv3 != null) {
            lv.add(lv3);
         }

      }
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("DespawnDelay", this.despawnDelay);
      if (this.wanderTarget != null) {
         nbt.put("WanderTarget", NbtHelper.fromBlockPos(this.wanderTarget));
      }

   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("DespawnDelay", NbtElement.NUMBER_TYPE)) {
         this.despawnDelay = nbt.getInt("DespawnDelay");
      }

      if (nbt.contains("WanderTarget")) {
         this.wanderTarget = NbtHelper.toBlockPos(nbt.getCompound("WanderTarget"));
      }

      this.setBreedingAge(Math.max(0, this.getBreedingAge()));
   }

   public boolean canImmediatelyDespawn(double distanceSquared) {
      return false;
   }

   protected void afterUsing(TradeOffer offer) {
      if (offer.shouldRewardPlayerExperience()) {
         int i = 3 + this.random.nextInt(4);
         this.world.spawnEntity(new ExperienceOrbEntity(this.world, this.getX(), this.getY() + 0.5, this.getZ(), i));
      }

   }

   protected SoundEvent getAmbientSound() {
      return this.hasCustomer() ? SoundEvents.ENTITY_WANDERING_TRADER_TRADE : SoundEvents.ENTITY_WANDERING_TRADER_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_WANDERING_TRADER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_WANDERING_TRADER_DEATH;
   }

   protected SoundEvent getDrinkSound(ItemStack stack) {
      return stack.isOf(Items.MILK_BUCKET) ? SoundEvents.ENTITY_WANDERING_TRADER_DRINK_MILK : SoundEvents.ENTITY_WANDERING_TRADER_DRINK_POTION;
   }

   protected SoundEvent getTradingSound(boolean sold) {
      return sold ? SoundEvents.ENTITY_WANDERING_TRADER_YES : SoundEvents.ENTITY_WANDERING_TRADER_NO;
   }

   public SoundEvent getYesSound() {
      return SoundEvents.ENTITY_WANDERING_TRADER_YES;
   }

   public void setDespawnDelay(int despawnDelay) {
      this.despawnDelay = despawnDelay;
   }

   public int getDespawnDelay() {
      return this.despawnDelay;
   }

   public void tickMovement() {
      super.tickMovement();
      if (!this.world.isClient) {
         this.tickDespawnDelay();
      }

   }

   private void tickDespawnDelay() {
      if (this.despawnDelay > 0 && !this.hasCustomer() && --this.despawnDelay == 0) {
         this.discard();
      }

   }

   public void setWanderTarget(@Nullable BlockPos wanderTarget) {
      this.wanderTarget = wanderTarget;
   }

   @Nullable
   BlockPos getWanderTarget() {
      return this.wanderTarget;
   }

   private class WanderToTargetGoal extends Goal {
      final WanderingTraderEntity trader;
      final double proximityDistance;
      final double speed;

      WanderToTargetGoal(WanderingTraderEntity trader, double proximityDistance, double speed) {
         this.trader = trader;
         this.proximityDistance = proximityDistance;
         this.speed = speed;
         this.setControls(EnumSet.of(Goal.Control.MOVE));
      }

      public void stop() {
         this.trader.setWanderTarget((BlockPos)null);
         WanderingTraderEntity.this.navigation.stop();
      }

      public boolean canStart() {
         BlockPos lv = this.trader.getWanderTarget();
         return lv != null && this.isTooFarFrom(lv, this.proximityDistance);
      }

      public void tick() {
         BlockPos lv = this.trader.getWanderTarget();
         if (lv != null && WanderingTraderEntity.this.navigation.isIdle()) {
            if (this.isTooFarFrom(lv, 10.0)) {
               Vec3d lv2 = (new Vec3d((double)lv.getX() - this.trader.getX(), (double)lv.getY() - this.trader.getY(), (double)lv.getZ() - this.trader.getZ())).normalize();
               Vec3d lv3 = lv2.multiply(10.0).add(this.trader.getX(), this.trader.getY(), this.trader.getZ());
               WanderingTraderEntity.this.navigation.startMovingTo(lv3.x, lv3.y, lv3.z, this.speed);
            } else {
               WanderingTraderEntity.this.navigation.startMovingTo((double)lv.getX(), (double)lv.getY(), (double)lv.getZ(), this.speed);
            }
         }

      }

      private boolean isTooFarFrom(BlockPos pos, double proximityDistance) {
         return !pos.isWithinDistance(this.trader.getPos(), proximityDistance);
      }
   }
}
