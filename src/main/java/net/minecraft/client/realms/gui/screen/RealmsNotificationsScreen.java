package net.minecraft.client.realms.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.RealmsPeriodicCheckers;
import net.minecraft.client.realms.dto.RealmsNotification;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.util.PeriodicRunnerFactory;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class RealmsNotificationsScreen extends RealmsScreen {
   private static final Identifier INVITE_ICON = new Identifier("realms", "textures/gui/realms/invite_icon.png");
   private static final Identifier TRIAL_ICON = new Identifier("realms", "textures/gui/realms/trial_icon.png");
   private static final Identifier NEWS_NOTIFICATION = new Identifier("realms", "textures/gui/realms/news_notification_mainscreen.png");
   private static final Identifier UNSEEN_NOTIFICATION = new Identifier("minecraft", "textures/gui/unseen_notification.png");
   @Nullable
   private PeriodicRunnerFactory.RunnersManager periodicRunnersManager;
   @Nullable
   private NotificationRunnersFactory currentRunnersFactory;
   private volatile int pendingInvitesCount;
   static boolean checkedMcoAvailability;
   private static boolean trialAvailable;
   static boolean validClient;
   private static boolean hasUnreadNews;
   private static boolean hasUnseenNotification;
   private final NotificationRunnersFactory newsAndNotifications = new NotificationRunnersFactory() {
      public PeriodicRunnerFactory.RunnersManager createPeriodicRunnersManager(RealmsPeriodicCheckers checkers) {
         PeriodicRunnerFactory.RunnersManager lv = checkers.runnerFactory.create();
         RealmsNotificationsScreen.this.addRunners(checkers, lv);
         RealmsNotificationsScreen.this.addNotificationRunner(checkers, lv);
         return lv;
      }

      public boolean isNews() {
         return true;
      }
   };
   private final NotificationRunnersFactory notificationsOnly = new NotificationRunnersFactory() {
      public PeriodicRunnerFactory.RunnersManager createPeriodicRunnersManager(RealmsPeriodicCheckers checkers) {
         PeriodicRunnerFactory.RunnersManager lv = checkers.runnerFactory.create();
         RealmsNotificationsScreen.this.addNotificationRunner(checkers, lv);
         return lv;
      }

      public boolean isNews() {
         return false;
      }
   };

   public RealmsNotificationsScreen() {
      super(NarratorManager.EMPTY);
   }

   public void init() {
      this.checkIfMcoEnabled();
      if (this.periodicRunnersManager != null) {
         this.periodicRunnersManager.forceRunListeners();
      }

   }

   public void onDisplayed() {
      super.onDisplayed();
      this.client.getRealmsPeriodicCheckers().notifications.reset();
   }

   @Nullable
   private NotificationRunnersFactory getRunnersFactory() {
      boolean bl = this.isTitleScreen() && validClient;
      if (!bl) {
         return null;
      } else {
         return this.shouldShowRealmsNews() ? this.newsAndNotifications : this.notificationsOnly;
      }
   }

   public void tick() {
      NotificationRunnersFactory lv = this.getRunnersFactory();
      if (!Objects.equals(this.currentRunnersFactory, lv)) {
         this.currentRunnersFactory = lv;
         if (this.currentRunnersFactory != null) {
            this.periodicRunnersManager = this.currentRunnersFactory.createPeriodicRunnersManager(this.client.getRealmsPeriodicCheckers());
         } else {
            this.periodicRunnersManager = null;
         }
      }

      if (this.periodicRunnersManager != null) {
         this.periodicRunnersManager.runAll();
      }

   }

   private boolean shouldShowRealmsNews() {
      return (Boolean)this.client.options.getRealmsNotifications().getValue();
   }

   private boolean isTitleScreen() {
      return this.client.currentScreen instanceof TitleScreen;
   }

   private void checkIfMcoEnabled() {
      if (!checkedMcoAvailability) {
         checkedMcoAvailability = true;
         (new Thread("Realms Notification Availability checker #1") {
            public void run() {
               RealmsClient lv = RealmsClient.create();

               try {
                  RealmsClient.CompatibleVersionResponse lv2 = lv.clientCompatible();
                  if (lv2 != RealmsClient.CompatibleVersionResponse.COMPATIBLE) {
                     return;
                  }
               } catch (RealmsServiceException var3) {
                  if (var3.httpResultCode != 401) {
                     RealmsNotificationsScreen.checkedMcoAvailability = false;
                  }

                  return;
               }

               RealmsNotificationsScreen.validClient = true;
            }
         }).start();
      }

   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      if (validClient) {
         this.drawIcons(matrices, mouseX, mouseY);
      }

      super.render(matrices, mouseX, mouseY, delta);
   }

   private void drawIcons(MatrixStack matrices, int mouseX, int mouseY) {
      int k = this.pendingInvitesCount;
      int l = true;
      int m = this.height / 4 + 48;
      int n = this.width / 2 + 80;
      int o = m + 48 + 2;
      int p = 0;
      if (hasUnseenNotification) {
         RenderSystem.setShaderTexture(0, UNSEEN_NOTIFICATION);
         DrawableHelper.drawTexture(matrices, n - p + 5, o + 3, 0.0F, 0.0F, 10, 10, 10, 10);
         p += 14;
      }

      if (this.currentRunnersFactory != null && this.currentRunnersFactory.isNews()) {
         if (hasUnreadNews) {
            RenderSystem.setShaderTexture(0, NEWS_NOTIFICATION);
            matrices.push();
            matrices.scale(0.4F, 0.4F, 0.4F);
            DrawableHelper.drawTexture(matrices, (int)((double)(n + 2 - p) * 2.5), (int)((double)o * 2.5), 0.0F, 0.0F, 40, 40, 40, 40);
            matrices.pop();
            p += 14;
         }

         if (k != 0) {
            RenderSystem.setShaderTexture(0, INVITE_ICON);
            DrawableHelper.drawTexture(matrices, n - p, o - 6, 0.0F, 0.0F, 15, 25, 31, 25);
            p += 16;
         }

         if (trialAvailable) {
            RenderSystem.setShaderTexture(0, TRIAL_ICON);
            int q = 0;
            if ((Util.getMeasuringTimeMs() / 800L & 1L) == 1L) {
               q = 8;
            }

            DrawableHelper.drawTexture(matrices, n + 4 - p, o + 4, 0.0F, (float)q, 8, 8, 8, 16);
         }
      }

   }

   void addRunners(RealmsPeriodicCheckers checkers, PeriodicRunnerFactory.RunnersManager manager) {
      manager.add(checkers.pendingInvitesCount, (pendingInvitesCount) -> {
         this.pendingInvitesCount = pendingInvitesCount;
      });
      manager.add(checkers.trialAvailability, (trialAvailable) -> {
         RealmsNotificationsScreen.trialAvailable = trialAvailable;
      });
      manager.add(checkers.news, (news) -> {
         checkers.newsUpdater.updateNews(news);
         hasUnreadNews = checkers.newsUpdater.hasUnreadNews();
      });
   }

   void addNotificationRunner(RealmsPeriodicCheckers checkers, PeriodicRunnerFactory.RunnersManager manager) {
      manager.add(checkers.notifications, (notifications) -> {
         hasUnseenNotification = false;
         Iterator var1 = notifications.iterator();

         while(var1.hasNext()) {
            RealmsNotification lv = (RealmsNotification)var1.next();
            if (!lv.isSeen()) {
               hasUnseenNotification = true;
               break;
            }
         }

      });
   }

   @Environment(EnvType.CLIENT)
   private interface NotificationRunnersFactory {
      PeriodicRunnerFactory.RunnersManager createPeriodicRunnersManager(RealmsPeriodicCheckers checkers);

      boolean isNews();
   }
}
