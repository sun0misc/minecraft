package net.minecraft.world.gen;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Iterator;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

public class StructureWeightSampler implements DensityFunctionTypes.Beardifying {
   public static final int field_31461 = 12;
   private static final int field_31462 = 24;
   private static final float[] STRUCTURE_WEIGHT_TABLE = (float[])Util.make(new float[13824], (array) -> {
      for(int i = 0; i < 24; ++i) {
         for(int j = 0; j < 24; ++j) {
            for(int k = 0; k < 24; ++k) {
               array[i * 24 * 24 + j * 24 + k] = (float)calculateStructureWeight(j - 12, k - 12, i - 12);
            }
         }
      }

   });
   private final ObjectListIterator pieceIterator;
   private final ObjectListIterator junctionIterator;

   public static StructureWeightSampler createStructureWeightSampler(StructureAccessor world, ChunkPos pos) {
      int i = pos.getStartX();
      int j = pos.getStartZ();
      ObjectList objectList = new ObjectArrayList(10);
      ObjectList objectList2 = new ObjectArrayList(32);
      world.getStructureStarts(pos, (structure) -> {
         return structure.getTerrainAdaptation() != StructureTerrainAdaptation.NONE;
      }).forEach((start) -> {
         StructureTerrainAdaptation lv = start.getStructure().getTerrainAdaptation();
         Iterator var7 = start.getChildren().iterator();

         while(true) {
            while(true) {
               StructurePiece lv2;
               do {
                  if (!var7.hasNext()) {
                     return;
                  }

                  lv2 = (StructurePiece)var7.next();
               } while(!lv2.intersectsChunk(pos, 12));

               if (lv2 instanceof PoolStructurePiece lv3) {
                  StructurePool.Projection lv4 = lv3.getPoolElement().getProjection();
                  if (lv4 == StructurePool.Projection.RIGID) {
                     objectList.add(new Piece(lv3.getBoundingBox(), lv, lv3.getGroundLevelDelta()));
                  }

                  Iterator var11 = lv3.getJunctions().iterator();

                  while(var11.hasNext()) {
                     JigsawJunction lv5 = (JigsawJunction)var11.next();
                     int k = lv5.getSourceX();
                     int l = lv5.getSourceZ();
                     if (k > i - 12 && l > j - 12 && k < i + 15 + 12 && l < j + 15 + 12) {
                        objectList2.add(lv5);
                     }
                  }
               } else {
                  objectList.add(new Piece(lv2.getBoundingBox(), lv, 0));
               }
            }
         }
      });
      return new StructureWeightSampler(objectList.iterator(), objectList2.iterator());
   }

   @VisibleForTesting
   public StructureWeightSampler(ObjectListIterator pieceIterator, ObjectListIterator junctionIterator) {
      this.pieceIterator = pieceIterator;
      this.junctionIterator = junctionIterator;
   }

   public double sample(DensityFunction.NoisePos pos) {
      int i = pos.blockX();
      int j = pos.blockY();
      int k = pos.blockZ();

      double d;
      int l;
      int m;
      double var10001;
      for(d = 0.0; this.pieceIterator.hasNext(); d += var10001) {
         Piece lv = (Piece)this.pieceIterator.next();
         BlockBox lv2 = lv.box();
         l = lv.groundLevelDelta();
         m = Math.max(0, Math.max(lv2.getMinX() - i, i - lv2.getMaxX()));
         int n = Math.max(0, Math.max(lv2.getMinZ() - k, k - lv2.getMaxZ()));
         int o = lv2.getMinY() + l;
         int p = j - o;
         int var10000;
         switch (lv.terrainAdjustment()) {
            case NONE:
               var10000 = 0;
               break;
            case BURY:
            case BEARD_THIN:
               var10000 = p;
               break;
            case BEARD_BOX:
               var10000 = Math.max(0, Math.max(o - j, j - lv2.getMaxY()));
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         int q = var10000;
         switch (lv.terrainAdjustment()) {
            case NONE:
               var10001 = 0.0;
               break;
            case BURY:
               var10001 = getMagnitudeWeight(m, q, n);
               break;
            case BEARD_THIN:
            case BEARD_BOX:
               var10001 = getStructureWeight(m, q, n, p) * 0.8;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }
      }

      this.pieceIterator.back(Integer.MAX_VALUE);

      while(this.junctionIterator.hasNext()) {
         JigsawJunction lv3 = (JigsawJunction)this.junctionIterator.next();
         int r = i - lv3.getSourceX();
         l = j - lv3.getSourceGroundY();
         m = k - lv3.getSourceZ();
         d += getStructureWeight(r, l, m, l) * 0.4;
      }

      this.junctionIterator.back(Integer.MAX_VALUE);
      return d;
   }

   public double minValue() {
      return Double.NEGATIVE_INFINITY;
   }

   public double maxValue() {
      return Double.POSITIVE_INFINITY;
   }

   private static double getMagnitudeWeight(int x, int y, int z) {
      double d = MathHelper.magnitude((double)x, (double)y / 2.0, (double)z);
      return MathHelper.clampedMap(d, 0.0, 6.0, 1.0, 0.0);
   }

   private static double getStructureWeight(int x, int y, int z, int l) {
      int m = x + 12;
      int n = y + 12;
      int o = z + 12;
      if (method_42692(m) && method_42692(n) && method_42692(o)) {
         double d = (double)l + 0.5;
         double e = MathHelper.squaredMagnitude((double)x, d, (double)z);
         double f = -d * MathHelper.fastInverseSqrt(e / 2.0) / 2.0;
         return f * (double)STRUCTURE_WEIGHT_TABLE[o * 24 * 24 + m * 24 + n];
      } else {
         return 0.0;
      }
   }

   private static boolean method_42692(int i) {
      return i >= 0 && i < 24;
   }

   private static double calculateStructureWeight(int x, int y, int z) {
      return method_42693(x, (double)y + 0.5, z);
   }

   private static double method_42693(int i, double d, int j) {
      double e = MathHelper.squaredMagnitude((double)i, d, (double)j);
      double f = Math.pow(Math.E, -e / 16.0);
      return f;
   }

   @VisibleForTesting
   public static record Piece(BlockBox box, StructureTerrainAdaptation terrainAdjustment, int groundLevelDelta) {
      public Piece(BlockBox arg, StructureTerrainAdaptation arg2, int i) {
         this.box = arg;
         this.terrainAdjustment = arg2;
         this.groundLevelDelta = i;
      }

      public BlockBox box() {
         return this.box;
      }

      public StructureTerrainAdaptation terrainAdjustment() {
         return this.terrainAdjustment;
      }

      public int groundLevelDelta() {
         return this.groundLevelDelta;
      }
   }
}
