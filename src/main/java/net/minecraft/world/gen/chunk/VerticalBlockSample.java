package net.minecraft.world.gen.chunk;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public final class VerticalBlockSample implements BlockColumn {
   private final int startY;
   private final BlockState[] states;

   public VerticalBlockSample(int startY, BlockState[] states) {
      this.startY = startY;
      this.states = states;
   }

   public BlockState getState(int y) {
      int j = y - this.startY;
      return j >= 0 && j < this.states.length ? this.states[j] : Blocks.AIR.getDefaultState();
   }

   public void setState(int y, BlockState state) {
      int j = y - this.startY;
      if (j >= 0 && j < this.states.length) {
         this.states[j] = state;
      } else {
         throw new IllegalArgumentException("Outside of column height: " + y);
      }
   }
}
