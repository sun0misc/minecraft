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
import net.minecraft.client.realms.dto.WorldDownload;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.exception.RetryCallException;
import net.minecraft.client.realms.gui.screen.RealmsDownloadLatestWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsGenericErrorScreen;
import net.minecraft.client.realms.task.LongRunningTask;
import net.minecraft.text.Text;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class DownloadTask
extends LongRunningTask {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text TITLE = Text.translatable("mco.download.preparing");
    private final long worldId;
    private final int slot;
    private final Screen lastScreen;
    private final String downloadName;

    public DownloadTask(long worldId, int slot, String downloadName, Screen lastScreen) {
        this.worldId = worldId;
        this.slot = slot;
        this.lastScreen = lastScreen;
        this.downloadName = downloadName;
    }

    @Override
    public void run() {
        RealmsClient lv = RealmsClient.create();
        for (int i = 0; i < 25; ++i) {
            try {
                if (this.aborted()) {
                    return;
                }
                WorldDownload lv2 = lv.download(this.worldId, this.slot);
                DownloadTask.pause(1L);
                if (this.aborted()) {
                    return;
                }
                DownloadTask.setScreen(new RealmsDownloadLatestWorldScreen(this.lastScreen, lv2, this.downloadName, bl -> {}));
                return;
            } catch (RetryCallException lv3) {
                if (this.aborted()) {
                    return;
                }
                DownloadTask.pause(lv3.delaySeconds);
                continue;
            } catch (RealmsServiceException lv4) {
                if (this.aborted()) {
                    return;
                }
                LOGGER.error("Couldn't download world data", lv4);
                DownloadTask.setScreen(new RealmsGenericErrorScreen(lv4, this.lastScreen));
                return;
            } catch (Exception exception) {
                if (this.aborted()) {
                    return;
                }
                LOGGER.error("Couldn't download world data", exception);
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

