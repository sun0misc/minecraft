package net.minecraft.client.gui.screen.multiplayer;

import com.google.common.base.Strings;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.report.log.ChatLog;
import net.minecraft.client.report.log.ChatLogEntry;
import net.minecraft.client.report.log.ReceivedMessage;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SocialInteractionsPlayerListWidget extends ElementListWidget {
   private final SocialInteractionsScreen parent;
   private final List players = Lists.newArrayList();
   @Nullable
   private String currentSearch;

   public SocialInteractionsPlayerListWidget(SocialInteractionsScreen parent, MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
      super(client, width, height, top, bottom, itemHeight);
      this.parent = parent;
      this.setRenderBackground(false);
      this.setRenderHorizontalShadows(false);
   }

   protected void enableScissor() {
      enableScissor(this.left, this.top + 4, this.right, this.bottom);
   }

   public void update(Collection uuids, double scrollAmount, boolean includeOffline) {
      Map map = new HashMap();
      this.setPlayers(uuids, map);
      this.markOfflineMembers(map, includeOffline);
      this.refresh(map.values(), scrollAmount);
   }

   private void setPlayers(Collection playerUuids, Map entriesByUuids) {
      ClientPlayNetworkHandler lv = this.client.player.networkHandler;
      Iterator var4 = playerUuids.iterator();

      while(var4.hasNext()) {
         UUID uUID = (UUID)var4.next();
         PlayerListEntry lv2 = lv.getPlayerListEntry(uUID);
         if (lv2 != null) {
            boolean bl = lv2.hasPublicKey();
            MinecraftClient var10004 = this.client;
            SocialInteractionsScreen var10005 = this.parent;
            String var10007 = lv2.getProfile().getName();
            Objects.requireNonNull(lv2);
            entriesByUuids.put(uUID, new SocialInteractionsPlayerListEntry(var10004, var10005, uUID, var10007, lv2::getSkinTexture, bl));
         }
      }

   }

   private void markOfflineMembers(Map entries, boolean includeOffline) {
      Collection collection = collectReportableProfiles(this.client.getAbuseReportContext().getChatLog());
      Iterator var4 = collection.iterator();

      while(true) {
         SocialInteractionsPlayerListEntry lv;
         do {
            if (!var4.hasNext()) {
               return;
            }

            GameProfile gameProfile = (GameProfile)var4.next();
            if (includeOffline) {
               lv = (SocialInteractionsPlayerListEntry)entries.computeIfAbsent(gameProfile.getId(), (uuid) -> {
                  SocialInteractionsPlayerListEntry lv = new SocialInteractionsPlayerListEntry(this.client, this.parent, gameProfile.getId(), gameProfile.getName(), Suppliers.memoize(() -> {
                     return this.client.getSkinProvider().loadSkin(gameProfile);
                  }), true);
                  lv.setOffline(true);
                  return lv;
               });
               break;
            }

            lv = (SocialInteractionsPlayerListEntry)entries.get(gameProfile.getId());
         } while(lv == null);

         lv.setSentMessage(true);
      }
   }

   private static Collection collectReportableProfiles(ChatLog log) {
      Set set = new ObjectLinkedOpenHashSet();

      for(int i = log.getMaxIndex(); i >= log.getMinIndex(); --i) {
         ChatLogEntry lv = log.get(i);
         if (lv instanceof ReceivedMessage.ChatMessage lv2) {
            if (lv2.message().hasSignature()) {
               set.add(lv2.profile());
            }
         }
      }

      return set;
   }

   private void sortPlayers() {
      this.players.sort(Comparator.comparing((player) -> {
         if (player.getUuid().equals(this.client.getSession().getUuidOrNull())) {
            return 0;
         } else if (player.getUuid().version() == 2) {
            return 4;
         } else if (this.client.getAbuseReportContext().draftPlayerUuidEquals(player.getUuid())) {
            return 1;
         } else {
            return player.hasSentMessage() ? 2 : 3;
         }
      }).thenComparing((player) -> {
         if (!player.getName().isBlank()) {
            int i = player.getName().codePointAt(0);
            if (i == 95 || i >= 97 && i <= 122 || i >= 65 && i <= 90 || i >= 48 && i <= 57) {
               return 0;
            }
         }

         return 1;
      }).thenComparing(SocialInteractionsPlayerListEntry::getName, String::compareToIgnoreCase));
   }

   private void refresh(Collection players, double scrollAmount) {
      this.players.clear();
      this.players.addAll(players);
      this.sortPlayers();
      this.filterPlayers();
      this.replaceEntries(this.players);
      this.setScrollAmount(scrollAmount);
   }

   private void filterPlayers() {
      if (this.currentSearch != null) {
         this.players.removeIf((player) -> {
            return !player.getName().toLowerCase(Locale.ROOT).contains(this.currentSearch);
         });
         this.replaceEntries(this.players);
      }

   }

   public void setCurrentSearch(String currentSearch) {
      this.currentSearch = currentSearch;
   }

   public boolean isEmpty() {
      return this.players.isEmpty();
   }

   public void setPlayerOnline(PlayerListEntry player, SocialInteractionsScreen.Tab tab) {
      UUID uUID = player.getProfile().getId();
      Iterator var4 = this.players.iterator();

      SocialInteractionsPlayerListEntry lv;
      while(var4.hasNext()) {
         lv = (SocialInteractionsPlayerListEntry)var4.next();
         if (lv.getUuid().equals(uUID)) {
            lv.setOffline(false);
            return;
         }
      }

      if ((tab == SocialInteractionsScreen.Tab.ALL || this.client.getSocialInteractionsManager().isPlayerMuted(uUID)) && (Strings.isNullOrEmpty(this.currentSearch) || player.getProfile().getName().toLowerCase(Locale.ROOT).contains(this.currentSearch))) {
         boolean bl = player.hasPublicKey();
         MinecraftClient var10002 = this.client;
         SocialInteractionsScreen var10003 = this.parent;
         UUID var10004 = player.getProfile().getId();
         String var10005 = player.getProfile().getName();
         Objects.requireNonNull(player);
         lv = new SocialInteractionsPlayerListEntry(var10002, var10003, var10004, var10005, player::getSkinTexture, bl);
         this.addEntry(lv);
         this.players.add(lv);
      }

   }

   public void setPlayerOffline(UUID uuid) {
      Iterator var2 = this.players.iterator();

      SocialInteractionsPlayerListEntry lv;
      do {
         if (!var2.hasNext()) {
            return;
         }

         lv = (SocialInteractionsPlayerListEntry)var2.next();
      } while(!lv.getUuid().equals(uuid));

      lv.setOffline(true);
   }
}
