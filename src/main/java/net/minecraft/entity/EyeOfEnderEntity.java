package net.minecraft.entity;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class EyeOfEnderEntity extends Entity implements FlyingItemEntity {
   private static final TrackedData ITEM;
   private double targetX;
   private double targetY;
   private double targetZ;
   private int lifespan;
   private boolean dropsItem;

   public EyeOfEnderEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public EyeOfEnderEntity(World world, double x, double y, double z) {
      this(EntityType.EYE_OF_ENDER, world);
      this.setPosition(x, y, z);
   }

   public void setItem(ItemStack stack) {
      if (!stack.isOf(Items.ENDER_EYE) || stack.hasNbt()) {
         this.getDataTracker().set(ITEM, stack.copyWithCount(1));
      }

   }

   private ItemStack getTrackedItem() {
      return (ItemStack)this.getDataTracker().get(ITEM);
   }

   public ItemStack getStack() {
      ItemStack lv = this.getTrackedItem();
      return lv.isEmpty() ? new ItemStack(Items.ENDER_EYE) : lv;
   }

   protected void initDataTracker() {
      this.getDataTracker().startTracking(ITEM, ItemStack.EMPTY);
   }

   public boolean shouldRender(double distance) {
      double e = this.getBoundingBox().getAverageSideLength() * 4.0;
      if (Double.isNaN(e)) {
         e = 4.0;
      }

      e *= 64.0;
      return distance < e * e;
   }

   public void initTargetPos(BlockPos pos) {
      double d = (double)pos.getX();
      int i = pos.getY();
      double e = (double)pos.getZ();
      double f = d - this.getX();
      double g = e - this.getZ();
      double h = Math.sqrt(f * f + g * g);
      if (h > 12.0) {
         this.targetX = this.getX() + f / h * 12.0;
         this.targetZ = this.getZ() + g / h * 12.0;
         this.targetY = this.getY() + 8.0;
      } else {
         this.targetX = d;
         this.targetY = (double)i;
         this.targetZ = e;
      }

      this.lifespan = 0;
      this.dropsItem = this.random.nextInt(5) > 0;
   }

   public void setVelocityClient(double x, double y, double z) {
      this.setVelocity(x, y, z);
      if (this.prevPitch == 0.0F && this.prevYaw == 0.0F) {
         double g = Math.sqrt(x * x + z * z);
         this.setYaw((float)(MathHelper.atan2(x, z) * 57.2957763671875));
         this.setPitch((float)(MathHelper.atan2(y, g) * 57.2957763671875));
         this.prevYaw = this.getYaw();
         this.prevPitch = this.getPitch();
      }

   }

   public void tick() {
      super.tick();
      Vec3d lv = this.getVelocity();
      double d = this.getX() + lv.x;
      double e = this.getY() + lv.y;
      double f = this.getZ() + lv.z;
      double g = lv.horizontalLength();
      this.setPitch(ProjectileEntity.updateRotation(this.prevPitch, (float)(MathHelper.atan2(lv.y, g) * 57.2957763671875)));
      this.setYaw(ProjectileEntity.updateRotation(this.prevYaw, (float)(MathHelper.atan2(lv.x, lv.z) * 57.2957763671875)));
      if (!this.world.isClient) {
         double h = this.targetX - d;
         double i = this.targetZ - f;
         float j = (float)Math.sqrt(h * h + i * i);
         float k = (float)MathHelper.atan2(i, h);
         double l = MathHelper.lerp(0.0025, g, (double)j);
         double m = lv.y;
         if (j < 1.0F) {
            l *= 0.8;
            m *= 0.8;
         }

         int n = this.getY() < this.targetY ? 1 : -1;
         lv = new Vec3d(Math.cos((double)k) * l, m + ((double)n - m) * 0.014999999664723873, Math.sin((double)k) * l);
         this.setVelocity(lv);
      }

      float o = 0.25F;
      if (this.isTouchingWater()) {
         for(int p = 0; p < 4; ++p) {
            this.world.addParticle(ParticleTypes.BUBBLE, d - lv.x * 0.25, e - lv.y * 0.25, f - lv.z * 0.25, lv.x, lv.y, lv.z);
         }
      } else {
         this.world.addParticle(ParticleTypes.PORTAL, d - lv.x * 0.25 + this.random.nextDouble() * 0.6 - 0.3, e - lv.y * 0.25 - 0.5, f - lv.z * 0.25 + this.random.nextDouble() * 0.6 - 0.3, lv.x, lv.y, lv.z);
      }

      if (!this.world.isClient) {
         this.setPosition(d, e, f);
         ++this.lifespan;
         if (this.lifespan > 80 && !this.world.isClient) {
            this.playSound(SoundEvents.ENTITY_ENDER_EYE_DEATH, 1.0F, 1.0F);
            this.discard();
            if (this.dropsItem) {
               this.world.spawnEntity(new ItemEntity(this.world, this.getX(), this.getY(), this.getZ(), this.getStack()));
            } else {
               this.world.syncWorldEvent(WorldEvents.EYE_OF_ENDER_BREAKS, this.getBlockPos(), 0);
            }
         }
      } else {
         this.setPos(d, e, f);
      }

   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      ItemStack lv = this.getTrackedItem();
      if (!lv.isEmpty()) {
         nbt.put("Item", lv.writeNbt(new NbtCompound()));
      }

   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      ItemStack lv = ItemStack.fromNbt(nbt.getCompound("Item"));
      this.setItem(lv);
   }

   public float getBrightnessAtEyes() {
      return 1.0F;
   }

   public boolean isAttackable() {
      return false;
   }

   static {
      ITEM = DataTracker.registerData(EyeOfEnderEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
   }
}
