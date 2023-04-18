package net.minecraft.item;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SmithingTemplateItem extends Item {
   private static final Formatting TITLE_FORMATTING;
   private static final Formatting DESCRIPTION_FORMATTING;
   private static final String TRANSLATION_KEY;
   private static final Text INGREDIENTS_TEXT;
   private static final Text APPLIES_TO_TEXT;
   private static final Text NETHERITE_UPGRADE_TEXT;
   private static final Text ARMOR_TRIM_APPLIES_TO_TEXT;
   private static final Text ARMOR_TRIM_INGREDIENTS_TEXT;
   private static final Text ARMOR_TRIM_BASE_SLOT_DESCRIPTION_TEXT;
   private static final Text ARMOR_TRIM_ADDITIONS_SLOT_DESCRIPTION_TEXT;
   private static final Text NETHERITE_UPGRADE_APPLIES_TO_TEXT;
   private static final Text NETHERITE_UPGRADE_INGREDIENTS_TEXT;
   private static final Text NETHERITE_UPGRADE_BASE_SLOT_DESCRIPTION_TEXT;
   private static final Text NETHERITE_UPGRADE_ADDITIONS_SLOT_DESCRIPTION_TEXT;
   private static final Identifier EMPTY_ARMOR_SLOT_HELMET_TEXTURE;
   private static final Identifier EMPTY_ARMOR_SLOT_CHESTPLATE_TEXTURE;
   private static final Identifier EMPTY_ARMOR_SLOT_LEGGINGS_TEXTURE;
   private static final Identifier EMPTY_ARMOR_SLOT_BOOTS_TEXTURE;
   private static final Identifier EMPTY_SLOT_HOE_TEXTURE;
   private static final Identifier EMPTY_SLOT_AXE_TEXTURE;
   private static final Identifier EMPTY_SLOT_SWORD_TEXTURE;
   private static final Identifier EMPTY_SLOT_SHOVEL_TEXTURE;
   private static final Identifier EMPTY_SLOT_PICKAXE_TEXTURE;
   private static final Identifier EMPTY_SLOT_INGOT_TEXTURE;
   private static final Identifier EMPTY_SLOT_REDSTONE_DUST_TEXTURE;
   private static final Identifier EMPTY_SLOT_QUARTZ_TEXTURE;
   private static final Identifier EMPTY_SLOT_EMERALD_TEXTURE;
   private static final Identifier EMPTY_SLOT_DIAMOND_TEXTURE;
   private static final Identifier EMPTY_SLOT_LAPIS_LAZULI_TEXTURE;
   private static final Identifier EMPTY_SLOT_AMETHYST_SHARD_TEXTURE;
   private final Text appliesToText;
   private final Text ingredientsText;
   private final Text titleText;
   private final Text baseSlotDescriptionText;
   private final Text additionsSlotDescriptionText;
   private final List emptyBaseSlotTextures;
   private final List emptyAdditionsSlotTextures;

   public SmithingTemplateItem(Text appliesToText, Text ingredientsText, Text titleText, Text baseSlotDescriptionText, Text additionsSlotDescriptionText, List emptyBaseSlotTextures, List emptyAdditionsSlotTextures) {
      super(new Item.Settings());
      this.appliesToText = appliesToText;
      this.ingredientsText = ingredientsText;
      this.titleText = titleText;
      this.baseSlotDescriptionText = baseSlotDescriptionText;
      this.additionsSlotDescriptionText = additionsSlotDescriptionText;
      this.emptyBaseSlotTextures = emptyBaseSlotTextures;
      this.emptyAdditionsSlotTextures = emptyAdditionsSlotTextures;
   }

   public static SmithingTemplateItem of(RegistryKey trimPattern) {
      return of(trimPattern.getValue());
   }

   public static SmithingTemplateItem of(Identifier trimPatternIn) {
      return new SmithingTemplateItem(ARMOR_TRIM_APPLIES_TO_TEXT, ARMOR_TRIM_INGREDIENTS_TEXT, Text.translatable(Util.createTranslationKey("trim_pattern", trimPatternIn)).formatted(TITLE_FORMATTING), ARMOR_TRIM_BASE_SLOT_DESCRIPTION_TEXT, ARMOR_TRIM_ADDITIONS_SLOT_DESCRIPTION_TEXT, getArmorTrimEmptyBaseSlotTextures(), getArmorTrimEmptyAdditionsSlotTextures());
   }

   public static SmithingTemplateItem createNetheriteUpgrade() {
      return new SmithingTemplateItem(NETHERITE_UPGRADE_APPLIES_TO_TEXT, NETHERITE_UPGRADE_INGREDIENTS_TEXT, NETHERITE_UPGRADE_TEXT, NETHERITE_UPGRADE_BASE_SLOT_DESCRIPTION_TEXT, NETHERITE_UPGRADE_ADDITIONS_SLOT_DESCRIPTION_TEXT, getNetheriteUpgradeEmptyBaseSlotTextures(), getNetheriteUpgradeEmptyAdditionsSlotTextures());
   }

   private static List getArmorTrimEmptyBaseSlotTextures() {
      return List.of(EMPTY_ARMOR_SLOT_HELMET_TEXTURE, EMPTY_ARMOR_SLOT_CHESTPLATE_TEXTURE, EMPTY_ARMOR_SLOT_LEGGINGS_TEXTURE, EMPTY_ARMOR_SLOT_BOOTS_TEXTURE);
   }

   private static List getArmorTrimEmptyAdditionsSlotTextures() {
      return List.of(EMPTY_SLOT_INGOT_TEXTURE, EMPTY_SLOT_REDSTONE_DUST_TEXTURE, EMPTY_SLOT_LAPIS_LAZULI_TEXTURE, EMPTY_SLOT_QUARTZ_TEXTURE, EMPTY_SLOT_DIAMOND_TEXTURE, EMPTY_SLOT_EMERALD_TEXTURE, EMPTY_SLOT_AMETHYST_SHARD_TEXTURE);
   }

   private static List getNetheriteUpgradeEmptyBaseSlotTextures() {
      return List.of(EMPTY_ARMOR_SLOT_HELMET_TEXTURE, EMPTY_SLOT_SWORD_TEXTURE, EMPTY_ARMOR_SLOT_CHESTPLATE_TEXTURE, EMPTY_SLOT_PICKAXE_TEXTURE, EMPTY_ARMOR_SLOT_LEGGINGS_TEXTURE, EMPTY_SLOT_AXE_TEXTURE, EMPTY_ARMOR_SLOT_BOOTS_TEXTURE, EMPTY_SLOT_HOE_TEXTURE, EMPTY_SLOT_SHOVEL_TEXTURE);
   }

   private static List getNetheriteUpgradeEmptyAdditionsSlotTextures() {
      return List.of(EMPTY_SLOT_INGOT_TEXTURE);
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(this.titleText);
      tooltip.add(ScreenTexts.EMPTY);
      tooltip.add(APPLIES_TO_TEXT);
      tooltip.add(ScreenTexts.space().append(this.appliesToText));
      tooltip.add(INGREDIENTS_TEXT);
      tooltip.add(ScreenTexts.space().append(this.ingredientsText));
   }

   public Text getBaseSlotDescription() {
      return this.baseSlotDescriptionText;
   }

   public Text getAdditionsSlotDescription() {
      return this.additionsSlotDescriptionText;
   }

   public List getEmptyBaseSlotTextures() {
      return this.emptyBaseSlotTextures;
   }

   public List getEmptyAdditionsSlotTextures() {
      return this.emptyAdditionsSlotTextures;
   }

   public String getTranslationKey() {
      return TRANSLATION_KEY;
   }

   static {
      TITLE_FORMATTING = Formatting.GRAY;
      DESCRIPTION_FORMATTING = Formatting.BLUE;
      TRANSLATION_KEY = Util.createTranslationKey("item", new Identifier("smithing_template"));
      INGREDIENTS_TEXT = Text.translatable(Util.createTranslationKey("item", new Identifier("smithing_template.ingredients"))).formatted(TITLE_FORMATTING);
      APPLIES_TO_TEXT = Text.translatable(Util.createTranslationKey("item", new Identifier("smithing_template.applies_to"))).formatted(TITLE_FORMATTING);
      NETHERITE_UPGRADE_TEXT = Text.translatable(Util.createTranslationKey("upgrade", new Identifier("netherite_upgrade"))).formatted(TITLE_FORMATTING);
      ARMOR_TRIM_APPLIES_TO_TEXT = Text.translatable(Util.createTranslationKey("item", new Identifier("smithing_template.armor_trim.applies_to"))).formatted(DESCRIPTION_FORMATTING);
      ARMOR_TRIM_INGREDIENTS_TEXT = Text.translatable(Util.createTranslationKey("item", new Identifier("smithing_template.armor_trim.ingredients"))).formatted(DESCRIPTION_FORMATTING);
      ARMOR_TRIM_BASE_SLOT_DESCRIPTION_TEXT = Text.translatable(Util.createTranslationKey("item", new Identifier("smithing_template.armor_trim.base_slot_description")));
      ARMOR_TRIM_ADDITIONS_SLOT_DESCRIPTION_TEXT = Text.translatable(Util.createTranslationKey("item", new Identifier("smithing_template.armor_trim.additions_slot_description")));
      NETHERITE_UPGRADE_APPLIES_TO_TEXT = Text.translatable(Util.createTranslationKey("item", new Identifier("smithing_template.netherite_upgrade.applies_to"))).formatted(DESCRIPTION_FORMATTING);
      NETHERITE_UPGRADE_INGREDIENTS_TEXT = Text.translatable(Util.createTranslationKey("item", new Identifier("smithing_template.netherite_upgrade.ingredients"))).formatted(DESCRIPTION_FORMATTING);
      NETHERITE_UPGRADE_BASE_SLOT_DESCRIPTION_TEXT = Text.translatable(Util.createTranslationKey("item", new Identifier("smithing_template.netherite_upgrade.base_slot_description")));
      NETHERITE_UPGRADE_ADDITIONS_SLOT_DESCRIPTION_TEXT = Text.translatable(Util.createTranslationKey("item", new Identifier("smithing_template.netherite_upgrade.additions_slot_description")));
      EMPTY_ARMOR_SLOT_HELMET_TEXTURE = new Identifier("item/empty_armor_slot_helmet");
      EMPTY_ARMOR_SLOT_CHESTPLATE_TEXTURE = new Identifier("item/empty_armor_slot_chestplate");
      EMPTY_ARMOR_SLOT_LEGGINGS_TEXTURE = new Identifier("item/empty_armor_slot_leggings");
      EMPTY_ARMOR_SLOT_BOOTS_TEXTURE = new Identifier("item/empty_armor_slot_boots");
      EMPTY_SLOT_HOE_TEXTURE = new Identifier("item/empty_slot_hoe");
      EMPTY_SLOT_AXE_TEXTURE = new Identifier("item/empty_slot_axe");
      EMPTY_SLOT_SWORD_TEXTURE = new Identifier("item/empty_slot_sword");
      EMPTY_SLOT_SHOVEL_TEXTURE = new Identifier("item/empty_slot_shovel");
      EMPTY_SLOT_PICKAXE_TEXTURE = new Identifier("item/empty_slot_pickaxe");
      EMPTY_SLOT_INGOT_TEXTURE = new Identifier("item/empty_slot_ingot");
      EMPTY_SLOT_REDSTONE_DUST_TEXTURE = new Identifier("item/empty_slot_redstone_dust");
      EMPTY_SLOT_QUARTZ_TEXTURE = new Identifier("item/empty_slot_quartz");
      EMPTY_SLOT_EMERALD_TEXTURE = new Identifier("item/empty_slot_emerald");
      EMPTY_SLOT_DIAMOND_TEXTURE = new Identifier("item/empty_slot_diamond");
      EMPTY_SLOT_LAPIS_LAZULI_TEXTURE = new Identifier("item/empty_slot_lapis_lazuli");
      EMPTY_SLOT_AMETHYST_SHARD_TEXTURE = new Identifier("item/empty_slot_amethyst_shard");
   }
}
