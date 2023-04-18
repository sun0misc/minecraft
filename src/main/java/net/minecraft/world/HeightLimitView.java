package net.minecraft.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

public interface HeightLimitView {
   int getHeight();

   int getBottomY();

   default int getTopY() {
      return this.getBottomY() + this.getHeight();
   }

   default int countVerticalSections() {
      return this.getTopSectionCoord() - this.getBottomSectionCoord();
   }

   default int getBottomSectionCoord() {
      return ChunkSectionPos.getSectionCoord(this.getBottomY());
   }

   default int getTopSectionCoord() {
      return ChunkSectionPos.getSectionCoord(this.getTopY() - 1) + 1;
   }

   default boolean isOutOfHeightLimit(BlockPos pos) {
      return this.isOutOfHeightLimit(pos.getY());
   }

   default boolean isOutOfHeightLimit(int y) {
      return y < this.getBottomY() || y >= this.getTopY();
   }

   default int getSectionIndex(int y) {
      return this.sectionCoordToIndex(ChunkSectionPos.getSectionCoord(y));
   }

   default int sectionCoordToIndex(int coord) {
      return coord - this.getBottomSectionCoord();
   }

   default int sectionIndexToCoord(int index) {
      return index + this.getBottomSectionCoord();
   }

   static HeightLimitView create(final int bottomY, final int height) {
      return new HeightLimitView() {
         public int getHeight() {
            return height;
         }

         public int getBottomY() {
            return bottomY;
         }
      };
   }
}
