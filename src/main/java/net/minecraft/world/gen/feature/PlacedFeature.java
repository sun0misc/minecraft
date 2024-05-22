/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import org.apache.commons.lang3.mutable.MutableBoolean;

public record PlacedFeature(RegistryEntry<ConfiguredFeature<?, ?>> feature, List<PlacementModifier> placementModifiers) {
    public static final Codec<PlacedFeature> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ConfiguredFeature.REGISTRY_CODEC.fieldOf("feature")).forGetter(arg -> arg.feature), ((MapCodec)PlacementModifier.CODEC.listOf().fieldOf("placement")).forGetter(arg -> arg.placementModifiers)).apply((Applicative<PlacedFeature, ?>)instance, PlacedFeature::new));
    public static final Codec<RegistryEntry<PlacedFeature>> REGISTRY_CODEC = RegistryElementCodec.of(RegistryKeys.PLACED_FEATURE, CODEC);
    public static final Codec<RegistryEntryList<PlacedFeature>> LIST_CODEC = RegistryCodecs.entryList(RegistryKeys.PLACED_FEATURE, CODEC);
    public static final Codec<List<RegistryEntryList<PlacedFeature>>> LISTS_CODEC = RegistryCodecs.entryList(RegistryKeys.PLACED_FEATURE, CODEC, true).listOf();

    public boolean generateUnregistered(StructureWorldAccess world, ChunkGenerator generator, Random random, BlockPos pos) {
        return this.generate(new FeaturePlacementContext(world, generator, Optional.empty()), random, pos);
    }

    public boolean generate(StructureWorldAccess world, ChunkGenerator generator, Random random, BlockPos pos) {
        return this.generate(new FeaturePlacementContext(world, generator, Optional.of(this)), random, pos);
    }

    private boolean generate(FeaturePlacementContext context, Random random, BlockPos pos) {
        Stream<BlockPos> stream = Stream.of(pos);
        for (PlacementModifier lv : this.placementModifiers) {
            stream = stream.flatMap(posx -> lv.getPositions(context, random, (BlockPos)posx));
        }
        ConfiguredFeature<?, ?> lv2 = this.feature.value();
        MutableBoolean mutableBoolean = new MutableBoolean();
        stream.forEach(placedPos -> {
            if (lv2.generate(context.getWorld(), context.getChunkGenerator(), random, (BlockPos)placedPos)) {
                mutableBoolean.setTrue();
            }
        });
        return mutableBoolean.isTrue();
    }

    public Stream<ConfiguredFeature<?, ?>> getDecoratedFeatures() {
        return this.feature.value().getDecoratedFeatures();
    }

    @Override
    public String toString() {
        return "Placed " + String.valueOf(this.feature);
    }
}

