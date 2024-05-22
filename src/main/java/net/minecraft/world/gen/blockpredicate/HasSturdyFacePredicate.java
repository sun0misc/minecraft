/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.blockpredicate;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;

public class HasSturdyFacePredicate
implements BlockPredicate {
    private final Vec3i offset;
    private final Direction face;
    public static final MapCodec<HasSturdyFacePredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Vec3i.createOffsetCodec(16).optionalFieldOf("offset", Vec3i.ZERO).forGetter(predicate -> predicate.offset), ((MapCodec)Direction.CODEC.fieldOf("direction")).forGetter(predicate -> predicate.face)).apply((Applicative<HasSturdyFacePredicate, ?>)instance, HasSturdyFacePredicate::new));

    public HasSturdyFacePredicate(Vec3i offset, Direction face) {
        this.offset = offset;
        this.face = face;
    }

    @Override
    public boolean test(StructureWorldAccess arg, BlockPos arg2) {
        BlockPos lv = arg2.add(this.offset);
        return arg.getBlockState(lv).isSideSolidFullSquare(arg, lv, this.face);
    }

    @Override
    public BlockPredicateType<?> getType() {
        return BlockPredicateType.HAS_STURDY_FACE;
    }

    @Override
    public /* synthetic */ boolean test(Object world, Object pos) {
        return this.test((StructureWorldAccess)world, (BlockPos)pos);
    }
}

