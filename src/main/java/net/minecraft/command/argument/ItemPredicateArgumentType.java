package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntryList;
import org.jetbrains.annotations.Nullable;

public class ItemPredicateArgumentType implements ArgumentType {
   private static final Collection EXAMPLES = Arrays.asList("stick", "minecraft:stick", "#stick", "#stick{foo=bar}");
   private final RegistryWrapper registryWrapper;

   public ItemPredicateArgumentType(CommandRegistryAccess commandRegistryAccess) {
      this.registryWrapper = commandRegistryAccess.createWrapper(RegistryKeys.ITEM);
   }

   public static ItemPredicateArgumentType itemPredicate(CommandRegistryAccess commandRegistryAccess) {
      return new ItemPredicateArgumentType(commandRegistryAccess);
   }

   public ItemStackPredicateArgument parse(StringReader stringReader) throws CommandSyntaxException {
      Either either = ItemStringReader.itemOrTag(this.registryWrapper, stringReader);
      return (ItemStackPredicateArgument)either.map((item) -> {
         return getItemStackPredicate((item2) -> {
            return item2 == item.item();
         }, item.nbt());
      }, (tag) -> {
         RegistryEntryList var10000 = tag.tag();
         Objects.requireNonNull(var10000);
         return getItemStackPredicate(var10000::contains, tag.nbt());
      });
   }

   public static Predicate getItemStackPredicate(CommandContext context, String name) {
      return (Predicate)context.getArgument(name, ItemStackPredicateArgument.class);
   }

   public CompletableFuture listSuggestions(CommandContext context, SuggestionsBuilder builder) {
      return ItemStringReader.getSuggestions(this.registryWrapper, builder, true);
   }

   public Collection getExamples() {
      return EXAMPLES;
   }

   private static ItemStackPredicateArgument getItemStackPredicate(Predicate predicate, @Nullable NbtCompound nbt) {
      return nbt != null ? (stack) -> {
         return stack.itemMatches(predicate) && NbtHelper.matches(nbt, stack.getNbt(), true);
      } : (stack) -> {
         return stack.itemMatches(predicate);
      };
   }

   // $FF: synthetic method
   public Object parse(StringReader reader) throws CommandSyntaxException {
      return this.parse(reader);
   }

   public interface ItemStackPredicateArgument extends Predicate {
   }
}
