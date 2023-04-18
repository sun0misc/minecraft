package net.minecraft.util.profiler;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.util.TimeHelper;

public class SamplerFactory {
   private final Set sampledFullPaths = new ObjectOpenHashSet();

   public Set createSamplers(Supplier profilerSupplier) {
      Set set = (Set)((ReadableProfiler)profilerSupplier.get()).getSampleTargets().stream().filter((target) -> {
         return !this.sampledFullPaths.contains(target.getLeft());
      }).map((target) -> {
         return createSampler(profilerSupplier, (String)target.getLeft(), (SampleType)target.getRight());
      }).collect(Collectors.toSet());
      Iterator var3 = set.iterator();

      while(var3.hasNext()) {
         Sampler lv = (Sampler)var3.next();
         this.sampledFullPaths.add(lv.getName());
      }

      return set;
   }

   private static Sampler createSampler(Supplier profilerSupplier, String id, SampleType type) {
      return Sampler.create(id, type, () -> {
         ProfilerSystem.LocatedInfo lv = ((ReadableProfiler)profilerSupplier.get()).getInfo(id);
         return lv == null ? 0.0 : (double)lv.getMaxTime() / (double)TimeHelper.MILLI_IN_NANOS;
      });
   }
}
