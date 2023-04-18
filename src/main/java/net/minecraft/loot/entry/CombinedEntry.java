package net.minecraft.loot.entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.JsonHelper;

public abstract class CombinedEntry extends LootPoolEntry {
   protected final LootPoolEntry[] children;
   private final EntryCombiner predicate;

   protected CombinedEntry(LootPoolEntry[] children, LootCondition[] conditions) {
      super(conditions);
      this.children = children;
      this.predicate = this.combine(children);
   }

   public void validate(LootTableReporter reporter) {
      super.validate(reporter);
      if (this.children.length == 0) {
         reporter.report("Empty children list");
      }

      for(int i = 0; i < this.children.length; ++i) {
         this.children[i].validate(reporter.makeChild(".entry[" + i + "]"));
      }

   }

   protected abstract EntryCombiner combine(EntryCombiner[] children);

   public final boolean expand(LootContext arg, Consumer consumer) {
      return !this.test(arg) ? false : this.predicate.expand(arg, consumer);
   }

   public static LootPoolEntry.Serializer createSerializer(final Factory factory) {
      return new LootPoolEntry.Serializer() {
         public void addEntryFields(JsonObject jsonObject, CombinedEntry arg, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("children", jsonSerializationContext.serialize(arg.children));
         }

         public final CombinedEntry fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
            LootPoolEntry[] lvs = (LootPoolEntry[])JsonHelper.deserialize(jsonObject, "children", jsonDeserializationContext, LootPoolEntry[].class);
            return factory.create(lvs, args);
         }

         // $FF: synthetic method
         public LootPoolEntry fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return this.fromJson(json, context, conditions);
         }
      };
   }

   @FunctionalInterface
   public interface Factory {
      CombinedEntry create(LootPoolEntry[] children, LootCondition[] conditions);
   }
}
