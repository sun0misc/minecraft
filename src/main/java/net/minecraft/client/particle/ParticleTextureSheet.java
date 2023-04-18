package net.minecraft.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;

@Environment(EnvType.CLIENT)
public interface ParticleTextureSheet {
   ParticleTextureSheet TERRAIN_SHEET = new ParticleTextureSheet() {
      public void begin(BufferBuilder builder, TextureManager textureManager) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.depthMask(true);
         RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
         builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
      }

      public void draw(Tessellator tessellator) {
         tessellator.draw();
      }

      public String toString() {
         return "TERRAIN_SHEET";
      }
   };
   ParticleTextureSheet PARTICLE_SHEET_OPAQUE = new ParticleTextureSheet() {
      public void begin(BufferBuilder builder, TextureManager textureManager) {
         RenderSystem.disableBlend();
         RenderSystem.depthMask(true);
         RenderSystem.setShader(GameRenderer::getParticleProgram);
         RenderSystem.setShaderTexture(0, SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
         builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
      }

      public void draw(Tessellator tessellator) {
         tessellator.draw();
      }

      public String toString() {
         return "PARTICLE_SHEET_OPAQUE";
      }
   };
   ParticleTextureSheet PARTICLE_SHEET_TRANSLUCENT = new ParticleTextureSheet() {
      public void begin(BufferBuilder builder, TextureManager textureManager) {
         RenderSystem.depthMask(true);
         RenderSystem.setShaderTexture(0, SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
      }

      public void draw(Tessellator tessellator) {
         tessellator.draw();
      }

      public String toString() {
         return "PARTICLE_SHEET_TRANSLUCENT";
      }
   };
   ParticleTextureSheet PARTICLE_SHEET_LIT = new ParticleTextureSheet() {
      public void begin(BufferBuilder builder, TextureManager textureManager) {
         RenderSystem.disableBlend();
         RenderSystem.depthMask(true);
         RenderSystem.setShaderTexture(0, SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
         builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
      }

      public void draw(Tessellator tessellator) {
         tessellator.draw();
      }

      public String toString() {
         return "PARTICLE_SHEET_LIT";
      }
   };
   ParticleTextureSheet CUSTOM = new ParticleTextureSheet() {
      public void begin(BufferBuilder builder, TextureManager textureManager) {
         RenderSystem.depthMask(true);
         RenderSystem.disableBlend();
      }

      public void draw(Tessellator tessellator) {
      }

      public String toString() {
         return "CUSTOM";
      }
   };
   ParticleTextureSheet NO_RENDER = new ParticleTextureSheet() {
      public void begin(BufferBuilder builder, TextureManager textureManager) {
      }

      public void draw(Tessellator tessellator) {
      }

      public String toString() {
         return "NO_RENDER";
      }
   };

   void begin(BufferBuilder builder, TextureManager textureManager);

   void draw(Tessellator tessellator);
}
