package net.minecraft.data.client;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;

public interface When extends Supplier {
   void validate(StateManager stateManager);

   static PropertyCondition create() {
      return new PropertyCondition();
   }

   static When allOf(When... conditions) {
      return new LogicalCondition(When.LogicalOperator.AND, Arrays.asList(conditions));
   }

   static When anyOf(When... conditions) {
      return new LogicalCondition(When.LogicalOperator.OR, Arrays.asList(conditions));
   }

   public static class PropertyCondition implements When {
      private final Map properties = Maps.newHashMap();

      private static String name(Property property, Stream valueStream) {
         Objects.requireNonNull(property);
         return (String)valueStream.map(property::name).collect(Collectors.joining("|"));
      }

      private static String name(Property property, Comparable value, Comparable[] otherValues) {
         return name(property, Stream.concat(Stream.of(value), Stream.of(otherValues)));
      }

      private void set(Property property, String value) {
         String string2 = (String)this.properties.put(property, value);
         if (string2 != null) {
            throw new IllegalStateException("Tried to replace " + property + " value from " + string2 + " to " + value);
         }
      }

      public final PropertyCondition set(Property property, Comparable value) {
         this.set(property, property.name(value));
         return this;
      }

      @SafeVarargs
      public final PropertyCondition set(Property property, Comparable value, Comparable... otherValues) {
         this.set(property, name(property, value, otherValues));
         return this;
      }

      public final PropertyCondition setNegated(Property property, Comparable value) {
         String var10002 = property.name(value);
         this.set(property, "!" + var10002);
         return this;
      }

      @SafeVarargs
      public final PropertyCondition setNegated(Property property, Comparable value, Comparable... otherValues) {
         String var10002 = name(property, value, otherValues);
         this.set(property, "!" + var10002);
         return this;
      }

      public JsonElement get() {
         JsonObject jsonObject = new JsonObject();
         this.properties.forEach((property, value) -> {
            jsonObject.addProperty(property.getName(), value);
         });
         return jsonObject;
      }

      public void validate(StateManager stateManager) {
         List list = (List)this.properties.keySet().stream().filter((property) -> {
            return stateManager.getProperty(property.getName()) != property;
         }).collect(Collectors.toList());
         if (!list.isEmpty()) {
            throw new IllegalStateException("Properties " + list + " are missing from " + stateManager);
         }
      }

      // $FF: synthetic method
      public Object get() {
         return this.get();
      }
   }

   public static class LogicalCondition implements When {
      private final LogicalOperator operator;
      private final List components;

      LogicalCondition(LogicalOperator operator, List components) {
         this.operator = operator;
         this.components = components;
      }

      public void validate(StateManager stateManager) {
         this.components.forEach((component) -> {
            component.validate(stateManager);
         });
      }

      public JsonElement get() {
         JsonArray jsonArray = new JsonArray();
         Stream var10000 = this.components.stream().map(Supplier::get);
         Objects.requireNonNull(jsonArray);
         var10000.forEach(jsonArray::add);
         JsonObject jsonObject = new JsonObject();
         jsonObject.add(this.operator.name, jsonArray);
         return jsonObject;
      }

      // $FF: synthetic method
      public Object get() {
         return this.get();
      }
   }

   public static enum LogicalOperator {
      AND("AND"),
      OR("OR");

      final String name;

      private LogicalOperator(String name) {
         this.name = name;
      }

      // $FF: synthetic method
      private static LogicalOperator[] method_36940() {
         return new LogicalOperator[]{AND, OR};
      }
   }
}
