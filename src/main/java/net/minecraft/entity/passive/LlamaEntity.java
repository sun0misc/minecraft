package net.minecraft.entity.passive;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.function.IntFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DyedCarpetBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.FormCaravanGoal;
import net.minecraft.entity.ai.goal.HorseBondWithPlayerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.DyeColor;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LlamaEntity extends AbstractDonkeyEntity implements VariantHolder, RangedAttackMob {
   private static final int MAX_STRENGTH = 5;
   private static final Ingredient TAMING_INGREDIENT;
   private static final TrackedData STRENGTH;
   private static final TrackedData CARPET_COLOR;
   private static final TrackedData VARIANT;
   boolean spit;
   @Nullable
   private LlamaEntity following;
   @Nullable
   private LlamaEntity follower;

   public LlamaEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public boolean isTrader() {
      return false;
   }

   private void setStrength(int strength) {
      this.dataTracker.set(STRENGTH, Math.max(1, Math.min(5, strength)));
   }

   private void initializeStrength(Random random) {
      int i = random.nextFloat() < 0.04F ? 5 : 3;
      this.setStrength(1 + random.nextInt(i));
   }

   public int getStrength() {
      return (Integer)this.dataTracker.get(STRENGTH);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("Variant", this.getVariant().id);
      nbt.putInt("Strength", this.getStrength());
      if (!this.items.getStack(1).isEmpty()) {
         nbt.put("DecorItem", this.items.getStack(1).writeNbt(new NbtCompound()));
      }

   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      this.setStrength(nbt.getInt("Strength"));
      super.readCustomDataFromNbt(nbt);
      this.setVariant(LlamaEntity.Variant.byId(nbt.getInt("Variant")));
      if (nbt.contains("DecorItem", NbtElement.COMPOUND_TYPE)) {
         this.items.setStack(1, ItemStack.fromNbt(nbt.getCompound("DecorItem")));
      }

      this.updateSaddle();
   }

   protected void initGoals() {
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(1, new HorseBondWithPlayerGoal(this, 1.2));
      this.goalSelector.add(2, new FormCaravanGoal(this, 2.0999999046325684));
      this.goalSelector.add(3, new ProjectileAttackGoal(this, 1.25, 40, 20.0F));
      this.goalSelector.add(3, new EscapeDangerGoal(this, 1.2));
      this.goalSelector.add(4, new AnimalMateGoal(this, 1.0));
      this.goalSelector.add(5, new TemptGoal(this, 1.25, Ingredient.ofItems(Items.HAY_BLOCK), false));
      this.goalSelector.add(6, new FollowParentGoal(this, 1.0));
      this.goalSelector.add(7, new WanderAroundFarGoal(this, 0.7));
      this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
      this.goalSelector.add(9, new LookAroundGoal(this));
      this.targetSelector.add(1, new SpitRevengeGoal(this));
      this.targetSelector.add(2, new ChaseWolvesGoal(this));
   }

   public static DefaultAttributeContainer.Builder createLlamaAttributes() {
      return createAbstractDonkeyAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(STRENGTH, 0);
      this.dataTracker.startTracking(CARPET_COLOR, -1);
      this.dataTracker.startTracking(VARIANT, 0);
   }

   public Variant getVariant() {
      return LlamaEntity.Variant.byId((Integer)this.dataTracker.get(VARIANT));
   }

   public void setVariant(Variant arg) {
      this.dataTracker.set(VARIANT, arg.id);
   }

   protected int getInventorySize() {
      return this.hasChest() ? 2 + 3 * this.getInventoryColumns() : super.getInventorySize();
   }

   public void updatePassengerPosition(Entity passenger) {
      if (this.hasPassenger(passenger)) {
         float f = MathHelper.cos(this.bodyYaw * 0.017453292F);
         float g = MathHelper.sin(this.bodyYaw * 0.017453292F);
         float h = 0.3F;
         passenger.setPosition(this.getX() + (double)(0.3F * g), this.getY() + this.getMountedHeightOffset() + passenger.getHeightOffset(), this.getZ() - (double)(0.3F * f));
      }
   }

   public double getMountedHeightOffset() {
      return (double)this.getHeight() * 0.6;
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      return null;
   }

   public boolean isBreedingItem(ItemStack stack) {
      return TAMING_INGREDIENT.test(stack);
   }

   protected boolean receiveFood(PlayerEntity player, ItemStack item) {
      int i = 0;
      int j = 0;
      float f = 0.0F;
      boolean bl = false;
      if (item.isOf(Items.WHEAT)) {
         i = 10;
         j = 3;
         f = 2.0F;
      } else if (item.isOf(Blocks.HAY_BLOCK.asItem())) {
         i = 90;
         j = 6;
         f = 10.0F;
         if (this.isTame() && this.getBreedingAge() == 0 && this.canEat()) {
            bl = true;
            this.lovePlayer(player);
         }
      }

      if (this.getHealth() < this.getMaxHealth() && f > 0.0F) {
         this.heal(f);
         bl = true;
      }

      if (this.isBaby() && i > 0) {
         this.world.addParticle(ParticleTypes.HAPPY_VILLAGER, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), 0.0, 0.0, 0.0);
         if (!this.world.isClient) {
            this.growUp(i);
         }

         bl = true;
      }

      if (j > 0 && (bl || !this.isTame()) && this.getTemper() < this.getMaxTemper()) {
         bl = true;
         if (!this.world.isClient) {
            this.addTemper(j);
         }
      }

      if (bl && !this.isSilent()) {
         SoundEvent lv = this.getEatSound();
         if (lv != null) {
            this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), this.getEatSound(), this.getSoundCategory(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
         }
      }

      return bl;
   }

   public boolean isImmobile() {
      return this.isDead() || this.isEatingGrass();
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      Random lv = world.getRandom();
      this.initializeStrength(lv);
      Variant lv2;
      if (entityData instanceof LlamaData) {
         lv2 = ((LlamaData)entityData).variant;
      } else {
         lv2 = (Variant)Util.getRandom((Object[])LlamaEntity.Variant.values(), lv);
         entityData = new LlamaData(lv2);
      }

      this.setVariant(lv2);
      return super.initialize(world, difficulty, spawnReason, (EntityData)entityData, entityNbt);
   }

   protected boolean shouldAmbientStand() {
      return false;
   }

   protected SoundEvent getAngrySound() {
      return SoundEvents.ENTITY_LLAMA_ANGRY;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_LLAMA_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_LLAMA_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_LLAMA_DEATH;
   }

   @Nullable
   protected SoundEvent getEatSound() {
      return SoundEvents.ENTITY_LLAMA_EAT;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.ENTITY_LLAMA_STEP, 0.15F, 1.0F);
   }

   protected void playAddChestSound() {
      this.playSound(SoundEvents.ENTITY_LLAMA_CHEST, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
   }

   public int getInventoryColumns() {
      return this.getStrength();
   }

   public boolean hasArmorSlot() {
      return true;
   }

   public boolean hasArmorInSlot() {
      return !this.items.getStack(1).isEmpty();
   }

   public boolean isHorseArmor(ItemStack item) {
      return item.isIn(ItemTags.WOOL_CARPETS);
   }

   public boolean canBeSaddled() {
      return false;
   }

   public void onInventoryChanged(Inventory sender) {
      DyeColor lv = this.getCarpetColor();
      super.onInventoryChanged(sender);
      DyeColor lv2 = this.getCarpetColor();
      if (this.age > 20 && lv2 != null && lv2 != lv) {
         this.playSound(SoundEvents.ENTITY_LLAMA_SWAG, 0.5F, 1.0F);
      }

   }

   protected void updateSaddle() {
      if (!this.world.isClient) {
         super.updateSaddle();
         this.setCarpetColor(getColorFromCarpet(this.items.getStack(1)));
      }
   }

   private void setCarpetColor(@Nullable DyeColor color) {
      this.dataTracker.set(CARPET_COLOR, color == null ? -1 : color.getId());
   }

   @Nullable
   private static DyeColor getColorFromCarpet(ItemStack color) {
      Block lv = Block.getBlockFromItem(color.getItem());
      return lv instanceof DyedCarpetBlock ? ((DyedCarpetBlock)lv).getDyeColor() : null;
   }

   @Nullable
   public DyeColor getCarpetColor() {
      int i = (Integer)this.dataTracker.get(CARPET_COLOR);
      return i == -1 ? null : DyeColor.byId(i);
   }

   public int getMaxTemper() {
      return 30;
   }

   public boolean canBreedWith(AnimalEntity other) {
      return other != this && other instanceof LlamaEntity && this.canBreed() && ((LlamaEntity)other).canBreed();
   }

   @Nullable
   public LlamaEntity createChild(ServerWorld arg, PassiveEntity arg2) {
      LlamaEntity lv = this.createChild();
      if (lv != null) {
         this.setChildAttributes(arg2, lv);
         LlamaEntity lv2 = (LlamaEntity)arg2;
         int i = this.random.nextInt(Math.max(this.getStrength(), lv2.getStrength())) + 1;
         if (this.random.nextFloat() < 0.03F) {
            ++i;
         }

         lv.setStrength(i);
         lv.setVariant(this.random.nextBoolean() ? this.getVariant() : lv2.getVariant());
      }

      return lv;
   }

   @Nullable
   protected LlamaEntity createChild() {
      return (LlamaEntity)EntityType.LLAMA.create(this.world);
   }

   private void spitAt(LivingEntity target) {
      LlamaSpitEntity lv = new LlamaSpitEntity(this.world, this);
      double d = target.getX() - this.getX();
      double e = target.getBodyY(0.3333333333333333) - lv.getY();
      double f = target.getZ() - this.getZ();
      double g = Math.sqrt(d * d + f * f) * 0.20000000298023224;
      lv.setVelocity(d, e + g, f, 1.5F, 10.0F);
      if (!this.isSilent()) {
         this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_LLAMA_SPIT, this.getSoundCategory(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
      }

      this.world.spawnEntity(lv);
      this.spit = true;
   }

   void setSpit(boolean spit) {
      this.spit = spit;
   }

   public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
      int i = this.computeFallDamage(fallDistance, damageMultiplier);
      if (i <= 0) {
         return false;
      } else {
         if (fallDistance >= 6.0F) {
            this.damage(damageSource, (float)i);
            if (this.hasPassengers()) {
               Iterator var5 = this.getPassengersDeep().iterator();

               while(var5.hasNext()) {
                  Entity lv = (Entity)var5.next();
                  lv.damage(damageSource, (float)i);
               }
            }
         }

         this.playBlockFallSound();
         return true;
      }
   }

   public void stopFollowing() {
      if (this.following != null) {
         this.following.follower = null;
      }

      this.following = null;
   }

   public void follow(LlamaEntity llama) {
      this.following = llama;
      this.following.follower = this;
   }

   public boolean hasFollower() {
      return this.follower != null;
   }

   public boolean isFollowing() {
      return this.following != null;
   }

   @Nullable
   public LlamaEntity getFollowing() {
      return this.following;
   }

   protected double getFollowLeashSpeed() {
      return 2.0;
   }

   protected void walkToParent() {
      if (!this.isFollowing() && this.isBaby()) {
         super.walkToParent();
      }

   }

   public boolean eatsGrass() {
      return false;
   }

   public void attack(LivingEntity target, float pullProgress) {
      this.spitAt(target);
   }

   public Vec3d getLeashOffset() {
      return new Vec3d(0.0, 0.75 * (double)this.getStandingEyeHeight(), (double)this.getWidth() * 0.5);
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
      TAMING_INGREDIENT = Ingredient.ofItems(Items.WHEAT, Blocks.HAY_BLOCK.asItem());
      STRENGTH = DataTracker.registerData(LlamaEntity.class, TrackedDataHandlerRegistry.INTEGER);
      CARPET_COLOR = DataTracker.registerData(LlamaEntity.class, TrackedDataHandlerRegistry.INTEGER);
      VARIANT = DataTracker.registerData(LlamaEntity.class, TrackedDataHandlerRegistry.INTEGER);
   }

   public static enum Variant implements StringIdentifiable {
      CREAMY(0, "creamy"),
      WHITE(1, "white"),
      BROWN(2, "brown"),
      GRAY(3, "gray");

      public static final Codec CODEC = StringIdentifiable.createCodec(Variant::values);
      private static final IntFunction BY_ID = ValueLists.createIdToValueFunction(Variant::getIndex, values(), (ValueLists.OutOfBoundsHandling)ValueLists.OutOfBoundsHandling.CLAMP);
      final int id;
      private final String name;

      private Variant(int id, String name) {
         this.id = id;
         this.name = name;
      }

      public int getIndex() {
         return this.id;
      }

      public static Variant byId(int id) {
         return (Variant)BY_ID.apply(id);
      }

      public String asString() {
         return this.name;
      }

      // $FF: synthetic method
      private static Variant[] method_47877() {
         return new Variant[]{CREAMY, WHITE, BROWN, GRAY};
      }
   }

   static class SpitRevengeGoal extends RevengeGoal {
      public SpitRevengeGoal(LlamaEntity llama) {
         super(llama);
      }

      public boolean shouldContinue() {
         if (this.mob instanceof LlamaEntity) {
            LlamaEntity lv = (LlamaEntity)this.mob;
            if (lv.spit) {
               lv.setSpit(false);
               return false;
            }
         }

         return super.shouldContinue();
      }
   }

   private static class ChaseWolvesGoal extends ActiveTargetGoal {
      public ChaseWolvesGoal(LlamaEntity llama) {
         super(llama, WolfEntity.class, 16, false, true, (wolf) -> {
            return !((WolfEntity)wolf).isTamed();
         });
      }

      protected double getFollowRange() {
         return super.getFollowRange() * 0.25;
      }
   }

   private static class LlamaData extends PassiveEntity.PassiveData {
      public final Variant variant;

      LlamaData(Variant variant) {
         super(true);
         this.variant = variant;
      }
   }
}
