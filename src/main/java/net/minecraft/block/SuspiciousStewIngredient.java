package net.minecraft.block;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;

public interface SuspiciousStewIngredient {
   StatusEffect getEffectInStew();

   int getEffectInStewDuration();

   static List getAll() {
      return (List)Registries.ITEM.stream().map(SuspiciousStewIngredient::of).filter(Objects::nonNull).collect(Collectors.toList());
   }

   @Nullable
   static SuspiciousStewIngredient of(ItemConvertible item) {
      Item var3 = item.asItem();
      if (var3 instanceof BlockItem lv) {
         Block var6 = lv.getBlock();
         if (var6 instanceof SuspiciousStewIngredient lv2) {
            return lv2;
         }
      }

      Item var2 = item.asItem();
      if (var2 instanceof SuspiciousStewIngredient lv3) {
         return lv3;
      } else {
         return null;
      }
   }
}
