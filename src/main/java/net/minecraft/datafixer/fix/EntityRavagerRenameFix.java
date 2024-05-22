/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;
import java.util.Objects;
import net.minecraft.datafixer.fix.EntityRenameFix;

public class EntityRavagerRenameFix
extends EntityRenameFix {
    public static final Map<String, String> ITEMS = ImmutableMap.builder().put("minecraft:illager_beast_spawn_egg", "minecraft:ravager_spawn_egg").build();

    public EntityRavagerRenameFix(Schema schema, boolean bl) {
        super("EntityRavagerRenameFix", schema, bl);
    }

    @Override
    protected String rename(String oldName) {
        return Objects.equals("minecraft:illager_beast", oldName) ? "minecraft:ravager" : oldName;
    }
}

