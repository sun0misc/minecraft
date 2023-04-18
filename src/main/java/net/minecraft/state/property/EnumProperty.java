package net.minecraft.state.property;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.util.StringIdentifiable;

public class EnumProperty extends Property {
   private final ImmutableSet values;
   private final Map byName = Maps.newHashMap();

   protected EnumProperty(String name, Class type, Collection values) {
      super(name, type);
      this.values = ImmutableSet.copyOf(values);
      Iterator var4 = values.iterator();

      while(var4.hasNext()) {
         Enum enum_ = (Enum)var4.next();
         String string2 = ((StringIdentifiable)enum_).asString();
         if (this.byName.containsKey(string2)) {
            throw new IllegalArgumentException("Multiple values have the same name '" + string2 + "'");
         }

         this.byName.put(string2, enum_);
      }

   }

   public Collection getValues() {
      return this.values;
   }

   public Optional parse(String name) {
      return Optional.ofNullable((Enum)this.byName.get(name));
   }

   public String name(Enum enum_) {
      return ((StringIdentifiable)enum_).asString();
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object instanceof EnumProperty && super.equals(object)) {
         EnumProperty lv = (EnumProperty)object;
         return this.values.equals(lv.values) && this.byName.equals(lv.byName);
      } else {
         return false;
      }
   }

   public int computeHashCode() {
      int i = super.computeHashCode();
      i = 31 * i + this.values.hashCode();
      i = 31 * i + this.byName.hashCode();
      return i;
   }

   public static EnumProperty of(String name, Class type) {
      return of(name, type, (enum_) -> {
         return true;
      });
   }

   public static EnumProperty of(String name, Class type, Predicate filter) {
      return of(name, type, (Collection)Arrays.stream((Enum[])type.getEnumConstants()).filter(filter).collect(Collectors.toList()));
   }

   public static EnumProperty of(String name, Class type, Enum... values) {
      return of(name, type, (Collection)Lists.newArrayList(values));
   }

   public static EnumProperty of(String name, Class type, Collection values) {
      return new EnumProperty(name, type, values);
   }
}
