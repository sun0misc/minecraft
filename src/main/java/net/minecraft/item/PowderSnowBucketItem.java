/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class PowderSnowBucketItem
extends BlockItem
implements FluidModificationItem {
    private final SoundEvent placeSound;

    public PowderSnowBucketItem(Block block, SoundEvent placeSound, Item.Settings settings) {
        super(block, settings);
        this.placeSound = placeSound;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ActionResult lv = super.useOnBlock(context);
        PlayerEntity lv2 = context.getPlayer();
        if (lv.isAccepted() && lv2 != null) {
            lv2.setStackInHand(context.getHand(), BucketItem.getEmptiedStack(context.getStack(), lv2));
        }
        return lv;
    }

    @Override
    public String getTranslationKey() {
        return this.getOrCreateTranslationKey();
    }

    @Override
    protected SoundEvent getPlaceSound(BlockState state) {
        return this.placeSound;
    }

    @Override
    public boolean placeFluid(@Nullable PlayerEntity player, World world, BlockPos pos, @Nullable BlockHitResult hitResult) {
        if (world.isInBuildLimit(pos) && world.isAir(pos)) {
            if (!world.isClient) {
                world.setBlockState(pos, this.getBlock().getDefaultState(), Block.NOTIFY_ALL);
            }
            world.emitGameEvent((Entity)player, GameEvent.FLUID_PLACE, pos);
            world.playSound(player, pos, this.placeSound, SoundCategory.BLOCKS, 1.0f, 1.0f);
            return true;
        }
        return false;
    }
}

