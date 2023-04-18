package net.minecraft.client.particle;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ParticleManager implements ResourceReloader {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceFinder FINDER = ResourceFinder.json("particles");
   private static final Identifier PARTICLES_PATH = new Identifier("particles");
   private static final int MAX_PARTICLE_COUNT = 16384;
   private static final List PARTICLE_TEXTURE_SHEETS;
   protected ClientWorld world;
   private final Map particles = Maps.newIdentityHashMap();
   private final Queue newEmitterParticles = Queues.newArrayDeque();
   private final TextureManager textureManager;
   private final Random random = Random.create();
   private final Int2ObjectMap factories = new Int2ObjectOpenHashMap();
   private final Queue newParticles = Queues.newArrayDeque();
   private final Map spriteAwareFactories = Maps.newHashMap();
   private final SpriteAtlasTexture particleAtlasTexture;
   private final Object2IntOpenHashMap groupCounts = new Object2IntOpenHashMap();

   public ParticleManager(ClientWorld world, TextureManager textureManager) {
      this.particleAtlasTexture = new SpriteAtlasTexture(SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
      textureManager.registerTexture(this.particleAtlasTexture.getId(), this.particleAtlasTexture);
      this.world = world;
      this.textureManager = textureManager;
      this.registerDefaultFactories();
   }

   private void registerDefaultFactories() {
      this.registerFactory(ParticleTypes.AMBIENT_ENTITY_EFFECT, (SpriteAwareFactory)(SpellParticle.EntityAmbientFactory::new));
      this.registerFactory(ParticleTypes.ANGRY_VILLAGER, (SpriteAwareFactory)(EmotionParticle.AngryVillagerFactory::new));
      this.registerFactory(ParticleTypes.BLOCK_MARKER, (ParticleFactory)(new BlockMarkerParticle.Factory()));
      this.registerFactory(ParticleTypes.BLOCK, (ParticleFactory)(new BlockDustParticle.Factory()));
      this.registerFactory(ParticleTypes.BUBBLE, (SpriteAwareFactory)(WaterBubbleParticle.Factory::new));
      this.registerFactory(ParticleTypes.BUBBLE_COLUMN_UP, (SpriteAwareFactory)(BubbleColumnUpParticle.Factory::new));
      this.registerFactory(ParticleTypes.BUBBLE_POP, (SpriteAwareFactory)(BubblePopParticle.Factory::new));
      this.registerFactory(ParticleTypes.CAMPFIRE_COSY_SMOKE, (SpriteAwareFactory)(CampfireSmokeParticle.CosySmokeFactory::new));
      this.registerFactory(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, (SpriteAwareFactory)(CampfireSmokeParticle.SignalSmokeFactory::new));
      this.registerFactory(ParticleTypes.CLOUD, (SpriteAwareFactory)(CloudParticle.CloudFactory::new));
      this.registerFactory(ParticleTypes.COMPOSTER, (SpriteAwareFactory)(SuspendParticle.Factory::new));
      this.registerFactory(ParticleTypes.CRIT, (SpriteAwareFactory)(DamageParticle.Factory::new));
      this.registerFactory(ParticleTypes.CURRENT_DOWN, (SpriteAwareFactory)(CurrentDownParticle.Factory::new));
      this.registerFactory(ParticleTypes.DAMAGE_INDICATOR, (SpriteAwareFactory)(DamageParticle.DefaultFactory::new));
      this.registerFactory(ParticleTypes.DRAGON_BREATH, (SpriteAwareFactory)(DragonBreathParticle.Factory::new));
      this.registerFactory(ParticleTypes.DOLPHIN, (SpriteAwareFactory)(SuspendParticle.DolphinFactory::new));
      this.registerBlockLeakFactory(ParticleTypes.DRIPPING_LAVA, BlockLeakParticle::createDrippingLava);
      this.registerBlockLeakFactory(ParticleTypes.FALLING_LAVA, BlockLeakParticle::createFallingLava);
      this.registerBlockLeakFactory(ParticleTypes.LANDING_LAVA, BlockLeakParticle::createLandingLava);
      this.registerBlockLeakFactory(ParticleTypes.DRIPPING_WATER, BlockLeakParticle::createDrippingWater);
      this.registerBlockLeakFactory(ParticleTypes.FALLING_WATER, BlockLeakParticle::createFallingWater);
      this.registerFactory(ParticleTypes.DUST, RedDustParticle.Factory::new);
      this.registerFactory(ParticleTypes.DUST_COLOR_TRANSITION, DustColorTransitionParticle.Factory::new);
      this.registerFactory(ParticleTypes.EFFECT, (SpriteAwareFactory)(SpellParticle.DefaultFactory::new));
      this.registerFactory(ParticleTypes.ELDER_GUARDIAN, (ParticleFactory)(new ElderGuardianAppearanceParticle.Factory()));
      this.registerFactory(ParticleTypes.ENCHANTED_HIT, (SpriteAwareFactory)(DamageParticle.EnchantedHitFactory::new));
      this.registerFactory(ParticleTypes.ENCHANT, (SpriteAwareFactory)(EnchantGlyphParticle.EnchantFactory::new));
      this.registerFactory(ParticleTypes.END_ROD, (SpriteAwareFactory)(EndRodParticle.Factory::new));
      this.registerFactory(ParticleTypes.ENTITY_EFFECT, (SpriteAwareFactory)(SpellParticle.EntityFactory::new));
      this.registerFactory(ParticleTypes.EXPLOSION_EMITTER, (ParticleFactory)(new ExplosionEmitterParticle.Factory()));
      this.registerFactory(ParticleTypes.EXPLOSION, (SpriteAwareFactory)(ExplosionLargeParticle.Factory::new));
      this.registerFactory(ParticleTypes.SONIC_BOOM, (SpriteAwareFactory)(SonicBoomParticle.Factory::new));
      this.registerFactory(ParticleTypes.FALLING_DUST, BlockFallingDustParticle.Factory::new);
      this.registerFactory(ParticleTypes.FIREWORK, (SpriteAwareFactory)(FireworksSparkParticle.ExplosionFactory::new));
      this.registerFactory(ParticleTypes.FISHING, (SpriteAwareFactory)(FishingParticle.Factory::new));
      this.registerFactory(ParticleTypes.FLAME, (SpriteAwareFactory)(FlameParticle.Factory::new));
      this.registerFactory(ParticleTypes.SCULK_SOUL, (SpriteAwareFactory)(SoulParticle.SculkSoulFactory::new));
      this.registerFactory(ParticleTypes.SCULK_CHARGE, SculkChargeParticle.Factory::new);
      this.registerFactory(ParticleTypes.SCULK_CHARGE_POP, (SpriteAwareFactory)(SculkChargePopParticle.Factory::new));
      this.registerFactory(ParticleTypes.SOUL, (SpriteAwareFactory)(SoulParticle.Factory::new));
      this.registerFactory(ParticleTypes.SOUL_FIRE_FLAME, (SpriteAwareFactory)(FlameParticle.Factory::new));
      this.registerFactory(ParticleTypes.FLASH, (SpriteAwareFactory)(FireworksSparkParticle.FlashFactory::new));
      this.registerFactory(ParticleTypes.HAPPY_VILLAGER, (SpriteAwareFactory)(SuspendParticle.HappyVillagerFactory::new));
      this.registerFactory(ParticleTypes.HEART, (SpriteAwareFactory)(EmotionParticle.HeartFactory::new));
      this.registerFactory(ParticleTypes.INSTANT_EFFECT, (SpriteAwareFactory)(SpellParticle.InstantFactory::new));
      this.registerFactory(ParticleTypes.ITEM, (ParticleFactory)(new CrackParticle.ItemFactory()));
      this.registerFactory(ParticleTypes.ITEM_SLIME, (ParticleFactory)(new CrackParticle.SlimeballFactory()));
      this.registerFactory(ParticleTypes.ITEM_SNOWBALL, (ParticleFactory)(new CrackParticle.SnowballFactory()));
      this.registerFactory(ParticleTypes.LARGE_SMOKE, (SpriteAwareFactory)(LargeFireSmokeParticle.Factory::new));
      this.registerFactory(ParticleTypes.LAVA, (SpriteAwareFactory)(LavaEmberParticle.Factory::new));
      this.registerFactory(ParticleTypes.MYCELIUM, (SpriteAwareFactory)(SuspendParticle.MyceliumFactory::new));
      this.registerFactory(ParticleTypes.NAUTILUS, (SpriteAwareFactory)(EnchantGlyphParticle.NautilusFactory::new));
      this.registerFactory(ParticleTypes.NOTE, (SpriteAwareFactory)(NoteParticle.Factory::new));
      this.registerFactory(ParticleTypes.POOF, (SpriteAwareFactory)(ExplosionSmokeParticle.Factory::new));
      this.registerFactory(ParticleTypes.PORTAL, (SpriteAwareFactory)(PortalParticle.Factory::new));
      this.registerFactory(ParticleTypes.RAIN, (SpriteAwareFactory)(RainSplashParticle.Factory::new));
      this.registerFactory(ParticleTypes.SMOKE, (SpriteAwareFactory)(FireSmokeParticle.Factory::new));
      this.registerFactory(ParticleTypes.SNEEZE, (SpriteAwareFactory)(CloudParticle.SneezeFactory::new));
      this.registerFactory(ParticleTypes.SNOWFLAKE, (SpriteAwareFactory)(SnowflakeParticle.Factory::new));
      this.registerFactory(ParticleTypes.SPIT, (SpriteAwareFactory)(SpitParticle.Factory::new));
      this.registerFactory(ParticleTypes.SWEEP_ATTACK, (SpriteAwareFactory)(SweepAttackParticle.Factory::new));
      this.registerFactory(ParticleTypes.TOTEM_OF_UNDYING, (SpriteAwareFactory)(TotemParticle.Factory::new));
      this.registerFactory(ParticleTypes.SQUID_INK, (SpriteAwareFactory)(SquidInkParticle.Factory::new));
      this.registerFactory(ParticleTypes.UNDERWATER, (SpriteAwareFactory)(WaterSuspendParticle.UnderwaterFactory::new));
      this.registerFactory(ParticleTypes.SPLASH, (SpriteAwareFactory)(WaterSplashParticle.SplashFactory::new));
      this.registerFactory(ParticleTypes.WITCH, (SpriteAwareFactory)(SpellParticle.WitchFactory::new));
      this.registerBlockLeakFactory(ParticleTypes.DRIPPING_HONEY, BlockLeakParticle::createDrippingHoney);
      this.registerBlockLeakFactory(ParticleTypes.FALLING_HONEY, BlockLeakParticle::createFallingHoney);
      this.registerBlockLeakFactory(ParticleTypes.LANDING_HONEY, BlockLeakParticle::createLandingHoney);
      this.registerBlockLeakFactory(ParticleTypes.FALLING_NECTAR, BlockLeakParticle::createFallingNectar);
      this.registerBlockLeakFactory(ParticleTypes.FALLING_SPORE_BLOSSOM, BlockLeakParticle::createFallingSporeBlossom);
      this.registerFactory(ParticleTypes.SPORE_BLOSSOM_AIR, (SpriteAwareFactory)(WaterSuspendParticle.SporeBlossomAirFactory::new));
      this.registerFactory(ParticleTypes.ASH, (SpriteAwareFactory)(AshParticle.Factory::new));
      this.registerFactory(ParticleTypes.CRIMSON_SPORE, (SpriteAwareFactory)(WaterSuspendParticle.CrimsonSporeFactory::new));
      this.registerFactory(ParticleTypes.WARPED_SPORE, (SpriteAwareFactory)(WaterSuspendParticle.WarpedSporeFactory::new));
      this.registerBlockLeakFactory(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, BlockLeakParticle::createDrippingObsidianTear);
      this.registerBlockLeakFactory(ParticleTypes.FALLING_OBSIDIAN_TEAR, BlockLeakParticle::createFallingObsidianTear);
      this.registerBlockLeakFactory(ParticleTypes.LANDING_OBSIDIAN_TEAR, BlockLeakParticle::createLandingObsidianTear);
      this.registerFactory(ParticleTypes.REVERSE_PORTAL, (SpriteAwareFactory)(ReversePortalParticle.Factory::new));
      this.registerFactory(ParticleTypes.WHITE_ASH, (SpriteAwareFactory)(WhiteAshParticle.Factory::new));
      this.registerFactory(ParticleTypes.SMALL_FLAME, (SpriteAwareFactory)(FlameParticle.SmallFactory::new));
      this.registerBlockLeakFactory(ParticleTypes.DRIPPING_DRIPSTONE_WATER, BlockLeakParticle::createDrippingDripstoneWater);
      this.registerBlockLeakFactory(ParticleTypes.FALLING_DRIPSTONE_WATER, BlockLeakParticle::createFallingDripstoneWater);
      this.registerFactory(ParticleTypes.CHERRY_LEAVES, (SpriteAwareFactory)((spriteProvider) -> {
         return (parameters, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            return new CherryLeavesParticle(world, x, y, z, spriteProvider);
         };
      }));
      this.registerBlockLeakFactory(ParticleTypes.DRIPPING_DRIPSTONE_LAVA, BlockLeakParticle::createDrippingDripstoneLava);
      this.registerBlockLeakFactory(ParticleTypes.FALLING_DRIPSTONE_LAVA, BlockLeakParticle::createFallingDripstoneLava);
      this.registerFactory(ParticleTypes.VIBRATION, VibrationParticle.Factory::new);
      this.registerFactory(ParticleTypes.GLOW_SQUID_INK, (SpriteAwareFactory)(SquidInkParticle.GlowSquidInkFactory::new));
      this.registerFactory(ParticleTypes.GLOW, (SpriteAwareFactory)(GlowParticle.GlowFactory::new));
      this.registerFactory(ParticleTypes.WAX_ON, (SpriteAwareFactory)(GlowParticle.WaxOnFactory::new));
      this.registerFactory(ParticleTypes.WAX_OFF, (SpriteAwareFactory)(GlowParticle.WaxOffFactory::new));
      this.registerFactory(ParticleTypes.ELECTRIC_SPARK, (SpriteAwareFactory)(GlowParticle.ElectricSparkFactory::new));
      this.registerFactory(ParticleTypes.SCRAPE, (SpriteAwareFactory)(GlowParticle.ScrapeFactory::new));
      this.registerFactory(ParticleTypes.SHRIEK, ShriekParticle.Factory::new);
      this.registerFactory(ParticleTypes.EGG_CRACK, (SpriteAwareFactory)(SuspendParticle.EggCrackFactory::new));
   }

   private void registerFactory(ParticleType type, ParticleFactory factory) {
      this.factories.put(Registries.PARTICLE_TYPE.getRawId(type), factory);
   }

   private void registerBlockLeakFactory(ParticleType type, ParticleFactory.BlockLeakParticleFactory factory) {
      this.registerFactory(type, (spriteBillboardParticle) -> {
         return (type, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            SpriteBillboardParticle lv = factory.createParticle(type, world, x, y, z, velocityX, velocityY, velocityZ);
            if (lv != null) {
               lv.setSprite(spriteBillboardParticle);
            }

            return lv;
         };
      });
   }

   private void registerFactory(ParticleType type, SpriteAwareFactory factory) {
      SimpleSpriteProvider lv = new SimpleSpriteProvider();
      this.spriteAwareFactories.put(Registries.PARTICLE_TYPE.getId(type), lv);
      this.factories.put(Registries.PARTICLE_TYPE.getRawId(type), factory.create(lv));
   }

   public CompletableFuture reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
      CompletableFuture completableFuture = CompletableFuture.supplyAsync(() -> {
         return FINDER.findResources(manager);
      }, prepareExecutor).thenCompose((particles) -> {
         List list = new ArrayList(particles.size());
         particles.forEach((id, resource) -> {
            Identifier lv = FINDER.toResourceId(id);
            list.add(CompletableFuture.supplyAsync(() -> {
               @Environment(EnvType.CLIENT)
               record ReloadResult(Identifier id, Optional sprites) {
                  ReloadResult(Identifier arg, Optional optional) {
                     this.id = arg;
                     this.sprites = optional;
                  }

                  public Identifier id() {
                     return this.id;
                  }

                  public Optional sprites() {
                     return this.sprites;
                  }
               }

               return new ReloadResult(lv, this.loadTextureList(lv, resource));
            }, prepareExecutor));
         });
         return Util.combineSafe(list);
      });
      CompletableFuture completableFuture2 = SpriteLoader.fromAtlas(this.particleAtlasTexture).load(manager, PARTICLES_PATH, 0, prepareExecutor).thenCompose(SpriteLoader.StitchResult::whenComplete);
      CompletableFuture var10000 = CompletableFuture.allOf(completableFuture2, completableFuture);
      Objects.requireNonNull(synchronizer);
      return var10000.thenCompose(synchronizer::whenPrepared).thenAcceptAsync((void_) -> {
         this.clearParticles();
         applyProfiler.startTick();
         applyProfiler.push("upload");
         SpriteLoader.StitchResult lv = (SpriteLoader.StitchResult)completableFuture2.join();
         this.particleAtlasTexture.upload(lv);
         applyProfiler.swap("bindSpriteSets");
         Set set = new HashSet();
         Sprite lv2 = lv.missing();
         ((List)completableFuture.join()).forEach((result) -> {
            Optional optional = result.sprites();
            if (!optional.isEmpty()) {
               List list = new ArrayList();
               Iterator var7 = ((List)optional.get()).iterator();

               while(var7.hasNext()) {
                  Identifier lvx = (Identifier)var7.next();
                  Sprite lv2x = (Sprite)lv.regions().get(lvx);
                  if (lv2x == null) {
                     set.add(lvx);
                     list.add(lv2);
                  } else {
                     list.add(lv2x);
                  }
               }

               if (list.isEmpty()) {
                  list.add(lv2);
               }

               ((SimpleSpriteProvider)this.spriteAwareFactories.get(result.id())).setSprites(list);
            }
         });
         if (!set.isEmpty()) {
            LOGGER.warn("Missing particle sprites: {}", set.stream().sorted().map(Identifier::toString).collect(Collectors.joining(",")));
         }

         applyProfiler.pop();
         applyProfiler.endTick();
      }, applyExecutor);
   }

   public void clearAtlas() {
      this.particleAtlasTexture.clear();
   }

   private Optional loadTextureList(Identifier id, Resource resource) {
      if (!this.spriteAwareFactories.containsKey(id)) {
         LOGGER.debug("Redundant texture list for particle: {}", id);
         return Optional.empty();
      } else {
         try {
            Reader reader = resource.getReader();

            Optional var5;
            try {
               ParticleTextureData lv = ParticleTextureData.load(JsonHelper.deserialize((Reader)reader));
               var5 = Optional.of(lv.getTextureList());
            } catch (Throwable var7) {
               if (reader != null) {
                  try {
                     reader.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (reader != null) {
               reader.close();
            }

            return var5;
         } catch (IOException var8) {
            throw new IllegalStateException("Failed to load description for particle " + id, var8);
         }
      }
   }

   public void addEmitter(Entity entity, ParticleEffect parameters) {
      this.newEmitterParticles.add(new EmitterParticle(this.world, entity, parameters));
   }

   public void addEmitter(Entity entity, ParticleEffect parameters, int maxAge) {
      this.newEmitterParticles.add(new EmitterParticle(this.world, entity, parameters, maxAge));
   }

   @Nullable
   public Particle addParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      Particle lv = this.createParticle(parameters, x, y, z, velocityX, velocityY, velocityZ);
      if (lv != null) {
         this.addParticle(lv);
         return lv;
      } else {
         return null;
      }
   }

   @Nullable
   private Particle createParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      ParticleFactory lv = (ParticleFactory)this.factories.get(Registries.PARTICLE_TYPE.getRawId(parameters.getType()));
      return lv == null ? null : lv.createParticle(parameters, this.world, x, y, z, velocityX, velocityY, velocityZ);
   }

   public void addParticle(Particle particle) {
      Optional optional = particle.getGroup();
      if (optional.isPresent()) {
         if (this.canAdd((ParticleGroup)optional.get())) {
            this.newParticles.add(particle);
            this.addTo((ParticleGroup)optional.get(), 1);
         }
      } else {
         this.newParticles.add(particle);
      }

   }

   public void tick() {
      this.particles.forEach((sheet, queue) -> {
         this.world.getProfiler().push(sheet.toString());
         this.tickParticles(queue);
         this.world.getProfiler().pop();
      });
      if (!this.newEmitterParticles.isEmpty()) {
         List list = Lists.newArrayList();
         Iterator var2 = this.newEmitterParticles.iterator();

         while(var2.hasNext()) {
            EmitterParticle lv = (EmitterParticle)var2.next();
            lv.tick();
            if (!lv.isAlive()) {
               list.add(lv);
            }
         }

         this.newEmitterParticles.removeAll(list);
      }

      Particle lv2;
      if (!this.newParticles.isEmpty()) {
         while((lv2 = (Particle)this.newParticles.poll()) != null) {
            ((Queue)this.particles.computeIfAbsent(lv2.getType(), (sheet) -> {
               return EvictingQueue.create(16384);
            })).add(lv2);
         }
      }

   }

   private void tickParticles(Collection particles) {
      if (!particles.isEmpty()) {
         Iterator iterator = particles.iterator();

         while(iterator.hasNext()) {
            Particle lv = (Particle)iterator.next();
            this.tickParticle(lv);
            if (!lv.isAlive()) {
               lv.getGroup().ifPresent((group) -> {
                  this.addTo(group, -1);
               });
               iterator.remove();
            }
         }
      }

   }

   private void addTo(ParticleGroup group, int count) {
      this.groupCounts.addTo(group, count);
   }

   private void tickParticle(Particle particle) {
      try {
         particle.tick();
      } catch (Throwable var5) {
         CrashReport lv = CrashReport.create(var5, "Ticking Particle");
         CrashReportSection lv2 = lv.addElement("Particle being ticked");
         Objects.requireNonNull(particle);
         lv2.add("Particle", particle::toString);
         ParticleTextureSheet var10002 = particle.getType();
         Objects.requireNonNull(var10002);
         lv2.add("Particle Type", var10002::toString);
         throw new CrashException(lv);
      }
   }

   public void renderParticles(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, LightmapTextureManager lightmapTextureManager, Camera camera, float tickDelta) {
      lightmapTextureManager.enable();
      RenderSystem.enableDepthTest();
      MatrixStack lv = RenderSystem.getModelViewStack();
      lv.push();
      lv.multiplyPositionMatrix(matrices.peek().getPositionMatrix());
      RenderSystem.applyModelViewMatrix();
      Iterator var7 = PARTICLE_TEXTURE_SHEETS.iterator();

      while(true) {
         ParticleTextureSheet lv2;
         Iterable iterable;
         do {
            if (!var7.hasNext()) {
               lv.pop();
               RenderSystem.applyModelViewMatrix();
               RenderSystem.depthMask(true);
               RenderSystem.disableBlend();
               lightmapTextureManager.disable();
               return;
            }

            lv2 = (ParticleTextureSheet)var7.next();
            iterable = (Iterable)this.particles.get(lv2);
         } while(iterable == null);

         RenderSystem.setShader(GameRenderer::getParticleProgram);
         Tessellator lv3 = Tessellator.getInstance();
         BufferBuilder lv4 = lv3.getBuffer();
         lv2.begin(lv4, this.textureManager);
         Iterator var12 = iterable.iterator();

         while(var12.hasNext()) {
            Particle lv5 = (Particle)var12.next();

            try {
               lv5.buildGeometry(lv4, camera, tickDelta);
            } catch (Throwable var17) {
               CrashReport lv6 = CrashReport.create(var17, "Rendering Particle");
               CrashReportSection lv7 = lv6.addElement("Particle being rendered");
               Objects.requireNonNull(lv5);
               lv7.add("Particle", lv5::toString);
               Objects.requireNonNull(lv2);
               lv7.add("Particle Type", lv2::toString);
               throw new CrashException(lv6);
            }
         }

         lv2.draw(lv3);
      }
   }

   public void setWorld(@Nullable ClientWorld world) {
      this.world = world;
      this.clearParticles();
      this.newEmitterParticles.clear();
   }

   public void addBlockBreakParticles(BlockPos pos, BlockState state) {
      if (!state.isAir() && state.hasBlockBreakParticles()) {
         VoxelShape lv = state.getOutlineShape(this.world, pos);
         double d = 0.25;
         lv.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double j = Math.min(1.0, maxX - minX);
            double k = Math.min(1.0, maxY - minY);
            double l = Math.min(1.0, maxZ - minZ);
            int m = Math.max(2, MathHelper.ceil(j / 0.25));
            int n = Math.max(2, MathHelper.ceil(k / 0.25));
            int o = Math.max(2, MathHelper.ceil(l / 0.25));

            for(int p = 0; p < m; ++p) {
               for(int q = 0; q < n; ++q) {
                  for(int r = 0; r < o; ++r) {
                     double s = ((double)p + 0.5) / (double)m;
                     double t = ((double)q + 0.5) / (double)n;
                     double u = ((double)r + 0.5) / (double)o;
                     double v = s * j + minX;
                     double w = t * k + minY;
                     double x = u * l + minZ;
                     this.addParticle(new BlockDustParticle(this.world, (double)pos.getX() + v, (double)pos.getY() + w, (double)pos.getZ() + x, s - 0.5, t - 0.5, u - 0.5, state, pos));
                  }
               }
            }

         });
      }
   }

   public void addBlockBreakingParticles(BlockPos pos, Direction direction) {
      BlockState lv = this.world.getBlockState(pos);
      if (lv.getRenderType() != BlockRenderType.INVISIBLE) {
         int i = pos.getX();
         int j = pos.getY();
         int k = pos.getZ();
         float f = 0.1F;
         Box lv2 = lv.getOutlineShape(this.world, pos).getBoundingBox();
         double d = (double)i + this.random.nextDouble() * (lv2.maxX - lv2.minX - 0.20000000298023224) + 0.10000000149011612 + lv2.minX;
         double e = (double)j + this.random.nextDouble() * (lv2.maxY - lv2.minY - 0.20000000298023224) + 0.10000000149011612 + lv2.minY;
         double g = (double)k + this.random.nextDouble() * (lv2.maxZ - lv2.minZ - 0.20000000298023224) + 0.10000000149011612 + lv2.minZ;
         if (direction == Direction.DOWN) {
            e = (double)j + lv2.minY - 0.10000000149011612;
         }

         if (direction == Direction.UP) {
            e = (double)j + lv2.maxY + 0.10000000149011612;
         }

         if (direction == Direction.NORTH) {
            g = (double)k + lv2.minZ - 0.10000000149011612;
         }

         if (direction == Direction.SOUTH) {
            g = (double)k + lv2.maxZ + 0.10000000149011612;
         }

         if (direction == Direction.WEST) {
            d = (double)i + lv2.minX - 0.10000000149011612;
         }

         if (direction == Direction.EAST) {
            d = (double)i + lv2.maxX + 0.10000000149011612;
         }

         this.addParticle((new BlockDustParticle(this.world, d, e, g, 0.0, 0.0, 0.0, lv, pos)).move(0.2F).scale(0.6F));
      }
   }

   public String getDebugString() {
      return String.valueOf(this.particles.values().stream().mapToInt(Collection::size).sum());
   }

   private boolean canAdd(ParticleGroup group) {
      return this.groupCounts.getInt(group) < group.getMaxCount();
   }

   private void clearParticles() {
      this.particles.clear();
      this.newParticles.clear();
      this.newEmitterParticles.clear();
      this.groupCounts.clear();
   }

   static {
      PARTICLE_TEXTURE_SHEETS = ImmutableList.of(ParticleTextureSheet.TERRAIN_SHEET, ParticleTextureSheet.PARTICLE_SHEET_OPAQUE, ParticleTextureSheet.PARTICLE_SHEET_LIT, ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT, ParticleTextureSheet.CUSTOM);
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   interface SpriteAwareFactory {
      ParticleFactory create(SpriteProvider spriteProvider);
   }

   @Environment(EnvType.CLIENT)
   static class SimpleSpriteProvider implements SpriteProvider {
      private List sprites;

      public Sprite getSprite(int age, int maxAge) {
         return (Sprite)this.sprites.get(age * (this.sprites.size() - 1) / maxAge);
      }

      public Sprite getSprite(Random random) {
         return (Sprite)this.sprites.get(random.nextInt(this.sprites.size()));
      }

      public void setSprites(List sprites) {
         this.sprites = ImmutableList.copyOf(sprites);
      }
   }
}
