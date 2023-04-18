package net.minecraft.client.render;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public abstract class RenderPhase {
   private static final float VIEW_OFFSET_Z_LAYERING_SCALE = 0.99975586F;
   public static final double field_42230 = 8.0;
   protected final String name;
   private final Runnable beginAction;
   private final Runnable endAction;
   protected static final Transparency NO_TRANSPARENCY = new Transparency("no_transparency", () -> {
      RenderSystem.disableBlend();
   }, () -> {
   });
   protected static final Transparency ADDITIVE_TRANSPARENCY = new Transparency("additive_transparency", () -> {
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE);
   }, () -> {
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
   });
   protected static final Transparency LIGHTNING_TRANSPARENCY = new Transparency("lightning_transparency", () -> {
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
   }, () -> {
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
   });
   protected static final Transparency GLINT_TRANSPARENCY = new Transparency("glint_transparency", () -> {
      RenderSystem.enableBlend();
      RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_COLOR, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
   }, () -> {
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
   });
   protected static final Transparency CRUMBLING_TRANSPARENCY = new Transparency("crumbling_transparency", () -> {
      RenderSystem.enableBlend();
      RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.DST_COLOR, GlStateManager.DstFactor.SRC_COLOR, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
   }, () -> {
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
   });
   protected static final Transparency TRANSLUCENT_TRANSPARENCY = new Transparency("translucent_transparency", () -> {
      RenderSystem.enableBlend();
      RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
   }, () -> {
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
   });
   protected static final ShaderProgram NO_PROGRAM = new ShaderProgram();
   protected static final ShaderProgram BLOCK_PROGRAM = new ShaderProgram(GameRenderer::getBlockProgram);
   protected static final ShaderProgram NEW_ENTITY_PROGRAM = new ShaderProgram(GameRenderer::getNewEntityProgram);
   protected static final ShaderProgram POSITION_COLOR_LIGHTMAP_PROGRAM = new ShaderProgram(GameRenderer::getPositionColorLightmapProgram);
   protected static final ShaderProgram POSITION_PROGRAM = new ShaderProgram(GameRenderer::getPositionProgram);
   protected static final ShaderProgram POSITION_COLOR_TEXTURE_PROGRAM = new ShaderProgram(GameRenderer::getPositionColorTexProgram);
   protected static final ShaderProgram POSITION_TEXTURE_PROGRAM = new ShaderProgram(GameRenderer::getPositionTexProgram);
   protected static final ShaderProgram POSITION_COLOR_TEXTURE_LIGHTMAP_PROGRAM = new ShaderProgram(GameRenderer::getPositionColorTexLightmapProgram);
   protected static final ShaderProgram COLOR_PROGRAM = new ShaderProgram(GameRenderer::getPositionColorProgram);
   protected static final ShaderProgram SOLID_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeSolidProgram);
   protected static final ShaderProgram CUTOUT_MIPPED_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeCutoutMippedProgram);
   protected static final ShaderProgram CUTOUT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeCutoutProgram);
   protected static final ShaderProgram TRANSLUCENT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeTranslucentProgram);
   protected static final ShaderProgram TRANSLUCENT_MOVING_BLOCK_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeTranslucentMovingBlockProgram);
   protected static final ShaderProgram TRANSLUCENT_NO_CRUMBLING_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeTranslucentNoCrumblingProgram);
   protected static final ShaderProgram ARMOR_CUTOUT_NO_CULL_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeArmorCutoutNoCullProgram);
   protected static final ShaderProgram ENTITY_SOLID_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntitySolidProgram);
   protected static final ShaderProgram ENTITY_CUTOUT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityCutoutProgram);
   protected static final ShaderProgram ENTITY_CUTOUT_NONULL_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityCutoutNoNullProgram);
   protected static final ShaderProgram ENTITY_CUTOUT_NONULL_OFFSET_Z_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityCutoutNoNullZOffsetProgram);
   protected static final ShaderProgram ITEM_ENTITY_TRANSLUCENT_CULL_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeItemEntityTranslucentCullProgram);
   protected static final ShaderProgram ENTITY_TRANSLUCENT_CULL_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityTranslucentCullProgram);
   protected static final ShaderProgram ENTITY_TRANSLUCENT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityTranslucentProgram);
   protected static final ShaderProgram ENTITY_TRANSLUCENT_EMISSIVE_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityTranslucentEmissiveProgram);
   protected static final ShaderProgram ENTITY_SMOOTH_CUTOUT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntitySmoothCutoutProgram);
   protected static final ShaderProgram BEACON_BEAM_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeBeaconBeamProgram);
   protected static final ShaderProgram ENTITY_DECAL_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityDecalProgram);
   protected static final ShaderProgram ENTITY_NO_OUTLINE_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityNoOutlineProgram);
   protected static final ShaderProgram ENTITY_SHADOW_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityShadowProgram);
   protected static final ShaderProgram ENTITY_ALPHA_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityAlphaProgram);
   protected static final ShaderProgram EYES_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEyesProgram);
   protected static final ShaderProgram ENERGY_SWIRL_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEnergySwirlProgram);
   protected static final ShaderProgram LEASH_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeLeashProgram);
   protected static final ShaderProgram WATER_MASK_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeWaterMaskProgram);
   protected static final ShaderProgram OUTLINE_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeOutlineProgram);
   protected static final ShaderProgram ARMOR_GLINT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeArmorGlintProgram);
   protected static final ShaderProgram ARMOR_ENTITY_GLINT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeArmorEntityGlintProgram);
   protected static final ShaderProgram TRANSLUCENT_GLINT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeGlintTranslucentProgram);
   protected static final ShaderProgram GLINT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeGlintProgram);
   protected static final ShaderProgram DIRECT_GLINT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeGlintDirectProgram);
   protected static final ShaderProgram ENTITY_GLINT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityGlintProgram);
   protected static final ShaderProgram DIRECT_ENTITY_GLINT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEntityGlintDirectProgram);
   protected static final ShaderProgram CRUMBLING_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeCrumblingProgram);
   protected static final ShaderProgram TEXT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeTextProgram);
   protected static final ShaderProgram TEXT_BACKGROUND_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeTextBackgroundProgram);
   protected static final ShaderProgram TEXT_INTENSITY_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeTextIntensityProgram);
   protected static final ShaderProgram TRANSPARENT_TEXT_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeTextSeeThroughProgram);
   protected static final ShaderProgram TRANSPARENT_TEXT_BACKGROUND_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeTextBackgroundSeeThroughProgram);
   protected static final ShaderProgram TRANSPARENT_TEXT_INTENSITY_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeTextIntensitySeeThroughProgram);
   protected static final ShaderProgram LIGHTNING_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeLightningProgram);
   protected static final ShaderProgram TRIPWIRE_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeTripwireProgram);
   protected static final ShaderProgram END_PORTAL_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEndPortalProgram);
   protected static final ShaderProgram END_GATEWAY_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeEndGatewayProgram);
   protected static final ShaderProgram LINES_PROGRAM = new ShaderProgram(GameRenderer::getRenderTypeLinesProgram);
   protected static final Texture MIPMAP_BLOCK_ATLAS_TEXTURE;
   protected static final Texture BLOCK_ATLAS_TEXTURE;
   protected static final TextureBase NO_TEXTURE;
   protected static final Texturing DEFAULT_TEXTURING;
   protected static final Texturing GLINT_TEXTURING;
   protected static final Texturing ENTITY_GLINT_TEXTURING;
   protected static final Lightmap ENABLE_LIGHTMAP;
   protected static final Lightmap DISABLE_LIGHTMAP;
   protected static final Overlay ENABLE_OVERLAY_COLOR;
   protected static final Overlay DISABLE_OVERLAY_COLOR;
   protected static final Cull ENABLE_CULLING;
   protected static final Cull DISABLE_CULLING;
   protected static final DepthTest ALWAYS_DEPTH_TEST;
   protected static final DepthTest EQUAL_DEPTH_TEST;
   protected static final DepthTest LEQUAL_DEPTH_TEST;
   protected static final WriteMaskState ALL_MASK;
   protected static final WriteMaskState COLOR_MASK;
   protected static final WriteMaskState DEPTH_MASK;
   protected static final Layering NO_LAYERING;
   protected static final Layering POLYGON_OFFSET_LAYERING;
   protected static final Layering VIEW_OFFSET_Z_LAYERING;
   protected static final Target MAIN_TARGET;
   protected static final Target OUTLINE_TARGET;
   protected static final Target TRANSLUCENT_TARGET;
   protected static final Target PARTICLES_TARGET;
   protected static final Target WEATHER_TARGET;
   protected static final Target CLOUDS_TARGET;
   protected static final Target ITEM_TARGET;
   protected static final LineWidth FULL_LINE_WIDTH;

   public RenderPhase(String name, Runnable beginAction, Runnable endAction) {
      this.name = name;
      this.beginAction = beginAction;
      this.endAction = endAction;
   }

   public void startDrawing() {
      this.beginAction.run();
   }

   public void endDrawing() {
      this.endAction.run();
   }

   public String toString() {
      return this.name;
   }

   private static void setupGlintTexturing(float scale) {
      long l = (long)((double)Util.getMeasuringTimeMs() * (Double)MinecraftClient.getInstance().options.getGlintSpeed().getValue() * 8.0);
      float g = (float)(l % 110000L) / 110000.0F;
      float h = (float)(l % 30000L) / 30000.0F;
      Matrix4f matrix4f = (new Matrix4f()).translation(-g, h, 0.0F);
      matrix4f.rotateZ(0.17453292F).scale(scale);
      RenderSystem.setTextureMatrix(matrix4f);
   }

   static {
      MIPMAP_BLOCK_ATLAS_TEXTURE = new Texture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, false, true);
      BLOCK_ATLAS_TEXTURE = new Texture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, false, false);
      NO_TEXTURE = new TextureBase();
      DEFAULT_TEXTURING = new Texturing("default_texturing", () -> {
      }, () -> {
      });
      GLINT_TEXTURING = new Texturing("glint_texturing", () -> {
         setupGlintTexturing(8.0F);
      }, () -> {
         RenderSystem.resetTextureMatrix();
      });
      ENTITY_GLINT_TEXTURING = new Texturing("entity_glint_texturing", () -> {
         setupGlintTexturing(0.16F);
      }, () -> {
         RenderSystem.resetTextureMatrix();
      });
      ENABLE_LIGHTMAP = new Lightmap(true);
      DISABLE_LIGHTMAP = new Lightmap(false);
      ENABLE_OVERLAY_COLOR = new Overlay(true);
      DISABLE_OVERLAY_COLOR = new Overlay(false);
      ENABLE_CULLING = new Cull(true);
      DISABLE_CULLING = new Cull(false);
      ALWAYS_DEPTH_TEST = new DepthTest("always", 519);
      EQUAL_DEPTH_TEST = new DepthTest("==", 514);
      LEQUAL_DEPTH_TEST = new DepthTest("<=", 515);
      ALL_MASK = new WriteMaskState(true, true);
      COLOR_MASK = new WriteMaskState(true, false);
      DEPTH_MASK = new WriteMaskState(false, true);
      NO_LAYERING = new Layering("no_layering", () -> {
      }, () -> {
      });
      POLYGON_OFFSET_LAYERING = new Layering("polygon_offset_layering", () -> {
         RenderSystem.polygonOffset(-1.0F, -10.0F);
         RenderSystem.enablePolygonOffset();
      }, () -> {
         RenderSystem.polygonOffset(0.0F, 0.0F);
         RenderSystem.disablePolygonOffset();
      });
      VIEW_OFFSET_Z_LAYERING = new Layering("view_offset_z_layering", () -> {
         MatrixStack lv = RenderSystem.getModelViewStack();
         lv.push();
         lv.scale(0.99975586F, 0.99975586F, 0.99975586F);
         RenderSystem.applyModelViewMatrix();
      }, () -> {
         MatrixStack lv = RenderSystem.getModelViewStack();
         lv.pop();
         RenderSystem.applyModelViewMatrix();
      });
      MAIN_TARGET = new Target("main_target", () -> {
      }, () -> {
      });
      OUTLINE_TARGET = new Target("outline_target", () -> {
         MinecraftClient.getInstance().worldRenderer.getEntityOutlinesFramebuffer().beginWrite(false);
      }, () -> {
         MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
      });
      TRANSLUCENT_TARGET = new Target("translucent_target", () -> {
         if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().worldRenderer.getTranslucentFramebuffer().beginWrite(false);
         }

      }, () -> {
         if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
         }

      });
      PARTICLES_TARGET = new Target("particles_target", () -> {
         if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().worldRenderer.getParticlesFramebuffer().beginWrite(false);
         }

      }, () -> {
         if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
         }

      });
      WEATHER_TARGET = new Target("weather_target", () -> {
         if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().worldRenderer.getWeatherFramebuffer().beginWrite(false);
         }

      }, () -> {
         if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
         }

      });
      CLOUDS_TARGET = new Target("clouds_target", () -> {
         if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().worldRenderer.getCloudsFramebuffer().beginWrite(false);
         }

      }, () -> {
         if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
         }

      });
      ITEM_TARGET = new Target("item_entity_target", () -> {
         if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().worldRenderer.getEntityFramebuffer().beginWrite(false);
         }

      }, () -> {
         if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
         }

      });
      FULL_LINE_WIDTH = new LineWidth(OptionalDouble.of(1.0));
   }

   @Environment(EnvType.CLIENT)
   protected static class Transparency extends RenderPhase {
      public Transparency(String string, Runnable runnable, Runnable runnable2) {
         super(string, runnable, runnable2);
      }
   }

   @Environment(EnvType.CLIENT)
   protected static class ShaderProgram extends RenderPhase {
      private final Optional supplier;

      public ShaderProgram(Supplier supplier) {
         super("shader", () -> {
            RenderSystem.setShader(supplier);
         }, () -> {
         });
         this.supplier = Optional.of(supplier);
      }

      public ShaderProgram() {
         super("shader", () -> {
            RenderSystem.setShader(() -> {
               return null;
            });
         }, () -> {
         });
         this.supplier = Optional.empty();
      }

      public String toString() {
         return this.name + "[" + this.supplier + "]";
      }
   }

   @Environment(EnvType.CLIENT)
   protected static class Texture extends TextureBase {
      private final Optional id;
      private final boolean blur;
      private final boolean mipmap;

      public Texture(Identifier id, boolean blur, boolean mipmap) {
         super(() -> {
            TextureManager lv = MinecraftClient.getInstance().getTextureManager();
            lv.getTexture(id).setFilter(blur, mipmap);
            RenderSystem.setShaderTexture(0, id);
         }, () -> {
         });
         this.id = Optional.of(id);
         this.blur = blur;
         this.mipmap = mipmap;
      }

      public String toString() {
         return this.name + "[" + this.id + "(blur=" + this.blur + ", mipmap=" + this.mipmap + ")]";
      }

      protected Optional getId() {
         return this.id;
      }
   }

   @Environment(EnvType.CLIENT)
   protected static class TextureBase extends RenderPhase {
      public TextureBase(Runnable apply, Runnable unapply) {
         super("texture", apply, unapply);
      }

      TextureBase() {
         super("texture", () -> {
         }, () -> {
         });
      }

      protected Optional getId() {
         return Optional.empty();
      }
   }

   @Environment(EnvType.CLIENT)
   protected static class Texturing extends RenderPhase {
      public Texturing(String string, Runnable runnable, Runnable runnable2) {
         super(string, runnable, runnable2);
      }
   }

   @Environment(EnvType.CLIENT)
   protected static class Lightmap extends Toggleable {
      public Lightmap(boolean lightmap) {
         super("lightmap", () -> {
            if (lightmap) {
               MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().enable();
            }

         }, () -> {
            if (lightmap) {
               MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().disable();
            }

         }, lightmap);
      }
   }

   @Environment(EnvType.CLIENT)
   protected static class Overlay extends Toggleable {
      public Overlay(boolean overlayColor) {
         super("overlay", () -> {
            if (overlayColor) {
               MinecraftClient.getInstance().gameRenderer.getOverlayTexture().setupOverlayColor();
            }

         }, () -> {
            if (overlayColor) {
               MinecraftClient.getInstance().gameRenderer.getOverlayTexture().teardownOverlayColor();
            }

         }, overlayColor);
      }
   }

   @Environment(EnvType.CLIENT)
   protected static class Cull extends Toggleable {
      public Cull(boolean culling) {
         super("cull", () -> {
            if (!culling) {
               RenderSystem.disableCull();
            }

         }, () -> {
            if (!culling) {
               RenderSystem.enableCull();
            }

         }, culling);
      }
   }

   @Environment(EnvType.CLIENT)
   protected static class DepthTest extends RenderPhase {
      private final String depthFunctionName;

      public DepthTest(String depthFunctionName, int depthFunction) {
         super("depth_test", () -> {
            if (depthFunction != 519) {
               RenderSystem.enableDepthTest();
               RenderSystem.depthFunc(depthFunction);
            }

         }, () -> {
            if (depthFunction != 519) {
               RenderSystem.disableDepthTest();
               RenderSystem.depthFunc(515);
            }

         });
         this.depthFunctionName = depthFunctionName;
      }

      public String toString() {
         return this.name + "[" + this.depthFunctionName + "]";
      }
   }

   @Environment(EnvType.CLIENT)
   protected static class WriteMaskState extends RenderPhase {
      private final boolean color;
      private final boolean depth;

      public WriteMaskState(boolean color, boolean depth) {
         super("write_mask_state", () -> {
            if (!depth) {
               RenderSystem.depthMask(depth);
            }

            if (!color) {
               RenderSystem.colorMask(color, color, color, color);
            }

         }, () -> {
            if (!depth) {
               RenderSystem.depthMask(true);
            }

            if (!color) {
               RenderSystem.colorMask(true, true, true, true);
            }

         });
         this.color = color;
         this.depth = depth;
      }

      public String toString() {
         return this.name + "[writeColor=" + this.color + ", writeDepth=" + this.depth + "]";
      }
   }

   @Environment(EnvType.CLIENT)
   protected static class Layering extends RenderPhase {
      public Layering(String string, Runnable runnable, Runnable runnable2) {
         super(string, runnable, runnable2);
      }
   }

   @Environment(EnvType.CLIENT)
   protected static class Target extends RenderPhase {
      public Target(String string, Runnable runnable, Runnable runnable2) {
         super(string, runnable, runnable2);
      }
   }

   @Environment(EnvType.CLIENT)
   protected static class LineWidth extends RenderPhase {
      private final OptionalDouble width;

      public LineWidth(OptionalDouble width) {
         super("line_width", () -> {
            if (!Objects.equals(width, OptionalDouble.of(1.0))) {
               if (width.isPresent()) {
                  RenderSystem.lineWidth((float)width.getAsDouble());
               } else {
                  RenderSystem.lineWidth(Math.max(2.5F, (float)MinecraftClient.getInstance().getWindow().getFramebufferWidth() / 1920.0F * 2.5F));
               }
            }

         }, () -> {
            if (!Objects.equals(width, OptionalDouble.of(1.0))) {
               RenderSystem.lineWidth(1.0F);
            }

         });
         this.width = width;
      }

      public String toString() {
         String var10000 = this.name;
         return var10000 + "[" + (this.width.isPresent() ? this.width.getAsDouble() : "window_scale") + "]";
      }
   }

   @Environment(EnvType.CLIENT)
   private static class Toggleable extends RenderPhase {
      private final boolean enabled;

      public Toggleable(String name, Runnable apply, Runnable unapply, boolean enabled) {
         super(name, apply, unapply);
         this.enabled = enabled;
      }

      public String toString() {
         return this.name + "[" + this.enabled + "]";
      }
   }

   @Environment(EnvType.CLIENT)
   protected static final class OffsetTexturing extends Texturing {
      public OffsetTexturing(float x, float y) {
         super("offset_texturing", () -> {
            RenderSystem.setTextureMatrix((new Matrix4f()).translation(x, y, 0.0F));
         }, () -> {
            RenderSystem.resetTextureMatrix();
         });
      }
   }

   @Environment(EnvType.CLIENT)
   protected static class Textures extends TextureBase {
      private final Optional id;

      Textures(ImmutableList textures) {
         super(() -> {
            int i = 0;
            UnmodifiableIterator var2 = textures.iterator();

            while(var2.hasNext()) {
               Triple triple = (Triple)var2.next();
               TextureManager lv = MinecraftClient.getInstance().getTextureManager();
               lv.getTexture((Identifier)triple.getLeft()).setFilter((Boolean)triple.getMiddle(), (Boolean)triple.getRight());
               RenderSystem.setShaderTexture(i++, (Identifier)triple.getLeft());
            }

         }, () -> {
         });
         this.id = textures.stream().findFirst().map(Triple::getLeft);
      }

      protected Optional getId() {
         return this.id;
      }

      public static Builder create() {
         return new Builder();
      }

      @Environment(EnvType.CLIENT)
      public static final class Builder {
         private final ImmutableList.Builder textures = new ImmutableList.Builder();

         public Builder add(Identifier id, boolean blur, boolean mipmap) {
            this.textures.add(Triple.of(id, blur, mipmap));
            return this;
         }

         public Textures build() {
            return new Textures(this.textures.build());
         }
      }
   }
}
