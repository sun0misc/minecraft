package net.minecraft.entity.decoration;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class ArmorStandEntity extends LivingEntity {
   public static final int field_30443 = 5;
   private static final boolean field_30445 = true;
   private static final EulerAngle DEFAULT_HEAD_ROTATION = new EulerAngle(0.0F, 0.0F, 0.0F);
   private static final EulerAngle DEFAULT_BODY_ROTATION = new EulerAngle(0.0F, 0.0F, 0.0F);
   private static final EulerAngle DEFAULT_LEFT_ARM_ROTATION = new EulerAngle(-10.0F, 0.0F, -10.0F);
   private static final EulerAngle DEFAULT_RIGHT_ARM_ROTATION = new EulerAngle(-15.0F, 0.0F, 10.0F);
   private static final EulerAngle DEFAULT_LEFT_LEG_ROTATION = new EulerAngle(-1.0F, 0.0F, -1.0F);
   private static final EulerAngle DEFAULT_RIGHT_LEG_ROTATION = new EulerAngle(1.0F, 0.0F, 1.0F);
   private static final EntityDimensions MARKER_DIMENSIONS = new EntityDimensions(0.0F, 0.0F, true);
   private static final EntityDimensions SMALL_DIMENSIONS;
   private static final double field_30447 = 0.1;
   private static final double field_30448 = 0.9;
   private static final double field_30449 = 0.4;
   private static final double field_30450 = 1.6;
   public static final int field_30446 = 8;
   public static final int field_30451 = 16;
   public static final int SMALL_FLAG = 1;
   public static final int SHOW_ARMS_FLAG = 4;
   public static final int HIDE_BASE_PLATE_FLAG = 8;
   public static final int MARKER_FLAG = 16;
   public static final TrackedData ARMOR_STAND_FLAGS;
   public static final TrackedData TRACKER_HEAD_ROTATION;
   public static final TrackedData TRACKER_BODY_ROTATION;
   public static final TrackedData TRACKER_LEFT_ARM_ROTATION;
   public static final TrackedData TRACKER_RIGHT_ARM_ROTATION;
   public static final TrackedData TRACKER_LEFT_LEG_ROTATION;
   public static final TrackedData TRACKER_RIGHT_LEG_ROTATION;
   private static final Predicate RIDEABLE_MINECART_PREDICATE;
   private final DefaultedList heldItems;
   private final DefaultedList armorItems;
   private boolean invisible;
   public long lastHitTime;
   private int disabledSlots;
   private EulerAngle headRotation;
   private EulerAngle bodyRotation;
   private EulerAngle leftArmRotation;
   private EulerAngle rightArmRotation;
   private EulerAngle leftLegRotation;
   private EulerAngle rightLegRotation;

   public ArmorStandEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.heldItems = DefaultedList.ofSize(2, ItemStack.EMPTY);
      this.armorItems = DefaultedList.ofSize(4, ItemStack.EMPTY);
      this.headRotation = DEFAULT_HEAD_ROTATION;
      this.bodyRotation = DEFAULT_BODY_ROTATION;
      this.leftArmRotation = DEFAULT_LEFT_ARM_ROTATION;
      this.rightArmRotation = DEFAULT_RIGHT_ARM_ROTATION;
      this.leftLegRotation = DEFAULT_LEFT_LEG_ROTATION;
      this.rightLegRotation = DEFAULT_RIGHT_LEG_ROTATION;
      this.setStepHeight(0.0F);
   }

   public ArmorStandEntity(World world, double x, double y, double z) {
      this(EntityType.ARMOR_STAND, world);
      this.setPosition(x, y, z);
   }

   public void calculateDimensions() {
      double d = this.getX();
      double e = this.getY();
      double f = this.getZ();
      super.calculateDimensions();
      this.setPosition(d, e, f);
   }

   private boolean canClip() {
      return !this.isMarker() && !this.hasNoGravity();
   }

   public boolean canMoveVoluntarily() {
      return super.canMoveVoluntarily() && this.canClip();
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(ARMOR_STAND_FLAGS, (byte)0);
      this.dataTracker.startTracking(TRACKER_HEAD_ROTATION, DEFAULT_HEAD_ROTATION);
      this.dataTracker.startTracking(TRACKER_BODY_ROTATION, DEFAULT_BODY_ROTATION);
      this.dataTracker.startTracking(TRACKER_LEFT_ARM_ROTATION, DEFAULT_LEFT_ARM_ROTATION);
      this.dataTracker.startTracking(TRACKER_RIGHT_ARM_ROTATION, DEFAULT_RIGHT_ARM_ROTATION);
      this.dataTracker.startTracking(TRACKER_LEFT_LEG_ROTATION, DEFAULT_LEFT_LEG_ROTATION);
      this.dataTracker.startTracking(TRACKER_RIGHT_LEG_ROTATION, DEFAULT_RIGHT_LEG_ROTATION);
   }

   public Iterable getHandItems() {
      return this.heldItems;
   }

   public Iterable getArmorItems() {
      return this.armorItems;
   }

   public ItemStack getEquippedStack(EquipmentSlot slot) {
      switch (slot.getType()) {
         case HAND:
            return (ItemStack)this.heldItems.get(slot.getEntitySlotId());
         case ARMOR:
            return (ItemStack)this.armorItems.get(slot.getEntitySlotId());
         default:
            return ItemStack.EMPTY;
      }
   }

   public void equipStack(EquipmentSlot slot, ItemStack stack) {
      this.processEquippedStack(stack);
      switch (slot.getType()) {
         case HAND:
            this.onEquipStack(slot, (ItemStack)this.heldItems.set(slot.getEntitySlotId(), stack), stack);
            break;
         case ARMOR:
            this.onEquipStack(slot, (ItemStack)this.armorItems.set(slot.getEntitySlotId(), stack), stack);
      }

   }

   public boolean canEquip(ItemStack stack) {
      EquipmentSlot lv = MobEntity.getPreferredEquipmentSlot(stack);
      return this.getEquippedStack(lv).isEmpty() && !this.isSlotDisabled(lv);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      NbtList lv = new NbtList();

      NbtCompound lv3;
      for(Iterator var3 = this.armorItems.iterator(); var3.hasNext(); lv.add(lv3)) {
         ItemStack lv2 = (ItemStack)var3.next();
         lv3 = new NbtCompound();
         if (!lv2.isEmpty()) {
            lv2.writeNbt(lv3);
         }
      }

      nbt.put("ArmorItems", lv);
      NbtList lv4 = new NbtList();

      NbtCompound lv6;
      for(Iterator var8 = this.heldItems.iterator(); var8.hasNext(); lv4.add(lv6)) {
         ItemStack lv5 = (ItemStack)var8.next();
         lv6 = new NbtCompound();
         if (!lv5.isEmpty()) {
            lv5.writeNbt(lv6);
         }
      }

      nbt.put("HandItems", lv4);
      nbt.putBoolean("Invisible", this.isInvisible());
      nbt.putBoolean("Small", this.isSmall());
      nbt.putBoolean("ShowArms", this.shouldShowArms());
      nbt.putInt("DisabledSlots", this.disabledSlots);
      nbt.putBoolean("NoBasePlate", this.shouldHideBasePlate());
      if (this.isMarker()) {
         nbt.putBoolean("Marker", this.isMarker());
      }

      nbt.put("Pose", this.poseToNbt());
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      NbtList lv;
      int i;
      if (nbt.contains("ArmorItems", NbtElement.LIST_TYPE)) {
         lv = nbt.getList("ArmorItems", NbtElement.COMPOUND_TYPE);

         for(i = 0; i < this.armorItems.size(); ++i) {
            this.armorItems.set(i, ItemStack.fromNbt(lv.getCompound(i)));
         }
      }

      if (nbt.contains("HandItems", NbtElement.LIST_TYPE)) {
         lv = nbt.getList("HandItems", NbtElement.COMPOUND_TYPE);

         for(i = 0; i < this.heldItems.size(); ++i) {
            this.heldItems.set(i, ItemStack.fromNbt(lv.getCompound(i)));
         }
      }

      this.setInvisible(nbt.getBoolean("Invisible"));
      this.setSmall(nbt.getBoolean("Small"));
      this.setShowArms(nbt.getBoolean("ShowArms"));
      this.disabledSlots = nbt.getInt("DisabledSlots");
      this.setHideBasePlate(nbt.getBoolean("NoBasePlate"));
      this.setMarker(nbt.getBoolean("Marker"));
      this.noClip = !this.canClip();
      NbtCompound lv2 = nbt.getCompound("Pose");
      this.readPoseNbt(lv2);
   }

   private void readPoseNbt(NbtCompound nbt) {
      NbtList lv = nbt.getList("Head", NbtElement.FLOAT_TYPE);
      this.setHeadRotation(lv.isEmpty() ? DEFAULT_HEAD_ROTATION : new EulerAngle(lv));
      NbtList lv2 = nbt.getList("Body", NbtElement.FLOAT_TYPE);
      this.setBodyRotation(lv2.isEmpty() ? DEFAULT_BODY_ROTATION : new EulerAngle(lv2));
      NbtList lv3 = nbt.getList("LeftArm", NbtElement.FLOAT_TYPE);
      this.setLeftArmRotation(lv3.isEmpty() ? DEFAULT_LEFT_ARM_ROTATION : new EulerAngle(lv3));
      NbtList lv4 = nbt.getList("RightArm", NbtElement.FLOAT_TYPE);
      this.setRightArmRotation(lv4.isEmpty() ? DEFAULT_RIGHT_ARM_ROTATION : new EulerAngle(lv4));
      NbtList lv5 = nbt.getList("LeftLeg", NbtElement.FLOAT_TYPE);
      this.setLeftLegRotation(lv5.isEmpty() ? DEFAULT_LEFT_LEG_ROTATION : new EulerAngle(lv5));
      NbtList lv6 = nbt.getList("RightLeg", NbtElement.FLOAT_TYPE);
      this.setRightLegRotation(lv6.isEmpty() ? DEFAULT_RIGHT_LEG_ROTATION : new EulerAngle(lv6));
   }

   private NbtCompound poseToNbt() {
      NbtCompound lv = new NbtCompound();
      if (!DEFAULT_HEAD_ROTATION.equals(this.headRotation)) {
         lv.put("Head", this.headRotation.toNbt());
      }

      if (!DEFAULT_BODY_ROTATION.equals(this.bodyRotation)) {
         lv.put("Body", this.bodyRotation.toNbt());
      }

      if (!DEFAULT_LEFT_ARM_ROTATION.equals(this.leftArmRotation)) {
         lv.put("LeftArm", this.leftArmRotation.toNbt());
      }

      if (!DEFAULT_RIGHT_ARM_ROTATION.equals(this.rightArmRotation)) {
         lv.put("RightArm", this.rightArmRotation.toNbt());
      }

      if (!DEFAULT_LEFT_LEG_ROTATION.equals(this.leftLegRotation)) {
         lv.put("LeftLeg", this.leftLegRotation.toNbt());
      }

      if (!DEFAULT_RIGHT_LEG_ROTATION.equals(this.rightLegRotation)) {
         lv.put("RightLeg", this.rightLegRotation.toNbt());
      }

      return lv;
   }

   public boolean isPushable() {
      return false;
   }

   protected void pushAway(Entity entity) {
   }

   protected void tickCramming() {
      List list = this.world.getOtherEntities(this, this.getBoundingBox(), RIDEABLE_MINECART_PREDICATE);

      for(int i = 0; i < list.size(); ++i) {
         Entity lv = (Entity)list.get(i);
         if (this.squaredDistanceTo(lv) <= 0.2) {
            lv.pushAwayFrom(this);
         }
      }

   }

   public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
      ItemStack lv = player.getStackInHand(hand);
      if (!this.isMarker() && !lv.isOf(Items.NAME_TAG)) {
         if (player.isSpectator()) {
            return ActionResult.SUCCESS;
         } else if (player.world.isClient) {
            return ActionResult.CONSUME;
         } else {
            EquipmentSlot lv2 = MobEntity.getPreferredEquipmentSlot(lv);
            if (lv.isEmpty()) {
               EquipmentSlot lv3 = this.getSlotFromPosition(hitPos);
               EquipmentSlot lv4 = this.isSlotDisabled(lv3) ? lv2 : lv3;
               if (this.hasStackEquipped(lv4) && this.equip(player, lv4, lv, hand)) {
                  return ActionResult.SUCCESS;
               }
            } else {
               if (this.isSlotDisabled(lv2)) {
                  return ActionResult.FAIL;
               }

               if (lv2.getType() == EquipmentSlot.Type.HAND && !this.shouldShowArms()) {
                  return ActionResult.FAIL;
               }

               if (this.equip(player, lv2, lv, hand)) {
                  return ActionResult.SUCCESS;
               }
            }

            return ActionResult.PASS;
         }
      } else {
         return ActionResult.PASS;
      }
   }

   private EquipmentSlot getSlotFromPosition(Vec3d hitPos) {
      EquipmentSlot lv = EquipmentSlot.MAINHAND;
      boolean bl = this.isSmall();
      double d = bl ? hitPos.y * 2.0 : hitPos.y;
      EquipmentSlot lv2 = EquipmentSlot.FEET;
      if (d >= 0.1 && d < 0.1 + (bl ? 0.8 : 0.45) && this.hasStackEquipped(lv2)) {
         lv = EquipmentSlot.FEET;
      } else if (d >= 0.9 + (bl ? 0.3 : 0.0) && d < 0.9 + (bl ? 1.0 : 0.7) && this.hasStackEquipped(EquipmentSlot.CHEST)) {
         lv = EquipmentSlot.CHEST;
      } else if (d >= 0.4 && d < 0.4 + (bl ? 1.0 : 0.8) && this.hasStackEquipped(EquipmentSlot.LEGS)) {
         lv = EquipmentSlot.LEGS;
      } else if (d >= 1.6 && this.hasStackEquipped(EquipmentSlot.HEAD)) {
         lv = EquipmentSlot.HEAD;
      } else if (!this.hasStackEquipped(EquipmentSlot.MAINHAND) && this.hasStackEquipped(EquipmentSlot.OFFHAND)) {
         lv = EquipmentSlot.OFFHAND;
      }

      return lv;
   }

   private boolean isSlotDisabled(EquipmentSlot slot) {
      return (this.disabledSlots & 1 << slot.getArmorStandSlotId()) != 0 || slot.getType() == EquipmentSlot.Type.HAND && !this.shouldShowArms();
   }

   private boolean equip(PlayerEntity player, EquipmentSlot slot, ItemStack stack, Hand hand) {
      ItemStack lv = this.getEquippedStack(slot);
      if (!lv.isEmpty() && (this.disabledSlots & 1 << slot.getArmorStandSlotId() + 8) != 0) {
         return false;
      } else if (lv.isEmpty() && (this.disabledSlots & 1 << slot.getArmorStandSlotId() + 16) != 0) {
         return false;
      } else if (player.getAbilities().creativeMode && lv.isEmpty() && !stack.isEmpty()) {
         this.equipStack(slot, stack.copyWithCount(1));
         return true;
      } else if (!stack.isEmpty() && stack.getCount() > 1) {
         if (!lv.isEmpty()) {
            return false;
         } else {
            this.equipStack(slot, stack.split(1));
            return true;
         }
      } else {
         this.equipStack(slot, stack);
         player.setStackInHand(hand, lv);
         return true;
      }
   }

   public boolean damage(DamageSource source, float amount) {
      if (!this.world.isClient && !this.isRemoved()) {
         if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.kill();
            return false;
         } else if (!this.isInvulnerableTo(source) && !this.invisible && !this.isMarker()) {
            if (source.isIn(DamageTypeTags.IS_EXPLOSION)) {
               this.onBreak(source);
               this.kill();
               return false;
            } else if (source.isIn(DamageTypeTags.IGNITES_ARMOR_STANDS)) {
               if (this.isOnFire()) {
                  this.updateHealth(source, 0.15F);
               } else {
                  this.setOnFireFor(5);
               }

               return false;
            } else if (source.isIn(DamageTypeTags.BURNS_ARMOR_STANDS) && this.getHealth() > 0.5F) {
               this.updateHealth(source, 4.0F);
               return false;
            } else {
               boolean bl = source.getSource() instanceof PersistentProjectileEntity;
               boolean bl2 = bl && ((PersistentProjectileEntity)source.getSource()).getPierceLevel() > 0;
               boolean bl3 = "player".equals(source.getName());
               if (!bl3 && !bl) {
                  return false;
               } else {
                  Entity var7 = source.getAttacker();
                  if (var7 instanceof PlayerEntity) {
                     PlayerEntity lv = (PlayerEntity)var7;
                     if (!lv.getAbilities().allowModifyWorld) {
                        return false;
                     }
                  }

                  if (source.isSourceCreativePlayer()) {
                     this.playBreakSound();
                     this.spawnBreakParticles();
                     this.kill();
                     return bl2;
                  } else {
                     long l = this.world.getTime();
                     if (l - this.lastHitTime > 5L && !bl) {
                        this.world.sendEntityStatus(this, EntityStatuses.HIT_ARMOR_STAND);
                        this.emitGameEvent(GameEvent.ENTITY_DAMAGE, source.getAttacker());
                        this.lastHitTime = l;
                     } else {
                        this.breakAndDropItem(source);
                        this.spawnBreakParticles();
                        this.kill();
                     }

                     return true;
                  }
               }
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.HIT_ARMOR_STAND) {
         if (this.world.isClient) {
            this.world.playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ARMOR_STAND_HIT, this.getSoundCategory(), 0.3F, 1.0F, false);
            this.lastHitTime = this.world.getTime();
         }
      } else {
         super.handleStatus(status);
      }

   }

   public boolean shouldRender(double distance) {
      double e = this.getBoundingBox().getAverageSideLength() * 4.0;
      if (Double.isNaN(e) || e == 0.0) {
         e = 4.0;
      }

      e *= 64.0;
      return distance < e * e;
   }

   private void spawnBreakParticles() {
      if (this.world instanceof ServerWorld) {
         ((ServerWorld)this.world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.getDefaultState()), this.getX(), this.getBodyY(0.6666666666666666), this.getZ(), 10, (double)(this.getWidth() / 4.0F), (double)(this.getHeight() / 4.0F), (double)(this.getWidth() / 4.0F), 0.05);
      }

   }

   private void updateHealth(DamageSource damageSource, float amount) {
      float g = this.getHealth();
      g -= amount;
      if (g <= 0.5F) {
         this.onBreak(damageSource);
         this.kill();
      } else {
         this.setHealth(g);
         this.emitGameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getAttacker());
      }

   }

   private void breakAndDropItem(DamageSource damageSource) {
      ItemStack lv = new ItemStack(Items.ARMOR_STAND);
      if (this.hasCustomName()) {
         lv.setCustomName(this.getCustomName());
      }

      Block.dropStack(this.world, this.getBlockPos(), lv);
      this.onBreak(damageSource);
   }

   private void onBreak(DamageSource damageSource) {
      this.playBreakSound();
      this.drop(damageSource);

      int i;
      ItemStack lv;
      for(i = 0; i < this.heldItems.size(); ++i) {
         lv = (ItemStack)this.heldItems.get(i);
         if (!lv.isEmpty()) {
            Block.dropStack(this.world, this.getBlockPos().up(), lv);
            this.heldItems.set(i, ItemStack.EMPTY);
         }
      }

      for(i = 0; i < this.armorItems.size(); ++i) {
         lv = (ItemStack)this.armorItems.get(i);
         if (!lv.isEmpty()) {
            Block.dropStack(this.world, this.getBlockPos().up(), lv);
            this.armorItems.set(i, ItemStack.EMPTY);
         }
      }

   }

   private void playBreakSound() {
      this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ARMOR_STAND_BREAK, this.getSoundCategory(), 1.0F, 1.0F);
   }

   protected float turnHead(float bodyRotation, float headRotation) {
      this.prevBodyYaw = this.prevYaw;
      this.bodyYaw = this.getYaw();
      return 0.0F;
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return dimensions.height * (this.isBaby() ? 0.5F : 0.9F);
   }

   public double getHeightOffset() {
      return this.isMarker() ? 0.0 : 0.10000000149011612;
   }

   public void travel(Vec3d movementInput) {
      if (this.canClip()) {
         super.travel(movementInput);
      }
   }

   public void setBodyYaw(float bodyYaw) {
      this.prevBodyYaw = this.prevYaw = bodyYaw;
      this.prevHeadYaw = this.headYaw = bodyYaw;
   }

   public void setHeadYaw(float headYaw) {
      this.prevBodyYaw = this.prevYaw = headYaw;
      this.prevHeadYaw = this.headYaw = headYaw;
   }

   public void tick() {
      super.tick();
      EulerAngle lv = (EulerAngle)this.dataTracker.get(TRACKER_HEAD_ROTATION);
      if (!this.headRotation.equals(lv)) {
         this.setHeadRotation(lv);
      }

      EulerAngle lv2 = (EulerAngle)this.dataTracker.get(TRACKER_BODY_ROTATION);
      if (!this.bodyRotation.equals(lv2)) {
         this.setBodyRotation(lv2);
      }

      EulerAngle lv3 = (EulerAngle)this.dataTracker.get(TRACKER_LEFT_ARM_ROTATION);
      if (!this.leftArmRotation.equals(lv3)) {
         this.setLeftArmRotation(lv3);
      }

      EulerAngle lv4 = (EulerAngle)this.dataTracker.get(TRACKER_RIGHT_ARM_ROTATION);
      if (!this.rightArmRotation.equals(lv4)) {
         this.setRightArmRotation(lv4);
      }

      EulerAngle lv5 = (EulerAngle)this.dataTracker.get(TRACKER_LEFT_LEG_ROTATION);
      if (!this.leftLegRotation.equals(lv5)) {
         this.setLeftLegRotation(lv5);
      }

      EulerAngle lv6 = (EulerAngle)this.dataTracker.get(TRACKER_RIGHT_LEG_ROTATION);
      if (!this.rightLegRotation.equals(lv6)) {
         this.setRightLegRotation(lv6);
      }

   }

   protected void updatePotionVisibility() {
      this.setInvisible(this.invisible);
   }

   public void setInvisible(boolean invisible) {
      this.invisible = invisible;
      super.setInvisible(invisible);
   }

   public boolean isBaby() {
      return this.isSmall();
   }

   public void kill() {
      this.remove(Entity.RemovalReason.KILLED);
      this.emitGameEvent(GameEvent.ENTITY_DIE);
   }

   public boolean isImmuneToExplosion() {
      return this.isInvisible();
   }

   public PistonBehavior getPistonBehavior() {
      return this.isMarker() ? PistonBehavior.IGNORE : super.getPistonBehavior();
   }

   private void setSmall(boolean small) {
      this.dataTracker.set(ARMOR_STAND_FLAGS, this.setBitField((Byte)this.dataTracker.get(ARMOR_STAND_FLAGS), SMALL_FLAG, small));
   }

   public boolean isSmall() {
      return ((Byte)this.dataTracker.get(ARMOR_STAND_FLAGS) & 1) != 0;
   }

   public void setShowArms(boolean showArms) {
      this.dataTracker.set(ARMOR_STAND_FLAGS, this.setBitField((Byte)this.dataTracker.get(ARMOR_STAND_FLAGS), SHOW_ARMS_FLAG, showArms));
   }

   public boolean shouldShowArms() {
      return ((Byte)this.dataTracker.get(ARMOR_STAND_FLAGS) & 4) != 0;
   }

   public void setHideBasePlate(boolean hideBasePlate) {
      this.dataTracker.set(ARMOR_STAND_FLAGS, this.setBitField((Byte)this.dataTracker.get(ARMOR_STAND_FLAGS), HIDE_BASE_PLATE_FLAG, hideBasePlate));
   }

   public boolean shouldHideBasePlate() {
      return ((Byte)this.dataTracker.get(ARMOR_STAND_FLAGS) & 8) != 0;
   }

   private void setMarker(boolean marker) {
      this.dataTracker.set(ARMOR_STAND_FLAGS, this.setBitField((Byte)this.dataTracker.get(ARMOR_STAND_FLAGS), MARKER_FLAG, marker));
   }

   public boolean isMarker() {
      return ((Byte)this.dataTracker.get(ARMOR_STAND_FLAGS) & 16) != 0;
   }

   private byte setBitField(byte value, int bitField, boolean set) {
      if (set) {
         value = (byte)(value | bitField);
      } else {
         value = (byte)(value & ~bitField);
      }

      return value;
   }

   public void setHeadRotation(EulerAngle angle) {
      this.headRotation = angle;
      this.dataTracker.set(TRACKER_HEAD_ROTATION, angle);
   }

   public void setBodyRotation(EulerAngle angle) {
      this.bodyRotation = angle;
      this.dataTracker.set(TRACKER_BODY_ROTATION, angle);
   }

   public void setLeftArmRotation(EulerAngle angle) {
      this.leftArmRotation = angle;
      this.dataTracker.set(TRACKER_LEFT_ARM_ROTATION, angle);
   }

   public void setRightArmRotation(EulerAngle angle) {
      this.rightArmRotation = angle;
      this.dataTracker.set(TRACKER_RIGHT_ARM_ROTATION, angle);
   }

   public void setLeftLegRotation(EulerAngle angle) {
      this.leftLegRotation = angle;
      this.dataTracker.set(TRACKER_LEFT_LEG_ROTATION, angle);
   }

   public void setRightLegRotation(EulerAngle angle) {
      this.rightLegRotation = angle;
      this.dataTracker.set(TRACKER_RIGHT_LEG_ROTATION, angle);
   }

   public EulerAngle getHeadRotation() {
      return this.headRotation;
   }

   public EulerAngle getBodyRotation() {
      return this.bodyRotation;
   }

   public EulerAngle getLeftArmRotation() {
      return this.leftArmRotation;
   }

   public EulerAngle getRightArmRotation() {
      return this.rightArmRotation;
   }

   public EulerAngle getLeftLegRotation() {
      return this.leftLegRotation;
   }

   public EulerAngle getRightLegRotation() {
      return this.rightLegRotation;
   }

   public boolean canHit() {
      return super.canHit() && !this.isMarker();
   }

   public boolean handleAttack(Entity attacker) {
      return attacker instanceof PlayerEntity && !this.world.canPlayerModifyAt((PlayerEntity)attacker, this.getBlockPos());
   }

   public Arm getMainArm() {
      return Arm.RIGHT;
   }

   public LivingEntity.FallSounds getFallSounds() {
      return new LivingEntity.FallSounds(SoundEvents.ENTITY_ARMOR_STAND_FALL, SoundEvents.ENTITY_ARMOR_STAND_FALL);
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_ARMOR_STAND_HIT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_ARMOR_STAND_BREAK;
   }

   public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
   }

   public boolean isAffectedBySplashPotions() {
      return false;
   }

   public void onTrackedDataSet(TrackedData data) {
      if (ARMOR_STAND_FLAGS.equals(data)) {
         this.calculateDimensions();
         this.intersectionChecked = !this.isMarker();
      }

      super.onTrackedDataSet(data);
   }

   public boolean isMobOrPlayer() {
      return false;
   }

   public EntityDimensions getDimensions(EntityPose pose) {
      return this.getDimensions(this.isMarker());
   }

   private EntityDimensions getDimensions(boolean marker) {
      if (marker) {
         return MARKER_DIMENSIONS;
      } else {
         return this.isBaby() ? SMALL_DIMENSIONS : this.getType().getDimensions();
      }
   }

   public Vec3d getClientCameraPosVec(float tickDelta) {
      if (this.isMarker()) {
         Box lv = this.getDimensions(false).getBoxAt(this.getPos());
         BlockPos lv2 = this.getBlockPos();
         int i = Integer.MIN_VALUE;
         Iterator var5 = BlockPos.iterate(BlockPos.ofFloored(lv.minX, lv.minY, lv.minZ), BlockPos.ofFloored(lv.maxX, lv.maxY, lv.maxZ)).iterator();

         while(var5.hasNext()) {
            BlockPos lv3 = (BlockPos)var5.next();
            int j = Math.max(this.world.getLightLevel(LightType.BLOCK, lv3), this.world.getLightLevel(LightType.SKY, lv3));
            if (j == 15) {
               return Vec3d.ofCenter(lv3);
            }

            if (j > i) {
               i = j;
               lv2 = lv3.toImmutable();
            }
         }

         return Vec3d.ofCenter(lv2);
      } else {
         return super.getClientCameraPosVec(tickDelta);
      }
   }

   public ItemStack getPickBlockStack() {
      return new ItemStack(Items.ARMOR_STAND);
   }

   public boolean isPartOfGame() {
      return !this.isInvisible() && !this.isMarker();
   }

   static {
      SMALL_DIMENSIONS = EntityType.ARMOR_STAND.getDimensions().scaled(0.5F);
      ARMOR_STAND_FLAGS = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.BYTE);
      TRACKER_HEAD_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
      TRACKER_BODY_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
      TRACKER_LEFT_ARM_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
      TRACKER_RIGHT_ARM_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
      TRACKER_LEFT_LEG_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
      TRACKER_RIGHT_LEG_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
      RIDEABLE_MINECART_PREDICATE = (entity) -> {
         return entity instanceof AbstractMinecartEntity && ((AbstractMinecartEntity)entity).getMinecartType() == AbstractMinecartEntity.Type.RIDEABLE;
      };
   }
}
