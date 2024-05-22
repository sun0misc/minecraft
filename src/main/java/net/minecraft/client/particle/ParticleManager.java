/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.class_9801;
import net.minecraft.client.particle.AshParticle;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.particle.BlockFallingDustParticle;
import net.minecraft.client.particle.BlockLeakParticle;
import net.minecraft.client.particle.BlockMarkerParticle;
import net.minecraft.client.particle.BubbleColumnUpParticle;
import net.minecraft.client.particle.BubblePopParticle;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.client.particle.CherryLeavesParticle;
import net.minecraft.client.particle.CloudParticle;
import net.minecraft.client.particle.ConnectionParticle;
import net.minecraft.client.particle.CrackParticle;
import net.minecraft.client.particle.CurrentDownParticle;
import net.minecraft.client.particle.DamageParticle;
import net.minecraft.client.particle.DragonBreathParticle;
import net.minecraft.client.particle.DustColorTransitionParticle;
import net.minecraft.client.particle.DustPlumeParticle;
import net.minecraft.client.particle.ElderGuardianAppearanceParticle;
import net.minecraft.client.particle.EmitterParticle;
import net.minecraft.client.particle.EmotionParticle;
import net.minecraft.client.particle.EndRodParticle;
import net.minecraft.client.particle.ExplosionEmitterParticle;
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.particle.ExplosionSmokeParticle;
import net.minecraft.client.particle.FireSmokeParticle;
import net.minecraft.client.particle.FireworksSparkParticle;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.GlowParticle;
import net.minecraft.client.particle.GustEmitterParticle;
import net.minecraft.client.particle.GustParticle;
import net.minecraft.client.particle.LargeFireSmokeParticle;
import net.minecraft.client.particle.LavaEmberParticle;
import net.minecraft.client.particle.NoteParticle;
import net.minecraft.client.particle.OminousSpawningParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleTextureData;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.particle.RainSplashParticle;
import net.minecraft.client.particle.RedDustParticle;
import net.minecraft.client.particle.ReversePortalParticle;
import net.minecraft.client.particle.SculkChargeParticle;
import net.minecraft.client.particle.SculkChargePopParticle;
import net.minecraft.client.particle.ShriekParticle;
import net.minecraft.client.particle.SnowflakeParticle;
import net.minecraft.client.particle.SonicBoomParticle;
import net.minecraft.client.particle.SoulParticle;
import net.minecraft.client.particle.SpellParticle;
import net.minecraft.client.particle.SpitParticle;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.particle.SquidInkParticle;
import net.minecraft.client.particle.SuspendParticle;
import net.minecraft.client.particle.SweepAttackParticle;
import net.minecraft.client.particle.TotemParticle;
import net.minecraft.client.particle.TrialSpawnerDetectionParticle;
import net.minecraft.client.particle.VibrationParticle;
import net.minecraft.client.particle.WaterBubbleParticle;
import net.minecraft.client.particle.WaterSplashParticle;
import net.minecraft.client.particle.WaterSuspendParticle;
import net.minecraft.client.particle.WhiteAshParticle;
import net.minecraft.client.particle.WhiteSmokeParticle;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.client.texture.TextureManager;
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
import net.minecraft.util.crash.CrashCallable;
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

