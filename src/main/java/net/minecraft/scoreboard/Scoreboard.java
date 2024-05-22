/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.scoreboard;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import net.minecraft.scoreboard.Scores;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class Scoreboard {
    public static final String field_47542 = "#";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Object2ObjectMap<String, ScoreboardObjective> objectives = new Object2ObjectOpenHashMap<String, ScoreboardObjective>(16, 0.5f);
    private final Reference2ObjectMap<ScoreboardCriterion, List<ScoreboardObjective>> objectivesByCriterion = new Reference2ObjectOpenHashMap<ScoreboardCriterion, List<ScoreboardObjective>>();
    private final Map<String, Scores> scores = new Object2ObjectOpenHashMap<String, Scores>(16, 0.5f);
    private final Map<ScoreboardDisplaySlot, ScoreboardObjective> objectiveSlots = new EnumMap<ScoreboardDisplaySlot, ScoreboardObjective>(ScoreboardDisplaySlot.class);
    private final Object2ObjectMap<String, Team> teams = new Object2ObjectOpenHashMap<String, Team>();
    private final Object2ObjectMap<String, Team> teamsByScoreHolder = new Object2ObjectOpenHashMap<String, Team>();

    @Nullable
    public ScoreboardObjective getNullableObjective(@Nullable String name) {
        return (ScoreboardObjective)this.objectives.get(name);
    }

    public ScoreboardObjective addObjective(String name, ScoreboardCriterion criterion, Text displayName, ScoreboardCriterion.RenderType renderType, boolean displayAutoUpdate, @Nullable NumberFormat numberFormat) {
        if (this.objectives.containsKey(name)) {
            throw new IllegalArgumentException("An objective with the name '" + name + "' already exists!");
        }
        ScoreboardObjective lv = new ScoreboardObjective(this, name, criterion, displayName, renderType, displayAutoUpdate, numberFormat);
        this.objectivesByCriterion.computeIfAbsent(criterion, criterion2 -> Lists.newArrayList()).add(lv);
        this.objectives.put(name, lv);
        this.updateObjective(lv);
        return lv;
    }

    public final void forEachScore(ScoreboardCriterion criterion, ScoreHolder scoreHolder, Consumer<ScoreAccess> action) {
        this.objectivesByCriterion.getOrDefault(criterion, Collections.emptyList()).forEach(objective -> action.accept(this.getOrCreateScore(scoreHolder, (ScoreboardObjective)objective, true)));
    }

    private Scores getScores(String scoreHolderName) {
        return this.scores.computeIfAbsent(scoreHolderName, name -> new Scores());
    }

    public ScoreAccess getOrCreateScore(ScoreHolder scoreHolder, ScoreboardObjective objective) {
        return this.getOrCreateScore(scoreHolder, objective, false);
    }

    public ScoreAccess getOrCreateScore(final ScoreHolder scoreHolder, final ScoreboardObjective objective, boolean forceWritable) {
        final boolean bl2 = forceWritable || !objective.getCriterion().isReadOnly();
        Scores lv = this.getScores(scoreHolder.getNameForScoreboard());
        final MutableBoolean mutableBoolean = new MutableBoolean();
        final ScoreboardScore lv2 = lv.getOrCreate(objective, score -> mutableBoolean.setTrue());
        return new ScoreAccess(){

            @Override
            public int getScore() {
                return lv2.getScore();
            }

            @Override
            public void setScore(int score) {
                Text lv;
                if (!bl2) {
                    throw new IllegalStateException("Cannot modify read-only score");
                }
                boolean bl = mutableBoolean.isTrue();
                if (objective.shouldDisplayAutoUpdate() && (lv = scoreHolder.getDisplayName()) != null && !lv.equals(lv2.getDisplayText())) {
                    lv2.setDisplayText(lv);
                    bl = true;
                }
                if (score != lv2.getScore()) {
                    lv2.setScore(score);
                    bl = true;
                }
                if (bl) {
                    this.update();
                }
            }

            @Override
            @Nullable
            public Text getDisplayText() {
                return lv2.getDisplayText();
            }

            @Override
            public void setDisplayText(@Nullable Text text) {
                if (mutableBoolean.isTrue() || !Objects.equals(text, lv2.getDisplayText())) {
                    lv2.setDisplayText(text);
                    this.update();
                }
            }

            @Override
            public void setNumberFormat(@Nullable NumberFormat numberFormat) {
                lv2.setNumberFormat(numberFormat);
                this.update();
            }

            @Override
            public boolean isLocked() {
                return lv2.isLocked();
            }

            @Override
            public void unlock() {
                this.setLocked(false);
            }

            @Override
            public void lock() {
                this.setLocked(true);
            }

            private void setLocked(boolean locked) {
                lv2.setLocked(locked);
                if (mutableBoolean.isTrue()) {
                    this.update();
                }
                Scoreboard.this.resetScore(scoreHolder, objective);
            }

            private void update() {
                Scoreboard.this.updateScore(scoreHolder, objective, lv2);
                mutableBoolean.setFalse();
            }
        };
    }

    @Nullable
    public ReadableScoreboardScore getScore(ScoreHolder scoreHolder, ScoreboardObjective objective) {
        Scores lv = this.scores.get(scoreHolder.getNameForScoreboard());
        if (lv != null) {
            return lv.get(objective);
        }
        return null;
    }

    public Collection<ScoreboardEntry> getScoreboardEntries(ScoreboardObjective objective) {
        ArrayList<ScoreboardEntry> list = new ArrayList<ScoreboardEntry>();
        this.scores.forEach((scoreHolderName, scores) -> {
            ScoreboardScore lv = scores.get(objective);
            if (lv != null) {
                list.add(new ScoreboardEntry((String)scoreHolderName, lv.getScore(), lv.getDisplayText(), lv.getNumberFormat()));
            }
        });
        return list;
    }

    public Collection<ScoreboardObjective> getObjectives() {
        return this.objectives.values();
    }

    public Collection<String> getObjectiveNames() {
        return this.objectives.keySet();
    }

    public Collection<ScoreHolder> getKnownScoreHolders() {
        return this.scores.keySet().stream().map(ScoreHolder::fromName).toList();
    }

    public void removeScores(ScoreHolder scoreHolder) {
        Scores lv = this.scores.remove(scoreHolder.getNameForScoreboard());
        if (lv != null) {
            this.onScoreHolderRemoved(scoreHolder);
        }
    }

    public void removeScore(ScoreHolder scoreHolder, ScoreboardObjective objective) {
        Scores lv = this.scores.get(scoreHolder.getNameForScoreboard());
        if (lv != null) {
            boolean bl = lv.remove(objective);
            if (!lv.hasScores()) {
                Scores lv2 = this.scores.remove(scoreHolder.getNameForScoreboard());
                if (lv2 != null) {
                    this.onScoreHolderRemoved(scoreHolder);
                }
            } else if (bl) {
                this.onScoreRemoved(scoreHolder, objective);
            }
        }
    }

    public Object2IntMap<ScoreboardObjective> getScoreHolderObjectives(ScoreHolder scoreHolder) {
        Scores lv = this.scores.get(scoreHolder.getNameForScoreboard());
        return lv != null ? lv.getScoresAsIntMap() : Object2IntMaps.emptyMap();
    }

    public void removeObjective(ScoreboardObjective objective) {
        this.objectives.remove(objective.getName());
        for (ScoreboardDisplaySlot lv : ScoreboardDisplaySlot.values()) {
            if (this.getObjectiveForSlot(lv) != objective) continue;
            this.setObjectiveSlot(lv, null);
        }
        List list = (List)this.objectivesByCriterion.get(objective.getCriterion());
        if (list != null) {
            list.remove(objective);
        }
        for (Scores lv2 : this.scores.values()) {
            lv2.remove(objective);
        }
        this.updateRemovedObjective(objective);
    }

    public void setObjectiveSlot(ScoreboardDisplaySlot slot, @Nullable ScoreboardObjective objective) {
        this.objectiveSlots.put(slot, objective);
    }

    @Nullable
    public ScoreboardObjective getObjectiveForSlot(ScoreboardDisplaySlot slot) {
        return this.objectiveSlots.get(slot);
    }

    @Nullable
    public Team getTeam(String name) {
        return (Team)this.teams.get(name);
    }

    public Team addTeam(String name) {
        Team lv = this.getTeam(name);
        if (lv != null) {
            LOGGER.warn("Requested creation of existing team '{}'", (Object)name);
            return lv;
        }
        lv = new Team(this, name);
        this.teams.put(name, lv);
        this.updateScoreboardTeamAndPlayers(lv);
        return lv;
    }

    public void removeTeam(Team team) {
        this.teams.remove(team.getName());
        for (String string : team.getPlayerList()) {
            this.teamsByScoreHolder.remove(string);
        }
        this.updateRemovedTeam(team);
    }

    public boolean addScoreHolderToTeam(String scoreHolderName, Team team) {
        if (this.getScoreHolderTeam(scoreHolderName) != null) {
            this.clearTeam(scoreHolderName);
        }
        this.teamsByScoreHolder.put(scoreHolderName, team);
        return team.getPlayerList().add(scoreHolderName);
    }

    public boolean clearTeam(String scoreHolderName) {
        Team lv = this.getScoreHolderTeam(scoreHolderName);
        if (lv != null) {
            this.removeScoreHolderFromTeam(scoreHolderName, lv);
            return true;
        }
        return false;
    }

    public void removeScoreHolderFromTeam(String scoreHolderName, Team team) {
        if (this.getScoreHolderTeam(scoreHolderName) != team) {
            throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + team.getName() + "'.");
        }
        this.teamsByScoreHolder.remove(scoreHolderName);
        team.getPlayerList().remove(scoreHolderName);
    }

    public Collection<String> getTeamNames() {
        return this.teams.keySet();
    }

    public Collection<Team> getTeams() {
        return this.teams.values();
    }

    @Nullable
    public Team getScoreHolderTeam(String scoreHolderName) {
        return (Team)this.teamsByScoreHolder.get(scoreHolderName);
    }

    public void updateObjective(ScoreboardObjective objective) {
    }

    public void updateExistingObjective(ScoreboardObjective objective) {
    }

    public void updateRemovedObjective(ScoreboardObjective objective) {
    }

    protected void updateScore(ScoreHolder scoreHolder, ScoreboardObjective objective, ScoreboardScore score) {
    }

    protected void resetScore(ScoreHolder scoreHolder, ScoreboardObjective objective) {
    }

    public void onScoreHolderRemoved(ScoreHolder scoreHolder) {
    }

    public void onScoreRemoved(ScoreHolder scoreHolder, ScoreboardObjective objective) {
    }

    public void updateScoreboardTeamAndPlayers(Team team) {
    }

    public void updateScoreboardTeam(Team team) {
    }

    public void updateRemovedTeam(Team team) {
    }

    public void clearDeadEntity(Entity entity) {
        if (entity instanceof PlayerEntity || entity.isAlive()) {
            return;
        }
        this.removeScores(entity);
        this.clearTeam(entity.getNameForScoreboard());
    }

    protected NbtList toNbt(RegistryWrapper.WrapperLookup registries) {
        NbtList lv = new NbtList();
        this.scores.forEach((name, scores) -> scores.getScores().forEach((objective, score) -> {
            NbtCompound lv = score.toNbt(registries);
            lv.putString("Name", (String)name);
            lv.putString("Objective", objective.getName());
            lv.add(lv);
        }));
        return lv;
    }

    protected void readNbt(NbtList list, RegistryWrapper.WrapperLookup registries) {
        for (int i = 0; i < list.size(); ++i) {
            NbtCompound lv = list.getCompound(i);
            ScoreboardScore lv2 = ScoreboardScore.fromNbt(lv, registries);
            String string = lv.getString("Name");
            String string2 = lv.getString("Objective");
            ScoreboardObjective lv3 = this.getNullableObjective(string2);
            if (lv3 == null) {
                LOGGER.error("Unknown objective {} for name {}, ignoring", (Object)string2, (Object)string);
                continue;
            }
            this.getScores(string).put(lv3, lv2);
        }
    }
}

