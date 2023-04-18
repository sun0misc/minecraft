package net.minecraft.client.render;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class Camera {
   private boolean ready;
   private BlockView area;
   private Entity focusedEntity;
   private Vec3d pos;
   private final BlockPos.Mutable blockPos;
   private final Vector3f horizontalPlane;
   private final Vector3f verticalPlane;
   private final Vector3f diagonalPlane;
   private float pitch;
   private float yaw;
   private final Quaternionf rotation;
   private boolean thirdPerson;
   private float cameraY;
   private float lastCameraY;
   public static final float field_32133 = 0.083333336F;

   public Camera() {
      this.pos = Vec3d.ZERO;
      this.blockPos = new BlockPos.Mutable();
      this.horizontalPlane = new Vector3f(0.0F, 0.0F, 1.0F);
      this.verticalPlane = new Vector3f(0.0F, 1.0F, 0.0F);
      this.diagonalPlane = new Vector3f(1.0F, 0.0F, 0.0F);
      this.rotation = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
   }

   public void update(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta) {
      this.ready = true;
      this.area = area;
      this.focusedEntity = focusedEntity;
      this.thirdPerson = thirdPerson;
      this.setRotation(focusedEntity.getYaw(tickDelta), focusedEntity.getPitch(tickDelta));
      this.setPos(MathHelper.lerp((double)tickDelta, focusedEntity.prevX, focusedEntity.getX()), MathHelper.lerp((double)tickDelta, focusedEntity.prevY, focusedEntity.getY()) + (double)MathHelper.lerp(tickDelta, this.lastCameraY, this.cameraY), MathHelper.lerp((double)tickDelta, focusedEntity.prevZ, focusedEntity.getZ()));
      if (thirdPerson) {
         if (inverseView) {
            this.setRotation(this.yaw + 180.0F, -this.pitch);
         }

         this.moveBy(-this.clipToSpace(4.0), 0.0, 0.0);
      } else if (focusedEntity instanceof LivingEntity && ((LivingEntity)focusedEntity).isSleeping()) {
         Direction lv = ((LivingEntity)focusedEntity).getSleepingDirection();
         this.setRotation(lv != null ? lv.asRotation() - 180.0F : 0.0F, 0.0F);
         this.moveBy(0.0, 0.3, 0.0);
      }

   }

   public void updateEyeHeight() {
      if (this.focusedEntity != null) {
         this.lastCameraY = this.cameraY;
         this.cameraY += (this.focusedEntity.getStandingEyeHeight() - this.cameraY) * 0.5F;
      }

   }

   private double clipToSpace(double desiredCameraDistance) {
      for(int i = 0; i < 8; ++i) {
         float f = (float)((i & 1) * 2 - 1);
         float g = (float)((i >> 1 & 1) * 2 - 1);
         float h = (float)((i >> 2 & 1) * 2 - 1);
         f *= 0.1F;
         g *= 0.1F;
         h *= 0.1F;
         Vec3d lv = this.pos.add((double)f, (double)g, (double)h);
         Vec3d lv2 = new Vec3d(this.pos.x - (double)this.horizontalPlane.x() * desiredCameraDistance + (double)f, this.pos.y - (double)this.horizontalPlane.y() * desiredCameraDistance + (double)g, this.pos.z - (double)this.horizontalPlane.z() * desiredCameraDistance + (double)h);
         HitResult lv3 = this.area.raycast(new RaycastContext(lv, lv2, RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.NONE, this.focusedEntity));
         if (lv3.getType() != HitResult.Type.MISS) {
            double e = lv3.getPos().distanceTo(this.pos);
            if (e < desiredCameraDistance) {
               desiredCameraDistance = e;
            }
         }
      }

      return desiredCameraDistance;
   }

   protected void moveBy(double x, double y, double z) {
      double g = (double)this.horizontalPlane.x() * x + (double)this.verticalPlane.x() * y + (double)this.diagonalPlane.x() * z;
      double h = (double)this.horizontalPlane.y() * x + (double)this.verticalPlane.y() * y + (double)this.diagonalPlane.y() * z;
      double i = (double)this.horizontalPlane.z() * x + (double)this.verticalPlane.z() * y + (double)this.diagonalPlane.z() * z;
      this.setPos(new Vec3d(this.pos.x + g, this.pos.y + h, this.pos.z + i));
   }

   protected void setRotation(float yaw, float pitch) {
      this.pitch = pitch;
      this.yaw = yaw;
      this.rotation.rotationYXZ(-yaw * 0.017453292F, pitch * 0.017453292F, 0.0F);
      this.horizontalPlane.set(0.0F, 0.0F, 1.0F).rotate(this.rotation);
      this.verticalPlane.set(0.0F, 1.0F, 0.0F).rotate(this.rotation);
      this.diagonalPlane.set(1.0F, 0.0F, 0.0F).rotate(this.rotation);
   }

   protected void setPos(double x, double y, double z) {
      this.setPos(new Vec3d(x, y, z));
   }

   protected void setPos(Vec3d pos) {
      this.pos = pos;
      this.blockPos.set(pos.x, pos.y, pos.z);
   }

   public Vec3d getPos() {
      return this.pos;
   }

   public BlockPos getBlockPos() {
      return this.blockPos;
   }

   public float getPitch() {
      return this.pitch;
   }

   public float getYaw() {
      return this.yaw;
   }

   public Quaternionf getRotation() {
      return this.rotation;
   }

   public Entity getFocusedEntity() {
      return this.focusedEntity;
   }

   public boolean isReady() {
      return this.ready;
   }

   public boolean isThirdPerson() {
      return this.thirdPerson;
   }

   public Projection getProjection() {
      MinecraftClient lv = MinecraftClient.getInstance();
      double d = (double)lv.getWindow().getFramebufferWidth() / (double)lv.getWindow().getFramebufferHeight();
      double e = Math.tan((double)((float)(Integer)lv.options.getFov().getValue() * 0.017453292F) / 2.0) * 0.05000000074505806;
      double f = e * d;
      Vec3d lv2 = (new Vec3d(this.horizontalPlane)).multiply(0.05000000074505806);
      Vec3d lv3 = (new Vec3d(this.diagonalPlane)).multiply(f);
      Vec3d lv4 = (new Vec3d(this.verticalPlane)).multiply(e);
      return new Projection(lv2, lv3, lv4);
   }

   public CameraSubmersionType getSubmersionType() {
      if (!this.ready) {
         return CameraSubmersionType.NONE;
      } else {
         FluidState lv = this.area.getFluidState(this.blockPos);
         if (lv.isIn(FluidTags.WATER) && this.pos.y < (double)((float)this.blockPos.getY() + lv.getHeight(this.area, this.blockPos))) {
            return CameraSubmersionType.WATER;
         } else {
            Projection lv2 = this.getProjection();
            List list = Arrays.asList(lv2.center, lv2.getBottomRight(), lv2.getTopRight(), lv2.getBottomLeft(), lv2.getTopLeft());
            Iterator var4 = list.iterator();

            while(var4.hasNext()) {
               Vec3d lv3 = (Vec3d)var4.next();
               Vec3d lv4 = this.pos.add(lv3);
               BlockPos lv5 = BlockPos.ofFloored(lv4);
               FluidState lv6 = this.area.getFluidState(lv5);
               if (lv6.isIn(FluidTags.LAVA)) {
                  if (lv4.y <= (double)(lv6.getHeight(this.area, lv5) + (float)lv5.getY())) {
                     return CameraSubmersionType.LAVA;
                  }
               } else {
                  BlockState lv7 = this.area.getBlockState(lv5);
                  if (lv7.isOf(Blocks.POWDER_SNOW)) {
                     return CameraSubmersionType.POWDER_SNOW;
                  }
               }
            }

            return CameraSubmersionType.NONE;
         }
      }
   }

   public final Vector3f getHorizontalPlane() {
      return this.horizontalPlane;
   }

   public final Vector3f getVerticalPlane() {
      return this.verticalPlane;
   }

   public final Vector3f getDiagonalPlane() {
      return this.diagonalPlane;
   }

   public void reset() {
      this.area = null;
      this.focusedEntity = null;
      this.ready = false;
   }

   @Environment(EnvType.CLIENT)
   public static class Projection {
      final Vec3d center;
      private final Vec3d x;
      private final Vec3d y;

      Projection(Vec3d center, Vec3d x, Vec3d y) {
         this.center = center;
         this.x = x;
         this.y = y;
      }

      public Vec3d getBottomRight() {
         return this.center.add(this.y).add(this.x);
      }

      public Vec3d getTopRight() {
         return this.center.add(this.y).subtract(this.x);
      }

      public Vec3d getBottomLeft() {
         return this.center.subtract(this.y).add(this.x);
      }

      public Vec3d getTopLeft() {
         return this.center.subtract(this.y).subtract(this.x);
      }

      public Vec3d getPosition(float factorX, float factorY) {
         return this.center.add(this.y.multiply((double)factorY)).subtract(this.x.multiply((double)factorX));
      }
   }
}
