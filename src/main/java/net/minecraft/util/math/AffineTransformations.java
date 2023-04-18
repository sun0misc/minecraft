package net.minecraft.util.math;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.Util;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

public class AffineTransformations {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Map DIRECTION_ROTATIONS = (Map)Util.make(Maps.newEnumMap(Direction.class), (map) -> {
      map.put(Direction.SOUTH, AffineTransformation.identity());
      map.put(Direction.EAST, new AffineTransformation((Vector3f)null, (new Quaternionf()).rotateY(1.5707964F), (Vector3f)null, (Quaternionf)null));
      map.put(Direction.WEST, new AffineTransformation((Vector3f)null, (new Quaternionf()).rotateY(-1.5707964F), (Vector3f)null, (Quaternionf)null));
      map.put(Direction.NORTH, new AffineTransformation((Vector3f)null, (new Quaternionf()).rotateY(3.1415927F), (Vector3f)null, (Quaternionf)null));
      map.put(Direction.UP, new AffineTransformation((Vector3f)null, (new Quaternionf()).rotateX(-1.5707964F), (Vector3f)null, (Quaternionf)null));
      map.put(Direction.DOWN, new AffineTransformation((Vector3f)null, (new Quaternionf()).rotateX(1.5707964F), (Vector3f)null, (Quaternionf)null));
   });
   public static final Map INVERTED_DIRECTION_ROTATIONS = (Map)Util.make(Maps.newEnumMap(Direction.class), (map) -> {
      Direction[] var1 = Direction.values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         Direction lv = var1[var3];
         map.put(lv, ((AffineTransformation)DIRECTION_ROTATIONS.get(lv)).invert());
      }

   });

   public static AffineTransformation setupUvLock(AffineTransformation arg) {
      Matrix4f matrix4f = (new Matrix4f()).translation(0.5F, 0.5F, 0.5F);
      matrix4f.mul(arg.getMatrix());
      matrix4f.translate(-0.5F, -0.5F, -0.5F);
      return new AffineTransformation(matrix4f);
   }

   public static AffineTransformation method_35829(AffineTransformation arg) {
      Matrix4f matrix4f = (new Matrix4f()).translation(-0.5F, -0.5F, -0.5F);
      matrix4f.mul(arg.getMatrix());
      matrix4f.translate(0.5F, 0.5F, 0.5F);
      return new AffineTransformation(matrix4f);
   }

   public static AffineTransformation uvLock(AffineTransformation arg, Direction arg2, Supplier supplier) {
      Direction lv = Direction.transform(arg.getMatrix(), arg2);
      AffineTransformation lv2 = arg.invert();
      if (lv2 == null) {
         LOGGER.warn((String)supplier.get());
         return new AffineTransformation((Vector3f)null, (Quaternionf)null, new Vector3f(0.0F, 0.0F, 0.0F), (Quaternionf)null);
      } else {
         AffineTransformation lv3 = ((AffineTransformation)INVERTED_DIRECTION_ROTATIONS.get(arg2)).multiply(lv2).multiply((AffineTransformation)DIRECTION_ROTATIONS.get(lv));
         return setupUvLock(lv3);
      }
   }
}
