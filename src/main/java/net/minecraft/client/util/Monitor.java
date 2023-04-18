package net.minecraft.client.util;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

@Environment(EnvType.CLIENT)
public final class Monitor {
   private final long handle;
   private final List videoModes;
   private VideoMode currentVideoMode;
   private int x;
   private int y;

   public Monitor(long handle) {
      this.handle = handle;
      this.videoModes = Lists.newArrayList();
      this.populateVideoModes();
   }

   public void populateVideoModes() {
      RenderSystem.assertInInitPhase();
      this.videoModes.clear();
      GLFWVidMode.Buffer buffer = GLFW.glfwGetVideoModes(this.handle);

      for(int i = buffer.limit() - 1; i >= 0; --i) {
         buffer.position(i);
         VideoMode lv = new VideoMode(buffer);
         if (lv.getRedBits() >= 8 && lv.getGreenBits() >= 8 && lv.getBlueBits() >= 8) {
            this.videoModes.add(lv);
         }
      }

      int[] is = new int[1];
      int[] js = new int[1];
      GLFW.glfwGetMonitorPos(this.handle, is, js);
      this.x = is[0];
      this.y = js[0];
      GLFWVidMode gLFWVidMode = GLFW.glfwGetVideoMode(this.handle);
      this.currentVideoMode = new VideoMode(gLFWVidMode);
   }

   public VideoMode findClosestVideoMode(Optional videoMode) {
      RenderSystem.assertInInitPhase();
      if (videoMode.isPresent()) {
         VideoMode lv = (VideoMode)videoMode.get();
         Iterator var3 = this.videoModes.iterator();

         while(var3.hasNext()) {
            VideoMode lv2 = (VideoMode)var3.next();
            if (lv2.equals(lv)) {
               return lv2;
            }
         }
      }

      return this.getCurrentVideoMode();
   }

   public int findClosestVideoModeIndex(VideoMode videoMode) {
      RenderSystem.assertInInitPhase();
      return this.videoModes.indexOf(videoMode);
   }

   public VideoMode getCurrentVideoMode() {
      return this.currentVideoMode;
   }

   public int getViewportX() {
      return this.x;
   }

   public int getViewportY() {
      return this.y;
   }

   public VideoMode getVideoMode(int index) {
      return (VideoMode)this.videoModes.get(index);
   }

   public int getVideoModeCount() {
      return this.videoModes.size();
   }

   public long getHandle() {
      return this.handle;
   }

   public String toString() {
      return String.format(Locale.ROOT, "Monitor[%s %sx%s %s]", this.handle, this.x, this.y, this.currentVideoMode);
   }
}
