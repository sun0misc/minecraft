package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;

public class NbtInt extends AbstractNbtNumber {
   private static final int SIZE = 12;
   public static final NbtType TYPE = new NbtType.OfFixedSize() {
      public NbtInt read(DataInput dataInput, int i, NbtTagSizeTracker arg) throws IOException {
         arg.add(12L);
         return NbtInt.of(dataInput.readInt());
      }

      public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor) throws IOException {
         return visitor.visitInt(input.readInt());
      }

      public int getSizeInBytes() {
         return 4;
      }

      public String getCrashReportName() {
         return "INT";
      }

      public String getCommandFeedbackName() {
         return "TAG_Int";
      }

      public boolean isImmutable() {
         return true;
      }

      // $FF: synthetic method
      public NbtElement read(DataInput input, int depth, NbtTagSizeTracker tracker) throws IOException {
         return this.read(input, depth, tracker);
      }
   };
   private final int value;

   NbtInt(int value) {
      this.value = value;
   }

   public static NbtInt of(int value) {
      return value >= -128 && value <= 1024 ? NbtInt.Cache.VALUES[value - -128] : new NbtInt(value);
   }

   public void write(DataOutput output) throws IOException {
      output.writeInt(this.value);
   }

   public int getSizeInBytes() {
      return 12;
   }

   public byte getType() {
      return NbtElement.INT_TYPE;
   }

   public NbtType getNbtType() {
      return TYPE;
   }

   public NbtInt copy() {
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof NbtInt && this.value == ((NbtInt)o).value;
      }
   }

   public int hashCode() {
      return this.value;
   }

   public void accept(NbtElementVisitor visitor) {
      visitor.visitInt(this);
   }

   public long longValue() {
      return (long)this.value;
   }

   public int intValue() {
      return this.value;
   }

   public short shortValue() {
      return (short)(this.value & '\uffff');
   }

   public byte byteValue() {
      return (byte)(this.value & 255);
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
      return visitor.visitInt(this.value);
   }

   // $FF: synthetic method
   public NbtElement copy() {
      return this.copy();
   }

   static class Cache {
      private static final int MAX = 1024;
      private static final int MIN = -128;
      static final NbtInt[] VALUES = new NbtInt[1153];

      private Cache() {
      }

      static {
         for(int i = 0; i < VALUES.length; ++i) {
            VALUES[i] = new NbtInt(-128 + i);
         }

      }
   }
}
