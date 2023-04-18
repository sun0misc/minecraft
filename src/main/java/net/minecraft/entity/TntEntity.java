package net.minecraft.entity;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TntEntity extends Entity implements Ownable {
   private static final TrackedData FUSE;
   private static final int DEFAULT_FUSE = 80;
   @Nullable
   private LivingEntity causingEntity;

   public TntEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.intersectionChecked = true;
   }

   public TntEntity(World world, double x, double y, double z, @Nullable LivingEntity igniter) {
      this(EntityType.TNT, world);
      this.setPosition(x, y, z);
      double g = world.random.nextDouble() * 6.2831854820251465;
      this.setVelocity(-Math.sin(g) * 0.02, 0.20000000298023224, -Math.cos(g) * 0.02);
      this.setFuse(80);
      this.prevX = x;
      this.prevY = y;
      this.prevZ = z;
      this.causingEntity = igniter;
   }

   protected void initDataTracker() {
      this.dataTracker.startTracking(FUSE, 80);
   }

   protected Entity.MoveEffect getMoveEffect() {
      return Entity.MoveEffect.NONE;
   }

   public boolean canHit() {
      return !this.isRemoved();
   }

   public void tick() {
      if (!this.hasNoGravity()) {
         this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
      }

      this.move(MovementType.SELF, this.getVelocity());
      this.setVelocity(this.getVelocity().multiply(0.98));
      if (this.onGround) {
         this.setVelocity(this.getVelocity().multiply(0.7, -0.5, 0.7));
      }

      int i = this.getFuse() - 1;
      this.setFuse(i);
      if (i <= 0) {
         this.discard();
         if (!this.world.isClient) {
            this.explode();
         }
      } else {
         this.updateWaterState();
         if (this.world.isClient) {
            this.world.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
         }
      }

   }

   private void explode() {
      float f = 4.0F;
      this.world.createExplosion(this, this.getX(), this.getBodyY(0.0625), this.getZ(), 4.0F, World.ExplosionSourceType.TNT);
   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      nbt.putShort("Fuse", (short)this.getFuse());
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      this.setFuse(nbt.getShort("Fuse"));
   }

   @Nullable
   public LivingEntity getOwner() {
      return this.causingEntity;
   }

   protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 0.15F;
   }

   public void setFuse(int fuse) {
      this.dataTracker.set(FUSE, fuse);
   }

   public int getFuse() {
      return (Integer)this.dataTracker.get(FUSE);
   }

   // $FF: synthetic method
   @Nullable
   public Entity getOwner() {
      return this.getOwner();
   }

   static {
      FUSE = DataTracker.registerData(TntEntity.class, TrackedDataHandlerRegistry.INTEGER);
   }
}
