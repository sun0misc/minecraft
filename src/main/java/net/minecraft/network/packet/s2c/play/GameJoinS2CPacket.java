package net.minecraft.network.packet.s2c.play;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.SerializableRegistries;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

public record GameJoinS2CPacket(int playerEntityId, boolean hardcore, GameMode gameMode, @Nullable GameMode previousGameMode, Set dimensionIds, DynamicRegistryManager.Immutable registryManager, RegistryKey dimensionType, RegistryKey dimensionId, long sha256Seed, int maxPlayers, int viewDistance, int simulationDistance, boolean reducedDebugInfo, boolean showDeathScreen, boolean debugWorld, boolean flatWorld, Optional lastDeathLocation) implements Packet {
   private static final RegistryOps REGISTRY_OPS;

   public GameJoinS2CPacket(PacketByteBuf buf) {
      this(buf.readInt(), buf.readBoolean(), GameMode.byId(buf.readByte()), GameMode.getOrNull(buf.readByte()), (Set)buf.readCollection(Sets::newHashSetWithExpectedSize, (b) -> {
         return b.readRegistryKey(RegistryKeys.WORLD);
      }), ((DynamicRegistryManager)buf.decode(REGISTRY_OPS, SerializableRegistries.CODEC)).toImmutable(), buf.readRegistryKey(RegistryKeys.DIMENSION_TYPE), buf.readRegistryKey(RegistryKeys.WORLD), buf.readLong(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readOptional(PacketByteBuf::readGlobalPos));
   }

   public GameJoinS2CPacket(int playerEntityId, boolean bl, GameMode previousGameMode, @Nullable GameMode arg2, Set set, DynamicRegistryManager.Immutable arg3, RegistryKey arg4, RegistryKey arg5, long l, int maxPlayers, int chunkLoadDistance, int m, boolean bl2, boolean bl3, boolean bl4, boolean bl5, Optional optional) {
      this.playerEntityId = playerEntityId;
      this.hardcore = bl;
      this.gameMode = previousGameMode;
      this.previousGameMode = arg2;
      this.dimensionIds = set;
      this.registryManager = arg3;
      this.dimensionType = arg4;
      this.dimensionId = arg5;
      this.sha256Seed = l;
      this.maxPlayers = maxPlayers;
      this.viewDistance = chunkLoadDistance;
      this.simulationDistance = m;
      this.reducedDebugInfo = bl2;
      this.showDeathScreen = bl3;
      this.debugWorld = bl4;
      this.flatWorld = bl5;
      this.lastDeathLocation = optional;
   }

   public void write(PacketByteBuf buf) {
      buf.writeInt(this.playerEntityId);
      buf.writeBoolean(this.hardcore);
      buf.writeByte(this.gameMode.getId());
      buf.writeByte(GameMode.getId(this.previousGameMode));
      buf.writeCollection(this.dimensionIds, PacketByteBuf::writeRegistryKey);
      buf.encode(REGISTRY_OPS, SerializableRegistries.CODEC, this.registryManager);
      buf.writeRegistryKey(this.dimensionType);
      buf.writeRegistryKey(this.dimensionId);
      buf.writeLong(this.sha256Seed);
      buf.writeVarInt(this.maxPlayers);
      buf.writeVarInt(this.viewDistance);
      buf.writeVarInt(this.simulationDistance);
      buf.writeBoolean(this.reducedDebugInfo);
      buf.writeBoolean(this.showDeathScreen);
      buf.writeBoolean(this.debugWorld);
      buf.writeBoolean(this.flatWorld);
      buf.writeOptional(this.lastDeathLocation, PacketByteBuf::writeGlobalPos);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onGameJoin(this);
   }

   public int playerEntityId() {
      return this.playerEntityId;
   }

   public boolean hardcore() {
      return this.hardcore;
   }

   public GameMode gameMode() {
      return this.gameMode;
   }

   @Nullable
   public GameMode previousGameMode() {
      return this.previousGameMode;
   }

   public Set dimensionIds() {
      return this.dimensionIds;
   }

   public DynamicRegistryManager.Immutable registryManager() {
      return this.registryManager;
   }

   public RegistryKey dimensionType() {
      return this.dimensionType;
   }

   public RegistryKey dimensionId() {
      return this.dimensionId;
   }

   public long sha256Seed() {
      return this.sha256Seed;
   }

   public int maxPlayers() {
      return this.maxPlayers;
   }

   public int viewDistance() {
      return this.viewDistance;
   }

   public int simulationDistance() {
      return this.simulationDistance;
   }

   public boolean reducedDebugInfo() {
      return this.reducedDebugInfo;
   }

   public boolean showDeathScreen() {
      return this.showDeathScreen;
   }

   public boolean debugWorld() {
      return this.debugWorld;
   }

   public boolean flatWorld() {
      return this.flatWorld;
   }

   public Optional lastDeathLocation() {
      return this.lastDeathLocation;
   }

   static {
      REGISTRY_OPS = RegistryOps.of(NbtOps.INSTANCE, (RegistryWrapper.WrapperLookup)DynamicRegistryManager.of(Registries.REGISTRIES));
   }
}
