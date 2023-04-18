package net.minecraft.world;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class PersistentStateManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Map loadedStates = Maps.newHashMap();
   private final DataFixer dataFixer;
   private final File directory;

   public PersistentStateManager(File directory, DataFixer dataFixer) {
      this.dataFixer = dataFixer;
      this.directory = directory;
   }

   private File getFile(String id) {
      return new File(this.directory, id + ".dat");
   }

   public PersistentState getOrCreate(Function readFunction, Supplier supplier, String id) {
      PersistentState lv = this.get(readFunction, id);
      if (lv != null) {
         return lv;
      } else {
         PersistentState lv2 = (PersistentState)supplier.get();
         this.set(id, lv2);
         return lv2;
      }
   }

   @Nullable
   public PersistentState get(Function readFunction, String id) {
      PersistentState lv = (PersistentState)this.loadedStates.get(id);
      if (lv == null && !this.loadedStates.containsKey(id)) {
         lv = this.readFromFile(readFunction, id);
         this.loadedStates.put(id, lv);
      }

      return lv;
   }

   @Nullable
   private PersistentState readFromFile(Function readFunction, String id) {
      try {
         File file = this.getFile(id);
         if (file.exists()) {
            NbtCompound lv = this.readNbt(id, SharedConstants.getGameVersion().getSaveVersion().getId());
            return (PersistentState)readFunction.apply(lv.getCompound("data"));
         }
      } catch (Exception var5) {
         LOGGER.error("Error loading saved data: {}", id, var5);
      }

      return null;
   }

   public void set(String id, PersistentState state) {
      this.loadedStates.put(id, state);
   }

   public NbtCompound readNbt(String id, int dataVersion) throws IOException {
      File file = this.getFile(id);
      FileInputStream fileInputStream = new FileInputStream(file);

      NbtCompound var8;
      try {
         PushbackInputStream pushbackInputStream = new PushbackInputStream(fileInputStream, 2);

         try {
            NbtCompound lv;
            if (this.isCompressed(pushbackInputStream)) {
               lv = NbtIo.readCompressed((InputStream)pushbackInputStream);
            } else {
               DataInputStream dataInputStream = new DataInputStream(pushbackInputStream);

               try {
                  lv = NbtIo.read((DataInput)dataInputStream);
               } catch (Throwable var13) {
                  try {
                     dataInputStream.close();
                  } catch (Throwable var12) {
                     var13.addSuppressed(var12);
                  }

                  throw var13;
               }

               dataInputStream.close();
            }

            int j = NbtHelper.getDataVersion(lv, 1343);
            var8 = DataFixTypes.SAVED_DATA.update(this.dataFixer, lv, j, dataVersion);
         } catch (Throwable var14) {
            try {
               pushbackInputStream.close();
            } catch (Throwable var11) {
               var14.addSuppressed(var11);
            }

            throw var14;
         }

         pushbackInputStream.close();
      } catch (Throwable var15) {
         try {
            fileInputStream.close();
         } catch (Throwable var10) {
            var15.addSuppressed(var10);
         }

         throw var15;
      }

      fileInputStream.close();
      return var8;
   }

   private boolean isCompressed(PushbackInputStream stream) throws IOException {
      byte[] bs = new byte[2];
      boolean bl = false;
      int i = stream.read(bs, 0, 2);
      if (i == 2) {
         int j = (bs[1] & 255) << 8 | bs[0] & 255;
         if (j == 35615) {
            bl = true;
         }
      }

      if (i != 0) {
         stream.unread(bs, 0, i);
      }

      return bl;
   }

   public void save() {
      this.loadedStates.forEach((id, state) -> {
         if (state != null) {
            state.save(this.getFile(id));
         }

      });
   }
}
