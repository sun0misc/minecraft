package net.minecraft.datafixer.fix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PointOfInterestRemoveFix extends PointOfInterestFix {
   private final Predicate keepPredicate;

   public PointOfInterestRemoveFix(Schema schema, String name, Predicate removePredicate) {
      super(schema, name);
      this.keepPredicate = removePredicate.negate();
   }

   protected Stream update(Stream dynamics) {
      return dynamics.filter(this::shouldKeepRecord);
   }

   private boolean shouldKeepRecord(Dynamic dynamic) {
      return dynamic.get("type").asString().result().filter(this.keepPredicate).isPresent();
   }
}
