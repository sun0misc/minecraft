package net.minecraft.client.resource;

import com.google.common.collect.ImmutableMap;
import com.google.common.math.LongMath;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class PeriodicNotificationManager extends SinglePreparationResourceReloader implements AutoCloseable {
   private static final Codec CODEC;
   private static final Logger LOGGER;
   private final Identifier id;
   private final Object2BooleanFunction countryPredicate;
   @Nullable
   private Timer timer;
   @Nullable
   private NotifyTask task;

   public PeriodicNotificationManager(Identifier id, Object2BooleanFunction countryPredicate) {
      this.id = id;
      this.countryPredicate = countryPredicate;
   }

   protected Map prepare(ResourceManager arg, Profiler arg2) {
      try {
         Reader reader = arg.openAsReader(this.id);

         Map var4;
         try {
            var4 = (Map)CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).result().orElseThrow();
         } catch (Throwable var7) {
            if (reader != null) {
               try {
                  reader.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (reader != null) {
            reader.close();
         }

         return var4;
      } catch (Exception var8) {
         LOGGER.warn("Failed to load {}", this.id, var8);
         return ImmutableMap.of();
      }
   }

   protected void apply(Map map, ResourceManager arg, Profiler arg2) {
      List list = (List)map.entrySet().stream().filter((entry) -> {
         return (Boolean)this.countryPredicate.apply((String)entry.getKey());
      }).map(Map.Entry::getValue).flatMap(Collection::stream).collect(Collectors.toList());
      if (list.isEmpty()) {
         this.cancelTimer();
      } else if (list.stream().anyMatch((entry) -> {
         return entry.period == 0L;
      })) {
         Util.error("A periodic notification in " + this.id + " has a period of zero minutes");
         this.cancelTimer();
      } else {
         long l = this.getMinDelay(list);
         long m = this.getPeriod(list, l);
         if (this.timer == null) {
            this.timer = new Timer();
         }

         if (this.task == null) {
            this.task = new NotifyTask(list, l, m);
         } else {
            this.task = this.task.reload(list, m);
         }

         this.timer.scheduleAtFixedRate(this.task, TimeUnit.MINUTES.toMillis(l), TimeUnit.MINUTES.toMillis(m));
      }
   }

   public void close() {
      this.cancelTimer();
   }

   private void cancelTimer() {
      if (this.timer != null) {
         this.timer.cancel();
      }

   }

   private long getPeriod(List entries, long minDelay) {
      return entries.stream().mapToLong((entry) -> {
         long m = entry.delay - minDelay;
         return LongMath.gcd(m, entry.period);
      }).reduce(LongMath::gcd).orElseThrow(() -> {
         return new IllegalStateException("Empty notifications from: " + this.id);
      });
   }

   private long getMinDelay(List entries) {
      return entries.stream().mapToLong((entry) -> {
         return entry.delay;
      }).min().orElse(0L);
   }

   // $FF: synthetic method
   protected Object prepare(ResourceManager manager, Profiler profiler) {
      return this.prepare(manager, profiler);
   }

   static {
      CODEC = Codec.unboundedMap(Codec.STRING, RecordCodecBuilder.create((instance) -> {
         return instance.group(Codec.LONG.optionalFieldOf("delay", 0L).forGetter(Entry::delay), Codec.LONG.fieldOf("period").forGetter(Entry::period), Codec.STRING.fieldOf("title").forGetter(Entry::title), Codec.STRING.fieldOf("message").forGetter(Entry::message)).apply(instance, Entry::new);
      }).listOf());
      LOGGER = LogUtils.getLogger();
   }

   @Environment(EnvType.CLIENT)
   static class NotifyTask extends TimerTask {
      private final MinecraftClient client = MinecraftClient.getInstance();
      private final List entries;
      private final long periodMs;
      private final AtomicLong delayMs;

      public NotifyTask(List entries, long minDelayMs, long periodMs) {
         this.entries = entries;
         this.periodMs = periodMs;
         this.delayMs = new AtomicLong(minDelayMs);
      }

      public NotifyTask reload(List entries, long period) {
         this.cancel();
         return new NotifyTask(entries, this.delayMs.get(), period);
      }

      public void run() {
         long l = this.delayMs.getAndAdd(this.periodMs);
         long m = this.delayMs.get();
         Iterator var5 = this.entries.iterator();

         while(var5.hasNext()) {
            Entry lv = (Entry)var5.next();
            if (l >= lv.delay) {
               long n = l / lv.period;
               long o = m / lv.period;
               if (n != o) {
                  this.client.execute(() -> {
                     SystemToast.add(MinecraftClient.getInstance().getToastManager(), SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable(lv.title, n), Text.translatable(lv.message, n));
                  });
                  return;
               }
            }
         }

      }
   }

   @Environment(EnvType.CLIENT)
   public static record Entry(long delay, long period, String title, String message) {
      final long delay;
      final long period;
      final String title;
      final String message;

      public Entry(long delay, long period, String title, String message) {
         this.delay = delay != 0L ? delay : period;
         this.period = period;
         this.title = title;
         this.message = message;
      }

      public long delay() {
         return this.delay;
      }

      public long period() {
         return this.period;
      }

      public String title() {
         return this.title;
      }

      public String message() {
         return this.message;
      }
   }
}
