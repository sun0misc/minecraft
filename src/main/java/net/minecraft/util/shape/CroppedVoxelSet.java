package net.minecraft.util.shape;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public final class CroppedVoxelSet extends VoxelSet {
   private final VoxelSet parent;
   private final int minX;
   private final int minY;
   private final int minZ;
   private final int maxX;
   private final int maxY;
   private final int maxZ;

   protected CroppedVoxelSet(VoxelSet parent, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      super(maxX - minX, maxY - minY, maxZ - minZ);
      this.parent = parent;
      this.minX = minX;
      this.minY = minY;
      this.minZ = minZ;
      this.maxX = maxX;
      this.maxY = maxY;
      this.maxZ = maxZ;
   }

   public boolean contains(int x, int y, int z) {
      return this.parent.contains(this.minX + x, this.minY + y, this.minZ + z);
   }

   public void set(int x, int y, int z) {
      this.parent.set(this.minX + x, this.minY + y, this.minZ + z);
   }

   public int getMin(Direction.Axis axis) {
      return this.clamp(axis, this.parent.getMin(axis));
   }

   public int getMax(Direction.Axis axis) {
      return this.clamp(axis, this.parent.getMax(axis));
   }

   private int clamp(Direction.Axis axis, int value) {
      int j = axis.choose(this.minX, this.minY, this.minZ);
      int k = axis.choose(this.maxX, this.maxY, this.maxZ);
      return MathHelper.clamp(value, j, k) - j;
   }
}
