package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.village.raid.Raid;

public class StartRaidTask {
   public static Task create() {
      return TaskTriggerer.task((context) -> {
         return context.point((world, entity, time) -> {
            if (world.random.nextInt(20) != 0) {
               return false;
            } else {
               Brain lv = entity.getBrain();
               Raid lv2 = world.getRaidAt(entity.getBlockPos());
               if (lv2 != null) {
                  if (lv2.hasSpawned() && !lv2.isPreRaid()) {
                     lv.setDefaultActivity(Activity.RAID);
                     lv.doExclusively(Activity.RAID);
                  } else {
                     lv.setDefaultActivity(Activity.PRE_RAID);
                     lv.doExclusively(Activity.PRE_RAID);
                  }
               }

               return true;
            }
         });
      });
   }
}
