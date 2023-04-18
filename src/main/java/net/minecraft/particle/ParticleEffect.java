package net.minecraft.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.PacketByteBuf;

public interface ParticleEffect {
   ParticleType getType();

   void write(PacketByteBuf buf);

   String asString();

   /** @deprecated */
   @Deprecated
   public interface Factory {
      ParticleEffect read(ParticleType type, StringReader reader) throws CommandSyntaxException;

      ParticleEffect read(ParticleType type, PacketByteBuf buf);
   }
}
