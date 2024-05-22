/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import net.minecraft.util.Identifier;

public class Schema1451v6
extends IdentifierNormalizingSchema {
    public static final String SPECIAL_TYPE = "_special";
    protected static final Hook.HookFunction field_34014 = new Hook.HookFunction(){

        @Override
        public <T> T apply(DynamicOps<T> ops, T value) {
            Dynamic dynamic = new Dynamic(ops, value);
            return DataFixUtils.orElse(dynamic.get("CriteriaName").asString().result().map(criteriaName -> {
                int i = criteriaName.indexOf(58);
                if (i < 0) {
                    return Pair.of(Schema1451v6.SPECIAL_TYPE, criteriaName);
                }
                try {
                    Identifier lv = Identifier.splitOn(criteriaName.substring(0, i), '.');
                    Identifier lv2 = Identifier.splitOn(criteriaName.substring(i + 1), '.');
                    return Pair.of(lv.toString(), lv2.toString());
                } catch (Exception exception) {
                    return Pair.of(Schema1451v6.SPECIAL_TYPE, criteriaName);
                }
            }).map(pair -> dynamic.set("CriteriaType", dynamic.createMap(ImmutableMap.of(dynamic.createString("type"), dynamic.createString((String)pair.getFirst()), dynamic.createString("id"), dynamic.createString((String)pair.getSecond()))))), dynamic).getValue();
        }
    };
    protected static final Hook.HookFunction field_34015 = new Hook.HookFunction(){

        @Override
        public <T> T apply(DynamicOps<T> ops, T value) {
            Dynamic dynamic = new Dynamic(ops, value);
            Optional<Dynamic> optional = dynamic.get("CriteriaType").get().result().flatMap(dynamic2 -> {
                Optional<String> optional = dynamic2.get("type").asString().result();
                Optional<String> optional2 = dynamic2.get("id").asString().result();
                if (optional.isPresent() && optional2.isPresent()) {
                    String string = optional.get();
                    if (string.equals(Schema1451v6.SPECIAL_TYPE)) {
                        return Optional.of(dynamic.createString(optional2.get()));
                    }
                    return Optional.of(dynamic2.createString(Schema1451v6.toDotSeparated(string) + ":" + Schema1451v6.toDotSeparated(optional2.get())));
                }
                return Optional.empty();
            });
            return DataFixUtils.orElse(optional.map(criteriaName -> dynamic.set("CriteriaName", (Dynamic<?>)criteriaName).remove("CriteriaType")), dynamic).getValue();
        }
    };

    public Schema1451v6(int i, Schema schema) {
        super(i, schema);
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);
        Supplier<TypeTemplate> supplier = () -> DSL.compoundList(TypeReferences.ITEM_NAME.in(schema), DSL.constType(DSL.intType()));
        schema.registerType(false, TypeReferences.STATS, () -> DSL.optionalFields("stats", DSL.optionalFields(Pair.of("minecraft:mined", DSL.compoundList(TypeReferences.BLOCK_NAME.in(schema), DSL.constType(DSL.intType()))), Pair.of("minecraft:crafted", (TypeTemplate)supplier.get()), Pair.of("minecraft:used", (TypeTemplate)supplier.get()), Pair.of("minecraft:broken", (TypeTemplate)supplier.get()), Pair.of("minecraft:picked_up", (TypeTemplate)supplier.get()), Pair.of("minecraft:dropped", (TypeTemplate)supplier.get()), Pair.of("minecraft:killed", DSL.compoundList(TypeReferences.ENTITY_NAME.in(schema), DSL.constType(DSL.intType()))), Pair.of("minecraft:killed_by", DSL.compoundList(TypeReferences.ENTITY_NAME.in(schema), DSL.constType(DSL.intType()))), Pair.of("minecraft:custom", DSL.compoundList(DSL.constType(Schema1451v6.getIdentifierType()), DSL.constType(DSL.intType()))))));
        Map<String, Supplier<TypeTemplate>> map3 = Schema1451v6.method_37389(schema);
        schema.registerType(false, TypeReferences.OBJECTIVE, () -> DSL.hook(DSL.optionalFields("CriteriaType", DSL.taggedChoiceLazy("type", DSL.string(), map3)), field_34014, field_34015));
    }

    protected static Map<String, Supplier<TypeTemplate>> method_37389(Schema schema) {
        Supplier<TypeTemplate> supplier = () -> DSL.optionalFields("id", TypeReferences.ITEM_NAME.in(schema));
        Supplier<TypeTemplate> supplier2 = () -> DSL.optionalFields("id", TypeReferences.BLOCK_NAME.in(schema));
        Supplier<TypeTemplate> supplier3 = () -> DSL.optionalFields("id", TypeReferences.ENTITY_NAME.in(schema));
        HashMap<String, Supplier<TypeTemplate>> map = Maps.newHashMap();
        map.put("minecraft:mined", supplier2);
        map.put("minecraft:crafted", supplier);
        map.put("minecraft:used", supplier);
        map.put("minecraft:broken", supplier);
        map.put("minecraft:picked_up", supplier);
        map.put("minecraft:dropped", supplier);
        map.put("minecraft:killed", supplier3);
        map.put("minecraft:killed_by", supplier3);
        map.put("minecraft:custom", () -> DSL.optionalFields("id", DSL.constType(Schema1451v6.getIdentifierType())));
        map.put(SPECIAL_TYPE, () -> DSL.optionalFields("id", DSL.constType(DSL.string())));
        return map;
    }

    public static String toDotSeparated(String id) {
        Identifier lv = Identifier.tryParse(id);
        return lv != null ? lv.getNamespace() + "." + lv.getPath() : id;
    }
}

