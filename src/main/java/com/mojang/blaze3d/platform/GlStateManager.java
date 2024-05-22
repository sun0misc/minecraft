/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package com.mojang.blaze3d.platform;

import com.google.common.base.Charsets;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.DeobfuscateClass;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@Environment(value=EnvType.CLIENT)
@DeobfuscateClass
public class GlStateManager {
    private static final boolean ON_LINUX = Util.getOperatingSystem() == Util.OperatingSystem.LINUX;
    public static final int TEXTURE_COUNT = 12;
    private static final BlendFuncState BLEND = new BlendFuncState();
    private static final DepthTestState DEPTH = new DepthTestState();
    private static final CullFaceState CULL = new CullFaceState();
    private static final PolygonOffsetState POLY_OFFSET = new PolygonOffsetState();
    private static final LogicOpState COLOR_LOGIC = new LogicOpState();
    private static final StencilState STENCIL = new StencilState();
    private static final ScissorTestState SCISSOR = new ScissorTestState();
    private static int activeTexture;
    private static final Texture2DState[] TEXTURES;
    private static final ColorMask COLOR_MASK;

    public static void _disableScissorTest() {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager.SCISSOR.capState.disable();
    }

    public static void _enableScissorTest() {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager.SCISSOR.capState.enable();
    }

