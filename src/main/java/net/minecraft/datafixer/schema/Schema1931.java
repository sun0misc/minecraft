package net.minecraft.datafixer.schema;

import com.mojang.datafixers.schemas.Schema;
import java.util.Map;

public class Schema1931 extends IdentifierNormalizingSchema {
   public Schema1931(int i, Schema schema) {
      super(i, schema);
   }

   public Map registerEntities(Schema schema) {
      Map map = super.registerEntities(schema);
      schema.register(map, "minecraft:fox", () -> {
         return Schema100.targetItems(schema);
      });
      return map;
   }
}
