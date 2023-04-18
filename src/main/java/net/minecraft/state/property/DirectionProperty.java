package net.minecraft.state.property;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.util.math.Direction;

public class DirectionProperty extends EnumProperty {
   protected DirectionProperty(String name, Collection values) {
      super(name, Direction.class, values);
   }

   public static DirectionProperty of(String name) {
      return of(name, (arg) -> {
         return true;
      });
   }

   public static DirectionProperty of(String name, Predicate filter) {
      return of(name, (Collection)Arrays.stream(Direction.values()).filter(filter).collect(Collectors.toList()));
   }

   public static DirectionProperty of(String name, Direction... values) {
      return of(name, (Collection)Lists.newArrayList(values));
   }

   public static DirectionProperty of(String name, Collection values) {
      return new DirectionProperty(name, values);
   }
}
