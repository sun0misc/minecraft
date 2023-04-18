package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.advancement.Advancement;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class IdentifierArgumentType implements ArgumentType {
   private static final Collection EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
   private static final DynamicCommandExceptionType UNKNOWN_ADVANCEMENT_EXCEPTION = new DynamicCommandExceptionType((id) -> {
      return Text.translatable("advancement.advancementNotFound", id);
   });
   private static final DynamicCommandExceptionType UNKNOWN_RECIPE_EXCEPTION = new DynamicCommandExceptionType((id) -> {
      return Text.translatable("recipe.notFound", id);
   });
   private static final DynamicCommandExceptionType UNKNOWN_PREDICATE_EXCEPTION = new DynamicCommandExceptionType((id) -> {
      return Text.translatable("predicate.unknown", id);
   });
   private static final DynamicCommandExceptionType UNKNOWN_ITEM_MODIFIER_EXCEPTION = new DynamicCommandExceptionType((id) -> {
      return Text.translatable("item_modifier.unknown", id);
   });

   public static IdentifierArgumentType identifier() {
      return new IdentifierArgumentType();
   }

   public static Advancement getAdvancementArgument(CommandContext context, String argumentName) throws CommandSyntaxException {
      Identifier lv = getIdentifier(context, argumentName);
      Advancement lv2 = ((ServerCommandSource)context.getSource()).getServer().getAdvancementLoader().get(lv);
      if (lv2 == null) {
         throw UNKNOWN_ADVANCEMENT_EXCEPTION.create(lv);
      } else {
         return lv2;
      }
   }

   public static Recipe getRecipeArgument(CommandContext context, String argumentName) throws CommandSyntaxException {
      RecipeManager lv = ((ServerCommandSource)context.getSource()).getServer().getRecipeManager();
      Identifier lv2 = getIdentifier(context, argumentName);
      return (Recipe)lv.get(lv2).orElseThrow(() -> {
         return UNKNOWN_RECIPE_EXCEPTION.create(lv2);
      });
   }

   public static LootCondition getPredicateArgument(CommandContext context, String argumentName) throws CommandSyntaxException {
      Identifier lv = getIdentifier(context, argumentName);
      LootManager lv2 = ((ServerCommandSource)context.getSource()).getServer().getLootManager();
      LootCondition lv3 = (LootCondition)lv2.getElement(LootDataType.PREDICATES, lv);
      if (lv3 == null) {
         throw UNKNOWN_PREDICATE_EXCEPTION.create(lv);
      } else {
         return lv3;
      }
   }

   public static LootFunction getItemModifierArgument(CommandContext context, String argumentName) throws CommandSyntaxException {
      Identifier lv = getIdentifier(context, argumentName);
      LootManager lv2 = ((ServerCommandSource)context.getSource()).getServer().getLootManager();
      LootFunction lv3 = (LootFunction)lv2.getElement(LootDataType.ITEM_MODIFIERS, lv);
      if (lv3 == null) {
         throw UNKNOWN_ITEM_MODIFIER_EXCEPTION.create(lv);
      } else {
         return lv3;
      }
   }

   public static Identifier getIdentifier(CommandContext context, String name) {
      return (Identifier)context.getArgument(name, Identifier.class);
   }

   public Identifier parse(StringReader stringReader) throws CommandSyntaxException {
      return Identifier.fromCommandInput(stringReader);
   }

   public Collection getExamples() {
      return EXAMPLES;
   }

   // $FF: synthetic method
   public Object parse(StringReader reader) throws CommandSyntaxException {
      return this.parse(reader);
   }
}
