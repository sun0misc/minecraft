package net.minecraft.loot.provider.score;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.JsonSerializing;

public class LootScoreProviderTypes {
   public static final LootScoreProviderType FIXED = register("fixed", new FixedLootScoreProvider.Serializer());
   public static final LootScoreProviderType CONTEXT = register("context", new ContextLootScoreProvider.Serializer());

   private static LootScoreProviderType register(String id, JsonSerializer jsonSerializer) {
      return (LootScoreProviderType)Registry.register(Registries.LOOT_SCORE_PROVIDER_TYPE, (Identifier)(new Identifier(id)), new LootScoreProviderType(jsonSerializer));
   }

   public static Object createGsonSerializer() {
      return JsonSerializing.createSerializerBuilder(Registries.LOOT_SCORE_PROVIDER_TYPE, "provider", "type", LootScoreProvider::getType).elementSerializer(CONTEXT, new ContextLootScoreProvider.CustomSerializer()).build();
   }
}
