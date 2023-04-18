package net.minecraft.world.gen.heightprovider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.HeightContext;

public class WeightedListHeightProvider extends HeightProvider {
   public static final Codec WEIGHTED_LIST_CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(DataPool.createCodec(HeightProvider.CODEC).fieldOf("distribution").forGetter((arg) -> {
         return arg.weightedList;
      })).apply(instance, WeightedListHeightProvider::new);
   });
   private final DataPool weightedList;

   public WeightedListHeightProvider(DataPool weightedList) {
      this.weightedList = weightedList;
   }

   public int get(Random random, HeightContext context) {
      return ((HeightProvider)this.weightedList.getDataOrEmpty(random).orElseThrow(IllegalStateException::new)).get(random, context);
   }

   public HeightProviderType getType() {
      return HeightProviderType.WEIGHTED_LIST;
   }
}
