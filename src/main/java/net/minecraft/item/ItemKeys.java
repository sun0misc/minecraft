/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ItemKeys {
    public static final RegistryKey<Item> PUMPKIN_SEEDS = ItemKeys.of("pumpkin_seeds");
    public static final RegistryKey<Item> MELON_SEEDS = ItemKeys.of("melon_seeds");

    private static RegistryKey<Item> of(String id) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.method_60656(id));
    }
}

