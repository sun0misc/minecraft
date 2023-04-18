package net.minecraft.client.render.model.json;

import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.state.StateManager;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface MultipartModelSelector {
   MultipartModelSelector TRUE = (stateFactory) -> {
      return (state) -> {
         return true;
      };
   };
   MultipartModelSelector FALSE = (stateFactory) -> {
      return (state) -> {
         return false;
      };
   };

   Predicate getPredicate(StateManager stateFactory);
}
