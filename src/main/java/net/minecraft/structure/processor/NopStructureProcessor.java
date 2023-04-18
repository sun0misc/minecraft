package net.minecraft.structure.processor;

import com.mojang.serialization.Codec;

public class NopStructureProcessor extends StructureProcessor {
   public static final Codec CODEC = Codec.unit(() -> {
      return INSTANCE;
   });
   public static final NopStructureProcessor INSTANCE = new NopStructureProcessor();

   private NopStructureProcessor() {
   }

   protected StructureProcessorType getType() {
      return StructureProcessorType.NOP;
   }
}
