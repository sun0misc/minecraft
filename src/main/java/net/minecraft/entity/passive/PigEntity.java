package net.minecraft.entity.passive;

import com.google.common.collect.UnmodifiableIterator;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PigEntity extends AnimalEntity implements ItemSteerable, Saddleable {
   private static final TrackedData SADDLED;
   private static final TrackedData BOOST_TIME;
   private static final Ingredient BREEDING_INGREDIENT;
   private final SaddledComponent saddledComponent;

   public PigEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.saddledComponent = new SaddledComponent(this.dataTracker, BOOST_TIME, SADDLED);
   }

   protected void initGoals() {
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(1, new EscapeDangerGoal(this, 1.25));
      this.goalSelector.add(3, new AnimalMateGoal(this, 1.0));
      this.goalSelector.add(4, new TemptGoal(this, 1.2, Ingredient.ofItems(Items.CARROT_ON_A_STICK), false));
      this.goalSelector.add(4, new TemptGoal(this, 1.2, BREEDING_INGREDIENT, false));
      this.goalSelector.add(5, new FollowParentGoal(this, 1.1));
      this.goalSelector.add(6, new WanderAroundFarGoal(this, 1.0));
      this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
      this.goalSelector.add(8, new LookAroundGoal(this));
   }

   public static DefaultAttributeContainer.Builder createPigAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25);
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      if (this.isSaddled()) {
         Entity var2 = this.getFirstPassenger();
         if (var2 instanceof PlayerEntity) {
            PlayerEntity lv = (PlayerEntity)var2;
            if (lv.getMainHandStack().isOf(Items.CARROT_ON_A_STICK) || lv.getOffHandStack().isOf(Items.CARROT_ON_A_STICK)) {
               return lv;
            }
         }
      }

      return null;
   }

   public void onTrackedDataSet(TrackedData data) {
      if (BOOST_TIME.equals(data) && this.world.isClient) {
         this.saddledComponent.boost();
      }

      super.onTrackedDataSet(data);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(SADDLED, false);
      this.dataTracker.startTracking(BOOST_TIME, 0);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      this.saddledComponent.writeNbt(nbt);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.saddledComponent.readNbt(nbt);
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_PIG_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_PIG_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_PIG_DEATH;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.ENTITY_PIG_STEP, 0.15F, 1.0F);
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      boolean bl = this.isBreedingItem(player.getStackInHand(hand));
      if (!bl && this.isSaddled() && !this.hasPassengers() && !player.shouldCancelInteraction()) {
         if (!this.world.isClient) {
            player.startRiding(this);
         }

         return ActionResult.success(this.world.isClient);
      } else {
         ActionResult lv = super.interactMob(player, hand);
         if (!lv.isAccepted()) {
            ItemStack lv2 = player.getStackInHand(hand);
            return lv2.isOf(Items.SADDLE) ? lv2.useOnEntity(player, this, hand) : ActionResult.PASS;
         } else {
            return lv;
         }
      }
   }

   public boolean canBeSaddled() {
      return this.isAlive() && !this.isBaby();
   }

   protected void dropInventory() {
      super.dropInventory();
      if (this.isSaddled()) {
         this.dropItem(Items.SADDLE);
      }

   }

   public boolean isSaddled() {
      return this.saddledComponent.isSaddled();
   }

   public void saddle(@Nullable SoundCategory sound) {
      this.saddledComponent.setSaddled(true);
      if (sound != null) {
         this.world.playSoundFromEntity((PlayerEntity)null, this, SoundEvents.ENTITY_PIG_SADDLE, sound, 0.5F, 1.0F);
      }

   }

   public Vec3d updatePassengerForDismount(LivingEntity passenger) {
      Direction lv = this.getMovementDirection();
      if (lv.getAxis() == Direction.Axis.Y) {
         return super.updatePassengerForDismount(passenger);
      } else {
         int[][] is = Dismounting.getDismountOffsets(lv);
         BlockPos lv2 = this.getBlockPos();
         BlockPos.Mutable lv3 = new BlockPos.Mutable();
         UnmodifiableIterator var6 = passenger.getPoses().iterator();

         while(var6.hasNext()) {
            EntityPose lv4 = (EntityPose)var6.next();
            Box lv5 = passenger.getBoundingBox(lv4);
            int[][] var9 = is;
            int var10 = is.length;

            for(int var11 = 0; var11 < var10; ++var11) {
               int[] js = var9[var11];
               lv3.set(lv2.getX() + js[0], lv2.getY(), lv2.getZ() + js[1]);
               double d = this.world.getDismountHeight(lv3);
               if (Dismounting.canDismountInBlock(d)) {
                  Vec3d lv6 = Vec3d.ofCenter(lv3, d);
                  if (Dismounting.canPlaceEntityAt(this.world, passenger, lv5.offset(lv6))) {
                     passenger.setPose(lv4);
                     return lv6;
                  }
               }
            }
         }

         return super.updatePassengerForDismount(passenger);
      }
   }

   public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
      if (world.getDifficulty() != Difficulty.PEACEFUL) {
         ZombifiedPiglinEntity lv = (ZombifiedPiglinEntity)EntityType.ZOMBIFIED_PIGLIN.create(world);
         if (lv != null) {
            lv.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
            lv.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
            lv.setAiDisabled(this.isAiDisabled());
            lv.setBaby(this.isBaby());
            if (this.hasCustomName()) {
               lv.setCustomName(this.getCustomName());
               lv.setCustomNameVisible(this.isCustomNameVisible());
            }

            lv.setPersistent();
            world.spawnEntity(lv);
            this.discard();
         } else {
            super.onStruckByLightning(world, lightning);
         }
      } else {
         super.onStruckByLightning(world, lightning);
      }

   }

   protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
      super.tickControlled(controllingPlayer, movementInput);
      this.setRotation(controllingPlayer.getYaw(), controllingPlayer.getPitch() * 0.5F);
      this.prevYaw = this.bodyYaw = this.headYaw = this.getYaw();
      this.saddledComponent.tickBoost();
   }

   protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput) {
      return new Vec3d(0.0, 0.0, 1.0);
   }

   protected float getSaddledSpeed(PlayerEntity controllingPlayer) {
      return (float)(this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * 0.225 * (double)this.saddledComponent.getMovementSpeedMultiplier());
   }

   public boolean consumeOnAStickItem() {
      return this.saddledComponent.boost(this.getRandom());
   }

   @Nullable
   public PigEntity createChild(ServerWorld arg, PassiveEntity arg2) {
      return (PigEntity)EntityType.PIG.create(arg);
   }

   public boolean isBreedingItem(ItemStack stack) {
      return BREEDING_INGREDIENT.test(stack);
   }

   public Vec3d getLeashOffset() {
      return new Vec3d(0.0, (double)(0.6F * this.getStandingEyeHeight()), (double)(this.getWidth() * 0.4F));
   }

   // $FF: synthetic method
   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      return this.createChild(world, entity);
   }

   static {
      SADDLED = DataTracker.registerData(PigEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      BOOST_TIME = DataTracker.registerData(PigEntity.class, TrackedDataHandlerRegistry.INTEGER);
      BREEDING_INGREDIENT = Ingredient.ofItems(Items.CARROT, Items.POTATO, Items.BEETROOT);
   }
}
