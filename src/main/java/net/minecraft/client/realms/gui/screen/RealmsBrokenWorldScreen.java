package net.minecraft.client.realms.gui.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsWorldOptions;
import net.minecraft.client.realms.dto.WorldDownload;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.gui.RealmsWorldSlotButton;
import net.minecraft.client.realms.task.OpenServerTask;
import net.minecraft.client.realms.task.SwitchSlotTask;
import net.minecraft.client.realms.util.RealmsTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsBrokenWorldScreen extends RealmsScreen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int field_32120 = 80;
   private final Screen parent;
   private final RealmsMainScreen mainScreen;
   @Nullable
   private RealmsServer serverData;
   private final long serverId;
   private final Text[] message = new Text[]{Text.translatable("mco.brokenworld.message.line1"), Text.translatable("mco.brokenworld.message.line2")};
   private int left_x;
   private int right_x;
   private final List slotsThatHasBeenDownloaded = Lists.newArrayList();
   private int animTick;

   public RealmsBrokenWorldScreen(Screen parent, RealmsMainScreen mainScreen, long serverId, boolean minigame) {
      super(minigame ? Text.translatable("mco.brokenworld.minigame.title") : Text.translatable("mco.brokenworld.title"));
      this.parent = parent;
      this.mainScreen = mainScreen;
      this.serverId = serverId;
   }

   public void init() {
      this.left_x = this.width / 2 - 150;
      this.right_x = this.width / 2 + 190;
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
         this.backButtonClicked();
      }).dimensions(this.right_x - 80 + 8, row(13) - 5, 70, 20).build());
      if (this.serverData == null) {
         this.fetchServerData(this.serverId);
      } else {
         this.addButtons();
      }

   }

   public Text getNarratedTitle() {
      return Texts.join((Collection)Stream.concat(Stream.of(this.title), Stream.of(this.message)).collect(Collectors.toList()), ScreenTexts.SPACE);
   }

   private void addButtons() {
      Iterator var1 = this.serverData.slots.entrySet().iterator();

      while(var1.hasNext()) {
         Map.Entry entry = (Map.Entry)var1.next();
         int i = (Integer)entry.getKey();
         boolean bl = i != this.serverData.activeSlot || this.serverData.worldType == RealmsServer.WorldType.MINIGAME;
         ButtonWidget lv;
         if (bl) {
            lv = ButtonWidget.builder(Text.translatable("mco.brokenworld.play"), (button) -> {
               if (((RealmsWorldOptions)this.serverData.slots.get(i)).empty) {
                  RealmsResetWorldScreen lv = new RealmsResetWorldScreen(this, this.serverData, Text.translatable("mco.configure.world.switch.slot"), Text.translatable("mco.configure.world.switch.slot.subtitle"), 10526880, ScreenTexts.CANCEL, this::play, () -> {
                     this.client.setScreen(this);
                     this.play();
                  });
                  lv.setSlot(i);
                  lv.setResetTitle(Text.translatable("mco.create.world.reset.title"));
                  this.client.setScreen(lv);
               } else {
                  this.client.setScreen(new RealmsLongRunningMcoTaskScreen(this.parent, new SwitchSlotTask(this.serverData.id, i, this::play)));
               }

            }).dimensions(this.getFramePositionX(i), row(8), 80, 20).build();
         } else {
            lv = ButtonWidget.builder(Text.translatable("mco.brokenworld.download"), (button) -> {
               Text lv = Text.translatable("mco.configure.world.restore.download.question.line1");
               Text lv2 = Text.translatable("mco.configure.world.restore.download.question.line2");
               this.client.setScreen(new RealmsLongConfirmationScreen((confirmed) -> {
                  if (confirmed) {
                     this.downloadWorld(i);
                  } else {
                     this.client.setScreen(this);
                  }

               }, RealmsLongConfirmationScreen.Type.INFO, lv, lv2, true));
            }).dimensions(this.getFramePositionX(i), row(8), 80, 20).build();
         }

         if (this.slotsThatHasBeenDownloaded.contains(i)) {
            lv.active = false;
            lv.setMessage(Text.translatable("mco.brokenworld.downloaded"));
         }

         this.addDrawableChild(lv);
         this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.brokenworld.reset"), (button) -> {
            RealmsResetWorldScreen lv = new RealmsResetWorldScreen(this, this.serverData, this::play, () -> {
               this.client.setScreen(this);
               this.play();
            });
            if (i != this.serverData.activeSlot || this.serverData.worldType == RealmsServer.WorldType.MINIGAME) {
               lv.setSlot(i);
            }

            this.client.setScreen(lv);
         }).dimensions(this.getFramePositionX(i), row(10), 80, 20).build());
      }

   }

   public void tick() {
      ++this.animTick;
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      super.render(matrices, mouseX, mouseY, delta);
      drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 17, 16777215);

      for(int k = 0; k < this.message.length; ++k) {
         drawCenteredTextWithShadow(matrices, this.textRenderer, this.message[k], this.width / 2, row(-1) + 3 + k * 12, 10526880);
      }

      if (this.serverData != null) {
         Iterator var7 = this.serverData.slots.entrySet().iterator();

         while(true) {
            while(var7.hasNext()) {
               Map.Entry entry = (Map.Entry)var7.next();
               if (((RealmsWorldOptions)entry.getValue()).templateImage != null && ((RealmsWorldOptions)entry.getValue()).templateId != -1L) {
                  this.drawSlotFrame(matrices, this.getFramePositionX((Integer)entry.getKey()), row(1) + 5, mouseX, mouseY, this.serverData.activeSlot == (Integer)entry.getKey() && !this.isMinigame(), ((RealmsWorldOptions)entry.getValue()).getSlotName((Integer)entry.getKey()), (Integer)entry.getKey(), ((RealmsWorldOptions)entry.getValue()).templateId, ((RealmsWorldOptions)entry.getValue()).templateImage, ((RealmsWorldOptions)entry.getValue()).empty);
               } else {
                  this.drawSlotFrame(matrices, this.getFramePositionX((Integer)entry.getKey()), row(1) + 5, mouseX, mouseY, this.serverData.activeSlot == (Integer)entry.getKey() && !this.isMinigame(), ((RealmsWorldOptions)entry.getValue()).getSlotName((Integer)entry.getKey()), (Integer)entry.getKey(), -1L, (String)null, ((RealmsWorldOptions)entry.getValue()).empty);
               }
            }

            return;
         }
      }
   }

   private int getFramePositionX(int i) {
      return this.left_x + (i - 1) * 110;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
         this.backButtonClicked();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   private void backButtonClicked() {
      this.client.setScreen(this.parent);
   }

   private void fetchServerData(long worldId) {
      (new Thread(() -> {
         RealmsClient lv = RealmsClient.create();

         try {
            this.serverData = lv.getOwnWorld(worldId);
            this.addButtons();
         } catch (RealmsServiceException var5) {
            LOGGER.error("Couldn't get own world");
            this.client.setScreen(new RealmsGenericErrorScreen(Text.of(var5.getMessage()), this.parent));
         }

      })).start();
   }

   public void play() {
      (new Thread(() -> {
         RealmsClient lv = RealmsClient.create();
         if (this.serverData.state == RealmsServer.State.CLOSED) {
            this.client.execute(() -> {
               this.client.setScreen(new RealmsLongRunningMcoTaskScreen(this, new OpenServerTask(this.serverData, this, this.mainScreen, true, this.client)));
            });
         } else {
            try {
               RealmsServer lv2 = lv.getOwnWorld(this.serverId);
               this.client.execute(() -> {
                  this.mainScreen.newScreen().play(lv2, this);
               });
            } catch (RealmsServiceException var3) {
               LOGGER.error("Couldn't get own world");
               this.client.execute(() -> {
                  this.client.setScreen(this.parent);
               });
            }
         }

      })).start();
   }

   private void downloadWorld(int slotId) {
      RealmsClient lv = RealmsClient.create();

      try {
         WorldDownload lv2 = lv.download(this.serverData.id, slotId);
         RealmsDownloadLatestWorldScreen lv3 = new RealmsDownloadLatestWorldScreen(this, lv2, this.serverData.getWorldName(slotId), (successful) -> {
            if (successful) {
               this.slotsThatHasBeenDownloaded.add(slotId);
               this.clearChildren();
               this.addButtons();
            } else {
               this.client.setScreen(this);
            }

         });
         this.client.setScreen(lv3);
      } catch (RealmsServiceException var5) {
         LOGGER.error("Couldn't download world data");
         this.client.setScreen(new RealmsGenericErrorScreen(var5, this));
      }

   }

   private boolean isMinigame() {
      return this.serverData != null && this.serverData.worldType == RealmsServer.WorldType.MINIGAME;
   }

   private void drawSlotFrame(MatrixStack matrices, int x, int y, int mouseX, int mouseY, boolean activeSlot, String slotName, int slotId, long templateId, @Nullable String templateImage, boolean empty) {
      if (empty) {
         RenderSystem.setShaderTexture(0, RealmsWorldSlotButton.EMPTY_FRAME);
      } else if (templateImage != null && templateId != -1L) {
         RenderSystem.setShaderTexture(0, RealmsTextureManager.getTextureId(String.valueOf(templateId), templateImage));
      } else if (slotId == 1) {
         RenderSystem.setShaderTexture(0, RealmsWorldSlotButton.PANORAMA_0);
      } else if (slotId == 2) {
         RenderSystem.setShaderTexture(0, RealmsWorldSlotButton.PANORAMA_2);
      } else if (slotId == 3) {
         RenderSystem.setShaderTexture(0, RealmsWorldSlotButton.PANORAMA_3);
      } else {
         RenderSystem.setShaderTexture(0, RealmsTextureManager.getTextureId(String.valueOf(this.serverData.minigameId), this.serverData.minigameImage));
      }

      if (!activeSlot) {
         RenderSystem.setShaderColor(0.56F, 0.56F, 0.56F, 1.0F);
      } else if (activeSlot) {
         float f = 0.9F + 0.1F * MathHelper.cos((float)this.animTick * 0.2F);
         RenderSystem.setShaderColor(f, f, f, 1.0F);
      }

      DrawableHelper.drawTexture(matrices, x + 3, y + 3, 0.0F, 0.0F, 74, 74, 74, 74);
      RenderSystem.setShaderTexture(0, RealmsWorldSlotButton.SLOT_FRAME);
      if (activeSlot) {
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      } else {
         RenderSystem.setShaderColor(0.56F, 0.56F, 0.56F, 1.0F);
      }

      DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 80, 80, 80, 80);
      drawCenteredTextWithShadow(matrices, this.textRenderer, slotName, x + 40, y + 66, 16777215);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }
}
