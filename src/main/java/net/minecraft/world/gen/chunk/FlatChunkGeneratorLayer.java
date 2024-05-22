/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.chunk;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.world.dimension.DimensionType;

public class FlatChunkGeneratorLayer {
    public static final Codec<FlatChunkGeneratorLayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.intRange(0, DimensionType.MAX_HEIGHT).fieldOf("height")).forGetter(FlatChunkGeneratorLayer::getThickness), ((MapCodec)Registries.BLOCK.getCodec().fieldOf("block")).orElse(Blocks.AIR).forGetter(layer -> layer.getBlockState().getBlock())).apply((Applicative<FlatChunkGeneratorLayer, ?>)instance, FlatChunkGeneratorLayer::new));
    private final Block block;
    private final int thickness;

    public FlatChunkGeneratorLayer(int thickness, Block block) {
        this.thickness = thickness;
        this.block = block;
    }

    public int getThickness() {
        return this.thickness;
    }

    public BlockState getBlockState() {
        return this.block.getDefaultState();
    }

    public String toString() {
        return (String)(this.thickness != 1 ? this.thickness + "*" : "") + String.valueOf(Registries.BLOCK.getId(this.block));
    }
}

