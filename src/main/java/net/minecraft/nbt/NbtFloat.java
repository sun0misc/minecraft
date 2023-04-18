package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;
import net.minecraft.util.math.MathHelper;

public class NbtFloat extends AbstractNbtNumber {
   private static final int SIZE = 12;
   public static final NbtFloat ZERO = new NbtFloat(0.0F);
   public static final NbtType TYPE = new NbtType.OfFixedSize() {
      public NbtFloat read(DataInput dataInput, int i, NbtTagSizeTracker arg) throws IOException {
         arg.add(12L);
         return NbtFloat.of(dataInput.readFloat());
      }

      public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor) throws IOException {
         return visitor.visitFloat(input.readFloat());
      }

      public int getSizeInBytes() {
         return 4;
      }

      public String getCrashReportName() {
         return "FLOAT";
      }

      public String getCommandFeedbackName() {
         return "TAG_Float";
      }

      public boolean isImmutable() {
         return true;
      }

      // $FF: synthetic method
      public NbtElement read(DataInput input, int depth, NbtTagSizeTracker tracker) throws IOException {
         return this.read(input, depth, tracker);
      }
   };
   private final float value;

   private NbtFloat(float value) {
      this.value = value;
   }

   public static NbtFloat of(float value) {
      return value == 0.0F ? ZERO : new NbtFloat(value);
   }

   public void write(DataOutput output) throws IOException {
      output.writeFloat(this.value);
   }

   public int getSizeInBytes() {
      return 12;
   }

   public byte getType() {
      return NbtElement.FLOAT_TYPE;
   }

   public NbtType getNbtType() {
      return TYPE;
   }

   public NbtFloat copy() {
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof NbtFloat && this.value == ((NbtFloat)o).value;
      }
   }

   public int hashCode() {
      return Float.floatToIntBits(this.value);
   }

   public void accept(NbtElementVisitor visitor) {
      visitor.visitFloat(this);
   }

   public long longValue() {
      return (long)this.value;
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
      return (double)this.value;
   }

   public float floatValue() {
      return this.value;
   }

   public Number numberValue() {
      return this.value;
   }

   public NbtScanner.Result doAccept(NbtScanner visitor) {
      return visitor.visitFloat(this.value);
   }

   // $FF: synthetic method
   public NbtElement copy() {
      return this.copy();
   }
}
