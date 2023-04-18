package net.minecraft.client.network;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientAdvancementManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final MinecraftClient client;
   private final AdvancementManager manager = new AdvancementManager();
   private final Map advancementProgresses = Maps.newHashMap();
   @Nullable
   private Listener listener;
   @Nullable
   private Advancement selectedTab;

   public ClientAdvancementManager(MinecraftClient client) {
      this.client = client;
   }

   public void onAdvancements(AdvancementUpdateS2CPacket packet) {
      if (packet.shouldClearCurrent()) {
         this.manager.clear();
         this.advancementProgresses.clear();
      }

      this.manager.removeAll(packet.getAdvancementIdsToRemove());
      this.manager.load(packet.getAdvancementsToEarn());
      Iterator var2 = packet.getAdvancementsToProgress().entrySet().iterator();

      while(var2.hasNext()) {
         Map.Entry entry = (Map.Entry)var2.next();
         Advancement lv = this.manager.get((Identifier)entry.getKey());
         if (lv != null) {
            AdvancementProgress lv2 = (AdvancementProgress)entry.getValue();
            lv2.init(lv.getCriteria(), lv.getRequirements());
            this.advancementProgresses.put(lv, lv2);
            if (this.listener != null) {
               this.listener.setProgress(lv, lv2);
            }

            if (!packet.shouldClearCurrent() && lv2.isDone() && lv.getDisplay() != null && lv.getDisplay().shouldShowToast()) {
               this.client.getToastManager().add(new AdvancementToast(lv));
            }
         } else {
            LOGGER.warn("Server informed client about progress for unknown advancement {}", entry.getKey());
         }
      }

   }

   public AdvancementManager getManager() {
      return this.manager;
   }

   public void selectTab(@Nullable Advancement tab, boolean local) {
      ClientPlayNetworkHandler lv = this.client.getNetworkHandler();
      if (lv != null && tab != null && local) {
         lv.sendPacket(AdvancementTabC2SPacket.open(tab));
      }

      if (this.selectedTab != tab) {
         this.selectedTab = tab;
         if (this.listener != null) {
            this.listener.selectTab(tab);
         }
      }

   }

   public void setListener(@Nullable Listener listener) {
      this.listener = listener;
      this.manager.setListener(listener);
      if (listener != null) {
         Iterator var2 = this.advancementProgresses.entrySet().iterator();

         while(var2.hasNext()) {
            Map.Entry entry = (Map.Entry)var2.next();
            listener.setProgress((Advancement)entry.getKey(), (AdvancementProgress)entry.getValue());
         }

         listener.selectTab(this.selectedTab);
      }

   }

   @Environment(EnvType.CLIENT)
   public interface Listener extends AdvancementManager.Listener {
      void setProgress(Advancement advancement, AdvancementProgress progress);

      void selectTab(@Nullable Advancement advancement);
   }
}
