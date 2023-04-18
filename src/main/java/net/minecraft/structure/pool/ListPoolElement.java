package net.minecraft.structure.pool;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class ListPoolElement extends StructurePoolElement {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(StructurePoolElement.CODEC.listOf().fieldOf("elements").forGetter((pool) -> {
         return pool.elements;
      }), projectionGetter()).apply(instance, ListPoolElement::new);
   });
   private final List elements;

   public ListPoolElement(List elements, StructurePool.Projection projection) {
      super(projection);
      if (elements.isEmpty()) {
         throw new IllegalArgumentException("Elements are empty");
      } else {
         this.elements = elements;
         this.setAllElementsProjection(projection);
      }
   }

   public Vec3i getStart(StructureTemplateManager structureTemplateManager, BlockRotation rotation) {
      int i = 0;
      int j = 0;
      int k = 0;

      Vec3i lv2;
      for(Iterator var6 = this.elements.iterator(); var6.hasNext(); k = Math.max(k, lv2.getZ())) {
         StructurePoolElement lv = (StructurePoolElement)var6.next();
         lv2 = lv.getStart(structureTemplateManager, rotation);
         i = Math.max(i, lv2.getX());
         j = Math.max(j, lv2.getY());
      }

      return new Vec3i(i, j, k);
   }

   public List getStructureBlockInfos(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation, Random random) {
      return ((StructurePoolElement)this.elements.get(0)).getStructureBlockInfos(structureTemplateManager, pos, rotation, random);
   }

   public BlockBox getBoundingBox(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation) {
      Stream stream = this.elements.stream().filter((element) -> {
         return element != EmptyPoolElement.INSTANCE;
      }).map((element) -> {
         return element.getBoundingBox(structureTemplateManager, pos, rotation);
      });
      Objects.requireNonNull(stream);
      return (BlockBox)BlockBox.encompass(stream::iterator).orElseThrow(() -> {
         return new IllegalStateException("Unable to calculate boundingbox for ListPoolElement");
      });
   }

   public boolean generate(StructureTemplateManager structureTemplateManager, StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, BlockPos pos, BlockPos pivot, BlockRotation rotation, BlockBox box, Random random, boolean keepJigsaws) {
      Iterator var11 = this.elements.iterator();

      StructurePoolElement lv;
      do {
         if (!var11.hasNext()) {
            return true;
         }

         lv = (StructurePoolElement)var11.next();
      } while(lv.generate(structureTemplateManager, world, structureAccessor, chunkGenerator, pos, pivot, rotation, box, random, keepJigsaws));

      return false;
   }

   public StructurePoolElementType getType() {
      return StructurePoolElementType.LIST_POOL_ELEMENT;
   }

   public StructurePoolElement setProjection(StructurePool.Projection projection) {
      super.setProjection(projection);
      this.setAllElementsProjection(projection);
      return this;
   }

   public String toString() {
      Stream var10000 = this.elements.stream().map(Object::toString);
      return "List[" + (String)var10000.collect(Collectors.joining(", ")) + "]";
   }

   private void setAllElementsProjection(StructurePool.Projection projection) {
      this.elements.forEach((element) -> {
         element.setProjection(projection);
      });
   }
}
