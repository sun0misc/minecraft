package net.minecraft.client.gui.screen.multiplayer;

import com.mojang.logging.LogUtils;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.AddServerScreen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.DirectConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AxisGridWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.network.LanServerInfo;
import net.minecraft.client.network.LanServerQueryManager;
import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class MultiplayerScreen extends Screen {
   public static final int field_41849 = 308;
   public static final int field_41850 = 100;
   public static final int field_41851 = 74;
   public static final int field_41852 = 64;
   private static final Logger LOGGER = LogUtils.getLogger();
   private final MultiplayerServerListPinger serverListPinger = new MultiplayerServerListPinger();
   private final Screen parent;
   protected MultiplayerServerListWidget serverListWidget;
   private ServerList serverList;
   private ButtonWidget buttonEdit;
   private ButtonWidget buttonJoin;
   private ButtonWidget buttonDelete;
   @Nullable
   private List multiplayerScreenTooltip;
   private ServerInfo selectedEntry;
   private LanServerQueryManager.LanServerEntryList lanServers;
   @Nullable
   private LanServerQueryManager.LanServerDetector lanServerDetector;
   private boolean initialized;

   public MultiplayerScreen(Screen parent) {
      super(Text.translatable("multiplayer.title"));
      this.parent = parent;
   }

   protected void init() {
      if (this.initialized) {
         this.serverListWidget.updateSize(this.width, this.height, 32, this.height - 64);
      } else {
         this.initialized = true;
         this.serverList = new ServerList(this.client);
         this.serverList.loadFile();
         this.lanServers = new LanServerQueryManager.LanServerEntryList();

         try {
            this.lanServerDetector = new LanServerQueryManager.LanServerDetector(this.lanServers);
            this.lanServerDetector.start();
         } catch (Exception var9) {
            LOGGER.warn("Unable to start LAN server detection: {}", var9.getMessage());
         }

         this.serverListWidget = new MultiplayerServerListWidget(this, this.client, this.width, this.height, 32, this.height - 64, 36);
         this.serverListWidget.setServers(this.serverList);
      }

      this.addSelectableChild(this.serverListWidget);
      this.buttonJoin = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectServer.select"), (button) -> {
         this.connect();
      }).width(100).build());
      ButtonWidget lv = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectServer.direct"), (button) -> {
         this.selectedEntry = new ServerInfo(I18n.translate("selectServer.defaultName"), "", false);
         this.client.setScreen(new DirectConnectScreen(this, this::directConnect, this.selectedEntry));
      }).width(100).build());
      ButtonWidget lv2 = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectServer.add"), (button) -> {
         this.selectedEntry = new ServerInfo(I18n.translate("selectServer.defaultName"), "", false);
         this.client.setScreen(new AddServerScreen(this, this::addEntry, this.selectedEntry));
      }).width(100).build());
      this.buttonEdit = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectServer.edit"), (button) -> {
         MultiplayerServerListWidget.Entry lv = (MultiplayerServerListWidget.Entry)this.serverListWidget.getSelectedOrNull();
         if (lv instanceof MultiplayerServerListWidget.ServerEntry) {
            ServerInfo lv2 = ((MultiplayerServerListWidget.ServerEntry)lv).getServer();
            this.selectedEntry = new ServerInfo(lv2.name, lv2.address, false);
            this.selectedEntry.copyWithSettingsFrom(lv2);
            this.client.setScreen(new AddServerScreen(this, this::editEntry, this.selectedEntry));
         }

      }).width(74).build());
      this.buttonDelete = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectServer.delete"), (button) -> {
         MultiplayerServerListWidget.Entry lv = (MultiplayerServerListWidget.Entry)this.serverListWidget.getSelectedOrNull();
         if (lv instanceof MultiplayerServerListWidget.ServerEntry) {
            String string = ((MultiplayerServerListWidget.ServerEntry)lv).getServer().name;
            if (string != null) {
               Text lv2 = Text.translatable("selectServer.deleteQuestion");
               Text lv3 = Text.translatable("selectServer.deleteWarning", string);
               Text lv4 = Text.translatable("selectServer.deleteButton");
               Text lv5 = ScreenTexts.CANCEL;
               this.client.setScreen(new ConfirmScreen(this::removeEntry, lv2, lv3, lv4, lv5));
            }
         }

      }).width(74).build());
      ButtonWidget lv3 = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectServer.refresh"), (button) -> {
         this.refresh();
      }).width(74).build());
      ButtonWidget lv4 = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
         this.client.setScreen(this.parent);
      }).width(74).build());
      GridWidget lv5 = new GridWidget();
      GridWidget.Adder lv6 = lv5.createAdder(1);
      AxisGridWidget lv7 = (AxisGridWidget)lv6.add(new AxisGridWidget(308, 20, AxisGridWidget.DisplayAxis.HORIZONTAL));
      lv7.add(this.buttonJoin);
      lv7.add(lv);
      lv7.add(lv2);
      lv6.add(EmptyWidget.ofHeight(4));
      AxisGridWidget lv8 = (AxisGridWidget)lv6.add(new AxisGridWidget(308, 20, AxisGridWidget.DisplayAxis.HORIZONTAL));
      lv8.add(this.buttonEdit);
      lv8.add(this.buttonDelete);
      lv8.add(lv3);
      lv8.add(lv4);
      lv5.refreshPositions();
      SimplePositioningWidget.setPos(lv5, 0, this.height - 64, this.width, 64);
      this.updateButtonActivationStates();
   }

   public void tick() {
      super.tick();
      List list = this.lanServers.getEntriesIfUpdated();
      if (list != null) {
         this.serverListWidget.setLanServers(list);
      }

      this.serverListPinger.tick();
   }

   public void removed() {
      if (this.lanServerDetector != null) {
         this.lanServerDetector.interrupt();
         this.lanServerDetector = null;
      }

      this.serverListPinger.cancel();
   }

   private void refresh() {
      this.client.setScreen(new MultiplayerScreen(this.parent));
   }

   private void removeEntry(boolean confirmedAction) {
      MultiplayerServerListWidget.Entry lv = (MultiplayerServerListWidget.Entry)this.serverListWidget.getSelectedOrNull();
      if (confirmedAction && lv instanceof MultiplayerServerListWidget.ServerEntry) {
         this.serverList.remove(((MultiplayerServerListWidget.ServerEntry)lv).getServer());
         this.serverList.saveFile();
         this.serverListWidget.setSelected((MultiplayerServerListWidget.Entry)null);
         this.serverListWidget.setServers(this.serverList);
      }

      this.client.setScreen(this);
   }

   private void editEntry(boolean confirmedAction) {
      MultiplayerServerListWidget.Entry lv = (MultiplayerServerListWidget.Entry)this.serverListWidget.getSelectedOrNull();
      if (confirmedAction && lv instanceof MultiplayerServerListWidget.ServerEntry) {
         ServerInfo lv2 = ((MultiplayerServerListWidget.ServerEntry)lv).getServer();
         lv2.name = this.selectedEntry.name;
         lv2.address = this.selectedEntry.address;
         lv2.copyWithSettingsFrom(this.selectedEntry);
         this.serverList.saveFile();
         this.serverListWidget.setServers(this.serverList);
      }

      this.client.setScreen(this);
   }

   private void addEntry(boolean confirmedAction) {
      if (confirmedAction) {
         ServerInfo lv = this.serverList.tryUnhide(this.selectedEntry.address);
         if (lv != null) {
            lv.copyFrom(this.selectedEntry);
            this.serverList.saveFile();
         } else {
            this.serverList.add(this.selectedEntry, false);
            this.serverList.saveFile();
         }

         this.serverListWidget.setSelected((MultiplayerServerListWidget.Entry)null);
         this.serverListWidget.setServers(this.serverList);
      }

      this.client.setScreen(this);
   }

   private void directConnect(boolean confirmedAction) {
      if (confirmedAction) {
         ServerInfo lv = this.serverList.get(this.selectedEntry.address);
         if (lv == null) {
            this.serverList.add(this.selectedEntry, true);
            this.serverList.saveFile();
            this.connect(this.selectedEntry);
         } else {
            this.connect(lv);
         }
      } else {
         this.client.setScreen(this);
      }

   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (super.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else if (keyCode == GLFW.GLFW_KEY_F5) {
         this.refresh();
         return true;
      } else if (this.serverListWidget.getSelectedOrNull() != null) {
         if (KeyCodes.isToggle(keyCode)) {
            this.connect();
            return true;
         } else {
            return this.serverListWidget.keyPressed(keyCode, scanCode, modifiers);
         }
      } else {
         return false;
      }
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.multiplayerScreenTooltip = null;
      this.renderBackground(matrices);
      this.serverListWidget.render(matrices, mouseX, mouseY, delta);
      drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 20, 16777215);
      super.render(matrices, mouseX, mouseY, delta);
      if (this.multiplayerScreenTooltip != null) {
         this.renderTooltip(matrices, this.multiplayerScreenTooltip, mouseX, mouseY);
      }

   }

   public void connect() {
      MultiplayerServerListWidget.Entry lv = (MultiplayerServerListWidget.Entry)this.serverListWidget.getSelectedOrNull();
      if (lv instanceof MultiplayerServerListWidget.ServerEntry) {
         this.connect(((MultiplayerServerListWidget.ServerEntry)lv).getServer());
      } else if (lv instanceof MultiplayerServerListWidget.LanServerEntry) {
         LanServerInfo lv2 = ((MultiplayerServerListWidget.LanServerEntry)lv).getLanServerEntry();
         this.connect(new ServerInfo(lv2.getMotd(), lv2.getAddressPort(), true));
      }

   }

   private void connect(ServerInfo entry) {
      ConnectScreen.connect(this, this.client, ServerAddress.parse(entry.address), entry, false);
   }

   public void select(MultiplayerServerListWidget.Entry entry) {
      this.serverListWidget.setSelected(entry);
      this.updateButtonActivationStates();
   }

   protected void updateButtonActivationStates() {
      this.buttonJoin.active = false;
      this.buttonEdit.active = false;
      this.buttonDelete.active = false;
      MultiplayerServerListWidget.Entry lv = (MultiplayerServerListWidget.Entry)this.serverListWidget.getSelectedOrNull();
      if (lv != null && !(lv instanceof MultiplayerServerListWidget.ScanningEntry)) {
         this.buttonJoin.active = true;
         if (lv instanceof MultiplayerServerListWidget.ServerEntry) {
            this.buttonEdit.active = true;
            this.buttonDelete.active = true;
         }
      }

   }

   public MultiplayerServerListPinger getServerListPinger() {
      return this.serverListPinger;
   }

   public void setMultiplayerScreenTooltip(List tooltip) {
      this.multiplayerScreenTooltip = tooltip;
   }

   public ServerList getServerList() {
      return this.serverList;
   }
}
