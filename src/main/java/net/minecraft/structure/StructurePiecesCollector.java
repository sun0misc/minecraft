package net.minecraft.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class StructurePiecesCollector implements StructurePiecesHolder {
   private final List pieces = Lists.newArrayList();

   public void addPiece(StructurePiece piece) {
      this.pieces.add(piece);
   }

   @Nullable
   public StructurePiece getIntersecting(BlockBox box) {
      return StructurePiece.firstIntersecting(this.pieces, box);
   }

   /** @deprecated */
   @Deprecated
   public void shift(int y) {
      Iterator var2 = this.pieces.iterator();

      while(var2.hasNext()) {
         StructurePiece lv = (StructurePiece)var2.next();
         lv.translate(0, y, 0);
      }

   }

   /** @deprecated */
   @Deprecated
   public int shiftInto(int topY, int bottomY, Random random, int topPenalty) {
      int l = topY - topPenalty;
      BlockBox lv = this.getBoundingBox();
      int m = lv.getBlockCountY() + bottomY + 1;
      if (m < l) {
         m += random.nextInt(l - m);
      }

      int n = m - lv.getMaxY();
      this.shift(n);
      return n;
   }

   /** @deprecated */
   public void shiftInto(Random random, int baseY, int topY) {
      BlockBox lv = this.getBoundingBox();
      int k = topY - baseY + 1 - lv.getBlockCountY();
      int l;
      if (k > 1) {
         l = baseY + random.nextInt(k);
      } else {
         l = baseY;
      }

      int m = l - lv.getMinY();
      this.shift(m);
   }

   public StructurePiecesList toList() {
      return new StructurePiecesList(this.pieces);
   }

   public void clear() {
      this.pieces.clear();
   }

   public boolean isEmpty() {
      return this.pieces.isEmpty();
   }

   public BlockBox getBoundingBox() {
      return StructurePiece.boundingBox(this.pieces.stream());
   }
}
