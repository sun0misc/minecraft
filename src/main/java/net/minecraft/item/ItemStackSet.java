package net.minecraft.item;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import java.util.Set;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public class ItemStackSet {
   private static final Hash.Strategy HASH_STRATEGY = new Hash.Strategy() {
      public int hashCode(@Nullable ItemStack arg) {
         return ItemStackSet.getHashCode(arg);
      }

      public boolean equals(@Nullable ItemStack arg, @Nullable ItemStack arg2) {
         return arg == arg2 || arg != null && arg2 != null && arg.isEmpty() == arg2.isEmpty() && ItemStack.canCombine(arg, arg2);
      }

      // $FF: synthetic method
      public boolean equals(@Nullable Object first, @Nullable Object second) {
         return this.equals((ItemStack)first, (ItemStack)second);
      }

      // $FF: synthetic method
      public int hashCode(@Nullable Object stack) {
         return this.hashCode((ItemStack)stack);
      }
   };

   static int getHashCode(@Nullable ItemStack stack) {
      if (stack != null) {
         NbtCompound lv = stack.getNbt();
         int i = 31 + stack.getItem().hashCode();
         return 31 * i + (lv == null ? 0 : lv.hashCode());
      } else {
         return 0;
      }
   }

   public static Set create() {
      return new ObjectLinkedOpenCustomHashSet(HASH_STRATEGY);
   }
}
