package net.minecraft.entity.mob;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public abstract class PatrolEntity extends HostileEntity {
   @Nullable
   private BlockPos patrolTarget;
   private boolean patrolLeader;
   private boolean patrolling;

   protected PatrolEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   protected void initGoals() {
      super.initGoals();
      this.goalSelector.add(4, new PatrolGoal(this, 0.7, 0.595));
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      if (this.patrolTarget != null) {
         nbt.put("PatrolTarget", NbtHelper.fromBlockPos(this.patrolTarget));
      }

      nbt.putBoolean("PatrolLeader", this.patrolLeader);
      nbt.putBoolean("Patrolling", this.patrolling);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("PatrolTarget")) {
         this.patrolTarget = NbtHelper.toBlockPos(nbt.getCompound("PatrolTarget"));
      }

      this.patrolLeader = nbt.getBoolean("PatrolLeader");
      this.patrolling = nbt.getBoolean("Patrolling");
   }

   public double getHeightOffset() {
      return -0.45;
   }

   public boolean canLead() {
      return true;
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      if (spawnReason != SpawnReason.PATROL && spawnReason != SpawnReason.EVENT && spawnReason != SpawnReason.STRUCTURE && world.getRandom().nextFloat() < 0.06F && this.canLead()) {
         this.patrolLeader = true;
      }

      if (this.isPatrolLeader()) {
         this.equipStack(EquipmentSlot.HEAD, Raid.getOminousBanner());
         this.setEquipmentDropChance(EquipmentSlot.HEAD, 2.0F);
      }

      if (spawnReason == SpawnReason.PATROL) {
         this.patrolling = true;
      }

      return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
   }

   public static boolean canSpawn(EntityType type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      return world.getLightLevel(LightType.BLOCK, pos) > 8 ? false : canSpawnIgnoreLightLevel(type, world, spawnReason, pos, random);
   }

   public boolean canImmediatelyDespawn(double distanceSquared) {
      return !this.patrolling || distanceSquared > 16384.0;
   }

   public void setPatrolTarget(BlockPos targetPos) {
      this.patrolTarget = targetPos;
      this.patrolling = true;
   }

   public BlockPos getPatrolTarget() {
      return this.patrolTarget;
   }

   public boolean hasPatrolTarget() {
      return this.patrolTarget != null;
   }

   public void setPatrolLeader(boolean patrolLeader) {
      this.patrolLeader = patrolLeader;
      this.patrolling = true;
   }

   public boolean isPatrolLeader() {
      return this.patrolLeader;
   }

   public boolean hasNoRaid() {
      return true;
   }

   public void setRandomPatrolTarget() {
      this.patrolTarget = this.getBlockPos().add(-500 + this.random.nextInt(1000), 0, -500 + this.random.nextInt(1000));
      this.patrolling = true;
   }

   protected boolean isRaidCenterSet() {
      return this.patrolling;
   }

   protected void setPatrolling(boolean patrolling) {
      this.patrolling = patrolling;
   }

   public static class PatrolGoal extends Goal {
      private static final int field_30474 = 200;
      private final PatrolEntity entity;
      private final double leaderSpeed;
      private final double followSpeed;
      private long nextPatrolSearchTime;

      public PatrolGoal(PatrolEntity entity, double leaderSpeed, double followSpeed) {
         this.entity = entity;
         this.leaderSpeed = leaderSpeed;
         this.followSpeed = followSpeed;
         this.nextPatrolSearchTime = -1L;
         this.setControls(EnumSet.of(Goal.Control.MOVE));
      }

      public boolean canStart() {
         boolean bl = this.entity.world.getTime() < this.nextPatrolSearchTime;
         return this.entity.isRaidCenterSet() && this.entity.getTarget() == null && !this.entity.hasPassengers() && this.entity.hasPatrolTarget() && !bl;
      }

      public void start() {
      }

      public void stop() {
      }

      public void tick() {
         boolean bl = this.entity.isPatrolLeader();
         EntityNavigation lv = this.entity.getNavigation();
         if (lv.isIdle()) {
            List list = this.findPatrolTargets();
            if (this.entity.isRaidCenterSet() && list.isEmpty()) {
               this.entity.setPatrolling(false);
            } else if (bl && this.entity.getPatrolTarget().isWithinDistance(this.entity.getPos(), 10.0)) {
               this.entity.setRandomPatrolTarget();
            } else {
               Vec3d lv2 = Vec3d.ofBottomCenter(this.entity.getPatrolTarget());
               Vec3d lv3 = this.entity.getPos();
               Vec3d lv4 = lv3.subtract(lv2);
               lv2 = lv4.rotateY(90.0F).multiply(0.4).add(lv2);
               Vec3d lv5 = lv2.subtract(lv3).normalize().multiply(10.0).add(lv3);
               BlockPos lv6 = BlockPos.ofFloored(lv5);
               lv6 = this.entity.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, lv6);
               if (!lv.startMovingTo((double)lv6.getX(), (double)lv6.getY(), (double)lv6.getZ(), bl ? this.followSpeed : this.leaderSpeed)) {
                  this.wander();
                  this.nextPatrolSearchTime = this.entity.world.getTime() + 200L;
               } else if (bl) {
                  Iterator var9 = list.iterator();

                  while(var9.hasNext()) {
                     PatrolEntity lv7 = (PatrolEntity)var9.next();
                     lv7.setPatrolTarget(lv6);
                  }
               }
            }
         }

      }

      private List findPatrolTargets() {
         return this.entity.world.getEntitiesByClass(PatrolEntity.class, this.entity.getBoundingBox().expand(16.0), (arg) -> {
            return arg.hasNoRaid() && !arg.isPartOf(this.entity);
         });
      }

      private boolean wander() {
         Random lv = this.entity.getRandom();
         BlockPos lv2 = this.entity.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, this.entity.getBlockPos().add(-8 + lv.nextInt(16), 0, -8 + lv.nextInt(16)));
         return this.entity.getNavigation().startMovingTo((double)lv2.getX(), (double)lv2.getY(), (double)lv2.getZ(), this.leaderSpeed);
      }
   }
}
