/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.RealmsNewsUpdater;
import net.minecraft.client.realms.dto.RealmsNews;
import net.minecraft.client.realms.dto.RealmsNotification;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsServerList;
import net.minecraft.client.realms.dto.RealmsServerPlayerList;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.realms.util.PeriodicRunnerFactory;
import net.minecraft.client.realms.util.RealmsPersistence;
import net.minecraft.client.util.Backoff;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class RealmsPeriodicCheckers {
    public final PeriodicRunnerFactory runnerFactory = new PeriodicRunnerFactory(Util.getIoWorkerExecutor(), TimeUnit.MILLISECONDS, Util.nanoTimeSupplier);
    private final List<PeriodicRunnerFactory.PeriodicRunner<?>> checkers;
    public final PeriodicRunnerFactory.PeriodicRunner<List<RealmsNotification>> notifications;
    public final PeriodicRunnerFactory.PeriodicRunner<AvailableServers> serverList;
    public final PeriodicRunnerFactory.PeriodicRunner<Integer> pendingInvitesCount;
    public final PeriodicRunnerFactory.PeriodicRunner<Boolean> trialAvailability;
    public final PeriodicRunnerFactory.PeriodicRunner<RealmsNews> news;
    public final PeriodicRunnerFactory.PeriodicRunner<RealmsServerPlayerList> field_52122;
    public final RealmsNewsUpdater newsUpdater = new RealmsNewsUpdater(new RealmsPersistence());

    public RealmsPeriodicCheckers(RealmsClient client) {
        this.serverList = this.runnerFactory.create("server list", () -> {
            RealmsServerList lv = client.listWorlds();
            if (RealmsMainScreen.isSnapshotRealmsEligible()) {
                return new AvailableServers(lv.servers, client.getPrereleaseEligibleServers());
            }
            return new AvailableServers(lv.servers, List.of());
        }, Duration.ofSeconds(60L), Backoff.ONE_CYCLE);
        this.pendingInvitesCount = this.runnerFactory.create("pending invite count", client::pendingInvitesCount, Duration.ofSeconds(10L), Backoff.exponential(360));
        this.trialAvailability = this.runnerFactory.create("trial availablity", client::trialAvailable, Duration.ofSeconds(60L), Backoff.exponential(60));
        this.news = this.runnerFactory.create("unread news", client::getNews, Duration.ofMinutes(5L), Backoff.ONE_CYCLE);
        this.notifications = this.runnerFactory.create("notifications", client::listNotifications, Duration.ofMinutes(5L), Backoff.ONE_CYCLE);
        this.field_52122 = this.runnerFactory.create("online players", client::getLiveStats, Duration.ofSeconds(10L), Backoff.ONE_CYCLE);
        this.checkers = List.of(this.notifications, this.serverList, this.pendingInvitesCount, this.trialAvailability, this.news, this.field_52122);
    }

    public List<PeriodicRunnerFactory.PeriodicRunner<?>> getCheckers() {
        return this.checkers;
    }

    @Environment(value=EnvType.CLIENT)
    public record AvailableServers(List<RealmsServer> serverList, List<RealmsServer> availableSnapshotServers) {
    }
}

