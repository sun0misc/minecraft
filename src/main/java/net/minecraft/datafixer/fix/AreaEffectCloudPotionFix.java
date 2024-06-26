/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;

public class AreaEffectCloudPotionFix
extends ChoiceFix {
    public AreaEffectCloudPotionFix(Schema outputSchema) {
        super(outputSchema, false, "AreaEffectCloudPotionFix", TypeReferences.ENTITY, "minecraft:area_effect_cloud");
    }

    @Override
    protected Typed<?> transform(Typed<?> inputType) {
        return inputType.update(DSL.remainderFinder(), this::method_57191);
    }

    private <T> Dynamic<T> method_57191(Dynamic<T> dynamic) {
        Optional<Dynamic<T>> optional = dynamic.get("Color").result();
        Optional<Dynamic<T>> optional2 = dynamic.get("effects").result();
        Optional<Dynamic<T>> optional3 = dynamic.get("Potion").result();
        dynamic = dynamic.remove("Color").remove("effects").remove("Potion");
        if (optional.isEmpty() && optional2.isEmpty() && optional3.isEmpty()) {
            return dynamic;
        }
        Dynamic dynamic2 = dynamic.emptyMap();
        if (optional.isPresent()) {
            dynamic2 = dynamic2.set("custom_color", optional.get());
        }
        if (optional2.isPresent()) {
            dynamic2 = dynamic2.set("custom_effects", optional2.get());
        }
        if (optional3.isPresent()) {
            dynamic2 = dynamic2.set("potion", optional3.get());
        }
        return dynamic.set("potion_contents", dynamic2);
    }
}

