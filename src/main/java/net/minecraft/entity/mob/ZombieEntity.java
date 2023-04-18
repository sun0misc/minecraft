package net.minecraft.entity.mob;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.BreakDoorGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.StepAndDestroyBlockGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class ZombieEntity extends HostileEntity {
   private static final UUID BABY_SPEED_ID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
   private static final EntityAttributeModifier BABY_SPEED_BONUS;
   private static final TrackedData BABY;
   private static final TrackedData ZOMBIE_TYPE;
   private static final TrackedData CONVERTING_IN_WATER;
   public static final float field_30519 = 0.05F;
   public static final int field_30515 = 50;
   public static final int field_30516 = 40;
   public static final int field_30517 = 7;
   protected static final float field_41028 = 0.81F;
   private static final float field_30518 = 0.1F;
   private static final Predicate DOOR_BREAK_DIFFICULTY_CHECKER;
   private final BreakDoorGoal breakDoorsGoal;
   private boolean canBreakDoors;
   private int inWaterTime;
   private int ticksUntilWaterConversion;

   public ZombieEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.breakDoorsGoal = new BreakDoorGoal(this, DOOR_BREAK_DIFFICULTY_CHECKER);
   }

   public ZombieEntity(World world) {
      this(EntityType.ZOMBIE, world);
   }

   protected void initGoals() {
      this.goalSelector.add(4, new DestroyEggGoal(this, 1.0, 3));
      this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.add(8, new LookAroundGoal(this));
      this.initCustomGoals();
   }

   protected void initCustomGoals() {
      this.goalSelector.add(2, new ZombieAttackGoal(this, 1.0, false));
      this.goalSelector.add(6, new MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors));
      this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0));
      this.targetSelector.add(1, (new RevengeGoal(this, new Class[0])).setGroupRevenge(ZombifiedPiglinEntity.class));
      this.targetSelector.add(2, new ActiveTargetGoal(this, PlayerEntity.class, true));
      this.targetSelector.add(3, new ActiveTargetGoal(this, MerchantEntity.class, false));
      this.targetSelector.add(3, new ActiveTargetGoal(this, IronGolemEntity.class, true));
      this.targetSelector.add(5, new ActiveTargetGoal(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
   }

   public static DefaultAttributeContainer.Builder createZombieAttributes() {
      return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23000000417232513).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0).add(EntityAttributes.GENERIC_ARMOR, 2.0).add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.getDataTracker().startTracking(BABY, false);
      this.getDataTracker().startTracking(ZOMBIE_TYPE, 0);
      this.getDataTracker().startTracking(CONVERTING_IN_WATER, false);
   }

   public boolean isConvertingInWater() {
      return (Boolean)this.getDataTracker().get(CONVERTING_IN_WATER);
   }

   public boolean canBreakDoors() {
      return this.canBreakDoors;
   }

   public void setCanBreakDoors(boolean canBreakDoors) {
      if (this.shouldBreakDoors() && NavigationConditions.hasMobNavigation(this)) {
         if (this.canBreakDoors != canBreakDoors) {
            this.canBreakDoors = canBreakDoors;
            ((MobNavigation)this.getNavigation()).setCanPathThroughDoors(canBreakDoors);
            if (canBreakDoors) {
               this.goalSelector.add(1, this.breakDoorsGoal);
            } else {
               this.goalSelector.remove(this.breakDoorsGoal);
            }
         }
      } else if (this.canBreakDoors) {
         this.goalSelector.remove(this.breakDoorsGoal);
         this.canBreakDoors = false;
      }

   }

   protected boolean shouldBreakDoors() {
      return true;
   }

   public boolean isBaby() {
      return (Boolean)this.getDataTracker().get(BABY);
   }

   public int getXpToDrop() {
      if (this.isBaby()) {
         this.experiencePoints = (int)((double)this.experiencePoints * 2.5);
      }

      return super.getXpToDrop();
   }

   public void setBaby(boolean baby) {
      this.getDataTracker().set(BABY, baby);
      if (this.world != null && !this.world.isClient) {
         EntityAttributeInstance lv = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
         lv.removeModifier(BABY_SPEED_BONUS);
         if (baby) {
            lv.addTemporaryModifier(BABY_SPEED_BONUS);
         }
      }

   }

   public void onTrackedDataSet(TrackedData data) {
      if (BABY.equals(data)) {
         this.calculateDimensions();
      }

      super.onTrackedDataSet(data);
   }

   protected boolean canConvertInWater() {
      return true;
   }

   public void tick() {
      if (!this.world.isClient && this.isAlive() && !this.isAiDisabled()) {
         if (this.isConvertingInWater()) {
            --this.ticksUntilWaterConversion;
            if (this.ticksUntilWaterConversion < 0) {
               this.convertInWater();
            }
         } else if (this.canConvertInWater()) {
            if (this.isSubmergedIn(FluidTags.WATER)) {
               ++this.inWaterTime;
               if (this.inWaterTime >= 600) {
                  this.setTicksUntilWaterConversion(300);
               }
            } else {
               this.inWaterTime = -1;
            }
         }
      }

      super.tick();
   }

   public void tickMovement() {
      if (this.isAlive()) {
         boolean bl = this.burnsInDaylight() && this.isAffectedByDaylight();
         if (bl) {
            ItemStack lv = this.getEquippedStack(EquipmentSlot.HEAD);
            if (!lv.isEmpty()) {
               if (lv.isDamageable()) {
                  lv.setDamage(lv.getDamage() + this.random.nextInt(2));
                  if (lv.getDamage() >= lv.getMaxDamage()) {
                     this.sendEquipmentBreakStatus(EquipmentSlot.HEAD);
                     this.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
                  }
               }

               bl = false;
            }

            if (bl) {
               this.setOnFireFor(8);
            }
         }
      }

      super.tickMovement();
   }

   private void setTicksUntilWaterConversion(int ticksUntilWaterConversion) {
      this.ticksUntilWaterConversion = ticksUntilWaterConversion;
      this.getDataTracker().set(CONVERTING_IN_WATER, true);
   }

   protected void convertInWater() {
      this.convertTo(EntityType.DROWNED);
      if (!this.isSilent()) {
         this.world.syncWorldEvent((PlayerEntity)null, WorldEvents.ZOMBIE_CONVERTS_TO_DROWNED, this.getBlockPos(), 0);
      }

   }

   protected void convertTo(EntityType entityType) {
      ZombieEntity lv = (ZombieEntity)this.convertTo(entityType, true);
      if (lv != null) {
         lv.applyAttributeModifiers(lv.world.getLocalDifficulty(lv.getBlockPos()).getClampedLocalDifficulty());
         lv.setCanBreakDoors(lv.shouldBreakDoors() && this.canBreakDoors());
      }

   }

   protected boolean burnsInDaylight() {
      return true;
   }

   public boolean damage(DamageSource source, float amount) {
      if (!super.damage(source, amount)) {
         return false;
      } else if (!(this.world instanceof ServerWorld)) {
         return false;
      } else {
         ServerWorld lv = (ServerWorld)this.world;
         LivingEntity lv2 = this.getTarget();
         if (lv2 == null && source.getAttacker() instanceof LivingEntity) {
            lv2 = (LivingEntity)source.getAttacker();
         }

         if (lv2 != null && this.world.getDifficulty() == Difficulty.HARD && (double)this.random.nextFloat() < this.getAttributeValue(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS) && this.world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)) {
            int i = MathHelper.floor(this.getX());
            int j = MathHelper.floor(this.getY());
            int k = MathHelper.floor(this.getZ());
            ZombieEntity lv3 = new ZombieEntity(this.world);

            for(int l = 0; l < 50; ++l) {
               int m = i + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
               int n = j + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
               int o = k + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
               BlockPos lv4 = new BlockPos(m, n, o);
               EntityType lv5 = lv3.getType();
               SpawnRestriction.Location lv6 = SpawnRestriction.getLocation(lv5);
               if (SpawnHelper.canSpawn(lv6, this.world, lv4, lv5) && SpawnRestriction.canSpawn(lv5, lv, SpawnReason.REINFORCEMENT, lv4, this.world.random)) {
                  lv3.setPosition((double)m, (double)n, (double)o);
                  if (!this.world.isPlayerInRange((double)m, (double)n, (double)o, 7.0) && this.world.doesNotIntersectEntities(lv3) && this.world.isSpaceEmpty(lv3) && !this.world.containsFluid(lv3.getBoundingBox())) {
                     lv3.setTarget(lv2);
                     lv3.initialize(lv, this.world.getLocalDifficulty(lv3.getBlockPos()), SpawnReason.REINFORCEMENT, (EntityData)null, (NbtCompound)null);
                     lv.spawnEntityAndPassengers(lv3);
                     this.getAttributeInstance(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS).addPersistentModifier(new EntityAttributeModifier("Zombie reinforcement caller charge", -0.05000000074505806, EntityAttributeModifier.Operation.ADDITION));
                     lv3.getAttributeInstance(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS).addPersistentModifier(new EntityAttributeModifier("Zombie reinforcement callee charge", -0.05000000074505806, EntityAttributeModifier.Operation.ADDITION));
                     break;
                  }
               }
            }
         }

         return true;
      }
   }

   public boolean tryAttack(Entity target) {
      boolean bl = super.tryAttack(target);
      if (bl) {
         float f = this.world.getLocalDifficulty(this.getBlockPos()).getLocalDifficulty();
         if (this.getMainHandStack().isEmpty() && this.isOnFire() && this.random.nextFloat() < f * 0.3F) {
            target.setOnFireFor(2 * (int)f);
         }
      }

      return bl;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_ZOMBIE_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_ZOMBIE_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_ZOMBIE_DEATH;
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ENTITY_ZOMBIE_STEP;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   public EntityGroup getGroup() {
      return EntityGroup.UNDEAD;
   }

   protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
      super.initEquipment(random, localDifficulty);
      if (random.nextFloat() < (this.world.getDifficulty() == Difficulty.HARD ? 0.05F : 0.01F)) {
         int i = random.nextInt(3);
         if (i == 0) {
            this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
         } else {
            this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
         }
      }

   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putBoolean("IsBaby", this.isBaby());
      nbt.putBoolean("CanBreakDoors", this.canBreakDoors());
      nbt.putInt("InWaterTime", this.isTouchingWater() ? this.inWaterTime : -1);
      nbt.putInt("DrownedConversionTime", this.isConvertingInWater() ? this.ticksUntilWaterConversion : -1);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.setBaby(nbt.getBoolean("IsBaby"));
      this.setCanBreakDoors(nbt.getBoolean("CanBreakDoors"));
      this.inWaterTime = nbt.getInt("InWaterTime");
      if (nbt.contains("DrownedConversionTime", NbtElement.NUMBER_TYPE) && nbt.getInt("DrownedConversionTime") > -1) {
         this.setTicksUntilWaterConversion(nbt.getInt("DrownedConversionTime"));
      }

   }

   public boolean onKilledOther(ServerWorld world, LivingEntity other) {
      boolean bl = super.onKilledOther(world, other);
      if ((world.getDifficulty() == Difficulty.NORMAL || world.getDifficulty() == Difficulty.HARD) && other instanceof VillagerEntity lv) {
         if (world.getDifficulty() != Difficulty.HARD && this.random.nextBoolean()) {
            return bl;
         }

         ZombieVillagerEntity lv2 = (ZombieVillagerEntity)lv.convertTo(EntityType.ZOMBIE_VILLAGER, false);
         if (lv2 != null) {
            lv2.initialize(world, world.getLocalDifficulty(lv2.getBlockPos()), SpawnReason.CONVERSION, new ZombieData(false, true), (NbtCompound)null);
            lv2.setVillagerData(lv.getVillagerData());
            lv2.setGossipData((NbtElement)lv.getGossip().serialize(NbtOps.INSTANCE));
            lv2.setOfferData(lv.getOffers().toNbt());
            lv2.setXp(lv.getExperience());
            if (!this.isSilent()) {
               world.syncWorldEvent((PlayerEntity)null, WorldEvents.ZOMBIE_INFECTS_VILLAGER, this.getBlockPos(), 0);
            }

            bl = false;
         }
      }

      return bl;
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return this.isBaby() ? 0.93F : 1.74F;
   }

   public boolean canPickupItem(ItemStack stack) {
      return stack.isOf(Items.EGG) && this.isBaby() && this.hasVehicle() ? false : super.canPickupItem(stack);
   }

   public boolean canGather(ItemStack stack) {
      return stack.isOf(Items.GLOW_INK_SAC) ? false : super.canGather(stack);
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      Random lv = world.getRandom();
      EntityData entityData = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
      float f = difficulty.getClampedLocalDifficulty();
      this.setCanPickUpLoot(lv.nextFloat() < 0.55F * f);
      if (entityData == null) {
         entityData = new ZombieData(shouldBeBaby(lv), true);
      }

      if (entityData instanceof ZombieData lv2) {
         if (lv2.baby) {
            this.setBaby(true);
            if (lv2.tryChickenJockey) {
               if ((double)lv.nextFloat() < 0.05) {
                  List list = world.getEntitiesByClass(ChickenEntity.class, this.getBoundingBox().expand(5.0, 3.0, 5.0), EntityPredicates.NOT_MOUNTED);
                  if (!list.isEmpty()) {
                     ChickenEntity lv3 = (ChickenEntity)list.get(0);
                     lv3.setHasJockey(true);
                     this.startRiding(lv3);
                  }
               } else if ((double)lv.nextFloat() < 0.05) {
                  ChickenEntity lv4 = (ChickenEntity)EntityType.CHICKEN.create(this.world);
                  if (lv4 != null) {
                     lv4.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), 0.0F);
                     lv4.initialize(world, difficulty, SpawnReason.JOCKEY, (EntityData)null, (NbtCompound)null);
                     lv4.setHasJockey(true);
                     this.startRiding(lv4);
                     world.spawnEntity(lv4);
                  }
               }
            }
         }

         this.setCanBreakDoors(this.shouldBreakDoors() && lv.nextFloat() < f * 0.1F);
         this.initEquipment(lv, difficulty);
         this.updateEnchantments(lv, difficulty);
      }

      if (this.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
         LocalDate localDate = LocalDate.now();
         int i = localDate.get(ChronoField.DAY_OF_MONTH);
         int j = localDate.get(ChronoField.MONTH_OF_YEAR);
         if (j == 10 && i == 31 && lv.nextFloat() < 0.25F) {
            this.equipStack(EquipmentSlot.HEAD, new ItemStack(lv.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
            this.armorDropChances[EquipmentSlot.HEAD.getEntitySlotId()] = 0.0F;
         }
      }

      this.applyAttributeModifiers(f);
      return (EntityData)entityData;
   }

   public static boolean shouldBeBaby(Random random) {
      return random.nextFloat() < 0.05F;
   }

   protected void applyAttributeModifiers(float chanceMultiplier) {
      this.initAttributes();
      this.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).addPersistentModifier(new EntityAttributeModifier("Random spawn bonus", this.random.nextDouble() * 0.05000000074505806, EntityAttributeModifier.Operation.ADDITION));
      double d = this.random.nextDouble() * 1.5 * (double)chanceMultiplier;
      if (d > 1.0) {
         this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE).addPersistentModifier(new EntityAttributeModifier("Random zombie-spawn bonus", d, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
      }

      if (this.random.nextFloat() < chanceMultiplier * 0.05F) {
         this.getAttributeInstance(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS).addPersistentModifier(new EntityAttributeModifier("Leader zombie bonus", this.random.nextDouble() * 0.25 + 0.5, EntityAttributeModifier.Operation.ADDITION));
         this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).addPersistentModifier(new EntityAttributeModifier("Leader zombie bonus", this.random.nextDouble() * 3.0 + 1.0, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
         this.setCanBreakDoors(this.shouldBreakDoors());
      }

   }

   protected void initAttributes() {
      this.getAttributeInstance(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS).setBaseValue(this.random.nextDouble() * 0.10000000149011612);
   }

   public double getHeightOffset() {
      return this.isBaby() ? 0.0 : -0.45;
   }

   protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
      super.dropEquipment(source, lootingMultiplier, allowDrops);
      Entity lv = source.getAttacker();
      if (lv instanceof CreeperEntity lv2) {
         if (lv2.shouldDropHead()) {
            ItemStack lv3 = this.getSkull();
            if (!lv3.isEmpty()) {
               lv2.onHeadDropped();
               this.dropStack(lv3);
            }
         }
      }

   }

   protected ItemStack getSkull() {
      return new ItemStack(Items.ZOMBIE_HEAD);
   }

   static {
      BABY_SPEED_BONUS = new EntityAttributeModifier(BABY_SPEED_ID, "Baby speed boost", 0.5, EntityAttributeModifier.Operation.MULTIPLY_BASE);
      BABY = DataTracker.registerData(ZombieEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      ZOMBIE_TYPE = DataTracker.registerData(ZombieEntity.class, TrackedDataHandlerRegistry.INTEGER);
      CONVERTING_IN_WATER = DataTracker.registerData(ZombieEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      DOOR_BREAK_DIFFICULTY_CHECKER = (difficulty) -> {
         return difficulty == Difficulty.HARD;
      };
   }

   private class DestroyEggGoal extends StepAndDestroyBlockGoal {
      DestroyEggGoal(PathAwareEntity mob, double speed, int maxYDifference) {
         super(Blocks.TURTLE_EGG, mob, speed, maxYDifference);
      }

      public void tickStepping(WorldAccess world, BlockPos pos) {
         world.playSound((PlayerEntity)null, pos, SoundEvents.ENTITY_ZOMBIE_DESTROY_EGG, SoundCategory.HOSTILE, 0.5F, 0.9F + ZombieEntity.this.random.nextFloat() * 0.2F);
      }

      public void onDestroyBlock(World world, BlockPos pos) {
         world.playSound((PlayerEntity)null, pos, SoundEvents.ENTITY_TURTLE_EGG_BREAK, SoundCategory.BLOCKS, 0.7F, 0.9F + world.random.nextFloat() * 0.2F);
      }

      public double getDesiredDistanceToTarget() {
         return 1.14;
      }
   }

   public static class ZombieData implements EntityData {
      public final boolean baby;
      public final boolean tryChickenJockey;

      public ZombieData(boolean baby, boolean tryChickenJockey) {
         this.baby = baby;
         this.tryChickenJockey = tryChickenJockey;
      }
   }
}
