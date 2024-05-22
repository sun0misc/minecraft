/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class LeadItem
extends Item {
    public LeadItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos lv2;
        World lv = context.getWorld();
        BlockState lv3 = lv.getBlockState(lv2 = context.getBlockPos());
        if (lv3.isIn(BlockTags.FENCES)) {
            PlayerEntity lv4 = context.getPlayer();
            if (!lv.isClient && lv4 != null) {
                LeadItem.attachHeldMobsToBlock(lv4, lv, lv2);
            }
            return ActionResult.success(lv.isClient);
        }
        return ActionResult.PASS;
    }

    public static ActionResult attachHeldMobsToBlock(PlayerEntity player, World world, BlockPos pos) {
        LeashKnotEntity lv = null;
        double d = 7.0;
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        Box lv2 = new Box((double)i - 7.0, (double)j - 7.0, (double)k - 7.0, (double)i + 7.0, (double)j + 7.0, (double)k + 7.0);
        List<MobEntity> list = world.getEntitiesByClass(MobEntity.class, lv2, entity -> entity.getHoldingEntity() == player);
        for (MobEntity lv3 : list) {
            if (lv == null) {
                lv = LeashKnotEntity.getOrCreate(world, pos);
                lv.onPlace();
            }
            lv3.attachLeash(lv, true);
        }
        if (!list.isEmpty()) {
            world.emitGameEvent(GameEvent.BLOCK_ATTACH, pos, GameEvent.Emitter.of(player));
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}

