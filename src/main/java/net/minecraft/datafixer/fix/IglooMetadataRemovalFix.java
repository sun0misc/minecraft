package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.datafixer.TypeReferences;

public class IglooMetadataRemovalFix extends DataFix {
   public IglooMetadataRemovalFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   protected TypeRewriteRule makeRule() {
      Type type = this.getInputSchema().getType(TypeReferences.STRUCTURE_FEATURE);
      return this.fixTypeEverywhereTyped("IglooMetadataRemovalFix", type, (typed) -> {
         return typed.update(DSL.remainderFinder(), IglooMetadataRemovalFix::removeMetadata);
      });
   }

   private static Dynamic removeMetadata(Dynamic dynamic) {
      boolean bl = (Boolean)dynamic.get("Children").asStreamOpt().map((stream) -> {
         return stream.allMatch(IglooMetadataRemovalFix::isIgloo);
      }).result().orElse(false);
      return bl ? dynamic.set("id", dynamic.createString("Igloo")).remove("Children") : dynamic.update("Children", IglooMetadataRemovalFix::removeIgloos);
   }

   private static Dynamic removeIgloos(Dynamic dynamic) {
      DataResult var10000 = dynamic.asStreamOpt().map((stream) -> {
         return stream.filter((dynamic) -> {
            return !isIgloo(dynamic);
         });
      });
      Objects.requireNonNull(dynamic);
      return (Dynamic)var10000.map(dynamic::createList).result().orElse(dynamic);
   }

   private static boolean isIgloo(Dynamic dynamic) {
      return dynamic.get("id").asString("").equals("Iglu");
   }
}
