package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public class EntityRidingToPassengerFix extends DataFix {
   public EntityRidingToPassengerFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   public TypeRewriteRule makeRule() {
      Schema schema = this.getInputSchema();
      Schema schema2 = this.getOutputSchema();
      Type type = schema.getTypeRaw(TypeReferences.ENTITY_TREE);
      Type type2 = schema2.getTypeRaw(TypeReferences.ENTITY_TREE);
      Type type3 = schema.getTypeRaw(TypeReferences.ENTITY);
      return this.fixEntityTree(schema, schema2, type, type2, type3);
   }

   private TypeRewriteRule fixEntityTree(Schema inputSchema, Schema outputSchema, Type inputEntityTreeType, Type outputEntityTreeType, Type inputEntityType) {
      Type type4 = DSL.named(TypeReferences.ENTITY_TREE.typeName(), DSL.and(DSL.optional(DSL.field("Riding", inputEntityTreeType)), inputEntityType));
      Type type5 = DSL.named(TypeReferences.ENTITY_TREE.typeName(), DSL.and(DSL.optional(DSL.field("Passengers", DSL.list(outputEntityTreeType))), inputEntityType));
      Type type6 = inputSchema.getType(TypeReferences.ENTITY_TREE);
      Type type7 = outputSchema.getType(TypeReferences.ENTITY_TREE);
      if (!Objects.equals(type6, type4)) {
         throw new IllegalStateException("Old entity type is not what was expected.");
      } else if (!type7.equals(type5, true, true)) {
         throw new IllegalStateException("New entity type is not what was expected.");
      } else {
         OpticFinder opticFinder = DSL.typeFinder(type4);
         OpticFinder opticFinder2 = DSL.typeFinder(type5);
         OpticFinder opticFinder3 = DSL.typeFinder(outputEntityTreeType);
         Type type8 = inputSchema.getType(TypeReferences.PLAYER);
         Type type9 = outputSchema.getType(TypeReferences.PLAYER);
         return TypeRewriteRule.seq(this.fixTypeEverywhere("EntityRidingToPassengerFix", type4, type5, (dynamicOps) -> {
            return (pair) -> {
               Optional optional = Optional.empty();
               Pair pair2 = pair;

               while(true) {
                  Either either = (Either)DataFixUtils.orElse(optional.map((pairx) -> {
                     Typed typed = (Typed)outputEntityTreeType.pointTyped(dynamicOps).orElseThrow(() -> {
                        return new IllegalStateException("Could not create new entity tree");
                     });
                     Object object = typed.set(opticFinder2, pairx).getOptional(opticFinder3).orElseThrow(() -> {
                        return new IllegalStateException("Should always have an entity tree here");
                     });
                     return Either.left(ImmutableList.of(object));
                  }), Either.right(DSL.unit()));
                  optional = Optional.of(Pair.of(TypeReferences.ENTITY_TREE.typeName(), Pair.of(either, ((Pair)pair2.getSecond()).getSecond())));
                  Optional optional2 = ((Either)((Pair)pair2.getSecond()).getFirst()).left();
                  if (!optional2.isPresent()) {
                     return (Pair)optional.orElseThrow(() -> {
                        return new IllegalStateException("Should always have an entity tree here");
                     });
                  }

                  pair2 = (Pair)(new Typed(inputEntityTreeType, dynamicOps, optional2.get())).getOptional(opticFinder).orElseThrow(() -> {
                     return new IllegalStateException("Should always have an entity here");
                  });
               }
            };
         }), this.writeAndRead("player RootVehicle injecter", type8, type9));
      }
   }
}
