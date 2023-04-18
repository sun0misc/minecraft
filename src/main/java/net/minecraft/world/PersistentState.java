package net.minecraft.world;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import org.slf4j.Logger;

public abstract class PersistentState {
   private static final Logger LOGGER = LogUtils.getLogger();
   private boolean dirty;

   public abstract NbtCompound writeNbt(NbtCompound nbt);

   public void markDirty() {
      this.setDirty(true);
   }

   public void setDirty(boolean dirty) {
      this.dirty = dirty;
   }

   public boolean isDirty() {
      return this.dirty;
   }

   public void save(File file) {
      if (this.isDirty()) {
         NbtCompound lv = new NbtCompound();
         lv.put("data", this.writeNbt(new NbtCompound()));
         NbtHelper.putDataVersion(lv);

         try {
            NbtIo.writeCompressed(lv, file);
         } catch (IOException var4) {
            LOGGER.error("Could not save data {}", this, var4);
         }

         this.setDirty(false);
      }
   }
}
