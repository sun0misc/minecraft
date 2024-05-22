/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.task;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.dto.Backup;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.exception.RetryCallException;
import net.minecraft.client.realms.gui.screen.RealmsConfigureWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsGenericErrorScreen;
import net.minecraft.client.realms.task.LongRunningTask;
import net.minecraft.text.Text;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RestoreTask
extends LongRunningTask {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text TITLE = Text.translatable("mco.backup.restoring");
    private final Backup backup;
    private final long worldId;
    private final RealmsConfigureWorldScreen lastScreen;

    public RestoreTask(Backup backup, long worldId, RealmsConfigureWorldScreen lastScreen) {
        this.backup = backup;
        this.worldId = worldId;
        this.lastScreen = lastScreen;
    }

    @Override
    public void run() {
        RealmsClient lv = RealmsClient.create();
        for (int i = 0; i < 25; ++i) {
            try {
                if (this.aborted()) {
                    return;
                }
                lv.restoreWorld(this.worldId, this.backup.backupId);
                RestoreTask.pause(1L);
                if (this.aborted()) {
                    return;
                }
                RestoreTask.setScreen(this.lastScreen.getNewScreen());
                return;
            } catch (RetryCallException lv2) {
                if (this.aborted()) {
                    return;
                }
                RestoreTask.pause(lv2.delaySeconds);
                continue;
            } catch (RealmsServiceException lv3) {
                if (this.aborted()) {
                    return;
                }
                LOGGER.error("Couldn't restore backup", lv3);
                RestoreTask.setScreen(new RealmsGenericErrorScreen(lv3, (Screen)this.lastScreen));
                return;
            } catch (Exception exception) {
                if (this.aborted()) {
                    return;
                }
                LOGGER.error("Couldn't restore backup", exception);
                this.error(exception);
                return;
            }
        }
    }

    @Override
    public Text getTitle() {
        return TITLE;
    }
}

