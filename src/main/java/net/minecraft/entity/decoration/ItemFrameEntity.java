package net.minecraft.entity.decoration;

import com.mojang.logging.LogUtils;
import java.util.OptionalInt;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ItemFrameEntity extends AbstractDecorationEntity {
   private static final Logger ITEM_FRAME_LOGGER = LogUtils.getLogger();
   private static final TrackedData ITEM_STACK;
   private static final TrackedData ROTATION;
   public static final int field_30454 = 8;
   private float itemDropChance;
   private boolean fixed;

   public ItemFrameEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.itemDropChance = 1.0F;
   }

   public ItemFrameEntity(World world, BlockPos pos, Direction facing) {
      this(EntityType.ITEM_FRAME, world, pos, facing);
   }

   public ItemFrameEntity(EntityType type, World world, BlockPos pos, Direction facing) {
      super(type, world, pos);
      this.itemDropChance = 1.0F;
      this.setFacing(facing);
   }

   protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 0.0F;
   }

   protected void initDataTracker() {
      this.getDataTracker().startTracking(ITEM_STACK, ItemStack.EMPTY);
      this.getDataTracker().startTracking(ROTATION, 0);
   }

   protected void setFacing(Direction facing) {
      Validate.notNull(facing);
      this.facing = facing;
      if (facing.getAxis().isHorizontal()) {
         this.setPitch(0.0F);
         this.setYaw((float)(this.facing.getHorizontal() * 90));
      } else {
         this.setPitch((float)(-90 * facing.getDirection().offset()));
         this.setYaw(0.0F);
      }

      this.prevPitch = this.getPitch();
      this.prevYaw = this.getYaw();
      this.updateAttachmentPosition();
   }

   protected void updateAttachmentPosition() {
      if (this.facing != null) {
         double d = 0.46875;
         double e = (double)this.attachmentPos.getX() + 0.5 - (double)this.facing.getOffsetX() * 0.46875;
         double f = (double)this.attachmentPos.getY() + 0.5 - (double)this.facing.getOffsetY() * 0.46875;
         double g = (double)this.attachmentPos.getZ() + 0.5 - (double)this.facing.getOffsetZ() * 0.46875;
         this.setPos(e, f, g);
         double h = (double)this.getWidthPixels();
         double i = (double)this.getHeightPixels();
         double j = (double)this.getWidthPixels();
         Direction.Axis lv = this.facing.getAxis();
         switch (lv) {
            case X:
               h = 1.0;
               break;
            case Y:
               i = 1.0;
               break;
            case Z:
               j = 1.0;
         }

         h /= 32.0;
         i /= 32.0;
         j /= 32.0;
         this.setBoundingBox(new Box(e - h, f - i, g - j, e + h, f + i, g + j));
      }
   }

   public boolean canStayAttached() {
      if (this.fixed) {
         return true;
      } else if (!this.world.isSpaceEmpty(this)) {
         return false;
      } else {
         BlockState lv = this.world.getBlockState(this.attachmentPos.offset(this.facing.getOpposite()));
         return lv.getMaterial().isSolid() || this.facing.getAxis().isHorizontal() && AbstractRedstoneGateBlock.isRedstoneGate(lv) ? this.world.getOtherEntities(this, this.getBoundingBox(), PREDICATE).isEmpty() : false;
      }
   }

   public void move(MovementType movementType, Vec3d movement) {
      if (!this.fixed) {
         super.move(movementType, movement);
      }

   }

   public void addVelocity(double deltaX, double deltaY, double deltaZ) {
      if (!this.fixed) {
         super.addVelocity(deltaX, deltaY, deltaZ);
      }

   }

   public float getTargetingMargin() {
      return 0.0F;
   }

   public void kill() {
      this.removeFromFrame(this.getHeldItemStack());
      super.kill();
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.fixed) {
         return !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) && !source.isSourceCreativePlayer() ? false : super.damage(source, amount);
      } else if (this.isInvulnerableTo(source)) {
         return false;
      } else if (!source.isIn(DamageTypeTags.IS_EXPLOSION) && !this.getHeldItemStack().isEmpty()) {
         if (!this.world.isClient) {
            this.dropHeldStack(source.getAttacker(), false);
            this.emitGameEvent(GameEvent.BLOCK_CHANGE, source.getAttacker());
            this.playSound(this.getRemoveItemSound(), 1.0F, 1.0F);
         }

         return true;
      } else {
         return super.damage(source, amount);
      }
   }

   public SoundEvent getRemoveItemSound() {
      return SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM;
   }

   public int getWidthPixels() {
      return 12;
   }

   public int getHeightPixels() {
      return 12;
   }

   public boolean shouldRender(double distance) {
      double e = 16.0;
      e *= 64.0 * getRenderDistanceMultiplier();
      return distance < e * e;
   }

   public void onBreak(@Nullable Entity entity) {
      this.playSound(this.getBreakSound(), 1.0F, 1.0F);
      this.dropHeldStack(entity, true);
      this.emitGameEvent(GameEvent.BLOCK_CHANGE, entity);
   }

   public SoundEvent getBreakSound() {
      return SoundEvents.ENTITY_ITEM_FRAME_BREAK;
   }

   public void onPlace() {
      this.playSound(this.getPlaceSound(), 1.0F, 1.0F);
   }

   public SoundEvent getPlaceSound() {
      return SoundEvents.ENTITY_ITEM_FRAME_PLACE;
   }

   private void dropHeldStack(@Nullable Entity entity, boolean alwaysDrop) {
      if (!this.fixed) {
         ItemStack lv = this.getHeldItemStack();
         this.setHeldItemStack(ItemStack.EMPTY);
         if (!this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            if (entity == null) {
               this.removeFromFrame(lv);
            }

         } else {
            if (entity instanceof PlayerEntity) {
               PlayerEntity lv2 = (PlayerEntity)entity;
               if (lv2.getAbilities().creativeMode) {
                  this.removeFromFrame(lv);
                  return;
               }
            }

            if (alwaysDrop) {
               this.dropStack(this.getAsItemStack());
            }

            if (!lv.isEmpty()) {
               lv = lv.copy();
               this.removeFromFrame(lv);
               if (this.random.nextFloat() < this.itemDropChance) {
                  this.dropStack(lv);
               }
            }

         }
      }
   }

   private void removeFromFrame(ItemStack arg) {
      this.getMapId().ifPresent((i) -> {
         MapState lv = FilledMapItem.getMapState(i, this.world);
         if (lv != null) {
            lv.removeFrame(this.attachmentPos, this.getId());
            lv.setDirty(true);
         }

      });
      arg.setHolder((Entity)null);
   }

   public ItemStack getHeldItemStack() {
      return (ItemStack)this.getDataTracker().get(ITEM_STACK);
   }

   public OptionalInt getMapId() {
      ItemStack lv = this.getHeldItemStack();
      if (lv.isOf(Items.FILLED_MAP)) {
         Integer integer = FilledMapItem.getMapId(lv);
         if (integer != null) {
            return OptionalInt.of(integer);
         }
      }

      return OptionalInt.empty();
   }

   public boolean containsMap() {
      return this.getMapId().isPresent();
   }

   public void setHeldItemStack(ItemStack stack) {
      this.setHeldItemStack(stack, true);
   }

   public void setHeldItemStack(ItemStack value, boolean update) {
      if (!value.isEmpty()) {
         value = value.copyWithCount(1);
      }

      this.setAsStackHolder(value);
      this.getDataTracker().set(ITEM_STACK, value);
      if (!value.isEmpty()) {
         this.playSound(this.getAddItemSound(), 1.0F, 1.0F);
      }

      if (update && this.attachmentPos != null) {
         this.world.updateComparators(this.attachmentPos, Blocks.AIR);
      }

   }

   public SoundEvent getAddItemSound() {
      return SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM;
   }

   public StackReference getStackReference(int mappedIndex) {
      return mappedIndex == 0 ? new StackReference() {
         public ItemStack get() {
            return ItemFrameEntity.this.getHeldItemStack();
         }

         public boolean set(ItemStack stack) {
            ItemFrameEntity.this.setHeldItemStack(stack);
            return true;
         }
      } : super.getStackReference(mappedIndex);
   }

   public void onTrackedDataSet(TrackedData data) {
      if (data.equals(ITEM_STACK)) {
         this.setAsStackHolder(this.getHeldItemStack());
      }

   }

   private void setAsStackHolder(ItemStack stack) {
      if (!stack.isEmpty() && stack.getFrame() != this) {
         stack.setHolder(this);
      }

      this.updateAttachmentPosition();
   }

   public int getRotation() {
      return (Integer)this.getDataTracker().get(ROTATION);
   }

   public void setRotation(int value) {
      this.setRotation(value, true);
   }

   private void setRotation(int value, boolean updateComparators) {
      this.getDataTracker().set(ROTATION, value % 8);
      if (updateComparators && this.attachmentPos != null) {
         this.world.updateComparators(this.attachmentPos, Blocks.AIR);
      }

   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      if (!this.getHeldItemStack().isEmpty()) {
         nbt.put("Item", this.getHeldItemStack().writeNbt(new NbtCompound()));
         nbt.putByte("ItemRotation", (byte)this.getRotation());
         nbt.putFloat("ItemDropChance", this.itemDropChance);
      }

      nbt.putByte("Facing", (byte)this.facing.getId());
      nbt.putBoolean("Invisible", this.isInvisible());
      nbt.putBoolean("Fixed", this.fixed);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      NbtCompound lv = nbt.getCompound("Item");
      if (lv != null && !lv.isEmpty()) {
         ItemStack lv2 = ItemStack.fromNbt(lv);
         if (lv2.isEmpty()) {
            ITEM_FRAME_LOGGER.warn("Unable to load item from: {}", lv);
         }

         ItemStack lv3 = this.getHeldItemStack();
         if (!lv3.isEmpty() && !ItemStack.areEqual(lv2, lv3)) {
            this.removeFromFrame(lv3);
         }

         this.setHeldItemStack(lv2, false);
         this.setRotation(nbt.getByte("ItemRotation"), false);
         if (nbt.contains("ItemDropChance", NbtElement.NUMBER_TYPE)) {
            this.itemDropChance = nbt.getFloat("ItemDropChance");
         }
      }

      this.setFacing(Direction.byId(nbt.getByte("Facing")));
      this.setInvisible(nbt.getBoolean("Invisible"));
      this.fixed = nbt.getBoolean("Fixed");
   }

   public ActionResult interact(PlayerEntity player, Hand hand) {
      ItemStack lv = player.getStackInHand(hand);
      boolean bl = !this.getHeldItemStack().isEmpty();
      boolean bl2 = !lv.isEmpty();
      if (this.fixed) {
         return ActionResult.PASS;
      } else if (!this.world.isClient) {
         if (!bl) {
            if (bl2 && !this.isRemoved()) {
               if (lv.isOf(Items.FILLED_MAP)) {
                  MapState lv2 = FilledMapItem.getMapState(lv, this.world);
                  if (lv2 != null && lv2.iconCountNotLessThan(256)) {
                     return ActionResult.FAIL;
                  }
               }

               this.setHeldItemStack(lv);
               this.emitGameEvent(GameEvent.BLOCK_CHANGE, player);
               if (!player.getAbilities().creativeMode) {
                  lv.decrement(1);
               }
            }
         } else {
            this.playSound(this.getRotateItemSound(), 1.0F, 1.0F);
            this.setRotation(this.getRotation() + 1);
            this.emitGameEvent(GameEvent.BLOCK_CHANGE, player);
         }

         return ActionResult.CONSUME;
      } else {
         return !bl && !bl2 ? ActionResult.PASS : ActionResult.SUCCESS;
      }
   }

   public SoundEvent getRotateItemSound() {
      return SoundEvents.ENTITY_ITEM_FRAME_ROTATE_ITEM;
   }

   public int getComparatorPower() {
      return this.getHeldItemStack().isEmpty() ? 0 : this.getRotation() % 8 + 1;
   }

   public Packet createSpawnPacket() {
      return new EntitySpawnS2CPacket(this, this.facing.getId(), this.getDecorationBlockPos());
   }

   public void onSpawnPacket(EntitySpawnS2CPacket packet) {
      super.onSpawnPacket(packet);
      this.setFacing(Direction.byId(packet.getEntityData()));
   }

   public ItemStack getPickBlockStack() {
      ItemStack lv = this.getHeldItemStack();
      return lv.isEmpty() ? this.getAsItemStack() : lv.copy();
   }

   protected ItemStack getAsItemStack() {
      return new ItemStack(Items.ITEM_FRAME);
   }

   public float getBodyYaw() {
      Direction lv = this.getHorizontalFacing();
      int i = lv.getAxis().isVertical() ? 90 * lv.getDirection().offset() : 0;
      return (float)MathHelper.wrapDegrees(180 + lv.getHorizontal() * 90 + this.getRotation() * 45 + i);
   }

   static {
      ITEM_STACK = DataTracker.registerData(ItemFrameEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
      ROTATION = DataTracker.registerData(ItemFrameEntity.class, TrackedDataHandlerRegistry.INTEGER);
   }
}
