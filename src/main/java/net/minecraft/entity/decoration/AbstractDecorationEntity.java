package net.minecraft.entity.decoration;

import com.mojang.logging.LogUtils;
import java.util.function.Predicate;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class AbstractDecorationEntity extends Entity {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected static final Predicate PREDICATE = (entity) -> {
      return entity instanceof AbstractDecorationEntity;
   };
   private int obstructionCheckCounter;
   protected BlockPos attachmentPos;
   protected Direction facing;

   protected AbstractDecorationEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.facing = Direction.SOUTH;
   }

   protected AbstractDecorationEntity(EntityType type, World world, BlockPos pos) {
      this(type, world);
      this.attachmentPos = pos;
   }

   protected void initDataTracker() {
   }

   protected void setFacing(Direction facing) {
      Validate.notNull(facing);
      Validate.isTrue(facing.getAxis().isHorizontal());
      this.facing = facing;
      this.setYaw((float)(this.facing.getHorizontal() * 90));
      this.prevYaw = this.getYaw();
      this.updateAttachmentPosition();
   }

   protected void updateAttachmentPosition() {
      if (this.facing != null) {
         double d = (double)this.attachmentPos.getX() + 0.5;
         double e = (double)this.attachmentPos.getY() + 0.5;
         double f = (double)this.attachmentPos.getZ() + 0.5;
         double g = 0.46875;
         double h = this.method_6893(this.getWidthPixels());
         double i = this.method_6893(this.getHeightPixels());
         d -= (double)this.facing.getOffsetX() * 0.46875;
         f -= (double)this.facing.getOffsetZ() * 0.46875;
         e += i;
         Direction lv = this.facing.rotateYCounterclockwise();
         d += h * (double)lv.getOffsetX();
         f += h * (double)lv.getOffsetZ();
         this.setPos(d, e, f);
         double j = (double)this.getWidthPixels();
         double k = (double)this.getHeightPixels();
         double l = (double)this.getWidthPixels();
         if (this.facing.getAxis() == Direction.Axis.Z) {
            l = 1.0;
         } else {
            j = 1.0;
         }

         j /= 32.0;
         k /= 32.0;
         l /= 32.0;
         this.setBoundingBox(new Box(d - j, e - k, f - l, d + j, e + k, f + l));
      }
   }

   private double method_6893(int i) {
      return i % 32 == 0 ? 0.5 : 0.0;
   }

   public void tick() {
      if (!this.world.isClient) {
         this.attemptTickInVoid();
         if (this.obstructionCheckCounter++ == 100) {
            this.obstructionCheckCounter = 0;
            if (!this.isRemoved() && !this.canStayAttached()) {
               this.discard();
               this.onBreak((Entity)null);
            }
         }
      }

   }

   public boolean canStayAttached() {
      if (!this.world.isSpaceEmpty(this)) {
         return false;
      } else {
         int i = Math.max(1, this.getWidthPixels() / 16);
         int j = Math.max(1, this.getHeightPixels() / 16);
         BlockPos lv = this.attachmentPos.offset(this.facing.getOpposite());
         Direction lv2 = this.facing.rotateYCounterclockwise();
         BlockPos.Mutable lv3 = new BlockPos.Mutable();

         for(int k = 0; k < i; ++k) {
            for(int l = 0; l < j; ++l) {
               int m = (i - 1) / -2;
               int n = (j - 1) / -2;
               lv3.set(lv).move(lv2, k + m).move(Direction.UP, l + n);
               BlockState lv4 = this.world.getBlockState(lv3);
               if (!lv4.getMaterial().isSolid() && !AbstractRedstoneGateBlock.isRedstoneGate(lv4)) {
                  return false;
               }
            }
         }

         return this.world.getOtherEntities(this, this.getBoundingBox(), PREDICATE).isEmpty();
      }
   }

   public boolean canHit() {
      return true;
   }

   public boolean handleAttack(Entity attacker) {
      if (attacker instanceof PlayerEntity lv) {
         return !this.world.canPlayerModifyAt(lv, this.attachmentPos) ? true : this.damage(this.getDamageSources().playerAttack(lv), 0.0F);
      } else {
         return false;
      }
   }

   public Direction getHorizontalFacing() {
      return this.facing;
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else {
         if (!this.isRemoved() && !this.world.isClient) {
            this.kill();
            this.scheduleVelocityUpdate();
            this.onBreak(source.getAttacker());
         }

         return true;
      }
   }

   public void move(MovementType movementType, Vec3d movement) {
      if (!this.world.isClient && !this.isRemoved() && movement.lengthSquared() > 0.0) {
         this.kill();
         this.onBreak((Entity)null);
      }

   }

   public void addVelocity(double deltaX, double deltaY, double deltaZ) {
      if (!this.world.isClient && !this.isRemoved() && deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > 0.0) {
         this.kill();
         this.onBreak((Entity)null);
      }

   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      BlockPos lv = this.getDecorationBlockPos();
      nbt.putInt("TileX", lv.getX());
      nbt.putInt("TileY", lv.getY());
      nbt.putInt("TileZ", lv.getZ());
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      BlockPos lv = new BlockPos(nbt.getInt("TileX"), nbt.getInt("TileY"), nbt.getInt("TileZ"));
      if (!lv.isWithinDistance(this.getBlockPos(), 16.0)) {
         LOGGER.error("Hanging entity at invalid position: {}", lv);
      } else {
         this.attachmentPos = lv;
      }
   }

   public abstract int getWidthPixels();

   public abstract int getHeightPixels();

   public abstract void onBreak(@Nullable Entity entity);

   public abstract void onPlace();

   public ItemEntity dropStack(ItemStack stack, float yOffset) {
      ItemEntity lv = new ItemEntity(this.world, this.getX() + (double)((float)this.facing.getOffsetX() * 0.15F), this.getY() + (double)yOffset, this.getZ() + (double)((float)this.facing.getOffsetZ() * 0.15F), stack);
      lv.setToDefaultPickupDelay();
      this.world.spawnEntity(lv);
      return lv;
   }

   protected boolean shouldSetPositionOnLoad() {
      return false;
   }

   public void setPosition(double x, double y, double z) {
      this.attachmentPos = BlockPos.ofFloored(x, y, z);
      this.updateAttachmentPosition();
      this.velocityDirty = true;
   }

   public BlockPos getDecorationBlockPos() {
      return this.attachmentPos;
   }

   public float applyRotation(BlockRotation rotation) {
      if (this.facing.getAxis() != Direction.Axis.Y) {
         switch (rotation) {
            case CLOCKWISE_180:
               this.facing = this.facing.getOpposite();
               break;
            case COUNTERCLOCKWISE_90:
               this.facing = this.facing.rotateYCounterclockwise();
               break;
            case CLOCKWISE_90:
               this.facing = this.facing.rotateYClockwise();
         }
      }

      float f = MathHelper.wrapDegrees(this.getYaw());
      switch (rotation) {
         case CLOCKWISE_180:
            return f + 180.0F;
         case COUNTERCLOCKWISE_90:
            return f + 90.0F;
         case CLOCKWISE_90:
            return f + 270.0F;
         default:
            return f;
      }
   }

   public float applyMirror(BlockMirror mirror) {
      return this.applyRotation(mirror.getRotation(this.facing));
   }

   public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
   }

   public void calculateDimensions() {
   }
}
