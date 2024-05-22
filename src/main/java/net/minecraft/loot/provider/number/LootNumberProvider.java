/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.provider.number;

import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextAware;
import net.minecraft.loot.provider.number.LootNumberProviderType;

public interface LootNumberProvider
extends LootContextAware {
    public float nextFloat(LootContext var1);

    default public int nextInt(LootContext context) {
        return Math.round(this.nextFloat(context));
    }

    public LootNumberProviderType getType();
}

