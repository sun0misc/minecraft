package net.minecraft.util;

import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.DirectionTransformation;

public enum BlockMirror implements StringIdentifiable {
   NONE("none", DirectionTransformation.IDENTITY),
   LEFT_RIGHT("left_right", DirectionTransformation.INVERT_Z),
   FRONT_BACK("front_back", DirectionTransformation.INVERT_X);

   public static final com.mojang.serialization.Codec CODEC = StringIdentifiable.createCodec(BlockMirror::values);
   private final String id;
   private final Text name;
   private final DirectionTransformation directionTransformation;

   private BlockMirror(String id, DirectionTransformation directionTransformation) {
      this.id = id;
      this.name = Text.translatable("mirror." + id);
      this.directionTransformation = directionTransformation;
   }

   public int mirror(int rotation, int fullTurn) {
      int k = fullTurn / 2;
      int l = rotation > k ? rotation - fullTurn : rotation;
      switch (this) {
         case FRONT_BACK:
            return (fullTurn - l) % fullTurn;
         case LEFT_RIGHT:
            return (k - l + fullTurn) % fullTurn;
         default:
            return rotation;
      }
   }

   public BlockRotation getRotation(Direction direction) {
      Direction.Axis lv = direction.getAxis();
      return (this != LEFT_RIGHT || lv != Direction.Axis.Z) && (this != FRONT_BACK || lv != Direction.Axis.X) ? BlockRotation.NONE : BlockRotation.CLOCKWISE_180;
   }

   public Direction apply(Direction direction) {
      if (this == FRONT_BACK && direction.getAxis() == Direction.Axis.X) {
         return direction.getOpposite();
      } else {
         return this == LEFT_RIGHT && direction.getAxis() == Direction.Axis.Z ? direction.getOpposite() : direction;
      }
   }

   public DirectionTransformation getDirectionTransformation() {
      return this.directionTransformation;
   }

   public Text getName() {
      return this.name;
   }

   public String asString() {
      return this.id;
   }

   // $FF: synthetic method
   private static BlockMirror[] method_36706() {
      return new BlockMirror[]{NONE, LEFT_RIGHT, FRONT_BACK};
   }
}
