package net.minecraft.loot;

import net.minecraft.util.Identifier;

public record LootDataKey(LootDataType type, Identifier id) {
   public LootDataKey(LootDataType arg, Identifier arg2) {
      this.type = arg;
      this.id = arg2;
   }

   public LootDataType type() {
      return this.type;
   }

   public Identifier id() {
      return this.id;
   }
}
