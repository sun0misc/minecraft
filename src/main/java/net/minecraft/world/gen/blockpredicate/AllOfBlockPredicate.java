/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.blockpredicate;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;
import net.minecraft.world.gen.blockpredicate.CombinedBlockPredicate;

class AllOfBlockPredicate
extends CombinedBlockPredicate {
    public static final MapCodec<AllOfBlockPredicate> CODEC = AllOfBlockPredicate.buildCodec(AllOfBlockPredicate::new);

    public AllOfBlockPredicate(List<BlockPredicate> list) {
        super(list);
    }

    @Override
    public boolean test(StructureWorldAccess arg, BlockPos arg2) {
        for (BlockPredicate lv : this.predicates) {
            if (lv.test(arg, arg2)) continue;
            return false;
        }
        return true;
    }

    @Override
    public BlockPredicateType<?> getType() {
        return BlockPredicateType.ALL_OF;
    }

    @Override
    public /* synthetic */ boolean test(Object world, Object pos) {
        return this.test((StructureWorldAccess)world, (BlockPos)pos);
    }
}

