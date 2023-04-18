package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;

public class NbtEnd implements NbtElement {
   private static final int SIZE = 8;
   public static final NbtType TYPE = new NbtType() {
      public NbtEnd read(DataInput dataInput, int i, NbtTagSizeTracker arg) {
         arg.add(8L);
         return NbtEnd.INSTANCE;
      }

      public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor) {
         return visitor.visitEnd();
      }

      public void skip(DataInput input, int count) {
      }

      public void skip(DataInput input) {
      }

      public String getCrashReportName() {
         return "END";
      }

      public String getCommandFeedbackName() {
         return "TAG_End";
      }

      public boolean isImmutable() {
         return true;
      }

      // $FF: synthetic method
      public NbtElement read(DataInput input, int depth, NbtTagSizeTracker tracker) throws IOException {
         return this.read(input, depth, tracker);
      }
   };
   public static final NbtEnd INSTANCE = new NbtEnd();

   private NbtEnd() {
   }

   public void write(DataOutput output) throws IOException {
   }

   public int getSizeInBytes() {
      return 8;
   }

   public byte getType() {
      return NbtElement.END_TYPE;
   }

   public NbtType getNbtType() {
      return TYPE;
   }

   public String toString() {
      return this.asString();
   }

   public NbtEnd copy() {
      return this;
   }

   public void accept(NbtElementVisitor visitor) {
      visitor.visitEnd(this);
   }

   public NbtScanner.Result doAccept(NbtScanner visitor) {
      return visitor.visitEnd();
   }

   // $FF: synthetic method
   public NbtElement copy() {
      return this.copy();
   }
}
