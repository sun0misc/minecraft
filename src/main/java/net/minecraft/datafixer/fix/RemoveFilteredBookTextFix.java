package net.minecraft.datafixer.fix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class RemoveFilteredBookTextFix extends ItemNbtFix {
   public RemoveFilteredBookTextFix(Schema schema) {
      super(schema, "Remove filtered text from books", (itemId) -> {
         return itemId.equals("minecraft:writable_book") || itemId.equals("minecraft:written_book");
      });
   }

   protected Dynamic fixNbt(Dynamic dynamic) {
      return dynamic.remove("filtered_title").remove("filtered_pages");
   }
}
