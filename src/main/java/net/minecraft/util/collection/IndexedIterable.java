package net.minecraft.util.collection;

import org.jetbrains.annotations.Nullable;

public interface IndexedIterable extends Iterable {
   int ABSENT_RAW_ID = -1;

   int getRawId(Object value);

   @Nullable
   Object get(int index);

   default Object getOrThrow(int index) {
      Object object = this.get(index);
      if (object == null) {
         throw new IllegalArgumentException("No value with id " + index);
      } else {
         return object;
      }
   }

   int size();
}
