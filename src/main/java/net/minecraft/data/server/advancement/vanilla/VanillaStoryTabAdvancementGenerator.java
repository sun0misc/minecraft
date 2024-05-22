/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.server.advancement.vanilla;

import java.util.function.Consumer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.criterion.ChangedDimensionCriterion;
import net.minecraft.advancement.criterion.CuredZombieVillagerCriterion;
import net.minecraft.advancement.criterion.EnchantedItemCriterion;
import net.minecraft.advancement.criterion.EntityHurtPlayerCriterion;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.advancement.AdvancementTabGenerator;
import net.minecraft.item.Items;
import net.minecraft.predicate.DamagePredicate;
import net.minecraft.predicate.TagPredicate;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureKeys;

public class VanillaStoryTabAdvancementGenerator
implements AdvancementTabGenerator {
    @Override
    public void accept(RegistryWrapper.WrapperLookup lookup, Consumer<AdvancementEntry> exporter) {
        AdvancementEntry lv = Advancement.Builder.create().display(Blocks.GRASS_BLOCK, (Text)Text.translatable("advancements.story.root.title"), (Text)Text.translatable("advancements.story.root.description"), Identifier.method_60656("textures/gui/advancements/backgrounds/stone.png"), AdvancementFrame.TASK, false, false, false).criterion("crafting_table", InventoryChangedCriterion.Conditions.items(Blocks.CRAFTING_TABLE)).build(exporter, "story/root");
        AdvancementEntry lv2 = Advancement.Builder.create().parent(lv).display(Items.WOODEN_PICKAXE, (Text)Text.translatable("advancements.story.mine_stone.title"), (Text)Text.translatable("advancements.story.mine_stone.description"), null, AdvancementFrame.TASK, true, true, false).criterion("get_stone", InventoryChangedCriterion.Conditions.items(ItemPredicate.Builder.create().tag(ItemTags.STONE_TOOL_MATERIALS))).build(exporter, "story/mine_stone");
        AdvancementEntry lv3 = Advancement.Builder.create().parent(lv2).display(Items.STONE_PICKAXE, (Text)Text.translatable("advancements.story.upgrade_tools.title"), (Text)Text.translatable("advancements.story.upgrade_tools.description"), null, AdvancementFrame.TASK, true, true, false).criterion("stone_pickaxe", InventoryChangedCriterion.Conditions.items(Items.STONE_PICKAXE)).build(exporter, "story/upgrade_tools");
        AdvancementEntry lv4 = Advancement.Builder.create().parent(lv3).display(Items.IRON_INGOT, (Text)Text.translatable("advancements.story.smelt_iron.title"), (Text)Text.translatable("advancements.story.smelt_iron.description"), null, AdvancementFrame.TASK, true, true, false).criterion("iron", InventoryChangedCriterion.Conditions.items(Items.IRON_INGOT)).build(exporter, "story/smelt_iron");
        AdvancementEntry lv5 = Advancement.Builder.create().parent(lv4).display(Items.IRON_PICKAXE, (Text)Text.translatable("advancements.story.iron_tools.title"), (Text)Text.translatable("advancements.story.iron_tools.description"), null, AdvancementFrame.TASK, true, true, false).criterion("iron_pickaxe", InventoryChangedCriterion.Conditions.items(Items.IRON_PICKAXE)).build(exporter, "story/iron_tools");
        AdvancementEntry lv6 = Advancement.Builder.create().parent(lv5).display(Items.DIAMOND, (Text)Text.translatable("advancements.story.mine_diamond.title"), (Text)Text.translatable("advancements.story.mine_diamond.description"), null, AdvancementFrame.TASK, true, true, false).criterion("diamond", InventoryChangedCriterion.Conditions.items(Items.DIAMOND)).build(exporter, "story/mine_diamond");
        AdvancementEntry lv7 = Advancement.Builder.create().parent(lv4).display(Items.LAVA_BUCKET, (Text)Text.translatable("advancements.story.lava_bucket.title"), (Text)Text.translatable("advancements.story.lava_bucket.description"), null, AdvancementFrame.TASK, true, true, false).criterion("lava_bucket", InventoryChangedCriterion.Conditions.items(Items.LAVA_BUCKET)).build(exporter, "story/lava_bucket");
        AdvancementEntry lv8 = Advancement.Builder.create().parent(lv4).display(Items.IRON_CHESTPLATE, (Text)Text.translatable("advancements.story.obtain_armor.title"), (Text)Text.translatable("advancements.story.obtain_armor.description"), null, AdvancementFrame.TASK, true, true, false).criteriaMerger(AdvancementRequirements.CriterionMerger.OR).criterion("iron_helmet", InventoryChangedCriterion.Conditions.items(Items.IRON_HELMET)).criterion("iron_chestplate", InventoryChangedCriterion.Conditions.items(Items.IRON_CHESTPLATE)).criterion("iron_leggings", InventoryChangedCriterion.Conditions.items(Items.IRON_LEGGINGS)).criterion("iron_boots", InventoryChangedCriterion.Conditions.items(Items.IRON_BOOTS)).build(exporter, "story/obtain_armor");
        Advancement.Builder.create().parent(lv6).display(Items.ENCHANTED_BOOK, (Text)Text.translatable("advancements.story.enchant_item.title"), (Text)Text.translatable("advancements.story.enchant_item.description"), null, AdvancementFrame.TASK, true, true, false).criterion("enchanted_item", EnchantedItemCriterion.Conditions.any()).build(exporter, "story/enchant_item");
        AdvancementEntry lv9 = Advancement.Builder.create().parent(lv7).display(Blocks.OBSIDIAN, (Text)Text.translatable("advancements.story.form_obsidian.title"), (Text)Text.translatable("advancements.story.form_obsidian.description"), null, AdvancementFrame.TASK, true, true, false).criterion("obsidian", InventoryChangedCriterion.Conditions.items(Blocks.OBSIDIAN)).build(exporter, "story/form_obsidian");
        Advancement.Builder.create().parent(lv8).display(Items.SHIELD, (Text)Text.translatable("advancements.story.deflect_arrow.title"), (Text)Text.translatable("advancements.story.deflect_arrow.description"), null, AdvancementFrame.TASK, true, true, false).criterion("deflected_projectile", EntityHurtPlayerCriterion.Conditions.create(DamagePredicate.Builder.create().type(DamageSourcePredicate.Builder.create().tag(TagPredicate.expected(DamageTypeTags.IS_PROJECTILE))).blocked(true))).build(exporter, "story/deflect_arrow");
        Advancement.Builder.create().parent(lv6).display(Items.DIAMOND_CHESTPLATE, (Text)Text.translatable("advancements.story.shiny_gear.title"), (Text)Text.translatable("advancements.story.shiny_gear.description"), null, AdvancementFrame.TASK, true, true, false).criteriaMerger(AdvancementRequirements.CriterionMerger.OR).criterion("diamond_helmet", InventoryChangedCriterion.Conditions.items(Items.DIAMOND_HELMET)).criterion("diamond_chestplate", InventoryChangedCriterion.Conditions.items(Items.DIAMOND_CHESTPLATE)).criterion("diamond_leggings", InventoryChangedCriterion.Conditions.items(Items.DIAMOND_LEGGINGS)).criterion("diamond_boots", InventoryChangedCriterion.Conditions.items(Items.DIAMOND_BOOTS)).build(exporter, "story/shiny_gear");
        AdvancementEntry lv10 = Advancement.Builder.create().parent(lv9).display(Items.FLINT_AND_STEEL, (Text)Text.translatable("advancements.story.enter_the_nether.title"), (Text)Text.translatable("advancements.story.enter_the_nether.description"), null, AdvancementFrame.TASK, true, true, false).criterion("entered_nether", ChangedDimensionCriterion.Conditions.to(World.NETHER)).build(exporter, "story/enter_the_nether");
        Advancement.Builder.create().parent(lv10).display(Items.GOLDEN_APPLE, (Text)Text.translatable("advancements.story.cure_zombie_villager.title"), (Text)Text.translatable("advancements.story.cure_zombie_villager.description"), null, AdvancementFrame.GOAL, true, true, false).criterion("cured_zombie", CuredZombieVillagerCriterion.Conditions.any()).build(exporter, "story/cure_zombie_villager");
        AdvancementEntry lv11 = Advancement.Builder.create().parent(lv10).display(Items.ENDER_EYE, (Text)Text.translatable("advancements.story.follow_ender_eye.title"), (Text)Text.translatable("advancements.story.follow_ender_eye.description"), null, AdvancementFrame.TASK, true, true, false).criterion("in_stronghold", TickCriterion.Conditions.createLocation(LocationPredicate.Builder.createStructure(lookup.getWrapperOrThrow(RegistryKeys.STRUCTURE).getOrThrow(StructureKeys.STRONGHOLD)))).build(exporter, "story/follow_ender_eye");
        Advancement.Builder.create().parent(lv11).display(Blocks.END_STONE, (Text)Text.translatable("advancements.story.enter_the_end.title"), (Text)Text.translatable("advancements.story.enter_the_end.description"), null, AdvancementFrame.TASK, true, true, false).criterion("entered_end", ChangedDimensionCriterion.Conditions.to(World.END)).build(exporter, "story/enter_the_end");
    }
}

