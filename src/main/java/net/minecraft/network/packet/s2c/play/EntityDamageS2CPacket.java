package net.minecraft.network.packet.s2c.play;

import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public record EntityDamageS2CPacket(int entityId, int sourceTypeId, int sourceCauseId, int sourceDirectId, Optional sourcePosition) implements Packet {
   public EntityDamageS2CPacket(Entity entity, DamageSource damageSource) {
      this(entity.getId(), entity.getWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).getRawId(damageSource.getType()), damageSource.getAttacker() != null ? damageSource.getAttacker().getId() : -1, damageSource.getSource() != null ? damageSource.getSource().getId() : -1, Optional.ofNullable(damageSource.getStoredPosition()));
   }

   public EntityDamageS2CPacket(PacketByteBuf buf) {
      this(buf.readVarInt(), buf.readVarInt(), readOffsetVarInt(buf), readOffsetVarInt(buf), buf.readOptional((pos) -> {
         return new Vec3d(pos.readDouble(), pos.readDouble(), pos.readDouble());
      }));
   }

   public EntityDamageS2CPacket(int i, int j, int k, int l, Optional optional) {
      this.entityId = i;
      this.sourceTypeId = j;
      this.sourceCauseId = k;
      this.sourceDirectId = l;
      this.sourcePosition = optional;
   }

   private static void writeOffsetVarInt(PacketByteBuf buf, int value) {
      buf.writeVarInt(value + 1);
   }

   private static int readOffsetVarInt(PacketByteBuf buf) {
      return buf.readVarInt() - 1;
   }

   public void write(PacketByteBuf buf) {
      buf.writeVarInt(this.entityId);
      buf.writeVarInt(this.sourceTypeId);
      writeOffsetVarInt(buf, this.sourceCauseId);
      writeOffsetVarInt(buf, this.sourceDirectId);
      buf.writeOptional(this.sourcePosition, (bufx, pos) -> {
         bufx.writeDouble(pos.getX());
         bufx.writeDouble(pos.getY());
         bufx.writeDouble(pos.getZ());
      });
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onEntityDamage(this);
   }

   public DamageSource createDamageSource(World world) {
      RegistryEntry lv = (RegistryEntry)world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).getEntry(this.sourceTypeId).get();
      if (this.sourcePosition.isPresent()) {
         return new DamageSource(lv, (Vec3d)this.sourcePosition.get());
      } else {
         Entity lv2 = world.getEntityById(this.sourceCauseId);
         Entity lv3 = world.getEntityById(this.sourceDirectId);
         return new DamageSource(lv, lv3, lv2);
      }
   }

   public int entityId() {
      return this.entityId;
   }

   public int sourceTypeId() {
      return this.sourceTypeId;
   }

   public int sourceCauseId() {
      return this.sourceCauseId;
   }

   public int sourceDirectId() {
      return this.sourceDirectId;
   }

   public Optional sourcePosition() {
      return this.sourcePosition;
   }
}
