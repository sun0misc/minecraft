package net.minecraft.client.gui.screen.multiplayer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.LoadingDisplay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.network.LanServerInfo;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class MultiplayerServerListWidget extends AlwaysSelectedEntryListWidget {
   static final Logger LOGGER = LogUtils.getLogger();
   static final ThreadPoolExecutor SERVER_PINGER_THREAD_POOL;
   static final Identifier UNKNOWN_SERVER_TEXTURE;
   static final Identifier SERVER_SELECTION_TEXTURE;
   static final Text LAN_SCANNING_TEXT;
   static final Text CANNOT_RESOLVE_TEXT;
   static final Text CANNOT_CONNECT_TEXT;
   static final Text INCOMPATIBLE_TEXT;
   static final Text NO_CONNECTION_TEXT;
   static final Text PINGING_TEXT;
   static final Text ONLINE_TEXT;
   private final MultiplayerScreen screen;
   private final List servers = Lists.newArrayList();
   private final Entry scanningEntry = new ScanningEntry();
   private final List lanServers = Lists.newArrayList();

   public MultiplayerServerListWidget(MultiplayerScreen screen, MinecraftClient client, int width, int height, int top, int bottom, int entryHeight) {
      super(client, width, height, top, bottom, entryHeight);
      this.screen = screen;
   }

   private void updateEntries() {
      this.clearEntries();
      this.servers.forEach((server) -> {
         this.addEntry(server);
      });
      this.addEntry(this.scanningEntry);
      this.lanServers.forEach((lanServer) -> {
         this.addEntry(lanServer);
      });
   }

   public void setSelected(@Nullable Entry arg) {
      super.setSelected(arg);
      this.screen.updateButtonActivationStates();
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      Entry lv = (Entry)this.getSelectedOrNull();
      return lv != null && lv.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
   }

   public void setServers(ServerList servers) {
      this.servers.clear();

      for(int i = 0; i < servers.size(); ++i) {
         this.servers.add(new ServerEntry(this.screen, servers.get(i)));
      }

      this.updateEntries();
   }

   public void setLanServers(List lanServers) {
      int i = lanServers.size() - this.lanServers.size();
      this.lanServers.clear();
      Iterator var3 = lanServers.iterator();

      while(var3.hasNext()) {
         LanServerInfo lv = (LanServerInfo)var3.next();
         this.lanServers.add(new LanServerEntry(this.screen, lv));
      }

      this.updateEntries();

      for(int j = this.lanServers.size() - i; j < this.lanServers.size(); ++j) {
         LanServerEntry lv2 = (LanServerEntry)this.lanServers.get(j);
         int k = j - this.lanServers.size() + this.children().size();
         int l = this.getRowTop(k);
         int m = this.getRowBottom(k);
         if (m >= this.top && l <= this.bottom) {
            this.client.getNarratorManager().narrateSystemMessage(Text.translatable("multiplayer.lan.server_found", lv2.getMotdNarration()));
         }
      }

   }

   protected int getScrollbarPositionX() {
      return super.getScrollbarPositionX() + 30;
   }

   public int getRowWidth() {
      return super.getRowWidth() + 85;
   }

   static {
      SERVER_PINGER_THREAD_POOL = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER)).build());
      UNKNOWN_SERVER_TEXTURE = new Identifier("textures/misc/unknown_server.png");
      SERVER_SELECTION_TEXTURE = new Identifier("textures/gui/server_selection.png");
      LAN_SCANNING_TEXT = Text.translatable("lanServer.scanning");
      CANNOT_RESOLVE_TEXT = Text.translatable("multiplayer.status.cannot_resolve").styled((style) -> {
         return style.withColor(-65536);
      });
      CANNOT_CONNECT_TEXT = Text.translatable("multiplayer.status.cannot_connect").styled((style) -> {
         return style.withColor(-65536);
      });
      INCOMPATIBLE_TEXT = Text.translatable("multiplayer.status.incompatible");
      NO_CONNECTION_TEXT = Text.translatable("multiplayer.status.no_connection");
      PINGING_TEXT = Text.translatable("multiplayer.status.pinging");
      ONLINE_TEXT = Text.translatable("multiplayer.status.online");
   }

   @Environment(EnvType.CLIENT)
   public static class ScanningEntry extends Entry {
      private final MinecraftClient client = MinecraftClient.getInstance();

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         int var10000 = y + entryHeight / 2;
         Objects.requireNonNull(this.client.textRenderer);
         int p = var10000 - 9 / 2;
         this.client.textRenderer.draw(matrices, MultiplayerServerListWidget.LAN_SCANNING_TEXT, (float)(this.client.currentScreen.width / 2 - this.client.textRenderer.getWidth((StringVisitable)MultiplayerServerListWidget.LAN_SCANNING_TEXT) / 2), (float)p, 16777215);
         String string = LoadingDisplay.get(Util.getMeasuringTimeMs());
         TextRenderer var13 = this.client.textRenderer;
         float var10003 = (float)(this.client.currentScreen.width / 2 - this.client.textRenderer.getWidth(string) / 2);
         Objects.requireNonNull(this.client.textRenderer);
         var13.draw(matrices, string, var10003, (float)(p + 9), 8421504);
      }

      public Text getNarration() {
         return MultiplayerServerListWidget.LAN_SCANNING_TEXT;
      }
   }

   @Environment(EnvType.CLIENT)
   public abstract static class Entry extends AlwaysSelectedEntryListWidget.Entry {
   }

   @Environment(EnvType.CLIENT)
   public class ServerEntry extends Entry {
      private static final int field_32387 = 32;
      private static final int field_32388 = 32;
      private static final int field_32389 = 0;
      private static final int field_32390 = 32;
      private static final int field_32391 = 64;
      private static final int field_32392 = 96;
      private static final int field_32393 = 0;
      private static final int field_32394 = 32;
      private final MultiplayerScreen screen;
      private final MinecraftClient client;
      private final ServerInfo server;
      private final Identifier iconTextureId;
      @Nullable
      private byte[] favicon;
      @Nullable
      private NativeImageBackedTexture icon;
      private long time;

      protected ServerEntry(MultiplayerScreen screen, ServerInfo server) {
         this.screen = screen;
         this.server = server;
         this.client = MinecraftClient.getInstance();
         this.iconTextureId = new Identifier("servers/" + Hashing.sha1().hashUnencodedChars(server.address) + "/icon");
         AbstractTexture lv = this.client.getTextureManager().getOrDefault(this.iconTextureId, MissingSprite.getMissingSpriteTexture());
         if (lv != MissingSprite.getMissingSpriteTexture() && lv instanceof NativeImageBackedTexture) {
            this.icon = (NativeImageBackedTexture)lv;
         }

      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         if (!this.server.online) {
            this.server.online = true;
            this.server.ping = -2L;
            this.server.label = ScreenTexts.EMPTY;
            this.server.playerCountLabel = ScreenTexts.EMPTY;
            MultiplayerServerListWidget.SERVER_PINGER_THREAD_POOL.submit(() -> {
               try {
                  this.screen.getServerListPinger().add(this.server, () -> {
                     this.client.execute(this::saveFile);
                  });
               } catch (UnknownHostException var2) {
                  this.server.ping = -1L;
                  this.server.label = MultiplayerServerListWidget.CANNOT_RESOLVE_TEXT;
               } catch (Exception var3) {
                  this.server.ping = -1L;
                  this.server.label = MultiplayerServerListWidget.CANNOT_CONNECT_TEXT;
               }

            });
         }

         boolean bl2 = !this.protocolVersionMatches();
         this.client.textRenderer.draw(matrices, this.server.name, (float)(x + 32 + 3), (float)(y + 1), 16777215);
         List list = this.client.textRenderer.wrapLines(this.server.label, entryWidth - 32 - 2);

         for(int p = 0; p < Math.min(list.size(), 2); ++p) {
            TextRenderer var10000 = this.client.textRenderer;
            OrderedText var10002 = (OrderedText)list.get(p);
            float var10003 = (float)(x + 32 + 3);
            int var10004 = y + 12;
            Objects.requireNonNull(this.client.textRenderer);
            var10000.draw(matrices, var10002, var10003, (float)(var10004 + 9 * p), 8421504);
         }

         Text lv = bl2 ? this.server.version.copy().formatted(Formatting.RED) : this.server.playerCountLabel;
         int q = this.client.textRenderer.getWidth((StringVisitable)lv);
         this.client.textRenderer.draw(matrices, (Text)lv, (float)(x + entryWidth - q - 15 - 2), (float)(y + 1), 8421504);
         int r = 0;
         int s;
         List list2;
         Object lv2;
         if (bl2) {
            s = 5;
            lv2 = MultiplayerServerListWidget.INCOMPATIBLE_TEXT;
            list2 = this.server.playerListSummary;
         } else if (this.pinged()) {
            if (this.server.ping < 0L) {
               s = 5;
            } else if (this.server.ping < 150L) {
               s = 0;
            } else if (this.server.ping < 300L) {
               s = 1;
            } else if (this.server.ping < 600L) {
               s = 2;
            } else if (this.server.ping < 1000L) {
               s = 3;
            } else {
               s = 4;
            }

            if (this.server.ping < 0L) {
               lv2 = MultiplayerServerListWidget.NO_CONNECTION_TEXT;
               list2 = Collections.emptyList();
            } else {
               lv2 = Text.translatable("multiplayer.status.ping", this.server.ping);
               list2 = this.server.playerListSummary;
            }
         } else {
            r = 1;
            s = (int)(Util.getMeasuringTimeMs() / 100L + (long)(index * 2) & 7L);
            if (s > 4) {
               s = 8 - s;
            }

            lv2 = MultiplayerServerListWidget.PINGING_TEXT;
            list2 = Collections.emptyList();
         }

         RenderSystem.setShaderTexture(0, DrawableHelper.GUI_ICONS_TEXTURE);
         DrawableHelper.drawTexture(matrices, x + entryWidth - 15, y, (float)(r * 10), (float)(176 + s * 8), 10, 8, 256, 256);
         byte[] bs = this.server.getFavicon();
         if (!Arrays.equals(bs, this.favicon)) {
            if (this.uploadFavicon(bs)) {
               this.favicon = bs;
            } else {
               this.server.setFavicon((byte[])null);
               this.saveFile();
            }
         }

         if (this.icon == null) {
            this.draw(matrices, x, y, MultiplayerServerListWidget.UNKNOWN_SERVER_TEXTURE);
         } else {
            this.draw(matrices, x, y, this.iconTextureId);
         }

         int t = mouseX - x;
         int u = mouseY - y;
         if (t >= entryWidth - 15 && t <= entryWidth - 5 && u >= 0 && u <= 8) {
            this.screen.setMultiplayerScreenTooltip(Collections.singletonList(lv2));
         } else if (t >= entryWidth - q - 15 - 2 && t <= entryWidth - 15 - 2 && u >= 0 && u <= 8) {
            this.screen.setMultiplayerScreenTooltip(list2);
         }

         if ((Boolean)this.client.options.getTouchscreen().getValue() || hovered) {
            RenderSystem.setShaderTexture(0, MultiplayerServerListWidget.SERVER_SELECTION_TEXTURE);
            DrawableHelper.fill(matrices, x, y, x + 32, y + 32, -1601138544);
            int v = mouseX - x;
            int w = mouseY - y;
            if (this.canConnect()) {
               if (v < 32 && v > 16) {
                  DrawableHelper.drawTexture(matrices, x, y, 0.0F, 32.0F, 32, 32, 256, 256);
               } else {
                  DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 32, 32, 256, 256);
               }
            }

            if (index > 0) {
               if (v < 16 && w < 16) {
                  DrawableHelper.drawTexture(matrices, x, y, 96.0F, 32.0F, 32, 32, 256, 256);
               } else {
                  DrawableHelper.drawTexture(matrices, x, y, 96.0F, 0.0F, 32, 32, 256, 256);
               }
            }

            if (index < this.screen.getServerList().size() - 1) {
               if (v < 16 && w > 16) {
                  DrawableHelper.drawTexture(matrices, x, y, 64.0F, 32.0F, 32, 32, 256, 256);
               } else {
                  DrawableHelper.drawTexture(matrices, x, y, 64.0F, 0.0F, 32, 32, 256, 256);
               }
            }
         }

      }

      private boolean pinged() {
         return this.server.online && this.server.ping != -2L;
      }

      private boolean protocolVersionMatches() {
         return this.server.protocolVersion == SharedConstants.getGameVersion().getProtocolVersion();
      }

      public void saveFile() {
         this.screen.getServerList().saveFile();
      }

      protected void draw(MatrixStack matrices, int x, int y, Identifier textureId) {
         RenderSystem.setShaderTexture(0, textureId);
         RenderSystem.enableBlend();
         DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
         RenderSystem.disableBlend();
      }

      private boolean canConnect() {
         return true;
      }

      private boolean uploadFavicon(@Nullable byte[] favicon) {
         if (favicon == null) {
            this.client.getTextureManager().destroyTexture(this.iconTextureId);
            if (this.icon != null && this.icon.getImage() != null) {
               this.icon.getImage().close();
            }

            this.icon = null;
         } else {
            try {
               NativeImage lv = NativeImage.read(favicon);
               Preconditions.checkState(lv.getWidth() == 64, "Must be 64 pixels wide");
               Preconditions.checkState(lv.getHeight() == 64, "Must be 64 pixels high");
               if (this.icon == null) {
                  this.icon = new NativeImageBackedTexture(lv);
               } else {
                  this.icon.setImage(lv);
                  this.icon.upload();
               }

               this.client.getTextureManager().registerTexture(this.iconTextureId, this.icon);
            } catch (Throwable var3) {
               MultiplayerServerListWidget.LOGGER.error("Invalid icon for server {} ({})", new Object[]{this.server.name, this.server.address, var3});
               return false;
            }
         }

         return true;
      }

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
         return true;
      }

      public ServerInfo getServer() {
         return this.server;
      }

      public Text getNarration() {
         MutableText lv = Text.empty();
         lv.append((Text)Text.translatable("narrator.select", this.server.name));
         lv.append(ScreenTexts.SENTENCE_SEPARATOR);
         if (!this.protocolVersionMatches()) {
            lv.append(MultiplayerServerListWidget.INCOMPATIBLE_TEXT);
            lv.append(ScreenTexts.SENTENCE_SEPARATOR);
            lv.append((Text)Text.translatable("multiplayer.status.version.narration", this.server.version));
            lv.append(ScreenTexts.SENTENCE_SEPARATOR);
            lv.append((Text)Text.translatable("multiplayer.status.motd.narration", this.server.label));
         } else if (this.server.ping < 0L) {
            lv.append(MultiplayerServerListWidget.NO_CONNECTION_TEXT);
         } else if (!this.pinged()) {
            lv.append(MultiplayerServerListWidget.PINGING_TEXT);
         } else {
            lv.append(MultiplayerServerListWidget.ONLINE_TEXT);
            lv.append(ScreenTexts.SENTENCE_SEPARATOR);
            lv.append((Text)Text.translatable("multiplayer.status.ping.narration", this.server.ping));
            lv.append(ScreenTexts.SENTENCE_SEPARATOR);
            lv.append((Text)Text.translatable("multiplayer.status.motd.narration", this.server.label));
            if (this.server.players != null) {
               lv.append(ScreenTexts.SENTENCE_SEPARATOR);
               lv.append((Text)Text.translatable("multiplayer.status.player_count.narration", this.server.players.online(), this.server.players.max()));
               lv.append(ScreenTexts.SENTENCE_SEPARATOR);
               lv.append(Texts.join(this.server.playerListSummary, (Text)Text.literal(", ")));
            }
         }

         return lv;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class LanServerEntry extends Entry {
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

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         this.client.textRenderer.draw(matrices, TITLE_TEXT, (float)(x + 32 + 3), (float)(y + 1), 16777215);
         this.client.textRenderer.draw(matrices, this.server.getMotd(), (float)(x + 32 + 3), (float)(y + 12), 8421504);
         if (this.client.options.hideServerAddress) {
            this.client.textRenderer.draw(matrices, HIDDEN_ADDRESS_TEXT, (float)(x + 32 + 3), (float)(y + 12 + 11), 3158064);
         } else {
            this.client.textRenderer.draw(matrices, this.server.getAddressPort(), (float)(x + 32 + 3), (float)(y + 12 + 11), 3158064);
         }

      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         this.screen.select(this);
         if (Util.getMeasuringTimeMs() - this.time < 250L) {
            this.screen.connect();
         }

         this.time = Util.getMeasuringTimeMs();
         return false;
      }

      public LanServerInfo getLanServerEntry() {
         return this.server;
      }

      public Text getNarration() {
         return Text.translatable("narrator.select", this.getMotdNarration());
      }

      public Text getMotdNarration() {
         return Text.empty().append(TITLE_TEXT).append(ScreenTexts.SPACE).append(this.server.getMotd());
      }
   }
}
