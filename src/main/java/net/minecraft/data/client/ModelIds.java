package net.minecraft.data.client;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ModelIds {
   /** @deprecated */
   @Deprecated
   public static Identifier getMinecraftNamespacedBlock(String name) {
      return new Identifier("minecraft", "block/" + name);
   }

   public static Identifier getMinecraftNamespacedItem(String name) {
      return new Identifier("minecraft", "item/" + name);
   }

   public static Identifier getBlockSubModelId(Block block, String suffix) {
      Identifier lv = Registries.BLOCK.getId(block);
      return lv.withPath((path) -> {
         return "block/" + path + suffix;
      });
   }

   public static Identifier getBlockModelId(Block block) {
      Identifier lv = Registries.BLOCK.getId(block);
      return lv.withPrefixedPath("block/");
   }

   public static Identifier getItemModelId(Item item) {
      Identifier lv = Registries.ITEM.getId(item);
      return lv.withPrefixedPath("item/");
   }

   public static Identifier getItemSubModelId(Item item, String suffix) {
      Identifier lv = Registries.ITEM.getId(item);
      return lv.withPath((path) -> {
         return "item/" + path + suffix;
      });
   }
}
