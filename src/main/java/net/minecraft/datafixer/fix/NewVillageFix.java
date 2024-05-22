/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.CompoundList;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class NewVillageFix
extends DataFix {
    public NewVillageFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        CompoundList.CompoundListType<String, ?> compoundListType = DSL.compoundList(DSL.string(), this.getInputSchema().getType(TypeReferences.STRUCTURE_FEATURE));
        OpticFinder opticFinder = compoundListType.finder();
        return this.fix(compoundListType);
    }

    private <SF> TypeRewriteRule fix(CompoundList.CompoundListType<String, SF> compoundListType) {
        Type<?> type = this.getInputSchema().getType(TypeReferences.CHUNK);
        Type<?> type2 = this.getInputSchema().getType(TypeReferences.STRUCTURE_FEATURE);
        OpticFinder<?> opticFinder = type.findField("Level");
        OpticFinder<?> opticFinder2 = opticFinder.type().findField("Structures");
        OpticFinder<?> opticFinder3 = opticFinder2.type().findField("Starts");
        OpticFinder opticFinder4 = compoundListType.finder();
        return TypeRewriteRule.seq(this.fixTypeEverywhereTyped("NewVillageFix", type, typed2 -> typed2.updateTyped(opticFinder, typed -> typed.updateTyped(opticFinder2, typed2 -> typed2.updateTyped(opticFinder3, typed -> typed.update(opticFinder4, list -> list.stream().filter(pair -> !Objects.equals(pair.getFirst(), "Village")).map(pair -> pair.mapFirst(string -> string.equals("New_Village") ? "Village" : string)).collect(Collectors.toList()))).update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("References", dynamic -> {
            Optional<Dynamic<Dynamic>> optional = dynamic.get("New_Village").result();
            return DataFixUtils.orElse(optional.map(dynamic2 -> dynamic.remove("New_Village").set("Village", (Dynamic<?>)dynamic2)), dynamic).remove("Village");
        }))))), this.fixTypeEverywhereTyped("NewVillageStartFix", type2, typed -> typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("id", dynamic -> Objects.equals(IdentifierNormalizingSchema.normalize(dynamic.asString("")), "minecraft:new_village") ? dynamic.createString("minecraft:village") : dynamic))));
    }
}

