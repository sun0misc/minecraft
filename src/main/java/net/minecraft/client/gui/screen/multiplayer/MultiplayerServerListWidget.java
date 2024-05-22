/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.LoadingDisplay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.WorldIcon;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.network.LanServerInfo;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class MultiplayerServerListWidget
extends AlwaysSelectedEntryListWidget<Entry> {
    static final Identifier INCOMPATIBLE_TEXTURE = Identifier.method_60656("server_list/incompatible");
    static final Identifier UNREACHABLE_TEXTURE = Identifier.method_60656("server_list/unreachable");
    static final Identifier PING_1_TEXTURE = Identifier.method_60656("server_list/ping_1");
    static final Identifier PING_2_TEXTURE = Identifier.method_60656("server_list/ping_2");
    static final Identifier PING_3_TEXTURE = Identifier.method_60656("server_list/ping_3");
    static final Identifier PING_4_TEXTURE = Identifier.method_60656("server_list/ping_4");
    static final Identifier PING_5_TEXTURE = Identifier.method_60656("server_list/ping_5");
    static final Identifier PINGING_1_TEXTURE = Identifier.method_60656("server_list/pinging_1");
    static final Identifier PINGING_2_TEXTURE = Identifier.method_60656("server_list/pinging_2");
    static final Identifier PINGING_3_TEXTURE = Identifier.method_60656("server_list/pinging_3");
    static final Identifier PINGING_4_TEXTURE = Identifier.method_60656("server_list/pinging_4");
    static final Identifier PINGING_5_TEXTURE = Identifier.method_60656("server_list/pinging_5");
    static final Identifier JOIN_HIGHLIGHTED_TEXTURE = Identifier.method_60656("server_list/join_highlighted");
    static final Identifier JOIN_TEXTURE = Identifier.method_60656("server_list/join");
    static final Identifier MOVE_UP_HIGHLIGHTED_TEXTURE = Identifier.method_60656("server_list/move_up_highlighted");
    static final Identifier MOVE_UP_TEXTURE = Identifier.method_60656("server_list/move_up");
    static final Identifier MOVE_DOWN_HIGHLIGHTED_TEXTURE = Identifier.method_60656("server_list/move_down_highlighted");
    static final Identifier MOVE_DOWN_TEXTURE = Identifier.method_60656("server_list/move_down");
    static final Logger LOGGER = LogUtils.getLogger();
    static final ThreadPoolExecutor SERVER_PINGER_THREAD_POOL = new ScheduledThreadPoolExecutor(5, new ThreadFactoryBuilder().setNameFormat("Server Pinger #%d").setDaemon(true).setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER)).build());
    static final Text LAN_SCANNING_TEXT = Text.translatable("lanServer.scanning");
    static final Text CANNOT_RESOLVE_TEXT = Text.translatable("multiplayer.status.cannot_resolve").withColor(Colors.RED);
    static final Text CANNOT_CONNECT_TEXT = Text.translatable("multiplayer.status.cannot_connect").withColor(Colors.RED);
    static final Text INCOMPATIBLE_TEXT = Text.translatable("multiplayer.status.incompatible");
    static final Text NO_CONNECTION_TEXT = Text.translatable("multiplayer.status.no_connection");
    static final Text PINGING_TEXT = Text.translatable("multiplayer.status.pinging");
    static final Text ONLINE_TEXT = Text.translatable("multiplayer.status.online");
    private final MultiplayerScreen screen;
    private final List<ServerEntry> servers = Lists.newArrayList();
    private final Entry scanningEntry = new ScanningEntry();
    private final List<LanServerEntry> lanServers = Lists.newArrayList();

    public MultiplayerServerListWidget(MultiplayerScreen screen, MinecraftClient client, int width, int height, int top, int bottom) {
        super(client, width, height, top, bottom);
        this.screen = screen;
    }

    private void updateEntries() {
        this.clearEntries();
        this.servers.forEach(server -> this.addEntry(server));
        this.addEntry(this.scanningEntry);
        this.lanServers.forEach(lanServer -> this.addEntry(lanServer));
    }

    @Override
    public void setSelected(@Nullable Entry arg) {
        super.setSelected(arg);
        this.screen.updateButtonActivationStates();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Entry lv = (Entry)this.getSelectedOrNull();
        return lv != null && lv.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void setServers(ServerList servers) {
        this.servers.clear();
        for (int i = 0; i < servers.size(); ++i) {
            this.servers.add(new ServerEntry(this.screen, servers.get(i)));
        }
        this.updateEntries();
    }

    public void setLanServers(List<LanServerInfo> lanServers) {
        int i = lanServers.size() - this.lanServers.size();
        this.lanServers.clear();
        for (LanServerInfo lv : lanServers) {
            this.lanServers.add(new LanServerEntry(this.screen, lv));
        }
        this.updateEntries();
        for (int j = this.lanServers.size() - i; j < this.lanServers.size(); ++j) {
            LanServerEntry lv2 = this.lanServers.get(j);
            int k = j - this.lanServers.size() + this.children().size();
            int l = this.getRowTop(k);
            int m = this.getRowBottom(k);
            if (m < this.getY() || l > this.getBottom()) continue;
            this.client.getNarratorManager().narrateSystemMessage(Text.translatable("multiplayer.lan.server_found", lv2.getMotdNarration()));
        }
    }

    @Override
    public int getRowWidth() {
        return 305;
    }

    public void onRemoved() {
    }

    @Environment(value=EnvType.CLIENT)
    public static class ScanningEntry
    extends Entry {
        private final MinecraftClient client = MinecraftClient.getInstance();

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int p = y + entryHeight / 2 - this.client.textRenderer.fontHeight / 2;
            context.drawText(this.client.textRenderer, LAN_SCANNING_TEXT, this.client.currentScreen.width / 2 - this.client.textRenderer.getWidth(LAN_SCANNING_TEXT) / 2, p, 0xFFFFFF, false);
            String string = LoadingDisplay.get(Util.getMeasuringTimeMs());
            context.drawText(this.client.textRenderer, string, this.client.currentScreen.width / 2 - this.client.textRenderer.getWidth(string) / 2, p + this.client.textRenderer.fontHeight, Colors.GRAY, false);
        }

        @Override
        public Text getNarration() {
            return LAN_SCANNING_TEXT;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class Entry
    extends AlwaysSelectedEntryListWidget.Entry<Entry>
    implements AutoCloseable {
        @Override
        public void close() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class ServerEntry
    extends Entry {
        private static final int field_32387 = 32;
        private static final int field_32388 = 32;
        private static final int field_47852 = 5;
        private static final int field_47853 = 10;
        private static final int field_47854 = 8;
        private final MultiplayerScreen screen;
        private final MinecraftClient client;
        private final ServerInfo server;
        private final WorldIcon icon;
        @Nullable
        private byte[] favicon;
        private long time;
        @Nullable
        private List<Text> playerListSummary;
        @Nullable
        private Identifier statusIconTexture;
        @Nullable
        private Text statusTooltipText;

        protected ServerEntry(MultiplayerScreen screen, ServerInfo server) {
            this.screen = screen;
            this.server = server;
            this.client = MinecraftClient.getInstance();
            this.icon = WorldIcon.forServer(this.client.getTextureManager(), server.address);
            this.update();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            byte[] bs;
            int p;
            if (this.server.getStatus() == ServerInfo.Status.INITIAL) {
                this.server.setStatus(ServerInfo.Status.PINGING);
                this.server.label = ScreenTexts.EMPTY;
                this.server.playerCountLabel = ScreenTexts.EMPTY;
                SERVER_PINGER_THREAD_POOL.submit(() -> {
                    try {
                        this.screen.getServerListPinger().add(this.server, () -> this.client.execute(this::saveFile), () -> {
                            this.server.setStatus(this.server.protocolVersion == SharedConstants.getGameVersion().getProtocolVersion() ? ServerInfo.Status.SUCCESSFUL : ServerInfo.Status.INCOMPATIBLE);
                            this.client.execute(this::update);
                        });
                    } catch (UnknownHostException unknownHostException) {
                        this.server.setStatus(ServerInfo.Status.UNREACHABLE);
                        this.server.label = CANNOT_RESOLVE_TEXT;
                        this.client.execute(this::update);
                    } catch (Exception exception) {
                        this.server.setStatus(ServerInfo.Status.UNREACHABLE);
                        this.server.label = CANNOT_CONNECT_TEXT;
                        this.client.execute(this::update);
                    }
                });
            }
            context.drawText(this.client.textRenderer, this.server.name, x + 32 + 3, y + 1, 0xFFFFFF, false);
            List<OrderedText> list = this.client.textRenderer.wrapLines(this.server.label, entryWidth - 32 - 2);
            for (p = 0; p < Math.min(list.size(), 2); ++p) {
                context.drawText(this.client.textRenderer, list.get(p), x + 32 + 3, y + 12 + this.client.textRenderer.fontHeight * p, -8355712, false);
            }
            this.draw(context, x, y, this.icon.getTextureId());
            if (this.server.getStatus() == ServerInfo.Status.PINGING) {
                p = (int)(Util.getMeasuringTimeMs() / 100L + (long)(index * 2) & 7L);
                if (p > 4) {
                    p = 8 - p;
                }
                this.statusIconTexture = switch (p) {
                    default -> PINGING_1_TEXTURE;
                    case 1 -> PINGING_2_TEXTURE;
                    case 2 -> PINGING_3_TEXTURE;
                    case 3 -> PINGING_4_TEXTURE;
                    case 4 -> PINGING_5_TEXTURE;
                };
            }
            p = x + entryWidth - 10 - 5;
            if (this.statusIconTexture != null) {
                context.drawGuiTexture(this.statusIconTexture, p, y, 10, 8);
            }
            if (!Arrays.equals(bs = this.server.getFavicon(), this.favicon)) {
                if (this.uploadFavicon(bs)) {
                    this.favicon = bs;
                } else {
                    this.server.setFavicon(null);
                    this.saveFile();
                }
            }
            Text lv = this.server.getStatus() == ServerInfo.Status.INCOMPATIBLE ? this.server.version.copy().formatted(Formatting.RED) : this.server.playerCountLabel;
            int q = this.client.textRenderer.getWidth(lv);
            int r = p - q - 5;
            context.drawText(this.client.textRenderer, lv, r, y + 1, Colors.GRAY, false);
            if (this.statusTooltipText != null && mouseX >= p && mouseX <= p + 10 && mouseY >= y && mouseY <= y + 8) {
                this.screen.setTooltip(this.statusTooltipText);
            } else if (this.playerListSummary != null && mouseX >= r && mouseX <= r + q && mouseY >= y && mouseY <= y - 1 + this.client.textRenderer.fontHeight) {
                this.screen.setTooltip(Lists.transform(this.playerListSummary, Text::asOrderedText));
            }
            if (this.client.options.getTouchscreen().getValue().booleanValue() || hovered) {
                context.fill(x, y, x + 32, y + 32, -1601138544);
                int s = mouseX - x;
                int t = mouseY - y;
                if (this.canConnect()) {
                    if (s < 32 && s > 16) {
                        context.drawGuiTexture(JOIN_HIGHLIGHTED_TEXTURE, x, y, 32, 32);
                    } else {
                        context.drawGuiTexture(JOIN_TEXTURE, x, y, 32, 32);
                    }
                }
                if (index > 0) {
                    if (s < 16 && t < 16) {
                        context.drawGuiTexture(MOVE_UP_HIGHLIGHTED_TEXTURE, x, y, 32, 32);
                    } else {
                        context.drawGuiTexture(MOVE_UP_TEXTURE, x, y, 32, 32);
                    }
                }
                if (index < this.screen.getServerList().size() - 1) {
                    if (s < 16 && t > 16) {
                        context.drawGuiTexture(MOVE_DOWN_HIGHLIGHTED_TEXTURE, x, y, 32, 32);
                    } else {
                        context.drawGuiTexture(MOVE_DOWN_TEXTURE, x, y, 32, 32);
                    }
                }
            }
        }

        private void update() {
            this.playerListSummary = null;
            switch (this.server.getStatus()) {
                case INITIAL: 
                case PINGING: {
                    this.statusIconTexture = PING_1_TEXTURE;
                    this.statusTooltipText = PINGING_TEXT;
                    break;
                }
                case INCOMPATIBLE: {
                    this.statusIconTexture = INCOMPATIBLE_TEXTURE;
                    this.statusTooltipText = INCOMPATIBLE_TEXT;
                    this.playerListSummary = this.server.playerListSummary;
                    break;
                }
                case UNREACHABLE: {
                    this.statusIconTexture = UNREACHABLE_TEXTURE;
                    this.statusTooltipText = NO_CONNECTION_TEXT;
                    break;
                }
                case SUCCESSFUL: {
                    this.statusIconTexture = this.server.ping < 150L ? PING_5_TEXTURE : (this.server.ping < 300L ? PING_4_TEXTURE : (this.server.ping < 600L ? PING_3_TEXTURE : (this.server.ping < 1000L ? PING_2_TEXTURE : PING_1_TEXTURE)));
                    this.statusTooltipText = Text.translatable("multiplayer.status.ping", this.server.ping);
                    this.playerListSummary = this.server.playerListSummary;
                }
            }
        }

        public void saveFile() {
            this.screen.getServerList().saveFile();
        }

        protected void draw(DrawContext context, int x, int y, Identifier textureId) {
            RenderSystem.enableBlend();
            context.drawTexture(textureId, x, y, 0.0f, 0.0f, 32, 32, 32, 32);
            RenderSystem.disableBlend();
        }

        private boolean canConnect() {
            return true;
        }

        private boolean uploadFavicon(@Nullable byte[] bytes) {
            if (bytes == null) {
                this.icon.destroy();
            } else {
                try {
                    this.icon.load(NativeImage.read(bytes));
                } catch (Throwable throwable) {
                    LOGGER.error("Invalid icon for server {} ({})", this.server.name, this.server.address, throwable);
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (Screen.hasShiftDown()) {
                MultiplayerServerListWidget lv = this.screen.serverListWidget;
                int l = lv.children().indexOf(this);
                if (l == -1) {
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_DOWN && l < this.screen.getServerList().size() - 1 || keyCode == GLFW.GLFW_KEY_UP && l > 0) {
                    this.swapEntries(l, keyCode == GLFW.GLFW_KEY_DOWN ? l + 1 : l - 1);
                    return true;
                }
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        private void swapEntries(int i, int j) {
            this.screen.getServerList().swapEntries(i, j);
            this.screen.serverListWidget.setServers(this.screen.getServerList());
            Entry lv = (Entry)this.screen.serverListWidget.children().get(j);
            this.screen.serverListWidget.setSelected(lv);
            MultiplayerServerListWidget.this.ensureVisible(lv);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            double f = mouseX - (double)MultiplayerServerListWidget.this.getRowLeft();
            double g = mouseY - (double)MultiplayerServerListWidget.this.getRowTop(MultiplayerServerListWidget.this.children().indexOf(this));
            if (f <= 32.0) {
                if (f < 32.0 && f > 16.0 && this.canConnect()) {
                    this.screen.select(this);
                    this.screen.connect();
                    return true;
                }
                int j = this.screen.serverListWidget.children().indexOf(this);
                if (f < 16.0 && g < 16.0 && j > 0) {
                    this.swapEntries(j, j - 1);
                    return true;
                }
                if (f < 16.0 && g > 16.0 && j < this.screen.getServerList().size() - 1) {
                    this.swapEntries(j, j + 1);
                    return true;
                }
            }
            this.screen.select(this);
            if (Util.getMeasuringTimeMs() - this.time < 250L) {
                this.screen.connect();
            }
            this.time = Util.getMeasuringTimeMs();
            return super.mouseClicked(mouseX, mouseY, button);
        }

        public ServerInfo getServer() {
            return this.server;
        }

        @Override
        public Text getNarration() {
            MutableText lv = Text.empty();
            lv.append(Text.translatable("narrator.select", this.server.name));
            lv.append(ScreenTexts.SENTENCE_SEPARATOR);
            switch (this.server.getStatus()) {
                case INCOMPATIBLE: {
                    lv.append(INCOMPATIBLE_TEXT);
                    lv.append(ScreenTexts.SENTENCE_SEPARATOR);
                    lv.append(Text.translatable("multiplayer.status.version.narration", this.server.version));
                    lv.append(ScreenTexts.SENTENCE_SEPARATOR);
                    lv.append(Text.translatable("multiplayer.status.motd.narration", this.server.label));
                    break;
                }
                case UNREACHABLE: {
                    lv.append(NO_CONNECTION_TEXT);
                    break;
                }
                case PINGING: {
                    lv.append(PINGING_TEXT);
                    break;
                }
                default: {
                    lv.append(ONLINE_TEXT);
                    lv.append(ScreenTexts.SENTENCE_SEPARATOR);
                    lv.append(Text.translatable("multiplayer.status.ping.narration", this.server.ping));
                    lv.append(ScreenTexts.SENTENCE_SEPARATOR);
                    lv.append(Text.translatable("multiplayer.status.motd.narration", this.server.label));
                    if (this.server.players == null) break;
                    lv.append(ScreenTexts.SENTENCE_SEPARATOR);
                    lv.append(Text.translatable("multiplayer.status.player_count.narration", this.server.players.online(), this.server.players.max()));
                    lv.append(ScreenTexts.SENTENCE_SEPARATOR);
                    lv.append(Texts.join(this.server.playerListSummary, Text.literal(", ")));
                }
            }
            return lv;
        }

        @Override
        public void close() {
            this.icon.close();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class LanServerEntry
    extends Entry {
        private static final int field_32386 = 32;
        private static final Text TITLE_TEXT = Text.translatable("lanServer.title");
        private static final Text HIDDEN_ADDRESS_TEXT = Text.translatable("selectServer.hiddenAddress");
        private final MultiplayerScreen screen;
        protected final MinecraftClient client;
        protected final LanServerInfo server;
        private long time;

        protected LanServerEntry(MultiplayerScreen screen, LanServerInfo server) {
            this.screen = screen;
            this.server = server;
            this.client = MinecraftClient.getInstance();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawText(this.client.textRenderer, TITLE_TEXT, x + 32 + 3, y + 1, 0xFFFFFF, false);
            context.drawText(this.client.textRenderer, this.server.getMotd(), x + 32 + 3, y + 12, Colors.GRAY, false);
            if (this.client.options.hideServerAddress) {
                context.drawText(this.client.textRenderer, HIDDEN_ADDRESS_TEXT, x + 32 + 3, y + 12 + 11, 0x303030, false);
            } else {
                context.drawText(this.client.textRenderer, this.server.getAddressPort(), x + 32 + 3, y + 12 + 11, 0x303030, false);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.screen.select(this);
            if (Util.getMeasuringTimeMs() - this.time < 250L) {
                this.screen.connect();
            }
            this.time = Util.getMeasuringTimeMs();
            return super.mouseClicked(mouseX, mouseY, button);
        }

        public LanServerInfo getLanServerEntry() {
            return this.server;
        }

        @Override
        public Text getNarration() {
            return Text.translatable("narrator.select", this.getMotdNarration());
        }

        public Text getMotdNarration() {
            return Text.empty().append(TITLE_TEXT).append(ScreenTexts.SPACE).append(this.server.getMotd());
        }
    }
}

