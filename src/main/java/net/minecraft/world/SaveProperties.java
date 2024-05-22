/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world;

import com.mojang.serialization.Lifecycle;
import java.util.Locale;
import java.util.Set;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.ServerWorldProperties;
import org.jetbrains.annotations.Nullable;

public interface SaveProperties {
    public static final int ANVIL_FORMAT_ID = 19133;
    public static final int MCREGION_FORMAT_ID = 19132;

    public DataConfiguration getDataConfiguration();

    public void updateLevelInfo(DataConfiguration var1);

    public boolean isModded();

    public Set<String> getServerBrands();

    public Set<String> getRemovedFeatures();

    public void addServerBrand(String var1, boolean var2);

    default public void populateCrashReport(CrashReportSection section) {
        section.add("Known server brands", () -> String.join((CharSequence)", ", this.getServerBrands()));
        section.add("Removed feature flags", () -> String.join((CharSequence)", ", this.getRemovedFeatures()));
        section.add("Level was modded", () -> Boolean.toString(this.isModded()));
        section.add("Level storage version", () -> {
            int i = this.getVersion();
            return String.format(Locale.ROOT, "0x%05X - %s", i, this.getFormatName(i));
        });
    }

    default public String getFormatName(int id) {
        switch (id) {
            case 19133: {
                return "Anvil";
            }
            case 19132: {
                return "McRegion";
            }
        }
        return "Unknown?";
    }

    @Nullable
    public NbtCompound getCustomBossEvents();

    public void setCustomBossEvents(@Nullable NbtCompound var1);

    public ServerWorldProperties getMainWorldProperties();

    public LevelInfo getLevelInfo();

    public NbtCompound cloneWorldNbt(DynamicRegistryManager var1, @Nullable NbtCompound var2);

    public boolean isHardcore();

    public int getVersion();

    public String getLevelName();

    public GameMode getGameMode();

    public void setGameMode(GameMode var1);

    public boolean areCommandsAllowed();

    public Difficulty getDifficulty();

    public void setDifficulty(Difficulty var1);

    public boolean isDifficultyLocked();

    public void setDifficultyLocked(boolean var1);

    public GameRules getGameRules();

    @Nullable
    public NbtCompound getPlayerData();

    public EnderDragonFight.Data getDragonFight();

    public void setDragonFight(EnderDragonFight.Data var1);

    public GeneratorOptions getGeneratorOptions();

    public boolean isFlatWorld();

    public boolean isDebugWorld();

    public Lifecycle getLifecycle();

    default public FeatureSet getEnabledFeatures() {
        return this.getDataConfiguration().enabledFeatures();
    }
}

