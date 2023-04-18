package net.minecraft.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class DustParticleEffect extends AbstractDustParticleEffect {
   public static final Vector3f RED = Vec3d.unpackRgb(16711680).toVector3f();
   public static final DustParticleEffect DEFAULT;
   public static final Codec CODEC;
   public static final ParticleEffect.Factory PARAMETERS_FACTORY;

   public DustParticleEffect(Vector3f vector3f, float f) {
      super(vector3f, f);
   }

   public ParticleType getType() {
      return ParticleTypes.DUST;
   }

   static {
      DEFAULT = new DustParticleEffect(RED, 1.0F);
      CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Codecs.VECTOR_3F.fieldOf("color").forGetter((effect) -> {
            return effect.color;
         }), Codec.FLOAT.fieldOf("scale").forGetter((effect) -> {
            return effect.scale;
         })).apply(instance, DustParticleEffect::new);
      });
      PARAMETERS_FACTORY = new ParticleEffect.Factory() {
         public DustParticleEffect read(ParticleType arg, StringReader stringReader) throws CommandSyntaxException {
            Vector3f vector3f = AbstractDustParticleEffect.readColor(stringReader);
            stringReader.expect(' ');
            float f = stringReader.readFloat();
            return new DustParticleEffect(vector3f, f);
         }

         public DustParticleEffect read(ParticleType arg, PacketByteBuf arg2) {
            return new DustParticleEffect(AbstractDustParticleEffect.readColor(arg2), arg2.readFloat());
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
   }
}
