package net.minecraft.client.util;

import com.mojang.authlib.minecraft.BanDetails;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.time.Duration;
import java.time.Instant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class Bans {
   private static final Text TEMPORARY_TITLE;
   private static final Text PERMANENT_TITLE;

   public static ConfirmLinkScreen createBanScreen(BooleanConsumer callback, BanDetails banDetails) {
      return new ConfirmLinkScreen(callback, getTitle(banDetails), getDescriptionText(banDetails), "https://aka.ms/mcjavamoderation", ScreenTexts.ACKNOWLEDGE, true);
   }

   private static Text getTitle(BanDetails banDetails) {
      return isTemporary(banDetails) ? TEMPORARY_TITLE : PERMANENT_TITLE;
   }

   private static Text getDescriptionText(BanDetails banDetails) {
      return Text.translatable("gui.banned.description", getReasonText(banDetails), getDurationText(banDetails), Text.literal("https://aka.ms/mcjavamoderation"));
   }

   private static Text getReasonText(BanDetails banDetails) {
      String string = banDetails.reason();
      String string2 = banDetails.reasonMessage();
      if (StringUtils.isNumeric(string)) {
         int i = Integer.parseInt(string);
         BanReason lv = BanReason.byId(i);
         MutableText lv2;
         if (lv != null) {
            lv2 = Texts.setStyleIfAbsent(lv.getDescription().copy(), Style.EMPTY.withBold(true));
         } else if (string2 != null) {
            lv2 = Text.translatable("gui.banned.description.reason_id_message", i, string2).formatted(Formatting.BOLD);
         } else {
            lv2 = Text.translatable("gui.banned.description.reason_id", i).formatted(Formatting.BOLD);
         }

         return Text.translatable("gui.banned.description.reason", lv2);
      } else {
         return Text.translatable("gui.banned.description.unknownreason");
      }
   }

   private static Text getDurationText(BanDetails banDetails) {
      if (isTemporary(banDetails)) {
         Text lv = getTemporaryBanDurationText(banDetails);
         return Text.translatable("gui.banned.description.temporary", Text.translatable("gui.banned.description.temporary.duration", lv).formatted(Formatting.BOLD));
      } else {
         return Text.translatable("gui.banned.description.permanent").formatted(Formatting.BOLD);
      }
   }

   private static Text getTemporaryBanDurationText(BanDetails banDetails) {
      Duration duration = Duration.between(Instant.now(), banDetails.expires());
      long l = duration.toHours();
      if (l > 72L) {
         return ScreenTexts.days(duration.toDays());
      } else {
         return l < 1L ? ScreenTexts.minutes(duration.toMinutes()) : ScreenTexts.hours(duration.toHours());
      }
   }

   private static boolean isTemporary(BanDetails banDetails) {
      return banDetails.expires() != null;
   }

   static {
      TEMPORARY_TITLE = Text.translatable("gui.banned.title.temporary").formatted(Formatting.BOLD);
      PERMANENT_TITLE = Text.translatable("gui.banned.title.permanent").formatted(Formatting.BOLD);
   }
}
