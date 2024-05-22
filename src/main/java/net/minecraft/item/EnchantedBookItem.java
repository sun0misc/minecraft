/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class EnchantedBookItem
extends Item {
    public EnchantedBookItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    public static ItemStack forEnchantment(EnchantmentLevelEntry info) {
        ItemStack lv = new ItemStack(Items.ENCHANTED_BOOK);
        lv.addEnchantment(info.enchantment, info.level);
        return lv;
    }
}

