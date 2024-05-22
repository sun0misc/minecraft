/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.profiler;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.SharedConstants;
import net.minecraft.class_9813;
import net.minecraft.util.profiler.ProfileLocationInfo;
import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.ProfilerTiming;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

public class ProfileResultImpl
implements ProfileResult {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ProfileLocationInfo EMPTY_INFO = new ProfileLocationInfo(){

        @Override
        public long getTotalTime() {
            return 0L;
        }

        @Override
        public long getMaxTime() {
            return 0L;
        }

        @Override
        public long getVisitCount() {
            return 0L;
        }

        @Override
        public Object2LongMap<String> getCounts() {
            return Object2LongMaps.emptyMap();
        }
    };
    private static final Splitter SPLITTER = Splitter.on('\u001e');
    private static final Comparator<Map.Entry<String, CounterInfo>> COMPARATOR = Map.Entry.comparingByValue(Comparator.comparingLong(arg -> arg.totalTime)).reversed();
    private final Map<String, ? extends ProfileLocationInfo> locationInfos;
    private final long startTime;
    private final int startTick;
    private final long endTime;
    private final int endTick;
    private final int tickDuration;

    public ProfileResultImpl(Map<String, ? extends ProfileLocationInfo> locationInfos, long startTime, int startTick, long endTime, int endTick) {
        this.locationInfos = locationInfos;
        this.startTime = startTime;
        this.startTick = startTick;
        this.endTime = endTime;
        this.endTick = endTick;
        this.tickDuration = endTick - startTick;
    }

    private ProfileLocationInfo getInfo(String path) {
        ProfileLocationInfo lv = this.locationInfos.get(path);
        return lv != null ? lv : EMPTY_INFO;
    }

    @Override
    public List<ProfilerTiming> getTimings(String parentPath) {
        String string2 = parentPath;
        ProfileLocationInfo lv = this.getInfo("root");
        long l = lv.getTotalTime();
        ProfileLocationInfo lv2 = this.getInfo((String)parentPath);
        long m = lv2.getTotalTime();
        long n = lv2.getVisitCount();
        ArrayList<ProfilerTiming> list = Lists.newArrayList();
        if (!((String)parentPath).isEmpty()) {
            parentPath = (String)parentPath + "\u001e";
        }
        long o = 0L;
        for (String string3 : this.locationInfos.keySet()) {
            if (!ProfileResultImpl.isSubpath((String)parentPath, string3)) continue;
            o += this.getInfo(string3).getTotalTime();
        }
        float f = o;
        if (o < m) {
            o = m;
        }
        if (l < o) {
            l = o;
        }
        for (String string4 : this.locationInfos.keySet()) {
            if (!ProfileResultImpl.isSubpath((String)parentPath, string4)) continue;
            ProfileLocationInfo lv3 = this.getInfo(string4);
            long p = lv3.getTotalTime();
            double d = (double)p * 100.0 / (double)o;
            double e = (double)p * 100.0 / (double)l;
            String string5 = string4.substring(((String)parentPath).length());
            list.add(new ProfilerTiming(string5, d, e, lv3.getVisitCount()));
        }
        if ((float)o > f) {
            list.add(new ProfilerTiming("unspecified", (double)((float)o - f) * 100.0 / (double)o, (double)((float)o - f) * 100.0 / (double)l, n));
        }
        Collections.sort(list);
        list.add(0, new ProfilerTiming(string2, 100.0, (double)o * 100.0 / (double)l, n));
        return list;
    }

    private static boolean isSubpath(String parent, String path) {
        return path.length() > parent.length() && path.startsWith(parent) && path.indexOf(30, parent.length() + 1) < 0;
    }

    private Map<String, CounterInfo> setupCounters() {
        TreeMap<String, CounterInfo> map = Maps.newTreeMap();
        this.locationInfos.forEach((location, info) -> {
            Object2LongMap<String> object2LongMap = info.getCounts();
            if (!object2LongMap.isEmpty()) {
                List<String> list = SPLITTER.splitToList((CharSequence)location);
                object2LongMap.forEach((marker, count) -> map.computeIfAbsent((String)marker, k -> new CounterInfo()).add(list.iterator(), (long)count));
            }
        });
        return map;
    }

    @Override
    public long getStartTime() {
        return this.startTime;
    }

    @Override
    public int getStartTick() {
        return this.startTick;
    }

    @Override
    public long getEndTime() {
        return this.endTime;
    }

    @Override
    public int getEndTick() {
        return this.endTick;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean save(Path path) {
        boolean bl;
        BufferedWriter writer = null;
        try {
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, new OpenOption[0]);
            writer.write(this.asString(this.getTimeSpan(), this.getTickSpan()));
            bl = true;
        } catch (Throwable throwable) {
            boolean bl2;
            try {
                LOGGER.error("Could not save profiler results to {}", (Object)path, (Object)throwable);
                bl2 = false;
            } catch (Throwable throwable2) {
                IOUtils.closeQuietly(writer);
                throw throwable2;
            }
            IOUtils.closeQuietly(writer);
            return bl2;
        }
        IOUtils.closeQuietly(writer);
        return bl;
    }

    protected String asString(long timeSpan, int tickSpan) {
        StringBuilder stringBuilder = new StringBuilder();
        class_9813.MINECRAFT_PROFILER_RESULTS.method_60928(stringBuilder, List.of());
        stringBuilder.append("Version: ").append(SharedConstants.getGameVersion().getId()).append('\n');
        stringBuilder.append("Time span: ").append(timeSpan / 1000000L).append(" ms\n");
        stringBuilder.append("Tick span: ").append(tickSpan).append(" ticks\n");
        stringBuilder.append("// This is approximately ").append(String.format(Locale.ROOT, "%.2f", Float.valueOf((float)tickSpan / ((float)timeSpan / 1.0E9f)))).append(" ticks per second. It should be ").append(20).append(" ticks per second\n\n");
        stringBuilder.append("--- BEGIN PROFILE DUMP ---\n\n");
        this.appendTiming(0, "root", stringBuilder);
        stringBuilder.append("--- END PROFILE DUMP ---\n\n");
        Map<String, CounterInfo> map = this.setupCounters();
        if (!map.isEmpty()) {
            stringBuilder.append("--- BEGIN COUNTER DUMP ---\n\n");
            this.appendCounterDump(map, stringBuilder, tickSpan);
            stringBuilder.append("--- END COUNTER DUMP ---\n\n");
        }
        return stringBuilder.toString();
    }

    @Override
    public String getRootTimings() {
        StringBuilder stringBuilder = new StringBuilder();
        this.appendTiming(0, "root", stringBuilder);
        return stringBuilder.toString();
    }

    private static StringBuilder indent(StringBuilder sb, int size) {
        sb.append(String.format(Locale.ROOT, "[%02d] ", size));
        for (int j = 0; j < size; ++j) {
            sb.append("|   ");
        }
        return sb;
    }

    private void appendTiming(int level, String name, StringBuilder sb) {
        List<ProfilerTiming> list = this.getTimings(name);
        Object2LongMap<String> object2LongMap = ObjectUtils.firstNonNull(this.locationInfos.get(name), EMPTY_INFO).getCounts();
        object2LongMap.forEach((marker, count) -> ProfileResultImpl.indent(sb, level).append('#').append((String)marker).append(' ').append(count).append('/').append(count / (long)this.tickDuration).append('\n'));
        if (list.size() < 3) {
            return;
        }
        for (int j = 1; j < list.size(); ++j) {
            ProfilerTiming lv = list.get(j);
            ProfileResultImpl.indent(sb, level).append(lv.name).append('(').append(lv.visitCount).append('/').append(String.format(Locale.ROOT, "%.0f", Float.valueOf((float)lv.visitCount / (float)this.tickDuration))).append(')').append(" - ").append(String.format(Locale.ROOT, "%.2f", lv.parentSectionUsagePercentage)).append("%/").append(String.format(Locale.ROOT, "%.2f", lv.totalUsagePercentage)).append("%\n");
            if ("unspecified".equals(lv.name)) continue;
            try {
                this.appendTiming(level + 1, name + "\u001e" + lv.name, sb);
                continue;
            } catch (Exception exception) {
                sb.append("[[ EXCEPTION ").append(exception).append(" ]]");
            }
        }
    }

    private void appendCounter(int depth, String name, CounterInfo info, int tickSpan, StringBuilder sb) {
        ProfileResultImpl.indent(sb, depth).append(name).append(" total:").append(info.selfTime).append('/').append(info.totalTime).append(" average: ").append(info.selfTime / (long)tickSpan).append('/').append(info.totalTime / (long)tickSpan).append('\n');
        info.subCounters.entrySet().stream().sorted(COMPARATOR).forEach(entry -> this.appendCounter(depth + 1, (String)entry.getKey(), (CounterInfo)entry.getValue(), tickSpan, sb));
    }

    private void appendCounterDump(Map<String, CounterInfo> counters, StringBuilder sb, int tickSpan) {
        counters.forEach((name, info) -> {
            sb.append("-- Counter: ").append((String)name).append(" --\n");
            this.appendCounter(0, "root", info.subCounters.get("root"), tickSpan, sb);
            sb.append("\n\n");
        });
    }

    @Override
    public int getTickSpan() {
        return this.tickDuration;
    }

    static class CounterInfo {
        long selfTime;
        long totalTime;
        final Map<String, CounterInfo> subCounters = Maps.newHashMap();

        CounterInfo() {
        }

        public void add(Iterator<String> pathIterator, long time) {
            this.totalTime += time;
            if (!pathIterator.hasNext()) {
                this.selfTime += time;
            } else {
                this.subCounters.computeIfAbsent(pathIterator.next(), k -> new CounterInfo()).add(pathIterator, time);
            }
        }
    }
}

