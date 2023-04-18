package net.minecraft.data;

import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.data.dev.NbtProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Util;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SnbtProvider implements DataProvider {
   @Nullable
   private static final Path DEBUG_OUTPUT_DIRECTORY = null;
   private static final Logger LOGGER = LogUtils.getLogger();
   private final DataOutput output;
   private final Iterable paths;
   private final List write = Lists.newArrayList();

   public SnbtProvider(DataOutput output, Iterable paths) {
      this.output = output;
      this.paths = paths;
   }

   public SnbtProvider addWriter(Tweaker tweaker) {
      this.write.add(tweaker);
      return this;
   }

   private NbtCompound write(String key, NbtCompound compound) {
      NbtCompound lv = compound;

      Tweaker lv2;
      for(Iterator var4 = this.write.iterator(); var4.hasNext(); lv = lv2.write(key, lv)) {
         lv2 = (Tweaker)var4.next();
      }

      return lv;
   }

   public CompletableFuture run(DataWriter writer) {
      Path path = this.output.getPath();
      List list = Lists.newArrayList();
      Iterator var4 = this.paths.iterator();

      while(var4.hasNext()) {
         Path path2 = (Path)var4.next();
         list.add(CompletableFuture.supplyAsync(() -> {
            try {
               Stream stream = Files.walk(path2);

               CompletableFuture var5;
               try {
                  var5 = CompletableFuture.allOf((CompletableFuture[])stream.filter((pathx) -> {
                     return pathx.toString().endsWith(".snbt");
                  }).map((pathx) -> {
                     return CompletableFuture.runAsync(() -> {
                        CompressedData lv = this.toCompressedNbt(pathx, this.getFileName(path2, pathx));
                        this.write(writer, lv, path);
                     }, Util.getMainWorkerExecutor());
                  }).toArray((i) -> {
                     return new CompletableFuture[i];
                  }));
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

               return var5;
            } catch (Exception var9) {
               throw new RuntimeException("Failed to read structure input directory, aborting", var9);
            }
         }, Util.getMainWorkerExecutor()).thenCompose((future) -> {
            return future;
         }));
      }

      return Util.combine(list);
   }

   public final String getName() {
      return "SNBT -> NBT";
   }

   private String getFileName(Path root, Path file) {
      String string = root.relativize(file).toString().replaceAll("\\\\", "/");
      return string.substring(0, string.length() - ".snbt".length());
   }

   private CompressedData toCompressedNbt(Path path, String name) {
      try {
         BufferedReader bufferedReader = Files.newBufferedReader(path);

         CompressedData var11;
         try {
            String string2 = IOUtils.toString(bufferedReader);
            NbtCompound lv = this.write(name, NbtHelper.fromNbtProviderString(string2));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), byteArrayOutputStream);
            NbtIo.writeCompressed(lv, (OutputStream)hashingOutputStream);
            byte[] bs = byteArrayOutputStream.toByteArray();
            HashCode hashCode = hashingOutputStream.hash();
            String string3;
            if (DEBUG_OUTPUT_DIRECTORY != null) {
               string3 = NbtHelper.toNbtProviderString(lv);
            } else {
               string3 = null;
            }

            var11 = new CompressedData(name, bs, string3, hashCode);
         } catch (Throwable var13) {
            if (bufferedReader != null) {
               try {
                  bufferedReader.close();
               } catch (Throwable var12) {
                  var13.addSuppressed(var12);
               }
            }

            throw var13;
         }

         if (bufferedReader != null) {
            bufferedReader.close();
         }

         return var11;
      } catch (Throwable var14) {
         throw new CompressionException(path, var14);
      }
   }

   private void write(DataWriter cache, CompressedData data, Path root) {
      Path path2;
      if (data.snbtContent != null) {
         path2 = DEBUG_OUTPUT_DIRECTORY.resolve(data.name + ".snbt");

         try {
            NbtProvider.writeTo(DataWriter.UNCACHED, path2, data.snbtContent);
         } catch (IOException var7) {
            LOGGER.error("Couldn't write structure SNBT {} at {}", new Object[]{data.name, path2, var7});
         }
      }

      path2 = root.resolve(data.name + ".nbt");

      try {
         cache.write(path2, data.bytes, data.sha1);
      } catch (IOException var6) {
         LOGGER.error("Couldn't write structure {} at {}", new Object[]{data.name, path2, var6});
      }

   }

   @FunctionalInterface
   public interface Tweaker {
      NbtCompound write(String name, NbtCompound nbt);
   }

   private static record CompressedData(String name, byte[] bytes, @Nullable String snbtContent, HashCode sha1) {
      final String name;
      final byte[] bytes;
      @Nullable
      final String snbtContent;
      final HashCode sha1;

      CompressedData(String name, byte[] bytes, @Nullable String snbtContent, HashCode hashCode) {
         this.name = name;
         this.bytes = bytes;
         this.snbtContent = snbtContent;
         this.sha1 = hashCode;
      }

      public String name() {
         return this.name;
      }

      public byte[] bytes() {
         return this.bytes;
      }

      @Nullable
      public String snbtContent() {
         return this.snbtContent;
      }

      public HashCode sha1() {
         return this.sha1;
      }
   }

   private static class CompressionException extends RuntimeException {
      public CompressionException(Path path, Throwable cause) {
         super(path.toAbsolutePath().toString(), cause);
      }
   }
}
