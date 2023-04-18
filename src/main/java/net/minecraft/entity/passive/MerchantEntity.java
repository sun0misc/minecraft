package net.minecraft.entity.passive;

import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.Npc;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class MerchantEntity extends PassiveEntity implements InventoryOwner, Npc, Merchant {
   private static final TrackedData HEAD_ROLLING_TIME_LEFT;
   public static final int field_30599 = 300;
   private static final int INVENTORY_SIZE = 8;
   @Nullable
   private PlayerEntity customer;
   @Nullable
   protected TradeOfferList offers;
   private final SimpleInventory inventory = new SimpleInventory(8);

   public MerchantEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, 16.0F);
      this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, -1.0F);
   }

   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      if (entityData == null) {
         entityData = new PassiveEntity.PassiveData(false);
      }

      return super.initialize(world, difficulty, spawnReason, (EntityData)entityData, entityNbt);
   }

   public int getHeadRollingTimeLeft() {
      return (Integer)this.dataTracker.get(HEAD_ROLLING_TIME_LEFT);
   }

   public void setHeadRollingTimeLeft(int ticks) {
      this.dataTracker.set(HEAD_ROLLING_TIME_LEFT, ticks);
   }

   public int getExperience() {
      return 0;
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return this.isBaby() ? 0.81F : 1.62F;
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(HEAD_ROLLING_TIME_LEFT, 0);
   }

   public void setCustomer(@Nullable PlayerEntity customer) {
      this.customer = customer;
   }

   @Nullable
   public PlayerEntity getCustomer() {
      return this.customer;
   }

   public boolean hasCustomer() {
      return this.customer != null;
   }

   public TradeOfferList getOffers() {
      if (this.offers == null) {
         this.offers = new TradeOfferList();
         this.fillRecipes();
      }

      return this.offers;
   }

   public void setOffersFromServer(@Nullable TradeOfferList offers) {
   }

   public void setExperienceFromServer(int experience) {
   }

   public void trade(TradeOffer offer) {
      offer.use();
      this.ambientSoundChance = -this.getMinAmbientSoundDelay();
      this.afterUsing(offer);
      if (this.customer instanceof ServerPlayerEntity) {
         Criteria.VILLAGER_TRADE.trigger((ServerPlayerEntity)this.customer, this, offer.getSellItem());
      }

   }

   protected abstract void afterUsing(TradeOffer offer);

   public boolean isLeveledMerchant() {
      return true;
   }

   public void onSellingItem(ItemStack stack) {
      if (!this.world.isClient && this.ambientSoundChance > -this.getMinAmbientSoundDelay() + 20) {
         this.ambientSoundChance = -this.getMinAmbientSoundDelay();
         this.playSound(this.getTradingSound(!stack.isEmpty()), this.getSoundVolume(), this.getSoundPitch());
      }

   }

   public SoundEvent getYesSound() {
      return SoundEvents.ENTITY_VILLAGER_YES;
   }

   protected SoundEvent getTradingSound(boolean sold) {
      return sold ? SoundEvents.ENTITY_VILLAGER_YES : SoundEvents.ENTITY_VILLAGER_NO;
   }

   public void playCelebrateSound() {
      this.playSound(SoundEvents.ENTITY_VILLAGER_CELEBRATE, this.getSoundVolume(), this.getSoundPitch());
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      TradeOfferList lv = this.getOffers();
      if (!lv.isEmpty()) {
         nbt.put("Offers", lv.toNbt());
      }

      this.writeInventory(nbt);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("Offers", NbtElement.COMPOUND_TYPE)) {
         this.offers = new TradeOfferList(nbt.getCompound("Offers"));
      }

      this.readInventory(nbt);
   }

   @Nullable
   public Entity moveToWorld(ServerWorld destination) {
      this.resetCustomer();
      return super.moveToWorld(destination);
   }

   protected void resetCustomer() {
      this.setCustomer((PlayerEntity)null);
   }

   public void onDeath(DamageSource damageSource) {
      super.onDeath(damageSource);
      this.resetCustomer();
   }

   protected void produceParticles(ParticleEffect parameters) {
      for(int i = 0; i < 5; ++i) {
         double d = this.random.nextGaussian() * 0.02;
         double e = this.random.nextGaussian() * 0.02;
         double f = this.random.nextGaussian() * 0.02;
         this.world.addParticle(parameters, this.getParticleX(1.0), this.getRandomBodyY() + 1.0, this.getParticleZ(1.0), d, e, f);
      }

   }

   public boolean canBeLeashedBy(PlayerEntity player) {
      return false;
   }

   public SimpleInventory getInventory() {
      return this.inventory;
   }

   public StackReference getStackReference(int mappedIndex) {
      int j = mappedIndex - 300;
      return j >= 0 && j < this.inventory.size() ? StackReference.of(this.inventory, j) : super.getStackReference(mappedIndex);
   }

   protected abstract void fillRecipes();

   protected void fillRecipesFromPool(TradeOfferList recipeList, TradeOffers.Factory[] pool, int count) {
      Set set = Sets.newHashSet();
      if (pool.length > count) {
         while(set.size() < count) {
            set.add(this.random.nextInt(pool.length));
         }
      } else {
         for(int j = 0; j < pool.length; ++j) {
            set.add(j);
         }
      }

      Iterator var9 = set.iterator();

      while(var9.hasNext()) {
         Integer integer = (Integer)var9.next();
         TradeOffers.Factory lv = pool[integer];
         TradeOffer lv2 = lv.create(this, this.random);
         if (lv2 != null) {
            recipeList.add(lv2);
         }
      }

   }

   public Vec3d getLeashPos(float delta) {
      float g = MathHelper.lerp(delta, this.prevBodyYaw, this.bodyYaw) * 0.017453292F;
      Vec3d lv = new Vec3d(0.0, this.getBoundingBox().getYLength() - 1.0, 0.2);
      return this.getLerpedPos(delta).add(lv.rotateY(-g));
   }

   public boolean isClient() {
      return this.world.isClient;
   }

   static {
      HEAD_ROLLING_TIME_LEFT = DataTracker.registerData(MerchantEntity.class, TrackedDataHandlerRegistry.INTEGER);
   }
}
