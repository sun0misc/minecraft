package net.minecraft.entity.ai.brain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import java.util.Collection;
import java.util.List;

public class ScheduleRule {
   private final List entries = Lists.newArrayList();
   private int prioritizedEntryIndex;

   public ImmutableList getEntries() {
      return ImmutableList.copyOf(this.entries);
   }

   public ScheduleRule add(int startTime, float priority) {
      this.entries.add(new ScheduleRuleEntry(startTime, priority));
      this.sort();
      return this;
   }

   public ScheduleRule add(Collection entries) {
      this.entries.addAll(entries);
      this.sort();
      return this;
   }

   private void sort() {
      Int2ObjectSortedMap int2ObjectSortedMap = new Int2ObjectAVLTreeMap();
      this.entries.forEach((arg) -> {
         int2ObjectSortedMap.put(arg.getStartTime(), arg);
      });
      this.entries.clear();
      this.entries.addAll(int2ObjectSortedMap.values());
      this.prioritizedEntryIndex = 0;
   }

   public float getPriority(int time) {
      if (this.entries.size() <= 0) {
         return 0.0F;
      } else {
         ScheduleRuleEntry lv = (ScheduleRuleEntry)this.entries.get(this.prioritizedEntryIndex);
         ScheduleRuleEntry lv2 = (ScheduleRuleEntry)this.entries.get(this.entries.size() - 1);
         boolean bl = time < lv.getStartTime();
         int j = bl ? 0 : this.prioritizedEntryIndex;
         float f = bl ? lv2.getPriority() : lv.getPriority();

         for(int k = j; k < this.entries.size(); ++k) {
            ScheduleRuleEntry lv3 = (ScheduleRuleEntry)this.entries.get(k);
            if (lv3.getStartTime() > time) {
               break;
            }

            this.prioritizedEntryIndex = k;
            f = lv3.getPriority();
         }

         return f;
      }
   }
}
