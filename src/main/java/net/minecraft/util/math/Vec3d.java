package net.minecraft.util.math;

import com.mojang.serialization.Codec;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3f;

public class Vec3d implements Position {
   public static final Codec CODEC;
   public static final Vec3d ZERO;
   public final double x;
   public final double y;
   public final double z;

   public static Vec3d unpackRgb(int rgb) {
      double d = (double)(rgb >> 16 & 255) / 255.0;
      double e = (double)(rgb >> 8 & 255) / 255.0;
      double f = (double)(rgb & 255) / 255.0;
      return new Vec3d(d, e, f);
   }

   public static Vec3d of(Vec3i vec) {
      return new Vec3d((double)vec.getX(), (double)vec.getY(), (double)vec.getZ());
   }

   public static Vec3d add(Vec3i vec, double deltaX, double deltaY, double deltaZ) {
      return new Vec3d((double)vec.getX() + deltaX, (double)vec.getY() + deltaY, (double)vec.getZ() + deltaZ);
   }

   public static Vec3d ofCenter(Vec3i vec) {
      return add(vec, 0.5, 0.5, 0.5);
   }

   public static Vec3d ofBottomCenter(Vec3i vec) {
      return add(vec, 0.5, 0.0, 0.5);
   }

   public static Vec3d ofCenter(Vec3i vec, double deltaY) {
      return add(vec, 0.5, deltaY, 0.5);
   }

