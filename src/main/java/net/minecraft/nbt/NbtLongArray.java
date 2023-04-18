package net.minecraft.nbt;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;
import org.apache.commons.lang3.ArrayUtils;

public class NbtLongArray extends AbstractNbtList {
   private static final int SIZE = 24;
   public static final NbtType TYPE = new NbtType.OfVariableSize() {
      public NbtLongArray read(DataInput dataInput, int i, NbtTagSizeTracker arg) throws IOException {
         arg.add(24L);
         int j = dataInput.readInt();
         arg.add(8L * (long)j);
         long[] ls = new long[j];

         for(int k = 0; k < j; ++k) {
            ls[k] = dataInput.readLong();
         }

         return new NbtLongArray(ls);
      }

      public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor) throws IOException {
         int i = input.readInt();
         long[] ls = new long[i];

         for(int j = 0; j < i; ++j) {
            ls[j] = input.readLong();
         }

         return visitor.visitLongArray(ls);
      }

      public void skip(DataInput input) throws IOException {
         input.skipBytes(input.readInt() * 8);
      }

      public String getCrashReportName() {
         return "LONG[]";
      }

      public String getCommandFeedbackName() {
         return "TAG_Long_Array";
      }

      // $FF: synthetic method
      public NbtElement read(DataInput input, int depth, NbtTagSizeTracker tracker) throws IOException {
         return this.read(input, depth, tracker);
      }
   };
   private long[] value;

   public NbtLongArray(long[] value) {
      this.value = value;
   }

   public NbtLongArray(LongSet value) {
      this.value = value.toLongArray();
   }

   public NbtLongArray(List value) {
      this(toArray(value));
   }

   private static long[] toArray(List list) {
      long[] ls = new long[list.size()];

      for(int i = 0; i < list.size(); ++i) {
         Long long_ = (Long)list.get(i);
         ls[i] = long_ == null ? 0L : long_;
      }

      return ls;
   }

   public void write(DataOutput output) throws IOException {
      output.writeInt(this.value.length);
      long[] var2 = this.value;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         long l = var2[var4];
         output.writeLong(l);
      }

   }

   public int getSizeInBytes() {
      return 24 + 8 * this.value.length;
   }

   public byte getType() {
      return NbtElement.LONG_ARRAY_TYPE;
   }

   public NbtType getNbtType() {
      return TYPE;
   }

   public String toString() {
      return this.asString();
   }

   public NbtLongArray copy() {
      long[] ls = new long[this.value.length];
      System.arraycopy(this.value, 0, ls, 0, this.value.length);
      return new NbtLongArray(ls);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof NbtLongArray && Arrays.equals(this.value, ((NbtLongArray)o).value);
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.value);
   }

   public void accept(NbtElementVisitor visitor) {
      visitor.visitLongArray(this);
   }

   public long[] getLongArray() {
      return this.value;
   }

   public int size() {
      return this.value.length;
   }

   public NbtLong get(int i) {
      return NbtLong.of(this.value[i]);
   }

   public NbtLong method_10606(int i, NbtLong arg) {
      long l = this.value[i];
      this.value[i] = arg.longValue();
      return NbtLong.of(l);
   }

   public void add(int i, NbtLong arg) {
      this.value = ArrayUtils.add(this.value, i, arg.longValue());
   }

   public boolean setElement(int index, NbtElement element) {
      if (element instanceof AbstractNbtNumber) {
         this.value[index] = ((AbstractNbtNumber)element).longValue();
         return true;
      } else {
         return false;
      }
   }

   public boolean addElement(int index, NbtElement element) {
      if (element instanceof AbstractNbtNumber) {
         this.value = ArrayUtils.add(this.value, index, ((AbstractNbtNumber)element).longValue());
         return true;
      } else {
         return false;
      }
   }

   public NbtLong remove(int i) {
      long l = this.value[i];
      this.value = ArrayUtils.remove(this.value, i);
      return NbtLong.of(l);
   }

   public byte getHeldType() {
      return NbtElement.LONG_TYPE;
   }

   public void clear() {
      this.value = new long[0];
   }

   public NbtScanner.Result doAccept(NbtScanner visitor) {
      return visitor.visitLongArray(this.value);
   }

   // $FF: synthetic method
   public NbtElement remove(int i) {
      return this.remove(i);
   }

   // $FF: synthetic method
   public void add(int i, NbtElement arg) {
      this.add(i, (NbtLong)arg);
   }

   // $FF: synthetic method
   public NbtElement set(int i, NbtElement arg) {
      return this.method_10606(i, (NbtLong)arg);
   }

   // $FF: synthetic method
   public NbtElement copy() {
      return this.copy();
   }

   // $FF: synthetic method
   public Object remove(int i) {
      return this.remove(i);
   }

   // $FF: synthetic method
   public void add(int i, Object object) {
      this.add(i, (NbtLong)object);
   }

   // $FF: synthetic method
   public Object set(int i, Object object) {
      return this.method_10606(i, (NbtLong)object);
   }

   // $FF: synthetic method
   public Object get(int index) {
      return this.get(index);
   }
}
