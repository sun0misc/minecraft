package net.minecraft.datafixer.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;
import net.minecraft.datafixer.TypeReferences;

public class Schema1906 extends IdentifierNormalizingSchema {
   public Schema1906(int i, Schema schema) {
      super(i, schema);
   }

   public Map registerBlockEntities(Schema schema) {
      Map map = super.registerBlockEntities(schema);
      method_16052(schema, map, "minecraft:barrel");
      method_16052(schema, map, "minecraft:smoker");
      method_16052(schema, map, "minecraft:blast_furnace");
      schema.register(map, "minecraft:lectern", (name) -> {
         return DSL.optionalFields("Book", TypeReferences.ITEM_STACK.in(schema));
      });
      schema.registerSimple(map, "minecraft:bell");
      return map;
   }

   protected static void method_16052(Schema schema, Map map, String name) {
      schema.register(map, name, () -> {
         return DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)));
      });
   }
}
