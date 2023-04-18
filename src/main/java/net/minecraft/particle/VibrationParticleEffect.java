package net.minecraft.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.PositionSourceType;

public class VibrationParticleEffect implements ParticleEffect {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(PositionSource.CODEC.fieldOf("destination").forGetter((effect) -> {
         return effect.destination;
      }), Codec.INT.fieldOf("arrival_in_ticks").forGetter((effect) -> {
         return effect.arrivalInTicks;
      })).apply(instance, VibrationParticleEffect::new);
   });
   public static final ParticleEffect.Factory PARAMETERS_FACTORY = new ParticleEffect.Factory() {
      public VibrationParticleEffect read(ParticleType arg, StringReader stringReader) throws CommandSyntaxException {
         stringReader.expect(' ');
         float f = (float)stringReader.readDouble();
         stringReader.expect(' ');
         float g = (float)stringReader.readDouble();
         stringReader.expect(' ');
         float h = (float)stringReader.readDouble();
         stringReader.expect(' ');
         int i = stringReader.readInt();
         BlockPos lv = BlockPos.ofFloored((double)f, (double)g, (double)h);
         return new VibrationParticleEffect(new BlockPositionSource(lv), i);
      }

      public VibrationParticleEffect read(ParticleType arg, PacketByteBuf arg2) {
         PositionSource lv = PositionSourceType.read(arg2);
         int i = arg2.readVarInt();
         return new VibrationParticleEffect(lv, i);
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
   private final PositionSource destination;
   private final int arrivalInTicks;

   public VibrationParticleEffect(PositionSource destination, int arrivalInTicks) {
      this.destination = destination;
      this.arrivalInTicks = arrivalInTicks;
   }

   public void write(PacketByteBuf buf) {
      PositionSourceType.write(this.destination, buf);
      buf.writeVarInt(this.arrivalInTicks);
   }

   public String asString() {
      Vec3d lv = (Vec3d)this.destination.getPos((World)null).get();
      double d = lv.getX();
      double e = lv.getY();
      double f = lv.getZ();
      return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %d", Registries.PARTICLE_TYPE.getId(this.getType()), d, e, f, this.arrivalInTicks);
   }

   public ParticleType getType() {
      return ParticleTypes.VIBRATION;
   }

   public PositionSource getVibration() {
      return this.destination;
   }

   public int getArrivalInTicks() {
      return this.arrivalInTicks;
   }
}
