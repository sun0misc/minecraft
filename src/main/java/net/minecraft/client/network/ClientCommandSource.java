/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.network;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.ChatSuggestionsS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ClientCommandSource
implements CommandSource {
    private final ClientPlayNetworkHandler networkHandler;
    private final MinecraftClient client;
    private int completionId = -1;
    @Nullable
    private CompletableFuture<Suggestions> pendingCommandCompletion;
    private final Set<String> chatSuggestions = new HashSet<String>();

    public ClientCommandSource(ClientPlayNetworkHandler networkHandler, MinecraftClient client) {
        this.networkHandler = networkHandler;
        this.client = client;
    }

    @Override
    public Collection<String> getPlayerNames() {
        ArrayList<String> list = Lists.newArrayList();
        for (PlayerListEntry lv : this.networkHandler.getPlayerList()) {
            list.add(lv.getProfile().getName());
        }
        return list;
    }

    @Override
    public Collection<String> getChatSuggestions() {
        if (this.chatSuggestions.isEmpty()) {
            return this.getPlayerNames();
        }
        HashSet<String> set = new HashSet<String>(this.getPlayerNames());
        set.addAll(this.chatSuggestions);
        return set;
    }

    @Override
    public Collection<String> getEntitySuggestions() {
        if (this.client.crosshairTarget != null && this.client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            return Collections.singleton(((EntityHitResult)this.client.crosshairTarget).getEntity().getUuidAsString());
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<String> getTeamNames() {
        return this.networkHandler.getScoreboard().getTeamNames();
    }

    @Override
    public Stream<Identifier> getSoundIds() {
        return this.client.getSoundManager().getKeys().stream();
    }

    @Override
    public Stream<Identifier> getRecipeIds() {
        return this.networkHandler.getRecipeManager().keys();
    }

    @Override
    public boolean hasPermissionLevel(int level) {
        ClientPlayerEntity lv = this.client.player;
        return lv != null ? lv.hasPermissionLevel(level) : level == 0;
    }

    @Override
    public CompletableFuture<Suggestions> listIdSuggestions(RegistryKey<? extends Registry<?>> registryRef, CommandSource.SuggestedIdType suggestedIdType, SuggestionsBuilder builder, CommandContext<?> context) {
        return this.getRegistryManager().getOptional(registryRef).map(registry -> {
            this.suggestIdentifiers((Registry<?>)registry, suggestedIdType, builder);
            return builder.buildFuture();
        }).orElseGet(() -> this.getCompletions(context));
    }

    @Override
    public CompletableFuture<Suggestions> getCompletions(CommandContext<?> context) {
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

    @Override
    public Collection<CommandSource.RelativePosition> getBlockPositionSuggestions() {
        HitResult lv = this.client.crosshairTarget;
        if (lv == null || lv.getType() != HitResult.Type.BLOCK) {
            return CommandSource.super.getBlockPositionSuggestions();
        }
        BlockPos lv2 = ((BlockHitResult)lv).getBlockPos();
        return Collections.singleton(new CommandSource.RelativePosition(ClientCommandSource.format(lv2.getX()), ClientCommandSource.format(lv2.getY()), ClientCommandSource.format(lv2.getZ())));
    }

    @Override
    public Collection<CommandSource.RelativePosition> getPositionSuggestions() {
        HitResult lv = this.client.crosshairTarget;
        if (lv == null || lv.getType() != HitResult.Type.BLOCK) {
            return CommandSource.super.getPositionSuggestions();
        }
        Vec3d lv2 = lv.getPos();
        return Collections.singleton(new CommandSource.RelativePosition(ClientCommandSource.format(lv2.x), ClientCommandSource.format(lv2.y), ClientCommandSource.format(lv2.z)));
    }

    @Override
    public Set<RegistryKey<World>> getWorldKeys() {
        return this.networkHandler.getWorldKeys();
    }

    @Override
    public DynamicRegistryManager getRegistryManager() {
        return this.networkHandler.getRegistryManager();
    }

    @Override
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

    public void onChatSuggestions(ChatSuggestionsS2CPacket.Action action, List<String> suggestions) {
        switch (action) {
            case ADD: {
                this.chatSuggestions.addAll(suggestions);
                break;
            }
            case REMOVE: {
                suggestions.forEach(this.chatSuggestions::remove);
                break;
            }
            case SET: {
                this.chatSuggestions.clear();
                this.chatSuggestions.addAll(suggestions);
            }
        }
    }
}

