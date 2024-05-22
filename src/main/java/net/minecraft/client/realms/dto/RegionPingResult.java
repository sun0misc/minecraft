/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.dto;

import com.google.gson.annotations.SerializedName;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.RealmsSerializable;
import net.minecraft.client.realms.dto.ValueObject;

@Environment(value=EnvType.CLIENT)
public class RegionPingResult
extends ValueObject
implements RealmsSerializable {
    @SerializedName(value="regionName")
    private final String regionName;
    @SerializedName(value="ping")
    private final int ping;

    public RegionPingResult(String regionName, int ping) {
        this.regionName = regionName;
        this.ping = ping;
    }

    public int getPing() {
        return this.ping;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s --> %.2f ms", this.regionName, Float.valueOf(this.ping));
    }
}

