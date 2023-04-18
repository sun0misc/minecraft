package net.minecraft.client.gl;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.IntSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class PostEffectPass implements AutoCloseable {
   private final JsonEffectShaderProgram program;
   public final Framebuffer input;
   public final Framebuffer output;
   private final List samplerValues = Lists.newArrayList();
   private final List samplerNames = Lists.newArrayList();
   private final List samplerWidths = Lists.newArrayList();
   private final List samplerHeights = Lists.newArrayList();
   private Matrix4f projectionMatrix;

   public PostEffectPass(ResourceManager resourceManager, String programName, Framebuffer input, Framebuffer output) throws IOException {
      this.program = new JsonEffectShaderProgram(resourceManager, programName);
      this.input = input;
      this.output = output;
   }

   public void close() {
      this.program.close();
   }

   public final String getName() {
      return this.program.getName();
   }

   public void addAuxTarget(String name, IntSupplier valueSupplier, int width, int height) {
      this.samplerNames.add(this.samplerNames.size(), name);
      this.samplerValues.add(this.samplerValues.size(), valueSupplier);
      this.samplerWidths.add(this.samplerWidths.size(), width);
      this.samplerHeights.add(this.samplerHeights.size(), height);
   }

   public void setProjectionMatrix(Matrix4f projectionMatrix) {
      this.projectionMatrix = projectionMatrix;
   }

   public void render(float time) {
      this.input.endWrite();
      float g = (float)this.output.textureWidth;
      float h = (float)this.output.textureHeight;
      RenderSystem.viewport(0, 0, (int)g, (int)h);
      JsonEffectShaderProgram var10000 = this.program;
      Framebuffer var10002 = this.input;
      Objects.requireNonNull(var10002);
      var10000.bindSampler("DiffuseSampler", var10002::getColorAttachment);

      for(int i = 0; i < this.samplerValues.size(); ++i) {
         this.program.bindSampler((String)this.samplerNames.get(i), (IntSupplier)this.samplerValues.get(i));
         this.program.getUniformByNameOrDummy("AuxSize" + i).set((float)(Integer)this.samplerWidths.get(i), (float)(Integer)this.samplerHeights.get(i));
      }

      this.program.getUniformByNameOrDummy("ProjMat").set(this.projectionMatrix);
      this.program.getUniformByNameOrDummy("InSize").set((float)this.input.textureWidth, (float)this.input.textureHeight);
      this.program.getUniformByNameOrDummy("OutSize").set(g, h);
      this.program.getUniformByNameOrDummy("Time").set(time);
      MinecraftClient lv = MinecraftClient.getInstance();
      this.program.getUniformByNameOrDummy("ScreenSize").set((float)lv.getWindow().getFramebufferWidth(), (float)lv.getWindow().getFramebufferHeight());
      this.program.enable();
      this.output.clear(MinecraftClient.IS_SYSTEM_MAC);
      this.output.beginWrite(false);
      RenderSystem.depthFunc(519);
      BufferBuilder lv2 = Tessellator.getInstance().getBuffer();
      lv2.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
      lv2.vertex(0.0, 0.0, 500.0).next();
      lv2.vertex((double)g, 0.0, 500.0).next();
      lv2.vertex((double)g, (double)h, 500.0).next();
      lv2.vertex(0.0, (double)h, 500.0).next();
      BufferRenderer.draw(lv2.end());
      RenderSystem.depthFunc(515);
      this.program.disable();
      this.output.endWrite();
      this.input.endRead();
      Iterator var6 = this.samplerValues.iterator();

      while(var6.hasNext()) {
         Object object = var6.next();
         if (object instanceof Framebuffer) {
            ((Framebuffer)object).endRead();
         }
      }

   }

   public JsonEffectShaderProgram getProgram() {
      return this.program;
   }
}
