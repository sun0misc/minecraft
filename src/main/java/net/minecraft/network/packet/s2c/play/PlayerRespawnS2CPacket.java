package net.minecraft.network.packet.s2c.play;

import java.util.Optional;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

public class PlayerRespawnS2CPacket implements Packet {
   public static final byte KEEP_ATTRIBUTES = 1;
   public static final byte KEEP_TRACKED_DATA = 2;
   public static final byte KEEP_ALL = 3;
   private final RegistryKey dimensionType;
   private final RegistryKey dimension;
   private final long sha256Seed;
   private final GameMode gameMode;
   @Nullable
   private final GameMode previousGameMode;
   private final boolean debugWorld;
   private final boolean flatWorld;
   private final byte flag;
   private final Optional lastDeathPos;

   public PlayerRespawnS2CPacket(RegistryKey dimensionType, RegistryKey dimension, long sha256Seed, GameMode gameMode, @Nullable GameMode previousGameMode, boolean debugWorld, boolean flatWorld, byte flag, Optional lastDeathPos) {
      this.dimensionType = dimensionType;
      this.dimension = dimension;
      this.sha256Seed = sha256Seed;
      this.gameMode = gameMode;
      this.previousGameMode = previousGameMode;
      this.debugWorld = debugWorld;
      this.flatWorld = flatWorld;
      this.flag = flag;
      this.lastDeathPos = lastDeathPos;
   }

   public PlayerRespawnS2CPacket(PacketByteBuf buf) {
      this.dimensionType = buf.readRegistryKey(RegistryKeys.DIMENSION_TYPE);
      this.dimension = buf.readRegistryKey(RegistryKeys.WORLD);
      this.sha256Seed = buf.readLong();
      this.gameMode = GameMode.byId(buf.readUnsignedByte());
      this.previousGameMode = GameMode.getOrNull(buf.readByte());
      this.debugWorld = buf.readBoolean();
      this.flatWorld = buf.readBoolean();
      this.flag = buf.readByte();
      this.lastDeathPos = buf.readOptional(PacketByteBuf::readGlobalPos);
   }

   public void write(PacketByteBuf buf) {
      buf.writeRegistryKey(this.dimensionType);
      buf.writeRegistryKey(this.dimension);
      buf.writeLong(this.sha256Seed);
      buf.writeByte(this.gameMode.getId());
      buf.writeByte(GameMode.getId(this.previousGameMode));
      buf.writeBoolean(this.debugWorld);
      buf.writeBoolean(this.flatWorld);
      buf.writeByte(this.flag);
      buf.writeOptional(this.lastDeathPos, PacketByteBuf::writeGlobalPos);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onPlayerRespawn(this);
   }

   public RegistryKey getDimensionType() {
      return this.dimensionType;
   }

   public RegistryKey getDimension() {
      return this.dimension;
   }

   public long getSha256Seed() {
      return this.sha256Seed;
   }

   public GameMode getGameMode() {
      return this.gameMode;
   }

   @Nullable
   public GameMode getPreviousGameMode() {
      return this.previousGameMode;
   }

   public boolean isDebugWorld() {
      return this.debugWorld;
   }

   public boolean isFlatWorld() {
      return this.flatWorld;
   }

   public boolean hasFlag(byte flag) {
      return (this.flag & flag) != 0;
   }

   public Optional getLastDeathPos() {
      return this.lastDeathPos;
   }
}
