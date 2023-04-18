package net.minecraft.item;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public interface Instruments {
   int GOAT_HORN_RANGE = 256;
   int GOAT_HORN_USE_DURATION = 140;
   RegistryKey PONDER_GOAT_HORN = of("ponder_goat_horn");
   RegistryKey SING_GOAT_HORN = of("sing_goat_horn");
   RegistryKey SEEK_GOAT_HORN = of("seek_goat_horn");
   RegistryKey FEEL_GOAT_HORN = of("feel_goat_horn");
   RegistryKey ADMIRE_GOAT_HORN = of("admire_goat_horn");
   RegistryKey CALL_GOAT_HORN = of("call_goat_horn");
   RegistryKey YEARN_GOAT_HORN = of("yearn_goat_horn");
   RegistryKey DREAM_GOAT_HORN = of("dream_goat_horn");

   private static RegistryKey of(String id) {
      return RegistryKey.of(RegistryKeys.INSTRUMENT, new Identifier(id));
   }

   static Instrument registerAndGetDefault(Registry registry) {
      Registry.register(registry, (RegistryKey)PONDER_GOAT_HORN, new Instrument((RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(0), 140, 256.0F));
      Registry.register(registry, (RegistryKey)SING_GOAT_HORN, new Instrument((RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(1), 140, 256.0F));
      Registry.register(registry, (RegistryKey)SEEK_GOAT_HORN, new Instrument((RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(2), 140, 256.0F));
      Registry.register(registry, (RegistryKey)FEEL_GOAT_HORN, new Instrument((RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(3), 140, 256.0F));
      Registry.register(registry, (RegistryKey)ADMIRE_GOAT_HORN, new Instrument((RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(4), 140, 256.0F));
      Registry.register(registry, (RegistryKey)CALL_GOAT_HORN, new Instrument((RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(5), 140, 256.0F));
      Registry.register(registry, (RegistryKey)YEARN_GOAT_HORN, new Instrument((RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(6), 140, 256.0F));
      return (Instrument)Registry.register(registry, (RegistryKey)DREAM_GOAT_HORN, new Instrument((RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(7), 140, 256.0F));
   }
}
