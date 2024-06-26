/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import com.mojang.util.UndashedUuid;
import java.util.UUID;

public class Urls {
    public static final String GDPR = "https://aka.ms/MinecraftGDPR";
    public static final String EULA = "https://aka.ms/MinecraftEULA";
    public static final String PRIVACY_STATEMENT = "http://go.microsoft.com/fwlink/?LinkId=521839";
    public static final String JAVA_ATTRIBUTION = "https://aka.ms/MinecraftJavaAttribution";
    public static final String JAVA_LICENSES = "https://aka.ms/MinecraftJavaLicenses";
    public static final String BUY_JAVA = "https://aka.ms/BuyMinecraftJava";
    public static final String JAVA_ACCOUNT_SETTINGS = "https://aka.ms/JavaAccountSettings";
    public static final String SNAPSHOT_FEEDBACK = "https://aka.ms/snapshotfeedback?ref=game";
    public static final String JAVA_FEEDBACK = "https://aka.ms/javafeedback?ref=game";
    public static final String SNAPSHOT_BUGS = "https://aka.ms/snapshotbugs?ref=game";
    public static final String MINECRAFT_SUPPORT = "https://aka.ms/Minecraft-Support";
    public static final String JAVA_ACCESSIBILITY = "https://aka.ms/MinecraftJavaAccessibility";
    public static final String ABOUT_JAVA_REPORTING = "https://aka.ms/aboutjavareporting";
    public static final String JAVA_MODERATION = "https://aka.ms/mcjavamoderation";
    public static final String JAVA_BLOCKING = "https://aka.ms/javablocking";
    public static final String MINECRAFT_SYMLINKS = "https://aka.ms/MinecraftSymLinks";
    public static final String JAVA_REALMS_TRIAL = "https://aka.ms/startjavarealmstrial";
    public static final String BUY_JAVA_REALMS = "https://aka.ms/BuyJavaRealms";
    public static final String REALMS_TERMS = "https://aka.ms/MinecraftRealmsTerms";
    public static final String REALMS_CONTENT_CREATOR = "https://aka.ms/MinecraftRealmsContentCreator";

    public static String getExtendJavaRealmsUrl(String subscriptionId, UUID uuid, boolean trial) {
        return Urls.getExtendJavaRealmsUrl(subscriptionId, uuid) + "&ref=" + (trial ? "expiredTrial" : "expiredRealm");
    }

    public static String getExtendJavaRealmsUrl(String subscriptionId, UUID uuid) {
        return "https://aka.ms/ExtendJavaRealms?subscriptionId=" + subscriptionId + "&profileId=" + UndashedUuid.toString(uuid);
    }
}

