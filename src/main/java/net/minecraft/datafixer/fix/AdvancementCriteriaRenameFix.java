package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.UnaryOperator;
import net.minecraft.datafixer.TypeReferences;

public class AdvancementCriteriaRenameFix extends DataFix {
   private final String description;
   private final String advancementId;
   private final UnaryOperator renamer;

   public AdvancementCriteriaRenameFix(Schema schema, String description, String advancementId, UnaryOperator renamer) {
      super(schema, false);
      this.description = description;
      this.advancementId = advancementId;
      this.renamer = renamer;
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(this.description, this.getInputSchema().getType(TypeReferences.ADVANCEMENTS), (typed) -> {
         return typed.update(DSL.remainderFinder(), this::update);
      });
   }

   private Dynamic update(Dynamic advancements) {
      return advancements.update(this.advancementId, (advancement) -> {
         return advancement.update("criteria", (criteria) -> {
            return criteria.updateMapValues((pair) -> {
               return pair.mapFirst((key) -> {
                  return (Dynamic)DataFixUtils.orElse(key.asString().map((keyString) -> {
                     return key.createString((String)this.renamer.apply(keyString));
                  }).result(), key);
               });
            });
         });
      });
   }
}
