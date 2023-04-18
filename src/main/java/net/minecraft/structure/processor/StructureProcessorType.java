package net.minecraft.structure.processor;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;

public interface StructureProcessorType {
   Codec CODEC = Registries.STRUCTURE_PROCESSOR.getCodec().dispatch("processor_type", StructureProcessor::getType, StructureProcessorType::codec);
   Codec LIST_CODEC = CODEC.listOf().xmap(StructureProcessorList::new, StructureProcessorList::getList);
   Codec PROCESSORS_CODEC = Codec.either(LIST_CODEC.fieldOf("processors").codec(), LIST_CODEC).xmap((either) -> {
      return (StructureProcessorList)either.map((arg) -> {
         return arg;
      }, (arg) -> {
         return arg;
      });
   }, Either::left);
   Codec REGISTRY_CODEC = RegistryElementCodec.of(RegistryKeys.PROCESSOR_LIST, PROCESSORS_CODEC);
   StructureProcessorType BLOCK_IGNORE = register("block_ignore", BlockIgnoreStructureProcessor.CODEC);
   StructureProcessorType BLOCK_ROT = register("block_rot", BlockRotStructureProcessor.CODEC);
   StructureProcessorType GRAVITY = register("gravity", GravityStructureProcessor.CODEC);
   StructureProcessorType JIGSAW_REPLACEMENT = register("jigsaw_replacement", JigsawReplacementStructureProcessor.CODEC);
   StructureProcessorType RULE = register("rule", RuleStructureProcessor.CODEC);
   StructureProcessorType NOP = register("nop", NopStructureProcessor.CODEC);
   StructureProcessorType BLOCK_AGE = register("block_age", BlockAgeStructureProcessor.CODEC);
   StructureProcessorType BLACKSTONE_REPLACE = register("blackstone_replace", BlackstoneReplacementStructureProcessor.CODEC);
   StructureProcessorType LAVA_SUBMERGED_BLOCK = register("lava_submerged_block", LavaSubmergedBlockStructureProcessor.CODEC);
   StructureProcessorType PROTECTED_BLOCKS = register("protected_blocks", ProtectedBlocksStructureProcessor.CODEC);
   StructureProcessorType CAPPED = register("capped", CappedStructureProcessor.CODEC);

   Codec codec();

   static StructureProcessorType register(String id, Codec codec) {
      return (StructureProcessorType)Registry.register(Registries.STRUCTURE_PROCESSOR, (String)id, () -> {
         return codec;
      });
   }
}
