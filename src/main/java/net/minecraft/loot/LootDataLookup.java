package net.minecraft.loot;

import java.util.Optional;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface LootDataLookup {
   @Nullable
   Object getElement(LootDataKey key);

   @Nullable
   default Object getElement(LootDataType type, Identifier id) {
      return this.getElement(new LootDataKey(type, id));
   }

   default Optional getElementOptional(LootDataKey key) {
      return Optional.ofNullable(this.getElement(key));
   }

   default Optional getElementOptional(LootDataType type, Identifier id) {
      return this.getElementOptional(new LootDataKey(type, id));
   }

   default LootTable getLootTable(Identifier id) {
      return (LootTable)this.getElementOptional(LootDataType.LOOT_TABLES, id).orElse(LootTable.EMPTY);
   }
}
