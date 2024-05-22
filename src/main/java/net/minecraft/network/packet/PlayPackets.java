/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet;

import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.c2s.play.AcknowledgeChunksC2SPacket;
import net.minecraft.network.packet.c2s.play.AcknowledgeReconfigurationC2SPacket;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.c2s.play.CraftRequestC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.DebugSampleSubscriptionC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.JigsawGeneratingC2SPacket;
import net.minecraft.network.packet.c2s.play.MessageAcknowledgmentC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
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
import net.minecraft.network.packet.c2s.play.SelectMerchantTradeC2SPacket;
import net.minecraft.network.packet.c2s.play.SlotChangedStateC2SPacket;
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
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.network.packet.s2c.play.BundleDelimiterS2CPacket;
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
import net.minecraft.util.Identifier;

public class PlayPackets {
    public static final PacketType<BundleS2CPacket> BUNDLE = PlayPackets.s2c("bundle");
    public static final PacketType<BundleDelimiterS2CPacket> BUNDLE_DELIMITER = PlayPackets.s2c("bundle_delimiter");
    public static final PacketType<EntitySpawnS2CPacket> ADD_ENTITY = PlayPackets.s2c("add_entity");
    public static final PacketType<ExperienceOrbSpawnS2CPacket> ADD_EXPERIENCE_ORB = PlayPackets.s2c("add_experience_orb");
    public static final PacketType<EntityAnimationS2CPacket> ANIMATE = PlayPackets.s2c("animate");
    public static final PacketType<StatisticsS2CPacket> AWARD_STATS = PlayPackets.s2c("award_stats");
    public static final PacketType<PlayerActionResponseS2CPacket> BLOCK_CHANGED_ACK = PlayPackets.s2c("block_changed_ack");
    public static final PacketType<BlockBreakingProgressS2CPacket> BLOCK_DESTRUCTION = PlayPackets.s2c("block_destruction");
    public static final PacketType<BlockEntityUpdateS2CPacket> BLOCK_ENTITY_DATA = PlayPackets.s2c("block_entity_data");
    public static final PacketType<BlockEventS2CPacket> BLOCK_EVENT = PlayPackets.s2c("block_event");
    public static final PacketType<BlockUpdateS2CPacket> BLOCK_UPDATE = PlayPackets.s2c("block_update");
    public static final PacketType<BossBarS2CPacket> BOSS_EVENT = PlayPackets.s2c("boss_event");
    public static final PacketType<DifficultyS2CPacket> CHANGE_DIFFICULTY_S2C = PlayPackets.s2c("change_difficulty");
    public static final PacketType<ChunkSentS2CPacket> CHUNK_BATCH_FINISHED = PlayPackets.s2c("chunk_batch_finished");
    public static final PacketType<StartChunkSendS2CPacket> CHUNK_BATCH_START = PlayPackets.s2c("chunk_batch_start");
    public static final PacketType<ChunkBiomeDataS2CPacket> CHUNKS_BIOMES = PlayPackets.s2c("chunks_biomes");
    public static final PacketType<ClearTitleS2CPacket> CLEAR_TITLES = PlayPackets.s2c("clear_titles");
    public static final PacketType<CommandSuggestionsS2CPacket> COMMAND_SUGGESTIONS = PlayPackets.s2c("command_suggestions");
    public static final PacketType<CommandTreeS2CPacket> COMMANDS = PlayPackets.s2c("commands");
    public static final PacketType<CloseScreenS2CPacket> CONTAINER_CLOSE_S2C = PlayPackets.s2c("container_close");
    public static final PacketType<InventoryS2CPacket> CONTAINER_SET_CONTENT = PlayPackets.s2c("container_set_content");
    public static final PacketType<ScreenHandlerPropertyUpdateS2CPacket> CONTAINER_SET_DATA = PlayPackets.s2c("container_set_data");
    public static final PacketType<ScreenHandlerSlotUpdateS2CPacket> CONTAINER_SET_SLOT = PlayPackets.s2c("container_set_slot");
    public static final PacketType<CooldownUpdateS2CPacket> COOLDOWN = PlayPackets.s2c("cooldown");
    public static final PacketType<ChatSuggestionsS2CPacket> CUSTOM_CHAT_COMPLETIONS = PlayPackets.s2c("custom_chat_completions");
    public static final PacketType<EntityDamageS2CPacket> DAMAGE_EVENT = PlayPackets.s2c("damage_event");
    public static final PacketType<DebugSampleS2CPacket> DEBUG_SAMPLE = PlayPackets.s2c("debug_sample");
    public static final PacketType<RemoveMessageS2CPacket> DELETE_CHAT = PlayPackets.s2c("delete_chat");
    public static final PacketType<ProfilelessChatMessageS2CPacket> DISGUISED_CHAT = PlayPackets.s2c("disguised_chat");
    public static final PacketType<EntityStatusS2CPacket> ENTITY_EVENT = PlayPackets.s2c("entity_event");
    public static final PacketType<ExplosionS2CPacket> EXPLODE = PlayPackets.s2c("explode");
    public static final PacketType<UnloadChunkS2CPacket> FORGET_LEVEL_CHUNK = PlayPackets.s2c("forget_level_chunk");
    public static final PacketType<GameStateChangeS2CPacket> GAME_EVENT = PlayPackets.s2c("game_event");
    public static final PacketType<OpenHorseScreenS2CPacket> HORSE_SCREEN_OPEN = PlayPackets.s2c("horse_screen_open");
    public static final PacketType<DamageTiltS2CPacket> HURT_ANIMATION = PlayPackets.s2c("hurt_animation");
    public static final PacketType<WorldBorderInitializeS2CPacket> INITIALIZE_BORDER = PlayPackets.s2c("initialize_border");
    public static final PacketType<ChunkDataS2CPacket> LEVEL_CHUNK_WITH_LIGHT = PlayPackets.s2c("level_chunk_with_light");
    public static final PacketType<WorldEventS2CPacket> LEVEL_EVENT = PlayPackets.s2c("level_event");
    public static final PacketType<ParticleS2CPacket> LEVEL_PARTICLES = PlayPackets.s2c("level_particles");
    public static final PacketType<LightUpdateS2CPacket> LIGHT_UPDATE = PlayPackets.s2c("light_update");
    public static final PacketType<GameJoinS2CPacket> LOGIN = PlayPackets.s2c("login");
    public static final PacketType<MapUpdateS2CPacket> MAP_ITEM_DATA = PlayPackets.s2c("map_item_data");
    public static final PacketType<SetTradeOffersS2CPacket> MERCHANT_OFFERS = PlayPackets.s2c("merchant_offers");
    public static final PacketType<EntityS2CPacket.MoveRelative> MOVE_ENTITY_POS = PlayPackets.s2c("move_entity_pos");
    public static final PacketType<EntityS2CPacket.RotateAndMoveRelative> MOVE_ENTITY_POS_ROT = PlayPackets.s2c("move_entity_pos_rot");
    public static final PacketType<EntityS2CPacket.Rotate> MOVE_ENTITY_ROT = PlayPackets.s2c("move_entity_rot");
    public static final PacketType<VehicleMoveS2CPacket> MOVE_VEHICLE_S2C = PlayPackets.s2c("move_vehicle");
    public static final PacketType<OpenWrittenBookS2CPacket> OPEN_BOOK = PlayPackets.s2c("open_book");
    public static final PacketType<OpenScreenS2CPacket> OPEN_SCREEN = PlayPackets.s2c("open_screen");
    public static final PacketType<SignEditorOpenS2CPacket> OPEN_SIGN_EDITOR = PlayPackets.s2c("open_sign_editor");
    public static final PacketType<CraftFailedResponseS2CPacket> PLACE_GHOST_RECIPE = PlayPackets.s2c("place_ghost_recipe");
    public static final PacketType<PlayerAbilitiesS2CPacket> PLAYER_ABILITIES_S2C = PlayPackets.s2c("player_abilities");
    public static final PacketType<ChatMessageS2CPacket> PLAYER_CHAT = PlayPackets.s2c("player_chat");
    public static final PacketType<EndCombatS2CPacket> PLAYER_COMBAT_END = PlayPackets.s2c("player_combat_end");
    public static final PacketType<EnterCombatS2CPacket> PLAYER_COMBAT_ENTER = PlayPackets.s2c("player_combat_enter");
    public static final PacketType<DeathMessageS2CPacket> PLAYER_COMBAT_KILL = PlayPackets.s2c("player_combat_kill");
    public static final PacketType<PlayerRemoveS2CPacket> PLAYER_INFO_REMOVE = PlayPackets.s2c("player_info_remove");
    public static final PacketType<PlayerListS2CPacket> PLAYER_INFO_UPDATE = PlayPackets.s2c("player_info_update");
    public static final PacketType<LookAtS2CPacket> PLAYER_LOOK_AT = PlayPackets.s2c("player_look_at");
    public static final PacketType<PlayerPositionLookS2CPacket> PLAYER_POSITION = PlayPackets.s2c("player_position");
    public static final PacketType<ChangeUnlockedRecipesS2CPacket> RECIPE = PlayPackets.s2c("recipe");
    public static final PacketType<EntitiesDestroyS2CPacket> REMOVE_ENTITIES = PlayPackets.s2c("remove_entities");
    public static final PacketType<RemoveEntityStatusEffectS2CPacket> REMOVE_MOB_EFFECT = PlayPackets.s2c("remove_mob_effect");
    public static final PacketType<PlayerRespawnS2CPacket> RESPAWN = PlayPackets.s2c("respawn");
    public static final PacketType<EntitySetHeadYawS2CPacket> ROTATE_HEAD = PlayPackets.s2c("rotate_head");
    public static final PacketType<ChunkDeltaUpdateS2CPacket> SECTION_BLOCKS_UPDATE = PlayPackets.s2c("section_blocks_update");
    public static final PacketType<SelectAdvancementTabS2CPacket> SELECT_ADVANCEMENTS_TAB = PlayPackets.s2c("select_advancements_tab");
    public static final PacketType<ServerMetadataS2CPacket> SERVER_DATA = PlayPackets.s2c("server_data");
    public static final PacketType<OverlayMessageS2CPacket> SET_ACTION_BAR_TEXT = PlayPackets.s2c("set_action_bar_text");
    public static final PacketType<WorldBorderCenterChangedS2CPacket> SET_BORDER_CENTER = PlayPackets.s2c("set_border_center");
    public static final PacketType<WorldBorderInterpolateSizeS2CPacket> SET_BORDER_LERP_SIZE = PlayPackets.s2c("set_border_lerp_size");
    public static final PacketType<WorldBorderSizeChangedS2CPacket> SET_BORDER_SIZE = PlayPackets.s2c("set_border_size");
    public static final PacketType<WorldBorderWarningTimeChangedS2CPacket> SET_BORDER_WARNING_DELAY = PlayPackets.s2c("set_border_warning_delay");
    public static final PacketType<WorldBorderWarningBlocksChangedS2CPacket> SET_BORDER_WARNING_DISTANCE = PlayPackets.s2c("set_border_warning_distance");
    public static final PacketType<SetCameraEntityS2CPacket> SET_CAMERA = PlayPackets.s2c("set_camera");
    public static final PacketType<UpdateSelectedSlotS2CPacket> SET_CARRIED_ITEM_S2C = PlayPackets.s2c("set_carried_item");
    public static final PacketType<ChunkRenderDistanceCenterS2CPacket> SET_CHUNK_CACHE_CENTER = PlayPackets.s2c("set_chunk_cache_center");
    public static final PacketType<ChunkLoadDistanceS2CPacket> SET_CHUNK_CACHE_RADIUS = PlayPackets.s2c("set_chunk_cache_radius");
    public static final PacketType<PlayerSpawnPositionS2CPacket> SET_DEFAULT_SPAWN_POSITION = PlayPackets.s2c("set_default_spawn_position");
    public static final PacketType<ScoreboardDisplayS2CPacket> SET_DISPLAY_OBJECTIVE = PlayPackets.s2c("set_display_objective");
    public static final PacketType<EntityTrackerUpdateS2CPacket> SET_ENTITY_DATA = PlayPackets.s2c("set_entity_data");
    public static final PacketType<EntityAttachS2CPacket> SET_ENTITY_LINK = PlayPackets.s2c("set_entity_link");
    public static final PacketType<EntityVelocityUpdateS2CPacket> SET_ENTITY_MOTION = PlayPackets.s2c("set_entity_motion");
    public static final PacketType<EntityEquipmentUpdateS2CPacket> SET_EQUIPMENT = PlayPackets.s2c("set_equipment");
    public static final PacketType<ExperienceBarUpdateS2CPacket> SET_EXPERIENCE = PlayPackets.s2c("set_experience");
    public static final PacketType<HealthUpdateS2CPacket> SET_HEALTH = PlayPackets.s2c("set_health");
    public static final PacketType<ScoreboardObjectiveUpdateS2CPacket> SET_OBJECTIVE = PlayPackets.s2c("set_objective");
    public static final PacketType<EntityPassengersSetS2CPacket> SET_PASSENGERS = PlayPackets.s2c("set_passengers");
    public static final PacketType<TeamS2CPacket> SET_PLAYER_TEAM = PlayPackets.s2c("set_player_team");
    public static final PacketType<ScoreboardScoreUpdateS2CPacket> SET_SCORE = PlayPackets.s2c("set_score");
    public static final PacketType<SimulationDistanceS2CPacket> SET_SIMULATION_DISTANCE = PlayPackets.s2c("set_simulation_distance");
    public static final PacketType<SubtitleS2CPacket> SET_SUBTITLE_TEXT = PlayPackets.s2c("set_subtitle_text");
    public static final PacketType<WorldTimeUpdateS2CPacket> SET_TIME = PlayPackets.s2c("set_time");
    public static final PacketType<TitleS2CPacket> SET_TITLE_TEXT = PlayPackets.s2c("set_title_text");
    public static final PacketType<TitleFadeS2CPacket> SET_TITLES_ANIMATION = PlayPackets.s2c("set_titles_animation");
    public static final PacketType<PlaySoundFromEntityS2CPacket> SOUND_ENTITY = PlayPackets.s2c("sound_entity");
    public static final PacketType<PlaySoundS2CPacket> SOUND = PlayPackets.s2c("sound");
    public static final PacketType<EnterReconfigurationS2CPacket> START_CONFIGURATION = PlayPackets.s2c("start_configuration");
    public static final PacketType<StopSoundS2CPacket> STOP_SOUND = PlayPackets.s2c("stop_sound");
    public static final PacketType<GameMessageS2CPacket> SYSTEM_CHAT = PlayPackets.s2c("system_chat");
    public static final PacketType<PlayerListHeaderS2CPacket> TAB_LIST = PlayPackets.s2c("tab_list");
    public static final PacketType<NbtQueryResponseS2CPacket> TAG_QUERY = PlayPackets.s2c("tag_query");
    public static final PacketType<ItemPickupAnimationS2CPacket> TAKE_ITEM_ENTITY = PlayPackets.s2c("take_item_entity");
    public static final PacketType<EntityPositionS2CPacket> TELEPORT_ENTITY = PlayPackets.s2c("teleport_entity");
    public static final PacketType<AdvancementUpdateS2CPacket> UPDATE_ADVANCEMENTS = PlayPackets.s2c("update_advancements");
    public static final PacketType<EntityAttributesS2CPacket> UPDATE_ATTRIBUTES = PlayPackets.s2c("update_attributes");
    public static final PacketType<EntityStatusEffectS2CPacket> UPDATE_MOB_EFFECT = PlayPackets.s2c("update_mob_effect");
    public static final PacketType<SynchronizeRecipesS2CPacket> UPDATE_RECIPES = PlayPackets.s2c("update_recipes");
    public static final PacketType<ProjectilePowerS2CPacket> PROJECTILE_POWER = PlayPackets.s2c("projectile_power");
    public static final PacketType<TeleportConfirmC2SPacket> ACCEPT_TELEPORTATION = PlayPackets.c2s("accept_teleportation");
    public static final PacketType<QueryBlockNbtC2SPacket> BLOCK_ENTITY_TAG_QUERY = PlayPackets.c2s("block_entity_tag_query");
    public static final PacketType<UpdateDifficultyC2SPacket> CHANGE_DIFFICULTY_C2S = PlayPackets.c2s("change_difficulty");
    public static final PacketType<MessageAcknowledgmentC2SPacket> CHAT_ACK = PlayPackets.c2s("chat_ack");
    public static final PacketType<CommandExecutionC2SPacket> CHAT_COMMAND = PlayPackets.c2s("chat_command");
    public static final PacketType<ChatCommandSignedC2SPacket> CHAT_COMMAND_SIGNED = PlayPackets.c2s("chat_command_signed");
    public static final PacketType<ChatMessageC2SPacket> CHAT = PlayPackets.c2s("chat");
    public static final PacketType<PlayerSessionC2SPacket> CHAT_SESSION_UPDATE = PlayPackets.c2s("chat_session_update");
    public static final PacketType<AcknowledgeChunksC2SPacket> CHUNK_BATCH_RECEIVED = PlayPackets.c2s("chunk_batch_received");
    public static final PacketType<ClientStatusC2SPacket> CLIENT_COMMAND = PlayPackets.c2s("client_command");
    public static final PacketType<RequestCommandCompletionsC2SPacket> COMMAND_SUGGESTION = PlayPackets.c2s("command_suggestion");
    public static final PacketType<AcknowledgeReconfigurationC2SPacket> CONFIGURATION_ACKNOWLEDGED = PlayPackets.c2s("configuration_acknowledged");
    public static final PacketType<ButtonClickC2SPacket> CONTAINER_BUTTON_CLICK = PlayPackets.c2s("container_button_click");
    public static final PacketType<ClickSlotC2SPacket> CONTAINER_CLICK = PlayPackets.c2s("container_click");
    public static final PacketType<CloseHandledScreenC2SPacket> CONTAINER_CLOSE_C2S = PlayPackets.c2s("container_close");
    public static final PacketType<SlotChangedStateC2SPacket> CONTAINER_SLOT_STATE_CHANGED = PlayPackets.c2s("container_slot_state_changed");
    public static final PacketType<DebugSampleSubscriptionC2SPacket> DEBUG_SAMPLE_SUBSCRIPTION = PlayPackets.c2s("debug_sample_subscription");
    public static final PacketType<BookUpdateC2SPacket> EDIT_BOOK = PlayPackets.c2s("edit_book");
    public static final PacketType<QueryEntityNbtC2SPacket> ENTITY_TAG_QUERY = PlayPackets.c2s("entity_tag_query");
    public static final PacketType<PlayerInteractEntityC2SPacket> INTERACT = PlayPackets.c2s("interact");
    public static final PacketType<JigsawGeneratingC2SPacket> JIGSAW_GENERATE = PlayPackets.c2s("jigsaw_generate");
    public static final PacketType<UpdateDifficultyLockC2SPacket> LOCK_DIFFICULTY = PlayPackets.c2s("lock_difficulty");
    public static final PacketType<PlayerMoveC2SPacket.PositionAndOnGround> MOVE_PLAYER_POS = PlayPackets.c2s("move_player_pos");
    public static final PacketType<PlayerMoveC2SPacket.Full> MOVE_PLAYER_POS_ROT = PlayPackets.c2s("move_player_pos_rot");
    public static final PacketType<PlayerMoveC2SPacket.LookAndOnGround> MOVE_PLAYER_ROT = PlayPackets.c2s("move_player_rot");
    public static final PacketType<PlayerMoveC2SPacket.OnGroundOnly> MOVE_PLAYER_STATUS_ONLY = PlayPackets.c2s("move_player_status_only");
    public static final PacketType<VehicleMoveC2SPacket> MOVE_VEHICLE_C2S = PlayPackets.c2s("move_vehicle");
    public static final PacketType<BoatPaddleStateC2SPacket> PADDLE_BOAT = PlayPackets.c2s("paddle_boat");
    public static final PacketType<PickFromInventoryC2SPacket> PICK_ITEM = PlayPackets.c2s("pick_item");
    public static final PacketType<CraftRequestC2SPacket> PLACE_RECIPE = PlayPackets.c2s("place_recipe");
    public static final PacketType<UpdatePlayerAbilitiesC2SPacket> PLAYER_ABILITIES_C2S = PlayPackets.c2s("player_abilities");
    public static final PacketType<PlayerActionC2SPacket> PLAYER_ACTION = PlayPackets.c2s("player_action");
    public static final PacketType<ClientCommandC2SPacket> PLAYER_COMMAND = PlayPackets.c2s("player_command");
    public static final PacketType<PlayerInputC2SPacket> PLAYER_INPUT = PlayPackets.c2s("player_input");
    public static final PacketType<RecipeCategoryOptionsC2SPacket> RECIPE_BOOK_CHANGE_SETTINGS = PlayPackets.c2s("recipe_book_change_settings");
    public static final PacketType<RecipeBookDataC2SPacket> RECIPE_BOOK_SEEN_RECIPE = PlayPackets.c2s("recipe_book_seen_recipe");
    public static final PacketType<RenameItemC2SPacket> RENAME_ITEM = PlayPackets.c2s("rename_item");
    public static final PacketType<AdvancementTabC2SPacket> SEEN_ADVANCEMENTS = PlayPackets.c2s("seen_advancements");
    public static final PacketType<SelectMerchantTradeC2SPacket> SELECT_TRADE = PlayPackets.c2s("select_trade");
    public static final PacketType<UpdateBeaconC2SPacket> SET_BEACON = PlayPackets.c2s("set_beacon");
    public static final PacketType<UpdateSelectedSlotC2SPacket> SET_CARRIED_ITEM_C2S = PlayPackets.c2s("set_carried_item");
    public static final PacketType<UpdateCommandBlockC2SPacket> SET_COMMAND_BLOCK = PlayPackets.c2s("set_command_block");
    public static final PacketType<UpdateCommandBlockMinecartC2SPacket> SET_COMMAND_MINECART = PlayPackets.c2s("set_command_minecart");
    public static final PacketType<CreativeInventoryActionC2SPacket> SET_CREATIVE_MODE_SLOT = PlayPackets.c2s("set_creative_mode_slot");
    public static final PacketType<UpdateJigsawC2SPacket> SET_JIGSAW_BLOCK = PlayPackets.c2s("set_jigsaw_block");
    public static final PacketType<UpdateStructureBlockC2SPacket> SET_STRUCTURE_BLOCK = PlayPackets.c2s("set_structure_block");
    public static final PacketType<UpdateSignC2SPacket> SIGN_UPDATE = PlayPackets.c2s("sign_update");
    public static final PacketType<HandSwingC2SPacket> SWING = PlayPackets.c2s("swing");
    public static final PacketType<SpectatorTeleportC2SPacket> TELEPORT_TO_ENTITY = PlayPackets.c2s("teleport_to_entity");
    public static final PacketType<PlayerInteractBlockC2SPacket> USE_ITEM_ON = PlayPackets.c2s("use_item_on");
    public static final PacketType<PlayerInteractItemC2SPacket> USE_ITEM = PlayPackets.c2s("use_item");
    public static final PacketType<ScoreboardScoreResetS2CPacket> RESET_SCORE = PlayPackets.s2c("reset_score");
    public static final PacketType<UpdateTickRateS2CPacket> TICKING_STATE = PlayPackets.s2c("ticking_state");
    public static final PacketType<TickStepS2CPacket> TICKING_STEP = PlayPackets.s2c("ticking_step");

    private static <T extends Packet<ClientPlayPacketListener>> PacketType<T> s2c(String id) {
        return new PacketType(NetworkSide.CLIENTBOUND, Identifier.method_60656(id));
    }

    private static <T extends Packet<ServerPlayPacketListener>> PacketType<T> c2s(String id) {
        return new PacketType(NetworkSide.SERVERBOUND, Identifier.method_60656(id));
    }
}
