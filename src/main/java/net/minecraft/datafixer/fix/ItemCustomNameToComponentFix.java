package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.text.Text;

public class ItemCustomNameToComponentFix extends DataFix {
   public ItemCustomNameToComponentFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   private Dynamic fixCustomName(Dynamic dynamic) {
      Optional optional = dynamic.get("display").result();
      if (optional.isPresent()) {
         Dynamic dynamic2 = (Dynamic)optional.get();
         Optional optional2 = dynamic2.get("Name").asString().result();
         if (optional2.isPresent()) {
            dynamic2 = dynamic2.set("Name", dynamic2.createString(Text.Serializer.toJson(Text.literal((String)optional2.get()))));
         } else {
            Optional optional3 = dynamic2.get("LocName").asString().result();
            if (optional3.isPresent()) {
               dynamic2 = dynamic2.set("Name", dynamic2.createString(Text.Serializer.toJson(Text.translatable((String)optional3.get()))));
               dynamic2 = dynamic2.remove("LocName");
            }
         }

         return dynamic.set("display", dynamic2);
      } else {
         return dynamic;
      }
   }

   public TypeRewriteRule makeRule() {
      Type type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
      OpticFinder opticFinder = type.findField("tag");
      return this.fixTypeEverywhereTyped("ItemCustomNameToComponentFix", type, (typed) -> {
         return typed.updateTyped(opticFinder, (typedx) -> {
            return typedx.update(DSL.remainderFinder(), this::fixCustomName);
         });
      });
   }
}
