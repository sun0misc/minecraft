package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class OptionsProgrammerArtFix extends DataFix {
   public OptionsProgrammerArtFix(Schema schema) {
      super(schema, false);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("OptionsProgrammerArtFix", this.getInputSchema().getType(TypeReferences.OPTIONS), (typed) -> {
         return typed.update(DSL.remainderFinder(), (options) -> {
            return options.update("resourcePacks", this::replaceTypo).update("incompatibleResourcePacks", this::replaceTypo);
         });
      });
   }

   private Dynamic replaceTypo(Dynamic option) {
      return (Dynamic)option.asString().result().map((value) -> {
         return option.createString(value.replace("\"programer_art\"", "\"programmer_art\""));
      }).orElse(option);
   }
}
