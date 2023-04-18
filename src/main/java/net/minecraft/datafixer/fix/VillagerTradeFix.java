package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class VillagerTradeFix extends ChoiceFix {
   public VillagerTradeFix(Schema schema, boolean bl) {
      super(schema, bl, "Villager trade fix", TypeReferences.ENTITY, "minecraft:villager");
   }

   protected Typed transform(Typed inputType) {
      OpticFinder opticFinder = inputType.getType().findField("Offers");
      OpticFinder opticFinder2 = opticFinder.type().findField("Recipes");
      Type type = opticFinder2.type();
      if (!(type instanceof List.ListType listType)) {
         throw new IllegalStateException("Recipes are expected to be a list.");
      } else {
         Type type2 = listType.getElement();
         OpticFinder opticFinder3 = DSL.typeFinder(type2);
         OpticFinder opticFinder4 = type2.findField("buy");
         OpticFinder opticFinder5 = type2.findField("buyB");
         OpticFinder opticFinder6 = type2.findField("sell");
         OpticFinder opticFinder7 = DSL.fieldFinder("id", DSL.named(TypeReferences.ITEM_NAME.typeName(), IdentifierNormalizingSchema.getIdentifierType()));
         Function function = (typed) -> {
            return this.fixPumpkinTrade(opticFinder7, typed);
         };
         return inputType.updateTyped(opticFinder, (typed) -> {
            return typed.updateTyped(opticFinder2, (typedx) -> {
               return typedx.updateTyped(opticFinder3, (typed) -> {
                  return typed.updateTyped(opticFinder4, function).updateTyped(opticFinder5, function).updateTyped(opticFinder6, function);
               });
            });
         });
      }
   }

   private Typed fixPumpkinTrade(OpticFinder opticFinder, Typed typed) {
      return typed.update(opticFinder, (pair) -> {
         return pair.mapSecond((string) -> {
            return Objects.equals(string, "minecraft:carved_pumpkin") ? "minecraft:pumpkin" : string;
         });
      });
   }
}
