package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

public class ItemStackArgumentType implements ArgumentType {
   private static final Collection EXAMPLES = Arrays.asList("stick", "minecraft:stick", "stick{foo=bar}");
   private final RegistryWrapper registryWrapper;

   public ItemStackArgumentType(CommandRegistryAccess commandRegistryAccess) {
      this.registryWrapper = commandRegistryAccess.createWrapper(RegistryKeys.ITEM);
   }

   public static ItemStackArgumentType itemStack(CommandRegistryAccess commandRegistryAccess) {
      return new ItemStackArgumentType(commandRegistryAccess);
   }

   public ItemStackArgument parse(StringReader stringReader) throws CommandSyntaxException {
      ItemStringReader.ItemResult lv = ItemStringReader.item(this.registryWrapper, stringReader);
      return new ItemStackArgument(lv.item(), lv.nbt());
   }

   public static ItemStackArgument getItemStackArgument(CommandContext context, String name) {
      return (ItemStackArgument)context.getArgument(name, ItemStackArgument.class);
   }

   public CompletableFuture listSuggestions(CommandContext context, SuggestionsBuilder builder) {
      return ItemStringReader.getSuggestions(this.registryWrapper, builder, false);
   }

   public Collection getExamples() {
      return EXAMPLES;
   }

   // $FF: synthetic method
   public Object parse(StringReader reader) throws CommandSyntaxException {
      return this.parse(reader);
   }
}
