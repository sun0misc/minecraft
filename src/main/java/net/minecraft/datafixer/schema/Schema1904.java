package net.minecraft.datafixer.schema;

import com.mojang.datafixers.schemas.Schema;
import java.util.Map;

public class Schema1904 extends IdentifierNormalizingSchema {
   public Schema1904(int i, Schema schema) {
      super(i, schema);
   }

   public Map registerEntities(Schema schema) {
      Map map = super.registerEntities(schema);
      schema.register(map, "minecraft:cat", () -> {
         return Schema100.targetItems(schema);
      });
      return map;
   }
}
