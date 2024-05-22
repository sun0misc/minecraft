/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.treedecorator;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

public class AttachedToLeavesTreeDecorator
extends TreeDecorator {
    public static final MapCodec<AttachedToLeavesTreeDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("probability")).forGetter(treeDecorator -> Float.valueOf(treeDecorator.probability)), ((MapCodec)Codec.intRange(0, 16).fieldOf("exclusion_radius_xz")).forGetter(treeDecorator -> treeDecorator.exclusionRadiusXZ), ((MapCodec)Codec.intRange(0, 16).fieldOf("exclusion_radius_y")).forGetter(treeDecorator -> treeDecorator.exclusionRadiusY), ((MapCodec)BlockStateProvider.TYPE_CODEC.fieldOf("block_provider")).forGetter(treeDecorator -> treeDecorator.blockProvider), ((MapCodec)Codec.intRange(1, 16).fieldOf("required_empty_blocks")).forGetter(treeDecorator -> treeDecorator.requiredEmptyBlocks), ((MapCodec)Codecs.nonEmptyList(Direction.CODEC.listOf()).fieldOf("directions")).forGetter(treeDecorator -> treeDecorator.directions)).apply((Applicative<AttachedToLeavesTreeDecorator, ?>)instance, AttachedToLeavesTreeDecorator::new));
    protected final float probability;
    protected final int exclusionRadiusXZ;
    protected final int exclusionRadiusY;
    protected final BlockStateProvider blockProvider;
    protected final int requiredEmptyBlocks;
    protected final List<Direction> directions;

    public AttachedToLeavesTreeDecorator(float probability, int exclusionRadiusXZ, int exclusionRadiusY, BlockStateProvider blockProvider, int requiredEmptyBlocks, List<Direction> directions) {
        this.probability = probability;
        this.exclusionRadiusXZ = exclusionRadiusXZ;
        this.exclusionRadiusY = exclusionRadiusY;
        this.blockProvider = blockProvider;
        this.requiredEmptyBlocks = requiredEmptyBlocks;
        this.directions = directions;
    }

    @Override
    public void generate(TreeDecorator.Generator generator) {
        HashSet<BlockPos> set = new HashSet<BlockPos>();
        Random lv = generator.getRandom();
        for (BlockPos lv2 : Util.copyShuffled(generator.getLeavesPositions(), lv)) {
            Direction lv3;
            BlockPos lv4 = lv2.offset(lv3 = Util.getRandom(this.directions, lv));
            if (set.contains(lv4) || !(lv.nextFloat() < this.probability) || !this.meetsRequiredEmptyBlocks(generator, lv2, lv3)) continue;
            BlockPos lv5 = lv4.add(-this.exclusionRadiusXZ, -this.exclusionRadiusY, -this.exclusionRadiusXZ);
            BlockPos lv6 = lv4.add(this.exclusionRadiusXZ, this.exclusionRadiusY, this.exclusionRadiusXZ);
            for (BlockPos lv7 : BlockPos.iterate(lv5, lv6)) {
                set.add(lv7.toImmutable());
            }
            generator.replace(lv4, this.blockProvider.get(lv, lv4));
        }
    }

    private boolean meetsRequiredEmptyBlocks(TreeDecorator.Generator generator, BlockPos pos, Direction direction) {
        for (int i = 1; i <= this.requiredEmptyBlocks; ++i) {
            BlockPos lv = pos.offset(direction, i);
            if (generator.isAir(lv)) continue;
            return false;
        }
        return true;
    }

    @Override
    protected TreeDecoratorType<?> getType() {
        return TreeDecoratorType.ATTACHED_TO_LEAVES;
    }
}

