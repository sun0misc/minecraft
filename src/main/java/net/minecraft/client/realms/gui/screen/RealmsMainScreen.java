package net.minecraft.client.realms.gui.screen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.AxisGridWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.IconWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.realms.KeyCombo;
import net.minecraft.client.realms.Ping;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.RealmsNewsUpdater;
import net.minecraft.client.realms.RealmsObjectSelectionList;
import net.minecraft.client.realms.RealmsPeriodicCheckers;
import net.minecraft.client.realms.dto.PingResult;
import net.minecraft.client.realms.dto.RealmsNotification;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsServerPlayerList;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.task.RealmsGetServerDetailsTask;
import net.minecraft.client.realms.util.PeriodicRunnerFactory;
import net.minecraft.client.realms.util.RealmsPersistence;
import net.minecraft.client.realms.util.RealmsServerFilterer;
import net.minecraft.client.realms.util.RealmsUtil;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Urls;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsMainScreen extends RealmsScreen {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final Identifier ON_ICON = new Identifier("realms", "textures/gui/realms/on_icon.png");
   private static final Identifier OFF_ICON = new Identifier("realms", "textures/gui/realms/off_icon.png");
   private static final Identifier EXPIRED_ICON = new Identifier("realms", "textures/gui/realms/expired_icon.png");
   private static final Identifier EXPIRES_SOON_ICON = new Identifier("realms", "textures/gui/realms/expires_soon_icon.png");
   static final Identifier INVITATION_ICON = new Identifier("realms", "textures/gui/realms/invitation_icons.png");
   static final Identifier INVITE_ICON = new Identifier("realms", "textures/gui/realms/invite_icon.png");
   static final Identifier WORLD_ICON = new Identifier("realms", "textures/gui/realms/world_icon.png");
   private static final Identifier REALMS = new Identifier("realms", "textures/gui/title/realms.png");
   private static final Identifier NEWS_ICON = new Identifier("realms", "textures/gui/realms/news_icon.png");
   private static final Identifier POPUP = new Identifier("realms", "textures/gui/realms/popup.png");
   private static final Identifier DARKEN = new Identifier("realms", "textures/gui/realms/darken.png");
   static final Identifier CROSS_ICON = new Identifier("realms", "textures/gui/realms/cross_icon.png");
   private static final Identifier TRIAL_ICON = new Identifier("realms", "textures/gui/realms/trial_icon.png");
   static final Identifier INFO_ICON = new Identifier("minecraft", "textures/gui/info_icon.png");
   static final List TRIAL_MESSAGE_LINES = ImmutableList.of(Text.translatable("mco.trial.message.line1"), Text.translatable("mco.trial.message.line2"));
   static final Text UNINITIALIZED_TEXT = Text.translatable("mco.selectServer.uninitialized");
   static final Text EXPIRED_LIST_TEXT = Text.translatable("mco.selectServer.expiredList");
   private static final Text EXPIRED_RENEW_TEXT = Text.translatable("mco.selectServer.expiredRenew");
   static final Text EXPIRED_TRIAL_TEXT = Text.translatable("mco.selectServer.expiredTrial");
   static final Text MINIGAME_TEXT;
   private static final Text POPUP_TEXT;
   private static final Text PLAY_TEXT;
   private static final Text LEAVE_TEXT;
   private static final Text CONFIGURE_TEXT;
   private static final Text EXPIRED_TEXT;
   private static final Text EXPIRES_SOON_TEXT;
   private static final Text EXPIRES_IN_A_DAY_TEXT;
   private static final Text OPEN_TEXT;
   private static final Text CLOSED_TEXT;
   private static final Text NEWS_TEXT;
   static final Text UNINITIALIZED_BUTTON_NARRATION;
   static final Text TRIAL_NARRATION;
   private static final int field_42862 = 100;
   private static final int field_42863 = 308;
   private static final int field_42864 = 204;
   private static final int field_42865 = 64;
   private static final int field_44509 = 128;
   private static final int field_44510 = 34;
   private static final int field_44511 = 128;
   private static final int field_44512 = 64;
   private static final int field_44513 = 5;
   private static final int field_44514 = 44;
   private static List IMAGES;
   @Nullable
   private PeriodicRunnerFactory.RunnersManager periodicRunnersManager;
   private RealmsServerFilterer serverFilterer;
   private final Set seenNotifications = new HashSet();
   private static boolean overrideConfigure;
   private static int lastScrollYPosition;
   static volatile boolean hasParentalConsent;
   static volatile boolean checkedParentalConsent;
   static volatile boolean checkedClientCompatibility;
   @Nullable
   static Screen realmsGenericErrorScreen;
   private static boolean regionsPinged;
   private final RateLimiter rateLimiter;
   private boolean dontSetConnectedToRealms;
   final Screen parent;
   RealmSelectionList realmSelectionList;
   private boolean hasSelectionList;
   private ButtonWidget playButton;
   private ButtonWidget backButton;
   private ButtonWidget renewButton;
   private ButtonWidget configureButton;
   private ButtonWidget leaveButton;
   private List realmsServers = ImmutableList.of();
   volatile int pendingInvitesCount;
   int animTick;
   private boolean hasFetchedServers;
   boolean popupOpenedByUser;
   private boolean justClosedPopup;
   private volatile boolean trialAvailable;
   private volatile boolean createdTrial;
   private volatile boolean showingPopup;
   volatile boolean hasUnreadNews;
   @Nullable
   volatile String newsLink;
   private int carouselIndex;
   private int carouselTick;
   private boolean hasSwitchedCarouselImage;
   private List keyCombos;
   long lastPlayButtonClickTime;
   private ReentrantLock connectLock = new ReentrantLock();
   private MultilineText popupText;
   private final List notifications;
   private ButtonWidget showPopupButton;
   private PendingInvitesButton pendingInvitesButton;
   private ButtonWidget newsButton;
   private ButtonWidget createTrialButton;
   private ButtonWidget buyARealmButton;
   private ButtonWidget closeButton;

   public RealmsMainScreen(Screen parent) {
      super(NarratorManager.EMPTY);
      this.popupText = MultilineText.EMPTY;
      this.notifications = new ArrayList();
      this.parent = parent;
      this.rateLimiter = RateLimiter.create(0.01666666753590107);
   }

   private boolean shouldShowMessageInList() {
      if (hasParentalConsent() && this.hasFetchedServers) {
         if (this.trialAvailable && !this.createdTrial) {
            return true;
         } else {
            Iterator var1 = this.realmsServers.iterator();

            RealmsServer lv;
            do {
               if (!var1.hasNext()) {
                  return true;
               }

               lv = (RealmsServer)var1.next();
            } while(!lv.ownerUUID.equals(this.client.getSession().getUuid()));

            return false;
         }
      } else {
         return false;
      }
   }

   public boolean shouldShowPopup() {
      if (hasParentalConsent() && this.hasFetchedServers) {
         return this.popupOpenedByUser ? true : this.realmsServers.isEmpty();
      } else {
         return false;
      }
   }

   public void init() {
      this.keyCombos = Lists.newArrayList(new KeyCombo[]{new KeyCombo(new char[]{'3', '2', '1', '4', '5', '6'}, () -> {
         overrideConfigure = !overrideConfigure;
      }), new KeyCombo(new char[]{'9', '8', '7', '1', '2', '3'}, () -> {
         if (RealmsClient.currentEnvironment == RealmsClient.Environment.STAGE) {
            this.switchToProd();
         } else {
            this.switchToStage();
         }

      }), new KeyCombo(new char[]{'9', '8', '7', '4', '5', '6'}, () -> {
         if (RealmsClient.currentEnvironment == RealmsClient.Environment.LOCAL) {
            this.switchToProd();
         } else {
            this.switchToLocal();
         }

      })});
      if (realmsGenericErrorScreen != null) {
         this.client.setScreen(realmsGenericErrorScreen);
      } else {
         this.connectLock = new ReentrantLock();
         if (checkedClientCompatibility && !hasParentalConsent()) {
            this.checkParentalConsent();
         }

         this.checkClientCompatibility();
         if (!this.dontSetConnectedToRealms) {
            this.client.setConnectedToRealms(false);
         }

         this.showingPopup = false;
         this.realmSelectionList = new RealmSelectionList();
         if (lastScrollYPosition != -1) {
            this.realmSelectionList.setScrollAmount((double)lastScrollYPosition);
         }

         this.addSelectableChild(this.realmSelectionList);
         this.hasSelectionList = true;
         this.setInitialFocus(this.realmSelectionList);
         this.addPurchaseButtons();
         this.addLowerButtons();
         this.addInvitesAndNewsButtons();
         this.updateButtonStates((RealmsServer)null);
         this.popupText = MultilineText.create(this.textRenderer, POPUP_TEXT, 100);
         RealmsNewsUpdater lv = this.client.getRealmsPeriodicCheckers().newsUpdater;
         this.hasUnreadNews = lv.hasUnreadNews();
         this.newsLink = lv.getNewsLink();
         if (this.serverFilterer == null) {
            this.serverFilterer = new RealmsServerFilterer(this.client);
         }

         if (this.periodicRunnersManager != null) {
            this.periodicRunnersManager.forceRunListeners();
         }

      }
   }

   private static boolean hasParentalConsent() {
      return checkedParentalConsent && hasParentalConsent;
   }

   public void addInvitesAndNewsButtons() {
      this.pendingInvitesButton = (PendingInvitesButton)this.addDrawableChild(new PendingInvitesButton());
      this.newsButton = (ButtonWidget)this.addDrawableChild(new NewsButton());
      this.showPopupButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.selectServer.purchase"), (button) -> {
         this.popupOpenedByUser = !this.popupOpenedByUser;
      }).dimensions(this.width - 90, 12, 80, 20).build());
   }

   public void addPurchaseButtons() {
      this.createTrialButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.selectServer.trial"), (button) -> {
         if (this.trialAvailable && !this.createdTrial) {
            Util.getOperatingSystem().open("https://aka.ms/startjavarealmstrial");
            this.client.setScreen(this.parent);
         }
      }).dimensions(this.width / 2 + 52, this.popupY0() + 137 - 20, 98, 20).build());
      this.buyARealmButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.selectServer.buy"), (button) -> {
         Util.getOperatingSystem().open("https://aka.ms/BuyJavaRealms");
      }).dimensions(this.width / 2 + 52, this.popupY0() + 160 - 20, 98, 20).build());
      this.closeButton = (ButtonWidget)this.addDrawableChild(new CloseButton());
   }

   public void addLowerButtons() {
      this.playButton = ButtonWidget.builder(PLAY_TEXT, (button) -> {
         this.play(this.findServer(), this);
      }).width(100).build();
      this.configureButton = ButtonWidget.builder(CONFIGURE_TEXT, (button) -> {
         this.configureClicked(this.findServer());
      }).width(100).build();
      this.renewButton = ButtonWidget.builder(EXPIRED_RENEW_TEXT, (button) -> {
         this.onRenew(this.findServer());
      }).width(100).build();
      this.leaveButton = ButtonWidget.builder(LEAVE_TEXT, (button) -> {
         this.leaveClicked(this.findServer());
      }).width(100).build();
      this.backButton = ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
         if (!this.justClosedPopup) {
            this.client.setScreen(this.parent);
         }

      }).width(100).build();
      GridWidget lv = new GridWidget();
      GridWidget.Adder lv2 = lv.createAdder(1);
      AxisGridWidget lv3 = (AxisGridWidget)lv2.add(new AxisGridWidget(308, 20, AxisGridWidget.DisplayAxis.HORIZONTAL), lv2.copyPositioner().marginBottom(4));
      lv3.add(this.playButton);
      lv3.add(this.configureButton);
      lv3.add(this.renewButton);
      AxisGridWidget lv4 = (AxisGridWidget)lv2.add(new AxisGridWidget(204, 20, AxisGridWidget.DisplayAxis.HORIZONTAL), lv2.copyPositioner().alignHorizontalCenter());
      lv4.add(this.leaveButton);
      lv4.add(this.backButton);
      lv.forEachChild((child) -> {
         ClickableWidget var10000 = (ClickableWidget)this.addDrawableChild(child);
      });
      lv.refreshPositions();
      SimplePositioningWidget.setPos(lv, 0, this.height - 64, this.width, 64);
   }

   void updateButtonStates(@Nullable RealmsServer server) {
      this.backButton.active = true;
      if (hasParentalConsent() && this.hasFetchedServers) {
         boolean bl = this.shouldShowPopup() && this.trialAvailable && !this.createdTrial;
         this.createTrialButton.visible = bl;
         this.createTrialButton.active = bl;
         this.buyARealmButton.visible = this.shouldShowPopup();
         this.closeButton.visible = this.shouldShowPopup();
         this.newsButton.active = true;
         this.newsButton.visible = this.newsLink != null;
         this.pendingInvitesButton.active = true;
         this.pendingInvitesButton.visible = true;
         this.showPopupButton.active = !this.shouldShowPopup();
         this.playButton.visible = !this.shouldShowPopup();
         this.renewButton.visible = !this.shouldShowPopup();
         this.leaveButton.visible = !this.shouldShowPopup();
         this.configureButton.visible = !this.shouldShowPopup();
         this.backButton.visible = !this.shouldShowPopup();
         this.playButton.active = this.shouldPlayButtonBeActive(server);
         this.renewButton.active = this.shouldRenewButtonBeActive(server);
         this.leaveButton.active = this.shouldLeaveButtonBeActive(server);
         this.configureButton.active = this.shouldConfigureButtonBeActive(server);
      } else {
         hide(new ClickableWidget[]{this.playButton, this.renewButton, this.configureButton, this.createTrialButton, this.buyARealmButton, this.closeButton, this.newsButton, this.pendingInvitesButton, this.showPopupButton, this.leaveButton});
      }
   }

   private boolean shouldShowPopupButton() {
      return (!this.shouldShowPopup() || this.popupOpenedByUser) && hasParentalConsent() && this.hasFetchedServers;
   }

   boolean shouldPlayButtonBeActive(@Nullable RealmsServer server) {
      return server != null && !server.expired && server.state == RealmsServer.State.OPEN;
   }

   private boolean shouldRenewButtonBeActive(@Nullable RealmsServer server) {
      return server != null && server.expired && this.isSelfOwnedServer(server);
   }

   private boolean shouldConfigureButtonBeActive(@Nullable RealmsServer server) {
      return server != null && this.isSelfOwnedServer(server);
   }

   private boolean shouldLeaveButtonBeActive(@Nullable RealmsServer server) {
      return server != null && !this.isSelfOwnedServer(server);
   }

   public void tick() {
      super.tick();
      if (this.pendingInvitesButton != null) {
         this.pendingInvitesButton.updatePendingText();
      }

      this.justClosedPopup = false;
      ++this.animTick;
      boolean bl = hasParentalConsent();
      if (this.periodicRunnersManager == null && bl) {
         this.periodicRunnersManager = this.createPeriodicRunnersManager(this.client.getRealmsPeriodicCheckers());
      } else if (this.periodicRunnersManager != null && !bl) {
         this.periodicRunnersManager = null;
      }

      if (this.periodicRunnersManager != null) {
         this.periodicRunnersManager.runAll();
      }

      if (this.shouldShowPopup()) {
         ++this.carouselTick;
      }

      if (this.showPopupButton != null) {
         this.showPopupButton.visible = this.shouldShowPopupButton();
         this.showPopupButton.active = this.showPopupButton.visible;
      }

   }

   private PeriodicRunnerFactory.RunnersManager createPeriodicRunnersManager(RealmsPeriodicCheckers periodicCheckers) {
      PeriodicRunnerFactory.RunnersManager lv = periodicCheckers.runnerFactory.create();
      lv.add(periodicCheckers.serverList, (servers) -> {
         List list2 = this.serverFilterer.filterAndSort(servers);
         boolean bl = false;
         Iterator var4 = list2.iterator();

         while(var4.hasNext()) {
            RealmsServer lv = (RealmsServer)var4.next();
            if (this.isOwnedNotExpired(lv)) {
               bl = true;
            }
         }

         this.realmsServers = list2;
         this.hasFetchedServers = true;
         this.refresh();
         if (!regionsPinged && bl) {
            regionsPinged = true;
            this.pingRegions();
         }

      });
      request(RealmsClient::listNotifications, (notifications) -> {
         this.notifications.clear();
         this.notifications.addAll(notifications);
         this.refresh();
      });
      lv.add(periodicCheckers.pendingInvitesCount, (pendingInvitesCount) -> {
         this.pendingInvitesCount = pendingInvitesCount;
         if (this.pendingInvitesCount > 0 && this.rateLimiter.tryAcquire(1)) {
            this.client.getNarratorManager().narrate((Text)Text.translatable("mco.configure.world.invite.narration", this.pendingInvitesCount));
         }

      });
      lv.add(periodicCheckers.trialAvailability, (trialAvailable) -> {
         if (!this.createdTrial) {
            if (trialAvailable != this.trialAvailable && this.shouldShowPopup()) {
               this.trialAvailable = trialAvailable;
               this.showingPopup = false;
            } else {
               this.trialAvailable = trialAvailable;
            }

         }
      });
      lv.add(periodicCheckers.liveStats, (liveStats) -> {
         Iterator var2 = liveStats.servers.iterator();

         while(true) {
            while(var2.hasNext()) {
               RealmsServerPlayerList lv = (RealmsServerPlayerList)var2.next();
               Iterator var4 = this.realmsServers.iterator();

               while(var4.hasNext()) {
                  RealmsServer lv2 = (RealmsServer)var4.next();
                  if (lv2.id == lv.serverId) {
                     lv2.updateServerPing(lv);
                     break;
                  }
               }
            }

            return;
         }
      });
      lv.add(periodicCheckers.news, (news) -> {
         periodicCheckers.newsUpdater.updateNews(news);
         this.hasUnreadNews = periodicCheckers.newsUpdater.hasUnreadNews();
         this.newsLink = periodicCheckers.newsUpdater.getNewsLink();
         this.updateButtonStates((RealmsServer)null);
      });
      return lv;
   }

   private static void request(Request request, Consumer resultConsumer) {
      MinecraftClient lv = MinecraftClient.getInstance();
      CompletableFuture.supplyAsync(() -> {
         try {
            return request.request(RealmsClient.createRealmsClient(lv));
         } catch (RealmsServiceException var3) {
            throw new RuntimeException(var3);
         }
      }).thenAcceptAsync(resultConsumer, lv).exceptionally((throwable) -> {
         LOGGER.error("Failed to execute call to Realms Service", throwable);
         return null;
      });
   }

   private void refresh() {
      boolean bl = !this.hasFetchedServers;
      this.realmSelectionList.clear();
      List list = new ArrayList();
      Iterator var3 = this.notifications.iterator();

      while(var3.hasNext()) {
         RealmsNotification lv = (RealmsNotification)var3.next();
         this.addNotificationEntry(this.realmSelectionList, lv);
         if (!lv.isSeen() && !this.seenNotifications.contains(lv.getUuid())) {
            list.add(lv.getUuid());
         }
      }

      if (!list.isEmpty()) {
         request((client) -> {
            client.markNotificationsAsSeen(list);
            return null;
         }, (void_) -> {
            this.seenNotifications.addAll(list);
         });
      }

      if (this.shouldShowMessageInList()) {
         this.realmSelectionList.addEntry(new RealmSelectionListTrialEntry());
      }

      Entry lv2 = null;
      RealmsServer lv3 = this.findServer();
      Iterator var5 = this.realmsServers.iterator();

      while(var5.hasNext()) {
         RealmsServer lv4 = (RealmsServer)var5.next();
         RealmSelectionListEntry lv5 = new RealmSelectionListEntry(lv4);
         this.realmSelectionList.addEntry(lv5);
         if (lv3 != null && lv3.id == lv4.id) {
            lv2 = lv5;
         }
      }

      if (bl) {
         this.updateButtonStates((RealmsServer)null);
      } else {
         this.realmSelectionList.setSelected((Entry)lv2);
      }

   }

   private void addNotificationEntry(RealmSelectionList selectionList, RealmsNotification notification) {
      if (notification instanceof RealmsNotification.VisitUrl lv) {
         selectionList.addEntry(new VisitUrlNotification(lv.getDefaultMessage(), lv));
         selectionList.addEntry(new VisitButtonEntry(lv.createButton(this)));
      }

   }

   void resetPeriodicRunnersManager() {
      if (this.periodicRunnersManager != null) {
         this.periodicRunnersManager.resetAll();
      }

   }

   private void pingRegions() {
      (new Thread(() -> {
         List list = Ping.pingAllRegions();
         RealmsClient lv = RealmsClient.create();
         PingResult lv2 = new PingResult();
         lv2.pingResults = list;
         lv2.worldIds = this.getOwnedNonExpiredWorldIds();

         try {
            lv.sendPingResults(lv2);
         } catch (Throwable var5) {
            LOGGER.warn("Could not send ping result to Realms: ", var5);
         }

      })).start();
   }

   private List getOwnedNonExpiredWorldIds() {
      List list = Lists.newArrayList();
      Iterator var2 = this.realmsServers.iterator();

      while(var2.hasNext()) {
         RealmsServer lv = (RealmsServer)var2.next();
         if (this.isOwnedNotExpired(lv)) {
            list.add(lv.id);
         }
      }

      return list;
   }

   public void setCreatedTrial(boolean createdTrial) {
      this.createdTrial = createdTrial;
   }

   private void onRenew(@Nullable RealmsServer realmsServer) {
      if (realmsServer != null) {
         String string = Urls.getExtendJavaRealmsUrl(realmsServer.remoteSubscriptionId, this.client.getSession().getUuid(), realmsServer.expiredTrial);
         this.client.keyboard.setClipboard(string);
         Util.getOperatingSystem().open(string);
      }

   }

   private void checkClientCompatibility() {
      if (!checkedClientCompatibility) {
         checkedClientCompatibility = true;
         (new Thread("MCO Compatability Checker #1") {
            public void run() {
               RealmsClient lv = RealmsClient.create();

               try {
                  RealmsClient.CompatibleVersionResponse lv2 = lv.clientCompatible();
                  if (lv2 != RealmsClient.CompatibleVersionResponse.COMPATIBLE) {
                     RealmsMainScreen.realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.parent);
                     RealmsMainScreen.this.client.execute(() -> {
                        RealmsMainScreen.this.client.setScreen(RealmsMainScreen.realmsGenericErrorScreen);
                     });
                     return;
                  }

                  RealmsMainScreen.this.checkParentalConsent();
               } catch (RealmsServiceException var3) {
                  RealmsMainScreen.checkedClientCompatibility = false;
                  RealmsMainScreen.LOGGER.error("Couldn't connect to realms", var3);
                  if (var3.httpResultCode == 401) {
                     RealmsMainScreen.realmsGenericErrorScreen = new RealmsGenericErrorScreen(Text.translatable("mco.error.invalid.session.title"), Text.translatable("mco.error.invalid.session.message"), RealmsMainScreen.this.parent);
                     RealmsMainScreen.this.client.execute(() -> {
                        RealmsMainScreen.this.client.setScreen(RealmsMainScreen.realmsGenericErrorScreen);
                     });
                  } else {
                     RealmsMainScreen.this.client.execute(() -> {
                        RealmsMainScreen.this.client.setScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this.parent));
                     });
                  }
               }

            }
         }).start();
      }

   }

   void checkParentalConsent() {
      (new Thread("MCO Compatability Checker #1") {
         public void run() {
            RealmsClient lv = RealmsClient.create();

            try {
               Boolean boolean_ = lv.mcoEnabled();
               if (boolean_) {
                  RealmsMainScreen.LOGGER.info("Realms is available for this user");
                  RealmsMainScreen.hasParentalConsent = true;
               } else {
                  RealmsMainScreen.LOGGER.info("Realms is not available for this user");
                  RealmsMainScreen.hasParentalConsent = false;
                  RealmsMainScreen.this.client.execute(() -> {
                     RealmsMainScreen.this.client.setScreen(new RealmsParentalConsentScreen(RealmsMainScreen.this.parent));
                  });
               }

               RealmsMainScreen.checkedParentalConsent = true;
            } catch (RealmsServiceException var3) {
               RealmsMainScreen.LOGGER.error("Couldn't connect to realms", var3);
               RealmsMainScreen.this.client.execute(() -> {
                  RealmsMainScreen.this.client.setScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this.parent));
               });
            }

         }
      }).start();
   }

   private void switchToStage() {
      if (RealmsClient.currentEnvironment != RealmsClient.Environment.STAGE) {
         (new Thread("MCO Stage Availability Checker #1") {
            public void run() {
               RealmsClient lv = RealmsClient.create();

               try {
                  Boolean boolean_ = lv.stageAvailable();
                  if (boolean_) {
                     RealmsClient.switchToStage();
                     RealmsMainScreen.LOGGER.info("Switched to stage");
                     RealmsMainScreen.this.resetPeriodicRunnersManager();
                  }
               } catch (RealmsServiceException var3) {
                  RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: {}", var3.toString());
               }

            }
         }).start();
      }

   }

   private void switchToLocal() {
      if (RealmsClient.currentEnvironment != RealmsClient.Environment.LOCAL) {
         (new Thread("MCO Local Availability Checker #1") {
            public void run() {
               RealmsClient lv = RealmsClient.create();

               try {
                  Boolean boolean_ = lv.stageAvailable();
                  if (boolean_) {
                     RealmsClient.switchToLocal();
                     RealmsMainScreen.LOGGER.info("Switched to local");
                     RealmsMainScreen.this.resetPeriodicRunnersManager();
                  }
               } catch (RealmsServiceException var3) {
                  RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: {}", var3.toString());
               }

            }
         }).start();
      }

   }

   private void switchToProd() {
      RealmsClient.switchToProd();
      this.resetPeriodicRunnersManager();
   }

   private void configureClicked(@Nullable RealmsServer serverData) {
      if (serverData != null && (this.client.getSession().getUuid().equals(serverData.ownerUUID) || overrideConfigure)) {
         this.saveListScrollPosition();
         this.client.setScreen(new RealmsConfigureWorldScreen(this, serverData.id));
      }

   }

   private void leaveClicked(@Nullable RealmsServer selectedServer) {
      if (selectedServer != null && !this.client.getSession().getUuid().equals(selectedServer.ownerUUID)) {
         this.saveListScrollPosition();
         Text lv = Text.translatable("mco.configure.world.leave.question.line1");
         Text lv2 = Text.translatable("mco.configure.world.leave.question.line2");
         this.client.setScreen(new RealmsLongConfirmationScreen((confirmed) -> {
            this.leaveServer(confirmed, selectedServer);
         }, RealmsLongConfirmationScreen.Type.INFO, lv, lv2, true));
      }

   }

   private void saveListScrollPosition() {
      lastScrollYPosition = (int)this.realmSelectionList.getScrollAmount();
   }

   @Nullable
   private RealmsServer findServer() {
      if (this.realmSelectionList == null) {
         return null;
      } else {
         Entry lv = (Entry)this.realmSelectionList.getSelectedOrNull();
         return lv != null ? lv.getRealmsServer() : null;
      }
   }

   private void leaveServer(boolean confirmed, final RealmsServer realmsServer) {
      if (confirmed) {
         (new Thread("Realms-leave-server") {
            public void run() {
               try {
                  RealmsClient lv = RealmsClient.create();
                  lv.uninviteMyselfFrom(realmsServer.id);
                  RealmsMainScreen.this.client.execute(() -> {
                     RealmsMainScreen.this.removeServer(realmsServer);
                  });
               } catch (RealmsServiceException var2) {
                  RealmsMainScreen.LOGGER.error("Couldn't configure world");
                  RealmsMainScreen.this.client.execute(() -> {
                     RealmsMainScreen.this.client.setScreen(new RealmsGenericErrorScreen(var2, RealmsMainScreen.this));
                  });
               }

            }
         }).start();
      }

      this.client.setScreen(this);
   }

   void removeServer(RealmsServer serverData) {
      this.realmsServers = this.serverFilterer.remove(serverData);
      this.realmSelectionList.children().removeIf((child) -> {
         RealmsServer lv = child.getRealmsServer();
         return lv != null && lv.id == serverData.id;
      });
      this.realmSelectionList.setSelected((Entry)null);
      this.updateButtonStates((RealmsServer)null);
      this.playButton.active = false;
   }

   void dismissNotification(UUID notification) {
      request((client) -> {
         client.dismissNotifications(List.of(notification));
         return null;
      }, (void_) -> {
         this.notifications.removeIf((notificationId) -> {
            return notificationId.isDismissable() && notification.equals(notificationId.getUuid());
         });
         this.refresh();
      });
   }

   public void removeSelection() {
      if (this.realmSelectionList != null) {
         this.realmSelectionList.setSelected((Entry)null);
      }

   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
         this.keyCombos.forEach(KeyCombo::reset);
         this.onClosePopup();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   void onClosePopup() {
      if (this.shouldShowPopup() && this.popupOpenedByUser) {
         this.popupOpenedByUser = false;
      } else {
         this.client.setScreen(this.parent);
      }

   }

   public boolean charTyped(char chr, int modifiers) {
      this.keyCombos.forEach((keyCombo) -> {
         keyCombo.keyPressed(chr);
      });
      return true;
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      this.realmSelectionList.render(matrices, mouseX, mouseY, delta);
      RenderSystem.setShaderTexture(0, REALMS);
      drawTexture(matrices, this.width / 2 - 64, 5, 0.0F, 0.0F, 128, 34, 128, 64);
      if (RealmsClient.currentEnvironment == RealmsClient.Environment.STAGE) {
         this.renderStage(matrices);
      }

      if (RealmsClient.currentEnvironment == RealmsClient.Environment.LOCAL) {
         this.renderLocal(matrices);
      }

      if (this.shouldShowPopup()) {
         matrices.push();
         matrices.translate(0.0F, 0.0F, 100.0F);
         this.drawPopup(matrices);
         matrices.pop();
      } else {
         if (this.showingPopup) {
            this.updateButtonStates((RealmsServer)null);
            if (!this.hasSelectionList) {
               this.addSelectableChild(this.realmSelectionList);
               this.hasSelectionList = true;
            }

            this.playButton.active = this.shouldPlayButtonBeActive(this.findServer());
         }

         this.showingPopup = false;
      }

      super.render(matrices, mouseX, mouseY, delta);
      if (this.trialAvailable && !this.createdTrial && this.shouldShowPopup()) {
         RenderSystem.setShaderTexture(0, TRIAL_ICON);
         int k = true;
         int l = true;
         int m = 0;
         if ((Util.getMeasuringTimeMs() / 800L & 1L) == 1L) {
            m = 8;
         }

         DrawableHelper.drawTexture(matrices, this.createTrialButton.getX() + this.createTrialButton.getWidth() - 8 - 4, this.createTrialButton.getY() + this.createTrialButton.getHeight() / 2 - 4, 0.0F, (float)m, 8, 8, 8, 16);
      }

   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isOutsidePopup(mouseX, mouseY) && this.popupOpenedByUser) {
         this.popupOpenedByUser = false;
         this.justClosedPopup = true;
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   private boolean isOutsidePopup(double xm, double ym) {
      int i = this.popupX0();
      int j = this.popupY0();
      return xm < (double)(i - 5) || xm > (double)(i + 315) || ym < (double)(j - 5) || ym > (double)(j + 171);
   }

   private void drawPopup(MatrixStack matrices) {
      int i = this.popupX0();
      int j = this.popupY0();
      if (!this.showingPopup) {
         this.carouselIndex = 0;
         this.carouselTick = 0;
         this.hasSwitchedCarouselImage = true;
         this.updateButtonStates((RealmsServer)null);
         if (this.hasSelectionList) {
            this.remove(this.realmSelectionList);
            this.hasSelectionList = false;
         }

         this.client.getNarratorManager().narrate(POPUP_TEXT);
      }

      if (this.hasFetchedServers) {
         this.showingPopup = true;
      }

      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.7F);
      RenderSystem.enableBlend();
      RenderSystem.setShaderTexture(0, DARKEN);
      int k = false;
      int l = true;
      DrawableHelper.drawTexture(matrices, 0, 32, 0.0F, 0.0F, this.width, this.height - 40 - 32, 310, 166);
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShaderTexture(0, POPUP);
      DrawableHelper.drawTexture(matrices, i, j, 0.0F, 0.0F, 310, 166, 310, 166);
      if (!IMAGES.isEmpty()) {
         RenderSystem.setShaderTexture(0, (Identifier)IMAGES.get(this.carouselIndex));
         DrawableHelper.drawTexture(matrices, i + 7, j + 7, 0.0F, 0.0F, 195, 152, 195, 152);
         if (this.carouselTick % 95 < 5) {
            if (!this.hasSwitchedCarouselImage) {
               this.carouselIndex = (this.carouselIndex + 1) % IMAGES.size();
               this.hasSwitchedCarouselImage = true;
            }
         } else {
            this.hasSwitchedCarouselImage = false;
         }
      }

      this.popupText.draw(matrices, this.width / 2 + 52, j + 7, 10, 16777215);
   }

   int popupX0() {
      return (this.width - 310) / 2;
   }

   int popupY0() {
      return this.height / 2 - 80;
   }

   public void play(@Nullable RealmsServer serverData, Screen parent) {
      if (serverData != null) {
         try {
            if (!this.connectLock.tryLock(1L, TimeUnit.SECONDS)) {
               return;
            }

            if (this.connectLock.getHoldCount() > 1) {
               return;
            }
         } catch (InterruptedException var4) {
            return;
         }

         this.dontSetConnectedToRealms = true;
         this.client.setScreen(new RealmsLongRunningMcoTaskScreen(parent, new RealmsGetServerDetailsTask(this, parent, serverData, this.connectLock)));
      }

   }

   boolean isSelfOwnedServer(RealmsServer serverData) {
      return serverData.ownerUUID != null && serverData.ownerUUID.equals(this.client.getSession().getUuid());
   }

   private boolean isOwnedNotExpired(RealmsServer serverData) {
      return this.isSelfOwnedServer(serverData) && !serverData.expired;
   }

   void drawExpired(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
      RenderSystem.setShaderTexture(0, EXPIRED_ICON);
      DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 10, 28, 10, 28);
      if (mouseX >= x && mouseX <= x + 9 && mouseY >= y && mouseY <= y + 27 && mouseY < this.height - 40 && mouseY > 32 && !this.shouldShowPopup()) {
         this.setTooltip(EXPIRED_TEXT);
      }

   }

   void drawExpiring(MatrixStack matrices, int x, int y, int mouseX, int mouseY, int remainingDays) {
      RenderSystem.setShaderTexture(0, EXPIRES_SOON_ICON);
      if (this.animTick % 20 < 10) {
         DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 10, 28, 20, 28);
      } else {
         DrawableHelper.drawTexture(matrices, x, y, 10.0F, 0.0F, 10, 28, 20, 28);
      }

      if (mouseX >= x && mouseX <= x + 9 && mouseY >= y && mouseY <= y + 27 && mouseY < this.height - 40 && mouseY > 32 && !this.shouldShowPopup()) {
         if (remainingDays <= 0) {
            this.setTooltip(EXPIRES_SOON_TEXT);
         } else if (remainingDays == 1) {
            this.setTooltip(EXPIRES_IN_A_DAY_TEXT);
         } else {
            this.setTooltip(Text.translatable("mco.selectServer.expires.days", remainingDays));
         }
      }

   }

   void drawOpen(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
      RenderSystem.setShaderTexture(0, ON_ICON);
      DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 10, 28, 10, 28);
      if (mouseX >= x && mouseX <= x + 9 && mouseY >= y && mouseY <= y + 27 && mouseY < this.height - 40 && mouseY > 32 && !this.shouldShowPopup()) {
         this.setTooltip(OPEN_TEXT);
      }

   }

   void drawClose(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
      RenderSystem.setShaderTexture(0, OFF_ICON);
      DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 10, 28, 10, 28);
      if (mouseX >= x && mouseX <= x + 9 && mouseY >= y && mouseY <= y + 27 && mouseY < this.height - 40 && mouseY > 32 && !this.shouldShowPopup()) {
         this.setTooltip(CLOSED_TEXT);
      }

   }

   void renderNews(MatrixStack matrices, int mouseX, int mouseY, boolean hasUnread, int x, int y, boolean hovered, boolean active) {
      boolean bl4 = false;
      if (mouseX >= x && mouseX <= x + 20 && mouseY >= y && mouseY <= y + 20) {
         bl4 = true;
      }

      RenderSystem.setShaderTexture(0, NEWS_ICON);
      if (!active) {
         RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
      }

      boolean bl5 = active && hovered;
      float f = bl5 ? 20.0F : 0.0F;
      DrawableHelper.drawTexture(matrices, x, y, f, 0.0F, 20, 20, 40, 20);
      if (bl4 && active) {
         this.setTooltip(NEWS_TEXT);
      }

      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      if (hasUnread && active) {
         int m = bl4 ? 0 : (int)(Math.max(0.0F, Math.max(MathHelper.sin((float)(10 + this.animTick) * 0.57F), MathHelper.cos((float)this.animTick * 0.35F))) * -6.0F);
         RenderSystem.setShaderTexture(0, INVITATION_ICON);
         DrawableHelper.drawTexture(matrices, x + 10, y + 2 + m, 40.0F, 0.0F, 8, 8, 48, 16);
      }

   }

   private void renderLocal(MatrixStack matrices) {
      String string = "LOCAL!";
      matrices.push();
      matrices.translate((float)(this.width / 2 - 25), 20.0F, 0.0F);
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-20.0F));
      matrices.scale(1.5F, 1.5F, 1.5F);
      this.textRenderer.draw(matrices, "LOCAL!", 0.0F, 0.0F, 8388479);
      matrices.pop();
   }

   private void renderStage(MatrixStack matrices) {
      String string = "STAGE!";
      matrices.push();
      matrices.translate((float)(this.width / 2 - 25), 20.0F, 0.0F);
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-20.0F));
      matrices.scale(1.5F, 1.5F, 1.5F);
      this.textRenderer.draw(matrices, (String)"STAGE!", 0.0F, 0.0F, -256);
      matrices.pop();
   }

   public RealmsMainScreen newScreen() {
      RealmsMainScreen lv = new RealmsMainScreen(this.parent);
      lv.init(this.client, this.width, this.height);
      return lv;
   }

   public static void loadImages(ResourceManager manager) {
      Collection collection = manager.findResources("textures/gui/images", (filename) -> {
         return filename.getPath().endsWith(".png");
      }).keySet();
      IMAGES = collection.stream().filter((id) -> {
         return id.getNamespace().equals("realms");
      }).toList();
   }

   static {
      MINIGAME_TEXT = Text.translatable("mco.selectServer.minigame").append(ScreenTexts.SPACE);
      POPUP_TEXT = Text.translatable("mco.selectServer.popup");
      PLAY_TEXT = Text.translatable("mco.selectServer.play");
      LEAVE_TEXT = Text.translatable("mco.selectServer.leave");
      CONFIGURE_TEXT = Text.translatable("mco.selectServer.configure");
      EXPIRED_TEXT = Text.translatable("mco.selectServer.expired");
      EXPIRES_SOON_TEXT = Text.translatable("mco.selectServer.expires.soon");
      EXPIRES_IN_A_DAY_TEXT = Text.translatable("mco.selectServer.expires.day");
      OPEN_TEXT = Text.translatable("mco.selectServer.open");
      CLOSED_TEXT = Text.translatable("mco.selectServer.closed");
      NEWS_TEXT = Text.translatable("mco.news");
      UNINITIALIZED_BUTTON_NARRATION = Text.translatable("gui.narrate.button", UNINITIALIZED_TEXT);
      TRIAL_NARRATION = ScreenTexts.joinLines((Collection)TRIAL_MESSAGE_LINES);
      IMAGES = ImmutableList.of();
      lastScrollYPosition = -1;
   }

   @Environment(EnvType.CLIENT)
   class RealmSelectionList extends RealmsObjectSelectionList {
      public RealmSelectionList() {
         super(RealmsMainScreen.this.width, RealmsMainScreen.this.height, 44, RealmsMainScreen.this.height - 64, 36);
      }

      public void setSelected(@Nullable Entry arg) {
         super.setSelected(arg);
         if (arg != null) {
            RealmsMainScreen.this.updateButtonStates(arg.getRealmsServer());
         } else {
            RealmsMainScreen.this.updateButtonStates((RealmsServer)null);
         }

      }

      public int getMaxPosition() {
         return this.getEntryCount() * 36;
      }

      public int getRowWidth() {
         return 300;
      }
   }

   @Environment(EnvType.CLIENT)
   class PendingInvitesButton extends TexturedButtonWidget {
      private static final Text INVITES_TITLE = Text.translatable("mco.invites.title");
      private static final Tooltip NO_PENDING_TEXT = Tooltip.of(Text.translatable("mco.invites.nopending"));
      private static final Tooltip PENDING_TEXT = Tooltip.of(Text.translatable("mco.invites.pending"));
      private static final int field_44519 = 18;
      private static final int field_44520 = 15;
      private static final int field_44521 = 10;
      private static final int field_44522 = 8;
      private static final int field_44523 = 8;
      private static final int field_44524 = 11;

      public PendingInvitesButton() {
         super(RealmsMainScreen.this.width / 2 + 64 + 10, 15, 18, 15, 0, 0, 15, RealmsMainScreen.INVITE_ICON, 18, 30, (button) -> {
            RealmsMainScreen.this.client.setScreen(new RealmsPendingInvitesScreen(RealmsMainScreen.this.parent, INVITES_TITLE));
         }, INVITES_TITLE);
         this.setTooltip(NO_PENDING_TEXT);
      }

      public void updatePendingText() {
         this.setTooltip(RealmsMainScreen.this.pendingInvitesCount == 0 ? NO_PENDING_TEXT : PENDING_TEXT);
      }

      public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
         super.renderButton(matrices, mouseX, mouseY, delta);
         this.render(matrices);
      }

      private void render(MatrixStack matrices) {
         boolean bl = this.active && RealmsMainScreen.this.pendingInvitesCount != 0;
         if (bl) {
            RenderSystem.setShaderTexture(0, RealmsMainScreen.INVITATION_ICON);
            int i = (Math.min(RealmsMainScreen.this.pendingInvitesCount, 6) - 1) * 8;
            int j = (int)(Math.max(0.0F, Math.max(MathHelper.sin((float)(10 + RealmsMainScreen.this.animTick) * 0.57F), MathHelper.cos((float)RealmsMainScreen.this.animTick * 0.35F))) * -6.0F);
            float f = this.isSelected() ? 8.0F : 0.0F;
            DrawableHelper.drawTexture(matrices, this.getX() + 11, this.getY() + j, (float)i, f, 8, 8, 48, 16);
         }

      }
   }

   @Environment(EnvType.CLIENT)
   class NewsButton extends ButtonWidget {
      private static final int field_44515 = 20;

      public NewsButton() {
         super(RealmsMainScreen.this.width - 115, 12, 20, 20, Text.translatable("mco.news"), (button) -> {
            if (RealmsMainScreen.this.newsLink != null) {
               ConfirmLinkScreen.open(RealmsMainScreen.this.newsLink, RealmsMainScreen.this, true);
               if (RealmsMainScreen.this.hasUnreadNews) {
                  RealmsPersistence.RealmsPersistenceData lv = RealmsPersistence.readFile();
                  lv.hasUnreadNews = false;
                  RealmsMainScreen.this.hasUnreadNews = false;
                  RealmsPersistence.writeFile(lv);
               }

            }
         }, DEFAULT_NARRATION_SUPPLIER);
      }

      public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
         RealmsMainScreen.this.renderNews(matrices, mouseX, mouseY, RealmsMainScreen.this.hasUnreadNews, this.getX(), this.getY(), this.isSelected(), this.active);
      }
   }

   @Environment(EnvType.CLIENT)
   private class CloseButton extends CrossButton {
      public CloseButton() {
         super(RealmsMainScreen.this.popupX0() + 4, RealmsMainScreen.this.popupY0() + 4, (button) -> {
            RealmsMainScreen.this.onClosePopup();
         }, Text.translatable("mco.selectServer.close"));
      }
   }

   @Environment(EnvType.CLIENT)
   interface Request {
      Object request(RealmsClient client) throws RealmsServiceException;
   }

   @Environment(EnvType.CLIENT)
   private class RealmSelectionListTrialEntry extends Entry {
      RealmSelectionListTrialEntry() {
         super();
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         this.renderTrialItem(matrices, index, x, y, mouseX, mouseY);
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         RealmsMainScreen.this.popupOpenedByUser = true;
         return true;
      }

      private void renderTrialItem(MatrixStack matrices, int index, int x, int y, int mouseX, int mouseY) {
         int n = y + 8;
         int o = 0;
         boolean bl = false;
         if (x <= mouseX && mouseX <= (int)RealmsMainScreen.this.realmSelectionList.getScrollAmount() && y <= mouseY && mouseY <= y + 32) {
            bl = true;
         }

         int p = 8388479;
         if (bl && !RealmsMainScreen.this.shouldShowPopup()) {
            p = 6077788;
         }

         for(Iterator var11 = RealmsMainScreen.TRIAL_MESSAGE_LINES.iterator(); var11.hasNext(); o += 10) {
            Text lv = (Text)var11.next();
            DrawableHelper.drawCenteredTextWithShadow(matrices, RealmsMainScreen.this.textRenderer, lv, RealmsMainScreen.this.width / 2, n + o, p);
         }

      }

      public Text getNarration() {
         return RealmsMainScreen.TRIAL_NARRATION;
      }
   }

   @Environment(EnvType.CLIENT)
   private class RealmSelectionListEntry extends Entry {
      private static final int field_32054 = 36;
      private final RealmsServer server;

      public RealmSelectionListEntry(RealmsServer server) {
         super();
         this.server = server;
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         this.render(this.server, matrices, x, y, mouseX, mouseY);
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         if (this.server.state == RealmsServer.State.UNINITIALIZED) {
            RealmsMainScreen.this.client.setScreen(new RealmsCreateRealmScreen(this.server, RealmsMainScreen.this));
         } else if (RealmsMainScreen.this.shouldPlayButtonBeActive(this.server)) {
            if (Util.getMeasuringTimeMs() - RealmsMainScreen.this.lastPlayButtonClickTime < 250L && this.isFocused()) {
               RealmsMainScreen.this.client.getSoundManager().play(PositionedSoundInstance.master((RegistryEntry)SoundEvents.UI_BUTTON_CLICK, 1.0F));
               RealmsMainScreen.this.play(this.server, RealmsMainScreen.this);
            }

            RealmsMainScreen.this.lastPlayButtonClickTime = Util.getMeasuringTimeMs();
         }

         return true;
      }

      public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
         if (KeyCodes.isToggle(keyCode) && RealmsMainScreen.this.shouldPlayButtonBeActive(this.server)) {
            RealmsMainScreen.this.client.getSoundManager().play(PositionedSoundInstance.master((RegistryEntry)SoundEvents.UI_BUTTON_CLICK, 1.0F));
            RealmsMainScreen.this.play(this.server, RealmsMainScreen.this);
            return true;
         } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
         }
      }

      private void render(RealmsServer server, MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
         this.renderRealmsServerItem(server, matrices, x + 36, y, mouseX, mouseY);
      }

      private void renderRealmsServerItem(RealmsServer server, MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
         if (server.state == RealmsServer.State.UNINITIALIZED) {
            RenderSystem.setShaderTexture(0, RealmsMainScreen.WORLD_ICON);
            DrawableHelper.drawTexture(matrices, x + 10, y + 6, 0.0F, 0.0F, 40, 20, 40, 20);
            float f = 0.5F + (1.0F + MathHelper.sin((float)RealmsMainScreen.this.animTick * 0.25F)) * 0.25F;
            int m = -16777216 | (int)(127.0F * f) << 16 | (int)(255.0F * f) << 8 | (int)(127.0F * f);
            DrawableHelper.drawCenteredTextWithShadow(matrices, RealmsMainScreen.this.textRenderer, RealmsMainScreen.UNINITIALIZED_TEXT, x + 10 + 40 + 75, y + 12, m);
         } else {
            int n = true;
            int mx = true;
            this.drawServerState(server, matrices, x, y, mouseX, mouseY, 225, 2);
            if (!"0".equals(server.serverPing.nrOfPlayers)) {
               String string = Formatting.GRAY + server.serverPing.nrOfPlayers;
               RealmsMainScreen.this.textRenderer.draw(matrices, string, (float)(x + 207 - RealmsMainScreen.this.textRenderer.getWidth(string)), (float)(y + 3), 8421504);
               if (mouseX >= x + 207 - RealmsMainScreen.this.textRenderer.getWidth(string) && mouseX <= x + 207 && mouseY >= y + 1 && mouseY <= y + 10 && mouseY < RealmsMainScreen.this.height - 40 && mouseY > 32 && !RealmsMainScreen.this.shouldShowPopup()) {
                  RealmsMainScreen.this.setTooltip(Text.literal(server.serverPing.playerList));
               }
            }

            int o;
            if (RealmsMainScreen.this.isSelfOwnedServer(server) && server.expired) {
               Text lv = server.expiredTrial ? RealmsMainScreen.EXPIRED_TRIAL_TEXT : RealmsMainScreen.EXPIRED_LIST_TEXT;
               o = y + 11 + 5;
               RealmsMainScreen.this.textRenderer.draw(matrices, lv, (float)(x + 2), (float)(o + 1), 15553363);
            } else {
               if (server.worldType == RealmsServer.WorldType.MINIGAME) {
                  int p = 13413468;
                  o = RealmsMainScreen.this.textRenderer.getWidth((StringVisitable)RealmsMainScreen.MINIGAME_TEXT);
                  RealmsMainScreen.this.textRenderer.draw(matrices, RealmsMainScreen.MINIGAME_TEXT, (float)(x + 2), (float)(y + 12), 13413468);
                  RealmsMainScreen.this.textRenderer.draw(matrices, server.getMinigameName(), (float)(x + 2 + o), (float)(y + 12), 7105644);
               } else {
                  RealmsMainScreen.this.textRenderer.draw(matrices, server.getDescription(), (float)(x + 2), (float)(y + 12), 7105644);
               }

               if (!RealmsMainScreen.this.isSelfOwnedServer(server)) {
                  RealmsMainScreen.this.textRenderer.draw(matrices, server.owner, (float)(x + 2), (float)(y + 12 + 11), 5000268);
               }
            }

            RealmsMainScreen.this.textRenderer.draw(matrices, server.getName(), (float)(x + 2), (float)(y + 1), 16777215);
            RealmsUtil.drawPlayerHead(matrices, x - 36, y, 32, server.ownerUUID);
         }
      }

      private void drawServerState(RealmsServer server, MatrixStack matrices, int x, int y, int mouseX, int mouseY, int xOffset, int yOffset) {
         int o = x + xOffset + 22;
         if (server.expired) {
            RealmsMainScreen.this.drawExpired(matrices, o, y + yOffset, mouseX, mouseY);
         } else if (server.state == RealmsServer.State.CLOSED) {
            RealmsMainScreen.this.drawClose(matrices, o, y + yOffset, mouseX, mouseY);
         } else if (RealmsMainScreen.this.isSelfOwnedServer(server) && server.daysLeft < 7) {
            RealmsMainScreen.this.drawExpiring(matrices, o, y + yOffset, mouseX, mouseY, server.daysLeft);
         } else if (server.state == RealmsServer.State.OPEN) {
            RealmsMainScreen.this.drawOpen(matrices, o, y + yOffset, mouseX, mouseY);
         }

      }

      public Text getNarration() {
         return (Text)(this.server.state == RealmsServer.State.UNINITIALIZED ? RealmsMainScreen.UNINITIALIZED_BUTTON_NARRATION : Text.translatable("narrator.select", this.server.name));
      }

      @Nullable
      public RealmsServer getRealmsServer() {
         return this.server;
      }
   }

   @Environment(EnvType.CLIENT)
   abstract class Entry extends AlwaysSelectedEntryListWidget.Entry {
      @Nullable
      public RealmsServer getRealmsServer() {
         return null;
      }
   }

   @Environment(EnvType.CLIENT)
   class VisitUrlNotification extends Entry {
      private static final int field_43002 = 40;
      private static final int field_43003 = 36;
      private static final int field_43004 = -12303292;
      private final Text message;
      private final List gridChildren = new ArrayList();
      @Nullable
      private final CrossButton dismissButton;
      private final MultilineTextWidget textWidget;
      private final GridWidget grid;
      private final SimplePositioningWidget textGrid;
      private int width = -1;

      public VisitUrlNotification(Text message, RealmsNotification notification) {
         super();
         this.message = message;
         this.grid = new GridWidget();
         int i = true;
         this.grid.add(new IconWidget(20, 20, RealmsMainScreen.INFO_ICON), 0, 0, this.grid.copyPositioner().margin(7, 7, 0, 0));
         this.grid.add(EmptyWidget.ofWidth(40), 0, 0);
         GridWidget var10001 = this.grid;
         Objects.requireNonNull(RealmsMainScreen.this.textRenderer);
         this.textGrid = (SimplePositioningWidget)var10001.add(new SimplePositioningWidget(0, 9 * 3), 0, 1, this.grid.copyPositioner().marginTop(7));
         this.textWidget = (MultilineTextWidget)this.textGrid.add((new MultilineTextWidget(message, RealmsMainScreen.this.textRenderer)).setCentered(true).setMaxRows(3), this.textGrid.copyPositioner().alignHorizontalCenter().alignTop());
         this.grid.add(EmptyWidget.ofWidth(40), 0, 2);
         if (notification.isDismissable()) {
            this.dismissButton = (CrossButton)this.grid.add(new CrossButton((button) -> {
               RealmsMainScreen.this.dismissNotification(notification.getUuid());
            }, Text.translatable("mco.notification.dismiss")), 0, 2, this.grid.copyPositioner().alignRight().margin(0, 7, 7, 0));
         } else {
            this.dismissButton = null;
         }

         GridWidget var10000 = this.grid;
         List var5 = this.gridChildren;
         Objects.requireNonNull(var5);
         var10000.forEachChild(var5::add);
      }

      public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
         return this.dismissButton != null && this.dismissButton.keyPressed(keyCode, scanCode, modifiers) ? true : super.keyPressed(keyCode, scanCode, modifiers);
      }

      private void setWidth(int width) {
         if (this.width != width) {
            this.updateWidth(width);
            this.width = width;
         }

      }

      private void updateWidth(int width) {
         int j = width - 80;
         this.textGrid.setMinWidth(j);
         this.textWidget.setMaxWidth(j);
         this.grid.refreshPositions();
      }

      public void drawBorder(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         super.drawBorder(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
         DrawableHelper.drawBorder(matrices, x - 2, y - 2, entryWidth, 70, -12303292);
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         this.grid.setPosition(x, y);
         this.setWidth(entryWidth - 4);
         this.gridChildren.forEach((child) -> {
            child.render(matrices, mouseX, mouseY, tickDelta);
         });
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         if (this.dismissButton != null) {
            this.dismissButton.mouseClicked(mouseX, mouseY, button);
         }

         return true;
      }

      public Text getNarration() {
         return this.message;
      }
   }

   @Environment(EnvType.CLIENT)
   class VisitButtonEntry extends Entry {
      private final ButtonWidget button;
      private final int x;

      public VisitButtonEntry(ButtonWidget button) {
         super();
         this.x = RealmsMainScreen.this.width / 2 - 75;
         this.button = button;
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         this.button.mouseClicked(mouseX, mouseY, button);
         return true;
      }

      public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
         return this.button.keyPressed(keyCode, scanCode, modifiers) ? true : super.keyPressed(keyCode, scanCode, modifiers);
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         this.button.setPosition(this.x, y + 4);
         this.button.render(matrices, mouseX, mouseY, tickDelta);
      }

      public Text getNarration() {
         return this.button.getMessage();
      }
   }

   @Environment(EnvType.CLIENT)
   private static class CrossButton extends ButtonWidget {
      protected CrossButton(ButtonWidget.PressAction onPress, Text message) {
         this(0, 0, onPress, message);
      }

      protected CrossButton(int x, int y, ButtonWidget.PressAction onPress, Text message) {
         super(x, y, 14, 14, message, onPress, DEFAULT_NARRATION_SUPPLIER);
         this.setTooltip(Tooltip.of(message));
      }

      public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
         RenderSystem.setShaderTexture(0, RealmsMainScreen.CROSS_ICON);
         float g = this.isSelected() ? 14.0F : 0.0F;
         drawTexture(matrices, this.getX(), this.getY(), 0.0F, g, 14, 14, 14, 28);
      }
   }
}
