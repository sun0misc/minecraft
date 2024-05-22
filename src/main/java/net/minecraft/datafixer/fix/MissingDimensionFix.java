/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.FieldFinder;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.CompoundList;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.List;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.StructureSeparationDataFix;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class MissingDimensionFix
extends DataFix {
    public MissingDimensionFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    protected static <A> Type<Pair<A, Dynamic<?>>> method_29913(String string, Type<A> type) {
        return DSL.and(DSL.field(string, type), DSL.remainderType());
    }

    protected static <A> Type<Pair<Either<A, Unit>, Dynamic<?>>> method_29915(String string, Type<A> type) {
        return DSL.and(DSL.optional(DSL.field(string, type)), DSL.remainderType());
    }

    protected static <A1, A2> Type<Pair<Either<A1, Unit>, Pair<Either<A2, Unit>, Dynamic<?>>>> method_29914(String string, Type<A1> type, String string2, Type<A2> type2) {
        return DSL.and(DSL.optional(DSL.field(string, type)), DSL.optional(DSL.field(string2, type2)), DSL.remainderType());
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Schema schema = this.getInputSchema();
        Type<Pair<String, Pair<Either<Pair<String, Dynamic<?>>, Unit>, Pair<Either<Either<String, Pair<Either<?, Unit>, Pair<Either<?, Unit>, Dynamic<?>>>>, Unit>, Dynamic<?>>>>> type = DSL.taggedChoiceType("type", DSL.string(), ImmutableMap.of("minecraft:debug", DSL.remainderType(), "minecraft:flat", MissingDimensionFix.method_38820(schema), "minecraft:noise", MissingDimensionFix.method_29914("biome_source", DSL.taggedChoiceType("type", DSL.string(), ImmutableMap.of("minecraft:fixed", MissingDimensionFix.method_29913("biome", schema.getType(TypeReferences.BIOME)), "minecraft:multi_noise", DSL.list(MissingDimensionFix.method_29913("biome", schema.getType(TypeReferences.BIOME))), "minecraft:checkerboard", MissingDimensionFix.method_29913("biomes", DSL.list(schema.getType(TypeReferences.BIOME))), "minecraft:vanilla_layered", DSL.remainderType(), "minecraft:the_end", DSL.remainderType())), "settings", DSL.or(DSL.string(), MissingDimensionFix.method_29914("default_block", schema.getType(TypeReferences.BLOCK_NAME), "default_fluid", schema.getType(TypeReferences.BLOCK_NAME))))));
        CompoundList.CompoundListType<String, Pair<Pair<String, Pair<Either<Pair<String, Dynamic<?>>, Unit>, Pair<Either<Either<String, Pair<Either<?, Unit>, Pair<Either<?, Unit>, Dynamic<?>>>>, Unit>, Dynamic<?>>>>, Dynamic<?>>> compoundListType = DSL.compoundList(IdentifierNormalizingSchema.getIdentifierType(), MissingDimensionFix.method_29913("generator", type));
        Type type2 = DSL.and(compoundListType, DSL.remainderType());
        Type<?> type3 = schema.getType(TypeReferences.WORLD_GEN_SETTINGS);
        FieldFinder fieldFinder = new FieldFinder("dimensions", type2);
        if (!type3.findFieldType("dimensions").equals(type2)) {
            throw new IllegalStateException();
        }
        OpticFinder opticFinder = compoundListType.finder();
        return this.fixTypeEverywhereTyped("MissingDimensionFix", type3, typed -> typed.updateTyped(fieldFinder, typed22 -> typed22.updateTyped(opticFinder, typed2 -> {
            if (!(typed2.getValue() instanceof List)) {
                throw new IllegalStateException("List exptected");
            }
            if (((List)typed2.getValue()).isEmpty()) {
                Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
                Dynamic<?> dynamic2 = this.method_29912(dynamic);
                return DataFixUtils.orElse(compoundListType.readTyped(dynamic2).result().map(Pair::getFirst), typed2);
            }
            return typed2;
        })));
    }

    protected static Type<? extends Pair<? extends Either<? extends Pair<? extends Either<?, Unit>, ? extends Pair<? extends Either<? extends List<? extends Pair<? extends Either<?, Unit>, Dynamic<?>>>, Unit>, Dynamic<?>>>, Unit>, Dynamic<?>>> method_38820(Schema schema) {
        return MissingDimensionFix.method_29915("settings", MissingDimensionFix.method_29914("biome", schema.getType(TypeReferences.BIOME), "layers", DSL.list(MissingDimensionFix.method_29915("block", schema.getType(TypeReferences.BLOCK_NAME)))));
    }

    private <T> Dynamic<T> method_29912(Dynamic<T> dynamic) {
        long l = dynamic.get("seed").asLong(0L);
        return new Dynamic(dynamic.getOps(), StructureSeparationDataFix.method_29917(dynamic, l, StructureSeparationDataFix.method_29916(dynamic, l), false));
    }
}

