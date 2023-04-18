package net.minecraft.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.HoneyBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Nameable;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.NetherPortal;
import net.minecraft.world.entity.EntityChangeListener;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class Entity implements Nameable, EntityLike, CommandOutput {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String ID_KEY = "id";
   public static final String PASSENGERS_KEY = "Passengers";
   private static final AtomicInteger CURRENT_ID = new AtomicInteger();
   private static final List EMPTY_STACK_LIST = Collections.emptyList();
   public static final int MAX_RIDING_COOLDOWN = 60;
   public static final int DEFAULT_PORTAL_COOLDOWN = 300;
   public static final int MAX_COMMAND_TAGS = 1024;
   public static final double VELOCITY_AFFECTING_POS_Y_OFFSET = 0.5000001;
   public static final float field_29991 = 0.11111111F;
   public static final int DEFAULT_MIN_FREEZE_DAMAGE_TICKS = 140;
   public static final int FREEZING_DAMAGE_INTERVAL = 40;
   private static final Box NULL_BOX = new Box(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
   private static final double SPEED_IN_WATER = 0.014;
   private static final double SPEED_IN_LAVA_IN_NETHER = 0.007;
   private static final double SPEED_IN_LAVA = 0.0023333333333333335;
   public static final String UUID_KEY = "UUID";
   private static double renderDistanceMultiplier = 1.0;
   private final EntityType type;
   private int id;
   public boolean intersectionChecked;
   private ImmutableList passengerList;
   protected int ridingCooldown;
   @Nullable
   private Entity vehicle;
   public World world;
   public double prevX;
   public double prevY;
   public double prevZ;
   private Vec3d pos;
   private BlockPos blockPos;
   private ChunkPos chunkPos;
   private Vec3d velocity;
   private float yaw;
   private float pitch;
   public float prevYaw;
   public float prevPitch;
   private Box boundingBox;
   protected boolean onGround;
   public boolean horizontalCollision;
   public boolean verticalCollision;
   public boolean field_36331;
   public boolean collidedSoftly;
   public boolean velocityModified;
   protected Vec3d movementMultiplier;
   @Nullable
   private RemovalReason removalReason;
   public static final float DEFAULT_FRICTION = 0.6F;
   public static final float MIN_RISING_BUBBLE_COLUMN_SPEED = 1.8F;
   public float prevHorizontalSpeed;
   public float horizontalSpeed;
   public float distanceTraveled;
   public float speed;
   public float fallDistance;
   private float nextStepSoundDistance;
   public double lastRenderX;
   public double lastRenderY;
   public double lastRenderZ;
   private float stepHeight;
   public boolean noClip;
   protected final Random random;
   public int age;
   private int fireTicks;
   protected boolean touchingWater;
   protected Object2DoubleMap fluidHeight;
   protected boolean submergedInWater;
   private final Set submergedFluidTag;
   public int timeUntilRegen;
   protected boolean firstUpdate;
   protected final DataTracker dataTracker;
   protected static final TrackedData FLAGS;
   protected static final int ON_FIRE_FLAG_INDEX = 0;
   private static final int SNEAKING_FLAG_INDEX = 1;
   private static final int SPRINTING_FLAG_INDEX = 3;
   private static final int SWIMMING_FLAG_INDEX = 4;
   private static final int INVISIBLE_FLAG_INDEX = 5;
   protected static final int GLOWING_FLAG_INDEX = 6;
   protected static final int FALL_FLYING_FLAG_INDEX = 7;
   private static final TrackedData AIR;
   private static final TrackedData CUSTOM_NAME;
   private static final TrackedData NAME_VISIBLE;
   private static final TrackedData SILENT;
   private static final TrackedData NO_GRAVITY;
   protected static final TrackedData POSE;
   private static final TrackedData FROZEN_TICKS;
   private EntityChangeListener changeListener;
   private final TrackedPosition trackedPosition;
   public boolean ignoreCameraFrustum;
   public boolean velocityDirty;
   private int portalCooldown;
   protected boolean inNetherPortal;
   protected int netherPortalTime;
   protected BlockPos lastNetherPortalPosition;
   private boolean invulnerable;
   protected UUID uuid;
   protected String uuidString;
   private boolean glowing;
   private final Set commandTags;
   private final double[] pistonMovementDelta;
   private long pistonMovementTick;
   private EntityDimensions dimensions;
   private float standingEyeHeight;
   public boolean inPowderSnow;
   public boolean wasInPowderSnow;
   public boolean wasOnFire;
   private float lastChimeIntensity;
   private int lastChimeAge;
   private boolean hasVisualFire;
   @Nullable
   private BlockState blockStateAtPos;

   public Entity(EntityType type, World world) {
      this.id = CURRENT_ID.incrementAndGet();
      this.passengerList = ImmutableList.of();
      this.velocity = Vec3d.ZERO;
      this.boundingBox = NULL_BOX;
      this.movementMultiplier = Vec3d.ZERO;
      this.nextStepSoundDistance = 1.0F;
      this.random = Random.create();
      this.fireTicks = -this.getBurningDuration();
      this.fluidHeight = new Object2DoubleArrayMap(2);
      this.submergedFluidTag = new HashSet();
      this.firstUpdate = true;
      this.changeListener = EntityChangeListener.NONE;
      this.trackedPosition = new TrackedPosition();
      this.uuid = MathHelper.randomUuid(this.random);
      this.uuidString = this.uuid.toString();
      this.commandTags = Sets.newHashSet();
      this.pistonMovementDelta = new double[]{0.0, 0.0, 0.0};
      this.blockStateAtPos = null;
      this.type = type;
      this.world = world;
      this.dimensions = type.getDimensions();
      this.pos = Vec3d.ZERO;
      this.blockPos = BlockPos.ORIGIN;
      this.chunkPos = ChunkPos.ORIGIN;
      this.dataTracker = new DataTracker(this);
      this.dataTracker.startTracking(FLAGS, (byte)0);
      this.dataTracker.startTracking(AIR, this.getMaxAir());
      this.dataTracker.startTracking(NAME_VISIBLE, false);
      this.dataTracker.startTracking(CUSTOM_NAME, Optional.empty());
      this.dataTracker.startTracking(SILENT, false);
      this.dataTracker.startTracking(NO_GRAVITY, false);
      this.dataTracker.startTracking(POSE, EntityPose.STANDING);
      this.dataTracker.startTracking(FROZEN_TICKS, 0);
      this.initDataTracker();
      this.setPosition(0.0, 0.0, 0.0);
      this.standingEyeHeight = this.getEyeHeight(EntityPose.STANDING, this.dimensions);
   }

   public boolean collidesWithStateAtPos(BlockPos pos, BlockState state) {
      VoxelShape lv = state.getCollisionShape(this.world, pos, ShapeContext.of(this));
      VoxelShape lv2 = lv.offset((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
      return VoxelShapes.matchesAnywhere(lv2, VoxelShapes.cuboid(this.getBoundingBox()), BooleanBiFunction.AND);
   }

   public int getTeamColorValue() {
      AbstractTeam lv = this.getScoreboardTeam();
      return lv != null && lv.getColor().getColorValue() != null ? lv.getColor().getColorValue() : 16777215;
   }

   public boolean isSpectator() {
      return false;
   }

   public final void detach() {
      if (this.hasPassengers()) {
         this.removeAllPassengers();
      }

      if (this.hasVehicle()) {
         this.stopRiding();
      }

   }

   public void updateTrackedPosition(double x, double y, double z) {
      this.trackedPosition.setPos(new Vec3d(x, y, z));
   }

   public TrackedPosition getTrackedPosition() {
      return this.trackedPosition;
   }

   public EntityType getType() {
      return this.type;
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public Set getCommandTags() {
      return this.commandTags;
   }

   public boolean addCommandTag(String tag) {
      return this.commandTags.size() >= 1024 ? false : this.commandTags.add(tag);
   }

   public boolean removeScoreboardTag(String tag) {
      return this.commandTags.remove(tag);
   }

   public void kill() {
      this.remove(Entity.RemovalReason.KILLED);
      this.emitGameEvent(GameEvent.ENTITY_DIE);
   }

   public final void discard() {
      this.remove(Entity.RemovalReason.DISCARDED);
   }

   protected abstract void initDataTracker();

   public DataTracker getDataTracker() {
      return this.dataTracker;
   }

   public boolean equals(Object o) {
      if (o instanceof Entity) {
         return ((Entity)o).id == this.id;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.id;
   }

   public void remove(RemovalReason reason) {
      this.setRemoved(reason);
   }

   public void onRemoved() {
   }

   public void setPose(EntityPose pose) {
      this.dataTracker.set(POSE, pose);
   }

   public EntityPose getPose() {
      return (EntityPose)this.dataTracker.get(POSE);
   }

   public boolean isInPose(EntityPose pose) {
      return this.getPose() == pose;
   }

   public boolean isInRange(Entity entity, double radius) {
      return this.getPos().isInRange(entity.getPos(), radius);
   }

   public boolean isInRange(Entity entity, double horizontalRadius, double verticalRadius) {
      double f = entity.getX() - this.getX();
      double g = entity.getY() - this.getY();
      double h = entity.getZ() - this.getZ();
      return MathHelper.squaredHypot(f, h) < MathHelper.square(horizontalRadius) && MathHelper.square(g) < MathHelper.square(verticalRadius);
   }

   protected void setRotation(float yaw, float pitch) {
      this.setYaw(yaw % 360.0F);
      this.setPitch(pitch % 360.0F);
   }

   public final void setPosition(Vec3d pos) {
      this.setPosition(pos.getX(), pos.getY(), pos.getZ());
   }

   public void setPosition(double x, double y, double z) {
      this.setPos(x, y, z);
      this.setBoundingBox(this.calculateBoundingBox());
   }

   protected Box calculateBoundingBox() {
      return this.dimensions.getBoxAt(this.pos);
   }

   protected void refreshPosition() {
      this.setPosition(this.pos.x, this.pos.y, this.pos.z);
   }

   public void changeLookDirection(double cursorDeltaX, double cursorDeltaY) {
      float f = (float)cursorDeltaY * 0.15F;
      float g = (float)cursorDeltaX * 0.15F;
      this.setPitch(this.getPitch() + f);
      this.setYaw(this.getYaw() + g);
      this.setPitch(MathHelper.clamp(this.getPitch(), -90.0F, 90.0F));
      this.prevPitch += f;
      this.prevYaw += g;
      this.prevPitch = MathHelper.clamp(this.prevPitch, -90.0F, 90.0F);
      if (this.vehicle != null) {
         this.vehicle.onPassengerLookAround(this);
      }

   }

   public void tick() {
      this.baseTick();
   }

   public void baseTick() {
      this.world.getProfiler().push("entityBaseTick");
      this.blockStateAtPos = null;
      if (this.hasVehicle() && this.getVehicle().isRemoved()) {
         this.stopRiding();
      }

      if (this.ridingCooldown > 0) {
         --this.ridingCooldown;
      }

      this.prevHorizontalSpeed = this.horizontalSpeed;
      this.prevPitch = this.getPitch();
      this.prevYaw = this.getYaw();
      this.tickPortal();
      if (this.shouldSpawnSprintingParticles()) {
         this.spawnSprintingParticles();
      }

      this.wasInPowderSnow = this.inPowderSnow;
      this.inPowderSnow = false;
      this.updateWaterState();
      this.updateSubmergedInWaterState();
      this.updateSwimming();
      if (this.world.isClient) {
         this.extinguish();
      } else if (this.fireTicks > 0) {
         if (this.isFireImmune()) {
            this.setFireTicks(this.fireTicks - 4);
            if (this.fireTicks < 0) {
               this.extinguish();
            }
         } else {
            if (this.fireTicks % 20 == 0 && !this.isInLava()) {
               this.damage(this.getDamageSources().onFire(), 1.0F);
            }

            this.setFireTicks(this.fireTicks - 1);
         }

         if (this.getFrozenTicks() > 0) {
            this.setFrozenTicks(0);
            this.world.syncWorldEvent((PlayerEntity)null, WorldEvents.FIRE_EXTINGUISHED, this.blockPos, 1);
         }
      }

      if (this.isInLava()) {
         this.setOnFireFromLava();
         this.fallDistance *= 0.5F;
      }

      this.attemptTickInVoid();
      if (!this.world.isClient) {
         this.setOnFire(this.fireTicks > 0);
      }

      this.firstUpdate = false;
      this.world.getProfiler().pop();
   }

   public void setOnFire(boolean onFire) {
      this.setFlag(ON_FIRE_FLAG_INDEX, onFire || this.hasVisualFire);
   }

   public void attemptTickInVoid() {
      if (this.getY() < (double)(this.world.getBottomY() - 64)) {
         this.tickInVoid();
      }

   }

   public void resetPortalCooldown() {
      this.portalCooldown = this.getDefaultPortalCooldown();
   }

   public boolean hasPortalCooldown() {
      return this.portalCooldown > 0;
   }

   protected void tickPortalCooldown() {
      if (this.hasPortalCooldown()) {
         --this.portalCooldown;
      }

   }

   public int getMaxNetherPortalTime() {
      return 0;
   }

   public void setOnFireFromLava() {
      if (!this.isFireImmune()) {
         this.setOnFireFor(15);
         if (this.damage(this.getDamageSources().lava(), 4.0F)) {
            this.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
         }

      }
   }

   public void setOnFireFor(int seconds) {
      int j = seconds * 20;
      if (this instanceof LivingEntity) {
         j = ProtectionEnchantment.transformFireDuration((LivingEntity)this, j);
      }

      if (this.fireTicks < j) {
         this.setFireTicks(j);
      }

   }

   public void setFireTicks(int fireTicks) {
      this.fireTicks = fireTicks;
   }

   public int getFireTicks() {
      return this.fireTicks;
   }

   public void extinguish() {
      this.setFireTicks(0);
   }

   protected void tickInVoid() {
      this.discard();
   }

   public boolean doesNotCollide(double offsetX, double offsetY, double offsetZ) {
      return this.doesNotCollide(this.getBoundingBox().offset(offsetX, offsetY, offsetZ));
   }

   private boolean doesNotCollide(Box box) {
      return this.world.isSpaceEmpty(this, box) && !this.world.containsFluid(box);
   }

   public void setOnGround(boolean onGround) {
      this.onGround = onGround;
   }

   public boolean isOnGround() {
      return this.onGround;
   }

   public void move(MovementType movementType, Vec3d movement) {
      if (this.noClip) {
         this.setPosition(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);
      } else {
         this.wasOnFire = this.isOnFire();
         if (movementType == MovementType.PISTON) {
            movement = this.adjustMovementForPiston(movement);
            if (movement.equals(Vec3d.ZERO)) {
               return;
            }
         }

         this.world.getProfiler().push("move");
         if (this.movementMultiplier.lengthSquared() > 1.0E-7) {
            movement = movement.multiply(this.movementMultiplier);
            this.movementMultiplier = Vec3d.ZERO;
            this.setVelocity(Vec3d.ZERO);
         }

         movement = this.adjustMovementForSneaking(movement, movementType);
         Vec3d lv = this.adjustMovementForCollisions(movement);
         double d = lv.lengthSquared();
         if (d > 1.0E-7) {
            if (this.fallDistance != 0.0F && d >= 1.0) {
               BlockHitResult lv2 = this.world.raycast(new RaycastContext(this.getPos(), this.getPos().add(lv), RaycastContext.ShapeType.FALLDAMAGE_RESETTING, RaycastContext.FluidHandling.WATER, this));
               if (lv2.getType() != HitResult.Type.MISS) {
                  this.onLanding();
               }
            }

            this.setPosition(this.getX() + lv.x, this.getY() + lv.y, this.getZ() + lv.z);
         }

         this.world.getProfiler().pop();
         this.world.getProfiler().push("rest");
         boolean bl = !MathHelper.approximatelyEquals(movement.x, lv.x);
         boolean bl2 = !MathHelper.approximatelyEquals(movement.z, lv.z);
         this.horizontalCollision = bl || bl2;
         this.verticalCollision = movement.y != lv.y;
         this.field_36331 = this.verticalCollision && movement.y < 0.0;
         if (this.horizontalCollision) {
            this.collidedSoftly = this.hasCollidedSoftly(lv);
         } else {
            this.collidedSoftly = false;
         }

         this.onGround = this.verticalCollision && movement.y < 0.0;
         BlockPos lv3 = this.getLandingPos();
         BlockState lv4 = this.world.getBlockState(lv3);
         this.fall(lv.y, this.onGround, lv4, lv3);
         if (this.isRemoved()) {
            this.world.getProfiler().pop();
         } else {
            if (this.horizontalCollision) {
               Vec3d lv5 = this.getVelocity();
               this.setVelocity(bl ? 0.0 : lv5.x, lv5.y, bl2 ? 0.0 : lv5.z);
            }

            Block lv6 = lv4.getBlock();
            if (movement.y != lv.y) {
               lv6.onEntityLand(this.world, this);
            }

            if (this.onGround) {
               lv6.onSteppedOn(this.world, lv3, lv4, this);
            }

            MoveEffect lv7 = this.getMoveEffect();
            if (lv7.hasAny() && !this.hasVehicle()) {
               double e = lv.x;
               double f = lv.y;
               double g = lv.z;
               this.speed += (float)(lv.length() * 0.6);
               boolean bl3 = lv4.isIn(BlockTags.CLIMBABLE) || lv4.isOf(Blocks.POWDER_SNOW);
               if (!bl3) {
                  f = 0.0;
               }

               this.horizontalSpeed += (float)lv.horizontalLength() * 0.6F;
               this.distanceTraveled += (float)Math.sqrt(e * e + f * f + g * g) * 0.6F;
               if (this.distanceTraveled > this.nextStepSoundDistance && !lv4.isAir()) {
                  if (this.onGround || bl3 || this.isInSneakingPose() && movement.y == 0.0) {
                     this.nextStepSoundDistance = this.calculateNextStepSoundDistance();
                     if (lv7.playsSounds()) {
                        this.playStepSounds(lv3, lv4);
                     }

                     if (lv7.emitsGameEvents()) {
                        this.world.emitGameEvent(GameEvent.STEP, this.pos, GameEvent.Emitter.of(this, this.getSteppingBlockState()));
                     }
                  } else if (this.isTouchingWater()) {
                     this.nextStepSoundDistance = this.calculateNextStepSoundDistance();
                     if (lv7.playsSounds()) {
                        Entity lv8 = this.hasPassengers() && this.getControllingPassenger() != null ? this.getControllingPassenger() : this;
                        float h = lv8 == this ? 0.35F : 0.4F;
                        Vec3d lv9 = ((Entity)lv8).getVelocity();
                        float i = Math.min(1.0F, (float)Math.sqrt(lv9.x * lv9.x * 0.20000000298023224 + lv9.y * lv9.y + lv9.z * lv9.z * 0.20000000298023224) * h);
                        this.playSwimSound(i);
                     }

                     if (lv7.emitsGameEvents()) {
                        this.emitGameEvent(GameEvent.SWIM);
                     }
                  }
               } else if (lv4.isAir()) {
                  this.addAirTravelEffects();
               }
            }

            this.tryCheckBlockCollision();
            float j = this.getVelocityMultiplier();
            this.setVelocity(this.getVelocity().multiply((double)j, 1.0, (double)j));
            if (this.world.getStatesInBoxIfLoaded(this.getBoundingBox().contract(1.0E-6)).noneMatch((state) -> {
               return state.isIn(BlockTags.FIRE) || state.isOf(Blocks.LAVA);
            })) {
               if (this.fireTicks <= 0) {
                  this.setFireTicks(-this.getBurningDuration());
               }

               if (this.wasOnFire && (this.inPowderSnow || this.isWet())) {
                  this.playExtinguishSound();
               }
            }

            if (this.isOnFire() && (this.inPowderSnow || this.isWet())) {
               this.setFireTicks(-this.getBurningDuration());
            }

            this.world.getProfiler().pop();
         }
      }
   }

   protected boolean hasCollidedSoftly(Vec3d adjustedMovement) {
      return false;
   }

   protected void tryCheckBlockCollision() {
      try {
         this.checkBlockCollision();
      } catch (Throwable var4) {
         CrashReport lv = CrashReport.create(var4, "Checking entity block collision");
         CrashReportSection lv2 = lv.addElement("Entity being checked for collision");
         this.populateCrashReport(lv2);
         throw new CrashException(lv);
      }
   }

   protected void playExtinguishSound() {
      this.playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
   }

   public void extinguishWithSound() {
      if (!this.world.isClient && this.wasOnFire) {
         this.playExtinguishSound();
      }

      this.extinguish();
   }

   protected void addAirTravelEffects() {
      if (this.isFlappingWings()) {
         this.addFlapEffects();
         if (this.getMoveEffect().emitsGameEvents()) {
            this.emitGameEvent(GameEvent.FLAP);
         }
      }

   }

   /** @deprecated */
   @Deprecated
   public BlockPos getLandingPos() {
      return this.getPosWithYOffset(0.2F);
   }

   public BlockPos getSteppingPos() {
      return this.getPosWithYOffset(1.0E-5F);
   }

   private BlockPos getPosWithYOffset(float offset) {
      int i = MathHelper.floor(this.pos.x);
      int j = MathHelper.floor(this.pos.y - (double)offset);
      int k = MathHelper.floor(this.pos.z);
      BlockPos lv = new BlockPos(i, j, k);
      if (this.world.getBlockState(lv).isAir()) {
         BlockPos lv2 = lv.down();
         BlockState lv3 = this.world.getBlockState(lv2);
         if (lv3.isIn(BlockTags.FENCES) || lv3.isIn(BlockTags.WALLS) || lv3.getBlock() instanceof FenceGateBlock) {
            return lv2;
         }
      }

      return lv;
   }

   protected float getJumpVelocityMultiplier() {
      float f = this.world.getBlockState(this.getBlockPos()).getBlock().getJumpVelocityMultiplier();
      float g = this.world.getBlockState(this.getVelocityAffectingPos()).getBlock().getJumpVelocityMultiplier();
      return (double)f == 1.0 ? g : f;
   }

   protected float getVelocityMultiplier() {
      BlockState lv = this.world.getBlockState(this.getBlockPos());
      float f = lv.getBlock().getVelocityMultiplier();
      if (!lv.isOf(Blocks.WATER) && !lv.isOf(Blocks.BUBBLE_COLUMN)) {
         return (double)f == 1.0 ? this.world.getBlockState(this.getVelocityAffectingPos()).getBlock().getVelocityMultiplier() : f;
      } else {
         return f;
      }
   }

   protected BlockPos getVelocityAffectingPos() {
      return BlockPos.ofFloored(this.pos.x, this.getBoundingBox().minY - 0.5000001, this.pos.z);
   }

   protected Vec3d adjustMovementForSneaking(Vec3d movement, MovementType type) {
      return movement;
   }

   protected Vec3d adjustMovementForPiston(Vec3d movement) {
      if (movement.lengthSquared() <= 1.0E-7) {
         return movement;
      } else {
         long l = this.world.getTime();
         if (l != this.pistonMovementTick) {
            Arrays.fill(this.pistonMovementDelta, 0.0);
            this.pistonMovementTick = l;
         }

         double d;
         if (movement.x != 0.0) {
            d = this.calculatePistonMovementFactor(Direction.Axis.X, movement.x);
            return Math.abs(d) <= 9.999999747378752E-6 ? Vec3d.ZERO : new Vec3d(d, 0.0, 0.0);
         } else if (movement.y != 0.0) {
            d = this.calculatePistonMovementFactor(Direction.Axis.Y, movement.y);
            return Math.abs(d) <= 9.999999747378752E-6 ? Vec3d.ZERO : new Vec3d(0.0, d, 0.0);
         } else if (movement.z != 0.0) {
            d = this.calculatePistonMovementFactor(Direction.Axis.Z, movement.z);
            return Math.abs(d) <= 9.999999747378752E-6 ? Vec3d.ZERO : new Vec3d(0.0, 0.0, d);
         } else {
            return Vec3d.ZERO;
         }
      }
   }

   private double calculatePistonMovementFactor(Direction.Axis axis, double offsetFactor) {
      int i = axis.ordinal();
      double e = MathHelper.clamp(offsetFactor + this.pistonMovementDelta[i], -0.51, 0.51);
      offsetFactor = e - this.pistonMovementDelta[i];
      this.pistonMovementDelta[i] = e;
      return offsetFactor;
   }

   private Vec3d adjustMovementForCollisions(Vec3d movement) {
      Box lv = this.getBoundingBox();
      List list = this.world.getEntityCollisions(this, lv.stretch(movement));
      Vec3d lv2 = movement.lengthSquared() == 0.0 ? movement : adjustMovementForCollisions(this, movement, lv, this.world, list);
      boolean bl = movement.x != lv2.x;
      boolean bl2 = movement.y != lv2.y;
      boolean bl3 = movement.z != lv2.z;
      boolean bl4 = this.onGround || bl2 && movement.y < 0.0;
      if (this.getStepHeight() > 0.0F && bl4 && (bl || bl3)) {
         Vec3d lv3 = adjustMovementForCollisions(this, new Vec3d(movement.x, (double)this.getStepHeight(), movement.z), lv, this.world, list);
         Vec3d lv4 = adjustMovementForCollisions(this, new Vec3d(0.0, (double)this.getStepHeight(), 0.0), lv.stretch(movement.x, 0.0, movement.z), this.world, list);
         if (lv4.y < (double)this.getStepHeight()) {
            Vec3d lv5 = adjustMovementForCollisions(this, new Vec3d(movement.x, 0.0, movement.z), lv.offset(lv4), this.world, list).add(lv4);
            if (lv5.horizontalLengthSquared() > lv3.horizontalLengthSquared()) {
               lv3 = lv5;
            }
         }

         if (lv3.horizontalLengthSquared() > lv2.horizontalLengthSquared()) {
            return lv3.add(adjustMovementForCollisions(this, new Vec3d(0.0, -lv3.y + movement.y, 0.0), lv.offset(lv3), this.world, list));
         }
      }

      return lv2;
   }

   public static Vec3d adjustMovementForCollisions(@Nullable Entity entity, Vec3d movement, Box entityBoundingBox, World world, List collisions) {
      ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize(collisions.size() + 1);
      if (!collisions.isEmpty()) {
         builder.addAll(collisions);
      }

      WorldBorder lv = world.getWorldBorder();
      boolean bl = entity != null && lv.canCollide(entity, entityBoundingBox.stretch(movement));
      if (bl) {
         builder.add(lv.asVoxelShape());
      }

      builder.addAll(world.getBlockCollisions(entity, entityBoundingBox.stretch(movement)));
      return adjustMovementForCollisions(movement, entityBoundingBox, builder.build());
   }

   private static Vec3d adjustMovementForCollisions(Vec3d movement, Box entityBoundingBox, List collisions) {
      if (collisions.isEmpty()) {
         return movement;
      } else {
         double d = movement.x;
         double e = movement.y;
         double f = movement.z;
         if (e != 0.0) {
            e = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, entityBoundingBox, collisions, e);
            if (e != 0.0) {
               entityBoundingBox = entityBoundingBox.offset(0.0, e, 0.0);
            }
         }

         boolean bl = Math.abs(d) < Math.abs(f);
         if (bl && f != 0.0) {
            f = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, collisions, f);
            if (f != 0.0) {
               entityBoundingBox = entityBoundingBox.offset(0.0, 0.0, f);
            }
         }

         if (d != 0.0) {
            d = VoxelShapes.calculateMaxOffset(Direction.Axis.X, entityBoundingBox, collisions, d);
            if (!bl && d != 0.0) {
               entityBoundingBox = entityBoundingBox.offset(d, 0.0, 0.0);
            }
         }

         if (!bl && f != 0.0) {
            f = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, collisions, f);
         }

         return new Vec3d(d, e, f);
      }
   }

   protected float calculateNextStepSoundDistance() {
      return (float)((int)this.distanceTraveled + 1);
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.ENTITY_GENERIC_SWIM;
   }

   protected SoundEvent getSplashSound() {
      return SoundEvents.ENTITY_GENERIC_SPLASH;
   }

   protected SoundEvent getHighSpeedSplashSound() {
      return SoundEvents.ENTITY_GENERIC_SPLASH;
   }

   protected void checkBlockCollision() {
      Box lv = this.getBoundingBox();
      BlockPos lv2 = BlockPos.ofFloored(lv.minX + 1.0E-7, lv.minY + 1.0E-7, lv.minZ + 1.0E-7);
      BlockPos lv3 = BlockPos.ofFloored(lv.maxX - 1.0E-7, lv.maxY - 1.0E-7, lv.maxZ - 1.0E-7);
      if (this.world.isRegionLoaded(lv2, lv3)) {
         BlockPos.Mutable lv4 = new BlockPos.Mutable();

         for(int i = lv2.getX(); i <= lv3.getX(); ++i) {
            for(int j = lv2.getY(); j <= lv3.getY(); ++j) {
               for(int k = lv2.getZ(); k <= lv3.getZ(); ++k) {
                  lv4.set(i, j, k);
                  BlockState lv5 = this.world.getBlockState(lv4);

                  try {
                     lv5.onEntityCollision(this.world, lv4, this);
                     this.onBlockCollision(lv5);
                  } catch (Throwable var12) {
                     CrashReport lv6 = CrashReport.create(var12, "Colliding entity with block");
                     CrashReportSection lv7 = lv6.addElement("Block being collided with");
                     CrashReportSection.addBlockInfo(lv7, this.world, lv4, lv5);
                     throw new CrashException(lv6);
                  }
               }
            }
         }
      }

   }

   protected void onBlockCollision(BlockState state) {
   }

   public void emitGameEvent(GameEvent event, @Nullable Entity entity) {
      this.world.emitGameEvent(entity, event, this.pos);
   }

   public void emitGameEvent(GameEvent event) {
      this.emitGameEvent(event, this);
   }

   private void playStepSounds(BlockPos pos, BlockState state) {
      BlockPos lv = this.getStepSoundPos(pos);
      if (this instanceof PlayerEntity && !pos.equals(lv)) {
         BlockState lv2 = this.world.getBlockState(lv);
         if (lv2.isIn(BlockTags.COMBINATION_STEP_SOUND_BLOCKS)) {
            this.playCombinationStepSounds(lv2, state);
         } else {
            this.playStepSound(lv, lv2);
         }
      } else {
         this.playStepSound(pos, state);
      }

      if (this.shouldPlayAmethystChimeSound(state)) {
         this.playAmethystChimeSound();
      }

   }

   private BlockPos getStepSoundPos(BlockPos pos) {
      BlockPos lv = pos.up();
      BlockState lv2 = this.world.getBlockState(lv);
      return !lv2.isIn(BlockTags.INSIDE_STEP_SOUND_BLOCKS) && !lv2.isIn(BlockTags.COMBINATION_STEP_SOUND_BLOCKS) ? pos : lv;
   }

   private void playCombinationStepSounds(BlockState primaryState, BlockState secondaryState) {
      BlockSoundGroup lv = primaryState.getSoundGroup();
      BlockSoundGroup lv2 = secondaryState.getSoundGroup();
      this.playSound(lv.getStepSound(), lv.getVolume() * 0.15F, lv.getPitch());
      this.playSound(lv2.getStepSound(), lv2.getVolume() * 0.05F, lv2.getPitch() * 0.8F);
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      BlockSoundGroup lv = state.getSoundGroup();
      this.playSound(lv.getStepSound(), lv.getVolume() * 0.15F, lv.getPitch());
   }

   private boolean shouldPlayAmethystChimeSound(BlockState state) {
      return state.isIn(BlockTags.CRYSTAL_SOUND_BLOCKS) && this.age >= this.lastChimeAge + 20;
   }

   private void playAmethystChimeSound() {
      this.lastChimeIntensity *= (float)Math.pow(0.997, (double)(this.age - this.lastChimeAge));
      this.lastChimeIntensity = Math.min(1.0F, this.lastChimeIntensity + 0.07F);
      float f = 0.5F + this.lastChimeIntensity * this.random.nextFloat() * 1.2F;
      float g = 0.1F + this.lastChimeIntensity * 1.2F;
      this.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, g, f);
      this.lastChimeAge = this.age;
   }

   protected void playSwimSound(float volume) {
      this.playSound(this.getSwimSound(), volume, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
   }

   protected void addFlapEffects() {
   }

   protected boolean isFlappingWings() {
      return false;
   }

   public void playSound(SoundEvent sound, float volume, float pitch) {
      if (!this.isSilent()) {
         this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), sound, this.getSoundCategory(), volume, pitch);
      }

   }

   public void playSoundIfNotSilent(SoundEvent event) {
      if (!this.isSilent()) {
         this.playSound(event, 1.0F, 1.0F);
      }

   }

   public boolean isSilent() {
      return (Boolean)this.dataTracker.get(SILENT);
   }

   public void setSilent(boolean silent) {
      this.dataTracker.set(SILENT, silent);
   }

   public boolean hasNoGravity() {
      return (Boolean)this.dataTracker.get(NO_GRAVITY);
   }

   public void setNoGravity(boolean noGravity) {
      this.dataTracker.set(NO_GRAVITY, noGravity);
   }

   protected MoveEffect getMoveEffect() {
      return Entity.MoveEffect.ALL;
   }

   public boolean occludeVibrationSignals() {
      return false;
   }

   protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
      if (onGround) {
         if (this.fallDistance > 0.0F) {
            state.getBlock().onLandedUpon(this.world, state, landedPosition, this, this.fallDistance);
            this.world.emitGameEvent(GameEvent.HIT_GROUND, this.pos, GameEvent.Emitter.of(this, this.getSteppingBlockState()));
         }

         this.onLanding();
      } else if (heightDifference < 0.0) {
         this.fallDistance -= (float)heightDifference;
      }

   }

   public boolean isFireImmune() {
      return this.getType().isFireImmune();
   }

   public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
      if (this.type.isIn(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
         return false;
      } else {
         if (this.hasPassengers()) {
            Iterator var4 = this.getPassengerList().iterator();

            while(var4.hasNext()) {
               Entity lv = (Entity)var4.next();
               lv.handleFallDamage(fallDistance, damageMultiplier, damageSource);
            }
         }

         return false;
      }
   }

   public boolean isTouchingWater() {
      return this.touchingWater;
   }

   private boolean isBeingRainedOn() {
      BlockPos lv = this.getBlockPos();
      return this.world.hasRain(lv) || this.world.hasRain(BlockPos.ofFloored((double)lv.getX(), this.getBoundingBox().maxY, (double)lv.getZ()));
   }

   private boolean isInsideBubbleColumn() {
      return this.world.getBlockState(this.getBlockPos()).isOf(Blocks.BUBBLE_COLUMN);
   }

   public boolean isTouchingWaterOrRain() {
      return this.isTouchingWater() || this.isBeingRainedOn();
   }

   public boolean isWet() {
      return this.isTouchingWater() || this.isBeingRainedOn() || this.isInsideBubbleColumn();
   }

   public boolean isInsideWaterOrBubbleColumn() {
      return this.isTouchingWater() || this.isInsideBubbleColumn();
   }

   public boolean isSubmergedInWater() {
      return this.submergedInWater && this.isTouchingWater();
   }

   public void updateSwimming() {
      if (this.isSwimming()) {
         this.setSwimming(this.isSprinting() && this.isTouchingWater() && !this.hasVehicle());
      } else {
         this.setSwimming(this.isSprinting() && this.isSubmergedInWater() && !this.hasVehicle() && this.world.getFluidState(this.blockPos).isIn(FluidTags.WATER));
      }

   }

   protected boolean updateWaterState() {
      this.fluidHeight.clear();
      this.checkWaterState();
      double d = this.world.getDimension().ultrawarm() ? 0.007 : 0.0023333333333333335;
      boolean bl = this.updateMovementInFluid(FluidTags.LAVA, d);
      return this.isTouchingWater() || bl;
   }

   void checkWaterState() {
      Entity var2 = this.getVehicle();
      if (var2 instanceof BoatEntity lv) {
         if (!lv.isSubmergedInWater()) {
            this.touchingWater = false;
            return;
         }
      }

      if (this.updateMovementInFluid(FluidTags.WATER, 0.014)) {
         if (!this.touchingWater && !this.firstUpdate) {
            this.onSwimmingStart();
         }

         this.onLanding();
         this.touchingWater = true;
         this.extinguish();
      } else {
         this.touchingWater = false;
      }

   }

   private void updateSubmergedInWaterState() {
      this.submergedInWater = this.isSubmergedIn(FluidTags.WATER);
      this.submergedFluidTag.clear();
      double d = this.getEyeY() - 0.1111111119389534;
      Entity lv = this.getVehicle();
      if (lv instanceof BoatEntity lv2) {
         if (!lv2.isSubmergedInWater() && lv2.getBoundingBox().maxY >= d && lv2.getBoundingBox().minY <= d) {
            return;
         }
      }

      BlockPos lv3 = BlockPos.ofFloored(this.getX(), d, this.getZ());
      FluidState lv4 = this.world.getFluidState(lv3);
      double e = (double)((float)lv3.getY() + lv4.getHeight(this.world, lv3));
      if (e > d) {
         Stream var10000 = lv4.streamTags();
         Set var10001 = this.submergedFluidTag;
         Objects.requireNonNull(var10001);
         var10000.forEach(var10001::add);
      }

   }

   protected void onSwimmingStart() {
      Entity lv = this.hasPassengers() && this.getControllingPassenger() != null ? this.getControllingPassenger() : this;
      float f = lv == this ? 0.2F : 0.9F;
      Vec3d lv2 = ((Entity)lv).getVelocity();
      float g = Math.min(1.0F, (float)Math.sqrt(lv2.x * lv2.x * 0.20000000298023224 + lv2.y * lv2.y + lv2.z * lv2.z * 0.20000000298023224) * f);
      if (g < 0.25F) {
         this.playSound(this.getSplashSound(), g, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
      } else {
         this.playSound(this.getHighSpeedSplashSound(), g, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
      }

      float h = (float)MathHelper.floor(this.getY());

      int i;
      double d;
      double e;
      for(i = 0; (float)i < 1.0F + this.dimensions.width * 20.0F; ++i) {
         d = (this.random.nextDouble() * 2.0 - 1.0) * (double)this.dimensions.width;
         e = (this.random.nextDouble() * 2.0 - 1.0) * (double)this.dimensions.width;
         this.world.addParticle(ParticleTypes.BUBBLE, this.getX() + d, (double)(h + 1.0F), this.getZ() + e, lv2.x, lv2.y - this.random.nextDouble() * 0.20000000298023224, lv2.z);
      }

      for(i = 0; (float)i < 1.0F + this.dimensions.width * 20.0F; ++i) {
         d = (this.random.nextDouble() * 2.0 - 1.0) * (double)this.dimensions.width;
         e = (this.random.nextDouble() * 2.0 - 1.0) * (double)this.dimensions.width;
         this.world.addParticle(ParticleTypes.SPLASH, this.getX() + d, (double)(h + 1.0F), this.getZ() + e, lv2.x, lv2.y, lv2.z);
      }

      this.emitGameEvent(GameEvent.SPLASH);
   }

   /** @deprecated */
   @Deprecated
   protected BlockState getLandingBlockState() {
      return this.world.getBlockState(this.getLandingPos());
   }

   public BlockState getSteppingBlockState() {
      return this.world.getBlockState(this.getSteppingPos());
   }

   public boolean shouldSpawnSprintingParticles() {
      return this.isSprinting() && !this.isTouchingWater() && !this.isSpectator() && !this.isInSneakingPose() && !this.isInLava() && this.isAlive();
   }

   protected void spawnSprintingParticles() {
      int i = MathHelper.floor(this.getX());
      int j = MathHelper.floor(this.getY() - 0.20000000298023224);
      int k = MathHelper.floor(this.getZ());
      BlockPos lv = new BlockPos(i, j, k);
      BlockState lv2 = this.world.getBlockState(lv);
      if (lv2.getRenderType() != BlockRenderType.INVISIBLE) {
         Vec3d lv3 = this.getVelocity();
         this.world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, lv2), this.getX() + (this.random.nextDouble() - 0.5) * (double)this.dimensions.width, this.getY() + 0.1, this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.dimensions.width, lv3.x * -4.0, 1.5, lv3.z * -4.0);
      }

   }

   public boolean isSubmergedIn(TagKey fluidTag) {
      return this.submergedFluidTag.contains(fluidTag);
   }

   public boolean isInLava() {
      return !this.firstUpdate && this.fluidHeight.getDouble(FluidTags.LAVA) > 0.0;
   }

   public void updateVelocity(float speed, Vec3d movementInput) {
      Vec3d lv = movementInputToVelocity(movementInput, speed, this.getYaw());
      this.setVelocity(this.getVelocity().add(lv));
   }

   private static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
      double d = movementInput.lengthSquared();
      if (d < 1.0E-7) {
         return Vec3d.ZERO;
      } else {
         Vec3d lv = (d > 1.0 ? movementInput.normalize() : movementInput).multiply((double)speed);
         float h = MathHelper.sin(yaw * 0.017453292F);
         float i = MathHelper.cos(yaw * 0.017453292F);
         return new Vec3d(lv.x * (double)i - lv.z * (double)h, lv.y, lv.z * (double)i + lv.x * (double)h);
      }
   }

   /** @deprecated */
   @Deprecated
   public float getBrightnessAtEyes() {
      return this.world.isPosLoaded(this.getBlockX(), this.getBlockZ()) ? this.world.getBrightness(BlockPos.ofFloored(this.getX(), this.getEyeY(), this.getZ())) : 0.0F;
   }

   public void updatePositionAndAngles(double x, double y, double z, float yaw, float pitch) {
      this.updatePosition(x, y, z);
      this.setYaw(yaw % 360.0F);
      this.setPitch(MathHelper.clamp(pitch, -90.0F, 90.0F) % 360.0F);
      this.prevYaw = this.getYaw();
      this.prevPitch = this.getPitch();
   }

   public void updatePosition(double x, double y, double z) {
      double g = MathHelper.clamp(x, -3.0E7, 3.0E7);
      double h = MathHelper.clamp(z, -3.0E7, 3.0E7);
      this.prevX = g;
      this.prevY = y;
      this.prevZ = h;
      this.setPosition(g, y, h);
   }

   public void refreshPositionAfterTeleport(Vec3d pos) {
      this.refreshPositionAfterTeleport(pos.x, pos.y, pos.z);
   }

   public void refreshPositionAfterTeleport(double x, double y, double z) {
      this.refreshPositionAndAngles(x, y, z, this.getYaw(), this.getPitch());
   }

   public void refreshPositionAndAngles(BlockPos pos, float yaw, float pitch) {
      this.refreshPositionAndAngles((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5, yaw, pitch);
   }

   public void refreshPositionAndAngles(double x, double y, double z, float yaw, float pitch) {
      this.setPos(x, y, z);
      this.setYaw(yaw);
      this.setPitch(pitch);
      this.resetPosition();
      this.refreshPosition();
   }

   public final void resetPosition() {
      double d = this.getX();
      double e = this.getY();
      double f = this.getZ();
      this.prevX = d;
      this.prevY = e;
      this.prevZ = f;
      this.lastRenderX = d;
      this.lastRenderY = e;
      this.lastRenderZ = f;
      this.prevYaw = this.getYaw();
      this.prevPitch = this.getPitch();
   }

   public float distanceTo(Entity entity) {
      float f = (float)(this.getX() - entity.getX());
      float g = (float)(this.getY() - entity.getY());
      float h = (float)(this.getZ() - entity.getZ());
      return MathHelper.sqrt(f * f + g * g + h * h);
   }

   public double squaredDistanceTo(double x, double y, double z) {
      double g = this.getX() - x;
      double h = this.getY() - y;
      double i = this.getZ() - z;
      return g * g + h * h + i * i;
   }

   public double squaredDistanceTo(Entity entity) {
      return this.squaredDistanceTo(entity.getPos());
   }

   public double squaredDistanceTo(Vec3d vector) {
      double d = this.getX() - vector.x;
      double e = this.getY() - vector.y;
      double f = this.getZ() - vector.z;
      return d * d + e * e + f * f;
   }

   public void onPlayerCollision(PlayerEntity player) {
   }

   public void pushAwayFrom(Entity entity) {
      if (!this.isConnectedThroughVehicle(entity)) {
         if (!entity.noClip && !this.noClip) {
            double d = entity.getX() - this.getX();
            double e = entity.getZ() - this.getZ();
            double f = MathHelper.absMax(d, e);
            if (f >= 0.009999999776482582) {
               f = Math.sqrt(f);
               d /= f;
               e /= f;
               double g = 1.0 / f;
               if (g > 1.0) {
                  g = 1.0;
               }

               d *= g;
               e *= g;
               d *= 0.05000000074505806;
               e *= 0.05000000074505806;
               if (!this.hasPassengers() && this.isPushable()) {
                  this.addVelocity(-d, 0.0, -e);
               }

               if (!entity.hasPassengers() && entity.isPushable()) {
                  entity.addVelocity(d, 0.0, e);
               }
            }

         }
      }
   }

   public void addVelocity(double deltaX, double deltaY, double deltaZ) {
      this.setVelocity(this.getVelocity().add(deltaX, deltaY, deltaZ));
      this.velocityDirty = true;
   }

   protected void scheduleVelocityUpdate() {
      this.velocityModified = true;
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else {
         this.scheduleVelocityUpdate();
         return false;
      }
   }

   public final Vec3d getRotationVec(float tickDelta) {
      return this.getRotationVector(this.getPitch(tickDelta), this.getYaw(tickDelta));
   }

   public float getPitch(float tickDelta) {
      return tickDelta == 1.0F ? this.getPitch() : MathHelper.lerp(tickDelta, this.prevPitch, this.getPitch());
   }

   public float getYaw(float tickDelta) {
      return tickDelta == 1.0F ? this.getYaw() : MathHelper.lerp(tickDelta, this.prevYaw, this.getYaw());
   }

   protected final Vec3d getRotationVector(float pitch, float yaw) {
      float h = pitch * 0.017453292F;
      float i = -yaw * 0.017453292F;
      float j = MathHelper.cos(i);
      float k = MathHelper.sin(i);
      float l = MathHelper.cos(h);
      float m = MathHelper.sin(h);
      return new Vec3d((double)(k * l), (double)(-m), (double)(j * l));
   }

   public final Vec3d getOppositeRotationVector(float tickDelta) {
      return this.getOppositeRotationVector(this.getPitch(tickDelta), this.getYaw(tickDelta));
   }

   protected final Vec3d getOppositeRotationVector(float pitch, float yaw) {
      return this.getRotationVector(pitch - 90.0F, yaw);
   }

   public final Vec3d getEyePos() {
      return new Vec3d(this.getX(), this.getEyeY(), this.getZ());
   }

   public final Vec3d getCameraPosVec(float tickDelta) {
      double d = MathHelper.lerp((double)tickDelta, this.prevX, this.getX());
      double e = MathHelper.lerp((double)tickDelta, this.prevY, this.getY()) + (double)this.getStandingEyeHeight();
      double g = MathHelper.lerp((double)tickDelta, this.prevZ, this.getZ());
      return new Vec3d(d, e, g);
   }

   public Vec3d getClientCameraPosVec(float tickDelta) {
      return this.getCameraPosVec(tickDelta);
   }

   public final Vec3d getLerpedPos(float delta) {
      double d = MathHelper.lerp((double)delta, this.prevX, this.getX());
      double e = MathHelper.lerp((double)delta, this.prevY, this.getY());
      double g = MathHelper.lerp((double)delta, this.prevZ, this.getZ());
      return new Vec3d(d, e, g);
   }

   public HitResult raycast(double maxDistance, float tickDelta, boolean includeFluids) {
      Vec3d lv = this.getCameraPosVec(tickDelta);
      Vec3d lv2 = this.getRotationVec(tickDelta);
      Vec3d lv3 = lv.add(lv2.x * maxDistance, lv2.y * maxDistance, lv2.z * maxDistance);
      return this.world.raycast(new RaycastContext(lv, lv3, RaycastContext.ShapeType.OUTLINE, includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, this));
   }

   public boolean canBeHitByProjectile() {
      return this.isAlive() && this.canHit();
   }

   public boolean canHit() {
      return false;
   }

   public boolean isPushable() {
      return false;
   }

   public void updateKilledAdvancementCriterion(Entity entityKilled, int score, DamageSource damageSource) {
      if (entityKilled instanceof ServerPlayerEntity) {
         Criteria.ENTITY_KILLED_PLAYER.trigger((ServerPlayerEntity)entityKilled, this, damageSource);
      }

   }

   public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
      double g = this.getX() - cameraX;
      double h = this.getY() - cameraY;
      double i = this.getZ() - cameraZ;
      double j = g * g + h * h + i * i;
      return this.shouldRender(j);
   }

   public boolean shouldRender(double distance) {
      double e = this.getBoundingBox().getAverageSideLength();
      if (Double.isNaN(e)) {
         e = 1.0;
      }

      e *= 64.0 * renderDistanceMultiplier;
      return distance < e * e;
   }

   public boolean saveSelfNbt(NbtCompound nbt) {
      if (this.removalReason != null && !this.removalReason.shouldSave()) {
         return false;
      } else {
         String string = this.getSavedEntityId();
         if (string == null) {
            return false;
         } else {
            nbt.putString("id", string);
            this.writeNbt(nbt);
            return true;
         }
      }
   }

   public boolean saveNbt(NbtCompound nbt) {
      return this.hasVehicle() ? false : this.saveSelfNbt(nbt);
   }

   public NbtCompound writeNbt(NbtCompound nbt) {
      try {
         if (this.vehicle != null) {
            nbt.put("Pos", this.toNbtList(this.vehicle.getX(), this.getY(), this.vehicle.getZ()));
         } else {
            nbt.put("Pos", this.toNbtList(this.getX(), this.getY(), this.getZ()));
         }

         Vec3d lv = this.getVelocity();
         nbt.put("Motion", this.toNbtList(lv.x, lv.y, lv.z));
         nbt.put("Rotation", this.toNbtList(this.getYaw(), this.getPitch()));
         nbt.putFloat("FallDistance", this.fallDistance);
         nbt.putShort("Fire", (short)this.fireTicks);
         nbt.putShort("Air", (short)this.getAir());
         nbt.putBoolean("OnGround", this.onGround);
         nbt.putBoolean("Invulnerable", this.invulnerable);
         nbt.putInt("PortalCooldown", this.portalCooldown);
         nbt.putUuid("UUID", this.getUuid());
         Text lv2 = this.getCustomName();
         if (lv2 != null) {
            nbt.putString("CustomName", Text.Serializer.toJson(lv2));
         }

         if (this.isCustomNameVisible()) {
            nbt.putBoolean("CustomNameVisible", this.isCustomNameVisible());
         }

         if (this.isSilent()) {
            nbt.putBoolean("Silent", this.isSilent());
         }

         if (this.hasNoGravity()) {
            nbt.putBoolean("NoGravity", this.hasNoGravity());
         }

         if (this.glowing) {
            nbt.putBoolean("Glowing", true);
         }

         int i = this.getFrozenTicks();
         if (i > 0) {
            nbt.putInt("TicksFrozen", this.getFrozenTicks());
         }

         if (this.hasVisualFire) {
            nbt.putBoolean("HasVisualFire", this.hasVisualFire);
         }

         NbtList lv3;
         Iterator var6;
         if (!this.commandTags.isEmpty()) {
            lv3 = new NbtList();
            var6 = this.commandTags.iterator();

            while(var6.hasNext()) {
               String string = (String)var6.next();
               lv3.add(NbtString.of(string));
            }

            nbt.put("Tags", lv3);
         }

         this.writeCustomDataToNbt(nbt);
         if (this.hasPassengers()) {
            lv3 = new NbtList();
            var6 = this.getPassengerList().iterator();

            while(var6.hasNext()) {
               Entity lv4 = (Entity)var6.next();
               NbtCompound lv5 = new NbtCompound();
               if (lv4.saveSelfNbt(lv5)) {
                  lv3.add(lv5);
               }
            }

            if (!lv3.isEmpty()) {
               nbt.put("Passengers", lv3);
            }
         }

         return nbt;
      } catch (Throwable var9) {
         CrashReport lv6 = CrashReport.create(var9, "Saving entity NBT");
         CrashReportSection lv7 = lv6.addElement("Entity being saved");
         this.populateCrashReport(lv7);
         throw new CrashException(lv6);
      }
   }

   public void readNbt(NbtCompound nbt) {
      try {
         NbtList lv = nbt.getList("Pos", NbtElement.DOUBLE_TYPE);
         NbtList lv2 = nbt.getList("Motion", NbtElement.DOUBLE_TYPE);
         NbtList lv3 = nbt.getList("Rotation", NbtElement.FLOAT_TYPE);
         double d = lv2.getDouble(0);
         double e = lv2.getDouble(1);
         double f = lv2.getDouble(2);
         this.setVelocity(Math.abs(d) > 10.0 ? 0.0 : d, Math.abs(e) > 10.0 ? 0.0 : e, Math.abs(f) > 10.0 ? 0.0 : f);
         double g = 3.0000512E7;
         this.setPos(MathHelper.clamp(lv.getDouble(0), -3.0000512E7, 3.0000512E7), MathHelper.clamp(lv.getDouble(1), -2.0E7, 2.0E7), MathHelper.clamp(lv.getDouble(2), -3.0000512E7, 3.0000512E7));
         this.setYaw(lv3.getFloat(0));
         this.setPitch(lv3.getFloat(1));
         this.resetPosition();
         this.setHeadYaw(this.getYaw());
         this.setBodyYaw(this.getYaw());
         this.fallDistance = nbt.getFloat("FallDistance");
         this.fireTicks = nbt.getShort("Fire");
         if (nbt.contains("Air")) {
            this.setAir(nbt.getShort("Air"));
         }

         this.onGround = nbt.getBoolean("OnGround");
         this.invulnerable = nbt.getBoolean("Invulnerable");
         this.portalCooldown = nbt.getInt("PortalCooldown");
         if (nbt.containsUuid("UUID")) {
            this.uuid = nbt.getUuid("UUID");
            this.uuidString = this.uuid.toString();
         }

         if (Double.isFinite(this.getX()) && Double.isFinite(this.getY()) && Double.isFinite(this.getZ())) {
            if (Double.isFinite((double)this.getYaw()) && Double.isFinite((double)this.getPitch())) {
               this.refreshPosition();
               this.setRotation(this.getYaw(), this.getPitch());
               if (nbt.contains("CustomName", NbtElement.STRING_TYPE)) {
                  String string = nbt.getString("CustomName");

                  try {
                     this.setCustomName(Text.Serializer.fromJson(string));
                  } catch (Exception var16) {
                     LOGGER.warn("Failed to parse entity custom name {}", string, var16);
                  }
               }

               this.setCustomNameVisible(nbt.getBoolean("CustomNameVisible"));
               this.setSilent(nbt.getBoolean("Silent"));
               this.setNoGravity(nbt.getBoolean("NoGravity"));
               this.setGlowing(nbt.getBoolean("Glowing"));
               this.setFrozenTicks(nbt.getInt("TicksFrozen"));
               this.hasVisualFire = nbt.getBoolean("HasVisualFire");
               if (nbt.contains("Tags", NbtElement.LIST_TYPE)) {
                  this.commandTags.clear();
                  NbtList lv4 = nbt.getList("Tags", NbtElement.STRING_TYPE);
                  int i = Math.min(lv4.size(), 1024);

                  for(int j = 0; j < i; ++j) {
                     this.commandTags.add(lv4.getString(j));
                  }
               }

               this.readCustomDataFromNbt(nbt);
               if (this.shouldSetPositionOnLoad()) {
                  this.refreshPosition();
               }

            } else {
               throw new IllegalStateException("Entity has invalid rotation");
            }
         } else {
            throw new IllegalStateException("Entity has invalid position");
         }
      } catch (Throwable var17) {
         CrashReport lv5 = CrashReport.create(var17, "Loading entity NBT");
         CrashReportSection lv6 = lv5.addElement("Entity being loaded");
         this.populateCrashReport(lv6);
         throw new CrashException(lv5);
      }
   }

   protected boolean shouldSetPositionOnLoad() {
      return true;
   }

   @Nullable
   protected final String getSavedEntityId() {
      EntityType lv = this.getType();
      Identifier lv2 = EntityType.getId(lv);
      return lv.isSaveable() && lv2 != null ? lv2.toString() : null;
   }

   protected abstract void readCustomDataFromNbt(NbtCompound nbt);

   protected abstract void writeCustomDataToNbt(NbtCompound nbt);

   protected NbtList toNbtList(double... values) {
      NbtList lv = new NbtList();
      double[] var3 = values;
      int var4 = values.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         double d = var3[var5];
         lv.add(NbtDouble.of(d));
      }

      return lv;
   }

   protected NbtList toNbtList(float... values) {
      NbtList lv = new NbtList();
      float[] var3 = values;
      int var4 = values.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         float f = var3[var5];
         lv.add(NbtFloat.of(f));
      }

      return lv;
   }

   @Nullable
   public ItemEntity dropItem(ItemConvertible item) {
      return this.dropItem(item, 0);
   }

   @Nullable
   public ItemEntity dropItem(ItemConvertible item, int yOffset) {
      return this.dropStack(new ItemStack(item), (float)yOffset);
   }

   @Nullable
   public ItemEntity dropStack(ItemStack stack) {
      return this.dropStack(stack, 0.0F);
   }

   @Nullable
   public ItemEntity dropStack(ItemStack stack, float yOffset) {
      if (stack.isEmpty()) {
         return null;
      } else if (this.world.isClient) {
         return null;
      } else {
         ItemEntity lv = new ItemEntity(this.world, this.getX(), this.getY() + (double)yOffset, this.getZ(), stack);
         lv.setToDefaultPickupDelay();
         this.world.spawnEntity(lv);
         return lv;
      }
   }

   public boolean isAlive() {
      return !this.isRemoved();
   }

   public boolean isInsideWall() {
      if (this.noClip) {
         return false;
      } else {
         float f = this.dimensions.width * 0.8F;
         Box lv = Box.of(this.getEyePos(), (double)f, 1.0E-6, (double)f);
         return BlockPos.stream(lv).anyMatch((pos) -> {
            BlockState lvx = this.world.getBlockState(pos);
            return !lvx.isAir() && lvx.shouldSuffocate(this.world, pos) && VoxelShapes.matchesAnywhere(lvx.getCollisionShape(this.world, pos).offset((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()), VoxelShapes.cuboid(lv), BooleanBiFunction.AND);
         });
      }
   }

   public ActionResult interact(PlayerEntity player, Hand hand) {
      return ActionResult.PASS;
   }

   public boolean collidesWith(Entity other) {
      return other.isCollidable() && !this.isConnectedThroughVehicle(other);
   }

   public boolean isCollidable() {
      return false;
   }

   public void tickRiding() {
      this.setVelocity(Vec3d.ZERO);
      this.tick();
      if (this.hasVehicle()) {
         this.getVehicle().updatePassengerPosition(this);
      }
   }

   public void updatePassengerPosition(Entity passenger) {
      this.updatePassengerPosition(passenger, Entity::setPosition);
   }

   private void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
      if (this.hasPassenger(passenger)) {
         double d = this.getY() + this.getMountedHeightOffset() + passenger.getHeightOffset();
         positionUpdater.accept(passenger, this.getX(), d, this.getZ());
      }
   }

   public void onPassengerLookAround(Entity passenger) {
   }

   public double getHeightOffset() {
      return 0.0;
   }

   public double getMountedHeightOffset() {
      return (double)this.dimensions.height * 0.75;
   }

   public boolean startRiding(Entity entity) {
      return this.startRiding(entity, false);
   }

   public boolean isLiving() {
      return this instanceof LivingEntity;
   }

   public boolean startRiding(Entity entity, boolean force) {
      if (entity == this.vehicle) {
         return false;
      } else if (!entity.couldAcceptPassenger()) {
         return false;
      } else {
         for(Entity lv = entity; lv.vehicle != null; lv = lv.vehicle) {
            if (lv.vehicle == this) {
               return false;
            }
         }

         if (!force && (!this.canStartRiding(entity) || !entity.canAddPassenger(this))) {
            return false;
         } else {
            if (this.hasVehicle()) {
               this.stopRiding();
            }

            this.setPose(EntityPose.STANDING);
            this.vehicle = entity;
            this.vehicle.addPassenger(this);
            entity.streamIntoPassengers().filter((passenger) -> {
               return passenger instanceof ServerPlayerEntity;
            }).forEach((player) -> {
               Criteria.STARTED_RIDING.trigger((ServerPlayerEntity)player);
            });
            return true;
         }
      }
   }

   protected boolean canStartRiding(Entity entity) {
      return !this.isSneaking() && this.ridingCooldown <= 0;
   }

   protected boolean wouldPoseNotCollide(EntityPose pose) {
      return this.world.isSpaceEmpty(this, this.calculateBoundsForPose(pose).contract(1.0E-7));
   }

   public void removeAllPassengers() {
      for(int i = this.passengerList.size() - 1; i >= 0; --i) {
         ((Entity)this.passengerList.get(i)).stopRiding();
      }

   }

   public void dismountVehicle() {
      if (this.vehicle != null) {
         Entity lv = this.vehicle;
         this.vehicle = null;
         lv.removePassenger(this);
      }

   }

   public void stopRiding() {
      this.dismountVehicle();
   }

   protected void addPassenger(Entity passenger) {
      if (passenger.getVehicle() != this) {
         throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
      } else {
         if (this.passengerList.isEmpty()) {
            this.passengerList = ImmutableList.of(passenger);
         } else {
            List list = Lists.newArrayList(this.passengerList);
            if (!this.world.isClient && passenger instanceof PlayerEntity && !(this.getFirstPassenger() instanceof PlayerEntity)) {
               list.add(0, passenger);
            } else {
               list.add(passenger);
            }

            this.passengerList = ImmutableList.copyOf(list);
         }

         this.emitGameEvent(GameEvent.ENTITY_MOUNT, passenger);
      }
   }

   protected void removePassenger(Entity passenger) {
      if (passenger.getVehicle() == this) {
         throw new IllegalStateException("Use x.stopRiding(y), not y.removePassenger(x)");
      } else {
         if (this.passengerList.size() == 1 && this.passengerList.get(0) == passenger) {
            this.passengerList = ImmutableList.of();
         } else {
            this.passengerList = (ImmutableList)this.passengerList.stream().filter((entity) -> {
               return entity != passenger;
            }).collect(ImmutableList.toImmutableList());
         }

         passenger.ridingCooldown = 60;
         this.emitGameEvent(GameEvent.ENTITY_DISMOUNT, passenger);
      }
   }

   protected boolean canAddPassenger(Entity passenger) {
      return this.passengerList.isEmpty();
   }

   protected boolean couldAcceptPassenger() {
      return true;
   }

   public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
      this.setPosition(x, y, z);
      this.setRotation(yaw, pitch);
   }

   public void updateTrackedHeadRotation(float yaw, int interpolationSteps) {
      this.setHeadYaw(yaw);
   }

   public float getTargetingMargin() {
      return 0.0F;
   }

   public Vec3d getRotationVector() {
      return this.getRotationVector(this.getPitch(), this.getYaw());
   }

   public Vec3d getHandPosOffset(Item item) {
      if (!(this instanceof PlayerEntity lv)) {
         return Vec3d.ZERO;
      } else {
         boolean bl = lv.getOffHandStack().isOf(item) && !lv.getMainHandStack().isOf(item);
         Arm lv2 = bl ? lv.getMainArm().getOpposite() : lv.getMainArm();
         return this.getRotationVector(0.0F, this.getYaw() + (float)(lv2 == Arm.RIGHT ? 80 : -80)).multiply(0.5);
      }
   }

   public Vec2f getRotationClient() {
      return new Vec2f(this.getPitch(), this.getYaw());
   }

   public Vec3d getRotationVecClient() {
      return Vec3d.fromPolar(this.getRotationClient());
   }

   public void setInNetherPortal(BlockPos pos) {
      if (this.hasPortalCooldown()) {
         this.resetPortalCooldown();
      } else {
         if (!this.world.isClient && !pos.equals(this.lastNetherPortalPosition)) {
            this.lastNetherPortalPosition = pos.toImmutable();
         }

         this.inNetherPortal = true;
      }
   }

   protected void tickPortal() {
      if (this.world instanceof ServerWorld) {
         int i = this.getMaxNetherPortalTime();
         ServerWorld lv = (ServerWorld)this.world;
         if (this.inNetherPortal) {
            MinecraftServer minecraftServer = lv.getServer();
            RegistryKey lv2 = this.world.getRegistryKey() == World.NETHER ? World.OVERWORLD : World.NETHER;
            ServerWorld lv3 = minecraftServer.getWorld(lv2);
            if (lv3 != null && minecraftServer.isNetherAllowed() && !this.hasVehicle() && this.netherPortalTime++ >= i) {
               this.world.getProfiler().push("portal");
               this.netherPortalTime = i;
               this.resetPortalCooldown();
               this.moveToWorld(lv3);
               this.world.getProfiler().pop();
            }

            this.inNetherPortal = false;
         } else {
            if (this.netherPortalTime > 0) {
               this.netherPortalTime -= 4;
            }

            if (this.netherPortalTime < 0) {
               this.netherPortalTime = 0;
            }
         }

         this.tickPortalCooldown();
      }
   }

   public int getDefaultPortalCooldown() {
      return 300;
   }

   public void setVelocityClient(double x, double y, double z) {
      this.setVelocity(x, y, z);
   }

   public void onDamaged(DamageSource damageSource) {
   }

   public void handleStatus(byte status) {
      switch (status) {
         case 53:
            HoneyBlock.addRegularParticles(this);
         default:
      }
   }

   public void animateDamage(float yaw) {
   }

   public Iterable getHandItems() {
      return EMPTY_STACK_LIST;
   }

   public Iterable getArmorItems() {
      return EMPTY_STACK_LIST;
   }

   public Iterable getItemsEquipped() {
      return Iterables.concat(this.getHandItems(), this.getArmorItems());
   }

   public void equipStack(EquipmentSlot slot, ItemStack stack) {
   }

   public boolean isOnFire() {
      boolean bl = this.world != null && this.world.isClient;
      return !this.isFireImmune() && (this.fireTicks > 0 || bl && this.getFlag(ON_FIRE_FLAG_INDEX));
   }

   public boolean hasVehicle() {
      return this.getVehicle() != null;
   }

   public boolean hasPassengers() {
      return !this.passengerList.isEmpty();
   }

   public boolean shouldDismountUnderwater() {
      return this.getType().isIn(EntityTypeTags.DISMOUNTS_UNDERWATER);
   }

   public void setSneaking(boolean sneaking) {
      this.setFlag(SNEAKING_FLAG_INDEX, sneaking);
   }

   public boolean isSneaking() {
      return this.getFlag(SNEAKING_FLAG_INDEX);
   }

   public boolean bypassesSteppingEffects() {
      return this.isSneaking();
   }

   public boolean bypassesLandingEffects() {
      return this.isSneaking();
   }

   public boolean isSneaky() {
      return this.isSneaking();
   }

   public boolean isDescending() {
      return this.isSneaking();
   }

   public boolean isInSneakingPose() {
      return this.isInPose(EntityPose.CROUCHING);
   }

   public boolean isSprinting() {
      return this.getFlag(SPRINTING_FLAG_INDEX);
   }

   public void setSprinting(boolean sprinting) {
      this.setFlag(SPRINTING_FLAG_INDEX, sprinting);
   }

   public boolean isSwimming() {
      return this.getFlag(SWIMMING_FLAG_INDEX);
   }

   public boolean isInSwimmingPose() {
      return this.isInPose(EntityPose.SWIMMING);
   }

   public boolean isCrawling() {
      return this.isInSwimmingPose() && !this.isTouchingWater();
   }

   public void setSwimming(boolean swimming) {
      this.setFlag(SWIMMING_FLAG_INDEX, swimming);
   }

   public final boolean isGlowingLocal() {
      return this.glowing;
   }

   public final void setGlowing(boolean glowing) {
      this.glowing = glowing;
      this.setFlag(GLOWING_FLAG_INDEX, this.isGlowing());
   }

   public boolean isGlowing() {
      return this.world.isClient() ? this.getFlag(GLOWING_FLAG_INDEX) : this.glowing;
   }

   public boolean isInvisible() {
      return this.getFlag(INVISIBLE_FLAG_INDEX);
   }

   public boolean isInvisibleTo(PlayerEntity player) {
      if (player.isSpectator()) {
         return false;
      } else {
         AbstractTeam lv = this.getScoreboardTeam();
         return lv != null && player != null && player.getScoreboardTeam() == lv && lv.shouldShowFriendlyInvisibles() ? false : this.isInvisible();
      }
   }

   public void updateEventHandler(BiConsumer callback) {
   }

   @Nullable
   public AbstractTeam getScoreboardTeam() {
      return this.world.getScoreboard().getPlayerTeam(this.getEntityName());
   }

   public boolean isTeammate(Entity other) {
      return this.isTeamPlayer(other.getScoreboardTeam());
   }

   public boolean isTeamPlayer(AbstractTeam team) {
      return this.getScoreboardTeam() != null ? this.getScoreboardTeam().isEqual(team) : false;
   }

   public void setInvisible(boolean invisible) {
      this.setFlag(INVISIBLE_FLAG_INDEX, invisible);
   }

   protected boolean getFlag(int index) {
      return ((Byte)this.dataTracker.get(FLAGS) & 1 << index) != 0;
   }

   protected void setFlag(int index, boolean value) {
      byte b = (Byte)this.dataTracker.get(FLAGS);
      if (value) {
         this.dataTracker.set(FLAGS, (byte)(b | 1 << index));
      } else {
         this.dataTracker.set(FLAGS, (byte)(b & ~(1 << index)));
      }

   }

   public int getMaxAir() {
      return 300;
   }

   public int getAir() {
      return (Integer)this.dataTracker.get(AIR);
   }

   public void setAir(int air) {
      this.dataTracker.set(AIR, air);
   }

   public int getFrozenTicks() {
      return (Integer)this.dataTracker.get(FROZEN_TICKS);
   }

   public void setFrozenTicks(int frozenTicks) {
      this.dataTracker.set(FROZEN_TICKS, frozenTicks);
   }

   public float getFreezingScale() {
      int i = this.getMinFreezeDamageTicks();
      return (float)Math.min(this.getFrozenTicks(), i) / (float)i;
   }

   public boolean isFrozen() {
      return this.getFrozenTicks() >= this.getMinFreezeDamageTicks();
   }

   public int getMinFreezeDamageTicks() {
      return 140;
   }

   public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
      this.setFireTicks(this.fireTicks + 1);
      if (this.fireTicks == 0) {
         this.setOnFireFor(8);
      }

      this.damage(this.getDamageSources().lightningBolt(), 5.0F);
   }

   public void onBubbleColumnSurfaceCollision(boolean drag) {
      Vec3d lv = this.getVelocity();
      double d;
      if (drag) {
         d = Math.max(-0.9, lv.y - 0.03);
      } else {
         d = Math.min(1.8, lv.y + 0.1);
      }

      this.setVelocity(lv.x, d, lv.z);
   }

   public void onBubbleColumnCollision(boolean drag) {
      Vec3d lv = this.getVelocity();
      double d;
      if (drag) {
         d = Math.max(-0.3, lv.y - 0.03);
      } else {
         d = Math.min(0.7, lv.y + 0.06);
      }

      this.setVelocity(lv.x, d, lv.z);
      this.onLanding();
   }

   public boolean onKilledOther(ServerWorld world, LivingEntity other) {
      return true;
   }

   public void limitFallDistance() {
      if (this.getVelocity().getY() > -0.5 && this.fallDistance > 1.0F) {
         this.fallDistance = 1.0F;
      }

   }

   public void onLanding() {
      this.fallDistance = 0.0F;
   }

   protected void pushOutOfBlocks(double x, double y, double z) {
      BlockPos lv = BlockPos.ofFloored(x, y, z);
      Vec3d lv2 = new Vec3d(x - (double)lv.getX(), y - (double)lv.getY(), z - (double)lv.getZ());
      BlockPos.Mutable lv3 = new BlockPos.Mutable();
      Direction lv4 = Direction.UP;
      double g = Double.MAX_VALUE;
      Direction[] var13 = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP};
      int var14 = var13.length;

      for(int var15 = 0; var15 < var14; ++var15) {
         Direction lv5 = var13[var15];
         lv3.set(lv, (Direction)lv5);
         if (!this.world.getBlockState(lv3).isFullCube(this.world, lv3)) {
            double h = lv2.getComponentAlongAxis(lv5.getAxis());
            double i = lv5.getDirection() == Direction.AxisDirection.POSITIVE ? 1.0 - h : h;
            if (i < g) {
               g = i;
               lv4 = lv5;
            }
         }
      }

      float j = this.random.nextFloat() * 0.2F + 0.1F;
      float k = (float)lv4.getDirection().offset();
      Vec3d lv6 = this.getVelocity().multiply(0.75);
      if (lv4.getAxis() == Direction.Axis.X) {
         this.setVelocity((double)(k * j), lv6.y, lv6.z);
      } else if (lv4.getAxis() == Direction.Axis.Y) {
         this.setVelocity(lv6.x, (double)(k * j), lv6.z);
      } else if (lv4.getAxis() == Direction.Axis.Z) {
         this.setVelocity(lv6.x, lv6.y, (double)(k * j));
      }

   }

   public void slowMovement(BlockState state, Vec3d multiplier) {
      this.onLanding();
      this.movementMultiplier = multiplier;
   }

   private static Text removeClickEvents(Text textComponent) {
      MutableText lv = textComponent.copyContentOnly().setStyle(textComponent.getStyle().withClickEvent((ClickEvent)null));
      Iterator var2 = textComponent.getSiblings().iterator();

      while(var2.hasNext()) {
         Text lv2 = (Text)var2.next();
         lv.append(removeClickEvents(lv2));
      }

      return lv;
   }

   public Text getName() {
      Text lv = this.getCustomName();
      return lv != null ? removeClickEvents(lv) : this.getDefaultName();
   }

   protected Text getDefaultName() {
      return this.type.getName();
   }

   public boolean isPartOf(Entity entity) {
      return this == entity;
   }

   public float getHeadYaw() {
      return 0.0F;
   }

   public void setHeadYaw(float headYaw) {
   }

   public void setBodyYaw(float bodyYaw) {
   }

   public boolean isAttackable() {
      return true;
   }

   public boolean handleAttack(Entity attacker) {
      return false;
   }

   public String toString() {
      String string = this.world == null ? "~NULL~" : this.world.toString();
      return this.removalReason != null ? String.format(Locale.ROOT, "%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f, removed=%s]", this.getClass().getSimpleName(), this.getName().getString(), this.id, string, this.getX(), this.getY(), this.getZ(), this.removalReason) : String.format(Locale.ROOT, "%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f]", this.getClass().getSimpleName(), this.getName().getString(), this.id, string, this.getX(), this.getY(), this.getZ());
   }

   public boolean isInvulnerableTo(DamageSource damageSource) {
      return this.isRemoved() || this.invulnerable && !damageSource.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) && !damageSource.isSourceCreativePlayer() || damageSource.isIn(DamageTypeTags.IS_FIRE) && this.isFireImmune() || damageSource.isIn(DamageTypeTags.IS_FALL) && this.getType().isIn(EntityTypeTags.FALL_DAMAGE_IMMUNE);
   }

   public boolean isInvulnerable() {
      return this.invulnerable;
   }

   public void setInvulnerable(boolean invulnerable) {
      this.invulnerable = invulnerable;
   }

   public void copyPositionAndRotation(Entity entity) {
      this.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
   }

   public void copyFrom(Entity original) {
      NbtCompound lv = original.writeNbt(new NbtCompound());
      lv.remove("Dimension");
      this.readNbt(lv);
      this.portalCooldown = original.portalCooldown;
      this.lastNetherPortalPosition = original.lastNetherPortalPosition;
   }

   @Nullable
   public Entity moveToWorld(ServerWorld destination) {
      if (this.world instanceof ServerWorld && !this.isRemoved()) {
         this.world.getProfiler().push("changeDimension");
         this.detach();
         this.world.getProfiler().push("reposition");
         TeleportTarget lv = this.getTeleportTarget(destination);
         if (lv == null) {
            return null;
         } else {
            this.world.getProfiler().swap("reloading");
            Entity lv2 = this.getType().create(destination);
            if (lv2 != null) {
               lv2.copyFrom(this);
               lv2.refreshPositionAndAngles(lv.position.x, lv.position.y, lv.position.z, lv.yaw, lv2.getPitch());
               lv2.setVelocity(lv.velocity);
               destination.onDimensionChanged(lv2);
               if (destination.getRegistryKey() == World.END) {
                  ServerWorld.createEndSpawnPlatform(destination);
               }
            }

            this.removeFromDimension();
            this.world.getProfiler().pop();
            ((ServerWorld)this.world).resetIdleTimeout();
            destination.resetIdleTimeout();
            this.world.getProfiler().pop();
            return lv2;
         }
      } else {
         return null;
      }
   }

   protected void removeFromDimension() {
      this.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
   }

   @Nullable
   protected TeleportTarget getTeleportTarget(ServerWorld destination) {
      boolean bl = this.world.getRegistryKey() == World.END && destination.getRegistryKey() == World.OVERWORLD;
      boolean bl2 = destination.getRegistryKey() == World.END;
      if (!bl && !bl2) {
         boolean bl3 = destination.getRegistryKey() == World.NETHER;
         if (this.world.getRegistryKey() != World.NETHER && !bl3) {
            return null;
         } else {
            WorldBorder lv2 = destination.getWorldBorder();
            double d = DimensionType.getCoordinateScaleFactor(this.world.getDimension(), destination.getDimension());
            BlockPos lv3 = lv2.clamp(this.getX() * d, this.getY(), this.getZ() * d);
            return (TeleportTarget)this.getPortalRect(destination, lv3, bl3, lv2).map((rect) -> {
               BlockState lv = this.world.getBlockState(this.lastNetherPortalPosition);
               Direction.Axis lv2;
               Vec3d lv4;
               if (lv.contains(Properties.HORIZONTAL_AXIS)) {
                  lv2 = (Direction.Axis)lv.get(Properties.HORIZONTAL_AXIS);
                  BlockLocating.Rectangle lv3 = BlockLocating.getLargestRectangle(this.lastNetherPortalPosition, lv2, 21, Direction.Axis.Y, 21, (pos) -> {
                     return this.world.getBlockState(pos) == lv;
                  });
                  lv4 = this.positionInPortal(lv2, lv3);
               } else {
                  lv2 = Direction.Axis.X;
                  lv4 = new Vec3d(0.5, 0.0, 0.0);
               }

               return NetherPortal.getNetherTeleportTarget(destination, rect, lv2, lv4, this, this.getVelocity(), this.getYaw(), this.getPitch());
            }).orElse((Object)null);
         }
      } else {
         BlockPos lv;
         if (bl2) {
            lv = ServerWorld.END_SPAWN_POS;
         } else {
            lv = destination.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, destination.getSpawnPos());
         }

         return new TeleportTarget(new Vec3d((double)lv.getX() + 0.5, (double)lv.getY(), (double)lv.getZ() + 0.5), this.getVelocity(), this.getYaw(), this.getPitch());
      }
   }

   protected Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect) {
      return NetherPortal.entityPosInPortal(portalRect, portalAxis, this.getPos(), this.getDimensions(this.getPose()));
   }

   protected Optional getPortalRect(ServerWorld destWorld, BlockPos destPos, boolean destIsNether, WorldBorder worldBorder) {
      return destWorld.getPortalForcer().getPortalRect(destPos, destIsNether, worldBorder);
   }

   public boolean canUsePortals() {
      return !this.hasVehicle() && !this.hasPassengers();
   }

   public float getEffectiveExplosionResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState, float max) {
      return max;
   }

   public boolean canExplosionDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float explosionPower) {
      return true;
   }

   public int getSafeFallDistance() {
      return 3;
   }

   public boolean canAvoidTraps() {
      return false;
   }

   public void populateCrashReport(CrashReportSection section) {
      section.add("Entity Type", () -> {
         Identifier var10000 = EntityType.getId(this.getType());
         return "" + var10000 + " (" + this.getClass().getCanonicalName() + ")";
      });
      section.add("Entity ID", (Object)this.id);
      section.add("Entity Name", () -> {
         return this.getName().getString();
      });
      section.add("Entity's Exact location", (Object)String.format(Locale.ROOT, "%.2f, %.2f, %.2f", this.getX(), this.getY(), this.getZ()));
      section.add("Entity's Block location", (Object)CrashReportSection.createPositionString(this.world, MathHelper.floor(this.getX()), MathHelper.floor(this.getY()), MathHelper.floor(this.getZ())));
      Vec3d lv = this.getVelocity();
      section.add("Entity's Momentum", (Object)String.format(Locale.ROOT, "%.2f, %.2f, %.2f", lv.x, lv.y, lv.z));
      section.add("Entity's Passengers", () -> {
         return this.getPassengerList().toString();
      });
      section.add("Entity's Vehicle", () -> {
         return String.valueOf(this.getVehicle());
      });
   }

   public boolean doesRenderOnFire() {
      return this.isOnFire() && !this.isSpectator();
   }

   public void setUuid(UUID uuid) {
      this.uuid = uuid;
      this.uuidString = this.uuid.toString();
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public String getUuidAsString() {
      return this.uuidString;
   }

   public String getEntityName() {
      return this.uuidString;
   }

   public boolean isPushedByFluids() {
      return true;
   }

   public static double getRenderDistanceMultiplier() {
      return renderDistanceMultiplier;
   }

   public static void setRenderDistanceMultiplier(double value) {
      renderDistanceMultiplier = value;
   }

   public Text getDisplayName() {
      return Team.decorateName(this.getScoreboardTeam(), this.getName()).styled((style) -> {
         return style.withHoverEvent(this.getHoverEvent()).withInsertion(this.getUuidAsString());
      });
   }

   public void setCustomName(@Nullable Text name) {
      this.dataTracker.set(CUSTOM_NAME, Optional.ofNullable(name));
   }

   @Nullable
   public Text getCustomName() {
      return (Text)((Optional)this.dataTracker.get(CUSTOM_NAME)).orElse((Object)null);
   }

   public boolean hasCustomName() {
      return ((Optional)this.dataTracker.get(CUSTOM_NAME)).isPresent();
   }

   public void setCustomNameVisible(boolean visible) {
      this.dataTracker.set(NAME_VISIBLE, visible);
   }

   public boolean isCustomNameVisible() {
      return (Boolean)this.dataTracker.get(NAME_VISIBLE);
   }

   public final void teleport(double destX, double destY, double destZ) {
      if (this.world instanceof ServerWorld) {
         ChunkPos lv = new ChunkPos(BlockPos.ofFloored(destX, destY, destZ));
         ((ServerWorld)this.world).getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, lv, 0, this.getId());
         this.world.getChunk(lv.x, lv.z);
         this.requestTeleport(destX, destY, destZ);
      }
   }

   public boolean teleport(ServerWorld world, double destX, double destY, double destZ, Set flags, float yaw, float pitch) {
      float i = MathHelper.clamp(pitch, -90.0F, 90.0F);
      if (world == this.world) {
         this.refreshPositionAndAngles(destX, destY, destZ, yaw, i);
         this.teleportPassengers();
         this.setHeadYaw(yaw);
      } else {
         this.detach();
         Entity lv = this.getType().create(world);
         if (lv == null) {
            return false;
         }

         lv.copyFrom(this);
         lv.refreshPositionAndAngles(destX, destY, destZ, yaw, i);
         lv.setHeadYaw(yaw);
         this.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
         world.onDimensionChanged(lv);
      }

      return true;
   }

   public void requestTeleportAndDismount(double destX, double destY, double destZ) {
      this.requestTeleport(destX, destY, destZ);
   }

   public void requestTeleport(double destX, double destY, double destZ) {
      if (this.world instanceof ServerWorld) {
         this.refreshPositionAndAngles(destX, destY, destZ, this.getYaw(), this.getPitch());
         this.teleportPassengers();
      }
   }

   private void teleportPassengers() {
      this.streamSelfAndPassengers().forEach((entity) -> {
         UnmodifiableIterator var1 = entity.passengerList.iterator();

         while(var1.hasNext()) {
            Entity lv = (Entity)var1.next();
            entity.updatePassengerPosition(lv, Entity::refreshPositionAfterTeleport);
         }

      });
   }

   public void requestTeleportOffset(double offsetX, double offsetY, double offsetZ) {
      this.requestTeleport(this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ);
   }

   public boolean shouldRenderName() {
      return this.isCustomNameVisible();
   }

   public void onDataTrackerUpdate(List dataEntries) {
   }

   public void onTrackedDataSet(TrackedData data) {
      if (POSE.equals(data)) {
         this.calculateDimensions();
      }

   }

   /** @deprecated */
   @Deprecated
   protected void reinitDimensions() {
      EntityPose lv = this.getPose();
      EntityDimensions lv2 = this.getDimensions(lv);
      this.dimensions = lv2;
      this.standingEyeHeight = this.getEyeHeight(lv, lv2);
   }

   public void calculateDimensions() {
      EntityDimensions lv = this.dimensions;
      EntityPose lv2 = this.getPose();
      EntityDimensions lv3 = this.getDimensions(lv2);
      this.dimensions = lv3;
      this.standingEyeHeight = this.getEyeHeight(lv2, lv3);
      this.refreshPosition();
      boolean bl = (double)lv3.width <= 4.0 && (double)lv3.height <= 4.0;
      if (!this.world.isClient && !this.firstUpdate && !this.noClip && bl && (lv3.width > lv.width || lv3.height > lv.height) && !(this instanceof PlayerEntity)) {
         Vec3d lv4 = this.getPos().add(0.0, (double)lv.height / 2.0, 0.0);
         double d = (double)Math.max(0.0F, lv3.width - lv.width) + 1.0E-6;
         double e = (double)Math.max(0.0F, lv3.height - lv.height) + 1.0E-6;
         VoxelShape lv5 = VoxelShapes.cuboid(Box.of(lv4, d, e, d));
         this.world.findClosestCollision(this, lv5, lv4, (double)lv3.width, (double)lv3.height, (double)lv3.width).ifPresent((pos) -> {
            this.setPosition(pos.add(0.0, (double)(-lv3.height) / 2.0, 0.0));
         });
      }

   }

   public Direction getHorizontalFacing() {
      return Direction.fromRotation((double)this.getYaw());
   }

   public Direction getMovementDirection() {
      return this.getHorizontalFacing();
   }

   protected HoverEvent getHoverEvent() {
      return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new HoverEvent.EntityContent(this.getType(), this.getUuid(), this.getName()));
   }

   public boolean canBeSpectated(ServerPlayerEntity spectator) {
      return true;
   }

   public final Box getBoundingBox() {
      return this.boundingBox;
   }

   public Box getVisibilityBoundingBox() {
      return this.getBoundingBox();
   }

   protected Box calculateBoundsForPose(EntityPose pos) {
      EntityDimensions lv = this.getDimensions(pos);
      float f = lv.width / 2.0F;
      Vec3d lv2 = new Vec3d(this.getX() - (double)f, this.getY(), this.getZ() - (double)f);
      Vec3d lv3 = new Vec3d(this.getX() + (double)f, this.getY() + (double)lv.height, this.getZ() + (double)f);
      return new Box(lv2, lv3);
   }

   public final void setBoundingBox(Box boundingBox) {
      this.boundingBox = boundingBox;
   }

   protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return dimensions.height * 0.85F;
   }

   public float getEyeHeight(EntityPose pose) {
      return this.getEyeHeight(pose, this.getDimensions(pose));
   }

   public final float getStandingEyeHeight() {
      return this.standingEyeHeight;
   }

   public Vec3d getLeashOffset(float tickDelta) {
      return this.getLeashOffset();
   }

   protected Vec3d getLeashOffset() {
      return new Vec3d(0.0, (double)this.getStandingEyeHeight(), (double)(this.getWidth() * 0.4F));
   }

   public StackReference getStackReference(int mappedIndex) {
      return StackReference.EMPTY;
   }

   public void sendMessage(Text message) {
   }

   public World getEntityWorld() {
      return this.world;
   }

   @Nullable
   public MinecraftServer getServer() {
      return this.world.getServer();
   }

   public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
      return ActionResult.PASS;
   }

   public boolean isImmuneToExplosion() {
      return false;
   }

   public void applyDamageEffects(LivingEntity attacker, Entity target) {
      if (target instanceof LivingEntity) {
         EnchantmentHelper.onUserDamaged((LivingEntity)target, attacker);
      }

      EnchantmentHelper.onTargetDamaged(attacker, target);
   }

   public void onStartedTrackingBy(ServerPlayerEntity player) {
   }

   public void onStoppedTrackingBy(ServerPlayerEntity player) {
   }

   public float applyRotation(BlockRotation rotation) {
      float f = MathHelper.wrapDegrees(this.getYaw());
      switch (rotation) {
         case CLOCKWISE_180:
            return f + 180.0F;
         case COUNTERCLOCKWISE_90:
            return f + 270.0F;
         case CLOCKWISE_90:
            return f + 90.0F;
         default:
            return f;
      }
   }

   public float applyMirror(BlockMirror mirror) {
      float f = MathHelper.wrapDegrees(this.getYaw());
      switch (mirror) {
         case FRONT_BACK:
            return -f;
         case LEFT_RIGHT:
            return 180.0F - f;
         default:
            return f;
      }
   }

   public boolean entityDataRequiresOperator() {
      return false;
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      return null;
   }

   public final boolean hasControllingPassenger() {
      return this.getControllingPassenger() != null;
   }

   public final List getPassengerList() {
      return this.passengerList;
   }

   @Nullable
   public Entity getFirstPassenger() {
      return this.passengerList.isEmpty() ? null : (Entity)this.passengerList.get(0);
   }

   public boolean hasPassenger(Entity passenger) {
      return this.passengerList.contains(passenger);
   }

   public boolean hasPassenger(Predicate predicate) {
      UnmodifiableIterator var2 = this.passengerList.iterator();

      Entity lv;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         lv = (Entity)var2.next();
      } while(!predicate.test(lv));

      return true;
   }

   private Stream streamIntoPassengers() {
      return this.passengerList.stream().flatMap(Entity::streamSelfAndPassengers);
   }

   public Stream streamSelfAndPassengers() {
      return Stream.concat(Stream.of(this), this.streamIntoPassengers());
   }

   public Stream streamPassengersAndSelf() {
      return Stream.concat(this.passengerList.stream().flatMap(Entity::streamPassengersAndSelf), Stream.of(this));
   }

   public Iterable getPassengersDeep() {
      return () -> {
         return this.streamIntoPassengers().iterator();
      };
   }

   public boolean hasPlayerRider() {
      return this.streamIntoPassengers().filter((entity) -> {
         return entity instanceof PlayerEntity;
      }).count() == 1L;
   }

   public Entity getRootVehicle() {
      Entity lv;
      for(lv = this; lv.hasVehicle(); lv = lv.getVehicle()) {
      }

      return lv;
   }

   public boolean isConnectedThroughVehicle(Entity entity) {
      return this.getRootVehicle() == entity.getRootVehicle();
   }

   public boolean hasPassengerDeep(Entity passenger) {
      if (!passenger.hasVehicle()) {
         return false;
      } else {
         Entity lv = passenger.getVehicle();
         return lv == this ? true : this.hasPassengerDeep(lv);
      }
   }

   public boolean isLogicalSideForUpdatingMovement() {
      LivingEntity var2 = this.getControllingPassenger();
      if (var2 instanceof PlayerEntity lv) {
         return lv.isMainPlayer();
      } else {
         return this.canMoveVoluntarily();
      }
   }

   public boolean canMoveVoluntarily() {
      return !this.world.isClient;
   }

   protected static Vec3d getPassengerDismountOffset(double vehicleWidth, double passengerWidth, float passengerYaw) {
      double g = (vehicleWidth + passengerWidth + 9.999999747378752E-6) / 2.0;
      float h = -MathHelper.sin(passengerYaw * 0.017453292F);
      float i = MathHelper.cos(passengerYaw * 0.017453292F);
      float j = Math.max(Math.abs(h), Math.abs(i));
      return new Vec3d((double)h * g / (double)j, 0.0, (double)i * g / (double)j);
   }

   public Vec3d updatePassengerForDismount(LivingEntity passenger) {
      return new Vec3d(this.getX(), this.getBoundingBox().maxY, this.getZ());
   }

   @Nullable
   public Entity getVehicle() {
      return this.vehicle;
   }

   @Nullable
   public Entity getControllingVehicle() {
      return this.vehicle != null && this.vehicle.getControllingPassenger() == this ? this.vehicle : null;
   }

   public PistonBehavior getPistonBehavior() {
      return PistonBehavior.NORMAL;
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.NEUTRAL;
   }

   protected int getBurningDuration() {
      return 1;
   }

   public ServerCommandSource getCommandSource() {
      return new ServerCommandSource(this, this.getPos(), this.getRotationClient(), this.world instanceof ServerWorld ? (ServerWorld)this.world : null, this.getPermissionLevel(), this.getName().getString(), this.getDisplayName(), this.world.getServer(), this);
   }

   protected int getPermissionLevel() {
      return 0;
   }

   public boolean hasPermissionLevel(int permissionLevel) {
      return this.getPermissionLevel() >= permissionLevel;
   }

   public boolean shouldReceiveFeedback() {
      return this.world.getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK);
   }

   public boolean shouldTrackOutput() {
      return true;
   }

   public boolean shouldBroadcastConsoleToOps() {
      return true;
   }

   public void lookAt(EntityAnchorArgumentType.EntityAnchor anchorPoint, Vec3d target) {
      Vec3d lv = anchorPoint.positionAt(this);
      double d = target.x - lv.x;
      double e = target.y - lv.y;
      double f = target.z - lv.z;
      double g = Math.sqrt(d * d + f * f);
      this.setPitch(MathHelper.wrapDegrees((float)(-(MathHelper.atan2(e, g) * 57.2957763671875))));
      this.setYaw(MathHelper.wrapDegrees((float)(MathHelper.atan2(f, d) * 57.2957763671875) - 90.0F));
      this.setHeadYaw(this.getYaw());
      this.prevPitch = this.getPitch();
      this.prevYaw = this.getYaw();
   }

   public boolean updateMovementInFluid(TagKey tag, double speed) {
      if (this.isRegionUnloaded()) {
         return false;
      } else {
         Box lv = this.getBoundingBox().contract(0.001);
         int i = MathHelper.floor(lv.minX);
         int j = MathHelper.ceil(lv.maxX);
         int k = MathHelper.floor(lv.minY);
         int l = MathHelper.ceil(lv.maxY);
         int m = MathHelper.floor(lv.minZ);
         int n = MathHelper.ceil(lv.maxZ);
         double e = 0.0;
         boolean bl = this.isPushedByFluids();
         boolean bl2 = false;
         Vec3d lv2 = Vec3d.ZERO;
         int o = 0;
         BlockPos.Mutable lv3 = new BlockPos.Mutable();

         for(int p = i; p < j; ++p) {
            for(int q = k; q < l; ++q) {
               for(int r = m; r < n; ++r) {
                  lv3.set(p, q, r);
                  FluidState lv4 = this.world.getFluidState(lv3);
                  if (lv4.isIn(tag)) {
                     double f = (double)((float)q + lv4.getHeight(this.world, lv3));
                     if (f >= lv.minY) {
                        bl2 = true;
                        e = Math.max(f - lv.minY, e);
                        if (bl) {
                           Vec3d lv5 = lv4.getVelocity(this.world, lv3);
                           if (e < 0.4) {
                              lv5 = lv5.multiply(e);
                           }

                           lv2 = lv2.add(lv5);
                           ++o;
                        }
                     }
                  }
               }
            }
         }

         if (lv2.length() > 0.0) {
            if (o > 0) {
               lv2 = lv2.multiply(1.0 / (double)o);
            }

            if (!(this instanceof PlayerEntity)) {
               lv2 = lv2.normalize();
            }

            Vec3d lv6 = this.getVelocity();
            lv2 = lv2.multiply(speed * 1.0);
            double g = 0.003;
            if (Math.abs(lv6.x) < 0.003 && Math.abs(lv6.z) < 0.003 && lv2.length() < 0.0045000000000000005) {
               lv2 = lv2.normalize().multiply(0.0045000000000000005);
            }

            this.setVelocity(this.getVelocity().add(lv2));
         }

         this.fluidHeight.put(tag, e);
         return bl2;
      }
   }

   public boolean isRegionUnloaded() {
      Box lv = this.getBoundingBox().expand(1.0);
      int i = MathHelper.floor(lv.minX);
      int j = MathHelper.ceil(lv.maxX);
      int k = MathHelper.floor(lv.minZ);
      int l = MathHelper.ceil(lv.maxZ);
      return !this.world.isRegionLoaded(i, k, j, l);
   }

   public double getFluidHeight(TagKey fluid) {
      return this.fluidHeight.getDouble(fluid);
   }

   public double getSwimHeight() {
      return (double)this.getStandingEyeHeight() < 0.4 ? 0.0 : 0.4;
   }

   public final float getWidth() {
      return this.dimensions.width;
   }

   public final float getHeight() {
      return this.dimensions.height;
   }

   public float getNameLabelHeight() {
      return this.getHeight() + 0.5F;
   }

   public Packet createSpawnPacket() {
      return new EntitySpawnS2CPacket(this);
   }

   public EntityDimensions getDimensions(EntityPose pose) {
      return this.type.getDimensions();
   }

   public Vec3d getPos() {
      return this.pos;
   }

   public Vec3d getSyncedPos() {
      return this.getPos();
   }

   public BlockPos getBlockPos() {
      return this.blockPos;
   }

   public BlockState getBlockStateAtPos() {
      if (this.blockStateAtPos == null) {
         this.blockStateAtPos = this.world.getBlockState(this.getBlockPos());
      }

      return this.blockStateAtPos;
   }

   public ChunkPos getChunkPos() {
      return this.chunkPos;
   }

   public Vec3d getVelocity() {
      return this.velocity;
   }

   public void setVelocity(Vec3d velocity) {
      this.velocity = velocity;
   }

   public void addVelocity(Vec3d velocity) {
      this.setVelocity(this.getVelocity().add(velocity));
   }

   public void setVelocity(double x, double y, double z) {
      this.setVelocity(new Vec3d(x, y, z));
   }

   public final int getBlockX() {
      return this.blockPos.getX();
   }

   public final double getX() {
      return this.pos.x;
   }

   public double offsetX(double widthScale) {
      return this.pos.x + (double)this.getWidth() * widthScale;
   }

   public double getParticleX(double widthScale) {
      return this.offsetX((2.0 * this.random.nextDouble() - 1.0) * widthScale);
   }

   public final int getBlockY() {
      return this.blockPos.getY();
   }

   public final double getY() {
      return this.pos.y;
   }

   public double getBodyY(double heightScale) {
      return this.pos.y + (double)this.getHeight() * heightScale;
   }

   public double getRandomBodyY() {
      return this.getBodyY(this.random.nextDouble());
   }

   public double getEyeY() {
      return this.pos.y + (double)this.standingEyeHeight;
   }

   public final int getBlockZ() {
      return this.blockPos.getZ();
   }

   public final double getZ() {
      return this.pos.z;
   }

   public double offsetZ(double widthScale) {
      return this.pos.z + (double)this.getWidth() * widthScale;
   }

   public double getParticleZ(double widthScale) {
      return this.offsetZ((2.0 * this.random.nextDouble() - 1.0) * widthScale);
   }

   public final void setPos(double x, double y, double z) {
      if (this.pos.x != x || this.pos.y != y || this.pos.z != z) {
         this.pos = new Vec3d(x, y, z);
         int i = MathHelper.floor(x);
         int j = MathHelper.floor(y);
         int k = MathHelper.floor(z);
         if (i != this.blockPos.getX() || j != this.blockPos.getY() || k != this.blockPos.getZ()) {
            this.blockPos = new BlockPos(i, j, k);
            this.blockStateAtPos = null;
            if (ChunkSectionPos.getSectionCoord(i) != this.chunkPos.x || ChunkSectionPos.getSectionCoord(k) != this.chunkPos.z) {
               this.chunkPos = new ChunkPos(this.blockPos);
            }
         }

         this.changeListener.updateEntityPosition();
      }

   }

   public void checkDespawn() {
   }

   public Vec3d getLeashPos(float delta) {
      return this.getLerpedPos(delta).add(0.0, (double)this.standingEyeHeight * 0.7, 0.0);
   }

   public void onSpawnPacket(EntitySpawnS2CPacket packet) {
      int i = packet.getId();
      double d = packet.getX();
      double e = packet.getY();
      double f = packet.getZ();
      this.updateTrackedPosition(d, e, f);
      this.refreshPositionAfterTeleport(d, e, f);
      this.setPitch(packet.getPitch());
      this.setYaw(packet.getYaw());
      this.setId(i);
      this.setUuid(packet.getUuid());
   }

   @Nullable
   public ItemStack getPickBlockStack() {
      return null;
   }

   public void setInPowderSnow(boolean inPowderSnow) {
      this.inPowderSnow = inPowderSnow;
   }

   public boolean canFreeze() {
      return !this.getType().isIn(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES);
   }

   public boolean shouldEscapePowderSnow() {
      return (this.inPowderSnow || this.wasInPowderSnow) && this.canFreeze();
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getBodyYaw() {
      return this.getYaw();
   }

   public void setYaw(float yaw) {
      if (!Float.isFinite(yaw)) {
         Util.error("Invalid entity rotation: " + yaw + ", discarding.");
      } else {
         this.yaw = yaw;
      }
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setPitch(float pitch) {
      if (!Float.isFinite(pitch)) {
         Util.error("Invalid entity rotation: " + pitch + ", discarding.");
      } else {
         this.pitch = pitch;
      }
   }

   public boolean canSprintAsVehicle() {
      return false;
   }

   public float getStepHeight() {
      return this.stepHeight;
   }

   public void setStepHeight(float stepHeight) {
      this.stepHeight = stepHeight;
   }

   public final boolean isRemoved() {
      return this.removalReason != null;
   }

   @Nullable
   public RemovalReason getRemovalReason() {
      return this.removalReason;
   }

   public final void setRemoved(RemovalReason reason) {
      if (this.removalReason == null) {
         this.removalReason = reason;
      }

      if (this.removalReason.shouldDestroy()) {
         this.stopRiding();
      }

      this.getPassengerList().forEach(Entity::stopRiding);
      this.changeListener.remove(reason);
   }

   protected void unsetRemoved() {
      this.removalReason = null;
   }

   public void setChangeListener(EntityChangeListener changeListener) {
      this.changeListener = changeListener;
   }

   public boolean shouldSave() {
      if (this.removalReason != null && !this.removalReason.shouldSave()) {
         return false;
      } else if (this.hasVehicle()) {
         return false;
      } else {
         return !this.hasPassengers() || !this.hasPlayerRider();
      }
   }

   public boolean isPlayer() {
      return false;
   }

   public boolean canModifyAt(World world, BlockPos pos) {
      return true;
   }

   public World getWorld() {
      return this.world;
   }

   public DamageSources getDamageSources() {
      return this.world.getDamageSources();
   }

   static {
      FLAGS = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.BYTE);
      AIR = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.INTEGER);
      CUSTOM_NAME = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.OPTIONAL_TEXT_COMPONENT);
      NAME_VISIBLE = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.BOOLEAN);
      SILENT = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.BOOLEAN);
      NO_GRAVITY = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.BOOLEAN);
      POSE = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.ENTITY_POSE);
      FROZEN_TICKS = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.INTEGER);
   }

   public static enum RemovalReason {
      KILLED(true, false),
      DISCARDED(true, false),
      UNLOADED_TO_CHUNK(false, true),
      UNLOADED_WITH_PLAYER(false, false),
      CHANGED_DIMENSION(false, false);

      private final boolean destroy;
      private final boolean save;

      private RemovalReason(boolean destroy, boolean save) {
         this.destroy = destroy;
         this.save = save;
      }

      public boolean shouldDestroy() {
         return this.destroy;
      }

      public boolean shouldSave() {
         return this.save;
      }

      // $FF: synthetic method
      private static RemovalReason[] method_36603() {
         return new RemovalReason[]{KILLED, DISCARDED, UNLOADED_TO_CHUNK, UNLOADED_WITH_PLAYER, CHANGED_DIMENSION};
      }
   }

   public static enum MoveEffect {
      NONE(false, false),
      SOUNDS(true, false),
      EVENTS(false, true),
      ALL(true, true);

      final boolean sounds;
      final boolean events;

      private MoveEffect(boolean sounds, boolean events) {
         this.sounds = sounds;
         this.events = events;
      }

      public boolean hasAny() {
         return this.events || this.sounds;
      }

      public boolean emitsGameEvents() {
         return this.events;
      }

      public boolean playsSounds() {
         return this.sounds;
      }

      // $FF: synthetic method
      private static MoveEffect[] method_36602() {
         return new MoveEffect[]{NONE, SOUNDS, EVENTS, ALL};
      }
   }

   @FunctionalInterface
   public interface PositionUpdater {
      void accept(Entity entity, double x, double y, double z);
   }
}
