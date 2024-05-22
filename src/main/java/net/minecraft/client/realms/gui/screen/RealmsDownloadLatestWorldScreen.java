/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.realms.gui.screen;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.FileDownload;
import net.minecraft.client.realms.SizeUnit;
import net.minecraft.client.realms.dto.WorldDownload;
import net.minecraft.client.realms.gui.RealmsPopups;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsDownloadLatestWorldScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ReentrantLock DOWNLOAD_LOCK = new ReentrantLock();
    private static final int field_41772 = 200;
    private static final int field_41769 = 80;
    private static final int field_41770 = 95;
    private static final int field_41771 = 1;
    private final Screen parent;
    private final WorldDownload worldDownload;
    private final Text downloadTitle;
    private final RateLimiter narrationRateLimiter;
    private ButtonWidget cancelButton;
    private final String worldName;
    private final DownloadStatus downloadStatus;
    @Nullable
    private volatile Text downloadError;
    private volatile Text status = Text.translatable("mco.download.preparing");
    @Nullable
    private volatile String progress;
    private volatile boolean cancelled;
    private volatile boolean showDots = true;
    private volatile boolean finished;
    private volatile boolean extracting;
    @Nullable
    private Long previousWrittenBytes;
    @Nullable
    private Long previousTimeSnapshot;
    private long bytesPerSecond;
    private int animTick;
    private static final String[] DOTS = new String[]{"", ".", ". .", ". . ."};
    private int dotIndex;
    private boolean checked;
    private final BooleanConsumer onBack;

    public RealmsDownloadLatestWorldScreen(Screen parent, WorldDownload worldDownload, String worldName, BooleanConsumer onBack) {
        super(NarratorManager.EMPTY);
        this.onBack = onBack;
        this.parent = parent;
        this.worldName = worldName;
        this.worldDownload = worldDownload;
        this.downloadStatus = new DownloadStatus();
        this.downloadTitle = Text.translatable("mco.download.title");
        this.narrationRateLimiter = RateLimiter.create(0.1f);
    }

    @Override
    public void init() {
        this.cancelButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.close()).dimensions((this.width - 200) / 2, this.height - 42, 200, 20).build());
        this.checkDownloadSize();
    }

    private void checkDownloadSize() {
        if (this.finished || this.checked) {
            return;
        }
        this.checked = true;
        if (this.getContentLength(this.worldDownload.downloadLink) >= 0x140000000L) {
            MutableText lv = Text.translatable("mco.download.confirmation.oversized", SizeUnit.getUserFriendlyString(0x140000000L));
            this.client.setScreen(RealmsPopups.createNonContinuableWarningPopup(this, lv, arg -> {
                this.client.setScreen(this);
                this.downloadSave();
            }));
        } else {
            this.downloadSave();
        }
    }

    private long getContentLength(String downloadLink) {
        FileDownload lv = new FileDownload();
        return lv.contentLength(downloadLink);
    }

    @Override
    public void tick() {
        super.tick();
        ++this.animTick;
        if (this.status != null && this.narrationRateLimiter.tryAcquire(1)) {
            Text lv = this.getNarration();
            this.client.getNarratorManager().narrate(lv);
        }
    }

    private Text getNarration() {
        ArrayList<Text> list = Lists.newArrayList();
        list.add(this.downloadTitle);
        list.add(this.status);
        if (this.progress != null) {
            list.add(Text.translatable("mco.download.percent", this.progress));
            list.add(Text.translatable("mco.download.speed.narration", SizeUnit.getUserFriendlyString(this.bytesPerSecond)));
        }
        if (this.downloadError != null) {
            list.add(this.downloadError);
        }
        return ScreenTexts.joinLines(list);
    }

    @Override
    public void close() {
        this.cancelled = true;
        if (this.finished && this.onBack != null && this.downloadError == null) {
            this.onBack.accept(true);
        }
        this.client.setScreen(this.parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.downloadTitle, this.width / 2, 20, Colors.WHITE);
        context.drawCenteredTextWithShadow(this.textRenderer, this.status, this.width / 2, 50, Colors.WHITE);
        if (this.showDots) {
            this.drawDots(context);
        }
        if (this.downloadStatus.bytesWritten != 0L && !this.cancelled) {
            this.drawProgressBar(context);
            this.drawDownloadSpeed(context);
        }
        if (this.downloadError != null) {
            context.drawCenteredTextWithShadow(this.textRenderer, this.downloadError, this.width / 2, 110, Colors.RED);
        }
    }

    private void drawDots(DrawContext context) {
        int i = this.textRenderer.getWidth(this.status);
        if (this.animTick != 0 && this.animTick % 10 == 0) {
            ++this.dotIndex;
        }
        context.drawText(this.textRenderer, DOTS[this.dotIndex % DOTS.length], this.width / 2 + i / 2 + 5, 50, Colors.WHITE, false);
    }

    private void drawProgressBar(DrawContext context) {
        double d = Math.min((double)this.downloadStatus.bytesWritten / (double)this.downloadStatus.totalBytes, 1.0);
        this.progress = String.format(Locale.ROOT, "%.1f", d * 100.0);
        int i = (this.width - 200) / 2;
        int j = i + (int)Math.round(200.0 * d);
        context.fill(i - 1, 79, j + 1, 96, Colors.WHITE);
        context.fill(i, 80, j, 95, Colors.GRAY);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("mco.download.percent", this.progress), this.width / 2, 84, Colors.WHITE);
    }

    private void drawDownloadSpeed(DrawContext context) {
        if (this.animTick % 20 == 0) {
            if (this.previousWrittenBytes != null) {
                long l = Util.getMeasuringTimeMs() - this.previousTimeSnapshot;
                if (l == 0L) {
                    l = 1L;
                }
                this.bytesPerSecond = 1000L * (this.downloadStatus.bytesWritten - this.previousWrittenBytes) / l;
                this.drawDownloadSpeed0(context, this.bytesPerSecond);
            }
            this.previousWrittenBytes = this.downloadStatus.bytesWritten;
            this.previousTimeSnapshot = Util.getMeasuringTimeMs();
        } else {
            this.drawDownloadSpeed0(context, this.bytesPerSecond);
        }
    }

    private void drawDownloadSpeed0(DrawContext context, long bytesPerSecond) {
        if (bytesPerSecond > 0L) {
            int i = this.textRenderer.getWidth(this.progress);
            context.drawText(this.textRenderer, Text.translatable("mco.download.speed", SizeUnit.getUserFriendlyString(bytesPerSecond)), this.width / 2 + i / 2 + 15, 84, Colors.WHITE, false);
        }
    }

    private void downloadSave() {
        new Thread(() -> {
            try {
                if (!DOWNLOAD_LOCK.tryLock(1L, TimeUnit.SECONDS)) {
                    this.status = Text.translatable("mco.download.failed");
                    return;
                }
                if (this.cancelled) {
                    this.downloadCancelled();
                    return;
                }
                this.status = Text.translatable("mco.download.downloading", this.worldName);
                FileDownload lv = new FileDownload();
                lv.contentLength(this.worldDownload.downloadLink);
                lv.downloadWorld(this.worldDownload, this.worldName, this.downloadStatus, this.client.getLevelStorage());
                while (!lv.isFinished()) {
                    if (lv.isError()) {
                        lv.cancel();
                        this.downloadError = Text.translatable("mco.download.failed");
                        this.cancelButton.setMessage(ScreenTexts.DONE);
                        return;
                    }
                    if (lv.isExtracting()) {
                        if (!this.extracting) {
                            this.status = Text.translatable("mco.download.extracting");
                        }
                        this.extracting = true;
                    }
                    if (this.cancelled) {
                        lv.cancel();
                        this.downloadCancelled();
                        return;
                    }
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException interruptedException) {
                        LOGGER.error("Failed to check Realms backup download status");
                    }
                }
                this.finished = true;
                this.status = Text.translatable("mco.download.done");
                this.cancelButton.setMessage(ScreenTexts.DONE);
            } catch (InterruptedException interruptedException2) {
                LOGGER.error("Could not acquire upload lock");
            } catch (Exception exception) {
                this.downloadError = Text.translatable("mco.download.failed");
                LOGGER.info("Exception while downloading world", exception);
            } finally {
                if (!DOWNLOAD_LOCK.isHeldByCurrentThread()) {
                    return;
                }
                DOWNLOAD_LOCK.unlock();
                this.showDots = false;
                this.finished = true;
            }
        }).start();
    }

    private void downloadCancelled() {
        this.status = Text.translatable("mco.download.cancelled");
    }

    @Environment(value=EnvType.CLIENT)
    public static class DownloadStatus {
        public volatile long bytesWritten;
        public volatile long totalBytes;
    }
}

