package net.minecraft.util.math;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.block.enums.JigsawOrientation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;

public enum DirectionTransformation implements StringIdentifiable {
   IDENTITY("identity", AxisTransformation.P123, false, false, false),
   ROT_180_FACE_XY("rot_180_face_xy", AxisTransformation.P123, true, true, false),
   ROT_180_FACE_XZ("rot_180_face_xz", AxisTransformation.P123, true, false, true),
   ROT_180_FACE_YZ("rot_180_face_yz", AxisTransformation.P123, false, true, true),
   ROT_120_NNN("rot_120_nnn", AxisTransformation.P231, false, false, false),
   ROT_120_NNP("rot_120_nnp", AxisTransformation.P312, true, false, true),
   ROT_120_NPN("rot_120_npn", AxisTransformation.P312, false, true, true),
   ROT_120_NPP("rot_120_npp", AxisTransformation.P231, true, false, true),
   ROT_120_PNN("rot_120_pnn", AxisTransformation.P312, true, true, false),
   ROT_120_PNP("rot_120_pnp", AxisTransformation.P231, true, true, false),
   ROT_120_PPN("rot_120_ppn", AxisTransformation.P231, false, true, true),
   ROT_120_PPP("rot_120_ppp", AxisTransformation.P312, false, false, false),
   ROT_180_EDGE_XY_NEG("rot_180_edge_xy_neg", AxisTransformation.P213, true, true, true),
   ROT_180_EDGE_XY_POS("rot_180_edge_xy_pos", AxisTransformation.P213, false, false, true),
   ROT_180_EDGE_XZ_NEG("rot_180_edge_xz_neg", AxisTransformation.P321, true, true, true),
   ROT_180_EDGE_XZ_POS("rot_180_edge_xz_pos", AxisTransformation.P321, false, true, false),
   ROT_180_EDGE_YZ_NEG("rot_180_edge_yz_neg", AxisTransformation.P132, true, true, true),
   ROT_180_EDGE_YZ_POS("rot_180_edge_yz_pos", AxisTransformation.P132, true, false, false),
   ROT_90_X_NEG("rot_90_x_neg", AxisTransformation.P132, false, false, true),
   ROT_90_X_POS("rot_90_x_pos", AxisTransformation.P132, false, true, false),
   ROT_90_Y_NEG("rot_90_y_neg", AxisTransformation.P321, true, false, false),
   ROT_90_Y_POS("rot_90_y_pos", AxisTransformation.P321, false, false, true),
   ROT_90_Z_NEG("rot_90_z_neg", AxisTransformation.P213, false, true, false),
   ROT_90_Z_POS("rot_90_z_pos", AxisTransformation.P213, true, false, false),
   INVERSION("inversion", AxisTransformation.P123, true, true, true),
   INVERT_X("invert_x", AxisTransformation.P123, true, false, false),
   INVERT_Y("invert_y", AxisTransformation.P123, false, true, false),
   INVERT_Z("invert_z", AxisTransformation.P123, false, false, true),
   ROT_60_REF_NNN("rot_60_ref_nnn", AxisTransformation.P312, true, true, true),
   ROT_60_REF_NNP("rot_60_ref_nnp", AxisTransformation.P231, true, false, false),
   ROT_60_REF_NPN("rot_60_ref_npn", AxisTransformation.P231, false, false, true),
   ROT_60_REF_NPP("rot_60_ref_npp", AxisTransformation.P312, false, false, true),
   ROT_60_REF_PNN("rot_60_ref_pnn", AxisTransformation.P231, false, true, false),
   ROT_60_REF_PNP("rot_60_ref_pnp", AxisTransformation.P312, true, false, false),
   ROT_60_REF_PPN("rot_60_ref_ppn", AxisTransformation.P312, false, true, false),
   ROT_60_REF_PPP("rot_60_ref_ppp", AxisTransformation.P231, true, true, true),
   SWAP_XY("swap_xy", AxisTransformation.P213, false, false, false),
   SWAP_YZ("swap_yz", AxisTransformation.P132, false, false, false),
   SWAP_XZ("swap_xz", AxisTransformation.P321, false, false, false),
   SWAP_NEG_XY("swap_neg_xy", AxisTransformation.P213, true, true, false),
   SWAP_NEG_YZ("swap_neg_yz", AxisTransformation.P132, false, true, true),
   SWAP_NEG_XZ("swap_neg_xz", AxisTransformation.P321, true, false, true),
   ROT_90_REF_X_NEG("rot_90_ref_x_neg", AxisTransformation.P132, true, false, true),
   ROT_90_REF_X_POS("rot_90_ref_x_pos", AxisTransformation.P132, true, true, false),
   ROT_90_REF_Y_NEG("rot_90_ref_y_neg", AxisTransformation.P321, true, true, false),
   ROT_90_REF_Y_POS("rot_90_ref_y_pos", AxisTransformation.P321, false, true, true),
   ROT_90_REF_Z_NEG("rot_90_ref_z_neg", AxisTransformation.P213, false, true, true),
   ROT_90_REF_Z_POS("rot_90_ref_z_pos", AxisTransformation.P213, true, false, true);

