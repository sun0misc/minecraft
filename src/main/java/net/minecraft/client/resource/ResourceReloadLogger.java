/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.resource;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ResourceReloadLogger {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private ReloadState reloadState;
    private int reloadCount;

    public void reload(ReloadReason reason, List<ResourcePack> packs) {
        ++this.reloadCount;
        if (this.reloadState != null && !this.reloadState.finished) {
            LOGGER.warn("Reload already ongoing, replacing");
        }
        this.reloadState = new ReloadState(reason, packs.stream().map(ResourcePack::getId).collect(ImmutableList.toImmutableList()));
    }

    public void recover(Throwable throwable) {
        if (this.reloadState == null) {
            LOGGER.warn("Trying to signal reload recovery, but nothing was started");
            this.reloadState = new ReloadState(ReloadReason.UNKNOWN, ImmutableList.of());
        }
        this.reloadState.recovery = new RecoveryEntry(throwable);
    }

    public void finish() {
        if (this.reloadState == null) {
            LOGGER.warn("Trying to finish reload, but nothing was started");
        } else {
            this.reloadState.finished = true;
        }
    }

    public void addReloadSection(CrashReport report) {
        CrashReportSection lv = report.addElement("Last reload");
        lv.add("Reload number", this.reloadCount);
        if (this.reloadState != null) {
            this.reloadState.addReloadSection(lv);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ReloadState {
        private final ReloadReason reason;
        private final List<String> packs;
        @Nullable
        RecoveryEntry recovery;
        boolean finished;

        ReloadState(ReloadReason reason, List<String> packs) {
            this.reason = reason;
            this.packs = packs;
        }

        public void addReloadSection(CrashReportSection section) {
            section.add("Reload reason", this.reason.name);
            section.add("Finished", this.finished ? "Yes" : "No");
            section.add("Packs", () -> String.join((CharSequence)", ", this.packs));
            if (this.recovery != null) {
                this.recovery.addRecoverySection(section);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum ReloadReason {
        INITIAL("initial"),
        MANUAL("manual"),
        UNKNOWN("unknown");

        final String name;

        private ReloadReason(String name) {
            this.name = name;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class RecoveryEntry {
        private final Throwable throwable;

        RecoveryEntry(Throwable throwable) {
            this.throwable = throwable;
        }

        public void addRecoverySection(CrashReportSection section) {
            section.add("Recovery", "Yes");
            section.add("Recovery reason", () -> {
                StringWriter stringWriter = new StringWriter();
                this.throwable.printStackTrace(new PrintWriter(stringWriter));
                return stringWriter.toString();
            });
        }
    }
}

