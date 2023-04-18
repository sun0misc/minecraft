package net.minecraft.datafixer.schema;

import com.mojang.datafixers.schemas.Schema;
import java.util.Map;

public class Schema2509 extends IdentifierNormalizingSchema {
   public Schema2509(int i, Schema schema) {
      super(i, schema);
   }

   public Map registerEntities(Schema schema) {
      Map map = super.registerEntities(schema);
      map.remove("minecraft:zombie_pigman");
      schema.register(map, "minecraft:zombified_piglin", () -> {
         return Schema100.targetItems(schema);
      });
      return map;
   }
}
