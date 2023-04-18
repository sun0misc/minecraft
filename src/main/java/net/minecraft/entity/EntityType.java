package net.minecraft.entity;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
import net.minecraft.entity.decoration.InteractionEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.StrayEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.ZoglinEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.SalmonEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.SnifferEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.passive.TadpoleEntity;
import net.minecraft.entity.passive.TraderLlamaEntity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.entity.vehicle.SpawnerMinecartEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.resource.featuretoggle.ToggleableFeature;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class EntityType implements ToggleableFeature, TypeFilter {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String ENTITY_TAG_KEY = "EntityTag";
   private final RegistryEntry.Reference registryEntry;
   private static final float field_30054 = 1.3964844F;
   private static final int field_42459 = 10;
   public static final EntityType ALLAY;
   public static final EntityType AREA_EFFECT_CLOUD;
   public static final EntityType ARMOR_STAND;
   public static final EntityType ARROW;
   public static final EntityType AXOLOTL;
   public static final EntityType BAT;
   public static final EntityType BEE;
   public static final EntityType BLAZE;
   public static final EntityType BLOCK_DISPLAY;
   public static final EntityType BOAT;
   public static final EntityType CAMEL;
   public static final EntityType CAT;
   public static final EntityType CAVE_SPIDER;
   public static final EntityType CHEST_BOAT;
   public static final EntityType CHEST_MINECART;
   public static final EntityType CHICKEN;
   public static final EntityType COD;
   public static final EntityType COMMAND_BLOCK_MINECART;
   public static final EntityType COW;
   public static final EntityType CREEPER;
   public static final EntityType DOLPHIN;
   public static final EntityType DONKEY;
   public static final EntityType DRAGON_FIREBALL;
   public static final EntityType DROWNED;
   public static final EntityType EGG;
   public static final EntityType ELDER_GUARDIAN;
   public static final EntityType END_CRYSTAL;
   public static final EntityType ENDER_DRAGON;
   public static final EntityType ENDER_PEARL;
   public static final EntityType ENDERMAN;
   public static final EntityType ENDERMITE;
   public static final EntityType EVOKER;
   public static final EntityType EVOKER_FANGS;
   public static final EntityType EXPERIENCE_BOTTLE;
   public static final EntityType EXPERIENCE_ORB;
   public static final EntityType EYE_OF_ENDER;
   public static final EntityType FALLING_BLOCK;
   public static final EntityType FIREWORK_ROCKET;
   public static final EntityType FOX;
   public static final EntityType FROG;
   public static final EntityType FURNACE_MINECART;
   public static final EntityType GHAST;
   public static final EntityType GIANT;
   public static final EntityType GLOW_ITEM_FRAME;
   public static final EntityType GLOW_SQUID;
   public static final EntityType GOAT;
   public static final EntityType GUARDIAN;
   public static final EntityType HOGLIN;
   public static final EntityType HOPPER_MINECART;
   public static final EntityType HORSE;
   public static final EntityType HUSK;
   public static final EntityType ILLUSIONER;
   public static final EntityType INTERACTION;
   public static final EntityType IRON_GOLEM;
   public static final EntityType ITEM;
   public static final EntityType ITEM_DISPLAY;
   public static final EntityType ITEM_FRAME;
   public static final EntityType FIREBALL;
   public static final EntityType LEASH_KNOT;
   public static final EntityType LIGHTNING_BOLT;
   public static final EntityType LLAMA;
   public static final EntityType LLAMA_SPIT;
   public static final EntityType MAGMA_CUBE;
   public static final EntityType MARKER;
   public static final EntityType MINECART;
   public static final EntityType MOOSHROOM;
   public static final EntityType MULE;
   public static final EntityType OCELOT;
   public static final EntityType PAINTING;
   public static final EntityType PANDA;
   public static final EntityType PARROT;
   public static final EntityType PHANTOM;
   public static final EntityType PIG;
   public static final EntityType PIGLIN;
   public static final EntityType PIGLIN_BRUTE;
   public static final EntityType PILLAGER;
   public static final EntityType POLAR_BEAR;
   public static final EntityType POTION;
   public static final EntityType PUFFERFISH;
   public static final EntityType RABBIT;
   public static final EntityType RAVAGER;
   public static final EntityType SALMON;
   public static final EntityType SHEEP;
   public static final EntityType SHULKER;
   public static final EntityType SHULKER_BULLET;
   public static final EntityType SILVERFISH;
   public static final EntityType SKELETON;
   public static final EntityType SKELETON_HORSE;
   public static final EntityType SLIME;
   public static final EntityType SMALL_FIREBALL;
   public static final EntityType SNIFFER;
   public static final EntityType SNOW_GOLEM;
   public static final EntityType SNOWBALL;
   public static final EntityType SPAWNER_MINECART;
   public static final EntityType SPECTRAL_ARROW;
   public static final EntityType SPIDER;
   public static final EntityType SQUID;
   public static final EntityType STRAY;
   public static final EntityType STRIDER;
   public static final EntityType TADPOLE;
   public static final EntityType TEXT_DISPLAY;
   public static final EntityType TNT;
   public static final EntityType TNT_MINECART;
   public static final EntityType TRADER_LLAMA;
   public static final EntityType TRIDENT;
   public static final EntityType TROPICAL_FISH;
   public static final EntityType TURTLE;
   public static final EntityType VEX;
   public static final EntityType VILLAGER;
   public static final EntityType VINDICATOR;
   public static final EntityType WANDERING_TRADER;
   public static final EntityType WARDEN;
   public static final EntityType WITCH;
   public static final EntityType WITHER;
   public static final EntityType WITHER_SKELETON;
   public static final EntityType WITHER_SKULL;
   public static final EntityType WOLF;
   public static final EntityType ZOGLIN;
   public static final EntityType ZOMBIE;
   public static final EntityType ZOMBIE_HORSE;
   public static final EntityType ZOMBIE_VILLAGER;
   public static final EntityType ZOMBIFIED_PIGLIN;
   public static final EntityType PLAYER;
   public static final EntityType FISHING_BOBBER;
   private final EntityFactory factory;
   private final SpawnGroup spawnGroup;
   private final ImmutableSet canSpawnInside;
   private final boolean saveable;
   private final boolean summonable;
   private final boolean fireImmune;
   private final boolean spawnableFarFromPlayer;
   private final int maxTrackDistance;
   private final int trackTickInterval;
   @Nullable
   private String translationKey;
   @Nullable
   private Text name;
   @Nullable
   private Identifier lootTableId;
   private final EntityDimensions dimensions;
   private final FeatureSet requiredFeatures;

   private static EntityType register(String id, Builder type) {
      return (EntityType)Registry.register(Registries.ENTITY_TYPE, (String)id, type.build(id));
   }

   public static Identifier getId(EntityType type) {
      return Registries.ENTITY_TYPE.getId(type);
   }

   public static Optional get(String id) {
      return Registries.ENTITY_TYPE.getOrEmpty(Identifier.tryParse(id));
   }

   public EntityType(EntityFactory factory, SpawnGroup spawnGroup, boolean saveable, boolean summonable, boolean fireImmune, boolean spawnableFarFromPlayer, ImmutableSet canSpawnInside, EntityDimensions dimensions, int maxTrackDistance, int trackTickInterval, FeatureSet requiredFeatures) {
      this.registryEntry = Registries.ENTITY_TYPE.createEntry(this);
      this.factory = factory;
      this.spawnGroup = spawnGroup;
      this.spawnableFarFromPlayer = spawnableFarFromPlayer;
      this.saveable = saveable;
      this.summonable = summonable;
      this.fireImmune = fireImmune;
      this.canSpawnInside = canSpawnInside;
      this.dimensions = dimensions;
      this.maxTrackDistance = maxTrackDistance;
      this.trackTickInterval = trackTickInterval;
      this.requiredFeatures = requiredFeatures;
   }

   @Nullable
   public Entity spawnFromItemStack(ServerWorld world, @Nullable ItemStack stack, @Nullable PlayerEntity player, BlockPos pos, SpawnReason spawnReason, boolean alignPosition, boolean invertY) {
      Consumer consumer;
      NbtCompound lv;
      if (stack != null) {
         lv = stack.getNbt();
         consumer = copier(world, stack, player);
      } else {
         consumer = (entity) -> {
         };
         lv = null;
      }

      return this.spawn(world, lv, consumer, pos, spawnReason, alignPosition, invertY);
   }

   public static Consumer copier(ServerWorld world, ItemStack stack, @Nullable PlayerEntity player) {
      return copier((entity) -> {
      }, world, stack, player);
   }

   public static Consumer copier(Consumer chained, ServerWorld world, ItemStack stack, @Nullable PlayerEntity player) {
      return nbtCopier(customNameCopier(chained, stack), world, stack, player);
   }

   public static Consumer customNameCopier(Consumer chained, ItemStack stack) {
      return stack.hasCustomName() ? chained.andThen((entity) -> {
         entity.setCustomName(stack.getName());
      }) : chained;
   }

   public static Consumer nbtCopier(Consumer chained, ServerWorld world, ItemStack stack, @Nullable PlayerEntity player) {
      NbtCompound lv = stack.getNbt();
      return lv != null ? chained.andThen((entity) -> {
         loadFromEntityNbt(world, player, entity, lv);
      }) : chained;
   }

   @Nullable
   public Entity spawn(ServerWorld world, BlockPos pos, SpawnReason reason) {
      return this.spawn(world, (NbtCompound)null, (Consumer)null, pos, reason, false, false);
   }

   @Nullable
   public Entity spawn(ServerWorld world, @Nullable NbtCompound itemNbt, @Nullable Consumer afterConsumer, BlockPos pos, SpawnReason reason, boolean alignPosition, boolean invertY) {
      Entity lv = this.create(world, itemNbt, afterConsumer, pos, reason, alignPosition, invertY);
      if (lv != null) {
         world.spawnEntityAndPassengers(lv);
      }

      return lv;
   }

   @Nullable
   public Entity create(ServerWorld world, @Nullable NbtCompound itemNbt, @Nullable Consumer afterConsumer, BlockPos pos, SpawnReason reason, boolean alignPosition, boolean invertY) {
      Entity lv = this.create(world);
      if (lv == null) {
         return null;
      } else {
         double d;
         if (alignPosition) {
            lv.setPosition((double)pos.getX() + 0.5, (double)(pos.getY() + 1), (double)pos.getZ() + 0.5);
            d = getOriginY(world, pos, invertY, lv.getBoundingBox());
         } else {
            d = 0.0;
         }

         lv.refreshPositionAndAngles((double)pos.getX() + 0.5, (double)pos.getY() + d, (double)pos.getZ() + 0.5, MathHelper.wrapDegrees(world.random.nextFloat() * 360.0F), 0.0F);
         if (lv instanceof MobEntity) {
            MobEntity lv2 = (MobEntity)lv;
            lv2.headYaw = lv2.getYaw();
            lv2.bodyYaw = lv2.getYaw();
            lv2.initialize(world, world.getLocalDifficulty(lv2.getBlockPos()), reason, (EntityData)null, itemNbt);
            lv2.playAmbientSound();
         }

         if (afterConsumer != null) {
            afterConsumer.accept(lv);
         }

         return lv;
      }
   }

   protected static double getOriginY(WorldView world, BlockPos pos, boolean invertY, Box boundingBox) {
      Box lv = new Box(pos);
      if (invertY) {
         lv = lv.stretch(0.0, -1.0, 0.0);
      }

      Iterable iterable = world.getCollisions((Entity)null, lv);
      return 1.0 + VoxelShapes.calculateMaxOffset(Direction.Axis.Y, boundingBox, iterable, invertY ? -2.0 : -1.0);
   }

   public static void loadFromEntityNbt(World world, @Nullable PlayerEntity player, @Nullable Entity entity, @Nullable NbtCompound itemNbt) {
      if (itemNbt != null && itemNbt.contains("EntityTag", NbtElement.COMPOUND_TYPE)) {
         MinecraftServer minecraftServer = world.getServer();
         if (minecraftServer != null && entity != null) {
            if (world.isClient || !entity.entityDataRequiresOperator() || player != null && minecraftServer.getPlayerManager().isOperator(player.getGameProfile())) {
               NbtCompound lv = entity.writeNbt(new NbtCompound());
               UUID uUID = entity.getUuid();
               lv.copyFrom(itemNbt.getCompound("EntityTag"));
               entity.setUuid(uUID);
               entity.readNbt(lv);
            }
         }
      }
   }

   public boolean isSaveable() {
      return this.saveable;
   }

   public boolean isSummonable() {
      return this.summonable;
   }

   public boolean isFireImmune() {
      return this.fireImmune;
   }

   public boolean isSpawnableFarFromPlayer() {
      return this.spawnableFarFromPlayer;
   }

   public SpawnGroup getSpawnGroup() {
      return this.spawnGroup;
   }

   public String getTranslationKey() {
      if (this.translationKey == null) {
         this.translationKey = Util.createTranslationKey("entity", Registries.ENTITY_TYPE.getId(this));
      }

      return this.translationKey;
   }

   public Text getName() {
      if (this.name == null) {
         this.name = Text.translatable(this.getTranslationKey());
      }

      return this.name;
   }

   public String toString() {
      return this.getTranslationKey();
   }

   public String getUntranslatedName() {
      int i = this.getTranslationKey().lastIndexOf(46);
      return i == -1 ? this.getTranslationKey() : this.getTranslationKey().substring(i + 1);
   }

   public Identifier getLootTableId() {
      if (this.lootTableId == null) {
         Identifier lv = Registries.ENTITY_TYPE.getId(this);
         this.lootTableId = lv.withPrefixedPath("entities/");
      }

      return this.lootTableId;
   }

   public float getWidth() {
      return this.dimensions.width;
   }

   public float getHeight() {
      return this.dimensions.height;
   }

   public FeatureSet getRequiredFeatures() {
      return this.requiredFeatures;
   }

   @Nullable
   public Entity create(World world) {
      return !this.isEnabled(world.getEnabledFeatures()) ? null : this.factory.create(this, world);
   }

   public static Optional getEntityFromNbt(NbtCompound nbt, World world) {
      return Util.ifPresentOrElse(fromNbt(nbt).map((entityType) -> {
         return entityType.create(world);
      }), (entity) -> {
         entity.readNbt(nbt);
      }, () -> {
         LOGGER.warn("Skipping Entity with id {}", nbt.getString("id"));
      });
   }

   public Box createSimpleBoundingBox(double feetX, double feetY, double feetZ) {
      float g = this.getWidth() / 2.0F;
      return new Box(feetX - (double)g, feetY, feetZ - (double)g, feetX + (double)g, feetY + (double)this.getHeight(), feetZ + (double)g);
   }

   public boolean isInvalidSpawn(BlockState state) {
      if (this.canSpawnInside.contains(state.getBlock())) {
         return false;
      } else if (!this.fireImmune && LandPathNodeMaker.inflictsFireDamage(state)) {
         return true;
      } else {
         return state.isOf(Blocks.WITHER_ROSE) || state.isOf(Blocks.SWEET_BERRY_BUSH) || state.isOf(Blocks.CACTUS) || state.isOf(Blocks.POWDER_SNOW);
      }
   }

   public EntityDimensions getDimensions() {
      return this.dimensions;
   }

   public static Optional fromNbt(NbtCompound nbt) {
      return Registries.ENTITY_TYPE.getOrEmpty(new Identifier(nbt.getString("id")));
   }

   @Nullable
   public static Entity loadEntityWithPassengers(NbtCompound nbt, World world, Function entityProcessor) {
      return (Entity)loadEntityFromNbt(nbt, world).map(entityProcessor).map((entity) -> {
         if (nbt.contains("Passengers", NbtElement.LIST_TYPE)) {
            NbtList lv = nbt.getList("Passengers", NbtElement.COMPOUND_TYPE);

            for(int i = 0; i < lv.size(); ++i) {
               Entity lv2 = loadEntityWithPassengers(lv.getCompound(i), world, entityProcessor);
               if (lv2 != null) {
                  lv2.startRiding(entity, true);
               }
            }
         }

         return entity;
      }).orElse((Object)null);
   }

   public static Stream streamFromNbt(final List entityNbtList, final World world) {
      final Spliterator spliterator = entityNbtList.spliterator();
      return StreamSupport.stream(new Spliterator() {
         public boolean tryAdvance(Consumer action) {
            return spliterator.tryAdvance((nbt) -> {
               EntityType.loadEntityWithPassengers((NbtCompound)nbt, world, (entity) -> {
                  action.accept(entity);
                  return entity;
               });
            });
         }

         public Spliterator trySplit() {
            return null;
         }

         public long estimateSize() {
            return (long)entityNbtList.size();
         }

         public int characteristics() {
            return Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.IMMUTABLE;
         }
      }, false);
   }

   private static Optional loadEntityFromNbt(NbtCompound nbt, World world) {
      try {
         return getEntityFromNbt(nbt, world);
      } catch (RuntimeException var3) {
         LOGGER.warn("Exception loading entity: ", var3);
         return Optional.empty();
      }
   }

   public int getMaxTrackDistance() {
      return this.maxTrackDistance;
   }

   public int getTrackTickInterval() {
      return this.trackTickInterval;
   }

   public boolean alwaysUpdateVelocity() {
      return this != PLAYER && this != LLAMA_SPIT && this != WITHER && this != BAT && this != ITEM_FRAME && this != GLOW_ITEM_FRAME && this != LEASH_KNOT && this != PAINTING && this != END_CRYSTAL && this != EVOKER_FANGS;
   }

   public boolean isIn(TagKey tag) {
      return this.registryEntry.isIn(tag);
   }

   @Nullable
   public Entity downcast(Entity arg) {
      return arg.getType() == this ? arg : null;
   }

   public Class getBaseClass() {
      return Entity.class;
   }

   /** @deprecated */
   @Deprecated
   public RegistryEntry.Reference getRegistryEntry() {
      return this.registryEntry;
   }

   static {
      ALLAY = register("allay", EntityType.Builder.create(AllayEntity::new, SpawnGroup.CREATURE).setDimensions(0.35F, 0.6F).maxTrackingRange(8).trackingTickInterval(2));
      AREA_EFFECT_CLOUD = register("area_effect_cloud", EntityType.Builder.create(AreaEffectCloudEntity::new, SpawnGroup.MISC).makeFireImmune().setDimensions(6.0F, 0.5F).maxTrackingRange(10).trackingTickInterval(Integer.MAX_VALUE));
      ARMOR_STAND = register("armor_stand", EntityType.Builder.create(ArmorStandEntity::new, SpawnGroup.MISC).setDimensions(0.5F, 1.975F).maxTrackingRange(10));
      ARROW = register("arrow", EntityType.Builder.create(ArrowEntity::new, SpawnGroup.MISC).setDimensions(0.5F, 0.5F).maxTrackingRange(4).trackingTickInterval(20));
      AXOLOTL = register("axolotl", EntityType.Builder.create(AxolotlEntity::new, SpawnGroup.AXOLOTLS).setDimensions(0.75F, 0.42F).maxTrackingRange(10));
      BAT = register("bat", EntityType.Builder.create(BatEntity::new, SpawnGroup.AMBIENT).setDimensions(0.5F, 0.9F).maxTrackingRange(5));
      BEE = register("bee", EntityType.Builder.create(BeeEntity::new, SpawnGroup.CREATURE).setDimensions(0.7F, 0.6F).maxTrackingRange(8));
      BLAZE = register("blaze", EntityType.Builder.create(BlazeEntity::new, SpawnGroup.MONSTER).makeFireImmune().setDimensions(0.6F, 1.8F).maxTrackingRange(8));
      BLOCK_DISPLAY = register("block_display", EntityType.Builder.create(DisplayEntity.BlockDisplayEntity::new, SpawnGroup.MISC).setDimensions(0.0F, 0.0F).maxTrackingRange(10).trackingTickInterval(1));
      BOAT = register("boat", EntityType.Builder.create(BoatEntity::new, SpawnGroup.MISC).setDimensions(1.375F, 0.5625F).maxTrackingRange(10));
      CAMEL = register("camel", EntityType.Builder.create(CamelEntity::new, SpawnGroup.CREATURE).setDimensions(1.7F, 2.375F).maxTrackingRange(10));
      CAT = register("cat", EntityType.Builder.create(CatEntity::new, SpawnGroup.CREATURE).setDimensions(0.6F, 0.7F).maxTrackingRange(8));
      CAVE_SPIDER = register("cave_spider", EntityType.Builder.create(CaveSpiderEntity::new, SpawnGroup.MONSTER).setDimensions(0.7F, 0.5F).maxTrackingRange(8));
      CHEST_BOAT = register("chest_boat", EntityType.Builder.create(ChestBoatEntity::new, SpawnGroup.MISC).setDimensions(1.375F, 0.5625F).maxTrackingRange(10));
      CHEST_MINECART = register("chest_minecart", EntityType.Builder.create(ChestMinecartEntity::new, SpawnGroup.MISC).setDimensions(0.98F, 0.7F).maxTrackingRange(8));
      CHICKEN = register("chicken", EntityType.Builder.create(ChickenEntity::new, SpawnGroup.CREATURE).setDimensions(0.4F, 0.7F).maxTrackingRange(10));
      COD = register("cod", EntityType.Builder.create(CodEntity::new, SpawnGroup.WATER_AMBIENT).setDimensions(0.5F, 0.3F).maxTrackingRange(4));
      COMMAND_BLOCK_MINECART = register("command_block_minecart", EntityType.Builder.create(CommandBlockMinecartEntity::new, SpawnGroup.MISC).setDimensions(0.98F, 0.7F).maxTrackingRange(8));
      COW = register("cow", EntityType.Builder.create(CowEntity::new, SpawnGroup.CREATURE).setDimensions(0.9F, 1.4F).maxTrackingRange(10));
      CREEPER = register("creeper", EntityType.Builder.create(CreeperEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.7F).maxTrackingRange(8));
      DOLPHIN = register("dolphin", EntityType.Builder.create(DolphinEntity::new, SpawnGroup.WATER_CREATURE).setDimensions(0.9F, 0.6F));
      DONKEY = register("donkey", EntityType.Builder.create(DonkeyEntity::new, SpawnGroup.CREATURE).setDimensions(1.3964844F, 1.5F).maxTrackingRange(10));
      DRAGON_FIREBALL = register("dragon_fireball", EntityType.Builder.create(DragonFireballEntity::new, SpawnGroup.MISC).setDimensions(1.0F, 1.0F).maxTrackingRange(4).trackingTickInterval(10));
      DROWNED = register("drowned", EntityType.Builder.create(DrownedEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      EGG = register("egg", EntityType.Builder.create(EggEntity::new, SpawnGroup.MISC).setDimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(10));
      ELDER_GUARDIAN = register("elder_guardian", EntityType.Builder.create(ElderGuardianEntity::new, SpawnGroup.MONSTER).setDimensions(1.9975F, 1.9975F).maxTrackingRange(10));
      END_CRYSTAL = register("end_crystal", EntityType.Builder.create(EndCrystalEntity::new, SpawnGroup.MISC).setDimensions(2.0F, 2.0F).maxTrackingRange(16).trackingTickInterval(Integer.MAX_VALUE));
      ENDER_DRAGON = register("ender_dragon", EntityType.Builder.create(EnderDragonEntity::new, SpawnGroup.MONSTER).makeFireImmune().setDimensions(16.0F, 8.0F).maxTrackingRange(10));
      ENDER_PEARL = register("ender_pearl", EntityType.Builder.create(EnderPearlEntity::new, SpawnGroup.MISC).setDimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(10));
      ENDERMAN = register("enderman", EntityType.Builder.create(EndermanEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 2.9F).maxTrackingRange(8));
      ENDERMITE = register("endermite", EntityType.Builder.create(EndermiteEntity::new, SpawnGroup.MONSTER).setDimensions(0.4F, 0.3F).maxTrackingRange(8));
      EVOKER = register("evoker", EntityType.Builder.create(EvokerEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      EVOKER_FANGS = register("evoker_fangs", EntityType.Builder.create(EvokerFangsEntity::new, SpawnGroup.MISC).setDimensions(0.5F, 0.8F).maxTrackingRange(6).trackingTickInterval(2));
      EXPERIENCE_BOTTLE = register("experience_bottle", EntityType.Builder.create(ExperienceBottleEntity::new, SpawnGroup.MISC).setDimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(10));
      EXPERIENCE_ORB = register("experience_orb", EntityType.Builder.create(ExperienceOrbEntity::new, SpawnGroup.MISC).setDimensions(0.5F, 0.5F).maxTrackingRange(6).trackingTickInterval(20));
      EYE_OF_ENDER = register("eye_of_ender", EntityType.Builder.create(EyeOfEnderEntity::new, SpawnGroup.MISC).setDimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(4));
      FALLING_BLOCK = register("falling_block", EntityType.Builder.create(FallingBlockEntity::new, SpawnGroup.MISC).setDimensions(0.98F, 0.98F).maxTrackingRange(10).trackingTickInterval(20));
      FIREWORK_ROCKET = register("firework_rocket", EntityType.Builder.create(FireworkRocketEntity::new, SpawnGroup.MISC).setDimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(10));
      FOX = register("fox", EntityType.Builder.create(FoxEntity::new, SpawnGroup.CREATURE).setDimensions(0.6F, 0.7F).maxTrackingRange(8).allowSpawningInside(Blocks.SWEET_BERRY_BUSH));
      FROG = register("frog", EntityType.Builder.create(FrogEntity::new, SpawnGroup.CREATURE).setDimensions(0.5F, 0.5F).maxTrackingRange(10));
      FURNACE_MINECART = register("furnace_minecart", EntityType.Builder.create(FurnaceMinecartEntity::new, SpawnGroup.MISC).setDimensions(0.98F, 0.7F).maxTrackingRange(8));
      GHAST = register("ghast", EntityType.Builder.create(GhastEntity::new, SpawnGroup.MONSTER).makeFireImmune().setDimensions(4.0F, 4.0F).maxTrackingRange(10));
      GIANT = register("giant", EntityType.Builder.create(GiantEntity::new, SpawnGroup.MONSTER).setDimensions(3.6F, 12.0F).maxTrackingRange(10));
      GLOW_ITEM_FRAME = register("glow_item_frame", EntityType.Builder.create(GlowItemFrameEntity::new, SpawnGroup.MISC).setDimensions(0.5F, 0.5F).maxTrackingRange(10).trackingTickInterval(Integer.MAX_VALUE));
      GLOW_SQUID = register("glow_squid", EntityType.Builder.create(GlowSquidEntity::new, SpawnGroup.UNDERGROUND_WATER_CREATURE).setDimensions(0.8F, 0.8F).maxTrackingRange(10));
      GOAT = register("goat", EntityType.Builder.create(GoatEntity::new, SpawnGroup.CREATURE).setDimensions(0.9F, 1.3F).maxTrackingRange(10));
      GUARDIAN = register("guardian", EntityType.Builder.create(GuardianEntity::new, SpawnGroup.MONSTER).setDimensions(0.85F, 0.85F).maxTrackingRange(8));
      HOGLIN = register("hoglin", EntityType.Builder.create(HoglinEntity::new, SpawnGroup.MONSTER).setDimensions(1.3964844F, 1.4F).maxTrackingRange(8));
      HOPPER_MINECART = register("hopper_minecart", EntityType.Builder.create(HopperMinecartEntity::new, SpawnGroup.MISC).setDimensions(0.98F, 0.7F).maxTrackingRange(8));
      HORSE = register("horse", EntityType.Builder.create(HorseEntity::new, SpawnGroup.CREATURE).setDimensions(1.3964844F, 1.6F).maxTrackingRange(10));
      HUSK = register("husk", EntityType.Builder.create(HuskEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      ILLUSIONER = register("illusioner", EntityType.Builder.create(IllusionerEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      INTERACTION = register("interaction", EntityType.Builder.create(InteractionEntity::new, SpawnGroup.MISC).setDimensions(0.0F, 0.0F).maxTrackingRange(10));
      IRON_GOLEM = register("iron_golem", EntityType.Builder.create(IronGolemEntity::new, SpawnGroup.MISC).setDimensions(1.4F, 2.7F).maxTrackingRange(10));
      ITEM = register("item", EntityType.Builder.create(ItemEntity::new, SpawnGroup.MISC).setDimensions(0.25F, 0.25F).maxTrackingRange(6).trackingTickInterval(20));
      ITEM_DISPLAY = register("item_display", EntityType.Builder.create(DisplayEntity.ItemDisplayEntity::new, SpawnGroup.MISC).setDimensions(0.0F, 0.0F).maxTrackingRange(10).trackingTickInterval(1));
      ITEM_FRAME = register("item_frame", EntityType.Builder.create(ItemFrameEntity::new, SpawnGroup.MISC).setDimensions(0.5F, 0.5F).maxTrackingRange(10).trackingTickInterval(Integer.MAX_VALUE));
      FIREBALL = register("fireball", EntityType.Builder.create(FireballEntity::new, SpawnGroup.MISC).setDimensions(1.0F, 1.0F).maxTrackingRange(4).trackingTickInterval(10));
      LEASH_KNOT = register("leash_knot", EntityType.Builder.create(LeashKnotEntity::new, SpawnGroup.MISC).disableSaving().setDimensions(0.375F, 0.5F).maxTrackingRange(10).trackingTickInterval(Integer.MAX_VALUE));
      LIGHTNING_BOLT = register("lightning_bolt", EntityType.Builder.create(LightningEntity::new, SpawnGroup.MISC).disableSaving().setDimensions(0.0F, 0.0F).maxTrackingRange(16).trackingTickInterval(Integer.MAX_VALUE));
      LLAMA = register("llama", EntityType.Builder.create(LlamaEntity::new, SpawnGroup.CREATURE).setDimensions(0.9F, 1.87F).maxTrackingRange(10));
      LLAMA_SPIT = register("llama_spit", EntityType.Builder.create(LlamaSpitEntity::new, SpawnGroup.MISC).setDimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(10));
      MAGMA_CUBE = register("magma_cube", EntityType.Builder.create(MagmaCubeEntity::new, SpawnGroup.MONSTER).makeFireImmune().setDimensions(2.04F, 2.04F).maxTrackingRange(8));
      MARKER = register("marker", EntityType.Builder.create(MarkerEntity::new, SpawnGroup.MISC).setDimensions(0.0F, 0.0F).maxTrackingRange(0));
      MINECART = register("minecart", EntityType.Builder.create(MinecartEntity::new, SpawnGroup.MISC).setDimensions(0.98F, 0.7F).maxTrackingRange(8));
      MOOSHROOM = register("mooshroom", EntityType.Builder.create(MooshroomEntity::new, SpawnGroup.CREATURE).setDimensions(0.9F, 1.4F).maxTrackingRange(10));
      MULE = register("mule", EntityType.Builder.create(MuleEntity::new, SpawnGroup.CREATURE).setDimensions(1.3964844F, 1.6F).maxTrackingRange(8));
      OCELOT = register("ocelot", EntityType.Builder.create(OcelotEntity::new, SpawnGroup.CREATURE).setDimensions(0.6F, 0.7F).maxTrackingRange(10));
      PAINTING = register("painting", EntityType.Builder.create(PaintingEntity::new, SpawnGroup.MISC).setDimensions(0.5F, 0.5F).maxTrackingRange(10).trackingTickInterval(Integer.MAX_VALUE));
      PANDA = register("panda", EntityType.Builder.create(PandaEntity::new, SpawnGroup.CREATURE).setDimensions(1.3F, 1.25F).maxTrackingRange(10));
      PARROT = register("parrot", EntityType.Builder.create(ParrotEntity::new, SpawnGroup.CREATURE).setDimensions(0.5F, 0.9F).maxTrackingRange(8));
      PHANTOM = register("phantom", EntityType.Builder.create(PhantomEntity::new, SpawnGroup.MONSTER).setDimensions(0.9F, 0.5F).maxTrackingRange(8));
      PIG = register("pig", EntityType.Builder.create(PigEntity::new, SpawnGroup.CREATURE).setDimensions(0.9F, 0.9F).maxTrackingRange(10));
      PIGLIN = register("piglin", EntityType.Builder.create(PiglinEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      PIGLIN_BRUTE = register("piglin_brute", EntityType.Builder.create(PiglinBruteEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      PILLAGER = register("pillager", EntityType.Builder.create(PillagerEntity::new, SpawnGroup.MONSTER).spawnableFarFromPlayer().setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      POLAR_BEAR = register("polar_bear", EntityType.Builder.create(PolarBearEntity::new, SpawnGroup.CREATURE).allowSpawningInside(Blocks.POWDER_SNOW).setDimensions(1.4F, 1.4F).maxTrackingRange(10));
      POTION = register("potion", EntityType.Builder.create(PotionEntity::new, SpawnGroup.MISC).setDimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(10));
      PUFFERFISH = register("pufferfish", EntityType.Builder.create(PufferfishEntity::new, SpawnGroup.WATER_AMBIENT).setDimensions(0.7F, 0.7F).maxTrackingRange(4));
      RABBIT = register("rabbit", EntityType.Builder.create(RabbitEntity::new, SpawnGroup.CREATURE).setDimensions(0.4F, 0.5F).maxTrackingRange(8));
      RAVAGER = register("ravager", EntityType.Builder.create(RavagerEntity::new, SpawnGroup.MONSTER).setDimensions(1.95F, 2.2F).maxTrackingRange(10));
      SALMON = register("salmon", EntityType.Builder.create(SalmonEntity::new, SpawnGroup.WATER_AMBIENT).setDimensions(0.7F, 0.4F).maxTrackingRange(4));
      SHEEP = register("sheep", EntityType.Builder.create(SheepEntity::new, SpawnGroup.CREATURE).setDimensions(0.9F, 1.3F).maxTrackingRange(10));
      SHULKER = register("shulker", EntityType.Builder.create(ShulkerEntity::new, SpawnGroup.MONSTER).makeFireImmune().spawnableFarFromPlayer().setDimensions(1.0F, 1.0F).maxTrackingRange(10));
      SHULKER_BULLET = register("shulker_bullet", EntityType.Builder.create(ShulkerBulletEntity::new, SpawnGroup.MISC).setDimensions(0.3125F, 0.3125F).maxTrackingRange(8));
      SILVERFISH = register("silverfish", EntityType.Builder.create(SilverfishEntity::new, SpawnGroup.MONSTER).setDimensions(0.4F, 0.3F).maxTrackingRange(8));
      SKELETON = register("skeleton", EntityType.Builder.create(SkeletonEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.99F).maxTrackingRange(8));
      SKELETON_HORSE = register("skeleton_horse", EntityType.Builder.create(SkeletonHorseEntity::new, SpawnGroup.CREATURE).setDimensions(1.3964844F, 1.6F).maxTrackingRange(10));
      SLIME = register("slime", EntityType.Builder.create(SlimeEntity::new, SpawnGroup.MONSTER).setDimensions(2.04F, 2.04F).maxTrackingRange(10));
      SMALL_FIREBALL = register("small_fireball", EntityType.Builder.create(SmallFireballEntity::new, SpawnGroup.MISC).setDimensions(0.3125F, 0.3125F).maxTrackingRange(4).trackingTickInterval(10));
      SNIFFER = register("sniffer", EntityType.Builder.create(SnifferEntity::new, SpawnGroup.CREATURE).setDimensions(1.9F, 1.75F).maxTrackingRange(10));
      SNOW_GOLEM = register("snow_golem", EntityType.Builder.create(SnowGolemEntity::new, SpawnGroup.MISC).allowSpawningInside(Blocks.POWDER_SNOW).setDimensions(0.7F, 1.9F).maxTrackingRange(8));
      SNOWBALL = register("snowball", EntityType.Builder.create(SnowballEntity::new, SpawnGroup.MISC).setDimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(10));
      SPAWNER_MINECART = register("spawner_minecart", EntityType.Builder.create(SpawnerMinecartEntity::new, SpawnGroup.MISC).setDimensions(0.98F, 0.7F).maxTrackingRange(8));
      SPECTRAL_ARROW = register("spectral_arrow", EntityType.Builder.create(SpectralArrowEntity::new, SpawnGroup.MISC).setDimensions(0.5F, 0.5F).maxTrackingRange(4).trackingTickInterval(20));
      SPIDER = register("spider", EntityType.Builder.create(SpiderEntity::new, SpawnGroup.MONSTER).setDimensions(1.4F, 0.9F).maxTrackingRange(8));
      SQUID = register("squid", EntityType.Builder.create(SquidEntity::new, SpawnGroup.WATER_CREATURE).setDimensions(0.8F, 0.8F).maxTrackingRange(8));
      STRAY = register("stray", EntityType.Builder.create(StrayEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.99F).allowSpawningInside(Blocks.POWDER_SNOW).maxTrackingRange(8));
      STRIDER = register("strider", EntityType.Builder.create(StriderEntity::new, SpawnGroup.CREATURE).makeFireImmune().setDimensions(0.9F, 1.7F).maxTrackingRange(10));
      TADPOLE = register("tadpole", EntityType.Builder.create(TadpoleEntity::new, SpawnGroup.CREATURE).setDimensions(TadpoleEntity.WIDTH, TadpoleEntity.HEIGHT).maxTrackingRange(10));
      TEXT_DISPLAY = register("text_display", EntityType.Builder.create(DisplayEntity.TextDisplayEntity::new, SpawnGroup.MISC).setDimensions(0.0F, 0.0F).maxTrackingRange(10).trackingTickInterval(1));
      TNT = register("tnt", EntityType.Builder.create(TntEntity::new, SpawnGroup.MISC).makeFireImmune().setDimensions(0.98F, 0.98F).maxTrackingRange(10).trackingTickInterval(10));
      TNT_MINECART = register("tnt_minecart", EntityType.Builder.create(TntMinecartEntity::new, SpawnGroup.MISC).setDimensions(0.98F, 0.7F).maxTrackingRange(8));
      TRADER_LLAMA = register("trader_llama", EntityType.Builder.create(TraderLlamaEntity::new, SpawnGroup.CREATURE).setDimensions(0.9F, 1.87F).maxTrackingRange(10));
      TRIDENT = register("trident", EntityType.Builder.create(TridentEntity::new, SpawnGroup.MISC).setDimensions(0.5F, 0.5F).maxTrackingRange(4).trackingTickInterval(20));
      TROPICAL_FISH = register("tropical_fish", EntityType.Builder.create(TropicalFishEntity::new, SpawnGroup.WATER_AMBIENT).setDimensions(0.5F, 0.4F).maxTrackingRange(4));
      TURTLE = register("turtle", EntityType.Builder.create(TurtleEntity::new, SpawnGroup.CREATURE).setDimensions(1.2F, 0.4F).maxTrackingRange(10));
      VEX = register("vex", EntityType.Builder.create(VexEntity::new, SpawnGroup.MONSTER).makeFireImmune().setDimensions(0.4F, 0.8F).maxTrackingRange(8));
      VILLAGER = register("villager", EntityType.Builder.create(VillagerEntity::new, SpawnGroup.MISC).setDimensions(0.6F, 1.95F).maxTrackingRange(10));
      VINDICATOR = register("vindicator", EntityType.Builder.create(VindicatorEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      WANDERING_TRADER = register("wandering_trader", EntityType.Builder.create(WanderingTraderEntity::new, SpawnGroup.CREATURE).setDimensions(0.6F, 1.95F).maxTrackingRange(10));
      WARDEN = register("warden", EntityType.Builder.create(WardenEntity::new, SpawnGroup.MONSTER).setDimensions(0.9F, 2.9F).maxTrackingRange(16).makeFireImmune());
      WITCH = register("witch", EntityType.Builder.create(WitchEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      WITHER = register("wither", EntityType.Builder.create(WitherEntity::new, SpawnGroup.MONSTER).makeFireImmune().allowSpawningInside(Blocks.WITHER_ROSE).setDimensions(0.9F, 3.5F).maxTrackingRange(10));
      WITHER_SKELETON = register("wither_skeleton", EntityType.Builder.create(WitherSkeletonEntity::new, SpawnGroup.MONSTER).makeFireImmune().allowSpawningInside(Blocks.WITHER_ROSE).setDimensions(0.7F, 2.4F).maxTrackingRange(8));
      WITHER_SKULL = register("wither_skull", EntityType.Builder.create(WitherSkullEntity::new, SpawnGroup.MISC).setDimensions(0.3125F, 0.3125F).maxTrackingRange(4).trackingTickInterval(10));
      WOLF = register("wolf", EntityType.Builder.create(WolfEntity::new, SpawnGroup.CREATURE).setDimensions(0.6F, 0.85F).maxTrackingRange(10));
      ZOGLIN = register("zoglin", EntityType.Builder.create(ZoglinEntity::new, SpawnGroup.MONSTER).makeFireImmune().setDimensions(1.3964844F, 1.4F).maxTrackingRange(8));
      ZOMBIE = register("zombie", EntityType.Builder.create(ZombieEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      ZOMBIE_HORSE = register("zombie_horse", EntityType.Builder.create(ZombieHorseEntity::new, SpawnGroup.CREATURE).setDimensions(1.3964844F, 1.6F).maxTrackingRange(10));
      ZOMBIE_VILLAGER = register("zombie_villager", EntityType.Builder.create(ZombieVillagerEntity::new, SpawnGroup.MONSTER).setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      ZOMBIFIED_PIGLIN = register("zombified_piglin", EntityType.Builder.create(ZombifiedPiglinEntity::new, SpawnGroup.MONSTER).makeFireImmune().setDimensions(0.6F, 1.95F).maxTrackingRange(8));
      PLAYER = register("player", EntityType.Builder.create(SpawnGroup.MISC).disableSaving().disableSummon().setDimensions(0.6F, 1.8F).maxTrackingRange(32).trackingTickInterval(2));
      FISHING_BOBBER = register("fishing_bobber", EntityType.Builder.create(FishingBobberEntity::new, SpawnGroup.MISC).disableSaving().disableSummon().setDimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(5));
   }

   public static class Builder {
      private final EntityFactory factory;
      private final SpawnGroup spawnGroup;
      private ImmutableSet canSpawnInside = ImmutableSet.of();
      private boolean saveable = true;
      private boolean summonable = true;
      private boolean fireImmune;
      private boolean spawnableFarFromPlayer;
      private int maxTrackingRange = 5;
      private int trackingTickInterval = 3;
      private EntityDimensions dimensions = EntityDimensions.changing(0.6F, 1.8F);
      private FeatureSet requiredFeatures;

      private Builder(EntityFactory factory, SpawnGroup spawnGroup) {
         this.requiredFeatures = FeatureFlags.VANILLA_FEATURES;
         this.factory = factory;
         this.spawnGroup = spawnGroup;
         this.spawnableFarFromPlayer = spawnGroup == SpawnGroup.CREATURE || spawnGroup == SpawnGroup.MISC;
      }

      public static Builder create(EntityFactory factory, SpawnGroup spawnGroup) {
         return new Builder(factory, spawnGroup);
      }

      public static Builder create(SpawnGroup spawnGroup) {
         return new Builder((type, world) -> {
            return null;
         }, spawnGroup);
      }

      public Builder setDimensions(float width, float height) {
         this.dimensions = EntityDimensions.changing(width, height);
         return this;
      }

      public Builder disableSummon() {
         this.summonable = false;
         return this;
      }

      public Builder disableSaving() {
         this.saveable = false;
         return this;
      }

      public Builder makeFireImmune() {
         this.fireImmune = true;
         return this;
      }

      public Builder allowSpawningInside(Block... blocks) {
         this.canSpawnInside = ImmutableSet.copyOf(blocks);
         return this;
      }

      public Builder spawnableFarFromPlayer() {
         this.spawnableFarFromPlayer = true;
         return this;
      }

      public Builder maxTrackingRange(int maxTrackingRange) {
         this.maxTrackingRange = maxTrackingRange;
         return this;
      }

      public Builder trackingTickInterval(int trackingTickInterval) {
         this.trackingTickInterval = trackingTickInterval;
         return this;
      }

      public Builder requires(FeatureFlag... features) {
         this.requiredFeatures = FeatureFlags.FEATURE_MANAGER.featureSetOf(features);
         return this;
      }

      public EntityType build(String id) {
         if (this.saveable) {
            Util.getChoiceType(TypeReferences.ENTITY_TREE, id);
         }

         return new EntityType(this.factory, this.spawnGroup, this.saveable, this.summonable, this.fireImmune, this.spawnableFarFromPlayer, this.canSpawnInside, this.dimensions, this.maxTrackingRange, this.trackingTickInterval, this.requiredFeatures);
      }
   }

   public interface EntityFactory {
      Entity create(EntityType type, World world);
   }
}
