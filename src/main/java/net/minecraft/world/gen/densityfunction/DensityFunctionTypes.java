/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.densityfunction;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.function.ToFloatFunction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.noise.InterpolatedNoiseSampler;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctions;
import org.slf4j.Logger;

public final class DensityFunctionTypes {
    private static final Codec<DensityFunction> DYNAMIC_RANGE = Registries.DENSITY_FUNCTION_TYPE.getCodec().dispatch(densityFunction -> densityFunction.getCodecHolder().codec(), Function.identity());
    protected static final double MAX_CONSTANT_VALUE = 1000000.0;
    static final Codec<Double> CONSTANT_RANGE = Codec.doubleRange(-1000000.0, 1000000.0);
    public static final Codec<DensityFunction> CODEC = Codec.either(CONSTANT_RANGE, DYNAMIC_RANGE).xmap(either -> either.map(DensityFunctionTypes::constant, Function.identity()), densityFunction -> {
        if (densityFunction instanceof Constant) {
            Constant lv = (Constant)densityFunction;
            return Either.left(lv.value());
        }
        return Either.right(densityFunction);
    });

    public static MapCodec<? extends DensityFunction> registerAndGetDefault(Registry<MapCodec<? extends DensityFunction>> registry) {
        DensityFunctionTypes.register(registry, "blend_alpha", BlendAlpha.CODEC);
        DensityFunctionTypes.register(registry, "blend_offset", BlendOffset.CODEC);
        DensityFunctionTypes.register(registry, "beardifier", Beardifier.CODEC_HOLDER);
        DensityFunctionTypes.register(registry, "old_blended_noise", InterpolatedNoiseSampler.CODEC);
        for (Wrapping.Type type : Wrapping.Type.values()) {
            DensityFunctionTypes.register(registry, type.asString(), type.codec);
        }
        DensityFunctionTypes.register(registry, "noise", Noise.CODEC_HOLDER);
        DensityFunctionTypes.register(registry, "end_islands", EndIslands.CODEC_HOLDER);
        DensityFunctionTypes.register(registry, "weird_scaled_sampler", WeirdScaledSampler.CODEC_HOLDER);
        DensityFunctionTypes.register(registry, "shifted_noise", ShiftedNoise.CODEC_HOLDER);
        DensityFunctionTypes.register(registry, "range_choice", RangeChoice.CODEC_HOLDER);
        DensityFunctionTypes.register(registry, "shift_a", ShiftA.CODEC_HOLDER);
        DensityFunctionTypes.register(registry, "shift_b", ShiftB.CODEC_HOLDER);
        DensityFunctionTypes.register(registry, "shift", Shift.CODEC_HOLDER);
        DensityFunctionTypes.register(registry, "blend_density", BlendDensity.CODEC_HOLDER);
        DensityFunctionTypes.register(registry, "clamp", Clamp.CODEC_HOLDER);
        for (Enum enum_ : UnaryOperation.Type.values()) {
            DensityFunctionTypes.register(registry, ((UnaryOperation.Type)enum_).asString(), ((UnaryOperation.Type)enum_).codecHolder);
        }
        for (Enum enum_ : BinaryOperationLike.Type.values()) {
            DensityFunctionTypes.register(registry, ((BinaryOperationLike.Type)enum_).asString(), ((BinaryOperationLike.Type)enum_).codecHolder);
        }
        DensityFunctionTypes.register(registry, "spline", Spline.CODEC_HOLDER);
        DensityFunctionTypes.register(registry, "constant", Constant.CODEC_HOLDER);
        return DensityFunctionTypes.register(registry, "y_clamped_gradient", YClampedGradient.CODEC_HOLDER);
    }

    private static MapCodec<? extends DensityFunction> register(Registry<MapCodec<? extends DensityFunction>> registry, String id, CodecHolder<? extends DensityFunction> codecHolder) {
        return Registry.register(registry, id, codecHolder.codec());
    }

    static <A, O> CodecHolder<O> holderOf(Codec<A> codec, Function<A, O> creator, Function<O, A> argumentGetter) {
        return CodecHolder.of(((MapCodec)codec.fieldOf("argument")).xmap(creator, argumentGetter));
    }

    static <O> CodecHolder<O> holderOf(Function<DensityFunction, O> creator, Function<O, DensityFunction> argumentGetter) {
        return DensityFunctionTypes.holderOf(DensityFunction.FUNCTION_CODEC, creator, argumentGetter);
    }

