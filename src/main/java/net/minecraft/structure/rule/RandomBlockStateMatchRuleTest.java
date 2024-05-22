/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure.rule;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.structure.rule.RuleTestType;
import net.minecraft.util.math.random.Random;

public class RandomBlockStateMatchRuleTest
extends RuleTest {
    public static final MapCodec<RandomBlockStateMatchRuleTest> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)BlockState.CODEC.fieldOf("block_state")).forGetter(ruleTest -> ruleTest.blockState), ((MapCodec)Codec.FLOAT.fieldOf("probability")).forGetter(ruleTest -> Float.valueOf(ruleTest.probability))).apply((Applicative<RandomBlockStateMatchRuleTest, ?>)instance, RandomBlockStateMatchRuleTest::new));
    private final BlockState blockState;
    private final float probability;

    public RandomBlockStateMatchRuleTest(BlockState blockState, float probability) {
        this.blockState = blockState;
        this.probability = probability;
    }

    @Override
    public boolean test(BlockState state, Random random) {
        return state == this.blockState && random.nextFloat() < this.probability;
    }

    @Override
    protected RuleTestType<?> getType() {
        return RuleTestType.RANDOM_BLOCKSTATE_MATCH;
    }
}

