package net.minecraft.entity.mob;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import net.minecraft.client.render.entity.feature.SkinOverlayOwner;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.CreeperIgniteGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class CreeperEntity extends HostileEntity implements SkinOverlayOwner {
   private static final TrackedData FUSE_SPEED;
   private static final TrackedData CHARGED;
   private static final TrackedData IGNITED;
   private int lastFuseTime;
   private int currentFuseTime;
   private int fuseTime = 30;
   private int explosionRadius = 3;
   private int headsDropped;

   public CreeperEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   protected void initGoals() {
      this.goalSelector.add(1, new SwimGoal(this));
      this.goalSelector.add(2, new CreeperIgniteGoal(this));
      this.goalSelector.add(3, new FleeEntityGoal(this, OcelotEntity.class, 6.0F, 1.0, 1.2));
      this.goalSelector.add(3, new FleeEntityGoal(this, CatEntity.class, 6.0F, 1.0, 1.2));
      this.goalSelector.add(4, new MeleeAttackGoal(this, 1.0, false));
      this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
      this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.add(6, new LookAroundGoal(this));
      this.targetSelector.add(1, new ActiveTargetGoal(this, PlayerEntity.class, true));
      this.targetSelector.add(2, new RevengeGoal(this, new Class[0]));
   }

   public static DefaultAttributeContainer.Builder createCreeperAttributes() {
      return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25);
   }

   public int getSafeFallDistance() {
      return this.getTarget() == null ? 3 : 3 + (int)(this.getHealth() - 1.0F);
   }

   public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
      boolean bl = super.handleFallDamage(fallDistance, damageMultiplier, damageSource);
      this.currentFuseTime += (int)(fallDistance * 1.5F);
      if (this.currentFuseTime > this.fuseTime - 5) {
         this.currentFuseTime = this.fuseTime - 5;
      }

      return bl;
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(FUSE_SPEED, -1);
      this.dataTracker.startTracking(CHARGED, false);
      this.dataTracker.startTracking(IGNITED, false);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      if ((Boolean)this.dataTracker.get(CHARGED)) {
         nbt.putBoolean("powered", true);
      }

      nbt.putShort("Fuse", (short)this.fuseTime);
      nbt.putByte("ExplosionRadius", (byte)this.explosionRadius);
      nbt.putBoolean("ignited", this.isIgnited());
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.dataTracker.set(CHARGED, nbt.getBoolean("powered"));
      if (nbt.contains("Fuse", NbtElement.NUMBER_TYPE)) {
         this.fuseTime = nbt.getShort("Fuse");
      }

      if (nbt.contains("ExplosionRadius", NbtElement.NUMBER_TYPE)) {
         this.explosionRadius = nbt.getByte("ExplosionRadius");
      }

      if (nbt.getBoolean("ignited")) {
         this.ignite();
      }

   }

   public void tick() {
      if (this.isAlive()) {
         this.lastFuseTime = this.currentFuseTime;
         if (this.isIgnited()) {
            this.setFuseSpeed(1);
         }

         int i = this.getFuseSpeed();
         if (i > 0 && this.currentFuseTime == 0) {
            this.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0F, 0.5F);
            this.emitGameEvent(GameEvent.PRIME_FUSE);
         }

         this.currentFuseTime += i;
         if (this.currentFuseTime < 0) {
            this.currentFuseTime = 0;
         }

         if (this.currentFuseTime >= this.fuseTime) {
            this.currentFuseTime = this.fuseTime;
            this.explode();
         }
      }

      super.tick();
   }

   public void setTarget(@Nullable LivingEntity target) {
      if (!(target instanceof GoatEntity)) {
         super.setTarget(target);
      }
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_CREEPER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_CREEPER_DEATH;
   }

   protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
      super.dropEquipment(source, lootingMultiplier, allowDrops);
      Entity lv = source.getAttacker();
      if (lv != this && lv instanceof CreeperEntity lv2) {
         if (lv2.shouldDropHead()) {
            lv2.onHeadDropped();
            this.dropItem(Items.CREEPER_HEAD);
         }
      }

   }

   public boolean tryAttack(Entity target) {
      return true;
   }

   public boolean shouldRenderOverlay() {
      return (Boolean)this.dataTracker.get(CHARGED);
   }

   public float getClientFuseTime(float timeDelta) {
      return MathHelper.lerp(timeDelta, (float)this.lastFuseTime, (float)this.currentFuseTime) / (float)(this.fuseTime - 2);
   }

   public int getFuseSpeed() {
      return (Integer)this.dataTracker.get(FUSE_SPEED);
   }

   public void setFuseSpeed(int fuseSpeed) {
      this.dataTracker.set(FUSE_SPEED, fuseSpeed);
   }

   public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
      super.onStruckByLightning(world, lightning);
      this.dataTracker.set(CHARGED, true);
   }

   protected ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack lv = player.getStackInHand(hand);
      if (lv.isIn(ItemTags.CREEPER_IGNITERS)) {
         SoundEvent lv2 = lv.isOf(Items.FIRE_CHARGE) ? SoundEvents.ITEM_FIRECHARGE_USE : SoundEvents.ITEM_FLINTANDSTEEL_USE;
         this.world.playSound(player, this.getX(), this.getY(), this.getZ(), lv2, this.getSoundCategory(), 1.0F, this.random.nextFloat() * 0.4F + 0.8F);
         if (!this.world.isClient) {
            this.ignite();
            if (!lv.isDamageable()) {
               lv.decrement(1);
            } else {
               lv.damage(1, (LivingEntity)player, (Consumer)((playerx) -> {
                  playerx.sendToolBreakStatus(hand);
               }));
            }
         }

         return ActionResult.success(this.world.isClient);
      } else {
         return super.interactMob(player, hand);
      }
   }

   private void explode() {
      if (!this.world.isClient) {
         float f = this.shouldRenderOverlay() ? 2.0F : 1.0F;
         this.dead = true;
         this.world.createExplosion(this, this.getX(), this.getY(), this.getZ(), (float)this.explosionRadius * f, World.ExplosionSourceType.MOB);
         this.discard();
         this.spawnEffectsCloud();
      }

   }

   private void spawnEffectsCloud() {
      Collection collection = this.getStatusEffects();
      if (!collection.isEmpty()) {
         AreaEffectCloudEntity lv = new AreaEffectCloudEntity(this.world, this.getX(), this.getY(), this.getZ());
         lv.setRadius(2.5F);
         lv.setRadiusOnUse(-0.5F);
         lv.setWaitTime(10);
         lv.setDuration(lv.getDuration() / 2);
         lv.setRadiusGrowth(-lv.getRadius() / (float)lv.getDuration());
         Iterator var3 = collection.iterator();

         while(var3.hasNext()) {
            StatusEffectInstance lv2 = (StatusEffectInstance)var3.next();
            lv.addEffect(new StatusEffectInstance(lv2));
         }

         this.world.spawnEntity(lv);
      }

   }

   public boolean isIgnited() {
      return (Boolean)this.dataTracker.get(IGNITED);
   }

   public void ignite() {
      this.dataTracker.set(IGNITED, true);
   }

   public boolean shouldDropHead() {
      return this.shouldRenderOverlay() && this.headsDropped < 1;
   }

   public void onHeadDropped() {
      ++this.headsDropped;
   }

   static {
      FUSE_SPEED = DataTracker.registerData(CreeperEntity.class, TrackedDataHandlerRegistry.INTEGER);
      CHARGED = DataTracker.registerData(CreeperEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      IGNITED = DataTracker.registerData(CreeperEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }
}