    static <O> CodecHolder<O> holderOf(BiFunction<DensityFunction, DensityFunction, O> creator, Function<O, DensityFunction> argument1Getter, Function<O, DensityFunction> argument2Getter) {
        return CodecHolder.of(RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)DensityFunction.FUNCTION_CODEC.fieldOf("argument1")).forGetter(argument1Getter), ((MapCodec)DensityFunction.FUNCTION_CODEC.fieldOf("argument2")).forGetter(argument2Getter)).apply(instance, creator)));
    }

    static <O> CodecHolder<O> holderOf(MapCodec<O> mapCodec) {
        return CodecHolder.of(mapCodec);
    }

    private DensityFunctionTypes() {
    }

    public static DensityFunction interpolated(DensityFunction inputFunction) {
        return new Wrapping(Wrapping.Type.INTERPOLATED, inputFunction);
    }

    public static DensityFunction flatCache(DensityFunction inputFunction) {
        return new Wrapping(Wrapping.Type.FLAT_CACHE, inputFunction);
    }

    public static DensityFunction cache2d(DensityFunction inputFunction) {
        return new Wrapping(Wrapping.Type.CACHE2D, inputFunction);
    }

    public static DensityFunction cacheOnce(DensityFunction inputFunction) {
        return new Wrapping(Wrapping.Type.CACHE_ONCE, inputFunction);
    }

    public static DensityFunction cacheAllInCell(DensityFunction inputFunction) {
        return new Wrapping(Wrapping.Type.CACHE_ALL_IN_CELL, inputFunction);
    }

    public static DensityFunction noiseInRange(RegistryEntry<DoublePerlinNoiseSampler.NoiseParameters> noiseParameters, @Deprecated double scaleXz, double scaleY, double min, double max) {
        return DensityFunctionTypes.mapRange(new Noise(new DensityFunction.Noise(noiseParameters), scaleXz, scaleY), min, max);
    }

    public static DensityFunction noiseInRange(RegistryEntry<DoublePerlinNoiseSampler.NoiseParameters> noiseParameters, double scaleY, double min, double max) {
        return DensityFunctionTypes.noiseInRange(noiseParameters, 1.0, scaleY, min, max);
    }

    public static DensityFunction noiseInRange(RegistryEntry<DoublePerlinNoiseSampler.NoiseParameters> noiseParameters, double min, double max) {
        return DensityFunctionTypes.noiseInRange(noiseParameters, 1.0, 1.0, min, max);
    }

    public static DensityFunction shiftedNoise(DensityFunction shiftX, DensityFunction shiftZ, double xzScale, RegistryEntry<DoublePerlinNoiseSampler.NoiseParameters> noiseParameters) {
        return new ShiftedNoise(shiftX, DensityFunctionTypes.zero(), shiftZ, xzScale, 0.0, new DensityFunction.Noise(noiseParameters));
    }

    public static DensityFunction noise(RegistryEntry<DoublePerlinNoiseSampler.NoiseParameters> noiseParameters) {
        return DensityFunctionTypes.noise(noiseParameters, 1.0, 1.0);
    }

    public static DensityFunction noise(RegistryEntry<DoublePerlinNoiseSampler.NoiseParameters> noiseParameters, double scaleXz, double scaleY) {
        return new Noise(new DensityFunction.Noise(noiseParameters), scaleXz, scaleY);
    }

    public static DensityFunction noise(RegistryEntry<DoublePerlinNoiseSampler.NoiseParameters> noiseParameters, double scaleY) {
        return DensityFunctionTypes.noise(noiseParameters, 1.0, scaleY);
    }

    public static DensityFunction rangeChoice(DensityFunction input, double minInclusive, double maxExclusive, DensityFunction whenInRange, DensityFunction whenOutOfRange) {
        return new RangeChoice(input, minInclusive, maxExclusive, whenInRange, whenOutOfRange);
    }

    public static DensityFunction shiftA(RegistryEntry<DoublePerlinNoiseSampler.NoiseParameters> noiseParameters) {
        return new ShiftA(new DensityFunction.Noise(noiseParameters));
    }

    public static DensityFunction shiftB(RegistryEntry<DoublePerlinNoiseSampler.NoiseParameters> noiseParameters) {
        return new ShiftB(new DensityFunction.Noise(noiseParameters));
    }

    public static DensityFunction shift(RegistryEntry<DoublePerlinNoiseSampler.NoiseParameters> noiseParameters) {
        return new Shift(new DensityFunction.Noise(noiseParameters));
    }

    public static DensityFunction blendDensity(DensityFunction input) {
        return new BlendDensity(input);
    }

    public static DensityFunction endIslands(long seed) {
        return new EndIslands(seed);
    }

    public static DensityFunction weirdScaledSampler(DensityFunction input, RegistryEntry<DoublePerlinNoiseSampler.NoiseParameters> parameters, WeirdScaledSampler.RarityValueMapper mapper) {
        return new WeirdScaledSampler(input, new DensityFunction.Noise(parameters), mapper);
    }

    public static DensityFunction add(DensityFunction a, DensityFunction b) {
        return BinaryOperationLike.create(BinaryOperationLike.Type.ADD, a, b);
    }

    public static DensityFunction mul(DensityFunction a, DensityFunction b) {
        return BinaryOperationLike.create(BinaryOperationLike.Type.MUL, a, b);
    }

    public static DensityFunction min(DensityFunction a, DensityFunction b) {
        return BinaryOperationLike.create(BinaryOperationLike.Type.MIN, a, b);
    }

    public static DensityFunction max(DensityFunction a, DensityFunction b) {
        return BinaryOperationLike.create(BinaryOperationLike.Type.MAX, a, b);
    }

    public static DensityFunction spline(net.minecraft.util.math.Spline<Spline.SplinePos, Spline.DensityFunctionWrapper> spline) {
        return new Spline(spline);
    }

    public static DensityFunction zero() {
        return Constant.ZERO;
    }

    public static DensityFunction constant(double density) {
        return new Constant(density);
    }

    public static DensityFunction yClampedGradient(int fromY, int toY, double fromValue, double toValue) {
        return new YClampedGradient(fromY, toY, fromValue, toValue);
    }

    public static DensityFunction unary(DensityFunction input, UnaryOperation.Type type) {
        return UnaryOperation.create(type, input);
    }

    private static DensityFunction mapRange(DensityFunction function, double min, double max) {
        double f = (min + max) * 0.5;
        double g = (max - min) * 0.5;
        return DensityFunctionTypes.add(DensityFunctionTypes.constant(f), DensityFunctionTypes.mul(DensityFunctionTypes.constant(g), function));
    }

    public static DensityFunction blendAlpha() {
        return BlendAlpha.INSTANCE;
    }

    public static DensityFunction blendOffset() {
        return BlendOffset.INSTANCE;
    }

    public static DensityFunction lerp(DensityFunction delta, DensityFunction start, DensityFunction end) {
        if (start instanceof Constant) {
            Constant lv = (Constant)start;
            return DensityFunctionTypes.lerp(delta, lv.value, end);
        }
        DensityFunction lv2 = DensityFunctionTypes.cacheOnce(delta);
        DensityFunction lv3 = DensityFunctionTypes.add(DensityFunctionTypes.mul(lv2, DensityFunctionTypes.constant(-1.0)), DensityFunctionTypes.constant(1.0));
        return DensityFunctionTypes.add(DensityFunctionTypes.mul(start, lv3), DensityFunctionTypes.mul(end, lv2));
    }

    public static DensityFunction lerp(DensityFunction delta, double start, DensityFunction end) {
        return DensityFunctionTypes.add(DensityFunctionTypes.mul(delta, DensityFunctionTypes.add(end, DensityFunctionTypes.constant(-start))), DensityFunctionTypes.constant(start));
    }

    protected static enum BlendAlpha implements DensityFunction.Base
    {
        INSTANCE;

        public static final CodecHolder<DensityFunction> CODEC;

        @Override
        public double sample(DensityFunction.NoisePos pos) {
            return 1.0;
        }

        @Override
        public void fill(double[] densities, DensityFunction.EachApplier applier) {
            Arrays.fill(densities, 1.0);
        }

        @Override
        public double minValue() {
            return 1.0;
        }

        @Override
        public double maxValue() {
            return 1.0;
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC;
        }

        static {
            CODEC = CodecHolder.of(MapCodec.unit(INSTANCE));
        }
    }

    protected static enum BlendOffset implements DensityFunction.Base
    {
        INSTANCE;

        public static final CodecHolder<DensityFunction> CODEC;

        @Override
        public double sample(DensityFunction.NoisePos pos) {
            return 0.0;
        }

        @Override
        public void fill(double[] densities, DensityFunction.EachApplier applier) {
            Arrays.fill(densities, 0.0);
        }

        @Override
        public double minValue() {
            return 0.0;
        }

        @Override
        public double maxValue() {
            return 0.0;
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC;
        }

        static {
            CODEC = CodecHolder.of(MapCodec.unit(INSTANCE));
        }
    }

    protected static enum Beardifier implements Beardifying
    {
        INSTANCE;


        @Override
        public double sample(DensityFunction.NoisePos pos) {
            return 0.0;
        }

        @Override
        public void fill(double[] densities, DensityFunction.EachApplier applier) {
            Arrays.fill(densities, 0.0);
        }

        @Override
        public double minValue() {
            return 0.0;
        }

        @Override
        public double maxValue() {
            return 0.0;
        }
    }

    protected record Wrapping(Type type, DensityFunction wrapped) implements Wrapper
    {
        @Override
        public double sample(DensityFunction.NoisePos pos) {
            return this.wrapped.sample(pos);
        }

        @Override
        public void fill(double[] densities, DensityFunction.EachApplier applier) {
            this.wrapped.fill(densities, applier);
        }

        @Override
        public double minValue() {
            return this.wrapped.minValue();
        }

        @Override
        public double maxValue() {
            return this.wrapped.maxValue();
        }

        static enum Type implements StringIdentifiable
        {
            INTERPOLATED("interpolated"),
            FLAT_CACHE("flat_cache"),
            CACHE2D("cache_2d"),
            CACHE_ONCE("cache_once"),
            CACHE_ALL_IN_CELL("cache_all_in_cell");

            private final String name;
            final CodecHolder<Wrapper> codec = DensityFunctionTypes.holderOf(arg -> new Wrapping(this, (DensityFunction)arg), Wrapper::wrapped);

            private Type(String name) {
                this.name = name;
            }

            @Override
            public String asString() {
                return this.name;
            }
        }
    }

    protected record Noise(DensityFunction.Noise noise, @Deprecated double xzScale, double yScale) implements DensityFunction
    {
        public static final MapCodec<Noise> NOISE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)DensityFunction.Noise.CODEC.fieldOf("noise")).forGetter(Noise::noise), ((MapCodec)Codec.DOUBLE.fieldOf("xz_scale")).forGetter(Noise::xzScale), ((MapCodec)Codec.DOUBLE.fieldOf("y_scale")).forGetter(Noise::yScale)).apply((Applicative<Noise, ?>)instance, Noise::new));
        public static final CodecHolder<Noise> CODEC_HOLDER = DensityFunctionTypes.holderOf(NOISE_CODEC);

        @Override
        public double sample(DensityFunction.NoisePos pos) {
            return this.noise.sample((double)pos.blockX() * this.xzScale, (double)pos.blockY() * this.yScale, (double)pos.blockZ() * this.xzScale);
        }

        @Override
        public void fill(double[] densities, DensityFunction.EachApplier applier) {
            applier.fill(densities, this);
        }

        @Override
        public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
            return visitor.apply(new Noise(visitor.apply(this.noise), this.xzScale, this.yScale));
        }

        @Override
        public double minValue() {
            return -this.maxValue();
        }

        @Override
        public double maxValue() {
            return this.noise.getMaxValue();
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC_HOLDER;
        }

        @Deprecated
        public double xzScale() {
            return this.xzScale;
        }
    }

    protected static final class EndIslands
    implements DensityFunction.Base {
        public static final CodecHolder<EndIslands> CODEC_HOLDER = CodecHolder.of(MapCodec.unit(new EndIslands(0L)));
        private static final float field_37677 = -0.9f;
        private final SimplexNoiseSampler sampler;

        public EndIslands(long seed) {
            CheckedRandom lv = new CheckedRandom(seed);
            lv.skip(17292);
            this.sampler = new SimplexNoiseSampler(lv);
        }

        private static float sample(SimplexNoiseSampler sampler, int x, int z) {
            int k = x / 2;
            int l = z / 2;
            int m = x % 2;
            int n = z % 2;
            float f = 100.0f - MathHelper.sqrt(x * x + z * z) * 8.0f;
            f = MathHelper.clamp(f, -100.0f, 80.0f);
            for (int o = -12; o <= 12; ++o) {
                for (int p = -12; p <= 12; ++p) {
                    long q = k + o;
                    long r = l + p;
                    if (q * q + r * r <= 4096L || !(sampler.sample(q, r) < (double)-0.9f)) continue;
                    float g = (MathHelper.abs(q) * 3439.0f + MathHelper.abs(r) * 147.0f) % 13.0f + 9.0f;
                    float h = m - o * 2;
                    float s = n - p * 2;
                    float t = 100.0f - MathHelper.sqrt(h * h + s * s) * g;
                    t = MathHelper.clamp(t, -100.0f, 80.0f);
                    f = Math.max(f, t);
                }
            }
            return f;
        }

        @Override
        public double sample(DensityFunction.NoisePos pos) {
            return ((double)EndIslands.sample(this.sampler, pos.blockX() / 8, pos.blockZ() / 8) - 8.0) / 128.0;
        }

        @Override
        public double minValue() {
            return -0.84375;
        }

        @Override
        public double maxValue() {
            return 0.5625;
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC_HOLDER;
        }
    }

    protected record WeirdScaledSampler(DensityFunction input, DensityFunction.Noise noise, RarityValueMapper rarityValueMapper) implements Positional
    {
        private static final MapCodec<WeirdScaledSampler> WEIRD_SCALED_SAMPLER_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)DensityFunction.FUNCTION_CODEC.fieldOf("input")).forGetter(WeirdScaledSampler::input), ((MapCodec)DensityFunction.Noise.CODEC.fieldOf("noise")).forGetter(WeirdScaledSampler::noise), ((MapCodec)RarityValueMapper.CODEC.fieldOf("rarity_value_mapper")).forGetter(WeirdScaledSampler::rarityValueMapper)).apply((Applicative<WeirdScaledSampler, ?>)instance, WeirdScaledSampler::new));
        public static final CodecHolder<WeirdScaledSampler> CODEC_HOLDER = DensityFunctionTypes.holderOf(WEIRD_SCALED_SAMPLER_CODEC);

        @Override
        public double apply(DensityFunction.NoisePos pos, double density) {
            double e = this.rarityValueMapper.scaleFunction.get(density);
            return e * Math.abs(this.noise.sample((double)pos.blockX() / e, (double)pos.blockY() / e, (double)pos.blockZ() / e));
        }

        @Override
        public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
            return visitor.apply(new WeirdScaledSampler(this.input.apply(visitor), visitor.apply(this.noise), this.rarityValueMapper));
        }

        @Override
        public double minValue() {
            return 0.0;
        }

        @Override
        public double maxValue() {
            return this.rarityValueMapper.maxValueMultiplier * this.noise.getMaxValue();
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC_HOLDER;
        }

        public static enum RarityValueMapper implements StringIdentifiable
        {
            TYPE1("type_1", DensityFunctions.CaveScaler::scaleTunnels, 2.0),
            TYPE2("type_2", DensityFunctions.CaveScaler::scaleCaves, 3.0);

            public static final Codec<RarityValueMapper> CODEC;
            private final String name;
            final Double2DoubleFunction scaleFunction;
            final double maxValueMultiplier;

            private RarityValueMapper(String name, Double2DoubleFunction scaleFunction, double maxValueMultiplier) {
                this.name = name;
                this.scaleFunction = scaleFunction;
                this.maxValueMultiplier = maxValueMultiplier;
            }

            @Override
            public String asString() {
                return this.name;
            }

            static {
                CODEC = StringIdentifiable.createCodec(RarityValueMapper::values);
            }
        }
    }

    protected record ShiftedNoise(DensityFunction shiftX, DensityFunction shiftY, DensityFunction shiftZ, double xzScale, double yScale, DensityFunction.Noise noise) implements DensityFunction
    {
        private static final MapCodec<ShiftedNoise> SHIFTED_NOISE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)DensityFunction.FUNCTION_CODEC.fieldOf("shift_x")).forGetter(ShiftedNoise::shiftX), ((MapCodec)DensityFunction.FUNCTION_CODEC.fieldOf("shift_y")).forGetter(ShiftedNoise::shiftY), ((MapCodec)DensityFunction.FUNCTION_CODEC.fieldOf("shift_z")).forGetter(ShiftedNoise::shiftZ), ((MapCodec)Codec.DOUBLE.fieldOf("xz_scale")).forGetter(ShiftedNoise::xzScale), ((MapCodec)Codec.DOUBLE.fieldOf("y_scale")).forGetter(ShiftedNoise::yScale), ((MapCodec)DensityFunction.Noise.CODEC.fieldOf("noise")).forGetter(ShiftedNoise::noise)).apply((Applicative<ShiftedNoise, ?>)instance, ShiftedNoise::new));
        public static final CodecHolder<ShiftedNoise> CODEC_HOLDER = DensityFunctionTypes.holderOf(SHIFTED_NOISE_CODEC);

        @Override
        public double sample(DensityFunction.NoisePos pos) {
            double d = (double)pos.blockX() * this.xzScale + this.shiftX.sample(pos);
            double e = (double)pos.blockY() * this.yScale + this.shiftY.sample(pos);
            double f = (double)pos.blockZ() * this.xzScale + this.shiftZ.sample(pos);
            return this.noise.sample(d, e, f);
        }

        @Override
        public void fill(double[] densities, DensityFunction.EachApplier applier) {
            applier.fill(densities, this);
        }

        @Override
        public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
            return visitor.apply(new ShiftedNoise(this.shiftX.apply(visitor), this.shiftY.apply(visitor), this.shiftZ.apply(visitor), this.xzScale, this.yScale, visitor.apply(this.noise)));
        }

        @Override
        public double minValue() {
            return -this.maxValue();
        }

        @Override
        public double maxValue() {
            return this.noise.getMaxValue();
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC_HOLDER;
        }
    }

    record RangeChoice(DensityFunction input, double minInclusive, double maxExclusive, DensityFunction whenInRange, DensityFunction whenOutOfRange) implements DensityFunction
    {
        public static final MapCodec<RangeChoice> RANGE_CHOICE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)DensityFunction.FUNCTION_CODEC.fieldOf("input")).forGetter(RangeChoice::input), ((MapCodec)CONSTANT_RANGE.fieldOf("min_inclusive")).forGetter(RangeChoice::minInclusive), ((MapCodec)CONSTANT_RANGE.fieldOf("max_exclusive")).forGetter(RangeChoice::maxExclusive), ((MapCodec)DensityFunction.FUNCTION_CODEC.fieldOf("when_in_range")).forGetter(RangeChoice::whenInRange), ((MapCodec)DensityFunction.FUNCTION_CODEC.fieldOf("when_out_of_range")).forGetter(RangeChoice::whenOutOfRange)).apply((Applicative<RangeChoice, ?>)instance, RangeChoice::new));
        public static final CodecHolder<RangeChoice> CODEC_HOLDER = DensityFunctionTypes.holderOf(RANGE_CHOICE_CODEC);

        @Override
        public double sample(DensityFunction.NoisePos pos) {
            double d = this.input.sample(pos);
            if (d >= this.minInclusive && d < this.maxExclusive) {
                return this.whenInRange.sample(pos);
            }
            return this.whenOutOfRange.sample(pos);
        }

        @Override
        public void fill(double[] densities, DensityFunction.EachApplier applier) {
            this.input.fill(densities, applier);
            for (int i = 0; i < densities.length; ++i) {
                double d = densities[i];
                densities[i] = d >= this.minInclusive && d < this.maxExclusive ? this.whenInRange.sample(applier.at(i)) : this.whenOutOfRange.sample(applier.at(i));
            }
        }

        @Override
        public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
            return visitor.apply(new RangeChoice(this.input.apply(visitor), this.minInclusive, this.maxExclusive, this.whenInRange.apply(visitor), this.whenOutOfRange.apply(visitor)));
        }

        @Override
        public double minValue() {
            return Math.min(this.whenInRange.minValue(), this.whenOutOfRange.minValue());
        }

        @Override
        public double maxValue() {
            return Math.max(this.whenInRange.maxValue(), this.whenOutOfRange.maxValue());
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC_HOLDER;
        }
    }

    protected record ShiftA(DensityFunction.Noise offsetNoise) implements Offset
    {
        static final CodecHolder<ShiftA> CODEC_HOLDER = DensityFunctionTypes.holderOf(DensityFunction.Noise.CODEC, ShiftA::new, ShiftA::offsetNoise);

        @Override
        public double sample(DensityFunction.NoisePos pos) {
            return this.sample(pos.blockX(), 0.0, pos.blockZ());
        }

        @Override
        public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
            return visitor.apply(new ShiftA(visitor.apply(this.offsetNoise)));
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC_HOLDER;
        }
    }

    protected record ShiftB(DensityFunction.Noise offsetNoise) implements Offset
    {
        static final CodecHolder<ShiftB> CODEC_HOLDER = DensityFunctionTypes.holderOf(DensityFunction.Noise.CODEC, ShiftB::new, ShiftB::offsetNoise);

        @Override
        public double sample(DensityFunction.NoisePos pos) {
            return this.sample(pos.blockZ(), pos.blockX(), 0.0);
        }

        @Override
        public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
            return visitor.apply(new ShiftB(visitor.apply(this.offsetNoise)));
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC_HOLDER;
        }
    }

    protected record Shift(DensityFunction.Noise offsetNoise) implements Offset
    {
        static final CodecHolder<Shift> CODEC_HOLDER = DensityFunctionTypes.holderOf(DensityFunction.Noise.CODEC, Shift::new, Shift::offsetNoise);

        @Override
        public double sample(DensityFunction.NoisePos pos) {
            return this.sample(pos.blockX(), pos.blockY(), pos.blockZ());
        }

        @Override
        public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
            return visitor.apply(new Shift(visitor.apply(this.offsetNoise)));
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC_HOLDER;
        }
    }

    record BlendDensity(DensityFunction input) implements Positional
    {
        static final CodecHolder<BlendDensity> CODEC_HOLDER = DensityFunctionTypes.holderOf(BlendDensity::new, BlendDensity::input);

        @Override
        public double apply(DensityFunction.NoisePos pos, double density) {
            return pos.getBlender().applyBlendDensity(pos, density);
        }

        @Override
        public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
            return visitor.apply(new BlendDensity(this.input.apply(visitor)));
        }

        @Override
        public double minValue() {
            return Double.NEGATIVE_INFINITY;
        }

        @Override
        public double maxValue() {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC_HOLDER;
        }
    }

    protected record Clamp(DensityFunction input, double minValue, double maxValue) implements Unary
    {
        private static final MapCodec<Clamp> CLAMP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)DensityFunction.CODEC.fieldOf("input")).forGetter(Clamp::input), ((MapCodec)CONSTANT_RANGE.fieldOf("min")).forGetter(Clamp::minValue), ((MapCodec)CONSTANT_RANGE.fieldOf("max")).forGetter(Clamp::maxValue)).apply((Applicative<Clamp, ?>)instance, Clamp::new));
        public static final CodecHolder<Clamp> CODEC_HOLDER = DensityFunctionTypes.holderOf(CLAMP_CODEC);

        @Override
        public double apply(double density) {
            return MathHelper.clamp(density, this.minValue, this.maxValue);
        }

        @Override
        public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
            return new Clamp(this.input.apply(visitor), this.minValue, this.maxValue);
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC_HOLDER;
        }
    }

    protected record UnaryOperation(Type type, DensityFunction input, double minValue, double maxValue) implements Unary
    {
        public static UnaryOperation create(Type type, DensityFunction input) {
            double d = input.minValue();
            double e = UnaryOperation.apply(type, d);
            double f = UnaryOperation.apply(type, input.maxValue());
            if (type == Type.ABS || type == Type.SQUARE) {
                return new UnaryOperation(type, input, Math.max(0.0, d), Math.max(e, f));
            }
            return new UnaryOperation(type, input, e, f);
        }

        private static double apply(Type type, double density) {
            return switch (type.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Math.abs(density);
                case 1 -> density * density;
                case 2 -> density * density * density;
                case 3 -> {
                    if (density > 0.0) {
                        yield density;
                    }
                    yield density * 0.5;
                }
                case 4 -> {
                    if (density > 0.0) {
                        yield density;
                    }
                    yield density * 0.25;
                }
                case 5 -> {
                    double e = MathHelper.clamp(density, -1.0, 1.0);
                    yield e / 2.0 - e * e * e / 24.0;
                }
            };
        }

        @Override
        public double apply(double density) {
            return UnaryOperation.apply(this.type, density);
        }

        @Override
        public UnaryOperation apply(DensityFunction.DensityFunctionVisitor arg) {
            return UnaryOperation.create(this.type, this.input.apply(arg));
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return this.type.codecHolder;
        }

        @Override
        public /* synthetic */ DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
            return this.apply(visitor);
        }

        static enum Type implements StringIdentifiable
        {
            ABS("abs"),
            SQUARE("square"),
            CUBE("cube"),
            HALF_NEGATIVE("half_negative"),
            QUARTER_NEGATIVE("quarter_negative"),
            SQUEEZE("squeeze");

            private final String name;
            final CodecHolder<UnaryOperation> codecHolder = DensityFunctionTypes.holderOf(input -> UnaryOperation.create(this, input), UnaryOperation::input);

            private Type(String name) {
                this.name = name;
            }

            @Override
            public String asString() {
                return this.name;
            }
        }
    }

    static interface BinaryOperationLike
    extends DensityFunction {
        public static final Logger LOGGER = LogUtils.getLogger();

        public static BinaryOperationLike create(Type type, DensityFunction argument1, DensityFunction argument2) {
            double i;
            double d = argument1.minValue();
            double e = argument2.minValue();
            double f = argument1.maxValue();
            double g = argument2.maxValue();
            if (type == Type.MIN || type == Type.MAX) {
                boolean bl2;
                boolean bl = d >= g;
                boolean bl3 = bl2 = e >= f;
                if (bl || bl2) {
                    LOGGER.warn("Creating a " + String.valueOf(type) + " function between two non-overlapping inputs: " + String.valueOf(argument1) + " and " + String.valueOf(argument2));
                }
            }
            double h = switch (type.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> d + e;
                case 3 -> Math.max(d, e);
                case 2 -> Math.min(d, e);
                case 1 -> d > 0.0 && e > 0.0 ? d * e : (f < 0.0 && g < 0.0 ? f * g : Math.min(d * g, f * e));
            };
            switch (type.ordinal()) {
                default: {
                    throw new MatchException(null, null);
                }
                case 0: {
                    double d2 = f + g;
                    break;
                }
                case 3: {
                    double d2 = Math.max(f, g);
                    break;
                }
                case 2: {
                    double d2 = Math.min(f, g);
                    break;
                }
                case 1: {
                    double d2 = d > 0.0 && e > 0.0 ? f * g : (i = f < 0.0 && g < 0.0 ? d * e : Math.max(d * e, f * g));
                }
            }
            if (type == Type.MUL || type == Type.ADD) {
                if (argument1 instanceof Constant) {
                    Constant lv = (Constant)argument1;
                    return new LinearOperation(type == Type.ADD ? LinearOperation.SpecificType.ADD : LinearOperation.SpecificType.MUL, argument2, h, i, lv.value);
                }
                if (argument2 instanceof Constant) {
                    Constant lv = (Constant)argument2;
                    return new LinearOperation(type == Type.ADD ? LinearOperation.SpecificType.ADD : LinearOperation.SpecificType.MUL, argument1, h, i, lv.value);
                }
            }
            return new BinaryOperation(type, argument1, argument2, h, i);
        }

        public Type type();

        public DensityFunction argument1();

        public DensityFunction argument2();

        @Override
        default public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return this.type().codecHolder;
        }

        public static enum Type implements StringIdentifiable
        {
            ADD("add"),
            MUL("mul"),
            MIN("min"),
            MAX("max");

            final CodecHolder<BinaryOperationLike> codecHolder = DensityFunctionTypes.holderOf((arg, arg2) -> BinaryOperationLike.create(this, arg, arg2), BinaryOperationLike::argument1, BinaryOperationLike::argument2);
            private final String name;

            private Type(String name) {
                this.name = name;
            }

            @Override
            public String asString() {
                return this.name;
            }
        }
    }

    public record Spline(net.minecraft.util.math.Spline<SplinePos, DensityFunctionWrapper> spline) implements DensityFunction
    {
        private static final Codec<net.minecraft.util.math.Spline<SplinePos, DensityFunctionWrapper>> SPLINE_CODEC = net.minecraft.util.math.Spline.createCodec(DensityFunctionWrapper.CODEC);
        private static final MapCodec<Spline> SPLINE_FUNCTION_CODEC = ((MapCodec)SPLINE_CODEC.fieldOf("spline")).xmap(Spline::new, Spline::spline);
        public static final CodecHolder<Spline> CODEC_HOLDER = DensityFunctionTypes.holderOf(SPLINE_FUNCTION_CODEC);

        @Override
        public double sample(DensityFunction.NoisePos pos) {
            return this.spline.apply(new SplinePos(pos));
        }

        @Override
        public double minValue() {
            return this.spline.min();
        }

        @Override
        public double maxValue() {
            return this.spline.max();
        }

        @Override
        public void fill(double[] densities, DensityFunction.EachApplier applier) {
            applier.fill(densities, this);
        }

        @Override
        public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
            return visitor.apply(new Spline(this.spline.apply((I densityFunctionWrapper) -> densityFunctionWrapper.apply(visitor))));
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC_HOLDER;
        }

        public record SplinePos(DensityFunction.NoisePos context) {
        }

        public record DensityFunctionWrapper(RegistryEntry<DensityFunction> function) implements ToFloatFunction<SplinePos>
        {
            public static final Codec<DensityFunctionWrapper> CODEC = DensityFunction.REGISTRY_ENTRY_CODEC.xmap(DensityFunctionWrapper::new, DensityFunctionWrapper::function);

            @Override
            public String toString() {
                Optional<RegistryKey<DensityFunction>> optional = this.function.getKey();
                if (optional.isPresent()) {
                    RegistryKey<DensityFunction> lv = optional.get();
                    if (lv == DensityFunctions.CONTINENTS_OVERWORLD) {
                        return "continents";
                    }
                    if (lv == DensityFunctions.EROSION_OVERWORLD) {
                        return "erosion";
                    }
                    if (lv == DensityFunctions.RIDGES_OVERWORLD) {
                        return "weirdness";
                    }
                    if (lv == DensityFunctions.RIDGES_FOLDED_OVERWORLD) {
                        return "ridges";
                    }
                }
                return "Coordinate[" + String.valueOf(this.function) + "]";
            }

            @Override
            public float apply(SplinePos arg) {
                return (float)this.function.value().sample(arg.context());
            }

            @Override
            public float min() {
                return this.function.hasKeyAndValue() ? (float)this.function.value().minValue() : Float.NEGATIVE_INFINITY;
            }

            @Override
            public float max() {
                return this.function.hasKeyAndValue() ? (float)this.function.value().maxValue() : Float.POSITIVE_INFINITY;
            }

            public DensityFunctionWrapper apply(DensityFunction.DensityFunctionVisitor visitor) {
                return new DensityFunctionWrapper(new RegistryEntry.Direct<DensityFunction>(this.function.value().apply(visitor)));
            }
        }
    }

    record Constant(double value) implements DensityFunction.Base
    {
        static final CodecHolder<Constant> CODEC_HOLDER = DensityFunctionTypes.holderOf(CONSTANT_RANGE, Constant::new, Constant::value);
        static final Constant ZERO = new Constant(0.0);

        @Override
        public double sample(DensityFunction.NoisePos pos) {
            return this.value;
        }

        @Override
        public void fill(double[] densities, DensityFunction.EachApplier applier) {
            Arrays.fill(densities, this.value);
        }

        @Override
        public double minValue() {
            return this.value;
        }

        @Override
        public double maxValue() {
            return this.value;
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC_HOLDER;
        }
    }

    record YClampedGradient(int fromY, int toY, double fromValue, double toValue) implements DensityFunction.Base
    {
        private static final MapCodec<YClampedGradient> Y_CLAMPED_GRADIENT_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.intRange(DimensionType.MIN_HEIGHT * 2, DimensionType.MAX_COLUMN_HEIGHT * 2).fieldOf("from_y")).forGetter(YClampedGradient::fromY), ((MapCodec)Codec.intRange(DimensionType.MIN_HEIGHT * 2, DimensionType.MAX_COLUMN_HEIGHT * 2).fieldOf("to_y")).forGetter(YClampedGradient::toY), ((MapCodec)CONSTANT_RANGE.fieldOf("from_value")).forGetter(YClampedGradient::fromValue), ((MapCodec)CONSTANT_RANGE.fieldOf("to_value")).forGetter(YClampedGradient::toValue)).apply((Applicative<YClampedGradient, ?>)instance, YClampedGradient::new));
        public static final CodecHolder<YClampedGradient> CODEC_HOLDER = DensityFunctionTypes.holderOf(Y_CLAMPED_GRADIENT_CODEC);

        @Override
        public double sample(DensityFunction.NoisePos pos) {
            return MathHelper.clampedMap((double)pos.blockY(), (double)this.fromY, (double)this.toY, this.fromValue, this.toValue);
        }

        @Override
        public double minValue() {
            return Math.min(this.fromValue, this.toValue);
        }

        @Override
        public double maxValue() {
            return Math.max(this.fromValue, this.toValue);
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC_HOLDER;
        }
    }

    record BinaryOperation(BinaryOperationLike.Type type, DensityFunction argument1, DensityFunction argument2, double minValue, double maxValue) implements BinaryOperationLike
    {
        @Override
        public double sample(DensityFunction.NoisePos pos) {
            double d = this.argument1.sample(pos);
            return switch (this.type.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> d + this.argument2.sample(pos);
                case 1 -> {
                    if (d == 0.0) {
                        yield 0.0;
                    }
                    yield d * this.argument2.sample(pos);
                }
                case 2 -> {
                    if (d < this.argument2.minValue()) {
                        yield d;
                    }
                    yield Math.min(d, this.argument2.sample(pos));
                }
                case 3 -> d > this.argument2.maxValue() ? d : Math.max(d, this.argument2.sample(pos));
            };
        }

        @Override
        public void fill(double[] densities, DensityFunction.EachApplier applier) {
            this.argument1.fill(densities, applier);
            switch (this.type.ordinal()) {
                case 0: {
                    double[] es = new double[densities.length];
                    this.argument2.fill(es, applier);
                    for (int i = 0; i < densities.length; ++i) {
                        densities[i] = densities[i] + es[i];
                    }
                    break;
                }
                case 1: {
                    for (int j = 0; j < densities.length; ++j) {
                        double d = densities[j];
                        densities[j] = d == 0.0 ? 0.0 : d * this.argument2.sample(applier.at(j));
                    }
                    break;
                }
                case 2: {
                    double e = this.argument2.minValue();
                    for (int k = 0; k < densities.length; ++k) {
                        double f = densities[k];
                        densities[k] = f < e ? f : Math.min(f, this.argument2.sample(applier.at(k)));
                    }
                    break;
                }
                case 3: {
                    double e = this.argument2.maxValue();
                    for (int k = 0; k < densities.length; ++k) {
                        double f = densities[k];
                        densities[k] = f > e ? f : Math.max(f, this.argument2.sample(applier.at(k)));
                    }
                    break;
                }
            }
        }

        @Override
        public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
            return visitor.apply(BinaryOperationLike.create(this.type, this.argument1.apply(visitor), this.argument2.apply(visitor)));
        }
    }

    record LinearOperation(SpecificType specificType, DensityFunction input, double minValue, double maxValue, double argument) implements Unary,
    BinaryOperationLike
    {
        @Override
        public BinaryOperationLike.Type type() {
            return this.specificType == SpecificType.MUL ? BinaryOperationLike.Type.MUL : BinaryOperationLike.Type.ADD;
        }

        @Override
        public DensityFunction argument1() {
            return DensityFunctionTypes.constant(this.argument);
        }

        @Override
        public DensityFunction argument2() {
            return this.input;
        }

        @Override
        public double apply(double density) {
            return switch (this.specificType.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> density * this.argument;
                case 1 -> density + this.argument;
            };
        }

        @Override
        public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
            double g;
            double f;
            DensityFunction lv = this.input.apply(visitor);
            double d = lv.minValue();
            double e = lv.maxValue();
            if (this.specificType == SpecificType.ADD) {
                f = d + this.argument;
                g = e + this.argument;
            } else if (this.argument >= 0.0) {
                f = d * this.argument;
                g = e * this.argument;
            } else {
                f = e * this.argument;
                g = d * this.argument;
            }
            return new LinearOperation(this.specificType, lv, f, g, this.argument);
        }

        static enum SpecificType {
            MUL,
            ADD;

        }
    }

    static interface Offset
    extends DensityFunction {
        public DensityFunction.Noise offsetNoise();

        @Override
        default public double minValue() {
            return -this.maxValue();
        }

        @Override
        default public double maxValue() {
            return this.offsetNoise().getMaxValue() * 4.0;
        }

        default public double sample(double x, double y, double z) {
            return this.offsetNoise().sample(x * 0.25, y * 0.25, z * 0.25) * 4.0;
        }

        @Override
        default public void fill(double[] densities, DensityFunction.EachApplier applier) {
            applier.fill(densities, this);
        }
    }

    public static interface Wrapper
    extends DensityFunction {
        public Wrapping.Type type();

        public DensityFunction wrapped();

        @Override
        default public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return this.type().codec;
        }

        @Override
        default public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
            return visitor.apply(new Wrapping(this.type(), this.wrapped().apply(visitor)));
        }
    }

    @Debug
    public record RegistryEntryHolder(RegistryEntry<DensityFunction> function) implements DensityFunction
    {
        @Override
        public double sample(DensityFunction.NoisePos pos) {
            return this.function.value().sample(pos);
        }

        @Override
        public void fill(double[] densities, DensityFunction.EachApplier applier) {
            this.function.value().fill(densities, applier);
        }

        @Override
        public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
            return visitor.apply(new RegistryEntryHolder(new RegistryEntry.Direct<DensityFunction>(this.function.value().apply(visitor))));
        }

        @Override
        public double minValue() {
            return this.function.hasKeyAndValue() ? this.function.value().minValue() : Double.NEGATIVE_INFINITY;
        }

        @Override
        public double maxValue() {
            return this.function.hasKeyAndValue() ? this.function.value().maxValue() : Double.POSITIVE_INFINITY;
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            throw new UnsupportedOperationException("Calling .codec() on HolderHolder");
        }
    }

    public static interface Beardifying
    extends DensityFunction.Base {
        public static final CodecHolder<DensityFunction> CODEC_HOLDER = CodecHolder.of(MapCodec.unit(Beardifier.INSTANCE));

        @Override
        default public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC_HOLDER;
        }
    }

    static interface Unary
    extends DensityFunction {
        public DensityFunction input();

        @Override
        default public double sample(DensityFunction.NoisePos pos) {
            return this.apply(this.input().sample(pos));
        }

        @Override
        default public void fill(double[] densities, DensityFunction.EachApplier applier) {
            this.input().fill(densities, applier);
            for (int i = 0; i < densities.length; ++i) {
                densities[i] = this.apply(densities[i]);
            }
        }

        public double apply(double var1);
    }

    static interface Positional
    extends DensityFunction {
        public DensityFunction input();

        @Override
        default public double sample(DensityFunction.NoisePos pos) {
            return this.apply(pos, this.input().sample(pos));
        }

        @Override
        default public void fill(double[] densities, DensityFunction.EachApplier applier) {
            this.input().fill(densities, applier);
            for (int i = 0; i < densities.length; ++i) {
                densities[i] = this.apply(applier.at(i), densities[i]);
            }
        }

        public double apply(DensityFunction.NoisePos var1, double var2);
    }
}

