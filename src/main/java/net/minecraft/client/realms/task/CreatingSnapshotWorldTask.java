/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.realms.task;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.gui.screen.RealmsCreateWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.realms.gui.screen.ResetWorldInfo;
import net.minecraft.client.realms.task.LongRunningTask;
import net.minecraft.client.realms.task.ResettingNormalWorldTask;
import net.minecraft.client.realms.task.WorldCreationTask;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class CreatingSnapshotWorldTask
extends LongRunningTask {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text TITLE_TEXT = Text.translatable("mco.snapshot.creating");
    private final long parentId;
    private final ResetWorldInfo resetWorldInfo;
    private final String name;
    private final String motd;
    private final RealmsMainScreen mainScreen;
    @Nullable
    private WorldCreationTask worldCreationTask;
    @Nullable
    private ResettingNormalWorldTask resettingNormalWorldTask;

    public CreatingSnapshotWorldTask(RealmsMainScreen mainScreen, long parentId, ResetWorldInfo resetWorldInfo, String name, String motd) {
        this.parentId = parentId;
        this.resetWorldInfo = resetWorldInfo;
        this.name = name;
        this.motd = motd;
        this.mainScreen = mainScreen;
    }

    @Override
    public void run() {
        RealmsClient lv = RealmsClient.create();
        try {
            RealmsServer lv2 = lv.createPrereleaseServer(this.parentId);
            this.worldCreationTask = new WorldCreationTask(lv2.id, this.name, this.motd);
            this.resettingNormalWorldTask = new ResettingNormalWorldTask(this.resetWorldInfo, lv2.id, RealmsCreateWorldScreen.CREATING_TEXT, () -> MinecraftClient.getInstance().execute(() -> RealmsMainScreen.play(lv2, this.mainScreen, true)));
            if (this.aborted()) {
                return;
            }
            this.worldCreationTask.run();
            if (this.aborted()) {
                return;
            }
            this.resettingNormalWorldTask.run();
        } catch (RealmsServiceException lv3) {
            LOGGER.error("Couldn't create snapshot world", lv3);
            this.error(lv3);
        } catch (Exception exception) {
            LOGGER.error("Couldn't create snapshot world", exception);
            this.error(exception);
        }
    }

    @Override
    public Text getTitle() {
        return TITLE_TEXT;
    }

    @Override
    public void abortTask() {
        super.abortTask();
        if (this.worldCreationTask != null) {
            this.worldCreationTask.abortTask();
        }
        if (this.resettingNormalWorldTask != null) {
            this.resettingNormalWorldTask.abortTask();
        }
    }
}

