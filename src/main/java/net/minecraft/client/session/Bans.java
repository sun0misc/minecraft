/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.session;

import com.mojang.authlib.minecraft.BanDetails;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.time.Duration;
import java.time.Instant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.session.BanReason;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;

@Environment(value=EnvType.CLIENT)
public class Bans {
    private static final Text TEMPORARY_TITLE = Text.translatable("gui.banned.title.temporary").formatted(Formatting.BOLD);
    private static final Text PERMANENT_TITLE = Text.translatable("gui.banned.title.permanent").formatted(Formatting.BOLD);
    public static final Text NAME_TITLE = Text.translatable("gui.banned.name.title").formatted(Formatting.BOLD);
    private static final Text SKIN_TITLE = Text.translatable("gui.banned.skin.title").formatted(Formatting.BOLD);
    private static final Text SKIN_DESCRIPTION = Text.translatable("gui.banned.skin.description", Text.literal("https://aka.ms/mcjavamoderation"));

    public static ConfirmLinkScreen createBanScreen(BooleanConsumer callback, BanDetails banDetails) {
        return new ConfirmLinkScreen(callback, Bans.getTitle(banDetails), Bans.getDescriptionText(banDetails), "https://aka.ms/mcjavamoderation", ScreenTexts.ACKNOWLEDGE, true);
    }

    public static ConfirmLinkScreen createSkinBanScreen(Runnable onClose) {
        String string = "https://aka.ms/mcjavamoderation";
        return new ConfirmLinkScreen(confirmed -> {
            if (confirmed) {
                Util.getOperatingSystem().open("https://aka.ms/mcjavamoderation");
            }
            onClose.run();
        }, SKIN_TITLE, SKIN_DESCRIPTION, "https://aka.ms/mcjavamoderation", ScreenTexts.ACKNOWLEDGE, true);
    }

    public static ConfirmLinkScreen createUsernameBanScreen(String username, Runnable onClose) {
        String string2 = "https://aka.ms/mcjavamoderation";
        return new ConfirmLinkScreen(confirmed -> {
            if (confirmed) {
                Util.getOperatingSystem().open("https://aka.ms/mcjavamoderation");
            }
            onClose.run();
        }, NAME_TITLE, Text.translatable("gui.banned.name.description", Text.literal(username).formatted(Formatting.YELLOW), "https://aka.ms/mcjavamoderation"), "https://aka.ms/mcjavamoderation", ScreenTexts.ACKNOWLEDGE, true);
    }

    private static Text getTitle(BanDetails banDetails) {
        return Bans.isTemporary(banDetails) ? TEMPORARY_TITLE : PERMANENT_TITLE;
    }

    private static Text getDescriptionText(BanDetails banDetails) {
        return Text.translatable("gui.banned.description", Bans.getReasonText(banDetails), Bans.getDurationText(banDetails), Text.literal("https://aka.ms/mcjavamoderation"));
    }

    private static Text getReasonText(BanDetails banDetails) {
        String string = banDetails.reason();
        String string2 = banDetails.reasonMessage();
        if (StringUtils.isNumeric(string)) {
            int i = Integer.parseInt(string);
            BanReason lv = BanReason.byId(i);
            MutableText lv2 = lv != null ? Texts.setStyleIfAbsent(lv.getDescription().copy(), Style.EMPTY.withBold(true)) : (string2 != null ? Text.translatable("gui.banned.description.reason_id_message", i, string2).formatted(Formatting.BOLD) : Text.translatable("gui.banned.description.reason_id", i).formatted(Formatting.BOLD));
            return Text.translatable("gui.banned.description.reason", lv2);
        }
        return Text.translatable("gui.banned.description.unknownreason");
    }

    private static Text getDurationText(BanDetails banDetails) {
        if (Bans.isTemporary(banDetails)) {
            Text lv = Bans.getTemporaryBanDurationText(banDetails);
            return Text.translatable("gui.banned.description.temporary", Text.translatable("gui.banned.description.temporary.duration", lv).formatted(Formatting.BOLD));
        }
        return Text.translatable("gui.banned.description.permanent").formatted(Formatting.BOLD);
    }

    private static Text getTemporaryBanDurationText(BanDetails banDetails) {
        Duration duration = Duration.between(Instant.now(), banDetails.expires());
        long l = duration.toHours();
        if (l > 72L) {
            return ScreenTexts.days(duration.toDays());
        }
        if (l < 1L) {
            return ScreenTexts.minutes(duration.toMinutes());
        }
        return ScreenTexts.hours(duration.toHours());
    }

    private static boolean isTemporary(BanDetails banDetails) {
        return banDetails.expires() != null;
    }
}

