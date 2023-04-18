package net.minecraft.datafixer.schema;

import com.mojang.datafixers.schemas.Schema;
import java.util.Map;

public class Schema1909 extends IdentifierNormalizingSchema {
   public Schema1909(int i, Schema schema) {
      super(i, schema);
   }

   public Map registerBlockEntities(Schema schema) {
      Map map = super.registerBlockEntities(schema);
      schema.registerSimple(map, "minecraft:jigsaw");
      return map;
   }
}
