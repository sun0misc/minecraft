package net.minecraft.client.render;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderStage;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class GameRenderer implements AutoCloseable {
   private static final Identifier NAUSEA_OVERLAY = new Identifier("textures/misc/nausea.png");
   static final Logger LOGGER = LogUtils.getLogger();
   private static final boolean field_32688 = false;
   public static final float CAMERA_DEPTH = 0.05F;
   final MinecraftClient client;
   private final ResourceManager resourceManager;
   private final Random random = Random.create();
   private float viewDistance;
   public final HeldItemRenderer firstPersonRenderer;
   private final MapRenderer mapRenderer;
   private final BufferBuilderStorage buffers;
   private int ticks;
   private float fovMultiplier;
   private float lastFovMultiplier;
   private float skyDarkness;
   private float lastSkyDarkness;
   private boolean renderHand = true;
   private boolean blockOutlineEnabled = true;
   private long lastWorldIconUpdate;
   private boolean hasWorldIcon;
   private long lastWindowFocusedTime = Util.getMeasuringTimeMs();
   private final LightmapTextureManager lightmapTextureManager;
   private final OverlayTexture overlayTexture = new OverlayTexture();
   private boolean renderingPanorama;
   private float zoom = 1.0F;
   private float zoomX;
   private float zoomY;
   public static final int field_32687 = 40;
   @Nullable
   private ItemStack floatingItem;
   private int floatingItemTimeLeft;
   private float floatingItemWidth;
   private float floatingItemHeight;
   @Nullable
   PostEffectProcessor postProcessor;
   static final Identifier[] SUPER_SECRET_SETTING_PROGRAMS = new Identifier[]{new Identifier("shaders/post/notch.json"), new Identifier("shaders/post/fxaa.json"), new Identifier("shaders/post/art.json"), new Identifier("shaders/post/bumpy.json"), new Identifier("shaders/post/blobs2.json"), new Identifier("shaders/post/pencil.json"), new Identifier("shaders/post/color_convolve.json"), new Identifier("shaders/post/deconverge.json"), new Identifier("shaders/post/flip.json"), new Identifier("shaders/post/invert.json"), new Identifier("shaders/post/ntsc.json"), new Identifier("shaders/post/outline.json"), new Identifier("shaders/post/phosphor.json"), new Identifier("shaders/post/scan_pincushion.json"), new Identifier("shaders/post/sobel.json"), new Identifier("shaders/post/bits.json"), new Identifier("shaders/post/desaturate.json"), new Identifier("shaders/post/green.json"), new Identifier("shaders/post/blur.json"), new Identifier("shaders/post/wobble.json"), new Identifier("shaders/post/blobs.json"), new Identifier("shaders/post/antialias.json"), new Identifier("shaders/post/creeper.json"), new Identifier("shaders/post/spider.json")};
   public static final int SUPER_SECRET_SETTING_COUNT;
   int superSecretSettingIndex;
   private boolean postProcessorEnabled;
   private final Camera camera;
   public ShaderProgram blitScreenProgram;
   private final Map programs;
   @Nullable
   private static ShaderProgram positionProgram;
   @Nullable
   private static ShaderProgram positionColorProgram;
   @Nullable
   private static ShaderProgram positionColorTexProgram;
   @Nullable
   private static ShaderProgram positionTexProgram;
   @Nullable
   private static ShaderProgram positionTexColorProgram;
   @Nullable
   private static ShaderProgram blockProgram;
   @Nullable
   private static ShaderProgram newEntityProgram;
   @Nullable
   private static ShaderProgram particleProgram;
   @Nullable
   private static ShaderProgram positionColorLightmapProgram;
   @Nullable
   private static ShaderProgram positionColorTexLightmapProgram;
   @Nullable
   private static ShaderProgram positionTexColorNormalProgram;
   @Nullable
   private static ShaderProgram positionTexLightmapColorProgram;
   @Nullable
   private static ShaderProgram renderTypeSolidProgram;
   @Nullable
   private static ShaderProgram renderTypeCutoutMippedProgram;
   @Nullable
   private static ShaderProgram renderTypeCutoutProgram;
   @Nullable
   private static ShaderProgram renderTypeTranslucentProgram;
   @Nullable
   private static ShaderProgram renderTypeTranslucentMovingBlockProgram;
   @Nullable
   private static ShaderProgram renderTypeTranslucentNoCrumblingProgram;
   @Nullable
   private static ShaderProgram renderTypeArmorCutoutNoCullProgram;
   @Nullable
   private static ShaderProgram renderTypeEntitySolidProgram;
   @Nullable
   private static ShaderProgram renderTypeEntityCutoutProgram;
   @Nullable
   private static ShaderProgram renderTypeEntityCutoutNoNullProgram;
   @Nullable
   private static ShaderProgram renderTypeEntityCutoutNoNullZOffsetProgram;
   @Nullable
   private static ShaderProgram renderTypeItemEntityTranslucentCullProgram;
   @Nullable
   private static ShaderProgram renderTypeEntityTranslucentCullProgram;
   @Nullable
   private static ShaderProgram renderTypeEntityTranslucentProgram;
   @Nullable
   private static ShaderProgram renderTypeEntityTranslucentEmissiveProgram;
   @Nullable
   private static ShaderProgram renderTypeEntitySmoothCutoutProgram;
   @Nullable
   private static ShaderProgram renderTypeBeaconBeamProgram;
   @Nullable
   private static ShaderProgram renderTypeEntityDecalProgram;
   @Nullable
   private static ShaderProgram renderTypeEntityNoOutlineProgram;
   @Nullable
   private static ShaderProgram renderTypeEntityShadowProgram;
   @Nullable
   private static ShaderProgram renderTypeEntityAlphaProgram;
   @Nullable
   private static ShaderProgram renderTypeEyesProgram;
   @Nullable
   private static ShaderProgram renderTypeEnergySwirlProgram;
   @Nullable
   private static ShaderProgram renderTypeLeashProgram;
   @Nullable
   private static ShaderProgram renderTypeWaterMaskProgram;
   @Nullable
   private static ShaderProgram renderTypeOutlineProgram;
   @Nullable
   private static ShaderProgram renderTypeArmorGlintProgram;
   @Nullable
   private static ShaderProgram renderTypeArmorEntityGlintProgram;
   @Nullable
   private static ShaderProgram renderTypeGlintTranslucentProgram;
   @Nullable
   private static ShaderProgram renderTypeGlintProgram;
   @Nullable
   private static ShaderProgram renderTypeGlintDirectProgram;
   @Nullable
   private static ShaderProgram renderTypeEntityGlintProgram;
   @Nullable
   private static ShaderProgram renderTypeEntityGlintDirectProgram;
   @Nullable
   private static ShaderProgram renderTypeTextProgram;
   @Nullable
   private static ShaderProgram renderTypeTextBackgroundProgram;
   @Nullable
   private static ShaderProgram renderTypeTextIntensityProgram;
   @Nullable
   private static ShaderProgram renderTypeTextSeeThroughProgram;
   @Nullable
   private static ShaderProgram renderTypeTextBackgroundSeeThroughProgram;
   @Nullable
   private static ShaderProgram renderTypeTextIntensitySeeThroughProgram;
   @Nullable
   private static ShaderProgram renderTypeLightningProgram;
   @Nullable
   private static ShaderProgram renderTypeTripwireProgram;
   @Nullable
   private static ShaderProgram renderTypeEndPortalProgram;
   @Nullable
   private static ShaderProgram renderTypeEndGatewayProgram;
   @Nullable
   private static ShaderProgram renderTypeLinesProgram;
   @Nullable
   private static ShaderProgram renderTypeCrumblingProgram;

   public GameRenderer(MinecraftClient client, HeldItemRenderer heldItemRenderer, ResourceManager resourceManager, BufferBuilderStorage buffers) {
      this.superSecretSettingIndex = SUPER_SECRET_SETTING_COUNT;
      this.camera = new Camera();
      this.programs = Maps.newHashMap();
      this.client = client;
      this.resourceManager = resourceManager;
      this.firstPersonRenderer = heldItemRenderer;
      this.mapRenderer = new MapRenderer(client.getTextureManager());
      this.lightmapTextureManager = new LightmapTextureManager(this, client);
      this.buffers = buffers;
      this.postProcessor = null;
   }

   public void close() {
      this.lightmapTextureManager.close();
      this.mapRenderer.close();
      this.overlayTexture.close();
      this.disablePostProcessor();
      this.clearPrograms();
      if (this.blitScreenProgram != null) {
         this.blitScreenProgram.close();
      }

   }

   public void setRenderHand(boolean renderHand) {
      this.renderHand = renderHand;
   }

   public void setBlockOutlineEnabled(boolean blockOutlineEnabled) {
      this.blockOutlineEnabled = blockOutlineEnabled;
   }

   public void setRenderingPanorama(boolean renderingPanorama) {
      this.renderingPanorama = renderingPanorama;
   }

   public boolean isRenderingPanorama() {
      return this.renderingPanorama;
   }

   public void disablePostProcessor() {
      if (this.postProcessor != null) {
         this.postProcessor.close();
      }

      this.postProcessor = null;
      this.superSecretSettingIndex = SUPER_SECRET_SETTING_COUNT;
   }

   public void togglePostProcessorEnabled() {
      this.postProcessorEnabled = !this.postProcessorEnabled;
   }

   public void onCameraEntitySet(@Nullable Entity entity) {
      if (this.postProcessor != null) {
         this.postProcessor.close();
      }

      this.postProcessor = null;
      if (entity instanceof CreeperEntity) {
         this.loadPostProcessor(new Identifier("shaders/post/creeper.json"));
      } else if (entity instanceof SpiderEntity) {
         this.loadPostProcessor(new Identifier("shaders/post/spider.json"));
      } else if (entity instanceof EndermanEntity) {
         this.loadPostProcessor(new Identifier("shaders/post/invert.json"));
      }

   }

   public void cycleSuperSecretSetting() {
      if (this.client.getCameraEntity() instanceof PlayerEntity) {
         if (this.postProcessor != null) {
            this.postProcessor.close();
         }

         this.superSecretSettingIndex = (this.superSecretSettingIndex + 1) % (SUPER_SECRET_SETTING_PROGRAMS.length + 1);
         if (this.superSecretSettingIndex == SUPER_SECRET_SETTING_COUNT) {
            this.postProcessor = null;
         } else {
            this.loadPostProcessor(SUPER_SECRET_SETTING_PROGRAMS[this.superSecretSettingIndex]);
         }

      }
   }

   void loadPostProcessor(Identifier id) {
      if (this.postProcessor != null) {
         this.postProcessor.close();
      }

      try {
         this.postProcessor = new PostEffectProcessor(this.client.getTextureManager(), this.resourceManager, this.client.getFramebuffer(), id);
         this.postProcessor.setupDimensions(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight());
         this.postProcessorEnabled = true;
      } catch (IOException var3) {
         LOGGER.warn("Failed to load shader: {}", id, var3);
         this.superSecretSettingIndex = SUPER_SECRET_SETTING_COUNT;
         this.postProcessorEnabled = false;
      } catch (JsonSyntaxException var4) {
         LOGGER.warn("Failed to parse shader: {}", id, var4);
         this.superSecretSettingIndex = SUPER_SECRET_SETTING_COUNT;
         this.postProcessorEnabled = false;
      }

   }

   public ResourceReloader createProgramReloader() {
      return new SinglePreparationResourceReloader() {
         protected CachedResourceFactory prepare(ResourceManager arg, Profiler arg2) {
            Map map = arg.findResources("shaders", (id) -> {
               String string = id.getPath();
               return string.endsWith(".json") || string.endsWith(ShaderStage.Type.FRAGMENT.getFileExtension()) || string.endsWith(ShaderStage.Type.VERTEX.getFileExtension()) || string.endsWith(".glsl");
            });
            Map map2 = new HashMap();
            map.forEach((id, resource) -> {
               try {
                  InputStream inputStream = resource.getInputStream();

                  try {
                     byte[] bs = inputStream.readAllBytes();
                     map2.put(id, new Resource(resource.getPack(), () -> {
                        return new ByteArrayInputStream(bs);
                     }));
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
               } catch (Exception var8) {
                  GameRenderer.LOGGER.warn("Failed to read resource {}", id, var8);
               }

            });
            return new CachedResourceFactory(arg, map2);
         }

         protected void apply(CachedResourceFactory arg, ResourceManager arg2, Profiler arg3) {
            GameRenderer.this.loadPrograms(arg);
            if (GameRenderer.this.postProcessor != null) {
               GameRenderer.this.postProcessor.close();
            }

            GameRenderer.this.postProcessor = null;
            if (GameRenderer.this.superSecretSettingIndex == GameRenderer.SUPER_SECRET_SETTING_COUNT) {
               GameRenderer.this.onCameraEntitySet(GameRenderer.this.client.getCameraEntity());
            } else {
               GameRenderer.this.loadPostProcessor(GameRenderer.SUPER_SECRET_SETTING_PROGRAMS[GameRenderer.this.superSecretSettingIndex]);
            }

         }

         public String getName() {
            return "Shader Loader";
         }

         // $FF: synthetic method
         protected Object prepare(ResourceManager manager, Profiler profiler) {
            return this.prepare(manager, profiler);
         }
      };
   }

   public void preloadPrograms(ResourceFactory factory) {
      if (this.blitScreenProgram != null) {
         throw new RuntimeException("Blit shader already preloaded");
      } else {
         try {
            this.blitScreenProgram = new ShaderProgram(factory, "blit_screen", VertexFormats.BLIT_SCREEN);
         } catch (IOException var3) {
            throw new RuntimeException("could not preload blit shader", var3);
         }

         positionProgram = this.preloadProgram(factory, "position", VertexFormats.POSITION);
         positionColorProgram = this.preloadProgram(factory, "position_color", VertexFormats.POSITION_COLOR);
         positionColorTexProgram = this.preloadProgram(factory, "position_color_tex", VertexFormats.POSITION_COLOR_TEXTURE);
         positionTexProgram = this.preloadProgram(factory, "position_tex", VertexFormats.POSITION_TEXTURE);
         positionTexColorProgram = this.preloadProgram(factory, "position_tex_color", VertexFormats.POSITION_TEXTURE_COLOR);
         renderTypeTextProgram = this.preloadProgram(factory, "rendertype_text", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);
      }
   }

   private ShaderProgram preloadProgram(ResourceFactory factory, String name, VertexFormat format) {
      try {
         ShaderProgram lv = new ShaderProgram(factory, name, format);
         this.programs.put(name, lv);
         return lv;
      } catch (Exception var5) {
         throw new IllegalStateException("could not preload shader " + name, var5);
      }
   }

   void loadPrograms(ResourceFactory factory) {
      RenderSystem.assertOnRenderThread();
      List list = Lists.newArrayList();
      list.addAll(ShaderStage.Type.FRAGMENT.getLoadedShaders().values());
      list.addAll(ShaderStage.Type.VERTEX.getLoadedShaders().values());
      list.forEach(ShaderStage::release);
      List list2 = Lists.newArrayListWithCapacity(this.programs.size());

      try {
         list2.add(Pair.of(new ShaderProgram(factory, "block", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL), (program) -> {
            blockProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "new_entity", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), (program) -> {
            newEntityProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "particle", VertexFormats.POSITION_TEXTURE_COLOR_LIGHT), (program) -> {
            particleProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "position", VertexFormats.POSITION), (program) -> {
            positionProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "position_color", VertexFormats.POSITION_COLOR), (program) -> {
            positionColorProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "position_color_lightmap", VertexFormats.POSITION_COLOR_LIGHT), (program) -> {
            positionColorLightmapProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "position_color_tex", VertexFormats.POSITION_COLOR_TEXTURE), (program) -> {
            positionColorTexProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "position_color_tex_lightmap", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT), (program) -> {
            positionColorTexLightmapProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "position_tex", VertexFormats.POSITION_TEXTURE), (program) -> {
            positionTexProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "position_tex_color", VertexFormats.POSITION_TEXTURE_COLOR), (program) -> {
            positionTexColorProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "position_tex_color_normal", VertexFormats.POSITION_TEXTURE_COLOR_NORMAL), (program) -> {
            positionTexColorNormalProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "position_tex_lightmap_color", VertexFormats.POSITION_TEXTURE_LIGHT_COLOR), (program) -> {
            positionTexLightmapColorProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_solid", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL), (program) -> {
            renderTypeSolidProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_cutout_mipped", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL), (program) -> {
            renderTypeCutoutMippedProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_cutout", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL), (program) -> {
            renderTypeCutoutProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_translucent", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL), (program) -> {
            renderTypeTranslucentProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_translucent_moving_block", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL), (program) -> {
            renderTypeTranslucentMovingBlockProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_translucent_no_crumbling", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL), (program) -> {
            renderTypeTranslucentNoCrumblingProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_armor_cutout_no_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), (program) -> {
            renderTypeArmorCutoutNoCullProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_solid", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), (program) -> {
            renderTypeEntitySolidProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_cutout", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), (program) -> {
            renderTypeEntityCutoutProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_cutout_no_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), (program) -> {
            renderTypeEntityCutoutNoNullProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_cutout_no_cull_z_offset", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), (program) -> {
            renderTypeEntityCutoutNoNullZOffsetProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_item_entity_translucent_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), (program) -> {
            renderTypeItemEntityTranslucentCullProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_translucent_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), (program) -> {
            renderTypeEntityTranslucentCullProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_translucent", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), (program) -> {
            renderTypeEntityTranslucentProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_translucent_emissive", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), (program) -> {
            renderTypeEntityTranslucentEmissiveProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_smooth_cutout", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), (program) -> {
            renderTypeEntitySmoothCutoutProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_beacon_beam", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL), (program) -> {
            renderTypeBeaconBeamProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_decal", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), (program) -> {
            renderTypeEntityDecalProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_no_outline", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), (program) -> {
            renderTypeEntityNoOutlineProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_shadow", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), (program) -> {
            renderTypeEntityShadowProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_alpha", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), (program) -> {
            renderTypeEntityAlphaProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_eyes", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), (program) -> {
            renderTypeEyesProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_energy_swirl", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), (program) -> {
            renderTypeEnergySwirlProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_leash", VertexFormats.POSITION_COLOR_LIGHT), (program) -> {
            renderTypeLeashProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_water_mask", VertexFormats.POSITION), (program) -> {
            renderTypeWaterMaskProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_outline", VertexFormats.POSITION_COLOR_TEXTURE), (program) -> {
            renderTypeOutlineProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_armor_glint", VertexFormats.POSITION_TEXTURE), (program) -> {
            renderTypeArmorGlintProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_armor_entity_glint", VertexFormats.POSITION_TEXTURE), (program) -> {
            renderTypeArmorEntityGlintProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_glint_translucent", VertexFormats.POSITION_TEXTURE), (program) -> {
            renderTypeGlintTranslucentProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_glint", VertexFormats.POSITION_TEXTURE), (program) -> {
            renderTypeGlintProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_glint_direct", VertexFormats.POSITION_TEXTURE), (program) -> {
            renderTypeGlintDirectProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_glint", VertexFormats.POSITION_TEXTURE), (program) -> {
            renderTypeEntityGlintProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_entity_glint_direct", VertexFormats.POSITION_TEXTURE), (program) -> {
            renderTypeEntityGlintDirectProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_text", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT), (program) -> {
            renderTypeTextProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_text_background", VertexFormats.POSITION_COLOR_LIGHT), (program) -> {
            renderTypeTextBackgroundProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_text_intensity", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT), (program) -> {
            renderTypeTextIntensityProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_text_see_through", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT), (program) -> {
            renderTypeTextSeeThroughProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_text_background_see_through", VertexFormats.POSITION_COLOR_LIGHT), (program) -> {
            renderTypeTextBackgroundSeeThroughProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_text_intensity_see_through", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT), (program) -> {
            renderTypeTextIntensitySeeThroughProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_lightning", VertexFormats.POSITION_COLOR), (program) -> {
            renderTypeLightningProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_tripwire", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL), (program) -> {
            renderTypeTripwireProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_end_portal", VertexFormats.POSITION), (program) -> {
            renderTypeEndPortalProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_end_gateway", VertexFormats.POSITION), (program) -> {
            renderTypeEndGatewayProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_lines", VertexFormats.LINES), (program) -> {
            renderTypeLinesProgram = program;
         }));
         list2.add(Pair.of(new ShaderProgram(factory, "rendertype_crumbling", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL), (program) -> {
            renderTypeCrumblingProgram = program;
         }));
      } catch (IOException var5) {
         list2.forEach((pair) -> {
            ((ShaderProgram)pair.getFirst()).close();
         });
         throw new RuntimeException("could not reload shaders", var5);
      }

      this.clearPrograms();
      list2.forEach((pair) -> {
         ShaderProgram lv = (ShaderProgram)pair.getFirst();
         this.programs.put(lv.getName(), lv);
         ((Consumer)pair.getSecond()).accept(lv);
      });
   }

   private void clearPrograms() {
      RenderSystem.assertOnRenderThread();
      this.programs.values().forEach(ShaderProgram::close);
      this.programs.clear();
   }

   @Nullable
   public ShaderProgram getProgram(@Nullable String name) {
      return name == null ? null : (ShaderProgram)this.programs.get(name);
   }

   public void tick() {
      this.updateFovMultiplier();
      this.lightmapTextureManager.tick();
      if (this.client.getCameraEntity() == null) {
         this.client.setCameraEntity(this.client.player);
      }

      this.camera.updateEyeHeight();
      ++this.ticks;
      this.firstPersonRenderer.updateHeldItems();
      this.client.worldRenderer.tickRainSplashing(this.camera);
      this.lastSkyDarkness = this.skyDarkness;
      if (this.client.inGameHud.getBossBarHud().shouldDarkenSky()) {
         this.skyDarkness += 0.05F;
         if (this.skyDarkness > 1.0F) {
            this.skyDarkness = 1.0F;
         }
      } else if (this.skyDarkness > 0.0F) {
         this.skyDarkness -= 0.0125F;
      }

      if (this.floatingItemTimeLeft > 0) {
         --this.floatingItemTimeLeft;
         if (this.floatingItemTimeLeft == 0) {
            this.floatingItem = null;
         }
      }

   }

   @Nullable
   public PostEffectProcessor getPostProcessor() {
      return this.postProcessor;
   }

   public void onResized(int width, int height) {
      if (this.postProcessor != null) {
         this.postProcessor.setupDimensions(width, height);
      }

      this.client.worldRenderer.onResized(width, height);
   }

   public void updateTargetedEntity(float tickDelta) {
      Entity lv = this.client.getCameraEntity();
      if (lv != null) {
         if (this.client.world != null) {
            this.client.getProfiler().push("pick");
            this.client.targetedEntity = null;
            double d = (double)this.client.interactionManager.getReachDistance();
            this.client.crosshairTarget = lv.raycast(d, tickDelta, false);
            Vec3d lv2 = lv.getCameraPosVec(tickDelta);
            boolean bl = false;
            int i = true;
            double e = d;
            if (this.client.interactionManager.hasExtendedReach()) {
               e = 6.0;
               d = e;
            } else {
               if (d > 3.0) {
                  bl = true;
               }

               d = d;
            }

            e *= e;
            if (this.client.crosshairTarget != null) {
               e = this.client.crosshairTarget.getPos().squaredDistanceTo(lv2);
            }

            Vec3d lv3 = lv.getRotationVec(1.0F);
            Vec3d lv4 = lv2.add(lv3.x * d, lv3.y * d, lv3.z * d);
            float g = 1.0F;
            Box lv5 = lv.getBoundingBox().stretch(lv3.multiply(d)).expand(1.0, 1.0, 1.0);
            EntityHitResult lv6 = ProjectileUtil.raycast(lv, lv2, lv4, lv5, (entity) -> {
               return !entity.isSpectator() && entity.canHit();
            }, e);
            if (lv6 != null) {
               Entity lv7 = lv6.getEntity();
               Vec3d lv8 = lv6.getPos();
               double h = lv2.squaredDistanceTo(lv8);
               if (bl && h > 9.0) {
                  this.client.crosshairTarget = BlockHitResult.createMissed(lv8, Direction.getFacing(lv3.x, lv3.y, lv3.z), BlockPos.ofFloored(lv8));
               } else if (h < e || this.client.crosshairTarget == null) {
                  this.client.crosshairTarget = lv6;
                  if (lv7 instanceof LivingEntity || lv7 instanceof ItemFrameEntity) {
                     this.client.targetedEntity = lv7;
                  }
               }
            }

            this.client.getProfiler().pop();
         }
      }
   }

   private void updateFovMultiplier() {
      float f = 1.0F;
      if (this.client.getCameraEntity() instanceof AbstractClientPlayerEntity) {
         AbstractClientPlayerEntity lv = (AbstractClientPlayerEntity)this.client.getCameraEntity();
         f = lv.getFovMultiplier();
      }

      this.lastFovMultiplier = this.fovMultiplier;
      this.fovMultiplier += (f - this.fovMultiplier) * 0.5F;
      if (this.fovMultiplier > 1.5F) {
         this.fovMultiplier = 1.5F;
      }

      if (this.fovMultiplier < 0.1F) {
         this.fovMultiplier = 0.1F;
      }

   }

   private double getFov(Camera camera, float tickDelta, boolean changingFov) {
      if (this.renderingPanorama) {
         return 90.0;
      } else {
         double d = 70.0;
         if (changingFov) {
            d = (double)(Integer)this.client.options.getFov().getValue();
            d *= (double)MathHelper.lerp(tickDelta, this.lastFovMultiplier, this.fovMultiplier);
         }

         if (camera.getFocusedEntity() instanceof LivingEntity && ((LivingEntity)camera.getFocusedEntity()).isDead()) {
            float g = Math.min((float)((LivingEntity)camera.getFocusedEntity()).deathTime + tickDelta, 20.0F);
            d /= (double)((1.0F - 500.0F / (g + 500.0F)) * 2.0F + 1.0F);
         }

         CameraSubmersionType lv = camera.getSubmersionType();
         if (lv == CameraSubmersionType.LAVA || lv == CameraSubmersionType.WATER) {
            d *= MathHelper.lerp((Double)this.client.options.getFovEffectScale().getValue(), 1.0, 0.8571428656578064);
         }

         return d;
      }
   }

   private void tiltViewWhenHurt(MatrixStack matrices, float tickDelta) {
      if (this.client.getCameraEntity() instanceof LivingEntity) {
         LivingEntity lv = (LivingEntity)this.client.getCameraEntity();
         float g = (float)lv.hurtTime - tickDelta;
         float h;
         if (lv.isDead()) {
            h = Math.min((float)lv.deathTime + tickDelta, 20.0F);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(40.0F - 8000.0F / (h + 200.0F)));
         }

         if (g < 0.0F) {
            return;
         }

         g /= (float)lv.maxHurtTime;
         g = MathHelper.sin(g * g * g * g * 3.1415927F);
         h = lv.getDamageTiltYaw();
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-h));
         float i = (float)((double)(-g) * 14.0 * (Double)this.client.options.getDamageTiltStrength().getValue());
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(h));
      }

   }

   private void bobView(MatrixStack matrices, float tickDelta) {
      if (this.client.getCameraEntity() instanceof PlayerEntity) {
         PlayerEntity lv = (PlayerEntity)this.client.getCameraEntity();
         float g = lv.horizontalSpeed - lv.prevHorizontalSpeed;
         float h = -(lv.horizontalSpeed + g * tickDelta);
         float i = MathHelper.lerp(tickDelta, lv.prevStrideDistance, lv.strideDistance);
         matrices.translate(MathHelper.sin(h * 3.1415927F) * i * 0.5F, -Math.abs(MathHelper.cos(h * 3.1415927F) * i), 0.0F);
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(h * 3.1415927F) * i * 3.0F));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(Math.abs(MathHelper.cos(h * 3.1415927F - 0.2F) * i) * 5.0F));
      }
   }

   public void renderWithZoom(float zoom, float zoomX, float zoomY) {
      this.zoom = zoom;
      this.zoomX = zoomX;
      this.zoomY = zoomY;
      this.setBlockOutlineEnabled(false);
      this.setRenderHand(false);
      this.renderWorld(1.0F, 0L, new MatrixStack());
      this.zoom = 1.0F;
   }

   private void renderHand(MatrixStack matrices, Camera camera, float tickDelta) {
      if (!this.renderingPanorama) {
         this.loadProjectionMatrix(this.getBasicProjectionMatrix(this.getFov(camera, tickDelta, false)));
         matrices.loadIdentity();
         matrices.push();
         this.tiltViewWhenHurt(matrices, tickDelta);
         if ((Boolean)this.client.options.getBobView().getValue()) {
            this.bobView(matrices, tickDelta);
         }

         boolean bl = this.client.getCameraEntity() instanceof LivingEntity && ((LivingEntity)this.client.getCameraEntity()).isSleeping();
         if (this.client.options.getPerspective().isFirstPerson() && !bl && !this.client.options.hudHidden && this.client.interactionManager.getCurrentGameMode() != GameMode.SPECTATOR) {
            this.lightmapTextureManager.enable();
            this.firstPersonRenderer.renderItem(tickDelta, matrices, this.buffers.getEntityVertexConsumers(), this.client.player, this.client.getEntityRenderDispatcher().getLight(this.client.player, tickDelta));
            this.lightmapTextureManager.disable();
         }

         matrices.pop();
         if (this.client.options.getPerspective().isFirstPerson() && !bl) {
            InGameOverlayRenderer.renderOverlays(this.client, matrices);
            this.tiltViewWhenHurt(matrices, tickDelta);
         }

         if ((Boolean)this.client.options.getBobView().getValue()) {
            this.bobView(matrices, tickDelta);
         }

      }
   }

   public void loadProjectionMatrix(Matrix4f projectionMatrix) {
      RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorter.BY_DISTANCE);
   }

   public Matrix4f getBasicProjectionMatrix(double fov) {
      MatrixStack lv = new MatrixStack();
      lv.peek().getPositionMatrix().identity();
      if (this.zoom != 1.0F) {
         lv.translate(this.zoomX, -this.zoomY, 0.0F);
         lv.scale(this.zoom, this.zoom, 1.0F);
      }

      lv.peek().getPositionMatrix().mul((new Matrix4f()).setPerspective((float)(fov * 0.01745329238474369), (float)this.client.getWindow().getFramebufferWidth() / (float)this.client.getWindow().getFramebufferHeight(), 0.05F, this.method_32796()));
      return lv.peek().getPositionMatrix();
   }

   public float method_32796() {
      return this.viewDistance * 4.0F;
   }

   public static float getNightVisionStrength(LivingEntity entity, float tickDelta) {
      StatusEffectInstance lv = entity.getStatusEffect(StatusEffects.NIGHT_VISION);
      return !lv.isDurationBelow(200) ? 1.0F : 0.7F + MathHelper.sin(((float)lv.getDuration() - tickDelta) * 3.1415927F * 0.2F) * 0.3F;
   }

   public void render(float tickDelta, long startTime, boolean tick) {
      if (this.client.isWindowFocused() || !this.client.options.pauseOnLostFocus || (Boolean)this.client.options.getTouchscreen().getValue() && this.client.mouse.wasRightButtonClicked()) {
         this.lastWindowFocusedTime = Util.getMeasuringTimeMs();
      } else if (Util.getMeasuringTimeMs() - this.lastWindowFocusedTime > 500L) {
         this.client.openPauseMenu(false);
      }

      if (!this.client.skipGameRender) {
         int i = (int)(this.client.mouse.getX() * (double)this.client.getWindow().getScaledWidth() / (double)this.client.getWindow().getWidth());
         int j = (int)(this.client.mouse.getY() * (double)this.client.getWindow().getScaledHeight() / (double)this.client.getWindow().getHeight());
         RenderSystem.viewport(0, 0, this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight());
         if (tick && this.client.world != null) {
            this.client.getProfiler().push("level");
            this.renderWorld(tickDelta, startTime, new MatrixStack());
            this.updateWorldIcon();
            this.client.worldRenderer.drawEntityOutlinesFramebuffer();
            if (this.postProcessor != null && this.postProcessorEnabled) {
               RenderSystem.disableBlend();
               RenderSystem.disableDepthTest();
               RenderSystem.resetTextureMatrix();
               this.postProcessor.render(tickDelta);
            }

            this.client.getFramebuffer().beginWrite(true);
         }

         Window lv = this.client.getWindow();
         RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
         Matrix4f matrix4f = (new Matrix4f()).setOrtho(0.0F, (float)((double)lv.getFramebufferWidth() / lv.getScaleFactor()), (float)((double)lv.getFramebufferHeight() / lv.getScaleFactor()), 0.0F, 1000.0F, 3000.0F);
         RenderSystem.setProjectionMatrix(matrix4f, VertexSorter.BY_Z);
         MatrixStack lv2 = RenderSystem.getModelViewStack();
         lv2.push();
         lv2.loadIdentity();
         lv2.translate(0.0F, 0.0F, -2000.0F);
         RenderSystem.applyModelViewMatrix();
         DiffuseLighting.enableGuiDepthLighting();
         MatrixStack lv3 = new MatrixStack();
         if (tick && this.client.world != null) {
            this.client.getProfiler().swap("gui");
            if (this.client.player != null) {
               float g = MathHelper.lerp(tickDelta, this.client.player.lastNauseaStrength, this.client.player.nextNauseaStrength);
               float h = ((Double)this.client.options.getDistortionEffectScale().getValue()).floatValue();
               if (g > 0.0F && this.client.player.hasStatusEffect(StatusEffects.NAUSEA) && h < 1.0F) {
                  this.renderNausea(g * (1.0F - h));
               }
            }

            if (!this.client.options.hudHidden || this.client.currentScreen != null) {
               this.renderFloatingItem(this.client.getWindow().getScaledWidth(), this.client.getWindow().getScaledHeight(), tickDelta);
               this.client.inGameHud.render(lv3, tickDelta);
               RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
            }

            this.client.getProfiler().pop();
         }

         CrashReportSection lv5;
         CrashReport lv4;
         if (this.client.getOverlay() != null) {
            try {
               this.client.getOverlay().render(lv3, i, j, this.client.getLastFrameDuration());
            } catch (Throwable var16) {
               lv4 = CrashReport.create(var16, "Rendering overlay");
               lv5 = lv4.addElement("Overlay render details");
               lv5.add("Overlay name", () -> {
                  return this.client.getOverlay().getClass().getCanonicalName();
               });
               throw new CrashException(lv4);
            }
         } else if (this.client.currentScreen != null) {
            try {
               this.client.currentScreen.renderWithTooltip(lv3, i, j, this.client.getLastFrameDuration());
            } catch (Throwable var15) {
               lv4 = CrashReport.create(var15, "Rendering screen");
               lv5 = lv4.addElement("Screen render details");
               lv5.add("Screen name", () -> {
                  return this.client.currentScreen.getClass().getCanonicalName();
               });
               lv5.add("Mouse location", () -> {
                  return String.format(Locale.ROOT, "Scaled: (%d, %d). Absolute: (%f, %f)", i, j, this.client.mouse.getX(), this.client.mouse.getY());
               });
               lv5.add("Screen size", () -> {
                  return String.format(Locale.ROOT, "Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %f", this.client.getWindow().getScaledWidth(), this.client.getWindow().getScaledHeight(), this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight(), this.client.getWindow().getScaleFactor());
               });
               throw new CrashException(lv4);
            }

            try {
               if (this.client.currentScreen != null) {
                  this.client.currentScreen.updateNarrator();
               }
            } catch (Throwable var14) {
               lv4 = CrashReport.create(var14, "Narrating screen");
               lv5 = lv4.addElement("Screen details");
               lv5.add("Screen name", () -> {
                  return this.client.currentScreen.getClass().getCanonicalName();
               });
               throw new CrashException(lv4);
            }
         }

         this.client.getProfiler().push("toasts");
         this.client.getToastManager().draw(lv3);
         this.client.getProfiler().pop();
         lv2.pop();
         RenderSystem.applyModelViewMatrix();
      }
   }

   private void updateWorldIcon() {
      if (!this.hasWorldIcon && this.client.isInSingleplayer()) {
         long l = Util.getMeasuringTimeMs();
         if (l - this.lastWorldIconUpdate >= 1000L) {
            this.lastWorldIconUpdate = l;
            IntegratedServer lv = this.client.getServer();
            if (lv != null && !lv.isStopped()) {
               lv.getIconFile().ifPresent((path) -> {
                  if (Files.isRegularFile(path, new LinkOption[0])) {
                     this.hasWorldIcon = true;
                  } else {
                     this.updateWorldIcon(path);
                  }

               });
            }
         }
      }
   }

   private void updateWorldIcon(Path path) {
      if (this.client.worldRenderer.getCompletedChunkCount() > 10 && this.client.worldRenderer.isTerrainRenderComplete()) {
         NativeImage lv = ScreenshotRecorder.takeScreenshot(this.client.getFramebuffer());
         Util.getIoWorkerExecutor().execute(() -> {
            int i = lv.getWidth();
            int j = lv.getHeight();
            int k = 0;
            int l = 0;
            if (i > j) {
               k = (i - j) / 2;
               i = j;
            } else {
               l = (j - i) / 2;
               j = i;
            }

            try {
               NativeImage lvx = new NativeImage(64, 64, false);

               try {
                  lv.resizeSubRectTo(k, l, i, j, lvx);
                  lvx.writeTo(path);
               } catch (Throwable var15) {
                  try {
                     lvx.close();
                  } catch (Throwable var14) {
                     var15.addSuppressed(var14);
                  }

                  throw var15;
               }

               lvx.close();
            } catch (IOException var16) {
               LOGGER.warn("Couldn't save auto screenshot", var16);
            } finally {
               lv.close();
            }

         });
      }

   }

   private boolean shouldRenderBlockOutline() {
      if (!this.blockOutlineEnabled) {
         return false;
      } else {
         Entity lv = this.client.getCameraEntity();
         boolean bl = lv instanceof PlayerEntity && !this.client.options.hudHidden;
         if (bl && !((PlayerEntity)lv).getAbilities().allowModifyWorld) {
            ItemStack lv2 = ((LivingEntity)lv).getMainHandStack();
            HitResult lv3 = this.client.crosshairTarget;
            if (lv3 != null && lv3.getType() == HitResult.Type.BLOCK) {
               BlockPos lv4 = ((BlockHitResult)lv3).getBlockPos();
               BlockState lv5 = this.client.world.getBlockState(lv4);
               if (this.client.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) {
                  bl = lv5.createScreenHandlerFactory(this.client.world, lv4) != null;
               } else {
                  CachedBlockPosition lv6 = new CachedBlockPosition(this.client.world, lv4, false);
                  Registry lv7 = this.client.world.getRegistryManager().get(RegistryKeys.BLOCK);
                  bl = !lv2.isEmpty() && (lv2.canDestroy(lv7, lv6) || lv2.canPlaceOn(lv7, lv6));
               }
            }
         }

         return bl;
      }
   }

   public void renderWorld(float tickDelta, long limitTime, MatrixStack matrices) {
      this.lightmapTextureManager.update(tickDelta);
      if (this.client.getCameraEntity() == null) {
         this.client.setCameraEntity(this.client.player);
      }

      this.updateTargetedEntity(tickDelta);
      this.client.getProfiler().push("center");
      boolean bl = this.shouldRenderBlockOutline();
      this.client.getProfiler().swap("camera");
      Camera lv = this.camera;
      this.viewDistance = (float)(this.client.options.getClampedViewDistance() * 16);
      MatrixStack lv2 = new MatrixStack();
      double d = this.getFov(lv, tickDelta, true);
      lv2.multiplyPositionMatrix(this.getBasicProjectionMatrix(d));
      this.tiltViewWhenHurt(lv2, tickDelta);
      if ((Boolean)this.client.options.getBobView().getValue()) {
         this.bobView(lv2, tickDelta);
      }

      float g = ((Double)this.client.options.getDistortionEffectScale().getValue()).floatValue();
      float h = MathHelper.lerp(tickDelta, this.client.player.lastNauseaStrength, this.client.player.nextNauseaStrength) * g * g;
      if (h > 0.0F) {
         int i = this.client.player.hasStatusEffect(StatusEffects.NAUSEA) ? 7 : 20;
         float j = 5.0F / (h * h + 5.0F) - h * 0.04F;
         j *= j;
         RotationAxis lv3 = RotationAxis.of(new Vector3f(0.0F, MathHelper.SQUARE_ROOT_OF_TWO / 2.0F, MathHelper.SQUARE_ROOT_OF_TWO / 2.0F));
         lv2.multiply(lv3.rotationDegrees(((float)this.ticks + tickDelta) * (float)i));
         lv2.scale(1.0F / j, 1.0F, 1.0F);
         float k = -((float)this.ticks + tickDelta) * (float)i;
         lv2.multiply(lv3.rotationDegrees(k));
      }

      Matrix4f matrix4f = lv2.peek().getPositionMatrix();
      this.loadProjectionMatrix(matrix4f);
      lv.update(this.client.world, (Entity)(this.client.getCameraEntity() == null ? this.client.player : this.client.getCameraEntity()), !this.client.options.getPerspective().isFirstPerson(), this.client.options.getPerspective().isFrontView(), tickDelta);
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(lv.getPitch()));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(lv.getYaw() + 180.0F));
      Matrix3f matrix3f = (new Matrix3f(matrices.peek().getNormalMatrix())).invert();
      RenderSystem.setInverseViewRotationMatrix(matrix3f);
      this.client.worldRenderer.setupFrustum(matrices, lv.getPos(), this.getBasicProjectionMatrix(Math.max(d, (double)(Integer)this.client.options.getFov().getValue())));
      this.client.worldRenderer.render(matrices, tickDelta, limitTime, bl, lv, this, this.lightmapTextureManager, matrix4f);
      this.client.getProfiler().swap("hand");
      if (this.renderHand) {
         RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
         this.renderHand(matrices, lv, tickDelta);
      }

      this.client.getProfiler().pop();
   }

   public void reset() {
      this.floatingItem = null;
      this.mapRenderer.clearStateTextures();
      this.camera.reset();
      this.hasWorldIcon = false;
   }

   public MapRenderer getMapRenderer() {
      return this.mapRenderer;
   }

   public void showFloatingItem(ItemStack floatingItem) {
      this.floatingItem = floatingItem;
      this.floatingItemTimeLeft = 40;
      this.floatingItemWidth = this.random.nextFloat() * 2.0F - 1.0F;
      this.floatingItemHeight = this.random.nextFloat() * 2.0F - 1.0F;
   }

   private void renderFloatingItem(int scaledWidth, int scaledHeight, float tickDelta) {
      if (this.floatingItem != null && this.floatingItemTimeLeft > 0) {
         int k = 40 - this.floatingItemTimeLeft;
         float g = ((float)k + tickDelta) / 40.0F;
         float h = g * g;
         float l = g * h;
         float m = 10.25F * l * h - 24.95F * h * h + 25.5F * l - 13.8F * h + 4.0F * g;
         float n = m * 3.1415927F;
         float o = this.floatingItemWidth * (float)(scaledWidth / 4);
         float p = this.floatingItemHeight * (float)(scaledHeight / 4);
         RenderSystem.enableDepthTest();
         RenderSystem.disableCull();
         MatrixStack lv = new MatrixStack();
         lv.push();
         lv.translate((float)(scaledWidth / 2) + o * MathHelper.abs(MathHelper.sin(n * 2.0F)), (float)(scaledHeight / 2) + p * MathHelper.abs(MathHelper.sin(n * 2.0F)), -50.0F);
         float q = 50.0F + 175.0F * MathHelper.sin(n);
         lv.scale(q, -q, q);
         lv.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(900.0F * MathHelper.abs(MathHelper.sin(n))));
         lv.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0F * MathHelper.cos(g * 8.0F)));
         lv.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(6.0F * MathHelper.cos(g * 8.0F)));
         VertexConsumerProvider.Immediate lv2 = this.buffers.getEntityVertexConsumers();
         this.client.getItemRenderer().renderItem(this.floatingItem, ModelTransformationMode.FIXED, 15728880, OverlayTexture.DEFAULT_UV, lv, lv2, this.client.world, 0);
         lv.pop();
         lv2.draw();
         RenderSystem.enableCull();
         RenderSystem.disableDepthTest();
      }
   }

   private void renderNausea(float distortionStrength) {
      int i = this.client.getWindow().getScaledWidth();
      int j = this.client.getWindow().getScaledHeight();
      double d = MathHelper.lerp((double)distortionStrength, 2.0, 1.0);
      float g = 0.2F * distortionStrength;
      float h = 0.4F * distortionStrength;
      float k = 0.2F * distortionStrength;
      double e = (double)i * d;
      double l = (double)j * d;
      double m = ((double)i - e) / 2.0;
      double n = ((double)j - l) / 2.0;
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.enableBlend();
      RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE);
      RenderSystem.setShaderColor(g, h, k, 1.0F);
      RenderSystem.setShader(GameRenderer::getPositionTexProgram);
      RenderSystem.setShaderTexture(0, NAUSEA_OVERLAY);
      Tessellator lv = Tessellator.getInstance();
      BufferBuilder lv2 = lv.getBuffer();
      lv2.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
      lv2.vertex(m, n + l, -90.0).texture(0.0F, 1.0F).next();
      lv2.vertex(m + e, n + l, -90.0).texture(1.0F, 1.0F).next();
      lv2.vertex(m + e, n, -90.0).texture(1.0F, 0.0F).next();
      lv2.vertex(m, n, -90.0).texture(0.0F, 0.0F).next();
      lv.draw();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
   }

   public MinecraftClient getClient() {
      return this.client;
   }

   public float getSkyDarkness(float tickDelta) {
      return MathHelper.lerp(tickDelta, this.lastSkyDarkness, this.skyDarkness);
   }

   public float getViewDistance() {
      return this.viewDistance;
   }

   public Camera getCamera() {
      return this.camera;
   }

   public LightmapTextureManager getLightmapTextureManager() {
      return this.lightmapTextureManager;
   }

   public OverlayTexture getOverlayTexture() {
      return this.overlayTexture;
   }

   @Nullable
   public static ShaderProgram getPositionProgram() {
      return positionProgram;
   }

   @Nullable
   public static ShaderProgram getPositionColorProgram() {
      return positionColorProgram;
   }

   @Nullable
   public static ShaderProgram getPositionColorTexProgram() {
      return positionColorTexProgram;
   }

   @Nullable
   public static ShaderProgram getPositionTexProgram() {
      return positionTexProgram;
   }

   @Nullable
   public static ShaderProgram getPositionTexColorProgram() {
      return positionTexColorProgram;
   }

   @Nullable
   public static ShaderProgram getBlockProgram() {
      return blockProgram;
   }

   @Nullable
   public static ShaderProgram getNewEntityProgram() {
      return newEntityProgram;
   }

   @Nullable
   public static ShaderProgram getParticleProgram() {
      return particleProgram;
   }

   @Nullable
   public static ShaderProgram getPositionColorLightmapProgram() {
      return positionColorLightmapProgram;
   }

   @Nullable
   public static ShaderProgram getPositionColorTexLightmapProgram() {
      return positionColorTexLightmapProgram;
   }

   @Nullable
   public static ShaderProgram getPositionTexColorNormalProgram() {
      return positionTexColorNormalProgram;
   }

   @Nullable
   public static ShaderProgram getPositionTexLightmapColorProgram() {
      return positionTexLightmapColorProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeSolidProgram() {
      return renderTypeSolidProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeCutoutMippedProgram() {
      return renderTypeCutoutMippedProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeCutoutProgram() {
      return renderTypeCutoutProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeTranslucentProgram() {
      return renderTypeTranslucentProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeTranslucentMovingBlockProgram() {
      return renderTypeTranslucentMovingBlockProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeTranslucentNoCrumblingProgram() {
      return renderTypeTranslucentNoCrumblingProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeArmorCutoutNoCullProgram() {
      return renderTypeArmorCutoutNoCullProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeEntitySolidProgram() {
      return renderTypeEntitySolidProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeEntityCutoutProgram() {
      return renderTypeEntityCutoutProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeEntityCutoutNoNullProgram() {
      return renderTypeEntityCutoutNoNullProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeEntityCutoutNoNullZOffsetProgram() {
      return renderTypeEntityCutoutNoNullZOffsetProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeItemEntityTranslucentCullProgram() {
      return renderTypeItemEntityTranslucentCullProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeEntityTranslucentCullProgram() {
      return renderTypeEntityTranslucentCullProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeEntityTranslucentProgram() {
      return renderTypeEntityTranslucentProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeEntityTranslucentEmissiveProgram() {
      return renderTypeEntityTranslucentEmissiveProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeEntitySmoothCutoutProgram() {
      return renderTypeEntitySmoothCutoutProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeBeaconBeamProgram() {
      return renderTypeBeaconBeamProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeEntityDecalProgram() {
      return renderTypeEntityDecalProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeEntityNoOutlineProgram() {
      return renderTypeEntityNoOutlineProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeEntityShadowProgram() {
      return renderTypeEntityShadowProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeEntityAlphaProgram() {
      return renderTypeEntityAlphaProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeEyesProgram() {
      return renderTypeEyesProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeEnergySwirlProgram() {
      return renderTypeEnergySwirlProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeLeashProgram() {
      return renderTypeLeashProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeWaterMaskProgram() {
      return renderTypeWaterMaskProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeOutlineProgram() {
      return renderTypeOutlineProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeArmorGlintProgram() {
      return renderTypeArmorGlintProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeArmorEntityGlintProgram() {
      return renderTypeArmorEntityGlintProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeGlintTranslucentProgram() {
      return renderTypeGlintTranslucentProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeGlintProgram() {
      return renderTypeGlintProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeGlintDirectProgram() {
      return renderTypeGlintDirectProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeEntityGlintProgram() {
      return renderTypeEntityGlintProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeEntityGlintDirectProgram() {
      return renderTypeEntityGlintDirectProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeTextProgram() {
      return renderTypeTextProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeTextBackgroundProgram() {
      return renderTypeTextBackgroundProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeTextIntensityProgram() {
      return renderTypeTextIntensityProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeTextSeeThroughProgram() {
      return renderTypeTextSeeThroughProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeTextBackgroundSeeThroughProgram() {
      return renderTypeTextBackgroundSeeThroughProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeTextIntensitySeeThroughProgram() {
      return renderTypeTextIntensitySeeThroughProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeLightningProgram() {
      return renderTypeLightningProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeTripwireProgram() {
      return renderTypeTripwireProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeEndPortalProgram() {
      return renderTypeEndPortalProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeEndGatewayProgram() {
      return renderTypeEndGatewayProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeLinesProgram() {
      return renderTypeLinesProgram;
   }

   @Nullable
   public static ShaderProgram getRenderTypeCrumblingProgram() {
      return renderTypeCrumblingProgram;
   }

   static {
      SUPER_SECRET_SETTING_COUNT = SUPER_SECRET_SETTING_PROGRAMS.length;
   }

   @Environment(EnvType.CLIENT)
   public static record CachedResourceFactory(ResourceFactory original, Map cache) implements ResourceFactory {
      public CachedResourceFactory(ResourceFactory arg, Map map) {
         this.original = arg;
         this.cache = map;
      }

      public Optional getResource(Identifier id) {
         Resource lv = (Resource)this.cache.get(id);
         return lv != null ? Optional.of(lv) : this.original.getResource(id);
      }

      public ResourceFactory original() {
         return this.original;
      }

      public Map cache() {
         return this.cache;
      }
   }
}
