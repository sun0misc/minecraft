package net.minecraft.item;

import net.minecraft.block.Block;

public class AliasedBlockItem extends BlockItem {
   public AliasedBlockItem(Block arg, Item.Settings arg2) {
      super(arg, arg2);
   }

   public String getTranslationKey() {
      return this.getOrCreateTranslationKey();
   }
}
