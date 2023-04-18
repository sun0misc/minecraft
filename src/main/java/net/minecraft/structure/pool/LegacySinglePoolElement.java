package net.minecraft.structure.pool;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;

public class LegacySinglePoolElement extends SinglePoolElement {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(locationGetter(), processorsGetter(), projectionGetter()).apply(instance, LegacySinglePoolElement::new);
   });

   protected LegacySinglePoolElement(Either either, RegistryEntry arg, StructurePool.Projection arg2) {
      super(either, arg, arg2);
   }

   protected StructurePlacementData createPlacementData(BlockRotation rotation, BlockBox box, boolean keepJigsaws) {
      StructurePlacementData lv = super.createPlacementData(rotation, box, keepJigsaws);
      lv.removeProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS);
      lv.addProcessor(BlockIgnoreStructureProcessor.IGNORE_AIR_AND_STRUCTURE_BLOCKS);
      return lv;
   }

   public StructurePoolElementType getType() {
      return StructurePoolElementType.LEGACY_SINGLE_POOL_ELEMENT;
   }

   public String toString() {
      return "LegacySingle[" + this.location + "]";
   }
}
