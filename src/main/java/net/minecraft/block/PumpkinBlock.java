/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class PumpkinBlock
extends Block {
    public static final MapCodec<PumpkinBlock> CODEC = PumpkinBlock.createCodec(PumpkinBlock::new);

    public MapCodec<PumpkinBlock> getCodec() {
        return CODEC;
    }

    protected PumpkinBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!stack.isOf(Items.SHEARS)) {
            return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
        }
        if (world.isClient) {
            return ItemActionResult.success(world.isClient);
        }
        Direction lv = hit.getSide();
        Direction lv2 = lv.getAxis() == Direction.Axis.Y ? player.getHorizontalFacing().getOpposite() : lv;
        world.playSound(null, pos, SoundEvents.BLOCK_PUMPKIN_CARVE, SoundCategory.BLOCKS, 1.0f, 1.0f);
        world.setBlockState(pos, (BlockState)Blocks.CARVED_PUMPKIN.getDefaultState().with(CarvedPumpkinBlock.FACING, lv2), Block.NOTIFY_ALL_AND_REDRAW);
        ItemEntity lv3 = new ItemEntity(world, (double)pos.getX() + 0.5 + (double)lv2.getOffsetX() * 0.65, (double)pos.getY() + 0.1, (double)pos.getZ() + 0.5 + (double)lv2.getOffsetZ() * 0.65, new ItemStack(Items.PUMPKIN_SEEDS, 4));
        lv3.setVelocity(0.05 * (double)lv2.getOffsetX() + world.random.nextDouble() * 0.02, 0.05, 0.05 * (double)lv2.getOffsetZ() + world.random.nextDouble() * 0.02);
        world.spawnEntity(lv3);
        stack.damage(1, player, LivingEntity.getSlotForHand(hand));
        world.emitGameEvent((Entity)player, GameEvent.SHEAR, pos);
        player.incrementStat(Stats.USED.getOrCreateStat(Items.SHEARS));
        return ItemActionResult.success(world.isClient);
    }
}

