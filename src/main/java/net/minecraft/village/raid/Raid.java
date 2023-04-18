package net.minecraft.village.raid;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BannerPatterns;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class Raid {
   private static final int field_30676 = 2;
   private static final int field_30677 = 0;
   private static final int field_30678 = 1;
   private static final int field_30679 = 2;
   private static final int field_30680 = 32;
   private static final int field_30681 = 48000;
   private static final int field_30682 = 3;
   private static final String OMINOUS_BANNER_TRANSLATION_KEY = "block.minecraft.ominous_banner";
   private static final String RAIDERS_REMAINING_TRANSLATION_KEY = "event.minecraft.raid.raiders_remaining";
   public static final int field_30669 = 16;
   private static final int field_30685 = 40;
   private static final int DEFAULT_PRE_RAID_TICKS = 300;
   public static final int MAX_DESPAWN_COUNTER = 2400;
   public static final int field_30671 = 600;
   private static final int field_30687 = 30;
   public static final int field_30672 = 24000;
   public static final int field_30673 = 5;
   private static final int field_30688 = 2;
   private static final Text EVENT_TEXT = Text.translatable("event.minecraft.raid");
   private static final Text VICTORY_SUFFIX_TEXT = Text.translatable("event.minecraft.raid.victory");
   private static final Text DEFEAT_SUFFIX_TEXT = Text.translatable("event.minecraft.raid.defeat");
   private static final Text VICTORY_TITLE;
   private static final Text DEFEAT_TITLE;
   private static final int MAX_ACTIVE_TICKS = 48000;
   public static final int field_30674 = 9216;
   public static final int SQUARED_MAX_RAIDER_DISTANCE = 12544;
   private final Map waveToCaptain = Maps.newHashMap();
   private final Map waveToRaiders = Maps.newHashMap();
   private final Set heroesOfTheVillage = Sets.newHashSet();
   private long ticksActive;
   private BlockPos center;
   private final ServerWorld world;
   private boolean started;
   private final int id;
   private float totalHealth;
   private int badOmenLevel;
   private boolean active;
   private int wavesSpawned;
   private final ServerBossBar bar;
   private int postRaidTicks;
   private int preRaidTicks;
   private final Random random;
   private final int waveCount;
   private Status status;
   private int finishCooldown;
   private Optional preCalculatedRavagerSpawnLocation;

   public Raid(int id, ServerWorld world, BlockPos pos) {
      this.bar = new ServerBossBar(EVENT_TEXT, BossBar.Color.RED, BossBar.Style.NOTCHED_10);
      this.random = Random.create();
      this.preCalculatedRavagerSpawnLocation = Optional.empty();
      this.id = id;
      this.world = world;
      this.active = true;
      this.preRaidTicks = 300;
      this.bar.setPercent(0.0F);
      this.center = pos;
      this.waveCount = this.getMaxWaves(world.getDifficulty());
      this.status = Raid.Status.ONGOING;
   }

   public Raid(ServerWorld world, NbtCompound nbt) {
      this.bar = new ServerBossBar(EVENT_TEXT, BossBar.Color.RED, BossBar.Style.NOTCHED_10);
      this.random = Random.create();
      this.preCalculatedRavagerSpawnLocation = Optional.empty();
      this.world = world;
      this.id = nbt.getInt("Id");
      this.started = nbt.getBoolean("Started");
      this.active = nbt.getBoolean("Active");
      this.ticksActive = nbt.getLong("TicksActive");
      this.badOmenLevel = nbt.getInt("BadOmenLevel");
      this.wavesSpawned = nbt.getInt("GroupsSpawned");
      this.preRaidTicks = nbt.getInt("PreRaidTicks");
      this.postRaidTicks = nbt.getInt("PostRaidTicks");
      this.totalHealth = nbt.getFloat("TotalHealth");
      this.center = new BlockPos(nbt.getInt("CX"), nbt.getInt("CY"), nbt.getInt("CZ"));
      this.waveCount = nbt.getInt("NumGroups");
      this.status = Raid.Status.fromName(nbt.getString("Status"));
      this.heroesOfTheVillage.clear();
      if (nbt.contains("HeroesOfTheVillage", NbtElement.LIST_TYPE)) {
         NbtList lv = nbt.getList("HeroesOfTheVillage", NbtElement.INT_ARRAY_TYPE);

         for(int i = 0; i < lv.size(); ++i) {
            this.heroesOfTheVillage.add(NbtHelper.toUuid(lv.get(i)));
         }
      }

   }

   public boolean isFinished() {
      return this.hasWon() || this.hasLost();
   }

   public boolean isPreRaid() {
      return this.hasSpawned() && this.getRaiderCount() == 0 && this.preRaidTicks > 0;
   }

   public boolean hasSpawned() {
      return this.wavesSpawned > 0;
   }

   public boolean hasStopped() {
      return this.status == Raid.Status.STOPPED;
   }

   public boolean hasWon() {
      return this.status == Raid.Status.VICTORY;
   }

   public boolean hasLost() {
      return this.status == Raid.Status.LOSS;
   }

   public float getTotalHealth() {
      return this.totalHealth;
   }

   public Set getAllRaiders() {
      Set set = Sets.newHashSet();
      Iterator var2 = this.waveToRaiders.values().iterator();

      while(var2.hasNext()) {
         Set set2 = (Set)var2.next();
         set.addAll(set2);
      }

      return set;
   }

   public World getWorld() {
      return this.world;
   }

   public boolean hasStarted() {
      return this.started;
   }

   public int getGroupsSpawned() {
      return this.wavesSpawned;
   }

   private Predicate isInRaidDistance() {
      return (player) -> {
         BlockPos lv = player.getBlockPos();
         return player.isAlive() && this.world.getRaidAt(lv) == this;
      };
   }

   private void updateBarToPlayers() {
      Set set = Sets.newHashSet(this.bar.getPlayers());
      List list = this.world.getPlayers(this.isInRaidDistance());
      Iterator var3 = list.iterator();

      ServerPlayerEntity lv;
      while(var3.hasNext()) {
         lv = (ServerPlayerEntity)var3.next();
         if (!set.contains(lv)) {
            this.bar.addPlayer(lv);
         }
      }

      var3 = set.iterator();

      while(var3.hasNext()) {
         lv = (ServerPlayerEntity)var3.next();
         if (!list.contains(lv)) {
            this.bar.removePlayer(lv);
         }
      }

   }

   public int getMaxAcceptableBadOmenLevel() {
      return 5;
   }

   public int getBadOmenLevel() {
      return this.badOmenLevel;
   }

   public void setBadOmenLevel(int badOmenLevel) {
      this.badOmenLevel = badOmenLevel;
   }

   public void start(PlayerEntity player) {
      if (player.hasStatusEffect(StatusEffects.BAD_OMEN)) {
         this.badOmenLevel += player.getStatusEffect(StatusEffects.BAD_OMEN).getAmplifier() + 1;
         this.badOmenLevel = MathHelper.clamp(this.badOmenLevel, 0, this.getMaxAcceptableBadOmenLevel());
      }

      player.removeStatusEffect(StatusEffects.BAD_OMEN);
   }

   public void invalidate() {
      this.active = false;
      this.bar.clearPlayers();
      this.status = Raid.Status.STOPPED;
   }

   public void tick() {
      if (!this.hasStopped()) {
         if (this.status == Raid.Status.ONGOING) {
            boolean bl = this.active;
            this.active = this.world.isChunkLoaded(this.center);
            if (this.world.getDifficulty() == Difficulty.PEACEFUL) {
               this.invalidate();
               return;
            }

            if (bl != this.active) {
               this.bar.setVisible(this.active);
            }

            if (!this.active) {
               return;
            }

            if (!this.world.isNearOccupiedPointOfInterest(this.center)) {
               this.moveRaidCenter();
            }

            if (!this.world.isNearOccupiedPointOfInterest(this.center)) {
               if (this.wavesSpawned > 0) {
                  this.status = Raid.Status.LOSS;
               } else {
                  this.invalidate();
               }
            }

            ++this.ticksActive;
            if (this.ticksActive >= 48000L) {
               this.invalidate();
               return;
            }

            int i = this.getRaiderCount();
            boolean bl2;
            if (i == 0 && this.shouldSpawnMoreGroups()) {
               if (this.preRaidTicks <= 0) {
                  if (this.preRaidTicks == 0 && this.wavesSpawned > 0) {
                     this.preRaidTicks = 300;
                     this.bar.setName(EVENT_TEXT);
                     return;
                  }
               } else {
                  bl2 = this.preCalculatedRavagerSpawnLocation.isPresent();
                  boolean bl3 = !bl2 && this.preRaidTicks % 5 == 0;
                  if (bl2 && !this.world.shouldTickEntity((BlockPos)this.preCalculatedRavagerSpawnLocation.get())) {
                     bl3 = true;
                  }

                  if (bl3) {
                     int j = 0;
                     if (this.preRaidTicks < 100) {
                        j = 1;
                     } else if (this.preRaidTicks < 40) {
                        j = 2;
                     }

                     this.preCalculatedRavagerSpawnLocation = this.preCalculateRavagerSpawnLocation(j);
                  }

                  if (this.preRaidTicks == 300 || this.preRaidTicks % 20 == 0) {
                     this.updateBarToPlayers();
                  }

                  --this.preRaidTicks;
                  this.bar.setPercent(MathHelper.clamp((float)(300 - this.preRaidTicks) / 300.0F, 0.0F, 1.0F));
               }
            }

            if (this.ticksActive % 20L == 0L) {
               this.updateBarToPlayers();
               this.removeObsoleteRaiders();
               if (i > 0) {
                  if (i <= 2) {
                     this.bar.setName(EVENT_TEXT.copy().append(" - ").append((Text)Text.translatable("event.minecraft.raid.raiders_remaining", i)));
                  } else {
                     this.bar.setName(EVENT_TEXT);
                  }
               } else {
                  this.bar.setName(EVENT_TEXT);
               }
            }

            bl2 = false;
            int k = 0;

            while(this.canSpawnRaiders()) {
               BlockPos lv = this.preCalculatedRavagerSpawnLocation.isPresent() ? (BlockPos)this.preCalculatedRavagerSpawnLocation.get() : this.getRavagerSpawnLocation(k, 20);
               if (lv != null) {
                  this.started = true;
                  this.spawnNextWave(lv);
                  if (!bl2) {
                     this.playRaidHorn(lv);
                     bl2 = true;
                  }
               } else {
                  ++k;
               }

               if (k > 3) {
                  this.invalidate();
                  break;
               }
            }

            if (this.hasStarted() && !this.shouldSpawnMoreGroups() && i == 0) {
               if (this.postRaidTicks < 40) {
                  ++this.postRaidTicks;
               } else {
                  this.status = Raid.Status.VICTORY;
                  Iterator var12 = this.heroesOfTheVillage.iterator();

                  while(var12.hasNext()) {
                     UUID uUID = (UUID)var12.next();
                     Entity lv2 = this.world.getEntity(uUID);
                     if (lv2 instanceof LivingEntity && !lv2.isSpectator()) {
                        LivingEntity lv3 = (LivingEntity)lv2;
                        lv3.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 48000, this.badOmenLevel - 1, false, false, true));
                        if (lv3 instanceof ServerPlayerEntity) {
                           ServerPlayerEntity lv4 = (ServerPlayerEntity)lv3;
                           lv4.incrementStat(Stats.RAID_WIN);
                           Criteria.HERO_OF_THE_VILLAGE.trigger(lv4);
                        }
                     }
                  }
               }
            }

            this.markDirty();
         } else if (this.isFinished()) {
            ++this.finishCooldown;
            if (this.finishCooldown >= 600) {
               this.invalidate();
               return;
            }

            if (this.finishCooldown % 20 == 0) {
               this.updateBarToPlayers();
               this.bar.setVisible(true);
               if (this.hasWon()) {
                  this.bar.setPercent(0.0F);
                  this.bar.setName(VICTORY_TITLE);
               } else {
                  this.bar.setName(DEFEAT_TITLE);
               }
            }
         }

      }
   }

   private void moveRaidCenter() {
      Stream stream = ChunkSectionPos.stream(ChunkSectionPos.from(this.center), 2);
      ServerWorld var10001 = this.world;
      Objects.requireNonNull(var10001);
      stream.filter(var10001::isNearOccupiedPointOfInterest).map(ChunkSectionPos::getCenterPos).min(Comparator.comparingDouble((pos) -> {
         return pos.getSquaredDistance(this.center);
      })).ifPresent(this::setCenter);
   }

   private Optional preCalculateRavagerSpawnLocation(int proximity) {
      for(int j = 0; j < 3; ++j) {
         BlockPos lv = this.getRavagerSpawnLocation(proximity, 1);
         if (lv != null) {
            return Optional.of(lv);
         }
      }

      return Optional.empty();
   }

   private boolean shouldSpawnMoreGroups() {
      if (this.hasExtraWave()) {
         return !this.hasSpawnedExtraWave();
      } else {
         return !this.hasSpawnedFinalWave();
      }
   }

   private boolean hasSpawnedFinalWave() {
      return this.getGroupsSpawned() == this.waveCount;
   }

   private boolean hasExtraWave() {
      return this.badOmenLevel > 1;
   }

   private boolean hasSpawnedExtraWave() {
      return this.getGroupsSpawned() > this.waveCount;
   }

   private boolean isSpawningExtraWave() {
      return this.hasSpawnedFinalWave() && this.getRaiderCount() == 0 && this.hasExtraWave();
   }

   private void removeObsoleteRaiders() {
      Iterator iterator = this.waveToRaiders.values().iterator();
      Set set = Sets.newHashSet();

      label54:
      while(iterator.hasNext()) {
         Set set2 = (Set)iterator.next();
         Iterator var4 = set2.iterator();

         while(true) {
            while(true) {
               if (!var4.hasNext()) {
                  continue label54;
               }

               RaiderEntity lv = (RaiderEntity)var4.next();
               BlockPos lv2 = lv.getBlockPos();
               if (!lv.isRemoved() && lv.world.getRegistryKey() == this.world.getRegistryKey() && !(this.center.getSquaredDistance(lv2) >= 12544.0)) {
                  if (lv.age > 600) {
                     if (this.world.getEntity(lv.getUuid()) == null) {
                        set.add(lv);
                     }

                     if (!this.world.isNearOccupiedPointOfInterest(lv2) && lv.getDespawnCounter() > 2400) {
                        lv.setOutOfRaidCounter(lv.getOutOfRaidCounter() + 1);
                     }

                     if (lv.getOutOfRaidCounter() >= 30) {
                        set.add(lv);
                     }
                  }
               } else {
                  set.add(lv);
               }
            }
         }
      }

      Iterator var7 = set.iterator();

      while(var7.hasNext()) {
         RaiderEntity lv3 = (RaiderEntity)var7.next();
         this.removeFromWave(lv3, true);
      }

   }

   private void playRaidHorn(BlockPos pos) {
      float f = 13.0F;
      int i = true;
      Collection collection = this.bar.getPlayers();
      long l = this.random.nextLong();
      Iterator var7 = this.world.getPlayers().iterator();

      while(true) {
         ServerPlayerEntity lv;
         double d;
         double e;
         double g;
         do {
            if (!var7.hasNext()) {
               return;
            }

            lv = (ServerPlayerEntity)var7.next();
            Vec3d lv2 = lv.getPos();
            Vec3d lv3 = Vec3d.ofCenter(pos);
            d = Math.sqrt((lv3.x - lv2.x) * (lv3.x - lv2.x) + (lv3.z - lv2.z) * (lv3.z - lv2.z));
            e = lv2.x + 13.0 / d * (lv3.x - lv2.x);
            g = lv2.z + 13.0 / d * (lv3.z - lv2.z);
         } while(!(d <= 64.0) && !collection.contains(lv));

         lv.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.EVENT_RAID_HORN, SoundCategory.NEUTRAL, e, lv.getY(), g, 64.0F, 1.0F, l));
      }
   }

   private void spawnNextWave(BlockPos pos) {
      boolean bl = false;
      int i = this.wavesSpawned + 1;
      this.totalHealth = 0.0F;
      LocalDifficulty lv = this.world.getLocalDifficulty(pos);
      boolean bl2 = this.isSpawningExtraWave();
      Member[] var6 = Raid.Member.VALUES;
      int var7 = var6.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         Member lv2 = var6[var8];
         int j = this.getCount(lv2, i, bl2) + this.getBonusCount(lv2, this.random, i, lv, bl2);
         int k = 0;

         for(int l = 0; l < j; ++l) {
            RaiderEntity lv3 = (RaiderEntity)lv2.type.create(this.world);
            if (lv3 == null) {
               break;
            }

            if (!bl && lv3.canLead()) {
               lv3.setPatrolLeader(true);
               this.setWaveCaptain(i, lv3);
               bl = true;
            }

            this.addRaider(i, lv3, pos, false);
            if (lv2.type == EntityType.RAVAGER) {
               RaiderEntity lv4 = null;
               if (i == this.getMaxWaves(Difficulty.NORMAL)) {
                  lv4 = (RaiderEntity)EntityType.PILLAGER.create(this.world);
               } else if (i >= this.getMaxWaves(Difficulty.HARD)) {
                  if (k == 0) {
                     lv4 = (RaiderEntity)EntityType.EVOKER.create(this.world);
                  } else {
                     lv4 = (RaiderEntity)EntityType.VINDICATOR.create(this.world);
                  }
               }

               ++k;
               if (lv4 != null) {
                  this.addRaider(i, lv4, pos, false);
                  lv4.refreshPositionAndAngles(pos, 0.0F, 0.0F);
                  lv4.startRiding(lv3);
               }
            }
         }
      }

      this.preCalculatedRavagerSpawnLocation = Optional.empty();
      ++this.wavesSpawned;
      this.updateBar();
      this.markDirty();
   }

   public void addRaider(int wave, RaiderEntity raider, @Nullable BlockPos pos, boolean existing) {
      boolean bl2 = this.addToWave(wave, raider);
      if (bl2) {
         raider.setRaid(this);
         raider.setWave(wave);
         raider.setAbleToJoinRaid(true);
         raider.setOutOfRaidCounter(0);
         if (!existing && pos != null) {
            raider.setPosition((double)pos.getX() + 0.5, (double)pos.getY() + 1.0, (double)pos.getZ() + 0.5);
            raider.initialize(this.world, this.world.getLocalDifficulty(pos), SpawnReason.EVENT, (EntityData)null, (NbtCompound)null);
            raider.addBonusForWave(wave, false);
            raider.setOnGround(true);
            this.world.spawnEntityAndPassengers(raider);
         }
      }

   }

   public void updateBar() {
      this.bar.setPercent(MathHelper.clamp(this.getCurrentRaiderHealth() / this.totalHealth, 0.0F, 1.0F));
   }

   public float getCurrentRaiderHealth() {
      float f = 0.0F;
      Iterator var2 = this.waveToRaiders.values().iterator();

      while(var2.hasNext()) {
         Set set = (Set)var2.next();

         RaiderEntity lv;
         for(Iterator var4 = set.iterator(); var4.hasNext(); f += lv.getHealth()) {
            lv = (RaiderEntity)var4.next();
         }
      }

      return f;
   }

   private boolean canSpawnRaiders() {
      return this.preRaidTicks == 0 && (this.wavesSpawned < this.waveCount || this.isSpawningExtraWave()) && this.getRaiderCount() == 0;
   }

   public int getRaiderCount() {
      return this.waveToRaiders.values().stream().mapToInt(Set::size).sum();
   }

   public void removeFromWave(RaiderEntity entity, boolean countHealth) {
      Set set = (Set)this.waveToRaiders.get(entity.getWave());
      if (set != null) {
         boolean bl2 = set.remove(entity);
         if (bl2) {
            if (countHealth) {
               this.totalHealth -= entity.getHealth();
            }

            entity.setRaid((Raid)null);
            this.updateBar();
            this.markDirty();
         }
      }

   }

   private void markDirty() {
      this.world.getRaidManager().markDirty();
   }

   public static ItemStack getOminousBanner() {
      ItemStack lv = new ItemStack(Items.WHITE_BANNER);
      NbtCompound lv2 = new NbtCompound();
      NbtList lv3 = (new BannerPattern.Patterns()).add(BannerPatterns.RHOMBUS, DyeColor.CYAN).add(BannerPatterns.STRIPE_BOTTOM, DyeColor.LIGHT_GRAY).add(BannerPatterns.STRIPE_CENTER, DyeColor.GRAY).add(BannerPatterns.BORDER, DyeColor.LIGHT_GRAY).add(BannerPatterns.STRIPE_MIDDLE, DyeColor.BLACK).add(BannerPatterns.HALF_HORIZONTAL, DyeColor.LIGHT_GRAY).add(BannerPatterns.CIRCLE, DyeColor.LIGHT_GRAY).add(BannerPatterns.BORDER, DyeColor.BLACK).toNbt();
      lv2.put("Patterns", lv3);
      BlockItem.setBlockEntityNbt(lv, BlockEntityType.BANNER, lv2);
      lv.addHideFlag(ItemStack.TooltipSection.ADDITIONAL);
      lv.setCustomName(Text.translatable("block.minecraft.ominous_banner").formatted(Formatting.GOLD));
      return lv;
   }

   @Nullable
   public RaiderEntity getCaptain(int wave) {
      return (RaiderEntity)this.waveToCaptain.get(wave);
   }

   @Nullable
   private BlockPos getRavagerSpawnLocation(int proximity, int tries) {
      int k = proximity == 0 ? 2 : 2 - proximity;
      BlockPos.Mutable lv = new BlockPos.Mutable();

      for(int l = 0; l < tries; ++l) {
         float f = this.world.random.nextFloat() * 6.2831855F;
         int m = this.center.getX() + MathHelper.floor(MathHelper.cos(f) * 32.0F * (float)k) + this.world.random.nextInt(5);
         int n = this.center.getZ() + MathHelper.floor(MathHelper.sin(f) * 32.0F * (float)k) + this.world.random.nextInt(5);
         int o = this.world.getTopY(Heightmap.Type.WORLD_SURFACE, m, n);
         lv.set(m, o, n);
         if (!this.world.isNearOccupiedPointOfInterest((BlockPos)lv) || proximity >= 2) {
            int p = true;
            if (this.world.isRegionLoaded(lv.getX() - 10, lv.getZ() - 10, lv.getX() + 10, lv.getZ() + 10) && this.world.shouldTickEntity(lv) && (SpawnHelper.canSpawn(SpawnRestriction.Location.ON_GROUND, this.world, lv, EntityType.RAVAGER) || this.world.getBlockState(lv.down()).isOf(Blocks.SNOW) && this.world.getBlockState(lv).isAir())) {
               return lv;
            }
         }
      }

      return null;
   }

   private boolean addToWave(int wave, RaiderEntity entity) {
      return this.addToWave(wave, entity, true);
   }

   public boolean addToWave(int wave, RaiderEntity entity, boolean countHealth) {
      this.waveToRaiders.computeIfAbsent(wave, (wavex) -> {
         return Sets.newHashSet();
      });
      Set set = (Set)this.waveToRaiders.get(wave);
      RaiderEntity lv = null;
      Iterator var6 = set.iterator();

      while(var6.hasNext()) {
         RaiderEntity lv2 = (RaiderEntity)var6.next();
         if (lv2.getUuid().equals(entity.getUuid())) {
            lv = lv2;
            break;
         }
      }

      if (lv != null) {
         set.remove(lv);
         set.add(entity);
      }

      set.add(entity);
      if (countHealth) {
         this.totalHealth += entity.getHealth();
      }

      this.updateBar();
      this.markDirty();
      return true;
   }

   public void setWaveCaptain(int wave, RaiderEntity entity) {
      this.waveToCaptain.put(wave, entity);
      entity.equipStack(EquipmentSlot.HEAD, getOminousBanner());
      entity.setEquipmentDropChance(EquipmentSlot.HEAD, 2.0F);
   }

   public void removeLeader(int wave) {
      this.waveToCaptain.remove(wave);
   }

   public BlockPos getCenter() {
      return this.center;
   }

   private void setCenter(BlockPos center) {
      this.center = center;
   }

   public int getRaidId() {
      return this.id;
   }

   private int getCount(Member member, int wave, boolean extra) {
      return extra ? member.countInWave[this.waveCount] : member.countInWave[wave];
   }

   private int getBonusCount(Member member, Random random, int wave, LocalDifficulty localDifficulty, boolean extra) {
      Difficulty lv = localDifficulty.getGlobalDifficulty();
      boolean bl2 = lv == Difficulty.EASY;
      boolean bl3 = lv == Difficulty.NORMAL;
      int j;
      switch (member) {
         case WITCH:
            if (!bl2 && wave > 2 && wave != 4) {
               j = 1;
               break;
            }

            return 0;
         case PILLAGER:
         case VINDICATOR:
            if (bl2) {
               j = random.nextInt(2);
            } else if (bl3) {
               j = 1;
            } else {
               j = 2;
            }
            break;
         case RAVAGER:
            j = !bl2 && extra ? 1 : 0;
            break;
         default:
            return 0;
      }

      return j > 0 ? random.nextInt(j + 1) : 0;
   }

   public boolean isActive() {
      return this.active;
   }

   public NbtCompound writeNbt(NbtCompound nbt) {
      nbt.putInt("Id", this.id);
      nbt.putBoolean("Started", this.started);
      nbt.putBoolean("Active", this.active);
      nbt.putLong("TicksActive", this.ticksActive);
      nbt.putInt("BadOmenLevel", this.badOmenLevel);
      nbt.putInt("GroupsSpawned", this.wavesSpawned);
      nbt.putInt("PreRaidTicks", this.preRaidTicks);
      nbt.putInt("PostRaidTicks", this.postRaidTicks);
      nbt.putFloat("TotalHealth", this.totalHealth);
      nbt.putInt("NumGroups", this.waveCount);
      nbt.putString("Status", this.status.getName());
      nbt.putInt("CX", this.center.getX());
      nbt.putInt("CY", this.center.getY());
      nbt.putInt("CZ", this.center.getZ());
      NbtList lv = new NbtList();
      Iterator var3 = this.heroesOfTheVillage.iterator();

      while(var3.hasNext()) {
         UUID uUID = (UUID)var3.next();
         lv.add(NbtHelper.fromUuid(uUID));
      }

      nbt.put("HeroesOfTheVillage", lv);
      return nbt;
   }

   public int getMaxWaves(Difficulty difficulty) {
      switch (difficulty) {
         case EASY:
            return 3;
         case NORMAL:
            return 5;
         case HARD:
            return 7;
         default:
            return 0;
      }
   }

   public float getEnchantmentChance() {
      int i = this.getBadOmenLevel();
      if (i == 2) {
         return 0.1F;
      } else if (i == 3) {
         return 0.25F;
      } else if (i == 4) {
         return 0.5F;
      } else {
         return i == 5 ? 0.75F : 0.0F;
      }
   }

   public void addHero(Entity entity) {
      this.heroesOfTheVillage.add(entity.getUuid());
   }

   static {
      VICTORY_TITLE = EVENT_TEXT.copy().append(" - ").append(VICTORY_SUFFIX_TEXT);
      DEFEAT_TITLE = EVENT_TEXT.copy().append(" - ").append(DEFEAT_SUFFIX_TEXT);
   }

   private static enum Status {
      ONGOING,
      VICTORY,
      LOSS,
      STOPPED;

      private static final Status[] VALUES = values();

      static Status fromName(String name) {
         Status[] var1 = VALUES;
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            Status lv = var1[var3];
            if (name.equalsIgnoreCase(lv.name())) {
               return lv;
            }
         }

         return ONGOING;
      }

      public String getName() {
         return this.name().toLowerCase(Locale.ROOT);
      }

      // $FF: synthetic method
      private static Status[] method_36666() {
         return new Status[]{ONGOING, VICTORY, LOSS, STOPPED};
      }
   }

   private static enum Member {
      VINDICATOR(EntityType.VINDICATOR, new int[]{0, 0, 2, 0, 1, 4, 2, 5}),
      EVOKER(EntityType.EVOKER, new int[]{0, 0, 0, 0, 0, 1, 1, 2}),
      PILLAGER(EntityType.PILLAGER, new int[]{0, 4, 3, 3, 4, 4, 4, 2}),
      WITCH(EntityType.WITCH, new int[]{0, 0, 0, 0, 3, 0, 0, 1}),
      RAVAGER(EntityType.RAVAGER, new int[]{0, 0, 0, 1, 0, 1, 0, 2});

      static final Member[] VALUES = values();
      final EntityType type;
      final int[] countInWave;

      private Member(EntityType type, int[] countInWave) {
         this.type = type;
         this.countInWave = countInWave;
      }

      // $FF: synthetic method
      private static Member[] method_36667() {
         return new Member[]{VINDICATOR, EVOKER, PILLAGER, WITCH, RAVAGER};
      }
   }
}
