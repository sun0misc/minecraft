package net.minecraft.world;

import com.google.common.collect.AbstractIterator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.CuboidBlockIterator;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

public class BlockCollisionSpliterator extends AbstractIterator {
   private final Box box;
   private final ShapeContext context;
   private final CuboidBlockIterator blockIterator;
   private final BlockPos.Mutable pos;
   private final VoxelShape boxShape;
   private final CollisionView world;
   private final boolean forEntity;
   @Nullable
   private BlockView chunk;
   private long chunkPos;

   public BlockCollisionSpliterator(CollisionView world, @Nullable Entity entity, Box box) {
      this(world, entity, box, false);
   }

   public BlockCollisionSpliterator(CollisionView world, @Nullable Entity entity, Box box, boolean forEntity) {
      this.context = entity == null ? ShapeContext.absent() : ShapeContext.of(entity);
      this.pos = new BlockPos.Mutable();
      this.boxShape = VoxelShapes.cuboid(box);
      this.world = world;
      this.box = box;
      this.forEntity = forEntity;
      int i = MathHelper.floor(box.minX - 1.0E-7) - 1;
      int j = MathHelper.floor(box.maxX + 1.0E-7) + 1;
      int k = MathHelper.floor(box.minY - 1.0E-7) - 1;
      int l = MathHelper.floor(box.maxY + 1.0E-7) + 1;
      int m = MathHelper.floor(box.minZ - 1.0E-7) - 1;
      int n = MathHelper.floor(box.maxZ + 1.0E-7) + 1;
      this.blockIterator = new CuboidBlockIterator(i, k, m, j, l, n);
   }

   @Nullable
   private BlockView getChunk(int x, int z) {
      int k = ChunkSectionPos.getSectionCoord(x);
      int l = ChunkSectionPos.getSectionCoord(z);
      long m = ChunkPos.toLong(k, l);
      if (this.chunk != null && this.chunkPos == m) {
         return this.chunk;
      } else {
         BlockView lv = this.world.getChunkAsView(k, l);
         this.chunk = lv;
         this.chunkPos = m;
         return lv;
      }
   }

   protected VoxelShape computeNext() {
      while(true) {
         if (this.blockIterator.step()) {
            int i = this.blockIterator.getX();
            int j = this.blockIterator.getY();
            int k = this.blockIterator.getZ();
            int l = this.blockIterator.getEdgeCoordinatesCount();
            if (l == 3) {
               continue;
            }

            BlockView lv = this.getChunk(i, k);
            if (lv == null) {
               continue;
            }

            this.pos.set(i, j, k);
            BlockState lv2 = lv.getBlockState(this.pos);
            if (this.forEntity && !lv2.shouldSuffocate(lv, this.pos) || l == 1 && !lv2.exceedsCube() || l == 2 && !lv2.isOf(Blocks.MOVING_PISTON)) {
               continue;
            }

            VoxelShape lv3 = lv2.getCollisionShape(this.world, this.pos, this.context);
            if (lv3 == VoxelShapes.fullCube()) {
               if (!this.box.intersects((double)i, (double)j, (double)k, (double)i + 1.0, (double)j + 1.0, (double)k + 1.0)) {
                  continue;
               }

               return lv3.offset((double)i, (double)j, (double)k);
            }

            VoxelShape lv4 = lv3.offset((double)i, (double)j, (double)k);
            if (!VoxelShapes.matchesAnywhere(lv4, this.boxShape, BooleanBiFunction.AND)) {
               continue;
            }

            return lv4;
         }

         return (VoxelShape)this.endOfData();
      }
   }

   // $FF: synthetic method
   protected Object computeNext() {
      return this.computeNext();
   }
}
