package net.minecraft.command;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentStateManager;

public class DataCommandStorage {
   private static final String COMMAND_STORAGE_PREFIX = "command_storage_";
   private final Map storages = Maps.newHashMap();
   private final PersistentStateManager stateManager;

   public DataCommandStorage(PersistentStateManager stateManager) {
      this.stateManager = stateManager;
   }

   private PersistentState createStorage(String namespace) {
      PersistentState lv = new PersistentState();
      this.storages.put(namespace, lv);
      return lv;
   }

   public NbtCompound get(Identifier id) {
      String string = id.getNamespace();
      PersistentState lv = (PersistentState)this.stateManager.get((data) -> {
         return this.createStorage(string).readNbt(data);
      }, getSaveKey(string));
      return lv != null ? lv.get(id.getPath()) : new NbtCompound();
   }

   public void set(Identifier id, NbtCompound nbt) {
      String string = id.getNamespace();
      ((PersistentState)this.stateManager.getOrCreate((data) -> {
         return this.createStorage(string).readNbt(data);
      }, () -> {
         return this.createStorage(string);
      }, getSaveKey(string))).set(id.getPath(), nbt);
   }

   public Stream getIds() {
      return this.storages.entrySet().stream().flatMap((entry) -> {
         return ((PersistentState)entry.getValue()).getIds((String)entry.getKey());
      });
   }

   private static String getSaveKey(String namespace) {
      return "command_storage_" + namespace;
   }

   private static class PersistentState extends net.minecraft.world.PersistentState {
      private static final String CONTENTS_KEY = "contents";
      private final Map map = Maps.newHashMap();

      PersistentState() {
      }

      PersistentState readNbt(NbtCompound nbt) {
         NbtCompound lv = nbt.getCompound("contents");
         Iterator var3 = lv.getKeys().iterator();

         while(var3.hasNext()) {
            String string = (String)var3.next();
            this.map.put(string, lv.getCompound(string));
         }

         return this;
      }

      public NbtCompound writeNbt(NbtCompound nbt) {
         NbtCompound lv = new NbtCompound();
         this.map.forEach((key, value) -> {
            lv.put(key, value.copy());
         });
         nbt.put("contents", lv);
         return nbt;
      }

      public NbtCompound get(String name) {
         NbtCompound lv = (NbtCompound)this.map.get(name);
         return lv != null ? lv : new NbtCompound();
      }

      public void set(String name, NbtCompound nbt) {
         if (nbt.isEmpty()) {
            this.map.remove(name);
         } else {
            this.map.put(name, nbt);
         }

         this.markDirty();
      }

      public Stream getIds(String namespace) {
         return this.map.keySet().stream().map((key) -> {
            return new Identifier(namespace, key);
         });
      }
   }
}
