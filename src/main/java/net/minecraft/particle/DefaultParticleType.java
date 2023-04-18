package net.minecraft.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;

public class DefaultParticleType extends ParticleType implements ParticleEffect {
   private static final ParticleEffect.Factory PARAMETER_FACTORY = new ParticleEffect.Factory() {
      public DefaultParticleType read(ParticleType arg, StringReader stringReader) {
         return (DefaultParticleType)arg;
      }

      public DefaultParticleType read(ParticleType arg, PacketByteBuf arg2) {
         return (DefaultParticleType)arg;
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
   private final Codec codec = Codec.unit(this::getType);

   protected DefaultParticleType(boolean alwaysShow) {
      super(alwaysShow, PARAMETER_FACTORY);
   }

   public DefaultParticleType getType() {
      return this;
   }

   public Codec getCodec() {
      return this.codec;
   }

   public void write(PacketByteBuf buf) {
   }

   public String asString() {
      return Registries.PARTICLE_TYPE.getId(this).toString();
   }

   // $FF: synthetic method
   public ParticleType getType() {
      return this.getType();
   }
}
