package net.minecraft.client.util;

import com.google.common.annotations.VisibleForTesting;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ModelIdentifier extends Identifier {
   @VisibleForTesting
   static final char SEPARATOR = '#';
   private final String variant;

   private ModelIdentifier(String namespace, String path, String variant, @Nullable Identifier.ExtraData extraData) {
      super(namespace, path, extraData);
      this.variant = variant;
   }

   public ModelIdentifier(String namespace, String path, String variant) {
      super(namespace, path);
      this.variant = toLowerCase(variant);
   }

   public ModelIdentifier(Identifier id, String variant) {
      this(id.getNamespace(), id.getPath(), toLowerCase(variant), (Identifier.ExtraData)null);
   }

   public static ModelIdentifier ofVanilla(String path, String variant) {
      return new ModelIdentifier("minecraft", path, variant);
   }

   private static String toLowerCase(String string) {
      return string.toLowerCase(Locale.ROOT);
   }

   public String getVariant() {
      return this.variant;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object instanceof ModelIdentifier && super.equals(object)) {
         ModelIdentifier lv = (ModelIdentifier)object;
         return this.variant.equals(lv.variant);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return 31 * super.hashCode() + this.variant.hashCode();
   }

   public String toString() {
      String var10000 = super.toString();
      return var10000 + "#" + this.variant;
   }
}
