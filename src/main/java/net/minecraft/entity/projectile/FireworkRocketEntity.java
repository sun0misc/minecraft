package net.minecraft.entity.projectile;

import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class FireworkRocketEntity extends ProjectileEntity implements FlyingItemEntity {
   private static final TrackedData ITEM;
   private static final TrackedData SHOOTER_ENTITY_ID;
   private static final TrackedData SHOT_AT_ANGLE;
   private int life;
   private int lifeTime;
   @Nullable
   private LivingEntity shooter;

   public FireworkRocketEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public FireworkRocketEntity(World world, double x, double y, double z, ItemStack stack) {
      super(EntityType.FIREWORK_ROCKET, world);
      this.life = 0;
      this.setPosition(x, y, z);
      int i = 1;
      if (!stack.isEmpty() && stack.hasNbt()) {
         this.dataTracker.set(ITEM, stack.copy());
         i += stack.getOrCreateSubNbt("Fireworks").getByte("Flight");
      }

      this.setVelocity(this.random.nextTriangular(0.0, 0.002297), 0.05, this.random.nextTriangular(0.0, 0.002297));
      this.lifeTime = 10 * i + this.random.nextInt(6) + this.random.nextInt(7);
   }

   public FireworkRocketEntity(World world, @Nullable Entity entity, double x, double y, double z, ItemStack stack) {
      this(world, x, y, z, stack);
      this.setOwner(entity);
   }

   public FireworkRocketEntity(World world, ItemStack stack, LivingEntity shooter) {
      this(world, shooter, shooter.getX(), shooter.getY(), shooter.getZ(), stack);
      this.dataTracker.set(SHOOTER_ENTITY_ID, OptionalInt.of(shooter.getId()));
      this.shooter = shooter;
   }

   public FireworkRocketEntity(World world, ItemStack stack, double x, double y, double z, boolean shotAtAngle) {
      this(world, x, y, z, stack);
      this.dataTracker.set(SHOT_AT_ANGLE, shotAtAngle);
   }

   public FireworkRocketEntity(World world, ItemStack stack, Entity entity, double x, double y, double z, boolean shotAtAngle) {
      this(world, stack, x, y, z, shotAtAngle);
      this.setOwner(entity);
   }

   protected void initDataTracker() {
      this.dataTracker.startTracking(ITEM, ItemStack.EMPTY);
      this.dataTracker.startTracking(SHOOTER_ENTITY_ID, OptionalInt.empty());
      this.dataTracker.startTracking(SHOT_AT_ANGLE, false);
   }

   public boolean shouldRender(double distance) {
      return distance < 4096.0 && !this.wasShotByEntity();
   }

   public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
      return super.shouldRender(cameraX, cameraY, cameraZ) && !this.wasShotByEntity();
   }

   public void tick() {
      super.tick();
      Vec3d lv3;
      if (this.wasShotByEntity()) {
         if (this.shooter == null) {
            ((OptionalInt)this.dataTracker.get(SHOOTER_ENTITY_ID)).ifPresent((id) -> {
               Entity lv = this.world.getEntityById(id);
               if (lv instanceof LivingEntity) {
                  this.shooter = (LivingEntity)lv;
               }

            });
         }

         if (this.shooter != null) {
            if (this.shooter.isFallFlying()) {
               Vec3d lv = this.shooter.getRotationVector();
               double d = 1.5;
               double e = 0.1;
               Vec3d lv2 = this.shooter.getVelocity();
               this.shooter.setVelocity(lv2.add(lv.x * 0.1 + (lv.x * 1.5 - lv2.x) * 0.5, lv.y * 0.1 + (lv.y * 1.5 - lv2.y) * 0.5, lv.z * 0.1 + (lv.z * 1.5 - lv2.z) * 0.5));
               lv3 = this.shooter.getHandPosOffset(Items.FIREWORK_ROCKET);
            } else {
               lv3 = Vec3d.ZERO;
            }

            this.setPosition(this.shooter.getX() + lv3.x, this.shooter.getY() + lv3.y, this.shooter.getZ() + lv3.z);
            this.setVelocity(this.shooter.getVelocity());
         }
      } else {
         if (!this.wasShotAtAngle()) {
            double f = this.horizontalCollision ? 1.0 : 1.15;
            this.setVelocity(this.getVelocity().multiply(f, 1.0, f).add(0.0, 0.04, 0.0));
         }

         lv3 = this.getVelocity();
         this.move(MovementType.SELF, lv3);
         this.setVelocity(lv3);
      }

      HitResult lv4 = ProjectileUtil.getCollision(this, this::canHit);
      if (!this.noClip) {
         this.onCollision(lv4);
         this.velocityDirty = true;
      }

      this.updateRotation();
      if (this.life == 0 && !this.isSilent()) {
         this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.AMBIENT, 3.0F, 1.0F);
      }

      ++this.life;
      if (this.world.isClient && this.life % 2 < 2) {
         this.world.addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.05, -this.getVelocity().y * 0.5, this.random.nextGaussian() * 0.05);
      }

      if (!this.world.isClient && this.life > this.lifeTime) {
         this.explodeAndRemove();
      }

   }

   private void explodeAndRemove() {
      this.world.sendEntityStatus(this, EntityStatuses.EXPLODE_FIREWORK_CLIENT);
      this.emitGameEvent(GameEvent.EXPLODE, this.getOwner());
      this.explode();
      this.discard();
   }

   protected void onEntityHit(EntityHitResult entityHitResult) {
      super.onEntityHit(entityHitResult);
      if (!this.world.isClient) {
         this.explodeAndRemove();
      }
   }

   protected void onBlockHit(BlockHitResult blockHitResult) {
      BlockPos lv = new BlockPos(blockHitResult.getBlockPos());
      this.world.getBlockState(lv).onEntityCollision(this.world, lv, this);
      if (!this.world.isClient() && this.hasExplosionEffects()) {
         this.explodeAndRemove();
      }

      super.onBlockHit(blockHitResult);
   }

   private boolean hasExplosionEffects() {
      ItemStack lv = (ItemStack)this.dataTracker.get(ITEM);
      NbtCompound lv2 = lv.isEmpty() ? null : lv.getSubNbt("Fireworks");
      NbtList lv3 = lv2 != null ? lv2.getList("Explosions", NbtElement.COMPOUND_TYPE) : null;
      return lv3 != null && !lv3.isEmpty();
   }

   private void explode() {
      float f = 0.0F;
      ItemStack lv = (ItemStack)this.dataTracker.get(ITEM);
      NbtCompound lv2 = lv.isEmpty() ? null : lv.getSubNbt("Fireworks");
      NbtList lv3 = lv2 != null ? lv2.getList("Explosions", NbtElement.COMPOUND_TYPE) : null;
      if (lv3 != null && !lv3.isEmpty()) {
         f = 5.0F + (float)(lv3.size() * 2);
      }

      if (f > 0.0F) {
         if (this.shooter != null) {
            this.shooter.damage(this.getDamageSources().fireworks(this, this.getOwner()), 5.0F + (float)(lv3.size() * 2));
         }

         double d = 5.0;
         Vec3d lv4 = this.getPos();
         List list = this.world.getNonSpectatingEntities(LivingEntity.class, this.getBoundingBox().expand(5.0));
         Iterator var9 = list.iterator();

         while(true) {
            LivingEntity lv5;
            do {
               do {
                  if (!var9.hasNext()) {
                     return;
                  }

                  lv5 = (LivingEntity)var9.next();
               } while(lv5 == this.shooter);
            } while(this.squaredDistanceTo(lv5) > 25.0);

            boolean bl = false;

            for(int i = 0; i < 2; ++i) {
               Vec3d lv6 = new Vec3d(lv5.getX(), lv5.getBodyY(0.5 * (double)i), lv5.getZ());
               HitResult lv7 = this.world.raycast(new RaycastContext(lv4, lv6, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
               if (lv7.getType() == HitResult.Type.MISS) {
                  bl = true;
                  break;
               }
            }

            if (bl) {
               float g = f * (float)Math.sqrt((5.0 - (double)this.distanceTo(lv5)) / 5.0);
               lv5.damage(this.getDamageSources().fireworks(this, this.getOwner()), g);
            }
         }
      }
   }

   private boolean wasShotByEntity() {
      return ((OptionalInt)this.dataTracker.get(SHOOTER_ENTITY_ID)).isPresent();
   }

   public boolean wasShotAtAngle() {
      return (Boolean)this.dataTracker.get(SHOT_AT_ANGLE);
   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.EXPLODE_FIREWORK_CLIENT && this.world.isClient) {
         if (!this.hasExplosionEffects()) {
            for(int i = 0; i < this.random.nextInt(3) + 2; ++i) {
               this.world.addParticle(ParticleTypes.POOF, this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.05, 0.005, this.random.nextGaussian() * 0.05);
            }
         } else {
            ItemStack lv = (ItemStack)this.dataTracker.get(ITEM);
            NbtCompound lv2 = lv.isEmpty() ? null : lv.getSubNbt("Fireworks");
            Vec3d lv3 = this.getVelocity();
            this.world.addFireworkParticle(this.getX(), this.getY(), this.getZ(), lv3.x, lv3.y, lv3.z, lv2);
         }
      }

      super.handleStatus(status);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("Life", this.life);
      nbt.putInt("LifeTime", this.lifeTime);
      ItemStack lv = (ItemStack)this.dataTracker.get(ITEM);
      if (!lv.isEmpty()) {
         nbt.put("FireworksItem", lv.writeNbt(new NbtCompound()));
      }

      nbt.putBoolean("ShotAtAngle", (Boolean)this.dataTracker.get(SHOT_AT_ANGLE));
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.life = nbt.getInt("Life");
      this.lifeTime = nbt.getInt("LifeTime");
      ItemStack lv = ItemStack.fromNbt(nbt.getCompound("FireworksItem"));
      if (!lv.isEmpty()) {
         this.dataTracker.set(ITEM, lv);
      }

      if (nbt.contains("ShotAtAngle")) {
         this.dataTracker.set(SHOT_AT_ANGLE, nbt.getBoolean("ShotAtAngle"));
      }

   }

   public ItemStack getStack() {
      ItemStack lv = (ItemStack)this.dataTracker.get(ITEM);
      return lv.isEmpty() ? new ItemStack(Items.FIREWORK_ROCKET) : lv;
   }

   public boolean isAttackable() {
      return false;
   }

   static {
      ITEM = DataTracker.registerData(FireworkRocketEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
      SHOOTER_ENTITY_ID = DataTracker.registerData(FireworkRocketEntity.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
      SHOT_AT_ANGLE = DataTracker.registerData(FireworkRocketEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }
}
