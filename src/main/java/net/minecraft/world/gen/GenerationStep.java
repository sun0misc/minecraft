/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public class GenerationStep {

    public static enum Carver implements StringIdentifiable
    {
        AIR("air"),
        LIQUID("liquid");

        public static final Codec<Carver> CODEC;
        private final String name;

        private Carver(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String asString() {
            return this.name;
        }

        static {
            CODEC = StringIdentifiable.createCodec(Carver::values);
        }
    }

    public static enum Feature implements StringIdentifiable
    {
        RAW_GENERATION("raw_generation"),
        LAKES("lakes"),
        LOCAL_MODIFICATIONS("local_modifications"),
        UNDERGROUND_STRUCTURES("underground_structures"),
        SURFACE_STRUCTURES("surface_structures"),
        STRONGHOLDS("strongholds"),
        UNDERGROUND_ORES("underground_ores"),
        UNDERGROUND_DECORATION("underground_decoration"),
        FLUID_SPRINGS("fluid_springs"),
        VEGETAL_DECORATION("vegetal_decoration"),
        TOP_LAYER_MODIFICATION("top_layer_modification");

        public static final Codec<Feature> CODEC;
        private final String name;

        private Feature(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String asString() {
            return this.name;
        }

        static {
            CODEC = StringIdentifiable.createCodec(Feature::values);
        }
    }
}

