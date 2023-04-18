package net.minecraft.registry.entry;

import com.mojang.datafixers.util.Either;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public interface RegistryEntryList extends Iterable {
   Stream stream();

   int size();

   Either getStorage();

   Optional getRandom(Random random);

   RegistryEntry get(int index);

   boolean contains(RegistryEntry entry);

   boolean ownerEquals(RegistryEntryOwner owner);

   Optional getTagKey();

   /** @deprecated */
   @Deprecated
   @VisibleForTesting
   static Named of(RegistryEntryOwner owner, TagKey tagKey) {
      return new Named(owner, tagKey);
   }

   @SafeVarargs
   static Direct of(RegistryEntry... entries) {
      return new Direct(List.of(entries));
   }

   static Direct of(List entries) {
      return new Direct(List.copyOf(entries));
   }

   @SafeVarargs
   static Direct of(Function mapper, Object... values) {
      return of(Stream.of(values).map(mapper).toList());
   }

   static Direct of(Function mapper, List values) {
      return of(values.stream().map(mapper).toList());
   }

   public static class Named extends ListBacked {
      private final RegistryEntryOwner owner;
      private final TagKey tag;
      private List entries = List.of();

      Named(RegistryEntryOwner owner, TagKey tag) {
         this.owner = owner;
         this.tag = tag;
      }

      void copyOf(List entries) {
         this.entries = List.copyOf(entries);
      }

      public TagKey getTag() {
         return this.tag;
      }

      protected List getEntries() {
         return this.entries;
      }

      public Either getStorage() {
         return Either.left(this.tag);
      }

      public Optional getTagKey() {
         return Optional.of(this.tag);
      }

      public boolean contains(RegistryEntry entry) {
         return entry.isIn(this.tag);
      }

      public String toString() {
         return "NamedSet(" + this.tag + ")[" + this.entries + "]";
      }

      public boolean ownerEquals(RegistryEntryOwner owner) {
         return this.owner.ownerEquals(owner);
      }
   }

   public static class Direct extends ListBacked {
      private final List entries;
      private @Nullable Set entrySet;

      Direct(List entries) {
         this.entries = entries;
      }

      protected List getEntries() {
         return this.entries;
      }

      public Either getStorage() {
         return Either.right(this.entries);
      }

      public Optional getTagKey() {
         return Optional.empty();
      }

      public boolean contains(RegistryEntry entry) {
         if (this.entrySet == null) {
            this.entrySet = Set.copyOf(this.entries);
         }

         return this.entrySet.contains(entry);
      }

      public String toString() {
         return "DirectSet[" + this.entries + "]";
      }
   }

   public abstract static class ListBacked implements RegistryEntryList {
      protected abstract List getEntries();

      public int size() {
         return this.getEntries().size();
      }

      public Spliterator spliterator() {
         return this.getEntries().spliterator();
      }

      public Iterator iterator() {
         return this.getEntries().iterator();
      }

      public Stream stream() {
         return this.getEntries().stream();
      }

      public Optional getRandom(Random random) {
         return Util.getRandomOrEmpty(this.getEntries(), random);
      }

      public RegistryEntry get(int index) {
         return (RegistryEntry)this.getEntries().get(index);
      }

      public boolean ownerEquals(RegistryEntryOwner owner) {
         return true;
      }
   }
}
