package net.minecraft.data.server.advancement.vanilla;

import java.util.function.Consumer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.ChangedDimensionCriterion;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.CuredZombieVillagerCriterion;
import net.minecraft.advancement.criterion.EnchantedItemCriterion;
import net.minecraft.advancement.criterion.EntityHurtPlayerCriterion;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.advancement.AdvancementTabGenerator;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.predicate.DamagePredicate;
import net.minecraft.predicate.TagPredicate;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureKeys;

public class VanillaStoryTabAdvancementGenerator implements AdvancementTabGenerator {
   public void accept(RegistryWrapper.WrapperLookup lookup, Consumer exporter) {
      Advancement lv = Advancement.Builder.create().display((ItemConvertible)Blocks.GRASS_BLOCK, Text.translatable("advancements.story.root.title"), Text.translatable("advancements.story.root.description"), new Identifier("textures/gui/advancements/backgrounds/stone.png"), AdvancementFrame.TASK, false, false, false).criterion("crafting_table", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Blocks.CRAFTING_TABLE)).build(exporter, "story/root");
      Advancement lv2 = Advancement.Builder.create().parent(lv).display((ItemConvertible)Items.WOODEN_PICKAXE, Text.translatable("advancements.story.mine_stone.title"), Text.translatable("advancements.story.mine_stone.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("get_stone", (CriterionConditions)InventoryChangedCriterion.Conditions.items(ItemPredicate.Builder.create().tag(ItemTags.STONE_TOOL_MATERIALS).build())).build(exporter, "story/mine_stone");
      Advancement lv3 = Advancement.Builder.create().parent(lv2).display((ItemConvertible)Items.STONE_PICKAXE, Text.translatable("advancements.story.upgrade_tools.title"), Text.translatable("advancements.story.upgrade_tools.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("stone_pickaxe", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Items.STONE_PICKAXE)).build(exporter, "story/upgrade_tools");
      Advancement lv4 = Advancement.Builder.create().parent(lv3).display((ItemConvertible)Items.IRON_INGOT, Text.translatable("advancements.story.smelt_iron.title"), Text.translatable("advancements.story.smelt_iron.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("iron", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Items.IRON_INGOT)).build(exporter, "story/smelt_iron");
      Advancement lv5 = Advancement.Builder.create().parent(lv4).display((ItemConvertible)Items.IRON_PICKAXE, Text.translatable("advancements.story.iron_tools.title"), Text.translatable("advancements.story.iron_tools.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("iron_pickaxe", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Items.IRON_PICKAXE)).build(exporter, "story/iron_tools");
      Advancement lv6 = Advancement.Builder.create().parent(lv5).display((ItemConvertible)Items.DIAMOND, Text.translatable("advancements.story.mine_diamond.title"), Text.translatable("advancements.story.mine_diamond.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("diamond", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Items.DIAMOND)).build(exporter, "story/mine_diamond");
      Advancement lv7 = Advancement.Builder.create().parent(lv4).display((ItemConvertible)Items.LAVA_BUCKET, Text.translatable("advancements.story.lava_bucket.title"), Text.translatable("advancements.story.lava_bucket.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("lava_bucket", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Items.LAVA_BUCKET)).build(exporter, "story/lava_bucket");
      Advancement lv8 = Advancement.Builder.create().parent(lv4).display((ItemConvertible)Items.IRON_CHESTPLATE, Text.translatable("advancements.story.obtain_armor.title"), Text.translatable("advancements.story.obtain_armor.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criteriaMerger(CriterionMerger.OR).criterion("iron_helmet", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Items.IRON_HELMET)).criterion("iron_chestplate", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Items.IRON_CHESTPLATE)).criterion("iron_leggings", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Items.IRON_LEGGINGS)).criterion("iron_boots", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Items.IRON_BOOTS)).build(exporter, "story/obtain_armor");
      Advancement.Builder.create().parent(lv6).display((ItemConvertible)Items.ENCHANTED_BOOK, Text.translatable("advancements.story.enchant_item.title"), Text.translatable("advancements.story.enchant_item.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("enchanted_item", (CriterionConditions)EnchantedItemCriterion.Conditions.any()).build(exporter, "story/enchant_item");
      Advancement lv9 = Advancement.Builder.create().parent(lv7).display((ItemConvertible)Blocks.OBSIDIAN, Text.translatable("advancements.story.form_obsidian.title"), Text.translatable("advancements.story.form_obsidian.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("obsidian", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Blocks.OBSIDIAN)).build(exporter, "story/form_obsidian");
      Advancement.Builder.create().parent(lv8).display((ItemConvertible)Items.SHIELD, Text.translatable("advancements.story.deflect_arrow.title"), Text.translatable("advancements.story.deflect_arrow.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("deflected_projectile", (CriterionConditions)EntityHurtPlayerCriterion.Conditions.create(DamagePredicate.Builder.create().type(DamageSourcePredicate.Builder.create().tag(TagPredicate.expected(DamageTypeTags.IS_PROJECTILE))).blocked(true))).build(exporter, "story/deflect_arrow");
      Advancement.Builder.create().parent(lv6).display((ItemConvertible)Items.DIAMOND_CHESTPLATE, Text.translatable("advancements.story.shiny_gear.title"), Text.translatable("advancements.story.shiny_gear.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criteriaMerger(CriterionMerger.OR).criterion("diamond_helmet", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Items.DIAMOND_HELMET)).criterion("diamond_chestplate", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Items.DIAMOND_CHESTPLATE)).criterion("diamond_leggings", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Items.DIAMOND_LEGGINGS)).criterion("diamond_boots", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Items.DIAMOND_BOOTS)).build(exporter, "story/shiny_gear");
      Advancement lv10 = Advancement.Builder.create().parent(lv9).display((ItemConvertible)Items.FLINT_AND_STEEL, Text.translatable("advancements.story.enter_the_nether.title"), Text.translatable("advancements.story.enter_the_nether.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("entered_nether", (CriterionConditions)ChangedDimensionCriterion.Conditions.to(World.NETHER)).build(exporter, "story/enter_the_nether");
      Advancement.Builder.create().parent(lv10).display((ItemConvertible)Items.GOLDEN_APPLE, Text.translatable("advancements.story.cure_zombie_villager.title"), Text.translatable("advancements.story.cure_zombie_villager.description"), (Identifier)null, AdvancementFrame.GOAL, true, true, false).criterion("cured_zombie", (CriterionConditions)CuredZombieVillagerCriterion.Conditions.any()).build(exporter, "story/cure_zombie_villager");
      Advancement lv11 = Advancement.Builder.create().parent(lv10).display((ItemConvertible)Items.ENDER_EYE, Text.translatable("advancements.story.follow_ender_eye.title"), Text.translatable("advancements.story.follow_ender_eye.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("in_stronghold", (CriterionConditions)TickCriterion.Conditions.createLocation(LocationPredicate.feature(StructureKeys.STRONGHOLD))).build(exporter, "story/follow_ender_eye");
      Advancement.Builder.create().parent(lv11).display((ItemConvertible)Blocks.END_STONE, Text.translatable("advancements.story.enter_the_end.title"), Text.translatable("advancements.story.enter_the_end.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("entered_end", (CriterionConditions)ChangedDimensionCriterion.Conditions.to(World.END)).build(exporter, "story/enter_the_end");
   }
}
