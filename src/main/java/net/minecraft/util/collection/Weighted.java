/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.collection;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.collection.Weight;

public interface Weighted {
    public Weight getWeight();

    public static <T> Present<T> of(T data, int weight) {
        return new Present<T>(data, Weight.of(weight));
    }

    public record Present<T>(T data, Weight weight) implements Weighted
    {
        @Override
        public Weight getWeight() {
            return this.weight;
        }

        public static <E> Codec<Present<E>> createCodec(Codec<E> dataCodec) {
            return RecordCodecBuilder.create(instance -> instance.group(((MapCodec)dataCodec.fieldOf("data")).forGetter(Present::data), ((MapCodec)Weight.CODEC.fieldOf("weight")).forGetter(Present::weight)).apply((Applicative<Present, ?>)instance, Present::new));
        }
    }

    public static class Absent
    implements Weighted {
        private final Weight weight;

        public Absent(int weight) {
            this.weight = Weight.of(weight);
        }

        public Absent(Weight weight) {
            this.weight = weight;
        }

        @Override
        public Weight getWeight() {
            return this.weight;
        }
    }
}

