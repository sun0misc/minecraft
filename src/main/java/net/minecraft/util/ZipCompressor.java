package net.minecraft.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;

public class ZipCompressor implements Closeable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Path file;
   private final Path temp;
   private final FileSystem zip;

   public ZipCompressor(Path file) {
      this.file = file;
      this.temp = file.resolveSibling(file.getFileName().toString() + "_tmp");

      try {
         this.zip = Util.JAR_FILE_SYSTEM_PROVIDER.newFileSystem(this.temp, ImmutableMap.of("create", "true"));
      } catch (IOException var3) {
         throw new UncheckedIOException(var3);
      }
   }

   public void write(Path target, String content) {
      try {
         Path path2 = this.zip.getPath(File.separator);
         Path path3 = path2.resolve(target.toString());
         Files.createDirectories(path3.getParent());
         Files.write(path3, content.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
      } catch (IOException var5) {
         throw new UncheckedIOException(var5);
      }
   }

   public void copy(Path target, File source) {
      try {
         Path path2 = this.zip.getPath(File.separator);
         Path path3 = path2.resolve(target.toString());
         Files.createDirectories(path3.getParent());
         Files.copy(source.toPath(), path3);
      } catch (IOException var5) {
         throw new UncheckedIOException(var5);
      }
   }

   public void copyAll(Path source) {
      try {
         Path path2 = this.zip.getPath(File.separator);
         if (Files.isRegularFile(source, new LinkOption[0])) {
            Path path3 = path2.resolve(source.getParent().relativize(source).toString());
            Files.copy(path3, source);
         } else {
            Stream stream = Files.find(source, Integer.MAX_VALUE, (path, attributes) -> {
               return attributes.isRegularFile();
            }, new FileVisitOption[0]);

            try {
               Iterator var4 = ((List)stream.collect(Collectors.toList())).iterator();

               while(var4.hasNext()) {
                  Path path4 = (Path)var4.next();
                  Path path5 = path2.resolve(source.relativize(path4).toString());
                  Files.createDirectories(path5.getParent());
                  Files.copy(path4, path5);
               }
            } catch (Throwable var8) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (stream != null) {
               stream.close();
            }

         }
      } catch (IOException var9) {
         throw new UncheckedIOException(var9);
      }
   }

   public void close() {
      try {
         this.zip.close();
         Files.move(this.temp, this.file);
         LOGGER.info("Compressed to {}", this.file);
      } catch (IOException var2) {
         throw new UncheckedIOException(var2);
      }
   }
}
