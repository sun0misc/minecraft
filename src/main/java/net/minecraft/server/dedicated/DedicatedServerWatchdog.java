/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated;

import com.google.common.collect.Streams;
import com.mojang.logging.LogUtils;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import net.minecraft.Bootstrap;
import net.minecraft.class_9813;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;

public class DedicatedServerWatchdog
implements Runnable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long field_29664 = 10000L;
    private static final int field_29665 = 1;
    private final MinecraftDedicatedServer server;
    private final long maxTickTime;

    public DedicatedServerWatchdog(MinecraftDedicatedServer server) {
        this.server = server;
        this.maxTickTime = server.getMaxTickTime() * TimeHelper.MILLI_IN_NANOS;
    }

    @Override
    public void run() {
        while (this.server.isRunning()) {
            long l = this.server.getTimeReference();
            long m = Util.getMeasuringTimeNano();
            long n = m - l;
            if (n > this.maxTickTime) {
                LOGGER.error(LogUtils.FATAL_MARKER, "A single server tick took {} seconds (should be max {})", (Object)String.format(Locale.ROOT, "%.2f", Float.valueOf((float)n / (float)TimeHelper.SECOND_IN_NANOS)), (Object)String.format(Locale.ROOT, "%.2f", Float.valueOf(this.server.getTickManager().getMillisPerTick() / (float)TimeHelper.SECOND_IN_MILLIS)));
                LOGGER.error(LogUtils.FATAL_MARKER, "Considering it to be crashed, server will forcibly shutdown.");
                ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
                ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
                StringBuilder stringBuilder = new StringBuilder();
                Error error = new Error("Watchdog");
                for (ThreadInfo threadInfo : threadInfos) {
                    if (threadInfo.getThreadId() == this.server.getThread().getId()) {
                        error.setStackTrace(threadInfo.getStackTrace());
                    }
                    stringBuilder.append(threadInfo);
                    stringBuilder.append("\n");
                }
                CrashReport lv = new CrashReport("Watching Server", error);
                this.server.addSystemDetails(lv.getSystemDetailsSection());
                CrashReportSection lv2 = lv.addElement("Thread Dump");
                lv2.add("Threads", stringBuilder);
                CrashReportSection lv3 = lv.addElement("Performance stats");
                lv3.add("Random tick rate", () -> this.server.getSaveProperties().getGameRules().get(GameRules.RANDOM_TICK_SPEED).toString());
                lv3.add("Level stats", () -> Streams.stream(this.server.getWorlds()).map(world -> String.valueOf(world.getRegistryKey()) + ": " + world.getDebugString()).collect(Collectors.joining(",\n")));
                Bootstrap.println("Crash report:\n" + lv.method_60920(class_9813.MINECRAFT_CRASH_REPORT));
                Path path = this.server.getRunDirectory().resolve("crash-reports").resolve("crash-" + Util.getFormattedCurrentTime() + "-server.txt");
                if (lv.method_60919(path, class_9813.MINECRAFT_CRASH_REPORT)) {
                    LOGGER.error("This crash report has been saved to: {}", (Object)path.toAbsolutePath());
                } else {
                    LOGGER.error("We were unable to save this crash report to disk.");
                }
                this.shutdown();
            }
            try {
                Thread.sleep((l + this.maxTickTime - m) / TimeHelper.MILLI_IN_NANOS);
            } catch (InterruptedException interruptedException) {}
        }
    }

    private void shutdown() {
        try {
            Timer timer = new Timer();
            timer.schedule(new TimerTask(this){

                @Override
                public void run() {
                    Runtime.getRuntime().halt(1);
                }
            }, 10000L);
            System.exit(1);
        } catch (Throwable throwable) {
            Runtime.getRuntime().halt(1);
        }
    }
}

