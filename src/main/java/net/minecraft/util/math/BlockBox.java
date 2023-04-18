package net.minecraft.util.math;

import com.google.common.base.MoreObjects;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import net.minecraft.SharedConstants;
import net.minecraft.util.Util;
import org.slf4j.Logger;

public class BlockBox {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Codec CODEC;
   private int minX;
   private int minY;
   private int minZ;
   private int maxX;
   private int maxY;
   private int maxZ;

   public BlockBox(BlockPos pos) {
      this(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
   }

   public BlockBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      this.minX = minX;
      this.minY = minY;
      this.minZ = minZ;
      this.maxX = maxX;
      this.maxY = maxY;
      this.maxZ = maxZ;
      if (maxX < minX || maxY < minY || maxZ < minZ) {
         String string = "Invalid bounding box data, inverted bounds for: " + this;
         if (SharedConstants.isDevelopment) {
            throw new IllegalStateException(string);
         }

         LOGGER.error(string);
         this.minX = Math.min(minX, maxX);
         this.minY = Math.min(minY, maxY);
         this.minZ = Math.min(minZ, maxZ);
         this.maxX = Math.max(minX, maxX);
         this.maxY = Math.max(minY, maxY);
         this.maxZ = Math.max(minZ, maxZ);
      }

   }

   public static BlockBox create(Vec3i first, Vec3i second) {
      return new BlockBox(Math.min(first.getX(), second.getX()), Math.min(first.getY(), second.getY()), Math.min(first.getZ(), second.getZ()), Math.max(first.getX(), second.getX()), Math.max(first.getY(), second.getY()), Math.max(first.getZ(), second.getZ()));
   }

