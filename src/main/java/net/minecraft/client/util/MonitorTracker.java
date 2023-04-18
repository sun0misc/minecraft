package net.minecraft.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMonitorCallback;
import org.lwjgl.glfw.GLFWMonitorCallbackI;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class MonitorTracker {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Long2ObjectMap pointerToMonitorMap = new Long2ObjectOpenHashMap();
   private final MonitorFactory monitorFactory;

   public MonitorTracker(MonitorFactory monitorFactory) {
      RenderSystem.assertInInitPhase();
      this.monitorFactory = monitorFactory;
      GLFW.glfwSetMonitorCallback(this::handleMonitorEvent);
      PointerBuffer pointerBuffer = GLFW.glfwGetMonitors();
      if (pointerBuffer != null) {
         for(int i = 0; i < pointerBuffer.limit(); ++i) {
            long l = pointerBuffer.get(i);
            this.pointerToMonitorMap.put(l, monitorFactory.createMonitor(l));
         }
      }

   }

   private void handleMonitorEvent(long monitor, int event) {
      RenderSystem.assertOnRenderThread();
      if (event == GLFW.GLFW_CONNECTED) {
         this.pointerToMonitorMap.put(monitor, this.monitorFactory.createMonitor(monitor));
         LOGGER.debug("Monitor {} connected. Current monitors: {}", monitor, this.pointerToMonitorMap);
      } else if (event == GLFW.GLFW_DISCONNECTED) {
         this.pointerToMonitorMap.remove(monitor);
         LOGGER.debug("Monitor {} disconnected. Current monitors: {}", monitor, this.pointerToMonitorMap);
      }

   }

   @Nullable
   public Monitor getMonitor(long pointer) {
      RenderSystem.assertInInitPhase();
      return (Monitor)this.pointerToMonitorMap.get(pointer);
   }

   @Nullable
   public Monitor getMonitor(Window window) {
      long l = GLFW.glfwGetWindowMonitor(window.getHandle());
      if (l != 0L) {
         return this.getMonitor(l);
      } else {
         int i = window.getX();
         int j = i + window.getWidth();
         int k = window.getY();
         int m = k + window.getHeight();
         int n = -1;
         Monitor lv = null;
         long o = GLFW.glfwGetPrimaryMonitor();
         LOGGER.debug("Selecting monitor - primary: {}, current monitors: {}", o, this.pointerToMonitorMap);
         ObjectIterator var12 = this.pointerToMonitorMap.values().iterator();

         while(var12.hasNext()) {
            Monitor lv2 = (Monitor)var12.next();
            int p = lv2.getViewportX();
            int q = p + lv2.getCurrentVideoMode().getWidth();
            int r = lv2.getViewportY();
            int s = r + lv2.getCurrentVideoMode().getHeight();
            int t = clamp(i, p, q);
            int u = clamp(j, p, q);
            int v = clamp(k, r, s);
            int w = clamp(m, r, s);
            int x = Math.max(0, u - t);
            int y = Math.max(0, w - v);
            int z = x * y;
            if (z > n) {
               lv = lv2;
               n = z;
            } else if (z == n && o == lv2.getHandle()) {
               LOGGER.debug("Primary monitor {} is preferred to monitor {}", lv2, lv);
               lv = lv2;
            }
         }

         LOGGER.debug("Selected monitor: {}", lv);
         return lv;
      }
   }

   public static int clamp(int value, int min, int max) {
      if (value < min) {
         return min;
      } else {
         return value > max ? max : value;
      }
   }

   public void stop() {
      RenderSystem.assertOnRenderThread();
      GLFWMonitorCallback gLFWMonitorCallback = GLFW.glfwSetMonitorCallback((GLFWMonitorCallbackI)null);
      if (gLFWMonitorCallback != null) {
         gLFWMonitorCallback.free();
      }

   }
}
