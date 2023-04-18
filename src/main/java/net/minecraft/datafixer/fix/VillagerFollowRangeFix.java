package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class VillagerFollowRangeFix extends ChoiceFix {
   private static final double OLD_RANGE = 16.0;
   private static final double NEW_RANGE = 48.0;

   public VillagerFollowRangeFix(Schema schema) {
      super(schema, false, "Villager Follow Range Fix", TypeReferences.ENTITY, "minecraft:villager");
   }

   protected Typed transform(Typed inputType) {
      return inputType.update(DSL.remainderFinder(), VillagerFollowRangeFix::fix);
   }

   private static Dynamic fix(Dynamic dynamic) {
      return dynamic.update("Attributes", (dynamic2) -> {
         return dynamic.createList(dynamic2.asStream().map((dynamicx) -> {
            return dynamicx.get("Name").asString("").equals("generic.follow_range") && dynamicx.get("Base").asDouble(0.0) == 16.0 ? dynamicx.set("Base", dynamicx.createDouble(48.0)) : dynamicx;
         }));
      });
   }
}
