package net.minecraft.entity.passive;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarrotsBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.ai.control.JumpControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.goal.PowderSnowJumpGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class RabbitEntity extends AnimalEntity implements VariantHolder {
   public static final double field_30356 = 0.6;
   public static final double field_30357 = 0.8;
   public static final double field_30358 = 1.0;
   public static final double ESCAPE_SPEED = 2.2;
   public static final double field_30360 = 1.4;
   private static final TrackedData RABBIT_TYPE;
   private static final Identifier KILLER_BUNNY;
   public static final int field_30368 = 8;
   public static final int field_30369 = 8;
   private static final int field_30370 = 40;
   private int jumpTicks;
   private int jumpDuration;
   private boolean lastOnGround;
   private int ticksUntilJump;
   int moreCarrotTicks;

   public RabbitEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.jumpControl = new RabbitJumpControl(this);
      this.moveControl = new RabbitMoveControl(this);
      this.setSpeed(0.0);
   }

   protected void initGoals() {
      this.goalSelector.add(1, new SwimGoal(this));
      this.goalSelector.add(1, new PowderSnowJumpGoal(this, this.world));
      this.goalSelector.add(1, new EscapeDangerGoal(this, 2.2));
      this.goalSelector.add(2, new AnimalMateGoal(this, 0.8));
      this.goalSelector.add(3, new TemptGoal(this, 1.0, Ingredient.ofItems(Items.CARROT, Items.GOLDEN_CARROT, Blocks.DANDELION), false));
      this.goalSelector.add(4, new FleeGoal(this, PlayerEntity.class, 8.0F, 2.2, 2.2));
      this.goalSelector.add(4, new FleeGoal(this, WolfEntity.class, 10.0F, 2.2, 2.2));
      this.goalSelector.add(4, new FleeGoal(this, HostileEntity.class, 4.0F, 2.2, 2.2));
      this.goalSelector.add(5, new EatCarrotCropGoal(this));
      this.goalSelector.add(6, new WanderAroundFarGoal(this, 0.6));
      this.goalSelector.add(11, new LookAtEntityGoal(this, PlayerEntity.class, 10.0F));
   }

   protected float getJumpVelocity() {
      if (!this.horizontalCollision && (!this.moveControl.isMoving() || !(this.moveControl.getTargetY() > this.getY() + 0.5))) {
         Path lv = this.navigation.getCurrentPath();
         if (lv != null && !lv.isFinished()) {
            Vec3d lv2 = lv.getNodePosition(this);
            if (lv2.y > this.getY() + 0.5) {
               return 0.5F;
            }
         }

         return this.moveControl.getSpeed() <= 0.6 ? 0.2F : 0.3F;
      } else {
         return 0.5F;
      }
   }

   protected void jump() {
      super.jump();
      double d = this.moveControl.getSpeed();
      if (d > 0.0) {
         double e = this.getVelocity().horizontalLengthSquared();
         if (e < 0.01) {
            this.updateVelocity(0.1F, new Vec3d(0.0, 0.0, 1.0));
         }
      }

      if (!this.world.isClient) {
         this.world.sendEntityStatus(this, EntityStatuses.ADD_SPRINTING_PARTICLES_OR_RESET_SPAWNER_MINECART_SPAWN_DELAY);
      }

   }

   public float getJumpProgress(float delta) {
      return this.jumpDuration == 0 ? 0.0F : ((float)this.jumpTicks + delta) / (float)this.jumpDuration;
   }

   public void setSpeed(double speed) {
      this.getNavigation().setSpeed(speed);
      this.moveControl.moveTo(this.moveControl.getTargetX(), this.moveControl.getTargetY(), this.moveControl.getTargetZ(), speed);
   }

   public void setJumping(boolean jumping) {
      super.setJumping(jumping);
      if (jumping) {
         this.playSound(this.getJumpSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * 0.8F);
      }

   }

   public void startJump() {
      this.setJumping(true);
      this.jumpDuration = 10;
      this.jumpTicks = 0;
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(RABBIT_TYPE, RabbitEntity.RabbitType.BROWN.id);
   }

   public void mobTick() {
      if (this.ticksUntilJump > 0) {
         --this.ticksUntilJump;
      }

      if (this.moreCarrotTicks > 0) {
         this.moreCarrotTicks -= this.random.nextInt(3);
         if (this.moreCarrotTicks < 0) {
            this.moreCarrotTicks = 0;
         }
      }

      if (this.onGround) {
         if (!this.lastOnGround) {
            this.setJumping(false);
            this.scheduleJump();
         }

         if (this.getVariant() == RabbitEntity.RabbitType.EVIL && this.ticksUntilJump == 0) {
            LivingEntity lv = this.getTarget();
            if (lv != null && this.squaredDistanceTo(lv) < 16.0) {
               this.lookTowards(lv.getX(), lv.getZ());
               this.moveControl.moveTo(lv.getX(), lv.getY(), lv.getZ(), this.moveControl.getSpeed());
               this.startJump();
               this.lastOnGround = true;
            }
         }

         RabbitJumpControl lv2 = (RabbitJumpControl)this.jumpControl;
         if (!lv2.isActive()) {
            if (this.moveControl.isMoving() && this.ticksUntilJump == 0) {
               Path lv3 = this.navigation.getCurrentPath();
               Vec3d lv4 = new Vec3d(this.moveControl.getTargetX(), this.moveControl.getTargetY(), this.moveControl.getTargetZ());
               if (lv3 != null && !lv3.isFinished()) {
                  lv4 = lv3.getNodePosition(this);
               }

               this.lookTowards(lv4.x, lv4.z);
               this.startJump();
            }
         } else if (!lv2.canJump()) {
            this.enableJump();
         }
      }

      this.lastOnGround = this.onGround;
   }

   public boolean shouldSpawnSprintingParticles() {
      return false;
   }

   private void lookTowards(double x, double z) {
      this.setYaw((float)(MathHelper.atan2(z - this.getZ(), x - this.getX()) * 57.2957763671875) - 90.0F);
   }

   private void enableJump() {
      ((RabbitJumpControl)this.jumpControl).setCanJump(true);
   }

   private void disableJump() {
      ((RabbitJumpControl)this.jumpControl).setCanJump(false);
   }

   private void doScheduleJump() {
      if (this.moveControl.getSpeed() < 2.2) {
         this.ticksUntilJump = 10;
      } else {
         this.ticksUntilJump = 1;
      }

   }

   private void scheduleJump() {
      this.doScheduleJump();
      this.disableJump();
   }

   public void tickMovement() {
      super.tickMovement();
      if (this.jumpTicks != this.jumpDuration) {
         ++this.jumpTicks;
      } else if (this.jumpDuration != 0) {
         this.jumpTicks = 0;
         this.jumpDuration = 0;
         this.setJumping(false);
      }

   }

   public static DefaultAttributeContainer.Builder createRabbitAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 3.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30000001192092896);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("RabbitType", this.getVariant().id);
      nbt.putInt("MoreCarrotTicks", this.moreCarrotTicks);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.setVariant(RabbitEntity.RabbitType.byId(nbt.getInt("RabbitType")));
      this.moreCarrotTicks = nbt.getInt("MoreCarrotTicks");
   }

   protected SoundEvent getJumpSound() {
      return SoundEvents.ENTITY_RABBIT_JUMP;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_RABBIT_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_RABBIT_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_RABBIT_DEATH;
   }

   public boolean tryAttack(Entity target) {
      if (this.getVariant() == RabbitEntity.RabbitType.EVIL) {
         this.playSound(SoundEvents.ENTITY_RABBIT_ATTACK, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
         return target.damage(this.getDamageSources().mobAttack(this), 8.0F);
      } else {
         return target.damage(this.getDamageSources().mobAttack(this), 3.0F);
      }
   }

   public SoundCategory getSoundCategory() {
      return this.getVariant() == RabbitEntity.RabbitType.EVIL ? SoundCategory.HOSTILE : SoundCategory.NEUTRAL;
   }

   private static boolean isTempting(ItemStack stack) {
      return stack.isOf(Items.CARROT) || stack.isOf(Items.GOLDEN_CARROT) || stack.isOf(Blocks.DANDELION.asItem());
   }

   @Nullable
   public RabbitEntity createChild(ServerWorld arg, PassiveEntity arg2) {
      RabbitEntity lv = (RabbitEntity)EntityType.RABBIT.create(arg);
      if (lv != null) {
         RabbitType lv2 = getTypeFromPos(arg, this.getBlockPos());
         if (this.random.nextInt(20) != 0) {
            label22: {
               if (arg2 instanceof RabbitEntity) {
                  RabbitEntity lv3 = (RabbitEntity)arg2;
                  if (this.random.nextBoolean()) {
                     lv2 = lv3.getVariant();
                     break label22;
                  }
               }

               lv2 = this.getVariant();
            }
         }

         lv.setVariant(lv2);
      }

      return lv;
   }

   public boolean isBreedingItem(ItemStack stack) {
      return isTempting(stack);
   }

   public RabbitType getVariant() {
      return RabbitEntity.RabbitType.byId((Integer)this.dataTracker.get(RABBIT_TYPE));
   }

   public void setVariant(RabbitType arg) {
      if (arg == RabbitEntity.RabbitType.EVIL) {
         this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(8.0);
         this.goalSelector.add(4, new RabbitAttackGoal(this));
         this.targetSelector.add(1, (new RevengeGoal(this, new Class[0])).setGroupRevenge());
         this.targetSelector.add(2, new ActiveTargetGoal(this, PlayerEntity.class, true));
         this.targetSelector.add(2, new ActiveTargetGoal(this, WolfEntity.class, true));
         if (!this.hasCustomName()) {
            this.setCustomName(Text.translatable(Util.createTranslationKey("entity", KILLER_BUNNY)));
         }
      }

      this.dataTracker.set(RABBIT_TYPE, arg.id);
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      RabbitType lv = getTypeFromPos(world, this.getBlockPos());
      if (entityData instanceof RabbitData) {
         lv = ((RabbitData)entityData).type;
      } else {
         entityData = new RabbitData(lv);
      }

      this.setVariant(lv);
      return super.initialize(world, difficulty, spawnReason, (EntityData)entityData, entityNbt);
   }

   private static RabbitType getTypeFromPos(WorldAccess world, BlockPos pos) {
      RegistryEntry lv = world.getBiome(pos);
      int i = world.getRandom().nextInt(100);
      if (lv.isIn(BiomeTags.SPAWNS_WHITE_RABBITS)) {
         return i < 80 ? RabbitEntity.RabbitType.WHITE : RabbitEntity.RabbitType.WHITE_SPLOTCHED;
      } else if (lv.isIn(BiomeTags.SPAWNS_GOLD_RABBITS)) {
         return RabbitEntity.RabbitType.GOLD;
      } else {
         return i < 50 ? RabbitEntity.RabbitType.BROWN : (i < 90 ? RabbitEntity.RabbitType.SALT : RabbitEntity.RabbitType.BLACK);
      }
   }

   public static boolean canSpawn(EntityType entity, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      return world.getBlockState(pos.down()).isIn(BlockTags.RABBITS_SPAWNABLE_ON) && isLightLevelValidForNaturalSpawn(world, pos);
   }

   boolean wantsCarrots() {
      return this.moreCarrotTicks <= 0;
   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.ADD_SPRINTING_PARTICLES_OR_RESET_SPAWNER_MINECART_SPAWN_DELAY) {
         this.spawnSprintingParticles();
         this.jumpDuration = 10;
         this.jumpTicks = 0;
      } else {
         super.handleStatus(status);
      }

   }

   public Vec3d getLeashOffset() {
      return new Vec3d(0.0, (double)(0.6F * this.getStandingEyeHeight()), (double)(this.getWidth() * 0.4F));
   }

   // $FF: synthetic method
   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      return this.createChild(world, entity);
   }

   // $FF: synthetic method
   public Object getVariant() {
      return this.getVariant();
   }

   static {
      RABBIT_TYPE = DataTracker.registerData(RabbitEntity.class, TrackedDataHandlerRegistry.INTEGER);
      KILLER_BUNNY = new Identifier("killer_bunny");
   }

   public static class RabbitJumpControl extends JumpControl {
      private final RabbitEntity rabbit;
      private boolean canJump;

      public RabbitJumpControl(RabbitEntity rabbit) {
         super(rabbit);
         this.rabbit = rabbit;
      }

      public boolean isActive() {
         return this.active;
      }

      public boolean canJump() {
         return this.canJump;
      }

      public void setCanJump(boolean canJump) {
         this.canJump = canJump;
      }

      public void tick() {
         if (this.active) {
            this.rabbit.startJump();
            this.active = false;
         }

      }
   }

   private static class RabbitMoveControl extends MoveControl {
      private final RabbitEntity rabbit;
      private double rabbitSpeed;

      public RabbitMoveControl(RabbitEntity owner) {
         super(owner);
         this.rabbit = owner;
      }

      public void tick() {
         if (this.rabbit.onGround && !this.rabbit.jumping && !((RabbitJumpControl)this.rabbit.jumpControl).isActive()) {
            this.rabbit.setSpeed(0.0);
         } else if (this.isMoving()) {
            this.rabbit.setSpeed(this.rabbitSpeed);
         }

         super.tick();
      }

      public void moveTo(double x, double y, double z, double speed) {
         if (this.rabbit.isTouchingWater()) {
            speed = 1.5;
         }

         super.moveTo(x, y, z, speed);
         if (speed > 0.0) {
            this.rabbitSpeed = speed;
         }

      }
   }

   private static class EscapeDangerGoal extends net.minecraft.entity.ai.goal.EscapeDangerGoal {
      private final RabbitEntity rabbit;

      public EscapeDangerGoal(RabbitEntity rabbit, double speed) {
         super(rabbit, speed);
         this.rabbit = rabbit;
      }

      public void tick() {
         super.tick();
         this.rabbit.setSpeed(this.speed);
      }
   }

   private static class FleeGoal extends FleeEntityGoal {
      private final RabbitEntity rabbit;

      public FleeGoal(RabbitEntity rabbit, Class fleeFromType, float distance, double slowSpeed, double fastSpeed) {
         super(rabbit, fleeFromType, distance, slowSpeed, fastSpeed);
         this.rabbit = rabbit;
      }

      public boolean canStart() {
         return this.rabbit.getVariant() != RabbitEntity.RabbitType.EVIL && super.canStart();
      }
   }

   private static class EatCarrotCropGoal extends MoveToTargetPosGoal {
      private final RabbitEntity rabbit;
      private boolean wantsCarrots;
      private boolean hasTarget;

      public EatCarrotCropGoal(RabbitEntity rabbit) {
         super(rabbit, 0.699999988079071, 16);
         this.rabbit = rabbit;
      }

      public boolean canStart() {
         if (this.cooldown <= 0) {
            if (!this.rabbit.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
               return false;
            }

            this.hasTarget = false;
            this.wantsCarrots = this.rabbit.wantsCarrots();
         }

         return super.canStart();
      }

      public boolean shouldContinue() {
         return this.hasTarget && super.shouldContinue();
      }

      public void tick() {
         super.tick();
         this.rabbit.getLookControl().lookAt((double)this.targetPos.getX() + 0.5, (double)(this.targetPos.getY() + 1), (double)this.targetPos.getZ() + 0.5, 10.0F, (float)this.rabbit.getMaxLookPitchChange());
         if (this.hasReached()) {
            World lv = this.rabbit.world;
            BlockPos lv2 = this.targetPos.up();
            BlockState lv3 = lv.getBlockState(lv2);
            Block lv4 = lv3.getBlock();
            if (this.hasTarget && lv4 instanceof CarrotsBlock) {
               int i = (Integer)lv3.get(CarrotsBlock.AGE);
               if (i == 0) {
                  lv.setBlockState(lv2, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                  lv.breakBlock(lv2, true, this.rabbit);
               } else {
                  lv.setBlockState(lv2, (BlockState)lv3.with(CarrotsBlock.AGE, i - 1), Block.NOTIFY_LISTENERS);
                  lv.syncWorldEvent(WorldEvents.BLOCK_BROKEN, lv2, Block.getRawIdFromState(lv3));
               }

               this.rabbit.moreCarrotTicks = 40;
            }

            this.hasTarget = false;
            this.cooldown = 10;
         }

      }

      protected boolean isTargetPos(WorldView world, BlockPos pos) {
         BlockState lv = world.getBlockState(pos);
         if (lv.isOf(Blocks.FARMLAND) && this.wantsCarrots && !this.hasTarget) {
            lv = world.getBlockState(pos.up());
            if (lv.getBlock() instanceof CarrotsBlock && ((CarrotsBlock)lv.getBlock()).isMature(lv)) {
               this.hasTarget = true;
               return true;
            }
         }

         return false;
      }
   }

   public static enum RabbitType implements StringIdentifiable {
      BROWN(0, "brown"),
      WHITE(1, "white"),
      BLACK(2, "black"),
      WHITE_SPLOTCHED(3, "white_splotched"),
      GOLD(4, "gold"),
      SALT(5, "salt"),
      EVIL(99, "evil");

      private static final IntFunction BY_ID = ValueLists.createIdToValueFunction(RabbitType::getId, values(), (Object)BROWN);
      public static final Codec CODEC = StringIdentifiable.createCodec(RabbitType::values);
      final int id;
      private final String name;

      private RabbitType(int id, String name) {
         this.id = id;
         this.name = name;
      }

      public String asString() {
         return this.name;
      }

      public int getId() {
         return this.id;
      }

      public static RabbitType byId(int id) {
         return (RabbitType)BY_ID.apply(id);
      }

      // $FF: synthetic method
      private static RabbitType[] method_47859() {
         return new RabbitType[]{BROWN, WHITE, BLACK, WHITE_SPLOTCHED, GOLD, SALT, EVIL};
      }
   }

   static class RabbitAttackGoal extends MeleeAttackGoal {
      public RabbitAttackGoal(RabbitEntity rabbit) {
         super(rabbit, 1.4, true);
      }

      protected double getSquaredMaxAttackDistance(LivingEntity entity) {
         return (double)(4.0F + entity.getWidth());
      }
   }

   public static class RabbitData extends PassiveEntity.PassiveData {
      public final RabbitType type;

      public RabbitData(RabbitType type) {
         super(1.0F);
         this.type = type;
      }
   }
}
