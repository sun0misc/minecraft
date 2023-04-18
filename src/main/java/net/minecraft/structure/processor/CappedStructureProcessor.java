package net.minecraft.structure.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;

public class CappedStructureProcessor extends StructureProcessor {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(StructureProcessorType.CODEC.fieldOf("delegate").forGetter((processor) -> {
         return processor.delegate;
      }), IntProvider.POSITIVE_CODEC.fieldOf("limit").forGetter((processor) -> {
         return processor.limit;
      })).apply(instance, CappedStructureProcessor::new);
   });
   private final StructureProcessor delegate;
   private final IntProvider limit;

   public CappedStructureProcessor(StructureProcessor delegate, IntProvider limit) {
      this.delegate = delegate;
      this.limit = limit;
   }

   protected StructureProcessorType getType() {
      return StructureProcessorType.CAPPED;
   }

   public final List reprocess(ServerWorldAccess world, BlockPos pos, BlockPos pivot, List originalBlockInfos, List currentBlockInfos, StructurePlacementData data) {
      if (this.limit.getMax() != 0 && !currentBlockInfos.isEmpty()) {
         if (originalBlockInfos.size() != currentBlockInfos.size()) {
            int var10000 = originalBlockInfos.size();
            Util.error("Original block info list not in sync with processed list, skipping processing. Original size: " + var10000 + ", Processed size: " + currentBlockInfos.size());
            return currentBlockInfos;
         } else {
            Random lv = Random.create(world.toServerWorld().getSeed()).nextSplitter().split(pos);
            int i = Math.min(this.limit.get(lv), currentBlockInfos.size());
            if (i < 1) {
               return currentBlockInfos;
            } else {
               IntArrayList intArrayList = Util.shuffle(IntStream.range(0, currentBlockInfos.size()), lv);
               IntIterator intIterator = intArrayList.intIterator();
               int j = 0;

               while(intIterator.hasNext() && j < i) {
                  int k = intIterator.nextInt();
                  StructureTemplate.StructureBlockInfo lv2 = (StructureTemplate.StructureBlockInfo)originalBlockInfos.get(k);
                  StructureTemplate.StructureBlockInfo lv3 = (StructureTemplate.StructureBlockInfo)currentBlockInfos.get(k);
                  StructureTemplate.StructureBlockInfo lv4 = this.delegate.process(world, pos, pivot, lv2, lv3, data);
                  if (lv4 != null && !lv3.equals(lv4)) {
                     ++j;
                     currentBlockInfos.set(k, lv4);
                  }
               }

               return currentBlockInfos;
            }
         }
      } else {
         return currentBlockInfos;
      }
   }
}
