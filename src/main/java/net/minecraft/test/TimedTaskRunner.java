package net.minecraft.test;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class TimedTaskRunner {
   final GameTestState test;
   private final List tasks = Lists.newArrayList();
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
      return this.expectMinDurationAndRun(minDuration, () -> {
      });
   }

   public TimedTaskRunner createAndAddReported(Runnable task) {
      this.tasks.add(TimedTask.create(() -> {
         this.tryRun(task);
      }));
      return this;
   }

   public TimedTaskRunner expectMinDurationAndRun(int minDuration, Runnable task) {
      this.tasks.add(TimedTask.create(() -> {
         if (this.test.getTick() < this.tick + (long)minDuration) {
            throw new GameTestException("Waiting");
         } else {
            this.tryRun(task);
         }
      }));
      return this;
   }

   public TimedTaskRunner expectMinDurationOrRun(int minDuration, Runnable task) {
      this.tasks.add(TimedTask.create(() -> {
         if (this.test.getTick() < this.tick + (long)minDuration) {
            this.tryRun(task);
            throw new GameTestException("Waiting");
         }
      }));
      return this;
   }

   public void completeIfSuccessful() {
      List var10000 = this.tasks;
      GameTestState var10001 = this.test;
      Objects.requireNonNull(var10001);
      var10000.add(TimedTask.create(var10001::completeIfSuccessful));
   }

   public void fail(Supplier exceptionSupplier) {
      this.tasks.add(TimedTask.create(() -> {
         this.test.fail((Throwable)exceptionSupplier.get());
      }));
   }

   public Trigger createAndAddTrigger() {
      Trigger lv = new Trigger();
      this.tasks.add(TimedTask.create(() -> {
         lv.trigger(this.test.getTick());
      }));
      return lv;
   }

   public void runSilently(long tick) {
      try {
         this.runTasks(tick);
      } catch (GameTestException var4) {
      }

   }

   public void runReported(long tick) {
      try {
         this.runTasks(tick);
      } catch (GameTestException var4) {
         this.test.fail(var4);
      }

   }

   private void tryRun(Runnable task) {
      try {
         task.run();
      } catch (GameTestException var3) {
         this.test.fail(var3);
      }

   }

   private void runTasks(long tick) {
      Iterator iterator = this.tasks.iterator();

      while(iterator.hasNext()) {
         TimedTask lv = (TimedTask)iterator.next();
         lv.task.run();
         iterator.remove();
         long m = tick - this.tick;
         long n = this.tick;
         this.tick = tick;
         if (lv.duration != null && lv.duration != m) {
            GameTestState var10000 = this.test;
            long var10003 = n + lv.duration;
            var10000.fail(new GameTestException("Succeeded in invalid tick: expected " + var10003 + ", but current tick is " + tick));
            break;
         }
      }

   }

   public class Trigger {
      private static final long UNTRIGGERED_TICK = -1L;
      private long triggeredTick = -1L;

      void trigger(long tick) {
         if (this.triggeredTick != -1L) {
            throw new IllegalStateException("Condition already triggered at " + this.triggeredTick);
         } else {
            this.triggeredTick = tick;
         }
      }

      public void checkTrigger() {
         long l = TimedTaskRunner.this.test.getTick();
         if (this.triggeredTick != l) {
            if (this.triggeredTick == -1L) {
               throw new GameTestException("Condition not triggered (t=" + l + ")");
            } else {
               throw new GameTestException("Condition triggered at " + this.triggeredTick + ", (t=" + l + ")");
            }
         }
      }
   }
}
