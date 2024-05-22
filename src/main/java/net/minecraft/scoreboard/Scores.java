/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.scoreboard;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import org.jetbrains.annotations.Nullable;

class Scores {
    private final Reference2ObjectOpenHashMap<ScoreboardObjective, ScoreboardScore> scores = new Reference2ObjectOpenHashMap(16, 0.5f);

    Scores() {
    }

    @Nullable
    public ScoreboardScore get(ScoreboardObjective objective) {
        return this.scores.get(objective);
    }

    public ScoreboardScore getOrCreate(ScoreboardObjective objective, Consumer<ScoreboardScore> scoreConsumer) {
        return this.scores.computeIfAbsent(objective, objective2 -> {
            ScoreboardScore lv = new ScoreboardScore();
            scoreConsumer.accept(lv);
            return lv;
        });
    }

    public boolean remove(ScoreboardObjective objective) {
        return this.scores.remove(objective) != null;
    }

    public boolean hasScores() {
        return !this.scores.isEmpty();
    }

    public Object2IntMap<ScoreboardObjective> getScoresAsIntMap() {
        Object2IntOpenHashMap<ScoreboardObjective> object2IntMap = new Object2IntOpenHashMap<ScoreboardObjective>();
        this.scores.forEach((objective, score) -> object2IntMap.put((ScoreboardObjective)objective, score.getScore()));
        return object2IntMap;
    }

    void put(ScoreboardObjective objective, ScoreboardScore score) {
        this.scores.put(objective, score);
    }

    Map<ScoreboardObjective, ScoreboardScore> getScores() {
        return Collections.unmodifiableMap(this.scores);
    }
}

