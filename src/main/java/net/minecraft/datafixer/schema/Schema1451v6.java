package net.minecraft.datafixer.schema;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.Identifier;

public class Schema1451v6 extends IdentifierNormalizingSchema {
   public static final String SPECIAL_TYPE = "_special";
   protected static final Hook.HookFunction field_34014 = new Hook.HookFunction() {
      public Object apply(DynamicOps ops, Object value) {
         Dynamic dynamic = new Dynamic(ops, value);
         return ((Dynamic)DataFixUtils.orElse(dynamic.get("CriteriaName").asString().get().left().map((criteriaName) -> {
            int i = criteriaName.indexOf(58);
            if (i < 0) {
               return Pair.of("_special", criteriaName);
            } else {
               try {
                  Identifier lv = Identifier.splitOn(criteriaName.substring(0, i), '.');
                  Identifier lv2 = Identifier.splitOn(criteriaName.substring(i + 1), '.');
                  return Pair.of(lv.toString(), lv2.toString());
               } catch (Exception var4) {
                  return Pair.of("_special", criteriaName);
               }
            }
         }).map((pair) -> {
            return dynamic.set("CriteriaType", dynamic.createMap(ImmutableMap.of(dynamic.createString("type"), dynamic.createString((String)pair.getFirst()), dynamic.createString("id"), dynamic.createString((String)pair.getSecond()))));
         }), dynamic)).getValue();
      }
   };
   protected static final Hook.HookFunction field_34015 = new Hook.HookFunction() {
      private String normalize(String id) {
         Identifier lv = Identifier.tryParse(id);
         return lv != null ? lv.getNamespace() + "." + lv.getPath() : id;
      }

      public Object apply(DynamicOps ops, Object value) {
         Dynamic dynamic = new Dynamic(ops, value);
         Optional optional = dynamic.get("CriteriaType").get().get().left().flatMap((criteriaType) -> {
            Optional optional = criteriaType.get("type").asString().get().left();
            Optional optional2 = criteriaType.get("id").asString().get().left();
            if (optional.isPresent() && optional2.isPresent()) {
               String string = (String)optional.get();
               if (string.equals("_special")) {
                  return Optional.of(dynamic.createString((String)optional2.get()));
               } else {
                  String var10001 = this.normalize(string);
                  return Optional.of(criteriaType.createString(var10001 + ":" + this.normalize((String)optional2.get())));
               }
            } else {
               return Optional.empty();
            }
         });
         return ((Dynamic)DataFixUtils.orElse(optional.map((criteriaName) -> {
            return dynamic.set("CriteriaName", criteriaName).remove("CriteriaType");
         }), dynamic)).getValue();
      }
   };

   public Schema1451v6(int i, Schema schema) {
      super(i, schema);
   }

   public void registerTypes(Schema schema, Map entityTypes, Map blockEntityTypes) {
      super.registerTypes(schema, entityTypes, blockEntityTypes);
      Supplier supplier = () -> {
         return DSL.compoundList(TypeReferences.ITEM_NAME.in(schema), DSL.constType(DSL.intType()));
      };
      schema.registerType(false, TypeReferences.STATS, () -> {
         return DSL.optionalFields("stats", DSL.optionalFields("minecraft:mined", DSL.compoundList(TypeReferences.BLOCK_NAME.in(schema), DSL.constType(DSL.intType())), "minecraft:crafted", (TypeTemplate)supplier.get(), "minecraft:used", (TypeTemplate)supplier.get(), "minecraft:broken", (TypeTemplate)supplier.get(), "minecraft:picked_up", (TypeTemplate)supplier.get(), DSL.optionalFields("minecraft:dropped", (TypeTemplate)supplier.get(), "minecraft:killed", DSL.compoundList(TypeReferences.ENTITY_NAME.in(schema), DSL.constType(DSL.intType())), "minecraft:killed_by", DSL.compoundList(TypeReferences.ENTITY_NAME.in(schema), DSL.constType(DSL.intType())), "minecraft:custom", DSL.compoundList(DSL.constType(getIdentifierType()), DSL.constType(DSL.intType())))));
      });
      Map map3 = method_37389(schema);
      schema.registerType(false, TypeReferences.OBJECTIVE, () -> {
         return DSL.hook(DSL.optionalFields("CriteriaType", DSL.taggedChoiceLazy("type", DSL.string(), map3)), field_34014, field_34015);
      });
   }

   protected static Map method_37389(Schema schema) {
      Supplier supplier = () -> {
         return DSL.optionalFields("id", TypeReferences.ITEM_NAME.in(schema));
      };
      Supplier supplier2 = () -> {
         return DSL.optionalFields("id", TypeReferences.BLOCK_NAME.in(schema));
      };
      Supplier supplier3 = () -> {
         return DSL.optionalFields("id", TypeReferences.ENTITY_NAME.in(schema));
      };
      Map map = Maps.newHashMap();
      map.put("minecraft:mined", supplier2);
      map.put("minecraft:crafted", supplier);
      map.put("minecraft:used", supplier);
      map.put("minecraft:broken", supplier);
      map.put("minecraft:picked_up", supplier);
      map.put("minecraft:dropped", supplier);
      map.put("minecraft:killed", supplier3);
      map.put("minecraft:killed_by", supplier3);
      map.put("minecraft:custom", () -> {
         return DSL.optionalFields("id", DSL.constType(getIdentifierType()));
      });
      map.put("_special", () -> {
         return DSL.optionalFields("id", DSL.constType(DSL.string()));
      });
      return map;
   }
}
