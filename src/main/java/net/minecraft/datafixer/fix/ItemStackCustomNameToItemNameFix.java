/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.TextFixes;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class ItemStackCustomNameToItemNameFix
extends DataFix {
    private static final Set<String> EXPLORER_MAP_NAMES = Set.of("filled_map.buried_treasure", "filled_map.explorer_jungle", "filled_map.explorer_swamp", "filled_map.mansion", "filled_map.monument", "filled_map.trial_chambers", "filled_map.village_desert", "filled_map.village_plains", "filled_map.village_savanna", "filled_map.village_snowy", "filled_map.village_taiga");

    public ItemStackCustomNameToItemNameFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    public final TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
        OpticFinder<Pair<String, String>> opticFinder = DSL.fieldFinder("id", DSL.named(TypeReferences.ITEM_NAME.typeName(), IdentifierNormalizingSchema.getIdentifierType()));
        OpticFinder<?> opticFinder2 = type.findField("components");
        return this.fixTypeEverywhereTyped("ItemStack custom_name to item_name component fix", type, typed2 -> {
            Optional optional = typed2.getOptional(opticFinder);
            Optional<String> optional2 = optional.map(Pair::getSecond);
            if (optional2.filter(itemId -> itemId.equals("minecraft:white_banner")).isPresent()) {
                return typed2.updateTyped(opticFinder2, typed -> typed.update(DSL.remainderFinder(), ItemStackCustomNameToItemNameFix::fixOminousBanner));
            }
            if (optional2.filter(itemId -> itemId.equals("minecraft:filled_map")).isPresent()) {
                return typed2.updateTyped(opticFinder2, typed -> typed.update(DSL.remainderFinder(), ItemStackCustomNameToItemNameFix::fixExplorerMaps));
            }
            return typed2;
        });
    }

    private static <T> Dynamic<T> fixExplorerMaps(Dynamic<T> data) {
        return ItemStackCustomNameToItemNameFix.fix(data, EXPLORER_MAP_NAMES::contains);
    }

    private static <T> Dynamic<T> fixOminousBanner(Dynamic<T> data) {
        return ItemStackCustomNameToItemNameFix.fix(data, name -> name.equals("block.minecraft.ominous_banner"));
    }

    private static <T> Dynamic<T> fix(Dynamic<T> data, Predicate<String> namePredicate) {
        OptionalDynamic<T> optionalDynamic = data.get("minecraft:custom_name");
        Optional<String> optional = optionalDynamic.asString().result().flatMap(TextFixes::getTranslate).filter(namePredicate);
        if (optional.isPresent()) {
            return data.renameField("minecraft:custom_name", "minecraft:item_name");
        }
        return data;
    }
}

