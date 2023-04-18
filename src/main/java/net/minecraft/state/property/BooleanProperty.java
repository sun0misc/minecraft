package net.minecraft.state.property;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Optional;

public class BooleanProperty extends Property {
   private final ImmutableSet values = ImmutableSet.of(true, false);

   protected BooleanProperty(String name) {
      super(name, Boolean.class);
   }

   public Collection getValues() {
      return this.values;
   }

   public static BooleanProperty of(String name) {
      return new BooleanProperty(name);
   }

   public Optional parse(String name) {
      return !"true".equals(name) && !"false".equals(name) ? Optional.empty() : Optional.of(Boolean.valueOf(name));
   }

   public String name(Boolean boolean_) {
      return boolean_.toString();
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object instanceof BooleanProperty && super.equals(object)) {
         BooleanProperty lv = (BooleanProperty)object;
         return this.values.equals(lv.values);
      } else {
         return false;
      }
   }

   public int computeHashCode() {
      return 31 * super.computeHashCode() + this.values.hashCode();
   }
}
