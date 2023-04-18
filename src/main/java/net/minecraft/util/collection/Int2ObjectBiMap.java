package net.minecraft.util.collection;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import java.util.Arrays;
import java.util.Iterator;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class Int2ObjectBiMap implements IndexedIterable {
   private static final int ABSENT = -1;
   private static final Object EMPTY = null;
   private static final float LOAD_FACTOR = 0.8F;
   private Object[] values;
   private int[] ids;
   private Object[] idToValues;
   private int nextId;
   private int size;

   private Int2ObjectBiMap(int size) {
      this.values = new Object[size];
      this.ids = new int[size];
      this.idToValues = new Object[size];
   }

   private Int2ObjectBiMap(Object[] values, int[] ids, Object[] idToValues, int nextId, int size) {
      this.values = values;
      this.ids = ids;
      this.idToValues = idToValues;
      this.nextId = nextId;
      this.size = size;
   }

   public static Int2ObjectBiMap create(int expectedSize) {
      return new Int2ObjectBiMap((int)((float)expectedSize / 0.8F));
   }

   public int getRawId(@Nullable Object value) {
      return this.getIdFromIndex(this.findIndex(value, this.getIdealIndex(value)));
   }

   @Nullable
   public Object get(int index) {
      return index >= 0 && index < this.idToValues.length ? this.idToValues[index] : null;
   }

   private int getIdFromIndex(int index) {
      return index == -1 ? -1 : this.ids[index];
   }

   public boolean contains(Object value) {
      return this.getRawId(value) != -1;
   }

   public boolean containsKey(int index) {
      return this.get(index) != null;
   }

   public int add(Object value) {
      int i = this.nextId();
      this.put(value, i);
      return i;
   }

   private int nextId() {
      while(this.nextId < this.idToValues.length && this.idToValues[this.nextId] != null) {
         ++this.nextId;
      }

      return this.nextId;
   }

   private void resize(int newSize) {
      Object[] objects = this.values;
      int[] is = this.ids;
      Int2ObjectBiMap lv = new Int2ObjectBiMap(newSize);

      for(int j = 0; j < objects.length; ++j) {
         if (objects[j] != null) {
            lv.put(objects[j], is[j]);
         }
      }

      this.values = lv.values;
      this.ids = lv.ids;
      this.idToValues = lv.idToValues;
      this.nextId = lv.nextId;
      this.size = lv.size;
   }

   public void put(Object value, int id) {
      int j = Math.max(id, this.size + 1);
      int k;
      if ((float)j >= (float)this.values.length * 0.8F) {
         for(k = this.values.length << 1; k < id; k <<= 1) {
         }

         this.resize(k);
      }

      k = this.findFree(this.getIdealIndex(value));
      this.values[k] = value;
      this.ids[k] = id;
      this.idToValues[id] = value;
      ++this.size;
      if (id == this.nextId) {
         ++this.nextId;
      }

   }

   private int getIdealIndex(@Nullable Object value) {
      return (MathHelper.idealHash(System.identityHashCode(value)) & Integer.MAX_VALUE) % this.values.length;
   }

   private int findIndex(@Nullable Object value, int id) {
      int j;
      for(j = id; j < this.values.length; ++j) {
         if (this.values[j] == value) {
            return j;
         }

         if (this.values[j] == EMPTY) {
            return -1;
         }
      }

      for(j = 0; j < id; ++j) {
         if (this.values[j] == value) {
            return j;
         }

         if (this.values[j] == EMPTY) {
            return -1;
         }
      }

      return -1;
   }

   private int findFree(int size) {
      int j;
      for(j = size; j < this.values.length; ++j) {
         if (this.values[j] == EMPTY) {
            return j;
         }
      }

      for(j = 0; j < size; ++j) {
         if (this.values[j] == EMPTY) {
            return j;
         }
      }

      throw new RuntimeException("Overflowed :(");
   }

   public Iterator iterator() {
      return Iterators.filter(Iterators.forArray(this.idToValues), Predicates.notNull());
   }

   public void clear() {
      Arrays.fill(this.values, (Object)null);
      Arrays.fill(this.idToValues, (Object)null);
      this.nextId = 0;
      this.size = 0;
   }

   public int size() {
      return this.size;
   }

   public Int2ObjectBiMap copy() {
      return new Int2ObjectBiMap((Object[])this.values.clone(), (int[])this.ids.clone(), (Object[])this.idToValues.clone(), this.nextId, this.size);
   }
}
