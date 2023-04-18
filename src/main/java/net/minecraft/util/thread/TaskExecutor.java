package net.minecraft.util.thread;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2BooleanFunction;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.SampleType;
import net.minecraft.util.profiler.Sampler;
import org.slf4j.Logger;

public class TaskExecutor implements SampleableExecutor, MessageListener, AutoCloseable, Runnable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int field_29940 = 1;
   private static final int field_29941 = 2;
   private final AtomicInteger stateFlags = new AtomicInteger(0);
   private final TaskQueue queue;
   private final Executor executor;
   private final String name;

   public static TaskExecutor create(Executor executor, String name) {
      return new TaskExecutor(new TaskQueue.Simple(new ConcurrentLinkedQueue()), executor, name);
   }

   public TaskExecutor(TaskQueue queue, Executor executor, String name) {
      this.executor = executor;
      this.queue = queue;
      this.name = name;
      ExecutorSampling.INSTANCE.add(this);
   }

   private boolean unpause() {
      int i;
      do {
         i = this.stateFlags.get();
         if ((i & 3) != 0) {
            return false;
         }
      } while(!this.stateFlags.compareAndSet(i, i | 2));

      return true;
   }

   private void pause() {
      int i;
      do {
         i = this.stateFlags.get();
      } while(!this.stateFlags.compareAndSet(i, i & -3));

   }

   private boolean hasMessages() {
      if ((this.stateFlags.get() & 1) != 0) {
         return false;
      } else {
         return !this.queue.isEmpty();
      }
   }

   public void close() {
      int i;
      do {
         i = this.stateFlags.get();
      } while(!this.stateFlags.compareAndSet(i, i | 1));

   }

   private boolean isUnpaused() {
      return (this.stateFlags.get() & 2) != 0;
   }

   private boolean runNext() {
      if (!this.isUnpaused()) {
         return false;
      } else {
         Runnable runnable = (Runnable)this.queue.poll();
         if (runnable == null) {
            return false;
         } else {
            Util.debugRunnable(this.name, runnable).run();
            return true;
         }
      }
   }

   public void run() {
      try {
         this.runWhile((runCount) -> {
            return runCount == 0;
         });
      } finally {
         this.pause();
         this.execute();
      }

   }

   public void awaitAll() {
      try {
         this.runWhile((runCount) -> {
            return true;
         });
      } finally {
         this.pause();
         this.execute();
      }

   }

   public void send(Object message) {
      this.queue.add(message);
      this.execute();
   }

   private void execute() {
      if (this.hasMessages() && this.unpause()) {
         try {
            this.executor.execute(this);
         } catch (RejectedExecutionException var4) {
            try {
               this.executor.execute(this);
            } catch (RejectedExecutionException var3) {
               LOGGER.error("Cound not schedule mailbox", var3);
            }
         }
      }

   }

   private int runWhile(Int2BooleanFunction condition) {
      int i;
      for(i = 0; condition.get(i) && this.runNext(); ++i) {
      }

      return i;
   }

   public int getQueueSize() {
      return this.queue.getSize();
   }

   public boolean hasQueuedTasks() {
      return this.isUnpaused() && !this.queue.isEmpty();
   }

   public String toString() {
      String var10000 = this.name;
      return var10000 + " " + this.stateFlags.get() + " " + this.queue.isEmpty();
   }

   public String getName() {
      return this.name;
   }

   public List createSamplers() {
      return ImmutableList.of(Sampler.create(this.name + "-queue-size", SampleType.MAIL_BOXES, this::getQueueSize));
   }
}
