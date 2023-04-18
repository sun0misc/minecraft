package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.function.Function;
import java.util.function.IntFunction;

public class EntityVariantTypeFix extends ChoiceFix {
   private final String variantKey;
   private final IntFunction variantIntToId;

   public EntityVariantTypeFix(Schema schema, String name, DSL.TypeReference typeReference, String entityId, String variantKey, IntFunction variantIntToId) {
      super(schema, false, name, typeReference, entityId);
      this.variantKey = variantKey;
      this.variantIntToId = variantIntToId;
   }

   private static Dynamic method_43072(Dynamic dynamic, String string, String string2, Function function) {
      return dynamic.map((object) -> {
         DynamicOps dynamicOps = dynamic.getOps();
         Function function2 = (objectx) -> {
            return ((Dynamic)function.apply(new Dynamic(dynamicOps, objectx))).getValue();
         };
         return dynamicOps.get(object, string).map((object2) -> {
            return dynamicOps.set(object, string2, function2.apply(object2));
         }).result().orElse(object);
      });
   }

   protected Typed transform(Typed inputType) {
      return inputType.update(DSL.remainderFinder(), (dynamic) -> {
         return method_43072(dynamic, this.variantKey, "variant", (dynamicx) -> {
            return (Dynamic)DataFixUtils.orElse(dynamicx.asNumber().map((number) -> {
               return dynamicx.createString((String)this.variantIntToId.apply(number.intValue()));
            }).result(), dynamicx);
         });
      });
   }
}
