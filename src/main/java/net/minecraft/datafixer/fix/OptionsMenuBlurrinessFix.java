/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.TypeReferences;

public class OptionsMenuBlurrinessFix
extends DataFix {
    public OptionsMenuBlurrinessFix(Schema schema) {
        super(schema, false);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("OptionsMenuBlurrinessFix", this.getInputSchema().getType(TypeReferences.OPTIONS), typed -> typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("menuBackgroundBlurriness", dynamic -> dynamic.createInt(this.update(dynamic.asString("0.5"))))));
    }

    private int update(String value) {
        try {
            return Math.round(Float.parseFloat(value) * 10.0f);
        } catch (NumberFormatException numberFormatException) {
            return 5;
        }
    }
}

