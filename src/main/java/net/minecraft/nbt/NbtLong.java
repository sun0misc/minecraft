package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;

public class NbtLong extends AbstractNbtNumber {
   private static final int SIZE = 16;
   public static final NbtType TYPE = new NbtType.OfFixedSize() {
      public NbtLong read(DataInput dataInput, int i, NbtTagSizeTracker arg) throws IOException {
         arg.add(16L);
         return NbtLong.of(dataInput.readLong());
      }

      public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor) throws IOException {
         return visitor.visitLong(input.readLong());
      }

      public int getSizeInBytes() {
         return 8;
      }

      public String getCrashReportName() {
         return "LONG";
      }

      public String getCommandFeedbackName() {
         return "TAG_Long";
      }

      public boolean isImmutable() {
         return true;
      }

      // $FF: synthetic method
      public NbtElement read(DataInput input, int depth, NbtTagSizeTracker tracker) throws IOException {
         return this.read(input, depth, tracker);
      }
   };
   private final long value;

   NbtLong(long value) {
      this.value = value;
   }

   public static NbtLong of(long value) {
      return value >= -128L && value <= 1024L ? NbtLong.Cache.VALUES[(int)value - -128] : new NbtLong(value);
   }

   public void write(DataOutput output) throws IOException {
      output.writeLong(this.value);
   }

   public int getSizeInBytes() {
      return 16;
   }

   public byte getType() {
      return NbtElement.LONG_TYPE;
   }

   public NbtType getNbtType() {
      return TYPE;
   }

   public NbtLong copy() {
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof NbtLong && this.value == ((NbtLong)o).value;
      }
   }

   public int hashCode() {
      return (int)(this.value ^ this.value >>> 32);
   }

   public void accept(NbtElementVisitor visitor) {
      visitor.visitLong(this);
   }

   public long longValue() {
      return this.value;
   }

   public int intValue() {
      return (int)(this.value & -1L);
   }

   public short shortValue() {
      return (short)((int)(this.value & 65535L));
   }

   public byte byteValue() {
      return (byte)((int)(this.value & 255L));
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
      return visitor.visitLong(this.value);
   }

   // $FF: synthetic method
   public NbtElement copy() {
      return this.copy();
   }

   static class Cache {
      private static final int MAX = 1024;
      private static final int MIN = -128;
      static final NbtLong[] VALUES = new NbtLong[1153];

      private Cache() {
      }

      static {
         for(int i = 0; i < VALUES.length; ++i) {
            VALUES[i] = new NbtLong((long)(-128 + i));
         }

      }
   }
}