   private final Matrix3f matrix;
   private final String name;
   @Nullable
   private Map mappings;
   private final boolean flipX;
   private final boolean flipY;
   private final boolean flipZ;
   private final AxisTransformation axisTransformation;
   private static final DirectionTransformation[][] COMBINATIONS = (DirectionTransformation[][])Util.make(new DirectionTransformation[values().length][values().length], (args) -> {
      Map map = (Map)Arrays.stream(values()).collect(Collectors.toMap((arg) -> {
         return Pair.of(arg.axisTransformation, arg.getAxisFlips());
      }, (arg) -> {
         return arg;
      }));
      DirectionTransformation[] var2 = values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         DirectionTransformation lv = var2[var4];
         DirectionTransformation[] var6 = values();
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            DirectionTransformation lv2 = var6[var8];
            BooleanList booleanList = lv.getAxisFlips();
            BooleanList booleanList2 = lv2.getAxisFlips();
            AxisTransformation lv3 = lv2.axisTransformation.prepend(lv.axisTransformation);
            BooleanArrayList booleanArrayList = new BooleanArrayList(3);

            for(int i = 0; i < 3; ++i) {
               booleanArrayList.add(booleanList.getBoolean(i) ^ booleanList2.getBoolean(lv.axisTransformation.map(i)));
            }

            args[lv.ordinal()][lv2.ordinal()] = (DirectionTransformation)map.get(Pair.of(lv3, booleanArrayList));
         }
      }

   });
   private static final DirectionTransformation[] INVERSES = (DirectionTransformation[])Arrays.stream(values()).map((arg) -> {
      return (DirectionTransformation)Arrays.stream(values()).filter((arg2) -> {
         return arg.prepend(arg2) == IDENTITY;
      }).findAny().get();
   }).toArray((i) -> {
      return new DirectionTransformation[i];
   });

   private DirectionTransformation(String name, AxisTransformation axisTransformation, boolean flipX, boolean flipY, boolean flipZ) {
      this.name = name;
      this.flipX = flipX;
      this.flipY = flipY;
      this.flipZ = flipZ;
      this.axisTransformation = axisTransformation;
      this.matrix = (new Matrix3f()).scaling(flipX ? -1.0F : 1.0F, flipY ? -1.0F : 1.0F, flipZ ? -1.0F : 1.0F);
      this.matrix.mul(axisTransformation.getMatrix());
   }

   private BooleanList getAxisFlips() {
      return new BooleanArrayList(new boolean[]{this.flipX, this.flipY, this.flipZ});
   }

   public DirectionTransformation prepend(DirectionTransformation transformation) {
      return COMBINATIONS[this.ordinal()][transformation.ordinal()];
   }

   public DirectionTransformation inverse() {
      return INVERSES[this.ordinal()];
   }

   public Matrix3f getMatrix() {
      return new Matrix3f(this.matrix);
   }

   public String toString() {
      return this.name;
   }

   public String asString() {
      return this.name;
   }

   public Direction map(Direction direction) {
      if (this.mappings == null) {
         this.mappings = Maps.newEnumMap(Direction.class);
         Direction.Axis[] lvs = Direction.Axis.values();
         Direction[] var3 = Direction.values();
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            Direction lv = var3[var5];
            Direction.Axis lv2 = lv.getAxis();
            Direction.AxisDirection lv3 = lv.getDirection();
            Direction.Axis lv4 = lvs[this.axisTransformation.map(lv2.ordinal())];
            Direction.AxisDirection lv5 = this.shouldFlipDirection(lv4) ? lv3.getOpposite() : lv3;
            Direction lv6 = Direction.from(lv4, lv5);
            this.mappings.put(lv, lv6);
         }
      }

      return (Direction)this.mappings.get(direction);
   }

   public boolean shouldFlipDirection(Direction.Axis axis) {
      switch (axis) {
         case X:
            return this.flipX;
         case Y:
            return this.flipY;
         case Z:
         default:
            return this.flipZ;
      }
   }

   public JigsawOrientation mapJigsawOrientation(JigsawOrientation orientation) {
      return JigsawOrientation.byDirections(this.map(orientation.getFacing()), this.map(orientation.getRotation()));
   }

   // $FF: synthetic method
   private static DirectionTransformation[] method_36928() {
      return new DirectionTransformation[]{IDENTITY, ROT_180_FACE_XY, ROT_180_FACE_XZ, ROT_180_FACE_YZ, ROT_120_NNN, ROT_120_NNP, ROT_120_NPN, ROT_120_NPP, ROT_120_PNN, ROT_120_PNP, ROT_120_PPN, ROT_120_PPP, ROT_180_EDGE_XY_NEG, ROT_180_EDGE_XY_POS, ROT_180_EDGE_XZ_NEG, ROT_180_EDGE_XZ_POS, ROT_180_EDGE_YZ_NEG, ROT_180_EDGE_YZ_POS, ROT_90_X_NEG, ROT_90_X_POS, ROT_90_Y_NEG, ROT_90_Y_POS, ROT_90_Z_NEG, ROT_90_Z_POS, INVERSION, INVERT_X, INVERT_Y, INVERT_Z, ROT_60_REF_NNN, ROT_60_REF_NNP, ROT_60_REF_NPN, ROT_60_REF_NPP, ROT_60_REF_PNN, ROT_60_REF_PNP, ROT_60_REF_PPN, ROT_60_REF_PPP, SWAP_XY, SWAP_YZ, SWAP_XZ, SWAP_NEG_XY, SWAP_NEG_YZ, SWAP_NEG_XZ, ROT_90_REF_X_NEG, ROT_90_REF_X_POS, ROT_90_REF_Y_NEG, ROT_90_REF_Y_POS, ROT_90_REF_Z_NEG, ROT_90_REF_Z_POS};
   }
}
