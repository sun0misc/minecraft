package net.minecraft.loot.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import net.minecraft.loot.LootDataKey;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import org.slf4j.Logger;

public class ReferenceLootCondition implements LootCondition {
   private static final Logger LOGGER = LogUtils.getLogger();
   final Identifier id;

   ReferenceLootCondition(Identifier id) {
      this.id = id;
   }

   public LootConditionType getType() {
      return LootConditionTypes.REFERENCE;
   }

   public void validate(LootTableReporter reporter) {
      LootDataKey lv = new LootDataKey(LootDataType.PREDICATES, this.id);
      if (reporter.isInStack(lv)) {
         reporter.report("Condition " + this.id + " is recursively called");
      } else {
         LootCondition.super.validate(reporter);
         reporter.getDataLookup().getElementOptional(lv).ifPresentOrElse((predicate) -> {
            predicate.validate(reporter.makeChild(".{" + this.id + "}", lv));
         }, () -> {
            reporter.report("Unknown condition table called " + this.id);
         });
      }
   }

   public boolean test(LootContext arg) {
      LootCondition lv = (LootCondition)arg.getDataLookup().getElement(LootDataType.PREDICATES, this.id);
      if (lv == null) {
         LOGGER.warn("Tried using unknown condition table called {}", this.id);
         return false;
      } else {
         LootContext.Entry lv2 = LootContext.predicate(lv);
         if (arg.markActive(lv2)) {
            boolean var4;
            try {
               var4 = lv.test(arg);
            } finally {
               arg.markInactive(lv2);
            }

            return var4;
         } else {
            LOGGER.warn("Detected infinite loop in loot tables");
            return false;
         }
      }
   }

   public static LootCondition.Builder builder(Identifier id) {
      return () -> {
         return new ReferenceLootCondition(id);
      };
   }

   // $FF: synthetic method
   public boolean test(Object context) {
      return this.test((LootContext)context);
   }

   public static class Serializer implements JsonSerializer {
      public void toJson(JsonObject jsonObject, ReferenceLootCondition arg, JsonSerializationContext jsonSerializationContext) {
         jsonObject.addProperty("name", arg.id.toString());
      }

      public ReferenceLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         Identifier lv = new Identifier(JsonHelper.getString(jsonObject, "name"));
         return new ReferenceLootCondition(lv);
      }

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }
}
