/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.OctaveSimplexNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.BiomeParticleConfig;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import org.jetbrains.annotations.Nullable;

public final class Biome {
    public static final Codec<Biome> CODEC = RecordCodecBuilder.create(instance -> instance.group(Weather.CODEC.forGetter(biome -> biome.weather), ((MapCodec)BiomeEffects.CODEC.fieldOf("effects")).forGetter(biome -> biome.effects), GenerationSettings.CODEC.forGetter(biome -> biome.generationSettings), SpawnSettings.CODEC.forGetter(biome -> biome.spawnSettings)).apply((Applicative<Biome, ?>)instance, Biome::new));
    public static final Codec<Biome> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(Weather.CODEC.forGetter(biome -> biome.weather), ((MapCodec)BiomeEffects.CODEC.fieldOf("effects")).forGetter(biome -> biome.effects)).apply((Applicative<Biome, ?>)instance, (weather, effects) -> new Biome((Weather)weather, (BiomeEffects)effects, GenerationSettings.INSTANCE, SpawnSettings.INSTANCE)));
    public static final Codec<RegistryEntry<Biome>> REGISTRY_CODEC = RegistryElementCodec.of(RegistryKeys.BIOME, CODEC);
    public static final Codec<RegistryEntryList<Biome>> REGISTRY_ENTRY_LIST_CODEC = RegistryCodecs.entryList(RegistryKeys.BIOME, CODEC);
    private static final OctaveSimplexNoiseSampler TEMPERATURE_NOISE = new OctaveSimplexNoiseSampler((Random)new ChunkRandom(new CheckedRandom(1234L)), ImmutableList.of(Integer.valueOf(0)));
    static final OctaveSimplexNoiseSampler FROZEN_OCEAN_NOISE = new OctaveSimplexNoiseSampler((Random)new ChunkRandom(new CheckedRandom(3456L)), ImmutableList.of(Integer.valueOf(-2), Integer.valueOf(-1), Integer.valueOf(0)));
    @Deprecated(forRemoval=true)
    public static final OctaveSimplexNoiseSampler FOLIAGE_NOISE = new OctaveSimplexNoiseSampler((Random)new ChunkRandom(new CheckedRandom(2345L)), ImmutableList.of(Integer.valueOf(0)));
    private static final int MAX_TEMPERATURE_CACHE_SIZE = 1024;
    private final Weather weather;
    private final GenerationSettings generationSettings;
    private final SpawnSettings spawnSettings;
    private final BiomeEffects effects;
    private final ThreadLocal<Long2FloatLinkedOpenHashMap> temperatureCache = ThreadLocal.withInitial(() -> Util.make(() -> {
        Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(1024, 0.25f){

            @Override
            protected void rehash(int n) {
            }
        };
        long2FloatLinkedOpenHashMap.defaultReturnValue(Float.NaN);
        return long2FloatLinkedOpenHashMap;
    }));

    Biome(Weather weather, BiomeEffects effects, GenerationSettings generationSettings, SpawnSettings spawnSettings) {
        this.weather = weather;
        this.generationSettings = generationSettings;
        this.spawnSettings = spawnSettings;
        this.effects = effects;
    }

    public int getSkyColor() {
        return this.effects.getSkyColor();
    }

    public SpawnSettings getSpawnSettings() {
        return this.spawnSettings;
    }

    public boolean hasPrecipitation() {
        return this.weather.hasPrecipitation();
    }

    public Precipitation getPrecipitation(BlockPos pos) {
        if (!this.hasPrecipitation()) {
            return Precipitation.NONE;
        }
        return this.isCold(pos) ? Precipitation.SNOW : Precipitation.RAIN;
    }

    private float computeTemperature(BlockPos pos) {
        float f = this.weather.temperatureModifier.getModifiedTemperature(pos, this.getTemperature());
        if (pos.getY() > 80) {
            float g = (float)(TEMPERATURE_NOISE.sample((float)pos.getX() / 8.0f, (float)pos.getZ() / 8.0f, false) * 8.0);
            return f - (g + (float)pos.getY() - 80.0f) * 0.05f / 40.0f;
        }
        return f;
    }

    @Deprecated
    private float getTemperature(BlockPos blockPos) {
        long l = blockPos.asLong();
        Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = this.temperatureCache.get();
        float f = long2FloatLinkedOpenHashMap.get(l);
        if (!Float.isNaN(f)) {
            return f;
        }
        float g = this.computeTemperature(blockPos);
        if (long2FloatLinkedOpenHashMap.size() == 1024) {
            long2FloatLinkedOpenHashMap.removeFirstFloat();
        }
        long2FloatLinkedOpenHashMap.put(l, g);
        return g;
    }

    public boolean canSetIce(WorldView world, BlockPos blockPos) {
        return this.canSetIce(world, blockPos, true);
    }

    public boolean canSetIce(WorldView world, BlockPos pos, boolean doWaterCheck) {
        if (this.doesNotSnow(pos)) {
            return false;
        }
        if (pos.getY() >= world.getBottomY() && pos.getY() < world.getTopY() && world.getLightLevel(LightType.BLOCK, pos) < 10) {
            BlockState lv = world.getBlockState(pos);
            FluidState lv2 = world.getFluidState(pos);
            if (lv2.getFluid() == Fluids.WATER && lv.getBlock() instanceof FluidBlock) {
                boolean bl2;
                if (!doWaterCheck) {
                    return true;
                }
                boolean bl = bl2 = world.isWater(pos.west()) && world.isWater(pos.east()) && world.isWater(pos.north()) && world.isWater(pos.south());
                if (!bl2) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isCold(BlockPos pos) {
        return !this.doesNotSnow(pos);
    }

    public boolean doesNotSnow(BlockPos pos) {
        return this.getTemperature(pos) >= 0.15f;
    }

    public boolean shouldGenerateLowerFrozenOceanSurface(BlockPos pos) {
        return this.getTemperature(pos) > 0.1f;
    }

    public boolean canSetSnow(WorldView world, BlockPos pos) {
        BlockState lv;
        if (this.doesNotSnow(pos)) {
            return false;
        }
        return pos.getY() >= world.getBottomY() && pos.getY() < world.getTopY() && world.getLightLevel(LightType.BLOCK, pos) < 10 && ((lv = world.getBlockState(pos)).isAir() || lv.isOf(Blocks.SNOW)) && Blocks.SNOW.getDefaultState().canPlaceAt(world, pos);
    }

    public GenerationSettings getGenerationSettings() {
        return this.generationSettings;
    }

    public int getFogColor() {
        return this.effects.getFogColor();
    }

    public int getGrassColorAt(double x, double z) {
        int i = this.effects.getGrassColor().orElseGet(this::getDefaultGrassColor);
        return this.effects.getGrassColorModifier().getModifiedGrassColor(x, z, i);
    }

    private int getDefaultGrassColor() {
        double d = MathHelper.clamp(this.weather.temperature, 0.0f, 1.0f);
        double e = MathHelper.clamp(this.weather.downfall, 0.0f, 1.0f);
        return GrassColors.getColor(d, e);
    }

    public int getFoliageColor() {
        return this.effects.getFoliageColor().orElseGet(this::getDefaultFoliageColor);
    }

    private int getDefaultFoliageColor() {
        double d = MathHelper.clamp(this.weather.temperature, 0.0f, 1.0f);
        double e = MathHelper.clamp(this.weather.downfall, 0.0f, 1.0f);
        return FoliageColors.getColor(d, e);
    }

    public float getTemperature() {
        return this.weather.temperature;
    }

    public BiomeEffects getEffects() {
        return this.effects;
    }

    public int getWaterColor() {
        return this.effects.getWaterColor();
    }

    public int getWaterFogColor() {
        return this.effects.getWaterFogColor();
    }

    public Optional<BiomeParticleConfig> getParticleConfig() {
        return this.effects.getParticleConfig();
    }

    public Optional<RegistryEntry<SoundEvent>> getLoopSound() {
        return this.effects.getLoopSound();
    }

    public Optional<BiomeMoodSound> getMoodSound() {
        return this.effects.getMoodSound();
    }

    public Optional<BiomeAdditionsSound> getAdditionsSound() {
        return this.effects.getAdditionsSound();
    }

    public Optional<MusicSound> getMusic() {
        return this.effects.getMusic();
    }

    record Weather(boolean hasPrecipitation, float temperature, TemperatureModifier temperatureModifier, float downfall) {
        public static final MapCodec<Weather> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.BOOL.fieldOf("has_precipitation")).forGetter(weather -> weather.hasPrecipitation), ((MapCodec)Codec.FLOAT.fieldOf("temperature")).forGetter(weather -> Float.valueOf(weather.temperature)), TemperatureModifier.CODEC.optionalFieldOf("temperature_modifier", TemperatureModifier.NONE).forGetter(weather -> weather.temperatureModifier), ((MapCodec)Codec.FLOAT.fieldOf("downfall")).forGetter(weather -> Float.valueOf(weather.downfall))).apply((Applicative<Weather, ?>)instance, Weather::new));
    }

    public static enum Precipitation implements StringIdentifiable
    {
        NONE("none"),
        RAIN("rain"),
        SNOW("snow");

        public static final Codec<Precipitation> CODEC;
        private final String name;

        private Precipitation(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }

        static {
            CODEC = StringIdentifiable.createCodec(Precipitation::values);
        }
    }

    public static enum TemperatureModifier implements StringIdentifiable
    {
        NONE("none"){

            @Override
            public float getModifiedTemperature(BlockPos pos, float temperature) {
                return temperature;
            }
        }
        ,
        FROZEN("frozen"){

            @Override
            public float getModifiedTemperature(BlockPos pos, float temperature) {
                double h;
                double e;
                double d = FROZEN_OCEAN_NOISE.sample((double)pos.getX() * 0.05, (double)pos.getZ() * 0.05, false) * 7.0;
                double g = d + (e = FOLIAGE_NOISE.sample((double)pos.getX() * 0.2, (double)pos.getZ() * 0.2, false));
                if (g < 0.3 && (h = FOLIAGE_NOISE.sample((double)pos.getX() * 0.09, (double)pos.getZ() * 0.09, false)) < 0.8) {
                    return 0.2f;
                }
                return temperature;
            }
        };

        private final String name;
        public static final Codec<TemperatureModifier> CODEC;

        public abstract float getModifiedTemperature(BlockPos var1, float var2);

        TemperatureModifier(String name) {
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
            CODEC = StringIdentifiable.createCodec(TemperatureModifier::values);
        }
    }

    public static class Builder {
        private boolean precipitation = true;
        @Nullable
        private Float temperature;
        private TemperatureModifier temperatureModifier = TemperatureModifier.NONE;
        @Nullable
        private Float downfall;
        @Nullable
        private BiomeEffects specialEffects;
        @Nullable
        private SpawnSettings spawnSettings;
        @Nullable
        private GenerationSettings generationSettings;

        public Builder precipitation(boolean precipitation) {
            this.precipitation = precipitation;
            return this;
        }

        public Builder temperature(float temperature) {
            this.temperature = Float.valueOf(temperature);
            return this;
        }

        public Builder downfall(float downfall) {
            this.downfall = Float.valueOf(downfall);
            return this;
        }

        public Builder effects(BiomeEffects effects) {
            this.specialEffects = effects;
            return this;
        }

        public Builder spawnSettings(SpawnSettings spawnSettings) {
            this.spawnSettings = spawnSettings;
            return this;
        }

        public Builder generationSettings(GenerationSettings generationSettings) {
            this.generationSettings = generationSettings;
            return this;
        }

        public Builder temperatureModifier(TemperatureModifier temperatureModifier) {
            this.temperatureModifier = temperatureModifier;
            return this;
        }

        public Biome build() {
            if (this.temperature == null || this.downfall == null || this.specialEffects == null || this.spawnSettings == null || this.generationSettings == null) {
                throw new IllegalStateException("You are missing parameters to build a proper biome\n" + String.valueOf(this));
            }
            return new Biome(new Weather(this.precipitation, this.temperature.floatValue(), this.temperatureModifier, this.downfall.floatValue()), this.specialEffects, this.generationSettings, this.spawnSettings);
        }

        public String toString() {
            return "BiomeBuilder{\nhasPrecipitation=" + this.precipitation + ",\ntemperature=" + this.temperature + ",\ntemperatureModifier=" + String.valueOf(this.temperatureModifier) + ",\ndownfall=" + this.downfall + ",\nspecialEffects=" + String.valueOf(this.specialEffects) + ",\nmobSpawnSettings=" + String.valueOf(this.spawnSettings) + ",\ngenerationSettings=" + String.valueOf(this.generationSettings) + ",\n}";
        }
    }
}

