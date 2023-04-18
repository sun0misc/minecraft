package net.minecraft.server.world;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.MessageListener;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.util.thread.TaskQueue;
import org.slf4j.Logger;

public class ChunkTaskPrioritySystem implements ChunkHolder.LevelUpdateListener, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Map queues;
   private final Set idleActors;
   private final TaskExecutor controlActor;

   public ChunkTaskPrioritySystem(List actors, Executor executor, int maxQueues) {
      this.queues = (Map)actors.stream().collect(Collectors.toMap(Function.identity(), (actor) -> {
         return new LevelPrioritizedQueue(actor.getName() + "_queue", maxQueues);
      }));
      this.idleActors = Sets.newHashSet(actors);
      this.controlActor = new TaskExecutor(new TaskQueue.Prioritized(4), executor, "sorter");
   }

   public boolean shouldDelayShutdown() {
      return this.controlActor.hasQueuedTasks() || this.queues.values().stream().anyMatch(LevelPrioritizedQueue::hasQueuedElement);
   }

   public static Task createTask(Function taskFunction, long pos, IntSupplier lastLevelUpdatedToProvider) {
      return new Task(taskFunction, pos, lastLevelUpdatedToProvider);
   }

   public static Task createMessage(Runnable task, long pos, IntSupplier lastLevelUpdatedToProvider) {
      return new Task((yield) -> {
         return () -> {
            task.run();
            yield.send(Unit.INSTANCE);
         };
      }, pos, lastLevelUpdatedToProvider);
   }

   public static Task createMessage(ChunkHolder holder, Runnable task) {
      long var10001 = holder.getPos().toLong();
      Objects.requireNonNull(holder);
      return createMessage(task, var10001, holder::getCompletedLevel);
   }

   public static Task createTask(ChunkHolder holder, Function taskFunction) {
      long var10001 = holder.getPos().toLong();
      Objects.requireNonNull(holder);
      return createTask(taskFunction, var10001, holder::getCompletedLevel);
   }

   public static UnblockingMessage createUnblockingMessage(Runnable task, long pos, boolean removeTask) {
      return new UnblockingMessage(task, pos, removeTask);
   }

   public MessageListener createExecutor(MessageListener executor, boolean addBlocker) {
      return (MessageListener)this.controlActor.ask((yield) -> {
         return new TaskQueue.PrioritizedTask(0, () -> {
            this.getQueue(executor);
            yield.send(MessageListener.create("chunk priority sorter around " + executor.getName(), (task) -> {
               this.enqueueChunk(executor, task.taskFunction, task.pos, task.lastLevelUpdatedToProvider, addBlocker);
            }));
         });
      }).join();
   }

   public MessageListener createUnblockingExecutor(MessageListener executor) {
      return (MessageListener)this.controlActor.ask((yield) -> {
         return new TaskQueue.PrioritizedTask(0, () -> {
            yield.send(MessageListener.create("chunk priority sorter around " + executor.getName(), (message) -> {
               this.removeChunk(executor, message.pos, message.callback, message.removeTask);
            }));
         });
      }).join();
   }

   public void updateLevel(ChunkPos pos, IntSupplier levelGetter, int targetLevel, IntConsumer levelSetter) {
      this.controlActor.send(new TaskQueue.PrioritizedTask(0, () -> {
         int j = levelGetter.getAsInt();
         this.queues.values().forEach((queue) -> {
            queue.updateLevel(j, pos, targetLevel);
         });
         levelSetter.accept(targetLevel);
      }));
   }

   private void removeChunk(MessageListener actor, long chunkPos, Runnable callback, boolean clearTask) {
      this.controlActor.send(new TaskQueue.PrioritizedTask(1, () -> {
         LevelPrioritizedQueue lv = this.getQueue(actor);
         lv.remove(chunkPos, clearTask);
         if (this.idleActors.remove(actor)) {
            this.enqueueExecution(lv, actor);
         }

         callback.run();
      }));
   }

   private void enqueueChunk(MessageListener actor, Function task, long chunkPos, IntSupplier lastLevelUpdatedToProvider, boolean addBlocker) {
      this.controlActor.send(new TaskQueue.PrioritizedTask(2, () -> {
         LevelPrioritizedQueue lv = this.getQueue(actor);
         int i = lastLevelUpdatedToProvider.getAsInt();
         lv.add(Optional.of(task), chunkPos, i);
         if (addBlocker) {
            lv.add(Optional.empty(), chunkPos, i);
         }

         if (this.idleActors.remove(actor)) {
            this.enqueueExecution(lv, actor);
         }

      }));
   }

   private void enqueueExecution(LevelPrioritizedQueue queue, MessageListener actor) {
      this.controlActor.send(new TaskQueue.PrioritizedTask(3, () -> {
         Stream stream = queue.poll();
         if (stream == null) {
            this.idleActors.add(actor);
         } else {
            CompletableFuture.allOf((CompletableFuture[])stream.map((executeOrAddBlocking) -> {
               Objects.requireNonNull(actor);
               return (CompletableFuture)executeOrAddBlocking.map(actor::ask, (addBlocking) -> {
                  addBlocking.run();
                  return CompletableFuture.completedFuture(Unit.INSTANCE);
               });
            }).toArray((i) -> {
               return new CompletableFuture[i];
            })).thenAccept((void_) -> {
               this.enqueueExecution(queue, actor);
            });
         }

      }));
   }

   private LevelPrioritizedQueue getQueue(MessageListener actor) {
      LevelPrioritizedQueue lv = (LevelPrioritizedQueue)this.queues.get(actor);
      if (lv == null) {
         throw (IllegalArgumentException)Util.throwOrPause(new IllegalArgumentException("No queue for: " + actor));
      } else {
         return lv;
      }
   }

   @VisibleForTesting
   public String getDebugString() {
      String var10000 = (String)this.queues.entrySet().stream().map((entry) -> {
         String var10000 = ((MessageListener)entry.getKey()).getName();
         return var10000 + "=[" + (String)((LevelPrioritizedQueue)entry.getValue()).getBlockingChunks().stream().map((pos) -> {
            return "" + pos + ":" + new ChunkPos(pos);
         }).collect(Collectors.joining(",")) + "]";
      }).collect(Collectors.joining(","));
      return var10000 + ", s=" + this.idleActors.size();
   }

   public void close() {
      this.queues.keySet().forEach(MessageListener::close);
   }

   public static final class Task {
      final Function taskFunction;
      final long pos;
      final IntSupplier lastLevelUpdatedToProvider;

      Task(Function taskFunction, long pos, IntSupplier lastLevelUpdatedToProvider) {
         this.taskFunction = taskFunction;
         this.pos = pos;
         this.lastLevelUpdatedToProvider = lastLevelUpdatedToProvider;
      }
   }

   public static final class UnblockingMessage {
      final Runnable callback;
      final long pos;
      final boolean removeTask;

      UnblockingMessage(Runnable callback, long pos, boolean removeTask) {
         this.callback = callback;
         this.pos = pos;
         this.removeTask = removeTask;
      }
   }
}
