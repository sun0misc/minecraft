package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import org.slf4j.Logger;

public class WorldUuidFix extends AbstractUuidFix {
   private static final Logger LOGGER = LogUtils.getLogger();

   public WorldUuidFix(Schema outputSchema) {
      super(outputSchema, TypeReferences.LEVEL);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("LevelUUIDFix", this.getInputSchema().getType(this.typeReference), (typed) -> {
         return typed.updateTyped(DSL.remainderFinder(), (typedx) -> {
            return typedx.update(DSL.remainderFinder(), (dynamic) -> {
               dynamic = this.fixCustomBossEvents(dynamic);
               dynamic = this.fixDragonUuid(dynamic);
               dynamic = this.fixWanderingTraderId(dynamic);
               return dynamic;
            });
         });
      });
   }

   private Dynamic fixWanderingTraderId(Dynamic dynamic) {
      return (Dynamic)updateStringUuid(dynamic, "WanderingTraderId", "WanderingTraderId").orElse(dynamic);
   }

   private Dynamic fixDragonUuid(Dynamic dynamic) {
      return dynamic.update("DimensionData", (dynamicx) -> {
         return dynamicx.updateMapValues((pair) -> {
            return pair.mapSecond((dynamic) -> {
               return dynamic.update("DragonFight", (dynamicx) -> {
                  return (Dynamic)updateRegularMostLeast(dynamicx, "DragonUUID", "Dragon").orElse(dynamicx);
               });
            });
         });
      });
   }

   private Dynamic fixCustomBossEvents(Dynamic dynamic) {
      return dynamic.update("CustomBossEvents", (dynamicx) -> {
         return dynamicx.updateMapValues((pair) -> {
            return pair.mapSecond((dynamic) -> {
               return dynamic.update("Players", (dynamic2) -> {
                  return dynamic.createList(dynamic2.asStream().map((dynamicx) -> {
                     return (Dynamic)createArrayFromCompoundUuid(dynamicx).orElseGet(() -> {
                        LOGGER.warn("CustomBossEvents contains invalid UUIDs.");
                        return dynamicx;
                     });
                  }));
               });
            });
         });
      });
   }
}
