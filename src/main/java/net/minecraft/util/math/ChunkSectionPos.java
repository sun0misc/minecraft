package net.minecraft.util.math;

import it.unimi.dsi.fastutil.longs.LongConsumer;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.util.CuboidBlockIterator;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.entity.EntityLike;

public class ChunkSectionPos extends Vec3i {
   public static final int field_33096 = 4;
   public static final int field_33097 = 16;
   public static final int field_33100 = 15;
   public static final int field_33098 = 8;
   public static final int field_33099 = 15;
   private static final int field_33101 = 22;
   private static final int field_33102 = 20;
   private static final int field_33103 = 22;
   private static final long field_33104 = 4194303L;
   private static final long field_33105 = 1048575L;
   private static final long field_33106 = 4194303L;
   private static final int field_33107 = 0;
   private static final int field_33108 = 20;
   private static final int field_33109 = 42;
   private static final int field_33110 = 8;
   private static final int field_33111 = 0;
   private static final int field_33112 = 4;

   ChunkSectionPos(int i, int j, int k) {
      super(i, j, k);
   }

   public static ChunkSectionPos from(int x, int y, int z) {
      return new ChunkSectionPos(x, y, z);
   }

   public static ChunkSectionPos from(BlockPos pos) {
      return new ChunkSectionPos(getSectionCoord(pos.getX()), getSectionCoord(pos.getY()), getSectionCoord(pos.getZ()));
   }

   public static ChunkSectionPos from(ChunkPos chunkPos, int y) {
      return new ChunkSectionPos(chunkPos.x, y, chunkPos.z);
   }

   public static ChunkSectionPos from(EntityLike entity) {
      return from(entity.getBlockPos());
   }

   public static ChunkSectionPos from(Position pos) {
      return new ChunkSectionPos(getSectionCoordFloored(pos.getX()), getSectionCoordFloored(pos.getY()), getSectionCoordFloored(pos.getZ()));
   }

   public static ChunkSectionPos from(long packed) {
      return new ChunkSectionPos(unpackX(packed), unpackY(packed), unpackZ(packed));
   }

   public static ChunkSectionPos from(Chunk chunk) {
      return from(chunk.getPos(), chunk.getBottomSectionCoord());
   }

   public static long offset(long packed, Direction direction) {
      return offset(packed, direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ());
   }

   public static long offset(long packed, int x, int y, int z) {
      return asLong(unpackX(packed) + x, unpackY(packed) + y, unpackZ(packed) + z);
   }

   public static int getSectionCoord(double coord) {
      return getSectionCoord(MathHelper.floor(coord));
   }

   public static int getSectionCoord(int coord) {
      return coord >> 4;
   }

   public static int getSectionCoordFloored(double coord) {
      return MathHelper.floor(coord) >> 4;
   }

   public static int getLocalCoord(int coord) {
      return coord & 15;
   }

   public static short packLocal(BlockPos pos) {
      int i = getLocalCoord(pos.getX());
      int j = getLocalCoord(pos.getY());
      int k = getLocalCoord(pos.getZ());
      return (short)(i << 8 | k << 4 | j << 0);
   }

   public static int unpackLocalX(short packedLocalPos) {
      return packedLocalPos >>> 8 & 15;
   }

   public static int unpackLocalY(short packedLocalPos) {
      return packedLocalPos >>> 0 & 15;
   }

   public static int unpackLocalZ(short packedLocalPos) {
      return packedLocalPos >>> 4 & 15;
   }

   public int unpackBlockX(short packedLocalPos) {
      return this.getMinX() + unpackLocalX(packedLocalPos);
   }

   public int unpackBlockY(short packedLocalPos) {
      return this.getMinY() + unpackLocalY(packedLocalPos);
   }

   public int unpackBlockZ(short packedLocalPos) {
      return this.getMinZ() + unpackLocalZ(packedLocalPos);
   }

   public BlockPos unpackBlockPos(short packedLocalPos) {
      return new BlockPos(this.unpackBlockX(packedLocalPos), this.unpackBlockY(packedLocalPos), this.unpackBlockZ(packedLocalPos));
   }

   public static int getBlockCoord(int sectionCoord) {
      return sectionCoord << 4;
   }

   public static int getOffsetPos(int chunkCoord, int offset) {
      return getBlockCoord(chunkCoord) + offset;
   }

   public static int unpackX(long packed) {
      return (int)(packed << 0 >> 42);
   }

   public static int unpackY(long packed) {
      return (int)(packed << 44 >> 44);
   }

   public static int unpackZ(long packed) {
      return (int)(packed << 22 >> 42);
   }

   public int getSectionX() {
      return this.getX();
   }

   public int getSectionY() {
      return this.getY();
   }

