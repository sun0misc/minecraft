package net.minecraft.client.util;

import com.mojang.logging.LogUtils;
import com.mojang.text2speech.Narrator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.NarratorMode;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class NarratorManager {
   public static final Text EMPTY;
   private static final Logger LOGGER;
   private final MinecraftClient client;
   private final Narrator narrator = Narrator.getNarrator();

   public NarratorManager(MinecraftClient client) {
      this.client = client;
   }

   public void narrateChatMessage(Text text) {
      if (this.getNarratorMode().shouldNarrateChat()) {
         String string = text.getString();
         this.debugPrintMessage(string);
         this.narrator.say(string, false);
      }

   }

   public void narrateSystemMessage(Text text) {
      String string = text.getString();
      if (this.getNarratorMode().shouldNarrateSystem() && !string.isEmpty()) {
         this.debugPrintMessage(string);
         this.narrator.say(string, false);
      }

   }

   public void narrate(Text text) {
      this.narrate(text.getString());
   }

   public void narrate(String text) {
      if (this.getNarratorMode().shouldNarrateSystem() && !text.isEmpty()) {
         this.debugPrintMessage(text);
         if (this.narrator.active()) {
            this.narrator.clear();
            this.narrator.say(text, true);
         }
      }

   }

   private NarratorMode getNarratorMode() {
      return (NarratorMode)this.client.options.getNarrator().getValue();
   }

   private void debugPrintMessage(String message) {
      if (SharedConstants.isDevelopment) {
         LOGGER.debug("Narrating: {}", message.replaceAll("\n", "\\\\n"));
      }

   }

   public void onModeChange(NarratorMode mode) {
      this.clear();
      this.narrator.say(Text.translatable("options.narrator").append(" : ").append(mode.getName()).getString(), true);
      ToastManager lv = MinecraftClient.getInstance().getToastManager();
      if (this.narrator.active()) {
         if (mode == NarratorMode.OFF) {
            SystemToast.show(lv, SystemToast.Type.NARRATOR_TOGGLE, Text.translatable("narrator.toast.disabled"), (Text)null);
         } else {
            SystemToast.show(lv, SystemToast.Type.NARRATOR_TOGGLE, Text.translatable("narrator.toast.enabled"), mode.getName());
         }
      } else {
         SystemToast.show(lv, SystemToast.Type.NARRATOR_TOGGLE, Text.translatable("narrator.toast.disabled"), Text.translatable("options.narrator.notavailable"));
      }

   }

   public boolean isActive() {
      return this.narrator.active();
   }

   public void clear() {
      if (this.getNarratorMode() != NarratorMode.OFF && this.narrator.active()) {
         this.narrator.clear();
      }
   }

   public void destroy() {
      this.narrator.destroy();
   }

   static {
      EMPTY = ScreenTexts.EMPTY;
      LOGGER = LogUtils.getLogger();
   }
}
