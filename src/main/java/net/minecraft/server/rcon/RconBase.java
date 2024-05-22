/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.rcon;

import com.mojang.logging.LogUtils;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.util.logging.UncaughtExceptionHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class RconBase
implements Runnable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(0);
    private static final int field_29794 = 5;
    protected volatile boolean running;
    protected final String description;
    @Nullable
    protected Thread thread;

    protected RconBase(String description) {
        this.description = description;
    }

    public synchronized boolean start() {
        if (this.running) {
            return true;
        }
        this.running = true;
        this.thread = new Thread((Runnable)this, this.description + " #" + THREAD_COUNTER.incrementAndGet());
        this.thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler(LOGGER));
        this.thread.start();
        LOGGER.info("Thread {} started", (Object)this.description);
        return true;
    }

    public synchronized void stop() {
        this.running = false;
        if (null == this.thread) {
            return;
        }
        int i = 0;
        while (this.thread.isAlive()) {
            try {
                this.thread.join(1000L);
                if (++i >= 5) {
                    LOGGER.warn("Waited {} seconds attempting force stop!", (Object)i);
                    continue;
                }
                if (!this.thread.isAlive()) continue;
                LOGGER.warn("Thread {} ({}) failed to exit after {} second(s)", new Object[]{this, this.thread.getState(), i, new Exception("Stack:")});
                this.thread.interrupt();
            } catch (InterruptedException interruptedException) {}
        }
        LOGGER.info("Thread {} stopped", (Object)this.description);
        this.thread = null;
    }

    public boolean isRunning() {
        return this.running;
    }
}

