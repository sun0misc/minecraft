/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure.rule;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.structure.rule.PosRuleTest;
import net.minecraft.structure.rule.PosRuleTestType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public class AxisAlignedLinearPosRuleTest
extends PosRuleTest {
    public static final MapCodec<AxisAlignedLinearPosRuleTest> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("min_chance")).orElse(Float.valueOf(0.0f)).forGetter(ruleTest -> Float.valueOf(ruleTest.minChance)), ((MapCodec)Codec.FLOAT.fieldOf("max_chance")).orElse(Float.valueOf(0.0f)).forGetter(ruleTest -> Float.valueOf(ruleTest.maxChance)), ((MapCodec)Codec.INT.fieldOf("min_dist")).orElse(0).forGetter(ruleTest -> ruleTest.minDistance), ((MapCodec)Codec.INT.fieldOf("max_dist")).orElse(0).forGetter(ruleTest -> ruleTest.maxDistance), ((MapCodec)Direction.Axis.CODEC.fieldOf("axis")).orElse(Direction.Axis.Y).forGetter(ruleTest -> ruleTest.axis)).apply((Applicative<AxisAlignedLinearPosRuleTest, ?>)instance, AxisAlignedLinearPosRuleTest::new));
    private final float minChance;
    private final float maxChance;
    private final int minDistance;
    private final int maxDistance;
    private final Direction.Axis axis;

    public AxisAlignedLinearPosRuleTest(float minChance, float maxChance, int minDistance, int maxDistance, Direction.Axis axis) {
        if (minDistance >= maxDistance) {
            throw new IllegalArgumentException("Invalid range: [" + minDistance + "," + maxDistance + "]");
        }
        this.minChance = minChance;
        this.maxChance = maxChance;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.axis = axis;
    }

    @Override
    public boolean test(BlockPos originalPos, BlockPos currentPos, BlockPos pivot, Random random) {
        Direction lv = Direction.get(Direction.AxisDirection.POSITIVE, this.axis);
        float f = Math.abs((currentPos.getX() - pivot.getX()) * lv.getOffsetX());
        float g = Math.abs((currentPos.getY() - pivot.getY()) * lv.getOffsetY());
        float h = Math.abs((currentPos.getZ() - pivot.getZ()) * lv.getOffsetZ());
        int i = (int)(f + g + h);
        float j = random.nextFloat();
        return j <= MathHelper.clampedLerp(this.minChance, this.maxChance, MathHelper.getLerpProgress(i, this.minDistance, this.maxDistance));
    }

    @Override
    protected PosRuleTestType<?> getType() {
        return PosRuleTestType.AXIS_ALIGNED_LINEAR_POS;
    }
}

