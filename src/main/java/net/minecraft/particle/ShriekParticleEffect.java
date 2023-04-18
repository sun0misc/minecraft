package net.minecraft.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;

public class ShriekParticleEffect implements ParticleEffect {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.INT.fieldOf("delay").forGetter((particleEffect) -> {
         return particleEffect.delay;
      })).apply(instance, ShriekParticleEffect::new);
   });
   public static final ParticleEffect.Factory FACTORY = new ParticleEffect.Factory() {
      public ShriekParticleEffect read(ParticleType arg, StringReader stringReader) throws CommandSyntaxException {
         stringReader.expect(' ');
         int i = stringReader.readInt();
         return new ShriekParticleEffect(i);
      }

      public ShriekParticleEffect read(ParticleType arg, PacketByteBuf arg2) {
         return new ShriekParticleEffect(arg2.readVarInt());
      }

      // $FF: synthetic method
      public ParticleEffect read(ParticleType type, PacketByteBuf buf) {
         return this.read(type, buf);
      }

      // $FF: synthetic method
      public ParticleEffect read(ParticleType type, StringReader reader) throws CommandSyntaxException {
         return this.read(type, reader);
      }
   };
   private final int delay;

   public ShriekParticleEffect(int delay) {
      this.delay = delay;
   }

   public void write(PacketByteBuf buf) {
      buf.writeVarInt(this.delay);
   }

   public String asString() {
      return String.format(Locale.ROOT, "%s %d", Registries.PARTICLE_TYPE.getId(this.getType()), this.delay);
   }

   public ParticleType getType() {
      return ParticleTypes.SHRIEK;
   }

   public int getDelay() {
      return this.delay;
   }
}
