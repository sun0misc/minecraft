package net.minecraft.entity.ai.goal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

public class GoalSelector {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final PrioritizedGoal REPLACEABLE_GOAL = new PrioritizedGoal(Integer.MAX_VALUE, new Goal() {
      public boolean canStart() {
         return false;
      }
   }) {
      public boolean isRunning() {
         return false;
      }
   };
   private final Map goalsByControl = new EnumMap(Goal.Control.class);
   private final Set goals = Sets.newLinkedHashSet();
   private final Supplier profiler;
   private final EnumSet disabledControls = EnumSet.noneOf(Goal.Control.class);
   private int field_30212;
   private int timeInterval = 3;

   public GoalSelector(Supplier profiler) {
      this.profiler = profiler;
   }

   public void add(int priority, Goal goal) {
      this.goals.add(new PrioritizedGoal(priority, goal));
   }

   @VisibleForTesting
   public void clear(Predicate predicate) {
      this.goals.removeIf((goal) -> {
         return predicate.test(goal.getGoal());
      });
   }

   public void remove(Goal goal) {
      this.goals.stream().filter((arg2) -> {
         return arg2.getGoal() == goal;
      }).filter(PrioritizedGoal::isRunning).forEach(PrioritizedGoal::stop);
      this.goals.removeIf((arg2) -> {
         return arg2.getGoal() == goal;
      });
   }

   private static boolean usesAny(PrioritizedGoal goal, EnumSet controls) {
      Iterator var2 = goal.getControls().iterator();

      Goal.Control lv;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         lv = (Goal.Control)var2.next();
      } while(!controls.contains(lv));

      return true;
   }

   private static boolean canReplaceAll(PrioritizedGoal goal, Map goalsByControl) {
      Iterator var2 = goal.getControls().iterator();

      Goal.Control lv;
      do {
         if (!var2.hasNext()) {
            return true;
         }

         lv = (Goal.Control)var2.next();
      } while(((PrioritizedGoal)goalsByControl.getOrDefault(lv, REPLACEABLE_GOAL)).canBeReplacedBy(goal));

      return false;
   }

   public void tick() {
      Profiler lv = (Profiler)this.profiler.get();
      lv.push("goalCleanup");
      Iterator iterator = this.goals.iterator();

      while(true) {
         PrioritizedGoal lv2;
         do {
            do {
               if (!iterator.hasNext()) {
                  iterator = this.goalsByControl.entrySet().iterator();

                  while(iterator.hasNext()) {
                     Map.Entry entry = (Map.Entry)iterator.next();
                     if (!((PrioritizedGoal)entry.getValue()).isRunning()) {
                        iterator.remove();
                     }
                  }

                  lv.pop();
                  lv.push("goalUpdate");
                  iterator = this.goals.iterator();

                  while(true) {
                     do {
                        do {
                           do {
                              do {
                                 if (!iterator.hasNext()) {
                                    lv.pop();
                                    this.tickGoals(true);
                                    return;
                                 }

                                 lv2 = (PrioritizedGoal)iterator.next();
                              } while(lv2.isRunning());
                           } while(usesAny(lv2, this.disabledControls));
                        } while(!canReplaceAll(lv2, this.goalsByControl));
                     } while(!lv2.canStart());

                     Iterator var4 = lv2.getControls().iterator();

                     while(var4.hasNext()) {
                        Goal.Control lv3 = (Goal.Control)var4.next();
                        PrioritizedGoal lv4 = (PrioritizedGoal)this.goalsByControl.getOrDefault(lv3, REPLACEABLE_GOAL);
                        lv4.stop();
                        this.goalsByControl.put(lv3, lv2);
                     }

                     lv2.start();
                  }
               }

               lv2 = (PrioritizedGoal)iterator.next();
            } while(!lv2.isRunning());
         } while(!usesAny(lv2, this.disabledControls) && lv2.shouldContinue());

         lv2.stop();
      }
   }

   public void tickGoals(boolean tickAll) {
      Profiler lv = (Profiler)this.profiler.get();
      lv.push("goalTick");
      Iterator var3 = this.goals.iterator();

      while(true) {
         PrioritizedGoal lv2;
         do {
            do {
               if (!var3.hasNext()) {
                  lv.pop();
                  return;
               }

               lv2 = (PrioritizedGoal)var3.next();
            } while(!lv2.isRunning());
         } while(!tickAll && !lv2.shouldRunEveryTick());

         lv2.tick();
      }
   }

   public Set getGoals() {
      return this.goals;
   }

   public Stream getRunningGoals() {
      return this.goals.stream().filter(PrioritizedGoal::isRunning);
   }

   public void setTimeInterval(int timeInterval) {
      this.timeInterval = timeInterval;
   }

   public void disableControl(Goal.Control control) {
      this.disabledControls.add(control);
   }

   public void enableControl(Goal.Control control) {
      this.disabledControls.remove(control);
   }

   public void setControlEnabled(Goal.Control control, boolean enabled) {
      if (enabled) {
         this.enableControl(control);
      } else {
         this.disableControl(control);
      }

   }
}
