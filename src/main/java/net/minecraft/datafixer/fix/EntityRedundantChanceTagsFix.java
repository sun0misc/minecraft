package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Codec;
import com.mojang.serialization.OptionalDynamic;
import java.util.Objects;
import net.minecraft.datafixer.TypeReferences;

public class EntityRedundantChanceTagsFix extends DataFix {
   private static final Codec field_25695;

   public EntityRedundantChanceTagsFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("EntityRedundantChanceTagsFix", this.getInputSchema().getType(TypeReferences.ENTITY), (typed) -> {
         return typed.update(DSL.remainderFinder(), (dynamic) -> {
            if (hasZeroDropChance(dynamic.get("HandDropChances"), 2)) {
               dynamic = dynamic.remove("HandDropChances");
            }

            if (hasZeroDropChance(dynamic.get("ArmorDropChances"), 4)) {
               dynamic = dynamic.remove("ArmorDropChances");
            }

            return dynamic;
         });
      });
   }

   private static boolean hasZeroDropChance(OptionalDynamic optionalDynamic, int i) {
      Codec var10001 = field_25695;
      Objects.requireNonNull(var10001);
      return (Boolean)optionalDynamic.flatMap(var10001::parse).map((list) -> {
         return list.size() == i && list.stream().allMatch((float_) -> {
            return float_ == 0.0F;
         });
      }).result().orElse(false);
   }

   static {
      field_25695 = Codec.FLOAT.listOf();
   }
}
