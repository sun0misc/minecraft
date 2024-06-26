/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class MilkBucketItem
extends Item {
    private static final int MAX_USE_TIME = 32;

    public MilkBucketItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv = (ServerPlayerEntity)user;
            Criteria.CONSUME_ITEM.trigger(lv, stack);
            lv.incrementStat(Stats.USED.getOrCreateStat(this));
        }
        if (!world.isClient) {
            user.clearStatusEffects();
        }
        if (user instanceof PlayerEntity) {
            PlayerEntity lv2 = (PlayerEntity)user;
            return ItemUsage.exchangeStack(stack, lv2, new ItemStack(Items.BUCKET), false);
        }
        stack.decrementUnlessCreative(1, user);
        return stack;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 32;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }
}

