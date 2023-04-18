package net.minecraft.entity;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class ItemEntity extends Entity implements Ownable {
   private static final TrackedData STACK;
   private static final int DESPAWN_AGE = 6000;
   private static final int CANNOT_PICK_UP_DELAY = 32767;
   private static final int NEVER_DESPAWN_AGE = -32768;
   private int itemAge;
   private int pickupDelay;
   private int health;
   @Nullable
   private UUID thrower;
   @Nullable
   private UUID owner;
   public final float uniqueOffset;

   public ItemEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.health = 5;
      this.uniqueOffset = this.random.nextFloat() * 3.1415927F * 2.0F;
      this.setYaw(this.random.nextFloat() * 360.0F);
   }

   public ItemEntity(World world, double x, double y, double z, ItemStack stack) {
      this(world, x, y, z, stack, world.random.nextDouble() * 0.2 - 0.1, 0.2, world.random.nextDouble() * 0.2 - 0.1);
   }

   public ItemEntity(World world, double x, double y, double z, ItemStack stack, double velocityX, double velocityY, double velocityZ) {
      this(EntityType.ITEM, world);
      this.setPosition(x, y, z);
      this.setVelocity(velocityX, velocityY, velocityZ);
      this.setStack(stack);
   }

   private ItemEntity(ItemEntity entity) {
      super(entity.getType(), entity.world);
      this.health = 5;
      this.setStack(entity.getStack().copy());
      this.copyPositionAndRotation(entity);
      this.itemAge = entity.itemAge;
      this.uniqueOffset = entity.uniqueOffset;
   }

   public boolean occludeVibrationSignals() {
      return this.getStack().isIn(ItemTags.DAMPENS_VIBRATIONS);
   }

   @Nullable
   public Entity getOwner() {
      if (this.thrower != null) {
         World var2 = this.world;
         if (var2 instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)var2;
            return lv.getEntity(this.thrower);
         }
      }

      return null;
   }

   protected Entity.MoveEffect getMoveEffect() {
      return Entity.MoveEffect.NONE;
   }

   protected void initDataTracker() {
      this.getDataTracker().startTracking(STACK, ItemStack.EMPTY);
   }

   public void tick() {
      if (this.getStack().isEmpty()) {
         this.discard();
      } else {
         super.tick();
         if (this.pickupDelay > 0 && this.pickupDelay != 32767) {
            --this.pickupDelay;
         }

         this.prevX = this.getX();
         this.prevY = this.getY();
         this.prevZ = this.getZ();
         Vec3d lv = this.getVelocity();
         float f = this.getStandingEyeHeight() - 0.11111111F;
         if (this.isTouchingWater() && this.getFluidHeight(FluidTags.WATER) > (double)f) {
            this.applyWaterBuoyancy();
         } else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > (double)f) {
            this.applyLavaBuoyancy();
         } else if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
         }

         if (this.world.isClient) {
            this.noClip = false;
         } else {
            this.noClip = !this.world.isSpaceEmpty(this, this.getBoundingBox().contract(1.0E-7));
            if (this.noClip) {
               this.pushOutOfBlocks(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
            }
         }

         if (!this.onGround || this.getVelocity().horizontalLengthSquared() > 9.999999747378752E-6 || (this.age + this.getId()) % 4 == 0) {
            this.move(MovementType.SELF, this.getVelocity());
            float g = 0.98F;
            if (this.onGround) {
               g = this.world.getBlockState(BlockPos.ofFloored(this.getX(), this.getY() - 1.0, this.getZ())).getBlock().getSlipperiness() * 0.98F;
            }

            this.setVelocity(this.getVelocity().multiply((double)g, 0.98, (double)g));
            if (this.onGround) {
               Vec3d lv2 = this.getVelocity();
               if (lv2.y < 0.0) {
                  this.setVelocity(lv2.multiply(1.0, -0.5, 1.0));
               }
            }
         }

         boolean bl = MathHelper.floor(this.prevX) != MathHelper.floor(this.getX()) || MathHelper.floor(this.prevY) != MathHelper.floor(this.getY()) || MathHelper.floor(this.prevZ) != MathHelper.floor(this.getZ());
         int i = bl ? 2 : 40;
         if (this.age % i == 0 && !this.world.isClient && this.canMerge()) {
            this.tryMerge();
         }

         if (this.itemAge != -32768) {
            ++this.itemAge;
         }

         this.velocityDirty |= this.updateWaterState();
         if (!this.world.isClient) {
            double d = this.getVelocity().subtract(lv).lengthSquared();
            if (d > 0.01) {
               this.velocityDirty = true;
            }
         }

         if (!this.world.isClient && this.itemAge >= 6000) {
            this.discard();
         }

      }
   }

   private void applyWaterBuoyancy() {
      Vec3d lv = this.getVelocity();
      this.setVelocity(lv.x * 0.9900000095367432, lv.y + (double)(lv.y < 0.05999999865889549 ? 5.0E-4F : 0.0F), lv.z * 0.9900000095367432);
   }

   private void applyLavaBuoyancy() {
      Vec3d lv = this.getVelocity();
      this.setVelocity(lv.x * 0.949999988079071, lv.y + (double)(lv.y < 0.05999999865889549 ? 5.0E-4F : 0.0F), lv.z * 0.949999988079071);
   }

   private void tryMerge() {
      if (this.canMerge()) {
         List list = this.world.getEntitiesByClass(ItemEntity.class, this.getBoundingBox().expand(0.5, 0.0, 0.5), (otherItemEntity) -> {
            return otherItemEntity != this && otherItemEntity.canMerge();
         });
         Iterator var2 = list.iterator();

         while(var2.hasNext()) {
            ItemEntity lv = (ItemEntity)var2.next();
            if (lv.canMerge()) {
               this.tryMerge(lv);
               if (this.isRemoved()) {
                  break;
               }
            }
         }

      }
   }

   private boolean canMerge() {
      ItemStack lv = this.getStack();
      return this.isAlive() && this.pickupDelay != 32767 && this.itemAge != -32768 && this.itemAge < 6000 && lv.getCount() < lv.getMaxCount();
   }

   private void tryMerge(ItemEntity other) {
      ItemStack lv = this.getStack();
      ItemStack lv2 = other.getStack();
      if (Objects.equals(this.owner, other.owner) && canMerge(lv, lv2)) {
         if (lv2.getCount() < lv.getCount()) {
            merge(this, lv, other, lv2);
         } else {
            merge(other, lv2, this, lv);
         }

      }
   }

   public static boolean canMerge(ItemStack stack1, ItemStack stack2) {
      if (!stack2.isOf(stack1.getItem())) {
         return false;
      } else if (stack2.getCount() + stack1.getCount() > stack2.getMaxCount()) {
         return false;
      } else if (stack2.hasNbt() ^ stack1.hasNbt()) {
         return false;
      } else {
         return !stack2.hasNbt() || stack2.getNbt().equals(stack1.getNbt());
      }
   }

   public static ItemStack merge(ItemStack stack1, ItemStack stack2, int maxCount) {
      int j = Math.min(Math.min(stack1.getMaxCount(), maxCount) - stack1.getCount(), stack2.getCount());
      ItemStack lv = stack1.copyWithCount(stack1.getCount() + j);
      stack2.decrement(j);
      return lv;
   }

   private static void merge(ItemEntity targetEntity, ItemStack stack1, ItemStack stack2) {
      ItemStack lv = merge(stack1, stack2, 64);
      targetEntity.setStack(lv);
   }

   private static void merge(ItemEntity targetEntity, ItemStack targetStack, ItemEntity sourceEntity, ItemStack sourceStack) {
      merge(targetEntity, targetStack, sourceStack);
      targetEntity.pickupDelay = Math.max(targetEntity.pickupDelay, sourceEntity.pickupDelay);
      targetEntity.itemAge = Math.min(targetEntity.itemAge, sourceEntity.itemAge);
      if (sourceStack.isEmpty()) {
         sourceEntity.discard();
      }

   }

   public boolean isFireImmune() {
      return this.getStack().getItem().isFireproof() || super.isFireImmune();
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else if (!this.getStack().isEmpty() && this.getStack().isOf(Items.NETHER_STAR) && source.isIn(DamageTypeTags.IS_EXPLOSION)) {
         return false;
      } else if (!this.getStack().getItem().damage(source)) {
         return false;
      } else if (this.world.isClient) {
         return true;
      } else {
         this.scheduleVelocityUpdate();
         this.health = (int)((float)this.health - amount);
         this.emitGameEvent(GameEvent.ENTITY_DAMAGE, source.getAttacker());
         if (this.health <= 0) {
            this.getStack().onItemEntityDestroyed(this);
            this.discard();
         }

         return true;
      }
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      nbt.putShort("Health", (short)this.health);
      nbt.putShort("Age", (short)this.itemAge);
      nbt.putShort("PickupDelay", (short)this.pickupDelay);
      if (this.thrower != null) {
         nbt.putUuid("Thrower", this.thrower);
      }

      if (this.owner != null) {
         nbt.putUuid("Owner", this.owner);
      }

      if (!this.getStack().isEmpty()) {
         nbt.put("Item", this.getStack().writeNbt(new NbtCompound()));
      }

   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      this.health = nbt.getShort("Health");
      this.itemAge = nbt.getShort("Age");
      if (nbt.contains("PickupDelay")) {
         this.pickupDelay = nbt.getShort("PickupDelay");
      }

      if (nbt.containsUuid("Owner")) {
         this.owner = nbt.getUuid("Owner");
      }

      if (nbt.containsUuid("Thrower")) {
         this.thrower = nbt.getUuid("Thrower");
      }

      NbtCompound lv = nbt.getCompound("Item");
      this.setStack(ItemStack.fromNbt(lv));
      if (this.getStack().isEmpty()) {
         this.discard();
      }

   }

   public void onPlayerCollision(PlayerEntity player) {
      if (!this.world.isClient) {
         ItemStack lv = this.getStack();
         Item lv2 = lv.getItem();
         int i = lv.getCount();
         if (this.pickupDelay == 0 && (this.owner == null || this.owner.equals(player.getUuid())) && player.getInventory().insertStack(lv)) {
            player.sendPickup(this, i);
            if (lv.isEmpty()) {
               this.discard();
               lv.setCount(i);
            }

            player.increaseStat(Stats.PICKED_UP.getOrCreateStat(lv2), i);
            player.triggerItemPickedUpByEntityCriteria(this);
         }

      }
   }

   public Text getName() {
      Text lv = this.getCustomName();
      return (Text)(lv != null ? lv : Text.translatable(this.getStack().getTranslationKey()));
   }

   public boolean isAttackable() {
      return false;
   }

   @Nullable
   public Entity moveToWorld(ServerWorld destination) {
      Entity lv = super.moveToWorld(destination);
      if (!this.world.isClient && lv instanceof ItemEntity) {
         ((ItemEntity)lv).tryMerge();
      }

      return lv;
   }

   public ItemStack getStack() {
      return (ItemStack)this.getDataTracker().get(STACK);
   }

   public void setStack(ItemStack stack) {
      this.getDataTracker().set(STACK, stack);
   }

   public void onTrackedDataSet(TrackedData data) {
      super.onTrackedDataSet(data);
      if (STACK.equals(data)) {
         this.getStack().setHolder(this);
      }

   }

   public void setOwner(@Nullable UUID owner) {
      this.owner = owner;
   }

   public void setThrower(@Nullable UUID thrower) {
      this.thrower = thrower;
   }

   public int getItemAge() {
      return this.itemAge;
   }

   public void setToDefaultPickupDelay() {
      this.pickupDelay = 10;
   }

   public void resetPickupDelay() {
      this.pickupDelay = 0;
   }

   public void setPickupDelayInfinite() {
      this.pickupDelay = 32767;
   }

   public void setPickupDelay(int pickupDelay) {
      this.pickupDelay = pickupDelay;
   }

   public boolean cannotPickup() {
      return this.pickupDelay > 0;
   }

   public void setNeverDespawn() {
      this.itemAge = -32768;
   }

   public void setCovetedItem() {
      this.itemAge = -6000;
   }

   public void setDespawnImmediately() {
      this.setPickupDelayInfinite();
      this.itemAge = 5999;
   }

   public float getRotation(float tickDelta) {
      return ((float)this.getItemAge() + tickDelta) / 20.0F + this.uniqueOffset;
   }

   public ItemEntity copy() {
      return new ItemEntity(this);
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.AMBIENT;
   }

   public float getBodyYaw() {
      return 180.0F - this.getRotation(0.5F) / 6.2831855F * 360.0F;
   }

   static {
      STACK = DataTracker.registerData(ItemEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
   }
}
