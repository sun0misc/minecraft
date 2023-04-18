package net.minecraft.client.realms.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.util.JsonUtils;
import net.minecraft.text.Text;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsNotification {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String NOTIFICATION_UUID_KEY = "notificationUuid";
   private static final String DISMISSABLE_KEY = "dismissable";
   private static final String SEEN_KEY = "seen";
   private static final String TYPE_KEY = "type";
   private static final String VISIT_URL_TYPE = "visitUrl";
   final UUID uuid;
   final boolean dismissable;
   final boolean seen;
   final String type;

   RealmsNotification(UUID uuid, boolean dismissable, boolean seen, String type) {
      this.uuid = uuid;
      this.dismissable = dismissable;
      this.seen = seen;
      this.type = type;
   }

   public boolean isSeen() {
      return this.seen;
   }

   public boolean isDismissable() {
      return this.dismissable;
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public static List parse(String json) {
      List list = new ArrayList();

      try {
         JsonArray jsonArray = JsonParser.parseString(json).getAsJsonObject().get("notifications").getAsJsonArray();
         Iterator var3 = jsonArray.iterator();

         while(var3.hasNext()) {
            JsonElement jsonElement = (JsonElement)var3.next();
            list.add(fromJson(jsonElement.getAsJsonObject()));
         }
      } catch (Exception var5) {
         LOGGER.error("Could not parse list of RealmsNotifications", var5);
      }

      return list;
   }

   private static RealmsNotification fromJson(JsonObject json) {
      UUID uUID = JsonUtils.getUuidOr("notificationUuid", json, (UUID)null);
      if (uUID == null) {
         throw new IllegalStateException("Missing required property notificationUuid");
      } else {
         boolean bl = JsonUtils.getBooleanOr("dismissable", json, true);
         boolean bl2 = JsonUtils.getBooleanOr("seen", json, false);
         String string = JsonUtils.getString("type", json);
         RealmsNotification lv = new RealmsNotification(uUID, bl, bl2, string);
         return (RealmsNotification)("visitUrl".equals(string) ? RealmsNotification.VisitUrl.fromJson(lv, json) : lv);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class VisitUrl extends RealmsNotification {
      private static final String URL_KEY = "url";
      private static final String BUTTON_TEXT_KEY = "buttonText";
      private static final String MESSAGE_KEY = "message";
      private final String url;
      private final RealmsText buttonText;
      private final RealmsText message;

      private VisitUrl(RealmsNotification parent, String url, RealmsText buttonText, RealmsText message) {
         super(parent.uuid, parent.dismissable, parent.seen, parent.type);
         this.url = url;
         this.buttonText = buttonText;
         this.message = message;
      }

      public static VisitUrl fromJson(RealmsNotification parent, JsonObject json) {
         String string = JsonUtils.getString("url", json);
         RealmsText lv = (RealmsText)JsonUtils.get("buttonText", json, RealmsText::fromJson);
         RealmsText lv2 = (RealmsText)JsonUtils.get("message", json, RealmsText::fromJson);
         return new VisitUrl(parent, string, lv, lv2);
      }

      public Text getDefaultMessage() {
         return this.message.toText(Text.translatable("mco.notification.visitUrl.message.default"));
      }

      public ButtonWidget createButton(Screen currentScreen) {
         Text lv = this.buttonText.toText(Text.translatable("mco.notification.visitUrl.buttonText.default"));
         return ButtonWidget.builder(lv, ConfirmLinkScreen.opening(this.url, currentScreen, true)).build();
      }
   }
}
