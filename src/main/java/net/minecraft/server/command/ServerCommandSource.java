package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedCommandArguments;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.thread.FutureQueue;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

public class ServerCommandSource implements CommandSource {
   public static final SimpleCommandExceptionType REQUIRES_PLAYER_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("permissions.requires.player"));
   public static final SimpleCommandExceptionType REQUIRES_ENTITY_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("permissions.requires.entity"));
   private final CommandOutput output;
   private final Vec3d position;
   private final ServerWorld world;
   private final int level;
   private final String name;
   private final Text displayName;
   private final MinecraftServer server;
   private final boolean silent;
   @Nullable
   private final Entity entity;
   @Nullable
   private final ResultConsumer resultConsumer;
   private final EntityAnchorArgumentType.EntityAnchor entityAnchor;
   private final Vec2f rotation;
   private final SignedCommandArguments signedArguments;
   private final FutureQueue messageChainTaskQueue;

   public ServerCommandSource(CommandOutput output, Vec3d pos, Vec2f rot, ServerWorld world, int level, String name, Text displayName, MinecraftServer server, @Nullable Entity entity) {
      this(output, pos, rot, world, level, name, displayName, server, entity, false, (context, success, result) -> {
      }, EntityAnchorArgumentType.EntityAnchor.FEET, SignedCommandArguments.EMPTY, FutureQueue.immediate(server));
   }

   protected ServerCommandSource(CommandOutput output, Vec3d pos, Vec2f rot, ServerWorld world, int level, String name, Text displayName, MinecraftServer server, @Nullable Entity entity, boolean silent, @Nullable ResultConsumer consumer, EntityAnchorArgumentType.EntityAnchor entityAnchor, SignedCommandArguments signedArguments, FutureQueue messageChainTaskQueue) {
      this.output = output;
      this.position = pos;
      this.world = world;
      this.silent = silent;
      this.entity = entity;
      this.level = level;
      this.name = name;
      this.displayName = displayName;
      this.server = server;
      this.resultConsumer = consumer;
      this.entityAnchor = entityAnchor;
      this.rotation = rot;
      this.signedArguments = signedArguments;
      this.messageChainTaskQueue = messageChainTaskQueue;
   }

   public ServerCommandSource withOutput(CommandOutput output) {
      return this.output == output ? this : new ServerCommandSource(output, this.position, this.rotation, this.world, this.level, this.name, this.displayName, this.server, this.entity, this.silent, this.resultConsumer, this.entityAnchor, this.signedArguments, this.messageChainTaskQueue);
   }

   public ServerCommandSource withEntity(Entity entity) {
      return this.entity == entity ? this : new ServerCommandSource(this.output, this.position, this.rotation, this.world, this.level, entity.getName().getString(), entity.getDisplayName(), this.server, entity, this.silent, this.resultConsumer, this.entityAnchor, this.signedArguments, this.messageChainTaskQueue);
   }

   public ServerCommandSource withPosition(Vec3d position) {
      return this.position.equals(position) ? this : new ServerCommandSource(this.output, position, this.rotation, this.world, this.level, this.name, this.displayName, this.server, this.entity, this.silent, this.resultConsumer, this.entityAnchor, this.signedArguments, this.messageChainTaskQueue);
   }

   public ServerCommandSource withRotation(Vec2f rotation) {
      return this.rotation.equals(rotation) ? this : new ServerCommandSource(this.output, this.position, rotation, this.world, this.level, this.name, this.displayName, this.server, this.entity, this.silent, this.resultConsumer, this.entityAnchor, this.signedArguments, this.messageChainTaskQueue);
   }

   public ServerCommandSource withConsumer(ResultConsumer consumer) {
      return Objects.equals(this.resultConsumer, consumer) ? this : new ServerCommandSource(this.output, this.position, this.rotation, this.world, this.level, this.name, this.displayName, this.server, this.entity, this.silent, consumer, this.entityAnchor, this.signedArguments, this.messageChainTaskQueue);
   }

   public ServerCommandSource mergeConsumers(ResultConsumer consumer, BinaryOperator merger) {
      ResultConsumer resultConsumer2 = (ResultConsumer)merger.apply(this.resultConsumer, consumer);
      return this.withConsumer(resultConsumer2);
   }

   public ServerCommandSource withSilent() {
      return !this.silent && !this.output.cannotBeSilenced() ? new ServerCommandSource(this.output, this.position, this.rotation, this.world, this.level, this.name, this.displayName, this.server, this.entity, true, this.resultConsumer, this.entityAnchor, this.signedArguments, this.messageChainTaskQueue) : this;
   }

   public ServerCommandSource withLevel(int level) {
      return level == this.level ? this : new ServerCommandSource(this.output, this.position, this.rotation, this.world, level, this.name, this.displayName, this.server, this.entity, this.silent, this.resultConsumer, this.entityAnchor, this.signedArguments, this.messageChainTaskQueue);
   }

   public ServerCommandSource withMaxLevel(int level) {
      return level <= this.level ? this : new ServerCommandSource(this.output, this.position, this.rotation, this.world, level, this.name, this.displayName, this.server, this.entity, this.silent, this.resultConsumer, this.entityAnchor, this.signedArguments, this.messageChainTaskQueue);
   }

   public ServerCommandSource withEntityAnchor(EntityAnchorArgumentType.EntityAnchor anchor) {
      return anchor == this.entityAnchor ? this : new ServerCommandSource(this.output, this.position, this.rotation, this.world, this.level, this.name, this.displayName, this.server, this.entity, this.silent, this.resultConsumer, anchor, this.signedArguments, this.messageChainTaskQueue);
   }

   public ServerCommandSource withWorld(ServerWorld world) {
      if (world == this.world) {
         return this;
      } else {
         double d = DimensionType.getCoordinateScaleFactor(this.world.getDimension(), world.getDimension());
         Vec3d lv = new Vec3d(this.position.x * d, this.position.y, this.position.z * d);
         return new ServerCommandSource(this.output, lv, this.rotation, world, this.level, this.name, this.displayName, this.server, this.entity, this.silent, this.resultConsumer, this.entityAnchor, this.signedArguments, this.messageChainTaskQueue);
      }
   }

   public ServerCommandSource withLookingAt(Entity entity, EntityAnchorArgumentType.EntityAnchor anchor) {
      return this.withLookingAt(anchor.positionAt(entity));
   }

   public ServerCommandSource withLookingAt(Vec3d position) {
      Vec3d lv = this.entityAnchor.positionAt(this);
      double d = position.x - lv.x;
      double e = position.y - lv.y;
      double f = position.z - lv.z;
      double g = Math.sqrt(d * d + f * f);
      float h = MathHelper.wrapDegrees((float)(-(MathHelper.atan2(e, g) * 57.2957763671875)));
      float i = MathHelper.wrapDegrees((float)(MathHelper.atan2(f, d) * 57.2957763671875) - 90.0F);
      return this.withRotation(new Vec2f(h, i));
   }

   public ServerCommandSource withSignedArguments(SignedCommandArguments signedArguments) {
      return signedArguments == this.signedArguments ? this : new ServerCommandSource(this.output, this.position, this.rotation, this.world, this.level, this.name, this.displayName, this.server, this.entity, this.silent, this.resultConsumer, this.entityAnchor, signedArguments, this.messageChainTaskQueue);
   }

   public ServerCommandSource withMessageChainTaskQueue(FutureQueue messageChainTaskQueue) {
      return messageChainTaskQueue == this.messageChainTaskQueue ? this : new ServerCommandSource(this.output, this.position, this.rotation, this.world, this.level, this.name, this.displayName, this.server, this.entity, this.silent, this.resultConsumer, this.entityAnchor, this.signedArguments, messageChainTaskQueue);
   }

   public Text getDisplayName() {
      return this.displayName;
   }

   public String getName() {
      return this.name;
   }

   public boolean hasPermissionLevel(int level) {
      return this.level >= level;
   }

   public Vec3d getPosition() {
      return this.position;
   }

   public ServerWorld getWorld() {
      return this.world;
   }

   @Nullable
   public Entity getEntity() {
      return this.entity;
   }

   public Entity getEntityOrThrow() throws CommandSyntaxException {
      if (this.entity == null) {
         throw REQUIRES_ENTITY_EXCEPTION.create();
      } else {
         return this.entity;
      }
   }

   public ServerPlayerEntity getPlayerOrThrow() throws CommandSyntaxException {
      Entity var2 = this.entity;
      if (var2 instanceof ServerPlayerEntity lv) {
         return lv;
      } else {
         throw REQUIRES_PLAYER_EXCEPTION.create();
      }
   }

   @Nullable
   public ServerPlayerEntity getPlayer() {
      Entity var2 = this.entity;
      ServerPlayerEntity var10000;
      if (var2 instanceof ServerPlayerEntity lv) {
         var10000 = lv;
      } else {
         var10000 = null;
      }

      return var10000;
   }

   public boolean isExecutedByPlayer() {
      return this.entity instanceof ServerPlayerEntity;
   }

   public Vec2f getRotation() {
      return this.rotation;
   }

   public MinecraftServer getServer() {
      return this.server;
   }

   public EntityAnchorArgumentType.EntityAnchor getEntityAnchor() {
      return this.entityAnchor;
   }

   public SignedCommandArguments getSignedArguments() {
      return this.signedArguments;
   }

   public FutureQueue getMessageChainTaskQueue() {
      return this.messageChainTaskQueue;
   }

   public boolean shouldFilterText(ServerPlayerEntity recipient) {
      ServerPlayerEntity lv = this.getPlayer();
      if (recipient == lv) {
         return false;
      } else {
         return lv != null && lv.shouldFilterText() || recipient.shouldFilterText();
      }
   }

   public void sendChatMessage(SentMessage message, boolean filterMaskEnabled, MessageType.Parameters params) {
      if (!this.silent) {
         ServerPlayerEntity lv = this.getPlayer();
         if (lv != null) {
            lv.sendChatMessage(message, filterMaskEnabled, params);
         } else {
            this.output.sendMessage(params.applyChatDecoration(message.getContent()));
         }

      }
   }

   public void sendMessage(Text message) {
      if (!this.silent) {
         ServerPlayerEntity lv = this.getPlayer();
         if (lv != null) {
            lv.sendMessage(message);
         } else {
            this.output.sendMessage(message);
         }

      }
   }

   public void sendFeedback(Text message, boolean broadcastToOps) {
      if (this.output.shouldReceiveFeedback() && !this.silent) {
         this.output.sendMessage(message);
      }

      if (broadcastToOps && this.output.shouldBroadcastConsoleToOps() && !this.silent) {
         this.sendToOps(message);
      }

   }

   private void sendToOps(Text message) {
      Text lv = Text.translatable("chat.type.admin", this.getDisplayName(), message).formatted(Formatting.GRAY, Formatting.ITALIC);
      if (this.server.getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
         Iterator var3 = this.server.getPlayerManager().getPlayerList().iterator();

         while(var3.hasNext()) {
            ServerPlayerEntity lv2 = (ServerPlayerEntity)var3.next();
            if (lv2 != this.output && this.server.getPlayerManager().isOperator(lv2.getGameProfile())) {
               lv2.sendMessage(lv);
            }
         }
      }

      if (this.output != this.server && this.server.getGameRules().getBoolean(GameRules.LOG_ADMIN_COMMANDS)) {
         this.server.sendMessage(lv);
      }

   }

   public void sendError(Text message) {
      if (this.output.shouldTrackOutput() && !this.silent) {
         this.output.sendMessage(Text.empty().append(message).formatted(Formatting.RED));
      }

   }

   public void onCommandComplete(CommandContext context, boolean success, int result) {
      if (this.resultConsumer != null) {
         this.resultConsumer.onCommandComplete(context, success, result);
      }

   }

   public Collection getPlayerNames() {
      return Lists.newArrayList(this.server.getPlayerNames());
   }

   public Collection getTeamNames() {
      return this.server.getScoreboard().getTeamNames();
   }

   public Stream getSoundIds() {
      return Registries.SOUND_EVENT.stream().map(SoundEvent::getId);
   }

   public Stream getRecipeIds() {
      return this.server.getRecipeManager().keys();
   }

   public CompletableFuture getCompletions(CommandContext context) {
      return Suggestions.empty();
   }

   public CompletableFuture listIdSuggestions(RegistryKey registryRef, CommandSource.SuggestedIdType suggestedIdType, SuggestionsBuilder builder, CommandContext context) {
      return (CompletableFuture)this.getRegistryManager().getOptional(registryRef).map((registry) -> {
         this.suggestIdentifiers(registry, suggestedIdType, builder);
         return builder.buildFuture();
      }).orElseGet(Suggestions::empty);
   }

   public Set getWorldKeys() {
      return this.server.getWorldRegistryKeys();
   }

   public DynamicRegistryManager getRegistryManager() {
      return this.server.getRegistryManager();
   }

   public FeatureSet getEnabledFeatures() {
      return this.world.getEnabledFeatures();
   }
}
