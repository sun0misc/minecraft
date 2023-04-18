package net.minecraft.scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class ServerScoreboard extends Scoreboard {
   private final MinecraftServer server;
   private final Set objectives = Sets.newHashSet();
   private final List updateListeners = Lists.newArrayList();

   public ServerScoreboard(MinecraftServer server) {
      this.server = server;
   }

   public void updateScore(ScoreboardPlayerScore score) {
      super.updateScore(score);
      if (this.objectives.contains(score.getObjective())) {
         this.server.getPlayerManager().sendToAll(new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.CHANGE, score.getObjective().getName(), score.getPlayerName(), score.getScore()));
      }

      this.runUpdateListeners();
   }

   public void updatePlayerScore(String playerName) {
      super.updatePlayerScore(playerName);
      this.server.getPlayerManager().sendToAll(new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.REMOVE, (String)null, playerName, 0));
      this.runUpdateListeners();
   }

   public void updatePlayerScore(String playerName, ScoreboardObjective objective) {
      super.updatePlayerScore(playerName, objective);
      if (this.objectives.contains(objective)) {
         this.server.getPlayerManager().sendToAll(new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.REMOVE, objective.getName(), playerName, 0));
      }

      this.runUpdateListeners();
   }

   public void setObjectiveSlot(int slot, @Nullable ScoreboardObjective objective) {
      ScoreboardObjective lv = this.getObjectiveForSlot(slot);
      super.setObjectiveSlot(slot, objective);
      if (lv != objective && lv != null) {
         if (this.getSlot(lv) > 0) {
            this.server.getPlayerManager().sendToAll(new ScoreboardDisplayS2CPacket(slot, objective));
         } else {
            this.removeScoreboardObjective(lv);
         }
      }

      if (objective != null) {
         if (this.objectives.contains(objective)) {
            this.server.getPlayerManager().sendToAll(new ScoreboardDisplayS2CPacket(slot, objective));
         } else {
            this.addScoreboardObjective(objective);
         }
      }

      this.runUpdateListeners();
   }

   public boolean addPlayerToTeam(String playerName, Team team) {
      if (super.addPlayerToTeam(playerName, team)) {
         this.server.getPlayerManager().sendToAll(TeamS2CPacket.changePlayerTeam(team, playerName, TeamS2CPacket.Operation.ADD));
         this.runUpdateListeners();
         return true;
      } else {
         return false;
      }
   }

   public void removePlayerFromTeam(String playerName, Team team) {
      super.removePlayerFromTeam(playerName, team);
      this.server.getPlayerManager().sendToAll(TeamS2CPacket.changePlayerTeam(team, playerName, TeamS2CPacket.Operation.REMOVE));
      this.runUpdateListeners();
   }

   public void updateObjective(ScoreboardObjective objective) {
      super.updateObjective(objective);
      this.runUpdateListeners();
   }

   public void updateExistingObjective(ScoreboardObjective objective) {
      super.updateExistingObjective(objective);
      if (this.objectives.contains(objective)) {
         this.server.getPlayerManager().sendToAll(new ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.UPDATE_MODE));
      }

      this.runUpdateListeners();
   }

   public void updateRemovedObjective(ScoreboardObjective objective) {
      super.updateRemovedObjective(objective);
      if (this.objectives.contains(objective)) {
         this.removeScoreboardObjective(objective);
      }

      this.runUpdateListeners();
   }

   public void updateScoreboardTeamAndPlayers(Team team) {
      super.updateScoreboardTeamAndPlayers(team);
      this.server.getPlayerManager().sendToAll(TeamS2CPacket.updateTeam(team, true));
      this.runUpdateListeners();
   }

   public void updateScoreboardTeam(Team team) {
      super.updateScoreboardTeam(team);
      this.server.getPlayerManager().sendToAll(TeamS2CPacket.updateTeam(team, false));
      this.runUpdateListeners();
   }

   public void updateRemovedTeam(Team team) {
      super.updateRemovedTeam(team);
      this.server.getPlayerManager().sendToAll(TeamS2CPacket.updateRemovedTeam(team));
      this.runUpdateListeners();
   }

   public void addUpdateListener(Runnable listener) {
      this.updateListeners.add(listener);
   }

   protected void runUpdateListeners() {
      Iterator var1 = this.updateListeners.iterator();

      while(var1.hasNext()) {
         Runnable runnable = (Runnable)var1.next();
         runnable.run();
      }

   }

   public List createChangePackets(ScoreboardObjective objective) {
      List list = Lists.newArrayList();
      list.add(new ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.ADD_MODE));

      for(int i = 0; i < 19; ++i) {
         if (this.getObjectiveForSlot(i) == objective) {
            list.add(new ScoreboardDisplayS2CPacket(i, objective));
         }
      }

      Iterator var5 = this.getAllPlayerScores(objective).iterator();

      while(var5.hasNext()) {
         ScoreboardPlayerScore lv = (ScoreboardPlayerScore)var5.next();
         list.add(new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.CHANGE, lv.getObjective().getName(), lv.getPlayerName(), lv.getScore()));
      }

      return list;
   }

   public void addScoreboardObjective(ScoreboardObjective objective) {
      List list = this.createChangePackets(objective);
      Iterator var3 = this.server.getPlayerManager().getPlayerList().iterator();

      while(var3.hasNext()) {
         ServerPlayerEntity lv = (ServerPlayerEntity)var3.next();
         Iterator var5 = list.iterator();

         while(var5.hasNext()) {
            Packet lv2 = (Packet)var5.next();
            lv.networkHandler.sendPacket(lv2);
         }
      }

      this.objectives.add(objective);
   }

   public List createRemovePackets(ScoreboardObjective objective) {
      List list = Lists.newArrayList();
      list.add(new ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.REMOVE_MODE));

      for(int i = 0; i < 19; ++i) {
         if (this.getObjectiveForSlot(i) == objective) {
            list.add(new ScoreboardDisplayS2CPacket(i, objective));
         }
      }

      return list;
   }

   public void removeScoreboardObjective(ScoreboardObjective objective) {
      List list = this.createRemovePackets(objective);
      Iterator var3 = this.server.getPlayerManager().getPlayerList().iterator();

      while(var3.hasNext()) {
         ServerPlayerEntity lv = (ServerPlayerEntity)var3.next();
         Iterator var5 = list.iterator();

         while(var5.hasNext()) {
            Packet lv2 = (Packet)var5.next();
            lv.networkHandler.sendPacket(lv2);
         }
      }

      this.objectives.remove(objective);
   }

   public int getSlot(ScoreboardObjective objective) {
      int i = 0;

      for(int j = 0; j < 19; ++j) {
         if (this.getObjectiveForSlot(j) == objective) {
            ++i;
         }
      }

      return i;
   }

   public ScoreboardState createState() {
      ScoreboardState lv = new ScoreboardState(this);
      Objects.requireNonNull(lv);
      this.addUpdateListener(lv::markDirty);
      return lv;
   }

   public ScoreboardState stateFromNbt(NbtCompound nbt) {
      return this.createState().readNbt(nbt);
   }

   public static enum UpdateMode {
      CHANGE,
      REMOVE;

      // $FF: synthetic method
      private static UpdateMode[] method_36963() {
         return new UpdateMode[]{CHANGE, REMOVE};
      }
   }
}
