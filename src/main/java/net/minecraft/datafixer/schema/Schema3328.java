package net.minecraft.datafixer.schema;

import com.mojang.datafixers.schemas.Schema;
import java.util.Map;

public class Schema3328 extends IdentifierNormalizingSchema {
   public Schema3328(int i, Schema schema) {
      super(i, schema);
   }

   public Map registerEntities(Schema schema) {
      Map map = super.registerEntities(schema);
      schema.registerSimple(map, "minecraft:interaction");
      return map;
   }
}
