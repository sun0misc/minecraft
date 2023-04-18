package net.minecraft.block;

public class MelonBlock extends GourdBlock {
   protected MelonBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public StemBlock getStem() {
      return (StemBlock)Blocks.MELON_STEM;
   }

   public AttachedStemBlock getAttachedStem() {
      return (AttachedStemBlock)Blocks.ATTACHED_MELON_STEM;
   }
}
