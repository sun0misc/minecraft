package net.minecraft.test;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class PositionedException extends GameTestException {
   private final BlockPos pos;
   private final BlockPos relativePos;
   private final long tick;

   public PositionedException(String message, BlockPos pos, BlockPos relativePos, long tick) {
      super(message);
      this.pos = pos;
      this.relativePos = relativePos;
      this.tick = tick;
   }

   public String getMessage() {
      int var10000 = this.pos.getX();
      String string = "" + var10000 + "," + this.pos.getY() + "," + this.pos.getZ() + " (relative: " + this.relativePos.getX() + "," + this.relativePos.getY() + "," + this.relativePos.getZ() + ")";
      return super.getMessage() + " at " + string + " (t=" + this.tick + ")";
   }

   @Nullable
   public String getDebugMessage() {
      return super.getMessage();
   }

   @Nullable
   public BlockPos getRelativePos() {
      return this.relativePos;
   }

   @Nullable
   public BlockPos getPos() {
      return this.pos;
   }
}
