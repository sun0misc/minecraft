/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;
import net.minecraft.util.math.random.Random;

public class EntityZombieVillagerTypeFix
extends ChoiceFix {
    private static final int field_29883 = 6;

    public EntityZombieVillagerTypeFix(Schema schema, boolean bl) {
        super(schema, bl, "EntityZombieVillagerTypeFix", TypeReferences.ENTITY, "Zombie");
    }

    public Dynamic<?> fixZombieType(Dynamic<?> dynamic) {
        if (dynamic.get("IsVillager").asBoolean(false)) {
            if (dynamic.get("ZombieType").result().isEmpty()) {
                int i = this.clampType(dynamic.get("VillagerProfession").asInt(-1));
                if (i == -1) {
                    i = this.clampType(Random.create().nextInt(6));
                }
                dynamic = dynamic.set("ZombieType", dynamic.createInt(i));
            }
            dynamic = dynamic.remove("IsVillager");
        }
        return dynamic;
    }

    private int clampType(int type) {
        if (type < 0 || type >= 6) {
            return -1;
        }
        return type;
    }

    @Override
    protected Typed<?> transform(Typed<?> inputType) {
        return inputType.update(DSL.remainderFinder(), this::fixZombieType);
    }
}

