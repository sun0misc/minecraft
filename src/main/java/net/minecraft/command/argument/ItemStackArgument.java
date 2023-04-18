package net.minecraft.command.argument;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import java.util.function.Predicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class ItemStackArgument implements Predicate {
   private static final Dynamic2CommandExceptionType OVERSTACKED_EXCEPTION = new Dynamic2CommandExceptionType((item, maxCount) -> {
      return Text.translatable("arguments.item.overstacked", item, maxCount);
   });
   private final RegistryEntry item;
   @Nullable
   private final NbtCompound nbt;

   public ItemStackArgument(RegistryEntry item, @Nullable NbtCompound nbt) {
      this.item = item;
      this.nbt = nbt;
   }

   public Item getItem() {
      return (Item)this.item.value();
   }

   public boolean test(ItemStack arg) {
      return arg.itemMatches(this.item) && NbtHelper.matches(this.nbt, arg.getNbt(), true);
   }

   public ItemStack createStack(int amount, boolean checkOverstack) throws CommandSyntaxException {
      ItemStack lv = new ItemStack(this.item, amount);
      if (this.nbt != null) {
         lv.setNbt(this.nbt);
      }

      if (checkOverstack && amount > lv.getMaxCount()) {
         throw OVERSTACKED_EXCEPTION.create(this.getIdString(), lv.getMaxCount());
      } else {
         return lv;
      }
   }

   public String asString() {
      StringBuilder stringBuilder = new StringBuilder(this.getIdString());
      if (this.nbt != null) {
         stringBuilder.append(this.nbt);
      }

      return stringBuilder.toString();
   }

   private String getIdString() {
      return this.item.getKey().map(RegistryKey::getValue).orElseGet(() -> {
         return "unknown[" + this.item + "]";
      }).toString();
   }

   // $FF: synthetic method
   public boolean test(Object stack) {
      return this.test((ItemStack)stack);
   }
}