   public Vec3d(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Vec3d(Vector3f vec) {
      this((double)vec.x(), (double)vec.y(), (double)vec.z());
   }

   public Vec3d relativize(Vec3d vec) {
      return new Vec3d(vec.x - this.x, vec.y - this.y, vec.z - this.z);
   }

   public Vec3d normalize() {
      double d = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
      return d < 1.0E-4 ? ZERO : new Vec3d(this.x / d, this.y / d, this.z / d);
   }

   public double dotProduct(Vec3d vec) {
      return this.x * vec.x + this.y * vec.y + this.z * vec.z;
   }

   public Vec3d crossProduct(Vec3d vec) {
      return new Vec3d(this.y * vec.z - this.z * vec.y, this.z * vec.x - this.x * vec.z, this.x * vec.y - this.y * vec.x);
   }

   public Vec3d subtract(Vec3d vec) {
      return this.subtract(vec.x, vec.y, vec.z);
   }

   public Vec3d subtract(double x, double y, double z) {
      return this.add(-x, -y, -z);
   }

   public Vec3d add(Vec3d vec) {
      return this.add(vec.x, vec.y, vec.z);
   }

   public Vec3d add(double x, double y, double z) {
      return new Vec3d(this.x + x, this.y + y, this.z + z);
   }

   public boolean isInRange(Position pos, double radius) {
      return this.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < radius * radius;
   }

   public double distanceTo(Vec3d vec) {
      double d = vec.x - this.x;
      double e = vec.y - this.y;
      double f = vec.z - this.z;
      return Math.sqrt(d * d + e * e + f * f);
   }

   public double squaredDistanceTo(Vec3d vec) {
      double d = vec.x - this.x;
      double e = vec.y - this.y;
      double f = vec.z - this.z;
      return d * d + e * e + f * f;
   }

   public double squaredDistanceTo(double x, double y, double z) {
      double g = x - this.x;
      double h = y - this.y;
      double i = z - this.z;
      return g * g + h * h + i * i;
   }

   public Vec3d multiply(double value) {
      return this.multiply(value, value, value);
   }

   public Vec3d negate() {
      return this.multiply(-1.0);
   }

   public Vec3d multiply(Vec3d vec) {
      return this.multiply(vec.x, vec.y, vec.z);
   }

   public Vec3d multiply(double x, double y, double z) {
      return new Vec3d(this.x * x, this.y * y, this.z * z);
   }

   public Vec3d addRandom(Random random, float multiplier) {
      return this.add((double)((random.nextFloat() - 0.5F) * multiplier), (double)((random.nextFloat() - 0.5F) * multiplier), (double)((random.nextFloat() - 0.5F) * multiplier));
   }

   public double length() {
      return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
   }

   public double lengthSquared() {
      return this.x * this.x + this.y * this.y + this.z * this.z;
   }

   public double horizontalLength() {
      return Math.sqrt(this.x * this.x + this.z * this.z);
   }

   public double horizontalLengthSquared() {
      return this.x * this.x + this.z * this.z;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Vec3d)) {
         return false;
      } else {
         Vec3d lv = (Vec3d)o;
         if (Double.compare(lv.x, this.x) != 0) {
            return false;
         } else if (Double.compare(lv.y, this.y) != 0) {
            return false;
         } else {
            return Double.compare(lv.z, this.z) == 0;
         }
      }
   }

   public int hashCode() {
      long l = Double.doubleToLongBits(this.x);
      int i = (int)(l ^ l >>> 32);
      l = Double.doubleToLongBits(this.y);
      i = 31 * i + (int)(l ^ l >>> 32);
      l = Double.doubleToLongBits(this.z);
      i = 31 * i + (int)(l ^ l >>> 32);
      return i;
   }

   public String toString() {
      return "(" + this.x + ", " + this.y + ", " + this.z + ")";
   }

   public Vec3d lerp(Vec3d to, double delta) {
      return new Vec3d(MathHelper.lerp(delta, this.x, to.x), MathHelper.lerp(delta, this.y, to.y), MathHelper.lerp(delta, this.z, to.z));
   }

   public Vec3d rotateX(float angle) {
      float g = MathHelper.cos(angle);
      float h = MathHelper.sin(angle);
      double d = this.x;
      double e = this.y * (double)g + this.z * (double)h;
      double i = this.z * (double)g - this.y * (double)h;
      return new Vec3d(d, e, i);
   }

   public Vec3d rotateY(float angle) {
      float g = MathHelper.cos(angle);
      float h = MathHelper.sin(angle);
      double d = this.x * (double)g + this.z * (double)h;
      double e = this.y;
      double i = this.z * (double)g - this.x * (double)h;
      return new Vec3d(d, e, i);
   }

   public Vec3d rotateZ(float angle) {
      float g = MathHelper.cos(angle);
      float h = MathHelper.sin(angle);
      double d = this.x * (double)g + this.y * (double)h;
      double e = this.y * (double)g - this.x * (double)h;
      double i = this.z;
      return new Vec3d(d, e, i);
   }

   public static Vec3d fromPolar(Vec2f polar) {
      return fromPolar(polar.x, polar.y);
   }

   public static Vec3d fromPolar(float pitch, float yaw) {
      float h = MathHelper.cos(-yaw * 0.017453292F - 3.1415927F);
      float i = MathHelper.sin(-yaw * 0.017453292F - 3.1415927F);
      float j = -MathHelper.cos(-pitch * 0.017453292F);
      float k = MathHelper.sin(-pitch * 0.017453292F);
      return new Vec3d((double)(i * j), (double)k, (double)(h * j));
   }

   public Vec3d floorAlongAxes(EnumSet axes) {
      double d = axes.contains(Direction.Axis.X) ? (double)MathHelper.floor(this.x) : this.x;
      double e = axes.contains(Direction.Axis.Y) ? (double)MathHelper.floor(this.y) : this.y;
      double f = axes.contains(Direction.Axis.Z) ? (double)MathHelper.floor(this.z) : this.z;
      return new Vec3d(d, e, f);
   }

   public double getComponentAlongAxis(Direction.Axis axis) {
      return axis.choose(this.x, this.y, this.z);
   }

   public Vec3d withAxis(Direction.Axis axis, double value) {
      double e = axis == Direction.Axis.X ? value : this.x;
      double f = axis == Direction.Axis.Y ? value : this.y;
      double g = axis == Direction.Axis.Z ? value : this.z;
      return new Vec3d(e, f, g);
   }

   public Vec3d offset(Direction direction, double value) {
      Vec3i lv = direction.getVector();
      return new Vec3d(this.x + value * (double)lv.getX(), this.y + value * (double)lv.getY(), this.z + value * (double)lv.getZ());
   }

   public final double getX() {
      return this.x;
   }

   public final double getY() {
      return this.y;
   }

   public final double getZ() {
      return this.z;
   }

   public Vector3f toVector3f() {
      return new Vector3f((float)this.x, (float)this.y, (float)this.z);
   }

   static {
      CODEC = Codec.DOUBLE.listOf().comapFlatMap((coordinates) -> {
         return Util.toArray((List)coordinates, 3).map((coords) -> {
            return new Vec3d((Double)coords.get(0), (Double)coords.get(1), (Double)coords.get(2));
         });
      }, (vec) -> {
         return List.of(vec.getX(), vec.getY(), vec.getZ());
      });
      ZERO = new Vec3d(0.0, 0.0, 0.0);
   }
}
