package net.minecraft.registry;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleDefaultedRegistry extends SimpleRegistry implements DefaultedRegistry {
   private final Identifier defaultId;
   private RegistryEntry.Reference defaultEntry;

   public SimpleDefaultedRegistry(String defaultId, RegistryKey key, Lifecycle lifecycle, boolean intrusive) {
      super(key, lifecycle, intrusive);
      this.defaultId = new Identifier(defaultId);
   }

   public RegistryEntry.Reference set(int i, RegistryKey arg, Object object, Lifecycle lifecycle) {
      RegistryEntry.Reference lv = super.set(i, arg, object, lifecycle);
      if (this.defaultId.equals(arg.getValue())) {
         this.defaultEntry = lv;
      }

      return lv;
   }

   public int getRawId(@Nullable Object value) {
      int i = super.getRawId(value);
      return i == -1 ? super.getRawId(this.defaultEntry.value()) : i;
   }

   @NotNull
   public Identifier getId(Object value) {
      Identifier lv = super.getId(value);
      return lv == null ? this.defaultId : lv;
   }

   @NotNull
   public Object get(@Nullable Identifier id) {
      Object object = super.get(id);
      return object == null ? this.defaultEntry.value() : object;
   }

   public Optional getOrEmpty(@Nullable Identifier id) {
      return Optional.ofNullable(super.get(id));
   }

   @NotNull
   public Object get(int index) {
      Object object = super.get(index);
      return object == null ? this.defaultEntry.value() : object;
   }

   public Optional getRandom(Random random) {
      return super.getRandom(random).or(() -> {
         return Optional.of(this.defaultEntry);
      });
   }

   public Identifier getDefaultId() {
      return this.defaultId;
   }

   // $FF: synthetic method
   public RegistryEntry set(int rawId, RegistryKey key, Object value, Lifecycle lifecycle) {
      return this.set(rawId, key, value, lifecycle);
   }
}
