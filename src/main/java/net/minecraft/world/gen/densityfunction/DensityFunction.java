/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.gen.densityfunction;

import com.mojang.serialization.Codec;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.jetbrains.annotations.Nullable;

public interface DensityFunction {
    public static final Codec<DensityFunction> CODEC = DensityFunctionTypes.CODEC;
    public static final Codec<RegistryEntry<DensityFunction>> REGISTRY_ENTRY_CODEC = RegistryElementCodec.of(RegistryKeys.DENSITY_FUNCTION, CODEC);
    public static final Codec<DensityFunction> FUNCTION_CODEC = REGISTRY_ENTRY_CODEC.xmap(DensityFunctionTypes.RegistryEntryHolder::new, function -> {
        if (function instanceof DensityFunctionTypes.RegistryEntryHolder) {
            DensityFunctionTypes.RegistryEntryHolder lv = (DensityFunctionTypes.RegistryEntryHolder)function;
            return lv.function();
        }
        return new RegistryEntry.Direct<DensityFunction>((DensityFunction)function);
    });

    public double sample(NoisePos var1);

    public void fill(double[] var1, EachApplier var2);

    public DensityFunction apply(DensityFunctionVisitor var1);

    public double minValue();

    public double maxValue();

    public CodecHolder<? extends DensityFunction> getCodecHolder();

    default public DensityFunction clamp(double min, double max) {
        return new DensityFunctionTypes.Clamp(this, min, max);
    }

    default public DensityFunction abs() {
        return DensityFunctionTypes.unary(this, DensityFunctionTypes.UnaryOperation.Type.ABS);
    }

    default public DensityFunction square() {
        return DensityFunctionTypes.unary(this, DensityFunctionTypes.UnaryOperation.Type.SQUARE);
    }

    default public DensityFunction cube() {
        return DensityFunctionTypes.unary(this, DensityFunctionTypes.UnaryOperation.Type.CUBE);
    }

    default public DensityFunction halfNegative() {
        return DensityFunctionTypes.unary(this, DensityFunctionTypes.UnaryOperation.Type.HALF_NEGATIVE);
    }

    default public DensityFunction quarterNegative() {
        return DensityFunctionTypes.unary(this, DensityFunctionTypes.UnaryOperation.Type.QUARTER_NEGATIVE);
    }

    default public DensityFunction squeeze() {
        return DensityFunctionTypes.unary(this, DensityFunctionTypes.UnaryOperation.Type.SQUEEZE);
    }

    public record UnblendedNoisePos(int blockX, int blockY, int blockZ) implements NoisePos
    {
    }

    public static interface NoisePos {
        public int blockX();

        public int blockY();

        public int blockZ();

        default public Blender getBlender() {
            return Blender.getNoBlending();
        }
    }

    public static interface Base
    extends DensityFunction {
        @Override
        default public void fill(double[] densities, EachApplier applier) {
            applier.fill(densities, this);
        }

        @Override
        default public DensityFunction apply(DensityFunctionVisitor visitor) {
            return visitor.apply(this);
        }
    }

    public static interface DensityFunctionVisitor {
        public DensityFunction apply(DensityFunction var1);

        default public Noise apply(Noise noiseDensityFunction) {
            return noiseDensityFunction;
        }
    }

    public record Noise(RegistryEntry<DoublePerlinNoiseSampler.NoiseParameters> noiseData, @Nullable DoublePerlinNoiseSampler noise) {
        public static final Codec<Noise> CODEC = DoublePerlinNoiseSampler.NoiseParameters.REGISTRY_ENTRY_CODEC.xmap(noiseData -> new Noise((RegistryEntry<DoublePerlinNoiseSampler.NoiseParameters>)noiseData, null), Noise::noiseData);

        public Noise(RegistryEntry<DoublePerlinNoiseSampler.NoiseParameters> noiseData) {
            this(noiseData, null);
        }

        public double sample(double x, double y, double z) {
            return this.noise == null ? 0.0 : this.noise.sample(x, y, z);
        }

        public double getMaxValue() {
            return this.noise == null ? 2.0 : this.noise.getMaxValue();
        }

        @Nullable
        public DoublePerlinNoiseSampler noise() {
            return this.noise;
        }
    }

    public static interface EachApplier {
        public NoisePos at(int var1);

        public void fill(double[] var1, DensityFunction var2);
    }
}

