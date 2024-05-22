/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure.rule;

import com.mojang.serialization.MapCodec;
import net.minecraft.structure.rule.PosRuleTest;
import net.minecraft.structure.rule.PosRuleTestType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class AlwaysTruePosRuleTest
extends PosRuleTest {
    public static final MapCodec<AlwaysTruePosRuleTest> CODEC = MapCodec.unit(() -> INSTANCE);
    public static final AlwaysTruePosRuleTest INSTANCE = new AlwaysTruePosRuleTest();

    private AlwaysTruePosRuleTest() {
    }

    @Override
    public boolean test(BlockPos originalPos, BlockPos currentPos, BlockPos pivot, Random random) {
        return true;
    }

    @Override
    protected PosRuleTestType<?> getType() {
        return PosRuleTestType.ALWAYS_TRUE;
    }
}

