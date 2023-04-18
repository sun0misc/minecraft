package net.minecraft.entity.projectile;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TridentEntity extends PersistentProjectileEntity {
   private static final TrackedData LOYALTY;
   private static final TrackedData ENCHANTED;
   private ItemStack tridentStack;
   private boolean dealtDamage;
   public int returnTimer;

   public TridentEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.tridentStack = new ItemStack(Items.TRIDENT);
   }

   public TridentEntity(World world, LivingEntity owner, ItemStack stack) {
      super(EntityType.TRIDENT, owner, world);
      this.tridentStack = new ItemStack(Items.TRIDENT);
      this.tridentStack = stack.copy();
      this.dataTracker.set(LOYALTY, (byte)EnchantmentHelper.getLoyalty(stack));
      this.dataTracker.set(ENCHANTED, stack.hasGlint());
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(LOYALTY, (byte)0);
      this.dataTracker.startTracking(ENCHANTED, false);
   }

   public void tick() {
      if (this.inGroundTime > 4) {
         this.dealtDamage = true;
      }

      Entity lv = this.getOwner();
      int i = (Byte)this.dataTracker.get(LOYALTY);
      if (i > 0 && (this.dealtDamage || this.isNoClip()) && lv != null) {
         if (!this.isOwnerAlive()) {
            if (!this.world.isClient && this.pickupType == PersistentProjectileEntity.PickupPermission.ALLOWED) {
               this.dropStack(this.asItemStack(), 0.1F);
            }

            this.discard();
         } else {
            this.setNoClip(true);
            Vec3d lv2 = lv.getEyePos().subtract(this.getPos());
            this.setPos(this.getX(), this.getY() + lv2.y * 0.015 * (double)i, this.getZ());
            if (this.world.isClient) {
               this.lastRenderY = this.getY();
            }

            double d = 0.05 * (double)i;
            this.setVelocity(this.getVelocity().multiply(0.95).add(lv2.normalize().multiply(d)));
            if (this.returnTimer == 0) {
               this.playSound(SoundEvents.ITEM_TRIDENT_RETURN, 10.0F, 1.0F);
            }

            ++this.returnTimer;
         }
      }

      super.tick();
   }

   private boolean isOwnerAlive() {
      Entity lv = this.getOwner();
      if (lv != null && lv.isAlive()) {
         return !(lv instanceof ServerPlayerEntity) || !lv.isSpectator();
      } else {
         return false;
      }
   }

   protected ItemStack asItemStack() {
      return this.tridentStack.copy();
   }

   public boolean isEnchanted() {
      return (Boolean)this.dataTracker.get(ENCHANTED);
   }

   @Nullable
   protected EntityHitResult getEntityCollision(Vec3d currentPosition, Vec3d nextPosition) {
      return this.dealtDamage ? null : super.getEntityCollision(currentPosition, nextPosition);
   }

   protected void onEntityHit(EntityHitResult entityHitResult) {
      Entity lv = entityHitResult.getEntity();
      float f = 8.0F;
      if (lv instanceof LivingEntity lv2) {
         f += EnchantmentHelper.getAttackDamage(this.tridentStack, lv2.getGroup());
      }

      Entity lv3 = this.getOwner();
      DamageSource lv4 = this.getDamageSources().trident(this, (Entity)(lv3 == null ? this : lv3));
      this.dealtDamage = true;
      SoundEvent lv5 = SoundEvents.ITEM_TRIDENT_HIT;
      if (lv.damage(lv4, f)) {
         if (lv.getType() == EntityType.ENDERMAN) {
            return;
         }

         if (lv instanceof LivingEntity) {
            LivingEntity lv6 = (LivingEntity)lv;
            if (lv3 instanceof LivingEntity) {
               EnchantmentHelper.onUserDamaged(lv6, lv3);
               EnchantmentHelper.onTargetDamaged((LivingEntity)lv3, lv6);
            }

            this.onHit(lv6);
         }
      }

      this.setVelocity(this.getVelocity().multiply(-0.01, -0.1, -0.01));
      float g = 1.0F;
      if (this.world instanceof ServerWorld && this.world.isThundering() && this.hasChanneling()) {
         BlockPos lv7 = lv.getBlockPos();
         if (this.world.isSkyVisible(lv7)) {
            LightningEntity lv8 = (LightningEntity)EntityType.LIGHTNING_BOLT.create(this.world);
            if (lv8 != null) {
               lv8.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(lv7));
               lv8.setChanneler(lv3 instanceof ServerPlayerEntity ? (ServerPlayerEntity)lv3 : null);
               this.world.spawnEntity(lv8);
               lv5 = SoundEvents.ITEM_TRIDENT_THUNDER;
               g = 5.0F;
            }
         }
      }

      this.playSound(lv5, g, 1.0F);
   }

   public boolean hasChanneling() {
      return EnchantmentHelper.hasChanneling(this.tridentStack);
   }

   protected boolean tryPickup(PlayerEntity player) {
      return super.tryPickup(player) || this.isNoClip() && this.isOwner(player) && player.getInventory().insertStack(this.asItemStack());
   }

   protected SoundEvent getHitSound() {
      return SoundEvents.ITEM_TRIDENT_HIT_GROUND;
   }

   public void onPlayerCollision(PlayerEntity player) {
      if (this.isOwner(player) || this.getOwner() == null) {
         super.onPlayerCollision(player);
      }

   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("Trident", NbtElement.COMPOUND_TYPE)) {
         this.tridentStack = ItemStack.fromNbt(nbt.getCompound("Trident"));
      }

      this.dealtDamage = nbt.getBoolean("DealtDamage");
      this.dataTracker.set(LOYALTY, (byte)EnchantmentHelper.getLoyalty(this.tridentStack));
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.put("Trident", this.tridentStack.writeNbt(new NbtCompound()));
      nbt.putBoolean("DealtDamage", this.dealtDamage);
   }

   public void age() {
      int i = (Byte)this.dataTracker.get(LOYALTY);
      if (this.pickupType != PersistentProjectileEntity.PickupPermission.ALLOWED || i <= 0) {
         super.age();
      }

   }

   protected float getDragInWater() {
      return 0.99F;
   }

   public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
      return true;
   }

   static {
      LOYALTY = DataTracker.registerData(TridentEntity.class, TrackedDataHandlerRegistry.BYTE);
      ENCHANTED = DataTracker.registerData(TridentEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }
}
