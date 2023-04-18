package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class ItemStackUuidFix extends AbstractUuidFix {
   public ItemStackUuidFix(Schema outputSchema) {
      super(outputSchema, TypeReferences.ITEM_STACK);
   }

   public TypeRewriteRule makeRule() {
      OpticFinder opticFinder = DSL.fieldFinder("id", DSL.named(TypeReferences.ITEM_NAME.typeName(), IdentifierNormalizingSchema.getIdentifierType()));
      return this.fixTypeEverywhereTyped("ItemStackUUIDFix", this.getInputSchema().getType(this.typeReference), (typed) -> {
         OpticFinder opticFinder2 = typed.getType().findField("tag");
         return typed.updateTyped(opticFinder2, (typed2) -> {
            return typed2.update(DSL.remainderFinder(), (dynamic) -> {
               dynamic = this.fixAttributeModifiers(dynamic);
               if ((Boolean)typed.getOptional(opticFinder).map((pair) -> {
                  return "minecraft:player_head".equals(pair.getSecond());
               }).orElse(false)) {
                  dynamic = this.fixSkullOwner(dynamic);
               }

               return dynamic;
            });
         });
      });
   }

   private Dynamic fixAttributeModifiers(Dynamic dynamic) {
      return dynamic.update("AttributeModifiers", (dynamic2) -> {
         return dynamic.createList(dynamic2.asStream().map((dynamicx) -> {
            return (Dynamic)updateRegularMostLeast(dynamicx, "UUID", "UUID").orElse(dynamicx);
         }));
      });
   }

   private Dynamic fixSkullOwner(Dynamic dynamic) {
      return dynamic.update("SkullOwner", (dynamicx) -> {
         return (Dynamic)updateStringUuid(dynamicx, "Id", "Id").orElse(dynamicx);
      });
   }
}
