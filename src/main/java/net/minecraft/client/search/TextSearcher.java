package net.minecraft.client.search;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface TextSearcher {
   static TextSearcher of() {
      return (text) -> {
         return List.of();
      };
   }

   static TextSearcher of(List values, Function textsGetter) {
      if (values.isEmpty()) {
         return of();
      } else {
         SuffixArray lv = new SuffixArray();
         Iterator var3 = values.iterator();

         while(var3.hasNext()) {
            Object object = var3.next();
            ((Stream)textsGetter.apply(object)).forEach((text) -> {
               lv.add(object, text.toLowerCase(Locale.ROOT));
            });
         }

         lv.build();
         Objects.requireNonNull(lv);
         return lv::findAll;
      }
   }

   List search(String text);
}
