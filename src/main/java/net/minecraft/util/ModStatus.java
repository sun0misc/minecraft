package net.minecraft.util;

import java.util.function.Supplier;
import org.apache.commons.lang3.ObjectUtils;

public record ModStatus(Confidence confidence, String description) {
   public ModStatus(Confidence arg, String string) {
      this.confidence = arg;
      this.description = string;
   }

   public static ModStatus check(String vanillaBrand, Supplier brandSupplier, String environment, Class clazz) {
      String string3 = (String)brandSupplier.get();
      if (!vanillaBrand.equals(string3)) {
         return new ModStatus(ModStatus.Confidence.DEFINITELY, environment + " brand changed to '" + string3 + "'");
      } else {
         return clazz.getSigners() == null ? new ModStatus(ModStatus.Confidence.VERY_LIKELY, environment + " jar signature invalidated") : new ModStatus(ModStatus.Confidence.PROBABLY_NOT, environment + " jar signature and brand is untouched");
      }
   }

   public boolean isModded() {
      return this.confidence.modded;
   }

   public ModStatus combine(ModStatus brand) {
      return new ModStatus((Confidence)ObjectUtils.max(new Confidence[]{this.confidence, brand.confidence}), this.description + "; " + brand.description);
   }

   public String getMessage() {
      return this.confidence.description + " " + this.description;
   }

   public Confidence confidence() {
      return this.confidence;
   }

   public String description() {
      return this.description;
   }

   public static enum Confidence {
      PROBABLY_NOT("Probably not.", false),
      VERY_LIKELY("Very likely;", true),
      DEFINITELY("Definitely;", true);

      final String description;
      final boolean modded;

      private Confidence(String description, boolean modded) {
         this.description = description;
         this.modded = modded;
      }

      // $FF: synthetic method
      private static Confidence[] method_39033() {
         return new Confidence[]{PROBABLY_NOT, VERY_LIKELY, DEFINITELY};
      }
   }
}
