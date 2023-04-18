package net.minecraft.client.search;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ReloadableSearchProvider extends SearchProvider {
   static ReloadableSearchProvider empty() {
      return (text) -> {
         return List.of();
      };
   }

   default void reload() {
   }
}
