package net.minecraft.state;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.state.property.Property;
import org.jetbrains.annotations.Nullable;

public class StateManager {
   static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
   private final Object owner;
   private final ImmutableSortedMap properties;
   private final ImmutableList states;

   protected StateManager(Function defaultStateGetter, Object owner, Factory factory, Map propertiesMap) {
      this.owner = owner;
      this.properties = ImmutableSortedMap.copyOf(propertiesMap);
      Supplier supplier = () -> {
         return (State)defaultStateGetter.apply(owner);
      };
      MapCodec mapCodec = MapCodec.of(Encoder.empty(), Decoder.unit(supplier));

      Map.Entry entry;
      for(UnmodifiableIterator var7 = this.properties.entrySet().iterator(); var7.hasNext(); mapCodec = addFieldToMapCodec(mapCodec, supplier, (String)entry.getKey(), (Property)entry.getValue())) {
         entry = (Map.Entry)var7.next();
      }

      Map map2 = Maps.newLinkedHashMap();
      List list = Lists.newArrayList();
      Stream stream = Stream.of(Collections.emptyList());

      Property lv;
      for(UnmodifiableIterator var11 = this.properties.values().iterator(); var11.hasNext(); stream = stream.flatMap((listx) -> {
         return lv.getValues().stream().map((comparable) -> {
            List list2 = Lists.newArrayList(listx);
            list2.add(Pair.of(lv, comparable));
            return list2;
         });
      })) {
         lv = (Property)var11.next();
      }

      stream.forEach((list2) -> {
         ImmutableMap immutableMap = (ImmutableMap)list2.stream().collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
         State lv = (State)factory.create(owner, immutableMap, mapCodec);
         map2.put(immutableMap, lv);
         list.add(lv);
      });
      Iterator var13 = list.iterator();

      while(var13.hasNext()) {
         State lv2 = (State)var13.next();
         lv2.createWithTable(map2);
      }

      this.states = ImmutableList.copyOf(list);
   }

   private static MapCodec addFieldToMapCodec(MapCodec mapCodec, Supplier defaultStateGetter, String key, Property property) {
      return Codec.mapPair(mapCodec, property.getValueCodec().fieldOf(key).orElseGet((string) -> {
      }, () -> {
         return property.createValue((State)defaultStateGetter.get());
      })).xmap((pair) -> {
         return (State)((State)pair.getFirst()).with(property, ((Property.Value)pair.getSecond()).value());
      }, (arg2) -> {
         return Pair.of(arg2, property.createValue(arg2));
      });
   }

   public ImmutableList getStates() {
      return this.states;
   }

   public State getDefaultState() {
      return (State)this.states.get(0);
   }

   public Object getOwner() {
      return this.owner;
   }

   public Collection getProperties() {
      return this.properties.values();
   }

   public String toString() {
      return MoreObjects.toStringHelper(this).add("block", this.owner).add("properties", this.properties.values().stream().map(Property::getName).collect(Collectors.toList())).toString();
   }

   @Nullable
   public Property getProperty(String name) {
      return (Property)this.properties.get(name);
   }

   public interface Factory {
      Object create(Object owner, ImmutableMap entries, MapCodec codec);
   }

   public static class Builder {
      private final Object owner;
      private final Map namedProperties = Maps.newHashMap();

      public Builder(Object owner) {
         this.owner = owner;
      }

      public Builder add(Property... properties) {
         Property[] var2 = properties;
         int var3 = properties.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            Property lv = var2[var4];
            this.validate(lv);
            this.namedProperties.put(lv.getName(), lv);
         }

         return this;
      }

      private void validate(Property property) {
         String string = property.getName();
         if (!StateManager.VALID_NAME_PATTERN.matcher(string).matches()) {
            throw new IllegalArgumentException(this.owner + " has invalidly named property: " + string);
         } else {
            Collection collection = property.getValues();
            if (collection.size() <= 1) {
               throw new IllegalArgumentException(this.owner + " attempted use property " + string + " with <= 1 possible values");
            } else {
               Iterator var4 = collection.iterator();

               String string2;
               do {
                  if (!var4.hasNext()) {
                     if (this.namedProperties.containsKey(string)) {
                        throw new IllegalArgumentException(this.owner + " has duplicate property: " + string);
                     }

                     return;
                  }

                  Comparable comparable = (Comparable)var4.next();
                  string2 = property.name(comparable);
               } while(StateManager.VALID_NAME_PATTERN.matcher(string2).matches());

               throw new IllegalArgumentException(this.owner + " has property: " + string + " with invalidly named value: " + string2);
            }
         }
      }

      public StateManager build(Function defaultStateGetter, Factory factory) {
         return new StateManager(defaultStateGetter, this.owner, factory, this.namedProperties);
      }
   }
}
