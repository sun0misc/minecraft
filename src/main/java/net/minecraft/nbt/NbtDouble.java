package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;
import net.minecraft.util.math.MathHelper;

public class NbtDouble extends AbstractNbtNumber {
   private static final int SIZE = 16;
   public static final NbtDouble ZERO = new NbtDouble(0.0);
   public static final NbtType TYPE = new NbtType.OfFixedSize() {
      public NbtDouble read(DataInput dataInput, int i, NbtTagSizeTracker arg) throws IOException {
         arg.add(16L);
         return NbtDouble.of(dataInput.readDouble());
      }

      public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor) throws IOException {
         return visitor.visitDouble(input.readDouble());
      }

      public int getSizeInBytes() {
         return 8;
      }

      public String getCrashReportName() {
         return "DOUBLE";
      }

      public String getCommandFeedbackName() {
         return "TAG_Double";
      }

      public boolean isImmutable() {
         return true;
      }

      // $FF: synthetic method
      public NbtElement read(DataInput input, int depth, NbtTagSizeTracker tracker) throws IOException {
         return this.read(input, depth, tracker);
      }
   };
   private final double value;

   private NbtDouble(double value) {
      this.value = value;
   }

   public static NbtDouble of(double value) {
      return value == 0.0 ? ZERO : new NbtDouble(value);
   }

   public void write(DataOutput output) throws IOException {
      output.writeDouble(this.value);
   }

   public int getSizeInBytes() {
      return 16;
   }

   public byte getType() {
      return NbtElement.DOUBLE_TYPE;
   }

   public NbtType getNbtType() {
      return TYPE;
   }

   public NbtDouble copy() {
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof NbtDouble && this.value == ((NbtDouble)o).value;
      }
   }

   public int hashCode() {
      long l = Double.doubleToLongBits(this.value);
      return (int)(l ^ l >>> 32);
   }

   public void accept(NbtElementVisitor visitor) {
      visitor.visitDouble(this);
   }

   public long longValue() {
      return (long)Math.floor(this.value);
   }

   public int intValue() {
      return MathHelper.floor(this.value);
   }

   public short shortValue() {
      return (short)(MathHelper.floor(this.value) & '\uffff');
   }

   public byte byteValue() {
      return (byte)(MathHelper.floor(this.value) & 255);
   }

   public double doubleValue() {
      return this.value;
   }

   public float floatValue() {
      return (float)this.value;
   }

   public Number numberValue() {
      return this.value;
   }

   public NbtScanner.Result doAccept(NbtScanner visitor) {
      return visitor.visitDouble(this.value);
   }

   // $FF: synthetic method
   public NbtElement copy() {
      return this.copy();
   }
}
