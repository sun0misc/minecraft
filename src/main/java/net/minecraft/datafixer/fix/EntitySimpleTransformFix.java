package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;

public abstract class EntitySimpleTransformFix extends EntityTransformFix {
   public EntitySimpleTransformFix(String string, Schema schema, boolean bl) {
      super(string, schema, bl);
   }

   protected Pair transform(String choice, Typed typed) {
      Pair pair = this.transform(choice, (Dynamic)typed.getOrCreate(DSL.remainderFinder()));
      return Pair.of((String)pair.getFirst(), typed.set(DSL.remainderFinder(), (Dynamic)pair.getSecond()));
   }

   protected abstract Pair transform(String choice, Dynamic dynamic);
}