@Environment(value=EnvType.CLIENT)
public class ParticleManager
implements ResourceReloader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceFinder FINDER = ResourceFinder.json("particles");
    private static final Identifier PARTICLES_PATH = Identifier.method_60656("particles");
    private static final int MAX_PARTICLE_COUNT = 16384;
    private static final List<ParticleTextureSheet> PARTICLE_TEXTURE_SHEETS = ImmutableList.of(ParticleTextureSheet.TERRAIN_SHEET, ParticleTextureSheet.PARTICLE_SHEET_OPAQUE, ParticleTextureSheet.PARTICLE_SHEET_LIT, ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT, ParticleTextureSheet.CUSTOM);
    protected ClientWorld world;
    private final Map<ParticleTextureSheet, Queue<Particle>> particles = Maps.newIdentityHashMap();
    private final Queue<EmitterParticle> newEmitterParticles = Queues.newArrayDeque();
    private final TextureManager textureManager;
    private final Random random = Random.create();
    private final Int2ObjectMap<ParticleFactory<?>> factories = new Int2ObjectOpenHashMap();
    private final Queue<Particle> newParticles = Queues.newArrayDeque();
    private final Map<Identifier, SimpleSpriteProvider> spriteAwareFactories = Maps.newHashMap();
    private final SpriteAtlasTexture particleAtlasTexture;
    private final Object2IntOpenHashMap<ParticleGroup> groupCounts = new Object2IntOpenHashMap();

    public ParticleManager(ClientWorld world, TextureManager textureManager) {
        this.particleAtlasTexture = new SpriteAtlasTexture(SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
        textureManager.registerTexture(this.particleAtlasTexture.getId(), this.particleAtlasTexture);
        this.world = world;
        this.textureManager = textureManager;
        this.registerDefaultFactories();
    }

    private void registerDefaultFactories() {
        this.registerFactory(ParticleTypes.ANGRY_VILLAGER, EmotionParticle.AngryVillagerFactory::new);
        this.registerFactory(ParticleTypes.BLOCK_MARKER, new BlockMarkerParticle.Factory());
        this.registerFactory(ParticleTypes.BLOCK, new BlockDustParticle.Factory());
        this.registerFactory(ParticleTypes.BUBBLE, WaterBubbleParticle.Factory::new);
        this.registerFactory(ParticleTypes.BUBBLE_COLUMN_UP, BubbleColumnUpParticle.Factory::new);
        this.registerFactory(ParticleTypes.BUBBLE_POP, BubblePopParticle.Factory::new);
        this.registerFactory(ParticleTypes.CAMPFIRE_COSY_SMOKE, CampfireSmokeParticle.CosySmokeFactory::new);
        this.registerFactory(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, CampfireSmokeParticle.SignalSmokeFactory::new);
        this.registerFactory(ParticleTypes.CLOUD, CloudParticle.CloudFactory::new);
        this.registerFactory(ParticleTypes.COMPOSTER, SuspendParticle.Factory::new);
        this.registerFactory(ParticleTypes.CRIT, DamageParticle.Factory::new);
        this.registerFactory(ParticleTypes.CURRENT_DOWN, CurrentDownParticle.Factory::new);
        this.registerFactory(ParticleTypes.DAMAGE_INDICATOR, DamageParticle.DefaultFactory::new);
        this.registerFactory(ParticleTypes.DRAGON_BREATH, DragonBreathParticle.Factory::new);
        this.registerFactory(ParticleTypes.DOLPHIN, SuspendParticle.DolphinFactory::new);
        this.registerBlockLeakFactory(ParticleTypes.DRIPPING_LAVA, BlockLeakParticle::createDrippingLava);
        this.registerBlockLeakFactory(ParticleTypes.FALLING_LAVA, BlockLeakParticle::createFallingLava);
        this.registerBlockLeakFactory(ParticleTypes.LANDING_LAVA, BlockLeakParticle::createLandingLava);
        this.registerBlockLeakFactory(ParticleTypes.DRIPPING_WATER, BlockLeakParticle::createDrippingWater);
        this.registerBlockLeakFactory(ParticleTypes.FALLING_WATER, BlockLeakParticle::createFallingWater);
        this.registerFactory(ParticleTypes.DUST, RedDustParticle.Factory::new);
        this.registerFactory(ParticleTypes.DUST_COLOR_TRANSITION, DustColorTransitionParticle.Factory::new);
        this.registerFactory(ParticleTypes.EFFECT, SpellParticle.DefaultFactory::new);
        this.registerFactory(ParticleTypes.ELDER_GUARDIAN, new ElderGuardianAppearanceParticle.Factory());
        this.registerFactory(ParticleTypes.ENCHANTED_HIT, DamageParticle.EnchantedHitFactory::new);
        this.registerFactory(ParticleTypes.ENCHANT, ConnectionParticle.EnchantFactory::new);
        this.registerFactory(ParticleTypes.END_ROD, EndRodParticle.Factory::new);
        this.registerFactory(ParticleTypes.ENTITY_EFFECT, SpellParticle.EntityFactory::new);
        this.registerFactory(ParticleTypes.EXPLOSION_EMITTER, new ExplosionEmitterParticle.Factory());
        this.registerFactory(ParticleTypes.EXPLOSION, ExplosionLargeParticle.Factory::new);
        this.registerFactory(ParticleTypes.SONIC_BOOM, SonicBoomParticle.Factory::new);
        this.registerFactory(ParticleTypes.FALLING_DUST, BlockFallingDustParticle.Factory::new);
        this.registerFactory(ParticleTypes.GUST, GustParticle.Factory::new);
        this.registerFactory(ParticleTypes.SMALL_GUST, GustParticle.SmallGustFactory::new);
        this.registerFactory(ParticleTypes.GUST_EMITTER_LARGE, new GustEmitterParticle.Factory(3.0, 7, 0));
        this.registerFactory(ParticleTypes.GUST_EMITTER_SMALL, new GustEmitterParticle.Factory(1.0, 3, 2));
        this.registerFactory(ParticleTypes.FIREWORK, FireworksSparkParticle.ExplosionFactory::new);
        this.registerFactory(ParticleTypes.FISHING, FishingParticle.Factory::new);
        this.registerFactory(ParticleTypes.FLAME, FlameParticle.Factory::new);
        this.registerFactory(ParticleTypes.INFESTED, SpellParticle.DefaultFactory::new);
        this.registerFactory(ParticleTypes.SCULK_SOUL, SoulParticle.SculkSoulFactory::new);
        this.registerFactory(ParticleTypes.SCULK_CHARGE, SculkChargeParticle.Factory::new);
        this.registerFactory(ParticleTypes.SCULK_CHARGE_POP, SculkChargePopParticle.Factory::new);
        this.registerFactory(ParticleTypes.SOUL, SoulParticle.Factory::new);
        this.registerFactory(ParticleTypes.SOUL_FIRE_FLAME, FlameParticle.Factory::new);
        this.registerFactory(ParticleTypes.FLASH, FireworksSparkParticle.FlashFactory::new);
        this.registerFactory(ParticleTypes.HAPPY_VILLAGER, SuspendParticle.HappyVillagerFactory::new);
        this.registerFactory(ParticleTypes.HEART, EmotionParticle.HeartFactory::new);
        this.registerFactory(ParticleTypes.INSTANT_EFFECT, SpellParticle.InstantFactory::new);
        this.registerFactory(ParticleTypes.ITEM, new CrackParticle.ItemFactory());
        this.registerFactory(ParticleTypes.ITEM_SLIME, new CrackParticle.SlimeballFactory());
        this.registerFactory(ParticleTypes.ITEM_COBWEB, new CrackParticle.CobwebFactory());
        this.registerFactory(ParticleTypes.ITEM_SNOWBALL, new CrackParticle.SnowballFactory());
        this.registerFactory(ParticleTypes.LARGE_SMOKE, LargeFireSmokeParticle.Factory::new);
        this.registerFactory(ParticleTypes.LAVA, LavaEmberParticle.Factory::new);
        this.registerFactory(ParticleTypes.MYCELIUM, SuspendParticle.MyceliumFactory::new);
        this.registerFactory(ParticleTypes.NAUTILUS, ConnectionParticle.NautilusFactory::new);
        this.registerFactory(ParticleTypes.NOTE, NoteParticle.Factory::new);
        this.registerFactory(ParticleTypes.POOF, ExplosionSmokeParticle.Factory::new);
        this.registerFactory(ParticleTypes.PORTAL, PortalParticle.Factory::new);
        this.registerFactory(ParticleTypes.RAIN, RainSplashParticle.Factory::new);
        this.registerFactory(ParticleTypes.SMOKE, FireSmokeParticle.Factory::new);
        this.registerFactory(ParticleTypes.WHITE_SMOKE, WhiteSmokeParticle.Factory::new);
        this.registerFactory(ParticleTypes.SNEEZE, CloudParticle.SneezeFactory::new);
        this.registerFactory(ParticleTypes.SNOWFLAKE, SnowflakeParticle.Factory::new);
        this.registerFactory(ParticleTypes.SPIT, SpitParticle.Factory::new);
        this.registerFactory(ParticleTypes.SWEEP_ATTACK, SweepAttackParticle.Factory::new);
        this.registerFactory(ParticleTypes.TOTEM_OF_UNDYING, TotemParticle.Factory::new);
        this.registerFactory(ParticleTypes.SQUID_INK, SquidInkParticle.Factory::new);
        this.registerFactory(ParticleTypes.UNDERWATER, WaterSuspendParticle.UnderwaterFactory::new);
        this.registerFactory(ParticleTypes.SPLASH, WaterSplashParticle.SplashFactory::new);
        this.registerFactory(ParticleTypes.WITCH, SpellParticle.WitchFactory::new);
        this.registerBlockLeakFactory(ParticleTypes.DRIPPING_HONEY, BlockLeakParticle::createDrippingHoney);
        this.registerBlockLeakFactory(ParticleTypes.FALLING_HONEY, BlockLeakParticle::createFallingHoney);
        this.registerBlockLeakFactory(ParticleTypes.LANDING_HONEY, BlockLeakParticle::createLandingHoney);
        this.registerBlockLeakFactory(ParticleTypes.FALLING_NECTAR, BlockLeakParticle::createFallingNectar);
        this.registerBlockLeakFactory(ParticleTypes.FALLING_SPORE_BLOSSOM, BlockLeakParticle::createFallingSporeBlossom);
        this.registerFactory(ParticleTypes.SPORE_BLOSSOM_AIR, WaterSuspendParticle.SporeBlossomAirFactory::new);
        this.registerFactory(ParticleTypes.ASH, AshParticle.Factory::new);
        this.registerFactory(ParticleTypes.CRIMSON_SPORE, WaterSuspendParticle.CrimsonSporeFactory::new);
        this.registerFactory(ParticleTypes.WARPED_SPORE, WaterSuspendParticle.WarpedSporeFactory::new);
        this.registerBlockLeakFactory(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, BlockLeakParticle::createDrippingObsidianTear);
        this.registerBlockLeakFactory(ParticleTypes.FALLING_OBSIDIAN_TEAR, BlockLeakParticle::createFallingObsidianTear);
        this.registerBlockLeakFactory(ParticleTypes.LANDING_OBSIDIAN_TEAR, BlockLeakParticle::createLandingObsidianTear);
        this.registerFactory(ParticleTypes.REVERSE_PORTAL, ReversePortalParticle.Factory::new);
        this.registerFactory(ParticleTypes.WHITE_ASH, WhiteAshParticle.Factory::new);
        this.registerFactory(ParticleTypes.SMALL_FLAME, FlameParticle.SmallFactory::new);
        this.registerBlockLeakFactory(ParticleTypes.DRIPPING_DRIPSTONE_WATER, BlockLeakParticle::createDrippingDripstoneWater);
        this.registerBlockLeakFactory(ParticleTypes.FALLING_DRIPSTONE_WATER, BlockLeakParticle::createFallingDripstoneWater);
        this.registerFactory(ParticleTypes.CHERRY_LEAVES, (SpriteProvider spriteProvider) -> (parameters, world, x, y, z, velocityX, velocityY, velocityZ) -> new CherryLeavesParticle(world, x, y, z, spriteProvider));
        this.registerBlockLeakFactory(ParticleTypes.DRIPPING_DRIPSTONE_LAVA, BlockLeakParticle::createDrippingDripstoneLava);
        this.registerBlockLeakFactory(ParticleTypes.FALLING_DRIPSTONE_LAVA, BlockLeakParticle::createFallingDripstoneLava);
        this.registerFactory(ParticleTypes.VIBRATION, VibrationParticle.Factory::new);
        this.registerFactory(ParticleTypes.GLOW_SQUID_INK, SquidInkParticle.GlowSquidInkFactory::new);
        this.registerFactory(ParticleTypes.GLOW, GlowParticle.GlowFactory::new);
        this.registerFactory(ParticleTypes.WAX_ON, GlowParticle.WaxOnFactory::new);
        this.registerFactory(ParticleTypes.WAX_OFF, GlowParticle.WaxOffFactory::new);
        this.registerFactory(ParticleTypes.ELECTRIC_SPARK, GlowParticle.ElectricSparkFactory::new);
        this.registerFactory(ParticleTypes.SCRAPE, GlowParticle.ScrapeFactory::new);
        this.registerFactory(ParticleTypes.SHRIEK, ShriekParticle.Factory::new);
        this.registerFactory(ParticleTypes.EGG_CRACK, SuspendParticle.EggCrackFactory::new);
        this.registerFactory(ParticleTypes.DUST_PLUME, DustPlumeParticle.Factory::new);
        this.registerFactory(ParticleTypes.TRIAL_SPAWNER_DETECTION, TrialSpawnerDetectionParticle.Factory::new);
        this.registerFactory(ParticleTypes.TRIAL_SPAWNER_DETECTION_OMINOUS, TrialSpawnerDetectionParticle.Factory::new);
        this.registerFactory(ParticleTypes.VAULT_CONNECTION, ConnectionParticle.VaultConnectionFactory::new);
        this.registerFactory(ParticleTypes.DUST_PILLAR, new BlockDustParticle.DustPillarFactory());
        this.registerFactory(ParticleTypes.RAID_OMEN, SpellParticle.DefaultFactory::new);
        this.registerFactory(ParticleTypes.TRIAL_OMEN, SpellParticle.DefaultFactory::new);
        this.registerFactory(ParticleTypes.OMINOUS_SPAWNING, OminousSpawningParticle.Factory::new);
    }

    private <T extends ParticleEffect> void registerFactory(ParticleType<T> type, ParticleFactory<T> factory) {
        this.factories.put(Registries.PARTICLE_TYPE.getRawId(type), (ParticleFactory<?>)factory);
    }

    private <T extends ParticleEffect> void registerBlockLeakFactory(ParticleType<T> type, ParticleFactory.BlockLeakParticleFactory<T> factory) {
        this.registerFactory(type, (SpriteProvider spriteBillboardParticle) -> (type, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            SpriteBillboardParticle lv = factory.createParticle(type, world, x, y, z, velocityX, velocityY, velocityZ);
            if (lv != null) {
                lv.setSprite(spriteBillboardParticle);
            }
            return lv;
        });
    }

    private <T extends ParticleEffect> void registerFactory(ParticleType<T> type, SpriteAwareFactory<T> factory) {
        SimpleSpriteProvider lv = new SimpleSpriteProvider();
        this.spriteAwareFactories.put(Registries.PARTICLE_TYPE.getId(type), lv);
        this.factories.put(Registries.PARTICLE_TYPE.getRawId(type), (ParticleFactory<?>)factory.create(lv));
    }

    @Override
    public CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        CompletionStage completableFuture = CompletableFuture.supplyAsync(() -> FINDER.findResources(manager), prepareExecutor).thenCompose(particles -> {
            ArrayList list = new ArrayList(particles.size());
            particles.forEach((id, resource) -> {
                Identifier lv = FINDER.toResourceId((Identifier)id);
                list.add(CompletableFuture.supplyAsync(() -> {
                    @Environment(value=EnvType.CLIENT)
                    record ReloadResult(Identifier id, Optional<List<Identifier>> sprites) {
                    }
                    return new ReloadResult(lv, this.loadTextureList(lv, (Resource)resource));
                }, prepareExecutor));
            });
            return Util.combineSafe(list);
        });
        CompletionStage completableFuture2 = SpriteLoader.fromAtlas(this.particleAtlasTexture).load(manager, PARTICLES_PATH, 0, prepareExecutor).thenCompose(SpriteLoader.StitchResult::whenComplete);
        return ((CompletableFuture)CompletableFuture.allOf(new CompletableFuture[]{completableFuture2, completableFuture}).thenCompose(synchronizer::whenPrepared)).thenAcceptAsync(void_ -> {
            this.clearParticles();
            applyProfiler.startTick();
            applyProfiler.push("upload");
            SpriteLoader.StitchResult lv = (SpriteLoader.StitchResult)((CompletableFuture)completableFuture2).join();
            this.particleAtlasTexture.upload(lv);
            applyProfiler.swap("bindSpriteSets");
            HashSet set = new HashSet();
            Sprite lv2 = lv.missing();
            ((List)((CompletableFuture)completableFuture).join()).forEach(result -> {
                Optional<List<Identifier>> optional = result.sprites();
                if (optional.isEmpty()) {
                    return;
                }
                ArrayList<Sprite> list = new ArrayList<Sprite>();
                for (Identifier lv : optional.get()) {
                    Sprite lv2 = lv.regions().get(lv);
                    if (lv2 == null) {
                        set.add(lv);
                        list.add(lv2);
                        continue;
                    }
                    list.add(lv2);
                }
                if (list.isEmpty()) {
                    list.add(lv2);
                }
                this.spriteAwareFactories.get(result.id()).setSprites(list);
            });
            if (!set.isEmpty()) {
                LOGGER.warn("Missing particle sprites: {}", (Object)set.stream().sorted().map(Identifier::toString).collect(Collectors.joining(",")));
            }
            applyProfiler.pop();
            applyProfiler.endTick();
        }, applyExecutor);
    }

    public void clearAtlas() {
        this.particleAtlasTexture.clear();
    }

    private Optional<List<Identifier>> loadTextureList(Identifier id, Resource resource) {
        Optional<List<Identifier>> optional;
        block9: {
            if (!this.spriteAwareFactories.containsKey(id)) {
                LOGGER.debug("Redundant texture list for particle: {}", (Object)id);
                return Optional.empty();
            }
            BufferedReader reader = resource.getReader();
            try {
                ParticleTextureData lv = ParticleTextureData.load(JsonHelper.deserialize(reader));
                optional = Optional.of(lv.getTextureList());
                if (reader == null) break block9;
            } catch (Throwable throwable) {
                try {
                    if (reader != null) {
                        try {
                            ((Reader)reader).close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                } catch (IOException iOException) {
                    throw new IllegalStateException("Failed to load description for particle " + String.valueOf(id), iOException);
                }
            }
            ((Reader)reader).close();
        }
        return optional;
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
        }
        return null;
    }

    @Nullable
    private <T extends ParticleEffect> Particle createParticle(T parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        ParticleFactory lv = (ParticleFactory)this.factories.get(Registries.PARTICLE_TYPE.getRawId(parameters.getType()));
        if (lv == null) {
            return null;
        }
        return lv.createParticle(parameters, this.world, x, y, z, velocityX, velocityY, velocityZ);
    }

    public void addParticle(Particle particle) {
        Optional<ParticleGroup> optional = particle.getGroup();
        if (optional.isPresent()) {
            if (this.canAdd(optional.get())) {
                this.newParticles.add(particle);
                this.addTo(optional.get(), 1);
            }
        } else {
            this.newParticles.add(particle);
        }
    }

    public void tick() {
        this.particles.forEach((sheet, queue) -> {
            this.world.getProfiler().push(sheet.toString());
            this.tickParticles((Collection<Particle>)queue);
            this.world.getProfiler().pop();
        });
        if (!this.newEmitterParticles.isEmpty()) {
            ArrayList<EmitterParticle> list = Lists.newArrayList();
            for (EmitterParticle lv : this.newEmitterParticles) {
                lv.tick();
                if (lv.isAlive()) continue;
                list.add(lv);
            }
            this.newEmitterParticles.removeAll(list);
        }
        if (!this.newParticles.isEmpty()) {
            Particle lv2;
            while ((lv2 = this.newParticles.poll()) != null) {
                this.particles.computeIfAbsent(lv2.getType(), sheet -> EvictingQueue.create(16384)).add(lv2);
            }
        }
    }

    private void tickParticles(Collection<Particle> particles) {
        if (!particles.isEmpty()) {
            Iterator<Particle> iterator = particles.iterator();
            while (iterator.hasNext()) {
                Particle lv = iterator.next();
                this.tickParticle(lv);
                if (lv.isAlive()) continue;
                lv.getGroup().ifPresent(group -> this.addTo((ParticleGroup)group, -1));
                iterator.remove();
            }
        }
    }

    private void addTo(ParticleGroup group, int count) {
        this.groupCounts.addTo(group, count);
    }

    private void tickParticle(Particle particle) {
        try {
            particle.tick();
        } catch (Throwable throwable) {
            CrashReport lv = CrashReport.create(throwable, "Ticking Particle");
            CrashReportSection lv2 = lv.addElement("Particle being ticked");
            lv2.add("Particle", particle::toString);
            lv2.add("Particle Type", (CrashCallable)LambdaMetafactory.metafactory(null, null, null, ()Ljava/lang/Object;, toString(), ()Ljava/lang/String;)((ParticleTextureSheet)particle.getType()));
            throw new CrashException(lv);
        }
    }

    public void renderParticles(LightmapTextureManager lightmapTextureManager, Camera camera, float tickDelta) {
        lightmapTextureManager.enable();
        RenderSystem.enableDepthTest();
        for (ParticleTextureSheet lv : PARTICLE_TEXTURE_SHEETS) {
            Queue<Particle> queue = this.particles.get(lv);
            if (queue == null || queue.isEmpty()) continue;
            RenderSystem.setShader(GameRenderer::getParticleProgram);
            Tessellator lv2 = Tessellator.getInstance();
            BufferBuilder lv3 = lv.begin(lv2, this.textureManager);
            if (lv3 == null) continue;
            for (Particle lv4 : queue) {
                try {
                    lv4.buildGeometry(lv3, camera, tickDelta);
                } catch (Throwable throwable) {
                    CrashReport lv5 = CrashReport.create(throwable, "Rendering Particle");
                    CrashReportSection lv6 = lv5.addElement("Particle being rendered");
                    lv6.add("Particle", lv4::toString);
                    lv6.add("Particle Type", (CrashCallable)LambdaMetafactory.metafactory(null, null, null, ()Ljava/lang/Object;, toString(), ()Ljava/lang/String;)((ParticleTextureSheet)lv));
                    throw new CrashException(lv5);
                }
            }
            class_9801 lv7 = lv3.method_60794();
            if (lv7 == null) continue;
            BufferRenderer.drawWithGlobalProgram(lv7);
        }
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        lightmapTextureManager.disable();
    }

    public void setWorld(@Nullable ClientWorld world) {
        this.world = world;
        this.clearParticles();
        this.newEmitterParticles.clear();
    }

    public void addBlockBreakParticles(BlockPos pos, BlockState state) {
        if (state.isAir() || !state.hasBlockBreakParticles()) {
            return;
        }
        VoxelShape lv = state.getOutlineShape(this.world, pos);
        double d = 0.25;
        lv.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double j = Math.min(1.0, maxX - minX);
            double k = Math.min(1.0, maxY - minY);
            double l = Math.min(1.0, maxZ - minZ);
            int m = Math.max(2, MathHelper.ceil(j / 0.25));
            int n = Math.max(2, MathHelper.ceil(k / 0.25));
            int o = Math.max(2, MathHelper.ceil(l / 0.25));
            for (int p = 0; p < m; ++p) {
                for (int q = 0; q < n; ++q) {
                    for (int r = 0; r < o; ++r) {
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

    public void addBlockBreakingParticles(BlockPos pos, Direction direction) {
        BlockState lv = this.world.getBlockState(pos);
        if (lv.getRenderType() == BlockRenderType.INVISIBLE || !lv.hasBlockBreakParticles()) {
            return;
        }
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        float f = 0.1f;
        Box lv2 = lv.getOutlineShape(this.world, pos).getBoundingBox();
        double d = (double)i + this.random.nextDouble() * (lv2.maxX - lv2.minX - (double)0.2f) + (double)0.1f + lv2.minX;
        double e = (double)j + this.random.nextDouble() * (lv2.maxY - lv2.minY - (double)0.2f) + (double)0.1f + lv2.minY;
        double g = (double)k + this.random.nextDouble() * (lv2.maxZ - lv2.minZ - (double)0.2f) + (double)0.1f + lv2.minZ;
        if (direction == Direction.DOWN) {
            e = (double)j + lv2.minY - (double)0.1f;
        }
        if (direction == Direction.UP) {
            e = (double)j + lv2.maxY + (double)0.1f;
        }
        if (direction == Direction.NORTH) {
            g = (double)k + lv2.minZ - (double)0.1f;
        }
        if (direction == Direction.SOUTH) {
            g = (double)k + lv2.maxZ + (double)0.1f;
        }
        if (direction == Direction.WEST) {
            d = (double)i + lv2.minX - (double)0.1f;
        }
        if (direction == Direction.EAST) {
            d = (double)i + lv2.maxX + (double)0.1f;
        }
        this.addParticle(new BlockDustParticle(this.world, d, e, g, 0.0, 0.0, 0.0, lv, pos).move(0.2f).scale(0.6f));
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

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    static interface SpriteAwareFactory<T extends ParticleEffect> {
        public ParticleFactory<T> create(SpriteProvider var1);
    }

    @Environment(value=EnvType.CLIENT)
    static class SimpleSpriteProvider
    implements SpriteProvider {
        private List<Sprite> sprites;

        SimpleSpriteProvider() {
        }

        @Override
        public Sprite getSprite(int age, int maxAge) {
            return this.sprites.get(age * (this.sprites.size() - 1) / maxAge);
        }

        @Override
        public Sprite getSprite(Random random) {
            return this.sprites.get(random.nextInt(this.sprites.size()));
        }

        public void setSprites(List<Sprite> sprites) {
            this.sprites = ImmutableList.copyOf(sprites);
        }
    }
}

