/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class VerticallyAttachableBlockItem
extends BlockItem {
    protected final Block wallBlock;
    private final Direction verticalAttachmentDirection;

    public VerticallyAttachableBlockItem(Block standingBlock, Block wallBlock, Item.Settings settings, Direction verticalAttachmentDirection) {
        super(standingBlock, settings);
        this.wallBlock = wallBlock;
        this.verticalAttachmentDirection = verticalAttachmentDirection;
    }

    protected boolean canPlaceAt(WorldView world, BlockState state, BlockPos pos) {
        return state.canPlaceAt(world, pos);
    }

    @Override
    @Nullable
    protected BlockState getPlacementState(ItemPlacementContext context) {
        BlockState lv = this.wallBlock.getPlacementState(context);
        BlockState lv2 = null;
        World lv3 = context.getWorld();
        BlockPos lv4 = context.getBlockPos();
        for (Direction lv5 : context.getPlacementDirections()) {
            BlockState lv6;
            if (lv5 == this.verticalAttachmentDirection.getOpposite()) continue;
            BlockState blockState = lv6 = lv5 == this.verticalAttachmentDirection ? this.getBlock().getPlacementState(context) : lv;
            if (lv6 == null || !this.canPlaceAt(lv3, lv6, lv4)) continue;
            lv2 = lv6;
            break;
        }
        return lv2 != null && lv3.canPlace(lv2, lv4, ShapeContext.absent()) ? lv2 : null;
    }

    @Override
    public void appendBlocks(Map<Block, Item> map, Item item) {
        super.appendBlocks(map, item);
        map.put(this.wallBlock, item);
    }
}

