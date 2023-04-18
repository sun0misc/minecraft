package net.minecraft.util.math;

import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;

public class EulerAngle {
   protected final float pitch;
   protected final float yaw;
   protected final float roll;

   public EulerAngle(float pitch, float yaw, float roll) {
      this.pitch = !Float.isInfinite(pitch) && !Float.isNaN(pitch) ? pitch % 360.0F : 0.0F;
      this.yaw = !Float.isInfinite(yaw) && !Float.isNaN(yaw) ? yaw % 360.0F : 0.0F;
      this.roll = !Float.isInfinite(roll) && !Float.isNaN(roll) ? roll % 360.0F : 0.0F;
   }

   public EulerAngle(NbtList serialized) {
      this(serialized.getFloat(0), serialized.getFloat(1), serialized.getFloat(2));
   }

   public NbtList toNbt() {
      NbtList lv = new NbtList();
      lv.add(NbtFloat.of(this.pitch));
      lv.add(NbtFloat.of(this.yaw));
      lv.add(NbtFloat.of(this.roll));
      return lv;
   }

   public boolean equals(Object o) {
      if (!(o instanceof EulerAngle lv)) {
         return false;
      } else {
         return this.pitch == lv.pitch && this.yaw == lv.yaw && this.roll == lv.roll;
      }
   }

   public float getPitch() {
      return this.pitch;
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getRoll() {
      return this.roll;
   }

   public float getWrappedPitch() {
      return MathHelper.wrapDegrees(this.pitch);
   }

   public float getWrappedYaw() {
      return MathHelper.wrapDegrees(this.yaw);
   }

   public float getWrappedRoll() {
      return MathHelper.wrapDegrees(this.roll);
   }
}
