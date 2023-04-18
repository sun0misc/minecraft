package net.minecraft.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;

public record SculkChargeParticleEffect(float roll) implements ParticleEffect {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.FLOAT.fieldOf("roll").forGetter((particleEffect) -> {
         return particleEffect.roll;
      })).apply(instance, SculkChargeParticleEffect::new);
   });
   public static final ParticleEffect.Factory FACTORY = new ParticleEffect.Factory() {
      public SculkChargeParticleEffect read(ParticleType arg, StringReader stringReader) throws CommandSyntaxException {
         stringReader.expect(' ');
         float f = stringReader.readFloat();
         return new SculkChargeParticleEffect(f);
      }

      public SculkChargeParticleEffect read(ParticleType arg, PacketByteBuf arg2) {
         return new SculkChargeParticleEffect(arg2.readFloat());
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

   public SculkChargeParticleEffect(float f) {
      this.roll = f;
   }

   public ParticleType getType() {
      return ParticleTypes.SCULK_CHARGE;
   }

   public void write(PacketByteBuf buf) {
      buf.writeFloat(this.roll);
   }

   public String asString() {
      return String.format(Locale.ROOT, "%s %.2f", Registries.PARTICLE_TYPE.getId(this.getType()), this.roll);
   }

   public float roll() {
      return this.roll;
   }
}
