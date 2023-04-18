package net.minecraft.entity.ai.brain.task;

import java.util.Iterator;
import java.util.List;
import net.minecraft.util.collection.WeightedList;

public class Tasks {
   public static SingleTickTask pickRandomly(List weightedTasks) {
      return weighted(weightedTasks, CompositeTask.Order.SHUFFLED, CompositeTask.RunMode.RUN_ONE);
   }

   public static SingleTickTask weighted(List weightedTasks, CompositeTask.Order order, CompositeTask.RunMode runMode) {
      WeightedList lv = new WeightedList();
      weightedTasks.forEach((task) -> {
         lv.add((TaskRunnable)task.getFirst(), (Integer)task.getSecond());
      });
      return TaskTriggerer.task((context) -> {
         return context.point((world, entity, time) -> {
            if (order == CompositeTask.Order.SHUFFLED) {
               lv.shuffle();
            }

            Iterator var7 = lv.iterator();

            while(var7.hasNext()) {
               TaskRunnable lvx = (TaskRunnable)var7.next();
               if (lvx.trigger(world, entity, time) && runMode == CompositeTask.RunMode.RUN_ONE) {
                  break;
               }
            }

            return true;
         });
      });
   }
}
