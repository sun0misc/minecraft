package net.minecraft.network.packet.s2c.play;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;

public class EntityStatusEffectS2CPacket implements Packet {
   private static final int AMBIENT_MASK = 1;
   private static final int SHOW_PARTICLES_MASK = 2;
   private static final int SHOW_ICON_MASK = 4;
   private final int entityId;
   private final StatusEffect effectId;
   private final byte amplifier;
   private final int duration;
   private final byte flags;
   @Nullable
   private final StatusEffectInstance.FactorCalculationData factorCalculationData;

   public EntityStatusEffectS2CPacket(int entityId, StatusEffectInstance effect) {
      this.entityId = entityId;
      this.effectId = effect.getEffectType();
      this.amplifier = (byte)(effect.getAmplifier() & 255);
      this.duration = effect.getDuration();
      byte b = 0;
      if (effect.isAmbient()) {
         b = (byte)(b | 1);
      }

      if (effect.shouldShowParticles()) {
         b = (byte)(b | 2);
      }

      if (effect.shouldShowIcon()) {
         b = (byte)(b | 4);
      }

      this.flags = b;
      this.factorCalculationData = (StatusEffectInstance.FactorCalculationData)effect.getFactorCalculationData().orElse((Object)null);
   }

   public EntityStatusEffectS2CPacket(PacketByteBuf buf) {
      this.entityId = buf.readVarInt();
      this.effectId = (StatusEffect)buf.readRegistryValue(Registries.STATUS_EFFECT);
      this.amplifier = buf.readByte();
      this.duration = buf.readVarInt();
      this.flags = buf.readByte();
      this.factorCalculationData = (StatusEffectInstance.FactorCalculationData)buf.readNullable((buf2) -> {
         return (StatusEffectInstance.FactorCalculationData)buf2.decode(NbtOps.INSTANCE, StatusEffectInstance.FactorCalculationData.CODEC);
      });
   }

   public void write(PacketByteBuf buf) {
      buf.writeVarInt(this.entityId);
      buf.writeRegistryValue(Registries.STATUS_EFFECT, this.effectId);
      buf.writeByte(this.amplifier);
      buf.writeVarInt(this.duration);
      buf.writeByte(this.flags);
      buf.writeNullable(this.factorCalculationData, (buf2, factorCalculationData) -> {
         buf2.encode(NbtOps.INSTANCE, StatusEffectInstance.FactorCalculationData.CODEC, factorCalculationData);
      });
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onEntityStatusEffect(this);
   }

   public int getEntityId() {
      return this.entityId;
   }

   public StatusEffect getEffectId() {
      return this.effectId;
   }

   public byte getAmplifier() {
      return this.amplifier;
   }

   public int getDuration() {
      return this.duration;
   }

   public boolean shouldShowParticles() {
      return (this.flags & 2) == 2;
   }

   public boolean isAmbient() {
      return (this.flags & 1) == 1;
   }

   public boolean shouldShowIcon() {
      return (this.flags & 4) == 4;
   }

   @Nullable
   public StatusEffectInstance.FactorCalculationData getFactorCalculationData() {
      return this.factorCalculationData;
   }
}
