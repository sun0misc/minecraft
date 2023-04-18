package net.minecraft.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class ExplosiveProjectileEntity extends ProjectileEntity {
   public double powerX;
   public double powerY;
   public double powerZ;

   protected ExplosiveProjectileEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public ExplosiveProjectileEntity(EntityType type, double x, double y, double z, double directionX, double directionY, double directionZ, World world) {
      this(type, world);
      this.refreshPositionAndAngles(x, y, z, this.getYaw(), this.getPitch());
      this.refreshPosition();
      double j = Math.sqrt(directionX * directionX + directionY * directionY + directionZ * directionZ);
      if (j != 0.0) {
         this.powerX = directionX / j * 0.1;
         this.powerY = directionY / j * 0.1;
         this.powerZ = directionZ / j * 0.1;
      }

   }

   public ExplosiveProjectileEntity(EntityType type, LivingEntity owner, double directionX, double directionY, double directionZ, World world) {
      this(type, owner.getX(), owner.getY(), owner.getZ(), directionX, directionY, directionZ, world);
      this.setOwner(owner);
      this.setRotation(owner.getYaw(), owner.getPitch());
   }

   protected void initDataTracker() {
   }

   public boolean shouldRender(double distance) {
      double e = this.getBoundingBox().getAverageSideLength() * 4.0;
      if (Double.isNaN(e)) {
         e = 4.0;
      }

      e *= 64.0;
      return distance < e * e;
   }

   public void tick() {
      Entity lv = this.getOwner();
      if (this.world.isClient || (lv == null || !lv.isRemoved()) && this.world.isChunkLoaded(this.getBlockPos())) {
         super.tick();
         if (this.isBurning()) {
            this.setOnFireFor(1);
         }

         HitResult lv2 = ProjectileUtil.getCollision(this, this::canHit);
         if (lv2.getType() != HitResult.Type.MISS) {
            this.onCollision(lv2);
         }

         this.checkBlockCollision();
         Vec3d lv3 = this.getVelocity();
         double d = this.getX() + lv3.x;
         double e = this.getY() + lv3.y;
         double f = this.getZ() + lv3.z;
         ProjectileUtil.setRotationFromVelocity(this, 0.2F);
         float g = this.getDrag();
         if (this.isTouchingWater()) {
            for(int i = 0; i < 4; ++i) {
               float h = 0.25F;
               this.world.addParticle(ParticleTypes.BUBBLE, d - lv3.x * 0.25, e - lv3.y * 0.25, f - lv3.z * 0.25, lv3.x, lv3.y, lv3.z);
            }

            g = 0.8F;
         }

         this.setVelocity(lv3.add(this.powerX, this.powerY, this.powerZ).multiply((double)g));
         this.world.addParticle(this.getParticleType(), d, e + 0.5, f, 0.0, 0.0, 0.0);
         this.setPosition(d, e, f);
      } else {
         this.discard();
      }
   }

   protected boolean canHit(Entity entity) {
      return super.canHit(entity) && !entity.noClip;
   }

   protected boolean isBurning() {
      return true;
   }

   protected ParticleEffect getParticleType() {
      return ParticleTypes.SMOKE;
   }

   protected float getDrag() {
      return 0.95F;
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.put("power", this.toNbtList(new double[]{this.powerX, this.powerY, this.powerZ}));
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("power", NbtElement.LIST_TYPE)) {
         NbtList lv = nbt.getList("power", NbtElement.DOUBLE_TYPE);
         if (lv.size() == 3) {
            this.powerX = lv.getDouble(0);
            this.powerY = lv.getDouble(1);
            this.powerZ = lv.getDouble(2);
         }
      }

   }

   public boolean canHit() {
      return true;
   }

   public float getTargetingMargin() {
      return 1.0F;
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else {
         this.scheduleVelocityUpdate();
         Entity lv = source.getAttacker();
         if (lv != null) {
            if (!this.world.isClient) {
               Vec3d lv2 = lv.getRotationVector();
               this.setVelocity(lv2);
               this.powerX = lv2.x * 0.1;
               this.powerY = lv2.y * 0.1;
               this.powerZ = lv2.z * 0.1;
               this.setOwner(lv);
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public float getBrightnessAtEyes() {
      return 1.0F;
   }

   public Packet createSpawnPacket() {
      Entity lv = this.getOwner();
      int i = lv == null ? 0 : lv.getId();
      return new EntitySpawnS2CPacket(this.getId(), this.getUuid(), this.getX(), this.getY(), this.getZ(), this.getPitch(), this.getYaw(), this.getType(), i, new Vec3d(this.powerX, this.powerY, this.powerZ), 0.0);
   }

   public void onSpawnPacket(EntitySpawnS2CPacket packet) {
      super.onSpawnPacket(packet);
      double d = packet.getVelocityX();
      double e = packet.getVelocityY();
      double f = packet.getVelocityZ();
      double g = Math.sqrt(d * d + e * e + f * f);
      if (g != 0.0) {
         this.powerX = d / g * 0.1;
         this.powerY = e / g * 0.1;
         this.powerZ = f / g * 0.1;
      }

   }
}
