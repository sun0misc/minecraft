package net.minecraft.predicate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

public class StatePredicate {
   public static final StatePredicate ANY = new StatePredicate(ImmutableList.of());
   private final List conditions;

   private static Condition createPredicate(String key, JsonElement json) {
      if (json.isJsonPrimitive()) {
         String string2 = json.getAsString();
         return new ExactValueCondition(key, string2);
      } else {
         JsonObject jsonObject = JsonHelper.asObject(json, "value");
         String string3 = jsonObject.has("min") ? asNullableString(jsonObject.get("min")) : null;
         String string4 = jsonObject.has("max") ? asNullableString(jsonObject.get("max")) : null;
         return (Condition)(string3 != null && string3.equals(string4) ? new ExactValueCondition(key, string3) : new RangedValueCondition(key, string3, string4));
      }
   }

   @Nullable
   private static String asNullableString(JsonElement json) {
      return json.isJsonNull() ? null : json.getAsString();
   }

   StatePredicate(List conditions) {
      this.conditions = ImmutableList.copyOf(conditions);
   }

   public boolean test(StateManager stateManager, State container) {
      Iterator var3 = this.conditions.iterator();

      Condition lv;
      do {
         if (!var3.hasNext()) {
            return true;
         }

         lv = (Condition)var3.next();
      } while(lv.test(stateManager, container));

      return false;
   }

   public boolean test(BlockState state) {
      return this.test(state.getBlock().getStateManager(), state);
   }

   public boolean test(FluidState state) {
      return this.test(state.getFluid().getStateManager(), state);
   }

   public void check(StateManager factory, Consumer reporter) {
      this.conditions.forEach((condition) -> {
         condition.reportMissing(factory, reporter);
      });
   }

   public static StatePredicate fromJson(@Nullable JsonElement json) {
      if (json != null && !json.isJsonNull()) {
         JsonObject jsonObject = JsonHelper.asObject(json, "properties");
         List list = Lists.newArrayList();
         Iterator var3 = jsonObject.entrySet().iterator();

         while(var3.hasNext()) {
            Map.Entry entry = (Map.Entry)var3.next();
            list.add(createPredicate((String)entry.getKey(), (JsonElement)entry.getValue()));
         }

         return new StatePredicate(list);
      } else {
         return ANY;
      }
   }

   public JsonElement toJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonObject = new JsonObject();
         if (!this.conditions.isEmpty()) {
            this.conditions.forEach((condition) -> {
               jsonObject.add(condition.getKey(), condition.toJson());
            });
         }

         return jsonObject;
      }
   }

   private static class ExactValueCondition extends Condition {
      private final String value;

      public ExactValueCondition(String key, String value) {
         super(key);
         this.value = value;
      }

      protected boolean test(State state, Property property) {
         Comparable comparable = state.get(property);
         Optional optional = property.parse(this.value);
         return optional.isPresent() && comparable.compareTo((Comparable)optional.get()) == 0;
      }

      public JsonElement toJson() {
         return new JsonPrimitive(this.value);
      }
   }

   private static class RangedValueCondition extends Condition {
      @Nullable
      private final String min;
      @Nullable
      private final String max;

      public RangedValueCondition(String key, @Nullable String min, @Nullable String max) {
         super(key);
         this.min = min;
         this.max = max;
      }

      protected boolean test(State state, Property property) {
         Comparable comparable = state.get(property);
         Optional optional;
         if (this.min != null) {
            optional = property.parse(this.min);
            if (!optional.isPresent() || comparable.compareTo((Comparable)optional.get()) < 0) {
               return false;
            }
         }

         if (this.max != null) {
            optional = property.parse(this.max);
            if (!optional.isPresent() || comparable.compareTo((Comparable)optional.get()) > 0) {
               return false;
            }
         }

         return true;
      }

      public JsonElement toJson() {
         JsonObject jsonObject = new JsonObject();
         if (this.min != null) {
            jsonObject.addProperty("min", this.min);
         }

         if (this.max != null) {
            jsonObject.addProperty("max", this.max);
         }

         return jsonObject;
      }
   }

   abstract static class Condition {
      private final String key;

      public Condition(String key) {
         this.key = key;
      }

      public boolean test(StateManager stateManager, State state) {
         Property lv = stateManager.getProperty(this.key);
         return lv == null ? false : this.test(state, lv);
      }

      protected abstract boolean test(State state, Property property);

      public abstract JsonElement toJson();

      public String getKey() {
         return this.key;
      }

      public void reportMissing(StateManager factory, Consumer reporter) {
         Property lv = factory.getProperty(this.key);
         if (lv == null) {
            reporter.accept(this.key);
         }

      }
   }

   public static class Builder {
      private final List conditions = Lists.newArrayList();

      private Builder() {
      }

      public static Builder create() {
         return new Builder();
      }

      public Builder exactMatch(Property property, String valueName) {
         this.conditions.add(new ExactValueCondition(property.getName(), valueName));
         return this;
      }

      public Builder exactMatch(Property property, int value) {
         return this.exactMatch(property, Integer.toString(value));
      }

      public Builder exactMatch(Property property, boolean value) {
         return this.exactMatch(property, Boolean.toString(value));
      }

      public Builder exactMatch(Property property, Comparable value) {
         return this.exactMatch(property, ((StringIdentifiable)value).asString());
      }

      public StatePredicate build() {
         return new StatePredicate(this.conditions);
      }
   }
}
