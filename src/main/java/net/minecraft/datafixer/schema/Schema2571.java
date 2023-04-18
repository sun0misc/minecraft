package net.minecraft.datafixer.schema;

import com.mojang.datafixers.schemas.Schema;
import java.util.Map;

public class Schema2571 extends IdentifierNormalizingSchema {
   public Schema2571(int i, Schema schema) {
      super(i, schema);
   }

   public Map registerEntities(Schema schema) {
      Map map = super.registerEntities(schema);
      schema.register(map, "minecraft:goat", () -> {
         return Schema100.targetItems(schema);
      });
      return map;
   }
}
