/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.profiler;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.profiler.ProfilerSystem;
import net.minecraft.util.profiler.ReadableProfiler;
import net.minecraft.util.profiler.SampleType;
import net.minecraft.util.profiler.Sampler;

public class SamplerFactory {
    private final Set<String> sampledFullPaths = new ObjectOpenHashSet<String>();

    public Set<Sampler> createSamplers(Supplier<ReadableProfiler> profilerSupplier) {
        Set<Sampler> set = profilerSupplier.get().getSampleTargets().stream().filter(target -> !this.sampledFullPaths.contains(target.getLeft())).map(target -> SamplerFactory.createSampler(profilerSupplier, (String)target.getLeft(), (SampleType)((Object)((Object)target.getRight())))).collect(Collectors.toSet());
        for (Sampler lv : set) {
            this.sampledFullPaths.add(lv.getName());
        }
        return set;
    }

    private static Sampler createSampler(Supplier<ReadableProfiler> profilerSupplier, String id, SampleType type) {
        return Sampler.create(id, type, () -> {
            ProfilerSystem.LocatedInfo lv = ((ReadableProfiler)profilerSupplier.get()).getInfo(id);
            return lv == null ? 0.0 : (double)lv.getMaxTime() / (double)TimeHelper.MILLI_IN_NANOS;
        });
    }
}

