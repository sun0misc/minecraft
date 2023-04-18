package net.minecraft.datafixer.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;
import net.minecraft.datafixer.TypeReferences;

public class Schema1800 extends IdentifierNormalizingSchema {
   public Schema1800(int i, Schema schema) {
      super(i, schema);
   }

   public Map registerEntities(Schema schema) {
      Map map = super.registerEntities(schema);
      schema.register(map, "minecraft:panda", () -> {
         return Schema100.targetItems(schema);
      });
      schema.register(map, "minecraft:pillager", (name) -> {
         return DSL.optionalFields("Inventory", DSL.list(TypeReferences.ITEM_STACK.in(schema)), Schema100.targetItems(schema));
      });
      return map;
   }
}
