package net.minecraft.util.math;

import com.google.common.collect.Iterators;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public enum Direction implements StringIdentifiable {
   DOWN(0, 1, -1, "down", Direction.AxisDirection.NEGATIVE, Direction.Axis.Y, new Vec3i(0, -1, 0)),
   UP(1, 0, -1, "up", Direction.AxisDirection.POSITIVE, Direction.Axis.Y, new Vec3i(0, 1, 0)),
   NORTH(2, 3, 2, "north", Direction.AxisDirection.NEGATIVE, Direction.Axis.Z, new Vec3i(0, 0, -1)),
   SOUTH(3, 2, 0, "south", Direction.AxisDirection.POSITIVE, Direction.Axis.Z, new Vec3i(0, 0, 1)),
   WEST(4, 5, 1, "west", Direction.AxisDirection.NEGATIVE, Direction.Axis.X, new Vec3i(-1, 0, 0)),
   EAST(5, 4, 3, "east", Direction.AxisDirection.POSITIVE, Direction.Axis.X, new Vec3i(1, 0, 0));

   public static final StringIdentifiable.Codec CODEC = StringIdentifiable.createCodec(Direction::values);
   public static final com.mojang.serialization.Codec VERTICAL_CODEC = Codecs.validate(CODEC, Direction::validateVertical);
   private final int id;
   private final int idOpposite;
   private final int idHorizontal;
   private final String name;
   private final Axis axis;
   private final AxisDirection direction;
   private final Vec3i vector;
   private static final Direction[] ALL = values();
   private static final Direction[] VALUES = (Direction[])Arrays.stream(ALL).sorted(Comparator.comparingInt((direction) -> {
      return direction.id;
   })).toArray((i) -> {
      return new Direction[i];
   });
   private static final Direction[] HORIZONTAL = (Direction[])Arrays.stream(ALL).filter((direction) -> {
      return direction.getAxis().isHorizontal();
   }).sorted(Comparator.comparingInt((direction) -> {
      return direction.idHorizontal;
   })).toArray((i) -> {
      return new Direction[i];
   });

   private Direction(int id, int idOpposite, int idHorizontal, String name, AxisDirection direction, Axis axis, Vec3i vector) {
      this.id = id;
      this.idHorizontal = idHorizontal;
      this.idOpposite = idOpposite;
      this.name = name;
      this.axis = axis;
      this.direction = direction;
      this.vector = vector;
   }

   public static Direction[] getEntityFacingOrder(Entity entity) {
      float f = entity.getPitch(1.0F) * 0.017453292F;
      float g = -entity.getYaw(1.0F) * 0.017453292F;
      float h = MathHelper.sin(f);
      float i = MathHelper.cos(f);
      float j = MathHelper.sin(g);
      float k = MathHelper.cos(g);
      boolean bl = j > 0.0F;
      boolean bl2 = h < 0.0F;
      boolean bl3 = k > 0.0F;
      float l = bl ? j : -j;
      float m = bl2 ? -h : h;
      float n = bl3 ? k : -k;
      float o = l * i;
      float p = n * i;
      Direction lv = bl ? EAST : WEST;
      Direction lv2 = bl2 ? UP : DOWN;
      Direction lv3 = bl3 ? SOUTH : NORTH;
      if (l > n) {
         if (m > o) {
            return listClosest(lv2, lv, lv3);
         } else {
            return p > m ? listClosest(lv, lv3, lv2) : listClosest(lv, lv2, lv3);
         }
      } else if (m > p) {
         return listClosest(lv2, lv3, lv);
      } else {
         return o > m ? listClosest(lv3, lv, lv2) : listClosest(lv3, lv2, lv);
      }
   }

   private static Direction[] listClosest(Direction first, Direction second, Direction third) {
      return new Direction[]{first, second, third, third.getOpposite(), second.getOpposite(), first.getOpposite()};
   }

   public static Direction transform(Matrix4f matrix, Direction direction) {
      Vec3i lv = direction.getVector();
      Vector4f vector4f = matrix.transform(new Vector4f((float)lv.getX(), (float)lv.getY(), (float)lv.getZ(), 0.0F));
      return getFacing(vector4f.x(), vector4f.y(), vector4f.z());
   }

   public static Collection shuffle(Random random) {
      return Util.copyShuffled((Object[])values(), random);
   }

   public static Stream stream() {
      return Stream.of(ALL);
   }

   public Quaternionf getRotationQuaternion() {
      Quaternionf var10000;
      switch (this) {
         case DOWN:
            var10000 = (new Quaternionf()).rotationX(3.1415927F);
            break;
         case UP:
            var10000 = new Quaternionf();
            break;
         case NORTH:
            var10000 = (new Quaternionf()).rotationXYZ(1.5707964F, 0.0F, 3.1415927F);
            break;
         case SOUTH:
            var10000 = (new Quaternionf()).rotationX(1.5707964F);
            break;
         case WEST:
            var10000 = (new Quaternionf()).rotationXYZ(1.5707964F, 0.0F, 1.5707964F);
            break;
         case EAST:
            var10000 = (new Quaternionf()).rotationXYZ(1.5707964F, 0.0F, -1.5707964F);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public int getId() {
      return this.id;
   }

   public int getHorizontal() {
      return this.idHorizontal;
   }

   public AxisDirection getDirection() {
      return this.direction;
   }

   public static Direction getLookDirectionForAxis(Entity entity, Axis axis) {
      Direction var10000;
      switch (axis) {
         case X:
            var10000 = EAST.pointsTo(entity.getYaw(1.0F)) ? EAST : WEST;
            break;
         case Z:
            var10000 = SOUTH.pointsTo(entity.getYaw(1.0F)) ? SOUTH : NORTH;
            break;
         case Y:
            var10000 = entity.getPitch(1.0F) < 0.0F ? UP : DOWN;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public Direction getOpposite() {
      return byId(this.idOpposite);
   }

   public Direction rotateClockwise(Axis axis) {
      Direction var10000;
      switch (axis) {
         case X:
            var10000 = this != WEST && this != EAST ? this.rotateXClockwise() : this;
            break;
         case Z:
            var10000 = this != NORTH && this != SOUTH ? this.rotateZClockwise() : this;
            break;
         case Y:
            var10000 = this != UP && this != DOWN ? this.rotateYClockwise() : this;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public Direction rotateCounterclockwise(Axis axis) {
      Direction var10000;
      switch (axis) {
         case X:
            var10000 = this != WEST && this != EAST ? this.rotateXCounterclockwise() : this;
            break;
         case Z:
            var10000 = this != NORTH && this != SOUTH ? this.rotateZCounterclockwise() : this;
            break;
         case Y:
            var10000 = this != UP && this != DOWN ? this.rotateYCounterclockwise() : this;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public Direction rotateYClockwise() {
      Direction var10000;
      switch (this) {
         case NORTH:
            var10000 = EAST;
            break;
         case SOUTH:
            var10000 = WEST;
            break;
         case WEST:
            var10000 = NORTH;
            break;
         case EAST:
            var10000 = SOUTH;
            break;
         default:
            throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
      }

      return var10000;
   }

   private Direction rotateXClockwise() {
      Direction var10000;
      switch (this) {
         case DOWN:
            var10000 = SOUTH;
            break;
         case UP:
            var10000 = NORTH;
            break;
         case NORTH:
            var10000 = DOWN;
            break;
         case SOUTH:
            var10000 = UP;
            break;
         default:
            throw new IllegalStateException("Unable to get X-rotated facing of " + this);
      }

      return var10000;
   }

   private Direction rotateXCounterclockwise() {
      Direction var10000;
      switch (this) {
         case DOWN:
            var10000 = NORTH;
            break;
         case UP:
            var10000 = SOUTH;
            break;
         case NORTH:
            var10000 = UP;
            break;
         case SOUTH:
            var10000 = DOWN;
            break;
         default:
            throw new IllegalStateException("Unable to get X-rotated facing of " + this);
      }

      return var10000;
   }

   private Direction rotateZClockwise() {
      Direction var10000;
      switch (this) {
         case DOWN:
            var10000 = WEST;
            break;
         case UP:
            var10000 = EAST;
            break;
         case NORTH:
         case SOUTH:
         default:
            throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
         case WEST:
            var10000 = UP;
            break;
         case EAST:
            var10000 = DOWN;
      }

      return var10000;
   }

   private Direction rotateZCounterclockwise() {
      Direction var10000;
      switch (this) {
         case DOWN:
            var10000 = EAST;
            break;
         case UP:
            var10000 = WEST;
            break;
         case NORTH:
         case SOUTH:
         default:
            throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
         case WEST:
            var10000 = DOWN;
            break;
         case EAST:
            var10000 = UP;
      }

      return var10000;
   }

   public Direction rotateYCounterclockwise() {
      Direction var10000;
      switch (this) {
         case NORTH:
            var10000 = WEST;
            break;
         case SOUTH:
            var10000 = EAST;
            break;
         case WEST:
            var10000 = SOUTH;
            break;
         case EAST:
            var10000 = NORTH;
            break;
         default:
            throw new IllegalStateException("Unable to get CCW facing of " + this);
      }

      return var10000;
   }

   public int getOffsetX() {
      return this.vector.getX();
   }

   public int getOffsetY() {
      return this.vector.getY();
   }

   public int getOffsetZ() {
      return this.vector.getZ();
   }

   public Vector3f getUnitVector() {
      return new Vector3f((float)this.getOffsetX(), (float)this.getOffsetY(), (float)this.getOffsetZ());
   }

   public String getName() {
      return this.name;
   }

   public Axis getAxis() {
      return this.axis;
   }

   @Nullable
   public static Direction byName(@Nullable String name) {
      return (Direction)CODEC.byId(name);
   }

   public static Direction byId(int id) {
      return VALUES[MathHelper.abs(id % VALUES.length)];
   }

   public static Direction fromHorizontal(int value) {
      return HORIZONTAL[MathHelper.abs(value % HORIZONTAL.length)];
   }

   @Nullable
   public static Direction fromVector(int x, int y, int z) {
      if (x == 0) {
         if (y == 0) {
            if (z > 0) {
               return SOUTH;
            }

            if (z < 0) {
               return NORTH;
            }
         } else if (z == 0) {
            if (y > 0) {
               return UP;
            }

            return DOWN;
         }
      } else if (y == 0 && z == 0) {
         if (x > 0) {
            return EAST;
         }

         return WEST;
      }

      return null;
   }

   public static Direction fromRotation(double rotation) {
      return fromHorizontal(MathHelper.floor(rotation / 90.0 + 0.5) & 3);
   }

   public static Direction from(Axis axis, AxisDirection direction) {
      Direction var10000;
      switch (axis) {
         case X:
            var10000 = direction == Direction.AxisDirection.POSITIVE ? EAST : WEST;
            break;
         case Z:
            var10000 = direction == Direction.AxisDirection.POSITIVE ? SOUTH : NORTH;
            break;
         case Y:
            var10000 = direction == Direction.AxisDirection.POSITIVE ? UP : DOWN;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public float asRotation() {
      return (float)((this.idHorizontal & 3) * 90);
   }

   public static Direction random(Random random) {
      return (Direction)Util.getRandom((Object[])ALL, random);
   }

   public static Direction getFacing(double x, double y, double z) {
      return getFacing((float)x, (float)y, (float)z);
   }

   public static Direction getFacing(float x, float y, float z) {
      Direction lv = NORTH;
      float i = Float.MIN_VALUE;
      Direction[] var5 = ALL;
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         Direction lv2 = var5[var7];
         float j = x * (float)lv2.vector.getX() + y * (float)lv2.vector.getY() + z * (float)lv2.vector.getZ();
         if (j > i) {
            i = j;
            lv = lv2;
         }
      }

      return lv;
   }

   public String toString() {
      return this.name;
   }

   public String asString() {
      return this.name;
   }

   private static DataResult validateVertical(Direction direction) {
      return direction.getAxis().isVertical() ? DataResult.success(direction) : DataResult.error(() -> {
         return "Expected a vertical direction";
      });
   }

   public static Direction get(AxisDirection direction, Axis axis) {
      Direction[] var2 = ALL;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Direction lv = var2[var4];
         if (lv.getDirection() == direction && lv.getAxis() == axis) {
            return lv;
         }
      }

      throw new IllegalArgumentException("No such direction: " + direction + " " + axis);
   }

   public Vec3i getVector() {
      return this.vector;
   }

   public boolean pointsTo(float yaw) {
      float g = yaw * 0.017453292F;
      float h = -MathHelper.sin(g);
      float i = MathHelper.cos(g);
      return (float)this.vector.getX() * h + (float)this.vector.getZ() * i > 0.0F;
   }

   // $FF: synthetic method
   private static Direction[] method_36931() {
      return new Direction[]{DOWN, UP, NORTH, SOUTH, WEST, EAST};
   }

   public static enum Axis implements StringIdentifiable, Predicate {
      X("x") {
         public int choose(int x, int y, int z) {
            return x;
         }

         public double choose(double x, double y, double z) {
            return x;
         }

         // $FF: synthetic method
         public boolean test(@Nullable Object object) {
            return super.test((Direction)object);
         }
      },
      Y("y") {
         public int choose(int x, int y, int z) {
            return y;
         }

         public double choose(double x, double y, double z) {
            return y;
         }

         // $FF: synthetic method
         public boolean test(@Nullable Object object) {
            return super.test((Direction)object);
         }
      },
      Z("z") {
         public int choose(int x, int y, int z) {
            return z;
         }

         public double choose(double x, double y, double z) {
            return z;
         }

         // $FF: synthetic method
         public boolean test(@Nullable Object object) {
            return super.test((Direction)object);
         }
      };

      public static final Axis[] VALUES = values();
      public static final StringIdentifiable.Codec CODEC = StringIdentifiable.createCodec(Axis::values);
      private final String name;

      Axis(String name) {
         this.name = name;
      }

      @Nullable
      public static Axis fromName(String name) {
         return (Axis)CODEC.byId(name);
      }

      public String getName() {
         return this.name;
      }

      public boolean isVertical() {
         return this == Y;
      }

      public boolean isHorizontal() {
         return this == X || this == Z;
      }

      public String toString() {
         return this.name;
      }

      public static Axis pickRandomAxis(Random random) {
         return (Axis)Util.getRandom((Object[])VALUES, random);
      }

      public boolean test(@Nullable Direction arg) {
         return arg != null && arg.getAxis() == this;
      }

      public Type getType() {
         Type var10000;
         switch (this) {
            case X:
            case Z:
               var10000 = Direction.Type.HORIZONTAL;
               break;
            case Y:
               var10000 = Direction.Type.VERTICAL;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      public String asString() {
         return this.name;
      }

      public abstract int choose(int x, int y, int z);

      public abstract double choose(double x, double y, double z);

      // $FF: synthetic method
      public boolean test(@Nullable Object object) {
         return this.test((Direction)object);
      }

      // $FF: synthetic method
      private static Axis[] method_36932() {
         return new Axis[]{X, Y, Z};
      }
   }

   public static enum AxisDirection {
      POSITIVE(1, "Towards positive"),
      NEGATIVE(-1, "Towards negative");

      private final int offset;
      private final String description;

      private AxisDirection(int offset, String description) {
         this.offset = offset;
         this.description = description;
      }

      public int offset() {
         return this.offset;
      }

      public String getDescription() {
         return this.description;
      }

      public String toString() {
         return this.description;
      }

      public AxisDirection getOpposite() {
         return this == POSITIVE ? NEGATIVE : POSITIVE;
      }

      // $FF: synthetic method
      private static AxisDirection[] method_36933() {
         return new AxisDirection[]{POSITIVE, NEGATIVE};
      }
   }

   public static enum Type implements Iterable, Predicate {
      HORIZONTAL(new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}, new Axis[]{Direction.Axis.X, Direction.Axis.Z}),
      VERTICAL(new Direction[]{Direction.UP, Direction.DOWN}, new Axis[]{Direction.Axis.Y});

      private final Direction[] facingArray;
      private final Axis[] axisArray;

      private Type(Direction[] facingArray, Axis[] axisArray) {
         this.facingArray = facingArray;
         this.axisArray = axisArray;
      }

      public Direction random(Random random) {
         return (Direction)Util.getRandom((Object[])this.facingArray, random);
      }

      public Axis randomAxis(Random random) {
         return (Axis)Util.getRandom((Object[])this.axisArray, random);
      }

      public boolean test(@Nullable Direction arg) {
         return arg != null && arg.getAxis().getType() == this;
      }

      public Iterator iterator() {
         return Iterators.forArray(this.facingArray);
      }

      public Stream stream() {
         return Arrays.stream(this.facingArray);
      }

      public List getShuffled(Random random) {
         return Util.copyShuffled((Object[])this.facingArray, random);
      }

      // $FF: synthetic method
      public boolean test(@Nullable Object direction) {
         return this.test((Direction)direction);
      }

      // $FF: synthetic method
      private static Type[] method_36934() {
         return new Type[]{HORIZONTAL, VERTICAL};
      }
   }
}
