/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.Date;
import java.util.UUID;
import net.minecraft.server.BanEntry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class BannedPlayerEntry
extends BanEntry<GameProfile> {
    public BannedPlayerEntry(@Nullable GameProfile profile) {
        this(profile, (Date)null, (String)null, (Date)null, (String)null);
    }

    public BannedPlayerEntry(@Nullable GameProfile profile, @Nullable Date created, @Nullable String source, @Nullable Date expiry, @Nullable String reason) {
        super(profile, created, source, expiry, reason);
    }

    public BannedPlayerEntry(JsonObject json) {
        super(BannedPlayerEntry.profileFromJson(json), json);
    }

    @Override
    protected void write(JsonObject json) {
        if (this.getKey() == null) {
            return;
        }
        json.addProperty("uuid", ((GameProfile)this.getKey()).getId().toString());
        json.addProperty("name", ((GameProfile)this.getKey()).getName());
        super.write(json);
    }

    @Override
    public Text toText() {
        GameProfile gameProfile = (GameProfile)this.getKey();
        return gameProfile != null ? Text.literal(gameProfile.getName()) : Text.translatable("commands.banlist.entry.unknown");
    }

    @Nullable
    private static GameProfile profileFromJson(JsonObject json) {
        UUID uUID;
        if (!json.has("uuid") || !json.has("name")) {
            return null;
        }
        String string = json.get("uuid").getAsString();
        try {
            uUID = UUID.fromString(string);
        } catch (Throwable throwable) {
            return null;
        }
        return new GameProfile(uUID, json.get("name").getAsString());
    }
}

