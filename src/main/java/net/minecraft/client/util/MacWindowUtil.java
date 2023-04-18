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

@Environment(EnvType.CLIENT)
public class MacWindowUtil {
   private static final int FULLSCREEN_MASK = 16384;

   public static void toggleFullscreen(long handle) {
      getCocoaWindow(handle).filter(MacWindowUtil::isFullscreen).ifPresent(MacWindowUtil::toggleFullscreen);
   }

   private static Optional getCocoaWindow(long handle) {
      long m = GLFWNativeCocoa.glfwGetCocoaWindow(handle);
      return m != 0L ? Optional.of(new NSObject(new Pointer(m))) : Optional.empty();
   }

   private static boolean isFullscreen(NSObject handle) {
      return ((Long)handle.sendRaw("styleMask", new Object[0]) & 16384L) == 16384L;
   }

   private static void toggleFullscreen(NSObject handle) {
      handle.send("toggleFullScreen:", new Object[]{Pointer.NULL});
   }

   public static void setApplicationIconImage(InputSupplier iconSupplier) throws IOException {
      InputStream inputStream = (InputStream)iconSupplier.get();

      try {
         String string = Base64.getEncoder().encodeToString(inputStream.readAllBytes());
         Client client = Client.getInstance();
         Object object = client.sendProxy("NSData", "alloc", new Object[0]).send("initWithBase64Encoding:", new Object[]{string});
         Object object2 = client.sendProxy("NSImage", "alloc", new Object[0]).send("initWithData:", new Object[]{object});
         client.sendProxy("NSApplication", "sharedApplication", new Object[0]).send("setApplicationIconImage:", new Object[]{object2});
      } catch (Throwable var7) {
         if (inputStream != null) {
            try {
               inputStream.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (inputStream != null) {
         inputStream.close();
      }

   }
}
