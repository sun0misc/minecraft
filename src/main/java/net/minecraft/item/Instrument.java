package net.minecraft.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.dynamic.Codecs;

public record Instrument(RegistryEntry soundEvent, int useDuration, float range) {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(SoundEvent.ENTRY_CODEC.fieldOf("sound_event").forGetter(Instrument::soundEvent), Codecs.POSITIVE_INT.fieldOf("use_duration").forGetter(Instrument::useDuration), Codecs.POSITIVE_FLOAT.fieldOf("range").forGetter(Instrument::range)).apply(instance, Instrument::new);
   });

   public Instrument(RegistryEntry arg, int i, float f) {
      this.soundEvent = arg;
      this.useDuration = i;
      this.range = f;
   }

   public RegistryEntry soundEvent() {
      return this.soundEvent;
   }

   public int useDuration() {
      return this.useDuration;
   }

   public float range() {
      return this.range;
   }
}
