package net.minecraft.block;

public abstract class GourdBlock extends Block {
   public GourdBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public abstract StemBlock getStem();

   public abstract AttachedStemBlock getAttachedStem();
}
