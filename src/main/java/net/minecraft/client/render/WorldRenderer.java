package net.minecraft.client.render;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BrushableBlock;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.MultifaceGrowthBlock;
import net.minecraft.block.PointedDripstoneBlock;
import net.minecraft.block.SculkShriekerBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegionBuilder;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ParticleUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SculkChargeParticleEffect;
import net.minecraft.particle.ShriekParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector4f;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class WorldRenderer implements SynchronousResourceReloader, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int field_32759 = 16;
   private static final int field_34812 = 8;
   private static final float field_32762 = 512.0F;
   private static final int field_34813 = 60;
   private static final double field_34814 = Math.ceil(Math.sqrt(3.0) * 16.0);
   private static final int field_32763 = 32;
   private static final int field_32764 = 10;
   private static final int field_32765 = 21;
   private static final int field_32766 = 15;
   private static final int field_34815 = 500;
   private static final Identifier MOON_PHASES = new Identifier("textures/environment/moon_phases.png");
   private static final Identifier SUN = new Identifier("textures/environment/sun.png");
   private static final Identifier CLOUDS = new Identifier("textures/environment/clouds.png");
   private static final Identifier END_SKY = new Identifier("textures/environment/end_sky.png");
   private static final Identifier FORCEFIELD = new Identifier("textures/misc/forcefield.png");
   private static final Identifier RAIN = new Identifier("textures/environment/rain.png");
   private static final Identifier SNOW = new Identifier("textures/environment/snow.png");
   public static final Direction[] DIRECTIONS = Direction.values();
   private final MinecraftClient client;
   private final EntityRenderDispatcher entityRenderDispatcher;
   private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
   private final BufferBuilderStorage bufferBuilders;
   @Nullable
   private ClientWorld world;
   private final BlockingQueue builtChunks = new LinkedBlockingQueue();
   private final AtomicReference renderableChunks = new AtomicReference();
   private final ObjectArrayList chunkInfos = new ObjectArrayList(10000);
   private final Set noCullingBlockEntities = Sets.newHashSet();
   @Nullable
   private Future fullUpdateFuture;
   @Nullable
   private BuiltChunkStorage chunks;
   @Nullable
   private VertexBuffer starsBuffer;
   @Nullable
   private VertexBuffer lightSkyBuffer;
   @Nullable
   private VertexBuffer darkSkyBuffer;
   private boolean cloudsDirty = true;
   @Nullable
   private VertexBuffer cloudsBuffer;
   private final FpsSmoother chunkUpdateSmoother = new FpsSmoother(100);
   private int ticks;
   private final Int2ObjectMap blockBreakingInfos = new Int2ObjectOpenHashMap();
   private final Long2ObjectMap blockBreakingProgressions = new Long2ObjectOpenHashMap();
   private final Map playingSongs = Maps.newHashMap();
   @Nullable
   private Framebuffer entityOutlinesFramebuffer;
   @Nullable
   private PostEffectProcessor entityOutlinePostProcessor;
   @Nullable
   private Framebuffer translucentFramebuffer;
   @Nullable
   private Framebuffer entityFramebuffer;
   @Nullable
   private Framebuffer particlesFramebuffer;
   @Nullable
   private Framebuffer weatherFramebuffer;
   @Nullable
   private Framebuffer cloudsFramebuffer;
   @Nullable
   private PostEffectProcessor transparencyPostProcessor;
   private double lastCameraChunkUpdateX = Double.MIN_VALUE;
   private double lastCameraChunkUpdateY = Double.MIN_VALUE;
   private double lastCameraChunkUpdateZ = Double.MIN_VALUE;
   private int cameraChunkX = Integer.MIN_VALUE;
   private int cameraChunkY = Integer.MIN_VALUE;
   private int cameraChunkZ = Integer.MIN_VALUE;
   private double lastCameraX = Double.MIN_VALUE;
   private double lastCameraY = Double.MIN_VALUE;
   private double lastCameraZ = Double.MIN_VALUE;
   private double lastCameraPitch = Double.MIN_VALUE;
   private double lastCameraYaw = Double.MIN_VALUE;
   private int lastCloudsBlockX = Integer.MIN_VALUE;
   private int lastCloudsBlockY = Integer.MIN_VALUE;
   private int lastCloudsBlockZ = Integer.MIN_VALUE;
   private Vec3d lastCloudsColor;
   @Nullable
   private CloudRenderMode lastCloudRenderMode;
   @Nullable
   private ChunkBuilder chunkBuilder;
   private int viewDistance;
   private int regularEntityCount;
   private int blockEntityCount;
   private Frustum frustum;
   private boolean shouldCaptureFrustum;
   @Nullable
   private Frustum capturedFrustum;
   private final Vector4f[] capturedFrustumOrientation;
   private final Vector3d capturedFrustumPosition;
   private double lastTranslucentSortX;
   private double lastTranslucentSortY;
   private double lastTranslucentSortZ;
   private boolean shouldUpdate;
   private final AtomicLong nextUpdateTime;
   private final AtomicBoolean updateFinished;
   private int rainSoundCounter;
   private final float[] field_20794;
   private final float[] field_20795;

   public WorldRenderer(MinecraftClient client, EntityRenderDispatcher entityRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher, BufferBuilderStorage bufferBuilders) {
      this.lastCloudsColor = Vec3d.ZERO;
      this.viewDistance = -1;
      this.capturedFrustumOrientation = new Vector4f[8];
      this.capturedFrustumPosition = new Vector3d(0.0, 0.0, 0.0);
      this.shouldUpdate = true;
      this.nextUpdateTime = new AtomicLong(0L);
      this.updateFinished = new AtomicBoolean(false);
      this.field_20794 = new float[1024];
      this.field_20795 = new float[1024];
      this.client = client;
      this.entityRenderDispatcher = entityRenderDispatcher;
      this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
      this.bufferBuilders = bufferBuilders;

      for(int i = 0; i < 32; ++i) {
         for(int j = 0; j < 32; ++j) {
            float f = (float)(j - 16);
            float g = (float)(i - 16);
            float h = MathHelper.sqrt(f * f + g * g);
            this.field_20794[i << 5 | j] = -g / h;
            this.field_20795[i << 5 | j] = f / h;
         }
      }

      this.renderStars();
      this.renderLightSky();
      this.renderDarkSky();
   }

   private void renderWeather(LightmapTextureManager manager, float tickDelta, double cameraX, double cameraY, double cameraZ) {
      float h = this.client.world.getRainGradient(tickDelta);
      if (!(h <= 0.0F)) {
         manager.enable();
         World lv = this.client.world;
         int i = MathHelper.floor(cameraX);
         int j = MathHelper.floor(cameraY);
         int k = MathHelper.floor(cameraZ);
         Tessellator lv2 = Tessellator.getInstance();
         BufferBuilder lv3 = lv2.getBuffer();
         RenderSystem.disableCull();
         RenderSystem.enableBlend();
         RenderSystem.enableDepthTest();
         int l = 5;
         if (MinecraftClient.isFancyGraphicsOrBetter()) {
            l = 10;
         }

         RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
         int m = -1;
         float n = (float)this.ticks + tickDelta;
         RenderSystem.setShader(GameRenderer::getParticleProgram);
         BlockPos.Mutable lv4 = new BlockPos.Mutable();

         for(int o = k - l; o <= k + l; ++o) {
            for(int p = i - l; p <= i + l; ++p) {
               int q = (o - k + 16) * 32 + p - i + 16;
               double r = (double)this.field_20794[q] * 0.5;
               double s = (double)this.field_20795[q] * 0.5;
               lv4.set((double)p, cameraY, (double)o);
               Biome lv5 = (Biome)lv.getBiome(lv4).value();
               if (lv5.hasPrecipitation()) {
                  int t = lv.getTopY(Heightmap.Type.MOTION_BLOCKING, p, o);
                  int u = j - l;
                  int v = j + l;
                  if (u < t) {
                     u = t;
                  }

                  if (v < t) {
                     v = t;
                  }

                  int w = t;
                  if (t < j) {
                     w = j;
                  }

                  if (u != v) {
                     Random lv6 = Random.create((long)(p * p * 3121 + p * 45238971 ^ o * o * 418711 + o * 13761));
                     lv4.set(p, u, o);
                     Biome.Precipitation lv7 = lv5.getPrecipitation(lv4);
                     float y;
                     float ac;
                     if (lv7 == Biome.Precipitation.RAIN) {
                        if (m != 0) {
                           if (m >= 0) {
                              lv2.draw();
                           }

                           m = 0;
                           RenderSystem.setShaderTexture(0, RAIN);
                           lv3.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
                        }

                        int x = this.ticks + p * p * 3121 + p * 45238971 + o * o * 418711 + o * 13761 & 31;
                        y = -((float)x + tickDelta) / 32.0F * (3.0F + lv6.nextFloat());
                        double z = (double)p + 0.5 - cameraX;
                        double aa = (double)o + 0.5 - cameraZ;
                        float ab = (float)Math.sqrt(z * z + aa * aa) / (float)l;
                        ac = ((1.0F - ab * ab) * 0.5F + 0.5F) * h;
                        lv4.set(p, w, o);
                        int ad = getLightmapCoordinates(lv, lv4);
                        lv3.vertex((double)p - cameraX - r + 0.5, (double)v - cameraY, (double)o - cameraZ - s + 0.5).texture(0.0F, (float)u * 0.25F + y).color(1.0F, 1.0F, 1.0F, ac).light(ad).next();
                        lv3.vertex((double)p - cameraX + r + 0.5, (double)v - cameraY, (double)o - cameraZ + s + 0.5).texture(1.0F, (float)u * 0.25F + y).color(1.0F, 1.0F, 1.0F, ac).light(ad).next();
                        lv3.vertex((double)p - cameraX + r + 0.5, (double)u - cameraY, (double)o - cameraZ + s + 0.5).texture(1.0F, (float)v * 0.25F + y).color(1.0F, 1.0F, 1.0F, ac).light(ad).next();
                        lv3.vertex((double)p - cameraX - r + 0.5, (double)u - cameraY, (double)o - cameraZ - s + 0.5).texture(0.0F, (float)v * 0.25F + y).color(1.0F, 1.0F, 1.0F, ac).light(ad).next();
                     } else if (lv7 == Biome.Precipitation.SNOW) {
                        if (m != 1) {
                           if (m >= 0) {
                              lv2.draw();
                           }

                           m = 1;
                           RenderSystem.setShaderTexture(0, SNOW);
                           lv3.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
                        }

                        float ae = -((float)(this.ticks & 511) + tickDelta) / 512.0F;
                        y = (float)(lv6.nextDouble() + (double)n * 0.01 * (double)((float)lv6.nextGaussian()));
                        float af = (float)(lv6.nextDouble() + (double)(n * (float)lv6.nextGaussian()) * 0.001);
                        double ag = (double)p + 0.5 - cameraX;
                        double ah = (double)o + 0.5 - cameraZ;
                        ac = (float)Math.sqrt(ag * ag + ah * ah) / (float)l;
                        float ai = ((1.0F - ac * ac) * 0.3F + 0.5F) * h;
                        lv4.set(p, w, o);
                        int aj = getLightmapCoordinates(lv, lv4);
                        int ak = aj >> 16 & '\uffff';
                        int al = aj & '\uffff';
                        int am = (ak * 3 + 240) / 4;
                        int an = (al * 3 + 240) / 4;
                        lv3.vertex((double)p - cameraX - r + 0.5, (double)v - cameraY, (double)o - cameraZ - s + 0.5).texture(0.0F + y, (float)u * 0.25F + ae + af).color(1.0F, 1.0F, 1.0F, ai).light(an, am).next();
                        lv3.vertex((double)p - cameraX + r + 0.5, (double)v - cameraY, (double)o - cameraZ + s + 0.5).texture(1.0F + y, (float)u * 0.25F + ae + af).color(1.0F, 1.0F, 1.0F, ai).light(an, am).next();
                        lv3.vertex((double)p - cameraX + r + 0.5, (double)u - cameraY, (double)o - cameraZ + s + 0.5).texture(1.0F + y, (float)v * 0.25F + ae + af).color(1.0F, 1.0F, 1.0F, ai).light(an, am).next();
                        lv3.vertex((double)p - cameraX - r + 0.5, (double)u - cameraY, (double)o - cameraZ - s + 0.5).texture(0.0F + y, (float)v * 0.25F + ae + af).color(1.0F, 1.0F, 1.0F, ai).light(an, am).next();
                     }
                  }
               }
            }
         }

         if (m >= 0) {
            lv2.draw();
         }

         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         manager.disable();
      }
   }

   public void tickRainSplashing(Camera camera) {
      float f = this.client.world.getRainGradient(1.0F) / (MinecraftClient.isFancyGraphicsOrBetter() ? 1.0F : 2.0F);
      if (!(f <= 0.0F)) {
         Random lv = Random.create((long)this.ticks * 312987231L);
         WorldView lv2 = this.client.world;
         BlockPos lv3 = BlockPos.ofFloored(camera.getPos());
         BlockPos lv4 = null;
         int i = (int)(100.0F * f * f) / (this.client.options.getParticles().getValue() == ParticlesMode.DECREASED ? 2 : 1);

         for(int j = 0; j < i; ++j) {
            int k = lv.nextInt(21) - 10;
            int l = lv.nextInt(21) - 10;
            BlockPos lv5 = lv2.getTopPosition(Heightmap.Type.MOTION_BLOCKING, lv3.add(k, 0, l));
            if (lv5.getY() > lv2.getBottomY() && lv5.getY() <= lv3.getY() + 10 && lv5.getY() >= lv3.getY() - 10) {
               Biome lv6 = (Biome)lv2.getBiome(lv5).value();
               if (lv6.getPrecipitation(lv5) == Biome.Precipitation.RAIN) {
                  lv4 = lv5.down();
                  if (this.client.options.getParticles().getValue() == ParticlesMode.MINIMAL) {
                     break;
                  }

                  double d = lv.nextDouble();
                  double e = lv.nextDouble();
                  BlockState lv7 = lv2.getBlockState(lv4);
                  FluidState lv8 = lv2.getFluidState(lv4);
                  VoxelShape lv9 = lv7.getCollisionShape(lv2, lv4);
                  double g = lv9.getEndingCoord(Direction.Axis.Y, d, e);
                  double h = (double)lv8.getHeight(lv2, lv4);
                  double m = Math.max(g, h);
                  ParticleEffect lv10 = !lv8.isIn(FluidTags.LAVA) && !lv7.isOf(Blocks.MAGMA_BLOCK) && !CampfireBlock.isLitCampfire(lv7) ? ParticleTypes.RAIN : ParticleTypes.SMOKE;
                  this.client.world.addParticle(lv10, (double)lv4.getX() + d, (double)lv4.getY() + m, (double)lv4.getZ() + e, 0.0, 0.0, 0.0);
               }
            }
         }

         if (lv4 != null && lv.nextInt(3) < this.rainSoundCounter++) {
            this.rainSoundCounter = 0;
            if (lv4.getY() > lv3.getY() + 1 && lv2.getTopPosition(Heightmap.Type.MOTION_BLOCKING, lv3).getY() > MathHelper.floor((float)lv3.getY())) {
               this.client.world.playSoundAtBlockCenter(lv4, SoundEvents.WEATHER_RAIN_ABOVE, SoundCategory.WEATHER, 0.1F, 0.5F, false);
            } else {
               this.client.world.playSoundAtBlockCenter(lv4, SoundEvents.WEATHER_RAIN, SoundCategory.WEATHER, 0.2F, 1.0F, false);
            }
         }

      }
   }

   public void close() {
      if (this.entityOutlinePostProcessor != null) {
         this.entityOutlinePostProcessor.close();
      }

      if (this.transparencyPostProcessor != null) {
         this.transparencyPostProcessor.close();
      }

   }

   public void reload(ResourceManager manager) {
      this.loadEntityOutlinePostProcessor();
      if (MinecraftClient.isFabulousGraphicsOrBetter()) {
         this.loadTransparencyPostProcessor();
      }

   }

   public void loadEntityOutlinePostProcessor() {
      if (this.entityOutlinePostProcessor != null) {
         this.entityOutlinePostProcessor.close();
      }

      Identifier lv = new Identifier("shaders/post/entity_outline.json");

      try {
         this.entityOutlinePostProcessor = new PostEffectProcessor(this.client.getTextureManager(), this.client.getResourceManager(), this.client.getFramebuffer(), lv);
         this.entityOutlinePostProcessor.setupDimensions(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight());
         this.entityOutlinesFramebuffer = this.entityOutlinePostProcessor.getSecondaryTarget("final");
      } catch (IOException var3) {
         LOGGER.warn("Failed to load shader: {}", lv, var3);
         this.entityOutlinePostProcessor = null;
         this.entityOutlinesFramebuffer = null;
      } catch (JsonSyntaxException var4) {
         LOGGER.warn("Failed to parse shader: {}", lv, var4);
         this.entityOutlinePostProcessor = null;
         this.entityOutlinesFramebuffer = null;
      }

   }

   private void loadTransparencyPostProcessor() {
      this.resetTransparencyPostProcessor();
      Identifier lv = new Identifier("shaders/post/transparency.json");

      try {
         PostEffectProcessor lv2 = new PostEffectProcessor(this.client.getTextureManager(), this.client.getResourceManager(), this.client.getFramebuffer(), lv);
         lv2.setupDimensions(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight());
         Framebuffer lv3 = lv2.getSecondaryTarget("translucent");
         Framebuffer lv4 = lv2.getSecondaryTarget("itemEntity");
         Framebuffer lv5 = lv2.getSecondaryTarget("particles");
         Framebuffer lv6 = lv2.getSecondaryTarget("weather");
         Framebuffer lv7 = lv2.getSecondaryTarget("clouds");
         this.transparencyPostProcessor = lv2;
         this.translucentFramebuffer = lv3;
         this.entityFramebuffer = lv4;
         this.particlesFramebuffer = lv5;
         this.weatherFramebuffer = lv6;
         this.cloudsFramebuffer = lv7;
      } catch (Exception var8) {
         String string = var8 instanceof JsonSyntaxException ? "parse" : "load";
         String string2 = "Failed to " + string + " shader: " + lv;
         ProgramInitException lv8 = new ProgramInitException(string2, var8);
         if (this.client.getResourcePackManager().getEnabledNames().size() > 1) {
            Text lv9 = (Text)this.client.getResourceManager().streamResourcePacks().findFirst().map((arg) -> {
               return Text.literal(arg.getName());
            }).orElse((Object)null);
            this.client.options.getGraphicsMode().setValue(GraphicsMode.FANCY);
            this.client.onResourceReloadFailure(lv8, lv9);
         } else {
            CrashReport lv10 = this.client.addDetailsToCrashReport(new CrashReport(string2, lv8));
            this.client.options.getGraphicsMode().setValue(GraphicsMode.FANCY);
            this.client.options.write();
            LOGGER.error(LogUtils.FATAL_MARKER, string2, lv8);
            this.client.cleanUpAfterCrash();
            MinecraftClient.printCrashReport(lv10);
         }
      }

   }

   private void resetTransparencyPostProcessor() {
      if (this.transparencyPostProcessor != null) {
         this.transparencyPostProcessor.close();
         this.translucentFramebuffer.delete();
         this.entityFramebuffer.delete();
         this.particlesFramebuffer.delete();
         this.weatherFramebuffer.delete();
         this.cloudsFramebuffer.delete();
         this.transparencyPostProcessor = null;
         this.translucentFramebuffer = null;
         this.entityFramebuffer = null;
         this.particlesFramebuffer = null;
         this.weatherFramebuffer = null;
         this.cloudsFramebuffer = null;
      }

   }

   public void drawEntityOutlinesFramebuffer() {
      if (this.canDrawEntityOutlines()) {
         RenderSystem.enableBlend();
         RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
         this.entityOutlinesFramebuffer.draw(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight(), false);
         RenderSystem.disableBlend();
         RenderSystem.defaultBlendFunc();
      }

   }

   protected boolean canDrawEntityOutlines() {
      return !this.client.gameRenderer.isRenderingPanorama() && this.entityOutlinesFramebuffer != null && this.entityOutlinePostProcessor != null && this.client.player != null;
   }

   private void renderDarkSky() {
      Tessellator lv = Tessellator.getInstance();
      BufferBuilder lv2 = lv.getBuffer();
      if (this.darkSkyBuffer != null) {
         this.darkSkyBuffer.close();
      }

      this.darkSkyBuffer = new VertexBuffer();
      BufferBuilder.BuiltBuffer lv3 = renderSky(lv2, -16.0F);
      this.darkSkyBuffer.bind();
      this.darkSkyBuffer.upload(lv3);
      VertexBuffer.unbind();
   }

   private void renderLightSky() {
      Tessellator lv = Tessellator.getInstance();
      BufferBuilder lv2 = lv.getBuffer();
      if (this.lightSkyBuffer != null) {
         this.lightSkyBuffer.close();
      }

      this.lightSkyBuffer = new VertexBuffer();
      BufferBuilder.BuiltBuffer lv3 = renderSky(lv2, 16.0F);
      this.lightSkyBuffer.bind();
      this.lightSkyBuffer.upload(lv3);
      VertexBuffer.unbind();
   }

   private static BufferBuilder.BuiltBuffer renderSky(BufferBuilder builder, float f) {
      float g = Math.signum(f) * 512.0F;
      float h = 512.0F;
      RenderSystem.setShader(GameRenderer::getPositionProgram);
      builder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION);
      builder.vertex(0.0, (double)f, 0.0).next();

      for(int i = -180; i <= 180; i += 45) {
         builder.vertex((double)(g * MathHelper.cos((float)i * 0.017453292F)), (double)f, (double)(512.0F * MathHelper.sin((float)i * 0.017453292F))).next();
      }

      return builder.end();
   }

   private void renderStars() {
      Tessellator lv = Tessellator.getInstance();
      BufferBuilder lv2 = lv.getBuffer();
      RenderSystem.setShader(GameRenderer::getPositionProgram);
      if (this.starsBuffer != null) {
         this.starsBuffer.close();
      }

      this.starsBuffer = new VertexBuffer();
      BufferBuilder.BuiltBuffer lv3 = this.renderStars(lv2);
      this.starsBuffer.bind();
      this.starsBuffer.upload(lv3);
      VertexBuffer.unbind();
   }

   private BufferBuilder.BuiltBuffer renderStars(BufferBuilder buffer) {
      Random lv = Random.create(10842L);
      buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

      for(int i = 0; i < 1500; ++i) {
         double d = (double)(lv.nextFloat() * 2.0F - 1.0F);
         double e = (double)(lv.nextFloat() * 2.0F - 1.0F);
         double f = (double)(lv.nextFloat() * 2.0F - 1.0F);
         double g = (double)(0.15F + lv.nextFloat() * 0.1F);
         double h = d * d + e * e + f * f;
         if (h < 1.0 && h > 0.01) {
            h = 1.0 / Math.sqrt(h);
            d *= h;
            e *= h;
            f *= h;
            double j = d * 100.0;
            double k = e * 100.0;
            double l = f * 100.0;
            double m = Math.atan2(d, f);
            double n = Math.sin(m);
            double o = Math.cos(m);
            double p = Math.atan2(Math.sqrt(d * d + f * f), e);
            double q = Math.sin(p);
            double r = Math.cos(p);
            double s = lv.nextDouble() * Math.PI * 2.0;
            double t = Math.sin(s);
            double u = Math.cos(s);

            for(int v = 0; v < 4; ++v) {
               double w = 0.0;
               double x = (double)((v & 2) - 1) * g;
               double y = (double)((v + 1 & 2) - 1) * g;
               double z = 0.0;
               double aa = x * u - y * t;
               double ab = y * u + x * t;
               double ad = aa * q + 0.0 * r;
               double ae = 0.0 * q - aa * r;
               double af = ae * n - ab * o;
               double ah = ab * n + ae * o;
               buffer.vertex(j + af, k + ad, l + ah).next();
            }
         }
      }

      return buffer.end();
   }

   public void setWorld(@Nullable ClientWorld world) {
      this.lastCameraChunkUpdateX = Double.MIN_VALUE;
      this.lastCameraChunkUpdateY = Double.MIN_VALUE;
      this.lastCameraChunkUpdateZ = Double.MIN_VALUE;
      this.cameraChunkX = Integer.MIN_VALUE;
      this.cameraChunkY = Integer.MIN_VALUE;
      this.cameraChunkZ = Integer.MIN_VALUE;
      this.entityRenderDispatcher.setWorld(world);
      this.world = world;
      if (world != null) {
         this.reload();
      } else {
         if (this.chunks != null) {
            this.chunks.clear();
            this.chunks = null;
         }

         if (this.chunkBuilder != null) {
            this.chunkBuilder.stop();
         }

         this.chunkBuilder = null;
         this.noCullingBlockEntities.clear();
         this.renderableChunks.set((Object)null);
         this.chunkInfos.clear();
      }

   }

   public void reloadTransparencyPostProcessor() {
      if (MinecraftClient.isFabulousGraphicsOrBetter()) {
         this.loadTransparencyPostProcessor();
      } else {
         this.resetTransparencyPostProcessor();
      }

   }

   public void reload() {
      if (this.world != null) {
         this.reloadTransparencyPostProcessor();
         this.world.reloadColor();
         if (this.chunkBuilder == null) {
            this.chunkBuilder = new ChunkBuilder(this.world, this, Util.getMainWorkerExecutor(), this.client.is64Bit(), this.bufferBuilders.getBlockBufferBuilders());
         } else {
            this.chunkBuilder.setWorld(this.world);
         }

         this.shouldUpdate = true;
         this.cloudsDirty = true;
         this.builtChunks.clear();
         RenderLayers.setFancyGraphicsOrBetter(MinecraftClient.isFancyGraphicsOrBetter());
         this.viewDistance = this.client.options.getClampedViewDistance();
         if (this.chunks != null) {
            this.chunks.clear();
         }

         this.chunkBuilder.reset();
         synchronized(this.noCullingBlockEntities) {
            this.noCullingBlockEntities.clear();
         }

         this.chunks = new BuiltChunkStorage(this.chunkBuilder, this.world, this.client.options.getClampedViewDistance(), this);
         if (this.fullUpdateFuture != null) {
            try {
               this.fullUpdateFuture.get();
               this.fullUpdateFuture = null;
            } catch (Exception var3) {
               LOGGER.warn("Full update failed", var3);
            }
         }

         this.renderableChunks.set(new RenderableChunks(this.chunks.chunks.length));
         this.chunkInfos.clear();
         Entity lv = this.client.getCameraEntity();
         if (lv != null) {
            this.chunks.updateCameraPosition(lv.getX(), lv.getZ());
         }

      }
   }

   public void onResized(int width, int height) {
      this.scheduleTerrainUpdate();
      if (this.entityOutlinePostProcessor != null) {
         this.entityOutlinePostProcessor.setupDimensions(width, height);
      }

      if (this.transparencyPostProcessor != null) {
         this.transparencyPostProcessor.setupDimensions(width, height);
      }

   }

   public String getChunksDebugString() {
      int i = this.chunks.chunks.length;
      int j = this.getCompletedChunkCount();
      return String.format(Locale.ROOT, "C: %d/%d %sD: %d, %s", j, i, this.client.chunkCullingEnabled ? "(s) " : "", this.viewDistance, this.chunkBuilder == null ? "null" : this.chunkBuilder.getDebugString());
   }

   public ChunkBuilder getChunkBuilder() {
      return this.chunkBuilder;
   }

   public double getChunkCount() {
      return (double)this.chunks.chunks.length;
   }

   public double getViewDistance() {
      return (double)this.viewDistance;
   }

   public int getCompletedChunkCount() {
      int i = 0;
      ObjectListIterator var2 = this.chunkInfos.iterator();

      while(var2.hasNext()) {
         ChunkInfo lv = (ChunkInfo)var2.next();
         if (!lv.chunk.getData().isEmpty()) {
            ++i;
         }
      }

      return i;
   }

   public String getEntitiesDebugString() {
      int var10000 = this.regularEntityCount;
      return "E: " + var10000 + "/" + this.world.getRegularEntityCount() + ", B: " + this.blockEntityCount + ", SD: " + this.world.getSimulationDistance();
   }

   private void setupTerrain(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator) {
      Vec3d lv = camera.getPos();
      if (this.client.options.getClampedViewDistance() != this.viewDistance) {
         this.reload();
      }

      this.world.getProfiler().push("camera");
      double d = this.client.player.getX();
      double e = this.client.player.getY();
      double f = this.client.player.getZ();
      int i = ChunkSectionPos.getSectionCoord(d);
      int j = ChunkSectionPos.getSectionCoord(e);
      int k = ChunkSectionPos.getSectionCoord(f);
      if (this.cameraChunkX != i || this.cameraChunkY != j || this.cameraChunkZ != k) {
         this.lastCameraChunkUpdateX = d;
         this.lastCameraChunkUpdateY = e;
         this.lastCameraChunkUpdateZ = f;
         this.cameraChunkX = i;
         this.cameraChunkY = j;
         this.cameraChunkZ = k;
         this.chunks.updateCameraPosition(d, f);
      }

      this.chunkBuilder.setCameraPosition(lv);
      this.world.getProfiler().swap("cull");
      this.client.getProfiler().swap("culling");
      BlockPos lv2 = camera.getBlockPos();
      double g = Math.floor(lv.x / 8.0);
      double h = Math.floor(lv.y / 8.0);
      double l = Math.floor(lv.z / 8.0);
      this.shouldUpdate = this.shouldUpdate || g != this.lastCameraX || h != this.lastCameraY || l != this.lastCameraZ;
      this.nextUpdateTime.updateAndGet((nextUpdateTime) -> {
         if (nextUpdateTime > 0L && System.currentTimeMillis() > nextUpdateTime) {
            this.shouldUpdate = true;
            return 0L;
         } else {
            return nextUpdateTime;
         }
      });
      this.lastCameraX = g;
      this.lastCameraY = h;
      this.lastCameraZ = l;
      this.client.getProfiler().swap("update");
      boolean bl3 = this.client.chunkCullingEnabled;
      if (spectator && this.world.getBlockState(lv2).isOpaqueFullCube(this.world, lv2)) {
         bl3 = false;
      }

      if (!hasForcedFrustum) {
         if (this.shouldUpdate && (this.fullUpdateFuture == null || this.fullUpdateFuture.isDone())) {
            this.client.getProfiler().push("full_update_schedule");
            this.shouldUpdate = false;
            this.fullUpdateFuture = Util.getMainWorkerExecutor().submit(() -> {
               Queue queue = Queues.newArrayDeque();
               this.enqueueChunksInViewDistance(camera, queue);
               RenderableChunks lvx = new RenderableChunks(this.chunks.chunks.length);
               this.collectRenderableChunks(lvx.chunks, lvx.chunkInfoList, lv, queue, bl3);
               this.renderableChunks.set(lvx);
               this.updateFinished.set(true);
            });
            this.client.getProfiler().pop();
         }

         RenderableChunks lv3 = (RenderableChunks)this.renderableChunks.get();
         if (!this.builtChunks.isEmpty()) {
            this.client.getProfiler().push("partial_update");
            Queue queue = Queues.newArrayDeque();

            while(!this.builtChunks.isEmpty()) {
               ChunkBuilder.BuiltChunk lv4 = (ChunkBuilder.BuiltChunk)this.builtChunks.poll();
               ChunkInfo lv5 = lv3.chunkInfoList.getInfo(lv4);
               if (lv5 != null && lv5.chunk == lv4) {
                  queue.add(lv5);
               }
            }

            this.collectRenderableChunks(lv3.chunks, lv3.chunkInfoList, lv, queue, bl3);
            this.updateFinished.set(true);
            this.client.getProfiler().pop();
         }

         double m = Math.floor((double)(camera.getPitch() / 2.0F));
         double n = Math.floor((double)(camera.getYaw() / 2.0F));
         if (this.updateFinished.compareAndSet(true, false) || m != this.lastCameraPitch || n != this.lastCameraYaw) {
            this.applyFrustum((new Frustum(frustum)).method_38557(8));
            this.lastCameraPitch = m;
            this.lastCameraYaw = n;
         }
      }

      this.client.getProfiler().pop();
   }

   private void applyFrustum(Frustum frustum) {
      if (!MinecraftClient.getInstance().isOnThread()) {
         throw new IllegalStateException("applyFrustum called from wrong thread: " + Thread.currentThread().getName());
      } else {
         this.client.getProfiler().push("apply_frustum");
         this.chunkInfos.clear();
         Iterator var2 = ((RenderableChunks)this.renderableChunks.get()).chunks.iterator();

         while(var2.hasNext()) {
            ChunkInfo lv = (ChunkInfo)var2.next();
            if (frustum.isVisible(lv.chunk.getBoundingBox())) {
               this.chunkInfos.add(lv);
            }
         }

         this.client.getProfiler().pop();
      }
   }

   private void enqueueChunksInViewDistance(Camera camera, Queue queue) {
      int i = true;
      Vec3d lv = camera.getPos();
      BlockPos lv2 = camera.getBlockPos();
      ChunkBuilder.BuiltChunk lv3 = this.chunks.getRenderedChunk(lv2);
      if (lv3 == null) {
         boolean bl = lv2.getY() > this.world.getBottomY();
         int j = bl ? this.world.getTopY() - 8 : this.world.getBottomY() + 8;
         int k = MathHelper.floor(lv.x / 16.0) * 16;
         int l = MathHelper.floor(lv.z / 16.0) * 16;
         List list = Lists.newArrayList();

         for(int m = -this.viewDistance; m <= this.viewDistance; ++m) {
            for(int n = -this.viewDistance; n <= this.viewDistance; ++n) {
               ChunkBuilder.BuiltChunk lv4 = this.chunks.getRenderedChunk(new BlockPos(k + ChunkSectionPos.getOffsetPos(m, 8), j, l + ChunkSectionPos.getOffsetPos(n, 8)));
               if (lv4 != null) {
                  list.add(new ChunkInfo(lv4, (Direction)null, 0));
               }
            }
         }

         list.sort(Comparator.comparingDouble((chunkInfo) -> {
            return lv2.getSquaredDistance(chunkInfo.chunk.getOrigin().add(8, 8, 8));
         }));
         queue.addAll(list);
      } else {
         queue.add(new ChunkInfo(lv3, (Direction)null, 0));
      }

   }

   public void addBuiltChunk(ChunkBuilder.BuiltChunk chunk) {
      this.builtChunks.add(chunk);
   }

   private void collectRenderableChunks(LinkedHashSet chunks, ChunkInfoList chunkInfoList, Vec3d cameraPos, Queue queue, boolean chunkCullingEnabled) {
      int i = true;
      BlockPos lv = new BlockPos(MathHelper.floor(cameraPos.x / 16.0) * 16, MathHelper.floor(cameraPos.y / 16.0) * 16, MathHelper.floor(cameraPos.z / 16.0) * 16);
      BlockPos lv2 = lv.add(8, 8, 8);
      Entity.setRenderDistanceMultiplier(MathHelper.clamp((double)this.client.options.getClampedViewDistance() / 8.0, 1.0, 2.5) * (Double)this.client.options.getEntityDistanceScaling().getValue());

      while(!queue.isEmpty()) {
         ChunkInfo lv3 = (ChunkInfo)queue.poll();
         ChunkBuilder.BuiltChunk lv4 = lv3.chunk;
         chunks.add(lv3);
         boolean bl2 = Math.abs(lv4.getOrigin().getX() - lv.getX()) > 60 || Math.abs(lv4.getOrigin().getY() - lv.getY()) > 60 || Math.abs(lv4.getOrigin().getZ() - lv.getZ()) > 60;
         Direction[] var12 = DIRECTIONS;
         int var13 = var12.length;

         for(int var14 = 0; var14 < var13; ++var14) {
            Direction lv5 = var12[var14];
            ChunkBuilder.BuiltChunk lv6 = this.getAdjacentChunk(lv, lv4, lv5);
            if (lv6 != null && (!chunkCullingEnabled || !lv3.canCull(lv5.getOpposite()))) {
               if (chunkCullingEnabled && lv3.hasAnyDirection()) {
                  ChunkBuilder.ChunkData lv7 = lv4.getData();
                  boolean bl3 = false;

                  for(int j = 0; j < DIRECTIONS.length; ++j) {
                     if (lv3.hasDirection(j) && lv7.isVisibleThrough(DIRECTIONS[j].getOpposite(), lv5)) {
                        bl3 = true;
                        break;
                     }
                  }

                  if (!bl3) {
                     continue;
                  }
               }

               if (chunkCullingEnabled && bl2) {
                  byte var10001;
                  BlockPos lv8;
                  label126: {
                     label125: {
                        lv8 = lv6.getOrigin();
                        if (lv5.getAxis() == Direction.Axis.X) {
                           if (lv2.getX() <= lv8.getX()) {
                              break label125;
                           }
                        } else if (lv2.getX() >= lv8.getX()) {
                           break label125;
                        }

                        var10001 = 16;
                        break label126;
                     }

                     var10001 = 0;
                  }

                  byte var10002;
                  label118: {
                     label117: {
                        if (lv5.getAxis() == Direction.Axis.Y) {
                           if (lv2.getY() > lv8.getY()) {
                              break label117;
                           }
                        } else if (lv2.getY() < lv8.getY()) {
                           break label117;
                        }

                        var10002 = 0;
                        break label118;
                     }

                     var10002 = 16;
                  }

                  byte var10003;
                  label110: {
                     label109: {
                        if (lv5.getAxis() == Direction.Axis.Z) {
                           if (lv2.getZ() > lv8.getZ()) {
                              break label109;
                           }
                        } else if (lv2.getZ() < lv8.getZ()) {
                           break label109;
                        }

                        var10003 = 0;
                        break label110;
                     }

                     var10003 = 16;
                  }

                  BlockPos lv9 = lv8.add(var10001, var10002, var10003);
                  Vec3d lv10 = new Vec3d((double)lv9.getX(), (double)lv9.getY(), (double)lv9.getZ());
                  Vec3d lv11 = cameraPos.subtract(lv10).normalize().multiply(field_34814);
                  boolean bl4 = true;

                  label101: {
                     ChunkBuilder.BuiltChunk lv12;
                     do {
                        if (!(cameraPos.subtract(lv10).lengthSquared() > 3600.0)) {
                           break label101;
                        }

                        lv10 = lv10.add(lv11);
                        if (lv10.y > (double)this.world.getTopY() || lv10.y < (double)this.world.getBottomY()) {
                           break label101;
                        }

                        lv12 = this.chunks.getRenderedChunk(BlockPos.ofFloored(lv10.x, lv10.y, lv10.z));
                     } while(lv12 != null && chunkInfoList.getInfo(lv12) != null);

                     bl4 = false;
                  }

                  if (!bl4) {
                     continue;
                  }
               }

               ChunkInfo lv13 = chunkInfoList.getInfo(lv6);
               if (lv13 != null) {
                  lv13.addDirection(lv5);
               } else if (!lv6.shouldBuild()) {
                  if (!this.isOutsideViewDistance(lv, lv4)) {
                     this.nextUpdateTime.set(System.currentTimeMillis() + 500L);
                  }
               } else {
                  ChunkInfo lv14 = new ChunkInfo(lv6, lv5, lv3.propagationLevel + 1);
                  lv14.updateCullingState(lv3.cullingState, lv5);
                  queue.add(lv14);
                  chunkInfoList.setInfo(lv6, lv14);
               }
            }
         }
      }

   }

   @Nullable
   private ChunkBuilder.BuiltChunk getAdjacentChunk(BlockPos pos, ChunkBuilder.BuiltChunk chunk, Direction direction) {
      BlockPos lv = chunk.getNeighborPosition(direction);
      if (MathHelper.abs(pos.getX() - lv.getX()) > this.viewDistance * 16) {
         return null;
      } else if (MathHelper.abs(pos.getY() - lv.getY()) <= this.viewDistance * 16 && lv.getY() >= this.world.getBottomY() && lv.getY() < this.world.getTopY()) {
         return MathHelper.abs(pos.getZ() - lv.getZ()) > this.viewDistance * 16 ? null : this.chunks.getRenderedChunk(lv);
      } else {
         return null;
      }
   }

   private boolean isOutsideViewDistance(BlockPos pos, ChunkBuilder.BuiltChunk chunk) {
      int i = ChunkSectionPos.getSectionCoord(pos.getX());
      int j = ChunkSectionPos.getSectionCoord(pos.getZ());
      BlockPos lv = chunk.getOrigin();
      int k = ChunkSectionPos.getSectionCoord(lv.getX());
      int l = ChunkSectionPos.getSectionCoord(lv.getZ());
      return !ThreadedAnvilChunkStorage.isWithinDistance(k, l, i, j, this.viewDistance - 2);
   }

   private void captureFrustum(Matrix4f positionMatrix, Matrix4f matrix4f2, double x, double y, double z, Frustum frustum) {
      this.capturedFrustum = frustum;
      Matrix4f matrix4f3 = new Matrix4f(matrix4f2);
      matrix4f3.mul(positionMatrix);
      matrix4f3.invert();
      this.capturedFrustumPosition.x = x;
      this.capturedFrustumPosition.y = y;
      this.capturedFrustumPosition.z = z;
      this.capturedFrustumOrientation[0] = new Vector4f(-1.0F, -1.0F, -1.0F, 1.0F);
      this.capturedFrustumOrientation[1] = new Vector4f(1.0F, -1.0F, -1.0F, 1.0F);
      this.capturedFrustumOrientation[2] = new Vector4f(1.0F, 1.0F, -1.0F, 1.0F);
      this.capturedFrustumOrientation[3] = new Vector4f(-1.0F, 1.0F, -1.0F, 1.0F);
      this.capturedFrustumOrientation[4] = new Vector4f(-1.0F, -1.0F, 1.0F, 1.0F);
      this.capturedFrustumOrientation[5] = new Vector4f(1.0F, -1.0F, 1.0F, 1.0F);
      this.capturedFrustumOrientation[6] = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.capturedFrustumOrientation[7] = new Vector4f(-1.0F, 1.0F, 1.0F, 1.0F);

      for(int i = 0; i < 8; ++i) {
         matrix4f3.transform(this.capturedFrustumOrientation[i]);
         this.capturedFrustumOrientation[i].div(this.capturedFrustumOrientation[i].w());
      }

   }

   public void setupFrustum(MatrixStack matrices, Vec3d pos, Matrix4f projectionMatrix) {
      Matrix4f matrix4f2 = matrices.peek().getPositionMatrix();
      double d = pos.getX();
      double e = pos.getY();
      double f = pos.getZ();
      this.frustum = new Frustum(matrix4f2, projectionMatrix);
      this.frustum.setPosition(d, e, f);
   }

   public void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix) {
      RenderSystem.setShaderGameTime(this.world.getTime(), tickDelta);
      this.blockEntityRenderDispatcher.configure(this.world, camera, this.client.crosshairTarget);
      this.entityRenderDispatcher.configure(this.world, camera, this.client.targetedEntity);
      Profiler lv = this.world.getProfiler();
      lv.swap("light_update_queue");
      this.world.runQueuedChunkUpdates();
      lv.swap("light_updates");
      boolean bl2 = this.world.hasNoChunkUpdaters();
      this.world.getChunkManager().getLightingProvider().doLightUpdates(Integer.MAX_VALUE, bl2, true);
      Vec3d lv2 = camera.getPos();
      double d = lv2.getX();
      double e = lv2.getY();
      double g = lv2.getZ();
      Matrix4f matrix4f2 = matrices.peek().getPositionMatrix();
      lv.swap("culling");
      boolean bl3 = this.capturedFrustum != null;
      Frustum lv3;
      if (bl3) {
         lv3 = this.capturedFrustum;
         lv3.setPosition(this.capturedFrustumPosition.x, this.capturedFrustumPosition.y, this.capturedFrustumPosition.z);
      } else {
         lv3 = this.frustum;
      }

      this.client.getProfiler().swap("captureFrustum");
      if (this.shouldCaptureFrustum) {
         this.captureFrustum(matrix4f2, positionMatrix, lv2.x, lv2.y, lv2.z, bl3 ? new Frustum(matrix4f2, positionMatrix) : lv3);
         this.shouldCaptureFrustum = false;
      }

      lv.swap("clear");
      BackgroundRenderer.render(camera, tickDelta, this.client.world, this.client.options.getClampedViewDistance(), gameRenderer.getSkyDarkness(tickDelta));
      BackgroundRenderer.setFogBlack();
      RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT | GlConst.GL_COLOR_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
      float h = gameRenderer.getViewDistance();
      boolean bl4 = this.client.world.getDimensionEffects().useThickFog(MathHelper.floor(d), MathHelper.floor(e)) || this.client.inGameHud.getBossBarHud().shouldThickenFog();
      lv.swap("sky");
      RenderSystem.setShader(GameRenderer::getPositionProgram);
      this.renderSky(matrices, positionMatrix, tickDelta, camera, bl4, () -> {
         BackgroundRenderer.applyFog(camera, BackgroundRenderer.FogType.FOG_SKY, h, bl4, tickDelta);
      });
      lv.swap("fog");
      BackgroundRenderer.applyFog(camera, BackgroundRenderer.FogType.FOG_TERRAIN, Math.max(h, 32.0F), bl4, tickDelta);
      lv.swap("terrain_setup");
      this.setupTerrain(camera, lv3, bl3, this.client.player.isSpectator());
      lv.swap("compilechunks");
      this.updateChunks(camera);
      lv.swap("terrain");
      this.renderLayer(RenderLayer.getSolid(), matrices, d, e, g, positionMatrix);
      this.renderLayer(RenderLayer.getCutoutMipped(), matrices, d, e, g, positionMatrix);
      this.renderLayer(RenderLayer.getCutout(), matrices, d, e, g, positionMatrix);
      if (this.world.getDimensionEffects().isDarkened()) {
         DiffuseLighting.enableForLevel(matrices.peek().getPositionMatrix());
      } else {
         DiffuseLighting.disableForLevel(matrices.peek().getPositionMatrix());
      }

      lv.swap("entities");
      this.regularEntityCount = 0;
      this.blockEntityCount = 0;
      if (this.entityFramebuffer != null) {
         this.entityFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
         this.entityFramebuffer.copyDepthFrom(this.client.getFramebuffer());
         this.client.getFramebuffer().beginWrite(false);
      }

      if (this.weatherFramebuffer != null) {
         this.weatherFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
      }

      if (this.canDrawEntityOutlines()) {
         this.entityOutlinesFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
         this.client.getFramebuffer().beginWrite(false);
      }

      boolean bl5 = false;
      VertexConsumerProvider.Immediate lv4 = this.bufferBuilders.getEntityVertexConsumers();
      Iterator var26 = this.world.getEntities().iterator();

      while(true) {
         Entity lv5;
         do {
            BlockPos lv6;
            do {
               do {
                  do {
                     if (!var26.hasNext()) {
                        lv4.drawCurrentLayer();
                        this.checkEmpty(matrices);
                        lv4.draw(RenderLayer.getEntitySolid(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
                        lv4.draw(RenderLayer.getEntityCutout(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
                        lv4.draw(RenderLayer.getEntityCutoutNoCull(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
                        lv4.draw(RenderLayer.getEntitySmoothCutout(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
                        lv.swap("blockentities");
                        ObjectListIterator var40 = this.chunkInfos.iterator();

                        while(true) {
                           List list;
                           do {
                              if (!var40.hasNext()) {
                                 synchronized(this.noCullingBlockEntities) {
                                    Iterator var44 = this.noCullingBlockEntities.iterator();

                                    while(var44.hasNext()) {
                                       BlockEntity lv15 = (BlockEntity)var44.next();
                                       BlockPos lv16 = lv15.getPos();
                                       matrices.push();
                                       matrices.translate((double)lv16.getX() - d, (double)lv16.getY() - e, (double)lv16.getZ() - g);
                                       this.blockEntityRenderDispatcher.render(lv15, tickDelta, matrices, lv4);
                                       matrices.pop();
                                    }
                                 }

                                 this.checkEmpty(matrices);
                                 lv4.draw(RenderLayer.getSolid());
                                 lv4.draw(RenderLayer.getEndPortal());
                                 lv4.draw(RenderLayer.getEndGateway());
                                 lv4.draw(TexturedRenderLayers.getEntitySolid());
                                 lv4.draw(TexturedRenderLayers.getEntityCutout());
                                 lv4.draw(TexturedRenderLayers.getBeds());
                                 lv4.draw(TexturedRenderLayers.getShulkerBoxes());
                                 lv4.draw(TexturedRenderLayers.getSign());
                                 lv4.draw(TexturedRenderLayers.getHangingSign());
                                 lv4.draw(TexturedRenderLayers.getChest());
                                 this.bufferBuilders.getOutlineVertexConsumers().draw();
                                 if (bl5) {
                                    this.entityOutlinePostProcessor.render(tickDelta);
                                    this.client.getFramebuffer().beginWrite(false);
                                 }

                                 lv.swap("destroyProgress");
                                 ObjectIterator var41 = this.blockBreakingProgressions.long2ObjectEntrySet().iterator();

                                 while(var41.hasNext()) {
                                    Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)var41.next();
                                    lv6 = BlockPos.fromLong(entry.getLongKey());
                                    double k = (double)lv6.getX() - d;
                                    double m = (double)lv6.getY() - e;
                                    double n = (double)lv6.getZ() - g;
                                    if (!(k * k + m * m + n * n > 1024.0)) {
                                       SortedSet sortedSet2 = (SortedSet)entry.getValue();
                                       if (sortedSet2 != null && !sortedSet2.isEmpty()) {
                                          int o = ((BlockBreakingInfo)sortedSet2.last()).getStage();
                                          matrices.push();
                                          matrices.translate((double)lv6.getX() - d, (double)lv6.getY() - e, (double)lv6.getZ() - g);
                                          MatrixStack.Entry lv17 = matrices.peek();
                                          VertexConsumer lv18 = new OverlayVertexConsumer(this.bufferBuilders.getEffectVertexConsumers().getBuffer((RenderLayer)ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.get(o)), lv17.getPositionMatrix(), lv17.getNormalMatrix(), 1.0F);
                                          this.client.getBlockRenderManager().renderDamage(this.world.getBlockState(lv6), lv6, this.world, matrices, lv18);
                                          matrices.pop();
                                       }
                                    }
                                 }

                                 this.checkEmpty(matrices);
                                 HitResult lv19 = this.client.crosshairTarget;
                                 if (renderBlockOutline && lv19 != null && lv19.getType() == HitResult.Type.BLOCK) {
                                    lv.swap("outline");
                                    BlockPos lv20 = ((BlockHitResult)lv19).getBlockPos();
                                    BlockState lv21 = this.world.getBlockState(lv20);
                                    if (!lv21.isAir() && this.world.getWorldBorder().contains(lv20)) {
                                       VertexConsumer lv22 = lv4.getBuffer(RenderLayer.getLines());
                                       this.drawBlockOutline(matrices, lv22, camera.getFocusedEntity(), d, e, g, lv20, lv21);
                                    }
                                 }

                                 this.client.debugRenderer.render(matrices, lv4, d, e, g);
                                 lv4.drawCurrentLayer();
                                 MatrixStack lv23 = RenderSystem.getModelViewStack();
                                 RenderSystem.applyModelViewMatrix();
                                 lv4.draw(TexturedRenderLayers.getEntityTranslucentCull());
                                 lv4.draw(TexturedRenderLayers.getBannerPatterns());
                                 lv4.draw(TexturedRenderLayers.getShieldPatterns());
                                 lv4.draw(RenderLayer.getArmorGlint());
                                 lv4.draw(RenderLayer.getArmorEntityGlint());
                                 lv4.draw(RenderLayer.getGlint());
                                 lv4.draw(RenderLayer.getDirectGlint());
                                 lv4.draw(RenderLayer.getGlintTranslucent());
                                 lv4.draw(RenderLayer.getEntityGlint());
                                 lv4.draw(RenderLayer.getDirectEntityGlint());
                                 lv4.draw(RenderLayer.getWaterMask());
                                 this.bufferBuilders.getEffectVertexConsumers().draw();
                                 if (this.transparencyPostProcessor != null) {
                                    lv4.draw(RenderLayer.getLines());
                                    lv4.draw();
                                    this.translucentFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
                                    this.translucentFramebuffer.copyDepthFrom(this.client.getFramebuffer());
                                    lv.swap("translucent");
                                    this.renderLayer(RenderLayer.getTranslucent(), matrices, d, e, g, positionMatrix);
                                    lv.swap("string");
                                    this.renderLayer(RenderLayer.getTripwire(), matrices, d, e, g, positionMatrix);
                                    this.particlesFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
                                    this.particlesFramebuffer.copyDepthFrom(this.client.getFramebuffer());
                                    RenderPhase.PARTICLES_TARGET.startDrawing();
                                    lv.swap("particles");
                                    this.client.particleManager.renderParticles(matrices, lv4, lightmapTextureManager, camera, tickDelta);
                                    RenderPhase.PARTICLES_TARGET.endDrawing();
                                 } else {
                                    lv.swap("translucent");
                                    if (this.translucentFramebuffer != null) {
                                       this.translucentFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
                                    }

                                    this.renderLayer(RenderLayer.getTranslucent(), matrices, d, e, g, positionMatrix);
                                    lv4.draw(RenderLayer.getLines());
                                    lv4.draw();
                                    lv.swap("string");
                                    this.renderLayer(RenderLayer.getTripwire(), matrices, d, e, g, positionMatrix);
                                    lv.swap("particles");
                                    this.client.particleManager.renderParticles(matrices, lv4, lightmapTextureManager, camera, tickDelta);
                                 }

                                 lv23.push();
                                 lv23.multiplyPositionMatrix(matrices.peek().getPositionMatrix());
                                 RenderSystem.applyModelViewMatrix();
                                 if (this.client.options.getCloudRenderModeValue() != CloudRenderMode.OFF) {
                                    if (this.transparencyPostProcessor != null) {
                                       this.cloudsFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
                                       RenderPhase.CLOUDS_TARGET.startDrawing();
                                       lv.swap("clouds");
                                       this.renderClouds(matrices, positionMatrix, tickDelta, d, e, g);
                                       RenderPhase.CLOUDS_TARGET.endDrawing();
                                    } else {
                                       lv.swap("clouds");
                                       RenderSystem.setShader(GameRenderer::getPositionTexColorNormalProgram);
                                       this.renderClouds(matrices, positionMatrix, tickDelta, d, e, g);
                                    }
                                 }

                                 if (this.transparencyPostProcessor != null) {
                                    RenderPhase.WEATHER_TARGET.startDrawing();
                                    lv.swap("weather");
                                    this.renderWeather(lightmapTextureManager, tickDelta, d, e, g);
                                    this.renderWorldBorder(camera);
                                    RenderPhase.WEATHER_TARGET.endDrawing();
                                    this.transparencyPostProcessor.render(tickDelta);
                                    this.client.getFramebuffer().beginWrite(false);
                                 } else {
                                    RenderSystem.depthMask(false);
                                    lv.swap("weather");
                                    this.renderWeather(lightmapTextureManager, tickDelta, d, e, g);
                                    this.renderWorldBorder(camera);
                                    RenderSystem.depthMask(true);
                                 }

                                 lv23.pop();
                                 RenderSystem.applyModelViewMatrix();
                                 this.renderChunkDebugInfo(matrices, lv4, camera);
                                 lv4.drawCurrentLayer();
                                 RenderSystem.depthMask(true);
                                 RenderSystem.disableBlend();
                                 BackgroundRenderer.clearFog();
                                 return;
                              }

                              ChunkInfo lv9 = (ChunkInfo)var40.next();
                              list = lv9.chunk.getData().getBlockEntities();
                           } while(list.isEmpty());

                           Iterator var51 = list.iterator();

                           while(var51.hasNext()) {
                              BlockEntity lv10 = (BlockEntity)var51.next();
                              BlockPos lv11 = lv10.getPos();
                              VertexConsumerProvider lv12 = lv4;
                              matrices.push();
                              matrices.translate((double)lv11.getX() - d, (double)lv11.getY() - e, (double)lv11.getZ() - g);
                              SortedSet sortedSet = (SortedSet)this.blockBreakingProgressions.get(lv11.asLong());
                              if (sortedSet != null && !sortedSet.isEmpty()) {
                                 int j = ((BlockBreakingInfo)sortedSet.last()).getStage();
                                 if (j >= 0) {
                                    MatrixStack.Entry lv13 = matrices.peek();
                                    VertexConsumer lv14 = new OverlayVertexConsumer(this.bufferBuilders.getEffectVertexConsumers().getBuffer((RenderLayer)ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.get(j)), lv13.getPositionMatrix(), lv13.getNormalMatrix(), 1.0F);
                                    lv12 = (renderLayer) -> {
                                       VertexConsumer lv = lv4.getBuffer(renderLayer);
                                       return renderLayer.hasCrumbling() ? VertexConsumers.union(lv14, lv) : lv;
                                    };
                                 }
                              }

                              this.blockEntityRenderDispatcher.render(lv10, tickDelta, matrices, (VertexConsumerProvider)lv12);
                              matrices.pop();
                           }
                        }
                     }

                     lv5 = (Entity)var26.next();
                  } while(!this.entityRenderDispatcher.shouldRender(lv5, lv3, d, e, g) && !lv5.hasPassengerDeep(this.client.player));

                  lv6 = lv5.getBlockPos();
               } while(!this.world.isOutOfHeightLimit(lv6.getY()) && !this.isRenderingReady(lv6));
            } while(lv5 == camera.getFocusedEntity() && !camera.isThirdPerson() && (!(camera.getFocusedEntity() instanceof LivingEntity) || !((LivingEntity)camera.getFocusedEntity()).isSleeping()));
         } while(lv5 instanceof ClientPlayerEntity && camera.getFocusedEntity() != lv5);

         ++this.regularEntityCount;
         if (lv5.age == 0) {
            lv5.lastRenderX = lv5.getX();
            lv5.lastRenderY = lv5.getY();
            lv5.lastRenderZ = lv5.getZ();
         }

         Object lv8;
         if (this.canDrawEntityOutlines() && this.client.hasOutline(lv5)) {
            bl5 = true;
            OutlineVertexConsumerProvider lv7 = this.bufferBuilders.getOutlineVertexConsumers();
            lv8 = lv7;
            int i = lv5.getTeamColorValue();
            lv7.setColor(ColorHelper.Argb.getRed(i), ColorHelper.Argb.getGreen(i), ColorHelper.Argb.getBlue(i), 255);
         } else {
            lv8 = lv4;
         }

         this.renderEntity(lv5, d, e, g, tickDelta, matrices, (VertexConsumerProvider)lv8);
      }
   }

   private void checkEmpty(MatrixStack matrices) {
      if (!matrices.isEmpty()) {
         throw new IllegalStateException("Pose stack not empty");
      }
   }

   private void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
      double h = MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX());
      double i = MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY());
      double j = MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ());
      float k = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw());
      this.entityRenderDispatcher.render(entity, h - cameraX, i - cameraY, j - cameraZ, k, tickDelta, matrices, vertexConsumers, this.entityRenderDispatcher.getLight(entity, tickDelta));
   }

   private void renderLayer(RenderLayer renderLayer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f positionMatrix) {
      RenderSystem.assertOnRenderThread();
      renderLayer.startDrawing();
      if (renderLayer == RenderLayer.getTranslucent()) {
         this.client.getProfiler().push("translucent_sort");
         double g = cameraX - this.lastTranslucentSortX;
         double h = cameraY - this.lastTranslucentSortY;
         double i = cameraZ - this.lastTranslucentSortZ;
         if (g * g + h * h + i * i > 1.0) {
            int j = ChunkSectionPos.getSectionCoord(cameraX);
            int k = ChunkSectionPos.getSectionCoord(cameraY);
            int l = ChunkSectionPos.getSectionCoord(cameraZ);
            boolean bl = j != ChunkSectionPos.getSectionCoord(this.lastTranslucentSortX) || l != ChunkSectionPos.getSectionCoord(this.lastTranslucentSortZ) || k != ChunkSectionPos.getSectionCoord(this.lastTranslucentSortY);
            this.lastTranslucentSortX = cameraX;
            this.lastTranslucentSortY = cameraY;
            this.lastTranslucentSortZ = cameraZ;
            int m = 0;
            ObjectListIterator var21 = this.chunkInfos.iterator();

            label125:
            while(true) {
               ChunkInfo lv;
               do {
                  do {
                     if (!var21.hasNext()) {
                        break label125;
                     }

                     lv = (ChunkInfo)var21.next();
                  } while(m >= 15);
               } while(!bl && !lv.method_49633(j, k, l));

               if (lv.chunk.scheduleSort(renderLayer, this.chunkBuilder)) {
                  ++m;
               }
            }
         }

         this.client.getProfiler().pop();
      }

      this.client.getProfiler().push("filterempty");
      this.client.getProfiler().swap(() -> {
         return "render_" + renderLayer;
      });
      boolean bl2 = renderLayer != RenderLayer.getTranslucent();
      ObjectListIterator objectListIterator = this.chunkInfos.listIterator(bl2 ? 0 : this.chunkInfos.size());
      ShaderProgram lv2 = RenderSystem.getShader();

      for(int n = 0; n < 12; ++n) {
         int o = RenderSystem.getShaderTexture(n);
         lv2.addSampler("Sampler" + n, o);
      }

      if (lv2.modelViewMat != null) {
         lv2.modelViewMat.set(matrices.peek().getPositionMatrix());
      }

      if (lv2.projectionMat != null) {
         lv2.projectionMat.set(positionMatrix);
      }

      if (lv2.colorModulator != null) {
         lv2.colorModulator.set(RenderSystem.getShaderColor());
      }

      if (lv2.glintAlpha != null) {
         lv2.glintAlpha.set(RenderSystem.getShaderGlintAlpha());
      }

      if (lv2.fogStart != null) {
         lv2.fogStart.set(RenderSystem.getShaderFogStart());
      }

      if (lv2.fogEnd != null) {
         lv2.fogEnd.set(RenderSystem.getShaderFogEnd());
      }

      if (lv2.fogColor != null) {
         lv2.fogColor.set(RenderSystem.getShaderFogColor());
      }

      if (lv2.fogShape != null) {
         lv2.fogShape.set(RenderSystem.getShaderFogShape().getId());
      }

      if (lv2.textureMat != null) {
         lv2.textureMat.set(RenderSystem.getTextureMatrix());
      }

      if (lv2.gameTime != null) {
         lv2.gameTime.set(RenderSystem.getShaderGameTime());
      }

      RenderSystem.setupShaderLights(lv2);
      lv2.bind();
      GlUniform lv3 = lv2.chunkOffset;

      while(true) {
         if (bl2) {
            if (!objectListIterator.hasNext()) {
               break;
            }
         } else if (!objectListIterator.hasPrevious()) {
            break;
         }

         ChunkInfo lv4 = bl2 ? (ChunkInfo)objectListIterator.next() : (ChunkInfo)objectListIterator.previous();
         ChunkBuilder.BuiltChunk lv5 = lv4.chunk;
         if (!lv5.getData().isEmpty(renderLayer)) {
            VertexBuffer lv6 = lv5.getBuffer(renderLayer);
            BlockPos lv7 = lv5.getOrigin();
            if (lv3 != null) {
               lv3.set((float)((double)lv7.getX() - cameraX), (float)((double)lv7.getY() - cameraY), (float)((double)lv7.getZ() - cameraZ));
               lv3.upload();
            }

            lv6.bind();
            lv6.draw();
         }
      }

      if (lv3 != null) {
         lv3.set(0.0F, 0.0F, 0.0F);
      }

      lv2.unbind();
      VertexBuffer.unbind();
      this.client.getProfiler().pop();
      renderLayer.endDrawing();
   }

   private void renderChunkDebugInfo(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Camera camera) {
      if (this.client.debugChunkInfo || this.client.debugChunkOcclusion) {
         double d = camera.getPos().getX();
         double e = camera.getPos().getY();
         double f = camera.getPos().getZ();

         for(ObjectListIterator var10 = this.chunkInfos.iterator(); var10.hasNext(); matrices.pop()) {
            ChunkInfo lv = (ChunkInfo)var10.next();
            ChunkBuilder.BuiltChunk lv2 = lv.chunk;
            BlockPos lv3 = lv2.getOrigin();
            matrices.push();
            matrices.translate((double)lv3.getX() - d, (double)lv3.getY() - e, (double)lv3.getZ() - f);
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            VertexConsumer lv4;
            int i;
            int k;
            int l;
            if (this.client.debugChunkInfo) {
               lv4 = vertexConsumers.getBuffer(RenderLayer.getLines());
               i = lv.propagationLevel == 0 ? 0 : MathHelper.hsvToRgb((float)lv.propagationLevel / 50.0F, 0.9F, 0.9F);
               int j = i >> 16 & 255;
               k = i >> 8 & 255;
               l = i & 255;

               for(int m = 0; m < DIRECTIONS.length; ++m) {
                  if (lv.hasDirection(m)) {
                     Direction lv5 = DIRECTIONS[m];
                     lv4.vertex(matrix4f, 8.0F, 8.0F, 8.0F).color(j, k, l, 255).normal((float)lv5.getOffsetX(), (float)lv5.getOffsetY(), (float)lv5.getOffsetZ()).next();
                     lv4.vertex(matrix4f, (float)(8 - 16 * lv5.getOffsetX()), (float)(8 - 16 * lv5.getOffsetY()), (float)(8 - 16 * lv5.getOffsetZ())).color(j, k, l, 255).normal((float)lv5.getOffsetX(), (float)lv5.getOffsetY(), (float)lv5.getOffsetZ()).next();
                  }
               }
            }

            if (this.client.debugChunkOcclusion && !lv2.getData().isEmpty()) {
               lv4 = vertexConsumers.getBuffer(RenderLayer.getLines());
               i = 0;
               Direction[] var28 = DIRECTIONS;
               k = var28.length;

               for(l = 0; l < k; ++l) {
                  Direction lv6 = var28[l];
                  Direction[] var33 = DIRECTIONS;
                  int var22 = var33.length;

                  for(int var23 = 0; var23 < var22; ++var23) {
                     Direction lv7 = var33[var23];
                     boolean bl = lv2.getData().isVisibleThrough(lv6, lv7);
                     if (!bl) {
                        ++i;
                        lv4.vertex(matrix4f, (float)(8 + 8 * lv6.getOffsetX()), (float)(8 + 8 * lv6.getOffsetY()), (float)(8 + 8 * lv6.getOffsetZ())).color(255, 0, 0, 255).normal((float)lv6.getOffsetX(), (float)lv6.getOffsetY(), (float)lv6.getOffsetZ()).next();
                        lv4.vertex(matrix4f, (float)(8 + 8 * lv7.getOffsetX()), (float)(8 + 8 * lv7.getOffsetY()), (float)(8 + 8 * lv7.getOffsetZ())).color(255, 0, 0, 255).normal((float)lv7.getOffsetX(), (float)lv7.getOffsetY(), (float)lv7.getOffsetZ()).next();
                     }
                  }
               }

               if (i > 0) {
                  VertexConsumer lv8 = vertexConsumers.getBuffer(RenderLayer.getDebugQuads());
                  float g = 0.5F;
                  float h = 0.2F;
                  lv8.vertex(matrix4f, 0.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 15.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 15.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 0.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 0.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 15.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 15.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 0.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 0.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 0.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 0.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 0.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 15.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 15.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 15.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 15.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 0.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 15.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 15.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 0.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 0.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 15.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 15.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
                  lv8.vertex(matrix4f, 0.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).next();
               }
            }
         }
      }

      if (this.capturedFrustum != null) {
         matrices.push();
         matrices.translate((float)(this.capturedFrustumPosition.x - camera.getPos().x), (float)(this.capturedFrustumPosition.y - camera.getPos().y), (float)(this.capturedFrustumPosition.z - camera.getPos().z));
         Matrix4f matrix4f2 = matrices.peek().getPositionMatrix();
         VertexConsumer lv9 = vertexConsumers.getBuffer(RenderLayer.getDebugQuads());
         this.method_22985(lv9, matrix4f2, 0, 1, 2, 3, 0, 1, 1);
         this.method_22985(lv9, matrix4f2, 4, 5, 6, 7, 1, 0, 0);
         this.method_22985(lv9, matrix4f2, 0, 1, 5, 4, 1, 1, 0);
         this.method_22985(lv9, matrix4f2, 2, 3, 7, 6, 0, 0, 1);
         this.method_22985(lv9, matrix4f2, 0, 4, 7, 3, 0, 1, 0);
         this.method_22985(lv9, matrix4f2, 1, 5, 6, 2, 1, 0, 1);
         VertexConsumer lv10 = vertexConsumers.getBuffer(RenderLayer.getLines());
         this.method_22984(lv10, matrix4f2, 0);
         this.method_22984(lv10, matrix4f2, 1);
         this.method_22984(lv10, matrix4f2, 1);
         this.method_22984(lv10, matrix4f2, 2);
         this.method_22984(lv10, matrix4f2, 2);
         this.method_22984(lv10, matrix4f2, 3);
         this.method_22984(lv10, matrix4f2, 3);
         this.method_22984(lv10, matrix4f2, 0);
         this.method_22984(lv10, matrix4f2, 4);
         this.method_22984(lv10, matrix4f2, 5);
         this.method_22984(lv10, matrix4f2, 5);
         this.method_22984(lv10, matrix4f2, 6);
         this.method_22984(lv10, matrix4f2, 6);
         this.method_22984(lv10, matrix4f2, 7);
         this.method_22984(lv10, matrix4f2, 7);
         this.method_22984(lv10, matrix4f2, 4);
         this.method_22984(lv10, matrix4f2, 0);
         this.method_22984(lv10, matrix4f2, 4);
         this.method_22984(lv10, matrix4f2, 1);
         this.method_22984(lv10, matrix4f2, 5);
         this.method_22984(lv10, matrix4f2, 2);
         this.method_22984(lv10, matrix4f2, 6);
         this.method_22984(lv10, matrix4f2, 3);
         this.method_22984(lv10, matrix4f2, 7);
         matrices.pop();
      }

   }

   private void method_22984(VertexConsumer arg, Matrix4f matrix4f, int i) {
      arg.vertex(matrix4f, this.capturedFrustumOrientation[i].x(), this.capturedFrustumOrientation[i].y(), this.capturedFrustumOrientation[i].z()).color(0, 0, 0, 255).normal(0.0F, 0.0F, -1.0F).next();
   }

   private void method_22985(VertexConsumer arg, Matrix4f matrix4f, int i, int j, int k, int l, int m, int n, int o) {
      float f = 0.25F;
      arg.vertex(matrix4f, this.capturedFrustumOrientation[i].x(), this.capturedFrustumOrientation[i].y(), this.capturedFrustumOrientation[i].z()).color((float)m, (float)n, (float)o, 0.25F).next();
      arg.vertex(matrix4f, this.capturedFrustumOrientation[j].x(), this.capturedFrustumOrientation[j].y(), this.capturedFrustumOrientation[j].z()).color((float)m, (float)n, (float)o, 0.25F).next();
      arg.vertex(matrix4f, this.capturedFrustumOrientation[k].x(), this.capturedFrustumOrientation[k].y(), this.capturedFrustumOrientation[k].z()).color((float)m, (float)n, (float)o, 0.25F).next();
      arg.vertex(matrix4f, this.capturedFrustumOrientation[l].x(), this.capturedFrustumOrientation[l].y(), this.capturedFrustumOrientation[l].z()).color((float)m, (float)n, (float)o, 0.25F).next();
   }

   public void captureFrustum() {
      this.shouldCaptureFrustum = true;
   }

   public void killFrustum() {
      this.capturedFrustum = null;
   }

   public void tick() {
      ++this.ticks;
      if (this.ticks % 20 == 0) {
         Iterator iterator = this.blockBreakingInfos.values().iterator();

         while(iterator.hasNext()) {
            BlockBreakingInfo lv = (BlockBreakingInfo)iterator.next();
            int i = lv.getLastUpdateTick();
            if (this.ticks - i > 400) {
               iterator.remove();
               this.removeBlockBreakingInfo(lv);
            }
         }

      }
   }

   private void removeBlockBreakingInfo(BlockBreakingInfo info) {
      long l = info.getPos().asLong();
      Set set = (Set)this.blockBreakingProgressions.get(l);
      set.remove(info);
      if (set.isEmpty()) {
         this.blockBreakingProgressions.remove(l);
      }

   }

   private void renderEndSky(MatrixStack matrices) {
      RenderSystem.enableBlend();
      RenderSystem.depthMask(false);
      RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
      RenderSystem.setShaderTexture(0, END_SKY);
      Tessellator lv = Tessellator.getInstance();
      BufferBuilder lv2 = lv.getBuffer();

      for(int i = 0; i < 6; ++i) {
         matrices.push();
         if (i == 1) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
         }

         if (i == 2) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
         }

         if (i == 3) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0F));
         }

         if (i == 4) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
         }

         if (i == 5) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-90.0F));
         }

         Matrix4f matrix4f = matrices.peek().getPositionMatrix();
         lv2.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         lv2.vertex(matrix4f, -100.0F, -100.0F, -100.0F).texture(0.0F, 0.0F).color(40, 40, 40, 255).next();
         lv2.vertex(matrix4f, -100.0F, -100.0F, 100.0F).texture(0.0F, 16.0F).color(40, 40, 40, 255).next();
         lv2.vertex(matrix4f, 100.0F, -100.0F, 100.0F).texture(16.0F, 16.0F).color(40, 40, 40, 255).next();
         lv2.vertex(matrix4f, 100.0F, -100.0F, -100.0F).texture(16.0F, 0.0F).color(40, 40, 40, 255).next();
         lv.draw();
         matrices.pop();
      }

      RenderSystem.depthMask(true);
      RenderSystem.disableBlend();
   }

   public void renderSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera arg2, boolean bl, Runnable runnable) {
      runnable.run();
      if (!bl) {
         CameraSubmersionType lv = arg2.getSubmersionType();
         if (lv != CameraSubmersionType.POWDER_SNOW && lv != CameraSubmersionType.LAVA && !this.hasBlindnessOrDarkness(arg2)) {
            if (this.client.world.getDimensionEffects().getSkyType() == DimensionEffects.SkyType.END) {
               this.renderEndSky(matrices);
            } else if (this.client.world.getDimensionEffects().getSkyType() == DimensionEffects.SkyType.NORMAL) {
               Vec3d lv2 = this.world.getSkyColor(this.client.gameRenderer.getCamera().getPos(), tickDelta);
               float g = (float)lv2.x;
               float h = (float)lv2.y;
               float i = (float)lv2.z;
               BackgroundRenderer.setFogBlack();
               BufferBuilder lv3 = Tessellator.getInstance().getBuffer();
               RenderSystem.depthMask(false);
               RenderSystem.setShaderColor(g, h, i, 1.0F);
               ShaderProgram lv4 = RenderSystem.getShader();
               this.lightSkyBuffer.bind();
               this.lightSkyBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix, lv4);
               VertexBuffer.unbind();
               RenderSystem.enableBlend();
               float[] fs = this.world.getDimensionEffects().getFogColorOverride(this.world.getSkyAngle(tickDelta), tickDelta);
               float j;
               float l;
               float p;
               float q;
               float r;
               if (fs != null) {
                  RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                  matrices.push();
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
                  j = MathHelper.sin(this.world.getSkyAngleRadians(tickDelta)) < 0.0F ? 180.0F : 0.0F;
                  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(j));
                  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
                  float k = fs[0];
                  l = fs[1];
                  float m = fs[2];
                  Matrix4f matrix4f2 = matrices.peek().getPositionMatrix();
                  lv3.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
                  lv3.vertex(matrix4f2, 0.0F, 100.0F, 0.0F).color(k, l, m, fs[3]).next();
                  int n = true;

                  for(int o = 0; o <= 16; ++o) {
                     p = (float)o * 6.2831855F / 16.0F;
                     q = MathHelper.sin(p);
                     r = MathHelper.cos(p);
                     lv3.vertex(matrix4f2, q * 120.0F, r * 120.0F, -r * 40.0F * fs[3]).color(fs[0], fs[1], fs[2], 0.0F).next();
                  }

                  BufferRenderer.drawWithGlobalProgram(lv3.end());
                  matrices.pop();
               }

               RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
               matrices.push();
               j = 1.0F - this.world.getRainGradient(tickDelta);
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, j);
               matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F));
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(this.world.getSkyAngle(tickDelta) * 360.0F));
               Matrix4f matrix4f3 = matrices.peek().getPositionMatrix();
               l = 30.0F;
               RenderSystem.setShader(GameRenderer::getPositionTexProgram);
               RenderSystem.setShaderTexture(0, SUN);
               lv3.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
               lv3.vertex(matrix4f3, -l, 100.0F, -l).texture(0.0F, 0.0F).next();
               lv3.vertex(matrix4f3, l, 100.0F, -l).texture(1.0F, 0.0F).next();
               lv3.vertex(matrix4f3, l, 100.0F, l).texture(1.0F, 1.0F).next();
               lv3.vertex(matrix4f3, -l, 100.0F, l).texture(0.0F, 1.0F).next();
               BufferRenderer.drawWithGlobalProgram(lv3.end());
               l = 20.0F;
               RenderSystem.setShaderTexture(0, MOON_PHASES);
               int s = this.world.getMoonPhase();
               int t = s % 4;
               int n = s / 4 % 2;
               float u = (float)(t + 0) / 4.0F;
               p = (float)(n + 0) / 2.0F;
               q = (float)(t + 1) / 4.0F;
               r = (float)(n + 1) / 2.0F;
               lv3.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
               lv3.vertex(matrix4f3, -l, -100.0F, l).texture(q, r).next();
               lv3.vertex(matrix4f3, l, -100.0F, l).texture(u, r).next();
               lv3.vertex(matrix4f3, l, -100.0F, -l).texture(u, p).next();
               lv3.vertex(matrix4f3, -l, -100.0F, -l).texture(q, p).next();
               BufferRenderer.drawWithGlobalProgram(lv3.end());
               float v = this.world.method_23787(tickDelta) * j;
               if (v > 0.0F) {
                  RenderSystem.setShaderColor(v, v, v, v);
                  BackgroundRenderer.clearFog();
                  this.starsBuffer.bind();
                  this.starsBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix, GameRenderer.getPositionProgram());
                  VertexBuffer.unbind();
                  runnable.run();
               }

               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               RenderSystem.disableBlend();
               RenderSystem.defaultBlendFunc();
               matrices.pop();
               RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
               double d = this.client.player.getCameraPosVec(tickDelta).y - this.world.getLevelProperties().getSkyDarknessHeight(this.world);
               if (d < 0.0) {
                  matrices.push();
                  matrices.translate(0.0F, 12.0F, 0.0F);
                  this.darkSkyBuffer.bind();
                  this.darkSkyBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix, lv4);
                  VertexBuffer.unbind();
                  matrices.pop();
               }

               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               RenderSystem.depthMask(true);
            }
         }
      }
   }

   private boolean hasBlindnessOrDarkness(Camera camera) {
      Entity var3 = camera.getFocusedEntity();
      if (!(var3 instanceof LivingEntity lv)) {
         return false;
      } else {
         return lv.hasStatusEffect(StatusEffects.BLINDNESS) || lv.hasStatusEffect(StatusEffects.DARKNESS);
      }
   }

   public void renderClouds(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, double d, double e, double g) {
      float h = this.world.getDimensionEffects().getCloudsHeight();
      if (!Float.isNaN(h)) {
         RenderSystem.disableCull();
         RenderSystem.enableBlend();
         RenderSystem.enableDepthTest();
         RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
         RenderSystem.depthMask(true);
         float i = 12.0F;
         float j = 4.0F;
         double k = 2.0E-4;
         double l = (double)(((float)this.ticks + tickDelta) * 0.03F);
         double m = (d + l) / 12.0;
         double n = (double)(h - (float)e + 0.33F);
         double o = g / 12.0 + 0.33000001311302185;
         m -= (double)(MathHelper.floor(m / 2048.0) * 2048);
         o -= (double)(MathHelper.floor(o / 2048.0) * 2048);
         float p = (float)(m - (double)MathHelper.floor(m));
         float q = (float)(n / 4.0 - (double)MathHelper.floor(n / 4.0)) * 4.0F;
         float r = (float)(o - (double)MathHelper.floor(o));
         Vec3d lv = this.world.getCloudsColor(tickDelta);
         int s = (int)Math.floor(m);
         int t = (int)Math.floor(n / 4.0);
         int u = (int)Math.floor(o);
         if (s != this.lastCloudsBlockX || t != this.lastCloudsBlockY || u != this.lastCloudsBlockZ || this.client.options.getCloudRenderModeValue() != this.lastCloudRenderMode || this.lastCloudsColor.squaredDistanceTo(lv) > 2.0E-4) {
            this.lastCloudsBlockX = s;
            this.lastCloudsBlockY = t;
            this.lastCloudsBlockZ = u;
            this.lastCloudsColor = lv;
            this.lastCloudRenderMode = this.client.options.getCloudRenderModeValue();
            this.cloudsDirty = true;
         }

         if (this.cloudsDirty) {
            this.cloudsDirty = false;
            BufferBuilder lv2 = Tessellator.getInstance().getBuffer();
            if (this.cloudsBuffer != null) {
               this.cloudsBuffer.close();
            }

            this.cloudsBuffer = new VertexBuffer();
            BufferBuilder.BuiltBuffer lv3 = this.renderClouds(lv2, m, n, o, lv);
            this.cloudsBuffer.bind();
            this.cloudsBuffer.upload(lv3);
            VertexBuffer.unbind();
         }

         RenderSystem.setShader(GameRenderer::getPositionTexColorNormalProgram);
         RenderSystem.setShaderTexture(0, CLOUDS);
         BackgroundRenderer.setFogBlack();
         matrices.push();
         matrices.scale(12.0F, 1.0F, 12.0F);
         matrices.translate(-p, q, -r);
         if (this.cloudsBuffer != null) {
            this.cloudsBuffer.bind();
            int v = this.lastCloudRenderMode == CloudRenderMode.FANCY ? 0 : 1;

            for(int w = v; w < 2; ++w) {
               if (w == 0) {
                  RenderSystem.colorMask(false, false, false, false);
               } else {
                  RenderSystem.colorMask(true, true, true, true);
               }

               ShaderProgram lv4 = RenderSystem.getShader();
               this.cloudsBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix, lv4);
            }

            VertexBuffer.unbind();
         }

         matrices.pop();
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         RenderSystem.defaultBlendFunc();
      }
   }

   private BufferBuilder.BuiltBuffer renderClouds(BufferBuilder builder, double x, double y, double z, Vec3d color) {
      float g = 4.0F;
      float h = 0.00390625F;
      int i = true;
      int j = true;
      float k = 9.765625E-4F;
      float l = (float)MathHelper.floor(x) * 0.00390625F;
      float m = (float)MathHelper.floor(z) * 0.00390625F;
      float n = (float)color.x;
      float o = (float)color.y;
      float p = (float)color.z;
      float q = n * 0.9F;
      float r = o * 0.9F;
      float s = p * 0.9F;
      float t = n * 0.7F;
      float u = o * 0.7F;
      float v = p * 0.7F;
      float w = n * 0.8F;
      float x = o * 0.8F;
      float y = p * 0.8F;
      RenderSystem.setShader(GameRenderer::getPositionTexColorNormalProgram);
      builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_NORMAL);
      float z = (float)Math.floor(y / 4.0) * 4.0F;
      if (this.lastCloudRenderMode == CloudRenderMode.FANCY) {
         for(int aa = -3; aa <= 4; ++aa) {
            for(int ab = -3; ab <= 4; ++ab) {
               float ac = (float)(aa * 8);
               float ad = (float)(ab * 8);
               if (z > -5.0F) {
                  builder.vertex((double)(ac + 0.0F), (double)(z + 0.0F), (double)(ad + 8.0F)).texture((ac + 0.0F) * 0.00390625F + l, (ad + 8.0F) * 0.00390625F + m).color(t, u, v, 0.8F).normal(0.0F, -1.0F, 0.0F).next();
                  builder.vertex((double)(ac + 8.0F), (double)(z + 0.0F), (double)(ad + 8.0F)).texture((ac + 8.0F) * 0.00390625F + l, (ad + 8.0F) * 0.00390625F + m).color(t, u, v, 0.8F).normal(0.0F, -1.0F, 0.0F).next();
                  builder.vertex((double)(ac + 8.0F), (double)(z + 0.0F), (double)(ad + 0.0F)).texture((ac + 8.0F) * 0.00390625F + l, (ad + 0.0F) * 0.00390625F + m).color(t, u, v, 0.8F).normal(0.0F, -1.0F, 0.0F).next();
                  builder.vertex((double)(ac + 0.0F), (double)(z + 0.0F), (double)(ad + 0.0F)).texture((ac + 0.0F) * 0.00390625F + l, (ad + 0.0F) * 0.00390625F + m).color(t, u, v, 0.8F).normal(0.0F, -1.0F, 0.0F).next();
               }

               if (z <= 5.0F) {
                  builder.vertex((double)(ac + 0.0F), (double)(z + 4.0F - 9.765625E-4F), (double)(ad + 8.0F)).texture((ac + 0.0F) * 0.00390625F + l, (ad + 8.0F) * 0.00390625F + m).color(n, o, p, 0.8F).normal(0.0F, 1.0F, 0.0F).next();
                  builder.vertex((double)(ac + 8.0F), (double)(z + 4.0F - 9.765625E-4F), (double)(ad + 8.0F)).texture((ac + 8.0F) * 0.00390625F + l, (ad + 8.0F) * 0.00390625F + m).color(n, o, p, 0.8F).normal(0.0F, 1.0F, 0.0F).next();
                  builder.vertex((double)(ac + 8.0F), (double)(z + 4.0F - 9.765625E-4F), (double)(ad + 0.0F)).texture((ac + 8.0F) * 0.00390625F + l, (ad + 0.0F) * 0.00390625F + m).color(n, o, p, 0.8F).normal(0.0F, 1.0F, 0.0F).next();
                  builder.vertex((double)(ac + 0.0F), (double)(z + 4.0F - 9.765625E-4F), (double)(ad + 0.0F)).texture((ac + 0.0F) * 0.00390625F + l, (ad + 0.0F) * 0.00390625F + m).color(n, o, p, 0.8F).normal(0.0F, 1.0F, 0.0F).next();
               }

               int ae;
               if (aa > -1) {
                  for(ae = 0; ae < 8; ++ae) {
                     builder.vertex((double)(ac + (float)ae + 0.0F), (double)(z + 0.0F), (double)(ad + 8.0F)).texture((ac + (float)ae + 0.5F) * 0.00390625F + l, (ad + 8.0F) * 0.00390625F + m).color(q, r, s, 0.8F).normal(-1.0F, 0.0F, 0.0F).next();
                     builder.vertex((double)(ac + (float)ae + 0.0F), (double)(z + 4.0F), (double)(ad + 8.0F)).texture((ac + (float)ae + 0.5F) * 0.00390625F + l, (ad + 8.0F) * 0.00390625F + m).color(q, r, s, 0.8F).normal(-1.0F, 0.0F, 0.0F).next();
                     builder.vertex((double)(ac + (float)ae + 0.0F), (double)(z + 4.0F), (double)(ad + 0.0F)).texture((ac + (float)ae + 0.5F) * 0.00390625F + l, (ad + 0.0F) * 0.00390625F + m).color(q, r, s, 0.8F).normal(-1.0F, 0.0F, 0.0F).next();
                     builder.vertex((double)(ac + (float)ae + 0.0F), (double)(z + 0.0F), (double)(ad + 0.0F)).texture((ac + (float)ae + 0.5F) * 0.00390625F + l, (ad + 0.0F) * 0.00390625F + m).color(q, r, s, 0.8F).normal(-1.0F, 0.0F, 0.0F).next();
                  }
               }

               if (aa <= 1) {
                  for(ae = 0; ae < 8; ++ae) {
                     builder.vertex((double)(ac + (float)ae + 1.0F - 9.765625E-4F), (double)(z + 0.0F), (double)(ad + 8.0F)).texture((ac + (float)ae + 0.5F) * 0.00390625F + l, (ad + 8.0F) * 0.00390625F + m).color(q, r, s, 0.8F).normal(1.0F, 0.0F, 0.0F).next();
                     builder.vertex((double)(ac + (float)ae + 1.0F - 9.765625E-4F), (double)(z + 4.0F), (double)(ad + 8.0F)).texture((ac + (float)ae + 0.5F) * 0.00390625F + l, (ad + 8.0F) * 0.00390625F + m).color(q, r, s, 0.8F).normal(1.0F, 0.0F, 0.0F).next();
                     builder.vertex((double)(ac + (float)ae + 1.0F - 9.765625E-4F), (double)(z + 4.0F), (double)(ad + 0.0F)).texture((ac + (float)ae + 0.5F) * 0.00390625F + l, (ad + 0.0F) * 0.00390625F + m).color(q, r, s, 0.8F).normal(1.0F, 0.0F, 0.0F).next();
                     builder.vertex((double)(ac + (float)ae + 1.0F - 9.765625E-4F), (double)(z + 0.0F), (double)(ad + 0.0F)).texture((ac + (float)ae + 0.5F) * 0.00390625F + l, (ad + 0.0F) * 0.00390625F + m).color(q, r, s, 0.8F).normal(1.0F, 0.0F, 0.0F).next();
                  }
               }

               if (ab > -1) {
                  for(ae = 0; ae < 8; ++ae) {
                     builder.vertex((double)(ac + 0.0F), (double)(z + 4.0F), (double)(ad + (float)ae + 0.0F)).texture((ac + 0.0F) * 0.00390625F + l, (ad + (float)ae + 0.5F) * 0.00390625F + m).color(w, x, y, 0.8F).normal(0.0F, 0.0F, -1.0F).next();
                     builder.vertex((double)(ac + 8.0F), (double)(z + 4.0F), (double)(ad + (float)ae + 0.0F)).texture((ac + 8.0F) * 0.00390625F + l, (ad + (float)ae + 0.5F) * 0.00390625F + m).color(w, x, y, 0.8F).normal(0.0F, 0.0F, -1.0F).next();
                     builder.vertex((double)(ac + 8.0F), (double)(z + 0.0F), (double)(ad + (float)ae + 0.0F)).texture((ac + 8.0F) * 0.00390625F + l, (ad + (float)ae + 0.5F) * 0.00390625F + m).color(w, x, y, 0.8F).normal(0.0F, 0.0F, -1.0F).next();
                     builder.vertex((double)(ac + 0.0F), (double)(z + 0.0F), (double)(ad + (float)ae + 0.0F)).texture((ac + 0.0F) * 0.00390625F + l, (ad + (float)ae + 0.5F) * 0.00390625F + m).color(w, x, y, 0.8F).normal(0.0F, 0.0F, -1.0F).next();
                  }
               }

               if (ab <= 1) {
                  for(ae = 0; ae < 8; ++ae) {
                     builder.vertex((double)(ac + 0.0F), (double)(z + 4.0F), (double)(ad + (float)ae + 1.0F - 9.765625E-4F)).texture((ac + 0.0F) * 0.00390625F + l, (ad + (float)ae + 0.5F) * 0.00390625F + m).color(w, x, y, 0.8F).normal(0.0F, 0.0F, 1.0F).next();
                     builder.vertex((double)(ac + 8.0F), (double)(z + 4.0F), (double)(ad + (float)ae + 1.0F - 9.765625E-4F)).texture((ac + 8.0F) * 0.00390625F + l, (ad + (float)ae + 0.5F) * 0.00390625F + m).color(w, x, y, 0.8F).normal(0.0F, 0.0F, 1.0F).next();
                     builder.vertex((double)(ac + 8.0F), (double)(z + 0.0F), (double)(ad + (float)ae + 1.0F - 9.765625E-4F)).texture((ac + 8.0F) * 0.00390625F + l, (ad + (float)ae + 0.5F) * 0.00390625F + m).color(w, x, y, 0.8F).normal(0.0F, 0.0F, 1.0F).next();
                     builder.vertex((double)(ac + 0.0F), (double)(z + 0.0F), (double)(ad + (float)ae + 1.0F - 9.765625E-4F)).texture((ac + 0.0F) * 0.00390625F + l, (ad + (float)ae + 0.5F) * 0.00390625F + m).color(w, x, y, 0.8F).normal(0.0F, 0.0F, 1.0F).next();
                  }
               }
            }
         }
      } else {
         int aa = true;
         int ab = true;

         for(int af = -32; af < 32; af += 32) {
            for(int ag = -32; ag < 32; ag += 32) {
               builder.vertex((double)(af + 0), (double)z, (double)(ag + 32)).texture((float)(af + 0) * 0.00390625F + l, (float)(ag + 32) * 0.00390625F + m).color(n, o, p, 0.8F).normal(0.0F, -1.0F, 0.0F).next();
               builder.vertex((double)(af + 32), (double)z, (double)(ag + 32)).texture((float)(af + 32) * 0.00390625F + l, (float)(ag + 32) * 0.00390625F + m).color(n, o, p, 0.8F).normal(0.0F, -1.0F, 0.0F).next();
               builder.vertex((double)(af + 32), (double)z, (double)(ag + 0)).texture((float)(af + 32) * 0.00390625F + l, (float)(ag + 0) * 0.00390625F + m).color(n, o, p, 0.8F).normal(0.0F, -1.0F, 0.0F).next();
               builder.vertex((double)(af + 0), (double)z, (double)(ag + 0)).texture((float)(af + 0) * 0.00390625F + l, (float)(ag + 0) * 0.00390625F + m).color(n, o, p, 0.8F).normal(0.0F, -1.0F, 0.0F).next();
            }
         }
      }

      return builder.end();
   }

   private void updateChunks(Camera camera) {
      this.client.getProfiler().push("populate_chunks_to_compile");
      ChunkRendererRegionBuilder lv = new ChunkRendererRegionBuilder();
      BlockPos lv2 = camera.getBlockPos();
      List list = Lists.newArrayList();
      ObjectListIterator var5 = this.chunkInfos.iterator();

      while(true) {
         ChunkBuilder.BuiltChunk lv4;
         ChunkPos lv5;
         do {
            do {
               if (!var5.hasNext()) {
                  this.client.getProfiler().swap("upload");
                  this.chunkBuilder.upload();
                  this.client.getProfiler().swap("schedule_async_compile");
                  Iterator var11 = list.iterator();

                  while(var11.hasNext()) {
                     ChunkBuilder.BuiltChunk lv7 = (ChunkBuilder.BuiltChunk)var11.next();
                     lv7.scheduleRebuild(this.chunkBuilder, lv);
                     lv7.cancelRebuild();
                  }

                  this.client.getProfiler().pop();
                  return;
               }

               ChunkInfo lv3 = (ChunkInfo)var5.next();
               lv4 = lv3.chunk;
               lv5 = new ChunkPos(lv4.getOrigin());
            } while(!lv4.needsRebuild());
         } while(!this.world.getChunk(lv5.x, lv5.z).shouldRenderOnUpdate());

         boolean bl = false;
         if (this.client.options.getChunkBuilderMode().getValue() != ChunkBuilderMode.NEARBY) {
            if (this.client.options.getChunkBuilderMode().getValue() == ChunkBuilderMode.PLAYER_AFFECTED) {
               bl = lv4.needsImportantRebuild();
            }
         } else {
            BlockPos lv6 = lv4.getOrigin().add(8, 8, 8);
            bl = lv6.getSquaredDistance(lv2) < 768.0 || lv4.needsImportantRebuild();
         }

         if (bl) {
            this.client.getProfiler().push("build_near_sync");
            this.chunkBuilder.rebuild(lv4, lv);
            lv4.cancelRebuild();
            this.client.getProfiler().pop();
         } else {
            list.add(lv4);
         }
      }
   }

   private void renderWorldBorder(Camera camera) {
      BufferBuilder lv = Tessellator.getInstance().getBuffer();
      WorldBorder lv2 = this.world.getWorldBorder();
      double d = (double)(this.client.options.getClampedViewDistance() * 16);
      if (!(camera.getPos().x < lv2.getBoundEast() - d) || !(camera.getPos().x > lv2.getBoundWest() + d) || !(camera.getPos().z < lv2.getBoundSouth() - d) || !(camera.getPos().z > lv2.getBoundNorth() + d)) {
         double e = 1.0 - lv2.getDistanceInsideBorder(camera.getPos().x, camera.getPos().z) / d;
         e = Math.pow(e, 4.0);
         e = MathHelper.clamp(e, 0.0, 1.0);
         double f = camera.getPos().x;
         double g = camera.getPos().z;
         double h = (double)this.client.gameRenderer.method_32796();
         RenderSystem.enableBlend();
         RenderSystem.enableDepthTest();
         RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
         RenderSystem.setShaderTexture(0, FORCEFIELD);
         RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
         MatrixStack lv3 = RenderSystem.getModelViewStack();
         lv3.push();
         RenderSystem.applyModelViewMatrix();
         int i = lv2.getStage().getColor();
         float j = (float)(i >> 16 & 255) / 255.0F;
         float k = (float)(i >> 8 & 255) / 255.0F;
         float l = (float)(i & 255) / 255.0F;
         RenderSystem.setShaderColor(j, k, l, (float)e);
         RenderSystem.setShader(GameRenderer::getPositionTexProgram);
         RenderSystem.polygonOffset(-3.0F, -3.0F);
         RenderSystem.enablePolygonOffset();
         RenderSystem.disableCull();
         float m = (float)(Util.getMeasuringTimeMs() % 3000L) / 3000.0F;
         float n = (float)(-MathHelper.fractionalPart(camera.getPos().y * 0.5));
         float o = n + (float)h;
         lv.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
         double p = Math.max((double)MathHelper.floor(g - d), lv2.getBoundNorth());
         double q = Math.min((double)MathHelper.ceil(g + d), lv2.getBoundSouth());
         float r = (float)(MathHelper.floor(p) & 1) * 0.5F;
         float v;
         float s;
         double t;
         double u;
         if (f > lv2.getBoundEast() - d) {
            s = r;

            for(t = p; t < q; s += 0.5F) {
               u = Math.min(1.0, q - t);
               v = (float)u * 0.5F;
               lv.vertex(lv2.getBoundEast() - f, -h, t - g).texture(m - s, m + o).next();
               lv.vertex(lv2.getBoundEast() - f, -h, t + u - g).texture(m - (v + s), m + o).next();
               lv.vertex(lv2.getBoundEast() - f, h, t + u - g).texture(m - (v + s), m + n).next();
               lv.vertex(lv2.getBoundEast() - f, h, t - g).texture(m - s, m + n).next();
               ++t;
            }
         }

         if (f < lv2.getBoundWest() + d) {
            s = r;

            for(t = p; t < q; s += 0.5F) {
               u = Math.min(1.0, q - t);
               v = (float)u * 0.5F;
               lv.vertex(lv2.getBoundWest() - f, -h, t - g).texture(m + s, m + o).next();
               lv.vertex(lv2.getBoundWest() - f, -h, t + u - g).texture(m + v + s, m + o).next();
               lv.vertex(lv2.getBoundWest() - f, h, t + u - g).texture(m + v + s, m + n).next();
               lv.vertex(lv2.getBoundWest() - f, h, t - g).texture(m + s, m + n).next();
               ++t;
            }
         }

         p = Math.max((double)MathHelper.floor(f - d), lv2.getBoundWest());
         q = Math.min((double)MathHelper.ceil(f + d), lv2.getBoundEast());
         r = (float)(MathHelper.floor(p) & 1) * 0.5F;
         if (g > lv2.getBoundSouth() - d) {
            s = r;

            for(t = p; t < q; s += 0.5F) {
               u = Math.min(1.0, q - t);
               v = (float)u * 0.5F;
               lv.vertex(t - f, -h, lv2.getBoundSouth() - g).texture(m + s, m + o).next();
               lv.vertex(t + u - f, -h, lv2.getBoundSouth() - g).texture(m + v + s, m + o).next();
               lv.vertex(t + u - f, h, lv2.getBoundSouth() - g).texture(m + v + s, m + n).next();
               lv.vertex(t - f, h, lv2.getBoundSouth() - g).texture(m + s, m + n).next();
               ++t;
            }
         }

         if (g < lv2.getBoundNorth() + d) {
            s = r;

            for(t = p; t < q; s += 0.5F) {
               u = Math.min(1.0, q - t);
               v = (float)u * 0.5F;
               lv.vertex(t - f, -h, lv2.getBoundNorth() - g).texture(m - s, m + o).next();
               lv.vertex(t + u - f, -h, lv2.getBoundNorth() - g).texture(m - (v + s), m + o).next();
               lv.vertex(t + u - f, h, lv2.getBoundNorth() - g).texture(m - (v + s), m + n).next();
               lv.vertex(t - f, h, lv2.getBoundNorth() - g).texture(m - s, m + n).next();
               ++t;
            }
         }

         BufferRenderer.drawWithGlobalProgram(lv.end());
         RenderSystem.enableCull();
         RenderSystem.polygonOffset(0.0F, 0.0F);
         RenderSystem.disablePolygonOffset();
         RenderSystem.disableBlend();
         RenderSystem.defaultBlendFunc();
         lv3.pop();
         RenderSystem.applyModelViewMatrix();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.depthMask(true);
      }
   }

   private void drawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos pos, BlockState state) {
      drawCuboidShapeOutline(matrices, vertexConsumer, state.getOutlineShape(this.world, pos, ShapeContext.of(entity)), (double)pos.getX() - cameraX, (double)pos.getY() - cameraY, (double)pos.getZ() - cameraZ, 0.0F, 0.0F, 0.0F, 0.4F);
   }

   public static void drawShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape shape, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float alpha) {
      List list = shape.getBoundingBoxes();
      int k = MathHelper.ceil((double)list.size() / 3.0);

      for(int l = 0; l < list.size(); ++l) {
         Box lv = (Box)list.get(l);
         float m = ((float)l % (float)k + 1.0F) / (float)k;
         float n = (float)(l / k);
         float o = m * (float)(n == 0.0F ? 1 : 0);
         float p = m * (float)(n == 1.0F ? 1 : 0);
         float q = m * (float)(n == 2.0F ? 1 : 0);
         drawCuboidShapeOutline(matrices, vertexConsumer, VoxelShapes.cuboid(lv.offset(0.0, 0.0, 0.0)), offsetX, offsetY, offsetZ, o, p, q, 1.0F);
      }

   }

   private static void drawCuboidShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape shape, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float alpha) {
      MatrixStack.Entry lv = matrices.peek();
      shape.forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> {
         float q = (float)(maxX - minX);
         float r = (float)(maxY - minY);
         float s = (float)(maxZ - minZ);
         float t = MathHelper.sqrt(q * q + r * r + s * s);
         q /= t;
         r /= t;
         s /= t;
         vertexConsumer.vertex(lv.getPositionMatrix(), (float)(minX + offsetX), (float)(minY + offsetY), (float)(minZ + offsetZ)).color(red, green, blue, alpha).normal(lv.getNormalMatrix(), q, r, s).next();
         vertexConsumer.vertex(lv.getPositionMatrix(), (float)(maxX + offsetX), (float)(maxY + offsetY), (float)(maxZ + offsetZ)).color(red, green, blue, alpha).normal(lv.getNormalMatrix(), q, r, s).next();
      });
   }

   public static void drawBox(VertexConsumer vertexConsumer, double x1, double y1, double z1, double x2, double y2, double z2, float red, float green, float blue, float alpha) {
      drawBox(new MatrixStack(), vertexConsumer, x1, y1, z1, x2, y2, z2, red, green, blue, alpha, red, green, blue);
   }

   public static void drawBox(MatrixStack matrices, VertexConsumer vertexConsumer, Box box, float red, float green, float blue, float alpha) {
      drawBox(matrices, vertexConsumer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha, red, green, blue);
   }

   public static void drawBox(MatrixStack matrices, VertexConsumer vertexConsumer, double x1, double y1, double z1, double x2, double y2, double z2, float red, float green, float blue, float alpha) {
      drawBox(matrices, vertexConsumer, x1, y1, z1, x2, y2, z2, red, green, blue, alpha, red, green, blue);
   }

   public static void drawBox(MatrixStack matrices, VertexConsumer vertexConsumer, double x1, double y1, double z1, double x2, double y2, double z2, float red, float green, float blue, float alpha, float xAxisRed, float yAxisGreen, float zAxisBlue) {
      Matrix4f matrix4f = matrices.peek().getPositionMatrix();
      Matrix3f matrix3f = matrices.peek().getNormalMatrix();
      float q = (float)x1;
      float r = (float)y1;
      float s = (float)z1;
      float t = (float)x2;
      float u = (float)y2;
      float v = (float)z2;
      vertexConsumer.vertex(matrix4f, q, r, s).color(red, yAxisGreen, zAxisBlue, alpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).next();
      vertexConsumer.vertex(matrix4f, t, r, s).color(red, yAxisGreen, zAxisBlue, alpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).next();
      vertexConsumer.vertex(matrix4f, q, r, s).color(xAxisRed, green, zAxisBlue, alpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).next();
      vertexConsumer.vertex(matrix4f, q, u, s).color(xAxisRed, green, zAxisBlue, alpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).next();
      vertexConsumer.vertex(matrix4f, q, r, s).color(xAxisRed, yAxisGreen, blue, alpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).next();
      vertexConsumer.vertex(matrix4f, q, r, v).color(xAxisRed, yAxisGreen, blue, alpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).next();
      vertexConsumer.vertex(matrix4f, t, r, s).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).next();
      vertexConsumer.vertex(matrix4f, t, u, s).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).next();
      vertexConsumer.vertex(matrix4f, t, u, s).color(red, green, blue, alpha).normal(matrix3f, -1.0F, 0.0F, 0.0F).next();
      vertexConsumer.vertex(matrix4f, q, u, s).color(red, green, blue, alpha).normal(matrix3f, -1.0F, 0.0F, 0.0F).next();
      vertexConsumer.vertex(matrix4f, q, u, s).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).next();
      vertexConsumer.vertex(matrix4f, q, u, v).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).next();
      vertexConsumer.vertex(matrix4f, q, u, v).color(red, green, blue, alpha).normal(matrix3f, 0.0F, -1.0F, 0.0F).next();
      vertexConsumer.vertex(matrix4f, q, r, v).color(red, green, blue, alpha).normal(matrix3f, 0.0F, -1.0F, 0.0F).next();
      vertexConsumer.vertex(matrix4f, q, r, v).color(red, green, blue, alpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).next();
      vertexConsumer.vertex(matrix4f, t, r, v).color(red, green, blue, alpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).next();
      vertexConsumer.vertex(matrix4f, t, r, v).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 0.0F, -1.0F).next();
      vertexConsumer.vertex(matrix4f, t, r, s).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 0.0F, -1.0F).next();
      vertexConsumer.vertex(matrix4f, q, u, v).color(red, green, blue, alpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).next();
      vertexConsumer.vertex(matrix4f, t, u, v).color(red, green, blue, alpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).next();
      vertexConsumer.vertex(matrix4f, t, r, v).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).next();
      vertexConsumer.vertex(matrix4f, t, u, v).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).next();
      vertexConsumer.vertex(matrix4f, t, u, s).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).next();
      vertexConsumer.vertex(matrix4f, t, u, v).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).next();
   }

   public static void method_3258(MatrixStack arg, VertexConsumer arg2, double d, double e, double f, double g, double h, double i, float j, float k, float l, float m) {
      method_49041(arg, arg2, (float)d, (float)e, (float)f, (float)g, (float)h, (float)i, j, k, l, m);
   }

   public static void method_49041(MatrixStack arg, VertexConsumer arg2, float f, float g, float h, float i, float j, float k, float l, float m, float n, float o) {
      Matrix4f matrix4f = arg.peek().getPositionMatrix();
      arg2.vertex(matrix4f, f, g, h).color(l, m, n, o).next();
      arg2.vertex(matrix4f, f, g, h).color(l, m, n, o).next();
      arg2.vertex(matrix4f, f, g, h).color(l, m, n, o).next();
      arg2.vertex(matrix4f, f, g, k).color(l, m, n, o).next();
      arg2.vertex(matrix4f, f, j, h).color(l, m, n, o).next();
      arg2.vertex(matrix4f, f, j, k).color(l, m, n, o).next();
      arg2.vertex(matrix4f, f, j, k).color(l, m, n, o).next();
      arg2.vertex(matrix4f, f, g, k).color(l, m, n, o).next();
      arg2.vertex(matrix4f, i, j, k).color(l, m, n, o).next();
      arg2.vertex(matrix4f, i, g, k).color(l, m, n, o).next();
      arg2.vertex(matrix4f, i, g, k).color(l, m, n, o).next();
      arg2.vertex(matrix4f, i, g, h).color(l, m, n, o).next();
      arg2.vertex(matrix4f, i, j, k).color(l, m, n, o).next();
      arg2.vertex(matrix4f, i, j, h).color(l, m, n, o).next();
      arg2.vertex(matrix4f, i, j, h).color(l, m, n, o).next();
      arg2.vertex(matrix4f, i, g, h).color(l, m, n, o).next();
      arg2.vertex(matrix4f, f, j, h).color(l, m, n, o).next();
      arg2.vertex(matrix4f, f, g, h).color(l, m, n, o).next();
      arg2.vertex(matrix4f, f, g, h).color(l, m, n, o).next();
      arg2.vertex(matrix4f, i, g, h).color(l, m, n, o).next();
      arg2.vertex(matrix4f, f, g, k).color(l, m, n, o).next();
      arg2.vertex(matrix4f, i, g, k).color(l, m, n, o).next();
      arg2.vertex(matrix4f, i, g, k).color(l, m, n, o).next();
      arg2.vertex(matrix4f, f, j, h).color(l, m, n, o).next();
      arg2.vertex(matrix4f, f, j, h).color(l, m, n, o).next();
      arg2.vertex(matrix4f, f, j, k).color(l, m, n, o).next();
      arg2.vertex(matrix4f, i, j, h).color(l, m, n, o).next();
      arg2.vertex(matrix4f, i, j, k).color(l, m, n, o).next();
      arg2.vertex(matrix4f, i, j, k).color(l, m, n, o).next();
      arg2.vertex(matrix4f, i, j, k).color(l, m, n, o).next();
   }

   public void updateBlock(BlockView world, BlockPos pos, BlockState oldState, BlockState newState, int flags) {
      this.scheduleSectionRender(pos, (flags & 8) != 0);
   }

   private void scheduleSectionRender(BlockPos pos, boolean important) {
      for(int i = pos.getZ() - 1; i <= pos.getZ() + 1; ++i) {
         for(int j = pos.getX() - 1; j <= pos.getX() + 1; ++j) {
            for(int k = pos.getY() - 1; k <= pos.getY() + 1; ++k) {
               this.scheduleChunkRender(ChunkSectionPos.getSectionCoord(j), ChunkSectionPos.getSectionCoord(k), ChunkSectionPos.getSectionCoord(i), important);
            }
         }
      }

   }

   public void scheduleBlockRenders(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      for(int o = minZ - 1; o <= maxZ + 1; ++o) {
         for(int p = minX - 1; p <= maxX + 1; ++p) {
            for(int q = minY - 1; q <= maxY + 1; ++q) {
               this.scheduleBlockRender(ChunkSectionPos.getSectionCoord(p), ChunkSectionPos.getSectionCoord(q), ChunkSectionPos.getSectionCoord(o));
            }
         }
      }

   }

   public void scheduleBlockRerenderIfNeeded(BlockPos pos, BlockState old, BlockState updated) {
      if (this.client.getBakedModelManager().shouldRerender(old, updated)) {
         this.scheduleBlockRenders(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
      }

   }

   public void scheduleBlockRenders(int x, int y, int z) {
      for(int l = z - 1; l <= z + 1; ++l) {
         for(int m = x - 1; m <= x + 1; ++m) {
            for(int n = y - 1; n <= y + 1; ++n) {
               this.scheduleBlockRender(m, n, l);
            }
         }
      }

   }

   public void scheduleBlockRender(int x, int y, int z) {
      this.scheduleChunkRender(x, y, z, false);
   }

   private void scheduleChunkRender(int x, int y, int z, boolean important) {
      this.chunks.scheduleRebuild(x, y, z, important);
   }

   public void playSong(@Nullable SoundEvent song, BlockPos songPosition) {
      SoundInstance lv = (SoundInstance)this.playingSongs.get(songPosition);
      if (lv != null) {
         this.client.getSoundManager().stop(lv);
         this.playingSongs.remove(songPosition);
      }

      if (song != null) {
         MusicDiscItem lv2 = MusicDiscItem.bySound(song);
         if (lv2 != null) {
            this.client.inGameHud.setRecordPlayingOverlay(lv2.getDescription());
         }

         SoundInstance lv = PositionedSoundInstance.record(song, Vec3d.ofCenter(songPosition));
         this.playingSongs.put(songPosition, lv);
         this.client.getSoundManager().play(lv);
      }

      this.updateEntitiesForSong(this.world, songPosition, song != null);
   }

   private void updateEntitiesForSong(World world, BlockPos pos, boolean playing) {
      List list = world.getNonSpectatingEntities(LivingEntity.class, (new Box(pos)).expand(3.0));
      Iterator var5 = list.iterator();

      while(var5.hasNext()) {
         LivingEntity lv = (LivingEntity)var5.next();
         lv.setNearbySongPlaying(pos, playing);
      }

   }

   public void addParticle(ParticleEffect parameters, boolean shouldAlwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      this.addParticle(parameters, shouldAlwaysSpawn, false, x, y, z, velocityX, velocityY, velocityZ);
   }

   public void addParticle(ParticleEffect parameters, boolean shouldAlwaysSpawn, boolean important, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      try {
         this.spawnParticle(parameters, shouldAlwaysSpawn, important, x, y, z, velocityX, velocityY, velocityZ);
      } catch (Throwable var19) {
         CrashReport lv = CrashReport.create(var19, "Exception while adding particle");
         CrashReportSection lv2 = lv.addElement("Particle being added");
         lv2.add("ID", (Object)Registries.PARTICLE_TYPE.getId(parameters.getType()));
         lv2.add("Parameters", (Object)parameters.asString());
         lv2.add("Position", () -> {
            return CrashReportSection.createPositionString(this.world, x, y, z);
         });
         throw new CrashException(lv);
      }
   }

   private void addParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      this.addParticle(parameters, parameters.getType().shouldAlwaysSpawn(), x, y, z, velocityX, velocityY, velocityZ);
   }

   @Nullable
   private Particle spawnParticle(ParticleEffect parameters, boolean alwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      return this.spawnParticle(parameters, alwaysSpawn, false, x, y, z, velocityX, velocityY, velocityZ);
   }

   @Nullable
   private Particle spawnParticle(ParticleEffect parameters, boolean alwaysSpawn, boolean canSpawnOnMinimal, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      Camera lv = this.client.gameRenderer.getCamera();
      if (this.client != null && lv.isReady() && this.client.particleManager != null) {
         ParticlesMode lv2 = this.getRandomParticleSpawnChance(canSpawnOnMinimal);
         if (alwaysSpawn) {
            return this.client.particleManager.addParticle(parameters, x, y, z, velocityX, velocityY, velocityZ);
         } else if (lv.getPos().squaredDistanceTo(x, y, z) > 1024.0) {
            return null;
         } else {
            return lv2 == ParticlesMode.MINIMAL ? null : this.client.particleManager.addParticle(parameters, x, y, z, velocityX, velocityY, velocityZ);
         }
      } else {
         return null;
      }
   }

   private ParticlesMode getRandomParticleSpawnChance(boolean canSpawnOnMinimal) {
      ParticlesMode lv = (ParticlesMode)this.client.options.getParticles().getValue();
      if (canSpawnOnMinimal && lv == ParticlesMode.MINIMAL && this.world.random.nextInt(10) == 0) {
         lv = ParticlesMode.DECREASED;
      }

      if (lv == ParticlesMode.DECREASED && this.world.random.nextInt(3) == 0) {
         lv = ParticlesMode.MINIMAL;
      }

      return lv;
   }

   public void cleanUp() {
   }

   public void processGlobalEvent(int eventId, BlockPos pos, int data) {
      switch (eventId) {
         case 1023:
         case 1028:
         case 1038:
            Camera lv = this.client.gameRenderer.getCamera();
            if (lv.isReady()) {
               double d = (double)pos.getX() - lv.getPos().x;
               double e = (double)pos.getY() - lv.getPos().y;
               double f = (double)pos.getZ() - lv.getPos().z;
               double g = Math.sqrt(d * d + e * e + f * f);
               double h = lv.getPos().x;
               double k = lv.getPos().y;
               double l = lv.getPos().z;
               if (g > 0.0) {
                  h += d / g * 2.0;
                  k += e / g * 2.0;
                  l += f / g * 2.0;
               }

               if (eventId == WorldEvents.WITHER_SPAWNS) {
                  this.world.playSound(h, k, l, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.0F, 1.0F, false);
               } else if (eventId == WorldEvents.END_PORTAL_OPENED) {
                  this.world.playSound(h, k, l, SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.HOSTILE, 1.0F, 1.0F, false);
               } else {
                  this.world.playSound(h, k, l, SoundEvents.ENTITY_ENDER_DRAGON_DEATH, SoundCategory.HOSTILE, 5.0F, 1.0F, false);
               }
            }
         default:
      }
   }

   public void processWorldEvent(int eventId, BlockPos pos, int data) {
      Random lv = this.world.random;
      double ab;
      int k;
      int l;
      int m;
      float y;
      double d;
      int z;
      float am;
      double e;
      double f;
      double aa;
      switch (eventId) {
         case 1000:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 1001:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0F, 1.2F, false);
            break;
         case 1002:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_DISPENSER_LAUNCH, SoundCategory.BLOCKS, 1.0F, 1.2F, false);
            break;
         case 1003:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.NEUTRAL, 1.0F, 1.2F, false);
            break;
         case 1004:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_FIREWORK_ROCKET_SHOOT, SoundCategory.NEUTRAL, 1.0F, 1.2F, false);
            break;
         case 1009:
            if (data == 0) {
               this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (lv.nextFloat() - lv.nextFloat()) * 0.8F, false);
            } else if (data == 1) {
               this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 0.7F, 1.6F + (lv.nextFloat() - lv.nextFloat()) * 0.4F, false);
            }
            break;
         case 1010:
            Item var62 = Item.byRawId(data);
            if (var62 instanceof MusicDiscItem lv15) {
               this.playSong(lv15.getSound(), pos);
            }
            break;
         case 1011:
            this.playSong((SoundEvent)null, pos);
            break;
         case 1015:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_GHAST_WARN, SoundCategory.HOSTILE, 10.0F, (lv.nextFloat() - lv.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1016:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_GHAST_SHOOT, SoundCategory.HOSTILE, 10.0F, (lv.nextFloat() - lv.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1017:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, SoundCategory.HOSTILE, 10.0F, (lv.nextFloat() - lv.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1018:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 2.0F, (lv.nextFloat() - lv.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1019:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.HOSTILE, 2.0F, (lv.nextFloat() - lv.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1020:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.HOSTILE, 2.0F, (lv.nextFloat() - lv.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1021:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, SoundCategory.HOSTILE, 2.0F, (lv.nextFloat() - lv.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1022:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_WITHER_BREAK_BLOCK, SoundCategory.HOSTILE, 2.0F, (lv.nextFloat() - lv.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1024:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.HOSTILE, 2.0F, (lv.nextFloat() - lv.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1025:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_BAT_TAKEOFF, SoundCategory.NEUTRAL, 0.05F, (lv.nextFloat() - lv.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1026:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_ZOMBIE_INFECT, SoundCategory.HOSTILE, 2.0F, (lv.nextFloat() - lv.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1027:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, SoundCategory.HOSTILE, 2.0F, (lv.nextFloat() - lv.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1029:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_ANVIL_DESTROY, SoundCategory.BLOCKS, 1.0F, lv.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1030:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0F, lv.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1031:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 0.3F, this.world.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1032:
            this.client.getSoundManager().play(PositionedSoundInstance.ambient(SoundEvents.BLOCK_PORTAL_TRAVEL, lv.nextFloat() * 0.4F + 0.8F, 0.25F));
            break;
         case 1033:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_CHORUS_FLOWER_GROW, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 1034:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_CHORUS_FLOWER_DEATH, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 1035:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 1039:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_PHANTOM_BITE, SoundCategory.HOSTILE, 0.3F, this.world.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1040:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_ZOMBIE_CONVERTED_TO_DROWNED, SoundCategory.HOSTILE, 2.0F, (lv.nextFloat() - lv.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1041:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_HUSK_CONVERTED_TO_ZOMBIE, SoundCategory.HOSTILE, 2.0F, (lv.nextFloat() - lv.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1042:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 1.0F, this.world.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1043:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1.0F, this.world.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1044:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_SMITHING_TABLE_USE, SoundCategory.BLOCKS, 1.0F, this.world.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1045:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_POINTED_DRIPSTONE_LAND, SoundCategory.BLOCKS, 2.0F, this.world.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1046:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON, SoundCategory.BLOCKS, 2.0F, this.world.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1047:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundCategory.BLOCKS, 2.0F, this.world.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1048:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_SKELETON_CONVERTED_TO_STRAY, SoundCategory.HOSTILE, 2.0F, (lv.nextFloat() - lv.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1500:
            ComposterBlock.playEffects(this.world, pos, data > 0);
            break;
         case 1501:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (lv.nextFloat() - lv.nextFloat()) * 0.8F, false);

            for(z = 0; z < 8; ++z) {
               this.world.addParticle(ParticleTypes.LARGE_SMOKE, (double)pos.getX() + lv.nextDouble(), (double)pos.getY() + 1.2, (double)pos.getZ() + lv.nextDouble(), 0.0, 0.0, 0.0);
            }

            return;
         case 1502:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.5F, 2.6F + (lv.nextFloat() - lv.nextFloat()) * 0.8F, false);

            for(z = 0; z < 5; ++z) {
               e = (double)pos.getX() + lv.nextDouble() * 0.6 + 0.2;
               f = (double)pos.getY() + lv.nextDouble() * 0.6 + 0.2;
               aa = (double)pos.getZ() + lv.nextDouble() * 0.6 + 0.2;
               this.world.addParticle(ParticleTypes.SMOKE, e, f, aa, 0.0, 0.0, 0.0);
            }

            return;
         case 1503:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F, false);

            for(z = 0; z < 16; ++z) {
               e = (double)pos.getX() + (5.0 + lv.nextDouble() * 6.0) / 16.0;
               f = (double)pos.getY() + 0.8125;
               aa = (double)pos.getZ() + (5.0 + lv.nextDouble() * 6.0) / 16.0;
               this.world.addParticle(ParticleTypes.SMOKE, e, f, aa, 0.0, 0.0, 0.0);
            }

            return;
         case 1504:
            PointedDripstoneBlock.createParticle(this.world, pos, this.world.getBlockState(pos));
            break;
         case 1505:
            BoneMealItem.createParticles(this.world, pos, data);
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 2000:
            Direction lv2 = Direction.byId(data);
            k = lv2.getOffsetX();
            l = lv2.getOffsetY();
            m = lv2.getOffsetZ();
            d = (double)pos.getX() + (double)k * 0.6 + 0.5;
            e = (double)pos.getY() + (double)l * 0.6 + 0.5;
            f = (double)pos.getZ() + (double)m * 0.6 + 0.5;

            for(int n = 0; n < 10; ++n) {
               double g = lv.nextDouble() * 0.2 + 0.01;
               double h = d + (double)k * 0.01 + (lv.nextDouble() - 0.5) * (double)m * 0.5;
               double o = e + (double)l * 0.01 + (lv.nextDouble() - 0.5) * (double)l * 0.5;
               double p = f + (double)m * 0.01 + (lv.nextDouble() - 0.5) * (double)k * 0.5;
               double q = (double)k * g + lv.nextGaussian() * 0.01;
               double r = (double)l * g + lv.nextGaussian() * 0.01;
               double s = (double)m * g + lv.nextGaussian() * 0.01;
               this.addParticle(ParticleTypes.SMOKE, h, o, p, q, r, s);
            }

            return;
         case 2001:
            BlockState lv6 = Block.getStateFromRawId(data);
            if (!lv6.isAir()) {
               BlockSoundGroup lv7 = lv6.getSoundGroup();
               this.world.playSoundAtBlockCenter(pos, lv7.getBreakSound(), SoundCategory.BLOCKS, (lv7.getVolume() + 1.0F) / 2.0F, lv7.getPitch() * 0.8F, false);
            }

            this.world.addBlockBreakParticles(pos, lv6);
            break;
         case 2002:
         case 2007:
            Vec3d lv3 = Vec3d.ofBottomCenter(pos);

            for(k = 0; k < 8; ++k) {
               this.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.SPLASH_POTION)), lv3.x, lv3.y, lv3.z, lv.nextGaussian() * 0.15, lv.nextDouble() * 0.2, lv.nextGaussian() * 0.15);
            }

            float w = (float)(data >> 16 & 255) / 255.0F;
            float x = (float)(data >> 8 & 255) / 255.0F;
            y = (float)(data >> 0 & 255) / 255.0F;
            ParticleEffect lv4 = eventId == 2007 ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.EFFECT;

            for(z = 0; z < 100; ++z) {
               e = lv.nextDouble() * 4.0;
               f = lv.nextDouble() * Math.PI * 2.0;
               aa = Math.cos(f) * e;
               ab = 0.01 + lv.nextDouble() * 0.5;
               double ac = Math.sin(f) * e;
               Particle lv5 = this.spawnParticle(lv4, lv4.getType().shouldAlwaysSpawn(), lv3.x + aa * 0.1, lv3.y + 0.3, lv3.z + ac * 0.1, aa, ab, ac);
               if (lv5 != null) {
                  float ad = 0.75F + lv.nextFloat() * 0.25F;
                  lv5.setColor(w * ad, x * ad, y * ad);
                  lv5.move((float)e);
               }
            }

            this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_SPLASH_POTION_BREAK, SoundCategory.NEUTRAL, 1.0F, lv.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 2003:
            double t = (double)pos.getX() + 0.5;
            double u = (double)pos.getY();
            d = (double)pos.getZ() + 0.5;

            for(int v = 0; v < 8; ++v) {
               this.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.ENDER_EYE)), t, u, d, lv.nextGaussian() * 0.15, lv.nextDouble() * 0.2, lv.nextGaussian() * 0.15);
            }

            for(e = 0.0; e < 6.283185307179586; e += 0.15707963267948966) {
               this.addParticle(ParticleTypes.PORTAL, t + Math.cos(e) * 5.0, u - 0.4, d + Math.sin(e) * 5.0, Math.cos(e) * -5.0, 0.0, Math.sin(e) * -5.0);
               this.addParticle(ParticleTypes.PORTAL, t + Math.cos(e) * 5.0, u - 0.4, d + Math.sin(e) * 5.0, Math.cos(e) * -7.0, 0.0, Math.sin(e) * -7.0);
            }

            return;
         case 2004:
            for(l = 0; l < 20; ++l) {
               double ae = (double)pos.getX() + 0.5 + (lv.nextDouble() - 0.5) * 2.0;
               double af = (double)pos.getY() + 0.5 + (lv.nextDouble() - 0.5) * 2.0;
               double ag = (double)pos.getZ() + 0.5 + (lv.nextDouble() - 0.5) * 2.0;
               this.world.addParticle(ParticleTypes.SMOKE, ae, af, ag, 0.0, 0.0, 0.0);
               this.world.addParticle(ParticleTypes.FLAME, ae, af, ag, 0.0, 0.0, 0.0);
            }

            return;
         case 2005:
            BoneMealItem.createParticles(this.world, pos, data);
            break;
         case 2006:
            for(z = 0; z < 200; ++z) {
               am = lv.nextFloat() * 4.0F;
               float aq = lv.nextFloat() * 6.2831855F;
               f = (double)(MathHelper.cos(aq) * am);
               aa = 0.01 + lv.nextDouble() * 0.5;
               ab = (double)(MathHelper.sin(aq) * am);
               Particle lv14 = this.spawnParticle(ParticleTypes.DRAGON_BREATH, false, (double)pos.getX() + f * 0.1, (double)pos.getY() + 0.3, (double)pos.getZ() + ab * 0.1, f, aa, ab);
               if (lv14 != null) {
                  lv14.move(am);
               }
            }

            if (data == 1) {
               this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.HOSTILE, 1.0F, lv.nextFloat() * 0.1F + 0.9F, false);
            }
            break;
         case 2008:
            this.world.addParticle(ParticleTypes.EXPLOSION, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 0.0, 0.0, 0.0);
            break;
         case 2009:
            for(z = 0; z < 8; ++z) {
               this.world.addParticle(ParticleTypes.CLOUD, (double)pos.getX() + lv.nextDouble(), (double)pos.getY() + 1.2, (double)pos.getZ() + lv.nextDouble(), 0.0, 0.0, 0.0);
            }

            return;
         case 3000:
            this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, true, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 0.0, 0.0, 0.0);
            this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_END_GATEWAY_SPAWN, SoundCategory.BLOCKS, 10.0F, (1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F, false);
            break;
         case 3001:
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 64.0F, 0.8F + this.world.random.nextFloat() * 0.3F, false);
            break;
         case 3002:
            if (data >= 0 && data < Direction.Axis.VALUES.length) {
               ParticleUtil.spawnParticle(Direction.Axis.VALUES[data], this.world, pos, 0.125, ParticleTypes.ELECTRIC_SPARK, UniformIntProvider.create(10, 19));
            } else {
               ParticleUtil.spawnParticle(this.world, pos, (ParticleEffect)ParticleTypes.ELECTRIC_SPARK, (IntProvider)UniformIntProvider.create(3, 5));
            }
            break;
         case 3003:
            ParticleUtil.spawnParticle(this.world, pos, (ParticleEffect)ParticleTypes.WAX_ON, (IntProvider)UniformIntProvider.create(3, 5));
            this.world.playSoundAtBlockCenter(pos, SoundEvents.ITEM_HONEYCOMB_WAX_ON, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 3004:
            ParticleUtil.spawnParticle(this.world, pos, (ParticleEffect)ParticleTypes.WAX_OFF, (IntProvider)UniformIntProvider.create(3, 5));
            break;
         case 3005:
            ParticleUtil.spawnParticle(this.world, pos, (ParticleEffect)ParticleTypes.SCRAPE, (IntProvider)UniformIntProvider.create(3, 5));
            break;
         case 3006:
            l = data >> 6;
            float ai;
            float ak;
            if (l > 0) {
               if (lv.nextFloat() < 0.3F + (float)l * 0.1F) {
                  y = 0.15F + 0.02F * (float)l * (float)l * lv.nextFloat();
                  float ah = 0.4F + 0.3F * (float)l * lv.nextFloat();
                  this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_SCULK_CHARGE, SoundCategory.BLOCKS, y, ah, false);
               }

               byte b = (byte)(data & 63);
               IntProvider lv10 = UniformIntProvider.create(0, l);
               ai = 0.005F;
               Supplier supplier = () -> {
                  return new Vec3d(MathHelper.nextDouble(lv, -0.004999999888241291, 0.004999999888241291), MathHelper.nextDouble(lv, -0.004999999888241291, 0.004999999888241291), MathHelper.nextDouble(lv, -0.004999999888241291, 0.004999999888241291));
               };
               if (b == 0) {
                  Direction[] var12 = Direction.values();
                  int var13 = var12.length;

                  for(int var14 = 0; var14 < var13; ++var14) {
                     Direction lv11 = var12[var14];
                     float aj = lv11 == Direction.DOWN ? 3.1415927F : 0.0F;
                     ab = lv11.getAxis() == Direction.Axis.Y ? 0.65 : 0.57;
                     ParticleUtil.spawnParticles(this.world, pos, new SculkChargeParticleEffect(aj), lv10, lv11, supplier, ab);
                  }

                  return;
               } else {
                  Iterator var54 = MultifaceGrowthBlock.flagToDirections(b).iterator();

                  while(var54.hasNext()) {
                     Direction lv12 = (Direction)var54.next();
                     ak = lv12 == Direction.UP ? 3.1415927F : 0.0F;
                     aa = 0.35;
                     ParticleUtil.spawnParticles(this.world, pos, new SculkChargeParticleEffect(ak), lv10, lv12, supplier, 0.35);
                  }

                  return;
               }
            } else {
               this.world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_SCULK_CHARGE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
               boolean bl = this.world.getBlockState(pos).isFullCube(this.world, pos);
               int al = bl ? 40 : 20;
               ai = bl ? 0.45F : 0.25F;
               am = 0.07F;

               for(int an = 0; an < al; ++an) {
                  float ao = 2.0F * lv.nextFloat() - 1.0F;
                  ak = 2.0F * lv.nextFloat() - 1.0F;
                  float ap = 2.0F * lv.nextFloat() - 1.0F;
                  this.world.addParticle(ParticleTypes.SCULK_CHARGE_POP, (double)pos.getX() + 0.5 + (double)(ao * ai), (double)pos.getY() + 0.5 + (double)(ak * ai), (double)pos.getZ() + 0.5 + (double)(ap * ai), (double)(ao * 0.07F), (double)(ak * 0.07F), (double)(ap * 0.07F));
               }

               return;
            }
         case 3007:
            for(m = 0; m < 10; ++m) {
               this.world.addParticle(new ShriekParticleEffect(m * 5), false, (double)pos.getX() + 0.5, (double)pos.getY() + SculkShriekerBlock.TOP, (double)pos.getZ() + 0.5, 0.0, 0.0, 0.0);
            }

            BlockState lv13 = this.world.getBlockState(pos);
            boolean bl2 = lv13.contains(Properties.WATERLOGGED) && (Boolean)lv13.get(Properties.WATERLOGGED);
            if (!bl2) {
               this.world.playSound((double)pos.getX() + 0.5, (double)pos.getY() + SculkShriekerBlock.TOP, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_SCULK_SHRIEKER_SHRIEK, SoundCategory.BLOCKS, 2.0F, 0.6F + this.world.random.nextFloat() * 0.4F, false);
            }
            break;
         case 3008:
            BlockState lv8 = Block.getStateFromRawId(data);
            Block var8 = lv8.getBlock();
            if (var8 instanceof BrushableBlock lv9) {
               this.world.playSoundAtBlockCenter(pos, lv9.getBrushingCompleteSound(), SoundCategory.PLAYERS, 1.0F, 1.0F, false);
            }

            this.world.addBlockBreakParticles(pos, lv8);
            break;
         case 3009:
            ParticleUtil.spawnParticle(this.world, pos, (ParticleEffect)ParticleTypes.EGG_CRACK, (IntProvider)(data == 1 ? UniformIntProvider.create(3, 6) : UniformIntProvider.create(1, 3)));
      }

   }

   public void setBlockBreakingInfo(int entityId, BlockPos pos, int stage) {
      BlockBreakingInfo lv;
      if (stage >= 0 && stage < 10) {
         lv = (BlockBreakingInfo)this.blockBreakingInfos.get(entityId);
         if (lv != null) {
            this.removeBlockBreakingInfo(lv);
         }

         if (lv == null || lv.getPos().getX() != pos.getX() || lv.getPos().getY() != pos.getY() || lv.getPos().getZ() != pos.getZ()) {
            lv = new BlockBreakingInfo(entityId, pos);
            this.blockBreakingInfos.put(entityId, lv);
         }

         lv.setStage(stage);
         lv.setLastUpdateTick(this.ticks);
         ((SortedSet)this.blockBreakingProgressions.computeIfAbsent(lv.getPos().asLong(), (l) -> {
            return Sets.newTreeSet();
         })).add(lv);
      } else {
         lv = (BlockBreakingInfo)this.blockBreakingInfos.remove(entityId);
         if (lv != null) {
            this.removeBlockBreakingInfo(lv);
         }
      }

   }

   public boolean isTerrainRenderComplete() {
      return this.chunkBuilder.isEmpty();
   }

   public void scheduleTerrainUpdate() {
      this.shouldUpdate = true;
      this.cloudsDirty = true;
   }

   public void updateNoCullingBlockEntities(Collection removed, Collection added) {
      synchronized(this.noCullingBlockEntities) {
         this.noCullingBlockEntities.removeAll(removed);
         this.noCullingBlockEntities.addAll(added);
      }
   }

   public static int getLightmapCoordinates(BlockRenderView world, BlockPos pos) {
      return getLightmapCoordinates(world, world.getBlockState(pos), pos);
   }

   public static int getLightmapCoordinates(BlockRenderView world, BlockState state, BlockPos pos) {
      if (state.hasEmissiveLighting(world, pos)) {
         return 15728880;
      } else {
         int i = world.getLightLevel(LightType.SKY, pos);
         int j = world.getLightLevel(LightType.BLOCK, pos);
         int k = state.getLuminance();
         if (j < k) {
            j = k;
         }

         return i << 20 | j << 4;
      }
   }

   public boolean isRenderingReady(BlockPos pos) {
      ChunkBuilder.BuiltChunk lv = this.chunks.getRenderedChunk(pos);
      return lv != null && lv.data.get() != ChunkBuilder.ChunkData.EMPTY;
   }

   @Nullable
   public Framebuffer getEntityOutlinesFramebuffer() {
      return this.entityOutlinesFramebuffer;
   }

   @Nullable
   public Framebuffer getTranslucentFramebuffer() {
      return this.translucentFramebuffer;
   }

   @Nullable
   public Framebuffer getEntityFramebuffer() {
      return this.entityFramebuffer;
   }

   @Nullable
   public Framebuffer getParticlesFramebuffer() {
      return this.particlesFramebuffer;
   }

   @Nullable
   public Framebuffer getWeatherFramebuffer() {
      return this.weatherFramebuffer;
   }

   @Nullable
   public Framebuffer getCloudsFramebuffer() {
      return this.cloudsFramebuffer;
   }

   @Environment(EnvType.CLIENT)
   public static class ProgramInitException extends RuntimeException {
      public ProgramInitException(String message, Throwable cause) {
         super(message, cause);
      }
   }

   @Environment(EnvType.CLIENT)
   private static class RenderableChunks {
      public final ChunkInfoList chunkInfoList;
      public final LinkedHashSet chunks;

      public RenderableChunks(int chunkCount) {
         this.chunkInfoList = new ChunkInfoList(chunkCount);
         this.chunks = new LinkedHashSet(chunkCount);
      }
   }

   @Environment(EnvType.CLIENT)
   static class ChunkInfo {
      final ChunkBuilder.BuiltChunk chunk;
      private byte direction;
      byte cullingState;
      final int propagationLevel;

      ChunkInfo(ChunkBuilder.BuiltChunk chunk, @Nullable Direction direction, int propagationLevel) {
         this.chunk = chunk;
         if (direction != null) {
            this.addDirection(direction);
         }

         this.propagationLevel = propagationLevel;
      }

      public void updateCullingState(byte parentCullingState, Direction from) {
         this.cullingState = (byte)(this.cullingState | parentCullingState | 1 << from.ordinal());
      }

      public boolean canCull(Direction from) {
         return (this.cullingState & 1 << from.ordinal()) > 0;
      }

      public void addDirection(Direction direction) {
         this.direction = (byte)(this.direction | this.direction | 1 << direction.ordinal());
      }

      public boolean hasDirection(int ordinal) {
         return (this.direction & 1 << ordinal) > 0;
      }

      public boolean hasAnyDirection() {
         return this.direction != 0;
      }

      public boolean method_49633(int i, int j, int k) {
         BlockPos lv = this.chunk.getOrigin();
         return i == lv.getX() / 16 || k == lv.getZ() / 16 || j == lv.getY() / 16;
      }

      public int hashCode() {
         return this.chunk.getOrigin().hashCode();
      }

      public boolean equals(Object o) {
         if (!(o instanceof ChunkInfo lv)) {
            return false;
         } else {
            return this.chunk.getOrigin().equals(lv.chunk.getOrigin());
         }
      }
   }

   @Environment(EnvType.CLIENT)
   static class ChunkInfoList {
      private final ChunkInfo[] current;

      ChunkInfoList(int size) {
         this.current = new ChunkInfo[size];
      }

      public void setInfo(ChunkBuilder.BuiltChunk chunk, ChunkInfo info) {
         this.current[chunk.index] = info;
      }

      @Nullable
      public ChunkInfo getInfo(ChunkBuilder.BuiltChunk chunk) {
         int i = chunk.index;
         return i >= 0 && i < this.current.length ? this.current[i] : null;
      }
   }
}
