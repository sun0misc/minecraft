package net.minecraft.util.shape;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class DisjointPairList extends AbstractDoubleList implements PairList {
   private final DoubleList first;
   private final DoubleList second;
   private final boolean inverted;

   protected DisjointPairList(DoubleList first, DoubleList second, boolean inverted) {
      this.first = first;
      this.second = second;
      this.inverted = inverted;
   }

   public int size() {
      return this.first.size() + this.second.size();
   }

   public boolean forEachPair(PairList.Consumer predicate) {
      return this.inverted ? this.iterateSections((x, y, index) -> {
         return predicate.merge(y, x, index);
      }) : this.iterateSections(predicate);
   }

   private boolean iterateSections(PairList.Consumer predicate) {
      int i = this.first.size();

      int j;
      for(j = 0; j < i; ++j) {
         if (!predicate.merge(j, -1, j)) {
            return false;
         }
      }

      j = this.second.size() - 1;

      for(int k = 0; k < j; ++k) {
         if (!predicate.merge(i - 1, k, i + k)) {
            return false;
         }
      }

      return true;
   }

   public double getDouble(int position) {
      return position < this.first.size() ? this.first.getDouble(position) : this.second.getDouble(position - this.first.size());
   }

   public DoubleList getPairs() {
      return this;
   }
}
