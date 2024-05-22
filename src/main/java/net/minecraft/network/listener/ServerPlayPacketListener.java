/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.listener;

import net.minecraft.network.NetworkPhase;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.listener.ServerQueryPingPacketListener;
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

public interface ServerPlayPacketListener
extends ServerQueryPingPacketListener,
ServerCommonPacketListener {
    @Override
    default public NetworkPhase getPhase() {
        return NetworkPhase.PLAY;
    }

    public void onHandSwing(HandSwingC2SPacket var1);

    public void onChatMessage(ChatMessageC2SPacket var1);

    public void onCommandExecution(CommandExecutionC2SPacket var1);

    public void onChatCommandSigned(ChatCommandSignedC2SPacket var1);

    public void onMessageAcknowledgment(MessageAcknowledgmentC2SPacket var1);

    public void onClientStatus(ClientStatusC2SPacket var1);

    public void onButtonClick(ButtonClickC2SPacket var1);

    public void onClickSlot(ClickSlotC2SPacket var1);

    public void onCraftRequest(CraftRequestC2SPacket var1);

    public void onCloseHandledScreen(CloseHandledScreenC2SPacket var1);

    public void onPlayerInteractEntity(PlayerInteractEntityC2SPacket var1);

    public void onPlayerMove(PlayerMoveC2SPacket var1);

    public void onUpdatePlayerAbilities(UpdatePlayerAbilitiesC2SPacket var1);

    public void onPlayerAction(PlayerActionC2SPacket var1);

    public void onClientCommand(ClientCommandC2SPacket var1);

    public void onPlayerInput(PlayerInputC2SPacket var1);

    public void onUpdateSelectedSlot(UpdateSelectedSlotC2SPacket var1);

    public void onCreativeInventoryAction(CreativeInventoryActionC2SPacket var1);

    public void onUpdateSign(UpdateSignC2SPacket var1);

    public void onPlayerInteractBlock(PlayerInteractBlockC2SPacket var1);

    public void onPlayerInteractItem(PlayerInteractItemC2SPacket var1);

    public void onSpectatorTeleport(SpectatorTeleportC2SPacket var1);

    public void onBoatPaddleState(BoatPaddleStateC2SPacket var1);

    public void onVehicleMove(VehicleMoveC2SPacket var1);

    public void onTeleportConfirm(TeleportConfirmC2SPacket var1);

    public void onRecipeBookData(RecipeBookDataC2SPacket var1);

    public void onRecipeCategoryOptions(RecipeCategoryOptionsC2SPacket var1);

    public void onAdvancementTab(AdvancementTabC2SPacket var1);

    public void onRequestCommandCompletions(RequestCommandCompletionsC2SPacket var1);

    public void onUpdateCommandBlock(UpdateCommandBlockC2SPacket var1);

    public void onUpdateCommandBlockMinecart(UpdateCommandBlockMinecartC2SPacket var1);

    public void onPickFromInventory(PickFromInventoryC2SPacket var1);

    public void onRenameItem(RenameItemC2SPacket var1);

    public void onUpdateBeacon(UpdateBeaconC2SPacket var1);

    public void onUpdateStructureBlock(UpdateStructureBlockC2SPacket var1);

    public void onSelectMerchantTrade(SelectMerchantTradeC2SPacket var1);

    public void onBookUpdate(BookUpdateC2SPacket var1);

    public void onQueryEntityNbt(QueryEntityNbtC2SPacket var1);

    public void onSlotChangedState(SlotChangedStateC2SPacket var1);

    public void onQueryBlockNbt(QueryBlockNbtC2SPacket var1);

    public void onUpdateJigsaw(UpdateJigsawC2SPacket var1);

    public void onJigsawGenerating(JigsawGeneratingC2SPacket var1);

    public void onUpdateDifficulty(UpdateDifficultyC2SPacket var1);

    public void onUpdateDifficultyLock(UpdateDifficultyLockC2SPacket var1);

    public void onPlayerSession(PlayerSessionC2SPacket var1);

    public void onAcknowledgeReconfiguration(AcknowledgeReconfigurationC2SPacket var1);

    public void onAcknowledgeChunks(AcknowledgeChunksC2SPacket var1);

    public void onDebugSampleSubscription(DebugSampleSubscriptionC2SPacket var1);
}

