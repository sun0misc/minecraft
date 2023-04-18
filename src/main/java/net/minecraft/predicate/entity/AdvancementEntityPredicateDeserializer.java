package net.minecraft.predicate.entity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mojang.logging.LogUtils;
import net.minecraft.loot.LootGsons;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

public class AdvancementEntityPredicateDeserializer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Identifier advancementId;
   private final LootManager field_44474;
   private final Gson gson = LootGsons.getConditionGsonBuilder().create();

   public AdvancementEntityPredicateDeserializer(Identifier advancementId, LootManager conditionManager) {
      this.advancementId = advancementId;
      this.field_44474 = conditionManager;
   }

   public final LootCondition[] loadConditions(JsonArray array, String key, LootContextType contextType) {
      LootCondition[] lvs = (LootCondition[])this.gson.fromJson(array, LootCondition[].class);
      LootTableReporter lv = new LootTableReporter(contextType, this.field_44474);
      LootCondition[] var6 = lvs;
      int var7 = lvs.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         LootCondition lv2 = var6[var8];
         lv2.validate(lv);
         lv.getMessages().forEach((name, message) -> {
            LOGGER.warn("Found validation problem in advancement trigger {}/{}: {}", new Object[]{key, name, message});
         });
      }

      return lvs;
   }

   public Identifier getAdvancementId() {
      return this.advancementId;
   }
}
