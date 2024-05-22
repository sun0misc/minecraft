/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.chunk;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.dimension.DimensionType;

public record GenerationShapeConfig(int minimumY, int height, int horizontalSize, int verticalSize) {
    public static final Codec<GenerationShapeConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.intRange(DimensionType.MIN_HEIGHT, DimensionType.MAX_COLUMN_HEIGHT).fieldOf("min_y")).forGetter(GenerationShapeConfig::minimumY), ((MapCodec)Codec.intRange(0, DimensionType.MAX_HEIGHT).fieldOf("height")).forGetter(GenerationShapeConfig::height), ((MapCodec)Codec.intRange(1, 4).fieldOf("size_horizontal")).forGetter(GenerationShapeConfig::horizontalSize), ((MapCodec)Codec.intRange(1, 4).fieldOf("size_vertical")).forGetter(GenerationShapeConfig::verticalSize)).apply((Applicative<GenerationShapeConfig, ?>)instance, GenerationShapeConfig::new)).comapFlatMap(GenerationShapeConfig::checkHeight, Function.identity());
    protected static final GenerationShapeConfig SURFACE = GenerationShapeConfig.create(-64, 384, 1, 2);
    protected static final GenerationShapeConfig NETHER = GenerationShapeConfig.create(0, 128, 1, 2);
    protected static final GenerationShapeConfig END = GenerationShapeConfig.create(0, 128, 2, 1);
    protected static final GenerationShapeConfig CAVES = GenerationShapeConfig.create(-64, 192, 1, 2);
    protected static final GenerationShapeConfig FLOATING_ISLANDS = GenerationShapeConfig.create(0, 256, 2, 1);

    private static DataResult<GenerationShapeConfig> checkHeight(GenerationShapeConfig config) {
        if (config.minimumY() + config.height() > DimensionType.MAX_COLUMN_HEIGHT + 1) {
            return DataResult.error(() -> "min_y + height cannot be higher than: " + (DimensionType.MAX_COLUMN_HEIGHT + 1));
        }
        if (config.height() % 16 != 0) {
            return DataResult.error(() -> "height has to be a multiple of 16");
        }
        if (config.minimumY() % 16 != 0) {
            return DataResult.error(() -> "min_y has to be a multiple of 16");
        }
        return DataResult.success(config);
    }

    public static GenerationShapeConfig create(int minimumY, int height, int horizontalSize, int verticalSize) {
        GenerationShapeConfig lv = new GenerationShapeConfig(minimumY, height, horizontalSize, verticalSize);
        GenerationShapeConfig.checkHeight(lv).error().ifPresent(error -> {
            throw new IllegalStateException(error.message());
        });
        return lv;
    }

    public int verticalCellBlockCount() {
        return BiomeCoords.toBlock(this.verticalSize());
    }

    public int horizontalCellBlockCount() {
        return BiomeCoords.toBlock(this.horizontalSize());
    }

    public GenerationShapeConfig trimHeight(HeightLimitView world) {
        int i = Math.max(this.minimumY, world.getBottomY());
        int j = Math.min(this.minimumY + this.height, world.getTopY()) - i;
        return new GenerationShapeConfig(i, j, this.horizontalSize, this.verticalSize);
    }
}

