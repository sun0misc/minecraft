package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;

public class MobSpawnerEntityIdentifiersFix extends DataFix {
   public MobSpawnerEntityIdentifiersFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   private Dynamic fixSpawner(Dynamic dynamic) {
      if (!"MobSpawner".equals(dynamic.get("id").asString(""))) {
         return dynamic;
      } else {
         Optional optional = dynamic.get("EntityId").asString().result();
         if (optional.isPresent()) {
            Dynamic dynamic2 = (Dynamic)DataFixUtils.orElse(dynamic.get("SpawnData").result(), dynamic.emptyMap());
            dynamic2 = dynamic2.set("id", dynamic2.createString(((String)optional.get()).isEmpty() ? "Pig" : (String)optional.get()));
            dynamic = dynamic.set("SpawnData", dynamic2);
            dynamic = dynamic.remove("EntityId");
         }

         Optional optional2 = dynamic.get("SpawnPotentials").asStreamOpt().result();
         if (optional2.isPresent()) {
            dynamic = dynamic.set("SpawnPotentials", dynamic.createList(((Stream)optional2.get()).map((dynamicx) -> {
               Optional optional = dynamicx.get("Type").asString().result();
               if (optional.isPresent()) {
                  Dynamic dynamic2 = ((Dynamic)DataFixUtils.orElse(dynamicx.get("Properties").result(), dynamicx.emptyMap())).set("id", dynamicx.createString((String)optional.get()));
                  return dynamicx.set("Entity", dynamic2).remove("Type").remove("Properties");
               } else {
                  return dynamicx;
               }
            })));
         }

         return dynamic;
      }
   }

   public TypeRewriteRule makeRule() {
      Type type = this.getOutputSchema().getType(TypeReferences.UNTAGGED_SPAWNER);
      return this.fixTypeEverywhereTyped("MobSpawnerEntityIdentifiersFix", this.getInputSchema().getType(TypeReferences.UNTAGGED_SPAWNER), type, (typed) -> {
         Dynamic dynamic = (Dynamic)typed.get(DSL.remainderFinder());
         dynamic = dynamic.set("id", dynamic.createString("MobSpawner"));
         DataResult dataResult = type.readTyped(this.fixSpawner(dynamic));
         return !dataResult.result().isPresent() ? typed : (Typed)((Pair)dataResult.result().get()).getFirst();
      });
   }
}
