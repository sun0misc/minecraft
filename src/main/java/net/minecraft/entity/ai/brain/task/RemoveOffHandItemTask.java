package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.item.Items;

public class RemoveOffHandItemTask {
   public static Task create() {
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryAbsent(MemoryModuleType.ADMIRING_ITEM)).apply(context, (admiringItem) -> {
            return (world, entity, time) -> {
               if (!entity.getOffHandStack().isEmpty() && !entity.getOffHandStack().isOf(Items.SHIELD)) {
                  PiglinBrain.consumeOffHandItem(entity, true);
                  return true;
               } else {
                  return false;
               }
            };
         });
      });
   }
}
