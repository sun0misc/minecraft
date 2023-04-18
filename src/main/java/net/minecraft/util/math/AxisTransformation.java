package net.minecraft.util.math;

import java.util.Arrays;
import net.minecraft.util.Util;
import org.joml.Matrix3f;

public enum AxisTransformation {
   P123(0, 1, 2),
   P213(1, 0, 2),
   P132(0, 2, 1),
   P231(1, 2, 0),
   P312(2, 0, 1),
   P321(2, 1, 0);

   private final int[] mappings;
   private final Matrix3f matrix;
   private static final int field_33113 = 3;
   private static final AxisTransformation[][] COMBINATIONS = (AxisTransformation[][])Util.make(new AxisTransformation[values().length][values().length], (args) -> {
      AxisTransformation[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         AxisTransformation lv = var1[var3];
         AxisTransformation[] var5 = values();
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            AxisTransformation lv2 = var5[var7];
            int[] is = new int[3];

            for(int i = 0; i < 3; ++i) {
               is[i] = lv.mappings[lv2.mappings[i]];
            }

            AxisTransformation lv3 = (AxisTransformation)Arrays.stream(values()).filter((arg) -> {
               return Arrays.equals(arg.mappings, is);
            }).findFirst().get();
            args[lv.ordinal()][lv2.ordinal()] = lv3;
         }
      }

   });

   private AxisTransformation(int xMapping, int yMapping, int zMapping) {
      this.mappings = new int[]{xMapping, yMapping, zMapping};
      this.matrix = new Matrix3f();
      this.matrix.set(this.map(0), 0, 1.0F);
      this.matrix.set(this.map(1), 1, 1.0F);
      this.matrix.set(this.map(2), 2, 1.0F);
   }

   public AxisTransformation prepend(AxisTransformation transformation) {
      return COMBINATIONS[this.ordinal()][transformation.ordinal()];
   }

   public int map(int oldAxis) {
      return this.mappings[oldAxis];
   }

   public Matrix3f getMatrix() {
      return this.matrix;
   }

   // $FF: synthetic method
   private static AxisTransformation[] method_36937() {
      return new AxisTransformation[]{P123, P213, P132, P231, P312, P321};
   }
}