    public static void _scissorBox(int x, int y, int width, int height) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL20.glScissor(x, y, width, height);
    }

    public static void _disableDepthTest() {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager.DEPTH.capState.disable();
    }

    public static void _enableDepthTest() {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager.DEPTH.capState.enable();
    }

    public static void _depthFunc(int func) {
        RenderSystem.assertOnRenderThreadOrInit();
        if (func != GlStateManager.DEPTH.func) {
            GlStateManager.DEPTH.func = func;
            GL11.glDepthFunc(func);
        }
    }

    public static void _depthMask(boolean mask) {
        RenderSystem.assertOnRenderThread();
        if (mask != GlStateManager.DEPTH.mask) {
            GlStateManager.DEPTH.mask = mask;
            GL11.glDepthMask(mask);
        }
    }

    public static void _disableBlend() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.BLEND.capState.disable();
    }

    public static void _enableBlend() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.BLEND.capState.enable();
    }

    public static void _blendFunc(int srcFactor, int dstFactor) {
        RenderSystem.assertOnRenderThread();
        if (srcFactor != GlStateManager.BLEND.srcFactorRGB || dstFactor != GlStateManager.BLEND.dstFactorRGB) {
            GlStateManager.BLEND.srcFactorRGB = srcFactor;
            GlStateManager.BLEND.dstFactorRGB = dstFactor;
            GL11.glBlendFunc(srcFactor, dstFactor);
        }
    }

    public static void _blendFuncSeparate(int srcFactorRGB, int dstFactorRGB, int srcFactorAlpha, int dstFactorAlpha) {
        RenderSystem.assertOnRenderThread();
        if (srcFactorRGB != GlStateManager.BLEND.srcFactorRGB || dstFactorRGB != GlStateManager.BLEND.dstFactorRGB || srcFactorAlpha != GlStateManager.BLEND.srcFactorAlpha || dstFactorAlpha != GlStateManager.BLEND.dstFactorAlpha) {
            GlStateManager.BLEND.srcFactorRGB = srcFactorRGB;
            GlStateManager.BLEND.dstFactorRGB = dstFactorRGB;
            GlStateManager.BLEND.srcFactorAlpha = srcFactorAlpha;
            GlStateManager.BLEND.dstFactorAlpha = dstFactorAlpha;
            GlStateManager.glBlendFuncSeparate(srcFactorRGB, dstFactorRGB, srcFactorAlpha, dstFactorAlpha);
        }
    }

    public static void _blendEquation(int mode) {
        RenderSystem.assertOnRenderThread();
        GL14.glBlendEquation(mode);
    }

    public static int glGetProgrami(int program, int pname) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetProgrami(program, pname);
    }

    public static void glAttachShader(int program, int shader) {
        RenderSystem.assertOnRenderThread();
        GL20.glAttachShader(program, shader);
    }

    public static void glDeleteShader(int shader) {
        RenderSystem.assertOnRenderThread();
        GL20.glDeleteShader(shader);
    }

    public static int glCreateShader(int type) {
        RenderSystem.assertOnRenderThread();
        return GL20.glCreateShader(type);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void glShaderSource(int shader, List<String> strings) {
        RenderSystem.assertOnRenderThread();
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : strings) {
            stringBuilder.append(string);
        }
        byte[] bs = stringBuilder.toString().getBytes(Charsets.UTF_8);
        ByteBuffer byteBuffer = MemoryUtil.memAlloc(bs.length + 1);
        byteBuffer.put(bs);
        byteBuffer.put((byte)0);
        byteBuffer.flip();
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
            pointerBuffer.put(byteBuffer);
            GL20C.nglShaderSource(shader, 1, pointerBuffer.address0(), 0L);
        } finally {
            MemoryUtil.memFree(byteBuffer);
        }
    }

    public static void glCompileShader(int shader) {
        RenderSystem.assertOnRenderThread();
        GL20.glCompileShader(shader);
    }

    public static int glGetShaderi(int shader, int pname) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetShaderi(shader, pname);
    }

    public static void _glUseProgram(int program) {
        RenderSystem.assertOnRenderThread();
        GL20.glUseProgram(program);
    }

    public static int glCreateProgram() {
        RenderSystem.assertOnRenderThread();
        return GL20.glCreateProgram();
    }

    public static void glDeleteProgram(int program) {
        RenderSystem.assertOnRenderThread();
        GL20.glDeleteProgram(program);
    }

    public static void glLinkProgram(int program) {
        RenderSystem.assertOnRenderThread();
        GL20.glLinkProgram(program);
    }

    public static int _glGetUniformLocation(int program, CharSequence name) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetUniformLocation(program, name);
    }

    public static void _glUniform1(int location, IntBuffer value) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform1iv(location, value);
    }

    public static void _glUniform1i(int location, int value) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform1i(location, value);
    }

    public static void _glUniform1(int location, FloatBuffer value) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform1fv(location, value);
    }

    public static void _glUniform2(int location, IntBuffer value) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform2iv(location, value);
    }

    public static void _glUniform2(int location, FloatBuffer value) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform2fv(location, value);
    }

    public static void _glUniform3(int location, IntBuffer value) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform3iv(location, value);
    }

    public static void _glUniform3(int location, FloatBuffer value) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform3fv(location, value);
    }

    public static void _glUniform4(int location, IntBuffer value) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform4iv(location, value);
    }

    public static void _glUniform4(int location, FloatBuffer value) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform4fv(location, value);
    }

    public static void _glUniformMatrix2(int location, boolean transpose, FloatBuffer value) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniformMatrix2fv(location, transpose, value);
    }

    public static void _glUniformMatrix3(int location, boolean transpose, FloatBuffer value) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniformMatrix3fv(location, transpose, value);
    }

    public static void _glUniformMatrix4(int location, boolean transpose, FloatBuffer value) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniformMatrix4fv(location, transpose, value);
    }

    public static int _glGetAttribLocation(int program, CharSequence name) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetAttribLocation(program, name);
    }

    public static void _glBindAttribLocation(int program, int index, CharSequence name) {
        RenderSystem.assertOnRenderThread();
        GL20.glBindAttribLocation(program, index, name);
    }

    public static int _glGenBuffers() {
        RenderSystem.assertOnRenderThreadOrInit();
        return GL15.glGenBuffers();
    }

    public static int _glGenVertexArrays() {
        RenderSystem.assertOnRenderThreadOrInit();
        return GL30.glGenVertexArrays();
    }

    public static void _glBindBuffer(int target, int buffer) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL15.glBindBuffer(target, buffer);
    }

    public static void _glBindVertexArray(int array) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL30.glBindVertexArray(array);
    }

    public static void _glBufferData(int target, ByteBuffer data, int usage) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL15.glBufferData(target, data, usage);
    }

    public static void _glBufferData(int target, long size, int usage) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL15.glBufferData(target, size, usage);
    }

    @Nullable
    public static ByteBuffer mapBuffer(int target, int access) {
        RenderSystem.assertOnRenderThreadOrInit();
        return GL15.glMapBuffer(target, access);
    }

    public static void _glUnmapBuffer(int target) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL15.glUnmapBuffer(target);
    }

    public static void _glDeleteBuffers(int buffer) {
        RenderSystem.assertOnRenderThread();
        if (ON_LINUX) {
            GL32C.glBindBuffer(GlConst.GL_ARRAY_BUFFER, buffer);
            GL32C.glBufferData(GlConst.GL_ARRAY_BUFFER, 0L, GlConst.GL_DYNAMIC_DRAW);
            GL32C.glBindBuffer(GlConst.GL_ARRAY_BUFFER, 0);
        }
        GL15.glDeleteBuffers(buffer);
    }

    public static void _glCopyTexSubImage2D(int target, int level, int xOffset, int yOffset, int x, int y, int width, int height) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL20.glCopyTexSubImage2D(target, level, xOffset, yOffset, x, y, width, height);
    }

    public static void _glDeleteVertexArrays(int array) {
        RenderSystem.assertOnRenderThread();
        GL30.glDeleteVertexArrays(array);
    }

    public static void _glBindFramebuffer(int target, int framebuffer) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL30.glBindFramebuffer(target, framebuffer);
    }

    public static void _glBlitFrameBuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL30.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
    }

    public static void _glBindRenderbuffer(int target, int renderbuffer) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL30.glBindRenderbuffer(target, renderbuffer);
    }

    public static void _glDeleteRenderbuffers(int renderbuffer) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL30.glDeleteRenderbuffers(renderbuffer);
    }

    public static void _glDeleteFramebuffers(int framebuffer) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL30.glDeleteFramebuffers(framebuffer);
    }

    public static int glGenFramebuffers() {
        RenderSystem.assertOnRenderThreadOrInit();
        return GL30.glGenFramebuffers();
    }

    public static int glGenRenderbuffers() {
        RenderSystem.assertOnRenderThreadOrInit();
        return GL30.glGenRenderbuffers();
    }

    public static void _glRenderbufferStorage(int target, int internalFormat, int width, int height) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL30.glRenderbufferStorage(target, internalFormat, width, height);
    }

    public static void _glFramebufferRenderbuffer(int target, int attachment, int renderbufferTarget, int renderbuffer) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL30.glFramebufferRenderbuffer(target, attachment, renderbufferTarget, renderbuffer);
    }

    public static int glCheckFramebufferStatus(int target) {
        RenderSystem.assertOnRenderThreadOrInit();
        return GL30.glCheckFramebufferStatus(target);
    }

    public static void _glFramebufferTexture2D(int target, int attachment, int textureTarget, int texture, int level) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL30.glFramebufferTexture2D(target, attachment, textureTarget, texture, level);
    }

    public static int getBoundFramebuffer() {
        RenderSystem.assertOnRenderThread();
        return GlStateManager._getInteger(36006);
    }

    public static void glActiveTexture(int texture) {
        RenderSystem.assertOnRenderThread();
        GL13.glActiveTexture(texture);
    }

    public static void glBlendFuncSeparate(int srcFactorRGB, int dstFactorRGB, int srcFactorAlpha, int dstFactorAlpha) {
        RenderSystem.assertOnRenderThread();
        GL14.glBlendFuncSeparate(srcFactorRGB, dstFactorRGB, srcFactorAlpha, dstFactorAlpha);
    }

    public static String glGetShaderInfoLog(int shader, int maxLength) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetShaderInfoLog(shader, maxLength);
    }

    public static String glGetProgramInfoLog(int program, int maxLength) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetProgramInfoLog(program, maxLength);
    }

    public static void setupLevelDiffuseLighting(Vector3f vector3f, Vector3f vector3f2, Matrix4f matrix4f) {
        RenderSystem.assertOnRenderThread();
        RenderSystem.setShaderLights(matrix4f.transformDirection(vector3f, new Vector3f()), matrix4f.transformDirection(vector3f2, new Vector3f()));
    }

    public static void setupGuiFlatDiffuseLighting(Vector3f vector3f, Vector3f vector3f2) {
        RenderSystem.assertOnRenderThread();
        Matrix4f matrix4f = new Matrix4f().rotationY(-0.3926991f).rotateX(2.3561945f);
        GlStateManager.setupLevelDiffuseLighting(vector3f, vector3f2, matrix4f);
    }

    public static void setupGui3DDiffuseLighting(Vector3f vector3f, Vector3f vector3f2) {
        RenderSystem.assertOnRenderThread();
        Matrix4f matrix4f = new Matrix4f().scaling(1.0f, -1.0f, 1.0f).rotateYXZ(1.0821041f, 3.2375858f, 0.0f).rotateYXZ(-0.3926991f, 2.3561945f, 0.0f);
        GlStateManager.setupLevelDiffuseLighting(vector3f, vector3f2, matrix4f);
    }

    public static void _enableCull() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.CULL.capState.enable();
    }

    public static void _disableCull() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.CULL.capState.disable();
    }

    public static void _polygonMode(int face, int mode) {
        RenderSystem.assertOnRenderThread();
        GL11.glPolygonMode(face, mode);
    }

    public static void _enablePolygonOffset() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.POLY_OFFSET.capFill.enable();
    }

    public static void _disablePolygonOffset() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.POLY_OFFSET.capFill.disable();
    }

    public static void _polygonOffset(float factor, float units) {
        RenderSystem.assertOnRenderThread();
        if (factor != GlStateManager.POLY_OFFSET.factor || units != GlStateManager.POLY_OFFSET.units) {
            GlStateManager.POLY_OFFSET.factor = factor;
            GlStateManager.POLY_OFFSET.units = units;
            GL11.glPolygonOffset(factor, units);
        }
    }

    public static void _enableColorLogicOp() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.COLOR_LOGIC.capState.enable();
    }

    public static void _disableColorLogicOp() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.COLOR_LOGIC.capState.disable();
    }

    public static void _logicOp(int op) {
        RenderSystem.assertOnRenderThread();
        if (op != GlStateManager.COLOR_LOGIC.op) {
            GlStateManager.COLOR_LOGIC.op = op;
            GL11.glLogicOp(op);
        }
    }

    public static void _activeTexture(int texture) {
        RenderSystem.assertOnRenderThread();
        if (activeTexture != texture - GlConst.GL_TEXTURE0) {
            activeTexture = texture - GlConst.GL_TEXTURE0;
            GlStateManager.glActiveTexture(texture);
        }
    }

    public static void _texParameter(int target, int pname, float param) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glTexParameterf(target, pname, param);
    }

    public static void _texParameter(int target, int pname, int param) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glTexParameteri(target, pname, param);
    }

    public static int _getTexLevelParameter(int target, int level, int pname) {
        return GL11.glGetTexLevelParameteri(target, level, pname);
    }

    public static int _genTexture() {
        RenderSystem.assertOnRenderThreadOrInit();
        return GL11.glGenTextures();
    }

    public static void _genTextures(int[] textures) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glGenTextures(textures);
    }

    public static void _deleteTexture(int texture) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glDeleteTextures(texture);
        for (Texture2DState lv : TEXTURES) {
            if (lv.boundTexture != texture) continue;
            lv.boundTexture = -1;
        }
    }

    public static void _deleteTextures(int[] textures) {
        RenderSystem.assertOnRenderThreadOrInit();
        for (Texture2DState lv : TEXTURES) {
            for (int i : textures) {
                if (lv.boundTexture != i) continue;
                lv.boundTexture = -1;
            }
        }
        GL11.glDeleteTextures(textures);
    }

    public static void _bindTexture(int texture) {
        RenderSystem.assertOnRenderThreadOrInit();
        if (texture != GlStateManager.TEXTURES[GlStateManager.activeTexture].boundTexture) {
            GlStateManager.TEXTURES[GlStateManager.activeTexture].boundTexture = texture;
            GL11.glBindTexture(GlConst.GL_TEXTURE_2D, texture);
        }
    }

    public static int _getActiveTexture() {
        return activeTexture + 33984;
    }

    public static void _texImage2D(int target, int level, int internalFormat, int width, int height, int border, int format, int type, @Nullable IntBuffer pixels) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glTexImage2D(target, level, internalFormat, width, height, border, format, type, pixels);
    }

    public static void _texSubImage2D(int target, int level, int offsetX, int offsetY, int width, int height, int format, int type, long pixels) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glTexSubImage2D(target, level, offsetX, offsetY, width, height, format, type, pixels);
    }

    public static void upload(int level, int offsetX, int offsetY, int width, int height, NativeImage.Format format, IntBuffer pixels, Consumer<IntBuffer> closer) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> GlStateManager._upload(level, offsetX, offsetY, width, height, format, pixels, closer));
        } else {
            GlStateManager._upload(level, offsetX, offsetY, width, height, format, pixels, closer);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void _upload(int level, int offsetX, int offsetY, int width, int height, NativeImage.Format format, IntBuffer pixels, Consumer<IntBuffer> closer) {
        try {
            RenderSystem.assertOnRenderThreadOrInit();
            GlStateManager._pixelStore(GlConst.GL_UNPACK_ROW_LENGTH, width);
            GlStateManager._pixelStore(GlConst.GL_UNPACK_SKIP_PIXELS, 0);
            GlStateManager._pixelStore(GlConst.GL_UNPACK_SKIP_ROWS, 0);
            format.setUnpackAlignment();
            GL11.glTexSubImage2D(3553, level, offsetX, offsetY, width, height, format.toGl(), 5121, pixels);
        } finally {
            closer.accept(pixels);
        }
    }

    public static void _getTexImage(int target, int level, int format, int type, long pixels) {
        RenderSystem.assertOnRenderThread();
        GL11.glGetTexImage(target, level, format, type, pixels);
    }

    public static void _viewport(int x, int y, int width, int height) {
        RenderSystem.assertOnRenderThreadOrInit();
        Viewport.INSTANCE.x = x;
        Viewport.INSTANCE.y = y;
        Viewport.INSTANCE.width = width;
        Viewport.INSTANCE.height = height;
        GL11.glViewport(x, y, width, height);
    }

    public static void _colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        RenderSystem.assertOnRenderThread();
        if (red != GlStateManager.COLOR_MASK.red || green != GlStateManager.COLOR_MASK.green || blue != GlStateManager.COLOR_MASK.blue || alpha != GlStateManager.COLOR_MASK.alpha) {
            GlStateManager.COLOR_MASK.red = red;
            GlStateManager.COLOR_MASK.green = green;
            GlStateManager.COLOR_MASK.blue = blue;
            GlStateManager.COLOR_MASK.alpha = alpha;
            GL11.glColorMask(red, green, blue, alpha);
        }
    }

    public static void _stencilFunc(int func, int ref, int mask) {
        RenderSystem.assertOnRenderThread();
        if (func != GlStateManager.STENCIL.subState.func || func != GlStateManager.STENCIL.subState.ref || func != GlStateManager.STENCIL.subState.mask) {
            GlStateManager.STENCIL.subState.func = func;
            GlStateManager.STENCIL.subState.ref = ref;
            GlStateManager.STENCIL.subState.mask = mask;
            GL11.glStencilFunc(func, ref, mask);
        }
    }

    public static void _stencilMask(int mask) {
        RenderSystem.assertOnRenderThread();
        if (mask != GlStateManager.STENCIL.mask) {
            GlStateManager.STENCIL.mask = mask;
            GL11.glStencilMask(mask);
        }
    }

    public static void _stencilOp(int sfail, int dpfail, int dppass) {
        RenderSystem.assertOnRenderThread();
        if (sfail != GlStateManager.STENCIL.sfail || dpfail != GlStateManager.STENCIL.dpfail || dppass != GlStateManager.STENCIL.dppass) {
            GlStateManager.STENCIL.sfail = sfail;
            GlStateManager.STENCIL.dpfail = dpfail;
            GlStateManager.STENCIL.dppass = dppass;
            GL11.glStencilOp(sfail, dpfail, dppass);
        }
    }

    public static void _clearDepth(double depth) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glClearDepth(depth);
    }

    public static void _clearColor(float red, float green, float blue, float alpha) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glClearColor(red, green, blue, alpha);
    }

    public static void _clearStencil(int stencil) {
        RenderSystem.assertOnRenderThread();
        GL11.glClearStencil(stencil);
    }

    public static void _clear(int mask, boolean getError) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glClear(mask);
        if (getError) {
            GlStateManager._getError();
        }
    }

    public static void _glDrawPixels(int width, int height, int format, int type, long pixels) {
        RenderSystem.assertOnRenderThread();
        GL11.glDrawPixels(width, height, format, type, pixels);
    }

    public static void _vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointer) {
        RenderSystem.assertOnRenderThread();
        GL20.glVertexAttribPointer(index, size, type, normalized, stride, pointer);
    }

    public static void _vertexAttribIPointer(int index, int size, int type, int stride, long pointer) {
        RenderSystem.assertOnRenderThread();
        GL30.glVertexAttribIPointer(index, size, type, stride, pointer);
    }

    public static void _enableVertexAttribArray(int index) {
        RenderSystem.assertOnRenderThread();
        GL20.glEnableVertexAttribArray(index);
    }

    public static void _disableVertexAttribArray(int index) {
        RenderSystem.assertOnRenderThread();
        GL20.glDisableVertexAttribArray(index);
    }

    public static void _drawElements(int mode, int count, int type, long indices) {
        RenderSystem.assertOnRenderThread();
        GL11.glDrawElements(mode, count, type, indices);
    }

    public static void _pixelStore(int pname, int param) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glPixelStorei(pname, param);
    }

    public static void _readPixels(int x, int y, int width, int height, int format, int type, ByteBuffer pixels) {
        RenderSystem.assertOnRenderThread();
        GL11.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public static void _readPixels(int x, int y, int width, int height, int format, int type, long pixels) {
        RenderSystem.assertOnRenderThread();
        GL11.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public static int _getError() {
        RenderSystem.assertOnRenderThread();
        return GL11.glGetError();
    }

    public static String _getString(int name) {
        RenderSystem.assertOnRenderThread();
        return GL11.glGetString(name);
    }

    public static int _getInteger(int pname) {
        RenderSystem.assertOnRenderThreadOrInit();
        return GL11.glGetInteger(pname);
    }

    static {
        TEXTURES = (Texture2DState[])IntStream.range(0, 12).mapToObj(i -> new Texture2DState()).toArray(Texture2DState[]::new);
        COLOR_MASK = new ColorMask();
    }

    @Environment(value=EnvType.CLIENT)
    static class ScissorTestState {
        public final CapabilityTracker capState = new CapabilityTracker(GL11.GL_SCISSOR_TEST);

        ScissorTestState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class CapabilityTracker {
        private final int cap;
        private boolean state;

        public CapabilityTracker(int cap) {
            this.cap = cap;
        }

        public void disable() {
            this.setState(false);
        }

        public void enable() {
            this.setState(true);
        }

        public void setState(boolean state) {
            RenderSystem.assertOnRenderThreadOrInit();
            if (state != this.state) {
                this.state = state;
                if (state) {
                    GL11.glEnable(this.cap);
                } else {
                    GL11.glDisable(this.cap);
                }
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class DepthTestState {
        public final CapabilityTracker capState = new CapabilityTracker(GL11.GL_DEPTH_TEST);
        public boolean mask = true;
        public int func = 513;

        DepthTestState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class BlendFuncState {
        public final CapabilityTracker capState = new CapabilityTracker(GL11.GL_BLEND);
        public int srcFactorRGB = 1;
        public int dstFactorRGB = 0;
        public int srcFactorAlpha = 1;
        public int dstFactorAlpha = 0;

        BlendFuncState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class CullFaceState {
        public final CapabilityTracker capState = new CapabilityTracker(GL11.GL_CULL_FACE);
        public int mode = 1029;

        CullFaceState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class PolygonOffsetState {
        public final CapabilityTracker capFill = new CapabilityTracker(32823);
        public final CapabilityTracker capLine = new CapabilityTracker(GL11.GL_POLYGON_OFFSET_LINE);
        public float factor;
        public float units;

        PolygonOffsetState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class LogicOpState {
        public final CapabilityTracker capState = new CapabilityTracker(GL11.GL_COLOR_LOGIC_OP);
        public int op = 5379;

        LogicOpState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Texture2DState {
        public int boundTexture;

        Texture2DState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Viewport {
        INSTANCE;

        protected int x;
        protected int y;
        protected int width;
        protected int height;

        public static int getX() {
            return Viewport.INSTANCE.x;
        }

        public static int getY() {
            return Viewport.INSTANCE.y;
        }

        public static int getWidth() {
            return Viewport.INSTANCE.width;
        }

        public static int getHeight() {
            return Viewport.INSTANCE.height;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ColorMask {
        public boolean red = true;
        public boolean green = true;
        public boolean blue = true;
        public boolean alpha = true;

        ColorMask() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class StencilState {
        public final StencilSubState subState = new StencilSubState();
        public int mask = -1;
        public int sfail = 7680;
        public int dpfail = 7680;
        public int dppass = 7680;

        StencilState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class StencilSubState {
        public int func = 519;
        public int ref;
        public int mask = -1;

        StencilSubState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    @DeobfuscateClass
    public static enum DstFactor {
        CONSTANT_ALPHA(GL14.GL_CONSTANT_ALPHA),
        CONSTANT_COLOR(GL14.GL_CONSTANT_COLOR),
        DST_ALPHA(GlConst.GL_DST_ALPHA),
        DST_COLOR(GlConst.GL_DST_COLOR),
        ONE(1),
        ONE_MINUS_CONSTANT_ALPHA(GL14.GL_ONE_MINUS_CONSTANT_ALPHA),
        ONE_MINUS_CONSTANT_COLOR(GL14.GL_ONE_MINUS_CONSTANT_COLOR),
        ONE_MINUS_DST_ALPHA(GlConst.GL_ONE_MINUS_DST_ALPHA),
        ONE_MINUS_DST_COLOR(GlConst.GL_ONE_MINUS_DST_COLOR),
        ONE_MINUS_SRC_ALPHA(GlConst.GL_ONE_MINUS_SRC_ALPHA),
        ONE_MINUS_SRC_COLOR(GlConst.GL_ONE_MINUS_SRC_COLOR),
        SRC_ALPHA(GlConst.GL_SRC_ALPHA),
        SRC_COLOR(GlConst.GL_SRC_COLOR),
        ZERO(0);

        public final int value;

        private DstFactor(int value) {
            this.value = value;
        }
    }

    @Environment(value=EnvType.CLIENT)
    @DeobfuscateClass
    public static enum SrcFactor {
        CONSTANT_ALPHA(32771),
        CONSTANT_COLOR(32769),
        DST_ALPHA(772),
        DST_COLOR(774),
        ONE(1),
        ONE_MINUS_CONSTANT_ALPHA(32772),
        ONE_MINUS_CONSTANT_COLOR(32770),
        ONE_MINUS_DST_ALPHA(773),
        ONE_MINUS_DST_COLOR(775),
        ONE_MINUS_SRC_ALPHA(771),
        ONE_MINUS_SRC_COLOR(769),
        SRC_ALPHA(770),
        SRC_ALPHA_SATURATE(776),
        SRC_COLOR(768),
        ZERO(0);

        public final int value;

        private SrcFactor(int value) {
            this.value = value;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum LogicOp {
        AND(5377),
        AND_INVERTED(5380),
        AND_REVERSE(5378),
        CLEAR(5376),
        COPY(5379),
        COPY_INVERTED(5388),
        EQUIV(5385),
        INVERT(5386),
        NAND(5390),
        NOOP(5381),
        NOR(5384),
        OR(5383),
        OR_INVERTED(5389),
        OR_REVERSE(5387),
        SET(5391),
        XOR(5382);

        public final int value;

        private LogicOp(int value) {
            this.value = value;
        }
    }
}

