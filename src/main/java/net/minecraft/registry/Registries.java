package net.minecraft.registry;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.block.Blocks;
import net.minecraft.block.DecoratedPotPatterns;
import net.minecraft.block.entity.BannerPatterns;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.painting.PaintingVariants;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.CatVariant;
import net.minecraft.entity.passive.FrogVariant;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Instruments;
import net.minecraft.item.Items;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.entry.LootPoolEntryTypes;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.provider.nbt.LootNbtProviderTypes;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.loot.provider.score.LootScoreProviderTypes;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.structure.rule.PosRuleTestType;
import net.minecraft.structure.rule.RuleTestType;
import net.minecraft.structure.rule.blockentity.RuleBlockEntityModifierType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.floatprovider.FloatProviderType;
import net.minecraft.util.math.intprovider.IntProviderType;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.biome.source.BiomeSources;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSourceType;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.chunk.ChunkGenerators;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.size.FeatureSizeType;
import net.minecraft.world.gen.foliage.FoliagePlacerType;
import net.minecraft.world.gen.heightprovider.HeightProviderType;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;
import net.minecraft.world.gen.root.RootPlacerType;
import net.minecraft.world.gen.stateprovider.BlockStateProviderType;
import net.minecraft.world.gen.structure.StructureType;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;
import net.minecraft.world.gen.trunk.TrunkPlacerType;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class Registries {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Map DEFAULT_ENTRIES = Maps.newLinkedHashMap();
   public static final Identifier ROOT_KEY = new Identifier("root");
   private static final MutableRegistry ROOT;
   public static final DefaultedRegistry GAME_EVENT;
   public static final Registry SOUND_EVENT;
   public static final DefaultedRegistry FLUID;
   public static final Registry STATUS_EFFECT;
   public static final DefaultedRegistry BLOCK;
   public static final Registry ENCHANTMENT;
   public static final DefaultedRegistry ENTITY_TYPE;
   public static final DefaultedRegistry ITEM;
   public static final DefaultedRegistry POTION;
   public static final Registry PARTICLE_TYPE;
   public static final Registry BLOCK_ENTITY_TYPE;
   public static final DefaultedRegistry PAINTING_VARIANT;
   public static final Registry CUSTOM_STAT;
   public static final DefaultedRegistry CHUNK_STATUS;
   public static final Registry RULE_TEST;
   public static final Registry RULE_BLOCK_ENTITY_MODIFIER;
   public static final Registry POS_RULE_TEST;
   public static final Registry SCREEN_HANDLER;
   public static final Registry RECIPE_TYPE;
   public static final Registry RECIPE_SERIALIZER;
   public static final Registry ATTRIBUTE;
   public static final Registry POSITION_SOURCE_TYPE;
   public static final Registry COMMAND_ARGUMENT_TYPE;
   public static final Registry STAT_TYPE;
   public static final DefaultedRegistry VILLAGER_TYPE;
   public static final DefaultedRegistry VILLAGER_PROFESSION;
   public static final Registry POINT_OF_INTEREST_TYPE;
   public static final DefaultedRegistry MEMORY_MODULE_TYPE;
   public static final DefaultedRegistry SENSOR_TYPE;
   public static final Registry SCHEDULE;
   public static final Registry ACTIVITY;
   public static final Registry LOOT_POOL_ENTRY_TYPE;
   public static final Registry LOOT_FUNCTION_TYPE;
   public static final Registry LOOT_CONDITION_TYPE;
   public static final Registry LOOT_NUMBER_PROVIDER_TYPE;
   public static final Registry LOOT_NBT_PROVIDER_TYPE;
   public static final Registry LOOT_SCORE_PROVIDER_TYPE;
   public static final Registry FLOAT_PROVIDER_TYPE;
   public static final Registry INT_PROVIDER_TYPE;
   public static final Registry HEIGHT_PROVIDER_TYPE;
   public static final Registry BLOCK_PREDICATE_TYPE;
   public static final Registry CARVER;
   public static final Registry FEATURE;
   public static final Registry STRUCTURE_PLACEMENT;
   public static final Registry STRUCTURE_PIECE;
   public static final Registry STRUCTURE_TYPE;
   public static final Registry PLACEMENT_MODIFIER_TYPE;
   public static final Registry BLOCK_STATE_PROVIDER_TYPE;
   public static final Registry FOLIAGE_PLACER_TYPE;
   public static final Registry TRUNK_PLACER_TYPE;
   public static final Registry ROOT_PLACER_TYPE;
   public static final Registry TREE_DECORATOR_TYPE;
   public static final Registry FEATURE_SIZE_TYPE;
   public static final Registry BIOME_SOURCE;
   public static final Registry CHUNK_GENERATOR;
   public static final Registry MATERIAL_CONDITION;
   public static final Registry MATERIAL_RULE;
   public static final Registry DENSITY_FUNCTION_TYPE;
   public static final Registry STRUCTURE_PROCESSOR;
   public static final Registry STRUCTURE_POOL_ELEMENT;
   public static final Registry CAT_VARIANT;
   public static final Registry FROG_VARIANT;
   public static final Registry BANNER_PATTERN;
   public static final Registry INSTRUMENT;
   public static final Registry DECORATED_POT_PATTERNS;
   public static final Registry REGISTRIES;

   private static Registry create(RegistryKey key, Initializer initializer) {
      return create(key, Lifecycle.stable(), initializer);
   }

   private static DefaultedRegistry create(RegistryKey key, String defaultId, Initializer initializer) {
      return create(key, defaultId, Lifecycle.stable(), initializer);
   }

   private static DefaultedRegistry createIntrusive(RegistryKey key, String defaultId, Initializer initializer) {
      return createIntrusive(key, defaultId, Lifecycle.stable(), initializer);
   }

   private static Registry create(RegistryKey key, Lifecycle lifecycle, Initializer initializer) {
      return create(key, (MutableRegistry)(new SimpleRegistry(key, lifecycle, false)), (Initializer)initializer, (Lifecycle)lifecycle);
   }

   private static DefaultedRegistry create(RegistryKey key, String defaultId, Lifecycle lifecycle, Initializer initializer) {
      return (DefaultedRegistry)create(key, (MutableRegistry)(new SimpleDefaultedRegistry(defaultId, key, lifecycle, false)), (Initializer)initializer, (Lifecycle)lifecycle);
   }

   private static DefaultedRegistry createIntrusive(RegistryKey key, String defaultId, Lifecycle lifecycle, Initializer initializer) {
      return (DefaultedRegistry)create(key, (MutableRegistry)(new SimpleDefaultedRegistry(defaultId, key, lifecycle, true)), (Initializer)initializer, (Lifecycle)lifecycle);
   }

   private static MutableRegistry create(RegistryKey key, MutableRegistry registry, Initializer initializer, Lifecycle lifecycle) {
      Identifier lv = key.getValue();
      DEFAULT_ENTRIES.put(lv, () -> {
         return initializer.run(registry);
      });
      ROOT.add(key, registry, lifecycle);
      return registry;
   }

   public static void bootstrap() {
      init();
      freezeRegistries();
      validate(REGISTRIES);
   }

   private static void init() {
      DEFAULT_ENTRIES.forEach((id, initializer) -> {
         if (initializer.get() == null) {
            LOGGER.error("Unable to bootstrap registry '{}'", id);
         }

      });
   }

   private static void freezeRegistries() {
      REGISTRIES.freeze();
      Iterator var0 = REGISTRIES.iterator();

      while(var0.hasNext()) {
         Registry lv = (Registry)var0.next();
         lv.freeze();
      }

   }

   private static void validate(Registry registries) {
      registries.forEach((registry) -> {
         if (registry.getIds().isEmpty()) {
            Identifier var10000 = registries.getId(registry);
            Util.error("Registry '" + var10000 + "' was empty after loading");
         }

         if (registry instanceof DefaultedRegistry) {
            Identifier lv = ((DefaultedRegistry)registry).getDefaultId();
            Validate.notNull(registry.get(lv), "Missing default of DefaultedMappedRegistry: " + lv, new Object[0]);
         }

      });
   }

   static {
      ROOT = new SimpleRegistry(RegistryKey.ofRegistry(ROOT_KEY), Lifecycle.stable());
      GAME_EVENT = createIntrusive(RegistryKeys.GAME_EVENT, "step", (registry) -> {
         return GameEvent.STEP;
      });
      SOUND_EVENT = create(RegistryKeys.SOUND_EVENT, (registry) -> {
         return SoundEvents.ENTITY_ITEM_PICKUP;
      });
      FLUID = createIntrusive(RegistryKeys.FLUID, "empty", (registry) -> {
         return Fluids.EMPTY;
      });
      STATUS_EFFECT = create(RegistryKeys.STATUS_EFFECT, (registry) -> {
         return StatusEffects.LUCK;
      });
      BLOCK = createIntrusive(RegistryKeys.BLOCK, "air", (registry) -> {
         return Blocks.AIR;
      });
      ENCHANTMENT = create(RegistryKeys.ENCHANTMENT, (registry) -> {
         return Enchantments.FORTUNE;
      });
      ENTITY_TYPE = createIntrusive(RegistryKeys.ENTITY_TYPE, "pig", (registry) -> {
         return EntityType.PIG;
      });
      ITEM = createIntrusive(RegistryKeys.ITEM, "air", (registry) -> {
         return Items.AIR;
      });
      POTION = create(RegistryKeys.POTION, "empty", (registry) -> {
         return Potions.EMPTY;
      });
      PARTICLE_TYPE = create(RegistryKeys.PARTICLE_TYPE, (registry) -> {
         return ParticleTypes.BLOCK;
      });
      BLOCK_ENTITY_TYPE = create(RegistryKeys.BLOCK_ENTITY_TYPE, (registry) -> {
         return BlockEntityType.FURNACE;
      });
      PAINTING_VARIANT = create(RegistryKeys.PAINTING_VARIANT, "kebab", PaintingVariants::registerAndGetDefault);
      CUSTOM_STAT = create(RegistryKeys.CUSTOM_STAT, (registry) -> {
         return Stats.JUMP;
      });
      CHUNK_STATUS = create(RegistryKeys.CHUNK_STATUS, "empty", (registry) -> {
         return ChunkStatus.EMPTY;
      });
      RULE_TEST = create(RegistryKeys.RULE_TEST, (registry) -> {
         return RuleTestType.ALWAYS_TRUE;
      });
      RULE_BLOCK_ENTITY_MODIFIER = create(RegistryKeys.RULE_BLOCK_ENTITY_MODIFIER, (registry) -> {
         return RuleBlockEntityModifierType.PASSTHROUGH;
      });
      POS_RULE_TEST = create(RegistryKeys.POS_RULE_TEST, (registry) -> {
         return PosRuleTestType.ALWAYS_TRUE;
      });
      SCREEN_HANDLER = create(RegistryKeys.SCREEN_HANDLER, (registry) -> {
         return ScreenHandlerType.ANVIL;
      });
      RECIPE_TYPE = create(RegistryKeys.RECIPE_TYPE, (registry) -> {
         return RecipeType.CRAFTING;
      });
      RECIPE_SERIALIZER = create(RegistryKeys.RECIPE_SERIALIZER, (registry) -> {
         return RecipeSerializer.SHAPELESS;
      });
      ATTRIBUTE = create(RegistryKeys.ATTRIBUTE, (registry) -> {
         return EntityAttributes.GENERIC_LUCK;
      });
      POSITION_SOURCE_TYPE = create(RegistryKeys.POSITION_SOURCE_TYPE, (registry) -> {
         return PositionSourceType.BLOCK;
      });
      COMMAND_ARGUMENT_TYPE = create(RegistryKeys.COMMAND_ARGUMENT_TYPE, ArgumentTypes::register);
      STAT_TYPE = create(RegistryKeys.STAT_TYPE, (registry) -> {
         return Stats.USED;
      });
      VILLAGER_TYPE = create(RegistryKeys.VILLAGER_TYPE, "plains", (registry) -> {
         return VillagerType.PLAINS;
      });
      VILLAGER_PROFESSION = create(RegistryKeys.VILLAGER_PROFESSION, "none", (registry) -> {
         return VillagerProfession.NONE;
      });
      POINT_OF_INTEREST_TYPE = create(RegistryKeys.POINT_OF_INTEREST_TYPE, PointOfInterestTypes::registerAndGetDefault);
      MEMORY_MODULE_TYPE = create(RegistryKeys.MEMORY_MODULE_TYPE, "dummy", (registry) -> {
         return MemoryModuleType.DUMMY;
      });
      SENSOR_TYPE = create(RegistryKeys.SENSOR_TYPE, "dummy", (registry) -> {
         return SensorType.DUMMY;
      });
      SCHEDULE = create(RegistryKeys.SCHEDULE, (registry) -> {
         return Schedule.EMPTY;
      });
      ACTIVITY = create(RegistryKeys.ACTIVITY, (registry) -> {
         return Activity.IDLE;
      });
      LOOT_POOL_ENTRY_TYPE = create(RegistryKeys.LOOT_POOL_ENTRY_TYPE, (registry) -> {
         return LootPoolEntryTypes.EMPTY;
      });
      LOOT_FUNCTION_TYPE = create(RegistryKeys.LOOT_FUNCTION_TYPE, (registry) -> {
         return LootFunctionTypes.SET_COUNT;
      });
      LOOT_CONDITION_TYPE = create(RegistryKeys.LOOT_CONDITION_TYPE, (registry) -> {
         return LootConditionTypes.INVERTED;
      });
      LOOT_NUMBER_PROVIDER_TYPE = create(RegistryKeys.LOOT_NUMBER_PROVIDER_TYPE, (registry) -> {
         return LootNumberProviderTypes.CONSTANT;
      });
      LOOT_NBT_PROVIDER_TYPE = create(RegistryKeys.LOOT_NBT_PROVIDER_TYPE, (registry) -> {
         return LootNbtProviderTypes.CONTEXT;
      });
      LOOT_SCORE_PROVIDER_TYPE = create(RegistryKeys.LOOT_SCORE_PROVIDER_TYPE, (registry) -> {
         return LootScoreProviderTypes.CONTEXT;
      });
      FLOAT_PROVIDER_TYPE = create(RegistryKeys.FLOAT_PROVIDER_TYPE, (registry) -> {
         return FloatProviderType.CONSTANT;
      });
      INT_PROVIDER_TYPE = create(RegistryKeys.INT_PROVIDER_TYPE, (registry) -> {
         return IntProviderType.CONSTANT;
      });
      HEIGHT_PROVIDER_TYPE = create(RegistryKeys.HEIGHT_PROVIDER_TYPE, (registry) -> {
         return HeightProviderType.CONSTANT;
      });
      BLOCK_PREDICATE_TYPE = create(RegistryKeys.BLOCK_PREDICATE_TYPE, (registry) -> {
         return BlockPredicateType.NOT;
      });
      CARVER = create(RegistryKeys.CARVER, (registry) -> {
         return Carver.CAVE;
      });
      FEATURE = create(RegistryKeys.FEATURE, (registry) -> {
         return Feature.ORE;
      });
      STRUCTURE_PLACEMENT = create(RegistryKeys.STRUCTURE_PLACEMENT, (registry) -> {
         return StructurePlacementType.RANDOM_SPREAD;
      });
      STRUCTURE_PIECE = create(RegistryKeys.STRUCTURE_PIECE, (registry) -> {
         return StructurePieceType.MINESHAFT_ROOM;
      });
      STRUCTURE_TYPE = create(RegistryKeys.STRUCTURE_TYPE, (registry) -> {
         return StructureType.JIGSAW;
      });
      PLACEMENT_MODIFIER_TYPE = create(RegistryKeys.PLACEMENT_MODIFIER_TYPE, (registry) -> {
         return PlacementModifierType.COUNT;
      });
      BLOCK_STATE_PROVIDER_TYPE = create(RegistryKeys.BLOCK_STATE_PROVIDER_TYPE, (registry) -> {
         return BlockStateProviderType.SIMPLE_STATE_PROVIDER;
      });
      FOLIAGE_PLACER_TYPE = create(RegistryKeys.FOLIAGE_PLACER_TYPE, (registry) -> {
         return FoliagePlacerType.BLOB_FOLIAGE_PLACER;
      });
      TRUNK_PLACER_TYPE = create(RegistryKeys.TRUNK_PLACER_TYPE, (registry) -> {
         return TrunkPlacerType.STRAIGHT_TRUNK_PLACER;
      });
      ROOT_PLACER_TYPE = create(RegistryKeys.ROOT_PLACER_TYPE, (registry) -> {
         return RootPlacerType.MANGROVE_ROOT_PLACER;
      });
      TREE_DECORATOR_TYPE = create(RegistryKeys.TREE_DECORATOR_TYPE, (registry) -> {
         return TreeDecoratorType.LEAVE_VINE;
      });
      FEATURE_SIZE_TYPE = create(RegistryKeys.FEATURE_SIZE_TYPE, (registry) -> {
         return FeatureSizeType.TWO_LAYERS_FEATURE_SIZE;
      });
      BIOME_SOURCE = create(RegistryKeys.BIOME_SOURCE, Lifecycle.stable(), BiomeSources::registerAndGetDefault);
      CHUNK_GENERATOR = create(RegistryKeys.CHUNK_GENERATOR, Lifecycle.stable(), ChunkGenerators::registerAndGetDefault);
      MATERIAL_CONDITION = create(RegistryKeys.MATERIAL_CONDITION, MaterialRules.MaterialCondition::registerAndGetDefault);
      MATERIAL_RULE = create(RegistryKeys.MATERIAL_RULE, MaterialRules.MaterialRule::registerAndGetDefault);
      DENSITY_FUNCTION_TYPE = create(RegistryKeys.DENSITY_FUNCTION_TYPE, DensityFunctionTypes::registerAndGetDefault);
      STRUCTURE_PROCESSOR = create(RegistryKeys.STRUCTURE_PROCESSOR, (registry) -> {
         return StructureProcessorType.BLOCK_IGNORE;
      });
      STRUCTURE_POOL_ELEMENT = create(RegistryKeys.STRUCTURE_POOL_ELEMENT, (registry) -> {
         return StructurePoolElementType.EMPTY_POOL_ELEMENT;
      });
      CAT_VARIANT = create(RegistryKeys.CAT_VARIANT, CatVariant::registerAndGetDefault);
      FROG_VARIANT = create(RegistryKeys.FROG_VARIANT, (registry) -> {
         return FrogVariant.TEMPERATE;
      });
      BANNER_PATTERN = create(RegistryKeys.BANNER_PATTERN, BannerPatterns::registerAndGetDefault);
      INSTRUMENT = create(RegistryKeys.INSTRUMENT, Instruments::registerAndGetDefault);
      DECORATED_POT_PATTERNS = create(RegistryKeys.DECORATED_POT_PATTERNS, DecoratedPotPatterns::registerAndGetDefault);
      REGISTRIES = ROOT;
   }

   @FunctionalInterface
   private interface Initializer {
      Object run(Registry registry);
   }
}
