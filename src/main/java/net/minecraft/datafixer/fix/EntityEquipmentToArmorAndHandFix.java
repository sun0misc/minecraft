package net.minecraft.datafixer.fix;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;

public class EntityEquipmentToArmorAndHandFix extends DataFix {
   public EntityEquipmentToArmorAndHandFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   public TypeRewriteRule makeRule() {
      return this.fixEquipment(this.getInputSchema().getTypeRaw(TypeReferences.ITEM_STACK));
   }

   private TypeRewriteRule fixEquipment(Type type) {
      Type type2 = DSL.and(DSL.optional(DSL.field("Equipment", DSL.list(type))), DSL.remainderType());
      Type type3 = DSL.and(DSL.optional(DSL.field("ArmorItems", DSL.list(type))), DSL.optional(DSL.field("HandItems", DSL.list(type))), DSL.remainderType());
      OpticFinder opticFinder = DSL.typeFinder(type2);
      OpticFinder opticFinder2 = DSL.fieldFinder("Equipment", DSL.list(type));
      return this.fixTypeEverywhereTyped("EntityEquipmentToArmorAndHandFix", this.getInputSchema().getType(TypeReferences.ENTITY), this.getOutputSchema().getType(TypeReferences.ENTITY), (typed) -> {
         Either either = Either.right(DSL.unit());
         Either either2 = Either.right(DSL.unit());
         Dynamic dynamic = (Dynamic)typed.getOrCreate(DSL.remainderFinder());
         Optional optional = typed.getOptional(opticFinder2);
         if (optional.isPresent()) {
            List list = (List)optional.get();
            Object object = ((Pair)type.read(dynamic.emptyMap()).result().orElseThrow(() -> {
               return new IllegalStateException("Could not parse newly created empty itemstack.");
            })).getFirst();
            if (!list.isEmpty()) {
               either = Either.left(Lists.newArrayList(new Object[]{list.get(0), object}));
            }

            if (list.size() > 1) {
               List list2 = Lists.newArrayList(new Object[]{object, object, object, object});

               for(int i = 1; i < Math.min(list.size(), 5); ++i) {
                  list2.set(i - 1, list.get(i));
               }

               either2 = Either.left(list2);
            }
         }

         Optional optional2 = dynamic.get("DropChances").asStreamOpt().result();
         if (optional2.isPresent()) {
            Iterator iterator = Stream.concat((Stream)optional2.get(), Stream.generate(() -> {
               return dynamic.createInt(0);
            })).iterator();
            float f = ((Dynamic)iterator.next()).asFloat(0.0F);
            Dynamic dynamic3;
            Stream var10001;
            if (!dynamic.get("HandDropChances").result().isPresent()) {
               var10001 = Stream.of(f, 0.0F);
               Objects.requireNonNull(dynamic);
               dynamic3 = dynamic.createList(var10001.map(dynamic::createFloat));
               dynamic = dynamic.set("HandDropChances", dynamic3);
            }

            if (!dynamic.get("ArmorDropChances").result().isPresent()) {
               var10001 = Stream.of(((Dynamic)iterator.next()).asFloat(0.0F), ((Dynamic)iterator.next()).asFloat(0.0F), ((Dynamic)iterator.next()).asFloat(0.0F), ((Dynamic)iterator.next()).asFloat(0.0F));
               Objects.requireNonNull(dynamic);
               dynamic3 = dynamic.createList(var10001.map(dynamic::createFloat));
               dynamic = dynamic.set("ArmorDropChances", dynamic3);
            }

            dynamic = dynamic.remove("DropChances");
         }

         return typed.set(opticFinder, type3, Pair.of(either, Pair.of(either2, dynamic)));
      });
   }
}
