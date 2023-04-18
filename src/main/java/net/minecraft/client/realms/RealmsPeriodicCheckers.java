package net.minecraft.client.realms;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.util.PeriodicRunnerFactory;
import net.minecraft.client.realms.util.RealmsPersistence;
import net.minecraft.client.util.Backoff;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class RealmsPeriodicCheckers {
   public final PeriodicRunnerFactory runnerFactory;
   public final PeriodicRunnerFactory.PeriodicRunner notifications;
   public final PeriodicRunnerFactory.PeriodicRunner serverList;
   public final PeriodicRunnerFactory.PeriodicRunner liveStats;
   public final PeriodicRunnerFactory.PeriodicRunner pendingInvitesCount;
   public final PeriodicRunnerFactory.PeriodicRunner trialAvailability;
   public final PeriodicRunnerFactory.PeriodicRunner news;
   public final RealmsNewsUpdater newsUpdater;

   public RealmsPeriodicCheckers(RealmsClient client) {
      this.runnerFactory = new PeriodicRunnerFactory(Util.getIoWorkerExecutor(), TimeUnit.MILLISECONDS, Util.nanoTimeSupplier);
      this.newsUpdater = new RealmsNewsUpdater(new RealmsPersistence());
      this.serverList = this.runnerFactory.create("server list", () -> {
         return client.listWorlds().servers;
      }, Duration.ofSeconds(60L), Backoff.ONE_CYCLE);
      PeriodicRunnerFactory var10001 = this.runnerFactory;
      Objects.requireNonNull(client);
      this.liveStats = var10001.create("live stats", client::getLiveStats, Duration.ofSeconds(10L), Backoff.ONE_CYCLE);
      var10001 = this.runnerFactory;
      Objects.requireNonNull(client);
      this.pendingInvitesCount = var10001.create("pending invite count", client::pendingInvitesCount, Duration.ofSeconds(10L), Backoff.exponential(360));
      var10001 = this.runnerFactory;
      Objects.requireNonNull(client);
      this.trialAvailability = var10001.create("trial availablity", client::trialAvailable, Duration.ofSeconds(60L), Backoff.exponential(60));
      var10001 = this.runnerFactory;
      Objects.requireNonNull(client);
      this.news = var10001.create("unread news", client::getNews, Duration.ofMinutes(5L), Backoff.ONE_CYCLE);
      var10001 = this.runnerFactory;
      Objects.requireNonNull(client);
      this.notifications = var10001.create("notifications", client::listNotifications, Duration.ofMinutes(5L), Backoff.ONE_CYCLE);
   }
}
