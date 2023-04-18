package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class LookingPosArgument implements PosArgument {
   public static final char CARET = '^';
   private final double x;
   private final double y;
   private final double z;

   public LookingPosArgument(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Vec3d toAbsolutePos(ServerCommandSource source) {
      Vec2f lv = source.getRotation();
      Vec3d lv2 = source.getEntityAnchor().positionAt(source);
      float f = MathHelper.cos((lv.y + 90.0F) * 0.017453292F);
      float g = MathHelper.sin((lv.y + 90.0F) * 0.017453292F);
      float h = MathHelper.cos(-lv.x * 0.017453292F);
      float i = MathHelper.sin(-lv.x * 0.017453292F);
      float j = MathHelper.cos((-lv.x + 90.0F) * 0.017453292F);
      float k = MathHelper.sin((-lv.x + 90.0F) * 0.017453292F);
      Vec3d lv3 = new Vec3d((double)(f * h), (double)i, (double)(g * h));
      Vec3d lv4 = new Vec3d((double)(f * j), (double)k, (double)(g * j));
      Vec3d lv5 = lv3.crossProduct(lv4).multiply(-1.0);
      double d = lv3.x * this.z + lv4.x * this.y + lv5.x * this.x;
      double e = lv3.y * this.z + lv4.y * this.y + lv5.y * this.x;
      double l = lv3.z * this.z + lv4.z * this.y + lv5.z * this.x;
      return new Vec3d(lv2.x + d, lv2.y + e, lv2.z + l);
   }

   public Vec2f toAbsoluteRotation(ServerCommandSource source) {
      return Vec2f.ZERO;
   }

   public boolean isXRelative() {
      return true;
   }

   public boolean isYRelative() {
      return true;
   }

   public boolean isZRelative() {
      return true;
   }

   public static LookingPosArgument parse(StringReader reader) throws CommandSyntaxException {
      int i = reader.getCursor();
      double d = readCoordinate(reader, i);
      if (reader.canRead() && reader.peek() == ' ') {
         reader.skip();
         double e = readCoordinate(reader, i);
         if (reader.canRead() && reader.peek() == ' ') {
            reader.skip();
            double f = readCoordinate(reader, i);
            return new LookingPosArgument(d, e, f);
         } else {
            reader.setCursor(i);
            throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
         }
      } else {
         reader.setCursor(i);
         throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
      }
   }

   private static double readCoordinate(StringReader reader, int startingCursorPos) throws CommandSyntaxException {
      if (!reader.canRead()) {
         throw CoordinateArgument.MISSING_COORDINATE.createWithContext(reader);
      } else if (reader.peek() != '^') {
         reader.setCursor(startingCursorPos);
         throw Vec3ArgumentType.MIXED_COORDINATE_EXCEPTION.createWithContext(reader);
      } else {
         reader.skip();
         return reader.canRead() && reader.peek() != ' ' ? reader.readDouble() : 0.0;
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof LookingPosArgument)) {
         return false;
      } else {
         LookingPosArgument lv = (LookingPosArgument)o;
         return this.x == lv.x && this.y == lv.y && this.z == lv.z;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.x, this.y, this.z});
   }
}
