package net.minecraft.world.storage;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.util.PathUtil;
import net.minecraft.util.ThrowableDeliverer;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;

public final class RegionBasedStorage implements AutoCloseable {
   public static final String MCA_EXTENSION = ".mca";
   private static final int MAX_CACHE_SIZE = 256;
   private final Long2ObjectLinkedOpenHashMap cachedRegionFiles = new Long2ObjectLinkedOpenHashMap();
   private final Path directory;
   private final boolean dsync;

   RegionBasedStorage(Path directory, boolean dsync) {
      this.directory = directory;
      this.dsync = dsync;
   }

   private RegionFile getRegionFile(ChunkPos pos) throws IOException {
      long l = ChunkPos.toLong(pos.getRegionX(), pos.getRegionZ());
      RegionFile lv = (RegionFile)this.cachedRegionFiles.getAndMoveToFirst(l);
      if (lv != null) {
         return lv;
      } else {
         if (this.cachedRegionFiles.size() >= 256) {
            ((RegionFile)this.cachedRegionFiles.removeLast()).close();
         }

         PathUtil.createDirectories(this.directory);
         Path var10000 = this.directory;
         int var10001 = pos.getRegionX();
         Path path = var10000.resolve("r." + var10001 + "." + pos.getRegionZ() + ".mca");
         RegionFile lv2 = new RegionFile(path, this.directory, this.dsync);
         this.cachedRegionFiles.putAndMoveToFirst(l, lv2);
         return lv2;
      }
   }

   @Nullable
   public NbtCompound getTagAt(ChunkPos pos) throws IOException {
      RegionFile lv = this.getRegionFile(pos);
      DataInputStream dataInputStream = lv.getChunkInputStream(pos);

      NbtCompound var4;
      label43: {
         try {
            if (dataInputStream == null) {
               var4 = null;
               break label43;
            }

            var4 = NbtIo.read((DataInput)dataInputStream);
         } catch (Throwable var7) {
            if (dataInputStream != null) {
               try {
                  dataInputStream.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (dataInputStream != null) {
            dataInputStream.close();
         }

         return var4;
      }

      if (dataInputStream != null) {
         dataInputStream.close();
      }

      return var4;
   }

   public void scanChunk(ChunkPos chunkPos, NbtScanner scanner) throws IOException {
      RegionFile lv = this.getRegionFile(chunkPos);
      DataInputStream dataInputStream = lv.getChunkInputStream(chunkPos);

      try {
         if (dataInputStream != null) {
            NbtIo.scan(dataInputStream, scanner);
         }
      } catch (Throwable var8) {
         if (dataInputStream != null) {
            try {
               dataInputStream.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (dataInputStream != null) {
         dataInputStream.close();
      }

   }

   protected void write(ChunkPos pos, @Nullable NbtCompound nbt) throws IOException {
      RegionFile lv = this.getRegionFile(pos);
      if (nbt == null) {
         lv.delete(pos);
      } else {
         DataOutputStream dataOutputStream = lv.getChunkOutputStream(pos);

         try {
            NbtIo.write((NbtCompound)nbt, (DataOutput)dataOutputStream);
         } catch (Throwable var8) {
            if (dataOutputStream != null) {
               try {
                  dataOutputStream.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (dataOutputStream != null) {
            dataOutputStream.close();
         }
      }

   }

   public void close() throws IOException {
      ThrowableDeliverer lv = new ThrowableDeliverer();
      ObjectIterator var2 = this.cachedRegionFiles.values().iterator();

      while(var2.hasNext()) {
         RegionFile lv2 = (RegionFile)var2.next();

         try {
            lv2.close();
         } catch (IOException var5) {
            lv.add(var5);
         }
      }

      lv.deliver();
   }

   public void sync() throws IOException {
      ObjectIterator var1 = this.cachedRegionFiles.values().iterator();

      while(var1.hasNext()) {
         RegionFile lv = (RegionFile)var1.next();
         lv.sync();
      }

   }
}
