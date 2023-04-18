package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class DefaultPosArgument implements PosArgument {
   private final CoordinateArgument x;
   private final CoordinateArgument y;
   private final CoordinateArgument z;

   public DefaultPosArgument(CoordinateArgument x, CoordinateArgument y, CoordinateArgument z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Vec3d toAbsolutePos(ServerCommandSource source) {
      Vec3d lv = source.getPosition();
      return new Vec3d(this.x.toAbsoluteCoordinate(lv.x), this.y.toAbsoluteCoordinate(lv.y), this.z.toAbsoluteCoordinate(lv.z));
   }

   public Vec2f toAbsoluteRotation(ServerCommandSource source) {
      Vec2f lv = source.getRotation();
      return new Vec2f((float)this.x.toAbsoluteCoordinate((double)lv.x), (float)this.y.toAbsoluteCoordinate((double)lv.y));
   }

   public boolean isXRelative() {
      return this.x.isRelative();
   }

   public boolean isYRelative() {
      return this.y.isRelative();
   }

   public boolean isZRelative() {
      return this.z.isRelative();
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof DefaultPosArgument)) {
         return false;
      } else {
         DefaultPosArgument lv = (DefaultPosArgument)o;
         if (!this.x.equals(lv.x)) {
            return false;
         } else {
            return !this.y.equals(lv.y) ? false : this.z.equals(lv.z);
         }
      }
   }

   public static DefaultPosArgument parse(StringReader reader) throws CommandSyntaxException {
      int i = reader.getCursor();
      CoordinateArgument lv = CoordinateArgument.parse(reader);
      if (reader.canRead() && reader.peek() == ' ') {
         reader.skip();
         CoordinateArgument lv2 = CoordinateArgument.parse(reader);
         if (reader.canRead() && reader.peek() == ' ') {
            reader.skip();
            CoordinateArgument lv3 = CoordinateArgument.parse(reader);
            return new DefaultPosArgument(lv, lv2, lv3);
         } else {
            reader.setCursor(i);
            throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
         }
      } else {
         reader.setCursor(i);
         throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
      }
   }

   public static DefaultPosArgument parse(StringReader reader, boolean centerIntegers) throws CommandSyntaxException {
      int i = reader.getCursor();
      CoordinateArgument lv = CoordinateArgument.parse(reader, centerIntegers);
      if (reader.canRead() && reader.peek() == ' ') {
         reader.skip();
         CoordinateArgument lv2 = CoordinateArgument.parse(reader, false);
         if (reader.canRead() && reader.peek() == ' ') {
            reader.skip();
            CoordinateArgument lv3 = CoordinateArgument.parse(reader, centerIntegers);
            return new DefaultPosArgument(lv, lv2, lv3);
         } else {
            reader.setCursor(i);
            throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
         }
      } else {
         reader.setCursor(i);
         throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
      }
   }

   public static DefaultPosArgument absolute(double x, double y, double z) {
      return new DefaultPosArgument(new CoordinateArgument(false, x), new CoordinateArgument(false, y), new CoordinateArgument(false, z));
   }

   public static DefaultPosArgument absolute(Vec2f vec) {
      return new DefaultPosArgument(new CoordinateArgument(false, (double)vec.x), new CoordinateArgument(false, (double)vec.y), new CoordinateArgument(true, 0.0));
   }

   public static DefaultPosArgument zero() {
      return new DefaultPosArgument(new CoordinateArgument(true, 0.0), new CoordinateArgument(true, 0.0), new CoordinateArgument(true, 0.0));
   }

   public int hashCode() {
      int i = this.x.hashCode();
      i = 31 * i + this.y.hashCode();
      i = 31 * i + this.z.hashCode();
      return i;
   }
}
