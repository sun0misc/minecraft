/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.obfuscate.DontObfuscate;

@Environment(value=EnvType.CLIENT)
public class ClientBrandRetriever {
    public static final String VANILLA = "vanilla";

    @DontObfuscate
    public static String getClientModName() {
        return VANILLA;
    }
}

