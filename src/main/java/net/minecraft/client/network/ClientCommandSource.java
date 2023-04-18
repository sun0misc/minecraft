package net.minecraft.client.network;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.ChatSuggestionsS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ClientCommandSource implements CommandSource {
   private final ClientPlayNetworkHandler networkHandler;
   private final MinecraftClient client;
   private int completionId = -1;
   @Nullable
   private CompletableFuture pendingCommandCompletion;
   private final Set chatSuggestions = new HashSet();

   public ClientCommandSource(ClientPlayNetworkHandler networkHandler, MinecraftClient client) {
      this.networkHandler = networkHandler;
      this.client = client;
   }

   public Collection getPlayerNames() {
      List list = Lists.newArrayList();
      Iterator var2 = this.networkHandler.getPlayerList().iterator();

      while(var2.hasNext()) {
         PlayerListEntry lv = (PlayerListEntry)var2.next();
         list.add(lv.getProfile().getName());
      }

      return list;
   }

   public Collection getChatSuggestions() {
      if (this.chatSuggestions.isEmpty()) {
         return this.getPlayerNames();
      } else {
         Set set = new HashSet(this.getPlayerNames());
         set.addAll(this.chatSuggestions);
         return set;
      }
   }

   public Collection getEntitySuggestions() {
      return (Collection)(this.client.crosshairTarget != null && this.client.crosshairTarget.getType() == HitResult.Type.ENTITY ? Collections.singleton(((EntityHitResult)this.client.crosshairTarget).getEntity().getUuidAsString()) : Collections.emptyList());
   }

   public Collection getTeamNames() {
      return this.networkHandler.getWorld().getScoreboard().getTeamNames();
   }

   public Stream getSoundIds() {
      return this.client.getSoundManager().getKeys().stream();
   }

   public Stream getRecipeIds() {
      return this.networkHandler.getRecipeManager().keys();
   }

   public boolean hasPermissionLevel(int level) {
      ClientPlayerEntity lv = this.client.player;
      return lv != null ? lv.hasPermissionLevel(level) : level == 0;
   }

   public CompletableFuture listIdSuggestions(RegistryKey registryRef, CommandSource.SuggestedIdType suggestedIdType, SuggestionsBuilder builder, CommandContext context) {
      return (CompletableFuture)this.getRegistryManager().getOptional(registryRef).map((registry) -> {
         this.suggestIdentifiers(registry, suggestedIdType, builder);
         return builder.buildFuture();
      }).orElseGet(() -> {
         return this.getCompletions(context);
      });
   }

   public CompletableFuture getCompletions(CommandContext context) {
      if (this.pendingCommandCompletion != null) {
         this.pendingCommandCompletion.cancel(false);
      }

      this.pendingCommandCompletion = new CompletableFuture();
      int i = ++this.completionId;
      this.networkHandler.sendPacket(new RequestCommandCompletionsC2SPacket(i, context.getInput()));
      return this.pendingCommandCompletion;
   }

   private static String format(double d) {
      return String.format(Locale.ROOT, "%.2f", d);
   }

   private static String format(int i) {
      return Integer.toString(i);
   }

   public Collection getBlockPositionSuggestions() {
      HitResult lv = this.client.crosshairTarget;
      if (lv != null && lv.getType() == HitResult.Type.BLOCK) {
         BlockPos lv2 = ((BlockHitResult)lv).getBlockPos();
         return Collections.singleton(new CommandSource.RelativePosition(format(lv2.getX()), format(lv2.getY()), format(lv2.getZ())));
      } else {
         return CommandSource.super.getBlockPositionSuggestions();
      }
   }

   public Collection getPositionSuggestions() {
      HitResult lv = this.client.crosshairTarget;
      if (lv != null && lv.getType() == HitResult.Type.BLOCK) {
         Vec3d lv2 = lv.getPos();
         return Collections.singleton(new CommandSource.RelativePosition(format(lv2.x), format(lv2.y), format(lv2.z)));
      } else {
         return CommandSource.super.getPositionSuggestions();
      }
   }

   public Set getWorldKeys() {
      return this.networkHandler.getWorldKeys();
   }

   public DynamicRegistryManager getRegistryManager() {
      return this.networkHandler.getRegistryManager();
   }

   public FeatureSet getEnabledFeatures() {
      return this.networkHandler.getEnabledFeatures();
   }

   public void onCommandSuggestions(int completionId, Suggestions suggestions) {
      if (completionId == this.completionId) {
         this.pendingCommandCompletion.complete(suggestions);
         this.pendingCommandCompletion = null;
         this.completionId = -1;
      }

   }

   public void onChatSuggestions(ChatSuggestionsS2CPacket.Action action, List suggestions) {
      switch (action) {
         case ADD:
            this.chatSuggestions.addAll(suggestions);
            break;
         case REMOVE:
            Set var10001 = this.chatSuggestions;
            Objects.requireNonNull(var10001);
            suggestions.forEach(var10001::remove);
            break;
         case SET:
            this.chatSuggestions.clear();
            this.chatSuggestions.addAll(suggestions);
      }

   }
}
