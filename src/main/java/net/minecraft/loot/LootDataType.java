package net.minecraft.loot;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

public class LootDataType {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final LootDataType PREDICATES = new LootDataType(LootGsons.getConditionGsonBuilder().create(), parserFactory(LootCondition.class, LootManager::and), "predicates", validator());
   public static final LootDataType ITEM_MODIFIERS = new LootDataType(LootGsons.getFunctionGsonBuilder().create(), parserFactory(LootFunction.class, LootManager::and), "item_modifiers", validator());
   public static final LootDataType LOOT_TABLES = new LootDataType(LootGsons.getTableGsonBuilder().create(), parserFactory(LootTable.class), "loot_tables", tableValidator());
   private final Gson gson;
   private final BiFunction parser;
   private final String id;
   private final Validator validator;

   private LootDataType(Gson gson, BiFunction parserFactory, String id, Validator validator) {
      this.gson = gson;
      this.id = id;
      this.validator = validator;
      this.parser = (BiFunction)parserFactory.apply(gson, id);
   }

   public Gson getGson() {
      return this.gson;
   }

   public String getId() {
      return this.id;
   }

   public void validate(LootTableReporter reporter, LootDataKey key, Object value) {
      this.validator.run(reporter, key, value);
   }

   public Optional parse(Identifier id, JsonElement json) {
      return (Optional)this.parser.apply(id, json);
   }

   public static Stream stream() {
      return Stream.of(PREDICATES, ITEM_MODIFIERS, LOOT_TABLES);
   }

   private static BiFunction parserFactory(Class clazz) {
      return (gson, dataTypeId) -> {
         return (id, json) -> {
            try {
               return Optional.of(gson.fromJson(json, clazz));
            } catch (Exception var6) {
               LOGGER.error("Couldn't parse element {}:{}", new Object[]{dataTypeId, id, var6});
               return Optional.empty();
            }
         };
      };
   }

   private static BiFunction parserFactory(Class clazz, Function combiner) {
      Class class2 = clazz.arrayType();
      return (gson, string) -> {
         return (id, json) -> {
            try {
               if (json.isJsonArray()) {
                  Object[] objects = (Object[])gson.fromJson(json, class2);
                  return Optional.of(combiner.apply(objects));
               } else {
                  return Optional.of(gson.fromJson(json, clazz));
               }
            } catch (Exception var8) {
               LOGGER.error("Couldn't parse element {}:{}", new Object[]{string, id, var8});
               return Optional.empty();
            }
         };
      };
   }

   private static Validator validator() {
      return (reporter, key, value) -> {
         value.validate(reporter.makeChild("{" + key.type().id + ":" + key.id() + "}", key));
      };
   }

   private static Validator tableValidator() {
      return (reporter, key, value) -> {
         value.validate(reporter.withContextType(value.getType()).makeChild("{" + key.type().id + ":" + key.id() + "}", key));
      };
   }

   @FunctionalInterface
   public interface Validator {
      void run(LootTableReporter reporter, LootDataKey key, Object value);
   }
}
