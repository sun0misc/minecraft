/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure.processor;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;

public class CappedStructureProcessor
extends StructureProcessor {
    public static final MapCodec<CappedStructureProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)StructureProcessorType.CODEC.fieldOf("delegate")).forGetter(processor -> processor.delegate), ((MapCodec)IntProvider.POSITIVE_CODEC.fieldOf("limit")).forGetter(processor -> processor.limit)).apply((Applicative<CappedStructureProcessor, ?>)instance, CappedStructureProcessor::new));
    private final StructureProcessor delegate;
    private final IntProvider limit;

    public CappedStructureProcessor(StructureProcessor delegate, IntProvider limit) {
        this.delegate = delegate;
        this.limit = limit;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.CAPPED;
    }

    @Override
    public final List<StructureTemplate.StructureBlockInfo> reprocess(ServerWorldAccess world, BlockPos pos, BlockPos pivot, List<StructureTemplate.StructureBlockInfo> originalBlockInfos, List<StructureTemplate.StructureBlockInfo> currentBlockInfos, StructurePlacementData data) {
        if (this.limit.getMax() == 0 || currentBlockInfos.isEmpty()) {
            return currentBlockInfos;
        }
        if (originalBlockInfos.size() != currentBlockInfos.size()) {
            Util.error("Original block info list not in sync with processed list, skipping processing. Original size: " + originalBlockInfos.size() + ", Processed size: " + currentBlockInfos.size());
            return currentBlockInfos;
        }
        Random lv = Random.create(world.toServerWorld().getSeed()).nextSplitter().split(pos);
        int i = Math.min(this.limit.get(lv), currentBlockInfos.size());
        if (i < 1) {
            return currentBlockInfos;
        }
        IntArrayList intArrayList = Util.shuffle(IntStream.range(0, currentBlockInfos.size()), lv);
        IntIterator intIterator = intArrayList.intIterator();
        int j = 0;
        while (intIterator.hasNext() && j < i) {
            StructureTemplate.StructureBlockInfo lv3;
            int k = intIterator.nextInt();
            StructureTemplate.StructureBlockInfo lv2 = originalBlockInfos.get(k);
            StructureTemplate.StructureBlockInfo lv4 = this.delegate.process(world, pos, pivot, lv2, lv3 = currentBlockInfos.get(k), data);
            if (lv4 == null || lv3.equals(lv4)) continue;
            ++j;
            currentBlockInfos.set(k, lv4);
        }
        return currentBlockInfos;
    }
}

