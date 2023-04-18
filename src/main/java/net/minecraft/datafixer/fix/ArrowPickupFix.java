package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;
import net.minecraft.datafixer.TypeReferences;

public class ArrowPickupFix extends DataFix {
   public ArrowPickupFix(Schema outputSchema) {
      super(outputSchema, false);
   }

   protected TypeRewriteRule makeRule() {
      Schema schema = this.getInputSchema();
      return this.fixTypeEverywhereTyped("AbstractArrowPickupFix", schema.getType(TypeReferences.ENTITY), this::update);
   }

   private Typed update(Typed typed) {
      typed = this.updateEntity(typed, "minecraft:arrow", ArrowPickupFix::update);
      typed = this.updateEntity(typed, "minecraft:spectral_arrow", ArrowPickupFix::update);
      typed = this.updateEntity(typed, "minecraft:trident", ArrowPickupFix::update);
      return typed;
   }

   private static Dynamic update(Dynamic arrowData) {
      if (arrowData.get("pickup").result().isPresent()) {
         return arrowData;
      } else {
         boolean bl = arrowData.get("player").asBoolean(true);
         return arrowData.set("pickup", arrowData.createByte((byte)(bl ? 1 : 0))).remove("player");
      }
   }

   private Typed updateEntity(Typed typed, String choiceName, Function updater) {
      Type type = this.getInputSchema().getChoiceType(TypeReferences.ENTITY, choiceName);
      Type type2 = this.getOutputSchema().getChoiceType(TypeReferences.ENTITY, choiceName);
      return typed.updateTyped(DSL.namedChoice(choiceName, type), type2, (t) -> {
         return t.update(DSL.remainderFinder(), updater);
      });
   }
}
