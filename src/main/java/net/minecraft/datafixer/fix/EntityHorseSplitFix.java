package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.datafixer.TypeReferences;

public class EntityHorseSplitFix extends EntityTransformFix {
   public EntityHorseSplitFix(Schema schema, boolean bl) {
      super("EntityHorseSplitFix", schema, bl);
   }

   protected Pair transform(String choice, Typed typed) {
      Dynamic dynamic = (Dynamic)typed.get(DSL.remainderFinder());
      if (Objects.equals("EntityHorse", choice)) {
         int i = dynamic.get("Type").asInt(0);
         String string2;
         switch (i) {
            case 0:
            default:
               string2 = "Horse";
               break;
            case 1:
               string2 = "Donkey";
               break;
            case 2:
               string2 = "Mule";
               break;
            case 3:
               string2 = "ZombieHorse";
               break;
            case 4:
               string2 = "SkeletonHorse";
         }

         dynamic.remove("Type");
         Type type = (Type)this.getOutputSchema().findChoiceType(TypeReferences.ENTITY).types().get(string2);
         DataResult var10001 = typed.write();
         Objects.requireNonNull(type);
         return Pair.of(string2, (Typed)((Pair)var10001.flatMap(type::readTyped).result().orElseThrow(() -> {
            return new IllegalStateException("Could not parse the new horse");
         })).getFirst());
      } else {
         return Pair.of(choice, typed);
      }
   }
}
