/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.EntityTransformFix;
import net.minecraft.util.Util;

public class EntityMinecartIdentifiersFix
extends EntityTransformFix {
    public EntityMinecartIdentifiersFix(Schema schema) {
        super("EntityMinecartIdentifiersFix", schema, true);
    }

    @Override
    protected Pair<String, Typed<?>> transform(String choice, Typed<?> typed) {
        if (!choice.equals("Minecart")) {
            return Pair.of(choice, typed);
        }
        int i = typed.getOrCreate(DSL.remainderFinder()).get("Type").asInt(0);
        String string2 = switch (i) {
            default -> "MinecartRideable";
            case 1 -> "MinecartChest";
            case 2 -> "MinecartFurnace";
        };
        Type<?> type = this.getOutputSchema().findChoiceType(TypeReferences.ENTITY).types().get(string2);
        return Pair.of(string2, Util.apply(typed, type, dynamic -> dynamic.remove("Type")));
    }
}

