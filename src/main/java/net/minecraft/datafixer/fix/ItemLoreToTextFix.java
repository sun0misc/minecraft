package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.text.Text;

public class ItemLoreToTextFix extends DataFix {
   public ItemLoreToTextFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   protected TypeRewriteRule makeRule() {
      Type type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
      OpticFinder opticFinder = type.findField("tag");
      return this.fixTypeEverywhereTyped("Item Lore componentize", type, (typed) -> {
         return typed.updateTyped(opticFinder, (typedx) -> {
            return typedx.update(DSL.remainderFinder(), (dynamic) -> {
               return dynamic.update("display", (dynamicx) -> {
                  return dynamicx.update("Lore", (dynamic) -> {
                     DataResult var10000 = dynamic.asStreamOpt().map(ItemLoreToTextFix::fixLoreNbt);
                     Objects.requireNonNull(dynamic);
                     return (Dynamic)DataFixUtils.orElse(var10000.map(dynamic::createList).result(), dynamic);
                  });
               });
            });
         });
      });
   }

   private static Stream fixLoreNbt(Stream nbt) {
      return nbt.map((dynamic) -> {
         DataResult var10000 = dynamic.asString().map(ItemLoreToTextFix::componentize);
         Objects.requireNonNull(dynamic);
         return (Dynamic)DataFixUtils.orElse(var10000.map(dynamic::createString).result(), dynamic);
      });
   }

   private static String componentize(String string) {
      return Text.Serializer.toJson(Text.literal(string));
   }
}
