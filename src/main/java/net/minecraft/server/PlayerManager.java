package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.FeaturesS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.SimulationDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderCenterChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInitializeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInterpolateSizeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderSizeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningBlocksChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningTimeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SerializableRegistries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagPacketSerializer;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.PathUtil;
import net.minecraft.util.UserCache;
import net.minecraft.util.Uuids;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class PlayerManager {
   public static final File BANNED_PLAYERS_FILE = new File("banned-players.json");
   public static final File BANNED_IPS_FILE = new File("banned-ips.json");
   public static final File OPERATORS_FILE = new File("ops.json");
   public static final File WHITELIST_FILE = new File("whitelist.json");
   public static final Text FILTERED_FULL_TEXT = Text.translatable("chat.filtered_full");
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int LATENCY_UPDATE_INTERVAL = 600;
   private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
   private final MinecraftServer server;
   private final List players = Lists.newArrayList();
   private final Map playerMap = Maps.newHashMap();
   private final BannedPlayerList bannedProfiles;
   private final BannedIpList bannedIps;
   private final OperatorList ops;
   private final Whitelist whitelist;
   private final Map statisticsMap;
   private final Map advancementTrackers;
   private final WorldSaveHandler saveHandler;
   private boolean whitelistEnabled;
   private final CombinedDynamicRegistries registryManager;
   private final DynamicRegistryManager.Immutable syncedRegistryManager;
   protected final int maxPlayers;
   private int viewDistance;
   private int simulationDistance;
   private boolean cheatsAllowed;
   private static final boolean field_29791 = false;
   private int latencyUpdateTimer;

   public PlayerManager(MinecraftServer server, CombinedDynamicRegistries registryManager, WorldSaveHandler saveHandler, int maxPlayers) {
      this.bannedProfiles = new BannedPlayerList(BANNED_PLAYERS_FILE);
      this.bannedIps = new BannedIpList(BANNED_IPS_FILE);
      this.ops = new OperatorList(OPERATORS_FILE);
      this.whitelist = new Whitelist(WHITELIST_FILE);
      this.statisticsMap = Maps.newHashMap();
      this.advancementTrackers = Maps.newHashMap();
      this.server = server;
      this.registryManager = registryManager;
      this.syncedRegistryManager = (new DynamicRegistryManager.ImmutableImpl(SerializableRegistries.streamDynamicEntries(registryManager))).toImmutable();
      this.maxPlayers = maxPlayers;
      this.saveHandler = saveHandler;
   }

   public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player) {
      GameProfile gameProfile = player.getGameProfile();
      UserCache lv = this.server.getUserCache();
      Optional optional = lv.getByUuid(gameProfile.getId());
      String string = (String)optional.map(GameProfile::getName).orElse(gameProfile.getName());
      lv.add(gameProfile);
      NbtCompound lv2 = this.loadPlayerData(player);
      RegistryKey var24;
      if (lv2 != null) {
         DataResult var10000 = DimensionType.worldFromDimensionNbt(new Dynamic(NbtOps.INSTANCE, lv2.get("Dimension")));
         Logger var10001 = LOGGER;
         Objects.requireNonNull(var10001);
         var24 = (RegistryKey)var10000.resultOrPartial(var10001::error).orElse(World.OVERWORLD);
      } else {
         var24 = World.OVERWORLD;
      }

      RegistryKey lv3 = var24;
      ServerWorld lv4 = this.server.getWorld(lv3);
      ServerWorld lv5;
      if (lv4 == null) {
         LOGGER.warn("Unknown respawn dimension {}, defaulting to overworld", lv3);
         lv5 = this.server.getOverworld();
      } else {
         lv5 = lv4;
      }

      player.setWorld(lv5);
      String string2 = "local";
      if (connection.getAddress() != null) {
         string2 = connection.getAddress().toString();
      }

      LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", new Object[]{player.getName().getString(), string2, player.getId(), player.getX(), player.getY(), player.getZ()});
      WorldProperties lv6 = lv5.getLevelProperties();
      player.setGameMode(lv2);
      ServerPlayNetworkHandler lv7 = new ServerPlayNetworkHandler(this.server, connection, player);
      GameRules lv8 = lv5.getGameRules();
      boolean bl = lv8.getBoolean(GameRules.DO_IMMEDIATE_RESPAWN);
      boolean bl2 = lv8.getBoolean(GameRules.REDUCED_DEBUG_INFO);
      lv7.sendPacket(new GameJoinS2CPacket(player.getId(), lv6.isHardcore(), player.interactionManager.getGameMode(), player.interactionManager.getPreviousGameMode(), this.server.getWorldRegistryKeys(), this.syncedRegistryManager, lv5.getDimensionKey(), lv5.getRegistryKey(), BiomeAccess.hashSeed(lv5.getSeed()), this.getMaxPlayerCount(), this.viewDistance, this.simulationDistance, bl2, !bl, lv5.isDebugWorld(), lv5.isFlat(), player.getLastDeathPos()));
      lv7.sendPacket(new FeaturesS2CPacket(FeatureFlags.FEATURE_MANAGER.toId(lv5.getEnabledFeatures())));
      lv7.sendPacket(new CustomPayloadS2CPacket(CustomPayloadS2CPacket.BRAND, (new PacketByteBuf(Unpooled.buffer())).writeString(this.getServer().getServerModName())));
      lv7.sendPacket(new DifficultyS2CPacket(lv6.getDifficulty(), lv6.isDifficultyLocked()));
      lv7.sendPacket(new PlayerAbilitiesS2CPacket(player.getAbilities()));
      lv7.sendPacket(new UpdateSelectedSlotS2CPacket(player.getInventory().selectedSlot));
      lv7.sendPacket(new SynchronizeRecipesS2CPacket(this.server.getRecipeManager().values()));
      lv7.sendPacket(new SynchronizeTagsS2CPacket(TagPacketSerializer.serializeTags(this.registryManager)));
      this.sendCommandTree(player);
      player.getStatHandler().updateStatSet();
      player.getRecipeBook().sendInitRecipesPacket(player);
      this.sendScoreboard(lv5.getScoreboard(), player);
      this.server.forcePlayerSampleUpdate();
      MutableText lv9;
      if (player.getGameProfile().getName().equalsIgnoreCase(string)) {
         lv9 = Text.translatable("multiplayer.player.joined", player.getDisplayName());
      } else {
         lv9 = Text.translatable("multiplayer.player.joined.renamed", player.getDisplayName(), string);
      }

      this.broadcast(lv9.formatted(Formatting.YELLOW), false);
      lv7.requestTeleport(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
      ServerMetadata lv10 = this.server.getServerMetadata();
      if (lv10 != null) {
         player.sendServerMetadata(lv10);
      }

      player.networkHandler.sendPacket(PlayerListS2CPacket.entryFromPlayer(this.players));
      this.players.add(player);
      this.playerMap.put(player.getUuid(), player);
      this.sendToAll(PlayerListS2CPacket.entryFromPlayer(List.of(player)));
      this.sendWorldInfo(player, lv5);
      lv5.onPlayerConnected(player);
      this.server.getBossBarManager().onPlayerConnect(player);
      this.server.getResourcePackProperties().ifPresent((properties) -> {
         player.sendResourcePackUrl(properties.url(), properties.hash(), properties.isRequired(), properties.prompt());
      });
      Iterator var19 = player.getStatusEffects().iterator();

      while(var19.hasNext()) {
         StatusEffectInstance lv11 = (StatusEffectInstance)var19.next();
         lv7.sendPacket(new EntityStatusEffectS2CPacket(player.getId(), lv11));
      }

      if (lv2 != null && lv2.contains("RootVehicle", NbtElement.COMPOUND_TYPE)) {
         NbtCompound lv12 = lv2.getCompound("RootVehicle");
         Entity lv13 = EntityType.loadEntityWithPassengers(lv12.getCompound("Entity"), lv5, (vehicle) -> {
            return !lv5.tryLoadEntity(vehicle) ? null : vehicle;
         });
         if (lv13 != null) {
            UUID uUID;
            if (lv12.containsUuid("Attach")) {
               uUID = lv12.getUuid("Attach");
            } else {
               uUID = null;
            }

            Iterator var22;
            Entity lv14;
            if (lv13.getUuid().equals(uUID)) {
               player.startRiding(lv13, true);
            } else {
               var22 = lv13.getPassengersDeep().iterator();

               while(var22.hasNext()) {
                  lv14 = (Entity)var22.next();
                  if (lv14.getUuid().equals(uUID)) {
                     player.startRiding(lv14, true);
                     break;
                  }
               }
            }

            if (!player.hasVehicle()) {
               LOGGER.warn("Couldn't reattach entity to player");
               lv13.discard();
               var22 = lv13.getPassengersDeep().iterator();

               while(var22.hasNext()) {
                  lv14 = (Entity)var22.next();
                  lv14.discard();
               }
            }
         }
      }

      player.onSpawn();
   }

   protected void sendScoreboard(ServerScoreboard scoreboard, ServerPlayerEntity player) {
      Set set = Sets.newHashSet();
      Iterator var4 = scoreboard.getTeams().iterator();

      while(var4.hasNext()) {
         Team lv = (Team)var4.next();
         player.networkHandler.sendPacket(TeamS2CPacket.updateTeam(lv, true));
      }

      for(int i = 0; i < 19; ++i) {
         ScoreboardObjective lv2 = scoreboard.getObjectiveForSlot(i);
         if (lv2 != null && !set.contains(lv2)) {
            List list = scoreboard.createChangePackets(lv2);
            Iterator var7 = list.iterator();

            while(var7.hasNext()) {
               Packet lv3 = (Packet)var7.next();
               player.networkHandler.sendPacket(lv3);
            }

            set.add(lv2);
         }
      }

   }

   public void setMainWorld(ServerWorld world) {
      world.getWorldBorder().addListener(new WorldBorderListener() {
         public void onSizeChange(WorldBorder border, double size) {
            PlayerManager.this.sendToAll(new WorldBorderSizeChangedS2CPacket(border));
         }

         public void onInterpolateSize(WorldBorder border, double fromSize, double toSize, long time) {
            PlayerManager.this.sendToAll(new WorldBorderInterpolateSizeS2CPacket(border));
         }

         public void onCenterChanged(WorldBorder border, double centerX, double centerZ) {
            PlayerManager.this.sendToAll(new WorldBorderCenterChangedS2CPacket(border));
         }

         public void onWarningTimeChanged(WorldBorder border, int warningTime) {
            PlayerManager.this.sendToAll(new WorldBorderWarningTimeChangedS2CPacket(border));
         }

         public void onWarningBlocksChanged(WorldBorder border, int warningBlockDistance) {
            PlayerManager.this.sendToAll(new WorldBorderWarningBlocksChangedS2CPacket(border));
         }

         public void onDamagePerBlockChanged(WorldBorder border, double damagePerBlock) {
         }

         public void onSafeZoneChanged(WorldBorder border, double safeZoneRadius) {
         }
      });
   }

   @Nullable
   public NbtCompound loadPlayerData(ServerPlayerEntity player) {
      NbtCompound lv = this.server.getSaveProperties().getPlayerData();
      NbtCompound lv2;
      if (this.server.isHost(player.getGameProfile()) && lv != null) {
         lv2 = lv;
         player.readNbt(lv);
         LOGGER.debug("loading single player");
      } else {
         lv2 = this.saveHandler.loadPlayerData(player);
      }

      return lv2;
   }

   protected void savePlayerData(ServerPlayerEntity player) {
      this.saveHandler.savePlayerData(player);
      ServerStatHandler lv = (ServerStatHandler)this.statisticsMap.get(player.getUuid());
      if (lv != null) {
         lv.save();
      }

      PlayerAdvancementTracker lv2 = (PlayerAdvancementTracker)this.advancementTrackers.get(player.getUuid());
      if (lv2 != null) {
         lv2.save();
      }

   }

   public void remove(ServerPlayerEntity player) {
      ServerWorld lv = player.getWorld();
      player.incrementStat(Stats.LEAVE_GAME);
      this.savePlayerData(player);
      if (player.hasVehicle()) {
         Entity lv2 = player.getRootVehicle();
         if (lv2.hasPlayerRider()) {
            LOGGER.debug("Removing player mount");
            player.stopRiding();
            lv2.streamPassengersAndSelf().forEach((entity) -> {
               entity.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
            });
         }
      }

      player.detach();
      lv.removePlayer(player, Entity.RemovalReason.UNLOADED_WITH_PLAYER);
      player.getAdvancementTracker().clearCriteria();
      this.players.remove(player);
      this.server.getBossBarManager().onPlayerDisconnect(player);
      UUID uUID = player.getUuid();
      ServerPlayerEntity lv3 = (ServerPlayerEntity)this.playerMap.get(uUID);
      if (lv3 == player) {
         this.playerMap.remove(uUID);
         this.statisticsMap.remove(uUID);
         this.advancementTrackers.remove(uUID);
      }

      this.sendToAll(new PlayerRemoveS2CPacket(List.of(player.getUuid())));
   }

   @Nullable
   public Text checkCanJoin(SocketAddress address, GameProfile profile) {
      MutableText lv2;
      if (this.bannedProfiles.contains(profile)) {
         BannedPlayerEntry lv = (BannedPlayerEntry)this.bannedProfiles.get(profile);
         lv2 = Text.translatable("multiplayer.disconnect.banned.reason", lv.getReason());
         if (lv.getExpiryDate() != null) {
            lv2.append((Text)Text.translatable("multiplayer.disconnect.banned.expiration", DATE_FORMATTER.format(lv.getExpiryDate())));
         }

         return lv2;
      } else if (!this.isWhitelisted(profile)) {
         return Text.translatable("multiplayer.disconnect.not_whitelisted");
      } else if (this.bannedIps.isBanned(address)) {
         BannedIpEntry lv3 = this.bannedIps.get(address);
         lv2 = Text.translatable("multiplayer.disconnect.banned_ip.reason", lv3.getReason());
         if (lv3.getExpiryDate() != null) {
            lv2.append((Text)Text.translatable("multiplayer.disconnect.banned_ip.expiration", DATE_FORMATTER.format(lv3.getExpiryDate())));
         }

         return lv2;
      } else {
         return this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(profile) ? Text.translatable("multiplayer.disconnect.server_full") : null;
      }
   }

   public ServerPlayerEntity createPlayer(GameProfile profile) {
      UUID uUID = Uuids.getUuidFromProfile(profile);
      List list = Lists.newArrayList();

      for(int i = 0; i < this.players.size(); ++i) {
         ServerPlayerEntity lv = (ServerPlayerEntity)this.players.get(i);
         if (lv.getUuid().equals(uUID)) {
            list.add(lv);
         }
      }

      ServerPlayerEntity lv2 = (ServerPlayerEntity)this.playerMap.get(profile.getId());
      if (lv2 != null && !list.contains(lv2)) {
         list.add(lv2);
      }

      Iterator var8 = list.iterator();

      while(var8.hasNext()) {
         ServerPlayerEntity lv3 = (ServerPlayerEntity)var8.next();
         lv3.networkHandler.disconnect(Text.translatable("multiplayer.disconnect.duplicate_login"));
      }

      return new ServerPlayerEntity(this.server, this.server.getOverworld(), profile);
   }

   public ServerPlayerEntity respawnPlayer(ServerPlayerEntity player, boolean alive) {
      this.players.remove(player);
      player.getWorld().removePlayer(player, Entity.RemovalReason.DISCARDED);
      BlockPos lv = player.getSpawnPointPosition();
      float f = player.getSpawnAngle();
      boolean bl2 = player.isSpawnForced();
      ServerWorld lv2 = this.server.getWorld(player.getSpawnPointDimension());
      Optional optional;
      if (lv2 != null && lv != null) {
         optional = PlayerEntity.findRespawnPosition(lv2, lv, f, bl2, alive);
      } else {
         optional = Optional.empty();
      }

      ServerWorld lv3 = lv2 != null && optional.isPresent() ? lv2 : this.server.getOverworld();
      ServerPlayerEntity lv4 = new ServerPlayerEntity(this.server, lv3, player.getGameProfile());
      lv4.networkHandler = player.networkHandler;
      lv4.copyFrom(player, alive);
      lv4.setId(player.getId());
      lv4.setMainArm(player.getMainArm());
      Iterator var10 = player.getCommandTags().iterator();

      while(var10.hasNext()) {
         String string = (String)var10.next();
         lv4.addCommandTag(string);
      }

      boolean bl3 = false;
      if (optional.isPresent()) {
         BlockState lv5 = lv3.getBlockState(lv);
         boolean bl4 = lv5.isOf(Blocks.RESPAWN_ANCHOR);
         Vec3d lv6 = (Vec3d)optional.get();
         float g;
         if (!lv5.isIn(BlockTags.BEDS) && !bl4) {
            g = f;
         } else {
            Vec3d lv7 = Vec3d.ofBottomCenter(lv).subtract(lv6).normalize();
            g = (float)MathHelper.wrapDegrees(MathHelper.atan2(lv7.z, lv7.x) * 57.2957763671875 - 90.0);
         }

         lv4.refreshPositionAndAngles(lv6.x, lv6.y, lv6.z, g, 0.0F);
         lv4.setSpawnPoint(lv3.getRegistryKey(), lv, f, bl2, false);
         bl3 = !alive && bl4;
      } else if (lv != null) {
         lv4.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.NO_RESPAWN_BLOCK, GameStateChangeS2CPacket.field_33328));
      }

      while(!lv3.isSpaceEmpty(lv4) && lv4.getY() < (double)lv3.getTopY()) {
         lv4.setPosition(lv4.getX(), lv4.getY() + 1.0, lv4.getZ());
      }

      byte b = alive ? 1 : 0;
      WorldProperties lv8 = lv4.world.getLevelProperties();
      lv4.networkHandler.sendPacket(new PlayerRespawnS2CPacket(lv4.world.getDimensionKey(), lv4.world.getRegistryKey(), BiomeAccess.hashSeed(lv4.getWorld().getSeed()), lv4.interactionManager.getGameMode(), lv4.interactionManager.getPreviousGameMode(), lv4.getWorld().isDebugWorld(), lv4.getWorld().isFlat(), (byte)b, lv4.getLastDeathPos()));
      lv4.networkHandler.requestTeleport(lv4.getX(), lv4.getY(), lv4.getZ(), lv4.getYaw(), lv4.getPitch());
      lv4.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(lv3.getSpawnPos(), lv3.getSpawnAngle()));
      lv4.networkHandler.sendPacket(new DifficultyS2CPacket(lv8.getDifficulty(), lv8.isDifficultyLocked()));
      lv4.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(lv4.experienceProgress, lv4.totalExperience, lv4.experienceLevel));
      this.sendWorldInfo(lv4, lv3);
      this.sendCommandTree(lv4);
      lv3.onPlayerRespawned(lv4);
      this.players.add(lv4);
      this.playerMap.put(lv4.getUuid(), lv4);
      lv4.onSpawn();
      lv4.setHealth(lv4.getHealth());
      if (bl3) {
         lv4.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.BLOCKS, (double)lv.getX(), (double)lv.getY(), (double)lv.getZ(), 1.0F, 1.0F, lv3.getRandom().nextLong()));
      }

      return lv4;
   }

   public void sendCommandTree(ServerPlayerEntity player) {
      GameProfile gameProfile = player.getGameProfile();
      int i = this.server.getPermissionLevel(gameProfile);
      this.sendCommandTree(player, i);
   }

   public void updatePlayerLatency() {
      if (++this.latencyUpdateTimer > 600) {
         this.sendToAll(new PlayerListS2CPacket(EnumSet.of(PlayerListS2CPacket.Action.UPDATE_LATENCY), this.players));
         this.latencyUpdateTimer = 0;
      }

   }

   public void sendToAll(Packet packet) {
      Iterator var2 = this.players.iterator();

      while(var2.hasNext()) {
         ServerPlayerEntity lv = (ServerPlayerEntity)var2.next();
         lv.networkHandler.sendPacket(packet);
      }

   }

   public void sendToDimension(Packet packet, RegistryKey dimension) {
      Iterator var3 = this.players.iterator();

      while(var3.hasNext()) {
         ServerPlayerEntity lv = (ServerPlayerEntity)var3.next();
         if (lv.world.getRegistryKey() == dimension) {
            lv.networkHandler.sendPacket(packet);
         }
      }

   }

   public void sendToTeam(PlayerEntity source, Text message) {
      AbstractTeam lv = source.getScoreboardTeam();
      if (lv != null) {
         Collection collection = lv.getPlayerList();
         Iterator var5 = collection.iterator();

         while(var5.hasNext()) {
            String string = (String)var5.next();
            ServerPlayerEntity lv2 = this.getPlayer(string);
            if (lv2 != null && lv2 != source) {
               lv2.sendMessage(message);
            }
         }

      }
   }

   public void sendToOtherTeams(PlayerEntity source, Text message) {
      AbstractTeam lv = source.getScoreboardTeam();
      if (lv == null) {
         this.broadcast(message, false);
      } else {
         for(int i = 0; i < this.players.size(); ++i) {
            ServerPlayerEntity lv2 = (ServerPlayerEntity)this.players.get(i);
            if (lv2.getScoreboardTeam() != lv) {
               lv2.sendMessage(message);
            }
         }

      }
   }

   public String[] getPlayerNames() {
      String[] strings = new String[this.players.size()];

      for(int i = 0; i < this.players.size(); ++i) {
         strings[i] = ((ServerPlayerEntity)this.players.get(i)).getGameProfile().getName();
      }

      return strings;
   }

   public BannedPlayerList getUserBanList() {
      return this.bannedProfiles;
   }

   public BannedIpList getIpBanList() {
      return this.bannedIps;
   }

   public void addToOperators(GameProfile profile) {
      this.ops.add(new OperatorEntry(profile, this.server.getOpPermissionLevel(), this.ops.canBypassPlayerLimit(profile)));
      ServerPlayerEntity lv = this.getPlayer(profile.getId());
      if (lv != null) {
         this.sendCommandTree(lv);
      }

   }

   public void removeFromOperators(GameProfile profile) {
      this.ops.remove(profile);
      ServerPlayerEntity lv = this.getPlayer(profile.getId());
      if (lv != null) {
         this.sendCommandTree(lv);
      }

   }

   private void sendCommandTree(ServerPlayerEntity player, int permissionLevel) {
      if (player.networkHandler != null) {
         byte b;
         if (permissionLevel <= 0) {
            b = 24;
         } else if (permissionLevel >= 4) {
            b = 28;
         } else {
            b = (byte)(EntityStatuses.SET_OP_LEVEL_0 + permissionLevel);
         }

         player.networkHandler.sendPacket(new EntityStatusS2CPacket(player, b));
      }

      this.server.getCommandManager().sendCommandTree(player);
   }

   public boolean isWhitelisted(GameProfile profile) {
      return !this.whitelistEnabled || this.ops.contains(profile) || this.whitelist.contains(profile);
   }

   public boolean isOperator(GameProfile profile) {
      return this.ops.contains(profile) || this.server.isHost(profile) && this.server.getSaveProperties().areCommandsAllowed() || this.cheatsAllowed;
   }

   @Nullable
   public ServerPlayerEntity getPlayer(String name) {
      Iterator var2 = this.players.iterator();

      ServerPlayerEntity lv;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         lv = (ServerPlayerEntity)var2.next();
      } while(!lv.getGameProfile().getName().equalsIgnoreCase(name));

      return lv;
   }

   public void sendToAround(@Nullable PlayerEntity player, double x, double y, double z, double distance, RegistryKey worldKey, Packet packet) {
      for(int i = 0; i < this.players.size(); ++i) {
         ServerPlayerEntity lv = (ServerPlayerEntity)this.players.get(i);
         if (lv != player && lv.world.getRegistryKey() == worldKey) {
            double h = x - lv.getX();
            double j = y - lv.getY();
            double k = z - lv.getZ();
            if (h * h + j * j + k * k < distance * distance) {
               lv.networkHandler.sendPacket(packet);
            }
         }
      }

   }

   public void saveAllPlayerData() {
      for(int i = 0; i < this.players.size(); ++i) {
         this.savePlayerData((ServerPlayerEntity)this.players.get(i));
      }

   }

   public Whitelist getWhitelist() {
      return this.whitelist;
   }

   public String[] getWhitelistedNames() {
      return this.whitelist.getNames();
   }

   public OperatorList getOpList() {
      return this.ops;
   }

   public String[] getOpNames() {
      return this.ops.getNames();
   }

   public void reloadWhitelist() {
   }

   public void sendWorldInfo(ServerPlayerEntity player, ServerWorld world) {
      WorldBorder lv = this.server.getOverworld().getWorldBorder();
      player.networkHandler.sendPacket(new WorldBorderInitializeS2CPacket(lv));
      player.networkHandler.sendPacket(new WorldTimeUpdateS2CPacket(world.getTime(), world.getTimeOfDay(), world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)));
      player.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(world.getSpawnPos(), world.getSpawnAngle()));
      if (world.isRaining()) {
         player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_STARTED, GameStateChangeS2CPacket.field_33328));
         player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED, world.getRainGradient(1.0F)));
         player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED, world.getThunderGradient(1.0F)));
      }

   }

   public void sendPlayerStatus(ServerPlayerEntity player) {
      player.playerScreenHandler.syncState();
      player.markHealthDirty();
      player.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(player.getInventory().selectedSlot));
   }

   public int getCurrentPlayerCount() {
      return this.players.size();
   }

   public int getMaxPlayerCount() {
      return this.maxPlayers;
   }

   public boolean isWhitelistEnabled() {
      return this.whitelistEnabled;
   }

   public void setWhitelistEnabled(boolean whitelistEnabled) {
      this.whitelistEnabled = whitelistEnabled;
   }

   public List getPlayersByIp(String ip) {
      List list = Lists.newArrayList();
      Iterator var3 = this.players.iterator();

      while(var3.hasNext()) {
         ServerPlayerEntity lv = (ServerPlayerEntity)var3.next();
         if (lv.getIp().equals(ip)) {
            list.add(lv);
         }
      }

      return list;
   }

   public int getViewDistance() {
      return this.viewDistance;
   }

   public int getSimulationDistance() {
      return this.simulationDistance;
   }

   public MinecraftServer getServer() {
      return this.server;
   }

   @Nullable
   public NbtCompound getUserData() {
      return null;
   }

   public void setCheatsAllowed(boolean cheatsAllowed) {
      this.cheatsAllowed = cheatsAllowed;
   }

   public void disconnectAllPlayers() {
      for(int i = 0; i < this.players.size(); ++i) {
         ((ServerPlayerEntity)this.players.get(i)).networkHandler.disconnect(Text.translatable("multiplayer.disconnect.server_shutdown"));
      }

   }

   public void broadcast(Text message, boolean overlay) {
      this.broadcast(message, (player) -> {
         return message;
      }, overlay);
   }

   public void broadcast(Text message, Function playerMessageFactory, boolean overlay) {
      this.server.sendMessage(message);
      Iterator var4 = this.players.iterator();

      while(var4.hasNext()) {
         ServerPlayerEntity lv = (ServerPlayerEntity)var4.next();
         Text lv2 = (Text)playerMessageFactory.apply(lv);
         if (lv2 != null) {
            lv.sendMessageToClient(lv2, overlay);
         }
      }

   }

   public void broadcast(SignedMessage message, ServerCommandSource source, MessageType.Parameters params) {
      Objects.requireNonNull(source);
      this.broadcast(message, source::shouldFilterText, source.getPlayer(), params);
   }

   public void broadcast(SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params) {
      Objects.requireNonNull(sender);
      this.broadcast(message, sender::shouldFilterMessagesSentTo, sender, params);
   }

   private void broadcast(SignedMessage message, Predicate shouldSendFiltered, @Nullable ServerPlayerEntity sender, MessageType.Parameters params) {
      boolean bl = this.verify(message);
      this.server.logChatMessage(message.getContent(), params, bl ? null : "Not Secure");
      SentMessage lv = SentMessage.of(message);
      boolean bl2 = false;

      boolean bl3;
      for(Iterator var8 = this.players.iterator(); var8.hasNext(); bl2 |= bl3 && message.isFullyFiltered()) {
         ServerPlayerEntity lv2 = (ServerPlayerEntity)var8.next();
         bl3 = shouldSendFiltered.test(lv2);
         lv2.sendChatMessage(lv, bl3, params);
      }

      if (bl2 && sender != null) {
         sender.sendMessage(FILTERED_FULL_TEXT);
      }

   }

   private boolean verify(SignedMessage message) {
      return message.hasSignature() && !message.isExpiredOnServer(Instant.now());
   }

   public ServerStatHandler createStatHandler(PlayerEntity player) {
      UUID uUID = player.getUuid();
      ServerStatHandler lv = (ServerStatHandler)this.statisticsMap.get(uUID);
      if (lv == null) {
         File file = this.server.getSavePath(WorldSavePath.STATS).toFile();
         File file2 = new File(file, "" + uUID + ".json");
         if (!file2.exists()) {
            File file3 = new File(file, player.getName().getString() + ".json");
            Path path = file3.toPath();
            if (PathUtil.isNormal(path) && PathUtil.isAllowedName(path) && path.startsWith(file.getPath()) && file3.isFile()) {
               file3.renameTo(file2);
            }
         }

         lv = new ServerStatHandler(this.server, file2);
         this.statisticsMap.put(uUID, lv);
      }

      return lv;
   }

   public PlayerAdvancementTracker getAdvancementTracker(ServerPlayerEntity player) {
      UUID uUID = player.getUuid();
      PlayerAdvancementTracker lv = (PlayerAdvancementTracker)this.advancementTrackers.get(uUID);
      if (lv == null) {
         Path path = this.server.getSavePath(WorldSavePath.ADVANCEMENTS).resolve("" + uUID + ".json");
         lv = new PlayerAdvancementTracker(this.server.getDataFixer(), this, this.server.getAdvancementLoader(), path, player);
         this.advancementTrackers.put(uUID, lv);
      }

      lv.setOwner(player);
      return lv;
   }

   public void setViewDistance(int viewDistance) {
      this.viewDistance = viewDistance;
      this.sendToAll(new ChunkLoadDistanceS2CPacket(viewDistance));
      Iterator var2 = this.server.getWorlds().iterator();

      while(var2.hasNext()) {
         ServerWorld lv = (ServerWorld)var2.next();
         if (lv != null) {
            lv.getChunkManager().applyViewDistance(viewDistance);
         }
      }

   }

   public void setSimulationDistance(int simulationDistance) {
      this.simulationDistance = simulationDistance;
      this.sendToAll(new SimulationDistanceS2CPacket(simulationDistance));
      Iterator var2 = this.server.getWorlds().iterator();

      while(var2.hasNext()) {
         ServerWorld lv = (ServerWorld)var2.next();
         if (lv != null) {
            lv.getChunkManager().applySimulationDistance(simulationDistance);
         }
      }

   }

   public List getPlayerList() {
      return this.players;
   }

   @Nullable
   public ServerPlayerEntity getPlayer(UUID uuid) {
      return (ServerPlayerEntity)this.playerMap.get(uuid);
   }

   public boolean canBypassPlayerLimit(GameProfile profile) {
      return false;
   }

   public void onDataPacksReloaded() {
      Iterator var1 = this.advancementTrackers.values().iterator();

      while(var1.hasNext()) {
         PlayerAdvancementTracker lv = (PlayerAdvancementTracker)var1.next();
         lv.reload(this.server.getAdvancementLoader());
      }

      this.sendToAll(new SynchronizeTagsS2CPacket(TagPacketSerializer.serializeTags(this.registryManager)));
      SynchronizeRecipesS2CPacket lv2 = new SynchronizeRecipesS2CPacket(this.server.getRecipeManager().values());
      Iterator var5 = this.players.iterator();

      while(var5.hasNext()) {
         ServerPlayerEntity lv3 = (ServerPlayerEntity)var5.next();
         lv3.networkHandler.sendPacket(lv2);
         lv3.getRecipeBook().sendInitRecipesPacket(lv3);
      }

   }

   public boolean areCheatsAllowed() {
      return this.cheatsAllowed;
   }
}
