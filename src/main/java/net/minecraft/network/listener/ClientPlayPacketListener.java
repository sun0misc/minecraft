/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.listener;

import net.minecraft.network.NetworkPhase;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.listener.ClientPingResultPacketListener;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.ChangeUnlockedRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ChatSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkBiomeDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkRenderDistanceCenterS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkSentS2CPacket;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CraftFailedResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.DamageTiltS2CPacket;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.DebugSampleS2CPacket;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.EndCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterReconfigurationS2CPacket;
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
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.LookAtS2CPacket;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.NbtQueryResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenHorseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenWrittenBookS2CPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
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
import net.minecraft.network.packet.s2c.play.ProfilelessChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ProjectilePowerS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreResetS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SelectAdvancementTabS2CPacket;
import net.minecraft.network.packet.s2c.play.ServerMetadataS2CPacket;
import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.network.packet.s2c.play.SimulationDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.StartChunkSendS2CPacket;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.network.packet.s2c.play.TickStepS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateTickRateS2CPacket;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderCenterChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInitializeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInterpolateSizeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderSizeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningBlocksChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningTimeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public interface ClientPlayPacketListener
extends ClientPingResultPacketListener,
ClientCommonPacketListener {
    @Override
    default public NetworkPhase getPhase() {
        return NetworkPhase.PLAY;
    }

    public void onEntitySpawn(EntitySpawnS2CPacket var1);

    public void onExperienceOrbSpawn(ExperienceOrbSpawnS2CPacket var1);

    public void onScoreboardObjectiveUpdate(ScoreboardObjectiveUpdateS2CPacket var1);

    public void onEntityAnimation(EntityAnimationS2CPacket var1);

    public void onDamageTilt(DamageTiltS2CPacket var1);

    public void onStatistics(StatisticsS2CPacket var1);

    public void onUnlockRecipes(ChangeUnlockedRecipesS2CPacket var1);

    public void onBlockBreakingProgress(BlockBreakingProgressS2CPacket var1);

    public void onSignEditorOpen(SignEditorOpenS2CPacket var1);

    public void onBlockEntityUpdate(BlockEntityUpdateS2CPacket var1);

    public void onBlockEvent(BlockEventS2CPacket var1);

    public void onBlockUpdate(BlockUpdateS2CPacket var1);

    public void onGameMessage(GameMessageS2CPacket var1);

    public void onChatMessage(ChatMessageS2CPacket var1);

    public void onProfilelessChatMessage(ProfilelessChatMessageS2CPacket var1);

    public void onRemoveMessage(RemoveMessageS2CPacket var1);

    public void onChunkDeltaUpdate(ChunkDeltaUpdateS2CPacket var1);

    public void onMapUpdate(MapUpdateS2CPacket var1);

    public void onCloseScreen(CloseScreenS2CPacket var1);

    public void onInventory(InventoryS2CPacket var1);

    public void onOpenHorseScreen(OpenHorseScreenS2CPacket var1);

    public void onScreenHandlerPropertyUpdate(ScreenHandlerPropertyUpdateS2CPacket var1);

    public void onScreenHandlerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket var1);

    public void onEntityStatus(EntityStatusS2CPacket var1);

    public void onEntityAttach(EntityAttachS2CPacket var1);

    public void onEntityPassengersSet(EntityPassengersSetS2CPacket var1);

    public void onExplosion(ExplosionS2CPacket var1);

    public void onGameStateChange(GameStateChangeS2CPacket var1);

    public void onChunkData(ChunkDataS2CPacket var1);

    public void onChunkBiomeData(ChunkBiomeDataS2CPacket var1);

    public void onUnloadChunk(UnloadChunkS2CPacket var1);

    public void onWorldEvent(WorldEventS2CPacket var1);

    public void onGameJoin(GameJoinS2CPacket var1);

    public void onEntity(EntityS2CPacket var1);

    public void onPlayerPositionLook(PlayerPositionLookS2CPacket var1);

    public void onParticle(ParticleS2CPacket var1);

    public void onPlayerAbilities(PlayerAbilitiesS2CPacket var1);

    public void onPlayerRemove(PlayerRemoveS2CPacket var1);

    public void onPlayerList(PlayerListS2CPacket var1);

    public void onEntitiesDestroy(EntitiesDestroyS2CPacket var1);

    public void onRemoveEntityStatusEffect(RemoveEntityStatusEffectS2CPacket var1);

    public void onPlayerRespawn(PlayerRespawnS2CPacket var1);

    public void onEntitySetHeadYaw(EntitySetHeadYawS2CPacket var1);

    public void onUpdateSelectedSlot(UpdateSelectedSlotS2CPacket var1);

    public void onScoreboardDisplay(ScoreboardDisplayS2CPacket var1);

    public void onEntityTrackerUpdate(EntityTrackerUpdateS2CPacket var1);

    public void onEntityVelocityUpdate(EntityVelocityUpdateS2CPacket var1);

    public void onEntityEquipmentUpdate(EntityEquipmentUpdateS2CPacket var1);

    public void onExperienceBarUpdate(ExperienceBarUpdateS2CPacket var1);

    public void onHealthUpdate(HealthUpdateS2CPacket var1);

    public void onTeam(TeamS2CPacket var1);

    public void onScoreboardScoreUpdate(ScoreboardScoreUpdateS2CPacket var1);

    public void onScoreboardScoreReset(ScoreboardScoreResetS2CPacket var1);

    public void onPlayerSpawnPosition(PlayerSpawnPositionS2CPacket var1);

    public void onWorldTimeUpdate(WorldTimeUpdateS2CPacket var1);

    public void onPlaySound(PlaySoundS2CPacket var1);

    public void onPlaySoundFromEntity(PlaySoundFromEntityS2CPacket var1);

    public void onItemPickupAnimation(ItemPickupAnimationS2CPacket var1);

    public void onEntityPosition(EntityPositionS2CPacket var1);

    public void onUpdateTickRate(UpdateTickRateS2CPacket var1);

    public void onTickStep(TickStepS2CPacket var1);

    public void onEntityAttributes(EntityAttributesS2CPacket var1);

    public void onEntityStatusEffect(EntityStatusEffectS2CPacket var1);

    public void onEndCombat(EndCombatS2CPacket var1);

    public void onEnterCombat(EnterCombatS2CPacket var1);

    public void onDeathMessage(DeathMessageS2CPacket var1);

    public void onDifficulty(DifficultyS2CPacket var1);

    public void onSetCameraEntity(SetCameraEntityS2CPacket var1);

    public void onWorldBorderInitialize(WorldBorderInitializeS2CPacket var1);

    public void onWorldBorderInterpolateSize(WorldBorderInterpolateSizeS2CPacket var1);

    public void onWorldBorderSizeChanged(WorldBorderSizeChangedS2CPacket var1);

    public void onWorldBorderWarningTimeChanged(WorldBorderWarningTimeChangedS2CPacket var1);

    public void onWorldBorderWarningBlocksChanged(WorldBorderWarningBlocksChangedS2CPacket var1);

    public void onWorldBorderCenterChanged(WorldBorderCenterChangedS2CPacket var1);

    public void onPlayerListHeader(PlayerListHeaderS2CPacket var1);

    public void onBossBar(BossBarS2CPacket var1);

    public void onCooldownUpdate(CooldownUpdateS2CPacket var1);

    public void onVehicleMove(VehicleMoveS2CPacket var1);

    public void onAdvancements(AdvancementUpdateS2CPacket var1);

    public void onSelectAdvancementTab(SelectAdvancementTabS2CPacket var1);

    public void onCraftFailedResponse(CraftFailedResponseS2CPacket var1);

    public void onCommandTree(CommandTreeS2CPacket var1);

    public void onStopSound(StopSoundS2CPacket var1);

    public void onCommandSuggestions(CommandSuggestionsS2CPacket var1);

    public void onSynchronizeRecipes(SynchronizeRecipesS2CPacket var1);

    public void onLookAt(LookAtS2CPacket var1);

    public void onNbtQueryResponse(NbtQueryResponseS2CPacket var1);

    public void onLightUpdate(LightUpdateS2CPacket var1);

    public void onOpenWrittenBook(OpenWrittenBookS2CPacket var1);

    public void onOpenScreen(OpenScreenS2CPacket var1);

    public void onSetTradeOffers(SetTradeOffersS2CPacket var1);

    public void onChunkLoadDistance(ChunkLoadDistanceS2CPacket var1);

    public void onSimulationDistance(SimulationDistanceS2CPacket var1);

    public void onChunkRenderDistanceCenter(ChunkRenderDistanceCenterS2CPacket var1);

    public void onPlayerActionResponse(PlayerActionResponseS2CPacket var1);

    public void onOverlayMessage(OverlayMessageS2CPacket var1);

    public void onSubtitle(SubtitleS2CPacket var1);

    public void onTitle(TitleS2CPacket var1);

    public void onTitleFade(TitleFadeS2CPacket var1);

    public void onTitleClear(ClearTitleS2CPacket var1);

    public void onServerMetadata(ServerMetadataS2CPacket var1);

    public void onChatSuggestions(ChatSuggestionsS2CPacket var1);

    public void onBundle(BundleS2CPacket var1);

    public void onEntityDamage(EntityDamageS2CPacket var1);

    public void onEnterReconfiguration(EnterReconfigurationS2CPacket var1);

    public void onStartChunkSend(StartChunkSendS2CPacket var1);

    public void onChunkSent(ChunkSentS2CPacket var1);

    public void onDebugSample(DebugSampleS2CPacket var1);

    public void onProjectilePower(ProjectilePowerS2CPacket var1);
}

