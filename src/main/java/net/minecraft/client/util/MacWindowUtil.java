/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.util;

import ca.weblite.objc.Client;
import ca.weblite.objc.NSObject;
import com.sun.jna.Pointer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.InputSupplier;
import org.lwjgl.glfw.GLFWNativeCocoa;

@Environment(value=EnvType.CLIENT)
public class MacWindowUtil {
    private static final int field_46537 = 8;
    private static final int FULLSCREEN_MASK = 16384;

    public static void toggleFullscreen(long handle) {
        MacWindowUtil.getCocoaWindow(handle).filter(MacWindowUtil::isFullscreen).ifPresent(MacWindowUtil::toggleFullscreen);
    }

    public static void fixStyleMask(long handle) {
        MacWindowUtil.getCocoaWindow(handle).ifPresent(windowHandle -> {
            long l = MacWindowUtil.getStyleMask(windowHandle);
            windowHandle.send("setStyleMask:", l & 0xFFFFFFFFFFFFFFF7L);
        });
    }

    private static Optional<NSObject> getCocoaWindow(long handle) {
        long m = GLFWNativeCocoa.glfwGetCocoaWindow(handle);
        if (m != 0L) {
            return Optional.of(new NSObject(new Pointer(m)));
        }
        return Optional.empty();
    }

    private static boolean isFullscreen(NSObject handle) {
        return (MacWindowUtil.getStyleMask(handle) & 0x4000L) != 0L;
    }

    private static long getStyleMask(NSObject handle) {
        return (Long)handle.sendRaw("styleMask", new Object[0]);
    }

    private static void toggleFullscreen(NSObject handle) {
        handle.send("toggleFullScreen:", Pointer.NULL);
    }

    public static void setApplicationIconImage(InputSupplier<InputStream> iconSupplier) throws IOException {
        try (InputStream inputStream = iconSupplier.get();){
            String string = Base64.getEncoder().encodeToString(inputStream.readAllBytes());
            Client client = Client.getInstance();
            Object object = client.sendProxy("NSData", "alloc", new Object[0]).send("initWithBase64Encoding:", string);
            Object object2 = client.sendProxy("NSImage", "alloc", new Object[0]).send("initWithData:", object);
            client.sendProxy("NSApplication", "sharedApplication", new Object[0]).send("setApplicationIconImage:", object2);
        }
    }
}

