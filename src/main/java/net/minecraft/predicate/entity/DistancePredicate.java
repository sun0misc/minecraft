/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.predicate.NumberRange;
import net.minecraft.util.math.MathHelper;

public record DistancePredicate(NumberRange.DoubleRange x, NumberRange.DoubleRange y, NumberRange.DoubleRange z, NumberRange.DoubleRange horizontal, NumberRange.DoubleRange absolute) {
    public static final Codec<DistancePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(NumberRange.DoubleRange.CODEC.optionalFieldOf("x", NumberRange.DoubleRange.ANY).forGetter(DistancePredicate::x), NumberRange.DoubleRange.CODEC.optionalFieldOf("y", NumberRange.DoubleRange.ANY).forGetter(DistancePredicate::y), NumberRange.DoubleRange.CODEC.optionalFieldOf("z", NumberRange.DoubleRange.ANY).forGetter(DistancePredicate::z), NumberRange.DoubleRange.CODEC.optionalFieldOf("horizontal", NumberRange.DoubleRange.ANY).forGetter(DistancePredicate::horizontal), NumberRange.DoubleRange.CODEC.optionalFieldOf("absolute", NumberRange.DoubleRange.ANY).forGetter(DistancePredicate::absolute)).apply((Applicative<DistancePredicate, ?>)instance, DistancePredicate::new));

    public static DistancePredicate horizontal(NumberRange.DoubleRange horizontal) {
        return new DistancePredicate(NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, horizontal, NumberRange.DoubleRange.ANY);
    }

    public static DistancePredicate y(NumberRange.DoubleRange y) {
        return new DistancePredicate(NumberRange.DoubleRange.ANY, y, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY);
    }

    public static DistancePredicate absolute(NumberRange.DoubleRange absolute) {
        return new DistancePredicate(NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, absolute);
    }

    public boolean test(double x0, double y0, double z0, double x1, double y1, double z1) {
        float j = (float)(x0 - x1);
        float k = (float)(y0 - y1);
        float l = (float)(z0 - z1);
        if (!(this.x.test(MathHelper.abs(j)) && this.y.test(MathHelper.abs(k)) && this.z.test(MathHelper.abs(l)))) {
            return false;
        }
        if (!this.horizontal.testSqrt(j * j + l * l)) {
            return false;
        }
        return this.absolute.testSqrt(j * j + k * k + l * l);
    }
}

