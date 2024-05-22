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
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.class_9805;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.PopupScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipState;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.IconWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.NarratedMultilineTextWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.realms.Ping;
import net.minecraft.client.realms.RealmsAvailability;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.RealmsObjectSelectionList;
import net.minecraft.client.realms.RealmsPeriodicCheckers;
import net.minecraft.client.realms.dto.PingResult;
import net.minecraft.client.realms.dto.RealmsNews;
import net.minecraft.client.realms.dto.RealmsNotification;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsServerPlayerList;
import net.minecraft.client.realms.dto.RegionPingResult;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.gui.RealmsLoadingWidget;
import net.minecraft.client.realms.gui.RealmsPopups;
import net.minecraft.client.realms.gui.screen.BuyRealmsScreen;
import net.minecraft.client.realms.gui.screen.RealmsConfigureWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsCreateRealmScreen;
import net.minecraft.client.realms.gui.screen.RealmsGenericErrorScreen;
import net.minecraft.client.realms.gui.screen.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.realms.gui.screen.RealmsPendingInvitesScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.realms.task.RealmsPrepareConnectionTask;
import net.minecraft.client.realms.util.PeriodicRunnerFactory;
import net.minecraft.client.realms.util.RealmsPersistence;
import net.minecraft.client.realms.util.RealmsServerFilterer;
import net.minecraft.client.realms.util.RealmsUtil;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Urls;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsMainScreen
extends RealmsScreen {
    static final Identifier INFO_ICON_TEXTURE = Identifier.method_60656("icon/info");
    static final Identifier NEW_REALM_ICON_TEXTURE = Identifier.method_60656("icon/new_realm");
    static final Identifier EXPIRED_STATUS_TEXTURE = Identifier.method_60656("realm_status/expired");
    static final Identifier EXPIRES_SOON_STATUS_TEXTURE = Identifier.method_60656("realm_status/expires_soon");
    static final Identifier OPEN_STATUS_TEXTURE = Identifier.method_60656("realm_status/open");
    static final Identifier CLOSED_STATUS_TEXTURE = Identifier.method_60656("realm_status/closed");
    private static final Identifier INVITE_ICON_TEXTURE = Identifier.method_60656("icon/invite");
    private static final Identifier NEWS_ICON_TEXTURE = Identifier.method_60656("icon/news");
    static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier REALMS_TITLE_TEXTURE = Identifier.method_60656("textures/gui/title/realms.png");
    private static final Identifier NO_REALMS_TEXTURE = Identifier.method_60656("textures/gui/realms/no_realms.png");
    private static final Text MENU_TEXT = Text.translatable("menu.online");
    private static final Text LOADING_TEXT = Text.translatable("mco.selectServer.loading");
    static final Text UNINITIALIZED_TEXT = Text.translatable("mco.selectServer.uninitialized");
    static final Text EXPIRED_LIST_TEXT = Text.translatable("mco.selectServer.expiredList");
    private static final Text EXPIRED_RENEW_TEXT = Text.translatable("mco.selectServer.expiredRenew");
    static final Text EXPIRED_TRIAL_TEXT = Text.translatable("mco.selectServer.expiredTrial");
    private static final Text PLAY_TEXT = Text.translatable("mco.selectServer.play");
    private static final Text LEAVE_TEXT = Text.translatable("mco.selectServer.leave");
    private static final Text CONFIGURE_TEXT = Text.translatable("mco.selectServer.configure");
    static final Text EXPIRED_TEXT = Text.translatable("mco.selectServer.expired");
    static final Text EXPIRES_SOON_TEXT = Text.translatable("mco.selectServer.expires.soon");
    static final Text EXPIRES_IN_A_DAY_TEXT = Text.translatable("mco.selectServer.expires.day");
    static final Text OPEN_TEXT = Text.translatable("mco.selectServer.open");
    static final Text CLOSED_TEXT = Text.translatable("mco.selectServer.closed");
    static final Text UNINITIALIZED_BUTTON_NARRATION = Text.translatable("gui.narrate.button", UNINITIALIZED_TEXT);
    private static final Text NO_REALMS_TEXT = Text.translatable("mco.selectServer.noRealms");
    private static final Text NO_PENDING_TOOLTIP = Text.translatable("mco.invites.nopending");
    private static final Text PENDING_TOOLTIP = Text.translatable("mco.invites.pending");
    private static final Text INCOMPATIBLE_POPUP_TITLE = Text.translatable("mco.compatibility.incompatible.popup.title");
    private static final Text INCOMPATIBLE_RELEASE_TYPE_MESSAGE = Text.translatable("mco.compatibility.incompatible.releaseType.popup.message");
    private static final int field_42862 = 100;
    private static final int field_45209 = 3;
    private static final int field_45210 = 4;
    private static final int field_45211 = 308;
    private static final int field_44509 = 128;
    private static final int field_44510 = 34;
    private static final int field_44511 = 128;
    private static final int field_44512 = 64;
    private static final int field_44513 = 5;
    private static final int field_44514 = 44;
    private static final int field_45212 = 11;
    private static final int field_46670 = 40;
    private static final int field_46671 = 20;
    private static final int field_46215 = 216;
    private static final int field_46216 = 36;
    private static final boolean gameOnSnapshot;
    private static boolean showingSnapshotRealms;
    private final CompletableFuture<RealmsAvailability.Info> availabilityInfo = RealmsAvailability.check();
    @Nullable
    private PeriodicRunnerFactory.RunnersManager periodicRunnersManager;
    private final Set<UUID> seenNotifications = new HashSet<UUID>();
    private static boolean regionsPinged;
    private final RateLimiter rateLimiter;
    private final Screen parent;
    private ButtonWidget playButton;
    private ButtonWidget backButton;
    private ButtonWidget renewButton;
    private ButtonWidget configureButton;
    private ButtonWidget leaveButton;
    RealmSelectionList realmSelectionList;
    private RealmsServerFilterer serverFilterer;
    private List<RealmsServer> availableSnapshotServers = List.of();
    RealmsServerPlayerList field_52116 = new RealmsServerPlayerList();
    private volatile boolean trialAvailable;
    @Nullable
    private volatile String newsLink;
    long lastPlayButtonClickTime;
    private final List<RealmsNotification> notifications = new ArrayList<RealmsNotification>();
    private ButtonWidget purchaseButton;
    private NotificationButtonWidget inviteButton;
    private NotificationButtonWidget newsButton;
    private LoadStatus loadStatus;
    @Nullable
    private ThreePartsLayoutWidget layout;

    public RealmsMainScreen(Screen parent) {
        super(MENU_TEXT);
        this.parent = parent;
        this.rateLimiter = RateLimiter.create(0.01666666753590107);
    }

    @Override
    public void init() {
        this.serverFilterer = new RealmsServerFilterer(this.client);
        this.realmSelectionList = new RealmSelectionList();
        MutableText lv = Text.translatable("mco.invites.title");
        this.inviteButton = new NotificationButtonWidget(lv, INVITE_ICON_TEXTURE, button -> this.client.setScreen(new RealmsPendingInvitesScreen(this, lv)));
        MutableText lv2 = Text.translatable("mco.news");
        this.newsButton = new NotificationButtonWidget(lv2, NEWS_ICON_TEXTURE, button -> {
            String string = this.newsLink;
            if (string == null) {
                return;
            }
            ConfirmLinkScreen.open(this, string);
            if (this.newsButton.getNotificationCount() != 0) {
                RealmsPersistence.RealmsPersistenceData lv = RealmsPersistence.readFile();
                lv.hasUnreadNews = false;
                RealmsPersistence.writeFile(lv);
                this.newsButton.setNotificationCount(0);
            }
        });
        this.newsButton.setTooltip(Tooltip.of(lv2));
        this.playButton = ButtonWidget.builder(PLAY_TEXT, button -> RealmsMainScreen.play(this.findServer(), this)).width(100).build();
        this.configureButton = ButtonWidget.builder(CONFIGURE_TEXT, button -> this.configureClicked(this.findServer())).width(100).build();
        this.renewButton = ButtonWidget.builder(EXPIRED_RENEW_TEXT, button -> this.onRenew(this.findServer())).width(100).build();
        this.leaveButton = ButtonWidget.builder(LEAVE_TEXT, button -> this.leaveClicked(this.findServer())).width(100).build();
        this.purchaseButton = ButtonWidget.builder(Text.translatable("mco.selectServer.purchase"), button -> this.showBuyRealmsScreen()).size(100, 20).build();
        this.backButton = ButtonWidget.builder(ScreenTexts.BACK, button -> this.close()).width(100).build();
        if (RealmsClient.ENVIRONMENT == RealmsClient.Environment.STAGE) {
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("Snapshot"), Text.literal("Release")).build(5, 5, 100, 20, Text.literal("Realm"), (button, snapshot) -> {
                showingSnapshotRealms = snapshot;
                this.availableSnapshotServers = List.of();
                this.resetPeriodicCheckers();
            }));
        }
        this.onLoadStatusChange(LoadStatus.LOADING);
        this.refreshButtons();
        this.availabilityInfo.thenAcceptAsync(availabilityInfo -> {
            Screen lv = availabilityInfo.createScreen(this.parent);
            if (lv == null) {
                this.periodicRunnersManager = this.createPeriodicRunnersManager(this.client.getRealmsPeriodicCheckers());
            } else {
                this.client.setScreen(lv);
            }
        }, this.executor);
    }

    public static boolean isSnapshotRealmsEligible() {
        return gameOnSnapshot && showingSnapshotRealms;
    }

    @Override
    protected void initTabNavigation() {
        if (this.layout != null) {
            this.realmSelectionList.position(this.width, this.layout);
            this.layout.refreshPositions();
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    private void updateLoadStatus() {
        if (this.serverFilterer.isEmpty() && this.availableSnapshotServers.isEmpty() && this.notifications.isEmpty()) {
            this.onLoadStatusChange(LoadStatus.NO_REALMS);
        } else {
            this.onLoadStatusChange(LoadStatus.LIST);
        }
    }

    private void onLoadStatusChange(LoadStatus loadStatus) {
        if (this.loadStatus == loadStatus) {
            return;
        }
        if (this.layout != null) {
            this.layout.forEachChild(child -> this.remove((Element)child));
        }
        this.layout = this.makeLayoutFor(loadStatus);
        this.loadStatus = loadStatus;
        this.layout.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
        this.initTabNavigation();
    }

    private ThreePartsLayoutWidget makeLayoutFor(LoadStatus loadStatus) {
        ThreePartsLayoutWidget lv = new ThreePartsLayoutWidget(this);
        lv.setHeaderHeight(44);
        lv.addHeader(this.makeHeader());
        LayoutWidget lv2 = this.makeInnerLayout(loadStatus);
        lv2.refreshPositions();
        lv.setFooterHeight(lv2.getHeight() + 22);
        lv.addFooter(lv2);
        switch (loadStatus.ordinal()) {
            case 0: {
                lv.addBody(new RealmsLoadingWidget(this.textRenderer, LOADING_TEXT));
                break;
            }
            case 1: {
                lv.addBody(this.makeNoRealmsLayout());
                break;
            }
            case 2: {
                lv.addBody(this.realmSelectionList);
            }
        }
        return lv;
    }

    private LayoutWidget makeHeader() {
        int i = 90;
        DirectionalLayoutWidget lv = DirectionalLayoutWidget.horizontal().spacing(4);
        lv.getMainPositioner().alignVerticalCenter();
        lv.add(this.inviteButton);
        lv.add(this.newsButton);
        DirectionalLayoutWidget lv2 = DirectionalLayoutWidget.horizontal();
        lv2.getMainPositioner().alignVerticalCenter();
        lv2.add(EmptyWidget.ofWidth(90));
        lv2.add(IconWidget.create(128, 34, REALMS_TITLE_TEXTURE, 128, 64), Positioner::alignHorizontalCenter);
        lv2.add(new SimplePositioningWidget(90, 44)).add(lv, Positioner::alignRight);
        return lv2;
    }

    private LayoutWidget makeInnerLayout(LoadStatus loadStatus) {
        GridWidget lv = new GridWidget().setSpacing(4);
        GridWidget.Adder lv2 = lv.createAdder(3);
        if (loadStatus == LoadStatus.LIST) {
            lv2.add(this.playButton);
            lv2.add(this.configureButton);
            lv2.add(this.renewButton);
            lv2.add(this.leaveButton);
        }
        lv2.add(this.purchaseButton);
        lv2.add(this.backButton);
        return lv;
    }

    private DirectionalLayoutWidget makeNoRealmsLayout() {
        DirectionalLayoutWidget lv = DirectionalLayoutWidget.vertical().spacing(8);
        lv.getMainPositioner().alignHorizontalCenter();
        lv.add(IconWidget.create(130, 64, NO_REALMS_TEXTURE, 130, 64));
        NarratedMultilineTextWidget lv2 = new NarratedMultilineTextWidget(308, NO_REALMS_TEXT, this.textRenderer, false, 4);
        lv.add(lv2);
        return lv;
    }

    void refreshButtons() {
        RealmsServer lv = this.findServer();
        this.purchaseButton.active = this.loadStatus != LoadStatus.LOADING;
        this.playButton.active = lv != null && this.shouldPlayButtonBeActive(lv);
        this.renewButton.active = lv != null && this.shouldRenewButtonBeActive(lv);
        this.leaveButton.active = lv != null && this.shouldLeaveButtonBeActive(lv);
        this.configureButton.active = lv != null && this.shouldConfigureButtonBeActive(lv);
    }

    boolean shouldPlayButtonBeActive(RealmsServer server) {
        boolean bl = !server.expired && server.state == RealmsServer.State.OPEN;
        return bl && (server.isCompatible() || server.needsUpgrade() || RealmsMainScreen.isSelfOwnedServer(server));
    }

    private boolean shouldRenewButtonBeActive(RealmsServer server) {
        return server.expired && RealmsMainScreen.isSelfOwnedServer(server);
    }

    private boolean shouldConfigureButtonBeActive(RealmsServer server) {
        return RealmsMainScreen.isSelfOwnedServer(server) && server.state != RealmsServer.State.UNINITIALIZED;
    }

    private boolean shouldLeaveButtonBeActive(RealmsServer server) {
        return !RealmsMainScreen.isSelfOwnedServer(server);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.periodicRunnersManager != null) {
            this.periodicRunnersManager.runAll();
        }
    }

    public static void resetPendingInvitesCount() {
        MinecraftClient.getInstance().getRealmsPeriodicCheckers().pendingInvitesCount.reset();
    }

    public static void resetServerList() {
        MinecraftClient.getInstance().getRealmsPeriodicCheckers().serverList.reset();
    }

    private void resetPeriodicCheckers() {
        for (PeriodicRunnerFactory.PeriodicRunner<?> lv : this.client.getRealmsPeriodicCheckers().getCheckers()) {
            lv.reset();
        }
    }

    private PeriodicRunnerFactory.RunnersManager createPeriodicRunnersManager(RealmsPeriodicCheckers periodicCheckers) {
        PeriodicRunnerFactory.RunnersManager lv = periodicCheckers.runnerFactory.create();
        lv.add(periodicCheckers.serverList, availableServers -> {
            this.serverFilterer.filterAndSort(availableServers.serverList());
            this.availableSnapshotServers = availableServers.availableSnapshotServers();
            this.refresh();
            boolean bl = false;
            for (RealmsServer lv : this.serverFilterer) {
                if (!this.isOwnedNotExpired(lv)) continue;
                bl = true;
            }
            if (!regionsPinged && bl) {
                regionsPinged = true;
                this.pingRegions();
            }
        });
        RealmsMainScreen.request(RealmsClient::listNotifications, notifications -> {
            this.notifications.clear();
            this.notifications.addAll((Collection<RealmsNotification>)notifications);
            for (RealmsNotification lv : notifications) {
                RealmsNotification.InfoPopup lv2;
                PopupScreen lv3;
                if (!(lv instanceof RealmsNotification.InfoPopup) || (lv3 = (lv2 = (RealmsNotification.InfoPopup)lv).createScreen(this, this::dismissNotification)) == null) continue;
                this.client.setScreen(lv3);
                this.markAsSeen(List.of(lv));
                break;
            }
            if (!this.notifications.isEmpty() && this.loadStatus != LoadStatus.LOADING) {
                this.refresh();
            }
        });
        lv.add(periodicCheckers.pendingInvitesCount, pendingInvitesCount -> {
            this.inviteButton.setNotificationCount((int)pendingInvitesCount);
            this.inviteButton.setTooltip(pendingInvitesCount == 0 ? Tooltip.of(NO_PENDING_TOOLTIP) : Tooltip.of(PENDING_TOOLTIP));
            if (pendingInvitesCount > 0 && this.rateLimiter.tryAcquire(1)) {
                this.client.getNarratorManager().narrate(Text.translatable("mco.configure.world.invite.narration", pendingInvitesCount));
            }
        });
        lv.add(periodicCheckers.trialAvailability, trialAvailable -> {
            this.trialAvailable = trialAvailable;
        });
        lv.add(periodicCheckers.field_52122, arg -> {
            this.field_52116 = arg;
        });
        lv.add(periodicCheckers.news, news -> {
            arg.newsUpdater.updateNews((RealmsNews)news);
            this.newsLink = arg.newsUpdater.getNewsLink();
            this.newsButton.setNotificationCount(arg.newsUpdater.hasUnreadNews() ? Integer.MAX_VALUE : 0);
        });
        return lv;
    }

    private void markAsSeen(Collection<RealmsNotification> notifications) {
        ArrayList<UUID> list = new ArrayList<UUID>(notifications.size());
        for (RealmsNotification lv : notifications) {
            if (lv.isSeen() || this.seenNotifications.contains(lv.getUuid())) continue;
            list.add(lv.getUuid());
        }
        if (!list.isEmpty()) {
            RealmsMainScreen.request(client -> {
                client.markNotificationsAsSeen(list);
                return null;
            }, result -> this.seenNotifications.addAll(list));
        }
    }

    private static <T> void request(Request<T> request, Consumer<T> resultConsumer) {
        MinecraftClient lv = MinecraftClient.getInstance();
        ((CompletableFuture)CompletableFuture.supplyAsync(() -> {
            try {
                return request.request(RealmsClient.createRealmsClient(lv));
            } catch (RealmsServiceException lv) {
                throw new RuntimeException(lv);
            }
        }).thenAcceptAsync(resultConsumer, (Executor)lv)).exceptionally(throwable -> {
            LOGGER.error("Failed to execute call to Realms Service", (Throwable)throwable);
            return null;
        });
    }

    private void refresh() {
        RealmsServer lv = this.findServer();
        this.realmSelectionList.clear();
        for (RealmsNotification lv2 : this.notifications) {
            if (!this.addNotificationEntry(lv2)) continue;
            this.markAsSeen(List.of(lv2));
            break;
        }
        for (RealmsServer lv3 : this.availableSnapshotServers) {
            this.realmSelectionList.addEntry(new SnapshotEntry(lv3));
        }
        for (RealmsServer lv3 : this.serverFilterer) {
            Entry lv4;
            if (RealmsMainScreen.isSnapshotRealmsEligible() && !lv3.hasParentWorld()) {
                if (lv3.state == RealmsServer.State.UNINITIALIZED) continue;
                lv4 = new ParentRealmSelectionListEntry(lv3);
            } else {
                lv4 = new RealmSelectionListEntry(lv3);
            }
            this.realmSelectionList.addEntry(lv4);
            if (lv == null || lv.id != lv3.id) continue;
            this.realmSelectionList.setSelected(lv4);
        }
        this.updateLoadStatus();
        this.refreshButtons();
    }

    private boolean addNotificationEntry(RealmsNotification notification) {
        if (notification instanceof RealmsNotification.VisitUrl) {
            RealmsNotification.VisitUrl lv = (RealmsNotification.VisitUrl)notification;
            Text lv2 = lv.getDefaultMessage();
            int i = this.textRenderer.getWrappedLinesHeight(lv2, 216);
            int j = MathHelper.ceilDiv(i + 7, 36) - 1;
            this.realmSelectionList.addEntry(new VisitUrlNotification(lv2, j + 2, lv));
            for (int k = 0; k < j; ++k) {
                this.realmSelectionList.addEntry(new EmptyEntry(this));
            }
            this.realmSelectionList.addEntry(new VisitButtonEntry(lv.createButton(this)));
            return true;
        }
        return false;
    }

    private void pingRegions() {
        new Thread(() -> {
            List<RegionPingResult> list = Ping.pingAllRegions();
            RealmsClient lv = RealmsClient.create();
            PingResult lv2 = new PingResult();
            lv2.pingResults = list;
            lv2.worldIds = this.getOwnedNonExpiredWorldIds();
            try {
                lv.sendPingResults(lv2);
            } catch (Throwable throwable) {
                LOGGER.warn("Could not send ping result to Realms: ", throwable);
            }
        }).start();
    }

    private List<Long> getOwnedNonExpiredWorldIds() {
        ArrayList<Long> list = Lists.newArrayList();
        for (RealmsServer lv : this.serverFilterer) {
            if (!this.isOwnedNotExpired(lv)) continue;
            list.add(lv.id);
        }
        return list;
    }

    private void onRenew(@Nullable RealmsServer realmsServer) {
        if (realmsServer != null) {
            String string = Urls.getExtendJavaRealmsUrl(realmsServer.remoteSubscriptionId, this.client.getSession().getUuidOrNull(), realmsServer.expiredTrial);
            this.client.keyboard.setClipboard(string);
            Util.getOperatingSystem().open(string);
        }
    }

    private void configureClicked(@Nullable RealmsServer serverData) {
        if (serverData != null && this.client.uuidEquals(serverData.ownerUUID)) {
            this.client.setScreen(new RealmsConfigureWorldScreen(this, serverData.id));
        }
    }

    private void leaveClicked(@Nullable RealmsServer selectedServer) {
        if (selectedServer != null && !this.client.uuidEquals(selectedServer.ownerUUID)) {
            MutableText lv = Text.translatable("mco.configure.world.leave.question.line1");
            this.client.setScreen(RealmsPopups.createInfoPopup(this, lv, popup -> this.leaveServer(selectedServer)));
        }
    }

    @Nullable
    private RealmsServer findServer() {
        Object e = this.realmSelectionList.getSelectedOrNull();
        if (e instanceof RealmSelectionListEntry) {
            RealmSelectionListEntry lv = (RealmSelectionListEntry)e;
            return lv.getRealmsServer();
        }
        return null;
    }

    private void leaveServer(final RealmsServer server) {
        new Thread("Realms-leave-server"){

            @Override
            public void run() {
                try {
                    RealmsClient lv = RealmsClient.create();
                    lv.uninviteMyselfFrom(server.id);
                    RealmsMainScreen.this.client.execute(RealmsMainScreen::resetServerList);
                } catch (RealmsServiceException lv2) {
                    LOGGER.error("Couldn't configure world", lv2);
                    RealmsMainScreen.this.client.execute(() -> RealmsMainScreen.this.client.setScreen(new RealmsGenericErrorScreen(lv2, (Screen)RealmsMainScreen.this)));
                }
            }
        }.start();
        this.client.setScreen(this);
    }

    void dismissNotification(UUID notification) {
        RealmsMainScreen.request(client -> {
            client.dismissNotifications(List.of(notification));
            return null;
        }, void_ -> {
            this.notifications.removeIf(notificationId -> notificationId.isDismissable() && notification.equals(notificationId.getUuid()));
            this.refresh();
        });
    }

    public void removeSelection() {
        this.realmSelectionList.setSelected((Entry)null);
        RealmsMainScreen.resetServerList();
    }

    @Override
    public Text getNarratedTitle() {
        return switch (this.loadStatus.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> ScreenTexts.joinSentences(super.getNarratedTitle(), LOADING_TEXT);
            case 1 -> ScreenTexts.joinSentences(super.getNarratedTitle(), NO_REALMS_TEXT);
            case 2 -> super.getNarratedTitle();
        };
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (RealmsMainScreen.isSnapshotRealmsEligible()) {
            context.drawTextWithShadow(this.textRenderer, "Minecraft " + SharedConstants.getGameVersion().getName(), 2, this.height - 10, Colors.WHITE);
        }
        if (this.trialAvailable && this.purchaseButton.active) {
            BuyRealmsScreen.drawTrialAvailableTexture(context, this.purchaseButton);
        }
        switch (RealmsClient.ENVIRONMENT) {
            case STAGE: {
                this.drawEnvironmentText(context, "STAGE!", -256);
                break;
            }
            case LOCAL: {
                this.drawEnvironmentText(context, "LOCAL!", 0x7FFF7F);
            }
        }
    }

    private void showBuyRealmsScreen() {
        this.client.setScreen(new BuyRealmsScreen(this, this.trialAvailable));
    }

    public static void play(@Nullable RealmsServer serverData, Screen parent) {
        RealmsMainScreen.play(serverData, parent, false);
    }

    public static void play(@Nullable RealmsServer serverData, Screen parent, boolean needsPreparation) {
        if (serverData != null) {
            if (!RealmsMainScreen.isSnapshotRealmsEligible() || needsPreparation || serverData.isMinigame()) {
                MinecraftClient.getInstance().setScreen(new RealmsLongRunningMcoTaskScreen(parent, new RealmsPrepareConnectionTask(parent, serverData)));
                return;
            }
            switch (serverData.compatibility) {
                case COMPATIBLE: {
                    MinecraftClient.getInstance().setScreen(new RealmsLongRunningMcoTaskScreen(parent, new RealmsPrepareConnectionTask(parent, serverData)));
                    break;
                }
                case UNVERIFIABLE: {
                    RealmsMainScreen.showCompatibilityScreen(serverData, parent, Text.translatable("mco.compatibility.unverifiable.title").withColor(Colors.LIGHT_YELLOW), Text.translatable("mco.compatibility.unverifiable.message"), ScreenTexts.CONTINUE);
                    break;
                }
                case NEEDS_DOWNGRADE: {
                    RealmsMainScreen.showCompatibilityScreen(serverData, parent, Text.translatable("selectWorld.backupQuestion.downgrade").withColor(Colors.LIGHT_RED), Text.translatable("mco.compatibility.downgrade.description", Text.literal(serverData.activeVersion).withColor(Colors.LIGHT_YELLOW), Text.literal(SharedConstants.getGameVersion().getName()).withColor(Colors.LIGHT_YELLOW)), Text.translatable("mco.compatibility.downgrade"));
                    break;
                }
                case NEEDS_UPGRADE: {
                    RealmsMainScreen.method_60861(serverData, parent);
                    break;
                }
                case INCOMPATIBLE: {
                    MinecraftClient.getInstance().setScreen(new PopupScreen.Builder(parent, INCOMPATIBLE_POPUP_TITLE).message(Text.translatable("mco.compatibility.incompatible.series.popup.message", Text.literal(serverData.activeVersion).withColor(Colors.LIGHT_YELLOW), Text.literal(SharedConstants.getGameVersion().getName()).withColor(Colors.LIGHT_YELLOW))).button(ScreenTexts.BACK, PopupScreen::close).build());
                    break;
                }
                case RELEASE_TYPE_INCOMPATIBLE: {
                    MinecraftClient.getInstance().setScreen(new PopupScreen.Builder(parent, INCOMPATIBLE_POPUP_TITLE).message(INCOMPATIBLE_RELEASE_TYPE_MESSAGE).button(ScreenTexts.BACK, PopupScreen::close).build());
                }
            }
        }
    }

    private static void showCompatibilityScreen(RealmsServer arg, Screen parent, Text arg32, Text arg4, Text arg5) {
        MinecraftClient.getInstance().setScreen(new PopupScreen.Builder(parent, arg32).message(arg4).button(arg5, arg3 -> {
            MinecraftClient.getInstance().setScreen(new RealmsLongRunningMcoTaskScreen(parent, new RealmsPrepareConnectionTask(parent, arg)));
            RealmsMainScreen.resetServerList();
        }).button(ScreenTexts.CANCEL, PopupScreen::close).build());
    }

    private static void method_60861(RealmsServer arg, Screen arg2) {
        MutableText lv = Text.translatable("mco.compatibility.upgrade.title").withColor(Colors.LIGHT_YELLOW);
        MutableText lv2 = Text.translatable("mco.compatibility.upgrade");
        MutableText lv3 = Text.literal(arg.activeVersion).withColor(Colors.LIGHT_YELLOW);
        MutableText lv4 = Text.literal(SharedConstants.getGameVersion().getName()).withColor(Colors.LIGHT_YELLOW);
        MutableText lv5 = RealmsMainScreen.isSelfOwnedServer(arg) ? Text.translatable("mco.compatibility.upgrade.description", lv3, lv4) : Text.translatable("mco.compatibility.upgrade.friend.description", lv3, lv4);
        RealmsMainScreen.showCompatibilityScreen(arg, arg2, lv, lv5, lv2);
    }

    public static Text getVersionText(String version, boolean compatible) {
        return RealmsMainScreen.getVersionText(version, compatible ? -8355712 : -2142128);
    }

    public static Text getVersionText(String version, int color) {
        if (StringUtils.isBlank(version)) {
            return ScreenTexts.EMPTY;
        }
        return Text.translatable("mco.version", Text.literal(version).withColor(color));
    }

    static boolean isSelfOwnedServer(RealmsServer arg) {
        return MinecraftClient.getInstance().uuidEquals(arg.ownerUUID);
    }

    private boolean isOwnedNotExpired(RealmsServer serverData) {
        return RealmsMainScreen.isSelfOwnedServer(serverData) && !serverData.expired;
    }

    private void drawEnvironmentText(DrawContext context, String text, int color) {
        context.getMatrices().push();
        context.getMatrices().translate(this.width / 2 - 25, 20.0f, 0.0f);
        context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-20.0f));
        context.getMatrices().scale(1.5f, 1.5f, 1.5f);
        context.drawText(this.textRenderer, text, 0, 0, color, false);
        context.getMatrices().pop();
    }

    static {
        showingSnapshotRealms = gameOnSnapshot = !SharedConstants.getGameVersion().isStable();
    }

    @Environment(value=EnvType.CLIENT)
    class RealmSelectionList
    extends RealmsObjectSelectionList<Entry> {
        public RealmSelectionList() {
            super(RealmsMainScreen.this.width, RealmsMainScreen.this.height, 0, 36);
        }

        @Override
        public void setSelected(@Nullable Entry arg) {
            super.setSelected(arg);
            RealmsMainScreen.this.refreshButtons();
        }

        @Override
        public int getMaxPosition() {
            return this.getEntryCount() * 36;
        }

        @Override
        public int getRowWidth() {
            return 300;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class NotificationButtonWidget
    extends TextIconButtonWidget.IconOnly {
        private static final Identifier[] TEXTURES = new Identifier[]{Identifier.method_60656("notification/1"), Identifier.method_60656("notification/2"), Identifier.method_60656("notification/3"), Identifier.method_60656("notification/4"), Identifier.method_60656("notification/5"), Identifier.method_60656("notification/more")};
        private static final int field_45228 = Integer.MAX_VALUE;
        private static final int SIZE = 20;
        private static final int TEXTURE_SIZE = 14;
        private int notificationCount;

        public NotificationButtonWidget(Text message, Identifier texture, ButtonWidget.PressAction onPress) {
            super(20, 20, message, 14, 14, texture, onPress, null);
        }

        int getNotificationCount() {
            return this.notificationCount;
        }

        public void setNotificationCount(int notificationCount) {
            this.notificationCount = notificationCount;
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
            if (this.active && this.notificationCount != 0) {
                this.render(context);
            }
        }

        private void render(DrawContext context) {
            context.drawGuiTexture(TEXTURES[Math.min(this.notificationCount, 6) - 1], this.getX() + this.getWidth() - 5, this.getY() - 3, 8, 8);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum LoadStatus {
        LOADING,
        NO_REALMS,
        LIST;

    }

    @Environment(value=EnvType.CLIENT)
    static interface Request<T> {
        public T request(RealmsClient var1) throws RealmsServiceException;
    }

    @Environment(value=EnvType.CLIENT)
    class SnapshotEntry
    extends Entry {
        private static final Text START_TEXT = Text.translatable("mco.snapshot.start");
        private static final int field_46677 = 5;
        private final TooltipState tooltip;
        private final RealmsServer server;

        public SnapshotEntry(RealmsServer server) {
            this.tooltip = new TooltipState();
            this.server = server;
            this.tooltip.setTooltip(Tooltip.of(Text.translatable("mco.snapshot.tooltip")));
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawGuiTexture(NEW_REALM_ICON_TEXTURE, x - 5, y + entryHeight / 2 - 10, 40, 20);
            int p = y + entryHeight / 2 - ((RealmsMainScreen)RealmsMainScreen.this).textRenderer.fontHeight / 2;
            context.drawTextWithShadow(RealmsMainScreen.this.textRenderer, START_TEXT, x + 40 - 2, p - 5, 0x7FFF7F);
            context.drawTextWithShadow(RealmsMainScreen.this.textRenderer, Text.translatable("mco.snapshot.description", this.server.name), x + 40 - 2, p + 5, Colors.GRAY);
            this.tooltip.render(hovered, this.isFocused(), new ScreenRect(x, y, entryWidth, entryHeight));
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.showPopup();
            return true;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (KeyCodes.isToggle(keyCode)) {
                this.showPopup();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        private void showPopup() {
            RealmsMainScreen.this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            RealmsMainScreen.this.client.setScreen(new PopupScreen.Builder(RealmsMainScreen.this, Text.translatable("mco.snapshot.createSnapshotPopup.title")).message(Text.translatable("mco.snapshot.createSnapshotPopup.text")).button(Text.translatable("mco.selectServer.create"), screen -> RealmsMainScreen.this.client.setScreen(new RealmsCreateRealmScreen(RealmsMainScreen.this, this.server.id))).button(ScreenTexts.CANCEL, PopupScreen::close).build());
        }

        @Override
        public Text getNarration() {
            return Text.translatable("gui.narrate.button", ScreenTexts.joinSentences(START_TEXT, Text.translatable("mco.snapshot.description", this.server.name)));
        }
    }

    @Environment(value=EnvType.CLIENT)
    class ParentRealmSelectionListEntry
    extends Entry {
        private final RealmsServer server;
        private final TooltipState tooltip;

        public ParentRealmSelectionListEntry(RealmsServer server) {
            this.tooltip = new TooltipState();
            this.server = server;
            if (!server.expired) {
                this.tooltip.setTooltip(Tooltip.of(Text.translatable("mco.snapshot.parent.tooltip")));
            }
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int p = this.getNameX(x);
            int q = this.getNameY(y);
            RealmsUtil.drawPlayerHead(context, x, y, 32, this.server.ownerUUID);
            Text lv = RealmsMainScreen.getVersionText(this.server.activeVersion, -8355712);
            int r = this.getVersionRight(x, entryWidth, lv);
            this.drawTrimmedText(context, this.server.getName(), p, q, r, -8355712);
            if (lv != ScreenTexts.EMPTY) {
                context.drawText(RealmsMainScreen.this.textRenderer, lv, r, q, Colors.GRAY, false);
            }
            context.drawText(RealmsMainScreen.this.textRenderer, this.server.getDescription(), p, this.getDescriptionY(q), Colors.GRAY, false);
            this.drawOwnerOrExpiredText(context, y, x, this.server);
            this.renderStatusIcon(this.server, context, x + entryWidth, y, mouseX, mouseY);
            this.tooltip.render(hovered, this.isFocused(), new ScreenRect(x, y, entryWidth, entryHeight));
        }

        @Override
        public Text getNarration() {
            return Text.literal(this.server.name);
        }
    }

    @Environment(value=EnvType.CLIENT)
    class RealmSelectionListEntry
    extends Entry {
        private static final Text field_52119 = Text.translatable("mco.onlinePlayers");
        private static final int field_52120 = 9;
        private static final int field_32054 = 36;
        private final RealmsServer server;
        private final TooltipState tooltip;

        public RealmSelectionListEntry(RealmsServer server) {
            this.tooltip = new TooltipState();
            this.server = server;
            boolean bl = RealmsMainScreen.isSelfOwnedServer(server);
            if (RealmsMainScreen.isSnapshotRealmsEligible() && bl && server.hasParentWorld()) {
                this.tooltip.setTooltip(Tooltip.of(Text.translatable("mco.snapshot.paired", server.parentWorldName)));
            } else if (!bl && server.needsDowngrade()) {
                this.tooltip.setTooltip(Tooltip.of(Text.translatable("mco.snapshot.friendsRealm.downgrade", server.activeVersion)));
            }
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if (this.server.state == RealmsServer.State.UNINITIALIZED) {
                context.drawGuiTexture(NEW_REALM_ICON_TEXTURE, x - 5, y + entryHeight / 2 - 10, 40, 20);
                int p = y + entryHeight / 2 - ((RealmsMainScreen)RealmsMainScreen.this).textRenderer.fontHeight / 2;
                context.drawTextWithShadow(RealmsMainScreen.this.textRenderer, UNINITIALIZED_TEXT, x + 40 - 2, p, 0x7FFF7F);
                return;
            }
            this.renderStatusIcon(this.server, context, x + 36, y, mouseX, mouseY);
            RealmsUtil.drawPlayerHead(context, x, y, 32, this.server.ownerUUID);
            this.drawServerNameAndVersion(context, y, x, entryWidth);
            this.drawDescription(context, y, x);
            this.drawOwnerOrExpiredText(context, y, x, this.server);
            boolean bl2 = this.method_60862(context, y, x, entryWidth, entryHeight, mouseX, mouseY);
            this.renderStatusIcon(this.server, context, x + entryWidth, y, mouseX, mouseY);
            if (!bl2) {
                this.tooltip.render(hovered, this.isFocused(), new ScreenRect(x, y, entryWidth, entryHeight));
            }
        }

        private void drawServerNameAndVersion(DrawContext context, int y, int x, int width) {
            int l = this.getNameX(x);
            int m = this.getNameY(y);
            Text lv = RealmsMainScreen.getVersionText(this.server.activeVersion, this.server.isCompatible());
            int n = this.getVersionRight(x, width, lv);
            this.drawTrimmedText(context, this.server.getName(), l, m, n, -1);
            if (lv != ScreenTexts.EMPTY && !this.server.isMinigame()) {
                context.drawText(RealmsMainScreen.this.textRenderer, lv, n, m, Colors.GRAY, false);
            }
        }

        private void drawDescription(DrawContext context, int y, int x) {
            int k = this.getNameX(x);
            int l = this.getNameY(y);
            int m = this.getDescriptionY(l);
            String string = this.server.getMinigameName();
            if (this.server.isMinigame() && string != null) {
                MutableText lv = Text.literal(string).formatted(Formatting.GRAY);
                context.drawText(RealmsMainScreen.this.textRenderer, Text.translatable("mco.selectServer.minigameName", lv).withColor(Colors.LIGHT_YELLOW), k, m, Colors.WHITE, false);
            } else {
                context.drawText(RealmsMainScreen.this.textRenderer, this.server.getDescription(), k, this.getDescriptionY(l), Colors.GRAY, false);
            }
        }

        private boolean method_60862(DrawContext arg, int i, int j, int k, int l, int m, int n) {
            List<ProfileResult> list = RealmsMainScreen.this.field_52116.method_60863(this.server.id);
            if (!list.isEmpty()) {
                int o = j + k - 21;
                int p = i + l - 9 - 2;
                int q = o;
                for (int r = 0; r < list.size(); ++r) {
                    PlayerSkinDrawer.draw(arg, MinecraftClient.getInstance().getSkinProvider().getSkinTextures(list.get(r).profile()), q -= 9 + (r == 0 ? 0 : 3), p, 9);
                }
                if (m >= q && m <= o && n >= p && n <= p + 9) {
                    arg.drawTooltip(RealmsMainScreen.this.textRenderer, List.of(field_52119), Optional.of(new class_9805.class_9806(list)), m, n);
                    return true;
                }
            }
            return false;
        }

        private void play() {
            RealmsMainScreen.this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            RealmsMainScreen.play(this.server, RealmsMainScreen.this);
        }

        private void createRealm() {
            RealmsMainScreen.this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            RealmsCreateRealmScreen lv = new RealmsCreateRealmScreen(RealmsMainScreen.this, this.server);
            RealmsMainScreen.this.client.setScreen(lv);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.server.state == RealmsServer.State.UNINITIALIZED) {
                this.createRealm();
            } else if (RealmsMainScreen.this.shouldPlayButtonBeActive(this.server)) {
                if (Util.getMeasuringTimeMs() - RealmsMainScreen.this.lastPlayButtonClickTime < 250L && this.isFocused()) {
                    this.play();
                }
                RealmsMainScreen.this.lastPlayButtonClickTime = Util.getMeasuringTimeMs();
            }
            return true;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (KeyCodes.isToggle(keyCode)) {
                if (this.server.state == RealmsServer.State.UNINITIALIZED) {
                    this.createRealm();
                    return true;
                }
                if (RealmsMainScreen.this.shouldPlayButtonBeActive(this.server)) {
                    this.play();
                    return true;
                }
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public Text getNarration() {
            if (this.server.state == RealmsServer.State.UNINITIALIZED) {
                return UNINITIALIZED_BUTTON_NARRATION;
            }
            return Text.translatable("narrator.select", this.server.name);
        }

        public RealmsServer getRealmsServer() {
            return this.server;
        }
    }

    @Environment(value=EnvType.CLIENT)
    abstract class Entry
    extends AlwaysSelectedEntryListWidget.Entry<Entry> {
        protected static final int field_46680 = 10;
        private static final int field_46681 = 28;
        protected static final int field_52117 = 7;
        protected static final int field_52118 = 2;

        Entry() {
        }

        protected void renderStatusIcon(RealmsServer server, DrawContext context, int x, int y, int mouseX, int mouseY) {
            int m = x - 10 - 7;
            int n = y + 2;
            if (server.expired) {
                this.drawTextureWithTooltip(context, m, n, mouseX, mouseY, EXPIRED_STATUS_TEXTURE, () -> EXPIRED_TEXT);
            } else if (server.state == RealmsServer.State.CLOSED) {
                this.drawTextureWithTooltip(context, m, n, mouseX, mouseY, CLOSED_STATUS_TEXTURE, () -> CLOSED_TEXT);
            } else if (RealmsMainScreen.isSelfOwnedServer(server) && server.daysLeft < 7) {
                this.drawTextureWithTooltip(context, m, n, mouseX, mouseY, EXPIRES_SOON_STATUS_TEXTURE, () -> {
                    if (arg.daysLeft <= 0) {
                        return EXPIRES_SOON_TEXT;
                    }
                    if (arg.daysLeft == 1) {
                        return EXPIRES_IN_A_DAY_TEXT;
                    }
                    return Text.translatable("mco.selectServer.expires.days", arg.daysLeft);
                });
            } else if (server.state == RealmsServer.State.OPEN) {
                this.drawTextureWithTooltip(context, m, n, mouseX, mouseY, OPEN_STATUS_TEXTURE, () -> OPEN_TEXT);
            }
        }

        private void drawTextureWithTooltip(DrawContext context, int x, int y, int mouseX, int mouseY, Identifier texture, Supplier<Text> tooltip) {
            context.drawGuiTexture(texture, x, y, 10, 28);
            if (RealmsMainScreen.this.realmSelectionList.isMouseOver(mouseX, mouseY) && mouseX >= x && mouseX <= x + 10 && mouseY >= y && mouseY <= y + 28) {
                RealmsMainScreen.this.setTooltip(tooltip.get());
            }
        }

        protected void drawOwnerOrExpiredText(DrawContext context, int y, int x, RealmsServer server) {
            int k = this.getNameX(x);
            int l = this.getNameY(y);
            int m = this.getStatusY(l);
            if (!RealmsMainScreen.isSelfOwnedServer(server)) {
                context.drawText(RealmsMainScreen.this.textRenderer, server.owner, k, this.getStatusY(l), Colors.GRAY, false);
            } else if (server.expired) {
                Text lv = server.expiredTrial ? EXPIRED_TRIAL_TEXT : EXPIRED_LIST_TEXT;
                context.drawText(RealmsMainScreen.this.textRenderer, lv, k, m, Colors.LIGHT_RED, false);
            }
        }

        protected void drawTrimmedText(DrawContext context, String string, int left, int y, int right, int color) {
            int m = right - left;
            if (RealmsMainScreen.this.textRenderer.getWidth(string) > m) {
                String string2 = RealmsMainScreen.this.textRenderer.trimToWidth(string, m - RealmsMainScreen.this.textRenderer.getWidth("... "));
                context.drawText(RealmsMainScreen.this.textRenderer, string2 + "...", left, y, color, false);
            } else {
                context.drawText(RealmsMainScreen.this.textRenderer, string, left, y, color, false);
            }
        }

        protected int getVersionRight(int x, int width, Text version) {
            return x + width - RealmsMainScreen.this.textRenderer.getWidth(version) - 20;
        }

        protected int getNameY(int y) {
            return y + 1;
        }

        protected int getTextHeight() {
            return 2 + ((RealmsMainScreen)RealmsMainScreen.this).textRenderer.fontHeight;
        }

        protected int getNameX(int x) {
            return x + 36 + 2;
        }

        protected int getDescriptionY(int y) {
            return y + this.getTextHeight();
        }

        protected int getStatusY(int y) {
            return y + this.getTextHeight() * 2;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class VisitUrlNotification
    extends Entry {
        private static final int field_43002 = 40;
        private static final int field_43004 = -12303292;
        private final Text message;
        private final int lines;
        private final List<ClickableWidget> gridChildren = new ArrayList<ClickableWidget>();
        @Nullable
        private final CrossButton dismissButton;
        private final MultilineTextWidget textWidget;
        private final GridWidget grid;
        private final SimplePositioningWidget textGrid;
        private int width = -1;

        public VisitUrlNotification(Text message, int lines, RealmsNotification notification) {
            this.message = message;
            this.lines = lines;
            this.grid = new GridWidget();
            int j = 7;
            this.grid.add(IconWidget.create(20, 20, INFO_ICON_TEXTURE), 0, 0, this.grid.copyPositioner().margin(7, 7, 0, 0));
            this.grid.add(EmptyWidget.ofWidth(40), 0, 0);
            this.textGrid = this.grid.add(new SimplePositioningWidget(0, ((RealmsMainScreen)RealmsMainScreen.this).textRenderer.fontHeight * 3 * (lines - 1)), 0, 1, this.grid.copyPositioner().marginTop(7));
            this.textWidget = this.textGrid.add(new MultilineTextWidget(message, RealmsMainScreen.this.textRenderer).setCentered(true), this.textGrid.copyPositioner().alignHorizontalCenter().alignTop());
            this.grid.add(EmptyWidget.ofWidth(40), 0, 2);
            this.dismissButton = notification.isDismissable() ? this.grid.add(new CrossButton(button -> RealmsMainScreen.this.dismissNotification(notification.getUuid()), Text.translatable("mco.notification.dismiss")), 0, 2, this.grid.copyPositioner().alignRight().margin(0, 7, 7, 0)) : null;
            this.grid.forEachChild(this.gridChildren::add);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (this.dismissButton != null && this.dismissButton.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
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

        @Override
        public void drawBorder(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            super.drawBorder(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
            context.drawBorder(x - 2, y - 2, entryWidth, 36 * this.lines - 2, -12303292);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.grid.setPosition(x, y);
            this.setWidth(entryWidth - 4);
            this.gridChildren.forEach(child -> child.render(context, mouseX, mouseY, tickDelta));
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.dismissButton != null) {
                this.dismissButton.mouseClicked(mouseX, mouseY, button);
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public Text getNarration() {
            return this.message;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class EmptyEntry
    extends Entry {
        EmptyEntry(RealmsMainScreen arg) {
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        }

        @Override
        public Text getNarration() {
            return Text.empty();
        }
    }

    @Environment(value=EnvType.CLIENT)
    class VisitButtonEntry
    extends Entry {
        private final ButtonWidget button;

        public VisitButtonEntry(ButtonWidget button) {
            this.button = button;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.button.mouseClicked(mouseX, mouseY, button);
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (this.button.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.button.setPosition(RealmsMainScreen.this.width / 2 - 75, y + 4);
            this.button.render(context, mouseX, mouseY, tickDelta);
        }

        @Override
        public void setFocused(boolean focused) {
            super.setFocused(focused);
            this.button.setFocused(focused);
        }

        @Override
        public Text getNarration() {
            return this.button.getMessage();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class CrossButton
    extends TexturedButtonWidget {
        private static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.method_60656("widget/cross_button"), Identifier.method_60656("widget/cross_button_highlighted"));

        protected CrossButton(ButtonWidget.PressAction onPress, Text tooltip) {
            super(0, 0, 14, 14, TEXTURES, onPress);
            this.setTooltip(Tooltip.of(tooltip));
        }
    }
}

