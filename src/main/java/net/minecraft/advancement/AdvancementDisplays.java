package net.minecraft.advancement;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Iterator;
import java.util.function.Predicate;

public class AdvancementDisplays {
   private static final int DISPLAY_DEPTH = 2;

   private static Status getStatus(Advancement advancement, boolean force) {
      AdvancementDisplay lv = advancement.getDisplay();
      if (lv == null) {
         return AdvancementDisplays.Status.HIDE;
      } else if (force) {
         return AdvancementDisplays.Status.SHOW;
      } else {
         return lv.isHidden() ? AdvancementDisplays.Status.HIDE : AdvancementDisplays.Status.NO_CHANGE;
      }
   }

   private static boolean shouldDisplay(Stack statuses) {
      for(int i = 0; i <= 2; ++i) {
         Status lv = (Status)statuses.peek(i);
         if (lv == AdvancementDisplays.Status.SHOW) {
            return true;
         }

         if (lv == AdvancementDisplays.Status.HIDE) {
            return false;
         }
      }

      return false;
   }

   private static boolean shouldDisplay(Advancement advancement, Stack statuses, Predicate donePredicate, ResultConsumer consumer) {
      boolean bl = donePredicate.test(advancement);
      Status lv = getStatus(advancement, bl);
      boolean bl2 = bl;
      statuses.push(lv);

      Advancement lv2;
      for(Iterator var7 = advancement.getChildren().iterator(); var7.hasNext(); bl2 |= shouldDisplay(lv2, statuses, donePredicate, consumer)) {
         lv2 = (Advancement)var7.next();
      }

      boolean bl3 = bl2 || shouldDisplay(statuses);
      statuses.pop();
      consumer.accept(advancement, bl3);
      return bl2;
   }

   public static void calculateDisplay(Advancement advancement, Predicate donePredicate, ResultConsumer consumer) {
      Advancement lv = advancement.getRoot();
      Stack stack = new ObjectArrayList();

      for(int i = 0; i <= 2; ++i) {
         stack.push(AdvancementDisplays.Status.NO_CHANGE);
      }

      shouldDisplay(lv, stack, donePredicate, consumer);
   }

   private static enum Status {
      SHOW,
      HIDE,
      NO_CHANGE;

      // $FF: synthetic method
      private static Status[] method_48034() {
         return new Status[]{SHOW, HIDE, NO_CHANGE};
      }
   }

   @FunctionalInterface
   public interface ResultConsumer {
      void accept(Advancement advancement, boolean shouldDisplay);
   }
}
