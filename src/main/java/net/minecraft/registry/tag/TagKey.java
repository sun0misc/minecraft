package net.minecraft.registry.tag;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public record TagKey(RegistryKey registry, Identifier id) {
   private static final Interner INTERNER = Interners.newWeakInterner();

   /** @deprecated */
   @Deprecated
   public TagKey(RegistryKey arg, Identifier arg2) {
      this.registry = arg;
      this.id = arg2;
   }

   public static Codec unprefixedCodec(RegistryKey registry) {
      return Identifier.CODEC.xmap((id) -> {
         return of(registry, id);
      }, TagKey::id);
   }

   public static Codec codec(RegistryKey registry) {
      return Codec.STRING.comapFlatMap((string) -> {
         return string.startsWith("#") ? Identifier.validate(string.substring(1)).map((id) -> {
            return of(registry, id);
         }) : DataResult.error(() -> {
            return "Not a tag id";
         });
      }, (string) -> {
         return "#" + string.id;
      });
   }

   public static TagKey of(RegistryKey registry, Identifier id) {
      return (TagKey)INTERNER.intern(new TagKey(registry, id));
   }

   public boolean isOf(RegistryKey registryRef) {
      return this.registry == registryRef;
   }

   public Optional tryCast(RegistryKey registryRef) {
      return this.isOf(registryRef) ? Optional.of(this) : Optional.empty();
   }

   public String toString() {
      Identifier var10000 = this.registry.getValue();
      return "TagKey[" + var10000 + " / " + this.id + "]";
   }

   public RegistryKey registry() {
      return this.registry;
   }

   public Identifier id() {
      return this.id;
   }
}
