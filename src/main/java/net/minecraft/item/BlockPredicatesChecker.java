package net.minecraft.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class BlockPredicatesChecker {
   private final String key;
   @Nullable
   private CachedBlockPosition cachedPos;
   private boolean lastResult;
   private boolean nbtAware;

   public BlockPredicatesChecker(String key) {
      this.key = key;
   }

   private static boolean canUseCache(CachedBlockPosition pos, @Nullable CachedBlockPosition cachedPos, boolean nbtAware) {
      if (cachedPos != null && pos.getBlockState() == cachedPos.getBlockState()) {
         if (!nbtAware) {
            return true;
         } else if (pos.getBlockEntity() == null && cachedPos.getBlockEntity() == null) {
            return true;
         } else {
            return pos.getBlockEntity() != null && cachedPos.getBlockEntity() != null ? Objects.equals(pos.getBlockEntity().createNbtWithId(), cachedPos.getBlockEntity().createNbtWithId()) : false;
         }
      } else {
         return false;
      }
   }

   public boolean check(ItemStack stack, Registry blockRegistry, CachedBlockPosition pos) {
      if (canUseCache(pos, this.cachedPos, this.nbtAware)) {
         return this.lastResult;
      } else {
         this.cachedPos = pos;
         this.nbtAware = false;
         NbtCompound lv = stack.getNbt();
         if (lv != null && lv.contains(this.key, NbtElement.LIST_TYPE)) {
            NbtList lv2 = lv.getList(this.key, NbtElement.STRING_TYPE);

            for(int i = 0; i < lv2.size(); ++i) {
               String string = lv2.getString(i);

               try {
                  BlockPredicateArgumentType.BlockPredicate lv3 = BlockPredicateArgumentType.parse(blockRegistry.getReadOnlyWrapper(), new StringReader(string));
                  this.nbtAware |= lv3.hasNbt();
                  if (lv3.test(pos)) {
                     this.lastResult = true;
                     return true;
                  }
               } catch (CommandSyntaxException var9) {
               }
            }
         }

         this.lastResult = false;
         return false;
      }
   }
}
