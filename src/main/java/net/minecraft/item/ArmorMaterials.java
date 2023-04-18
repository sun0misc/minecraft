package net.minecraft.item;

import java.util.EnumMap;
import java.util.function.Supplier;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Lazy;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;

public enum ArmorMaterials implements StringIdentifiable, ArmorMaterial {
   LEATHER("leather", 5, (EnumMap)Util.make(new EnumMap(ArmorItem.Type.class), (map) -> {
      map.put(ArmorItem.Type.BOOTS, 1);
      map.put(ArmorItem.Type.LEGGINGS, 2);
      map.put(ArmorItem.Type.CHESTPLATE, 3);
      map.put(ArmorItem.Type.HELMET, 1);
   }), 15, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> {
      return Ingredient.ofItems(Items.LEATHER);
   }),
   CHAIN("chainmail", 15, (EnumMap)Util.make(new EnumMap(ArmorItem.Type.class), (map) -> {
      map.put(ArmorItem.Type.BOOTS, 1);
      map.put(ArmorItem.Type.LEGGINGS, 4);
      map.put(ArmorItem.Type.CHESTPLATE, 5);
      map.put(ArmorItem.Type.HELMET, 2);
   }), 12, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 0.0F, 0.0F, () -> {
      return Ingredient.ofItems(Items.IRON_INGOT);
   }),
   IRON("iron", 15, (EnumMap)Util.make(new EnumMap(ArmorItem.Type.class), (map) -> {
      map.put(ArmorItem.Type.BOOTS, 2);
      map.put(ArmorItem.Type.LEGGINGS, 5);
      map.put(ArmorItem.Type.CHESTPLATE, 6);
      map.put(ArmorItem.Type.HELMET, 2);
   }), 9, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 0.0F, 0.0F, () -> {
      return Ingredient.ofItems(Items.IRON_INGOT);
   }),
   GOLD("gold", 7, (EnumMap)Util.make(new EnumMap(ArmorItem.Type.class), (map) -> {
      map.put(ArmorItem.Type.BOOTS, 1);
      map.put(ArmorItem.Type.LEGGINGS, 3);
      map.put(ArmorItem.Type.CHESTPLATE, 5);
      map.put(ArmorItem.Type.HELMET, 2);
   }), 25, SoundEvents.ITEM_ARMOR_EQUIP_GOLD, 0.0F, 0.0F, () -> {
      return Ingredient.ofItems(Items.GOLD_INGOT);
   }),
   DIAMOND("diamond", 33, (EnumMap)Util.make(new EnumMap(ArmorItem.Type.class), (map) -> {
      map.put(ArmorItem.Type.BOOTS, 3);
      map.put(ArmorItem.Type.LEGGINGS, 6);
      map.put(ArmorItem.Type.CHESTPLATE, 8);
      map.put(ArmorItem.Type.HELMET, 3);
   }), 10, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F, () -> {
      return Ingredient.ofItems(Items.DIAMOND);
   }),
   TURTLE("turtle", 25, (EnumMap)Util.make(new EnumMap(ArmorItem.Type.class), (map) -> {
      map.put(ArmorItem.Type.BOOTS, 2);
      map.put(ArmorItem.Type.LEGGINGS, 5);
      map.put(ArmorItem.Type.CHESTPLATE, 6);
      map.put(ArmorItem.Type.HELMET, 2);
   }), 9, SoundEvents.ITEM_ARMOR_EQUIP_TURTLE, 0.0F, 0.0F, () -> {
      return Ingredient.ofItems(Items.SCUTE);
   }),
   NETHERITE("netherite", 37, (EnumMap)Util.make(new EnumMap(ArmorItem.Type.class), (map) -> {
      map.put(ArmorItem.Type.BOOTS, 3);
      map.put(ArmorItem.Type.LEGGINGS, 6);
      map.put(ArmorItem.Type.CHESTPLATE, 8);
      map.put(ArmorItem.Type.HELMET, 3);
   }), 15, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, () -> {
      return Ingredient.ofItems(Items.NETHERITE_INGOT);
   });

   public static final StringIdentifiable.Codec CODEC = StringIdentifiable.createCodec(ArmorMaterials::values);
   private static final EnumMap BASE_DURABILITY = (EnumMap)Util.make(new EnumMap(ArmorItem.Type.class), (map) -> {
      map.put(ArmorItem.Type.BOOTS, 13);
      map.put(ArmorItem.Type.LEGGINGS, 15);
      map.put(ArmorItem.Type.CHESTPLATE, 16);
      map.put(ArmorItem.Type.HELMET, 11);
   });
   private final String name;
   private final int durabilityMultiplier;
   private final EnumMap protectionAmounts;
   private final int enchantability;
   private final SoundEvent equipSound;
   private final float toughness;
   private final float knockbackResistance;
   private final Lazy repairIngredientSupplier;

   private ArmorMaterials(String name, int durabilityMultiplier, EnumMap protectionAmounts, int enchantability, SoundEvent equipSound, float toughness, float knockbackResistance, Supplier repairIngredientSupplier) {
      this.name = name;
      this.durabilityMultiplier = durabilityMultiplier;
      this.protectionAmounts = protectionAmounts;
      this.enchantability = enchantability;
      this.equipSound = equipSound;
      this.toughness = toughness;
      this.knockbackResistance = knockbackResistance;
      this.repairIngredientSupplier = new Lazy(repairIngredientSupplier);
   }

   public int getDurability(ArmorItem.Type type) {
      return (Integer)BASE_DURABILITY.get(type) * this.durabilityMultiplier;
   }

   public int getProtection(ArmorItem.Type type) {
      return (Integer)this.protectionAmounts.get(type);
   }

   public int getEnchantability() {
      return this.enchantability;
   }

   public SoundEvent getEquipSound() {
      return this.equipSound;
   }

   public Ingredient getRepairIngredient() {
      return (Ingredient)this.repairIngredientSupplier.get();
   }

   public String getName() {
      return this.name;
   }

   public float getToughness() {
      return this.toughness;
   }

   public float getKnockbackResistance() {
      return this.knockbackResistance;
   }

   public String asString() {
      return this.name;
   }

   // $FF: synthetic method
   private static ArmorMaterials[] method_36675() {
      return new ArmorMaterials[]{LEATHER, CHAIN, IRON, GOLD, DIAMOND, TURTLE, NETHERITE};
   }
}
