/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.blockpredicate;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;

public class WouldSurviveBlockPredicate
implements BlockPredicate {
    public static final MapCodec<WouldSurviveBlockPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Vec3i.createOffsetCodec(16).optionalFieldOf("offset", Vec3i.ZERO).forGetter(predicate -> predicate.offset), ((MapCodec)BlockState.CODEC.fieldOf("state")).forGetter(predicate -> predicate.state)).apply((Applicative<WouldSurviveBlockPredicate, ?>)instance, WouldSurviveBlockPredicate::new));
    private final Vec3i offset;
    private final BlockState state;

    protected WouldSurviveBlockPredicate(Vec3i offset, BlockState state) {
        this.offset = offset;
        this.state = state;
    }

    @Override
    public boolean test(StructureWorldAccess arg, BlockPos arg2) {
        return this.state.canPlaceAt(arg, arg2.add(this.offset));
    }

    @Override
    public BlockPredicateType<?> getType() {
        return BlockPredicateType.WOULD_SURVIVE;
    }

    @Override
    public /* synthetic */ boolean test(Object world, Object pos) {
        return this.test((StructureWorldAccess)world, (BlockPos)pos);
    }
}

