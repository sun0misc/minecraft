package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;
import org.apache.commons.lang3.ArrayUtils;

public class NbtByteArray extends AbstractNbtList {
   private static final int SIZE = 24;
   public static final NbtType TYPE = new NbtType.OfVariableSize() {
      public NbtByteArray read(DataInput dataInput, int i, NbtTagSizeTracker arg) throws IOException {
         arg.add(24L);
         int j = dataInput.readInt();
         arg.add(1L * (long)j);
         byte[] bs = new byte[j];
         dataInput.readFully(bs);
         return new NbtByteArray(bs);
      }

      public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor) throws IOException {
         int i = input.readInt();
         byte[] bs = new byte[i];
         input.readFully(bs);
         return visitor.visitByteArray(bs);
      }

      public void skip(DataInput input) throws IOException {
         input.skipBytes(input.readInt() * 1);
      }

      public String getCrashReportName() {
         return "BYTE[]";
      }

      public String getCommandFeedbackName() {
         return "TAG_Byte_Array";
      }

      // $FF: synthetic method
      public NbtElement read(DataInput input, int depth, NbtTagSizeTracker tracker) throws IOException {
         return this.read(input, depth, tracker);
      }
   };
   private byte[] value;

   public NbtByteArray(byte[] value) {
      this.value = value;
   }

   public NbtByteArray(List value) {
      this(toArray(value));
   }

   private static byte[] toArray(List list) {
      byte[] bs = new byte[list.size()];

      for(int i = 0; i < list.size(); ++i) {
         Byte byte_ = (Byte)list.get(i);
         bs[i] = byte_ == null ? 0 : byte_;
      }

      return bs;
   }

   public void write(DataOutput output) throws IOException {
      output.writeInt(this.value.length);
      output.write(this.value);
   }

   public int getSizeInBytes() {
      return 24 + 1 * this.value.length;
   }

   public byte getType() {
      return NbtElement.BYTE_ARRAY_TYPE;
   }

   public NbtType getNbtType() {
      return TYPE;
   }

   public String toString() {
      return this.asString();
   }

   public NbtElement copy() {
      byte[] bs = new byte[this.value.length];
      System.arraycopy(this.value, 0, bs, 0, this.value.length);
      return new NbtByteArray(bs);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof NbtByteArray && Arrays.equals(this.value, ((NbtByteArray)o).value);
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.value);
   }

   public void accept(NbtElementVisitor visitor) {
      visitor.visitByteArray(this);
   }

   public byte[] getByteArray() {
      return this.value;
   }

   public int size() {
      return this.value.length;
   }

   public NbtByte get(int i) {
      return NbtByte.of(this.value[i]);
   }

   public NbtByte set(int i, NbtByte arg) {
      byte b = this.value[i];
      this.value[i] = arg.byteValue();
      return NbtByte.of(b);
   }

   public void method_10531(int i, NbtByte arg) {
      this.value = ArrayUtils.add(this.value, i, arg.byteValue());
   }

   public boolean setElement(int index, NbtElement element) {
      if (element instanceof AbstractNbtNumber) {
         this.value[index] = ((AbstractNbtNumber)element).byteValue();
         return true;
      } else {
         return false;
      }
   }

   public boolean addElement(int index, NbtElement element) {
      if (element instanceof AbstractNbtNumber) {
         this.value = ArrayUtils.add(this.value, index, ((AbstractNbtNumber)element).byteValue());
         return true;
      } else {
         return false;
      }
   }

   public NbtByte method_10536(int i) {
      byte b = this.value[i];
      this.value = ArrayUtils.remove(this.value, i);
      return NbtByte.of(b);
   }

   public byte getHeldType() {
      return NbtElement.BYTE_TYPE;
   }

   public void clear() {
      this.value = new byte[0];
   }

   public NbtScanner.Result doAccept(NbtScanner visitor) {
      return visitor.visitByteArray(this.value);
   }

   // $FF: synthetic method
   public NbtElement remove(int i) {
      return this.method_10536(i);
   }

   // $FF: synthetic method
   public void add(int i, NbtElement arg) {
      this.method_10531(i, (NbtByte)arg);
   }

   // $FF: synthetic method
   public NbtElement set(int i, NbtElement arg) {
      return this.set(i, (NbtByte)arg);
   }

   // $FF: synthetic method
   public Object remove(int i) {
      return this.method_10536(i);
   }

   // $FF: synthetic method
   public void add(int i, Object object) {
      this.method_10531(i, (NbtByte)object);
   }

   // $FF: synthetic method
   public Object set(int i, Object object) {
      return this.set(i, (NbtByte)object);
   }

   // $FF: synthetic method
   public Object get(int index) {
      return this.get(index);
   }
}
