package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.util.Objects;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;
import net.minecraft.util.Util;

public class NbtString implements NbtElement {
   private static final int SIZE = 36;
   public static final NbtType TYPE = new NbtType.OfVariableSize() {
      public NbtString read(DataInput dataInput, int i, NbtTagSizeTracker arg) throws IOException {
         arg.add(36L);
         String string = dataInput.readUTF();
         arg.add((long)(2 * string.length()));
         return NbtString.of(string);
      }

      public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor) throws IOException {
         return visitor.visitString(input.readUTF());
      }

      public void skip(DataInput input) throws IOException {
         NbtString.skip(input);
      }

      public String getCrashReportName() {
         return "STRING";
      }

      public String getCommandFeedbackName() {
         return "TAG_String";
      }

      public boolean isImmutable() {
         return true;
      }

      // $FF: synthetic method
      public NbtElement read(DataInput input, int depth, NbtTagSizeTracker tracker) throws IOException {
         return this.read(input, depth, tracker);
      }
   };
   private static final NbtString EMPTY = new NbtString("");
   private static final char DOUBLE_QUOTE = '"';
   private static final char SINGLE_QUOTE = '\'';
   private static final char BACKSLASH = '\\';
   private static final char NULL = '\u0000';
   private final String value;

   public static void skip(DataInput input) throws IOException {
      input.skipBytes(input.readUnsignedShort());
   }

   private NbtString(String value) {
      Objects.requireNonNull(value, "Null string not allowed");
      this.value = value;
   }

   public static NbtString of(String value) {
      return value.isEmpty() ? EMPTY : new NbtString(value);
   }

   public void write(DataOutput output) throws IOException {
      try {
         output.writeUTF(this.value);
      } catch (UTFDataFormatException var3) {
         Util.error("Failed to write NBT String", var3);
         output.writeUTF("");
      }

   }

   public int getSizeInBytes() {
      return 36 + 2 * this.value.length();
   }

   public byte getType() {
      return NbtElement.STRING_TYPE;
   }

   public NbtType getNbtType() {
      return TYPE;
   }

   public String toString() {
      return NbtElement.super.asString();
   }

   public NbtString copy() {
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof NbtString && Objects.equals(this.value, ((NbtString)o).value);
      }
   }

   public int hashCode() {
      return this.value.hashCode();
   }

   public String asString() {
      return this.value;
   }

   public void accept(NbtElementVisitor visitor) {
      visitor.visitString(this);
   }

   public static String escape(String value) {
      StringBuilder stringBuilder = new StringBuilder(" ");
      char c = 0;

      for(int i = 0; i < value.length(); ++i) {
         char d = value.charAt(i);
         if (d == '\\') {
            stringBuilder.append('\\');
         } else if (d == '"' || d == '\'') {
            if (c == 0) {
               c = d == '"' ? 39 : 34;
            }

            if (c == d) {
               stringBuilder.append('\\');
            }
         }

         stringBuilder.append(d);
      }

      if (c == 0) {
         c = 34;
      }

      stringBuilder.setCharAt(0, (char)c);
      stringBuilder.append((char)c);
      return stringBuilder.toString();
   }

   public NbtScanner.Result doAccept(NbtScanner visitor) {
      return visitor.visitString(this.value);
   }

   // $FF: synthetic method
   public NbtElement copy() {
      return this.copy();
   }
}
