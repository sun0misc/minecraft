package net.minecraft.util.thread;

import com.google.common.collect.Queues;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.Nullable;

public interface TaskQueue {
   @Nullable
   Object poll();

   boolean add(Object message);

   boolean isEmpty();

   int getSize();

   public static final class Prioritized implements TaskQueue {
      private final Queue[] queue;
      private final AtomicInteger queueSize = new AtomicInteger();

      public Prioritized(int priorityCount) {
         this.queue = new Queue[priorityCount];

         for(int j = 0; j < priorityCount; ++j) {
            this.queue[j] = Queues.newConcurrentLinkedQueue();
         }

      }

      @Nullable
      public Runnable poll() {
         Queue[] var1 = this.queue;
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            Queue queue = var1[var3];
            Runnable runnable = (Runnable)queue.poll();
            if (runnable != null) {
               this.queueSize.decrementAndGet();
               return runnable;
            }
         }

         return null;
      }

      public boolean add(PrioritizedTask arg) {
         int i = arg.priority;
         if (i < this.queue.length && i >= 0) {
            this.queue[i].add(arg);
            this.queueSize.incrementAndGet();
            return true;
         } else {
            throw new IndexOutOfBoundsException(String.format(Locale.ROOT, "Priority %d not supported. Expected range [0-%d]", i, this.queue.length - 1));
         }
      }

      public boolean isEmpty() {
         return this.queueSize.get() == 0;
      }

      public int getSize() {
         return this.queueSize.get();
      }

      // $FF: synthetic method
      @Nullable
      public Object poll() {
         return this.poll();
      }
   }

   public static final class PrioritizedTask implements Runnable {
      final int priority;
      private final Runnable runnable;

      public PrioritizedTask(int priority, Runnable runnable) {
         this.priority = priority;
         this.runnable = runnable;
      }

      public void run() {
         this.runnable.run();
      }

      public int getPriority() {
         return this.priority;
      }
   }

   public static final class Simple implements TaskQueue {
      private final Queue queue;

      public Simple(Queue queue) {
         this.queue = queue;
      }

      @Nullable
      public Object poll() {
         return this.queue.poll();
      }

      public boolean add(Object message) {
         return this.queue.add(message);
      }

      public boolean isEmpty() {
         return this.queue.isEmpty();
      }

      public int getSize() {
         return this.queue.size();
      }
   }
}
