/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.session.report;

import com.mojang.authlib.minecraft.UserApiService;
import java.util.Objects;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.session.report.AbuseReport;
import net.minecraft.client.session.report.AbuseReportSender;
import net.minecraft.client.session.report.ReporterEnvironment;
import net.minecraft.client.session.report.log.ChatLog;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public final class AbuseReportContext {
    private static final int MAX_LOGS = 1024;
    private final AbuseReportSender sender;
    private final ReporterEnvironment environment;
    private final ChatLog chatLog;
    @Nullable
    private AbuseReport draft;

    public AbuseReportContext(AbuseReportSender sender, ReporterEnvironment environment, ChatLog chatLog) {
        this.sender = sender;
        this.environment = environment;
        this.chatLog = chatLog;
    }

    public static AbuseReportContext create(ReporterEnvironment environment, UserApiService userApiService) {
        ChatLog lv = new ChatLog(1024);
        AbuseReportSender lv2 = AbuseReportSender.create(environment, userApiService);
        return new AbuseReportContext(lv2, environment, lv);
    }

    public void tryShowDraftScreen(MinecraftClient client, Screen parent, Runnable callback, boolean quit) {
        if (this.draft != null) {
            AbuseReport lv = this.draft.copy();
            client.setScreen(new ConfirmScreen(confirmed -> {
                this.setDraft(null);
                if (confirmed) {
                    client.setScreen(lv.createReportScreen(parent, this));
                } else {
                    callback.run();
                }
            }, Text.translatable(quit ? "gui.abuseReport.draft.quittotitle.title" : "gui.abuseReport.draft.title"), Text.translatable(quit ? "gui.abuseReport.draft.quittotitle.content" : "gui.abuseReport.draft.content"), Text.translatable("gui.abuseReport.draft.edit"), Text.translatable("gui.abuseReport.draft.discard")));
        } else {
            callback.run();
        }
    }

    public AbuseReportSender getSender() {
        return this.sender;
    }

    public ChatLog getChatLog() {
        return this.chatLog;
    }

    public boolean environmentEquals(ReporterEnvironment environment) {
        return Objects.equals(this.environment, environment);
    }

    public void setDraft(@Nullable AbuseReport draft) {
        this.draft = draft;
    }

    public boolean hasDraft() {
        return this.draft != null;
    }

    public boolean draftPlayerUuidEquals(UUID uuid) {
        return this.hasDraft() && this.draft.playerUuidEquals(uuid);
    }
}

