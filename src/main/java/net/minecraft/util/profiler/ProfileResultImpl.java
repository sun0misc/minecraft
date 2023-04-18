package net.minecraft.util.profiler;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.SharedConstants;
import net.minecraft.util.Util;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

public class ProfileResultImpl implements ProfileResult {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ProfileLocationInfo EMPTY_INFO = new ProfileLocationInfo() {
      public long getTotalTime() {
         return 0L;
      }

      public long getMaxTime() {
         return 0L;
      }

      public long getVisitCount() {
         return 0L;
      }

      public Object2LongMap getCounts() {
         return Object2LongMaps.emptyMap();
      }
   };
   private static final Splitter SPLITTER = Splitter.on('\u001e');
   private static final Comparator COMPARATOR = Entry.comparingByValue(Comparator.comparingLong((arg) -> {
      return arg.totalTime;
   })).reversed();
   private final Map locationInfos;
   private final long startTime;
   private final int startTick;
   private final long endTime;
   private final int endTick;
   private final int tickDuration;

   public ProfileResultImpl(Map locationInfos, long startTime, int startTick, long endTime, int endTick) {
      this.locationInfos = locationInfos;
      this.startTime = startTime;
      this.startTick = startTick;
      this.endTime = endTime;
      this.endTick = endTick;
      this.tickDuration = endTick - startTick;
   }

   private ProfileLocationInfo getInfo(String path) {
      ProfileLocationInfo lv = (ProfileLocationInfo)this.locationInfos.get(path);
      return lv != null ? lv : EMPTY_INFO;
   }

   public List getTimings(String parentPath) {
      ProfileLocationInfo lv = this.getInfo("root");
      long l = lv.getTotalTime();
      ProfileLocationInfo lv2 = this.getInfo(parentPath);
      long m = lv2.getTotalTime();
      long n = lv2.getVisitCount();
      List list = Lists.newArrayList();
      if (!parentPath.isEmpty()) {
         parentPath = parentPath + "\u001e";
      }

      long o = 0L;
      Iterator var14 = this.locationInfos.keySet().iterator();

      while(var14.hasNext()) {
         String string3 = (String)var14.next();
         if (isSubpath(parentPath, string3)) {
            o += this.getInfo(string3).getTotalTime();
         }
      }

      float f = (float)o;
      if (o < m) {
         o = m;
      }

      if (l < o) {
         l = o;
      }

      Iterator var26 = this.locationInfos.keySet().iterator();

      while(var26.hasNext()) {
         String string4 = (String)var26.next();
         if (isSubpath(parentPath, string4)) {
            ProfileLocationInfo lv3 = this.getInfo(string4);
            long p = lv3.getTotalTime();
            double d = (double)p * 100.0 / (double)o;
            double e = (double)p * 100.0 / (double)l;
            String string5 = string4.substring(parentPath.length());
            list.add(new ProfilerTiming(string5, d, e, lv3.getVisitCount()));
         }
      }

      if ((float)o > f) {
         list.add(new ProfilerTiming("unspecified", (double)((float)o - f) * 100.0 / (double)o, (double)((float)o - f) * 100.0 / (double)l, n));
      }

      Collections.sort(list);
      list.add(0, new ProfilerTiming(parentPath, 100.0, (double)o * 100.0 / (double)l, n));
      return list;
   }

   private static boolean isSubpath(String parent, String path) {
      return path.length() > parent.length() && path.startsWith(parent) && path.indexOf(30, parent.length() + 1) < 0;
   }

   private Map setupCounters() {
      Map map = Maps.newTreeMap();
      this.locationInfos.forEach((location, info) -> {
         Object2LongMap object2LongMap = info.getCounts();
         if (!object2LongMap.isEmpty()) {
            List list = SPLITTER.splitToList(location);
            object2LongMap.forEach((marker, count) -> {
               ((CounterInfo)map.computeIfAbsent(marker, (k) -> {
                  return new CounterInfo();
               })).add(list.iterator(), count);
            });
         }

      });
      return map;
   }

   public long getStartTime() {
      return this.startTime;
   }

   public int getStartTick() {
      return this.startTick;
   }

   public long getEndTime() {
      return this.endTime;
   }

   public int getEndTick() {
      return this.endTick;
   }

   public boolean save(Path path) {
      Writer writer = null;

      boolean var4;
      try {
         Files.createDirectories(path.getParent());
         writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
         writer.write(this.asString(this.getTimeSpan(), this.getTickSpan()));
         boolean var3 = true;
         return var3;
      } catch (Throwable var8) {
         LOGGER.error("Could not save profiler results to {}", path, var8);
         var4 = false;
      } finally {
         IOUtils.closeQuietly(writer);
      }

      return var4;
   }

