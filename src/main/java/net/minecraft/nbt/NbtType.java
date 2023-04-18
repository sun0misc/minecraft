package net.minecraft.nbt;

import java.io.DataInput;
import java.io.IOException;
import net.minecraft.nbt.scanner.NbtScanner;

public interface NbtType {
   NbtElement read(DataInput input, int depth, NbtTagSizeTracker tracker) throws IOException;

   NbtScanner.Result doAccept(DataInput input, NbtScanner visitor) throws IOException;

   default void accept(DataInput input, NbtScanner visitor) throws IOException {
      switch (visitor.start(this)) {
         case CONTINUE:
            this.doAccept(input, visitor);
         case HALT:
         default:
            break;
         case BREAK:
            this.skip(input);
      }

   }

   void skip(DataInput input, int count) throws IOException;

   void skip(DataInput input) throws IOException;

   default boolean isImmutable() {
      return false;
   }

   String getCrashReportName();

   String getCommandFeedbackName();

   static NbtType createInvalid(final int type) {
      return new NbtType() {
         private IOException createException() {
            return new IOException("Invalid tag id: " + type);
         }

         public NbtEnd read(DataInput dataInput, int i, NbtTagSizeTracker arg) throws IOException {
            throw this.createException();
         }

         public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor) throws IOException {
            throw this.createException();
         }

         public void skip(DataInput input, int count) throws IOException {
            throw this.createException();
         }

         public void skip(DataInput input) throws IOException {
            throw this.createException();
         }

         public String getCrashReportName() {
            return "INVALID[" + type + "]";
         }

         public String getCommandFeedbackName() {
            return "UNKNOWN_" + type;
         }

         // $FF: synthetic method
         public NbtElement read(DataInput input, int depth, NbtTagSizeTracker tracker) throws IOException {
            return this.read(input, depth, tracker);
         }
      };
   }

   public interface OfVariableSize extends NbtType {
      default void skip(DataInput input, int count) throws IOException {
         for(int j = 0; j < count; ++j) {
            this.skip(input);
         }

      }
   }

   public interface OfFixedSize extends NbtType {
      default void skip(DataInput input) throws IOException {
         input.skipBytes(this.getSizeInBytes());
      }

      default void skip(DataInput input, int count) throws IOException {
         input.skipBytes(this.getSizeInBytes() * count);
      }

      int getSizeInBytes();
   }
}
