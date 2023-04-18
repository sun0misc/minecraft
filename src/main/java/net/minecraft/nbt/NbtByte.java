package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;

public class NbtByte extends AbstractNbtNumber {
   private static final int SIZE = 9;
   public static final NbtType TYPE = new NbtType.OfFixedSize() {
      public NbtByte read(DataInput dataInput, int i, NbtTagSizeTracker arg) throws IOException {
         arg.add(9L);
         return NbtByte.of(dataInput.readByte());
      }

      public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor) throws IOException {
         return visitor.visitByte(input.readByte());
      }

      public int getSizeInBytes() {
         return 1;
      }

      public String getCrashReportName() {
         return "BYTE";
      }

      public String getCommandFeedbackName() {
         return "TAG_Byte";
      }

      public boolean isImmutable() {
         return true;
      }

      // $FF: synthetic method
      public NbtElement read(DataInput input, int depth, NbtTagSizeTracker tracker) throws IOException {
         return this.read(input, depth, tracker);
      }
   };
   public static final NbtByte ZERO = of((byte)0);
   public static final NbtByte ONE = of((byte)1);
   private final byte value;

   NbtByte(byte value) {
      this.value = value;
   }

   public static NbtByte of(byte value) {
      return NbtByte.Cache.VALUES[128 + value];
   }

   public static NbtByte of(boolean value) {
      return value ? ONE : ZERO;
   }

   public void write(DataOutput output) throws IOException {
      output.writeByte(this.value);
   }

   public int getSizeInBytes() {
      return 9;
   }

   public byte getType() {
      return NbtElement.BYTE_TYPE;
   }

   public NbtType getNbtType() {
      return TYPE;
   }

   public NbtByte copy() {
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof NbtByte && this.value == ((NbtByte)o).value;
      }
   }

   public int hashCode() {
      return this.value;
   }

   public void accept(NbtElementVisitor visitor) {
      visitor.visitByte(this);
   }

   public long longValue() {
      return (long)this.value;
   }

   public int intValue() {
      return this.value;
   }

   public short shortValue() {
      return (short)this.value;
   }

   public byte byteValue() {
      return this.value;
   }

   public double doubleValue() {
      return (double)this.value;
   }

   public float floatValue() {
      return (float)this.value;
   }

   public Number numberValue() {
      return this.value;
   }

   public NbtScanner.Result doAccept(NbtScanner visitor) {
      return visitor.visitByte(this.value);
   }

   // $FF: synthetic method
   public NbtElement copy() {
      return this.copy();
   }

   private static class Cache {
      static final NbtByte[] VALUES = new NbtByte[256];

      static {
         for(int i = 0; i < VALUES.length; ++i) {
            VALUES[i] = new NbtByte((byte)(i - 128));
         }

      }
   }
}
