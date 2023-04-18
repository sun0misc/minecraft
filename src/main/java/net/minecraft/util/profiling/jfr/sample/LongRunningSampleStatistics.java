package net.minecraft.util.profiling.jfr.sample;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.util.math.Quantiles;
import org.jetbrains.annotations.Nullable;

public record LongRunningSampleStatistics(LongRunningSample fastestSample, LongRunningSample slowestSample, @Nullable LongRunningSample secondSlowestSample, int count, Map quantiles, Duration totalDuration) {
   public LongRunningSampleStatistics(LongRunningSample arg, LongRunningSample arg2, @Nullable LongRunningSample arg3, int i, Map map, Duration duration) {
      this.fastestSample = arg;
      this.slowestSample = arg2;
      this.secondSlowestSample = arg3;
      this.count = i;
      this.quantiles = map;
      this.totalDuration = duration;
   }

   public static LongRunningSampleStatistics fromSamples(List samples) {
      if (samples.isEmpty()) {
         throw new IllegalArgumentException("No values");
      } else {
         List list2 = samples.stream().sorted(Comparator.comparing(LongRunningSample::duration)).toList();
         Duration duration = (Duration)list2.stream().map(LongRunningSample::duration).reduce(Duration::plus).orElse(Duration.ZERO);
         LongRunningSample lv = (LongRunningSample)list2.get(0);
         LongRunningSample lv2 = (LongRunningSample)list2.get(list2.size() - 1);
         LongRunningSample lv3 = list2.size() > 1 ? (LongRunningSample)list2.get(list2.size() - 2) : null;
         int i = list2.size();
         Map map = Quantiles.create(list2.stream().mapToLong((sample) -> {
            return sample.duration().toNanos();
         }).toArray());
         return new LongRunningSampleStatistics(lv, lv2, lv3, i, map, duration);
      }
   }

   public LongRunningSample fastestSample() {
      return this.fastestSample;
   }

   public LongRunningSample slowestSample() {
      return this.slowestSample;
   }

   @Nullable
   public LongRunningSample secondSlowestSample() {
      return this.secondSlowestSample;
   }

   public int count() {
      return this.count;
   }

   public Map quantiles() {
      return this.quantiles;
   }

   public Duration totalDuration() {
      return this.totalDuration;
   }
}
