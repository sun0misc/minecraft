package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.net.SocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.client.option.ChatVisibility;
import net.minecraft.command.argument.SignedArgumentList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.RideableInventory;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.message.AcknowledgmentValidator;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.message.MessageChainTaskQueue;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.MessageSignatureStorage;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedCommandArguments;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.c2s.play.CraftRequestC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.JigsawGeneratingC2SPacket;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.MessageAcknowledgmentC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayPongC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerSessionC2SPacket;
import net.minecraft.network.packet.c2s.play.QueryBlockNbtC2SPacket;
import net.minecraft.network.packet.c2s.play.QueryEntityNbtC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeBookDataC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeCategoryOptionsC2SPacket;
import net.minecraft.network.packet.c2s.play.RenameItemC2SPacket;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.SelectMerchantTradeC2SPacket;
import net.minecraft.network.packet.c2s.play.SpectatorTeleportC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateBeaconC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockMinecartC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyLockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateJigsawC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateStructureBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.NbtQueryResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerActionResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.network.packet.s2c.play.ProfilelessChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerPlayNetworkHandler implements EntityTrackingListener, TickablePacketListener, ServerPlayPacketListener {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int KEEP_ALIVE_INTERVAL = 15000;
   public static final double MAX_BREAK_SQUARED_DISTANCE = MathHelper.square(6.0);
   private static final int DEFAULT_SEQUENCE = -1;
   private static final int MAX_PENDING_ACKNOWLEDGMENTS = 4096;
   private static final Text CHAT_VALIDATION_FAILED_TEXT = Text.translatable("multiplayer.disconnect.chat_validation_failed");
   private final ClientConnection connection;
   private final MinecraftServer server;
   public ServerPlayerEntity player;
   private int ticks;
   private int sequence = -1;
   private long lastKeepAliveTime;
   private boolean waitingForKeepAlive;
   private long keepAliveId;
   private int messageCooldown;
   private int creativeItemDropThreshold;
   private double lastTickX;
   private double lastTickY;
   private double lastTickZ;
   private double updatedX;
   private double updatedY;
   private double updatedZ;
   @Nullable
   private Entity topmostRiddenEntity;
   private double lastTickRiddenX;
   private double lastTickRiddenY;
   private double lastTickRiddenZ;
   private double updatedRiddenX;
   private double updatedRiddenY;
   private double updatedRiddenZ;
   @Nullable
   private Vec3d requestedTeleportPos;
   private int requestedTeleportId;
   private int teleportRequestTick;
   private boolean floating;
   private int floatingTicks;
   private boolean vehicleFloating;
   private int vehicleFloatingTicks;
   private int movePacketsCount;
   private int lastTickMovePacketsCount;
   private final AtomicReference lastMessageTimestamp;
   @Nullable
   private PublicPlayerSession session;
   private MessageChain.Unpacker messageUnpacker;
   private final AcknowledgmentValidator acknowledgmentValidator;
   private final MessageSignatureStorage signatureStorage;
   private final MessageChainTaskQueue messageChainTaskQueue;

   public ServerPlayNetworkHandler(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player) {
      this.lastMessageTimestamp = new AtomicReference(Instant.EPOCH);
      this.acknowledgmentValidator = new AcknowledgmentValidator(20);
      this.signatureStorage = MessageSignatureStorage.create();
      this.server = server;
      this.connection = connection;
      connection.setPacketListener(this);
      this.player = player;
      player.networkHandler = this;
      this.lastKeepAliveTime = Util.getMeasuringTimeMs();
      player.getTextStream().onConnect();
      this.messageUnpacker = server.shouldEnforceSecureProfile() ? MessageChain.Unpacker.NOT_INITIALIZED : MessageChain.Unpacker.unsigned(player.getUuid());
      this.messageChainTaskQueue = new MessageChainTaskQueue(server);
   }

   public void tick() {
      if (this.sequence > -1) {
         this.sendPacket(new PlayerActionResponseS2CPacket(this.sequence));
         this.sequence = -1;
      }

      this.syncWithPlayerPosition();
      this.player.prevX = this.player.getX();
      this.player.prevY = this.player.getY();
      this.player.prevZ = this.player.getZ();
      this.player.playerTick();
      this.player.updatePositionAndAngles(this.lastTickX, this.lastTickY, this.lastTickZ, this.player.getYaw(), this.player.getPitch());
      ++this.ticks;
      this.lastTickMovePacketsCount = this.movePacketsCount;
      if (this.floating && !this.player.isSleeping() && !this.player.hasVehicle() && !this.player.isDead()) {
         if (++this.floatingTicks > 80) {
            LOGGER.warn("{} was kicked for floating too long!", this.player.getName().getString());
            this.disconnect(Text.translatable("multiplayer.disconnect.flying"));
            return;
         }
      } else {
         this.floating = false;
         this.floatingTicks = 0;
      }

      this.topmostRiddenEntity = this.player.getRootVehicle();
      if (this.topmostRiddenEntity != this.player && this.topmostRiddenEntity.getControllingPassenger() == this.player) {
         this.lastTickRiddenX = this.topmostRiddenEntity.getX();
         this.lastTickRiddenY = this.topmostRiddenEntity.getY();
         this.lastTickRiddenZ = this.topmostRiddenEntity.getZ();
         this.updatedRiddenX = this.topmostRiddenEntity.getX();
         this.updatedRiddenY = this.topmostRiddenEntity.getY();
         this.updatedRiddenZ = this.topmostRiddenEntity.getZ();
         if (this.vehicleFloating && this.player.getRootVehicle().getControllingPassenger() == this.player) {
            if (++this.vehicleFloatingTicks > 80) {
               LOGGER.warn("{} was kicked for floating a vehicle too long!", this.player.getName().getString());
               this.disconnect(Text.translatable("multiplayer.disconnect.flying"));
               return;
            }
         } else {
            this.vehicleFloating = false;
            this.vehicleFloatingTicks = 0;
         }
      } else {
         this.topmostRiddenEntity = null;
         this.vehicleFloating = false;
         this.vehicleFloatingTicks = 0;
      }

      this.server.getProfiler().push("keepAlive");
      long l = Util.getMeasuringTimeMs();
      if (l - this.lastKeepAliveTime >= 15000L) {
         if (this.waitingForKeepAlive) {
            this.disconnect(Text.translatable("disconnect.timeout"));
         } else {
            this.waitingForKeepAlive = true;
            this.lastKeepAliveTime = l;
            this.keepAliveId = l;
            this.sendPacket(new KeepAliveS2CPacket(this.keepAliveId));
         }
      }

      this.server.getProfiler().pop();
      if (this.messageCooldown > 0) {
         --this.messageCooldown;
      }

      if (this.creativeItemDropThreshold > 0) {
         --this.creativeItemDropThreshold;
      }

      if (this.player.getLastActionTime() > 0L && this.server.getPlayerIdleTimeout() > 0 && Util.getMeasuringTimeMs() - this.player.getLastActionTime() > (long)(this.server.getPlayerIdleTimeout() * 1000 * 60)) {
         this.disconnect(Text.translatable("multiplayer.disconnect.idling"));
      }

   }

   public void syncWithPlayerPosition() {
      this.lastTickX = this.player.getX();
      this.lastTickY = this.player.getY();
      this.lastTickZ = this.player.getZ();
      this.updatedX = this.player.getX();
      this.updatedY = this.player.getY();
      this.updatedZ = this.player.getZ();
   }

   public boolean isConnectionOpen() {
      return this.connection.isOpen();
   }

   private boolean isHost() {
      return this.server.isHost(this.player.getGameProfile());
   }

   public void disconnect(Text reason) {
      this.connection.send(new DisconnectS2CPacket(reason), PacketCallbacks.always(() -> {
         this.connection.disconnect(reason);
      }));
      this.connection.disableAutoRead();
      MinecraftServer var10000 = this.server;
      ClientConnection var10001 = this.connection;
      Objects.requireNonNull(var10001);
      var10000.submitAndJoin(var10001::handleDisconnection);
   }

   private CompletableFuture filterText(Object text, BiFunction filterer) {
      return ((CompletableFuture)filterer.apply(this.player.getTextStream(), text)).thenApply((filtered) -> {
         if (!this.isConnectionOpen()) {
            LOGGER.debug("Ignoring packet due to disconnection");
            throw new CancellationException("disconnected");
         } else {
            return filtered;
         }
      });
   }

   private CompletableFuture filterText(String text) {
      return this.filterText(text, TextStream::filterText);
   }

   private CompletableFuture filterTexts(List texts) {
      return this.filterText(texts, TextStream::filterTexts);
   }

   public void onPlayerInput(PlayerInputC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      this.player.updateInput(packet.getSideways(), packet.getForward(), packet.isJumping(), packet.isSneaking());
   }

   private static boolean isMovementInvalid(double x, double y, double z, float yaw, float pitch) {
      return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z) || !Floats.isFinite(pitch) || !Floats.isFinite(yaw);
   }

   private static double clampHorizontal(double d) {
      return MathHelper.clamp(d, -3.0E7, 3.0E7);
   }

   private static double clampVertical(double d) {
      return MathHelper.clamp(d, -2.0E7, 2.0E7);
   }

   public void onVehicleMove(VehicleMoveC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      if (isMovementInvalid(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch())) {
         this.disconnect(Text.translatable("multiplayer.disconnect.invalid_vehicle_movement"));
      } else {
         Entity lv = this.player.getRootVehicle();
         if (lv != this.player && lv.getControllingPassenger() == this.player && lv == this.topmostRiddenEntity) {
            ServerWorld lv2 = this.player.getWorld();
            double d = lv.getX();
            double e = lv.getY();
            double f = lv.getZ();
            double g = clampHorizontal(packet.getX());
            double h = clampVertical(packet.getY());
            double i = clampHorizontal(packet.getZ());
            float j = MathHelper.wrapDegrees(packet.getYaw());
            float k = MathHelper.wrapDegrees(packet.getPitch());
            double l = g - this.lastTickRiddenX;
            double m = h - this.lastTickRiddenY;
            double n = i - this.lastTickRiddenZ;
            double o = lv.getVelocity().lengthSquared();
            double p = l * l + m * m + n * n;
            if (p - o > 100.0 && !this.isHost()) {
               LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", new Object[]{lv.getName().getString(), this.player.getName().getString(), l, m, n});
               this.connection.send(new VehicleMoveS2CPacket(lv));
               return;
            }

            boolean bl = lv2.isSpaceEmpty(lv, lv.getBoundingBox().contract(0.0625));
            l = g - this.updatedRiddenX;
            m = h - this.updatedRiddenY - 1.0E-6;
            n = i - this.updatedRiddenZ;
            boolean bl2 = lv.field_36331;
            lv.move(MovementType.PLAYER, new Vec3d(l, m, n));
            double q = m;
            l = g - lv.getX();
            m = h - lv.getY();
            if (m > -0.5 || m < 0.5) {
               m = 0.0;
            }

            n = i - lv.getZ();
            p = l * l + m * m + n * n;
            boolean bl3 = false;
            if (p > 0.0625) {
               bl3 = true;
               LOGGER.warn("{} (vehicle of {}) moved wrongly! {}", new Object[]{lv.getName().getString(), this.player.getName().getString(), Math.sqrt(p)});
            }

            lv.updatePositionAndAngles(g, h, i, j, k);
            boolean bl4 = lv2.isSpaceEmpty(lv, lv.getBoundingBox().contract(0.0625));
            if (bl && (bl3 || !bl4)) {
               lv.updatePositionAndAngles(d, e, f, j, k);
               this.connection.send(new VehicleMoveS2CPacket(lv));
               return;
            }

            this.player.getWorld().getChunkManager().updatePosition(this.player);
            this.player.increaseTravelMotionStats(this.player.getX() - d, this.player.getY() - e, this.player.getZ() - f);
            this.vehicleFloating = q >= -0.03125 && !bl2 && !this.server.isFlightEnabled() && !lv.hasNoGravity() && this.isEntityOnAir(lv);
            this.updatedRiddenX = lv.getX();
            this.updatedRiddenY = lv.getY();
            this.updatedRiddenZ = lv.getZ();
         }

      }
   }

   private boolean isEntityOnAir(Entity entity) {
      return entity.world.getStatesInBox(entity.getBoundingBox().expand(0.0625).stretch(0.0, -0.55, 0.0)).allMatch(AbstractBlock.AbstractBlockState::isAir);
   }

   public void onTeleportConfirm(TeleportConfirmC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      if (packet.getTeleportId() == this.requestedTeleportId) {
         if (this.requestedTeleportPos == null) {
            this.disconnect(Text.translatable("multiplayer.disconnect.invalid_player_movement"));
            return;
         }

         this.player.updatePositionAndAngles(this.requestedTeleportPos.x, this.requestedTeleportPos.y, this.requestedTeleportPos.z, this.player.getYaw(), this.player.getPitch());
         this.updatedX = this.requestedTeleportPos.x;
         this.updatedY = this.requestedTeleportPos.y;
         this.updatedZ = this.requestedTeleportPos.z;
         if (this.player.isInTeleportationState()) {
            this.player.onTeleportationDone();
         }

         this.requestedTeleportPos = null;
      }

   }

   public void onRecipeBookData(RecipeBookDataC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      Optional var10000 = this.server.getRecipeManager().get(packet.getRecipeId());
      ServerRecipeBook var10001 = this.player.getRecipeBook();
      Objects.requireNonNull(var10001);
      var10000.ifPresent(var10001::onRecipeDisplayed);
   }

   public void onRecipeCategoryOptions(RecipeCategoryOptionsC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      this.player.getRecipeBook().setCategoryOptions(packet.getCategory(), packet.isGuiOpen(), packet.isFilteringCraftable());
   }

   public void onAdvancementTab(AdvancementTabC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      if (packet.getAction() == AdvancementTabC2SPacket.Action.OPENED_TAB) {
         Identifier lv = packet.getTabToOpen();
         Advancement lv2 = this.server.getAdvancementLoader().get(lv);
         if (lv2 != null) {
            this.player.getAdvancementTracker().setDisplayTab(lv2);
         }
      }

   }

   public void onRequestCommandCompletions(RequestCommandCompletionsC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      StringReader stringReader = new StringReader(packet.getPartialCommand());
      if (stringReader.canRead() && stringReader.peek() == '/') {
         stringReader.skip();
      }

      ParseResults parseResults = this.server.getCommandManager().getDispatcher().parse(stringReader, this.player.getCommandSource());
      this.server.getCommandManager().getDispatcher().getCompletionSuggestions(parseResults).thenAccept((suggestions) -> {
         this.connection.send(new CommandSuggestionsS2CPacket(packet.getCompletionId(), suggestions));
      });
   }

   public void onUpdateCommandBlock(UpdateCommandBlockC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      if (!this.server.areCommandBlocksEnabled()) {
         this.player.sendMessage(Text.translatable("advMode.notEnabled"));
      } else if (!this.player.isCreativeLevelTwoOp()) {
         this.player.sendMessage(Text.translatable("advMode.notAllowed"));
      } else {
         CommandBlockExecutor lv = null;
         CommandBlockBlockEntity lv2 = null;
         BlockPos lv3 = packet.getBlockPos();
         BlockEntity lv4 = this.player.world.getBlockEntity(lv3);
         if (lv4 instanceof CommandBlockBlockEntity) {
            lv2 = (CommandBlockBlockEntity)lv4;
            lv = lv2.getCommandExecutor();
         }

         String string = packet.getCommand();
         boolean bl = packet.shouldTrackOutput();
         if (lv != null) {
            CommandBlockBlockEntity.Type lv5 = lv2.getCommandBlockType();
            BlockState lv6 = this.player.world.getBlockState(lv3);
            Direction lv7 = (Direction)lv6.get(CommandBlock.FACING);
            BlockState lv8;
            switch (packet.getType()) {
               case SEQUENCE:
                  lv8 = Blocks.CHAIN_COMMAND_BLOCK.getDefaultState();
                  break;
               case AUTO:
                  lv8 = Blocks.REPEATING_COMMAND_BLOCK.getDefaultState();
                  break;
               case REDSTONE:
               default:
                  lv8 = Blocks.COMMAND_BLOCK.getDefaultState();
            }

            BlockState lv9 = (BlockState)((BlockState)lv8.with(CommandBlock.FACING, lv7)).with(CommandBlock.CONDITIONAL, packet.isConditional());
            if (lv9 != lv6) {
               this.player.world.setBlockState(lv3, lv9, Block.NOTIFY_LISTENERS);
               lv4.setCachedState(lv9);
               this.player.world.getWorldChunk(lv3).setBlockEntity(lv4);
            }

            lv.setCommand(string);
            lv.setTrackOutput(bl);
            if (!bl) {
               lv.setLastOutput((Text)null);
            }

            lv2.setAuto(packet.isAlwaysActive());
            if (lv5 != packet.getType()) {
               lv2.updateCommandBlock();
            }

            lv.markDirty();
            if (!StringHelper.isEmpty(string)) {
               this.player.sendMessage(Text.translatable("advMode.setCommand.success", string));
            }
         }

      }
   }

   public void onUpdateCommandBlockMinecart(UpdateCommandBlockMinecartC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      if (!this.server.areCommandBlocksEnabled()) {
         this.player.sendMessage(Text.translatable("advMode.notEnabled"));
      } else if (!this.player.isCreativeLevelTwoOp()) {
         this.player.sendMessage(Text.translatable("advMode.notAllowed"));
      } else {
         CommandBlockExecutor lv = packet.getMinecartCommandExecutor(this.player.world);
         if (lv != null) {
            lv.setCommand(packet.getCommand());
            lv.setTrackOutput(packet.shouldTrackOutput());
            if (!packet.shouldTrackOutput()) {
               lv.setLastOutput((Text)null);
            }

            lv.markDirty();
            this.player.sendMessage(Text.translatable("advMode.setCommand.success", packet.getCommand()));
         }

      }
   }

   public void onPickFromInventory(PickFromInventoryC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      this.player.getInventory().swapSlotWithHotbar(packet.getSlot());
      this.player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, this.player.getInventory().selectedSlot, this.player.getInventory().getStack(this.player.getInventory().selectedSlot)));
      this.player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, packet.getSlot(), this.player.getInventory().getStack(packet.getSlot())));
      this.player.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(this.player.getInventory().selectedSlot));
   }

   public void onRenameItem(RenameItemC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      ScreenHandler var3 = this.player.currentScreenHandler;
      if (var3 instanceof AnvilScreenHandler lv) {
         if (!lv.canUse(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, lv);
            return;
         }

         String string = SharedConstants.stripInvalidChars(packet.getName());
         if (string.length() <= 50) {
            lv.setNewItemName(string);
         }
      }

   }

   public void onUpdateBeacon(UpdateBeaconC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      ScreenHandler var3 = this.player.currentScreenHandler;
      if (var3 instanceof BeaconScreenHandler lv) {
         if (!this.player.currentScreenHandler.canUse(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.currentScreenHandler);
            return;
         }

         lv.setEffects(packet.getPrimaryEffectId(), packet.getSecondaryEffectId());
      }

   }

   public void onUpdateStructureBlock(UpdateStructureBlockC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      if (this.player.isCreativeLevelTwoOp()) {
         BlockPos lv = packet.getPos();
         BlockState lv2 = this.player.world.getBlockState(lv);
         BlockEntity lv3 = this.player.world.getBlockEntity(lv);
         if (lv3 instanceof StructureBlockBlockEntity) {
            StructureBlockBlockEntity lv4 = (StructureBlockBlockEntity)lv3;
            lv4.setMode(packet.getMode());
            lv4.setTemplateName(packet.getTemplateName());
            lv4.setOffset(packet.getOffset());
            lv4.setSize(packet.getSize());
            lv4.setMirror(packet.getMirror());
            lv4.setRotation(packet.getRotation());
            lv4.setMetadata(packet.getMetadata());
            lv4.setIgnoreEntities(packet.shouldIgnoreEntities());
            lv4.setShowAir(packet.shouldShowAir());
            lv4.setShowBoundingBox(packet.shouldShowBoundingBox());
            lv4.setIntegrity(packet.getIntegrity());
            lv4.setSeed(packet.getSeed());
            if (lv4.hasStructureName()) {
               String string = lv4.getTemplateName();
               if (packet.getAction() == StructureBlockBlockEntity.Action.SAVE_AREA) {
                  if (lv4.saveStructure()) {
                     this.player.sendMessage(Text.translatable("structure_block.save_success", string), false);
                  } else {
                     this.player.sendMessage(Text.translatable("structure_block.save_failure", string), false);
                  }
               } else if (packet.getAction() == StructureBlockBlockEntity.Action.LOAD_AREA) {
                  if (!lv4.isStructureAvailable()) {
                     this.player.sendMessage(Text.translatable("structure_block.load_not_found", string), false);
                  } else if (lv4.loadStructure(this.player.getWorld())) {
                     this.player.sendMessage(Text.translatable("structure_block.load_success", string), false);
                  } else {
                     this.player.sendMessage(Text.translatable("structure_block.load_prepare", string), false);
                  }
               } else if (packet.getAction() == StructureBlockBlockEntity.Action.SCAN_AREA) {
                  if (lv4.detectStructureSize()) {
                     this.player.sendMessage(Text.translatable("structure_block.size_success", string), false);
                  } else {
                     this.player.sendMessage(Text.translatable("structure_block.size_failure"), false);
                  }
               }
            } else {
               this.player.sendMessage(Text.translatable("structure_block.invalid_structure_name", packet.getTemplateName()), false);
            }

            lv4.markDirty();
            this.player.world.updateListeners(lv, lv2, lv2, Block.NOTIFY_ALL);
         }

      }
   }

   public void onUpdateJigsaw(UpdateJigsawC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      if (this.player.isCreativeLevelTwoOp()) {
         BlockPos lv = packet.getPos();
         BlockState lv2 = this.player.world.getBlockState(lv);
         BlockEntity lv3 = this.player.world.getBlockEntity(lv);
         if (lv3 instanceof JigsawBlockEntity) {
            JigsawBlockEntity lv4 = (JigsawBlockEntity)lv3;
            lv4.setName(packet.getName());
            lv4.setTarget(packet.getTarget());
            lv4.setPool(RegistryKey.of(RegistryKeys.TEMPLATE_POOL, packet.getPool()));
            lv4.setFinalState(packet.getFinalState());
            lv4.setJoint(packet.getJointType());
            lv4.markDirty();
            this.player.world.updateListeners(lv, lv2, lv2, Block.NOTIFY_ALL);
         }

      }
   }

   public void onJigsawGenerating(JigsawGeneratingC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      if (this.player.isCreativeLevelTwoOp()) {
         BlockPos lv = packet.getPos();
         BlockEntity lv2 = this.player.world.getBlockEntity(lv);
         if (lv2 instanceof JigsawBlockEntity) {
            JigsawBlockEntity lv3 = (JigsawBlockEntity)lv2;
            lv3.generate(this.player.getWorld(), packet.getMaxDepth(), packet.shouldKeepJigsaws());
         }

      }
   }

   public void onSelectMerchantTrade(SelectMerchantTradeC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      int i = packet.getTradeId();
      ScreenHandler var4 = this.player.currentScreenHandler;
      if (var4 instanceof MerchantScreenHandler lv) {
         if (!lv.canUse(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, lv);
            return;
         }

         lv.setRecipeIndex(i);
         lv.switchTo(i);
      }

   }

   public void onBookUpdate(BookUpdateC2SPacket packet) {
      int i = packet.getSlot();
      if (PlayerInventory.isValidHotbarIndex(i) || i == 40) {
         List list = Lists.newArrayList();
         Optional optional = packet.getTitle();
         Objects.requireNonNull(list);
         optional.ifPresent(list::add);
         Stream var10000 = packet.getPages().stream().limit(100L);
         Objects.requireNonNull(list);
         var10000.forEach(list::add);
         Consumer consumer = optional.isPresent() ? (texts) -> {
            this.addBook((FilteredMessage)texts.get(0), texts.subList(1, texts.size()), i);
         } : (texts) -> {
            this.updateBookContent(texts, i);
         };
         this.filterTexts(list).thenAcceptAsync(consumer, this.server);
      }
   }

   private void updateBookContent(List pages, int slotId) {
      ItemStack lv = this.player.getInventory().getStack(slotId);
      if (lv.isOf(Items.WRITABLE_BOOK)) {
         this.setTextToBook(pages, UnaryOperator.identity(), lv);
      }
   }

   private void addBook(FilteredMessage title, List pages, int slotId) {
      ItemStack lv = this.player.getInventory().getStack(slotId);
      if (lv.isOf(Items.WRITABLE_BOOK)) {
         ItemStack lv2 = new ItemStack(Items.WRITTEN_BOOK);
         NbtCompound lv3 = lv.getNbt();
         if (lv3 != null) {
            lv2.setNbt(lv3.copy());
         }

         lv2.setSubNbt("author", NbtString.of(this.player.getName().getString()));
         if (this.player.shouldFilterText()) {
            lv2.setSubNbt("title", NbtString.of(title.getString()));
         } else {
            lv2.setSubNbt("filtered_title", NbtString.of(title.getString()));
            lv2.setSubNbt("title", NbtString.of(title.raw()));
         }

         this.setTextToBook(pages, (text) -> {
            return Text.Serializer.toJson(Text.literal(text));
         }, lv2);
         this.player.getInventory().setStack(slotId, lv2);
      }
   }

   private void setTextToBook(List messages, UnaryOperator postProcessor, ItemStack book) {
      NbtList lv = new NbtList();
      if (this.player.shouldFilterText()) {
         Stream var10000 = messages.stream().map((message) -> {
            return NbtString.of((String)postProcessor.apply(message.getString()));
         });
         Objects.requireNonNull(lv);
         var10000.forEach(lv::add);
      } else {
         NbtCompound lv2 = new NbtCompound();
         int i = 0;

         for(int j = messages.size(); i < j; ++i) {
            FilteredMessage lv3 = (FilteredMessage)messages.get(i);
            String string = lv3.raw();
            lv.add(NbtString.of((String)postProcessor.apply(string)));
            if (lv3.isFiltered()) {
               lv2.putString(String.valueOf(i), (String)postProcessor.apply(lv3.getString()));
            }
         }

         if (!lv2.isEmpty()) {
            book.setSubNbt("filtered_pages", lv2);
         }
      }

      book.setSubNbt("pages", lv);
   }

   public void onQueryEntityNbt(QueryEntityNbtC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      if (this.player.hasPermissionLevel(2)) {
         Entity lv = this.player.getWorld().getEntityById(packet.getEntityId());
         if (lv != null) {
            NbtCompound lv2 = lv.writeNbt(new NbtCompound());
            this.player.networkHandler.sendPacket(new NbtQueryResponseS2CPacket(packet.getTransactionId(), lv2));
         }

      }
   }

   public void onQueryBlockNbt(QueryBlockNbtC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      if (this.player.hasPermissionLevel(2)) {
         BlockEntity lv = this.player.getWorld().getBlockEntity(packet.getPos());
         NbtCompound lv2 = lv != null ? lv.createNbt() : null;
         this.player.networkHandler.sendPacket(new NbtQueryResponseS2CPacket(packet.getTransactionId(), lv2));
      }
   }

   public void onPlayerMove(PlayerMoveC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      if (isMovementInvalid(packet.getX(0.0), packet.getY(0.0), packet.getZ(0.0), packet.getYaw(0.0F), packet.getPitch(0.0F))) {
         this.disconnect(Text.translatable("multiplayer.disconnect.invalid_player_movement"));
      } else {
         ServerWorld lv = this.player.getWorld();
         if (!this.player.notInAnyWorld) {
            if (this.ticks == 0) {
               this.syncWithPlayerPosition();
            }

            if (this.requestedTeleportPos != null) {
               if (this.ticks - this.teleportRequestTick > 20) {
                  this.teleportRequestTick = this.ticks;
                  this.requestTeleport(this.requestedTeleportPos.x, this.requestedTeleportPos.y, this.requestedTeleportPos.z, this.player.getYaw(), this.player.getPitch());
               }

            } else {
               this.teleportRequestTick = this.ticks;
               double d = clampHorizontal(packet.getX(this.player.getX()));
               double e = clampVertical(packet.getY(this.player.getY()));
               double f = clampHorizontal(packet.getZ(this.player.getZ()));
               float g = MathHelper.wrapDegrees(packet.getYaw(this.player.getYaw()));
               float h = MathHelper.wrapDegrees(packet.getPitch(this.player.getPitch()));
               if (this.player.hasVehicle()) {
                  this.player.updatePositionAndAngles(this.player.getX(), this.player.getY(), this.player.getZ(), g, h);
                  this.player.getWorld().getChunkManager().updatePosition(this.player);
               } else {
                  double i = this.player.getX();
                  double j = this.player.getY();
                  double k = this.player.getZ();
                  double l = this.player.getY();
                  double m = d - this.lastTickX;
                  double n = e - this.lastTickY;
                  double o = f - this.lastTickZ;
                  double p = this.player.getVelocity().lengthSquared();
                  double q = m * m + n * n + o * o;
                  if (this.player.isSleeping()) {
                     if (q > 1.0) {
                        this.requestTeleport(this.player.getX(), this.player.getY(), this.player.getZ(), g, h);
                     }

                  } else {
                     ++this.movePacketsCount;
                     int r = this.movePacketsCount - this.lastTickMovePacketsCount;
                     if (r > 5) {
                        LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", this.player.getName().getString(), r);
                        r = 1;
                     }

                     if (!this.player.isInTeleportationState() && (!this.player.getWorld().getGameRules().getBoolean(GameRules.DISABLE_ELYTRA_MOVEMENT_CHECK) || !this.player.isFallFlying())) {
                        float s = this.player.isFallFlying() ? 300.0F : 100.0F;
                        if (q - p > (double)(s * (float)r) && !this.isHost()) {
                           LOGGER.warn("{} moved too quickly! {},{},{}", new Object[]{this.player.getName().getString(), m, n, o});
                           this.requestTeleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYaw(), this.player.getPitch());
                           return;
                        }
                     }

                     Box lv2 = this.player.getBoundingBox();
                     m = d - this.updatedX;
                     n = e - this.updatedY;
                     o = f - this.updatedZ;
                     boolean bl = n > 0.0;
                     if (this.player.isOnGround() && !packet.isOnGround() && bl) {
                        this.player.jump();
                     }

                     boolean bl2 = this.player.field_36331;
                     this.player.move(MovementType.PLAYER, new Vec3d(m, n, o));
                     double t = n;
                     m = d - this.player.getX();
                     n = e - this.player.getY();
                     if (n > -0.5 || n < 0.5) {
                        n = 0.0;
                     }

                     o = f - this.player.getZ();
                     q = m * m + n * n + o * o;
                     boolean bl3 = false;
                     if (!this.player.isInTeleportationState() && q > 0.0625 && !this.player.isSleeping() && !this.player.interactionManager.isCreative() && this.player.interactionManager.getGameMode() != GameMode.SPECTATOR) {
                        bl3 = true;
                        LOGGER.warn("{} moved wrongly!", this.player.getName().getString());
                     }

                     this.player.updatePositionAndAngles(d, e, f, g, h);
                     if (this.player.noClip || this.player.isSleeping() || (!bl3 || !lv.isSpaceEmpty(this.player, lv2)) && !this.isPlayerNotCollidingWithBlocks(lv, lv2)) {
                        this.floating = t >= -0.03125 && !bl2 && this.player.interactionManager.getGameMode() != GameMode.SPECTATOR && !this.server.isFlightEnabled() && !this.player.getAbilities().allowFlying && !this.player.hasStatusEffect(StatusEffects.LEVITATION) && !this.player.isFallFlying() && !this.player.isUsingRiptide() && this.isEntityOnAir(this.player);
                        this.player.getWorld().getChunkManager().updatePosition(this.player);
                        this.player.handleFall(this.player.getY() - l, packet.isOnGround());
                        this.player.setOnGround(packet.isOnGround());
                        if (bl) {
                           this.player.onLanding();
                        }

                        this.player.increaseTravelMotionStats(this.player.getX() - i, this.player.getY() - j, this.player.getZ() - k);
                        this.updatedX = this.player.getX();
                        this.updatedY = this.player.getY();
                        this.updatedZ = this.player.getZ();
                     } else {
                        this.requestTeleport(i, j, k, g, h);
                        this.player.handleFall(this.player.getY() - l, packet.isOnGround());
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isPlayerNotCollidingWithBlocks(WorldView world, Box box) {
      Iterable iterable = world.getCollisions(this.player, this.player.getBoundingBox().contract(9.999999747378752E-6));
      VoxelShape lv = VoxelShapes.cuboid(box.contract(9.999999747378752E-6));
      Iterator var5 = iterable.iterator();

      VoxelShape lv2;
      do {
         if (!var5.hasNext()) {
            return false;
         }

         lv2 = (VoxelShape)var5.next();
      } while(VoxelShapes.matchesAnywhere(lv2, lv, BooleanBiFunction.AND));

      return true;
   }

   public void requestTeleport(double x, double y, double z, float yaw, float pitch) {
      this.requestTeleport(x, y, z, yaw, pitch, Collections.emptySet());
   }

   public void requestTeleport(double x, double y, double z, float yaw, float pitch, Set set) {
      double i = set.contains(PositionFlag.X) ? this.player.getX() : 0.0;
      double j = set.contains(PositionFlag.Y) ? this.player.getY() : 0.0;
      double k = set.contains(PositionFlag.Z) ? this.player.getZ() : 0.0;
      float l = set.contains(PositionFlag.Y_ROT) ? this.player.getYaw() : 0.0F;
      float m = set.contains(PositionFlag.X_ROT) ? this.player.getPitch() : 0.0F;
      this.requestedTeleportPos = new Vec3d(x, y, z);
      if (++this.requestedTeleportId == Integer.MAX_VALUE) {
         this.requestedTeleportId = 0;
      }

      this.teleportRequestTick = this.ticks;
      this.player.updatePositionAndAngles(x, y, z, yaw, pitch);
      this.player.networkHandler.sendPacket(new PlayerPositionLookS2CPacket(x - i, y - j, z - k, yaw - l, pitch - m, set, this.requestedTeleportId));
   }

   public void onPlayerAction(PlayerActionC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      BlockPos lv = packet.getPos();
      this.player.updateLastActionTime();
      PlayerActionC2SPacket.Action lv2 = packet.getAction();
      switch (lv2) {
         case SWAP_ITEM_WITH_OFFHAND:
            if (!this.player.isSpectator()) {
               ItemStack lv3 = this.player.getStackInHand(Hand.OFF_HAND);
               this.player.setStackInHand(Hand.OFF_HAND, this.player.getStackInHand(Hand.MAIN_HAND));
               this.player.setStackInHand(Hand.MAIN_HAND, lv3);
               this.player.clearActiveItem();
            }

            return;
         case DROP_ITEM:
            if (!this.player.isSpectator()) {
               this.player.dropSelectedItem(false);
            }

            return;
         case DROP_ALL_ITEMS:
            if (!this.player.isSpectator()) {
               this.player.dropSelectedItem(true);
            }

            return;
         case RELEASE_USE_ITEM:
            this.player.stopUsingItem();
            return;
         case START_DESTROY_BLOCK:
         case ABORT_DESTROY_BLOCK:
         case STOP_DESTROY_BLOCK:
            this.player.interactionManager.processBlockBreakingAction(lv, lv2, packet.getDirection(), this.player.world.getTopY(), packet.getSequence());
            this.player.networkHandler.updateSequence(packet.getSequence());
            return;
         default:
            throw new IllegalArgumentException("Invalid player action");
      }
   }

   private static boolean canPlace(ServerPlayerEntity player, ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         Item lv = stack.getItem();
         return (lv instanceof BlockItem || lv instanceof BucketItem) && !player.getItemCooldownManager().isCoolingDown(lv);
      }
   }

   public void onPlayerInteractBlock(PlayerInteractBlockC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      this.player.networkHandler.updateSequence(packet.getSequence());
      ServerWorld lv = this.player.getWorld();
      Hand lv2 = packet.getHand();
      ItemStack lv3 = this.player.getStackInHand(lv2);
      if (lv3.isItemEnabled(lv.getEnabledFeatures())) {
         BlockHitResult lv4 = packet.getBlockHitResult();
         Vec3d lv5 = lv4.getPos();
         BlockPos lv6 = lv4.getBlockPos();
         Vec3d lv7 = Vec3d.ofCenter(lv6);
         if (!(this.player.getEyePos().squaredDistanceTo(lv7) > MAX_BREAK_SQUARED_DISTANCE)) {
            Vec3d lv8 = lv5.subtract(lv7);
            double d = 1.0000001;
            if (Math.abs(lv8.getX()) < 1.0000001 && Math.abs(lv8.getY()) < 1.0000001 && Math.abs(lv8.getZ()) < 1.0000001) {
               Direction lv9 = lv4.getSide();
               this.player.updateLastActionTime();
               int i = this.player.world.getTopY();
               if (lv6.getY() < i) {
                  if (this.requestedTeleportPos == null && this.player.squaredDistanceTo((double)lv6.getX() + 0.5, (double)lv6.getY() + 0.5, (double)lv6.getZ() + 0.5) < 64.0 && lv.canPlayerModifyAt(this.player, lv6)) {
                     ActionResult lv10 = this.player.interactionManager.interactBlock(this.player, lv, lv3, lv2, lv4);
                     if (lv9 == Direction.UP && !lv10.isAccepted() && lv6.getY() >= i - 1 && canPlace(this.player, lv3)) {
                        Text lv11 = Text.translatable("build.tooHigh", i - 1).formatted(Formatting.RED);
                        this.player.sendMessageToClient(lv11, true);
                     } else if (lv10.shouldSwingHand()) {
                        this.player.swingHand(lv2, true);
                     }
                  }
               } else {
                  Text lv12 = Text.translatable("build.tooHigh", i - 1).formatted(Formatting.RED);
                  this.player.sendMessageToClient(lv12, true);
               }

               this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(lv, lv6));
               this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(lv, lv6.offset(lv9)));
            } else {
               LOGGER.warn("Rejecting UseItemOnPacket from {}: Location {} too far away from hit block {}.", new Object[]{this.player.getGameProfile().getName(), lv5, lv6});
            }
         }
      }
   }

   public void onPlayerInteractItem(PlayerInteractItemC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      this.updateSequence(packet.getSequence());
      ServerWorld lv = this.player.getWorld();
      Hand lv2 = packet.getHand();
      ItemStack lv3 = this.player.getStackInHand(lv2);
      this.player.updateLastActionTime();
      if (!lv3.isEmpty() && lv3.isItemEnabled(lv.getEnabledFeatures())) {
         ActionResult lv4 = this.player.interactionManager.interactItem(this.player, lv, lv3, lv2);
         if (lv4.shouldSwingHand()) {
            this.player.swingHand(lv2, true);
         }

      }
   }

   public void onSpectatorTeleport(SpectatorTeleportC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      if (this.player.isSpectator()) {
         Iterator var2 = this.server.getWorlds().iterator();

         while(var2.hasNext()) {
            ServerWorld lv = (ServerWorld)var2.next();
            Entity lv2 = packet.getTarget(lv);
            if (lv2 != null) {
               this.player.teleport(lv, lv2.getX(), lv2.getY(), lv2.getZ(), lv2.getYaw(), lv2.getPitch());
               return;
            }
         }
      }

   }

   public void onResourcePackStatus(ResourcePackStatusC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      if (packet.getStatus() == ResourcePackStatusC2SPacket.Status.DECLINED && this.server.requireResourcePack()) {
         LOGGER.info("Disconnecting {} due to resource pack rejection", this.player.getName());
         this.disconnect(Text.translatable("multiplayer.requiredTexturePrompt.disconnect"));
      }

   }

   public void onBoatPaddleState(BoatPaddleStateC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      Entity lv = this.player.getControllingVehicle();
      if (lv instanceof BoatEntity lv2) {
         lv2.setPaddleMovings(packet.isLeftPaddling(), packet.isRightPaddling());
      }

   }

   public void onPong(PlayPongC2SPacket packet) {
   }

   public void onDisconnected(Text reason) {
      this.messageChainTaskQueue.close();
      LOGGER.info("{} lost connection: {}", this.player.getName().getString(), reason.getString());
      this.server.forcePlayerSampleUpdate();
      this.server.getPlayerManager().broadcast(Text.translatable("multiplayer.player.left", this.player.getDisplayName()).formatted(Formatting.YELLOW), false);
      this.player.onDisconnect();
      this.server.getPlayerManager().remove(this.player);
      this.player.getTextStream().onDisconnect();
      if (this.isHost()) {
         LOGGER.info("Stopping singleplayer server as player logged out");
         this.server.stop(false);
      }

   }

   public void updateSequence(int sequence) {
      if (sequence < 0) {
         throw new IllegalArgumentException("Expected packet sequence nr >= 0");
      } else {
         this.sequence = Math.max(sequence, this.sequence);
      }
   }

   public void sendPacket(Packet packet) {
      this.sendPacket(packet, (PacketCallbacks)null);
   }

   public void sendPacket(Packet packet, @Nullable PacketCallbacks callbacks) {
      try {
         this.connection.send(packet, callbacks);
      } catch (Throwable var6) {
         CrashReport lv = CrashReport.create(var6, "Sending packet");
         CrashReportSection lv2 = lv.addElement("Packet being sent");
         lv2.add("Packet class", () -> {
            return packet.getClass().getCanonicalName();
         });
         throw new CrashException(lv);
      }
   }

   public void onUpdateSelectedSlot(UpdateSelectedSlotC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      if (packet.getSelectedSlot() >= 0 && packet.getSelectedSlot() < PlayerInventory.getHotbarSize()) {
         if (this.player.getInventory().selectedSlot != packet.getSelectedSlot() && this.player.getActiveHand() == Hand.MAIN_HAND) {
            this.player.clearActiveItem();
         }

         this.player.getInventory().selectedSlot = packet.getSelectedSlot();
         this.player.updateLastActionTime();
      } else {
         LOGGER.warn("{} tried to set an invalid carried item", this.player.getName().getString());
      }
   }

   public void onChatMessage(ChatMessageC2SPacket packet) {
      if (hasIllegalCharacter(packet.chatMessage())) {
         this.disconnect(Text.translatable("multiplayer.disconnect.illegal_characters"));
      } else {
         Optional optional = this.validateMessage(packet.chatMessage(), packet.timestamp(), packet.acknowledgment());
         if (optional.isPresent()) {
            this.server.submit(() -> {
               SignedMessage lv;
               try {
                  lv = this.getSignedMessage(packet, (LastSeenMessageList)optional.get());
               } catch (MessageChain.MessageChainException var6) {
                  this.handleMessageChainException(var6);
                  return;
               }

               CompletableFuture completableFuture = this.filterText(lv.getSignedContent());
               CompletableFuture completableFuture2 = this.server.getMessageDecorator().decorate(this.player, lv.getContent());
               this.messageChainTaskQueue.append((executor) -> {
                  return CompletableFuture.allOf(completableFuture, completableFuture2).thenAcceptAsync((void_) -> {
                     SignedMessage lvx = lv.withUnsignedContent((Text)completableFuture2.join()).withFilterMask(((FilteredMessage)completableFuture.join()).mask());
                     this.handleDecoratedMessage(lvx);
                  }, executor);
               });
            });
         }

      }
   }

   public void onCommandExecution(CommandExecutionC2SPacket packet) {
      if (hasIllegalCharacter(packet.command())) {
         this.disconnect(Text.translatable("multiplayer.disconnect.illegal_characters"));
      } else {
         Optional optional = this.validateMessage(packet.command(), packet.timestamp(), packet.acknowledgment());
         if (optional.isPresent()) {
            this.server.submit(() -> {
               this.handleCommandExecution(packet, (LastSeenMessageList)optional.get());
               this.checkForSpam();
            });
         }

      }
   }

   private void handleCommandExecution(CommandExecutionC2SPacket packet, LastSeenMessageList lastSeenMessages) {
      ParseResults parseResults = this.parse(packet.command());

      Map map;
      try {
         map = this.collectArgumentMessages(packet, SignedArgumentList.of(parseResults), lastSeenMessages);
      } catch (MessageChain.MessageChainException var6) {
         this.handleMessageChainException(var6);
         return;
      }

      SignedCommandArguments lv2 = new SignedCommandArguments.Impl(map);
      parseResults = CommandManager.withCommandSource(parseResults, (source) -> {
         return source.withSignedArguments(lv2);
      });
      this.server.getCommandManager().execute(parseResults, packet.command());
   }

   private void handleMessageChainException(MessageChain.MessageChainException exception) {
      if (exception.shouldDisconnect()) {
         this.disconnect(exception.getMessageText());
      } else {
         this.player.sendMessage(exception.getMessageText().copy().formatted(Formatting.RED));
      }

   }

   private Map collectArgumentMessages(CommandExecutionC2SPacket packet, SignedArgumentList arguments, LastSeenMessageList lastSeenMessages) throws MessageChain.MessageChainException {
      Map map = new Object2ObjectOpenHashMap();
      Iterator var5 = arguments.arguments().iterator();

      while(var5.hasNext()) {
         SignedArgumentList.ParsedArgument lv = (SignedArgumentList.ParsedArgument)var5.next();
         MessageSignatureData lv2 = packet.argumentSignatures().get(lv.getNodeName());
         MessageBody lv3 = new MessageBody(lv.value(), packet.timestamp(), packet.salt(), lastSeenMessages);
         map.put(lv.getNodeName(), this.messageUnpacker.unpack(lv2, lv3));
      }

      return map;
   }

   private ParseResults parse(String command) {
      CommandDispatcher commandDispatcher = this.server.getCommandManager().getDispatcher();
      return commandDispatcher.parse(command, this.player.getCommandSource());
   }

   private Optional validateMessage(String message, Instant timestamp, LastSeenMessageList.Acknowledgment acknowledgment) {
      if (!this.isInProperOrder(timestamp)) {
         LOGGER.warn("{} sent out-of-order chat: '{}'", this.player.getName().getString(), message);
         this.disconnect(Text.translatable("multiplayer.disconnect.out_of_order_chat"));
         return Optional.empty();
      } else {
         Optional optional = this.validateAcknowledgment(acknowledgment);
         if (this.player.getClientChatVisibility() == ChatVisibility.HIDDEN) {
            this.sendPacket(new GameMessageS2CPacket(Text.translatable("chat.disabled.options").formatted(Formatting.RED), false));
            return Optional.empty();
         } else {
            this.player.updateLastActionTime();
            return optional;
         }
      }
   }

   private Optional validateAcknowledgment(LastSeenMessageList.Acknowledgment acknowledgment) {
      synchronized(this.acknowledgmentValidator) {
         Optional optional = this.acknowledgmentValidator.validate(acknowledgment);
         if (optional.isEmpty()) {
            LOGGER.warn("Failed to validate message acknowledgements from {}", this.player.getName().getString());
            this.disconnect(CHAT_VALIDATION_FAILED_TEXT);
         }

         return optional;
      }
   }

   private boolean isInProperOrder(Instant timestamp) {
      Instant instant2;
      do {
         instant2 = (Instant)this.lastMessageTimestamp.get();
         if (timestamp.isBefore(instant2)) {
            return false;
         }
      } while(!this.lastMessageTimestamp.compareAndSet(instant2, timestamp));

      return true;
   }

   private static boolean hasIllegalCharacter(String message) {
      for(int i = 0; i < message.length(); ++i) {
         if (!SharedConstants.isValidChar(message.charAt(i))) {
            return true;
         }
      }

      return false;
   }

   private SignedMessage getSignedMessage(ChatMessageC2SPacket packet, LastSeenMessageList lastSeenMessages) throws MessageChain.MessageChainException {
      MessageBody lv = new MessageBody(packet.chatMessage(), packet.timestamp(), packet.salt(), lastSeenMessages);
      return this.messageUnpacker.unpack(packet.signature(), lv);
   }

   private void handleDecoratedMessage(SignedMessage message) {
      this.server.getPlayerManager().broadcast(message, this.player, MessageType.params(MessageType.CHAT, (Entity)this.player));
      this.checkForSpam();
   }

   private void checkForSpam() {
      this.messageCooldown += 20;
      if (this.messageCooldown > 200 && !this.server.getPlayerManager().isOperator(this.player.getGameProfile())) {
         this.disconnect(Text.translatable("disconnect.spam"));
      }

   }

   public void onMessageAcknowledgment(MessageAcknowledgmentC2SPacket packet) {
      synchronized(this.acknowledgmentValidator) {
         if (!this.acknowledgmentValidator.removeUntil(packet.offset())) {
            LOGGER.warn("Failed to validate message acknowledgements from {}", this.player.getName().getString());
            this.disconnect(CHAT_VALIDATION_FAILED_TEXT);
         }

      }
   }

   public void onHandSwing(HandSwingC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      this.player.updateLastActionTime();
      this.player.swingHand(packet.getHand());
   }

   public void onClientCommand(ClientCommandC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      this.player.updateLastActionTime();
      Entity var3;
      switch (packet.getMode()) {
         case PRESS_SHIFT_KEY:
            this.player.setSneaking(true);
            break;
         case RELEASE_SHIFT_KEY:
            this.player.setSneaking(false);
            break;
         case START_SPRINTING:
            this.player.setSprinting(true);
            break;
         case STOP_SPRINTING:
            this.player.setSprinting(false);
            break;
         case STOP_SLEEPING:
            if (this.player.isSleeping()) {
               this.player.wakeUp(false, true);
               this.requestedTeleportPos = this.player.getPos();
            }
            break;
         case START_RIDING_JUMP:
            var3 = this.player.getControllingVehicle();
            if (var3 instanceof JumpingMount lv) {
               int i = packet.getMountJumpHeight();
               if (lv.canJump() && i > 0) {
                  lv.startJumping(i);
               }
            }
            break;
         case STOP_RIDING_JUMP:
            var3 = this.player.getControllingVehicle();
            if (var3 instanceof JumpingMount lv) {
               lv.stopJumping();
            }
            break;
         case OPEN_INVENTORY:
            var3 = this.player.getVehicle();
            if (var3 instanceof RideableInventory lv2) {
               lv2.openInventory(this.player);
            }
            break;
         case START_FALL_FLYING:
            if (!this.player.checkFallFlying()) {
               this.player.stopFallFlying();
            }
            break;
         default:
            throw new IllegalArgumentException("Invalid client command!");
      }

   }

   public void addPendingAcknowledgment(SignedMessage message) {
      MessageSignatureData lv = message.signature();
      if (lv != null) {
         this.signatureStorage.add(message);
         int i;
         synchronized(this.acknowledgmentValidator) {
            this.acknowledgmentValidator.addPending(lv);
            i = this.acknowledgmentValidator.getMessageCount();
         }

         if (i > 4096) {
            this.disconnect(Text.translatable("multiplayer.disconnect.too_many_pending_chats"));
         }

      }
   }

   public void sendChatMessage(SignedMessage message, MessageType.Parameters params) {
      this.sendPacket(new ChatMessageS2CPacket(message.link().sender(), message.link().index(), message.signature(), message.signedBody().toSerialized(this.signatureStorage), message.unsignedContent(), message.filterMask(), params.toSerialized(this.player.world.getRegistryManager())));
      this.addPendingAcknowledgment(message);
   }

   public void sendProfilelessChatMessage(Text message, MessageType.Parameters params) {
      this.sendPacket(new ProfilelessChatMessageS2CPacket(message, params.toSerialized(this.player.world.getRegistryManager())));
   }

   public SocketAddress getConnectionAddress() {
      return this.connection.getAddress();
   }

   public void onPlayerInteractEntity(PlayerInteractEntityC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      final ServerWorld lv = this.player.getWorld();
      final Entity lv2 = packet.getEntity(lv);
      this.player.updateLastActionTime();
      this.player.setSneaking(packet.isPlayerSneaking());
      if (lv2 != null) {
         if (!lv.getWorldBorder().contains(lv2.getBlockPos())) {
            return;
         }

         Box lv3 = lv2.getBoundingBox();
         if (lv3.squaredMagnitude(this.player.getEyePos()) < MAX_BREAK_SQUARED_DISTANCE) {
            packet.handle(new PlayerInteractEntityC2SPacket.Handler() {
               private void processInteract(Hand hand, Interaction action) {
                  ItemStack lvx = ServerPlayNetworkHandler.this.player.getStackInHand(hand);
                  if (lvx.isItemEnabled(lv.getEnabledFeatures())) {
                     ItemStack lv2x = lvx.copy();
                     ActionResult lv3 = action.run(ServerPlayNetworkHandler.this.player, lv2, hand);
                     if (lv3.isAccepted()) {
                        Criteria.PLAYER_INTERACTED_WITH_ENTITY.trigger(ServerPlayNetworkHandler.this.player, lv2x, lv2);
                        if (lv3.shouldSwingHand()) {
                           ServerPlayNetworkHandler.this.player.swingHand(hand, true);
                        }
                     }

                  }
               }

               public void interact(Hand hand) {
                  this.processInteract(hand, PlayerEntity::interact);
               }

               public void interactAt(Hand hand, Vec3d pos) {
                  this.processInteract(hand, (player, entity, handx) -> {
                     return entity.interactAt(player, pos, handx);
                  });
               }

               public void attack() {
                  if (!(lv2 instanceof ItemEntity) && !(lv2 instanceof ExperienceOrbEntity) && !(lv2 instanceof PersistentProjectileEntity) && lv2 != ServerPlayNetworkHandler.this.player) {
                     ItemStack lvx = ServerPlayNetworkHandler.this.player.getStackInHand(Hand.MAIN_HAND);
                     if (lvx.isItemEnabled(lv.getEnabledFeatures())) {
                        ServerPlayNetworkHandler.this.player.attack(lv2);
                     }
                  } else {
                     ServerPlayNetworkHandler.this.disconnect(Text.translatable("multiplayer.disconnect.invalid_entity_attacked"));
                     ServerPlayNetworkHandler.LOGGER.warn("Player {} tried to attack an invalid entity", ServerPlayNetworkHandler.this.player.getName().getString());
                  }
               }
            });
         }
      }

   }

   public void onClientStatus(ClientStatusC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      this.player.updateLastActionTime();
      ClientStatusC2SPacket.Mode lv = packet.getMode();
      switch (lv) {
         case PERFORM_RESPAWN:
            if (this.player.notInAnyWorld) {
               this.player.notInAnyWorld = false;
               this.player = this.server.getPlayerManager().respawnPlayer(this.player, true);
               Criteria.CHANGED_DIMENSION.trigger(this.player, World.END, World.OVERWORLD);
            } else {
               if (this.player.getHealth() > 0.0F) {
                  return;
               }

               this.player = this.server.getPlayerManager().respawnPlayer(this.player, false);
               if (this.server.isHardcore()) {
                  this.player.changeGameMode(GameMode.SPECTATOR);
                  ((GameRules.BooleanRule)this.player.getWorld().getGameRules().get(GameRules.SPECTATORS_GENERATE_CHUNKS)).set(false, this.server);
               }
            }
            break;
         case REQUEST_STATS:
            this.player.getStatHandler().sendStats(this.player);
      }

   }

   public void onCloseHandledScreen(CloseHandledScreenC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      this.player.onHandledScreenClosed();
   }

   public void onClickSlot(ClickSlotC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      this.player.updateLastActionTime();
      if (this.player.currentScreenHandler.syncId == packet.getSyncId()) {
         if (this.player.isSpectator()) {
            this.player.currentScreenHandler.syncState();
         } else if (!this.player.currentScreenHandler.canUse(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.currentScreenHandler);
         } else {
            int i = packet.getSlot();
            if (!this.player.currentScreenHandler.isValid(i)) {
               LOGGER.debug("Player {} clicked invalid slot index: {}, available slots: {}", new Object[]{this.player.getName(), i, this.player.currentScreenHandler.slots.size()});
            } else {
               boolean bl = packet.getRevision() != this.player.currentScreenHandler.getRevision();
               this.player.currentScreenHandler.disableSyncing();
               this.player.currentScreenHandler.onSlotClick(i, packet.getButton(), packet.getActionType(), this.player);
               ObjectIterator var4 = Int2ObjectMaps.fastIterable(packet.getModifiedStacks()).iterator();

               while(var4.hasNext()) {
                  Int2ObjectMap.Entry entry = (Int2ObjectMap.Entry)var4.next();
                  this.player.currentScreenHandler.setPreviousTrackedSlotMutable(entry.getIntKey(), (ItemStack)entry.getValue());
               }

               this.player.currentScreenHandler.setPreviousCursorStack(packet.getStack());
               this.player.currentScreenHandler.enableSyncing();
               if (bl) {
                  this.player.currentScreenHandler.updateToClient();
               } else {
                  this.player.currentScreenHandler.sendContentUpdates();
               }

            }
         }
      }
   }

   public void onCraftRequest(CraftRequestC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      this.player.updateLastActionTime();
      if (!this.player.isSpectator() && this.player.currentScreenHandler.syncId == packet.getSyncId() && this.player.currentScreenHandler instanceof AbstractRecipeScreenHandler) {
         if (!this.player.currentScreenHandler.canUse(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.currentScreenHandler);
         } else {
            this.server.getRecipeManager().get(packet.getRecipe()).ifPresent((recipe) -> {
               ((AbstractRecipeScreenHandler)this.player.currentScreenHandler).fillInputSlots(packet.shouldCraftAll(), recipe, this.player);
            });
         }
      }
   }

   public void onButtonClick(ButtonClickC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      this.player.updateLastActionTime();
      if (this.player.currentScreenHandler.syncId == packet.getSyncId() && !this.player.isSpectator()) {
         if (!this.player.currentScreenHandler.canUse(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.currentScreenHandler);
         } else {
            boolean bl = this.player.currentScreenHandler.onButtonClick(this.player, packet.getButtonId());
            if (bl) {
               this.player.currentScreenHandler.sendContentUpdates();
            }

         }
      }
   }

   public void onCreativeInventoryAction(CreativeInventoryActionC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      if (this.player.interactionManager.isCreative()) {
         boolean bl = packet.getSlot() < 0;
         ItemStack lv = packet.getItemStack();
         if (!lv.isItemEnabled(this.player.getWorld().getEnabledFeatures())) {
            return;
         }

         NbtCompound lv2 = BlockItem.getBlockEntityNbt(lv);
         if (!lv.isEmpty() && lv2 != null && lv2.contains("x") && lv2.contains("y") && lv2.contains("z")) {
            BlockPos lv3 = BlockEntity.posFromNbt(lv2);
            if (this.player.world.canSetBlock(lv3)) {
               BlockEntity lv4 = this.player.world.getBlockEntity(lv3);
               if (lv4 != null) {
                  lv4.setStackNbt(lv);
               }
            }
         }

         boolean bl2 = packet.getSlot() >= 1 && packet.getSlot() <= 45;
         boolean bl3 = lv.isEmpty() || lv.getDamage() >= 0 && lv.getCount() <= 64 && !lv.isEmpty();
         if (bl2 && bl3) {
            this.player.playerScreenHandler.getSlot(packet.getSlot()).setStack(lv);
            this.player.playerScreenHandler.sendContentUpdates();
         } else if (bl && bl3 && this.creativeItemDropThreshold < 200) {
            this.creativeItemDropThreshold += 20;
            this.player.dropItem(lv, true);
         }
      }

   }

   public void onUpdateSign(UpdateSignC2SPacket packet) {
      List list = (List)Stream.of(packet.getText()).map(Formatting::strip).collect(Collectors.toList());
      this.filterTexts(list).thenAcceptAsync((texts) -> {
         this.onSignUpdate(packet, texts);
      }, this.server);
   }

   private void onSignUpdate(UpdateSignC2SPacket packet, List signText) {
      this.player.updateLastActionTime();
      ServerWorld lv = this.player.getWorld();
      BlockPos lv2 = packet.getPos();
      if (lv.isChunkLoaded(lv2)) {
         BlockEntity lv3 = lv.getBlockEntity(lv2);
         if (!(lv3 instanceof SignBlockEntity)) {
            return;
         }

         SignBlockEntity lv4 = (SignBlockEntity)lv3;
         lv4.tryChangeText(this.player, packet.isFront(), signText);
      }

   }

   public void onKeepAlive(KeepAliveC2SPacket packet) {
      if (this.waitingForKeepAlive && packet.getId() == this.keepAliveId) {
         int i = (int)(Util.getMeasuringTimeMs() - this.lastKeepAliveTime);
         this.player.pingMilliseconds = (this.player.pingMilliseconds * 3 + i) / 4;
         this.waitingForKeepAlive = false;
      } else if (!this.isHost()) {
         this.disconnect(Text.translatable("disconnect.timeout"));
      }

   }

   public void onUpdatePlayerAbilities(UpdatePlayerAbilitiesC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      this.player.getAbilities().flying = packet.isFlying() && this.player.getAbilities().allowFlying;
   }

   public void onClientSettings(ClientSettingsC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      this.player.setClientSettings(packet);
   }

   public void onCustomPayload(CustomPayloadC2SPacket packet) {
   }

   public void onUpdateDifficulty(UpdateDifficultyC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      if (this.player.hasPermissionLevel(2) || this.isHost()) {
         this.server.setDifficulty(packet.getDifficulty(), false);
      }
   }

   public void onUpdateDifficultyLock(UpdateDifficultyLockC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      if (this.player.hasPermissionLevel(2) || this.isHost()) {
         this.server.setDifficultyLocked(packet.isDifficultyLocked());
      }
   }

   public void onPlayerSession(PlayerSessionC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getWorld());
      PublicPlayerSession.Serialized lv = packet.chatSession();
      PlayerPublicKey.PublicKeyData lv2 = this.session != null ? this.session.publicKeyData().data() : null;
      PlayerPublicKey.PublicKeyData lv3 = lv.publicKeyData();
      if (!Objects.equals(lv2, lv3)) {
         if (lv2 != null && lv3.expiresAt().isBefore(lv2.expiresAt())) {
            this.disconnect(PlayerPublicKey.EXPIRED_PUBLIC_KEY_TEXT);
         } else {
            try {
               SignatureVerifier lv4 = this.server.getServicesSignatureVerifier();
               this.setSession(lv.toSession(this.player.getGameProfile(), lv4, Duration.ZERO));
            } catch (PlayerPublicKey.PublicKeyException var6) {
               LOGGER.error("Failed to validate profile key: {}", var6.getMessage());
               this.disconnect(var6.getMessageText());
            }

         }
      }
   }

   private void setSession(PublicPlayerSession session) {
      this.session = session;
      this.messageUnpacker = session.createUnpacker(this.player.getUuid());
      this.messageChainTaskQueue.append((executor) -> {
         this.player.setSession(session);
         this.server.getPlayerManager().sendToAll(new PlayerListS2CPacket(EnumSet.of(PlayerListS2CPacket.Action.INITIALIZE_CHAT), List.of(this.player)));
         return CompletableFuture.completedFuture((Object)null);
      });
   }

   public ServerPlayerEntity getPlayer() {
      return this.player;
   }

   @FunctionalInterface
   private interface Interaction {
      ActionResult run(ServerPlayerEntity player, Entity entity, Hand hand);
   }
}
