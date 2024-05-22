/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.scoreboard;

import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public interface ScoreAccess {
    public int getScore();

    public void setScore(int var1);

    default public int incrementScore(int amount) {
        int j = this.getScore() + amount;
        this.setScore(j);
        return j;
    }

    default public int incrementScore() {
        return this.incrementScore(1);
    }

    default public void resetScore() {
        this.setScore(0);
    }

    public boolean isLocked();

    public void unlock();

    public void lock();

    @Nullable
    public Text getDisplayText();

    public void setDisplayText(@Nullable Text var1);

    public void setNumberFormat(@Nullable NumberFormat var1);
}

