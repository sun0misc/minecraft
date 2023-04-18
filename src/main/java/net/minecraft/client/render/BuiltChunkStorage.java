package net.minecraft.client.render;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BuiltChunkStorage {
   protected final WorldRenderer worldRenderer;
   protected final World world;
   protected int sizeY;
   protected int sizeX;
   protected int sizeZ;
   public ChunkBuilder.BuiltChunk[] chunks;

   public BuiltChunkStorage(ChunkBuilder chunkBuilder, World world, int viewDistance, WorldRenderer worldRenderer) {
      this.worldRenderer = worldRenderer;
      this.world = world;
      this.setViewDistance(viewDistance);
      this.createChunks(chunkBuilder);
   }

   protected void createChunks(ChunkBuilder chunkBuilder) {
      if (!MinecraftClient.getInstance().isOnThread()) {
         throw new IllegalStateException("createChunks called from wrong thread: " + Thread.currentThread().getName());
      } else {
         int i = this.sizeX * this.sizeY * this.sizeZ;
         this.chunks = new ChunkBuilder.BuiltChunk[i];

         for(int j = 0; j < this.sizeX; ++j) {
            for(int k = 0; k < this.sizeY; ++k) {
               for(int l = 0; l < this.sizeZ; ++l) {
                  int m = this.getChunkIndex(j, k, l);
                  ChunkBuilder.BuiltChunk[] var10000 = this.chunks;
                  Objects.requireNonNull(chunkBuilder);
                  var10000[m] = chunkBuilder.new BuiltChunk(m, j * 16, k * 16, l * 16);
               }
            }
         }

      }
   }

   public void clear() {
      ChunkBuilder.BuiltChunk[] var1 = this.chunks;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         ChunkBuilder.BuiltChunk lv = var1[var3];
         lv.delete();
      }

   }

   private int getChunkIndex(int x, int y, int z) {
      return (z * this.sizeY + y) * this.sizeX + x;
   }

   protected void setViewDistance(int viewDistance) {
      int j = viewDistance * 2 + 1;
      this.sizeX = j;
      this.sizeY = this.world.countVerticalSections();
      this.sizeZ = j;
   }

   public void updateCameraPosition(double x, double z) {
      int i = MathHelper.ceil(x);
      int j = MathHelper.ceil(z);

      for(int k = 0; k < this.sizeX; ++k) {
         int l = this.sizeX * 16;
         int m = i - 8 - l / 2;
         int n = m + Math.floorMod(k * 16 - m, l);

         for(int o = 0; o < this.sizeZ; ++o) {
            int p = this.sizeZ * 16;
            int q = j - 8 - p / 2;
            int r = q + Math.floorMod(o * 16 - q, p);

            for(int s = 0; s < this.sizeY; ++s) {
               int t = this.world.getBottomY() + s * 16;
               ChunkBuilder.BuiltChunk lv = this.chunks[this.getChunkIndex(k, s, o)];
               BlockPos lv2 = lv.getOrigin();
               if (n != lv2.getX() || t != lv2.getY() || r != lv2.getZ()) {
                  lv.setOrigin(n, t, r);
               }
            }
         }
      }

   }

   public void scheduleRebuild(int x, int y, int z, boolean important) {
      int l = Math.floorMod(x, this.sizeX);
      int m = Math.floorMod(y - this.world.getBottomSectionCoord(), this.sizeY);
      int n = Math.floorMod(z, this.sizeZ);
      ChunkBuilder.BuiltChunk lv = this.chunks[this.getChunkIndex(l, m, n)];
      lv.scheduleRebuild(important);
   }

   @Nullable
   protected ChunkBuilder.BuiltChunk getRenderedChunk(BlockPos pos) {
      int i = MathHelper.floorDiv(pos.getX(), 16);
      int j = MathHelper.floorDiv(pos.getY() - this.world.getBottomY(), 16);
      int k = MathHelper.floorDiv(pos.getZ(), 16);
      if (j >= 0 && j < this.sizeY) {
         i = MathHelper.floorMod(i, this.sizeX);
         k = MathHelper.floorMod(k, this.sizeZ);
         return this.chunks[this.getChunkIndex(i, j, k)];
      } else {
         return null;
      }
   }
}
