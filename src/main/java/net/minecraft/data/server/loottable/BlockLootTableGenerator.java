package net.minecraft.data.server.loottable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.CaveVines;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.FlowerbedBlock;
import net.minecraft.block.MultifaceGrowthBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.condition.LocationCheckLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionConsumingBuilder;
import net.minecraft.loot.condition.MatchToolLootCondition;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.condition.TableBonusLootCondition;
import net.minecraft.loot.entry.DynamicEntry;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.CopyNameLootFunction;
import net.minecraft.loot.function.CopyNbtLootFunction;
import net.minecraft.loot.function.CopyStateFunction;
import net.minecraft.loot.function.ExplosionDecayLootFunction;
import net.minecraft.loot.function.LimitCountLootFunction;
import net.minecraft.loot.function.LootFunctionConsumingBuilder;
import net.minecraft.loot.function.SetContentsLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.operator.BoundedIntUnaryOperator;
import net.minecraft.loot.provider.nbt.ContextLootNbtProvider;
import net.minecraft.loot.provider.nbt.LootNbtProvider;
import net.minecraft.loot.provider.number.BinomialLootNumberProvider;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.item.EnchantmentPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public abstract class BlockLootTableGenerator implements LootTableGenerator {
   protected static final LootCondition.Builder WITH_SILK_TOUCH;
   protected static final LootCondition.Builder WITHOUT_SILK_TOUCH;
   protected static final LootCondition.Builder WITH_SHEARS;
   private static final LootCondition.Builder WITH_SILK_TOUCH_OR_SHEARS;
   private static final LootCondition.Builder WITHOUT_SILK_TOUCH_NOR_SHEARS;
   private final Set explosionImmuneItems;
   private final FeatureSet requiredFeatures;
   private final Map lootTables = new HashMap();
   protected static final float[] SAPLING_DROP_CHANCE;
   private static final float[] LEAVES_STICK_DROP_CHANCE;

   protected BlockLootTableGenerator(Set explosionImmuneItems, FeatureSet requiredFeatures) {
      this.explosionImmuneItems = explosionImmuneItems;
      this.requiredFeatures = requiredFeatures;
   }

   protected LootFunctionConsumingBuilder applyExplosionDecay(ItemConvertible drop, LootFunctionConsumingBuilder builder) {
      return !this.explosionImmuneItems.contains(drop.asItem()) ? builder.apply(ExplosionDecayLootFunction.builder()) : builder.getThisFunctionConsumingBuilder();
   }

   protected LootConditionConsumingBuilder addSurvivesExplosionCondition(ItemConvertible drop, LootConditionConsumingBuilder builder) {
      return !this.explosionImmuneItems.contains(drop.asItem()) ? builder.conditionally(SurvivesExplosionLootCondition.builder()) : builder.getThisConditionConsumingBuilder();
   }

   public LootTable.Builder drops(ItemConvertible drop) {
      return LootTable.builder().pool((LootPool.Builder)this.addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)).with(ItemEntry.builder(drop))));
   }

   private static LootTable.Builder drops(Block drop, LootCondition.Builder conditionBuilder, LootPoolEntry.Builder child) {
      return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)).with(((LeafEntry.Builder)ItemEntry.builder(drop).conditionally(conditionBuilder)).alternatively(child)));
   }

   protected static LootTable.Builder dropsWithSilkTouch(Block drop, LootPoolEntry.Builder child) {
      return drops(drop, WITH_SILK_TOUCH, child);
   }

   protected static LootTable.Builder dropsWithShears(Block drop, LootPoolEntry.Builder child) {
      return drops(drop, WITH_SHEARS, child);
   }

   protected static LootTable.Builder dropsWithSilkTouchOrShears(Block drop, LootPoolEntry.Builder child) {
      return drops(drop, WITH_SILK_TOUCH_OR_SHEARS, child);
   }

   protected LootTable.Builder drops(Block dropWithSilkTouch, ItemConvertible drop) {
      return dropsWithSilkTouch(dropWithSilkTouch, (LootPoolEntry.Builder)this.addSurvivesExplosionCondition(dropWithSilkTouch, ItemEntry.builder(drop)));
   }

   protected LootTable.Builder drops(ItemConvertible drop, LootNumberProvider count) {
      return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)).with((LootPoolEntry.Builder)this.applyExplosionDecay(drop, ItemEntry.builder(drop).apply(SetCountLootFunction.builder(count)))));
   }

   protected LootTable.Builder drops(Block dropWithSilkTouch, ItemConvertible drop, LootNumberProvider count) {
      return dropsWithSilkTouch(dropWithSilkTouch, (LootPoolEntry.Builder)this.applyExplosionDecay(dropWithSilkTouch, ItemEntry.builder(drop).apply(SetCountLootFunction.builder(count))));
   }

   private static LootTable.Builder dropsWithSilkTouch(ItemConvertible drop) {
      return LootTable.builder().pool(LootPool.builder().conditionally(WITH_SILK_TOUCH).rolls(ConstantLootNumberProvider.create(1.0F)).with(ItemEntry.builder(drop)));
   }

   private LootTable.Builder pottedPlantDrops(ItemConvertible drop) {
      return LootTable.builder().pool((LootPool.Builder)this.addSurvivesExplosionCondition(Blocks.FLOWER_POT, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)).with(ItemEntry.builder(Blocks.FLOWER_POT)))).pool((LootPool.Builder)this.addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)).with(ItemEntry.builder(drop))));
   }

   protected LootTable.Builder slabDrops(Block drop) {
      return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)).with((LootPoolEntry.Builder)this.applyExplosionDecay(drop, ItemEntry.builder(drop).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(2.0F)).conditionally(BlockStatePropertyLootCondition.builder(drop).properties(StatePredicate.Builder.create().exactMatch(SlabBlock.TYPE, (Comparable)SlabType.DOUBLE)))))));
   }

   protected LootTable.Builder dropsWithProperty(Block drop, Property property, Comparable value) {
      return LootTable.builder().pool((LootPool.Builder)this.addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)).with(ItemEntry.builder(drop).conditionally(BlockStatePropertyLootCondition.builder(drop).properties(StatePredicate.Builder.create().exactMatch(property, value))))));
   }

   protected LootTable.Builder nameableContainerDrops(Block drop) {
      return LootTable.builder().pool((LootPool.Builder)this.addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)).with(ItemEntry.builder(drop).apply(CopyNameLootFunction.builder(CopyNameLootFunction.Source.BLOCK_ENTITY)))));
   }

   protected LootTable.Builder shulkerBoxDrops(Block drop) {
      return LootTable.builder().pool((LootPool.Builder)this.addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)).with(ItemEntry.builder(drop).apply(CopyNameLootFunction.builder(CopyNameLootFunction.Source.BLOCK_ENTITY)).apply(CopyNbtLootFunction.builder((LootNbtProvider)ContextLootNbtProvider.BLOCK_ENTITY).withOperation("Lock", "BlockEntityTag.Lock").withOperation("LootTable", "BlockEntityTag.LootTable").withOperation("LootTableSeed", "BlockEntityTag.LootTableSeed")).apply(SetContentsLootFunction.builder(BlockEntityType.SHULKER_BOX).withEntry(DynamicEntry.builder(ShulkerBoxBlock.CONTENTS))))));
   }

   protected LootTable.Builder copperOreDrops(Block drop) {
      return dropsWithSilkTouch(drop, (LootPoolEntry.Builder)this.applyExplosionDecay(drop, ItemEntry.builder(Items.RAW_COPPER).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2.0F, 5.0F))).apply(ApplyBonusLootFunction.oreDrops(Enchantments.FORTUNE))));
   }

   protected LootTable.Builder lapisOreDrops(Block drop) {
      return dropsWithSilkTouch(drop, (LootPoolEntry.Builder)this.applyExplosionDecay(drop, ItemEntry.builder(Items.LAPIS_LAZULI).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(4.0F, 9.0F))).apply(ApplyBonusLootFunction.oreDrops(Enchantments.FORTUNE))));
   }

   protected LootTable.Builder redstoneOreDrops(Block drop) {
      return dropsWithSilkTouch(drop, (LootPoolEntry.Builder)this.applyExplosionDecay(drop, ItemEntry.builder(Items.REDSTONE).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(4.0F, 5.0F))).apply(ApplyBonusLootFunction.uniformBonusCount(Enchantments.FORTUNE))));
   }

   protected LootTable.Builder bannerDrops(Block drop) {
      return LootTable.builder().pool((LootPool.Builder)this.addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)).with(ItemEntry.builder(drop).apply(CopyNameLootFunction.builder(CopyNameLootFunction.Source.BLOCK_ENTITY)).apply(CopyNbtLootFunction.builder((LootNbtProvider)ContextLootNbtProvider.BLOCK_ENTITY).withOperation("Patterns", "BlockEntityTag.Patterns")))));
   }

   protected static LootTable.Builder beeNestDrops(Block drop) {
      return LootTable.builder().pool(LootPool.builder().conditionally(WITH_SILK_TOUCH).rolls(ConstantLootNumberProvider.create(1.0F)).with(ItemEntry.builder(drop).apply(CopyNbtLootFunction.builder((LootNbtProvider)ContextLootNbtProvider.BLOCK_ENTITY).withOperation("Bees", "BlockEntityTag.Bees")).apply(CopyStateFunction.builder(drop).addProperty(BeehiveBlock.HONEY_LEVEL))));
   }

   protected static LootTable.Builder beehiveDrops(Block drop) {
      return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)).with(((LeafEntry.Builder)ItemEntry.builder(drop).conditionally(WITH_SILK_TOUCH)).apply(CopyNbtLootFunction.builder((LootNbtProvider)ContextLootNbtProvider.BLOCK_ENTITY).withOperation("Bees", "BlockEntityTag.Bees")).apply(CopyStateFunction.builder(drop).addProperty(BeehiveBlock.HONEY_LEVEL)).alternatively(ItemEntry.builder(drop))));
   }

   protected static LootTable.Builder glowBerryDrops(Block drop) {
      return LootTable.builder().pool(LootPool.builder().with(ItemEntry.builder(Items.GLOW_BERRIES)).conditionally(BlockStatePropertyLootCondition.builder(drop).properties(StatePredicate.Builder.create().exactMatch(CaveVines.BERRIES, true))));
   }

   protected LootTable.Builder oreDrops(Block dropWithSilkTouch, Item drop) {
      return dropsWithSilkTouch(dropWithSilkTouch, (LootPoolEntry.Builder)this.applyExplosionDecay(dropWithSilkTouch, ItemEntry.builder(drop).apply(ApplyBonusLootFunction.oreDrops(Enchantments.FORTUNE))));
   }

   protected LootTable.Builder mushroomBlockDrops(Block dropWithSilkTouch, ItemConvertible drop) {
      return dropsWithSilkTouch(dropWithSilkTouch, (LootPoolEntry.Builder)this.applyExplosionDecay(dropWithSilkTouch, ItemEntry.builder(drop).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(-6.0F, 2.0F))).apply(LimitCountLootFunction.builder(BoundedIntUnaryOperator.createMin(0)))));
   }

   protected LootTable.Builder grassDrops(Block dropWithShears) {
      return dropsWithShears(dropWithShears, (LootPoolEntry.Builder)this.applyExplosionDecay(dropWithShears, ((LeafEntry.Builder)ItemEntry.builder(Items.WHEAT_SEEDS).conditionally(RandomChanceLootCondition.builder(0.125F))).apply(ApplyBonusLootFunction.uniformBonusCount(Enchantments.FORTUNE, 2))));
   }

   public LootTable.Builder cropStemDrops(Block stem, Item drop) {
      return LootTable.builder().pool((LootPool.Builder)this.applyExplosionDecay(stem, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)).with((LootPoolEntry.Builder)ItemEntry.builder(drop).apply(StemBlock.AGE.getValues(), (integer) -> {
         return SetCountLootFunction.builder(BinomialLootNumberProvider.create(3, (float)(integer + 1) / 15.0F)).conditionally(BlockStatePropertyLootCondition.builder(stem).properties(StatePredicate.Builder.create().exactMatch(StemBlock.AGE, integer)));
      }))));
   }

   public LootTable.Builder attachedCropStemDrops(Block stem, Item drop) {
      return LootTable.builder().pool((LootPool.Builder)this.applyExplosionDecay(stem, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)).with(ItemEntry.builder(drop).apply(SetCountLootFunction.builder(BinomialLootNumberProvider.create(3, 0.53333336F))))));
   }

   protected static LootTable.Builder dropsWithShears(ItemConvertible drop) {
      return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)).conditionally(WITH_SHEARS).with(ItemEntry.builder(drop)));
   }

   protected LootTable.Builder multifaceGrowthDrops(Block drop, LootCondition.Builder condition) {
      return LootTable.builder().pool(LootPool.builder().with((LootPoolEntry.Builder)this.applyExplosionDecay(drop, ((LeafEntry.Builder)((LeafEntry.Builder)ItemEntry.builder(drop).conditionally(condition)).apply(Direction.values(), (arg2) -> {
         return SetCountLootFunction.builder(ConstantLootNumberProvider.create(1.0F), true).conditionally(BlockStatePropertyLootCondition.builder(drop).properties(StatePredicate.Builder.create().exactMatch(MultifaceGrowthBlock.getProperty(arg2), true)));
      })).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(-1.0F), true)))));
   }

   protected LootTable.Builder leavesDrops(Block leaves, Block drop, float... chance) {
      return dropsWithSilkTouchOrShears(leaves, ((LeafEntry.Builder)this.addSurvivesExplosionCondition(leaves, ItemEntry.builder(drop))).conditionally(TableBonusLootCondition.builder(Enchantments.FORTUNE, chance))).pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)).conditionally(WITHOUT_SILK_TOUCH_NOR_SHEARS).with(((LeafEntry.Builder)this.applyExplosionDecay(leaves, ItemEntry.builder(Items.STICK).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0F, 2.0F))))).conditionally(TableBonusLootCondition.builder(Enchantments.FORTUNE, LEAVES_STICK_DROP_CHANCE))));
   }

   protected LootTable.Builder oakLeavesDrops(Block leaves, Block drop, float... chance) {
      return this.leavesDrops(leaves, drop, chance).pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)).conditionally(WITHOUT_SILK_TOUCH_NOR_SHEARS).with(((LeafEntry.Builder)this.addSurvivesExplosionCondition(leaves, ItemEntry.builder(Items.APPLE))).conditionally(TableBonusLootCondition.builder(Enchantments.FORTUNE, 0.005F, 0.0055555557F, 0.00625F, 0.008333334F, 0.025F))));
   }

   protected LootTable.Builder mangroveLeavesDrops(Block leaves) {
      return dropsWithSilkTouchOrShears(leaves, ((LeafEntry.Builder)this.applyExplosionDecay(Blocks.MANGROVE_LEAVES, ItemEntry.builder(Items.STICK).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0F, 2.0F))))).conditionally(TableBonusLootCondition.builder(Enchantments.FORTUNE, LEAVES_STICK_DROP_CHANCE)));
   }

   protected LootTable.Builder cropDrops(Block crop, Item product, Item seeds, LootCondition.Builder condition) {
      return (LootTable.Builder)this.applyExplosionDecay(crop, LootTable.builder().pool(LootPool.builder().with(((LeafEntry.Builder)ItemEntry.builder(product).conditionally(condition)).alternatively(ItemEntry.builder(seeds)))).pool(LootPool.builder().conditionally(condition).with(ItemEntry.builder(seeds).apply(ApplyBonusLootFunction.binomialWithBonusCount(Enchantments.FORTUNE, 0.5714286F, 3)))));
   }

   protected static LootTable.Builder seagrassDrops(Block seagrass) {
      return LootTable.builder().pool(LootPool.builder().conditionally(WITH_SHEARS).with(ItemEntry.builder(seagrass).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(2.0F)))));
   }

   protected LootTable.Builder tallGrassDrops(Block tallGrass, Block grass) {
      LootPoolEntry.Builder lv = ((LeafEntry.Builder)ItemEntry.builder(grass).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(2.0F))).conditionally(WITH_SHEARS)).alternatively(((LeafEntry.Builder)this.addSurvivesExplosionCondition(tallGrass, ItemEntry.builder(Items.WHEAT_SEEDS))).conditionally(RandomChanceLootCondition.builder(0.125F)));
      return LootTable.builder().pool(LootPool.builder().with(lv).conditionally(BlockStatePropertyLootCondition.builder(tallGrass).properties(StatePredicate.Builder.create().exactMatch(TallPlantBlock.HALF, (Comparable)DoubleBlockHalf.LOWER))).conditionally(LocationCheckLootCondition.builder(LocationPredicate.Builder.create().block(BlockPredicate.Builder.create().blocks(tallGrass).state(StatePredicate.Builder.create().exactMatch(TallPlantBlock.HALF, (Comparable)DoubleBlockHalf.UPPER).build()).build()), new BlockPos(0, 1, 0)))).pool(LootPool.builder().with(lv).conditionally(BlockStatePropertyLootCondition.builder(tallGrass).properties(StatePredicate.Builder.create().exactMatch(TallPlantBlock.HALF, (Comparable)DoubleBlockHalf.UPPER))).conditionally(LocationCheckLootCondition.builder(LocationPredicate.Builder.create().block(BlockPredicate.Builder.create().blocks(tallGrass).state(StatePredicate.Builder.create().exactMatch(TallPlantBlock.HALF, (Comparable)DoubleBlockHalf.LOWER).build()).build()), new BlockPos(0, -1, 0))));
   }

   protected LootTable.Builder candleDrops(Block candle) {
      return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)).with((LootPoolEntry.Builder)this.applyExplosionDecay(candle, ItemEntry.builder(candle).apply(List.of(2, 3, 4), (candles) -> {
         return SetCountLootFunction.builder(ConstantLootNumberProvider.create((float)candles)).conditionally(BlockStatePropertyLootCondition.builder(candle).properties(StatePredicate.Builder.create().exactMatch(CandleBlock.CANDLES, candles)));
      }))));
   }

   protected LootTable.Builder flowerbedDrops(Block flowerbed) {
      return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)).with((LootPoolEntry.Builder)this.applyExplosionDecay(flowerbed, ItemEntry.builder(flowerbed).apply(IntStream.rangeClosed(1, 4).boxed().toList(), (flowerAmount) -> {
         return SetCountLootFunction.builder(ConstantLootNumberProvider.create((float)flowerAmount)).conditionally(BlockStatePropertyLootCondition.builder(flowerbed).properties(StatePredicate.Builder.create().exactMatch(FlowerbedBlock.FLOWER_AMOUNT, flowerAmount)));
      }))));
   }

   protected static LootTable.Builder candleCakeDrops(Block candleCake) {
      return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)).with(ItemEntry.builder(candleCake)));
   }

   public static LootTable.Builder dropsNothing() {
      return LootTable.builder();
   }

   protected abstract void generate();

   public void accept(BiConsumer exporter) {
      this.generate();
      Set set = new HashSet();
      Iterator var3 = Registries.BLOCK.iterator();

      while(var3.hasNext()) {
         Block lv = (Block)var3.next();
         if (lv.isEnabled(this.requiredFeatures)) {
            Identifier lv2 = lv.getLootTableId();
            if (lv2 != LootTables.EMPTY && set.add(lv2)) {
               LootTable.Builder lv3 = (LootTable.Builder)this.lootTables.remove(lv2);
               if (lv3 == null) {
                  throw new IllegalStateException(String.format(Locale.ROOT, "Missing loottable '%s' for '%s'", lv2, Registries.BLOCK.getId(lv)));
               }

               exporter.accept(lv2, lv3);
            }
         }
      }

      if (!this.lootTables.isEmpty()) {
         throw new IllegalStateException("Created block loot tables for non-blocks: " + this.lootTables.keySet());
      }
   }

   protected void addVinePlantDrop(Block block, Block drop) {
      LootTable.Builder lv = dropsWithSilkTouchOrShears(block, ItemEntry.builder(block).conditionally(TableBonusLootCondition.builder(Enchantments.FORTUNE, 0.33F, 0.55F, 0.77F, 1.0F)));
      this.addDrop(block, lv);
      this.addDrop(drop, lv);
   }

   protected LootTable.Builder doorDrops(Block block) {
      return this.dropsWithProperty(block, DoorBlock.HALF, DoubleBlockHalf.LOWER);
   }

   protected void addPottedPlantDrops(Block block) {
      this.addDrop(block, (flowerPot) -> {
         return this.pottedPlantDrops(((FlowerPotBlock)flowerPot).getContent());
      });
   }

   protected void addDropWithSilkTouch(Block block, Block drop) {
      this.addDrop(block, dropsWithSilkTouch(drop));
   }

   protected void addDrop(Block block, ItemConvertible drop) {
      this.addDrop(block, this.drops(drop));
   }

   protected void addDropWithSilkTouch(Block block) {
      this.addDropWithSilkTouch(block, block);
   }

   protected void addDrop(Block block) {
      this.addDrop(block, (ItemConvertible)block);
   }

   protected void addDrop(Block block, Function lootTableFunction) {
      this.addDrop(block, (LootTable.Builder)lootTableFunction.apply(block));
   }

   protected void addDrop(Block block, LootTable.Builder lootTable) {
      this.lootTables.put(block.getLootTableId(), lootTable);
   }

   static {
      WITH_SILK_TOUCH = MatchToolLootCondition.builder(ItemPredicate.Builder.create().enchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, NumberRange.IntRange.atLeast(1))));
      WITHOUT_SILK_TOUCH = WITH_SILK_TOUCH.invert();
      WITH_SHEARS = MatchToolLootCondition.builder(ItemPredicate.Builder.create().items(Items.SHEARS));
      WITH_SILK_TOUCH_OR_SHEARS = WITH_SHEARS.or(WITH_SILK_TOUCH);
      WITHOUT_SILK_TOUCH_NOR_SHEARS = WITH_SILK_TOUCH_OR_SHEARS.invert();
      SAPLING_DROP_CHANCE = new float[]{0.05F, 0.0625F, 0.083333336F, 0.1F};
      LEAVES_STICK_DROP_CHANCE = new float[]{0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F};
   }
}
