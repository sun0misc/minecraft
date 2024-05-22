/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.session;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public enum BanReason {
    GENERIC_VIOLATION("generic_violation"),
    FALSE_REPORTING("false_reporting"),
    HATE_SPEECH("hate_speech"),
    HATE_TERRORISM_NOTORIOUS_FIGURE("hate_terrorism_notorious_figure"),
    HARASSMENT_OR_BULLYING("harassment_or_bullying"),
    DEFAMATION_IMPERSONATION_FALSE_INFORMATION("defamation_impersonation_false_information"),
    DRUGS("drugs"),
    FRAUD("fraud"),
    SPAM_OR_ADVERTISING("spam_or_advertising"),
    NUDITY_OR_PORNOGRAPHY("nudity_or_pornography"),
    SEXUALLY_INAPPROPRIATE("sexually_inappropriate"),
    EXTREME_VIOLENCE_OR_GORE("extreme_violence_or_gore"),
    IMMINENT_HARM_TO_PERSON_OR_PROPERTY("imminent_harm_to_person_or_property");

    private final Text description;

    private BanReason(String id) {
        this.description = Text.translatable("gui.banned.reason." + id);
    }

    public Text getDescription() {
        return this.description;
    }

    @Nullable
    public static BanReason byId(int id) {
        return switch (id) {
            case 17, 19, 23, 31 -> GENERIC_VIOLATION;
            case 2 -> FALSE_REPORTING;
            case 5 -> HATE_SPEECH;
            case 16, 25 -> HATE_TERRORISM_NOTORIOUS_FIGURE;
            case 21 -> HARASSMENT_OR_BULLYING;
            case 27 -> DEFAMATION_IMPERSONATION_FALSE_INFORMATION;
            case 28 -> DRUGS;
            case 29 -> FRAUD;
            case 30 -> SPAM_OR_ADVERTISING;
            case 32 -> NUDITY_OR_PORNOGRAPHY;
            case 33 -> SEXUALLY_INAPPROPRIATE;
            case 34 -> EXTREME_VIOLENCE_OR_GORE;
            case 53 -> IMMINENT_HARM_TO_PERSON_OR_PROPERTY;
            default -> null;
        };
    }
}

