package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;
import org.apache.commons.lang3.ArrayUtils;

public class NbtIntArray extends AbstractNbtList {
   private static final int SIZE = 24;
   public static final NbtType TYPE = new NbtType.OfVariableSize() {
      public NbtIntArray read(DataInput dataInput, int i, NbtTagSizeTracker arg) throws IOException {
         arg.add(24L);
         int j = dataInput.readInt();
         arg.add(4L * (long)j);
         int[] is = new int[j];

         for(int k = 0; k < j; ++k) {
            is[k] = dataInput.readInt();
         }

         return new NbtIntArray(is);
      }

      public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor) throws IOException {
         int i = input.readInt();
         int[] is = new int[i];

         for(int j = 0; j < i; ++j) {
            is[j] = input.readInt();
         }

         return visitor.visitIntArray(is);
      }

      public void skip(DataInput input) throws IOException {
         input.skipBytes(input.readInt() * 4);
      }

      public String getCrashReportName() {
         return "INT[]";
      }

      public String getCommandFeedbackName() {
         return "TAG_Int_Array";
      }

      // $FF: synthetic method
      public NbtElement read(DataInput input, int depth, NbtTagSizeTracker tracker) throws IOException {
         return this.read(input, depth, tracker);
      }
   };
   private int[] value;

   public NbtIntArray(int[] value) {
      this.value = value;
   }

   public NbtIntArray(List value) {
      this(toArray(value));
   }

   private static int[] toArray(List list) {
      int[] is = new int[list.size()];

      for(int i = 0; i < list.size(); ++i) {
         Integer integer = (Integer)list.get(i);
         is[i] = integer == null ? 0 : integer;
      }

      return is;
   }

   public void write(DataOutput output) throws IOException {
      output.writeInt(this.value.length);
      int[] var2 = this.value;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         int i = var2[var4];
         output.writeInt(i);
      }

   }

   public int getSizeInBytes() {
      return 24 + 4 * this.value.length;
   }

   public byte getType() {
      return NbtElement.INT_ARRAY_TYPE;
   }

   public NbtType getNbtType() {
      return TYPE;
   }

   public String toString() {
      return this.asString();
   }

   public NbtIntArray copy() {
      int[] is = new int[this.value.length];
      System.arraycopy(this.value, 0, is, 0, this.value.length);
      return new NbtIntArray(is);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof NbtIntArray && Arrays.equals(this.value, ((NbtIntArray)o).value);
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.value);
   }

   public int[] getIntArray() {
      return this.value;
   }

   public void accept(NbtElementVisitor visitor) {
      visitor.visitIntArray(this);
   }

   public int size() {
      return this.value.length;
   }

   public NbtInt get(int i) {
      return NbtInt.of(this.value[i]);
   }

   public NbtInt set(int i, NbtInt arg) {
      int j = this.value[i];
      this.value[i] = arg.intValue();
      return NbtInt.of(j);
   }

   public void add(int i, NbtInt arg) {
      this.value = ArrayUtils.add(this.value, i, arg.intValue());
   }

   public boolean setElement(int index, NbtElement element) {
      if (element instanceof AbstractNbtNumber) {
         this.value[index] = ((AbstractNbtNumber)element).intValue();
         return true;
      } else {
         return false;
      }
   }

   public boolean addElement(int index, NbtElement element) {
      if (element instanceof AbstractNbtNumber) {
         this.value = ArrayUtils.add(this.value, index, ((AbstractNbtNumber)element).intValue());
         return true;
      } else {
         return false;
      }
   }

   public NbtInt remove(int i) {
      int j = this.value[i];
      this.value = ArrayUtils.remove(this.value, i);
      return NbtInt.of(j);
   }

   public byte getHeldType() {
      return NbtElement.INT_TYPE;
   }

   public void clear() {
      this.value = new int[0];
   }

   public NbtScanner.Result doAccept(NbtScanner visitor) {
      return visitor.visitIntArray(this.value);
   }

   // $FF: synthetic method
   public NbtElement remove(int i) {
      return this.remove(i);
   }

   // $FF: synthetic method
   public void add(int i, NbtElement arg) {
      this.add(i, (NbtInt)arg);
   }

   // $FF: synthetic method
   public NbtElement set(int i, NbtElement arg) {
      return this.set(i, (NbtInt)arg);
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
      this.add(i, (NbtInt)object);
   }

   // $FF: synthetic method
   public Object set(int i, Object object) {
      return this.set(i, (NbtInt)object);
   }

   // $FF: synthetic method
   public Object get(int index) {
      return this.get(index);
   }
}
