package net.minecraft.structure.pool;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.structure.processor.JigsawReplacementStructureProcessor;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class SinglePoolElement extends StructurePoolElement {
   private static final Codec LOCATION_CODEC;
   public static final Codec CODEC;
   protected final Either location;
   protected final RegistryEntry processors;

   private static DataResult encodeLocation(Either location, DynamicOps ops, Object prefix) {
      Optional optional = location.left();
      return !optional.isPresent() ? DataResult.error(() -> {
         return "Can not serialize a runtime pool element";
      }) : Identifier.CODEC.encode((Identifier)optional.get(), ops, prefix);
   }

   protected static RecordCodecBuilder processorsGetter() {
      return StructureProcessorType.REGISTRY_CODEC.fieldOf("processors").forGetter((pool) -> {
         return pool.processors;
      });
   }

   protected static RecordCodecBuilder locationGetter() {
      return LOCATION_CODEC.fieldOf("location").forGetter((pool) -> {
         return pool.location;
      });
   }

   protected SinglePoolElement(Either location, RegistryEntry processors, StructurePool.Projection projection) {
      super(projection);
      this.location = location;
      this.processors = processors;
   }

   public Vec3i getStart(StructureTemplateManager structureTemplateManager, BlockRotation rotation) {
      StructureTemplate lv = this.getStructure(structureTemplateManager);
      return lv.getRotatedSize(rotation);
   }

   private StructureTemplate getStructure(StructureTemplateManager structureTemplateManager) {
      Either var10000 = this.location;
      Objects.requireNonNull(structureTemplateManager);
      return (StructureTemplate)var10000.map(structureTemplateManager::getTemplateOrBlank, Function.identity());
   }

   public List getDataStructureBlocks(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation, boolean mirroredAndRotated) {
      StructureTemplate lv = this.getStructure(structureTemplateManager);
      List list = lv.getInfosForBlock(pos, (new StructurePlacementData()).setRotation(rotation), Blocks.STRUCTURE_BLOCK, mirroredAndRotated);
      List list2 = Lists.newArrayList();
      Iterator var8 = list.iterator();

      while(var8.hasNext()) {
         StructureTemplate.StructureBlockInfo lv2 = (StructureTemplate.StructureBlockInfo)var8.next();
         NbtCompound lv3 = lv2.nbt();
         if (lv3 != null) {
            StructureBlockMode lv4 = StructureBlockMode.valueOf(lv3.getString("mode"));
            if (lv4 == StructureBlockMode.DATA) {
               list2.add(lv2);
            }
         }
      }

      return list2;
   }

   public List getStructureBlockInfos(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation, Random random) {
      StructureTemplate lv = this.getStructure(structureTemplateManager);
      ObjectArrayList objectArrayList = lv.getInfosForBlock(pos, (new StructurePlacementData()).setRotation(rotation), Blocks.JIGSAW, true);
      Util.shuffle(objectArrayList, random);
      return objectArrayList;
   }

   public BlockBox getBoundingBox(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation) {
      StructureTemplate lv = this.getStructure(structureTemplateManager);
      return lv.calculateBoundingBox((new StructurePlacementData()).setRotation(rotation), pos);
   }

   public boolean generate(StructureTemplateManager structureTemplateManager, StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, BlockPos pos, BlockPos pivot, BlockRotation rotation, BlockBox box, Random random, boolean keepJigsaws) {
      StructureTemplate lv = this.getStructure(structureTemplateManager);
      StructurePlacementData lv2 = this.createPlacementData(rotation, box, keepJigsaws);
      if (!lv.place(world, pos, pivot, lv2, random, 18)) {
         return false;
      } else {
         List list = StructureTemplate.process(world, pos, pivot, lv2, this.getDataStructureBlocks(structureTemplateManager, pos, rotation, false));
         Iterator var14 = list.iterator();

         while(var14.hasNext()) {
            StructureTemplate.StructureBlockInfo lv3 = (StructureTemplate.StructureBlockInfo)var14.next();
            this.method_16756(world, lv3, pos, rotation, random, box);
         }

         return true;
      }
   }

   protected StructurePlacementData createPlacementData(BlockRotation rotation, BlockBox box, boolean keepJigsaws) {
      StructurePlacementData lv = new StructurePlacementData();
      lv.setBoundingBox(box);
      lv.setRotation(rotation);
      lv.setUpdateNeighbors(true);
      lv.setIgnoreEntities(false);
      lv.addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS);
      lv.setInitializeMobs(true);
      if (!keepJigsaws) {
         lv.addProcessor(JigsawReplacementStructureProcessor.INSTANCE);
      }

      List var10000 = ((StructureProcessorList)this.processors.value()).getList();
      Objects.requireNonNull(lv);
      var10000.forEach(lv::addProcessor);
      ImmutableList var5 = this.getProjection().getProcessors();
      Objects.requireNonNull(lv);
      var5.forEach(lv::addProcessor);
      return lv;
   }

   public StructurePoolElementType getType() {
      return StructurePoolElementType.SINGLE_POOL_ELEMENT;
   }

   public String toString() {
      return "Single[" + this.location + "]";
   }

   static {
      LOCATION_CODEC = Codec.of(SinglePoolElement::encodeLocation, Identifier.CODEC.map(Either::left));
      CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(locationGetter(), processorsGetter(), projectionGetter()).apply(instance, SinglePoolElement::new);
      });
   }
}
