package net.minecraft.datafixer.schema;

import com.mojang.datafixers.schemas.Schema;
import java.util.Map;

public class Schema702 extends Schema {
   public Schema702(int versionKey, Schema parent) {
      super(versionKey, parent);
   }

   protected static void targetEntityItems(Schema schema, Map map, String entityId) {
      schema.register(map, entityId, () -> {
         return Schema100.targetItems(schema);
      });
   }

   public Map registerEntities(Schema schema) {
      Map map = super.registerEntities(schema);
      targetEntityItems(schema, map, "ZombieVillager");
      targetEntityItems(schema, map, "Husk");
      return map;
   }
}
