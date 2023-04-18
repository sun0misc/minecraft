package net.minecraft.loot.entry;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.loot.condition.LootCondition;

public class SequenceEntry extends CombinedEntry {
   SequenceEntry(LootPoolEntry[] args, LootCondition[] args2) {
      super(args, args2);
   }

   public LootPoolEntryType getType() {
      return LootPoolEntryTypes.SEQUENCE;
   }

   protected EntryCombiner combine(EntryCombiner[] children) {
      switch (children.length) {
         case 0:
            return ALWAYS_TRUE;
         case 1:
            return children[0];
         case 2:
            return children[0].and(children[1]);
         default:
            return (context, lootChoiceExpander) -> {
               EntryCombiner[] var3 = children;
               int var4 = children.length;

               for(int var5 = 0; var5 < var4; ++var5) {
                  EntryCombiner lv = var3[var5];
                  if (!lv.expand(context, lootChoiceExpander)) {
                     return false;
                  }
               }

               return true;
            };
      }
   }

   public static Builder create(LootPoolEntry.Builder... entries) {
      return new Builder(entries);
   }

   public static class Builder extends LootPoolEntry.Builder {
      private final List entries = Lists.newArrayList();

      public Builder(LootPoolEntry.Builder... entries) {
         LootPoolEntry.Builder[] var2 = entries;
         int var3 = entries.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            LootPoolEntry.Builder lv = var2[var4];
            this.entries.add(lv.build());
         }

      }

      protected Builder getThisBuilder() {
         return this;
      }

      public Builder groupEntry(LootPoolEntry.Builder entry) {
         this.entries.add(entry.build());
         return this;
      }

      public LootPoolEntry build() {
         return new SequenceEntry((LootPoolEntry[])this.entries.toArray(new LootPoolEntry[0]), this.getConditions());
      }

      // $FF: synthetic method
      protected LootPoolEntry.Builder getThisBuilder() {
         return this.getThisBuilder();
      }
   }
}
