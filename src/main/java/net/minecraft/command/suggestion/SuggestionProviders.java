package net.minecraft.command.suggestion;

import com.google.common.collect.Maps;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class SuggestionProviders {
   private static final Map REGISTRY = Maps.newHashMap();
   private static final Identifier ASK_SERVER_NAME = new Identifier("ask_server");
   public static final SuggestionProvider ASK_SERVER;
   public static final SuggestionProvider ALL_RECIPES;
   public static final SuggestionProvider AVAILABLE_SOUNDS;
   public static final SuggestionProvider SUMMONABLE_ENTITIES;

   public static SuggestionProvider register(Identifier id, SuggestionProvider provider) {
      if (REGISTRY.containsKey(id)) {
         throw new IllegalArgumentException("A command suggestion provider is already registered with the name " + id);
      } else {
         REGISTRY.put(id, provider);
         return new LocalProvider(id, provider);
      }
   }

   public static SuggestionProvider byId(Identifier id) {
      return (SuggestionProvider)REGISTRY.getOrDefault(id, ASK_SERVER);
   }

   public static Identifier computeId(SuggestionProvider provider) {
      return provider instanceof LocalProvider ? ((LocalProvider)provider).id : ASK_SERVER_NAME;
   }

   public static SuggestionProvider getLocalProvider(SuggestionProvider provider) {
      return provider instanceof LocalProvider ? provider : ASK_SERVER;
   }

   static {
      ASK_SERVER = register(ASK_SERVER_NAME, (context, builder) -> {
         return ((CommandSource)context.getSource()).getCompletions(context);
      });
      ALL_RECIPES = register(new Identifier("all_recipes"), (context, builder) -> {
         return CommandSource.suggestIdentifiers(((CommandSource)context.getSource()).getRecipeIds(), builder);
      });
      AVAILABLE_SOUNDS = register(new Identifier("available_sounds"), (context, builder) -> {
         return CommandSource.suggestIdentifiers(((CommandSource)context.getSource()).getSoundIds(), builder);
      });
      SUMMONABLE_ENTITIES = register(new Identifier("summonable_entities"), (context, builder) -> {
         return CommandSource.suggestFromIdentifier(Registries.ENTITY_TYPE.stream().filter((arg) -> {
            return arg.isEnabled(((CommandSource)context.getSource()).getEnabledFeatures()) && arg.isSummonable();
         }), builder, EntityType::getId, (entityType) -> {
            return Text.translatable(Util.createTranslationKey("entity", EntityType.getId(entityType)));
         });
      });
   }

   protected static class LocalProvider implements SuggestionProvider {
      private final SuggestionProvider provider;
      final Identifier id;

      public LocalProvider(Identifier id, SuggestionProvider provider) {
         this.provider = provider;
         this.id = id;
      }

      public CompletableFuture getSuggestions(CommandContext context, SuggestionsBuilder builder) throws CommandSyntaxException {
         return this.provider.getSuggestions(context, builder);
      }
   }
}
