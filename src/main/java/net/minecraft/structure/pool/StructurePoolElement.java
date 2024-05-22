/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.structure.pool;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.EmptyPoolElement;
import net.minecraft.structure.pool.FeaturePoolElement;
import net.minecraft.structure.pool.LegacySinglePoolElement;
import net.minecraft.structure.pool.ListPoolElement;
import net.minecraft.structure.pool.SinglePoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.jetbrains.annotations.Nullable;

public abstract class StructurePoolElement {
    public static final Codec<StructurePoolElement> CODEC = Registries.STRUCTURE_POOL_ELEMENT.getCodec().dispatch("element_type", StructurePoolElement::getType, StructurePoolElementType::codec);
    private static final RegistryEntry<StructureProcessorList> EMPTY_PROCESSORS = RegistryEntry.of(new StructureProcessorList(List.of()));
    @Nullable
    private volatile StructurePool.Projection projection;

    protected static <E extends StructurePoolElement> RecordCodecBuilder<E, StructurePool.Projection> projectionGetter() {
        return ((MapCodec)StructurePool.Projection.CODEC.fieldOf("projection")).forGetter(StructurePoolElement::getProjection);
    }

    protected StructurePoolElement(StructurePool.Projection projection) {
        this.projection = projection;
    }

    public abstract Vec3i getStart(StructureTemplateManager var1, BlockRotation var2);

    public abstract List<StructureTemplate.StructureBlockInfo> getStructureBlockInfos(StructureTemplateManager var1, BlockPos var2, BlockRotation var3, Random var4);

    public abstract BlockBox getBoundingBox(StructureTemplateManager var1, BlockPos var2, BlockRotation var3);

    public abstract boolean generate(StructureTemplateManager var1, StructureWorldAccess var2, StructureAccessor var3, ChunkGenerator var4, BlockPos var5, BlockPos var6, BlockRotation var7, BlockBox var8, Random var9, boolean var10);

    public abstract StructurePoolElementType<?> getType();

    public void method_16756(WorldAccess world, StructureTemplate.StructureBlockInfo structureBlockInfo, BlockPos pos, BlockRotation rotation, Random random, BlockBox box) {
    }

    public StructurePoolElement setProjection(StructurePool.Projection projection) {
        this.projection = projection;
        return this;
    }

    public StructurePool.Projection getProjection() {
        StructurePool.Projection lv = this.projection;
        if (lv == null) {
            throw new IllegalStateException();
        }
        return lv;
    }

    public int getGroundLevelDelta() {
        return 1;
    }

    public static Function<StructurePool.Projection, EmptyPoolElement> ofEmpty() {
        return projection -> EmptyPoolElement.INSTANCE;
    }

    public static Function<StructurePool.Projection, LegacySinglePoolElement> ofLegacySingle(String id) {
        return projection -> new LegacySinglePoolElement(Either.left(Identifier.method_60654(id)), EMPTY_PROCESSORS, (StructurePool.Projection)projection);
    }

    public static Function<StructurePool.Projection, LegacySinglePoolElement> ofProcessedLegacySingle(String id, RegistryEntry<StructureProcessorList> processorListEntry) {
        return projection -> new LegacySinglePoolElement(Either.left(Identifier.method_60654(id)), processorListEntry, (StructurePool.Projection)projection);
    }

    public static Function<StructurePool.Projection, SinglePoolElement> ofSingle(String id) {
        return projection -> new SinglePoolElement(Either.left(Identifier.method_60654(id)), EMPTY_PROCESSORS, (StructurePool.Projection)projection);
    }

    public static Function<StructurePool.Projection, SinglePoolElement> ofProcessedSingle(String id, RegistryEntry<StructureProcessorList> processorListEntry) {
        return projection -> new SinglePoolElement(Either.left(Identifier.method_60654(id)), processorListEntry, (StructurePool.Projection)projection);
    }

    public static Function<StructurePool.Projection, FeaturePoolElement> ofFeature(RegistryEntry<PlacedFeature> placedFeatureEntry) {
        return projection -> new FeaturePoolElement(placedFeatureEntry, (StructurePool.Projection)projection);
    }

    public static Function<StructurePool.Projection, ListPoolElement> ofList(List<Function<StructurePool.Projection, ? extends StructurePoolElement>> elementGetters) {
        return projection -> new ListPoolElement(elementGetters.stream().map(elementGetter -> (StructurePoolElement)elementGetter.apply(projection)).collect(Collectors.toList()), (StructurePool.Projection)projection);
    }
}

