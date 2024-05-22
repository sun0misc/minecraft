/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.event.GameEvent;

public class SaddleItem
extends Item {
    public SaddleItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (entity instanceof Saddleable) {
            Saddleable lv = (Saddleable)((Object)entity);
            if (entity.isAlive() && !lv.isSaddled() && lv.canBeSaddled()) {
                if (!user.getWorld().isClient) {
                    lv.saddle(SoundCategory.NEUTRAL);
                    entity.getWorld().emitGameEvent((Entity)entity, GameEvent.EQUIP, entity.getPos());
                    stack.decrement(1);
                }
                return ActionResult.success(user.getWorld().isClient);
            }
        }
        return ActionResult.PASS;
    }
}

