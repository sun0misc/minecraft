package net.minecraft.entity.projectile;

import com.google.common.base.MoreObjects;
import java.util.Iterator;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public abstract class ProjectileEntity extends Entity implements Ownable {
   @Nullable
   private UUID ownerUuid;
   @Nullable
   private Entity owner;
   private boolean leftOwner;
   private boolean shot;

   ProjectileEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public void setOwner(@Nullable Entity entity) {
      if (entity != null) {
         this.ownerUuid = entity.getUuid();
         this.owner = entity;
      }

   }

   @Nullable
   public Entity getOwner() {
      if (this.owner != null && !this.owner.isRemoved()) {
         return this.owner;
      } else if (this.ownerUuid != null && this.world instanceof ServerWorld) {
         this.owner = ((ServerWorld)this.world).getEntity(this.ownerUuid);
         return this.owner;
      } else {
         return null;
      }
   }

   public Entity getEffectCause() {
      return (Entity)MoreObjects.firstNonNull(this.getOwner(), this);
   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      if (this.ownerUuid != null) {
         nbt.putUuid("Owner", this.ownerUuid);
      }

      if (this.leftOwner) {
         nbt.putBoolean("LeftOwner", true);
      }

      nbt.putBoolean("HasBeenShot", this.shot);
   }

   protected boolean isOwner(Entity entity) {
      return entity.getUuid().equals(this.ownerUuid);
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      if (nbt.containsUuid("Owner")) {
         this.ownerUuid = nbt.getUuid("Owner");
      }

      this.leftOwner = nbt.getBoolean("LeftOwner");
      this.shot = nbt.getBoolean("HasBeenShot");
   }

   public void tick() {
      if (!this.shot) {
         this.emitGameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner());
         this.shot = true;
      }

      if (!this.leftOwner) {
         this.leftOwner = this.shouldLeaveOwner();
      }

      super.tick();
   }

   private boolean shouldLeaveOwner() {
      Entity lv = this.getOwner();
      if (lv != null) {
         Iterator var2 = this.world.getOtherEntities(this, this.getBoundingBox().stretch(this.getVelocity()).expand(1.0), (entity) -> {
            return !entity.isSpectator() && entity.canHit();
         }).iterator();

         while(var2.hasNext()) {
            Entity lv2 = (Entity)var2.next();
            if (lv2.getRootVehicle() == lv.getRootVehicle()) {
               return false;
            }
         }
      }

      return true;
   }

   public void setVelocity(double x, double y, double z, float speed, float divergence) {
      Vec3d lv = (new Vec3d(x, y, z)).normalize().add(this.random.nextTriangular(0.0, 0.0172275 * (double)divergence), this.random.nextTriangular(0.0, 0.0172275 * (double)divergence), this.random.nextTriangular(0.0, 0.0172275 * (double)divergence)).multiply((double)speed);
      this.setVelocity(lv);
      double i = lv.horizontalLength();
      this.setYaw((float)(MathHelper.atan2(lv.x, lv.z) * 57.2957763671875));
      this.setPitch((float)(MathHelper.atan2(lv.y, i) * 57.2957763671875));
      this.prevYaw = this.getYaw();
      this.prevPitch = this.getPitch();
   }

   public void setVelocity(Entity shooter, float pitch, float yaw, float roll, float speed, float divergence) {
      float k = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
      float l = -MathHelper.sin((pitch + roll) * 0.017453292F);
      float m = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
      this.setVelocity((double)k, (double)l, (double)m, speed, divergence);
      Vec3d lv = shooter.getVelocity();
      this.setVelocity(this.getVelocity().add(lv.x, shooter.isOnGround() ? 0.0 : lv.y, lv.z));
   }

   protected void onCollision(HitResult hitResult) {
      HitResult.Type lv = hitResult.getType();
      if (lv == HitResult.Type.ENTITY) {
         this.onEntityHit((EntityHitResult)hitResult);
         this.world.emitGameEvent(GameEvent.PROJECTILE_LAND, hitResult.getPos(), GameEvent.Emitter.of(this, (BlockState)null));
      } else if (lv == HitResult.Type.BLOCK) {
         BlockHitResult lv2 = (BlockHitResult)hitResult;
         this.onBlockHit(lv2);
         BlockPos lv3 = lv2.getBlockPos();
         this.world.emitGameEvent(GameEvent.PROJECTILE_LAND, lv3, GameEvent.Emitter.of(this, this.world.getBlockState(lv3)));
      }

   }

   protected void onEntityHit(EntityHitResult entityHitResult) {
   }

   protected void onBlockHit(BlockHitResult blockHitResult) {
      BlockState lv = this.world.getBlockState(blockHitResult.getBlockPos());
      lv.onProjectileHit(this.world, lv, blockHitResult, this);
   }

   public void setVelocityClient(double x, double y, double z) {
      this.setVelocity(x, y, z);
      if (this.prevPitch == 0.0F && this.prevYaw == 0.0F) {
         double g = Math.sqrt(x * x + z * z);
         this.setPitch((float)(MathHelper.atan2(y, g) * 57.2957763671875));
         this.setYaw((float)(MathHelper.atan2(x, z) * 57.2957763671875));
         this.prevPitch = this.getPitch();
         this.prevYaw = this.getYaw();
         this.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
      }

   }

   protected boolean canHit(Entity entity) {
      if (!entity.canBeHitByProjectile()) {
         return false;
      } else {
         Entity lv = this.getOwner();
         return lv == null || this.leftOwner || !lv.isConnectedThroughVehicle(entity);
      }
   }

   protected void updateRotation() {
      Vec3d lv = this.getVelocity();
      double d = lv.horizontalLength();
      this.setPitch(updateRotation(this.prevPitch, (float)(MathHelper.atan2(lv.y, d) * 57.2957763671875)));
      this.setYaw(updateRotation(this.prevYaw, (float)(MathHelper.atan2(lv.x, lv.z) * 57.2957763671875)));
   }

   protected static float updateRotation(float prevRot, float newRot) {
      while(newRot - prevRot < -180.0F) {
         prevRot -= 360.0F;
      }

      while(newRot - prevRot >= 180.0F) {
         prevRot += 360.0F;
      }

      return MathHelper.lerp(0.2F, prevRot, newRot);
   }

   public Packet createSpawnPacket() {
      Entity lv = this.getOwner();
      return new EntitySpawnS2CPacket(this, lv == null ? 0 : lv.getId());
   }

   public void onSpawnPacket(EntitySpawnS2CPacket packet) {
      super.onSpawnPacket(packet);
      Entity lv = this.world.getEntityById(packet.getEntityData());
      if (lv != null) {
         this.setOwner(lv);
      }

   }

   public boolean canModifyAt(World world, BlockPos pos) {
      Entity lv = this.getOwner();
      if (lv instanceof PlayerEntity) {
         return lv.canModifyAt(world, pos);
      } else {
         return lv == null || world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
      }
   }
}
