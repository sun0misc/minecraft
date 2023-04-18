package net.minecraft.util.math.intprovider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.random.Random;

public class WeightedListIntProvider extends IntProvider {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(DataPool.createCodec(IntProvider.VALUE_CODEC).fieldOf("distribution").forGetter((provider) -> {
         return provider.weightedList;
      })).apply(instance, WeightedListIntProvider::new);
   });
   private final DataPool weightedList;
   private final int min;
   private final int max;

   public WeightedListIntProvider(DataPool weightedList) {
      this.weightedList = weightedList;
      List list = weightedList.getEntries();
      int i = Integer.MAX_VALUE;
      int j = Integer.MIN_VALUE;

      int l;
      for(Iterator var5 = list.iterator(); var5.hasNext(); j = Math.max(j, l)) {
         Weighted.Present lv = (Weighted.Present)var5.next();
         int k = ((IntProvider)lv.getData()).getMin();
         l = ((IntProvider)lv.getData()).getMax();
         i = Math.min(i, k);
      }

      this.min = i;
      this.max = j;
   }

   public int get(Random random) {
      return ((IntProvider)this.weightedList.getDataOrEmpty(random).orElseThrow(IllegalStateException::new)).get(random);
   }

   public int getMin() {
      return this.min;
   }

   public int getMax() {
      return this.max;
   }

   public IntProviderType getType() {
      return IntProviderType.WEIGHTED_LIST;
   }
}
