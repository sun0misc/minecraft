/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure.rule;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.structure.rule.RuleTestType;
import net.minecraft.util.math.random.Random;

public abstract class RuleTest {
    public static final Codec<RuleTest> TYPE_CODEC = Registries.RULE_TEST.getCodec().dispatch("predicate_type", RuleTest::getType, RuleTestType::codec);

    public abstract boolean test(BlockState var1, Random var2);

    protected abstract RuleTestType<?> getType();
}

