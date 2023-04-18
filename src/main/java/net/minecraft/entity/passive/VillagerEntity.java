package net.minecraft.entity.passive;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.InteractionObserver;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LargeEntitySpawnHelper;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.sensor.GolemLastSeenSensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.VillagerTaskListProvider;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillageGossipType;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerGossips;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class VillagerEntity extends MerchantEntity implements InteractionObserver, VillagerDataContainer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final TrackedData VILLAGER_DATA;
   public static final int field_30602 = 12;
   public static final Map ITEM_FOOD_VALUES;
   private static final int field_30604 = 2;
   private static final Set GATHERABLE_ITEMS;
   private static final int field_30605 = 10;
   private static final int field_30606 = 1200;
   private static final int field_30607 = 24000;
   private static final int field_30608 = 25;
   private static final int field_30609 = 10;
   private static final int field_30610 = 5;
   private static final long field_30611 = 24000L;
   @VisibleForTesting
   public static final float field_30603 = 0.5F;
   private int levelUpTimer;
   private boolean levelingUp;
   @Nullable
   private PlayerEntity lastCustomer;
   private boolean field_30612;
   private int foodLevel;
   private final VillagerGossips gossip;
   private long gossipStartTime;
   private long lastGossipDecayTime;
   private int experience;
   private long lastRestockTime;
   private int restocksToday;
   private long lastRestockCheckTime;
   private boolean natural;
   private static final ImmutableList MEMORY_MODULES;
   private static final ImmutableList SENSORS;
   public static final Map POINTS_OF_INTEREST;

   public VillagerEntity(EntityType arg, World arg2) {
      this(arg, arg2, VillagerType.PLAINS);
   }

   public VillagerEntity(EntityType entityType, World world, VillagerType type) {
      super(entityType, world);
      this.gossip = new VillagerGossips();
      ((MobNavigation)this.getNavigation()).setCanPathThroughDoors(true);
      this.getNavigation().setCanSwim(true);
      this.setCanPickUpLoot(true);
      this.setVillagerData(this.getVillagerData().withType(type).withProfession(VillagerProfession.NONE));
   }

   public Brain getBrain() {
      return super.getBrain();
   }

   protected Brain.Profile createBrainProfile() {
      return Brain.createProfile(MEMORY_MODULES, SENSORS);
   }

   protected Brain deserializeBrain(Dynamic dynamic) {
      Brain lv = this.createBrainProfile().deserialize(dynamic);
      this.initBrain(lv);
      return lv;
   }

   public void reinitializeBrain(ServerWorld world) {
      Brain lv = this.getBrain();
      lv.stopAllTasks(world, this);
      this.brain = lv.copy();
      this.initBrain(this.getBrain());
   }

   private void initBrain(Brain brain) {
      VillagerProfession lv = this.getVillagerData().getProfession();
      if (this.isBaby()) {
         brain.setSchedule(Schedule.VILLAGER_BABY);
         brain.setTaskList(Activity.PLAY, VillagerTaskListProvider.createPlayTasks(0.5F));
      } else {
         brain.setSchedule(Schedule.VILLAGER_DEFAULT);
         brain.setTaskList(Activity.WORK, VillagerTaskListProvider.createWorkTasks(lv, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryModuleState.VALUE_PRESENT)));
      }

      brain.setTaskList(Activity.CORE, VillagerTaskListProvider.createCoreTasks(lv, 0.5F));
      brain.setTaskList(Activity.MEET, VillagerTaskListProvider.createMeetTasks(lv, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryModuleState.VALUE_PRESENT)));
      brain.setTaskList(Activity.REST, VillagerTaskListProvider.createRestTasks(lv, 0.5F));
      brain.setTaskList(Activity.IDLE, VillagerTaskListProvider.createIdleTasks(lv, 0.5F));
      brain.setTaskList(Activity.PANIC, VillagerTaskListProvider.createPanicTasks(lv, 0.5F));
      brain.setTaskList(Activity.PRE_RAID, VillagerTaskListProvider.createPreRaidTasks(lv, 0.5F));
      brain.setTaskList(Activity.RAID, VillagerTaskListProvider.createRaidTasks(lv, 0.5F));
      brain.setTaskList(Activity.HIDE, VillagerTaskListProvider.createHideTasks(lv, 0.5F));
      brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
      brain.setDefaultActivity(Activity.IDLE);
      brain.doExclusively(Activity.IDLE);
      brain.refreshActivities(this.world.getTimeOfDay(), this.world.getTime());
   }

   protected void onGrowUp() {
      super.onGrowUp();
      if (this.world instanceof ServerWorld) {
         this.reinitializeBrain((ServerWorld)this.world);
      }

   }

   public static DefaultAttributeContainer.Builder createVillagerAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
   }

   public boolean isNatural() {
      return this.natural;
   }

   protected void mobTick() {
      this.world.getProfiler().push("villagerBrain");
      this.getBrain().tick((ServerWorld)this.world, this);
      this.world.getProfiler().pop();
      if (this.natural) {
         this.natural = false;
      }

      if (!this.hasCustomer() && this.levelUpTimer > 0) {
         --this.levelUpTimer;
         if (this.levelUpTimer <= 0) {
            if (this.levelingUp) {
               this.levelUp();
               this.levelingUp = false;
            }

            this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 0));
         }
      }

      if (this.lastCustomer != null && this.world instanceof ServerWorld) {
         ((ServerWorld)this.world).handleInteraction(EntityInteraction.TRADE, this.lastCustomer, this);
         this.world.sendEntityStatus(this, EntityStatuses.ADD_VILLAGER_HAPPY_PARTICLES);
         this.lastCustomer = null;
      }

      if (!this.isAiDisabled() && this.random.nextInt(100) == 0) {
         Raid lv = ((ServerWorld)this.world).getRaidAt(this.getBlockPos());
         if (lv != null && lv.isActive() && !lv.isFinished()) {
            this.world.sendEntityStatus(this, EntityStatuses.ADD_SPLASH_PARTICLES);
         }
      }

      if (this.getVillagerData().getProfession() == VillagerProfession.NONE && this.hasCustomer()) {
         this.resetCustomer();
      }

      super.mobTick();
   }

   public void tick() {
      super.tick();
      if (this.getHeadRollingTimeLeft() > 0) {
         this.setHeadRollingTimeLeft(this.getHeadRollingTimeLeft() - 1);
      }

      this.decayGossip();
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack lv = player.getStackInHand(hand);
      if (!lv.isOf(Items.VILLAGER_SPAWN_EGG) && this.isAlive() && !this.hasCustomer() && !this.isSleeping()) {
         if (this.isBaby()) {
            this.sayNo();
            return ActionResult.success(this.world.isClient);
         } else {
            boolean bl = this.getOffers().isEmpty();
            if (hand == Hand.MAIN_HAND) {
               if (bl && !this.world.isClient) {
                  this.sayNo();
               }

               player.incrementStat(Stats.TALKED_TO_VILLAGER);
            }

            if (bl) {
               return ActionResult.success(this.world.isClient);
            } else {
               if (!this.world.isClient && !this.offers.isEmpty()) {
                  this.beginTradeWith(player);
               }

               return ActionResult.success(this.world.isClient);
            }
         }
      } else {
         return super.interactMob(player, hand);
      }
   }

   private void sayNo() {
      this.setHeadRollingTimeLeft(40);
      if (!this.world.isClient()) {
         this.playSound(SoundEvents.ENTITY_VILLAGER_NO, this.getSoundVolume(), this.getSoundPitch());
      }

   }

   private void beginTradeWith(PlayerEntity customer) {
      this.prepareOffersFor(customer);
      this.setCustomer(customer);
      this.sendOffers(customer, this.getDisplayName(), this.getVillagerData().getLevel());
   }

   public void setCustomer(@Nullable PlayerEntity customer) {
      boolean bl = this.getCustomer() != null && customer == null;
      super.setCustomer(customer);
      if (bl) {
         this.resetCustomer();
      }

   }

   protected void resetCustomer() {
      super.resetCustomer();
      this.clearSpecialPrices();
   }

   private void clearSpecialPrices() {
      Iterator var1 = this.getOffers().iterator();

      while(var1.hasNext()) {
         TradeOffer lv = (TradeOffer)var1.next();
         lv.clearSpecialPrice();
      }

   }

   public boolean canRefreshTrades() {
      return true;
   }

   public boolean isClient() {
      return this.getWorld().isClient;
   }

   public void restock() {
      this.updateDemandBonus();
      Iterator var1 = this.getOffers().iterator();

      while(var1.hasNext()) {
         TradeOffer lv = (TradeOffer)var1.next();
         lv.resetUses();
      }

      this.sendOffersToCustomer();
      this.lastRestockTime = this.world.getTime();
      ++this.restocksToday;
   }

   private void sendOffersToCustomer() {
      TradeOfferList lv = this.getOffers();
      PlayerEntity lv2 = this.getCustomer();
      if (lv2 != null && !lv.isEmpty()) {
         lv2.sendTradeOffers(lv2.currentScreenHandler.syncId, lv, this.getVillagerData().getLevel(), this.getExperience(), this.isLeveledMerchant(), this.canRefreshTrades());
      }

   }

   private boolean needsRestock() {
      Iterator var1 = this.getOffers().iterator();

      TradeOffer lv;
      do {
         if (!var1.hasNext()) {
            return false;
         }

         lv = (TradeOffer)var1.next();
      } while(!lv.hasBeenUsed());

      return true;
   }

   private boolean canRestock() {
      return this.restocksToday == 0 || this.restocksToday < 2 && this.world.getTime() > this.lastRestockTime + 2400L;
   }

   public boolean shouldRestock() {
      long l = this.lastRestockTime + 12000L;
      long m = this.world.getTime();
      boolean bl = m > l;
      long n = this.world.getTimeOfDay();
      if (this.lastRestockCheckTime > 0L) {
         long o = this.lastRestockCheckTime / 24000L;
         long p = n / 24000L;
         bl |= p > o;
      }

      this.lastRestockCheckTime = n;
      if (bl) {
         this.lastRestockTime = m;
         this.clearDailyRestockCount();
      }

      return this.canRestock() && this.needsRestock();
   }

   private void restockAndUpdateDemandBonus() {
      int i = 2 - this.restocksToday;
      if (i > 0) {
         Iterator var2 = this.getOffers().iterator();

         while(var2.hasNext()) {
            TradeOffer lv = (TradeOffer)var2.next();
            lv.resetUses();
         }
      }

      for(int j = 0; j < i; ++j) {
         this.updateDemandBonus();
      }

      this.sendOffersToCustomer();
   }

   private void updateDemandBonus() {
      Iterator var1 = this.getOffers().iterator();

      while(var1.hasNext()) {
         TradeOffer lv = (TradeOffer)var1.next();
         lv.updateDemandBonus();
      }

   }

   private void prepareOffersFor(PlayerEntity player) {
      int i = this.getReputation(player);
      if (i != 0) {
         Iterator var3 = this.getOffers().iterator();

         while(var3.hasNext()) {
            TradeOffer lv = (TradeOffer)var3.next();
            lv.increaseSpecialPrice(-MathHelper.floor((float)i * lv.getPriceMultiplier()));
         }
      }

      if (player.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE)) {
         StatusEffectInstance lv2 = player.getStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE);
         int j = lv2.getAmplifier();
         Iterator var5 = this.getOffers().iterator();

         while(var5.hasNext()) {
            TradeOffer lv3 = (TradeOffer)var5.next();
            double d = 0.3 + 0.0625 * (double)j;
            int k = (int)Math.floor(d * (double)lv3.getOriginalFirstBuyItem().getCount());
            lv3.increaseSpecialPrice(-Math.max(k, 1));
         }
      }

   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      DataResult var10000 = VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, this.getVillagerData());
      Logger var10001 = LOGGER;
      Objects.requireNonNull(var10001);
      var10000.resultOrPartial(var10001::error).ifPresent((arg2) -> {
         nbt.put("VillagerData", arg2);
      });
      nbt.putByte("FoodLevel", (byte)this.foodLevel);
      nbt.put("Gossips", (NbtElement)this.gossip.serialize(NbtOps.INSTANCE));
      nbt.putInt("Xp", this.experience);
      nbt.putLong("LastRestock", this.lastRestockTime);
      nbt.putLong("LastGossipDecay", this.lastGossipDecayTime);
      nbt.putInt("RestocksToday", this.restocksToday);
      if (this.natural) {
         nbt.putBoolean("AssignProfessionWhenSpawned", true);
      }

   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("VillagerData", NbtElement.COMPOUND_TYPE)) {
         DataResult dataResult = VillagerData.CODEC.parse(new Dynamic(NbtOps.INSTANCE, nbt.get("VillagerData")));
         Logger var10001 = LOGGER;
         Objects.requireNonNull(var10001);
         dataResult.resultOrPartial(var10001::error).ifPresent(this::setVillagerData);
      }

      if (nbt.contains("Offers", NbtElement.COMPOUND_TYPE)) {
         this.offers = new TradeOfferList(nbt.getCompound("Offers"));
      }

      if (nbt.contains("FoodLevel", NbtElement.BYTE_TYPE)) {
         this.foodLevel = nbt.getByte("FoodLevel");
      }

      NbtList lv = nbt.getList("Gossips", NbtElement.COMPOUND_TYPE);
      this.gossip.deserialize(new Dynamic(NbtOps.INSTANCE, lv));
      if (nbt.contains("Xp", NbtElement.INT_TYPE)) {
         this.experience = nbt.getInt("Xp");
      }

      this.lastRestockTime = nbt.getLong("LastRestock");
      this.lastGossipDecayTime = nbt.getLong("LastGossipDecay");
      this.setCanPickUpLoot(true);
      if (this.world instanceof ServerWorld) {
         this.reinitializeBrain((ServerWorld)this.world);
      }

      this.restocksToday = nbt.getInt("RestocksToday");
      if (nbt.contains("AssignProfessionWhenSpawned")) {
         this.natural = nbt.getBoolean("AssignProfessionWhenSpawned");
      }

   }

   public boolean canImmediatelyDespawn(double distanceSquared) {
      return false;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      if (this.isSleeping()) {
         return null;
      } else {
         return this.hasCustomer() ? SoundEvents.ENTITY_VILLAGER_TRADE : SoundEvents.ENTITY_VILLAGER_AMBIENT;
      }
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_VILLAGER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_VILLAGER_DEATH;
   }

   public void playWorkSound() {
      SoundEvent lv = this.getVillagerData().getProfession().workSound();
      if (lv != null) {
         this.playSound(lv, this.getSoundVolume(), this.getSoundPitch());
      }

   }

   public void setVillagerData(VillagerData villagerData) {
      VillagerData lv = this.getVillagerData();
      if (lv.getProfession() != villagerData.getProfession()) {
         this.offers = null;
      }

      this.dataTracker.set(VILLAGER_DATA, villagerData);
   }

   public VillagerData getVillagerData() {
      return (VillagerData)this.dataTracker.get(VILLAGER_DATA);
   }

   protected void afterUsing(TradeOffer offer) {
      int i = 3 + this.random.nextInt(4);
      this.experience += offer.getMerchantExperience();
      this.lastCustomer = this.getCustomer();
      if (this.canLevelUp()) {
         this.levelUpTimer = 40;
         this.levelingUp = true;
         i += 5;
      }

      if (offer.shouldRewardPlayerExperience()) {
         this.world.spawnEntity(new ExperienceOrbEntity(this.world, this.getX(), this.getY() + 0.5, this.getZ(), i));
      }

   }

   public void method_35201(boolean bl) {
      this.field_30612 = bl;
   }

   public boolean method_35200() {
      return this.field_30612;
   }

   public void setAttacker(@Nullable LivingEntity attacker) {
      if (attacker != null && this.world instanceof ServerWorld) {
         ((ServerWorld)this.world).handleInteraction(EntityInteraction.VILLAGER_HURT, attacker, this);
         if (this.isAlive() && attacker instanceof PlayerEntity) {
            this.world.sendEntityStatus(this, EntityStatuses.ADD_VILLAGER_ANGRY_PARTICLES);
         }
      }

      super.setAttacker(attacker);
   }

   public void onDeath(DamageSource damageSource) {
      LOGGER.info("Villager {} died, message: '{}'", this, damageSource.getDeathMessage(this).getString());
      Entity lv = damageSource.getAttacker();
      if (lv != null) {
         this.notifyDeath(lv);
      }

      this.releaseAllTickets();
      super.onDeath(damageSource);
   }

   private void releaseAllTickets() {
      this.releaseTicketFor(MemoryModuleType.HOME);
      this.releaseTicketFor(MemoryModuleType.JOB_SITE);
      this.releaseTicketFor(MemoryModuleType.POTENTIAL_JOB_SITE);
      this.releaseTicketFor(MemoryModuleType.MEETING_POINT);
   }

   private void notifyDeath(Entity killer) {
      World var3 = this.world;
      if (var3 instanceof ServerWorld lv) {
         Optional optional = this.brain.getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS);
         if (!optional.isEmpty()) {
            LivingTargetCache var10000 = (LivingTargetCache)optional.get();
            Objects.requireNonNull(InteractionObserver.class);
            var10000.iterate(InteractionObserver.class::isInstance).forEach((observer) -> {
               lv.handleInteraction(EntityInteraction.VILLAGER_KILLED, killer, (InteractionObserver)observer);
            });
         }
      }
   }

   public void releaseTicketFor(MemoryModuleType pos) {
      if (this.world instanceof ServerWorld) {
         MinecraftServer minecraftServer = ((ServerWorld)this.world).getServer();
         this.brain.getOptionalRegisteredMemory(pos).ifPresent((posx) -> {
            ServerWorld lv = minecraftServer.getWorld(posx.getDimension());
            if (lv != null) {
               PointOfInterestStorage lv2 = lv.getPointOfInterestStorage();
               Optional optional = lv2.getType(posx.getPos());
               BiPredicate biPredicate = (BiPredicate)POINTS_OF_INTEREST.get(pos);
               if (optional.isPresent() && biPredicate.test(this, (RegistryEntry)optional.get())) {
                  lv2.releaseTicket(posx.getPos());
                  DebugInfoSender.sendPointOfInterest(lv, posx.getPos());
               }

            }
         });
      }
   }

   public boolean isReadyToBreed() {
      return this.foodLevel + this.getAvailableFood() >= 12 && !this.isSleeping() && this.getBreedingAge() == 0;
   }

   private boolean lacksFood() {
      return this.foodLevel < 12;
   }

   private void consumeAvailableFood() {
      if (this.lacksFood() && this.getAvailableFood() != 0) {
         for(int i = 0; i < this.getInventory().size(); ++i) {
            ItemStack lv = this.getInventory().getStack(i);
            if (!lv.isEmpty()) {
               Integer integer = (Integer)ITEM_FOOD_VALUES.get(lv.getItem());
               if (integer != null) {
                  int j = lv.getCount();

                  for(int k = j; k > 0; --k) {
                     this.foodLevel += integer;
                     this.getInventory().removeStack(i, 1);
                     if (!this.lacksFood()) {
                        return;
                     }
                  }
               }
            }
         }

      }
   }

   public int getReputation(PlayerEntity player) {
      return this.gossip.getReputationFor(player.getUuid(), (gossipType) -> {
         return true;
      });
   }

   private void depleteFood(int amount) {
      this.foodLevel -= amount;
   }

   public void eatForBreeding() {
      this.consumeAvailableFood();
      this.depleteFood(12);
   }

   public void setOffers(TradeOfferList offers) {
      this.offers = offers;
   }

   private boolean canLevelUp() {
      int i = this.getVillagerData().getLevel();
      return VillagerData.canLevelUp(i) && this.experience >= VillagerData.getUpperLevelExperience(i);
   }

   private void levelUp() {
      this.setVillagerData(this.getVillagerData().withLevel(this.getVillagerData().getLevel() + 1));
      this.fillRecipes();
   }

   protected Text getDefaultName() {
      String var10000 = this.getType().getTranslationKey();
      return Text.translatable(var10000 + "." + Registries.VILLAGER_PROFESSION.getId(this.getVillagerData().getProfession()).getPath());
   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.ADD_VILLAGER_HEART_PARTICLES) {
         this.produceParticles(ParticleTypes.HEART);
      } else if (status == EntityStatuses.ADD_VILLAGER_ANGRY_PARTICLES) {
         this.produceParticles(ParticleTypes.ANGRY_VILLAGER);
      } else if (status == EntityStatuses.ADD_VILLAGER_HAPPY_PARTICLES) {
         this.produceParticles(ParticleTypes.HAPPY_VILLAGER);
      } else if (status == EntityStatuses.ADD_SPLASH_PARTICLES) {
         this.produceParticles(ParticleTypes.SPLASH);
      } else {
         super.handleStatus(status);
      }

   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      if (spawnReason == SpawnReason.BREEDING) {
         this.setVillagerData(this.getVillagerData().withProfession(VillagerProfession.NONE));
      }

      if (spawnReason == SpawnReason.COMMAND || spawnReason == SpawnReason.SPAWN_EGG || spawnReason == SpawnReason.SPAWNER || spawnReason == SpawnReason.DISPENSER) {
         this.setVillagerData(this.getVillagerData().withType(VillagerType.forBiome(world.getBiome(this.getBlockPos()))));
      }

      if (spawnReason == SpawnReason.STRUCTURE) {
         this.natural = true;
      }

      return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
   }

   @Nullable
   public VillagerEntity createChild(ServerWorld arg, PassiveEntity arg2) {
      double d = this.random.nextDouble();
      VillagerType lv;
      if (d < 0.5) {
         lv = VillagerType.forBiome(arg.getBiome(this.getBlockPos()));
      } else if (d < 0.75) {
         lv = this.getVillagerData().getType();
      } else {
         lv = ((VillagerEntity)arg2).getVillagerData().getType();
      }

      VillagerEntity lv2 = new VillagerEntity(EntityType.VILLAGER, arg, lv);
      lv2.initialize(arg, arg.getLocalDifficulty(lv2.getBlockPos()), SpawnReason.BREEDING, (EntityData)null, (NbtCompound)null);
      return lv2;
   }

   public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
      if (world.getDifficulty() != Difficulty.PEACEFUL) {
         LOGGER.info("Villager {} was struck by lightning {}.", this, lightning);
         WitchEntity lv = (WitchEntity)EntityType.WITCH.create(world);
         if (lv != null) {
            lv.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
            lv.initialize(world, world.getLocalDifficulty(lv.getBlockPos()), SpawnReason.CONVERSION, (EntityData)null, (NbtCompound)null);
            lv.setAiDisabled(this.isAiDisabled());
            if (this.hasCustomName()) {
               lv.setCustomName(this.getCustomName());
               lv.setCustomNameVisible(this.isCustomNameVisible());
            }

            lv.setPersistent();
            world.spawnEntityAndPassengers(lv);
            this.releaseAllTickets();
            this.discard();
         } else {
            super.onStruckByLightning(world, lightning);
         }
      } else {
         super.onStruckByLightning(world, lightning);
      }

   }

   protected void loot(ItemEntity item) {
      InventoryOwner.pickUpItem(this, this, item);
   }

   public boolean canGather(ItemStack stack) {
      Item lv = stack.getItem();
      return (GATHERABLE_ITEMS.contains(lv) || this.getVillagerData().getProfession().gatherableItems().contains(lv)) && this.getInventory().canInsert(stack);
   }

   public boolean wantsToStartBreeding() {
      return this.getAvailableFood() >= 24;
   }

   public boolean canBreed() {
      return this.getAvailableFood() < 12;
   }

   private int getAvailableFood() {
      SimpleInventory lv = this.getInventory();
      return ITEM_FOOD_VALUES.entrySet().stream().mapToInt((item) -> {
         return lv.count((Item)item.getKey()) * (Integer)item.getValue();
      }).sum();
   }

   public boolean hasSeedToPlant() {
      return this.getInventory().containsAny(ImmutableSet.of(Items.WHEAT_SEEDS, Items.POTATO, Items.CARROT, Items.BEETROOT_SEEDS));
   }

   protected void fillRecipes() {
      VillagerData lv = this.getVillagerData();
      Int2ObjectMap int2ObjectMap = (Int2ObjectMap)TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(lv.getProfession());
      if (int2ObjectMap != null && !int2ObjectMap.isEmpty()) {
         TradeOffers.Factory[] lvs = (TradeOffers.Factory[])int2ObjectMap.get(lv.getLevel());
         if (lvs != null) {
            TradeOfferList lv2 = this.getOffers();
            this.fillRecipesFromPool(lv2, lvs, 2);
         }
      }
   }

   public void talkWithVillager(ServerWorld world, VillagerEntity villager, long time) {
      if ((time < this.gossipStartTime || time >= this.gossipStartTime + 1200L) && (time < villager.gossipStartTime || time >= villager.gossipStartTime + 1200L)) {
         this.gossip.shareGossipFrom(villager.gossip, this.random, 10);
         this.gossipStartTime = time;
         villager.gossipStartTime = time;
         this.summonGolem(world, time, 5);
      }
   }

   private void decayGossip() {
      long l = this.world.getTime();
      if (this.lastGossipDecayTime == 0L) {
         this.lastGossipDecayTime = l;
      } else if (l >= this.lastGossipDecayTime + 24000L) {
         this.gossip.decay();
         this.lastGossipDecayTime = l;
      }
   }

   public void summonGolem(ServerWorld world, long time, int requiredCount) {
      if (this.canSummonGolem(time)) {
         Box lv = this.getBoundingBox().expand(10.0, 10.0, 10.0);
         List list = world.getNonSpectatingEntities(VillagerEntity.class, lv);
         List list2 = (List)list.stream().filter((villager) -> {
            return villager.canSummonGolem(time);
         }).limit(5L).collect(Collectors.toList());
         if (list2.size() >= requiredCount) {
            if (LargeEntitySpawnHelper.trySpawnAt(EntityType.IRON_GOLEM, SpawnReason.MOB_SUMMONED, world, this.getBlockPos(), 10, 8, 6, LargeEntitySpawnHelper.Requirements.IRON_GOLEM).isPresent()) {
               list.forEach(GolemLastSeenSensor::rememberIronGolem);
            }
         }
      }
   }

   public boolean canSummonGolem(long time) {
      if (!this.hasRecentlySlept(this.world.getTime())) {
         return false;
      } else {
         return !this.brain.hasMemoryModule(MemoryModuleType.GOLEM_DETECTED_RECENTLY);
      }
   }

   public void onInteractionWith(EntityInteraction interaction, Entity entity) {
      if (interaction == EntityInteraction.ZOMBIE_VILLAGER_CURED) {
         this.gossip.startGossip(entity.getUuid(), VillageGossipType.MAJOR_POSITIVE, 20);
         this.gossip.startGossip(entity.getUuid(), VillageGossipType.MINOR_POSITIVE, 25);
      } else if (interaction == EntityInteraction.TRADE) {
         this.gossip.startGossip(entity.getUuid(), VillageGossipType.TRADING, 2);
      } else if (interaction == EntityInteraction.VILLAGER_HURT) {
         this.gossip.startGossip(entity.getUuid(), VillageGossipType.MINOR_NEGATIVE, 25);
      } else if (interaction == EntityInteraction.VILLAGER_KILLED) {
         this.gossip.startGossip(entity.getUuid(), VillageGossipType.MAJOR_NEGATIVE, 25);
      }

   }

   public int getExperience() {
      return this.experience;
   }

   public void setExperience(int experience) {
      this.experience = experience;
   }

   private void clearDailyRestockCount() {
      this.restockAndUpdateDemandBonus();
      this.restocksToday = 0;
   }

   public VillagerGossips getGossip() {
      return this.gossip;
   }

   public void readGossipDataNbt(NbtElement nbt) {
      this.gossip.deserialize(new Dynamic(NbtOps.INSTANCE, nbt));
   }

   protected void sendAiDebugData() {
      super.sendAiDebugData();
      DebugInfoSender.sendBrainDebugData(this);
   }

   public void sleep(BlockPos pos) {
      super.sleep(pos);
      this.brain.remember(MemoryModuleType.LAST_SLEPT, (Object)this.world.getTime());
      this.brain.forget(MemoryModuleType.WALK_TARGET);
      this.brain.forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
   }

   public void wakeUp() {
      super.wakeUp();
      this.brain.remember(MemoryModuleType.LAST_WOKEN, (Object)this.world.getTime());
   }

   private boolean hasRecentlySlept(long worldTime) {
      Optional optional = this.brain.getOptionalRegisteredMemory(MemoryModuleType.LAST_SLEPT);
      if (optional.isPresent()) {
         return worldTime - (Long)optional.get() < 24000L;
      } else {
         return false;
      }
   }

   // $FF: synthetic method
   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      return this.createChild(world, entity);
   }

   static {
      VILLAGER_DATA = DataTracker.registerData(VillagerEntity.class, TrackedDataHandlerRegistry.VILLAGER_DATA);
      ITEM_FOOD_VALUES = ImmutableMap.of(Items.BREAD, 4, Items.POTATO, 1, Items.CARROT, 1, Items.BEETROOT, 1);
      GATHERABLE_ITEMS = ImmutableSet.of(Items.BREAD, Items.POTATO, Items.CARROT, Items.WHEAT, Items.WHEAT_SEEDS, Items.BEETROOT, new Item[]{Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD});
      MEMORY_MODULES = ImmutableList.of(MemoryModuleType.HOME, MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleType.MEETING_POINT, MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, new MemoryModuleType[]{MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.BREED_TARGET, MemoryModuleType.PATH, MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.NEAREST_BED, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.NEAREST_HOSTILE, MemoryModuleType.SECONDARY_JOB_SITE, MemoryModuleType.HIDING_PLACE, MemoryModuleType.HEARD_BELL_TIME, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.LAST_SLEPT, MemoryModuleType.LAST_WOKEN, MemoryModuleType.LAST_WORKED_AT_POI, MemoryModuleType.GOLEM_DETECTED_RECENTLY});
      SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.NEAREST_BED, SensorType.HURT_BY, SensorType.VILLAGER_HOSTILES, SensorType.VILLAGER_BABIES, SensorType.SECONDARY_POIS, SensorType.GOLEM_DETECTED);
      POINTS_OF_INTEREST = ImmutableMap.of(MemoryModuleType.HOME, (villager, arg2) -> {
         return arg2.matchesKey(PointOfInterestTypes.HOME);
      }, MemoryModuleType.JOB_SITE, (villager, arg2) -> {
         return villager.getVillagerData().getProfession().heldWorkstation().test(arg2);
      }, MemoryModuleType.POTENTIAL_JOB_SITE, (villager, arg2) -> {
         return VillagerProfession.IS_ACQUIRABLE_JOB_SITE.test(arg2);
      }, MemoryModuleType.MEETING_POINT, (villager, arg2) -> {
         return arg2.matchesKey(PointOfInterestTypes.MEETING);
      });
   }
}
