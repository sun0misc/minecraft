package net.minecraft.nbt;

import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.util.FixedBufferInputStream;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.jetbrains.annotations.Nullable;

public class NbtIo {
   public static NbtCompound readCompressed(File file) throws IOException {
      InputStream inputStream = new FileInputStream(file);

      NbtCompound var2;
      try {
         var2 = readCompressed((InputStream)inputStream);
      } catch (Throwable var5) {
         try {
            inputStream.close();
         } catch (Throwable var4) {
            var5.addSuppressed(var4);
         }

         throw var5;
      }

      inputStream.close();
      return var2;
   }

   private static DataInputStream decompress(InputStream stream) throws IOException {
      return new DataInputStream(new FixedBufferInputStream(new GZIPInputStream(stream)));
   }

   public static NbtCompound readCompressed(InputStream stream) throws IOException {
      DataInputStream dataInputStream = decompress(stream);

      NbtCompound var2;
      try {
         var2 = read(dataInputStream, NbtTagSizeTracker.EMPTY);
      } catch (Throwable var5) {
         if (dataInputStream != null) {
            try {
               dataInputStream.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (dataInputStream != null) {
         dataInputStream.close();
      }

      return var2;
   }

   public static void scanCompressed(File file, NbtScanner scanner) throws IOException {
      InputStream inputStream = new FileInputStream(file);

      try {
         scanCompressed((InputStream)inputStream, scanner);
      } catch (Throwable var6) {
         try {
            inputStream.close();
         } catch (Throwable var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }

      inputStream.close();
   }

   public static void scanCompressed(InputStream stream, NbtScanner scanner) throws IOException {
      DataInputStream dataInputStream = decompress(stream);

      try {
         scan(dataInputStream, scanner);
      } catch (Throwable var6) {
         if (dataInputStream != null) {
            try {
               dataInputStream.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (dataInputStream != null) {
         dataInputStream.close();
      }

   }

   public static void writeCompressed(NbtCompound nbt, File file) throws IOException {
      OutputStream outputStream = new FileOutputStream(file);

      try {
         writeCompressed(nbt, (OutputStream)outputStream);
      } catch (Throwable var6) {
         try {
            outputStream.close();
         } catch (Throwable var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }

      outputStream.close();
   }

   public static void writeCompressed(NbtCompound nbt, OutputStream stream) throws IOException {
      DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(stream)));

      try {
         write((NbtCompound)nbt, (DataOutput)dataOutputStream);
      } catch (Throwable var6) {
         try {
            dataOutputStream.close();
         } catch (Throwable var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }

      dataOutputStream.close();
   }

   public static void write(NbtCompound nbt, File file) throws IOException {
      FileOutputStream fileOutputStream = new FileOutputStream(file);

      try {
         DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);

         try {
            write((NbtCompound)nbt, (DataOutput)dataOutputStream);
         } catch (Throwable var8) {
            try {
               dataOutputStream.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }

            throw var8;
         }

         dataOutputStream.close();
      } catch (Throwable var9) {
         try {
            fileOutputStream.close();
         } catch (Throwable var6) {
            var9.addSuppressed(var6);
         }

         throw var9;
      }

      fileOutputStream.close();
   }

   @Nullable
   public static NbtCompound read(File file) throws IOException {
      if (!file.exists()) {
         return null;
      } else {
         FileInputStream fileInputStream = new FileInputStream(file);

         NbtCompound var3;
         try {
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);

            try {
               var3 = read(dataInputStream, NbtTagSizeTracker.EMPTY);
            } catch (Throwable var7) {
               try {
                  dataInputStream.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            dataInputStream.close();
         } catch (Throwable var8) {
            try {
               fileInputStream.close();
            } catch (Throwable var5) {
               var8.addSuppressed(var5);
            }

            throw var8;
         }

         fileInputStream.close();
         return var3;
      }
   }

   public static NbtCompound read(DataInput input) throws IOException {
      return read(input, NbtTagSizeTracker.EMPTY);
   }

   public static NbtCompound read(DataInput input, NbtTagSizeTracker tracker) throws IOException {
      NbtElement lv = read(input, 0, tracker);
      if (lv instanceof NbtCompound) {
         return (NbtCompound)lv;
      } else {
         throw new IOException("Root tag must be a named compound tag");
      }
   }

   public static void write(NbtCompound nbt, DataOutput output) throws IOException {
      write((NbtElement)nbt, (DataOutput)output);
   }

   public static void scan(DataInput input, NbtScanner scanner) throws IOException {
      NbtType lv = NbtTypes.byId(input.readByte());
      if (lv == NbtEnd.TYPE) {
         if (scanner.start(NbtEnd.TYPE) == NbtScanner.Result.CONTINUE) {
            scanner.visitEnd();
         }

      } else {
         switch (scanner.start(lv)) {
            case HALT:
            default:
               break;
            case BREAK:
               NbtString.skip(input);
               lv.skip(input);
               break;
            case CONTINUE:
               NbtString.skip(input);
               lv.doAccept(input, scanner);
         }

      }
   }

   public static void write(NbtElement nbt, DataOutput output) throws IOException {
      output.writeByte(nbt.getType());
      if (nbt.getType() != 0) {
         output.writeUTF("");
         nbt.write(output);
      }
   }

   private static NbtElement read(DataInput input, int depth, NbtTagSizeTracker tracker) throws IOException {
      byte b = input.readByte();
      if (b == 0) {
         return NbtEnd.INSTANCE;
      } else {
         NbtString.skip(input);

         try {
            return NbtTypes.byId(b).read(input, depth, tracker);
         } catch (IOException var7) {
            CrashReport lv = CrashReport.create(var7, "Loading NBT data");
            CrashReportSection lv2 = lv.addElement("NBT Tag");
            lv2.add("Tag type", (Object)b);
            throw new CrashException(lv);
         }
      }
   }
}
