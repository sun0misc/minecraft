/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.profiling.jfr.event;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.EventType;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.Period;
import jdk.jfr.StackTrace;
import jdk.jfr.Timespan;
import net.minecraft.obfuscate.DontObfuscate;

@Name(value="minecraft.ServerTickTime")
@Label(value="Server Tick Time")
@Category(value={"Minecraft", "Ticking"})
@StackTrace(value=false)
@Period(value="1 s")
@DontObfuscate
public class ServerTickTimeEvent
extends Event {
    public static final String EVENT_NAME = "minecraft.ServerTickTime";
    public static final EventType TYPE = EventType.getEventType(ServerTickTimeEvent.class);
    @Name(value="averageTickDuration")
    @Label(value="Average Server Tick Duration")
    @Timespan
    public final long averageTickDurationNanos;

    public ServerTickTimeEvent(float averageTickMilliseconds) {
        this.averageTickDurationNanos = (long)(1000000.0f * averageTickMilliseconds);
    }

    public static class Names {
        public static final String AVERAGE_TICK_DURATION = "averageTickDuration";

        private Names() {
        }
    }
}

