package net.minecraft.client.render.model.json;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;

@Environment(EnvType.CLIENT)
public class SimpleMultipartModelSelector implements MultipartModelSelector {
   private static final Splitter VALUE_SPLITTER = Splitter.on('|').omitEmptyStrings();
   private final String key;
   private final String valueString;

   public SimpleMultipartModelSelector(String key, String valueString) {
      this.key = key;
      this.valueString = valueString;
   }

   public Predicate getPredicate(StateManager arg) {
      Property lv = arg.getProperty(this.key);
      if (lv == null) {
         throw new RuntimeException(String.format(Locale.ROOT, "Unknown property '%s' on '%s'", this.key, arg.getOwner()));
      } else {
         String string = this.valueString;
         boolean bl = !string.isEmpty() && string.charAt(0) == '!';
         if (bl) {
            string = string.substring(1);
         }

         List list = VALUE_SPLITTER.splitToList(string);
         if (list.isEmpty()) {
            throw new RuntimeException(String.format(Locale.ROOT, "Empty value '%s' for property '%s' on '%s'", this.valueString, this.key, arg.getOwner()));
         } else {
            Predicate predicate;
            if (list.size() == 1) {
               predicate = this.createPredicate(arg, lv, string);
            } else {
               List list2 = (List)list.stream().map((value) -> {
                  return this.createPredicate(arg, lv, value);
               }).collect(Collectors.toList());
               predicate = (state) -> {
                  return list2.stream().anyMatch((predicate) -> {
                     return predicate.test(state);
                  });
               };
            }

            return bl ? predicate.negate() : predicate;
         }
      }
   }

   private Predicate createPredicate(StateManager stateFactory, Property property, String valueString) {
      Optional optional = property.parse(valueString);
      if (!optional.isPresent()) {
         throw new RuntimeException(String.format(Locale.ROOT, "Unknown value '%s' for property '%s' on '%s' in '%s'", valueString, this.key, stateFactory.getOwner(), this.valueString));
      } else {
         return (state) -> {
            return state.get(property).equals(optional.get());
         };
      }
   }

   public String toString() {
      return MoreObjects.toStringHelper(this).add("key", this.key).add("value", this.valueString).toString();
   }
}
