package net.minecraft.entity.projectile;

import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class FishingBobberEntity extends ProjectileEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Random velocityRandom;
   private boolean caughtFish;
   private int outOfOpenWaterTicks;
   private static final int field_30665 = 10;
   private static final TrackedData HOOK_ENTITY_ID;
   private static final TrackedData CAUGHT_FISH;
   private int removalTimer;
   private int hookCountdown;
   private int waitCountdown;
   private int fishTravelCountdown;
   private float fishAngle;
   private boolean inOpenWater;
   @Nullable
   private Entity hookedEntity;
   private State state;
   private final int luckOfTheSeaLevel;
   private final int lureLevel;

   private FishingBobberEntity(EntityType type, World world, int luckOfTheSeaLevel, int lureLevel) {
      super(type, world);
      this.velocityRandom = Random.create();
      this.inOpenWater = true;
      this.state = FishingBobberEntity.State.FLYING;
      this.ignoreCameraFrustum = true;
      this.luckOfTheSeaLevel = Math.max(0, luckOfTheSeaLevel);
      this.lureLevel = Math.max(0, lureLevel);
   }

   public FishingBobberEntity(EntityType arg, World arg2) {
      this((EntityType)arg, arg2, 0, 0);
   }

   public FishingBobberEntity(PlayerEntity thrower, World world, int luckOfTheSeaLevel, int lureLevel) {
      this(EntityType.FISHING_BOBBER, world, luckOfTheSeaLevel, lureLevel);
      this.setOwner(thrower);
      float f = thrower.getPitch();
      float g = thrower.getYaw();
      float h = MathHelper.cos(-g * 0.017453292F - 3.1415927F);
      float k = MathHelper.sin(-g * 0.017453292F - 3.1415927F);
      float l = -MathHelper.cos(-f * 0.017453292F);
      float m = MathHelper.sin(-f * 0.017453292F);
      double d = thrower.getX() - (double)k * 0.3;
      double e = thrower.getEyeY();
      double n = thrower.getZ() - (double)h * 0.3;
      this.refreshPositionAndAngles(d, e, n, g, f);
      Vec3d lv = new Vec3d((double)(-k), (double)MathHelper.clamp(-(m / l), -5.0F, 5.0F), (double)(-h));
      double o = lv.length();
      lv = lv.multiply(0.6 / o + this.random.nextTriangular(0.5, 0.0103365), 0.6 / o + this.random.nextTriangular(0.5, 0.0103365), 0.6 / o + this.random.nextTriangular(0.5, 0.0103365));
      this.setVelocity(lv);
      this.setYaw((float)(MathHelper.atan2(lv.x, lv.z) * 57.2957763671875));
      this.setPitch((float)(MathHelper.atan2(lv.y, lv.horizontalLength()) * 57.2957763671875));
      this.prevYaw = this.getYaw();
      this.prevPitch = this.getPitch();
   }

   protected void initDataTracker() {
      this.getDataTracker().startTracking(HOOK_ENTITY_ID, 0);
      this.getDataTracker().startTracking(CAUGHT_FISH, false);
   }

   public void onTrackedDataSet(TrackedData data) {
      if (HOOK_ENTITY_ID.equals(data)) {
         int i = (Integer)this.getDataTracker().get(HOOK_ENTITY_ID);
         this.hookedEntity = i > 0 ? this.world.getEntityById(i - 1) : null;
      }

      if (CAUGHT_FISH.equals(data)) {
         this.caughtFish = (Boolean)this.getDataTracker().get(CAUGHT_FISH);
         if (this.caughtFish) {
            this.setVelocity(this.getVelocity().x, (double)(-0.4F * MathHelper.nextFloat(this.velocityRandom, 0.6F, 1.0F)), this.getVelocity().z);
         }
      }

      super.onTrackedDataSet(data);
   }

   public boolean shouldRender(double distance) {
      double e = 64.0;
      return distance < 4096.0;
   }

   public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
   }

   public void tick() {
      this.velocityRandom.setSeed(this.getUuid().getLeastSignificantBits() ^ this.world.getTime());
      super.tick();
      PlayerEntity lv = this.getPlayerOwner();
      if (lv == null) {
         this.discard();
      } else if (this.world.isClient || !this.removeIfInvalid(lv)) {
         if (this.onGround) {
            ++this.removalTimer;
            if (this.removalTimer >= 1200) {
               this.discard();
               return;
            }
         } else {
            this.removalTimer = 0;
         }

         float f = 0.0F;
         BlockPos lv2 = this.getBlockPos();
         FluidState lv3 = this.world.getFluidState(lv2);
         if (lv3.isIn(FluidTags.WATER)) {
            f = lv3.getHeight(this.world, lv2);
         }

         boolean bl = f > 0.0F;
         if (this.state == FishingBobberEntity.State.FLYING) {
            if (this.hookedEntity != null) {
               this.setVelocity(Vec3d.ZERO);
               this.state = FishingBobberEntity.State.HOOKED_IN_ENTITY;
               return;
            }

            if (bl) {
               this.setVelocity(this.getVelocity().multiply(0.3, 0.2, 0.3));
               this.state = FishingBobberEntity.State.BOBBING;
               return;
            }

            this.checkForCollision();
         } else {
            if (this.state == FishingBobberEntity.State.HOOKED_IN_ENTITY) {
               if (this.hookedEntity != null) {
                  if (!this.hookedEntity.isRemoved() && this.hookedEntity.world.getRegistryKey() == this.world.getRegistryKey()) {
                     this.setPosition(this.hookedEntity.getX(), this.hookedEntity.getBodyY(0.8), this.hookedEntity.getZ());
                  } else {
                     this.updateHookedEntityId((Entity)null);
                     this.state = FishingBobberEntity.State.FLYING;
                  }
               }

               return;
            }

            if (this.state == FishingBobberEntity.State.BOBBING) {
               Vec3d lv4 = this.getVelocity();
               double d = this.getY() + lv4.y - (double)lv2.getY() - (double)f;
               if (Math.abs(d) < 0.01) {
                  d += Math.signum(d) * 0.1;
               }

               this.setVelocity(lv4.x * 0.9, lv4.y - d * (double)this.random.nextFloat() * 0.2, lv4.z * 0.9);
               if (this.hookCountdown <= 0 && this.fishTravelCountdown <= 0) {
                  this.inOpenWater = true;
               } else {
                  this.inOpenWater = this.inOpenWater && this.outOfOpenWaterTicks < 10 && this.isOpenOrWaterAround(lv2);
               }

               if (bl) {
                  this.outOfOpenWaterTicks = Math.max(0, this.outOfOpenWaterTicks - 1);
                  if (this.caughtFish) {
                     this.setVelocity(this.getVelocity().add(0.0, -0.1 * (double)this.velocityRandom.nextFloat() * (double)this.velocityRandom.nextFloat(), 0.0));
                  }

                  if (!this.world.isClient) {
                     this.tickFishingLogic(lv2);
                  }
               } else {
                  this.outOfOpenWaterTicks = Math.min(10, this.outOfOpenWaterTicks + 1);
               }
            }
         }

         if (!lv3.isIn(FluidTags.WATER)) {
            this.setVelocity(this.getVelocity().add(0.0, -0.03, 0.0));
         }

         this.move(MovementType.SELF, this.getVelocity());
         this.updateRotation();
         if (this.state == FishingBobberEntity.State.FLYING && (this.onGround || this.horizontalCollision)) {
            this.setVelocity(Vec3d.ZERO);
         }

         double e = 0.92;
         this.setVelocity(this.getVelocity().multiply(0.92));
         this.refreshPosition();
      }
   }

   private boolean removeIfInvalid(PlayerEntity player) {
      ItemStack lv = player.getMainHandStack();
      ItemStack lv2 = player.getOffHandStack();
      boolean bl = lv.isOf(Items.FISHING_ROD);
      boolean bl2 = lv2.isOf(Items.FISHING_ROD);
      if (!player.isRemoved() && player.isAlive() && (bl || bl2) && !(this.squaredDistanceTo(player) > 1024.0)) {
         return false;
      } else {
         this.discard();
         return true;
      }
   }

   private void checkForCollision() {
      HitResult lv = ProjectileUtil.getCollision(this, this::canHit);
      this.onCollision(lv);
   }

   protected boolean canHit(Entity entity) {
      return super.canHit(entity) || entity.isAlive() && entity instanceof ItemEntity;
   }

   protected void onEntityHit(EntityHitResult entityHitResult) {
      super.onEntityHit(entityHitResult);
      if (!this.world.isClient) {
         this.updateHookedEntityId(entityHitResult.getEntity());
      }

   }

   protected void onBlockHit(BlockHitResult blockHitResult) {
      super.onBlockHit(blockHitResult);
      this.setVelocity(this.getVelocity().normalize().multiply(blockHitResult.squaredDistanceTo(this)));
   }

   private void updateHookedEntityId(@Nullable Entity entity) {
      this.hookedEntity = entity;
      this.getDataTracker().set(HOOK_ENTITY_ID, entity == null ? 0 : entity.getId() + 1);
   }

   private void tickFishingLogic(BlockPos pos) {
      ServerWorld lv = (ServerWorld)this.world;
      int i = 1;
      BlockPos lv2 = pos.up();
      if (this.random.nextFloat() < 0.25F && this.world.hasRain(lv2)) {
         ++i;
      }

      if (this.random.nextFloat() < 0.5F && !this.world.isSkyVisible(lv2)) {
         --i;
      }

      if (this.hookCountdown > 0) {
         --this.hookCountdown;
         if (this.hookCountdown <= 0) {
            this.waitCountdown = 0;
            this.fishTravelCountdown = 0;
            this.getDataTracker().set(CAUGHT_FISH, false);
         }
      } else {
         float f;
         float g;
         float h;
         double d;
         double e;
         double j;
         BlockState lv3;
         if (this.fishTravelCountdown > 0) {
            this.fishTravelCountdown -= i;
            if (this.fishTravelCountdown > 0) {
               this.fishAngle += (float)this.random.nextTriangular(0.0, 9.188);
               f = this.fishAngle * 0.017453292F;
               g = MathHelper.sin(f);
               h = MathHelper.cos(f);
               d = this.getX() + (double)(g * (float)this.fishTravelCountdown * 0.1F);
               e = (double)((float)MathHelper.floor(this.getY()) + 1.0F);
               j = this.getZ() + (double)(h * (float)this.fishTravelCountdown * 0.1F);
               lv3 = lv.getBlockState(BlockPos.ofFloored(d, e - 1.0, j));
               if (lv3.isOf(Blocks.WATER)) {
                  if (this.random.nextFloat() < 0.15F) {
                     lv.spawnParticles(ParticleTypes.BUBBLE, d, e - 0.10000000149011612, j, 1, (double)g, 0.1, (double)h, 0.0);
                  }

                  float k = g * 0.04F;
                  float l = h * 0.04F;
                  lv.spawnParticles(ParticleTypes.FISHING, d, e, j, 0, (double)l, 0.01, (double)(-k), 1.0);
                  lv.spawnParticles(ParticleTypes.FISHING, d, e, j, 0, (double)(-l), 0.01, (double)k, 1.0);
               }
            } else {
               this.playSound(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
               double m = this.getY() + 0.5;
               lv.spawnParticles(ParticleTypes.BUBBLE, this.getX(), m, this.getZ(), (int)(1.0F + this.getWidth() * 20.0F), (double)this.getWidth(), 0.0, (double)this.getWidth(), 0.20000000298023224);
               lv.spawnParticles(ParticleTypes.FISHING, this.getX(), m, this.getZ(), (int)(1.0F + this.getWidth() * 20.0F), (double)this.getWidth(), 0.0, (double)this.getWidth(), 0.20000000298023224);
               this.hookCountdown = MathHelper.nextInt(this.random, 20, 40);
               this.getDataTracker().set(CAUGHT_FISH, true);
            }
         } else if (this.waitCountdown > 0) {
            this.waitCountdown -= i;
            f = 0.15F;
            if (this.waitCountdown < 20) {
               f += (float)(20 - this.waitCountdown) * 0.05F;
            } else if (this.waitCountdown < 40) {
               f += (float)(40 - this.waitCountdown) * 0.02F;
            } else if (this.waitCountdown < 60) {
               f += (float)(60 - this.waitCountdown) * 0.01F;
            }

            if (this.random.nextFloat() < f) {
               g = MathHelper.nextFloat(this.random, 0.0F, 360.0F) * 0.017453292F;
               h = MathHelper.nextFloat(this.random, 25.0F, 60.0F);
               d = this.getX() + (double)(MathHelper.sin(g) * h) * 0.1;
               e = (double)((float)MathHelper.floor(this.getY()) + 1.0F);
               j = this.getZ() + (double)(MathHelper.cos(g) * h) * 0.1;
               lv3 = lv.getBlockState(BlockPos.ofFloored(d, e - 1.0, j));
               if (lv3.isOf(Blocks.WATER)) {
                  lv.spawnParticles(ParticleTypes.SPLASH, d, e, j, 2 + this.random.nextInt(2), 0.10000000149011612, 0.0, 0.10000000149011612, 0.0);
               }
            }

            if (this.waitCountdown <= 0) {
               this.fishAngle = MathHelper.nextFloat(this.random, 0.0F, 360.0F);
               this.fishTravelCountdown = MathHelper.nextInt(this.random, 20, 80);
            }
         } else {
            this.waitCountdown = MathHelper.nextInt(this.random, 100, 600);
            this.waitCountdown -= this.lureLevel * 20 * 5;
         }
      }

   }

   private boolean isOpenOrWaterAround(BlockPos pos) {
      PositionType lv = FishingBobberEntity.PositionType.INVALID;

      for(int i = -1; i <= 2; ++i) {
         PositionType lv2 = this.getPositionType(pos.add(-2, i, -2), pos.add(2, i, 2));
         switch (lv2) {
            case INVALID:
               return false;
            case ABOVE_WATER:
               if (lv == FishingBobberEntity.PositionType.INVALID) {
                  return false;
               }
               break;
            case INSIDE_WATER:
               if (lv == FishingBobberEntity.PositionType.ABOVE_WATER) {
                  return false;
               }
         }

         lv = lv2;
      }

      return true;
   }

   private PositionType getPositionType(BlockPos start, BlockPos end) {
      return (PositionType)BlockPos.stream(start, end).map(this::getPositionType).reduce((arg, arg2) -> {
         return arg == arg2 ? arg : FishingBobberEntity.PositionType.INVALID;
      }).orElse(FishingBobberEntity.PositionType.INVALID);
   }

   private PositionType getPositionType(BlockPos pos) {
      BlockState lv = this.world.getBlockState(pos);
      if (!lv.isAir() && !lv.isOf(Blocks.LILY_PAD)) {
         FluidState lv2 = lv.getFluidState();
         return lv2.isIn(FluidTags.WATER) && lv2.isStill() && lv.getCollisionShape(this.world, pos).isEmpty() ? FishingBobberEntity.PositionType.INSIDE_WATER : FishingBobberEntity.PositionType.INVALID;
      } else {
         return FishingBobberEntity.PositionType.ABOVE_WATER;
      }
   }

   public boolean isInOpenWater() {
      return this.inOpenWater;
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
   }

   public int use(ItemStack usedItem) {
      PlayerEntity lv = this.getPlayerOwner();
      if (!this.world.isClient && lv != null && !this.removeIfInvalid(lv)) {
         int i = 0;
         if (this.hookedEntity != null) {
            this.pullHookedEntity(this.hookedEntity);
            Criteria.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity)lv, usedItem, this, Collections.emptyList());
            this.world.sendEntityStatus(this, EntityStatuses.PULL_HOOKED_ENTITY);
            i = this.hookedEntity instanceof ItemEntity ? 3 : 5;
         } else if (this.hookCountdown > 0) {
            LootContext.Builder lv2 = (new LootContext.Builder((ServerWorld)this.world)).parameter(LootContextParameters.ORIGIN, this.getPos()).parameter(LootContextParameters.TOOL, usedItem).parameter(LootContextParameters.THIS_ENTITY, this).random(this.random).luck((float)this.luckOfTheSeaLevel + lv.getLuck());
            LootTable lv3 = this.world.getServer().getLootManager().getLootTable(LootTables.FISHING_GAMEPLAY);
            List list = lv3.generateLoot(lv2.build(LootContextTypes.FISHING));
            Criteria.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity)lv, usedItem, this, list);
            Iterator var7 = list.iterator();

            while(var7.hasNext()) {
               ItemStack lv4 = (ItemStack)var7.next();
               ItemEntity lv5 = new ItemEntity(this.world, this.getX(), this.getY(), this.getZ(), lv4);
               double d = lv.getX() - this.getX();
               double e = lv.getY() - this.getY();
               double f = lv.getZ() - this.getZ();
               double g = 0.1;
               lv5.setVelocity(d * 0.1, e * 0.1 + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);
               this.world.spawnEntity(lv5);
               lv.world.spawnEntity(new ExperienceOrbEntity(lv.world, lv.getX(), lv.getY() + 0.5, lv.getZ() + 0.5, this.random.nextInt(6) + 1));
               if (lv4.isIn(ItemTags.FISHES)) {
                  lv.increaseStat((Identifier)Stats.FISH_CAUGHT, 1);
               }
            }

            i = 1;
         }

         if (this.onGround) {
            i = 2;
         }

         this.discard();
         return i;
      } else {
         return 0;
      }
   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.PULL_HOOKED_ENTITY && this.world.isClient && this.hookedEntity instanceof PlayerEntity && ((PlayerEntity)this.hookedEntity).isMainPlayer()) {
         this.pullHookedEntity(this.hookedEntity);
      }

      super.handleStatus(status);
   }

   protected void pullHookedEntity(Entity entity) {
      Entity lv = this.getOwner();
      if (lv != null) {
         Vec3d lv2 = (new Vec3d(lv.getX() - this.getX(), lv.getY() - this.getY(), lv.getZ() - this.getZ())).multiply(0.1);
         entity.setVelocity(entity.getVelocity().add(lv2));
      }
   }

   protected Entity.MoveEffect getMoveEffect() {
      return Entity.MoveEffect.NONE;
   }

   public void remove(Entity.RemovalReason reason) {
      this.setPlayerFishHook((FishingBobberEntity)null);
      super.remove(reason);
   }

   public void onRemoved() {
      this.setPlayerFishHook((FishingBobberEntity)null);
   }

   public void setOwner(@Nullable Entity entity) {
      super.setOwner(entity);
      this.setPlayerFishHook(this);
   }

   private void setPlayerFishHook(@Nullable FishingBobberEntity fishingBobber) {
      PlayerEntity lv = this.getPlayerOwner();
      if (lv != null) {
         lv.fishHook = fishingBobber;
      }

   }

   @Nullable
   public PlayerEntity getPlayerOwner() {
      Entity lv = this.getOwner();
      return lv instanceof PlayerEntity ? (PlayerEntity)lv : null;
   }

   @Nullable
   public Entity getHookedEntity() {
      return this.hookedEntity;
   }

   public boolean canUsePortals() {
      return false;
   }

   public Packet createSpawnPacket() {
      Entity lv = this.getOwner();
      return new EntitySpawnS2CPacket(this, lv == null ? this.getId() : lv.getId());
   }

   public void onSpawnPacket(EntitySpawnS2CPacket packet) {
      super.onSpawnPacket(packet);
      if (this.getPlayerOwner() == null) {
         int i = packet.getEntityData();
         LOGGER.error("Failed to recreate fishing hook on client. {} (id: {}) is not a valid owner.", this.world.getEntityById(i), i);
         this.kill();
      }

   }

   static {
      HOOK_ENTITY_ID = DataTracker.registerData(FishingBobberEntity.class, TrackedDataHandlerRegistry.INTEGER);
      CAUGHT_FISH = DataTracker.registerData(FishingBobberEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }

   private static enum State {
      FLYING,
      HOOKED_IN_ENTITY,
      BOBBING;

      // $FF: synthetic method
      private static State[] method_36664() {
         return new State[]{FLYING, HOOKED_IN_ENTITY, BOBBING};
      }
   }

   private static enum PositionType {
      ABOVE_WATER,
      INSIDE_WATER,
      INVALID;

      // $FF: synthetic method
      private static PositionType[] method_36665() {
         return new PositionType[]{ABOVE_WATER, INSIDE_WATER, INVALID};
      }
   }
}
