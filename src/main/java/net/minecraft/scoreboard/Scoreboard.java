package net.minecraft.scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class Scoreboard {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int LIST_DISPLAY_SLOT_ID = 0;
   public static final int SIDEBAR_DISPLAY_SLOT_ID = 1;
   public static final int BELOW_NAME_DISPLAY_SLOT_ID = 2;
   public static final int MIN_SIDEBAR_TEAM_DISPLAY_SLOT_ID = 3;
   public static final int MAX_SIDEBAR_TEAM_DISPLAY_SLOT_ID = 18;
   public static final int DISPLAY_SLOT_COUNT = 19;
   private final Map objectives = Maps.newHashMap();
   private final Map objectivesByCriterion = Maps.newHashMap();
   private final Map playerObjectives = Maps.newHashMap();
   private final ScoreboardObjective[] objectiveSlots = new ScoreboardObjective[19];
   private final Map teams = Maps.newHashMap();
   private final Map teamsByPlayer = Maps.newHashMap();
   @Nullable
   private static String[] displaySlotNames;

   public boolean containsObjective(String name) {
      return this.objectives.containsKey(name);
   }

   public ScoreboardObjective getObjective(String name) {
      return (ScoreboardObjective)this.objectives.get(name);
   }

   @Nullable
   public ScoreboardObjective getNullableObjective(@Nullable String name) {
      return (ScoreboardObjective)this.objectives.get(name);
   }

   public ScoreboardObjective addObjective(String name, ScoreboardCriterion criterion, Text displayName, ScoreboardCriterion.RenderType renderType) {
      if (this.objectives.containsKey(name)) {
         throw new IllegalArgumentException("An objective with the name '" + name + "' already exists!");
      } else {
         ScoreboardObjective lv = new ScoreboardObjective(this, name, criterion, displayName, renderType);
         ((List)this.objectivesByCriterion.computeIfAbsent(criterion, (criterionx) -> {
            return Lists.newArrayList();
         })).add(lv);
         this.objectives.put(name, lv);
         this.updateObjective(lv);
         return lv;
      }
   }

   public final void forEachScore(ScoreboardCriterion criterion, String player, Consumer action) {
      ((List)this.objectivesByCriterion.getOrDefault(criterion, Collections.emptyList())).forEach((objective) -> {
         action.accept(this.getPlayerScore(player, objective));
      });
   }

   public boolean playerHasObjective(String playerName, ScoreboardObjective objective) {
      Map map = (Map)this.playerObjectives.get(playerName);
      if (map == null) {
         return false;
      } else {
         ScoreboardPlayerScore lv = (ScoreboardPlayerScore)map.get(objective);
         return lv != null;
      }
   }

   public ScoreboardPlayerScore getPlayerScore(String playerName, ScoreboardObjective objective) {
      Map map = (Map)this.playerObjectives.computeIfAbsent(playerName, (name) -> {
         return Maps.newHashMap();
      });
      return (ScoreboardPlayerScore)map.computeIfAbsent(objective, (objectivex) -> {
         ScoreboardPlayerScore lv = new ScoreboardPlayerScore(this, objectivex, playerName);
         lv.setScore(0);
         return lv;
      });
   }

   public Collection getAllPlayerScores(ScoreboardObjective objective) {
      List list = Lists.newArrayList();
      Iterator var3 = this.playerObjectives.values().iterator();

      while(var3.hasNext()) {
         Map map = (Map)var3.next();
         ScoreboardPlayerScore lv = (ScoreboardPlayerScore)map.get(objective);
         if (lv != null) {
            list.add(lv);
         }
      }

      list.sort(ScoreboardPlayerScore.COMPARATOR);
      return list;
   }

   public Collection getObjectives() {
      return this.objectives.values();
   }

   public Collection getObjectiveNames() {
      return this.objectives.keySet();
   }

   public Collection getKnownPlayers() {
      return Lists.newArrayList(this.playerObjectives.keySet());
   }

   public void resetPlayerScore(String playerName, @Nullable ScoreboardObjective objective) {
      Map map;
      if (objective == null) {
         map = (Map)this.playerObjectives.remove(playerName);
         if (map != null) {
            this.updatePlayerScore(playerName);
         }
      } else {
         map = (Map)this.playerObjectives.get(playerName);
         if (map != null) {
            ScoreboardPlayerScore lv = (ScoreboardPlayerScore)map.remove(objective);
            if (map.size() < 1) {
               Map map2 = (Map)this.playerObjectives.remove(playerName);
               if (map2 != null) {
                  this.updatePlayerScore(playerName);
               }
            } else if (lv != null) {
               this.updatePlayerScore(playerName, objective);
            }
         }
      }

   }

   public Map getPlayerObjectives(String playerName) {
      Map map = (Map)this.playerObjectives.get(playerName);
      if (map == null) {
         map = Maps.newHashMap();
      }

      return (Map)map;
   }

   public void removeObjective(ScoreboardObjective objective) {
      this.objectives.remove(objective.getName());

      for(int i = 0; i < 19; ++i) {
         if (this.getObjectiveForSlot(i) == objective) {
            this.setObjectiveSlot(i, (ScoreboardObjective)null);
         }
      }

      List list = (List)this.objectivesByCriterion.get(objective.getCriterion());
      if (list != null) {
         list.remove(objective);
      }

      Iterator var3 = this.playerObjectives.values().iterator();

      while(var3.hasNext()) {
         Map map = (Map)var3.next();
         map.remove(objective);
      }

      this.updateRemovedObjective(objective);
   }

   public void setObjectiveSlot(int slot, @Nullable ScoreboardObjective objective) {
      this.objectiveSlots[slot] = objective;
   }

   @Nullable
   public ScoreboardObjective getObjectiveForSlot(int slot) {
      return this.objectiveSlots[slot];
   }

   @Nullable
   public Team getTeam(String name) {
      return (Team)this.teams.get(name);
   }

   public Team addTeam(String name) {
      Team lv = this.getTeam(name);
      if (lv != null) {
         LOGGER.warn("Requested creation of existing team '{}'", name);
         return lv;
      } else {
         lv = new Team(this, name);
         this.teams.put(name, lv);
         this.updateScoreboardTeamAndPlayers(lv);
         return lv;
      }
   }

   public void removeTeam(Team team) {
      this.teams.remove(team.getName());
      Iterator var2 = team.getPlayerList().iterator();

      while(var2.hasNext()) {
         String string = (String)var2.next();
         this.teamsByPlayer.remove(string);
      }

      this.updateRemovedTeam(team);
   }

   public boolean addPlayerToTeam(String playerName, Team team) {
      if (this.getPlayerTeam(playerName) != null) {
         this.clearPlayerTeam(playerName);
      }

      this.teamsByPlayer.put(playerName, team);
      return team.getPlayerList().add(playerName);
   }

   public boolean clearPlayerTeam(String playerName) {
      Team lv = this.getPlayerTeam(playerName);
      if (lv != null) {
         this.removePlayerFromTeam(playerName, lv);
         return true;
      } else {
         return false;
      }
   }

   public void removePlayerFromTeam(String playerName, Team team) {
      if (this.getPlayerTeam(playerName) != team) {
         throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + team.getName() + "'.");
      } else {
         this.teamsByPlayer.remove(playerName);
         team.getPlayerList().remove(playerName);
      }
   }

   public Collection getTeamNames() {
      return this.teams.keySet();
   }

   public Collection getTeams() {
      return this.teams.values();
   }

   @Nullable
   public Team getPlayerTeam(String playerName) {
      return (Team)this.teamsByPlayer.get(playerName);
   }

   public void updateObjective(ScoreboardObjective objective) {
   }

   public void updateExistingObjective(ScoreboardObjective objective) {
   }

   public void updateRemovedObjective(ScoreboardObjective objective) {
   }

   public void updateScore(ScoreboardPlayerScore score) {
   }

   public void updatePlayerScore(String playerName) {
   }

   public void updatePlayerScore(String playerName, ScoreboardObjective objective) {
   }

   public void updateScoreboardTeamAndPlayers(Team team) {
   }

   public void updateScoreboardTeam(Team team) {
   }

   public void updateRemovedTeam(Team team) {
   }

   public static String getDisplaySlotName(int slotId) {
      switch (slotId) {
         case 0:
            return "list";
         case 1:
            return "sidebar";
         case 2:
            return "belowName";
         default:
            if (slotId >= 3 && slotId <= 18) {
               Formatting lv = Formatting.byColorIndex(slotId - 3);
               if (lv != null && lv != Formatting.RESET) {
                  return "sidebar.team." + lv.getName();
               }
            }

            return null;
      }
   }

   public static int getDisplaySlotId(String slotName) {
      if ("list".equalsIgnoreCase(slotName)) {
         return 0;
      } else if ("sidebar".equalsIgnoreCase(slotName)) {
         return 1;
      } else if ("belowName".equalsIgnoreCase(slotName)) {
         return 2;
      } else {
         if (slotName.startsWith("sidebar.team.")) {
            String string2 = slotName.substring("sidebar.team.".length());
            Formatting lv = Formatting.byName(string2);
            if (lv != null && lv.getColorIndex() >= 0) {
               return lv.getColorIndex() + 3;
            }
         }

         return -1;
      }
   }

   public static String[] getDisplaySlotNames() {
      if (displaySlotNames == null) {
         displaySlotNames = new String[19];

         for(int i = 0; i < 19; ++i) {
            displaySlotNames[i] = getDisplaySlotName(i);
         }
      }

      return displaySlotNames;
   }

   public void resetEntityScore(Entity entity) {
      if (entity != null && !(entity instanceof PlayerEntity) && !entity.isAlive()) {
         String string = entity.getUuidAsString();
         this.resetPlayerScore(string, (ScoreboardObjective)null);
         this.clearPlayerTeam(string);
      }
   }

   protected NbtList toNbt() {
      NbtList lv = new NbtList();
      this.playerObjectives.values().stream().map(Map::values).forEach((scores) -> {
         scores.stream().filter((score) -> {
            return score.getObjective() != null;
         }).forEach((score) -> {
            NbtCompound lvx = new NbtCompound();
            lvx.putString("Name", score.getPlayerName());
            lvx.putString("Objective", score.getObjective().getName());
            lvx.putInt("Score", score.getScore());
            lvx.putBoolean("Locked", score.isLocked());
            lv.add(lvx);
         });
      });
      return lv;
   }

   protected void readNbt(NbtList list) {
      for(int i = 0; i < list.size(); ++i) {
         NbtCompound lv = list.getCompound(i);
         ScoreboardObjective lv2 = this.getObjective(lv.getString("Objective"));
         String string = lv.getString("Name");
         ScoreboardPlayerScore lv3 = this.getPlayerScore(string, lv2);
         lv3.setScore(lv.getInt("Score"));
         if (lv.contains("Locked")) {
            lv3.setLocked(lv.getBoolean("Locked"));
         }
      }

   }
}
