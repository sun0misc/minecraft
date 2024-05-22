/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure.pool;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.EmptyPoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class ListPoolElement
extends StructurePoolElement {
    public static final MapCodec<ListPoolElement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)StructurePoolElement.CODEC.listOf().fieldOf("elements")).forGetter(pool -> pool.elements), ListPoolElement.projectionGetter()).apply((Applicative<ListPoolElement, ?>)instance, ListPoolElement::new));
    private final List<StructurePoolElement> elements;

    public ListPoolElement(List<StructurePoolElement> elements, StructurePool.Projection projection) {
        super(projection);
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("Elements are empty");
        }
        this.elements = elements;
        this.setAllElementsProjection(projection);
    }

    @Override
    public Vec3i getStart(StructureTemplateManager structureTemplateManager, BlockRotation rotation) {
        int i = 0;
        int j = 0;
        int k = 0;
        for (StructurePoolElement lv : this.elements) {
            Vec3i lv2 = lv.getStart(structureTemplateManager, rotation);
            i = Math.max(i, lv2.getX());
            j = Math.max(j, lv2.getY());
            k = Math.max(k, lv2.getZ());
        }
        return new Vec3i(i, j, k);
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> getStructureBlockInfos(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation, Random random) {
        return this.elements.get(0).getStructureBlockInfos(structureTemplateManager, pos, rotation, random);
    }

    @Override
    public BlockBox getBoundingBox(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation) {
        Stream<BlockBox> stream = this.elements.stream().filter(element -> element != EmptyPoolElement.INSTANCE).map(element -> element.getBoundingBox(structureTemplateManager, pos, rotation));
        return BlockBox.encompass(stream::iterator).orElseThrow(() -> new IllegalStateException("Unable to calculate boundingbox for ListPoolElement"));
    }

    @Override
    public boolean generate(StructureTemplateManager structureTemplateManager, StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, BlockPos pos, BlockPos pivot, BlockRotation rotation, BlockBox box, Random random, boolean keepJigsaws) {
        for (StructurePoolElement lv : this.elements) {
            if (lv.generate(structureTemplateManager, world, structureAccessor, chunkGenerator, pos, pivot, rotation, box, random, keepJigsaws)) continue;
            return false;
        }
        return true;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return StructurePoolElementType.LIST_POOL_ELEMENT;
    }

    @Override
    public StructurePoolElement setProjection(StructurePool.Projection projection) {
        super.setProjection(projection);
        this.setAllElementsProjection(projection);
        return this;
    }

    public String toString() {
        return "List[" + this.elements.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }

    private void setAllElementsProjection(StructurePool.Projection projection) {
        this.elements.forEach(element -> element.setProjection(projection));
    }
}

