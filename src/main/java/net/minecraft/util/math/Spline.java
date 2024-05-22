/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.function.ToFloatFunction;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.mutable.MutableObject;

public interface Spline<C, I extends ToFloatFunction<C>>
extends ToFloatFunction<C> {
    @Debug
    public String getDebugString();

    public Spline<C, I> apply(Visitor<I> var1);

    public static <C, I extends ToFloatFunction<C>> Codec<Spline<C, I>> createCodec(Codec<I> locationFunctionCodec) {
        record Serialized<C, I extends ToFloatFunction<C>>(float location, Spline<C, I> value, float derivative) {
        }
        MutableObject<Codec<Spline>> mutableObject = new MutableObject<Codec<Spline>>();
        Codec codec2 = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("location")).forGetter(Serialized::location), ((MapCodec)Codec.lazyInitialized(mutableObject::getValue).fieldOf("value")).forGetter(Serialized::value), ((MapCodec)Codec.FLOAT.fieldOf("derivative")).forGetter(Serialized::derivative)).apply((Applicative<Serialized, ?>)instance, (location, value, derivative) -> new Serialized((float)location, value, (float)derivative)));
        Codec codec3 = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)locationFunctionCodec.fieldOf("coordinate")).forGetter(Implementation::locationFunction), ((MapCodec)Codecs.nonEmptyList(codec2.listOf()).fieldOf("points")).forGetter(spline -> IntStream.range(0, spline.locations.length).mapToObj(index -> new Serialized(spline.locations()[index], spline.values().get(index), spline.derivatives()[index])).toList())).apply((Applicative<Implementation, ?>)instance, (locationFunction, splines) -> {
            float[] fs = new float[splines.size()];
            ImmutableList.Builder builder = ImmutableList.builder();
            float[] gs = new float[splines.size()];
            for (int i = 0; i < splines.size(); ++i) {
                Serialized lv = (Serialized)splines.get(i);
                fs[i] = lv.location();
                builder.add(lv.value());
                gs[i] = lv.derivative();
            }
            return Implementation.build(locationFunction, fs, builder.build(), gs);
        }));
        mutableObject.setValue(Codec.either(Codec.FLOAT, codec3).xmap(either -> (Spline)((Object)either.map(FixedFloatFunction::new, spline -> spline)), spline -> {
            Either either;
            if (spline instanceof FixedFloatFunction) {
                FixedFloatFunction lv = (FixedFloatFunction)spline;
                either = Either.left(Float.valueOf(lv.value()));
            } else {
                either = Either.right((Implementation)spline);
            }
            return either;
        }));
        return (Codec)mutableObject.getValue();
    }

    public static <C, I extends ToFloatFunction<C>> Spline<C, I> fixedFloatFunction(float value) {
        return new FixedFloatFunction(value);
    }

    public static <C, I extends ToFloatFunction<C>> Builder<C, I> builder(I locationFunction) {
        return new Builder(locationFunction);
    }

    public static <C, I extends ToFloatFunction<C>> Builder<C, I> builder(I locationFunction, ToFloatFunction<Float> amplifier) {
        return new Builder(locationFunction, amplifier);
    }

    @Debug
    public record FixedFloatFunction<C, I extends ToFloatFunction<C>>(float value) implements Spline<C, I>
    {
        @Override
        public float apply(C x) {
            return this.value;
        }

        @Override
        public String getDebugString() {
            return String.format(Locale.ROOT, "k=%.3f", Float.valueOf(this.value));
        }

        @Override
        public float min() {
            return this.value;
        }

        @Override
        public float max() {
            return this.value;
        }

        @Override
        public Spline<C, I> apply(Visitor<I> visitor) {
            return this;
        }
    }

    public static final class Builder<C, I extends ToFloatFunction<C>> {
        private final I locationFunction;
        private final ToFloatFunction<Float> amplifier;
        private final FloatList locations = new FloatArrayList();
        private final List<Spline<C, I>> values = Lists.newArrayList();
        private final FloatList derivatives = new FloatArrayList();

        protected Builder(I locationFunction) {
            this(locationFunction, ToFloatFunction.IDENTITY);
        }

        protected Builder(I locationFunction, ToFloatFunction<Float> amplifier) {
            this.locationFunction = locationFunction;
            this.amplifier = amplifier;
        }

        public Builder<C, I> add(float location, float value) {
            return this.addPoint(location, new FixedFloatFunction(this.amplifier.apply(Float.valueOf(value))), 0.0f);
        }

        public Builder<C, I> add(float location, float value, float derivative) {
            return this.addPoint(location, new FixedFloatFunction(this.amplifier.apply(Float.valueOf(value))), derivative);
        }

        public Builder<C, I> add(float location, Spline<C, I> value) {
            return this.addPoint(location, value, 0.0f);
        }

        private Builder<C, I> addPoint(float location, Spline<C, I> value, float derivative) {
            if (!this.locations.isEmpty() && location <= this.locations.getFloat(this.locations.size() - 1)) {
                throw new IllegalArgumentException("Please register points in ascending order");
            }
            this.locations.add(location);
            this.values.add(value);
            this.derivatives.add(derivative);
            return this;
        }

        public Spline<C, I> build() {
            if (this.locations.isEmpty()) {
                throw new IllegalStateException("No elements added");
            }
            return Implementation.build(this.locationFunction, this.locations.toFloatArray(), ImmutableList.copyOf(this.values), this.derivatives.toFloatArray());
        }
    }

    @Debug
    public record Implementation<C, I extends ToFloatFunction<C>>(I locationFunction, float[] locations, List<Spline<C, I>> values, float[] derivatives, float min, float max) implements Spline<C, I>
    {
        public Implementation {
            Implementation.assertParametersValid(fs, list, gs);
        }

        static <C, I extends ToFloatFunction<C>> Implementation<C, I> build(I locationFunction, float[] locations, List<Spline<C, I>> values, float[] derivatives) {
            float l;
            float k;
            Implementation.assertParametersValid(locations, values, derivatives);
            int i = locations.length - 1;
            float f = Float.POSITIVE_INFINITY;
            float g = Float.NEGATIVE_INFINITY;
            float h = locationFunction.min();
            float j = locationFunction.max();
            if (h < locations[0]) {
                k = Implementation.sampleOutsideRange(h, locations, values.get(0).min(), derivatives, 0);
                l = Implementation.sampleOutsideRange(h, locations, values.get(0).max(), derivatives, 0);
                f = Math.min(f, Math.min(k, l));
                g = Math.max(g, Math.max(k, l));
            }
            if (j > locations[i]) {
                k = Implementation.sampleOutsideRange(j, locations, values.get(i).min(), derivatives, i);
                l = Implementation.sampleOutsideRange(j, locations, values.get(i).max(), derivatives, i);
                f = Math.min(f, Math.min(k, l));
                g = Math.max(g, Math.max(k, l));
            }
            for (Spline<C, I> lv : values) {
                f = Math.min(f, lv.min());
                g = Math.max(g, lv.max());
            }
            for (int m = 0; m < i; ++m) {
                l = locations[m];
                float n = locations[m + 1];
                float o = n - l;
                Spline<C, I> lv2 = values.get(m);
                Spline<C, I> lv3 = values.get(m + 1);
                float p = lv2.min();
                float q = lv2.max();
                float r = lv3.min();
                float s = lv3.max();
                float t = derivatives[m];
                float u = derivatives[m + 1];
                if (t == 0.0f && u == 0.0f) continue;
                float v = t * o;
                float w = u * o;
                float x = Math.min(p, r);
                float y = Math.max(q, s);
                float z = v - s + p;
                float aa = v - r + q;
                float ab = -w + r - q;
                float ac = -w + s - p;
                float ad = Math.min(z, ab);
                float ae = Math.max(aa, ac);
                f = Math.min(f, x + 0.25f * ad);
                g = Math.max(g, y + 0.25f * ae);
            }
            return new Implementation<C, I>(locationFunction, locations, values, derivatives, f, g);
        }

        private static float sampleOutsideRange(float point, float[] locations, float value, float[] derivatives, int i) {
            float h = derivatives[i];
            if (h == 0.0f) {
                return value;
            }
            return value + h * (point - locations[i]);
        }

        private static <C, I extends ToFloatFunction<C>> void assertParametersValid(float[] locations, List<Spline<C, I>> values, float[] derivatives) {
            if (locations.length != values.size() || locations.length != derivatives.length) {
                throw new IllegalArgumentException("All lengths must be equal, got: " + locations.length + " " + values.size() + " " + derivatives.length);
            }
            if (locations.length == 0) {
                throw new IllegalArgumentException("Cannot create a multipoint spline with no points");
            }
        }

        @Override
        public float apply(C x) {
            float f = this.locationFunction.apply(x);
            int i = Implementation.findRangeForLocation(this.locations, f);
            int j = this.locations.length - 1;
            if (i < 0) {
                return Implementation.sampleOutsideRange(f, this.locations, this.values.get(0).apply(x), this.derivatives, 0);
            }
            if (i == j) {
                return Implementation.sampleOutsideRange(f, this.locations, this.values.get(j).apply(x), this.derivatives, j);
            }
            float g = this.locations[i];
            float h = this.locations[i + 1];
            float k = (f - g) / (h - g);
            ToFloatFunction lv = this.values.get(i);
            ToFloatFunction lv2 = this.values.get(i + 1);
            float l = this.derivatives[i];
            float m = this.derivatives[i + 1];
            float n = lv.apply(x);
            float o = lv2.apply(x);
            float p = l * (h - g) - (o - n);
            float q = -m * (h - g) + (o - n);
            float r = MathHelper.lerp(k, n, o) + k * (1.0f - k) * MathHelper.lerp(k, p, q);
            return r;
        }

        private static int findRangeForLocation(float[] locations, float x) {
            return MathHelper.binarySearch(0, locations.length, i -> x < locations[i]) - 1;
        }

        @Override
        @VisibleForTesting
        public String getDebugString() {
            return "Spline{coordinate=" + String.valueOf(this.locationFunction) + ", locations=" + this.format(this.locations) + ", derivatives=" + this.format(this.derivatives) + ", values=" + this.values.stream().map(Spline::getDebugString).collect(Collectors.joining(", ", "[", "]")) + "}";
        }

        private String format(float[] values) {
            return "[" + IntStream.range(0, values.length).mapToDouble(index -> values[index]).mapToObj(value -> String.format(Locale.ROOT, "%.3f", value)).collect(Collectors.joining(", ")) + "]";
        }

        @Override
        public Spline<C, I> apply(Visitor<I> visitor) {
            return Implementation.build((ToFloatFunction)visitor.visit(this.locationFunction), this.locations, this.values().stream().map(value -> value.apply(visitor)).toList(), this.derivatives);
        }
    }

    public static interface Visitor<I> {
        public I visit(I var1);
    }
}

