/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import org.apache.commons.lang3.ArrayUtils;

@Environment(value=EnvType.CLIENT)
public enum Icons {
    RELEASE("icons"),
    SNAPSHOT("icons", "snapshot");

    private final String[] path;

    private Icons(String ... path) {
        this.path = path;
    }

    public List<InputSupplier<InputStream>> getIcons(ResourcePack resourcePack) throws IOException {
        return List.of(this.getIcon(resourcePack, "icon_16x16.png"), this.getIcon(resourcePack, "icon_32x32.png"), this.getIcon(resourcePack, "icon_48x48.png"), this.getIcon(resourcePack, "icon_128x128.png"), this.getIcon(resourcePack, "icon_256x256.png"));
    }

    public InputSupplier<InputStream> getMacIcon(ResourcePack resourcePack) throws IOException {
        return this.getIcon(resourcePack, "minecraft.icns");
    }

    private InputSupplier<InputStream> getIcon(ResourcePack resourcePack, String fileName) throws IOException {
        CharSequence[] strings = ArrayUtils.add(this.path, fileName);
        InputSupplier<InputStream> lv = resourcePack.openRoot((String[])strings);
        if (lv == null) {
            throw new FileNotFoundException(String.join((CharSequence)"/", strings));
        }
        return lv;
    }
}

