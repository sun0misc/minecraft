package net.minecraft.nbt.scanner;

import java.util.List;
import net.minecraft.nbt.NbtType;

public record NbtScanQuery(List path, NbtType type, String key) {
   public NbtScanQuery(NbtType type, String key) {
      this(List.of(), type, key);
   }

   public NbtScanQuery(String path, NbtType type, String key) {
      this(List.of(path), type, key);
   }

   public NbtScanQuery(String path1, String path2, NbtType type, String key) {
      this(List.of(path1, path2), type, key);
   }

   public NbtScanQuery(List list, NbtType arg, String string) {
      this.path = list;
      this.type = arg;
      this.key = string;
   }

   public List path() {
      return this.path;
   }

   public NbtType type() {
      return this.type;
   }

   public String key() {
      return this.key;
   }
}
