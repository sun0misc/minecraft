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
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import org.apache.commons.lang3.StringUtils;

public class ItemWrittenBookPagesStrictJsonFix extends DataFix {
   public ItemWrittenBookPagesStrictJsonFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   public Dynamic fixBookPages(Dynamic dynamic) {
      return dynamic.update("pages", (dynamic2) -> {
         DataResult var10000 = dynamic2.asStreamOpt().map((stream) -> {
            return stream.map((dynamic) -> {
               if (!dynamic.asString().result().isPresent()) {
                  return dynamic;
               } else {
                  String string = dynamic.asString("");
                  Text lv = null;
                  if (!"null".equals(string) && !StringUtils.isEmpty(string)) {
                     if (string.charAt(0) == '"' && string.charAt(string.length() - 1) == '"' || string.charAt(0) == '{' && string.charAt(string.length() - 1) == '}') {
                        try {
                           lv = (Text)JsonHelper.deserializeNullable(BlockEntitySignTextStrictJsonFix.GSON, string, Text.class, true);
                           if (lv == null) {
                              lv = ScreenTexts.EMPTY;
                           }
                        } catch (Exception var6) {
                        }

                        if (lv == null) {
                           try {
                              lv = Text.Serializer.fromJson(string);
                           } catch (Exception var5) {
                           }
                        }

                        if (lv == null) {
                           try {
                              lv = Text.Serializer.fromLenientJson(string);
                           } catch (Exception var4) {
                           }
                        }

                        if (lv == null) {
                           lv = Text.literal(string);
                        }
                     } else {
                        lv = Text.literal(string);
                     }
                  } else {
                     lv = ScreenTexts.EMPTY;
                  }

                  return dynamic.createString(Text.Serializer.toJson((Text)lv));
               }
            });
         });
         Objects.requireNonNull(dynamic);
         return (Dynamic)DataFixUtils.orElse(var10000.map(dynamic::createList).result(), dynamic.emptyList());
      });
   }

   public TypeRewriteRule makeRule() {
      Type type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
      OpticFinder opticFinder = type.findField("tag");
      return this.fixTypeEverywhereTyped("ItemWrittenBookPagesStrictJsonFix", type, (typed) -> {
         return typed.updateTyped(opticFinder, (typedx) -> {
            return typedx.update(DSL.remainderFinder(), this::fixBookPages);
         });
      });
   }
}
