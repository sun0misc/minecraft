/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.chunk.placement;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;

public class ConcentricRingsStructurePlacement
extends StructurePlacement {
    public static final MapCodec<ConcentricRingsStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> ConcentricRingsStructurePlacement.buildConcentricRingsCodec(instance).apply((Applicative<RecordCodecBuilder.Mu<ConcentricRingsStructurePlacement>, ?>)instance, ConcentricRingsStructurePlacement::new));
    private final int distance;
    private final int spread;
    private final int count;
    private final RegistryEntryList<Biome> preferredBiomes;

    private static Products.P9<RecordCodecBuilder.Mu<ConcentricRingsStructurePlacement>, Vec3i, StructurePlacement.FrequencyReductionMethod, Float, Integer, Optional<StructurePlacement.ExclusionZone>, Integer, Integer, Integer, RegistryEntryList<Biome>> buildConcentricRingsCodec(RecordCodecBuilder.Instance<ConcentricRingsStructurePlacement> instance) {
        Products.P5<RecordCodecBuilder.Mu<ConcentricRingsStructurePlacement>, Vec3i, StructurePlacement.FrequencyReductionMethod, Float, Integer, Optional<StructurePlacement.ExclusionZone>> p5 = ConcentricRingsStructurePlacement.buildCodec(instance);
        Products.P4<ConcentricRingsStructurePlacement, Integer, Integer, Integer, RegistryEntryList> p4 = instance.group(((MapCodec)Codec.intRange(0, 1023).fieldOf("distance")).forGetter(ConcentricRingsStructurePlacement::getDistance), ((MapCodec)Codec.intRange(0, 1023).fieldOf("spread")).forGetter(ConcentricRingsStructurePlacement::getSpread), ((MapCodec)Codec.intRange(1, 4095).fieldOf("count")).forGetter(ConcentricRingsStructurePlacement::getCount), ((MapCodec)RegistryCodecs.entryList(RegistryKeys.BIOME).fieldOf("preferred_biomes")).forGetter(ConcentricRingsStructurePlacement::getPreferredBiomes));
        return new Products.P9<ConcentricRingsStructurePlacement, Vec3i, StructurePlacement.FrequencyReductionMethod, Float, Integer, Optional<StructurePlacement.ExclusionZone>, Integer, Integer, Integer, RegistryEntryList>(p5.t1(), p5.t2(), p5.t3(), p5.t4(), p5.t5(), p4.t1(), p4.t2(), p4.t3(), p4.t4());
    }

    public ConcentricRingsStructurePlacement(Vec3i locateOffset, StructurePlacement.FrequencyReductionMethod generationPredicateType, float frequency, int salt, Optional<StructurePlacement.ExclusionZone> exclusionZone, int distance, int spread, int structureCount, RegistryEntryList<Biome> preferredBiomes) {
        super(locateOffset, generationPredicateType, frequency, salt, exclusionZone);
        this.distance = distance;
        this.spread = spread;
        this.count = structureCount;
        this.preferredBiomes = preferredBiomes;
    }

    public ConcentricRingsStructurePlacement(int distance, int spread, int structureCount, RegistryEntryList<Biome> preferredBiomes) {
        this(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0f, 0, Optional.empty(), distance, spread, structureCount, preferredBiomes);
    }

    public int getDistance() {
        return this.distance;
    }

    public int getSpread() {
        return this.spread;
    }

    public int getCount() {
        return this.count;
    }

    public RegistryEntryList<Biome> getPreferredBiomes() {
        return this.preferredBiomes;
    }

    @Override
    protected boolean isStartChunk(StructurePlacementCalculator calculator, int chunkX, int chunkZ) {
        List<ChunkPos> list = calculator.getPlacementPositions(this);
        if (list == null) {
            return false;
        }
        return list.contains(new ChunkPos(chunkX, chunkZ));
    }

    @Override
    public StructurePlacementType<?> getType() {
        return StructurePlacementType.CONCENTRIC_RINGS;
    }
}