   public int getSectionZ() {
      return this.getZ();
   }

   public int getMinX() {
      return getBlockCoord(this.getSectionX());
   }

   public int getMinY() {
      return getBlockCoord(this.getSectionY());
   }

   public int getMinZ() {
      return getBlockCoord(this.getSectionZ());
   }

   public int getMaxX() {
      return getOffsetPos(this.getSectionX(), 15);
   }

   public int getMaxY() {
      return getOffsetPos(this.getSectionY(), 15);
   }

   public int getMaxZ() {
      return getOffsetPos(this.getSectionZ(), 15);
   }

   public static long fromBlockPos(long blockPos) {
      return asLong(getSectionCoord(BlockPos.unpackLongX(blockPos)), getSectionCoord(BlockPos.unpackLongY(blockPos)), getSectionCoord(BlockPos.unpackLongZ(blockPos)));
   }

   public static long withZeroY(long pos) {
      return pos & -1048576L;
   }

   public BlockPos getMinPos() {
      return new BlockPos(getBlockCoord(this.getSectionX()), getBlockCoord(this.getSectionY()), getBlockCoord(this.getSectionZ()));
   }

   public BlockPos getCenterPos() {
      int i = true;
      return this.getMinPos().add(8, 8, 8);
   }

   public ChunkPos toChunkPos() {
      return new ChunkPos(this.getSectionX(), this.getSectionZ());
   }

   public static long toLong(BlockPos pos) {
      return asLong(getSectionCoord(pos.getX()), getSectionCoord(pos.getY()), getSectionCoord(pos.getZ()));
   }

   public static long asLong(int x, int y, int z) {
      long l = 0L;
      l |= ((long)x & 4194303L) << 42;
      l |= ((long)y & 1048575L) << 0;
      l |= ((long)z & 4194303L) << 20;
      return l;
   }

   public long asLong() {
      return asLong(this.getSectionX(), this.getSectionY(), this.getSectionZ());
   }

   public ChunkSectionPos add(int i, int j, int k) {
      return i == 0 && j == 0 && k == 0 ? this : new ChunkSectionPos(this.getSectionX() + i, this.getSectionY() + j, this.getSectionZ() + k);
   }

   public Stream streamBlocks() {
      return BlockPos.stream(this.getMinX(), this.getMinY(), this.getMinZ(), this.getMaxX(), this.getMaxY(), this.getMaxZ());
   }

   public static Stream stream(ChunkSectionPos center, int radius) {
      int j = center.getSectionX();
      int k = center.getSectionY();
      int l = center.getSectionZ();
      return stream(j - radius, k - radius, l - radius, j + radius, k + radius, l + radius);
   }

   public static Stream stream(ChunkPos center, int radius, int minY, int maxY) {
      int l = center.x;
      int m = center.z;
      return stream(l - radius, minY, m - radius, l + radius, maxY - 1, m + radius);
   }

   public static Stream stream(final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ) {
      return StreamSupport.stream(new Spliterators.AbstractSpliterator((long)((maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1)), 64) {
         final CuboidBlockIterator iterator = new CuboidBlockIterator(minX, minY, minZ, maxX, maxY, maxZ);

         public boolean tryAdvance(Consumer consumer) {
            if (this.iterator.step()) {
               consumer.accept(new ChunkSectionPos(this.iterator.getX(), this.iterator.getY(), this.iterator.getZ()));
               return true;
            } else {
               return false;
            }
         }
      }, false);
   }

   public static void forEachChunkSectionAround(BlockPos pos, LongConsumer consumer) {
      forEachChunkSectionAround(pos.getX(), pos.getY(), pos.getZ(), consumer);
   }

   public static void forEachChunkSectionAround(long pos, LongConsumer consumer) {
      forEachChunkSectionAround(BlockPos.unpackLongX(pos), BlockPos.unpackLongY(pos), BlockPos.unpackLongZ(pos), consumer);
   }

   public static void forEachChunkSectionAround(int x, int y, int z, LongConsumer consumer) {
      int l = getSectionCoord(x - 1);
      int m = getSectionCoord(x + 1);
      int n = getSectionCoord(y - 1);
      int o = getSectionCoord(y + 1);
      int p = getSectionCoord(z - 1);
      int q = getSectionCoord(z + 1);
      if (l == m && n == o && p == q) {
         consumer.accept(asLong(l, n, p));
      } else {
         for(int r = l; r <= m; ++r) {
            for(int s = n; s <= o; ++s) {
               for(int t = p; t <= q; ++t) {
                  consumer.accept(asLong(r, s, t));
               }
            }
         }
      }

   }

   // $FF: synthetic method
   public Vec3i add(int x, int y, int z) {
      return this.add(x, y, z);
   }
}