   protected String asString(long timeSpan, int tickSpan) {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("---- Minecraft Profiler Results ----\n");
      stringBuilder.append("// ");
      stringBuilder.append(generateWittyComment());
      stringBuilder.append("\n\n");
      stringBuilder.append("Version: ").append(SharedConstants.getGameVersion().getId()).append('\n');
      stringBuilder.append("Time span: ").append(timeSpan / 1000000L).append(" ms\n");
      stringBuilder.append("Tick span: ").append(tickSpan).append(" ticks\n");
      stringBuilder.append("// This is approximately ").append(String.format(Locale.ROOT, "%.2f", (float)tickSpan / ((float)timeSpan / 1.0E9F))).append(" ticks per second. It should be ").append(20).append(" ticks per second\n\n");
      stringBuilder.append("--- BEGIN PROFILE DUMP ---\n\n");
      this.appendTiming(0, "root", stringBuilder);
      stringBuilder.append("--- END PROFILE DUMP ---\n\n");
      Map map = this.setupCounters();
      if (!map.isEmpty()) {
         stringBuilder.append("--- BEGIN COUNTER DUMP ---\n\n");
         this.appendCounterDump(map, stringBuilder, tickSpan);
         stringBuilder.append("--- END COUNTER DUMP ---\n\n");
      }

      return stringBuilder.toString();
   }

   public String getRootTimings() {
      StringBuilder stringBuilder = new StringBuilder();
      this.appendTiming(0, "root", stringBuilder);
      return stringBuilder.toString();
   }

   private static StringBuilder indent(StringBuilder sb, int size) {
      sb.append(String.format(Locale.ROOT, "[%02d] ", size));

      for(int j = 0; j < size; ++j) {
         sb.append("|   ");
      }

      return sb;
   }

   private void appendTiming(int level, String name, StringBuilder sb) {
      List list = this.getTimings(name);
      Object2LongMap object2LongMap = ((ProfileLocationInfo)ObjectUtils.firstNonNull(new ProfileLocationInfo[]{(ProfileLocationInfo)this.locationInfos.get(name), EMPTY_INFO})).getCounts();
      object2LongMap.forEach((marker, count) -> {
         indent(sb, level).append('#').append(marker).append(' ').append(count).append('/').append(count / (long)this.tickDuration).append('\n');
      });
      if (list.size() >= 3) {
         for(int j = 1; j < list.size(); ++j) {
            ProfilerTiming lv = (ProfilerTiming)list.get(j);
            indent(sb, level).append(lv.name).append('(').append(lv.visitCount).append('/').append(String.format(Locale.ROOT, "%.0f", (float)lv.visitCount / (float)this.tickDuration)).append(')').append(" - ").append(String.format(Locale.ROOT, "%.2f", lv.parentSectionUsagePercentage)).append("%/").append(String.format(Locale.ROOT, "%.2f", lv.totalUsagePercentage)).append("%\n");
            if (!"unspecified".equals(lv.name)) {
               try {
                  this.appendTiming(level + 1, name + "\u001e" + lv.name, sb);
               } catch (Exception var9) {
                  sb.append("[[ EXCEPTION ").append(var9).append(" ]]");
               }
            }
         }

      }
   }

   private void appendCounter(int depth, String name, CounterInfo info, int tickSpan, StringBuilder sb) {
      indent(sb, depth).append(name).append(" total:").append(info.selfTime).append('/').append(info.totalTime).append(" average: ").append(info.selfTime / (long)tickSpan).append('/').append(info.totalTime / (long)tickSpan).append('\n');
      info.subCounters.entrySet().stream().sorted(COMPARATOR).forEach((entry) -> {
         this.appendCounter(depth + 1, (String)entry.getKey(), (CounterInfo)entry.getValue(), tickSpan, sb);
      });
   }

   private void appendCounterDump(Map counters, StringBuilder sb, int tickSpan) {
      counters.forEach((name, info) -> {
         sb.append("-- Counter: ").append(name).append(" --\n");
         this.appendCounter(0, "root", (CounterInfo)info.subCounters.get("root"), tickSpan, sb);
         sb.append("\n\n");
      });
   }

   private static String generateWittyComment() {
      String[] strings = new String[]{"I'd Rather Be Surfing", "Shiny numbers!", "Am I not running fast enough? :(", "I'm working as hard as I can!", "Will I ever be good enough for you? :(", "Speedy. Zoooooom!", "Hello world", "40% better than a crash report.", "Now with extra numbers", "Now with less numbers", "Now with the same numbers", "You should add flames to things, it makes them go faster!", "Do you feel the need for... optimization?", "*cracks redstone whip*", "Maybe if you treated it better then it'll have more motivation to work faster! Poor server."};

      try {
         return strings[(int)(Util.getMeasuringTimeNano() % (long)strings.length)];
      } catch (Throwable var2) {
         return "Witty comment unavailable :(";
      }
   }

   public int getTickSpan() {
      return this.tickDuration;
   }

   static class CounterInfo {
      long selfTime;
      long totalTime;
      final Map subCounters = Maps.newHashMap();

      public void add(Iterator pathIterator, long time) {
         this.totalTime += time;
         if (!pathIterator.hasNext()) {
            this.selfTime += time;
         } else {
            ((CounterInfo)this.subCounters.computeIfAbsent((String)pathIterator.next(), (k) -> {
               return new CounterInfo();
            })).add(pathIterator, time);
         }

      }
   }
}
