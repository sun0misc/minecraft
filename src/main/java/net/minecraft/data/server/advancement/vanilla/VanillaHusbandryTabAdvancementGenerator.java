package net.minecraft.data.server.advancement.vanilla;

import com.google.common.collect.BiMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.BeeNestDestroyedCriterion;
import net.minecraft.advancement.criterion.BredAnimalsCriterion;
import net.minecraft.advancement.criterion.ConsumeItemCriterion;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.EffectsChangedCriterion;
import net.minecraft.advancement.criterion.FilledBucketCriterion;
import net.minecraft.advancement.criterion.FishingRodHookedCriterion;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.advancement.criterion.ItemCriterion;
import net.minecraft.advancement.criterion.PlacedBlockCriterion;
import net.minecraft.advancement.criterion.PlayerInteractedWithEntityCriterion;
import net.minecraft.advancement.criterion.StartedRidingCriterion;
import net.minecraft.advancement.criterion.TameAnimalCriterion;
import net.minecraft.advancement.criterion.ThrownItemPickedUpByEntityCriterion;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.advancement.AdvancementTabGenerator;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CatVariant;
import net.minecraft.entity.passive.FrogVariant;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.entity.TypeSpecificPredicate;
import net.minecraft.predicate.item.EnchantmentPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class VanillaHusbandryTabAdvancementGenerator implements AdvancementTabGenerator {
   public static final List BREEDABLE_ANIMALS;
   public static final List EGG_LAYING_ANIMALS;
   private static final Item[] FISH_ITEMS;
   private static final Item[] FISH_BUCKET_ITEMS;
   private static final Item[] FOOD_ITEMS;
   private static final Item[] AXE_ITEMS;

   public void accept(RegistryWrapper.WrapperLookup lookup, Consumer exporter) {
      Advancement lv = Advancement.Builder.create().display((ItemConvertible)Blocks.HAY_BLOCK, Text.translatable("advancements.husbandry.root.title"), Text.translatable("advancements.husbandry.root.description"), new Identifier("textures/gui/advancements/backgrounds/husbandry.png"), AdvancementFrame.TASK, false, false, false).criterion("consumed_item", (CriterionConditions)ConsumeItemCriterion.Conditions.any()).build(exporter, "husbandry/root");
      Advancement lv2 = Advancement.Builder.create().parent(lv).display((ItemConvertible)Items.WHEAT, Text.translatable("advancements.husbandry.plant_seed.title"), Text.translatable("advancements.husbandry.plant_seed.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criteriaMerger(CriterionMerger.OR).criterion("wheat", (CriterionConditions)PlacedBlockCriterion.Conditions.block(Blocks.WHEAT)).criterion("pumpkin_stem", (CriterionConditions)PlacedBlockCriterion.Conditions.block(Blocks.PUMPKIN_STEM)).criterion("melon_stem", (CriterionConditions)PlacedBlockCriterion.Conditions.block(Blocks.MELON_STEM)).criterion("beetroots", (CriterionConditions)PlacedBlockCriterion.Conditions.block(Blocks.BEETROOTS)).criterion("nether_wart", (CriterionConditions)PlacedBlockCriterion.Conditions.block(Blocks.NETHER_WART)).criterion("torchflower", (CriterionConditions)PlacedBlockCriterion.Conditions.block(Blocks.TORCHFLOWER_CROP)).criterion("pitcher_pod", (CriterionConditions)PlacedBlockCriterion.Conditions.block(Blocks.PITCHER_CROP)).build(exporter, "husbandry/plant_seed");
      Advancement lv3 = Advancement.Builder.create().parent(lv).display((ItemConvertible)Items.WHEAT, Text.translatable("advancements.husbandry.breed_an_animal.title"), Text.translatable("advancements.husbandry.breed_an_animal.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criteriaMerger(CriterionMerger.OR).criterion("bred", (CriterionConditions)BredAnimalsCriterion.Conditions.any()).build(exporter, "husbandry/breed_an_animal");
      createBreedAllAnimalsAdvancement(lv3, exporter, BREEDABLE_ANIMALS.stream(), EGG_LAYING_ANIMALS.stream());
      this.requireFoodItemsEaten(Advancement.Builder.create()).parent(lv2).display((ItemConvertible)Items.APPLE, Text.translatable("advancements.husbandry.balanced_diet.title"), Text.translatable("advancements.husbandry.balanced_diet.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(100)).build(exporter, "husbandry/balanced_diet");
      Advancement.Builder.create().parent(lv2).display((ItemConvertible)Items.NETHERITE_HOE, Text.translatable("advancements.husbandry.netherite_hoe.title"), Text.translatable("advancements.husbandry.netherite_hoe.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(100)).criterion("netherite_hoe", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Items.NETHERITE_HOE)).build(exporter, "husbandry/obtain_netherite_hoe");
      Advancement lv4 = Advancement.Builder.create().parent(lv).display((ItemConvertible)Items.LEAD, Text.translatable("advancements.husbandry.tame_an_animal.title"), Text.translatable("advancements.husbandry.tame_an_animal.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("tamed_animal", (CriterionConditions)TameAnimalCriterion.Conditions.any()).build(exporter, "husbandry/tame_an_animal");
      Advancement lv5 = this.requireListedFishCaught(Advancement.Builder.create()).parent(lv).criteriaMerger(CriterionMerger.OR).display((ItemConvertible)Items.FISHING_ROD, Text.translatable("advancements.husbandry.fishy_business.title"), Text.translatable("advancements.husbandry.fishy_business.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).build(exporter, "husbandry/fishy_business");
      Advancement lv6 = this.requireListedFishBucketsFilled(Advancement.Builder.create()).parent(lv5).criteriaMerger(CriterionMerger.OR).display((ItemConvertible)Items.PUFFERFISH_BUCKET, Text.translatable("advancements.husbandry.tactical_fishing.title"), Text.translatable("advancements.husbandry.tactical_fishing.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).build(exporter, "husbandry/tactical_fishing");
      Advancement lv7 = Advancement.Builder.create().parent(lv6).criteriaMerger(CriterionMerger.OR).criterion(Registries.ITEM.getId(Items.AXOLOTL_BUCKET).getPath(), (CriterionConditions)FilledBucketCriterion.Conditions.create(ItemPredicate.Builder.create().items(Items.AXOLOTL_BUCKET).build())).display((ItemConvertible)Items.AXOLOTL_BUCKET, Text.translatable("advancements.husbandry.axolotl_in_a_bucket.title"), Text.translatable("advancements.husbandry.axolotl_in_a_bucket.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).build(exporter, "husbandry/axolotl_in_a_bucket");
      Advancement.Builder.create().parent(lv7).criterion("kill_axolotl_target", (CriterionConditions)EffectsChangedCriterion.Conditions.create(EntityPredicate.Builder.create().type(EntityType.AXOLOTL).build())).display((ItemConvertible)Items.TROPICAL_FISH_BUCKET, Text.translatable("advancements.husbandry.kill_axolotl_target.title"), Text.translatable("advancements.husbandry.kill_axolotl_target.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).build(exporter, "husbandry/kill_axolotl_target");
      this.requireAllCatsTamed(Advancement.Builder.create()).parent(lv4).display((ItemConvertible)Items.COD, Text.translatable("advancements.husbandry.complete_catalogue.title"), Text.translatable("advancements.husbandry.complete_catalogue.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).build(exporter, "husbandry/complete_catalogue");
      Advancement lv8 = Advancement.Builder.create().parent(lv).criterion("safely_harvest_honey", (CriterionConditions)ItemCriterion.Conditions.create(LocationPredicate.Builder.create().block(BlockPredicate.Builder.create().tag(BlockTags.BEEHIVES).build()).smokey(true), ItemPredicate.Builder.create().items(Items.GLASS_BOTTLE))).display((ItemConvertible)Items.HONEY_BOTTLE, Text.translatable("advancements.husbandry.safely_harvest_honey.title"), Text.translatable("advancements.husbandry.safely_harvest_honey.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).build(exporter, "husbandry/safely_harvest_honey");
      Advancement lv9 = Advancement.Builder.create().parent(lv8).display((ItemConvertible)Items.HONEYCOMB, Text.translatable("advancements.husbandry.wax_on.title"), Text.translatable("advancements.husbandry.wax_on.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("wax_on", (CriterionConditions)ItemCriterion.Conditions.create(LocationPredicate.Builder.create().block(BlockPredicate.Builder.create().blocks((Iterable)((BiMap)HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get()).keySet()).build()), ItemPredicate.Builder.create().items(Items.HONEYCOMB))).build(exporter, "husbandry/wax_on");
      Advancement.Builder.create().parent(lv9).display((ItemConvertible)Items.STONE_AXE, Text.translatable("advancements.husbandry.wax_off.title"), Text.translatable("advancements.husbandry.wax_off.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("wax_off", (CriterionConditions)ItemCriterion.Conditions.create(LocationPredicate.Builder.create().block(BlockPredicate.Builder.create().blocks((Iterable)((BiMap)HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get()).keySet()).build()), ItemPredicate.Builder.create().items(AXE_ITEMS))).build(exporter, "husbandry/wax_off");
      Advancement lv10 = Advancement.Builder.create().parent(lv).criterion(Registries.ITEM.getId(Items.TADPOLE_BUCKET).getPath(), (CriterionConditions)FilledBucketCriterion.Conditions.create(ItemPredicate.Builder.create().items(Items.TADPOLE_BUCKET).build())).display((ItemConvertible)Items.TADPOLE_BUCKET, Text.translatable("advancements.husbandry.tadpole_in_a_bucket.title"), Text.translatable("advancements.husbandry.tadpole_in_a_bucket.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).build(exporter, "husbandry/tadpole_in_a_bucket");
      Advancement lv11 = this.requireAllFrogsOnLeads(Advancement.Builder.create()).parent(lv10).display((ItemConvertible)Items.LEAD, Text.translatable("advancements.husbandry.leash_all_frog_variants.title"), Text.translatable("advancements.husbandry.leash_all_frog_variants.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).build(exporter, "husbandry/leash_all_frog_variants");
      Advancement.Builder.create().parent(lv11).display((ItemConvertible)Items.VERDANT_FROGLIGHT, Text.translatable("advancements.husbandry.froglights.title"), Text.translatable("advancements.husbandry.froglights.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).criterion("froglights", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Items.OCHRE_FROGLIGHT, Items.PEARLESCENT_FROGLIGHT, Items.VERDANT_FROGLIGHT)).build(exporter, "husbandry/froglights");
      Advancement.Builder.create().parent(lv).criterion("silk_touch_nest", (CriterionConditions)BeeNestDestroyedCriterion.Conditions.create(Blocks.BEE_NEST, ItemPredicate.Builder.create().enchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, NumberRange.IntRange.atLeast(1))), NumberRange.IntRange.exactly(3))).display((ItemConvertible)Blocks.BEE_NEST, Text.translatable("advancements.husbandry.silk_touch_nest.title"), Text.translatable("advancements.husbandry.silk_touch_nest.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).build(exporter, "husbandry/silk_touch_nest");
      Advancement.Builder.create().parent(lv).display((ItemConvertible)Items.OAK_BOAT, Text.translatable("advancements.husbandry.ride_a_boat_with_a_goat.title"), Text.translatable("advancements.husbandry.ride_a_boat_with_a_goat.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("ride_a_boat_with_a_goat", (CriterionConditions)StartedRidingCriterion.Conditions.create(EntityPredicate.Builder.create().vehicle(EntityPredicate.Builder.create().type(EntityType.BOAT).passenger(EntityPredicate.Builder.create().type(EntityType.GOAT).build()).build()))).build(exporter, "husbandry/ride_a_boat_with_a_goat");
      Advancement.Builder.create().parent(lv).display((ItemConvertible)Items.GLOW_INK_SAC, Text.translatable("advancements.husbandry.make_a_sign_glow.title"), Text.translatable("advancements.husbandry.make_a_sign_glow.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("make_a_sign_glow", (CriterionConditions)ItemCriterion.Conditions.create(LocationPredicate.Builder.create().block(BlockPredicate.Builder.create().tag(BlockTags.ALL_SIGNS).build()), ItemPredicate.Builder.create().items(Items.GLOW_INK_SAC))).build(exporter, "husbandry/make_a_sign_glow");
      Advancement lv12 = Advancement.Builder.create().parent(lv).display((ItemConvertible)Items.COOKIE, Text.translatable("advancements.husbandry.allay_deliver_item_to_player.title"), Text.translatable("advancements.husbandry.allay_deliver_item_to_player.description"), (Identifier)null, AdvancementFrame.TASK, true, true, true).criterion("allay_deliver_item_to_player", (CriterionConditions)ThrownItemPickedUpByEntityCriterion.Conditions.createThrownItemPickedUpByPlayer(EntityPredicate.Extended.EMPTY, ItemPredicate.ANY, EntityPredicate.Extended.ofLegacy(EntityPredicate.Builder.create().type(EntityType.ALLAY).build()))).build(exporter, "husbandry/allay_deliver_item_to_player");
      Advancement.Builder.create().parent(lv12).display((ItemConvertible)Items.NOTE_BLOCK, Text.translatable("advancements.husbandry.allay_deliver_cake_to_note_block.title"), Text.translatable("advancements.husbandry.allay_deliver_cake_to_note_block.description"), (Identifier)null, AdvancementFrame.TASK, true, true, true).criterion("allay_deliver_cake_to_note_block", (CriterionConditions)ItemCriterion.Conditions.createAllayDropItemOnBlock(LocationPredicate.Builder.create().block(BlockPredicate.Builder.create().blocks(Blocks.NOTE_BLOCK).build()), ItemPredicate.Builder.create().items(Items.CAKE))).build(exporter, "husbandry/allay_deliver_cake_to_note_block");
   }

   public static Advancement createBreedAllAnimalsAdvancement(Advancement parent, Consumer exporter, Stream breedableAnimals, Stream eggLayingAnimals) {
      return requireListedAnimalsBred(Advancement.Builder.create(), breedableAnimals, eggLayingAnimals).parent(parent).display((ItemConvertible)Items.GOLDEN_CARROT, Text.translatable("advancements.husbandry.breed_all_animals.title"), Text.translatable("advancements.husbandry.breed_all_animals.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(100)).build(exporter, "husbandry/bred_all_animals");
   }

   private Advancement.Builder requireAllFrogsOnLeads(Advancement.Builder builder) {
      Registries.FROG_VARIANT.streamEntries().forEach((variant) -> {
         builder.criterion(variant.registryKey().getValue().toString(), (CriterionConditions)PlayerInteractedWithEntityCriterion.Conditions.create(ItemPredicate.Builder.create().items(Items.LEAD), EntityPredicate.Extended.ofLegacy(EntityPredicate.Builder.create().type(EntityType.FROG).typeSpecific(TypeSpecificPredicate.frog((FrogVariant)variant.value())).build())));
      });
      return builder;
   }

   private Advancement.Builder requireFoodItemsEaten(Advancement.Builder builder) {
      Item[] var2 = FOOD_ITEMS;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Item lv = var2[var4];
         builder.criterion(Registries.ITEM.getId(lv).getPath(), (CriterionConditions)ConsumeItemCriterion.Conditions.item(lv));
      }

      return builder;
   }

   private static Advancement.Builder requireListedAnimalsBred(Advancement.Builder advancementBuilder, Stream breedableAnimals, Stream eggLayingAnimals) {
      breedableAnimals.forEach((type) -> {
         advancementBuilder.criterion(EntityType.getId(type).toString(), (CriterionConditions)BredAnimalsCriterion.Conditions.create(EntityPredicate.Builder.create().type(type)));
      });
      eggLayingAnimals.forEach((type) -> {
         advancementBuilder.criterion(EntityType.getId(type).toString(), (CriterionConditions)BredAnimalsCriterion.Conditions.create(EntityPredicate.Builder.create().type(type).build(), EntityPredicate.Builder.create().type(type).build(), EntityPredicate.ANY));
      });
      return advancementBuilder;
   }

   private Advancement.Builder requireListedFishBucketsFilled(Advancement.Builder builder) {
      Item[] var2 = FISH_BUCKET_ITEMS;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Item lv = var2[var4];
         builder.criterion(Registries.ITEM.getId(lv).getPath(), (CriterionConditions)FilledBucketCriterion.Conditions.create(ItemPredicate.Builder.create().items(lv).build()));
      }

      return builder;
   }

   private Advancement.Builder requireListedFishCaught(Advancement.Builder builder) {
      Item[] var2 = FISH_ITEMS;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Item lv = var2[var4];
         builder.criterion(Registries.ITEM.getId(lv).getPath(), (CriterionConditions)FishingRodHookedCriterion.Conditions.create(ItemPredicate.ANY, EntityPredicate.ANY, ItemPredicate.Builder.create().items(lv).build()));
      }

      return builder;
   }

   private Advancement.Builder requireAllCatsTamed(Advancement.Builder builder) {
      Registries.CAT_VARIANT.getEntrySet().stream().sorted(Entry.comparingByKey(Comparator.comparing(RegistryKey::getValue))).forEach((entry) -> {
         builder.criterion(((RegistryKey)entry.getKey()).getValue().toString(), (CriterionConditions)TameAnimalCriterion.Conditions.create(EntityPredicate.Builder.create().typeSpecific(TypeSpecificPredicate.cat((CatVariant)entry.getValue())).build()));
      });
      return builder;
   }

   static {
      BREEDABLE_ANIMALS = List.of(EntityType.HORSE, EntityType.DONKEY, EntityType.MULE, EntityType.SHEEP, EntityType.COW, EntityType.MOOSHROOM, EntityType.PIG, EntityType.CHICKEN, EntityType.WOLF, EntityType.OCELOT, EntityType.RABBIT, EntityType.LLAMA, EntityType.CAT, EntityType.PANDA, EntityType.FOX, EntityType.BEE, EntityType.HOGLIN, EntityType.STRIDER, EntityType.GOAT, EntityType.AXOLOTL, EntityType.CAMEL);
      EGG_LAYING_ANIMALS = List.of(EntityType.TURTLE, EntityType.FROG, EntityType.SNIFFER);
      FISH_ITEMS = new Item[]{Items.COD, Items.TROPICAL_FISH, Items.PUFFERFISH, Items.SALMON};
      FISH_BUCKET_ITEMS = new Item[]{Items.COD_BUCKET, Items.TROPICAL_FISH_BUCKET, Items.PUFFERFISH_BUCKET, Items.SALMON_BUCKET};
      FOOD_ITEMS = new Item[]{Items.APPLE, Items.MUSHROOM_STEW, Items.BREAD, Items.PORKCHOP, Items.COOKED_PORKCHOP, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH, Items.COOKED_COD, Items.COOKED_SALMON, Items.COOKIE, Items.MELON_SLICE, Items.BEEF, Items.COOKED_BEEF, Items.CHICKEN, Items.COOKED_CHICKEN, Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.CARROT, Items.POTATO, Items.BAKED_POTATO, Items.POISONOUS_POTATO, Items.GOLDEN_CARROT, Items.PUMPKIN_PIE, Items.RABBIT, Items.COOKED_RABBIT, Items.RABBIT_STEW, Items.MUTTON, Items.COOKED_MUTTON, Items.CHORUS_FRUIT, Items.BEETROOT, Items.BEETROOT_SOUP, Items.DRIED_KELP, Items.SUSPICIOUS_STEW, Items.SWEET_BERRIES, Items.HONEY_BOTTLE, Items.GLOW_BERRIES};
      AXE_ITEMS = new Item[]{Items.WOODEN_AXE, Items.GOLDEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE};
   }
}
