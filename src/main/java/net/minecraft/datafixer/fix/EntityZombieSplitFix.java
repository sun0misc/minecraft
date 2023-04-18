package net.minecraft.datafixer.fix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class EntityZombieSplitFix extends EntitySimpleTransformFix {
   public EntityZombieSplitFix(Schema schema, boolean bl) {
      super("EntityZombieSplitFix", schema, bl);
   }

   protected Pair transform(String choice, Dynamic dynamic) {
      if (Objects.equals("Zombie", choice)) {
         String string2 = "Zombie";
         int i = dynamic.get("ZombieType").asInt(0);
         switch (i) {
            case 0:
            default:
               break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
               string2 = "ZombieVillager";
               dynamic = dynamic.set("Profession", dynamic.createInt(i - 1));
               break;
            case 6:
               string2 = "Husk";
         }

         dynamic = dynamic.remove("ZombieType");
         return Pair.of(string2, dynamic);
      } else {
         return Pair.of(choice, dynamic);
      }
   }
}
