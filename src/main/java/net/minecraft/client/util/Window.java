/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.util;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.GlException;
import net.minecraft.client.util.Icons;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.MacWindowUtil;
import net.minecraft.client.util.Monitor;
import net.minecraft.client.util.MonitorTracker;
import net.minecraft.client.util.VideoMode;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public final class Window
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final GLFWErrorCallback errorCallback = GLFWErrorCallback.create(this::logGlError);
    private final WindowEventHandler eventHandler;
    private final MonitorTracker monitorTracker;
    private final long handle;
    private int windowedX;
    private int windowedY;
    private int windowedWidth;
    private int windowedHeight;
    private Optional<VideoMode> videoMode;
    private boolean fullscreen;
    private boolean currentFullscreen;
    private int x;
    private int y;
    private int width;
    private int height;
    private int framebufferWidth;
    private int framebufferHeight;
    private int scaledWidth;
    private int scaledHeight;
    private double scaleFactor;
    private String phase = "";
    private boolean videoModeDirty;
    private int framerateLimit;
    private boolean vsync;

    public Window(WindowEventHandler eventHandler, MonitorTracker monitorTracker, WindowSettings settings, @Nullable String videoMode, String title) {
        this.monitorTracker = monitorTracker;
        this.throwOnGlError();
        this.setPhase("Pre startup");
        this.eventHandler = eventHandler;
        Optional<VideoMode> optional = VideoMode.fromString(videoMode);
        this.videoMode = optional.isPresent() ? optional : (settings.fullscreenWidth.isPresent() && settings.fullscreenHeight.isPresent() ? Optional.of(new VideoMode(settings.fullscreenWidth.getAsInt(), settings.fullscreenHeight.getAsInt(), 8, 8, 8, 60)) : Optional.empty());
        this.currentFullscreen = this.fullscreen = settings.fullscreen;
        Monitor lv = monitorTracker.getMonitor(GLFW.glfwGetPrimaryMonitor());
        this.width = settings.width > 0 ? settings.width : 1;
        this.windowedWidth = this.width;
        this.height = settings.height > 0 ? settings.height : 1;
        this.windowedHeight = this.height;
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_NATIVE_CONTEXT_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, 1);
        this.handle = GLFW.glfwCreateWindow(this.width, this.height, title, this.fullscreen && lv != null ? lv.getHandle() : 0L, 0L);
        if (lv != null) {
            VideoMode lv2 = lv.findClosestVideoMode(this.fullscreen ? this.videoMode : Optional.empty());
            this.windowedX = this.x = lv.getViewportX() + lv2.getWidth() / 2 - this.width / 2;
            this.windowedY = this.y = lv.getViewportY() + lv2.getHeight() / 2 - this.height / 2;
        } else {
            int[] is = new int[1];
            int[] js = new int[1];
            GLFW.glfwGetWindowPos(this.handle, is, js);
            this.windowedX = this.x = is[0];
            this.windowedY = this.y = js[0];
        }
        GLFW.glfwMakeContextCurrent(this.handle);
        GL.createCapabilities();
        this.updateWindowRegion();
        this.updateFramebufferSize();
        GLFW.glfwSetFramebufferSizeCallback(this.handle, this::onFramebufferSizeChanged);
        GLFW.glfwSetWindowPosCallback(this.handle, this::onWindowPosChanged);
        GLFW.glfwSetWindowSizeCallback(this.handle, this::onWindowSizeChanged);
        GLFW.glfwSetWindowFocusCallback(this.handle, this::onWindowFocusChanged);
        GLFW.glfwSetCursorEnterCallback(this.handle, this::onCursorEnterChanged);
    }

    public static String method_60793() {
        int i = GLFW.glfwGetPlatform();
        return switch (i) {
            case 0 -> "<error>";
            case 393217 -> "win32";
            case 393218 -> "cocoa";
            case 393219 -> "wayland";
            case 393220 -> "x11";
            case 393221 -> "null";
            default -> String.format(Locale.ROOT, "unknown (%08X)", i);
        };
    }

    public int getRefreshRate() {
        RenderSystem.assertOnRenderThread();
        return GLX._getRefreshRate(this);
    }

    public boolean shouldClose() {
        return GLX._shouldClose(this);
    }

    public static void acceptError(BiConsumer<Integer, String> consumer) {
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
            int i = GLFW.glfwGetError(pointerBuffer);
            if (i != 0) {
                long l = pointerBuffer.get();
                String string = l == 0L ? "" : MemoryUtil.memUTF8(l);
                consumer.accept(i, string);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setIcon(ResourcePack resourcePack, Icons icons) throws IOException {
        int i = GLFW.glfwGetPlatform();
        switch (i) {
            case 393217: 
            case 393220: {
                List<InputSupplier<InputStream>> list = icons.getIcons(resourcePack);
                ArrayList<ByteBuffer> list2 = new ArrayList<ByteBuffer>(list.size());
                try (MemoryStack memoryStack = MemoryStack.stackPush();){
                    GLFWImage.Buffer buffer = GLFWImage.malloc(list.size(), memoryStack);
                    for (int j = 0; j < list.size(); ++j) {
                        try (NativeImage lv = NativeImage.read(list.get(j).get());){
                            ByteBuffer byteBuffer = MemoryUtil.memAlloc(lv.getWidth() * lv.getHeight() * 4);
                            list2.add(byteBuffer);
                            byteBuffer.asIntBuffer().put(lv.copyPixelsRgba());
                            buffer.position(j);
                            buffer.width(lv.getWidth());
                            buffer.height(lv.getHeight());
                            buffer.pixels(byteBuffer);
                            continue;
                        }
                    }
                    GLFW.glfwSetWindowIcon(this.handle, (GLFWImage.Buffer)buffer.position(0));
                    break;
                } finally {
                    list2.forEach(MemoryUtil::memFree);
                }
            }
            case 393218: {
                MacWindowUtil.setApplicationIconImage(icons.getMacIcon(resourcePack));
                break;
            }
            case 393219: 
            case 393221: {
                break;
            }
            default: {
                LOGGER.warn("Not setting icon for unrecognized platform: {}", (Object)i);
            }
        }
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    private void throwOnGlError() {
        GLFW.glfwSetErrorCallback(Window::throwGlError);
    }

    private static void throwGlError(int error, long description) {
        String string = "GLFW error " + error + ": " + MemoryUtil.memUTF8(description);
        TinyFileDialogs.tinyfd_messageBox("Minecraft", string + ".\n\nPlease make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).", "ok", "error", false);
        throw new GlErroredException(string);
    }

    public void logGlError(int error, long description) {
        RenderSystem.assertOnRenderThread();
        String string = MemoryUtil.memUTF8(description);
        LOGGER.error("########## GL ERROR ##########");
        LOGGER.error("@ {}", (Object)this.phase);
        LOGGER.error("{}: {}", (Object)error, (Object)string);
    }

    public void logOnGlError() {
        GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback(this.errorCallback);
        if (gLFWErrorCallback != null) {
            gLFWErrorCallback.free();
        }
    }

    public void setVsync(boolean vsync) {
        RenderSystem.assertOnRenderThreadOrInit();
        this.vsync = vsync;
        GLFW.glfwSwapInterval(vsync ? 1 : 0);
    }

    @Override
    public void close() {
        RenderSystem.assertOnRenderThread();
        Callbacks.glfwFreeCallbacks(this.handle);
        this.errorCallback.close();
        GLFW.glfwDestroyWindow(this.handle);
        GLFW.glfwTerminate();
    }

    private void onWindowPosChanged(long window, int x, int y) {
        this.x = x;
        this.y = y;
    }

    private void onFramebufferSizeChanged(long window, int width, int height) {
        if (window != this.handle) {
            return;
        }
        int k = this.getFramebufferWidth();
        int m = this.getFramebufferHeight();
        if (width == 0 || height == 0) {
            return;
        }
        this.framebufferWidth = width;
        this.framebufferHeight = height;
        if (this.getFramebufferWidth() != k || this.getFramebufferHeight() != m) {
            this.eventHandler.onResolutionChanged();
        }
    }

    private void updateFramebufferSize() {
        int[] is = new int[1];
        int[] js = new int[1];
        GLFW.glfwGetFramebufferSize(this.handle, is, js);
        this.framebufferWidth = is[0] > 0 ? is[0] : 1;
        this.framebufferHeight = js[0] > 0 ? js[0] : 1;
    }

    private void onWindowSizeChanged(long window, int width, int height) {
        this.width = width;
        this.height = height;
    }

    private void onWindowFocusChanged(long window, boolean focused) {
        if (window == this.handle) {
            this.eventHandler.onWindowFocusChanged(focused);
        }
    }

    private void onCursorEnterChanged(long window, boolean entered) {
        if (entered) {
            this.eventHandler.onCursorEnterChanged();
        }
    }

    public void setFramerateLimit(int framerateLimit) {
        this.framerateLimit = framerateLimit;
    }

    public int getFramerateLimit() {
        return this.framerateLimit;
    }

    public void swapBuffers() {
        RenderSystem.flipFrame(this.handle);
        if (this.fullscreen != this.currentFullscreen) {
            this.currentFullscreen = this.fullscreen;
            this.updateFullscreen(this.vsync);
        }
    }

    public Optional<VideoMode> getVideoMode() {
        return this.videoMode;
    }

    public void setVideoMode(Optional<VideoMode> videoMode) {
        boolean bl = !videoMode.equals(this.videoMode);
        this.videoMode = videoMode;
        if (bl) {
            this.videoModeDirty = true;
        }
    }

    public void applyVideoMode() {
        if (this.fullscreen && this.videoModeDirty) {
            this.videoModeDirty = false;
            this.updateWindowRegion();
            this.eventHandler.onResolutionChanged();
        }
    }

    private void updateWindowRegion() {
        boolean bl;
        boolean bl2 = bl = GLFW.glfwGetWindowMonitor(this.handle) != 0L;
        if (this.fullscreen) {
            Monitor lv = this.monitorTracker.getMonitor(this);
            if (lv == null) {
                LOGGER.warn("Failed to find suitable monitor for fullscreen mode");
                this.fullscreen = false;
            } else {
                if (MinecraftClient.IS_SYSTEM_MAC) {
                    MacWindowUtil.toggleFullscreen(this.handle);
                }
                VideoMode lv2 = lv.findClosestVideoMode(this.videoMode);
                if (!bl) {
                    this.windowedX = this.x;
                    this.windowedY = this.y;
                    this.windowedWidth = this.width;
                    this.windowedHeight = this.height;
                }
                this.x = 0;
                this.y = 0;
                this.width = lv2.getWidth();
                this.height = lv2.getHeight();
                GLFW.glfwSetWindowMonitor(this.handle, lv.getHandle(), this.x, this.y, this.width, this.height, lv2.getRefreshRate());
                if (MinecraftClient.IS_SYSTEM_MAC) {
                    MacWindowUtil.fixStyleMask(this.handle);
                }
            }
        } else {
            this.x = this.windowedX;
            this.y = this.windowedY;
            this.width = this.windowedWidth;
            this.height = this.windowedHeight;
            GLFW.glfwSetWindowMonitor(this.handle, 0L, this.x, this.y, this.width, this.height, -1);
        }
    }

    public void toggleFullscreen() {
        this.fullscreen = !this.fullscreen;
    }

    public void setWindowedSize(int width, int height) {
        this.windowedWidth = width;
        this.windowedHeight = height;
        this.fullscreen = false;
        this.updateWindowRegion();
    }

    private void updateFullscreen(boolean vsync) {
        RenderSystem.assertOnRenderThread();
        try {
            this.updateWindowRegion();
            this.eventHandler.onResolutionChanged();
            this.setVsync(vsync);
            this.swapBuffers();
        } catch (Exception exception) {
            LOGGER.error("Couldn't toggle fullscreen", exception);
        }
    }

    public int calculateScaleFactor(int guiScale, boolean forceUnicodeFont) {
        int j;
        for (j = 1; j != guiScale && j < this.framebufferWidth && j < this.framebufferHeight && this.framebufferWidth / (j + 1) >= 320 && this.framebufferHeight / (j + 1) >= 240; ++j) {
        }
        if (forceUnicodeFont && j % 2 != 0) {
            ++j;
        }
        return j;
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
        int i = (int)((double)this.framebufferWidth / scaleFactor);
        this.scaledWidth = (double)this.framebufferWidth / scaleFactor > (double)i ? i + 1 : i;
        int j = (int)((double)this.framebufferHeight / scaleFactor);
        this.scaledHeight = (double)this.framebufferHeight / scaleFactor > (double)j ? j + 1 : j;
    }

    public void setTitle(String title) {
        GLFW.glfwSetWindowTitle(this.handle, title);
    }

    public long getHandle() {
        return this.handle;
    }

    public boolean isFullscreen() {
        return this.fullscreen;
    }

    public int getFramebufferWidth() {
        return this.framebufferWidth;
    }

    public int getFramebufferHeight() {
        return this.framebufferHeight;
    }

    public void setFramebufferWidth(int framebufferWidth) {
        this.framebufferWidth = framebufferWidth;
    }

    public void setFramebufferHeight(int framebufferHeight) {
        this.framebufferHeight = framebufferHeight;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getScaledWidth() {
        return this.scaledWidth;
    }

    public int getScaledHeight() {
        return this.scaledHeight;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public double getScaleFactor() {
        return this.scaleFactor;
    }

    @Nullable
    public Monitor getMonitor() {
        return this.monitorTracker.getMonitor(this);
    }

    public void setRawMouseMotion(boolean rawMouseMotion) {
        InputUtil.setRawMouseMotionMode(this.handle, rawMouseMotion);
    }

    @Environment(value=EnvType.CLIENT)
    public static class GlErroredException
    extends GlException {
        GlErroredException(String string) {
            super(string);
        }
    }
}

