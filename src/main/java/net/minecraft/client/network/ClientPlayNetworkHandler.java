package net.minecraft.client.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.DemoScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsListener;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.ingame.CommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.realms.gui.screen.DisconnectedRealmsScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.debug.BeeDebugRenderer;
import net.minecraft.client.render.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.render.debug.NeighborUpdateDebugRenderer;
import net.minecraft.client.render.debug.VillageDebugRenderer;
import net.minecraft.client.render.debug.WorldGenAttemptDebugRenderer;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.sound.AggressiveBeeSoundInstance;
import net.minecraft.client.sound.GuardianAttackSoundInstance;
import net.minecraft.client.sound.MovingMinecartSoundInstance;
import net.minecraft.client.sound.PassiveBeeSoundInstance;
import net.minecraft.client.sound.SnifferDigSoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.client.toast.RecipeToast;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.ProfileKeys;
import net.minecraft.client.util.telemetry.WorldSession;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.SignedArgumentList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TrackedPosition;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.SnifferEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encryption.ClientPlayerSession;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.encryption.PlayerKeyPair;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.message.ArgumentSignatureDataMap;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.message.MessageLink;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.MessageSignatureStorage;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.MessageAcknowledgmentC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayPongC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerSessionC2SPacket;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ChatSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkBiomeDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkRenderDistanceCenterS2CPacket;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CraftFailedResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.DamageTiltS2CPacket;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.EndCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceOrbSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.FeaturesS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.LookAtS2CPacket;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.NbtQueryResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenHorseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenWrittenBookS2CPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayPingS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerActionResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.network.packet.s2c.play.ProfilelessChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SelectAdvancementTabS2CPacket;
import net.minecraft.network.packet.s2c.play.ServerMetadataS2CPacket;
import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.network.packet.s2c.play.SimulationDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.network.packet.s2c.play.UnlockRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderCenterChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInitializeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInterpolateSizeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderSizeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningBlocksChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningTimeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagPacketSerializer;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.PositionImpl;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.LightType;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.PositionSourceType;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientPlayNetworkHandler implements TickablePacketListener, ClientPlayPacketListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Text DISCONNECT_LOST_TEXT = Text.translatable("disconnect.lost");
   private static final Text UNSECURE_SERVER_TOAST_TITLE = Text.translatable("multiplayer.unsecureserver.toast.title");
   private static final Text UNSECURE_SERVER_TOAST_TEXT = Text.translatable("multiplayer.unsecureserver.toast");
   private static final Text INVALID_PACKET_TEXT = Text.translatable("multiplayer.disconnect.invalid_packet");
   private static final Text CHAT_VALIDATION_FAILED_TEXT = Text.translatable("multiplayer.disconnect.chat_validation_failed");
   private static final int ACKNOWLEDGMENT_BATCH_SIZE = 64;
   private final ClientConnection connection;
   private final List queuedPackets = new ArrayList();
   @Nullable
   private final ServerInfo serverInfo;
   private final GameProfile profile;
   private final Screen loginScreen;
   private final MinecraftClient client;
   private ClientWorld world;
   private ClientWorld.Properties worldProperties;
   private final Map playerListEntries = Maps.newHashMap();
   private final Set listedPlayerListEntries = new ReferenceOpenHashSet();
   private final ClientAdvancementManager advancementHandler;
   private final ClientCommandSource commandSource;
   private final DataQueryHandler dataQueryHandler = new DataQueryHandler(this);
   private int chunkLoadDistance = 3;
   private int simulationDistance = 3;
   private final Random random = Random.createThreadSafe();
   private CommandDispatcher commandDispatcher = new CommandDispatcher();
   private final RecipeManager recipeManager = new RecipeManager();
   private final UUID sessionId = UUID.randomUUID();
   private Set worldKeys;
   private CombinedDynamicRegistries combinedDynamicRegistries = ClientDynamicRegistryType.createCombinedDynamicRegistries();
   private FeatureSet enabledFeatures;
   private final WorldSession worldSession;
   @Nullable
   private ClientPlayerSession session;
   private MessageChain.Packer messagePacker;
   private LastSeenMessagesCollector lastSeenMessagesCollector;
   private MessageSignatureStorage signatureStorage;

   public ClientPlayNetworkHandler(MinecraftClient client, Screen screen, ClientConnection connection, @Nullable ServerInfo serverInfo, GameProfile profile, WorldSession worldSession) {
      this.enabledFeatures = FeatureFlags.DEFAULT_ENABLED_FEATURES;
      this.messagePacker = MessageChain.Packer.NONE;
      this.lastSeenMessagesCollector = new LastSeenMessagesCollector(20);
      this.signatureStorage = MessageSignatureStorage.create();
      this.client = client;
      this.loginScreen = screen;
      this.connection = connection;
      this.serverInfo = serverInfo;
      this.profile = profile;
      this.advancementHandler = new ClientAdvancementManager(client);
      this.commandSource = new ClientCommandSource(this, client);
      this.worldSession = worldSession;
   }

   public ClientCommandSource getCommandSource() {
      return this.commandSource;
   }

   public void clearWorld() {
      this.world = null;
      this.worldSession.onUnload();
   }

   public RecipeManager getRecipeManager() {
      return this.recipeManager;
   }

   public void onGameJoin(GameJoinS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.interactionManager = new ClientPlayerInteractionManager(this.client, this);
      this.combinedDynamicRegistries = this.combinedDynamicRegistries.with(ClientDynamicRegistryType.REMOTE, (DynamicRegistryManager.Immutable[])(packet.registryManager()));
      if (!this.connection.isLocal()) {
         this.combinedDynamicRegistries.getCombinedRegistryManager().streamAllRegistries().forEach((entry) -> {
            entry.value().clearTags();
         });
      }

      List list = Lists.newArrayList(packet.dimensionIds());
      Collections.shuffle(list);
      this.worldKeys = Sets.newLinkedHashSet(list);
      RegistryKey lv = packet.dimensionId();
      RegistryEntry lv2 = this.combinedDynamicRegistries.getCombinedRegistryManager().get(RegistryKeys.DIMENSION_TYPE).entryOf(packet.dimensionType());
      this.chunkLoadDistance = packet.viewDistance();
      this.simulationDistance = packet.simulationDistance();
      boolean bl = packet.debugWorld();
      boolean bl2 = packet.flatWorld();
      ClientWorld.Properties lv3 = new ClientWorld.Properties(Difficulty.NORMAL, packet.hardcore(), bl2);
      this.worldProperties = lv3;
      int var10007 = this.chunkLoadDistance;
      int var10008 = this.simulationDistance;
      MinecraftClient var10009 = this.client;
      Objects.requireNonNull(var10009);
      this.world = new ClientWorld(this, lv3, lv, lv2, var10007, var10008, var10009::getProfiler, this.client.worldRenderer, bl, packet.sha256Seed());
      this.client.joinWorld(this.world);
      if (this.client.player == null) {
         this.client.player = this.client.interactionManager.createPlayer(this.world, new StatHandler(), new ClientRecipeBook());
         this.client.player.setYaw(-180.0F);
         if (this.client.getServer() != null) {
            this.client.getServer().setLocalPlayerUuid(this.client.player.getUuid());
         }
      }

      this.client.debugRenderer.reset();
      this.client.player.init();
      int i = packet.playerEntityId();
      this.client.player.setId(i);
      this.world.addPlayer(i, this.client.player);
      this.client.player.input = new KeyboardInput(this.client.options);
      this.client.interactionManager.copyAbilities(this.client.player);
      this.client.cameraEntity = this.client.player;
      this.client.setScreen(new DownloadingTerrainScreen());
      this.client.player.setReducedDebugInfo(packet.reducedDebugInfo());
      this.client.player.setShowsDeathScreen(packet.showDeathScreen());
      this.client.player.setLastDeathPos(packet.lastDeathLocation());
      this.client.interactionManager.setGameModes(packet.gameMode(), packet.previousGameMode());
      this.client.options.setServerViewDistance(packet.viewDistance());
      this.client.options.sendClientSettings();
      this.connection.send(new CustomPayloadC2SPacket(CustomPayloadC2SPacket.BRAND, (new PacketByteBuf(Unpooled.buffer())).writeString(ClientBrandRetriever.getClientModName())));
      this.session = null;
      this.lastSeenMessagesCollector = new LastSeenMessagesCollector(20);
      this.signatureStorage = MessageSignatureStorage.create();
      if (this.connection.isEncrypted()) {
         this.client.getProfileKeys().fetchKeyPair().thenAcceptAsync((keyPair) -> {
            keyPair.ifPresent(this::updateKeyPair);
         }, this.client);
      }

      this.worldSession.setGameMode(packet.gameMode(), packet.hardcore());
      this.client.getQuickPlayLogger().save(this.client);
   }

   public void onEntitySpawn(EntitySpawnS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      EntityType lv = packet.getEntityType();
      Entity lv2 = lv.create(this.world);
      if (lv2 != null) {
         lv2.onSpawnPacket(packet);
         int i = packet.getId();
         this.world.addEntity(i, lv2);
         this.playSpawnSound(lv2);
      } else {
         LOGGER.warn("Skipping Entity with id {}", lv);
      }

   }

   private void playSpawnSound(Entity entity) {
      if (entity instanceof AbstractMinecartEntity) {
         this.client.getSoundManager().play(new MovingMinecartSoundInstance((AbstractMinecartEntity)entity));
      } else if (entity instanceof BeeEntity) {
         boolean bl = ((BeeEntity)entity).hasAngerTime();
         Object lv;
         if (bl) {
            lv = new AggressiveBeeSoundInstance((BeeEntity)entity);
         } else {
            lv = new PassiveBeeSoundInstance((BeeEntity)entity);
         }

         this.client.getSoundManager().playNextTick((TickableSoundInstance)lv);
      }

   }

   public void onExperienceOrbSpawn(ExperienceOrbSpawnS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      double d = packet.getX();
      double e = packet.getY();
      double f = packet.getZ();
      Entity lv = new ExperienceOrbEntity(this.world, d, e, f, packet.getExperience());
      lv.updateTrackedPosition(d, e, f);
      lv.setYaw(0.0F);
      lv.setPitch(0.0F);
      lv.setId(packet.getId());
      this.world.addEntity(packet.getId(), lv);
   }

   public void onEntityVelocityUpdate(EntityVelocityUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = this.world.getEntityById(packet.getId());
      if (lv != null) {
         lv.setVelocityClient((double)packet.getVelocityX() / 8000.0, (double)packet.getVelocityY() / 8000.0, (double)packet.getVelocityZ() / 8000.0);
      }
   }

   public void onEntityTrackerUpdate(EntityTrackerUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = this.world.getEntityById(packet.id());
      if (lv != null) {
         lv.getDataTracker().writeUpdatedEntries(packet.trackedValues());
      }

   }

   public void onPlayerSpawn(PlayerSpawnS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      PlayerListEntry lv = this.getPlayerListEntry(packet.getPlayerUuid());
      if (lv == null) {
         LOGGER.warn("Server attempted to add player prior to sending player info (Player id {})", packet.getPlayerUuid());
      } else {
         double d = packet.getX();
         double e = packet.getY();
         double f = packet.getZ();
         float g = (float)(packet.getYaw() * 360) / 256.0F;
         float h = (float)(packet.getPitch() * 360) / 256.0F;
         int i = packet.getId();
         OtherClientPlayerEntity lv2 = new OtherClientPlayerEntity(this.client.world, lv.getProfile());
         lv2.setId(i);
         lv2.updateTrackedPosition(d, e, f);
         lv2.updatePositionAndAngles(d, e, f, g, h);
         lv2.resetPosition();
         this.world.addPlayer(i, lv2);
      }
   }

   public void onEntityPosition(EntityPositionS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = this.world.getEntityById(packet.getId());
      if (lv != null) {
         double d = packet.getX();
         double e = packet.getY();
         double f = packet.getZ();
         lv.updateTrackedPosition(d, e, f);
         if (!lv.isLogicalSideForUpdatingMovement()) {
            float g = (float)(packet.getYaw() * 360) / 256.0F;
            float h = (float)(packet.getPitch() * 360) / 256.0F;
            lv.updateTrackedPositionAndAngles(d, e, f, g, h, 3, true);
            lv.setOnGround(packet.isOnGround());
         }

      }
   }

   public void onUpdateSelectedSlot(UpdateSelectedSlotS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      if (PlayerInventory.isValidHotbarIndex(packet.getSlot())) {
         this.client.player.getInventory().selectedSlot = packet.getSlot();
      }

   }

   public void onEntity(EntityS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = packet.getEntity(this.world);
      if (lv != null) {
         if (!lv.isLogicalSideForUpdatingMovement()) {
            if (packet.isPositionChanged()) {
               TrackedPosition lv2 = lv.getTrackedPosition();
               Vec3d lv3 = lv2.withDelta((long)packet.getDeltaX(), (long)packet.getDeltaY(), (long)packet.getDeltaZ());
               lv2.setPos(lv3);
               float f = packet.hasRotation() ? (float)(packet.getYaw() * 360) / 256.0F : lv.getYaw();
               float g = packet.hasRotation() ? (float)(packet.getPitch() * 360) / 256.0F : lv.getPitch();
               lv.updateTrackedPositionAndAngles(lv3.getX(), lv3.getY(), lv3.getZ(), f, g, 3, false);
            } else if (packet.hasRotation()) {
               float h = (float)(packet.getYaw() * 360) / 256.0F;
               float i = (float)(packet.getPitch() * 360) / 256.0F;
               lv.updateTrackedPositionAndAngles(lv.getX(), lv.getY(), lv.getZ(), h, i, 3, false);
            }

            lv.setOnGround(packet.isOnGround());
         }

      }
   }

   public void onEntitySetHeadYaw(EntitySetHeadYawS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = packet.getEntity(this.world);
      if (lv != null) {
         float f = (float)(packet.getHeadYaw() * 360) / 256.0F;
         lv.updateTrackedHeadRotation(f, 3);
      }
   }

   public void onEntitiesDestroy(EntitiesDestroyS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      packet.getEntityIds().forEach((entityId) -> {
         this.world.removeEntity(entityId, Entity.RemovalReason.DISCARDED);
      });
   }

   public void onPlayerPositionLook(PlayerPositionLookS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      PlayerEntity lv = this.client.player;
      Vec3d lv2 = lv.getVelocity();
      boolean bl = packet.getFlags().contains(PositionFlag.X);
      boolean bl2 = packet.getFlags().contains(PositionFlag.Y);
      boolean bl3 = packet.getFlags().contains(PositionFlag.Z);
      double d;
      double e;
      if (bl) {
         d = lv2.getX();
         e = lv.getX() + packet.getX();
         lv.lastRenderX += packet.getX();
         lv.prevX += packet.getX();
      } else {
         d = 0.0;
         e = packet.getX();
         lv.lastRenderX = e;
         lv.prevX = e;
      }

      double f;
      double g;
      if (bl2) {
         f = lv2.getY();
         g = lv.getY() + packet.getY();
         lv.lastRenderY += packet.getY();
         lv.prevY += packet.getY();
      } else {
         f = 0.0;
         g = packet.getY();
         lv.lastRenderY = g;
         lv.prevY = g;
      }

      double h;
      double i;
      if (bl3) {
         h = lv2.getZ();
         i = lv.getZ() + packet.getZ();
         lv.lastRenderZ += packet.getZ();
         lv.prevZ += packet.getZ();
      } else {
         h = 0.0;
         i = packet.getZ();
         lv.lastRenderZ = i;
         lv.prevZ = i;
      }

      lv.setPosition(e, g, i);
      lv.setVelocity(d, f, h);
      float j = packet.getYaw();
      float k = packet.getPitch();
      if (packet.getFlags().contains(PositionFlag.X_ROT)) {
         lv.setPitch(lv.getPitch() + k);
         lv.prevPitch += k;
      } else {
         lv.setPitch(k);
         lv.prevPitch = k;
      }

      if (packet.getFlags().contains(PositionFlag.Y_ROT)) {
         lv.setYaw(lv.getYaw() + j);
         lv.prevYaw += j;
      } else {
         lv.setYaw(j);
         lv.prevYaw = j;
      }

      this.connection.send(new TeleportConfirmC2SPacket(packet.getTeleportId()));
      this.connection.send(new PlayerMoveC2SPacket.Full(lv.getX(), lv.getY(), lv.getZ(), lv.getYaw(), lv.getPitch(), false));
   }

   public void onChunkDeltaUpdate(ChunkDeltaUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      int i = Block.NOTIFY_ALL | Block.FORCE_STATE | (packet.shouldSkipLightingUpdates() ? Block.SKIP_LIGHTING_UPDATES : 0);
      packet.visitUpdates((pos, state) -> {
         this.world.handleBlockUpdate(pos, state, i);
      });
   }

   public void onChunkData(ChunkDataS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.loadChunk(packet.getX(), packet.getZ(), packet.getChunkData());
      this.updateChunk(packet.getX(), packet.getZ(), packet.getLightData());
   }

   public void onChunkBiomeData(ChunkBiomeDataS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Iterator var2 = packet.chunkBiomeData().iterator();

      ChunkBiomeDataS2CPacket.Serialized lv;
      while(var2.hasNext()) {
         lv = (ChunkBiomeDataS2CPacket.Serialized)var2.next();
         this.world.getChunkManager().onChunkBiomeData(lv.pos().x, lv.pos().z, lv.toReadingBuf());
      }

      var2 = packet.chunkBiomeData().iterator();

      while(var2.hasNext()) {
         lv = (ChunkBiomeDataS2CPacket.Serialized)var2.next();
         this.world.resetChunkColor(new ChunkPos(lv.pos().x, lv.pos().z));
      }

      var2 = packet.chunkBiomeData().iterator();

      while(var2.hasNext()) {
         lv = (ChunkBiomeDataS2CPacket.Serialized)var2.next();

         for(int i = -1; i <= 1; ++i) {
            for(int j = -1; j <= 1; ++j) {
               for(int k = this.world.getBottomSectionCoord(); k < this.world.getTopSectionCoord(); ++k) {
                  this.client.worldRenderer.scheduleBlockRender(lv.pos().x + i, k, lv.pos().z + j);
               }
            }
         }
      }

   }

   private void loadChunk(int x, int z, ChunkData chunkData) {
      this.world.getChunkManager().loadChunkFromPacket(x, z, chunkData.getSectionsDataBuf(), chunkData.getHeightmap(), chunkData.getBlockEntities(x, z));
   }

   private void updateChunk(int x, int z, LightData lightData) {
      this.world.enqueueChunkUpdate(() -> {
         this.readLightData(x, z, lightData);
         WorldChunk lv = this.world.getChunkManager().getWorldChunk(x, z, false);
         if (lv != null) {
            this.scheduleRenderChunk(lv, x, z);
         }

      });
   }

   private void scheduleRenderChunk(WorldChunk chunk, int x, int z) {
      LightingProvider lv = this.world.getChunkManager().getLightingProvider();
      ChunkSection[] lvs = chunk.getSectionArray();
      ChunkPos lv2 = chunk.getPos();
      lv.setColumnEnabled(lv2, true);

      for(int k = 0; k < lvs.length; ++k) {
         ChunkSection lv3 = lvs[k];
         int l = this.world.sectionIndexToCoord(k);
         lv.setSectionStatus(ChunkSectionPos.from(lv2, l), lv3.isEmpty());
         this.world.scheduleBlockRenders(x, l, z);
      }

      this.world.markChunkRenderability(x, z);
   }

   public void onUnloadChunk(UnloadChunkS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      int i = packet.getX();
      int j = packet.getZ();
      ClientChunkManager lv = this.world.getChunkManager();
      lv.unload(i, j);
      this.unloadChunk(packet);
   }

   private void unloadChunk(UnloadChunkS2CPacket packet) {
      this.world.enqueueChunkUpdate(() -> {
         LightingProvider lv = this.world.getLightingProvider();

         for(int i = this.world.getBottomSectionCoord(); i < this.world.getTopSectionCoord(); ++i) {
            lv.setSectionStatus(ChunkSectionPos.from(packet.getX(), i, packet.getZ()), true);
         }

         lv.setColumnEnabled(new ChunkPos(packet.getX(), packet.getZ()), false);
         this.world.markChunkRenderability(packet.getX(), packet.getZ());
      });
   }

   public void onBlockUpdate(BlockUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.world.handleBlockUpdate(packet.getPos(), packet.getState(), 19);
   }

   public void onDisconnect(DisconnectS2CPacket packet) {
      this.connection.disconnect(packet.getReason());
   }

   public void onDisconnected(Text reason) {
      this.client.disconnect();
      this.worldSession.onUnload();
      if (this.loginScreen != null) {
         if (this.loginScreen instanceof RealmsScreen) {
            this.client.setScreen(new DisconnectedRealmsScreen(this.loginScreen, DISCONNECT_LOST_TEXT, reason));
         } else {
            this.client.setScreen(new DisconnectedScreen(this.loginScreen, DISCONNECT_LOST_TEXT, reason));
         }
      } else {
         this.client.setScreen(new DisconnectedScreen(new MultiplayerScreen(new TitleScreen()), DISCONNECT_LOST_TEXT, reason));
      }

   }

   public void sendPacket(Packet packet) {
      this.connection.send(packet);
   }

   public void onItemPickupAnimation(ItemPickupAnimationS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = this.world.getEntityById(packet.getEntityId());
      LivingEntity lv2 = (LivingEntity)this.world.getEntityById(packet.getCollectorEntityId());
      if (lv2 == null) {
         lv2 = this.client.player;
      }

      if (lv != null) {
         if (lv instanceof ExperienceOrbEntity) {
            this.world.playSound(lv.getX(), lv.getY(), lv.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.1F, (this.random.nextFloat() - this.random.nextFloat()) * 0.35F + 0.9F, false);
         } else {
            this.world.playSound(lv.getX(), lv.getY(), lv.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, (this.random.nextFloat() - this.random.nextFloat()) * 1.4F + 2.0F, false);
         }

         this.client.particleManager.addParticle(new ItemPickupParticle(this.client.getEntityRenderDispatcher(), this.client.getBufferBuilders(), this.world, lv, (Entity)lv2));
         if (lv instanceof ItemEntity) {
            ItemEntity lv3 = (ItemEntity)lv;
            ItemStack lv4 = lv3.getStack();
            if (!lv4.isEmpty()) {
               lv4.decrement(packet.getStackAmount());
            }

            if (lv4.isEmpty()) {
               this.world.removeEntity(packet.getEntityId(), Entity.RemovalReason.DISCARDED);
            }
         } else if (!(lv instanceof ExperienceOrbEntity)) {
            this.world.removeEntity(packet.getEntityId(), Entity.RemovalReason.DISCARDED);
         }
      }

   }

   public void onGameMessage(GameMessageS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.getMessageHandler().onGameMessage(packet.content(), packet.overlay());
   }

   public void onChatMessage(ChatMessageS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Optional optional = packet.body().toBody(this.signatureStorage);
      Optional optional2 = packet.serializedParameters().toParameters(this.combinedDynamicRegistries.getCombinedRegistryManager());
      if (!optional.isEmpty() && !optional2.isEmpty()) {
         UUID uUID = packet.sender();
         PlayerListEntry lv = this.getPlayerListEntry(uUID);
         if (lv == null) {
            this.connection.disconnect(CHAT_VALIDATION_FAILED_TEXT);
         } else {
            PublicPlayerSession lv2 = lv.getSession();
            MessageLink lv3;
            if (lv2 != null) {
               lv3 = new MessageLink(packet.index(), uUID, lv2.sessionId());
            } else {
               lv3 = MessageLink.of(uUID);
            }

            SignedMessage lv4 = new SignedMessage(lv3, packet.signature(), (MessageBody)optional.get(), packet.unsignedContent(), packet.filterMask());
            if (!lv.getMessageVerifier().isVerified(lv4)) {
               this.connection.disconnect(CHAT_VALIDATION_FAILED_TEXT);
            } else {
               this.client.getMessageHandler().onChatMessage(lv4, lv.getProfile(), (MessageType.Parameters)optional2.get());
               this.signatureStorage.add(lv4);
            }
         }
      } else {
         this.connection.disconnect(INVALID_PACKET_TEXT);
      }
   }

   public void onProfilelessChatMessage(ProfilelessChatMessageS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Optional optional = packet.chatType().toParameters(this.combinedDynamicRegistries.getCombinedRegistryManager());
      if (optional.isEmpty()) {
         this.connection.disconnect(INVALID_PACKET_TEXT);
      } else {
         this.client.getMessageHandler().onProfilelessMessage(packet.message(), (MessageType.Parameters)optional.get());
      }
   }

   public void onRemoveMessage(RemoveMessageS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Optional optional = packet.messageSignature().getSignature(this.signatureStorage);
      if (optional.isEmpty()) {
         this.connection.disconnect(INVALID_PACKET_TEXT);
      } else {
         this.lastSeenMessagesCollector.remove((MessageSignatureData)optional.get());
         if (!this.client.getMessageHandler().removeDelayedMessage((MessageSignatureData)optional.get())) {
            this.client.inGameHud.getChatHud().removeMessage((MessageSignatureData)optional.get());
         }

      }
   }

   public void onEntityAnimation(EntityAnimationS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = this.world.getEntityById(packet.getId());
      if (lv != null) {
         LivingEntity lv2;
         if (packet.getAnimationId() == 0) {
            lv2 = (LivingEntity)lv;
            lv2.swingHand(Hand.MAIN_HAND);
         } else if (packet.getAnimationId() == EntityAnimationS2CPacket.SWING_OFF_HAND) {
            lv2 = (LivingEntity)lv;
            lv2.swingHand(Hand.OFF_HAND);
         } else if (packet.getAnimationId() == EntityAnimationS2CPacket.WAKE_UP) {
            PlayerEntity lv3 = (PlayerEntity)lv;
            lv3.wakeUp(false, false);
         } else if (packet.getAnimationId() == EntityAnimationS2CPacket.CRIT) {
            this.client.particleManager.addEmitter(lv, ParticleTypes.CRIT);
         } else if (packet.getAnimationId() == EntityAnimationS2CPacket.ENCHANTED_HIT) {
            this.client.particleManager.addEmitter(lv, ParticleTypes.ENCHANTED_HIT);
         }

      }
   }

   public void onDamageTilt(DamageTiltS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = this.world.getEntityById(packet.id());
      if (lv != null) {
         lv.animateDamage(packet.yaw());
      }
   }

   public void onWorldTimeUpdate(WorldTimeUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.world.setTime(packet.getTime());
      this.client.world.setTimeOfDay(packet.getTimeOfDay());
      this.worldSession.setTick(packet.getTime());
   }

   public void onPlayerSpawnPosition(PlayerSpawnPositionS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.world.setSpawnPos(packet.getPos(), packet.getAngle());
      Screen var3 = this.client.currentScreen;
      if (var3 instanceof DownloadingTerrainScreen lv) {
         lv.setReady();
      }

   }

   public void onEntityPassengersSet(EntityPassengersSetS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = this.world.getEntityById(packet.getId());
      if (lv == null) {
         LOGGER.warn("Received passengers for unknown entity");
      } else {
         boolean bl = lv.hasPassengerDeep(this.client.player);
         lv.removeAllPassengers();
         int[] var4 = packet.getPassengerIds();
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            int i = var4[var6];
            Entity lv2 = this.world.getEntityById(i);
            if (lv2 != null) {
               lv2.startRiding(lv, true);
               if (lv2 == this.client.player && !bl) {
                  if (lv instanceof BoatEntity) {
                     this.client.player.prevYaw = lv.getYaw();
                     this.client.player.setYaw(lv.getYaw());
                     this.client.player.setHeadYaw(lv.getYaw());
                  }

                  Text lv3 = Text.translatable("mount.onboard", this.client.options.sneakKey.getBoundKeyLocalizedText());
                  this.client.inGameHud.setOverlayMessage(lv3, false);
                  this.client.getNarratorManager().narrate((Text)lv3);
               }
            }
         }

      }
   }

   public void onEntityAttach(EntityAttachS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = this.world.getEntityById(packet.getAttachedEntityId());
      if (lv instanceof MobEntity) {
         ((MobEntity)lv).setHoldingEntityId(packet.getHoldingEntityId());
      }

   }

   private static ItemStack getActiveTotemOfUndying(PlayerEntity player) {
      Hand[] var1 = Hand.values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         Hand lv = var1[var3];
         ItemStack lv2 = player.getStackInHand(lv);
         if (lv2.isOf(Items.TOTEM_OF_UNDYING)) {
            return lv2;
         }
      }

      return new ItemStack(Items.TOTEM_OF_UNDYING);
   }

   public void onEntityStatus(EntityStatusS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = packet.getEntity(this.world);
      if (lv != null) {
         switch (packet.getStatus()) {
            case 21:
               this.client.getSoundManager().play(new GuardianAttackSoundInstance((GuardianEntity)lv));
               break;
            case 35:
               int i = true;
               this.client.particleManager.addEmitter(lv, ParticleTypes.TOTEM_OF_UNDYING, 30);
               this.world.playSound(lv.getX(), lv.getY(), lv.getZ(), SoundEvents.ITEM_TOTEM_USE, lv.getSoundCategory(), 1.0F, 1.0F, false);
               if (lv == this.client.player) {
                  this.client.gameRenderer.showFloatingItem(getActiveTotemOfUndying(this.client.player));
               }
               break;
            case 63:
               this.client.getSoundManager().play(new SnifferDigSoundInstance((SnifferEntity)lv));
               break;
            default:
               lv.handleStatus(packet.getStatus());
         }
      }

   }

   public void onEntityDamage(EntityDamageS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = this.world.getEntityById(packet.entityId());
      if (lv != null) {
         lv.onDamaged(packet.createDamageSource(this.world));
      }
   }

   public void onHealthUpdate(HealthUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.player.updateHealth(packet.getHealth());
      this.client.player.getHungerManager().setFoodLevel(packet.getFood());
      this.client.player.getHungerManager().setSaturationLevel(packet.getSaturation());
   }

   public void onExperienceBarUpdate(ExperienceBarUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.player.setExperience(packet.getBarProgress(), packet.getExperienceLevel(), packet.getExperience());
   }

   public void onPlayerRespawn(PlayerRespawnS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      RegistryKey lv = packet.getDimension();
      RegistryEntry lv2 = this.combinedDynamicRegistries.getCombinedRegistryManager().get(RegistryKeys.DIMENSION_TYPE).entryOf(packet.getDimensionType());
      ClientPlayerEntity lv3 = this.client.player;
      int i = lv3.getId();
      if (lv != lv3.world.getRegistryKey()) {
         Scoreboard lv4 = this.world.getScoreboard();
         Map map = this.world.getMapStates();
         boolean bl = packet.isDebugWorld();
         boolean bl2 = packet.isFlatWorld();
         ClientWorld.Properties lv5 = new ClientWorld.Properties(this.worldProperties.getDifficulty(), this.worldProperties.isHardcore(), bl2);
         this.worldProperties = lv5;
         int var10007 = this.chunkLoadDistance;
         int var10008 = this.simulationDistance;
         MinecraftClient var10009 = this.client;
         Objects.requireNonNull(var10009);
         this.world = new ClientWorld(this, lv5, lv, lv2, var10007, var10008, var10009::getProfiler, this.client.worldRenderer, bl, packet.getSha256Seed());
         this.world.setScoreboard(lv4);
         this.world.putMapStates(map);
         this.client.joinWorld(this.world);
         this.client.setScreen(new DownloadingTerrainScreen());
      }

      String string = lv3.getServerBrand();
      this.client.cameraEntity = null;
      if (lv3.shouldCloseHandledScreenOnRespawn()) {
         lv3.closeHandledScreen();
      }

      ClientPlayerEntity lv6 = this.client.interactionManager.createPlayer(this.world, lv3.getStatHandler(), lv3.getRecipeBook(), lv3.isSneaking(), lv3.isSprinting());
      lv6.setId(i);
      this.client.player = lv6;
      if (lv != lv3.world.getRegistryKey()) {
         this.client.getMusicTracker().stop();
      }

      this.client.cameraEntity = lv6;
      if (packet.hasFlag((byte)2)) {
         List list = lv3.getDataTracker().getChangedEntries();
         if (list != null) {
            lv6.getDataTracker().writeUpdatedEntries(list);
         }
      }

      if (packet.hasFlag((byte)1)) {
         lv6.getAttributes().setFrom(lv3.getAttributes());
      }

      lv6.init();
      lv6.setServerBrand(string);
      this.world.addPlayer(i, lv6);
      lv6.setYaw(-180.0F);
      lv6.input = new KeyboardInput(this.client.options);
      this.client.interactionManager.copyAbilities(lv6);
      lv6.setReducedDebugInfo(lv3.hasReducedDebugInfo());
      lv6.setShowsDeathScreen(lv3.showsDeathScreen());
      lv6.setLastDeathPos(packet.getLastDeathPos());
      if (this.client.currentScreen instanceof DeathScreen || this.client.currentScreen instanceof DeathScreen.TitleScreenConfirmScreen) {
         this.client.setScreen((Screen)null);
      }

      this.client.interactionManager.setGameModes(packet.getGameMode(), packet.getPreviousGameMode());
   }

   public void onExplosion(ExplosionS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Explosion lv = new Explosion(this.client.world, (Entity)null, packet.getX(), packet.getY(), packet.getZ(), packet.getRadius(), packet.getAffectedBlocks());
      lv.affectWorld(true);
      this.client.player.setVelocity(this.client.player.getVelocity().add((double)packet.getPlayerVelocityX(), (double)packet.getPlayerVelocityY(), (double)packet.getPlayerVelocityZ()));
   }

   public void onOpenHorseScreen(OpenHorseScreenS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = this.world.getEntityById(packet.getHorseId());
      if (lv instanceof AbstractHorseEntity) {
         ClientPlayerEntity lv2 = this.client.player;
         AbstractHorseEntity lv3 = (AbstractHorseEntity)lv;
         SimpleInventory lv4 = new SimpleInventory(packet.getSlotCount());
         HorseScreenHandler lv5 = new HorseScreenHandler(packet.getSyncId(), lv2.getInventory(), lv4, lv3);
         lv2.currentScreenHandler = lv5;
         this.client.setScreen(new HorseScreen(lv5, lv2.getInventory(), lv3));
      }

   }

   public void onOpenScreen(OpenScreenS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      HandledScreens.open(packet.getScreenHandlerType(), this.client, packet.getSyncId(), packet.getName());
   }

   public void onScreenHandlerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      PlayerEntity lv = this.client.player;
      ItemStack lv2 = packet.getItemStack();
      int i = packet.getSlot();
      this.client.getTutorialManager().onSlotUpdate(lv2);
      if (packet.getSyncId() == ScreenHandlerSlotUpdateS2CPacket.UPDATE_CURSOR_SYNC_ID) {
         if (!(this.client.currentScreen instanceof CreativeInventoryScreen)) {
            lv.currentScreenHandler.setCursorStack(lv2);
         }
      } else if (packet.getSyncId() == ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID) {
         lv.getInventory().setStack(i, lv2);
      } else {
         boolean bl = false;
         Screen var7 = this.client.currentScreen;
         if (var7 instanceof CreativeInventoryScreen) {
            CreativeInventoryScreen lv3 = (CreativeInventoryScreen)var7;
            bl = !lv3.isInventoryTabSelected();
         }

         if (packet.getSyncId() == 0 && PlayerScreenHandler.isInHotbar(i)) {
            if (!lv2.isEmpty()) {
               ItemStack lv4 = lv.playerScreenHandler.getSlot(i).getStack();
               if (lv4.isEmpty() || lv4.getCount() < lv2.getCount()) {
                  lv2.setBobbingAnimationTime(5);
               }
            }

            lv.playerScreenHandler.setStackInSlot(i, packet.getRevision(), lv2);
         } else if (packet.getSyncId() == lv.currentScreenHandler.syncId && (packet.getSyncId() != 0 || !bl)) {
            lv.currentScreenHandler.setStackInSlot(i, packet.getRevision(), lv2);
         }
      }

   }

   public void onInventory(InventoryS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      PlayerEntity lv = this.client.player;
      if (packet.getSyncId() == 0) {
         lv.playerScreenHandler.updateSlotStacks(packet.getRevision(), packet.getContents(), packet.getCursorStack());
      } else if (packet.getSyncId() == lv.currentScreenHandler.syncId) {
         lv.currentScreenHandler.updateSlotStacks(packet.getRevision(), packet.getContents(), packet.getCursorStack());
      }

   }

   public void onSignEditorOpen(SignEditorOpenS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      BlockPos lv = packet.getPos();
      BlockEntity var4 = this.world.getBlockEntity(lv);
      if (var4 instanceof SignBlockEntity lv2) {
         this.client.player.openEditSignScreen(lv2, packet.isFront());
      } else {
         BlockState lv3 = this.world.getBlockState(lv);
         SignBlockEntity lv4 = new SignBlockEntity(lv, lv3);
         lv4.setWorld(this.world);
         this.client.player.openEditSignScreen(lv4, packet.isFront());
      }

   }

   public void onBlockEntityUpdate(BlockEntityUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      BlockPos lv = packet.getPos();
      this.client.world.getBlockEntity(lv, packet.getBlockEntityType()).ifPresent((blockEntity) -> {
         NbtCompound lv = packet.getNbt();
         if (lv != null) {
            blockEntity.readNbt(lv);
         }

         if (blockEntity instanceof CommandBlockBlockEntity && this.client.currentScreen instanceof CommandBlockScreen) {
            ((CommandBlockScreen)this.client.currentScreen).updateCommandBlock();
         }

      });
   }

   public void onScreenHandlerPropertyUpdate(ScreenHandlerPropertyUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      PlayerEntity lv = this.client.player;
      if (lv.currentScreenHandler != null && lv.currentScreenHandler.syncId == packet.getSyncId()) {
         lv.currentScreenHandler.setProperty(packet.getPropertyId(), packet.getValue());
      }

   }

   public void onEntityEquipmentUpdate(EntityEquipmentUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = this.world.getEntityById(packet.getId());
      if (lv != null) {
         packet.getEquipmentList().forEach((pair) -> {
            lv.equipStack((EquipmentSlot)pair.getFirst(), (ItemStack)pair.getSecond());
         });
      }

   }

   public void onCloseScreen(CloseScreenS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.player.closeScreen();
   }

   public void onBlockEvent(BlockEventS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.world.addSyncedBlockEvent(packet.getPos(), packet.getBlock(), packet.getType(), packet.getData());
   }

   public void onBlockBreakingProgress(BlockBreakingProgressS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.world.setBlockBreakingInfo(packet.getEntityId(), packet.getPos(), packet.getProgress());
   }

   public void onGameStateChange(GameStateChangeS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      PlayerEntity lv = this.client.player;
      GameStateChangeS2CPacket.Reason lv2 = packet.getReason();
      float f = packet.getValue();
      int i = MathHelper.floor(f + 0.5F);
      if (lv2 == GameStateChangeS2CPacket.NO_RESPAWN_BLOCK) {
         lv.sendMessage(Text.translatable("block.minecraft.spawn.not_valid"), false);
      } else if (lv2 == GameStateChangeS2CPacket.RAIN_STARTED) {
         this.world.getLevelProperties().setRaining(true);
         this.world.setRainGradient(0.0F);
      } else if (lv2 == GameStateChangeS2CPacket.RAIN_STOPPED) {
         this.world.getLevelProperties().setRaining(false);
         this.world.setRainGradient(1.0F);
      } else if (lv2 == GameStateChangeS2CPacket.GAME_MODE_CHANGED) {
         this.client.interactionManager.setGameMode(GameMode.byId(i));
      } else if (lv2 == GameStateChangeS2CPacket.GAME_WON) {
         if (i == 0) {
            this.client.player.networkHandler.sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
            this.client.setScreen(new DownloadingTerrainScreen());
         } else if (i == 1) {
            this.client.setScreen(new CreditsScreen(true, () -> {
               this.client.player.networkHandler.sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
               this.client.setScreen((Screen)null);
            }));
         }
      } else if (lv2 == GameStateChangeS2CPacket.DEMO_MESSAGE_SHOWN) {
         GameOptions lv3 = this.client.options;
         if (f == GameStateChangeS2CPacket.field_33328) {
            this.client.setScreen(new DemoScreen());
         } else if (f == GameStateChangeS2CPacket.field_33329) {
            this.client.inGameHud.getChatHud().addMessage(Text.translatable("demo.help.movement", lv3.forwardKey.getBoundKeyLocalizedText(), lv3.leftKey.getBoundKeyLocalizedText(), lv3.backKey.getBoundKeyLocalizedText(), lv3.rightKey.getBoundKeyLocalizedText()));
         } else if (f == GameStateChangeS2CPacket.field_33330) {
            this.client.inGameHud.getChatHud().addMessage(Text.translatable("demo.help.jump", lv3.jumpKey.getBoundKeyLocalizedText()));
         } else if (f == GameStateChangeS2CPacket.field_33331) {
            this.client.inGameHud.getChatHud().addMessage(Text.translatable("demo.help.inventory", lv3.inventoryKey.getBoundKeyLocalizedText()));
         } else if (f == GameStateChangeS2CPacket.field_33332) {
            this.client.inGameHud.getChatHud().addMessage(Text.translatable("demo.day.6", lv3.screenshotKey.getBoundKeyLocalizedText()));
         }
      } else if (lv2 == GameStateChangeS2CPacket.PROJECTILE_HIT_PLAYER) {
         this.world.playSound(lv, lv.getX(), lv.getEyeY(), lv.getZ(), SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.18F, 0.45F);
      } else if (lv2 == GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED) {
         this.world.setRainGradient(f);
      } else if (lv2 == GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED) {
         this.world.setThunderGradient(f);
      } else if (lv2 == GameStateChangeS2CPacket.PUFFERFISH_STING) {
         this.world.playSound(lv, lv.getX(), lv.getY(), lv.getZ(), SoundEvents.ENTITY_PUFFER_FISH_STING, SoundCategory.NEUTRAL, 1.0F, 1.0F);
      } else if (lv2 == GameStateChangeS2CPacket.ELDER_GUARDIAN_EFFECT) {
         this.world.addParticle(ParticleTypes.ELDER_GUARDIAN, lv.getX(), lv.getY(), lv.getZ(), 0.0, 0.0, 0.0);
         if (i == 1) {
            this.world.playSound(lv, lv.getX(), lv.getY(), lv.getZ(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 1.0F, 1.0F);
         }
      } else if (lv2 == GameStateChangeS2CPacket.IMMEDIATE_RESPAWN) {
         this.client.player.setShowsDeathScreen(f == GameStateChangeS2CPacket.field_33328);
      }

   }

   public void onMapUpdate(MapUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      MapRenderer lv = this.client.gameRenderer.getMapRenderer();
      int i = packet.getId();
      String string = FilledMapItem.getMapName(i);
      MapState lv2 = this.client.world.getMapState(string);
      if (lv2 == null) {
         lv2 = MapState.of(packet.getScale(), packet.isLocked(), this.client.world.getRegistryKey());
         this.client.world.putClientsideMapState(string, lv2);
      }

      packet.apply(lv2);
      lv.updateTexture(i, lv2);
   }

   public void onWorldEvent(WorldEventS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      if (packet.isGlobal()) {
         this.client.world.syncGlobalEvent(packet.getEventId(), packet.getPos(), packet.getData());
      } else {
         this.client.world.syncWorldEvent(packet.getEventId(), packet.getPos(), packet.getData());
      }

   }

   public void onAdvancements(AdvancementUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.advancementHandler.onAdvancements(packet);
   }

   public void onSelectAdvancementTab(SelectAdvancementTabS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Identifier lv = packet.getTabId();
      if (lv == null) {
         this.advancementHandler.selectTab((Advancement)null, false);
      } else {
         Advancement lv2 = this.advancementHandler.getManager().get(lv);
         this.advancementHandler.selectTab(lv2, false);
      }

   }

   public void onCommandTree(CommandTreeS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.commandDispatcher = new CommandDispatcher(packet.getCommandTree(CommandRegistryAccess.of((RegistryWrapper.WrapperLookup)this.combinedDynamicRegistries.getCombinedRegistryManager(), this.enabledFeatures)));
   }

   public void onStopSound(StopSoundS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.getSoundManager().stopSounds(packet.getSoundId(), packet.getCategory());
   }

   public void onCommandSuggestions(CommandSuggestionsS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.commandSource.onCommandSuggestions(packet.getCompletionId(), packet.getSuggestions());
   }

   public void onSynchronizeRecipes(SynchronizeRecipesS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.recipeManager.setRecipes(packet.getRecipes());
      ClientRecipeBook lv = this.client.player.getRecipeBook();
      lv.reload(this.recipeManager.values(), this.client.world.getRegistryManager());
      this.client.reloadSearchProvider(SearchManager.RECIPE_OUTPUT, lv.getOrderedResults());
   }

   public void onLookAt(LookAtS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Vec3d lv = packet.getTargetPosition(this.world);
      if (lv != null) {
         this.client.player.lookAt(packet.getSelfAnchor(), lv);
      }

   }

   public void onNbtQueryResponse(NbtQueryResponseS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      if (!this.dataQueryHandler.handleQueryResponse(packet.getTransactionId(), packet.getNbt())) {
         LOGGER.debug("Got unhandled response to tag query {}", packet.getTransactionId());
      }

   }

   public void onStatistics(StatisticsS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Iterator var2 = packet.getStatMap().entrySet().iterator();

      while(var2.hasNext()) {
         Map.Entry entry = (Map.Entry)var2.next();
         Stat lv = (Stat)entry.getKey();
         int i = (Integer)entry.getValue();
         this.client.player.getStatHandler().setStat(this.client.player, lv, i);
      }

      if (this.client.currentScreen instanceof StatsListener) {
         ((StatsListener)this.client.currentScreen).onStatsReady();
      }

   }

   public void onUnlockRecipes(UnlockRecipesS2CPacket packet) {
      ClientRecipeBook lv;
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      lv = this.client.player.getRecipeBook();
      lv.setOptions(packet.getOptions());
      UnlockRecipesS2CPacket.Action lv2 = packet.getAction();
      Optional var10000;
      Iterator var4;
      Identifier lv3;
      label45:
      switch (lv2) {
         case REMOVE:
            var4 = packet.getRecipeIdsToChange().iterator();

            while(true) {
               if (!var4.hasNext()) {
                  break label45;
               }

               lv3 = (Identifier)var4.next();
               var10000 = this.recipeManager.get(lv3);
               Objects.requireNonNull(lv);
               var10000.ifPresent(lv::remove);
            }
         case INIT:
            var4 = packet.getRecipeIdsToChange().iterator();

            while(var4.hasNext()) {
               lv3 = (Identifier)var4.next();
               var10000 = this.recipeManager.get(lv3);
               Objects.requireNonNull(lv);
               var10000.ifPresent(lv::add);
            }

            var4 = packet.getRecipeIdsToInit().iterator();

            while(true) {
               if (!var4.hasNext()) {
                  break label45;
               }

               lv3 = (Identifier)var4.next();
               var10000 = this.recipeManager.get(lv3);
               Objects.requireNonNull(lv);
               var10000.ifPresent(lv::display);
            }
         case ADD:
            var4 = packet.getRecipeIdsToChange().iterator();

            while(var4.hasNext()) {
               lv3 = (Identifier)var4.next();
               this.recipeManager.get(lv3).ifPresent((recipe) -> {
                  lv.add(recipe);
                  lv.display(recipe);
                  if (recipe.showNotification()) {
                     RecipeToast.show(this.client.getToastManager(), recipe);
                  }

               });
            }
      }

      lv.getOrderedResults().forEach((recipeResultCollection) -> {
         recipeResultCollection.initialize(lv);
      });
      if (this.client.currentScreen instanceof RecipeBookProvider) {
         ((RecipeBookProvider)this.client.currentScreen).refreshRecipeBook();
      }

   }

   public void onEntityStatusEffect(EntityStatusEffectS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = this.world.getEntityById(packet.getEntityId());
      if (lv instanceof LivingEntity) {
         StatusEffect lv2 = packet.getEffectId();
         if (lv2 != null) {
            StatusEffectInstance lv3 = new StatusEffectInstance(lv2, packet.getDuration(), packet.getAmplifier(), packet.isAmbient(), packet.shouldShowParticles(), packet.shouldShowIcon(), (StatusEffectInstance)null, Optional.ofNullable(packet.getFactorCalculationData()));
            ((LivingEntity)lv).setStatusEffect(lv3, (Entity)null);
         }
      }
   }

   public void onSynchronizeTags(SynchronizeTagsS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      packet.getGroups().forEach(this::loadTags);
      if (!this.connection.isLocal()) {
         Blocks.refreshShapeCache();
      }

      ItemGroups.getSearchGroup().reloadSearchProvider();
   }

   public void onFeatures(FeaturesS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.enabledFeatures = FeatureFlags.FEATURE_MANAGER.featureSetOf((Iterable)packet.features());
   }

   private void loadTags(RegistryKey registryKey, TagPacketSerializer.Serialized serialized) {
      if (!serialized.isEmpty()) {
         Registry lv = (Registry)this.combinedDynamicRegistries.getCombinedRegistryManager().getOptional(registryKey).orElseThrow(() -> {
            return new IllegalStateException("Unknown registry " + registryKey);
         });
         Map map = new HashMap();
         Objects.requireNonNull(map);
         TagPacketSerializer.loadTags(registryKey, lv, serialized, map::put);
         lv.populateTags(map);
      }
   }

   public void onEndCombat(EndCombatS2CPacket packet) {
   }

   public void onEnterCombat(EnterCombatS2CPacket packet) {
   }

   public void onDeathMessage(DeathMessageS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = this.world.getEntityById(packet.getEntityId());
      if (lv == this.client.player) {
         if (this.client.player.showsDeathScreen()) {
            this.client.setScreen(new DeathScreen(packet.getMessage(), this.world.getLevelProperties().isHardcore()));
         } else {
            this.client.player.requestRespawn();
         }
      }

   }

   public void onDifficulty(DifficultyS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.worldProperties.setDifficulty(packet.getDifficulty());
      this.worldProperties.setDifficultyLocked(packet.isDifficultyLocked());
   }

   public void onSetCameraEntity(SetCameraEntityS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = packet.getEntity(this.world);
      if (lv != null) {
         this.client.setCameraEntity(lv);
      }

   }

   public void onWorldBorderInitialize(WorldBorderInitializeS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      WorldBorder lv = this.world.getWorldBorder();
      lv.setCenter(packet.getCenterX(), packet.getCenterZ());
      long l = packet.getSizeLerpTime();
      if (l > 0L) {
         lv.interpolateSize(packet.getSize(), packet.getSizeLerpTarget(), l);
      } else {
         lv.setSize(packet.getSizeLerpTarget());
      }

      lv.setMaxRadius(packet.getMaxRadius());
      lv.setWarningBlocks(packet.getWarningBlocks());
      lv.setWarningTime(packet.getWarningTime());
   }

   public void onWorldBorderCenterChanged(WorldBorderCenterChangedS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.world.getWorldBorder().setCenter(packet.getCenterX(), packet.getCenterZ());
   }

   public void onWorldBorderInterpolateSize(WorldBorderInterpolateSizeS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.world.getWorldBorder().interpolateSize(packet.getSize(), packet.getSizeLerpTarget(), packet.getSizeLerpTime());
   }

   public void onWorldBorderSizeChanged(WorldBorderSizeChangedS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.world.getWorldBorder().setSize(packet.getSizeLerpTarget());
   }

   public void onWorldBorderWarningBlocksChanged(WorldBorderWarningBlocksChangedS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.world.getWorldBorder().setWarningBlocks(packet.getWarningBlocks());
   }

   public void onWorldBorderWarningTimeChanged(WorldBorderWarningTimeChangedS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.world.getWorldBorder().setWarningTime(packet.getWarningTime());
   }

   public void onTitleClear(ClearTitleS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.inGameHud.clearTitle();
      if (packet.shouldReset()) {
         this.client.inGameHud.setDefaultTitleFade();
      }

   }

   public void onServerMetadata(ServerMetadataS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      if (this.serverInfo != null) {
         this.serverInfo.label = packet.getDescription();
         Optional var10000 = packet.getFavicon();
         ServerInfo var10001 = this.serverInfo;
         Objects.requireNonNull(var10001);
         var10000.ifPresent(var10001::setFavicon);
         this.serverInfo.setSecureChatEnforced(packet.isSecureChatEnforced());
         ServerList.updateServerListEntry(this.serverInfo);
         if (!packet.isSecureChatEnforced()) {
            SystemToast lv = SystemToast.create(this.client, SystemToast.Type.UNSECURE_SERVER_WARNING, UNSECURE_SERVER_TOAST_TITLE, UNSECURE_SERVER_TOAST_TEXT);
            this.client.getToastManager().add(lv);
         }

      }
   }

   public void onChatSuggestions(ChatSuggestionsS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.commandSource.onChatSuggestions(packet.action(), packet.entries());
   }

   public void onOverlayMessage(OverlayMessageS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.inGameHud.setOverlayMessage(packet.getMessage(), false);
   }

   public void onTitle(TitleS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.inGameHud.setTitle(packet.getTitle());
   }

   public void onSubtitle(SubtitleS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.inGameHud.setSubtitle(packet.getSubtitle());
   }

   public void onTitleFade(TitleFadeS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.inGameHud.setTitleTicks(packet.getFadeInTicks(), packet.getStayTicks(), packet.getFadeOutTicks());
   }

   public void onPlayerListHeader(PlayerListHeaderS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.inGameHud.getPlayerListHud().setHeader(packet.getHeader().getString().isEmpty() ? null : packet.getHeader());
      this.client.inGameHud.getPlayerListHud().setFooter(packet.getFooter().getString().isEmpty() ? null : packet.getFooter());
   }

   public void onRemoveEntityStatusEffect(RemoveEntityStatusEffectS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = packet.getEntity(this.world);
      if (lv instanceof LivingEntity) {
         ((LivingEntity)lv).removeStatusEffectInternal(packet.getEffectType());
      }

   }

   public void onPlayerRemove(PlayerRemoveS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Iterator var2 = packet.profileIds().iterator();

      while(var2.hasNext()) {
         UUID uUID = (UUID)var2.next();
         this.client.getSocialInteractionsManager().setPlayerOffline(uUID);
         PlayerListEntry lv = (PlayerListEntry)this.playerListEntries.remove(uUID);
         if (lv != null) {
            this.listedPlayerListEntries.remove(lv);
         }
      }

   }

   public void onPlayerList(PlayerListS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Iterator var2 = packet.getPlayerAdditionEntries().iterator();

      PlayerListS2CPacket.Entry lv;
      PlayerListEntry lv2;
      while(var2.hasNext()) {
         lv = (PlayerListS2CPacket.Entry)var2.next();
         lv2 = new PlayerListEntry(lv.profile(), this.isSecureChatEnforced());
         if (this.playerListEntries.putIfAbsent(lv.profileId(), lv2) == null) {
            this.client.getSocialInteractionsManager().setPlayerOnline(lv2);
         }
      }

      var2 = packet.getEntries().iterator();

      while(true) {
         while(var2.hasNext()) {
            lv = (PlayerListS2CPacket.Entry)var2.next();
            lv2 = (PlayerListEntry)this.playerListEntries.get(lv.profileId());
            if (lv2 == null) {
               LOGGER.warn("Ignoring player info update for unknown player {}", lv.profileId());
            } else {
               Iterator var5 = packet.getActions().iterator();

               while(var5.hasNext()) {
                  PlayerListS2CPacket.Action lv3 = (PlayerListS2CPacket.Action)var5.next();
                  this.handlePlayerListAction(lv3, lv, lv2);
               }
            }
         }

         return;
      }
   }

   private void handlePlayerListAction(PlayerListS2CPacket.Action action, PlayerListS2CPacket.Entry receivedEntry, PlayerListEntry currentEntry) {
      switch (action) {
         case INITIALIZE_CHAT:
            this.setPublicSession(receivedEntry, currentEntry);
            break;
         case UPDATE_GAME_MODE:
            currentEntry.setGameMode(receivedEntry.gameMode());
            break;
         case UPDATE_LISTED:
            if (receivedEntry.listed()) {
               this.listedPlayerListEntries.add(currentEntry);
            } else {
               this.listedPlayerListEntries.remove(currentEntry);
            }
            break;
         case UPDATE_LATENCY:
            currentEntry.setLatency(receivedEntry.latency());
            break;
         case UPDATE_DISPLAY_NAME:
            currentEntry.setDisplayName(receivedEntry.displayName());
      }

   }

   private void setPublicSession(PlayerListS2CPacket.Entry receivedEntry, PlayerListEntry currentEntry) {
      GameProfile gameProfile = currentEntry.getProfile();
      PublicPlayerSession.Serialized lv = receivedEntry.chatSession();
      if (lv != null) {
         try {
            PublicPlayerSession lv2 = lv.toSession(gameProfile, this.client.getServicesSignatureVerifier(), PlayerPublicKey.EXPIRATION_GRACE_PERIOD);
            currentEntry.setSession(lv2);
         } catch (PlayerPublicKey.PublicKeyException var6) {
            LOGGER.error("Failed to validate profile key for player: '{}'", gameProfile.getName(), var6);
            this.connection.disconnect(var6.getMessageText());
         }
      } else {
         currentEntry.resetSession(this.isSecureChatEnforced());
      }

   }

   private boolean isSecureChatEnforced() {
      return this.serverInfo != null && this.serverInfo.isSecureChatEnforced();
   }

   public void onKeepAlive(KeepAliveS2CPacket packet) {
      this.sendPacket(new KeepAliveC2SPacket(packet.getId()), () -> {
         return !RenderSystem.isFrozenAtPollEvents();
      }, Duration.ofMinutes(1L));
   }

   private void sendPacket(Packet packet, BooleanSupplier sendCondition, Duration expirationTime) {
      if (sendCondition.getAsBoolean()) {
         this.sendPacket(packet);
      } else {
         this.queuedPackets.add(new QueuedPacket(packet, sendCondition, Util.getMeasuringTimeMs() + expirationTime.toMillis()));
      }

   }

   private void tickQueuedPackets() {
      Iterator iterator = this.queuedPackets.iterator();

      while(iterator.hasNext()) {
         QueuedPacket lv = (QueuedPacket)iterator.next();
         if (lv.sendCondition().getAsBoolean()) {
            this.sendPacket(lv.packet);
            iterator.remove();
         } else if (lv.expirationTime() <= Util.getMeasuringTimeMs()) {
            iterator.remove();
         }
      }

   }

   public void onPlayerAbilities(PlayerAbilitiesS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      PlayerEntity lv = this.client.player;
      lv.getAbilities().flying = packet.isFlying();
      lv.getAbilities().creativeMode = packet.isCreativeMode();
      lv.getAbilities().invulnerable = packet.isInvulnerable();
      lv.getAbilities().allowFlying = packet.allowFlying();
      lv.getAbilities().setFlySpeed(packet.getFlySpeed());
      lv.getAbilities().setWalkSpeed(packet.getWalkSpeed());
   }

   public void onPlaySound(PlaySoundS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.world.playSound(this.client.player, packet.getX(), packet.getY(), packet.getZ(), packet.getSound(), packet.getCategory(), packet.getVolume(), packet.getPitch(), packet.getSeed());
   }

   public void onPlaySoundFromEntity(PlaySoundFromEntityS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = this.world.getEntityById(packet.getEntityId());
      if (lv != null) {
         this.client.world.playSoundFromEntity(this.client.player, lv, packet.getSound(), packet.getCategory(), packet.getVolume(), packet.getPitch(), packet.getSeed());
      }
   }

   public void onResourcePackSend(ResourcePackSendS2CPacket packet) {
      URL uRL = resolveUrl(packet.getURL());
      if (uRL == null) {
         this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.FAILED_DOWNLOAD);
      } else {
         String string = packet.getSHA1();
         boolean bl = packet.isRequired();
         if (this.serverInfo != null && this.serverInfo.getResourcePackPolicy() == ServerInfo.ResourcePackPolicy.ENABLED) {
            this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.ACCEPTED);
            this.feedbackAfterDownload(this.client.getServerResourcePackProvider().download(uRL, string, true));
         } else if (this.serverInfo == null || this.serverInfo.getResourcePackPolicy() == ServerInfo.ResourcePackPolicy.PROMPT || bl && this.serverInfo.getResourcePackPolicy() == ServerInfo.ResourcePackPolicy.DISABLED) {
            this.client.execute(() -> {
               this.client.setScreen(new ConfirmScreen((enabled) -> {
                  this.client.setScreen((Screen)null);
                  if (enabled) {
                     if (this.serverInfo != null) {
                        this.serverInfo.setResourcePackPolicy(ServerInfo.ResourcePackPolicy.ENABLED);
                     }

                     this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.ACCEPTED);
                     this.feedbackAfterDownload(this.client.getServerResourcePackProvider().download(uRL, string, true));
                  } else {
                     this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.DECLINED);
                     if (bl) {
                        this.connection.disconnect(Text.translatable("multiplayer.requiredTexturePrompt.disconnect"));
                     } else if (this.serverInfo != null) {
                        this.serverInfo.setResourcePackPolicy(ServerInfo.ResourcePackPolicy.DISABLED);
                     }
                  }

                  if (this.serverInfo != null) {
                     ServerList.updateServerListEntry(this.serverInfo);
                  }

               }, bl ? Text.translatable("multiplayer.requiredTexturePrompt.line1") : Text.translatable("multiplayer.texturePrompt.line1"), getServerResourcePackPrompt(bl ? Text.translatable("multiplayer.requiredTexturePrompt.line2").formatted(Formatting.YELLOW, Formatting.BOLD) : Text.translatable("multiplayer.texturePrompt.line2"), packet.getPrompt()), bl ? ScreenTexts.PROCEED : ScreenTexts.YES, (Text)(bl ? Text.translatable("menu.disconnect") : ScreenTexts.NO)));
            });
         } else {
            this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.DECLINED);
            if (bl) {
               this.connection.disconnect(Text.translatable("multiplayer.requiredTexturePrompt.disconnect"));
            }
         }

      }
   }

   private static Text getServerResourcePackPrompt(Text defaultPrompt, @Nullable Text customPrompt) {
      return (Text)(customPrompt == null ? defaultPrompt : Text.translatable("multiplayer.texturePrompt.serverPrompt", defaultPrompt, customPrompt));
   }

   @Nullable
   private static URL resolveUrl(String url) {
      try {
         URL uRL = new URL(url);
         String string2 = uRL.getProtocol();
         return !"http".equals(string2) && !"https".equals(string2) ? null : uRL;
      } catch (MalformedURLException var3) {
         return null;
      }
   }

   private void feedbackAfterDownload(CompletableFuture downloadFuture) {
      downloadFuture.thenRun(() -> {
         this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED);
      }).exceptionally((throwable) -> {
         this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.FAILED_DOWNLOAD);
         return null;
      });
   }

   private void sendResourcePackStatus(ResourcePackStatusC2SPacket.Status packStatus) {
      this.connection.send(new ResourcePackStatusC2SPacket(packStatus));
   }

   public void onBossBar(BossBarS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.client.inGameHud.getBossBarHud().handlePacket(packet);
   }

   public void onCooldownUpdate(CooldownUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      if (packet.getCooldown() == 0) {
         this.client.player.getItemCooldownManager().remove(packet.getItem());
      } else {
         this.client.player.getItemCooldownManager().set(packet.getItem(), packet.getCooldown());
      }

   }

   public void onVehicleMove(VehicleMoveS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = this.client.player.getRootVehicle();
      if (lv != this.client.player && lv.isLogicalSideForUpdatingMovement()) {
         lv.updatePositionAndAngles(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch());
         this.connection.send(new VehicleMoveC2SPacket(lv));
      }

   }

   public void onOpenWrittenBook(OpenWrittenBookS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      ItemStack lv = this.client.player.getStackInHand(packet.getHand());
      if (lv.isOf(Items.WRITTEN_BOOK)) {
         this.client.setScreen(new BookScreen(new BookScreen.WrittenBookContents(lv)));
      }

   }

   public void onCustomPayload(CustomPayloadS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Identifier lv = packet.getChannel();
      PacketByteBuf lv2 = null;

      try {
         lv2 = packet.getData();
         if (CustomPayloadS2CPacket.BRAND.equals(lv)) {
            String string = lv2.readString();
            this.client.player.setServerBrand(string);
            this.worldSession.setBrand(string);
         } else {
            int i;
            if (CustomPayloadS2CPacket.DEBUG_PATH.equals(lv)) {
               i = lv2.readInt();
               float f = lv2.readFloat();
               Path lv3 = Path.fromBuffer(lv2);
               this.client.debugRenderer.pathfindingDebugRenderer.addPath(i, lv3, f);
            } else if (CustomPayloadS2CPacket.DEBUG_NEIGHBORS_UPDATE.equals(lv)) {
               long l = lv2.readVarLong();
               BlockPos lv4 = lv2.readBlockPos();
               ((NeighborUpdateDebugRenderer)this.client.debugRenderer.neighborUpdateDebugRenderer).addNeighborUpdate(l, lv4);
            } else {
               ArrayList list;
               int k;
               int j;
               if (CustomPayloadS2CPacket.DEBUG_STRUCTURES.equals(lv)) {
                  DimensionType lv5 = (DimensionType)this.combinedDynamicRegistries.getCombinedRegistryManager().get(RegistryKeys.DIMENSION_TYPE).get(lv2.readIdentifier());
                  BlockBox lv6 = new BlockBox(lv2.readInt(), lv2.readInt(), lv2.readInt(), lv2.readInt(), lv2.readInt(), lv2.readInt());
                  j = lv2.readInt();
                  list = Lists.newArrayList();
                  List list2 = Lists.newArrayList();

                  for(k = 0; k < j; ++k) {
                     list.add(new BlockBox(lv2.readInt(), lv2.readInt(), lv2.readInt(), lv2.readInt(), lv2.readInt(), lv2.readInt()));
                     list2.add(lv2.readBoolean());
                  }

                  this.client.debugRenderer.structureDebugRenderer.addStructure(lv6, list, list2, lv5);
               } else if (CustomPayloadS2CPacket.DEBUG_WORLDGEN_ATTEMPT.equals(lv)) {
                  ((WorldGenAttemptDebugRenderer)this.client.debugRenderer.worldGenAttemptDebugRenderer).addBox(lv2.readBlockPos(), lv2.readFloat(), lv2.readFloat(), lv2.readFloat(), lv2.readFloat(), lv2.readFloat());
               } else {
                  int m;
                  if (CustomPayloadS2CPacket.DEBUG_VILLAGE_SECTIONS.equals(lv)) {
                     i = lv2.readInt();

                     for(m = 0; m < i; ++m) {
                        this.client.debugRenderer.villageSectionsDebugRenderer.addSection(lv2.readChunkSectionPos());
                     }

                     m = lv2.readInt();

                     for(j = 0; j < m; ++j) {
                        this.client.debugRenderer.villageSectionsDebugRenderer.removeSection(lv2.readChunkSectionPos());
                     }
                  } else {
                     BlockPos lv7;
                     String string2;
                     if (CustomPayloadS2CPacket.DEBUG_POI_ADDED.equals(lv)) {
                        lv7 = lv2.readBlockPos();
                        string2 = lv2.readString();
                        j = lv2.readInt();
                        VillageDebugRenderer.PointOfInterest lv8 = new VillageDebugRenderer.PointOfInterest(lv7, string2, j);
                        this.client.debugRenderer.villageDebugRenderer.addPointOfInterest(lv8);
                     } else if (CustomPayloadS2CPacket.DEBUG_POI_REMOVED.equals(lv)) {
                        lv7 = lv2.readBlockPos();
                        this.client.debugRenderer.villageDebugRenderer.removePointOfInterest(lv7);
                     } else if (CustomPayloadS2CPacket.DEBUG_POI_TICKET_COUNT.equals(lv)) {
                        lv7 = lv2.readBlockPos();
                        m = lv2.readInt();
                        this.client.debugRenderer.villageDebugRenderer.setFreeTicketCount(lv7, m);
                     } else if (CustomPayloadS2CPacket.DEBUG_GOAL_SELECTOR.equals(lv)) {
                        lv7 = lv2.readBlockPos();
                        m = lv2.readInt();
                        j = lv2.readInt();
                        list = Lists.newArrayList();

                        for(int n = 0; n < j; ++n) {
                           k = lv2.readInt();
                           boolean bl = lv2.readBoolean();
                           String string3 = lv2.readString(255);
                           list.add(new GoalSelectorDebugRenderer.GoalSelector(lv7, k, string3, bl));
                        }

                        this.client.debugRenderer.goalSelectorDebugRenderer.setGoalSelectorList(m, list);
                     } else if (CustomPayloadS2CPacket.DEBUG_RAIDS.equals(lv)) {
                        i = lv2.readInt();
                        Collection collection = Lists.newArrayList();

                        for(j = 0; j < i; ++j) {
                           collection.add(lv2.readBlockPos());
                        }

                        this.client.debugRenderer.raidCenterDebugRenderer.setRaidCenters(collection);
                     } else {
                        int o;
                        int p;
                        double d;
                        double e;
                        double g;
                        PositionImpl lv9;
                        UUID uUID;
                        if (CustomPayloadS2CPacket.DEBUG_BRAIN.equals(lv)) {
                           d = lv2.readDouble();
                           e = lv2.readDouble();
                           g = lv2.readDouble();
                           lv9 = new PositionImpl(d, e, g);
                           uUID = lv2.readUuid();
                           o = lv2.readInt();
                           String string4 = lv2.readString();
                           String string5 = lv2.readString();
                           p = lv2.readInt();
                           float h = lv2.readFloat();
                           float q = lv2.readFloat();
                           String string6 = lv2.readString();
                           Path lv10 = (Path)lv2.readNullable(Path::fromBuffer);
                           boolean bl2 = lv2.readBoolean();
                           int r = lv2.readInt();
                           VillageDebugRenderer.Brain lv11 = new VillageDebugRenderer.Brain(uUID, o, string4, string5, p, h, q, lv9, string6, lv10, bl2, r);
                           int s = lv2.readVarInt();

                           int t;
                           for(t = 0; t < s; ++t) {
                              String string7 = lv2.readString();
                              lv11.possibleActivities.add(string7);
                           }

                           t = lv2.readVarInt();

                           int u;
                           for(u = 0; u < t; ++u) {
                              String string8 = lv2.readString();
                              lv11.runningTasks.add(string8);
                           }

                           u = lv2.readVarInt();

                           int v;
                           for(v = 0; v < u; ++v) {
                              String string9 = lv2.readString();
                              lv11.memories.add(string9);
                           }

                           v = lv2.readVarInt();

                           int w;
                           for(w = 0; w < v; ++w) {
                              BlockPos lv12 = lv2.readBlockPos();
                              lv11.pointsOfInterest.add(lv12);
                           }

                           w = lv2.readVarInt();

                           int x;
                           for(x = 0; x < w; ++x) {
                              BlockPos lv13 = lv2.readBlockPos();
                              lv11.potentialJobSites.add(lv13);
                           }

                           x = lv2.readVarInt();

                           for(int y = 0; y < x; ++y) {
                              String string10 = lv2.readString();
                              lv11.gossips.add(string10);
                           }

                           this.client.debugRenderer.villageDebugRenderer.addBrain(lv11);
                        } else if (CustomPayloadS2CPacket.DEBUG_BEE.equals(lv)) {
                           d = lv2.readDouble();
                           e = lv2.readDouble();
                           g = lv2.readDouble();
                           lv9 = new PositionImpl(d, e, g);
                           uUID = lv2.readUuid();
                           o = lv2.readInt();
                           BlockPos lv14 = (BlockPos)lv2.readNullable(PacketByteBuf::readBlockPos);
                           BlockPos lv15 = (BlockPos)lv2.readNullable(PacketByteBuf::readBlockPos);
                           p = lv2.readInt();
                           Path lv16 = (Path)lv2.readNullable(Path::fromBuffer);
                           BeeDebugRenderer.Bee lv17 = new BeeDebugRenderer.Bee(uUID, o, lv9, lv16, lv14, lv15, p);
                           int z = lv2.readVarInt();

                           int aa;
                           for(aa = 0; aa < z; ++aa) {
                              String string11 = lv2.readString();
                              lv17.labels.add(string11);
                           }

                           aa = lv2.readVarInt();

                           for(int ab = 0; ab < aa; ++ab) {
                              BlockPos lv18 = lv2.readBlockPos();
                              lv17.blacklist.add(lv18);
                           }

                           this.client.debugRenderer.beeDebugRenderer.addBee(lv17);
                        } else {
                           int ac;
                           if (CustomPayloadS2CPacket.DEBUG_HIVE.equals(lv)) {
                              lv7 = lv2.readBlockPos();
                              string2 = lv2.readString();
                              j = lv2.readInt();
                              ac = lv2.readInt();
                              boolean bl3 = lv2.readBoolean();
                              BeeDebugRenderer.Hive lv19 = new BeeDebugRenderer.Hive(lv7, string2, j, ac, bl3, this.world.getTime());
                              this.client.debugRenderer.beeDebugRenderer.addHive(lv19);
                           } else if (CustomPayloadS2CPacket.DEBUG_GAME_TEST_CLEAR.equals(lv)) {
                              this.client.debugRenderer.gameTestDebugRenderer.clear();
                           } else if (CustomPayloadS2CPacket.DEBUG_GAME_TEST_ADD_MARKER.equals(lv)) {
                              lv7 = lv2.readBlockPos();
                              m = lv2.readInt();
                              String string12 = lv2.readString();
                              ac = lv2.readInt();
                              this.client.debugRenderer.gameTestDebugRenderer.addMarker(lv7, m, string12, ac);
                           } else if (CustomPayloadS2CPacket.DEBUG_GAME_EVENT.equals(lv)) {
                              GameEvent lv20 = (GameEvent)Registries.GAME_EVENT.get(new Identifier(lv2.readString()));
                              Vec3d lv21 = new Vec3d(lv2.readDouble(), lv2.readDouble(), lv2.readDouble());
                              this.client.debugRenderer.gameEventDebugRenderer.addEvent(lv20, lv21);
                           } else if (CustomPayloadS2CPacket.DEBUG_GAME_EVENT_LISTENERS.equals(lv)) {
                              Identifier lv22 = lv2.readIdentifier();
                              PositionSource lv23 = ((PositionSourceType)Registries.POSITION_SOURCE_TYPE.getOrEmpty(lv22).orElseThrow(() -> {
                                 return new IllegalArgumentException("Unknown position source type " + lv22);
                              })).readFromBuf(lv2);
                              j = lv2.readVarInt();
                              this.client.debugRenderer.gameEventDebugRenderer.addListener(lv23, j);
                           } else {
                              LOGGER.warn("Unknown custom packed identifier: {}", lv);
                           }
                        }
                     }
                  }
               }
            }
         }
      } finally {
         if (lv2 != null) {
            lv2.release();
         }

      }

   }

   public void onScoreboardObjectiveUpdate(ScoreboardObjectiveUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Scoreboard lv = this.world.getScoreboard();
      String string = packet.getName();
      if (packet.getMode() == 0) {
         lv.addObjective(string, ScoreboardCriterion.DUMMY, packet.getDisplayName(), packet.getType());
      } else if (lv.containsObjective(string)) {
         ScoreboardObjective lv2 = lv.getNullableObjective(string);
         if (packet.getMode() == ScoreboardObjectiveUpdateS2CPacket.REMOVE_MODE) {
            lv.removeObjective(lv2);
         } else if (packet.getMode() == ScoreboardObjectiveUpdateS2CPacket.UPDATE_MODE) {
            lv2.setRenderType(packet.getType());
            lv2.setDisplayName(packet.getDisplayName());
         }
      }

   }

   public void onScoreboardPlayerUpdate(ScoreboardPlayerUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Scoreboard lv = this.world.getScoreboard();
      String string = packet.getObjectiveName();
      switch (packet.getUpdateMode()) {
         case CHANGE:
            ScoreboardObjective lv2 = lv.getObjective(string);
            ScoreboardPlayerScore lv3 = lv.getPlayerScore(packet.getPlayerName(), lv2);
            lv3.setScore(packet.getScore());
            break;
         case REMOVE:
            lv.resetPlayerScore(packet.getPlayerName(), lv.getNullableObjective(string));
      }

   }

   public void onScoreboardDisplay(ScoreboardDisplayS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Scoreboard lv = this.world.getScoreboard();
      String string = packet.getName();
      ScoreboardObjective lv2 = string == null ? null : lv.getObjective(string);
      lv.setObjectiveSlot(packet.getSlot(), lv2);
   }

   public void onTeam(TeamS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Scoreboard lv = this.world.getScoreboard();
      TeamS2CPacket.Operation lv2 = packet.getTeamOperation();
      Team lv3;
      if (lv2 == TeamS2CPacket.Operation.ADD) {
         lv3 = lv.addTeam(packet.getTeamName());
      } else {
         lv3 = lv.getTeam(packet.getTeamName());
         if (lv3 == null) {
            LOGGER.warn("Received packet for unknown team {}: team action: {}, player action: {}", new Object[]{packet.getTeamName(), packet.getTeamOperation(), packet.getPlayerListOperation()});
            return;
         }
      }

      Optional optional = packet.getTeam();
      optional.ifPresent((team) -> {
         lv3.setDisplayName(team.getDisplayName());
         lv3.setColor(team.getColor());
         lv3.setFriendlyFlagsBitwise(team.getFriendlyFlagsBitwise());
         AbstractTeam.VisibilityRule lv = AbstractTeam.VisibilityRule.getRule(team.getNameTagVisibilityRule());
         if (lv != null) {
            lv3.setNameTagVisibilityRule(lv);
         }

         AbstractTeam.CollisionRule lv2 = AbstractTeam.CollisionRule.getRule(team.getCollisionRule());
         if (lv2 != null) {
            lv3.setCollisionRule(lv2);
         }

         lv3.setPrefix(team.getPrefix());
         lv3.setSuffix(team.getSuffix());
      });
      TeamS2CPacket.Operation lv4 = packet.getPlayerListOperation();
      Iterator var7;
      String string;
      if (lv4 == TeamS2CPacket.Operation.ADD) {
         var7 = packet.getPlayerNames().iterator();

         while(var7.hasNext()) {
            string = (String)var7.next();
            lv.addPlayerToTeam(string, lv3);
         }
      } else if (lv4 == TeamS2CPacket.Operation.REMOVE) {
         var7 = packet.getPlayerNames().iterator();

         while(var7.hasNext()) {
            string = (String)var7.next();
            lv.removePlayerFromTeam(string, lv3);
         }
      }

      if (lv2 == TeamS2CPacket.Operation.REMOVE) {
         lv.removeTeam(lv3);
      }

   }

   public void onParticle(ParticleS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      if (packet.getCount() == 0) {
         double d = (double)(packet.getSpeed() * packet.getOffsetX());
         double e = (double)(packet.getSpeed() * packet.getOffsetY());
         double f = (double)(packet.getSpeed() * packet.getOffsetZ());

         try {
            this.world.addParticle(packet.getParameters(), packet.isLongDistance(), packet.getX(), packet.getY(), packet.getZ(), d, e, f);
         } catch (Throwable var17) {
            LOGGER.warn("Could not spawn particle effect {}", packet.getParameters());
         }
      } else {
         for(int i = 0; i < packet.getCount(); ++i) {
            double g = this.random.nextGaussian() * (double)packet.getOffsetX();
            double h = this.random.nextGaussian() * (double)packet.getOffsetY();
            double j = this.random.nextGaussian() * (double)packet.getOffsetZ();
            double k = this.random.nextGaussian() * (double)packet.getSpeed();
            double l = this.random.nextGaussian() * (double)packet.getSpeed();
            double m = this.random.nextGaussian() * (double)packet.getSpeed();

            try {
               this.world.addParticle(packet.getParameters(), packet.isLongDistance(), packet.getX() + g, packet.getY() + h, packet.getZ() + j, k, l, m);
            } catch (Throwable var16) {
               LOGGER.warn("Could not spawn particle effect {}", packet.getParameters());
               return;
            }
         }
      }

   }

   public void onPing(PlayPingS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.sendPacket(new PlayPongC2SPacket(packet.getParameter()));
   }

   public void onEntityAttributes(EntityAttributesS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Entity lv = this.world.getEntityById(packet.getEntityId());
      if (lv != null) {
         if (!(lv instanceof LivingEntity)) {
            throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + lv + ")");
         } else {
            AttributeContainer lv2 = ((LivingEntity)lv).getAttributes();
            Iterator var4 = packet.getEntries().iterator();

            while(true) {
               while(var4.hasNext()) {
                  EntityAttributesS2CPacket.Entry lv3 = (EntityAttributesS2CPacket.Entry)var4.next();
                  EntityAttributeInstance lv4 = lv2.getCustomInstance(lv3.getId());
                  if (lv4 == null) {
                     LOGGER.warn("Entity {} does not have attribute {}", lv, Registries.ATTRIBUTE.getId(lv3.getId()));
                  } else {
                     lv4.setBaseValue(lv3.getBaseValue());
                     lv4.clearModifiers();
                     Iterator var7 = lv3.getModifiers().iterator();

                     while(var7.hasNext()) {
                        EntityAttributeModifier lv5 = (EntityAttributeModifier)var7.next();
                        lv4.addTemporaryModifier(lv5);
                     }
                  }
               }

               return;
            }
         }
      }
   }

   public void onCraftFailedResponse(CraftFailedResponseS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      ScreenHandler lv = this.client.player.currentScreenHandler;
      if (lv.syncId == packet.getSyncId()) {
         this.recipeManager.get(packet.getRecipeId()).ifPresent((recipe) -> {
            if (this.client.currentScreen instanceof RecipeBookProvider) {
               RecipeBookWidget lvx = ((RecipeBookProvider)this.client.currentScreen).getRecipeBookWidget();
               lvx.showGhostRecipe(recipe, lv.slots);
            }

         });
      }
   }

   public void onLightUpdate(LightUpdateS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      int i = packet.getChunkX();
      int j = packet.getChunkZ();
      LightData lv = packet.getData();
      this.world.enqueueChunkUpdate(() -> {
         this.readLightData(i, j, lv);
      });
   }

   private void readLightData(int x, int z, LightData data) {
      LightingProvider lv = this.world.getChunkManager().getLightingProvider();
      BitSet bitSet = data.getInitedSky();
      BitSet bitSet2 = data.getUninitedSky();
      Iterator iterator = data.getSkyNibbles().iterator();
      this.updateLighting(x, z, lv, LightType.SKY, bitSet, bitSet2, iterator, data.isNonEdge());
      BitSet bitSet3 = data.getInitedBlock();
      BitSet bitSet4 = data.getUninitedBlock();
      Iterator iterator2 = data.getBlockNibbles().iterator();
      this.updateLighting(x, z, lv, LightType.BLOCK, bitSet3, bitSet4, iterator2, data.isNonEdge());
      this.world.markChunkRenderability(x, z);
   }

   public void onSetTradeOffers(SetTradeOffersS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      ScreenHandler lv = this.client.player.currentScreenHandler;
      if (packet.getSyncId() == lv.syncId && lv instanceof MerchantScreenHandler lv2) {
         lv2.setOffers(new TradeOfferList(packet.getOffers().toNbt()));
         lv2.setExperienceFromServer(packet.getExperience());
         lv2.setLevelProgress(packet.getLevelProgress());
         lv2.setLeveled(packet.isLeveled());
         lv2.setCanRefreshTrades(packet.isRefreshable());
      }

   }

   public void onChunkLoadDistance(ChunkLoadDistanceS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.chunkLoadDistance = packet.getDistance();
      this.client.options.setServerViewDistance(this.chunkLoadDistance);
      this.world.getChunkManager().updateLoadDistance(packet.getDistance());
   }

   public void onSimulationDistance(SimulationDistanceS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.simulationDistance = packet.simulationDistance();
      this.world.setSimulationDistance(this.simulationDistance);
   }

   public void onChunkRenderDistanceCenter(ChunkRenderDistanceCenterS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.world.getChunkManager().setChunkMapCenter(packet.getChunkX(), packet.getChunkZ());
   }

   public void onPlayerActionResponse(PlayerActionResponseS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      this.world.handlePlayerActionResponse(packet.sequence());
   }

   public void onBundle(BundleS2CPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ThreadExecutor)this.client);
      Iterator var2 = packet.getPackets().iterator();

      while(var2.hasNext()) {
         Packet lv = (Packet)var2.next();
         lv.apply(this);
      }

   }

   private void updateLighting(int chunkX, int chunkZ, LightingProvider provider, LightType type, BitSet inited, BitSet uninited, Iterator nibbles, boolean nonEdge) {
      for(int k = 0; k < provider.getHeight(); ++k) {
         int l = provider.getBottomY() + k;
         boolean bl2 = inited.get(k);
         boolean bl3 = uninited.get(k);
         if (bl2 || bl3) {
            provider.enqueueSectionData(type, ChunkSectionPos.from(chunkX, l, chunkZ), bl2 ? new ChunkNibbleArray((byte[])((byte[])nibbles.next()).clone()) : new ChunkNibbleArray(), nonEdge);
            this.world.scheduleBlockRenders(chunkX, l, chunkZ);
         }
      }

   }

   public ClientConnection getConnection() {
      return this.connection;
   }

   public boolean isConnectionOpen() {
      return this.connection.isOpen();
   }

   public Collection getListedPlayerListEntries() {
      return this.listedPlayerListEntries;
   }

   public Collection getPlayerList() {
      return this.playerListEntries.values();
   }

   public Collection getPlayerUuids() {
      return this.playerListEntries.keySet();
   }

   @Nullable
   public PlayerListEntry getPlayerListEntry(UUID uuid) {
      return (PlayerListEntry)this.playerListEntries.get(uuid);
   }

   @Nullable
   public PlayerListEntry getPlayerListEntry(String profileName) {
      Iterator var2 = this.playerListEntries.values().iterator();

      PlayerListEntry lv;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         lv = (PlayerListEntry)var2.next();
      } while(!lv.getProfile().getName().equals(profileName));

      return lv;
   }

   public GameProfile getProfile() {
      return this.profile;
   }

   public ClientAdvancementManager getAdvancementHandler() {
      return this.advancementHandler;
   }

   public CommandDispatcher getCommandDispatcher() {
      return this.commandDispatcher;
   }

   public ClientWorld getWorld() {
      return this.world;
   }

   public DataQueryHandler getDataQueryHandler() {
      return this.dataQueryHandler;
   }

   public UUID getSessionId() {
      return this.sessionId;
   }

   public Set getWorldKeys() {
      return this.worldKeys;
   }

   public DynamicRegistryManager getRegistryManager() {
      return this.combinedDynamicRegistries.getCombinedRegistryManager();
   }

   public void acknowledge(SignedMessage message, boolean displayed) {
      MessageSignatureData lv = message.signature();
      if (lv != null && this.lastSeenMessagesCollector.add(lv, displayed) && this.lastSeenMessagesCollector.getMessageCount() > 64) {
         this.sendAcknowledgment();
      }

   }

   private void sendAcknowledgment() {
      int i = this.lastSeenMessagesCollector.resetMessageCount();
      if (i > 0) {
         this.sendPacket(new MessageAcknowledgmentC2SPacket(i));
      }

   }

   public void sendChatMessage(String content) {
      Instant instant = Instant.now();
      long l = NetworkEncryptionUtils.SecureRandomUtil.nextLong();
      LastSeenMessagesCollector.LastSeenMessages lv = this.lastSeenMessagesCollector.collect();
      MessageSignatureData lv2 = this.messagePacker.pack(new MessageBody(content, instant, l, lv.lastSeen()));
      this.sendPacket(new ChatMessageC2SPacket(content, instant, l, lv2, lv.update()));
   }

   public void sendChatCommand(String command) {
      Instant instant = Instant.now();
      long l = NetworkEncryptionUtils.SecureRandomUtil.nextLong();
      LastSeenMessagesCollector.LastSeenMessages lv = this.lastSeenMessagesCollector.collect();
      ArgumentSignatureDataMap lv2 = ArgumentSignatureDataMap.sign(SignedArgumentList.of(this.parse(command)), (value) -> {
         MessageBody lvx = new MessageBody(value, instant, l, lv.lastSeen());
         return this.messagePacker.pack(lvx);
      });
      this.sendPacket(new CommandExecutionC2SPacket(command, instant, l, lv2, lv.update()));
   }

   public boolean sendCommand(String command) {
      if (SignedArgumentList.of(this.parse(command)).arguments().isEmpty()) {
         LastSeenMessagesCollector.LastSeenMessages lv = this.lastSeenMessagesCollector.collect();
         this.sendPacket(new CommandExecutionC2SPacket(command, Instant.now(), 0L, ArgumentSignatureDataMap.EMPTY, lv.update()));
         return true;
      } else {
         return false;
      }
   }

   private ParseResults parse(String command) {
      return this.commandDispatcher.parse(command, this.commandSource);
   }

   public void tick() {
      if (this.connection.isEncrypted()) {
         ProfileKeys lv = this.client.getProfileKeys();
         if (lv.isExpired()) {
            lv.fetchKeyPair().thenAcceptAsync((keyPair) -> {
               keyPair.ifPresent(this::updateKeyPair);
            }, this.client);
         }
      }

      this.tickQueuedPackets();
      this.worldSession.tick();
   }

   public void updateKeyPair(PlayerKeyPair keyPair) {
      if (this.profile.getId().equals(this.client.getSession().getUuidOrNull())) {
         if (this.session == null || !this.session.keyPair().equals(keyPair)) {
            this.session = ClientPlayerSession.create(keyPair);
            this.messagePacker = this.session.createPacker(this.profile.getId());
            this.sendPacket(new PlayerSessionC2SPacket(this.session.toPublicSession().toSerialized()));
         }
      }
   }

   @Nullable
   public ServerInfo getServerInfo() {
      return this.serverInfo;
   }

   public FeatureSet getEnabledFeatures() {
      return this.enabledFeatures;
   }

   public boolean hasFeature(FeatureSet feature) {
      return feature.isSubsetOf(this.getEnabledFeatures());
   }

   @Environment(EnvType.CLIENT)
   private static record QueuedPacket(Packet packet, BooleanSupplier sendCondition, long expirationTime) {
      final Packet packet;

      QueuedPacket(Packet arg, BooleanSupplier booleanSupplier, long l) {
         this.packet = arg;
         this.sendCondition = booleanSupplier;
         this.expirationTime = l;
      }

      public Packet packet() {
         return this.packet;
      }

      public BooleanSupplier sendCondition() {
         return this.sendCondition;
      }

      public long expirationTime() {
         return this.expirationTime;
      }
   }
}
