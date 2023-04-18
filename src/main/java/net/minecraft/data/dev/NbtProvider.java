package net.minecraft.data.dev;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class NbtProvider implements DataProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Iterable paths;
   private final DataOutput output;

   public NbtProvider(DataOutput output, Collection paths) {
      this.paths = paths;
      this.output = output;
   }

   public CompletableFuture run(DataWriter writer) {
      Path path = this.output.getPath();
      List list = new ArrayList();
      Iterator var4 = this.paths.iterator();

      while(var4.hasNext()) {
         Path path2 = (Path)var4.next();
         list.add(CompletableFuture.supplyAsync(() -> {
            try {
               Stream stream = Files.walk(path2);

               CompletableFuture var4;
               try {
                  var4 = CompletableFuture.allOf((CompletableFuture[])stream.filter((pathx) -> {
                     return pathx.toString().endsWith(".nbt");
                  }).map((pathx) -> {
                     return CompletableFuture.runAsync(() -> {
                        convertNbtToSnbt(writer, pathx, getLocation(path2, pathx), path);
                     }, Util.getIoWorkerExecutor());
                  }).toArray((i) -> {
                     return new CompletableFuture[i];
                  }));
               } catch (Throwable var7) {
                  if (stream != null) {
                     try {
                        stream.close();
                     } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                     }
                  }

                  throw var7;
               }

               if (stream != null) {
                  stream.close();
               }

               return var4;
            } catch (IOException var8) {
               LOGGER.error("Failed to read structure input directory", var8);
               return CompletableFuture.completedFuture((Object)null);
            }
         }, Util.getMainWorkerExecutor()).thenCompose((future) -> {
            return future;
         }));
      }

      return CompletableFuture.allOf((CompletableFuture[])list.toArray((i) -> {
         return new CompletableFuture[i];
      }));
   }

   public final String getName() {
      return "NBT -> SNBT";
   }

   private static String getLocation(Path inputPath, Path filePath) {
      String string = inputPath.relativize(filePath).toString().replaceAll("\\\\", "/");
      return string.substring(0, string.length() - ".nbt".length());
   }

   @Nullable
   public static Path convertNbtToSnbt(DataWriter writer, Path inputPath, String filename, Path outputPath) {
      try {
         InputStream inputStream = Files.newInputStream(inputPath);

         Path var6;
         try {
            Path path3 = outputPath.resolve(filename + ".snbt");
            writeTo(writer, path3, NbtHelper.toNbtProviderString(NbtIo.readCompressed(inputStream)));
            LOGGER.info("Converted {} from NBT to SNBT", filename);
            var6 = path3;
         } catch (Throwable var8) {
            if (inputStream != null) {
               try {
                  inputStream.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (inputStream != null) {
            inputStream.close();
         }

         return var6;
      } catch (IOException var9) {
         LOGGER.error("Couldn't convert {} from NBT to SNBT at {}", new Object[]{filename, inputPath, var9});
         return null;
      }
   }

   public static void writeTo(DataWriter writer, Path path, String content) throws IOException {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), byteArrayOutputStream);
      hashingOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
      hashingOutputStream.write(10);
      writer.write(path, byteArrayOutputStream.toByteArray(), hashingOutputStream.hash());
   }
}
