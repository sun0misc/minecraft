/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.MathHelper;

public interface EnchantmentLevelBasedValueType {
    public static final Codec<EnchantmentLevelBasedValueType> BASE_CODEC = Registries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE.getCodec().dispatch(EnchantmentLevelBasedValueType::getCodec, codec -> codec);
    public static final Codec<EnchantmentLevelBasedValueType> CODEC = Codec.either(Constant.CODEC, BASE_CODEC).xmap(either -> either.map(type -> type, type -> type), type -> {
        Either<Object, EnchantmentLevelBasedValueType> either;
        if (type instanceof Constant) {
            Constant lv = (Constant)type;
            either = Either.left(lv);
        } else {
            either = Either.right(type);
        }
        return either;
    });

    public static MapCodec<? extends EnchantmentLevelBasedValueType> registerAndGetDefault(Registry<MapCodec<? extends EnchantmentLevelBasedValueType>> registry) {
        Registry.register(registry, "clamped", Clamped.CODEC);
        Registry.register(registry, "fraction", Fraction.CODEC);
        Registry.register(registry, "levels_squared", LevelsSquared.CODEC);
        return Registry.register(registry, "linear", Linear.CODEC);
    }

    public static Constant constant(float value) {
        return new Constant(value);
    }

    public static Linear linear(float base, float perLevelAboveFirst) {
        return new Linear(base, perLevelAboveFirst);
    }

    public static Linear linear(float base) {
        return EnchantmentLevelBasedValueType.linear(base, base);
    }

    public float getValue(int var1);

    public MapCodec<? extends EnchantmentLevelBasedValueType> getCodec();

    public record Clamped(EnchantmentLevelBasedValueType value, float min, float max) implements EnchantmentLevelBasedValueType
    {
        public static final MapCodec<Clamped> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)CODEC.fieldOf("value")).forGetter(Clamped::value), ((MapCodec)Codec.FLOAT.fieldOf("min")).forGetter(Clamped::min), ((MapCodec)Codec.FLOAT.fieldOf("max")).forGetter(Clamped::max)).apply((Applicative<Clamped, ?>)instance, Clamped::new)).validate(type -> {
            if (type.max <= type.min) {
                return DataResult.error(() -> "Max must be larger than min, min: " + arg.min + ", max: " + arg.max);
            }
            return DataResult.success(type);
        });

        @Override
        public float getValue(int level) {
            return MathHelper.clamp(this.value.getValue(level), this.min, this.max);
        }

        public MapCodec<Clamped> getCodec() {
            return CODEC;
        }
    }

    public record Fraction(EnchantmentLevelBasedValueType numerator, EnchantmentLevelBasedValueType denominator) implements EnchantmentLevelBasedValueType
    {
        public static final MapCodec<Fraction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)CODEC.fieldOf("numerator")).forGetter(Fraction::numerator), ((MapCodec)CODEC.fieldOf("denominator")).forGetter(Fraction::denominator)).apply((Applicative<Fraction, ?>)instance, Fraction::new));

        @Override
        public float getValue(int level) {
            float f = this.denominator.getValue(level);
            if (f == 0.0f) {
                return 0.0f;
            }
            return this.numerator.getValue(level) / f;
        }

        public MapCodec<Fraction> getCodec() {
            return CODEC;
        }
    }

    public record LevelsSquared(float added) implements EnchantmentLevelBasedValueType
    {
        public static final MapCodec<LevelsSquared> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("added")).forGetter(LevelsSquared::added)).apply((Applicative<LevelsSquared, ?>)instance, LevelsSquared::new));

        @Override
        public float getValue(int level) {
            return (float)MathHelper.square(level) + this.added;
        }

        public MapCodec<LevelsSquared> getCodec() {
            return CODEC;
        }
    }

    public record Linear(float base, float perLevelAboveFirst) implements EnchantmentLevelBasedValueType
    {
        public static final MapCodec<Linear> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("base")).forGetter(Linear::base), ((MapCodec)Codec.FLOAT.fieldOf("per_level_above_first")).forGetter(Linear::perLevelAboveFirst)).apply((Applicative<Linear, ?>)instance, Linear::new));

        @Override
        public float getValue(int level) {
            return this.base + this.perLevelAboveFirst * (float)(level - 1);
        }

        public MapCodec<Linear> getCodec() {
            return CODEC;
        }
    }

    public record Constant(float value) implements EnchantmentLevelBasedValueType
    {
        public static final Codec<Constant> CODEC = Codec.FLOAT.xmap(Constant::new, Constant::value);
        public static final MapCodec<Constant> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("value")).forGetter(Constant::value)).apply((Applicative<Constant, ?>)instance, Constant::new));

        @Override
        public float getValue(int level) {
            return this.value;
        }

        public MapCodec<Constant> getCodec() {
            return TYPE_CODEC;
        }
    }
}

