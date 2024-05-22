/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.test.GameTestException;
import net.minecraft.test.GameTestState;
import net.minecraft.test.TimedTask;

public class TimedTaskRunner {
    final GameTestState test;
    private final List<TimedTask> tasks = Lists.newArrayList();
    private long tick;

    TimedTaskRunner(GameTestState gameTest) {
        this.test = gameTest;
        this.tick = gameTest.getTick();
    }

    public TimedTaskRunner createAndAdd(Runnable task) {
        this.tasks.add(TimedTask.create(task));
        return this;
    }

    public TimedTaskRunner createAndAdd(long duration, Runnable task) {
        this.tasks.add(TimedTask.create(duration, task));
        return this;
    }

    public TimedTaskRunner expectMinDuration(int minDuration) {
        return this.expectMinDurationAndRun(minDuration, () -> {});
    }

    public TimedTaskRunner createAndAddReported(Runnable task) {
        this.tasks.add(TimedTask.create(() -> this.tryRun(task)));
        return this;
    }

    public TimedTaskRunner expectMinDurationAndRun(int minDuration, Runnable task) {
        this.tasks.add(TimedTask.create(() -> {
            if (this.test.getTick() < this.tick + (long)minDuration) {
                throw new GameTestException("Test timed out before sequence completed");
            }
            this.tryRun(task);
        }));
        return this;
    }

    public TimedTaskRunner expectMinDurationOrRun(int minDuration, Runnable task) {
        this.tasks.add(TimedTask.create(() -> {
            if (this.test.getTick() < this.tick + (long)minDuration) {
                this.tryRun(task);
                throw new GameTestException("Test timed out before sequence completed");
            }
        }));
        return this;
    }

    public void completeIfSuccessful() {
        this.tasks.add(TimedTask.create(this.test::completeIfSuccessful));
    }

    public void fail(Supplier<Exception> exceptionSupplier) {
        this.tasks.add(TimedTask.create(() -> this.test.fail((Throwable)exceptionSupplier.get())));
    }

    public Trigger createAndAddTrigger() {
        Trigger lv = new Trigger();
        this.tasks.add(TimedTask.create(() -> lv.trigger(this.test.getTick())));
        return lv;
    }

    public void runSilently(long tick) {
        try {
            this.runTasks(tick);
        } catch (GameTestException gameTestException) {
            // empty catch block
        }
    }

    public void runReported(long tick) {
        try {
            this.runTasks(tick);
        } catch (GameTestException lv) {
            this.test.fail(lv);
        }
    }

    private void tryRun(Runnable task) {
        try {
            task.run();
        } catch (GameTestException lv) {
            this.test.fail(lv);
        }
    }

    private void runTasks(long tick) {
        Iterator<TimedTask> iterator = this.tasks.iterator();
        while (iterator.hasNext()) {
            TimedTask lv = iterator.next();
            lv.task.run();
            iterator.remove();
            long m = tick - this.tick;
            long n = this.tick;
            this.tick = tick;
            if (lv.duration == null || lv.duration == m) continue;
            this.test.fail(new GameTestException("Succeeded in invalid tick: expected " + (n + lv.duration) + ", but current tick is " + tick));
            break;
        }
    }

    public class Trigger {
        private static final long UNTRIGGERED_TICK = -1L;
        private long triggeredTick = -1L;

        void trigger(long tick) {
            if (this.triggeredTick != -1L) {
                throw new IllegalStateException("Condition already triggered at " + this.triggeredTick);
            }
            this.triggeredTick = tick;
        }

        public void checkTrigger() {
            long l = TimedTaskRunner.this.test.getTick();
            if (this.triggeredTick != l) {
                if (this.triggeredTick == -1L) {
                    throw new GameTestException("Condition not triggered (t=" + l + ")");
                }
                throw new GameTestException("Condition triggered at " + this.triggeredTick + ", (t=" + l + ")");
            }
        }
    }
}

