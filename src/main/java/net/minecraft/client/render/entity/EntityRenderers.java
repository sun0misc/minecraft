/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.AllayEntityRenderer;
import net.minecraft.client.render.entity.ArmadilloEntityRenderer;
import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import net.minecraft.client.render.entity.ArrowEntityRenderer;
import net.minecraft.client.render.entity.AxolotlEntityRenderer;
import net.minecraft.client.render.entity.BatEntityRenderer;
import net.minecraft.client.render.entity.BeeEntityRenderer;
import net.minecraft.client.render.entity.BlazeEntityRenderer;
import net.minecraft.client.render.entity.BoatEntityRenderer;
import net.minecraft.client.render.entity.BoggedEntityRenderer;
import net.minecraft.client.render.entity.BreezeEntityRenderer;
import net.minecraft.client.render.entity.CamelEntityRenderer;
import net.minecraft.client.render.entity.CatEntityRenderer;
import net.minecraft.client.render.entity.CaveSpiderEntityRenderer;
import net.minecraft.client.render.entity.ChickenEntityRenderer;
import net.minecraft.client.render.entity.CodEntityRenderer;
import net.minecraft.client.render.entity.CowEntityRenderer;
import net.minecraft.client.render.entity.CreeperEntityRenderer;
import net.minecraft.client.render.entity.DisplayEntityRenderer;
import net.minecraft.client.render.entity.DolphinEntityRenderer;
import net.minecraft.client.render.entity.DonkeyEntityRenderer;
import net.minecraft.client.render.entity.DragonFireballEntityRenderer;
import net.minecraft.client.render.entity.DrownedEntityRenderer;
import net.minecraft.client.render.entity.ElderGuardianEntityRenderer;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.render.entity.EnderDragonEntityRenderer;
import net.minecraft.client.render.entity.EndermanEntityRenderer;
import net.minecraft.client.render.entity.EndermiteEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EvokerEntityRenderer;
import net.minecraft.client.render.entity.EvokerFangsEntityRenderer;
import net.minecraft.client.render.entity.ExperienceOrbEntityRenderer;
import net.minecraft.client.render.entity.FallingBlockEntityRenderer;
import net.minecraft.client.render.entity.FireworkRocketEntityRenderer;
import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.FoxEntityRenderer;
import net.minecraft.client.render.entity.FrogEntityRenderer;
import net.minecraft.client.render.entity.GhastEntityRenderer;
import net.minecraft.client.render.entity.GiantEntityRenderer;
import net.minecraft.client.render.entity.GlowSquidEntityRenderer;
import net.minecraft.client.render.entity.GoatEntityRenderer;
import net.minecraft.client.render.entity.GuardianEntityRenderer;
import net.minecraft.client.render.entity.HoglinEntityRenderer;
import net.minecraft.client.render.entity.HorseEntityRenderer;
import net.minecraft.client.render.entity.HuskEntityRenderer;
import net.minecraft.client.render.entity.IllusionerEntityRenderer;
import net.minecraft.client.render.entity.IronGolemEntityRenderer;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.render.entity.LeashKnotEntityRenderer;
import net.minecraft.client.render.entity.LightningEntityRenderer;
import net.minecraft.client.render.entity.LlamaEntityRenderer;
import net.minecraft.client.render.entity.LlamaSpitEntityRenderer;
import net.minecraft.client.render.entity.MagmaCubeEntityRenderer;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.render.entity.MooshroomEntityRenderer;
import net.minecraft.client.render.entity.OcelotEntityRenderer;
import net.minecraft.client.render.entity.OminousItemSpawnerEntityRenderer;
import net.minecraft.client.render.entity.PaintingEntityRenderer;
import net.minecraft.client.render.entity.PandaEntityRenderer;
import net.minecraft.client.render.entity.ParrotEntityRenderer;
import net.minecraft.client.render.entity.PhantomEntityRenderer;
import net.minecraft.client.render.entity.PigEntityRenderer;
import net.minecraft.client.render.entity.PiglinEntityRenderer;
import net.minecraft.client.render.entity.PillagerEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.PolarBearEntityRenderer;
import net.minecraft.client.render.entity.PufferfishEntityRenderer;
import net.minecraft.client.render.entity.RabbitEntityRenderer;
import net.minecraft.client.render.entity.RavagerEntityRenderer;
import net.minecraft.client.render.entity.SalmonEntityRenderer;
import net.minecraft.client.render.entity.SheepEntityRenderer;
import net.minecraft.client.render.entity.ShulkerBulletEntityRenderer;
import net.minecraft.client.render.entity.ShulkerEntityRenderer;
import net.minecraft.client.render.entity.SilverfishEntityRenderer;
import net.minecraft.client.render.entity.SkeletonEntityRenderer;
import net.minecraft.client.render.entity.SlimeEntityRenderer;
import net.minecraft.client.render.entity.SnifferEntityRenderer;
import net.minecraft.client.render.entity.SnowGolemEntityRenderer;
import net.minecraft.client.render.entity.SpectralArrowEntityRenderer;
import net.minecraft.client.render.entity.SpiderEntityRenderer;
import net.minecraft.client.render.entity.SquidEntityRenderer;
import net.minecraft.client.render.entity.StrayEntityRenderer;
import net.minecraft.client.render.entity.StriderEntityRenderer;
import net.minecraft.client.render.entity.TadpoleEntityRenderer;
import net.minecraft.client.render.entity.TntEntityRenderer;
import net.minecraft.client.render.entity.TntMinecartEntityRenderer;
import net.minecraft.client.render.entity.TridentEntityRenderer;
import net.minecraft.client.render.entity.TropicalFishEntityRenderer;
import net.minecraft.client.render.entity.TurtleEntityRenderer;
import net.minecraft.client.render.entity.VexEntityRenderer;
import net.minecraft.client.render.entity.VillagerEntityRenderer;
import net.minecraft.client.render.entity.VindicatorEntityRenderer;
import net.minecraft.client.render.entity.WanderingTraderEntityRenderer;
import net.minecraft.client.render.entity.WardenEntityRenderer;
import net.minecraft.client.render.entity.WindChargeEntityRenderer;
import net.minecraft.client.render.entity.WitchEntityRenderer;
import net.minecraft.client.render.entity.WitherEntityRenderer;
import net.minecraft.client.render.entity.WitherSkeletonEntityRenderer;
import net.minecraft.client.render.entity.WitherSkullEntityRenderer;
import net.minecraft.client.render.entity.WolfEntityRenderer;
import net.minecraft.client.render.entity.ZoglinEntityRenderer;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import net.minecraft.client.render.entity.ZombieHorseEntityRenderer;
import net.minecraft.client.render.entity.ZombieVillagerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SquidEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class EntityRenderers {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<EntityType<?>, EntityRendererFactory<?>> RENDERER_FACTORIES = new Object2ObjectOpenHashMap();
    private static final Map<SkinTextures.Model, EntityRendererFactory<AbstractClientPlayerEntity>> PLAYER_RENDERER_FACTORIES = Map.of(SkinTextures.Model.WIDE, context -> new PlayerEntityRenderer(context, false), SkinTextures.Model.SLIM, context -> new PlayerEntityRenderer(context, true));

    private static <T extends Entity> void register(EntityType<? extends T> type, EntityRendererFactory<T> factory) {
        RENDERER_FACTORIES.put(type, factory);
    }

    public static Map<EntityType<?>, EntityRenderer<?>> reloadEntityRenderers(EntityRendererFactory.Context ctx) {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        RENDERER_FACTORIES.forEach((entityType, factory) -> {
            try {
                builder.put(entityType, factory.create(ctx));
            } catch (Exception exception) {
                throw new IllegalArgumentException("Failed to create model for " + String.valueOf(Registries.ENTITY_TYPE.getId((EntityType<?>)entityType)), exception);
            }
        });
        return builder.build();
    }

    public static Map<SkinTextures.Model, EntityRenderer<? extends PlayerEntity>> reloadPlayerRenderers(EntityRendererFactory.Context ctx) {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        PLAYER_RENDERER_FACTORIES.forEach((model, factory) -> {
            try {
                builder.put(model, factory.create(ctx));
            } catch (Exception exception) {
                throw new IllegalArgumentException("Failed to create player model for " + String.valueOf(model), exception);
            }
        });
        return builder.build();
    }

    public static boolean isMissingRendererFactories() {
        boolean bl = true;
        for (EntityType entityType : Registries.ENTITY_TYPE) {
            if (entityType == EntityType.PLAYER || RENDERER_FACTORIES.containsKey(entityType)) continue;
            LOGGER.warn("No renderer registered for {}", (Object)Registries.ENTITY_TYPE.getId(entityType));
            bl = false;
        }
        return !bl;
    }

    static {
        EntityRenderers.register(EntityType.ALLAY, AllayEntityRenderer::new);
        EntityRenderers.register(EntityType.AREA_EFFECT_CLOUD, EmptyEntityRenderer::new);
        EntityRenderers.register(EntityType.ARMADILLO, ArmadilloEntityRenderer::new);
        EntityRenderers.register(EntityType.ARMOR_STAND, ArmorStandEntityRenderer::new);
        EntityRenderers.register(EntityType.ARROW, ArrowEntityRenderer::new);
        EntityRenderers.register(EntityType.AXOLOTL, AxolotlEntityRenderer::new);
        EntityRenderers.register(EntityType.BAT, BatEntityRenderer::new);
        EntityRenderers.register(EntityType.BEE, BeeEntityRenderer::new);
        EntityRenderers.register(EntityType.BLAZE, BlazeEntityRenderer::new);
        EntityRenderers.register(EntityType.BLOCK_DISPLAY, DisplayEntityRenderer.BlockDisplayEntityRenderer::new);
        EntityRenderers.register(EntityType.BOAT, context -> new BoatEntityRenderer(context, false));
        EntityRenderers.register(EntityType.BOGGED, BoggedEntityRenderer::new);
        EntityRenderers.register(EntityType.BREEZE, BreezeEntityRenderer::new);
        EntityRenderers.register(EntityType.BREEZE_WIND_CHARGE, WindChargeEntityRenderer::new);
        EntityRenderers.register(EntityType.CAT, CatEntityRenderer::new);
        EntityRenderers.register(EntityType.CAMEL, context -> new CamelEntityRenderer(context, EntityModelLayers.CAMEL));
        EntityRenderers.register(EntityType.CAVE_SPIDER, CaveSpiderEntityRenderer::new);
        EntityRenderers.register(EntityType.CHEST_BOAT, context -> new BoatEntityRenderer(context, true));
        EntityRenderers.register(EntityType.CHEST_MINECART, context -> new MinecartEntityRenderer(context, EntityModelLayers.CHEST_MINECART));
        EntityRenderers.register(EntityType.CHICKEN, ChickenEntityRenderer::new);
        EntityRenderers.register(EntityType.COD, CodEntityRenderer::new);
        EntityRenderers.register(EntityType.COMMAND_BLOCK_MINECART, context -> new MinecartEntityRenderer(context, EntityModelLayers.COMMAND_BLOCK_MINECART));
        EntityRenderers.register(EntityType.COW, CowEntityRenderer::new);
        EntityRenderers.register(EntityType.CREEPER, CreeperEntityRenderer::new);
        EntityRenderers.register(EntityType.DOLPHIN, DolphinEntityRenderer::new);
        EntityRenderers.register(EntityType.DONKEY, context -> new DonkeyEntityRenderer(context, 0.87f, EntityModelLayers.DONKEY));
        EntityRenderers.register(EntityType.DRAGON_FIREBALL, DragonFireballEntityRenderer::new);
        EntityRenderers.register(EntityType.DROWNED, DrownedEntityRenderer::new);
        EntityRenderers.register(EntityType.EGG, FlyingItemEntityRenderer::new);
        EntityRenderers.register(EntityType.ELDER_GUARDIAN, ElderGuardianEntityRenderer::new);
        EntityRenderers.register(EntityType.ENDERMAN, EndermanEntityRenderer::new);
        EntityRenderers.register(EntityType.ENDERMITE, EndermiteEntityRenderer::new);
        EntityRenderers.register(EntityType.ENDER_DRAGON, EnderDragonEntityRenderer::new);
        EntityRenderers.register(EntityType.ENDER_PEARL, FlyingItemEntityRenderer::new);
        EntityRenderers.register(EntityType.END_CRYSTAL, EndCrystalEntityRenderer::new);
        EntityRenderers.register(EntityType.EVOKER, EvokerEntityRenderer::new);
        EntityRenderers.register(EntityType.EVOKER_FANGS, EvokerFangsEntityRenderer::new);
        EntityRenderers.register(EntityType.EXPERIENCE_BOTTLE, FlyingItemEntityRenderer::new);
        EntityRenderers.register(EntityType.EXPERIENCE_ORB, ExperienceOrbEntityRenderer::new);
        EntityRenderers.register(EntityType.EYE_OF_ENDER, context -> new FlyingItemEntityRenderer(context, 1.0f, true));
        EntityRenderers.register(EntityType.FALLING_BLOCK, FallingBlockEntityRenderer::new);
        EntityRenderers.register(EntityType.FIREBALL, context -> new FlyingItemEntityRenderer(context, 3.0f, true));
        EntityRenderers.register(EntityType.FIREWORK_ROCKET, FireworkRocketEntityRenderer::new);
        EntityRenderers.register(EntityType.FISHING_BOBBER, FishingBobberEntityRenderer::new);
        EntityRenderers.register(EntityType.FOX, FoxEntityRenderer::new);
        EntityRenderers.register(EntityType.FROG, FrogEntityRenderer::new);
        EntityRenderers.register(EntityType.FURNACE_MINECART, context -> new MinecartEntityRenderer(context, EntityModelLayers.FURNACE_MINECART));
        EntityRenderers.register(EntityType.GHAST, GhastEntityRenderer::new);
        EntityRenderers.register(EntityType.GIANT, context -> new GiantEntityRenderer(context, 6.0f));
        EntityRenderers.register(EntityType.GLOW_ITEM_FRAME, ItemFrameEntityRenderer::new);
        EntityRenderers.register(EntityType.GLOW_SQUID, context -> new GlowSquidEntityRenderer(context, new SquidEntityModel<GlowSquidEntity>(context.getPart(EntityModelLayers.GLOW_SQUID))));
        EntityRenderers.register(EntityType.GOAT, GoatEntityRenderer::new);
        EntityRenderers.register(EntityType.GUARDIAN, GuardianEntityRenderer::new);
        EntityRenderers.register(EntityType.HOGLIN, HoglinEntityRenderer::new);
        EntityRenderers.register(EntityType.HOPPER_MINECART, context -> new MinecartEntityRenderer(context, EntityModelLayers.HOPPER_MINECART));
        EntityRenderers.register(EntityType.HORSE, HorseEntityRenderer::new);
        EntityRenderers.register(EntityType.HUSK, HuskEntityRenderer::new);
        EntityRenderers.register(EntityType.ILLUSIONER, IllusionerEntityRenderer::new);
        EntityRenderers.register(EntityType.INTERACTION, EmptyEntityRenderer::new);
        EntityRenderers.register(EntityType.IRON_GOLEM, IronGolemEntityRenderer::new);
        EntityRenderers.register(EntityType.ITEM, ItemEntityRenderer::new);
        EntityRenderers.register(EntityType.ITEM_DISPLAY, DisplayEntityRenderer.ItemDisplayEntityRenderer::new);
        EntityRenderers.register(EntityType.ITEM_FRAME, ItemFrameEntityRenderer::new);
        EntityRenderers.register(EntityType.OMINOUS_ITEM_SPAWNER, OminousItemSpawnerEntityRenderer::new);
        EntityRenderers.register(EntityType.LEASH_KNOT, LeashKnotEntityRenderer::new);
        EntityRenderers.register(EntityType.LIGHTNING_BOLT, LightningEntityRenderer::new);
        EntityRenderers.register(EntityType.LLAMA, context -> new LlamaEntityRenderer(context, EntityModelLayers.LLAMA));
        EntityRenderers.register(EntityType.LLAMA_SPIT, LlamaSpitEntityRenderer::new);
        EntityRenderers.register(EntityType.MAGMA_CUBE, MagmaCubeEntityRenderer::new);
        EntityRenderers.register(EntityType.MARKER, EmptyEntityRenderer::new);
        EntityRenderers.register(EntityType.MINECART, context -> new MinecartEntityRenderer(context, EntityModelLayers.MINECART));
        EntityRenderers.register(EntityType.MOOSHROOM, MooshroomEntityRenderer::new);
        EntityRenderers.register(EntityType.MULE, context -> new DonkeyEntityRenderer(context, 0.92f, EntityModelLayers.MULE));
        EntityRenderers.register(EntityType.OCELOT, OcelotEntityRenderer::new);
        EntityRenderers.register(EntityType.PAINTING, PaintingEntityRenderer::new);
        EntityRenderers.register(EntityType.PANDA, PandaEntityRenderer::new);
        EntityRenderers.register(EntityType.PARROT, ParrotEntityRenderer::new);
        EntityRenderers.register(EntityType.PHANTOM, PhantomEntityRenderer::new);
        EntityRenderers.register(EntityType.PIG, PigEntityRenderer::new);
        EntityRenderers.register(EntityType.PIGLIN, context -> new PiglinEntityRenderer(context, EntityModelLayers.PIGLIN, EntityModelLayers.PIGLIN_INNER_ARMOR, EntityModelLayers.PIGLIN_OUTER_ARMOR, false));
        EntityRenderers.register(EntityType.PIGLIN_BRUTE, context -> new PiglinEntityRenderer(context, EntityModelLayers.PIGLIN_BRUTE, EntityModelLayers.PIGLIN_BRUTE_INNER_ARMOR, EntityModelLayers.PIGLIN_BRUTE_OUTER_ARMOR, false));
        EntityRenderers.register(EntityType.PILLAGER, PillagerEntityRenderer::new);
        EntityRenderers.register(EntityType.POLAR_BEAR, PolarBearEntityRenderer::new);
        EntityRenderers.register(EntityType.POTION, FlyingItemEntityRenderer::new);
        EntityRenderers.register(EntityType.PUFFERFISH, PufferfishEntityRenderer::new);
        EntityRenderers.register(EntityType.RABBIT, RabbitEntityRenderer::new);
        EntityRenderers.register(EntityType.RAVAGER, RavagerEntityRenderer::new);
        EntityRenderers.register(EntityType.SALMON, SalmonEntityRenderer::new);
        EntityRenderers.register(EntityType.SHEEP, SheepEntityRenderer::new);
        EntityRenderers.register(EntityType.SHULKER, ShulkerEntityRenderer::new);
        EntityRenderers.register(EntityType.SHULKER_BULLET, ShulkerBulletEntityRenderer::new);
        EntityRenderers.register(EntityType.SILVERFISH, SilverfishEntityRenderer::new);
        EntityRenderers.register(EntityType.SKELETON, SkeletonEntityRenderer::new);
        EntityRenderers.register(EntityType.SKELETON_HORSE, context -> new ZombieHorseEntityRenderer(context, EntityModelLayers.SKELETON_HORSE));
        EntityRenderers.register(EntityType.SLIME, SlimeEntityRenderer::new);
        EntityRenderers.register(EntityType.SMALL_FIREBALL, context -> new FlyingItemEntityRenderer(context, 0.75f, true));
        EntityRenderers.register(EntityType.SNIFFER, SnifferEntityRenderer::new);
        EntityRenderers.register(EntityType.SNOWBALL, FlyingItemEntityRenderer::new);
        EntityRenderers.register(EntityType.SNOW_GOLEM, SnowGolemEntityRenderer::new);
        EntityRenderers.register(EntityType.SPAWNER_MINECART, context -> new MinecartEntityRenderer(context, EntityModelLayers.SPAWNER_MINECART));
        EntityRenderers.register(EntityType.SPECTRAL_ARROW, SpectralArrowEntityRenderer::new);
        EntityRenderers.register(EntityType.SPIDER, SpiderEntityRenderer::new);
        EntityRenderers.register(EntityType.SQUID, context -> new SquidEntityRenderer(context, new SquidEntityModel(context.getPart(EntityModelLayers.SQUID))));
        EntityRenderers.register(EntityType.STRAY, StrayEntityRenderer::new);
        EntityRenderers.register(EntityType.STRIDER, StriderEntityRenderer::new);
        EntityRenderers.register(EntityType.TADPOLE, TadpoleEntityRenderer::new);
        EntityRenderers.register(EntityType.TEXT_DISPLAY, DisplayEntityRenderer.TextDisplayEntityRenderer::new);
        EntityRenderers.register(EntityType.TNT, TntEntityRenderer::new);
        EntityRenderers.register(EntityType.TNT_MINECART, TntMinecartEntityRenderer::new);
        EntityRenderers.register(EntityType.TRADER_LLAMA, context -> new LlamaEntityRenderer(context, EntityModelLayers.TRADER_LLAMA));
        EntityRenderers.register(EntityType.TRIDENT, TridentEntityRenderer::new);
        EntityRenderers.register(EntityType.TROPICAL_FISH, TropicalFishEntityRenderer::new);
        EntityRenderers.register(EntityType.TURTLE, TurtleEntityRenderer::new);
        EntityRenderers.register(EntityType.VEX, VexEntityRenderer::new);
        EntityRenderers.register(EntityType.VILLAGER, VillagerEntityRenderer::new);
        EntityRenderers.register(EntityType.VINDICATOR, VindicatorEntityRenderer::new);
        EntityRenderers.register(EntityType.WARDEN, WardenEntityRenderer::new);
        EntityRenderers.register(EntityType.WANDERING_TRADER, WanderingTraderEntityRenderer::new);
        EntityRenderers.register(EntityType.WIND_CHARGE, WindChargeEntityRenderer::new);
        EntityRenderers.register(EntityType.WITCH, WitchEntityRenderer::new);
        EntityRenderers.register(EntityType.WITHER, WitherEntityRenderer::new);
        EntityRenderers.register(EntityType.WITHER_SKELETON, WitherSkeletonEntityRenderer::new);
        EntityRenderers.register(EntityType.WITHER_SKULL, WitherSkullEntityRenderer::new);
        EntityRenderers.register(EntityType.WOLF, WolfEntityRenderer::new);
        EntityRenderers.register(EntityType.ZOGLIN, ZoglinEntityRenderer::new);
        EntityRenderers.register(EntityType.ZOMBIE, ZombieEntityRenderer::new);
        EntityRenderers.register(EntityType.ZOMBIE_HORSE, context -> new ZombieHorseEntityRenderer(context, EntityModelLayers.ZOMBIE_HORSE));
        EntityRenderers.register(EntityType.ZOMBIE_VILLAGER, ZombieVillagerEntityRenderer::new);
        EntityRenderers.register(EntityType.ZOMBIFIED_PIGLIN, context -> new PiglinEntityRenderer(context, EntityModelLayers.ZOMBIFIED_PIGLIN, EntityModelLayers.ZOMBIFIED_PIGLIN_INNER_ARMOR, EntityModelLayers.ZOMBIFIED_PIGLIN_OUTER_ARMOR, true));
    }
}