   public static BlockBox infinite() {
      return new BlockBox(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
   }

   public static BlockBox rotated(int x, int y, int z, int offsetX, int offsetY, int offsetZ, int sizeX, int sizeY, int sizeZ, Direction facing) {
      switch (facing) {
         case SOUTH:
         default:
            return new BlockBox(x + offsetX, y + offsetY, z + offsetZ, x + sizeX - 1 + offsetX, y + sizeY - 1 + offsetY, z + sizeZ - 1 + offsetZ);
         case NORTH:
            return new BlockBox(x + offsetX, y + offsetY, z - sizeZ + 1 + offsetZ, x + sizeX - 1 + offsetX, y + sizeY - 1 + offsetY, z + offsetZ);
         case WEST:
            return new BlockBox(x - sizeZ + 1 + offsetZ, y + offsetY, z + offsetX, x + offsetZ, y + sizeY - 1 + offsetY, z + sizeX - 1 + offsetX);
         case EAST:
            return new BlockBox(x + offsetZ, y + offsetY, z + offsetX, x + sizeZ - 1 + offsetZ, y + sizeY - 1 + offsetY, z + sizeX - 1 + offsetX);
      }
   }

   public boolean intersects(BlockBox other) {
      return this.maxX >= other.minX && this.minX <= other.maxX && this.maxZ >= other.minZ && this.minZ <= other.maxZ && this.maxY >= other.minY && this.minY <= other.maxY;
   }

   public boolean intersectsXZ(int minX, int minZ, int maxX, int maxZ) {
      return this.maxX >= minX && this.minX <= maxX && this.maxZ >= minZ && this.minZ <= maxZ;
   }

   public static Optional encompassPositions(Iterable positions) {
      Iterator iterator = positions.iterator();
      if (!iterator.hasNext()) {
         return Optional.empty();
      } else {
         BlockBox lv = new BlockBox((BlockPos)iterator.next());
         Objects.requireNonNull(lv);
         iterator.forEachRemaining(lv::encompass);
         return Optional.of(lv);
      }
   }

   public static Optional encompass(Iterable boxes) {
      Iterator iterator = boxes.iterator();
      if (!iterator.hasNext()) {
         return Optional.empty();
      } else {
         BlockBox lv = (BlockBox)iterator.next();
         BlockBox lv2 = new BlockBox(lv.minX, lv.minY, lv.minZ, lv.maxX, lv.maxY, lv.maxZ);
         Objects.requireNonNull(lv2);
         iterator.forEachRemaining(lv2::encompass);
         return Optional.of(lv2);
      }
   }

   /** @deprecated */
   @Deprecated
   public BlockBox encompass(BlockBox box) {
      this.minX = Math.min(this.minX, box.minX);
      this.minY = Math.min(this.minY, box.minY);
      this.minZ = Math.min(this.minZ, box.minZ);
      this.maxX = Math.max(this.maxX, box.maxX);
      this.maxY = Math.max(this.maxY, box.maxY);
      this.maxZ = Math.max(this.maxZ, box.maxZ);
      return this;
   }

   /** @deprecated */
   @Deprecated
   public BlockBox encompass(BlockPos pos) {
      this.minX = Math.min(this.minX, pos.getX());
      this.minY = Math.min(this.minY, pos.getY());
      this.minZ = Math.min(this.minZ, pos.getZ());
      this.maxX = Math.max(this.maxX, pos.getX());
      this.maxY = Math.max(this.maxY, pos.getY());
      this.maxZ = Math.max(this.maxZ, pos.getZ());
      return this;
   }

   /** @deprecated */
   @Deprecated
   public BlockBox move(int dx, int dy, int dz) {
      this.minX += dx;
      this.minY += dy;
      this.minZ += dz;
      this.maxX += dx;
      this.maxY += dy;
      this.maxZ += dz;
      return this;
   }

   /** @deprecated */
   @Deprecated
   public BlockBox move(Vec3i vec) {
      return this.move(vec.getX(), vec.getY(), vec.getZ());
   }

   public BlockBox offset(int x, int y, int z) {
      return new BlockBox(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
   }

   public BlockBox expand(int offset) {
      return new BlockBox(this.getMinX() - offset, this.getMinY() - offset, this.getMinZ() - offset, this.getMaxX() + offset, this.getMaxY() + offset, this.getMaxZ() + offset);
   }

   public boolean contains(Vec3i pos) {
      return this.contains(pos.getX(), pos.getY(), pos.getZ());
   }

   public boolean contains(int x, int y, int z) {
      return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ && y >= this.minY && y <= this.maxY;
   }

   public Vec3i getDimensions() {
      return new Vec3i(this.maxX - this.minX, this.maxY - this.minY, this.maxZ - this.minZ);
   }

   public int getBlockCountX() {
      return this.maxX - this.minX + 1;
   }

   public int getBlockCountY() {
      return this.maxY - this.minY + 1;
   }

   public int getBlockCountZ() {
      return this.maxZ - this.minZ + 1;
   }

   public BlockPos getCenter() {
      return new BlockPos(this.minX + (this.maxX - this.minX + 1) / 2, this.minY + (this.maxY - this.minY + 1) / 2, this.minZ + (this.maxZ - this.minZ + 1) / 2);
   }

   public void forEachVertex(Consumer consumer) {
      BlockPos.Mutable lv = new BlockPos.Mutable();
      consumer.accept(lv.set(this.maxX, this.maxY, this.maxZ));
      consumer.accept(lv.set(this.minX, this.maxY, this.maxZ));
      consumer.accept(lv.set(this.maxX, this.minY, this.maxZ));
      consumer.accept(lv.set(this.minX, this.minY, this.maxZ));
      consumer.accept(lv.set(this.maxX, this.maxY, this.minZ));
      consumer.accept(lv.set(this.minX, this.maxY, this.minZ));
      consumer.accept(lv.set(this.maxX, this.minY, this.minZ));
      consumer.accept(lv.set(this.minX, this.minY, this.minZ));
   }

   public String toString() {
      return MoreObjects.toStringHelper(this).add("minX", this.minX).add("minY", this.minY).add("minZ", this.minZ).add("maxX", this.maxX).add("maxY", this.maxY).add("maxZ", this.maxZ).toString();
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof BlockBox)) {
         return false;
      } else {
         BlockBox lv = (BlockBox)o;
         return this.minX == lv.minX && this.minY == lv.minY && this.minZ == lv.minZ && this.maxX == lv.maxX && this.maxY == lv.maxY && this.maxZ == lv.maxZ;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ});
   }

   public int getMinX() {
      return this.minX;
   }

   public int getMinY() {
      return this.minY;
   }

   public int getMinZ() {
      return this.minZ;
   }

   public int getMaxX() {
      return this.maxX;
   }

   public int getMaxY() {
      return this.maxY;
   }

   public int getMaxZ() {
      return this.maxZ;
   }

   static {
      CODEC = Codec.INT_STREAM.comapFlatMap((values) -> {
         return Util.toArray((IntStream)values, 6).map((array) -> {
            return new BlockBox(array[0], array[1], array[2], array[3], array[4], array[5]);
         });
      }, (box) -> {
         return IntStream.of(new int[]{box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ});
      }).stable();
   }
}
