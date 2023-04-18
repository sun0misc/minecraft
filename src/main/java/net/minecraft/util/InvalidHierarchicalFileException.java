package net.minecraft.util;

import com.google.common.collect.Lists;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class InvalidHierarchicalFileException extends IOException {
   private final List invalidFiles = Lists.newArrayList();
   private final String message;

   public InvalidHierarchicalFileException(String message) {
      this.invalidFiles.add(new File());
      this.message = message;
   }

   public InvalidHierarchicalFileException(String message, Throwable cause) {
      super(cause);
      this.invalidFiles.add(new File());
      this.message = message;
   }

   public void addInvalidKey(String key) {
      ((File)this.invalidFiles.get(0)).addKey(key);
   }

   public void addInvalidFile(String fileName) {
      ((File)this.invalidFiles.get(0)).name = fileName;
      this.invalidFiles.add(0, new File());
   }

   public String getMessage() {
      Object var10000 = this.invalidFiles.get(this.invalidFiles.size() - 1);
      return "Invalid " + var10000 + ": " + this.message;
   }

   public static InvalidHierarchicalFileException wrap(Exception cause) {
      if (cause instanceof InvalidHierarchicalFileException) {
         return (InvalidHierarchicalFileException)cause;
      } else {
         String string = cause.getMessage();
         if (cause instanceof FileNotFoundException) {
            string = "File not found";
         }

         return new InvalidHierarchicalFileException(string, cause);
      }
   }

   public static class File {
      @Nullable
      String name;
      private final List keys = Lists.newArrayList();

      File() {
      }

      void addKey(String key) {
         this.keys.add(0, key);
      }

      @Nullable
      public String getName() {
         return this.name;
      }

      public String joinKeys() {
         return StringUtils.join(this.keys, "->");
      }

      public String toString() {
         if (this.name != null) {
            if (this.keys.isEmpty()) {
               return this.name;
            } else {
               String var10000 = this.name;
               return var10000 + " " + this.joinKeys();
            }
         } else {
            return this.keys.isEmpty() ? "(Unknown file)" : "(Unknown file) " + this.joinKeys();
         }
      }
   }
}
