package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class StatusEffectDurationFix extends DataFix {
   private static final Set POTION_ITEM_IDS = Set.of("minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion", "minecraft:tipped_arrow");

   public StatusEffectDurationFix(Schema outputSchema) {
      super(outputSchema, false);
   }

   protected TypeRewriteRule makeRule() {
      Schema schema = this.getInputSchema();
      Type type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
      OpticFinder opticFinder = DSL.fieldFinder("id", DSL.named(TypeReferences.ITEM_NAME.typeName(), IdentifierNormalizingSchema.getIdentifierType()));
      OpticFinder opticFinder2 = type.findField("tag");
      return TypeRewriteRule.seq(this.fixTypeEverywhereTyped("EffectDurationEntity", schema.getType(TypeReferences.ENTITY), (typed) -> {
         return typed.update(DSL.remainderFinder(), this::fixEntityStatusEffects);
      }), new TypeRewriteRule[]{this.fixTypeEverywhereTyped("EffectDurationPlayer", schema.getType(TypeReferences.PLAYER), (typed) -> {
         return typed.update(DSL.remainderFinder(), this::fixEntityStatusEffects);
      }), this.fixTypeEverywhereTyped("EffectDurationItem", type, (typed) -> {
         Optional optional = typed.getOptional(opticFinder);
         Set var10001 = POTION_ITEM_IDS;
         Objects.requireNonNull(var10001);
         if (optional.filter(var10001::contains).isPresent()) {
            Optional optional2 = typed.getOptionalTyped(opticFinder2);
            if (optional2.isPresent()) {
               Dynamic dynamic = (Dynamic)((Typed)optional2.get()).get(DSL.remainderFinder());
               Typed typed2 = ((Typed)optional2.get()).set(DSL.remainderFinder(), dynamic.update("CustomPotionEffects", this::fixPotionEffects));
               return typed.set(opticFinder2, typed2);
            }
         }

         return typed;
      })});
   }

   private Dynamic fixPotionEffect(Dynamic dynamic) {
      return dynamic.update("FactorCalculationData", (dynamic2) -> {
         int i = dynamic2.get("effect_changed_timestamp").asInt(-1);
         dynamic2 = dynamic2.remove("effect_changed_timestamp");
         int j = dynamic.get("Duration").asInt(-1);
         int k = i - j;
         return dynamic2.set("ticks_active", dynamic2.createInt(k));
      });
   }

   private Dynamic fixPotionEffects(Dynamic dynamic) {
      return dynamic.createList(dynamic.asStream().map(this::fixPotionEffect));
   }

   private Dynamic fixEntityStatusEffects(Dynamic dynamic) {
      dynamic = dynamic.update("Effects", this::fixPotionEffects);
      dynamic = dynamic.update("ActiveEffects", this::fixPotionEffects);
      dynamic = dynamic.update("CustomPotionEffects", this::fixPotionEffects);
      return dynamic;
   }
}
