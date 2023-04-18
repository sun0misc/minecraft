package net.minecraft.state.property;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class IntProperty extends Property {
   private final ImmutableSet values;
   private final int min;
   private final int max;

   protected IntProperty(String name, int min, int max) {
      super(name, Integer.class);
      if (min < 0) {
         throw new IllegalArgumentException("Min value of " + name + " must be 0 or greater");
      } else if (max <= min) {
         throw new IllegalArgumentException("Max value of " + name + " must be greater than min (" + min + ")");
      } else {
         this.min = min;
         this.max = max;
         Set set = Sets.newHashSet();

         for(int k = min; k <= max; ++k) {
            set.add(k);
         }

         this.values = ImmutableSet.copyOf(set);
      }
   }

   public Collection getValues() {
      return this.values;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object instanceof IntProperty && super.equals(object)) {
         IntProperty lv = (IntProperty)object;
         return this.values.equals(lv.values);
      } else {
         return false;
      }
   }

   public int computeHashCode() {
      return 31 * super.computeHashCode() + this.values.hashCode();
   }

   public static IntProperty of(String name, int min, int max) {
      return new IntProperty(name, min, max);
   }

   public Optional parse(String name) {
      try {
         Integer integer = Integer.valueOf(name);
         return integer >= this.min && integer <= this.max ? Optional.of(integer) : Optional.empty();
      } catch (NumberFormatException var3) {
         return Optional.empty();
      }
   }

   public String name(Integer integer) {
      return integer.toString();
   }
}
