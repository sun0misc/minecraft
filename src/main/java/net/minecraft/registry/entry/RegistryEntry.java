package net.minecraft.registry.entry;

import com.mojang.datafixers.util.Either;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface RegistryEntry {
   Object value();

   boolean hasKeyAndValue();

   boolean matchesId(Identifier id);

   boolean matchesKey(RegistryKey key);

   boolean matches(Predicate predicate);

   boolean isIn(TagKey tag);

   Stream streamTags();

   Either getKeyOrValue();

   Optional getKey();

   Type getType();

   boolean ownerEquals(RegistryEntryOwner owner);

   static RegistryEntry of(Object value) {
      return new Direct(value);
   }

   public static record Direct(Object value) implements RegistryEntry {
      public Direct(Object object) {
         this.value = object;
      }

      public boolean hasKeyAndValue() {
         return true;
      }

      public boolean matchesId(Identifier id) {
         return false;
      }

      public boolean matchesKey(RegistryKey key) {
         return false;
      }

      public boolean isIn(TagKey tag) {
         return false;
      }

      public boolean matches(Predicate predicate) {
         return false;
      }

      public Either getKeyOrValue() {
         return Either.right(this.value);
      }

      public Optional getKey() {
         return Optional.empty();
      }

      public Type getType() {
         return RegistryEntry.Type.DIRECT;
      }

      public String toString() {
         return "Direct{" + this.value + "}";
      }

      public boolean ownerEquals(RegistryEntryOwner owner) {
         return true;
      }

      public Stream streamTags() {
         return Stream.of();
      }

      public Object value() {
         return this.value;
      }
   }

   public static class Reference implements RegistryEntry {
      private final RegistryEntryOwner owner;
      private Set tags = Set.of();
      private final Type referenceType;
      @Nullable
      private RegistryKey registryKey;
      @Nullable
      private Object value;

      private Reference(Type referenceType, RegistryEntryOwner owner, @Nullable RegistryKey registryKey, @Nullable Object value) {
         this.owner = owner;
         this.referenceType = referenceType;
         this.registryKey = registryKey;
         this.value = value;
      }

      public static Reference standAlone(RegistryEntryOwner owner, RegistryKey registryKey) {
         return new Reference(RegistryEntry.Reference.Type.STAND_ALONE, owner, registryKey, (Object)null);
      }

      /** @deprecated */
      @Deprecated
      public static Reference intrusive(RegistryEntryOwner owner, @Nullable Object value) {
         return new Reference(RegistryEntry.Reference.Type.INTRUSIVE, owner, (RegistryKey)null, value);
      }

      public RegistryKey registryKey() {
         if (this.registryKey == null) {
            throw new IllegalStateException("Trying to access unbound value '" + this.value + "' from registry " + this.owner);
         } else {
            return this.registryKey;
         }
      }

      public Object value() {
         if (this.value == null) {
            throw new IllegalStateException("Trying to access unbound value '" + this.registryKey + "' from registry " + this.owner);
         } else {
            return this.value;
         }
      }

      public boolean matchesId(Identifier id) {
         return this.registryKey().getValue().equals(id);
      }

      public boolean matchesKey(RegistryKey key) {
         return this.registryKey() == key;
      }

      public boolean isIn(TagKey tag) {
         return this.tags.contains(tag);
      }

      public boolean matches(Predicate predicate) {
         return predicate.test(this.registryKey());
      }

      public boolean ownerEquals(RegistryEntryOwner owner) {
         return this.owner.ownerEquals(owner);
      }

      public Either getKeyOrValue() {
         return Either.left(this.registryKey());
      }

      public Optional getKey() {
         return Optional.of(this.registryKey());
      }

      public Type getType() {
         return RegistryEntry.Type.REFERENCE;
      }

      public boolean hasKeyAndValue() {
         return this.registryKey != null && this.value != null;
      }

      void setRegistryKey(RegistryKey registryKey) {
         if (this.registryKey != null && registryKey != this.registryKey) {
            throw new IllegalStateException("Can't change holder key: existing=" + this.registryKey + ", new=" + registryKey);
         } else {
            this.registryKey = registryKey;
         }
      }

      void setValue(Object value) {
         if (this.referenceType == RegistryEntry.Reference.Type.INTRUSIVE && this.value != value) {
            throw new IllegalStateException("Can't change holder " + this.registryKey + " value: existing=" + this.value + ", new=" + value);
         } else {
            this.value = value;
         }
      }

      void setTags(Collection tags) {
         this.tags = Set.copyOf(tags);
      }

      public Stream streamTags() {
         return this.tags.stream();
      }

      public String toString() {
         return "Reference{" + this.registryKey + "=" + this.value + "}";
      }

      static enum Type {
         STAND_ALONE,
         INTRUSIVE;

         // $FF: synthetic method
         private static Type[] method_40238() {
            return new Type[]{STAND_ALONE, INTRUSIVE};
         }
      }
   }

   public static enum Type {
      REFERENCE,
      DIRECT;

      // $FF: synthetic method
      private static Type[] method_40232() {
         return new Type[]{REFERENCE, DIRECT};
      }
   }
}
