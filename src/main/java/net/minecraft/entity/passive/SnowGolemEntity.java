package net.minecraft.entity.passive;

import java.util.function.Consumer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Shearable;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class SnowGolemEntity extends GolemEntity implements Shearable, RangedAttackMob {
   private static final TrackedData SNOW_GOLEM_FLAGS;
   private static final byte HAS_PUMPKIN_FLAG = 16;
   private static final float EYE_HEIGHT = 1.7F;

   public SnowGolemEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   protected void initGoals() {
      this.goalSelector.add(1, new ProjectileAttackGoal(this, 1.25, 20, 10.0F));
      this.goalSelector.add(2, new WanderAroundFarGoal(this, 1.0, 1.0000001E-5F));
      this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
      this.goalSelector.add(4, new LookAroundGoal(this));
      this.targetSelector.add(1, new ActiveTargetGoal(this, MobEntity.class, 10, true, false, (entity) -> {
         return entity instanceof Monster;
      }));
   }

   public static DefaultAttributeContainer.Builder createSnowGolemAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 4.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.20000000298023224);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(SNOW_GOLEM_FLAGS, (byte)16);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putBoolean("Pumpkin", this.hasPumpkin());
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("Pumpkin")) {
         this.setHasPumpkin(nbt.getBoolean("Pumpkin"));
      }

   }

   public boolean hurtByWater() {
      return true;
   }

   public void tickMovement() {
      super.tickMovement();
      if (!this.world.isClient) {
         if (this.world.getBiome(this.getBlockPos()).isIn(BiomeTags.SNOW_GOLEM_MELTS)) {
            this.damage(this.getDamageSources().onFire(), 1.0F);
         }

         if (!this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            return;
         }

         BlockState lv = Blocks.SNOW.getDefaultState();

         for(int i = 0; i < 4; ++i) {
            int j = MathHelper.floor(this.getX() + (double)((float)(i % 2 * 2 - 1) * 0.25F));
            int k = MathHelper.floor(this.getY());
            int l = MathHelper.floor(this.getZ() + (double)((float)(i / 2 % 2 * 2 - 1) * 0.25F));
            BlockPos lv2 = new BlockPos(j, k, l);
            if (this.world.getBlockState(lv2).isAir() && lv.canPlaceAt(this.world, lv2)) {
               this.world.setBlockState(lv2, lv);
               this.world.emitGameEvent(GameEvent.BLOCK_PLACE, lv2, GameEvent.Emitter.of(this, lv));
            }
         }
      }

   }

   public void attack(LivingEntity target, float pullProgress) {
      SnowballEntity lv = new SnowballEntity(this.world, this);
      double d = target.getEyeY() - 1.100000023841858;
      double e = target.getX() - this.getX();
      double g = d - lv.getY();
      double h = target.getZ() - this.getZ();
      double i = Math.sqrt(e * e + h * h) * 0.20000000298023224;
      lv.setVelocity(e, g + i, h, 1.6F, 12.0F);
      this.playSound(SoundEvents.ENTITY_SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
      this.world.spawnEntity(lv);
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 1.7F;
   }

   protected ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack lv = player.getStackInHand(hand);
      if (lv.isOf(Items.SHEARS) && this.isShearable()) {
         this.sheared(SoundCategory.PLAYERS);
         this.emitGameEvent(GameEvent.SHEAR, player);
         if (!this.world.isClient) {
            lv.damage(1, (LivingEntity)player, (Consumer)((playerx) -> {
               playerx.sendToolBreakStatus(hand);
            }));
         }

         return ActionResult.success(this.world.isClient);
      } else {
         return ActionResult.PASS;
      }
   }

   public void sheared(SoundCategory shearedSoundCategory) {
      this.world.playSoundFromEntity((PlayerEntity)null, this, SoundEvents.ENTITY_SNOW_GOLEM_SHEAR, shearedSoundCategory, 1.0F, 1.0F);
      if (!this.world.isClient()) {
         this.setHasPumpkin(false);
         this.dropStack(new ItemStack(Items.CARVED_PUMPKIN), 1.7F);
      }

   }

   public boolean isShearable() {
      return this.isAlive() && this.hasPumpkin();
   }

   public boolean hasPumpkin() {
      return ((Byte)this.dataTracker.get(SNOW_GOLEM_FLAGS) & 16) != 0;
   }

   public void setHasPumpkin(boolean hasPumpkin) {
      byte b = (Byte)this.dataTracker.get(SNOW_GOLEM_FLAGS);
      if (hasPumpkin) {
         this.dataTracker.set(SNOW_GOLEM_FLAGS, (byte)(b | 16));
      } else {
         this.dataTracker.set(SNOW_GOLEM_FLAGS, (byte)(b & -17));
      }

   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_SNOW_GOLEM_AMBIENT;
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_SNOW_GOLEM_HURT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_SNOW_GOLEM_DEATH;
   }

   public Vec3d getLeashOffset() {
      return new Vec3d(0.0, (double)(0.75F * this.getStandingEyeHeight()), (double)(this.getWidth() * 0.4F));
   }

   static {
      SNOW_GOLEM_FLAGS = DataTracker.registerData(SnowGolemEntity.class, TrackedDataHandlerRegistry.BYTE);
   }
}
