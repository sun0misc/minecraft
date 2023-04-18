package net.minecraft.text;

import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

public record StorageNbtDataSource(Identifier id) implements NbtDataSource {
   public StorageNbtDataSource(Identifier arg) {
      this.id = arg;
   }

   public Stream get(ServerCommandSource source) {
      NbtCompound lv = source.getServer().getDataCommandStorage().get(this.id);
      return Stream.of(lv);
   }

   public String toString() {
      return "storage=" + this.id;
   }

   public Identifier id() {
      return this.id;
   }
}
