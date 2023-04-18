package net.minecraft.state;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.state.property.Property;
import org.jetbrains.annotations.Nullable;

public abstract class State {
   public static final String NAME = "Name";
   public static final String PROPERTIES = "Properties";
   private static final Function PROPERTY_MAP_PRINTER = new Function() {
      public String apply(@Nullable Map.Entry entry) {
         if (entry == null) {
            return "<NULL>";
         } else {
            Property lv = (Property)entry.getKey();
            String var10000 = lv.getName();
            return var10000 + "=" + this.nameValue(lv, (Comparable)entry.getValue());
         }
      }

      private String nameValue(Property property, Comparable value) {
         return property.name(value);
      }

      // $FF: synthetic method
      public Object apply(@Nullable Object entry) {
         return this.apply((Map.Entry)entry);
      }
   };
   protected final Object owner;
   private final ImmutableMap entries;
   private Table withTable;
   protected final MapCodec codec;

   protected State(Object owner, ImmutableMap entries, MapCodec codec) {
      this.owner = owner;
      this.entries = entries;
      this.codec = codec;
   }

   public Object cycle(Property property) {
      return this.with(property, (Comparable)getNext(property.getValues(), this.get(property)));
   }

   protected static Object getNext(Collection values, Object value) {
      Iterator iterator = values.iterator();

      do {
         if (!iterator.hasNext()) {
            return iterator.next();
         }
      } while(!iterator.next().equals(value));

      if (iterator.hasNext()) {
         return iterator.next();
      } else {
         return values.iterator().next();
      }
   }

   public String toString() {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(this.owner);
      if (!this.getEntries().isEmpty()) {
         stringBuilder.append('[');
         stringBuilder.append((String)this.getEntries().entrySet().stream().map(PROPERTY_MAP_PRINTER).collect(Collectors.joining(",")));
         stringBuilder.append(']');
      }

      return stringBuilder.toString();
   }

   public Collection getProperties() {
      return Collections.unmodifiableCollection(this.entries.keySet());
   }

   public boolean contains(Property property) {
      return this.entries.containsKey(property);
   }

   public Comparable get(Property property) {
      Comparable comparable = (Comparable)this.entries.get(property);
      if (comparable == null) {
         throw new IllegalArgumentException("Cannot get property " + property + " as it does not exist in " + this.owner);
      } else {
         return (Comparable)property.getType().cast(comparable);
      }
   }

   public Optional getOrEmpty(Property property) {
      Comparable comparable = (Comparable)this.entries.get(property);
      return comparable == null ? Optional.empty() : Optional.of((Comparable)property.getType().cast(comparable));
   }

   public Object with(Property property, Comparable value) {
      Comparable comparable2 = (Comparable)this.entries.get(property);
      if (comparable2 == null) {
         throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist in " + this.owner);
      } else if (comparable2 == value) {
         return this;
      } else {
         Object object = this.withTable.get(property, value);
         if (object == null) {
            throw new IllegalArgumentException("Cannot set property " + property + " to " + value + " on " + this.owner + ", it is not an allowed value");
         } else {
            return object;
         }
      }
   }

   public Object withIfExists(Property property, Comparable value) {
      Comparable comparable2 = (Comparable)this.entries.get(property);
      if (comparable2 != null && comparable2 != value) {
         Object object = this.withTable.get(property, value);
         if (object == null) {
            throw new IllegalArgumentException("Cannot set property " + property + " to " + value + " on " + this.owner + ", it is not an allowed value");
         } else {
            return object;
         }
      } else {
         return this;
      }
   }

   public void createWithTable(Map states) {
      if (this.withTable != null) {
         throw new IllegalStateException();
      } else {
         Table table = HashBasedTable.create();
         UnmodifiableIterator var3 = this.entries.entrySet().iterator();

         while(var3.hasNext()) {
            Map.Entry entry = (Map.Entry)var3.next();
            Property lv = (Property)entry.getKey();
            Iterator var6 = lv.getValues().iterator();

            while(var6.hasNext()) {
               Comparable comparable = (Comparable)var6.next();
               if (comparable != entry.getValue()) {
                  table.put(lv, comparable, states.get(this.toMapWith(lv, comparable)));
               }
            }
         }

         this.withTable = (Table)(table.isEmpty() ? table : ArrayTable.create(table));
      }
   }

   private Map toMapWith(Property property, Comparable value) {
      Map map = Maps.newHashMap(this.entries);
      map.put(property, value);
      return map;
   }

   public ImmutableMap getEntries() {
      return this.entries;
   }

   protected static Codec createCodec(Codec codec, Function ownerToStateFunction) {
      return codec.dispatch("Name", (arg) -> {
         return arg.owner;
      }, (object) -> {
         State lv = (State)ownerToStateFunction.apply(object);
         return lv.getEntries().isEmpty() ? Codec.unit(lv) : lv.codec.codec().optionalFieldOf("Properties").xmap((optional) -> {
            return (State)optional.orElse(lv);
         }, Optional::of).codec();
      });
   }
}
